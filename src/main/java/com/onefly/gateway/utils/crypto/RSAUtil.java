package com.onefly.gateway.utils.crypto;

import com.onefly.gateway.utils.binary.CodecUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

/**
 * RSA 工具类
 * <pre>
 *  RSA/ECB/PKCS1Padding (1024, 2048)
 *  RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)
 *  RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)
 * </pre>
 */
public class RSAUtil {

    public static final String DEFAULT_CIPHER = "RSA/ECB/PKCS1Padding";

    public static final String SIGN_ALGORITHMS_SHA1 = "SHA1WithRSA";
    public static final String SIGN_ALGORITHMS_SHA256 = "SHA256WithRSA";
    public static final String SIGN_ALGORITHMS_SHA512 = "SHA512WithRSA";

    /**
     * 默认签名算法
     */
    public static final String DEFAULT_SIGN_ALGORITHMS = SIGN_ALGORITHMS_SHA1;
    private static volatile String signAlgorithms = null;

//    /**
//     * 使用私钥解密
//     *
//     * @param hexPrivateKey 16进制 表示的私钥
//     * @param cipherText    密文
//     * @return 明文
//     * @throws Exception 解密失败
//     */
//    public static byte[] privateKeyDecryptWithHex(String hexPrivateKey, String cipherText) throws Exception {
//        return privateKeyDecryptWithHex(hexPrivateKey, cipherText, Cipher.getInstance(DEFAULT_CIPHER));
//    }
//
//    /**
//     * 私钥解密
//     *
//     * @param hexPrivateKey 16进制 表示的私钥
//     * @param hexCipherText    密文
//     * @param cipher        填充方式
//     * @return 解密以后的数据
//     */
//    public static byte[] privateKeyDecryptWithHex(String hexPrivateKey, String hexCipherText, Cipher cipher) throws BadPaddingException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException {
//        return privateKeyDecryptWithHex(hexPrivateKey, 9, hexCipherText, cipher);
//    }
//
//    /**
//     * 私钥解密
//     *
//     * @param privateKeyStr 16进制 表示的私钥(der 格式)
//     * @param sequence      私钥段数,如果不包含其他信息,该值为9
//     * @param data          需要解密的数据
//     * @param cipher        填充方式
//     * @return 解密以后的数据
//     */
//    public static byte[] privateKeyDecryptWithHex(String hexPrivateKey, int sequence, String hexCipherText, Cipher cipher) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
//        PrivateKey privateKey = RSA.getPrivateKey(hexPrivateKey, sequence);
//        cipher.init(Cipher.DECRYPT_MODE, privateKey);
//        return cipher.doFinal(data);
//    }

    /**
     * 私钥解密
     *
     * @param privateKeyBase64 私钥
     * @param base64CipherText base64编码的密文
     * @return 解密后的数据
     */
    public static byte[] privateKeyDecryptWithBase64(String privateKeyBase64, String base64CipherText) throws Exception {
        return privateKeyDecryptWithBase64(privateKeyBase64, base64CipherText, Cipher.getInstance(DEFAULT_CIPHER));
    }

    /**
     * 私钥解密
     *
     * @param privateKeyBase64 私钥
     * @param base64CipherText base64编码的密文
     * @param cipher           填充方式
     * @return 解密后的数据
     */
    public static byte[] privateKeyDecryptWithBase64(String privateKeyBase64, String base64CipherText, Cipher cipher) throws Exception {
        PrivateKey privateKey = RSA.parsePrivateKeyWithBase64(privateKeyBase64);

        byte[] ciphertext = Base64.getDecoder().decode(base64CipherText);
        return privateKeyDecrypt(privateKey, ciphertext, cipher);
    }

    /**
     * 私钥解密 使用默认的解密算法进行解密<code>RSA/ECB/PKCS1Padding</code>
     *
     * @param privateKey 私钥
     * @param encrypted  加密的数据
     * @return 明文
     * @throws Exception 解密失败
     */
    public static byte[] privateKeyDecrypt(PrivateKey privateKey, byte[] encrypted) throws Exception {
        return privateKeyDecrypt(privateKey, encrypted, Cipher.getInstance(DEFAULT_CIPHER));
    }

