package com.ewfresh.pay.util.shopWithdraw;

import com.ewfresh.pay.util.JsonUtil;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Cipher;

/**
 * description: 店铺提现接口的RSA加密工具类
 * @author: JiuDongDong
 * date: 2019/6/17.
 */
public class RSACoder {

    public final static String ALGORITHM_SHA256WITHRSA = "SHA256withRSA";

    public static final String KEY_ALGORITHM = "RSA";

    private static char[] HEXCHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * 组装签名排好顺序的字段
     * @param map
     * @param allowValueNull 允许map的值为空，true允许；若允许为空会出现a=&b=
     * @return
     */
    public static String generateSortString(Map<String, String> map, boolean allowValueNull) {
        List<String> keys = new ArrayList<>(map.size());
        for (String key : map.keySet()) {
            if ((!allowValueNull && isEmpty(map.get(key))) || "sign".equals(key)
                    || "signValue".equals(key)) {
                continue;
            }
            keys.add(key);
        }
        Collections.sort(keys);
        StringBuffer sb = new StringBuffer();

        boolean isFirst = true;
        for (String key : keys) {
            if (isFirst) {
                sb.append(key).append("=").append(map.get(key));
                isFirst = false;
                continue;
            }
            sb.append("&").append(key).append("=").append(map.get(key));
        }
        return sb.toString();
    }

    /**
     * 签名
     * @param rawData 签名裸数据
     * @param privateKey 私钥
     * @param algorithm 签名验签算法
     * @throws Exception
     */
    public static String sign(byte[] rawData, String privateKey, String algorithm) throws Exception {
        byte[] keyBytes = decryptBASE64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature instance = Signature.getInstance(algorithm);
        instance.initSign(priKey);
        instance.update(rawData);
        return toHexString(instance.sign());
    }

    /**
     * 验证签名.
     * @param data
     * @param publicKey
     * @param sign
     * @param algorithm
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign, String algorithm)
            throws Exception {
        byte[] keyBytes = decryptBASE64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(pubKey);
        signature.update(data);
        return signature.verify(toBytes(sign));
    }

    /**
     * 公钥分段加密
     * @param data 源数据
     * @param publicKey 公钥(BASE64编码)
     * @param length 段长 1024长度的公钥最大取 117
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey, int length) throws Exception {
        byte[] keyBytes = decryptBASE64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > length) {
                cache = cipher.doFinal(data, offSet, length);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * length;
        }
        byte[] encryptData = out.toByteArray();
        out.close();
        return encryptData;
    }


    /**
     * 私钥解密
     * @param data 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @param length 分段解密长度  128
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String privateKey,int length)
            throws Exception {
        byte[] keyBytes =  decryptBASE64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > length) {
                cache = cipher.doFinal(data, offSet, length);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * length;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * base64解码
     * @param src
     */
    public static byte[] decryptBASE64(String src) {
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
        try {
            return decoder.decodeBuffer(src);
        } catch (Exception ex) {
            return null;
        }
    }
    /**
     * base64编码
     * @param src
     * @return
     */
    public static String encryptBASE64(byte[] src) {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        return encoder.encode(src);
    }

    /**
     * 从byte数组转16进制字符串
     * @param b
     * @return
     */
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEXCHAR[(b[i] & 0xf0) >>> 4]);
            sb.append(HEXCHAR[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 从16进制字符串转byte数组
     * @param s
     * @return
     */
    public static final byte[] toBytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    public static void main(String[] args) throws Exception {
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmxuPyNHXhXYB8QGALvvGmJugtxKo+YWL2mgWrnpM3GWSmET4q99XIELeTKN3rvLxHdJdFbm/1jSOKwey5CMklPoR0R+mZkvuoyIE8QXv880YGoetMf1DFKIzjtMe31rVKzNvOueJtNPHo43B8/SQXtiOyTPTres+p8h6cQmitIQIDAQAB";
        String privateKey="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKbG4/I0deFdgHxAYAu+8aYm6C3Eqj5hYvaaBauekzcZZKYRPir31cgQt5Mo3eu8vEd0l0Vub/WNI4rB7LkIySU+hHRH6ZmS+6jIgTxBe/zzRgah60x/UMUojOO0x7fWtUrM28654m008ejjcHz9JBe2I7JM9Ot6z6nyHpxCaK0hAgMBAAECgYBxnddWfsS71n4cp6KG/fsULTdJTsyIGMJZq44nX619APCfbenDTfm3BSR6vYGKApbluxj+9QCS8ScAdaJ2X/TfMBGXENJt3IxZYYxdkrBTIVqwNrxw5NugRI9UEiVhK7K1cJi+yoWz97cUvxz4wSjdETMkW7/wwKz1IenM8EqcHQJBAM+Y+Wi1kTerfx97M6qA+jfFwpA6rtIjvheVfzIB8QWPzeuNzYFBa8+Ap3cm4DKU6smL+1zlWKmvUkA4EkGjXFcCQQDNqWzPYXPSK1WiRUEl+OhKJ6ykFVD0olSR5QveRWNFVpYMznbQySa0QocSOLijz96SDbzCG5+K5NN78ILYu9dHAkA61SKRryspeLu9I0BAKO9AkRYTo93ZhfGgY2i5tl0k810rTXOZFv5DvzU2iljtXNCxL6+b4w9ef2Yy9vENkwtfAkEAwKeFln7j0G1nuqrFbJzOfSjNQKf3PjMSpdi6VW0KoVmLFlQSWWMVLqdjgF5CGLIQ/SCBQhQ+UA4rTl7vM7hC4QJAWCvn/p32/W1WfLnS8W5peMOgNICbZ491wJtcUc3GGO/wpfK/Lh7L7cyfB4oK+Cz6ISlcHSDyhzbdNfAP47Hddw==";

        String str = "234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890234567890-234567890234567890-1234567890234567890";
        //公钥加密
        byte[] encryptData = encryptByPublicKey(str.getBytes(), publicKey, 117);

        //私钥解密
        byte[] data = decryptByPrivateKey(encryptData, privateKey, 128);

        System.out.println(new String(data));


        byte[] rowData = str.getBytes();//待签名数据
        String signStr = sign(rowData, privateKey, ALGORITHM_SHA256WITHRSA);
        System.out.println(signStr);//签名结果数据（16进制字符串）
        //验证签名
        System.out.println(verify(rowData, publicKey, signStr, ALGORITHM_SHA256WITHRSA));
    }
}
