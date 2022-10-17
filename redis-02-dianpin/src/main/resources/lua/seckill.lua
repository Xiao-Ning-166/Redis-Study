-- 判断是否有秒杀资格的lua脚本
-- 判断库存是否充足
-- 判断用户是否已经下过单

-- 1、参数列表
-- 优惠券id
local voucherId = ARGV[1]
-- 用户id
local userId = ARGV[2]
-- 订单id
local orderId = ARGV[3]

-- 2、key列表
-- 库存key
local stockKey = KEYS[1] .. voucherId
-- 存储购买优惠券用户id的key
local orderKey = KEYS[2] .. voucherId

-- 3、判断业务
-- 3.1、判断库存是否充足
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 不充足
    return 1
end
-- 3.2、判断用户是否已经购买
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 已经购买
    return 2
end
-- 3.3、扣减库存
redis.call('incrby', stockKey, -1)
-- 3.4、保存用户购买记录
redis.call('sadd', orderKey, userId)

-- 3.5、将userId、voucherId、orderId保存到消息队列中
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)

-- 3.6、返回0，表示有购买资格
return 0