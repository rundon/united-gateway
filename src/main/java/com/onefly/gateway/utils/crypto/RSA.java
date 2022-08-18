package com.onefly.gateway.utils.crypto;

import com.onefly.gateway.utils.binary.CodecUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class RSA {

    private static final String DEFAULT_ALGORITHM = "RSA";

    public static Key generateKey() throws NoSuchAlgorithmException {
        return generateKey(2048);
    }

    public static Key generateKey(int keySize) throws NoSuchAlgorithmException {
        return generateKey(DEFAULT_ALGORITHM, keySize);
    }

    public static Key generateKey(String algorithm, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(algorithm);
        keyGenerator.initialize(keySize);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        return new Key(publicKey, privateKey);
    }

    /**
     * 获取密钥的长度
     *
     * @param key 密钥信息
     * @return 密钥长度
     * @throws NoSuchAlgorithmException 无效的算法
     * @throws InvalidKeySpecException  无效的key
     */
    public static int getKeySize(java.security.Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFact = KeyFactory.getInstance(key.getAlgorithm());
        if (key instanceof PublicKey) {
            RSAPublicKeySpec keySpec = keyFact.getKeySpec(key, RSAPublicKeySpec.class);
            return keySpec.getModulus().toString(2).length();
        } else if (key instanceof PrivateKey) {
            RSAPrivateCrtKeySpec keySpec = keyFact.getKeySpec(key, RSAPrivateCrtKeySpec.class);
            return keySpec.getModulus().toString(2).length();
        }
        return 0;
    }


    /**
     * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的公钥对象，并生成公钥。
     *
     * @param hexModulus        系数。
     * @param hexPublicExponent 专用指数。
     * @return RSA公钥。
     */
    public static PublicKey getPublicKey(String hexModulus, String hexPublicExponent) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return getPublicKey(hexModulus, hexPublicExponent, DEFAULT_ALGORITHM);
    }

    /**
     * 根据给定的16进制系数和专用指数字符串构造一个RSA专用的公钥对象，并生成公钥。
     *
     * @param hexModulus        系数。
     * @param hexPublicExponent 专用指数。
     * @param algorithm         the name of the requested key algorithm.
     * @return RSA公钥。
     */
    public static PublicKey getPublicKey(String hexModulus, String hexPublicExponent, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException {
        BigInteger modulus = new BigInteger(hexModulus, 16);
        BigInteger exponent = new BigInteger(hexPublicExponent, 16);
        RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(modulus, exponent);

        KeyFactory fact = KeyFactory.getInstance(algorithm);
        return fact.generatePublic(rsaPubKeySpec);
    }


    /**
     * 使用模和指数生成RSA私钥
     *
     * @param hexModulus  模
     * @param hexExponent 指数
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(String hexModulus, String hexExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return getPrivateKey(hexModulus, hexExponent, DEFAULT_ALGORITHM);
    }

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】 如果调用了加密机【RSA/ECB/NoPadding】
     *
     * @param hexModulus  模
     * @param hexExponent 指数
     * @param algorithm   the name of the requested key algorithm.
     * @return 私钥
     */
    public static PrivateKey getPrivateKey(String hexModulus, String hexExponent, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger modulus = new BigInteger(hexModulus, 16);
        BigInteger exponent = new BigInteger(hexExponent, 16);
        RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(privateKeySpec);
    }

//    public static PrivateKey getPrivateKey(String hexDerPrivateKey, int sequence) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//        DerInputStream dis = new DerInputStream(CodecUtils.hex2byte(hexDerPrivateKey));
//        DerValue[] ders = dis.getSequence(sequence);
//        //依次读取 RSA 因子信息
////        int version = ders[0].getBigInteger().intValue();
//        BigInteger modulus = ders[1].getBigInteger();
//        BigInteger publicExponent = ders[2].getBigInteger();
//        BigInteger privateExponent = ders[3].getBigInteger();
//        BigInteger primeP = ders[4].getBigInteger();
//        BigInteger primeQ = ders[5].getBigInteger();
//        BigInteger primeExponentP = ders[6].getBigInteger();
//        BigInteger primeExponentQ = ders[7].getBigInteger();
//        BigInteger crtCoefficient = ders[8].getBigInteger();
//        RSAPrivateCrtKeySpec rsaPrivateKeySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
//
//        KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
//        return keyFactory.generatePrivate(rsaPrivateKeySpec);
//    }

    /**
     * 读取pem文件中的公钥
     *
     * @param inputStream pem 文件流
     * @return 公钥
     * @throws Exception 发生异常
     */
    public static PublicKey readPemPublicKey(InputStream inputStream) throws Exception {
        return readPemPublicKey(inputStream, DEFAULT_ALGORITHM);
    }

    /**
     * 读取pem文件中的公钥
     *
     * @param inputStream pem 文件流
     * @param algorithm   the name of the requested key algorithm.
     * @return 公钥
     * @throws Exception 发生异常
     */
    public static PublicKey readPemPublicKey(InputStream inputStream, String algorithm) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String readLine;

            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) != 45) {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }

            return parsePublicKeyWithBase64(sb.toString(), algorithm);
        } catch (FileNotFoundException var20) {
            throw new Exception("公钥路径文件不存在");
        } catch (IOException var21) {
            throw new Exception("读取公钥异常");
        } catch (NoSuchAlgorithmException var22) {
            throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", algorithm));
        } catch (InvalidKeySpecException var23) {
            throw new Exception("生成公钥对象异常");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }

        }
    }

    /**
     * 从pem文件读取私钥信息，默认使用RSA KeyFactory
     *
     * @param inputStream pem 文件流
     * @return 私钥
     * @throws Exception 读取失败
     */
    public static PrivateKey readPemPrivateKey(InputStream inputStream) throws Exception {
        return readPemPrivateKey(inputStream, DEFAULT_ALGORITHM);
    }

    /**
     * 从pem文件读取私钥信息
     *
     * @param inputStream pem 文件流
     * @param algorithm   the name of the requested key algorithm.
     * @return 私钥
     * @throws Exception 读取失败
     */
    public static PrivateKey readPemPrivateKey(InputStream inputStream, String algorithm) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String readLine;

            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) != 45) {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            byte[] key = Base64.getDecoder().decode(sb.toString());
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return keyFactory.generatePrivate(priPKCS8);
        } catch (FileNotFoundException var20) {
            throw new Exception("私钥路径文件不存在");
        } catch (IOException var21) {
            throw new Exception("读取私钥异常");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", algorithm));
        } catch (InvalidKeySpecException var22) {
            throw new Exception("生成私钥对象异常");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }

        }
    }

    /**
     * 解析公钥
     *
     * @param hexPublicKey 16进制表示的公钥
     * @return 公钥
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    public static PublicKey parsePublicKeyWithHex(String hexPublicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return parsePublicKeyWithHex(hexPublicKey, DEFAULT_ALGORITHM);
    }

    /**
     * 解析公钥
     *
     * @param hexPublicKey 16进制表示的公钥
     * @param algorithm    the name of the requested key algorithm.
     * @return 公钥
     * @throws InvalidKeySpecException  InvalidKeySpecException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    public static PublicKey parsePublicKeyWithHex(String hexPublicKey, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] key = CodecUtils.hex2byte(hexPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(new X509EncodedKeySpec(key));
    }

    /**
     * 从base64字符串种解析公钥
     *
     * @param base64PublicKey base64 表示的公钥
     * @return 公钥
     */
    public static PublicKey parsePublicKeyWithBase64(String base64PublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return parsePublicKeyWithBase64(base64PublicKey, DEFAULT_ALGORITHM);
    }

    /**
     * 从base64字符串种解析公钥
     *
     * @param base64PublicKey base64 表示的公钥
     * @param algorithm       the name of the requested key algorithm.
     * @return 公钥
     */
    public static PublicKey parsePublicKeyWithBase64(String base64PublicKey, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(pubX509);
    }

    /**
     * 从 base64字符串中解析私钥
     *
     * @param base64PrivateKey 私钥
     * @return 私钥
     */
    public static PrivateKey parsePrivateKeyWithBase64(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return parsePrivateKeyWithBase64(base64PrivateKey, DEFAULT_ALGORITHM);
    }

    /**
     * 从 base64字符串中解析私钥
     *
     * @param base64PrivateKey 私钥
     * @param algorithm        the name of the requested key algorithm.
     * @return 私钥
     */
    public static PrivateKey parsePrivateKeyWithBase64(String base64PrivateKey, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec pubX509 = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(pubX509);
    }

    private static int[] calLength(String[] bytes, int index, int length) {
        if (length > 128) {
            int i = length - 128;
            StringBuilder lenStr = new StringBuilder();
            for (int j = index + 1; j < index + i + 1; j++)
                lenStr.append(bytes[j]);

            index += i;
            length = Integer.parseInt(lenStr.toString(), 16);
        }

        return new int[]{index, length};
    }

    public static class Key {
        private PublicKey publicKey;
        private PrivateKey privateKey;

        public Key() {
        }

        public Key(PublicKey publicKey, PrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(PublicKey publicKey) {
            this.publicKey = publicKey;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }
    }
}
