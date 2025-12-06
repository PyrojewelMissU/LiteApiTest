package com.sen.api.exceptions;

/**
 * API测试异常基类
 * 所有API测试相关异常的父类
 *
 * @author sen
 */
public class ApiTestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String detail;

    public ApiTestException(String message) {
        super(message);
        this.errorCode = "API_ERROR";
        this.detail = null;
    }

    public ApiTestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "API_ERROR";
        this.detail = null;
    }

    public ApiTestException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public ApiTestException(String errorCode, String message, String detail) {
        super(message);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ApiTestException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ").append(getMessage());
        if (detail != null) {
            sb.append(" - ").append(detail);
        }
        return sb.toString();
    }
}
