package com.sen.api.functions;

import com.sen.api.utils.CryptoUtil;

/**
 * AES加密函数
 * 用法: __aes(plainText,key) 或 __aes(plainText,key,encrypt) 或 __aes(cipherText,key,decrypt)
 */
public class AesFunction implements Function {

    @Override
    public String execute(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("AES函数至少需要2个参数: text, key");
        }

        String text = args[0];
        String key = args[1];
        String mode = args.length > 2 ? args[2] : "encrypt";

        if ("decrypt".equalsIgnoreCase(mode)) {
            return CryptoUtil.aesDecrypt(text, key);
        } else {
            return CryptoUtil.aesEncrypt(text, key);
        }
    }

    @Override
    public String getReferenceKey() {
        return "aes";
    }
}
