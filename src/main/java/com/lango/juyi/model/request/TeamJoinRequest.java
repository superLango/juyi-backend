package com.lango.juyi.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lango
 * @version 1.0
 * 加入队伍请求体
 */
@Data
public class TeamJoinRequest implements Serializable {


    private static final long serialVersionUID = -7519461225350637188L;
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
