package com.yql.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("aaaa", "您好，测试数据提交3，hello");
        System.out.println("redis get:" + redisTemplate.opsForValue().get("aaaa"));
    }
}
