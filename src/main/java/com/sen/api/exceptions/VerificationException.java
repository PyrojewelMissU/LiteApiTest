package com.sen.api.exceptions;

/**
 * 验证异常
 * 响应验证失败时抛出
 *
 * @author sen
 */
public class VerificationException extends ApiTestException {

    private static final long serialVersionUID = 1L;

    private final String expected;
    private final String actual;

    public VerificationException(String message) {
        super("VERIFY_ERROR", message);
        this.expected = null;
        this.actual = null;
    }

    public VerificationException(String message, String expected, String actual) {
        super("VERIFY_ERROR", message, String.format("期望: %s, 实际: %s", expected, actual));
        this.expected = expected;
        this.actual = actual;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }
}
