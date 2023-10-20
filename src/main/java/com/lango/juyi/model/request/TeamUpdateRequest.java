package com.lango.juyi.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lango
 * @version 1.0
 * 修改队伍请求体
 */
@Data
public class TeamUpdateRequest implements Serializable {


    private static final long serialVersionUID = -2481714416405625022L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 队伍头像
     */
    private String teamAvatarUrl;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}
