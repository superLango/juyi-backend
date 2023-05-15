package com.lango.juyi.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lango
 * @version 1.0
 * 退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 7516963670779750819L;
    /**
     * id
     */
    private Long teamId;

}
