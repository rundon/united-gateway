import com.google.common.hash.Hashing;
import com.onefly.gateway.api.RequestMessage;
import com.onefly.gateway.constant.Common;
import com.onefly.gateway.constant.HttpHeader;
import com.onefly.gateway.constant.MessageVersion;
import com.onefly.gateway.constant.SignType;
import com.onefly.gateway.utils.MapBuilder;
import com.onefly.gateway.utils.crypto.AesUtils;
import com.onefly.gateway.utils.crypto.RSAUtil;
import com.onefly.gateway.utils.json.Json;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sean createAt 2021/6/23
 */
public class ClientTest {

    private String pk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAylGKjhqXjh8tKD2yTqBXnBzQBJShyFJhfo9lnl3OySLM4zAJ64gZHA8dLw6tJFtxL3aVE605PGN/TB9ghtWUMCkLFva+6IoyokiAIMkJcSpspXOLzNuIPVotQb5JJ7DeL2pqIR/INKcDgUmmVfh3iR0dLCVOznmew1ooUnaf+p55emFnS5ebKWKuhps1H2eldGWwKNoQweqJuk8G1WxnfZqRgCTW/mQ9umKX2kkv92hPWoTppeaRFwqAe2OMP/O0RrmMwV7l24LcvSjNazCY2JK36dW7ALX/2h7oWR7L6i93UxQgwb0Zze7Rm880XHYz/U9DGoGcbXhRy7kLh10KQQIDAQAB";

    private String prk = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDKUYqOGpeOHy0oPbJOoFecHNAElKHIUmF+j2WeXc7JIszjMAnriBkcDx0vDq0kW3EvdpUTrTk8Y39MH2CG1ZQwKQsW9r7oijKiSIAgyQlxKmylc4vM24g9Wi1BvkknsN4vamohH8g0pwOBSaZV+HeJHR0sJU7OeZ7DWihSdp/6nnl6YWdLl5spYq6GmzUfZ6V0ZbAo2hDB6om6TwbVbGd9mpGAJNb+ZD26YpfaSS/3aE9ahOml5pEXCoB7Y4w/87RGuYzBXuXbgty9KM1rMJjYkrfp1bsAtf/aHuhZHsvqL3dTFCDBvRnN7tGbzzRcdjP9T0MagZxteFHLuQuHXQpBAgMBAAECggEBAMRw0fhSR48+JClrZkLDmu1AaJXZ/w+zNWieMQvIh6xx9sAsd6VSmxbMcgir1l9zzf1IxUy6p9VDwmkWGjIxFFaCs3rTj9/Xt3wsqwOqT1mq2Jz5COeazLjNYx3vdbZtG/6r82pAIrNE6rlQ2omk2+Os+hNQEimWmxmQ44/WEFVUaFrTw4yJR+B3lE1ZqpobCr4pEJT01Ds9pDAI/Cl7uGMwfd7bmUJ+sU9wFEjZtZE/UNcQQgqTxb4WgCU7ZDRFrvZIqCx5buACLZR0FtQGEksXg43/x2PMEhLvlcv0XShoi6fIKFdeTgDGbnd+YFrK9TpVOmxVN0tkTBRwZrslVcECgYEA+SPP4GaX6QS6bTKVfMb21XkqzoKvxlQFW56NDP034iiQzc3jGaRm6pvZbQDecEVgcd8C22vJMKZbmBTl3/hjKkjVMdgyKlwi7vKBAcoZVmvPfxFLkkT91uMKWO14EtuWHG/6Mpz8JAN4VAUVQsS0L9Ubkpjx56Ew/Fkn56ZdYRkCgYEAz+Ovaa2DaNX44vBxhcCUSuCWA1ea5U0UXnaHkHAwbQVo9kwNhxyzyauq5NYJol4Pyk6WBpJfpLhjsxmLI/SFVn4ZyaY6eUvRoWpFpskYvynbDl9LiuC1q5YOq+nrLtlyi1HNAz8ZJfmQLlGJyL7zCYnqmnUZbfMCqHf53jaVz2kCgYEAsHKDlEs0xWx62EGeC7wiLvhcr9twwAbbsJKvFQb1oC/YtlldwNhlpzzvlTqrT1pjPuKR9HL3D4SSlDggwin5mYXxsBaNGOEeQJrxcSIAJeu/DiBipFpGaP1tY6PziW+JdeR8j4INNThb7S2YbCxB7SqCF6ZIlSLdPaurDm4N7mkCgYEAi2AY4H7mFUkvXebaFVQxl6nOqVr4jDcLKvHInXu5272+yzHd9/G0T8b6AgXF28e4Smg5iRplaSf+H7tGX8q2AnD0lQ8PMPc2CkQXgmRcZP2I0a/uE6Pn6KvoFjXz6Sr78o/bJQwOrjkNAyDDgYUTqBeA5CER9XbxF0WojeSGt9ECgYEAzl8d3PKMOly74bqd0eoQNnLUWrgAYV2w9kNCtWkETtRz82j0CVSkbFTOsMTuhGW0gj0AUTGCZjDyNGEOTnLYggIRUOTSkGjlv21+rM5xtYRAg5slec2VuNYdnlByquKwXXiHpH5mNstkiE9jWVdH+aNwq5GEOQ5OrmD/O/CQtlE=";

