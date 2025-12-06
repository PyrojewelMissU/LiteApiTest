package com.sen.api.exceptions;

/**
 * 配置异常
 * 配置文件读取或解析失败时抛出
 *
 * @author sen
 */
public class ConfigurationException extends ApiTestException {

    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super("CONFIG_ERROR", message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super("CONFIG_ERROR", message, cause);
    }

    public ConfigurationException(String message, String configFile) {
        super("CONFIG_ERROR", message, "配置文件: " + configFile);
    }
}
