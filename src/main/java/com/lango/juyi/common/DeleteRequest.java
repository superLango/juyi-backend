package com.lango.juyi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 *
 * @author Lango
 * @version 1.0
 */
@Data
public class DeleteRequest implements Serializable{


    private static final long serialVersionUID = -8545981073457786176L;

    private long id;
}
