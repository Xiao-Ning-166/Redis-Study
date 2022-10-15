-- 获取key
local key = KEYS[1]
-- 获取线程标识
local threadId = ARGV[1]

-- 从redis中获取线程标识
local id = redis.call('get', key)

-- 比较线程标识是否一致
if (threadId == id) then
    -- 一致，释放锁
    redis.call('del', key)
end
-- 否则，返回0
return 0
