package com.lango.juyi.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lango
 * @version 1.0
 * 添加队伍请求体
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
     * 队伍头像
     */
    private String teamAvatarUrl;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     * 格式化后端接口
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
