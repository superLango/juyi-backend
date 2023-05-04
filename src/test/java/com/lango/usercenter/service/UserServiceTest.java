package com.lango.usercenter.service;

import com.lango.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author lango
 * @version 1.0
 * 用户服务测试
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("tsetqingwa");
        user.setUserAccount("123");
        user.setAvatarUrl("http///");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }
    @Test
    void userRegister(){
        String userAccount = "lango";
        String userPassword = "";
        String checkPassword = "245680";
        String planetCOde = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        userAccount = "wa";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        userAccount = "lango";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        userAccount = "test lango";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        checkPassword = "123465789";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        userAccount = "123";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);
        userAccount = "lango";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCOde);
        Assertions.assertEquals(-1,result);


    }
}