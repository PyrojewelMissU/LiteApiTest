package com.sen.api.functions;

import com.sen.api.utils.CryptoUtil;

/**
 * RSA加密函数
 * 用法: __rsa(plainText,publicKey) 或 __rsa(plainText,publicKey,encrypt)
 *       或 __rsa(cipherText,privateKey,decrypt)
 */
public class RsaFunction implements Function {

    @Override
    public String execute(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("RSA函数至少需要2个参数: text, key");
        }

        String text = args[0];
        String key = args[1];
        String mode = args.length > 2 ? args[2] : "encrypt";

        if ("decrypt".equalsIgnoreCase(mode)) {
            return CryptoUtil.rsaDecrypt(text, key);
        } else if ("sign".equalsIgnoreCase(mode)) {
            return CryptoUtil.rsaSign(text, key);
        } else {
            return CryptoUtil.rsaEncrypt(text, key);
        }
    }

    @Override
    public String getReferenceKey() {
        return "rsa";
    }
}
