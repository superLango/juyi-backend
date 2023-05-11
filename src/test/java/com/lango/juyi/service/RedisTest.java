package com.lango.juyi.service;

import com.lango.juyi.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author Lango
 * @version 1.0
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("langoString","dog");
        valueOperations.set("langoInt",1);
        valueOperations.set("langoDouble",2.5);
        User user = new User();
        user.setId(1L);
        user.setUsername("lango");
        valueOperations.set("langoUser",user);
        // 查
        Object lango = valueOperations.get("langoString");
        Assertions.assertTrue("dog".equals((String)lango));
        lango = valueOperations.get("langoInt");
        Assertions.assertTrue(1 == (Integer) lango);
        lango = valueOperations.get("langoDouble");
        Assertions.assertTrue(2.5 == (Double)lango);
        System.out.println(valueOperations.get("langoUser"));
        valueOperations.set("langoString","dog");
        redisTemplate.delete("langoString");
    }
}