    private String clientSecret = "1333a2346f2542b29464a45b082e1848";

    /**
     * v1 明文传输
     *
     * @throws Exception
     */
    @Test
    public void testV1() throws Exception {
        RequestMessage message = new RequestMessage();
        message.setClientId("61b16dc8b818124d0384162d");
        message.setTimestamp(DateFormatUtils.format(new Date(), Common.DATETIME_FORMAT));
        message.setRandomString(UUID.randomUUID().toString().replaceAll("-", ""));
        StringBuffer sb = new StringBuffer();
        sb.append("code=SYSTEM_NAME").append("1.0.0").append(message.getRandomString()).append(message.getTimestamp());
        String signData = sb.toString();
        //ars
        //String sign = RSAUtil.signWithBase64(prk, RSAUtil.SIGN_ALGORITHMS_SHA256, signData.getBytes(message.charset()));
        //hash256
        String sign = Hashing.hmacMd5(clientSecret.getBytes()).newHasher().putBytes(signData.getBytes(message.charset())).hash().toString();
        System.out.println(signData + "," + sign);
        message.setSign(sign);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeader.MESSAGE_VERSION, MessageVersion.V1_0_0.getVersion());//报文版本
        headers.add(HttpHeader.VERSION, "1.0.0");//API 版本
        headers.add(HttpHeader.CLIENT_ID, message.getClientId());//clientId
        headers.add(HttpHeader.SIGNATURE, message.getSign());//sign
        headers.add(HttpHeader.TIMESTAMP, message.getTimestamp());//timestamp
        headers.add(HttpHeader.NONCE, message.getRandomString());//randomString
        headers.add(HttpHeader.PARAMS, "code");
        headers.add(HttpHeader.method, SignType.HmacMD5.toString());
        HttpEntity<?> requestEntity = new HttpEntity<Object>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/v1/gateway")
                .queryParam("code", "SYSTEM_NAME");
        HttpEntity<String> resBody = restTemplate.exchange(builder.build().toString(), HttpMethod.GET, requestEntity, String.class);
        System.out.println(resBody.getBody());
        System.out.println(resBody.getHeaders());
    }

    /**
     * v2 传输的是密文
     *
     * @throws Exception
     */
    @Test
    public void testV2() throws Exception {
        RequestMessage message = new RequestMessage();
        message.setClientId("61b16dc8b818124d0384162d");

        AesUtils.setTransformation(AesUtils.AES_CBC_PKCS5Padding);
        AesUtils.setKeyLength(128);
        byte[] key = AesUtils.generateKey();

        message.setTrk(RSAUtil.publicKeyEncryptWithBase64(pk, key));
        message.setTimestamp(DateFormatUtils.format(new Date(), Common.DATETIME_FORMAT));
        message.setRandomString(UUID.randomUUID().toString().replaceAll("-", ""));

        Map<String, String> payload = MapBuilder.of(
                "code", "SYSTEM_NAME"
        );
        String payloadStr = Json.toJson(payload).toString();
        byte[] payloadCi = AesUtils.encrypt(key, payloadStr.getBytes(StandardCharsets.UTF_8));
        message.setPayload(Base64.getEncoder().encodeToString(payloadCi));

        String signData = message.getTimestamp() + message.getRandomString() + StringUtils.trimToEmpty(message.getPayload());
        System.out.println(signData);

        String sign = RSAUtil.signWithBase64(prk, RSAUtil.SIGN_ALGORITHMS_SHA512, signData.getBytes(message.charset()));

        message.setSign(sign);

        RestTemplate restTemplate = new RestTemplate();

        System.out.println(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeader.MESSAGE_VERSION, MessageVersion.V2_0_0.getVersion());//报文版本
        headers.add(HttpHeader.VERSION, "1.0.0");//API 版本
        headers.add(HttpHeader.CLIENT_ID, message.getClientId());//clientId
        headers.add(HttpHeader.SIGNATURE, URLEncoder.encode(message.getSign()));//sign
        headers.add(HttpHeader.TIMESTAMP, message.getTimestamp());//timestamp
        headers.add(HttpHeader.NONCE, message.getRandomString());//randomString
        headers.add(HttpHeader.TRK, URLEncoder.encode(message.getTrk()));//randomString
        HttpEntity<?> requestEntity = new HttpEntity<Object>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/v1/gateway")
                .queryParam("payload", URLEncoder.encode(message.getPayload()));

        HttpEntity<String> resBody = restTemplate.exchange(builder.build().toString(), HttpMethod.GET, requestEntity, String.class);

//        ResponseMessage res = restTemplate.postForObject(builder.build().toString(), message, ResponseMessage.class);
        System.out.println(resBody.getBody());
        System.out.println(resBody.getHeaders());
    }

    /**
     * v1 明文传输
     *
     * @throws Exception
     */
    @Test
    public void testV3() throws Exception {
        RequestMessage message = new RequestMessage();
        message.setClientId("61b16dc8b818124d0384162d");
        message.setTimestamp(DateFormatUtils.format(new Date(), Common.DATETIME_FORMAT));
        message.setRandomString(UUID.randomUUID().toString().replaceAll("-", ""));
        StringBuffer sb = new StringBuffer();
        sb.append("grant_type=password&scope=openid&username=admin&password=123456").append("1.0.0").append(message.getRandomString()).append(message.getTimestamp());
        String signData = sb.toString();
        String sign = RSAUtil.signWithBase64(prk, RSAUtil.SIGN_ALGORITHMS_SHA256, signData.getBytes(message.charset()));
        System.out.println(signData + "," + sign);
        message.setSign(sign);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Basic b3BlbmlkOjY5M2JiMDBhMTkxMzRlM2M5ZmM5OTBiYzk3NDJmNjE0");//
        headers.add(HttpHeader.MESSAGE_VERSION, MessageVersion.V1_0_0.getVersion());//报文版本
        headers.add(HttpHeader.VERSION, "1.0.0");//API 版本
        headers.add(HttpHeader.CLIENT_ID, message.getClientId());//clientId
        headers.add(HttpHeader.SIGNATURE, message.getSign());//sign
        headers.add(HttpHeader.TIMESTAMP, message.getTimestamp());//timestamp
        headers.add(HttpHeader.NONCE, message.getRandomString());//randomString
        headers.add(HttpHeader.PARAMS, "grant_type,scope,username,password");
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();
        param.add("grant_type", "password");
        param.add("scope", "openid");
        param.add("username", "admin");
        param.add("password", "123456");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(param, headers);
        String url = "http://localhost:8181/oauth/token";
        String resBody = restTemplate.postForObject(url, requestEntity, String.class);
        System.out.println(resBody);
    }

    @Test
    public void uuid() {
        System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
    }

}
