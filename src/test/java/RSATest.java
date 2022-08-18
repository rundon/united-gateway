import com.google.common.hash.Hashing;
import com.onefly.gateway.utils.crypto.AesUtils;
import com.onefly.gateway.utils.crypto.RSA;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Sean createAt 2021/6/23
 */
public class RSATest {

    @Test
    public void test() throws NoSuchAlgorithmException {

        RSA.Key key = RSA.generateKey(2048);

        System.out.println(Base64.getEncoder().encodeToString(key.getPublicKey().getEncoded()));
        System.out.println();
        System.out.println(Base64.getEncoder().encodeToString(key.getPrivateKey().getEncoded()));
    }

    @Test
    public void aesTest() throws Exception {
        byte[] key = AesUtils.generateKey();
        String keyStr = Base64.getEncoder().encodeToString(key);
        System.out.println(keyStr);
        byte[] keyChasr = Base64.getDecoder().decode(keyStr);
        byte[] data = AesUtils.encrypt(keyChasr, "阿西吧".getBytes(StandardCharsets.UTF_8));
        String encr = Base64.getEncoder().encodeToString(data);
        System.out.println("加密:" + encr);
        byte[] dd = AesUtils.decrypt(keyChasr, Base64.getDecoder().decode(encr));
        System.out.println(new String(dd));
    }

    @Test
    public void Hmac256() {
        String valueToDigest = "The quick brown fox jumps over the lazy dog";
        byte[] key = "secret".getBytes();
        //HmacSHA256
        String gHmac = Hashing.hmacSha256(key).newHasher().putString(valueToDigest, UTF_8).hash().toString();
        //HmacMD5
        String md5 = Hashing.hmacMd5(key).newHasher().putString(valueToDigest, UTF_8).hash().toString();
        //HmacSHA1
        String sha1 = Hashing.hmacSha1(key).newHasher().putString(valueToDigest, UTF_8).hash().toString();
        System.out.println(gHmac + "," + md5 + "," + sha1);
    }
}
