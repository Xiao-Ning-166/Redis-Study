package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.SeckillVoucher;
import com.example.entity.VoucherOrder;
import com.example.mapper.VoucherOrderMapper;
import com.example.service.ISeckillVoucherService;
import com.example.service.IVoucherOrderService;
import com.example.utils.RedisConstants;
import com.example.utils.RedisIdWorker;
import com.example.utils.SimpleRedisLock;
import com.example.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaoning
 * @date 2022/10/14
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 判断有无购买资格的脚本
     */
    private static final DefaultRedisScript SECKILL_SCRIPT;

    /**
     * 初始化释放锁的脚本
     */
    static {
        SECKILL_SCRIPT = new DefaultRedisScript();
        // 从类路径加载释放锁的脚本
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        // 设置返回值类型
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀下单的阻塞队列
     */
    private BlockingQueue<VoucherOrder> seckillTasks = new ArrayBlockingQueue<>(1024 * 1024);

    private static final ExecutorService SECKILL_TASK_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        /* // 阻塞队列的方式
        SECKILL_TASK_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // 1、从阻塞队列中获取订单信息
                        VoucherOrder voucherOrder = seckillTasks.take();
                        // 2、创建订单
                        createVoucherOrderWithBlockingQueue(voucherOrder);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        */
        SECKILL_TASK_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                String queueName = "stream.orders";
                while (true) {
                    try {
                        // 1、从Redis消息队列中获取消息
                        List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                                Consumer.from("g1", "c1"),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                                StreamOffset.create(queueName, ReadOffset.lastConsumed())
                        );
                        // 2、判断消息是否为空
                        if (CollectionUtil.isEmpty(list)) {
                            // 为空，说明没有消息，继续下一次循环
                            continue;
                        }
                        // 3、有消息，解析消息
                        MapRecord<String, Object, Object> record = list.get(0);
                        Map<Object, Object> recordValue = record.getValue();
                        VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(recordValue, new VoucherOrder(), false);
                        // 4、创建订单
                        createVoucherOrderWithStream(voucherOrder);
                        // 5、确认消息
                        stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                    } catch (Exception e) {
                        // 记录异常
                        log.error("创建订单异常", e);
                        // 处理pending-list中的消息
                        handlePendingList();
                    }
                }
            }
        });
    }

    private void handlePendingList() {
        String queueName = "stream.orders";
        while (true) {
            try {
                // 1、从Redis消息队列中获取消息
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(queueName, ReadOffset.from("0"))
                );
                // 2、判断消息是否为空
                if (CollectionUtil.isEmpty(list)) {
                    // 为空，说明pending-list中没有已消费但未确认的消息，跳出循环
                    break;
                }
                // 3、有消息，解析消息
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> recordValue = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(recordValue, new VoucherOrder(), false);
                // 4、创建订单
                createVoucherOrderWithStream(voucherOrder);
                // 5、确认消息
                stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
            } catch (Exception e) {
                // 记录异常
                log.error("创建订单异常", e);
            }
        }
    }

    /**
     * 抢购秒杀优惠券
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucherAsync(Long voucherId) {
        UserDTO user = UserHolder.getUser();
        String stockPrefix = RedisConstants.SECKILL_STOCK_PREFIX;
        String orderPrefix = RedisConstants.SECKILL_ORDER_PREFIX;
        // 生成订单id
        long voucherOrderId = redisIdWorker.getId("order");
        // 1、执行lua脚本判断用户是否有购买资格
        Long seckillRes = (Long) stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Arrays.asList(stockPrefix, orderPrefix),
                voucherId.toString(), user.getId().toString(), String.valueOf(voucherOrderId)
        );
        // 拆箱。避免出现空指针
        long r = seckillRes.longValue();
        if (r != 0) {
            // 没有购买资格，返回错误信息
            return Result.fail(r == 1 ? "库存不足！" : "每人限购一单！");
        }

        // 2、有购买资格，生成订单id，保存队列中
        /*
        VoucherOrder voucherOrder = new VoucherOrder();
        long voucherOrderId = redisIdWorker.getId("order");
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(user.getId());
        voucherOrder.setId(voucherOrderId);
        // 使用阻塞队列进行异步下单
        seckillTasks.add(voucherOrder);
        */

        // 3、返回订单信息
        return Result.ok(voucherOrderId);
    }

    /**
     * 基于Redis的Stream消息队列创建订单
     *
     * @param voucherOrder
     */
    private void createVoucherOrderWithStream(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // 创建锁对象
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_PREFIX + "order:" + userId);
        // 获取锁
        boolean isLock = false;
        try {
            isLock = lock.tryLock(1L, 10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isLock) {
            // 获取锁失败，返回错误信息
            log.error("每人限购一单！");
            return ;
        }

        try {
            // 1、一人一单
            // 1.1、根据用户id、优惠券id查询订单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                // 已经存在
                log.error("每人限购一单！");
                return ;
            }

            // 2、扣库存
            boolean isSuccess = seckillVoucherService.update()
                    // 扣减库存
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    // 判断库存是否大于0
                    .gt("stock", 0).update();
            if (!isSuccess) {
                // 扣库存失败
                log.error("库存不足！");
                return ;
            }

            // 3、生成订单
            // 3.1、保存订单信息
            save(voucherOrder);
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 基于阻塞队列创建订单
     *
     * @param voucherOrder
     */
    private void createVoucherOrderWithBlockingQueue(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // 创建锁对象
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_PREFIX + "order:" + userId);
        // 获取锁
        boolean isLock = false;
        try {
            isLock = lock.tryLock(1L, 10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isLock) {
            // 获取锁失败，返回错误信息
            log.error("每人限购一单！");
            return ;
        }

        try {
            // 1、一人一单
            // 1.1、根据用户id、优惠券id查询订单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                // 已经存在
                log.error("每人限购一单！");
                return ;
            }

            // 2、扣库存
            boolean isSuccess = seckillVoucherService.update()
                    // 扣减库存
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    // 判断库存是否大于0
                    .gt("stock", 0).update();
            if (!isSuccess) {
                // 扣库存失败
                log.error("库存不足！");
                return ;
            }

            // 3、生成订单
            // 3.1、保存订单信息
            save(voucherOrder);
        } finally {
            // 释放锁
            lock.unlock();
        }
    }


    /**
     * 抢购秒杀优惠券（同步执行）
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucherSync(Long voucherId) {
        // 1、获取优惠券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);

        // 2、判断限时抢购是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 未开始，返回错误信息
            return Result.fail("抱歉，限时抢购还未开始！");
        }

        // 3、判断限时抢购是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 秒杀已经结束，返回错误信息
            return Result.fail("抱歉，限时抢购已经结束！");
        }

        // 4、判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            // 库存不足，返回错误信息
            return Result.fail("库存不足！");
        }

        // 5、创建订单返回信息
        // return createVoucherOrderWithSingleMole(voucherId);
        // return createVoucherOrderWithCluster(voucherId);
        return createVoucherOrderWithRedisson(voucherId);
    }

    /**
     * 创建订单（使用Redisson）
     *
     * @param voucherId 优惠券id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrderWithRedisson(Long voucherId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 创建锁对象
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_PREFIX + "order:" + userId);
        // 获取锁
        boolean isLock = false;
        try {
            isLock = lock.tryLock(1L, 10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isLock) {
            // 获取锁失败，返回错误信息
            return Result.fail("每人限购一单！");
        }

        try {
            // 1、一人一单
            // 1.1、根据用户id、优惠券id查询订单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                // 已经存在
                return Result.fail("每人限购一单！");
            }

            // 2、扣库存
            boolean isSuccess = seckillVoucherService.update()
                    // 扣减库存
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    // 判断库存是否大于0
                    .gt("stock", 0).update();
            if (!isSuccess) {
                // 扣库存失败
                return Result.fail("库存不足！");
            }

            // 3、生成订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 3.1、设置订单id
            long voucherOrderId = redisIdWorker.getId("order");
            voucherOrder.setId(voucherOrderId);
            // 3.2、设置优惠券id
            voucherOrder.setVoucherId(voucherId);
            // 3.3、设置用户id
            voucherOrder.setUserId(userId);
            // 3.4、保存订单信息
            save(voucherOrder);

            // 4、返回订单id
            return Result.ok(voucherOrder.getId());
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 创建订单（集群模式）
     *
     * @param voucherId 优惠券id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrderWithCluster(Long voucherId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 创建锁对象
        SimpleRedisLock redisLock = new SimpleRedisLock(stringRedisTemplate, "order");
        // 获取锁
        boolean isLock = redisLock.tryLock(5L);
        if (!isLock) {
            // 获取锁失败，返回错误信息
            return Result.fail("每人限购一单！");
        }

        try {
            // 1、一人一单
            // 1.1、根据用户id、优惠券id查询订单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                // 已经存在
                return Result.fail("每人限购一单！");
            }

            // 2、扣库存
            boolean isSuccess = seckillVoucherService.update()
                    // 扣减库存
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    // 判断库存是否大于0
                    .gt("stock", 0).update();
            if (!isSuccess) {
                // 扣库存失败
                return Result.fail("库存不足！");
            }

            // 3、生成订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 3.1、设置订单id
            long voucherOrderId = redisIdWorker.getId("order");
            voucherOrder.setId(voucherOrderId);
            // 3.2、设置优惠券id
            voucherOrder.setVoucherId(voucherId);
            // 3.3、设置用户id
            voucherOrder.setUserId(userId);
            // 3.4、保存订单信息
            save(voucherOrder);

            // 4、返回订单id
            return Result.ok(voucherOrder.getId());
        } finally {
            // 释放锁
            redisLock.unlock();
        }
    }

    /**
     * 创建订单（单结点模式）
     *
     * @param voucherId 优惠券id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrderWithSingleMole(Long voucherId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 使用userId作为同步监视器，加锁
        synchronized (userId.toString().intern()) {
            // 1、一人一单
            // 1.1、根据用户id、优惠券id查询订单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                // 已经存在
                return Result.fail("每人限购一单！");
            }

            // 2、扣库存
            boolean isSuccess = seckillVoucherService.update()
                    // 扣减库存
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    // 判断库存是否大于0
                    .gt("stock", 0).update();
            if (!isSuccess) {
                // 扣库存失败
                return Result.fail("库存不足！");
            }

            // 3、生成订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 3.1、设置订单id
            long voucherOrderId = redisIdWorker.getId("order");
            voucherOrder.setId(voucherOrderId);
            // 3.2、设置优惠券id
            voucherOrder.setVoucherId(voucherId);
            // 3.3、设置用户id
            voucherOrder.setUserId(userId);
            // 3.4、保存订单信息
            save(voucherOrder);

            // 4、返回订单id
            return Result.ok(voucherOrder.getId());
        }
    }
}
