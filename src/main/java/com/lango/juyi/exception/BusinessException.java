package com.lango.juyi.exception;

import com.lango.juyi.common.ErrorCode;
import io.swagger.annotations.Api;

/**
 * 自定义异常类
 * @author lango
 * @version 1.0
 */
@Api(tags = "自定义异常类")
public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = -4497316946127295216L;
    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
    public BusinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
