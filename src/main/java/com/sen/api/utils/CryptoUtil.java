package com.sen.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加密解密工具类
 * 支持AES和RSA加密解密
 */
public class CryptoUtil {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";

    // 默认的AES IV（16字节）
    private static final byte[] DEFAULT_IV = "1234567890123456".getBytes(StandardCharsets.UTF_8);

    private CryptoUtil() {
        // 工具类不允许实例化
    }

    // ==================== AES 加密解密 ====================

    /**
     * AES加密
     *
     * @param plainText 明文
     * @param key       密钥（16/24/32字节）
     * @return Base64编码的密文
     */
    public static String aesEncrypt(String plainText, String key) {
        return aesEncrypt(plainText, key, DEFAULT_IV);
    }

    /**
     * AES加密（指定IV）
     *
     * @param plainText 明文
     * @param key       密钥（16/24/32字节）
     * @param iv        初始化向量（16字节）
     * @return Base64编码的密文
     */
    public static String aesEncrypt(String plainText, String key, byte[] iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(padKey(key), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("AES encryption failed", e);
            throw new RuntimeException("AES加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES解密
     *
     * @param cipherText Base64编码的密文
     * @param key        密钥（16/24/32字节）
     * @return 明文
     */
    public static String aesDecrypt(String cipherText, String key) {
        return aesDecrypt(cipherText, key, DEFAULT_IV);
    }

    /**
     * AES解密（指定IV）
     *
     * @param cipherText Base64编码的密文
     * @param key        密钥（16/24/32字节）
     * @param iv         初始化向量（16字节）
     * @return 明文
     */
    public static String aesDecrypt(String cipherText, String key, byte[] iv) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(padKey(key), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("AES decryption failed", e);
            throw new RuntimeException("AES解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成AES密钥
     *
     * @param keySize 密钥长度（128/192/256）
     * @return Base64编码的密钥
     */
    public static String generateAesKey(int keySize) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(keySize);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            logger.error("Failed to generate AES key", e);
            throw new RuntimeException("生成AES密钥失败: " + e.getMessage(), e);
        }
    }

    // ==================== RSA 加密解密 ====================

    /**
     * RSA公钥加密
     *
     * @param plainText     明文
     * @param publicKeyStr  Base64编码的公钥
     * @return Base64编码的密文
     */
    public static String rsaEncrypt(String plainText, String publicKeyStr) {
        try {
            PublicKey publicKey = getPublicKey(publicKeyStr);
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("RSA encryption failed", e);
            throw new RuntimeException("RSA加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA私钥解密
     *
     * @param cipherText    Base64编码的密文
     * @param privateKeyStr Base64编码的私钥
     * @return 明文
     */
    public static String rsaDecrypt(String cipherText, String privateKeyStr) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyStr);
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("RSA decryption failed", e);
            throw new RuntimeException("RSA解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA私钥签名
     *
     * @param data          待签名数据
     * @param privateKeyStr Base64编码的私钥
     * @return Base64编码的签名
     */
    public static String rsaSign(String data, String privateKeyStr) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyStr);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            logger.error("RSA signing failed", e);
            throw new RuntimeException("RSA签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA公钥验签
     *
     * @param data         原始数据
     * @param signatureStr Base64编码的签名
     * @param publicKeyStr Base64编码的公钥
     * @return 验证结果
     */
    public static boolean rsaVerify(String data, String signatureStr, String publicKeyStr) {
        try {
            PublicKey publicKey = getPublicKey(publicKeyStr);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(signatureStr));
        } catch (Exception e) {
            logger.error("RSA verification failed", e);
            return false;
        }
    }

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度（1024/2048/4096）
     * @return 密钥对（[0]公钥, [1]私钥），Base64编码
     */
    public static String[] generateRsaKeyPair(int keySize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyGen.initialize(keySize);
            KeyPair keyPair = keyGen.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return new String[]{publicKey, privateKey};
        } catch (Exception e) {
            logger.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("生成RSA密钥对失败: " + e.getMessage(), e);
        }
    }

    // ==================== MD5 哈希 ====================

    /**
     * MD5哈希
     *
     * @param text 原文
     * @return MD5哈希值（32位小写）
     */
    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("MD5 hashing failed", e);
            throw new RuntimeException("MD5哈希失败: " + e.getMessage(), e);
        }
    }

    /**
     * MD5哈希（大写）
     */
    public static String md5Upper(String text) {
        return md5(text).toUpperCase();
    }

    // ==================== SHA 哈希 ====================

    /**
     * SHA-256哈希
     *
     * @param text 原文
     * @return SHA-256哈希值
     */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("SHA-256 hashing failed", e);
            throw new RuntimeException("SHA-256哈希失败: " + e.getMessage(), e);
        }
    }

    /**
     * SHA-512哈希
     *
     * @param text 原文
     * @return SHA-512哈希值
     */
    public static String sha512(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("SHA-512 hashing failed", e);
            throw new RuntimeException("SHA-512哈希失败: " + e.getMessage(), e);
        }
    }

    // ==================== Base64 编解码 ====================

    /**
     * Base64编码
     */
    public static String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     */
    public static String base64Decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    // ==================== 辅助方法 ====================

    /**
     * 补齐密钥到16字节
     */
    private static byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[16];

        if (keyBytes.length >= 16) {
            System.arraycopy(keyBytes, 0, paddedKey, 0, 16);
        } else {
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            // 剩余部分填充0
        }

        return paddedKey;
    }

    /**
     * 获取公钥对象
     */
    private static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 获取私钥对象
     */
    private static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
