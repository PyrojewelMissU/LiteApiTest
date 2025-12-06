package com.sen.api.utils;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 职责：字符串判空、格式化、替换、截取等操作
 */
public class StringUtil {

    private StringUtil() {
        // 工具类不允许实例化
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为空白（null、空字符串或只包含空白字符）
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 替换第一个匹配的字符串
     *
     * @param sourceStr  待替换字符串
     * @param matchStr   匹配字符串
     * @param replaceStr 目标替换字符串
     * @return 替换后的字符串
     */
    public static String replaceFirst(String sourceStr, String matchStr, String replaceStr) {
        if (isEmpty(sourceStr) || isEmpty(matchStr)) {
            return sourceStr;
        }
        int index = sourceStr.indexOf(matchStr);
        if (index < 0) {
            return sourceStr;
        }
        int matLength = matchStr.length();
        String beginStr = sourceStr.substring(0, index);
        String endStr = sourceStr.substring(index + matLength);
        return beginStr + replaceStr + endStr;
    }

    /**
     * 获取默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 获取默认值（空白时）
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 安全地去除首尾空白
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除首尾空白，如果为null则返回空字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 判断字符串是否以指定前缀开头（忽略大小写）
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * 判断字符串是否以指定后缀结尾（忽略大小写）
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.toLowerCase().endsWith(suffix.toLowerCase());
    }

    /**
     * 判断字符串是否包含指定子串（忽略大小写）
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    /**
     * 字符串拼接
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(elements[i]);
        }
        return sb.toString();
    }

    /**
     * 集合字符串拼接
     */
    public static String join(String delimiter, Collection<String> elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }
        return String.join(delimiter, elements);
    }

    /**
     * 截取字符串（安全方式，不会抛出索引越界异常）
     */
    public static String substring(String str, int start, int end) {
        if (isEmpty(str)) {
            return str;
        }
        int len = str.length();
        if (start < 0) {
            start = 0;
        }
        if (end > len) {
            end = len;
        }
        if (start > end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 重复字符串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(ch));
                    nextUpper = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * 格式化字符串，使用 {} 作为占位符
     */
    public static String format(String template, Object... args) {
        if (isEmpty(template) || args == null || args.length == 0) {
            return template;
        }
        String result = template;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", arg == null ? "null" : Matcher.quoteReplacement(arg.toString()));
        }
        return result;
    }

    /**
     * 判断集合是否为空
     */
    public static boolean isCollectionEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断Map是否为空
     */
    public static boolean isMapEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 掩码字符串（如手机号、身份证号脱敏）
     */
    public static String mask(String str, int start, int end, char maskChar) {
        if (isEmpty(str)) {
            return str;
        }
        int len = str.length();
        if (start < 0) start = 0;
        if (end > len) end = len;
        if (start >= end) return str;

        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, start));
        sb.append(repeat(String.valueOf(maskChar), end - start));
        sb.append(str.substring(end));
        return sb.toString();
    }

    /**
     * 手机号脱敏 (138****1234)
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() != 11) {
            return phone;
        }
        return mask(phone, 3, 7, '*');
    }

    /**
     * 邮箱脱敏 (t***@example.com)
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + repeat("*", atIndex - 1) + email.substring(atIndex);
    }
}