    public static byte[] privateKeyDecrypt(PrivateKey privateKey, byte[] encrypted, Cipher cipher) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return execute(privateKey, cipher, encrypted);
    }

    /**
     * 使用指定的公钥进行加密,
     * 填充方式默认使用RSA/ECB/NoPadding
     *
     * @param hexPublicKey 十六进制表示DER编码的公钥字符串
     * @param plaintext    需要加密的数据
     * @return TheEncryptedData    经加密后的数据 十六进制表示
     */
    public static String publicKeyEncryptWithHex(String hexPublicKey, byte[] plaintext) throws Exception {
        return publicKeyEncryptWithHex(hexPublicKey, plaintext, Cipher.getInstance(DEFAULT_CIPHER));
    }

    /**
     * 根据hex类型公钥 对数据进行加密
     * 首先公钥得到mod和exp然后生成对象，根据对象对数据进行加密
     *
     * @param plaintext    待加密数据（不足8位或8的倍数补F）
     * @param cipher       注意填充方式：
     *                     1:如果调用了加密机【RSA/ECB/NoPadding】
     *                     2:此代码用了默认补位方式【RSA/None/PKCS1Padding】，不同JDK默认的补位方式可能不同，如Android默认是RSARSA/ECB/PKCS1Padding
     * @param hexPublicKey 十六进制表示DER编码的公钥字符串
     * @return TheEncryptedData    经加密后的数据 十六进制表示
     */
    public static String publicKeyEncryptWithHex(String hexPublicKey, byte[] plaintext, Cipher cipher) throws Exception {
        PublicKey publicKey = RSA.parsePublicKeyWithHex(hexPublicKey);
        byte[] cipherData = publicKeyEncrypt(publicKey, plaintext, cipher);
        return CodecUtils.hexString(cipherData);
    }

    /**
     * 使用指定的公钥进行加密,
     * 填充方式默认使用RSA/ECB/NoPadding
     *
     * @param publicKey 公钥
     * @param plaintext 需要加密的数据
     * @return TheEncryptedData    经加密后的数据 十六进制表示
     */
    public static String publicKeyEncryptWithHex(PublicKey publicKey, byte[] plaintext) throws Exception {
        return publicKeyEncryptWithHex(publicKey, plaintext, Cipher.getInstance(DEFAULT_CIPHER));
    }

    /**
     * 使用指定的公钥进行加密,
     *
     * @param publicKey 公钥
     * @param plaintext 需要加密的数据
     * @param cipher    cipher
     * @return TheEncryptedData    经加密后的数据 十六进制表示
     */
    public static String publicKeyEncryptWithHex(PublicKey publicKey, byte[] plaintext, Cipher cipher) throws Exception {
        byte[] cipherData = publicKeyEncrypt(publicKey, plaintext, cipher);
        return CodecUtils.hexString(cipherData);
    }

    /**
     * 使用指定的公钥进行加密,
     * 填充方式默认使用RSA/ECB/NoPadding
     *
     * @param base64PublicKey base64表示的公钥字符串
     * @param plaintext       需要加密的数据
     * @return TheEncryptedData    经加密后的数据 base64
     */
    public static String publicKeyEncryptWithBase64(String base64PublicKey, byte[] plaintext) throws Exception {
        return publicKeyEncryptWithBase64(base64PublicKey, plaintext, Cipher.getInstance(DEFAULT_CIPHER));
    }

    /**
     * 使用指定的公钥进行加密,
     *
     * @param base64PublicKey base64表示的公钥字符串
     * @param plaintext       需要加密的数据
     * @param cipher          填充方式
     * @return TheEncryptedData    经加密后的数据 base64
     */
    public static String publicKeyEncryptWithBase64(String base64PublicKey, byte[] plaintext, Cipher cipher) throws Exception {
        PublicKey publicKey = RSA.parsePublicKeyWithBase64(base64PublicKey);
        return publicKeyEncryptWithBase64(publicKey, plaintext, cipher);
    }

    /**
     * 使用指定的公钥进行加密,
     *
     * @param publicKey 公钥
     * @param plaintext 需要加密的数据
     * @return TheEncryptedData    经加密后的数据 base64
     */
    public static String publicKeyEncryptWithBase64(PublicKey publicKey, byte[] plaintext) throws Exception {
        byte[] ciphertext = publicKeyEncrypt(publicKey, plaintext, Cipher.getInstance(DEFAULT_CIPHER));
        return Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * 使用指定的公钥进行加密,
     *
     * @param publicKey 公钥
     * @param plaintext 需要加密的数据
     * @param cipher    填充方式
     * @return TheEncryptedData    经加密后的数据 base64
     */
    public static String publicKeyEncryptWithBase64(PublicKey publicKey, byte[] plaintext, Cipher cipher) throws Exception {
        byte[] ciphertext = publicKeyEncrypt(publicKey, plaintext, cipher);
        return Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * 公钥加密,使用填充方式RSA/ECB/NoPadding
     *
     * @param publicKey 公钥
     * @param plaintext 明文
     * @return 加密后的数据
     * @throws Exception 加密发生异常
     */
    public static byte[] publicKeyEncrypt(PublicKey publicKey, byte[] plaintext) throws Exception {
        return publicKeyEncrypt(publicKey, plaintext, Cipher.getInstance(DEFAULT_CIPHER));
    }

    /**
     * 公钥加密
     *
     * @param publicKey 公钥
     * @param plaintext 待加密的数据(明文)
     * @param cipher    cipher
     * @return 加密后的数据
     */
    public static byte[] publicKeyEncrypt(PublicKey publicKey, byte[] plaintext, Cipher cipher) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return execute(publicKey, cipher, plaintext);
    }

    public static String signWithBase64(String privateKeyStr, byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        return signWithBase64(privateKeyStr, signAlgorithms(), data);
    }

    /**
     * 对数据进行签名
     *
     * @param privateKeyStr base64 格式的私钥
     * @param algorithm     签名所用的algorithm
     * @param data          待签名的数据
     * @return 签名
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws SignatureException       SignatureException
     * @throws InvalidKeyException      InvalidKeyException
     */
    public static String signWithBase64(String privateKeyStr, String algorithm, byte[] data) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        PrivateKey privateKey = RSA.parsePrivateKeyWithBase64(privateKeyStr);
        byte[] sign = sign(privateKey, algorithm, data);
        return Base64.getEncoder().encodeToString(sign);
    }

    public static byte[] sign(PrivateKey privateKey, String algorithm, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean signVerifyWithBase64(String publicKey, byte[] data, String signature) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return signVerifyWithBase64(publicKey, signAlgorithms(), data, signature);
    }

    public static boolean signVerifyWithBase64(String publicKey, String algorithm, byte[] data, String signature) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return signVerify(
                RSA.parsePublicKeyWithBase64(publicKey),
                algorithm,
                data,
                Base64.getDecoder().decode(signature)
        );
    }

    public static boolean signVerify(PublicKey publicKey, String algorithm, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance(algorithm);
        sign.initVerify(publicKey);
        sign.update(data);
        return sign.verify(signature);
    }

    private static int getBlockSize(Key key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int keySize = RSA.getKeySize(key);
        int blockSize = keySize / 8;
        if (key instanceof PublicKey) {
            return blockSize - 11;
        }
        return blockSize;
    }

    private static byte[] execute(Key key, Cipher cipher, byte[] data) throws Exception {
        int blockSize = getBlockSize(key);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (int offset = 0; offset < data.length; offset += blockSize) {
                int inputLen = data.length - offset;
                if (inputLen > blockSize) {
                    inputLen = blockSize;
                }
                byte[] block = cipher.doFinal(data, offset, inputLen);
                outputStream.write(block);
            }
            return outputStream.toByteArray();
        } catch (IllegalBlockSizeException e) {
            throw new Exception("块大小不合法", e);
        } catch (BadPaddingException e) {
            throw new Exception("错误填充模式", e);
        } catch (IOException e) {
            throw new Exception("字节输出流异常", e);
        }
    }

    private static String signAlgorithms() {
        return Optional.ofNullable(signAlgorithms).orElse(DEFAULT_SIGN_ALGORITHMS);
    }

    public static void setSignAlgorithms(String algorithms) {
        signAlgorithms = algorithms;
    }
}
