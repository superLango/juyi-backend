package com.lango.juyi.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lango
 * @version 1.0
 * 用户登录请求体
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 1447949434505641371L;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人 id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
