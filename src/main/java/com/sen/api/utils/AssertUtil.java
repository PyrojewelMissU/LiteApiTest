package com.sen.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 增强版断言工具类
 * 支持多种比较运算符: =, !=, >, >=, <, <=, ~= (正则), :exist, :null, :notNull, :empty, :notEmpty, :in
 *
 * @author sen
 */
public class AssertUtil {

    private static final Logger logger = LoggerFactory.getLogger(AssertUtil.class);

    /**
     * 验证包含关系
     */
    public static void contains(String source, String search) {
        Assert.assertTrue(source.contains(search),
                String.format("期待'%s'包含'%s'，实际为不包含.", source, search));
    }

    /**
     * 验证不包含关系
     */
    public static void notContains(String source, String search) {
        Assert.assertFalse(source.contains(search),
                String.format("期待'%s'不包含'%s'，实际为包含.", source, search));
    }

    /**
     * 验证相等
     */
    public static void assertEquals(Object actual, Object expected, String message) {
        logger.debug("断言相等: actual={}, expected={}", actual, expected);
        Assert.assertEquals(actual, expected, message);
    }

    /**
     * 验证不相等
     */
    public static void assertNotEquals(Object actual, Object expected, String message) {
        logger.debug("断言不相等: actual={}, expected={}", actual, expected);
        Assert.assertNotEquals(actual, expected, message);
    }

    /**
     * 验证大于
     */
    public static void assertGreaterThan(String actual, String expected, String message) {
        logger.debug("断言大于: actual={}, expected={}", actual, expected);
        BigDecimal actualNum = new BigDecimal(actual.trim());
        BigDecimal expectedNum = new BigDecimal(expected.trim());
        Assert.assertTrue(actualNum.compareTo(expectedNum) > 0,
                String.format("%s 期望 %s > %s", message, actual, expected));
    }

    /**
     * 验证大于等于
     */
    public static void assertGreaterThanOrEqual(String actual, String expected, String message) {
        logger.debug("断言大于等于: actual={}, expected={}", actual, expected);
        BigDecimal actualNum = new BigDecimal(actual.trim());
        BigDecimal expectedNum = new BigDecimal(expected.trim());
        Assert.assertTrue(actualNum.compareTo(expectedNum) >= 0,
                String.format("%s 期望 %s >= %s", message, actual, expected));
    }

    /**
     * 验证小于
     */
    public static void assertLessThan(String actual, String expected, String message) {
        logger.debug("断言小于: actual={}, expected={}", actual, expected);
        BigDecimal actualNum = new BigDecimal(actual.trim());
        BigDecimal expectedNum = new BigDecimal(expected.trim());
        Assert.assertTrue(actualNum.compareTo(expectedNum) < 0,
                String.format("%s 期望 %s < %s", message, actual, expected));
    }

    /**
     * 验证小于等于
     */
    public static void assertLessThanOrEqual(String actual, String expected, String message) {
        logger.debug("断言小于等于: actual={}, expected={}", actual, expected);
        BigDecimal actualNum = new BigDecimal(actual.trim());
        BigDecimal expectedNum = new BigDecimal(expected.trim());
        Assert.assertTrue(actualNum.compareTo(expectedNum) <= 0,
                String.format("%s 期望 %s <= %s", message, actual, expected));
    }

    /**
     * 验证正则匹配
     */
    public static void assertMatches(String actual, String regex, String message) {
        logger.debug("断言正则匹配: actual={}, regex={}", actual, regex);
        Pattern pattern = Pattern.compile(regex);
        Assert.assertTrue(pattern.matcher(actual).matches(),
                String.format("%s 期望 '%s' 匹配正则 '%s'", message, actual, regex));
    }

    /**
     * 验证值存在（不为null）
     */
    public static void assertExists(Object value, String message) {
        logger.debug("断言存在: value={}", value);
        Assert.assertNotNull(value, String.format("%s 期望值存在，实际为null", message));
    }

    /**
     * 验证值为null
     */
    public static void assertNull(Object value, String message) {
        logger.debug("断言为null: value={}", value);
        Assert.assertNull(value, String.format("%s 期望值为null，实际为: %s", message, value));
    }

    /**
     * 验证值不为null
     */
    public static void assertNotNull(Object value, String message) {
        logger.debug("断言不为null: value={}", value);
        Assert.assertNotNull(value, String.format("%s 期望值不为null", message));
    }

    /**
     * 验证字符串为空
     */
    public static void assertEmpty(String value, String message) {
        logger.debug("断言为空: value={}", value);
        Assert.assertTrue(value == null || value.isEmpty(),
                String.format("%s 期望值为空，实际为: %s", message, value));
    }

    /**
     * 验证字符串不为空
     */
    public static void assertNotEmpty(String value, String message) {
        logger.debug("断言不为空: value={}", value);
        Assert.assertTrue(value != null && !value.isEmpty(),
                String.format("%s 期望值不为空", message));
    }

