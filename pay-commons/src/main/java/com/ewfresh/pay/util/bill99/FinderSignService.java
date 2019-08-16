package com.ewfresh.pay.util.bill99;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.ewfresh.commons.util.ItvJsonUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinderSignService {

    private static String KEY_ALGORITHM = "RSA";

    private static String SIGNATURE_ALGORITHM = "SHA1WITHRSA";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 平台商户自己生成的私钥加签
     *
     * @param platformCode        商户在蝶巢的会员编号
     * @param body                请求原报文
     * @param platformPrivateKeys 私钥
     * @return
     */
    public String sign(String platformCode, String body, String platformPrivateKeys) {
        System.out.println("请求原报文是:" + body);
        //加签后的字符串
        String sign = null;
        try {
            PKCS8EncodedKeySpec x509EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(platformPrivateKeys));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(x509EncodedKeySpec);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(body.getBytes("utf-8"));
            sign = new String(Base64.encodeBase64(signature.sign()));
        } catch (Exception e) {
            System.out.println("error:" + e);
        }
        System.out.println("加签后的字符串是:" + sign);
        return sign;
    }

    /**
     * 利用蝶巢的公钥进行验签
     *
     * @param response
     * @param responseString 响应回来的明文
     * @param billPublicKey  公钥
     * @return
     */
    public boolean verify(CloseableHttpResponse response, String responseString, String billPublicKey) {
        Header[] headers = response.getHeaders("X-99Bill-Signature");
        logger.info("the hearder X-99Bill-Signature is------->{}", ItvJsonUtil.toJson(headers));
        String sign = headers[0].getValue();
        System.out.println("sign verify sign:" + sign);
        boolean result = false;
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(billPublicKey.getBytes("utf-8")));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify(publicKey);
            verifySign.update(responseString.getBytes("utf-8"));
            result = verifySign.verify(Base64.decodeBase64(sign.getBytes("utf-8")));
        } catch (Exception e) {
            System.out.println("SignatureService verify error:" + e);
        }
        System.out.println("sign verify result:" + result);
        return result;
    }
}
