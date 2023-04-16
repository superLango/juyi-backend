package com.qingwa.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingwa
 * @version 1.0
 * 用户登录请求体
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private String userAccount;
    private String userPassword;
}