    /**
     * 验证值在列表中
     */
    public static void assertIn(String actual, String[] expectedValues, String message) {
        logger.debug("断言在列表中: actual={}, expectedValues={}", actual, Arrays.toString(expectedValues));
        List<String> valueList = Arrays.asList(expectedValues);
        Assert.assertTrue(valueList.contains(actual),
                String.format("%s 期望 '%s' 在 %s 中", message, actual, valueList));
    }

    /**
     * 验证值不在列表中
     */
    public static void assertNotIn(String actual, String[] expectedValues, String message) {
        logger.debug("断言不在列表中: actual={}, expectedValues={}", actual, Arrays.toString(expectedValues));
        List<String> valueList = Arrays.asList(expectedValues);
        Assert.assertFalse(valueList.contains(actual),
                String.format("%s 期望 '%s' 不在 %s 中", message, actual, valueList));
    }

    /**
     * 验证数组/字符串长度
     */
    public static void assertLength(int actualLength, int expectedLength, String message) {
        logger.debug("断言长度: actualLength={}, expectedLength={}", actualLength, expectedLength);
        Assert.assertEquals(actualLength, expectedLength,
                String.format("%s 期望长度为 %d，实际为 %d", message, expectedLength, actualLength));
    }

    /**
     * 智能断言 - 根据操作符自动选择断言方法
     *
     * @param actual   实际值
     * @param operator 操作符 (=, !=, >, >=, <, <=, ~=, :exist, :null, :notNull, :empty, :notEmpty, :in)
     * @param expected 期望值
     * @param message  断言消息
     */
    public static void smartAssert(String actual, String operator, String expected, String message) {
        logger.info("智能断言: actual='{}', operator='{}', expected='{}', message='{}'",
                actual, operator, expected, message);

        switch (operator.trim().toLowerCase()) {
            case "=":
            case "==":
            case "eq":
                assertEquals(actual, expected, message);
                break;
            case "!=":
            case "<>":
            case "ne":
                assertNotEquals(actual, expected, message);
                break;
            case ">":
            case "gt":
                assertGreaterThan(actual, expected, message);
                break;
            case ">=":
            case "gte":
                assertGreaterThanOrEqual(actual, expected, message);
                break;
            case "<":
            case "lt":
                assertLessThan(actual, expected, message);
                break;
            case "<=":
            case "lte":
                assertLessThanOrEqual(actual, expected, message);
                break;
            case "~=":
            case "regex":
            case "matches":
                assertMatches(actual, expected, message);
                break;
            case ":exist":
            case ":exists":
                assertExists(actual, message);
                break;
            case ":null":
                assertNull(actual, message);
                break;
            case ":notnull":
                assertNotNull(actual, message);
                break;
            case ":empty":
                assertEmpty(actual, message);
                break;
            case ":notempty":
                assertNotEmpty(actual, message);
                break;
            case ":in":
                String[] values = expected.replace("[", "").replace("]", "").split(",");
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                assertIn(actual, values, message);
                break;
            case ":notin":
                String[] notInValues = expected.replace("[", "").replace("]", "").split(",");
                for (int i = 0; i < notInValues.length; i++) {
                    notInValues[i] = notInValues[i].trim();
                }
                assertNotIn(actual, notInValues, message);
                break;
            case "contains":
                contains(actual, expected);
                break;
            case "notcontains":
                notContains(actual, expected);
                break;
            default:
                throw new IllegalArgumentException("不支持的操作符: " + operator);
        }
    }

    /**
     * 解析验证表达式并执行断言
     * 支持的格式:
     * - $.path=value (相等)
     * - $.path!=value (不相等)
     * - $.path>value (大于)
     * - $.path>=value (大于等于)
     * - $.path<value (小于)
     * - $.path<=value (小于等于)
     * - $.path~=regex (正则匹配)
     * - $.path:exist (存在)
     * - $.path:null (为空)
     * - $.path:notNull (不为空)
     * - $.path:in[a,b,c] (在列表中)
     */
    public static VerifyExpression parseExpression(String expression) {
        expression = expression.trim();

        // 解析操作符和值
        String[] operators = {"!=", ">=", "<=", "~=", ">", "<", "=",
                ":exist", ":exists", ":null", ":notnull", ":empty", ":notempty", ":in", ":notin"};

        for (String op : operators) {
            int index = expression.indexOf(op);
            if (index > 0) {
                String path = expression.substring(0, index).trim();
                String value = "";
                if (!op.startsWith(":") || op.equals(":in") || op.equals(":notin")) {
                    value = expression.substring(index + op.length()).trim();
                }
                return new VerifyExpression(path, op, value);
            }
        }

        throw new IllegalArgumentException("无法解析验证表达式: " + expression);
    }

    /**
     * 验证表达式对象
     */
    public static class VerifyExpression {
        private final String path;
        private final String operator;
        private final String expectedValue;

        public VerifyExpression(String path, String operator, String expectedValue) {
            this.path = path;
            this.operator = operator;
            this.expectedValue = expectedValue;
        }

        public String getPath() {
            return path;
        }

        public String getOperator() {
            return operator;
        }

        public String getExpectedValue() {
            return expectedValue;
        }

        @Override
        public String toString() {
            return String.format("VerifyExpression{path='%s', operator='%s', expected='%s'}",
                    path, operator, expectedValue);
        }
    }
}
