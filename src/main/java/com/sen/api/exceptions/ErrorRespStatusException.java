package com.sen.api.exceptions;

/**
 * HTTP响应状态码异常
 * 当API返回的状态码不符合预期时抛出此异常
 */
public class ErrorRespStatusException extends Exception {

    private static final long serialVersionUID = -4954101493084921990L;

    private int statusCode;

    public ErrorRespStatusException(String msg) {
        super(msg);
    }

    public ErrorRespStatusException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public ErrorRespStatusException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
