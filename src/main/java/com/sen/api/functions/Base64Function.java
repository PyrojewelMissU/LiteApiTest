package com.sen.api.functions;

import com.sen.api.utils.CryptoUtil;

/**
 * Base64编解码函数
 * 用法: __base64(text) 或 __base64(text,encode) 或 __base64(text,decode)
 */
public class Base64Function implements Function {

    @Override
    public String execute(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Base64函数至少需要1个参数: text");
        }

        String text = args[0];
        String mode = args.length > 1 ? args[1] : "encode";

        if ("decode".equalsIgnoreCase(mode)) {
            return CryptoUtil.base64Decode(text);
        } else {
            return CryptoUtil.base64Encode(text);
        }
    }

    @Override
    public String getReferenceKey() {
        return "base64";
    }
}
