package com.lango.juyi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 *
 * @author Lango
 * @version 1.0
 */
@Data
public class PageRequest implements Serializable{

    private static final long serialVersionUID = -6603790009845194953L;
    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
