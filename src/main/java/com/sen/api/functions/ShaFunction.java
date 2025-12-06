package com.sen.api.functions;

import com.sen.api.utils.CryptoUtil;

/**
 * SHA哈希函数
 * 用法: __sha(text) 或 __sha(text,256) 或 __sha(text,512)
 */
public class ShaFunction implements Function {

    @Override
    public String execute(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("SHA函数至少需要1个参数: text");
        }

        String text = args[0];
        String type = args.length > 1 ? args[1] : "256";

        if ("512".equals(type)) {
            return CryptoUtil.sha512(text);
        } else {
            return CryptoUtil.sha256(text);
        }
    }

    @Override
    public String getReferenceKey() {
        return "sha";
    }
}
