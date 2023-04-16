package com.qingwa.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingwa
 * @version 1.0
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable{

    private static final long serialVersionUID = 42L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
