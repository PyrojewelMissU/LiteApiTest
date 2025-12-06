package com.sen.api.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StringUtil 单元测试
 */
@DisplayName("StringUtil 工具类测试")
class StringUtilTest {

    @Test
    @DisplayName("测试 isEmpty 方法")
    void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("test"));
    }

    @Test
    @DisplayName("测试 isNotEmpty 方法")
    void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("test"));
    }

    @Test
    @DisplayName("测试 isBlank 方法")
    void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("test"));
    }

    @Test
    @DisplayName("测试 replaceFirst 方法")
    void testReplaceFirst() {
        assertEquals("hello world", StringUtil.replaceFirst("hello test", "test", "world"));
        assertEquals("hello test", StringUtil.replaceFirst("hello test", "foo", "world"));
        assertNull(StringUtil.replaceFirst(null, "test", "world"));
    }

    @Test
    @DisplayName("测试 defaultIfEmpty 方法")
    void testDefaultIfEmpty() {
        assertEquals("default", StringUtil.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtil.defaultIfEmpty("", "default"));
        assertEquals("test", StringUtil.defaultIfEmpty("test", "default"));
    }

    @Test
    @DisplayName("测试 camelToUnderscore 方法")
    void testCamelToUnderscore() {
        assertEquals("hello_world", StringUtil.camelToUnderscore("helloWorld"));
        assertEquals("user_name", StringUtil.camelToUnderscore("userName"));
        assertEquals("id", StringUtil.camelToUnderscore("id"));
    }

    @Test
    @DisplayName("测试 underscoreToCamel 方法")
    void testUnderscoreToCamel() {
        assertEquals("helloWorld", StringUtil.underscoreToCamel("hello_world"));
        assertEquals("userName", StringUtil.underscoreToCamel("user_name"));
        assertEquals("id", StringUtil.underscoreToCamel("id"));
    }

    @Test
    @DisplayName("测试 maskPhone 方法")
    void testMaskPhone() {
        assertEquals("138****5678", StringUtil.maskPhone("13812345678"));
        assertEquals("123", StringUtil.maskPhone("123")); // 非11位不处理
    }

    @Test
    @DisplayName("测试 format 方法")
    void testFormat() {
        assertEquals("Hello World", StringUtil.format("Hello {}", "World"));
        assertEquals("Hello World !", StringUtil.format("Hello {} {}", "World", "!"));
    }
}
