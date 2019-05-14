package com.youzi.teaChain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import static com.youzi.teaChain.common.StringUtil.getNumberRandom;

@Service
public class RedisClient {

    private JedisPool jedisPool;//非切片连接池

    @Value("${redis.master.host}")
    private String masterHost;
    @Value("${redis.master.port}")
    private int masterPort;
    @Value("${redis.master.pool.max-idle}")
    private int masterMaxIdle;
    @Value("${redis.master.pool.max-wait}")
    private long masterMaxWaitMillis;
//    @Value("${redis.master.password}")
    private String masterPassword;

    public Jedis getResource() {
        initialPool();
        //非切片额客户端连接
        Jedis jedis = jedisPool.getResource();
        jedis.select(2);
        return jedis;
    }

    /**
     * 初始化非切片池
     */
    private void initialPool() {
        if (jedisPool == null) {
            // 池基本配置 k
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(masterMaxIdle);
            config.setMaxWaitMillis(masterMaxWaitMillis);
            config.setTestOnBorrow(false);

            jedisPool = new JedisPool(config, masterHost, masterPort, Protocol.DEFAULT_TIMEOUT, masterPassword);
        }
    }

    // 等待 10000 超时 20000
    public synchronized boolean lock(Jedis jedis, String uuid, int timeout, int expireMsecs) throws InterruptedException {
        while (timeout >= 0) {
            Long expires = System.currentTimeMillis() + expireMsecs + 1;
            long setNXSuccess = jedis.setnx("lock:" + uuid, String.valueOf(expires));
            if (setNXSuccess == 1) { // 写入成功
                return true;
            }

            String redisTime = jedis.get("lock:" + uuid);
            if (redisTime != null && Long.parseLong(redisTime) < System.currentTimeMillis()) { //存在 且超时
                String oldTime = jedis.getSet("lock:" + uuid, String.valueOf(expires));
                if (oldTime != null && oldTime.equals(redisTime)) {
                    return true;
                }
            }
            Integer sleepTime = Integer.parseInt(getNumberRandom(50, 150));
            timeout -= sleepTime;
            Thread.sleep(sleepTime);
        }
        return false;
    }

    public void unlock(Jedis jedis, String uuid) {
        jedis.del("lock:" + uuid);
    }
}