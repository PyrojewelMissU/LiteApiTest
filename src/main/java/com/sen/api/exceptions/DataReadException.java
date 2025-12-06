package com.sen.api.exceptions;

/**
 * 数据读取异常
 * 测试数据读取失败时抛出
 *
 * @author sen
 */
public class DataReadException extends ApiTestException {

    private static final long serialVersionUID = 1L;

    public DataReadException(String message) {
        super("DATA_READ_ERROR", message);
    }

    public DataReadException(String message, Throwable cause) {
        super("DATA_READ_ERROR", message, cause);
    }

    public DataReadException(String message, String filePath) {
        super("DATA_READ_ERROR", message, "文件: " + filePath);
    }
}
