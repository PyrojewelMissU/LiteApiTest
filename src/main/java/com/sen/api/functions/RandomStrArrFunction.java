package com.sen.api.functions;

import com.sen.api.utils.RandomUtil;

/**
 * 随机字符串数组生成函数
 * 用法: __randomStrArr(arrLength, strLength, onlyNumber)
 *
 * @author sen
 */
public class RandomStrArrFunction implements Function {

    @Override
    public String execute(String[] args) {
        int len = args.length;
        int arrLength = 1;      // 默认数组长度为1
        int strLength = 6;      // 默认字符串长度为6
        boolean onlyNumber = false;  // 默认包含字母和数字

        try {
            if (len >= 1 && args[0] != null && !args[0].isEmpty()) {
                arrLength = Integer.parseInt(args[0].trim());
            }
            if (len >= 2 && args[1] != null && !args[1].isEmpty()) {
                strLength = Integer.parseInt(args[1].trim());
            }
            if (len >= 3 && args[2] != null && !args[2].isEmpty()) {
                onlyNumber = Boolean.parseBoolean(args[2].trim());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("randomStrArr函数参数格式错误: " + e.getMessage(), e);
        }

        return RandomUtil.getRandomArr(arrLength, strLength, onlyNumber);
    }

    @Override
    public String getReferenceKey() {
        return "randomStrArr";
    }
}
