package com.onefly.gateway.utils.crypto;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * AES 工具
 *
 * <pre>
 *     AES 是一个新的可以用于保护电子数据的加密算法。
 *     明确地说，AES 是一个迭代的、对称密钥分组的密码
 *     它可以使用128、192 和 256 位密钥，并且用 128 位（16字节）分组加密和解密数据。
 *     与公共密钥密码使用密钥对不同，对称密钥密码使用相同的密钥加密和解密数据。
 *     通过分组密码返回的加密数据 的位数与输入数据相同。迭代加密使用一个循环结构，在该循环中重复置换（permutations ）和替换(substitutions）输入数据。
 * </pre>
 * <pre>
 * 算法/模式/填充                 16字节加密后数据长度       不满16字节加密后长度
 * AES/CBC/NoPadding                   16                          不支持
 * AES/CBC/PKCS5Padding                32                          16
 * AES/CBC/ISO10126Padding             32                          16
 * AES/CFB/NoPadding                   16                          原始数据长度
 * AES/CFB/PKCS5Padding                32                          16
 * AES/CFB/ISO10126Padding             32                          16
 * AES/ECB/NoPadding                   16                          不支持
 * AES/ECB/PKCS5Padding                32                          16
 * AES/ECB/ISO10126Padding             32                          16
 * AES/OFB/NoPadding                   16                          原始数据长度
 * AES/OFB/PKCS5Padding                32                          16
 * AES/OFB/ISO10126Padding             32                          16
 * AES/PCBC/NoPadding                  16                          不支持
 * AES/PCBC/PKCS5Padding               32                          16
 * AES/PCBC/ISO10126Padding            32                          16
 * </pre>
 *
 * @author 田尘殇Sean sean.snow@live.com
 */
public class AesUtils {

    public static final String AES = "AES";

    public static final String AES_CBC_NoPadding = "AES/CBC/NoPadding";
    public static final String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";

    public static final String AES_ECB_NoPadding = "AES/ECB/NoPadding";
    public static final String AES_ECB_PKCS5Padding = "AES/ECB/PKCS5Padding";

    private static final String DEFAULT_TRANSFORMATION = AES;
    private static volatile String transformation = null;

    private static final int DEFAULT_KEY_LENGTH = 128;
    private static volatile Integer keyLength = null;


    public static final String SHA1PRNG = "SHA1PRNG";
    public static final String NativePRNG = "NativePRNG";

    /**
     * 随机数算法
     */
    private static final String DEFAULT_RANDOM_ALGORITHM = SHA1PRNG;
    private static volatile String randomAlgorithm = null;


    public static byte[] generateKey() throws NoSuchAlgorithmException {
        return generateKey(keyLength());
    }

    public static byte[] generateKey(int keyLength) throws NoSuchAlgorithmException {
        return generateKey(keyLength, randomAlgorithm());
    }

    public static byte[] generateKey(int keyLength, String secureRandomAlgorithm) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(AES);
        generator.init(keyLength, SecureRandom.getInstance(secureRandomAlgorithm));
        SecretKey key = generator.generateKey();
        return key.getEncoded();
    }


    /**
     * 加密
     *
     * @param bash64Key 密钥Base64
     * @param plaintext 数据
     * @return BASE64 字符串
     */
    public static String encryptWithBase64(String bash64Key, byte[] plaintext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return encryptWithBase64(transformation(), bash64Key, plaintext);
    }

    public static String encryptWithBase64(String transformation, String base64Key, byte[] plaintext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return encryptWithBase64(transformation, keyLength(), base64Key, plaintext);
    }

    public static String encryptWithBase64(String transformation, int keyLength, String base64Key, byte[] plaintext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return encryptWithBase64(transformation, keyLength, randomAlgorithm(), base64Key, plaintext);
    }

    public static String encryptWithBase64(String transformation, int keyLength, String secureRandomAlgorithm, String base64Key, byte[] plaintext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] ciphertext = encrypt(transformation, keyLength, secureRandomAlgorithm, Base64.getDecoder().decode(base64Key), plaintext);
        return Base64.getEncoder().encodeToString(ciphertext);
    }


    public static byte[] encrypt(byte[] key, byte[] plaintext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return encrypt(transformation(), key, plaintext);
    }

    public static byte[] encrypt(String transformation, byte[] key, byte[] plaintext) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return encrypt(transformation, keyLength(), key, plaintext);
    }

    public static byte[] encrypt(String transformation, int keyLength, byte[] key, byte[] plaintext) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return encrypt(transformation, keyLength, randomAlgorithm(), key, plaintext);
    }

    /**
     * 数据加密
     *
     * @param transformation        加密算法、模式、填充方式
     * @param keyLength             秘钥长度
     * @param secureRandomAlgorithm 随机数算法
     * @param key                   秘钥
     * @param plaintext             明文
     * @return 密文
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] encrypt(String transformation, int keyLength, String secureRandomAlgorithm, byte[] key, byte[] plaintext) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        SecureRandom secureRandom = SecureRandom.getInstance(secureRandomAlgorithm);
        secureRandom.setSeed(key);
        KeyGenerator kgen = KeyGenerator.getInstance(AES);
        kgen.init(keyLength, secureRandom);

        SecretKeySpec secretKeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);

        Cipher cipher = Cipher.getInstance(transformation);
        if (transformation.contains("CBC")) {
            IvParameterSpec iv = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        }

        return cipher.doFinal(plaintext);
    }


    /**
     * 解密
     *
     * @param bash64Key        编码的秘钥
     * @param base64Ciphertext base64编码的密文
     * @return 解密的数据
     */
    public static byte[] decryptWithBase64(String bash64Key, String base64Ciphertext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return decryptWithBase64(transformation(), bash64Key, base64Ciphertext);
    }

    public static byte[] decryptWithBase64(String transformation, String bash64Key, String base64Ciphertext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return decryptWithBase64(transformation, keyLength(), bash64Key, base64Ciphertext);
    }

    public static byte[] decryptWithBase64(String transformation, int keyLength, String bash64Key, String base64Ciphertext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return decryptWithBase64(transformation, keyLength, randomAlgorithm(), bash64Key, base64Ciphertext);
    }

    public static byte[] decryptWithBase64(String transformation, int keyLength, String secureRandomAlgorithm, String bash64Key, String base64Ciphertext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return decrypt(transformation, keyLength, secureRandomAlgorithm, Base64.getDecoder().decode(bash64Key), Base64.getDecoder().decode(base64Ciphertext));
    }

    public static byte[] decrypt(byte[] key, byte[] ciphertext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return decrypt(transformation(), key, ciphertext);
    }

    public static byte[] decrypt(String transformation, byte[] key, byte[] ciphertext) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return decrypt(transformation, keyLength(), key, ciphertext);
    }

    public static byte[] decrypt(String transformation, int keyLength, byte[] key, byte[] ciphertext) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return decrypt(transformation, keyLength, randomAlgorithm(), key, ciphertext);
    }

    /**
     * 数据解密
     *
     * @param transformation        算法、模式、填充模式
     * @param keyLength             秘钥长度
     * @param secureRandomAlgorithm 随机数据算法
     * @param key                   秘钥
     * @param ciphertext            密文
     * @return 明文
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     */
    public static byte[] decrypt(String transformation, int keyLength, String secureRandomAlgorithm, byte[] key, byte[] ciphertext) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        SecureRandom secureRandom = SecureRandom.getInstance(secureRandomAlgorithm);
        secureRandom.setSeed(key);
        KeyGenerator kgen = KeyGenerator.getInstance(AES);
        kgen.init(keyLength, secureRandom);

        SecretKeySpec secretKeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);

        Cipher cipher = Cipher.getInstance(transformation);
        if (transformation.contains("CBC")) {
            IvParameterSpec iv = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        }
        return cipher.doFinal(ciphertext);
    }


    private static String transformation() {
        return Optional.ofNullable(transformation).orElse(DEFAULT_TRANSFORMATION);
    }

    public static void setTransformation(String t) {
        transformation = t;
    }

    private static int keyLength() {
        return Optional.ofNullable(keyLength).orElse(DEFAULT_KEY_LENGTH);
    }

    public static void setKeyLength(int i) {
        keyLength = i;
    }

    private static String randomAlgorithm() {
        return Optional.ofNullable(randomAlgorithm).orElse(DEFAULT_RANDOM_ALGORITHM);
    }

    public static void setRandomAlgorithm(String algorithm) {
        randomAlgorithm = algorithm;
    }
}
