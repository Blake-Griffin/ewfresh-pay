package com.ewfresh.pay.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * description 统一工具类
 *
 * @author huangyabing
 * date 2018/3/29 11:13
 */
public class CommonUtils {
    public static final String ALGORITHM = "AES/ECB/PKCS7Padding";


    /**
     * 获取时间戳
     *
     * @return
     */
    public static String GetTimestamp() {
        return Long.toString(new Date().getTime() / 1000);
    }

    /**
     * 生成随机数
     *
     * @return
     */
    public static String CreateNoncestr() {
        Random random = new Random();
        return MD5.GetMD5String(String.valueOf(random.nextInt(10000)));
    }

    /**
     * @param characterEncoding 编码格式
     * @param packageParams     请求参数
     * @param API_KEY
     * @return
     * @author
     * @date 2016-4-22
     * @Description：sign签名
     */
    public static String createSign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + API_KEY);
        String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    /**
     * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     *
     * @return boolean
     */
    public static boolean isTenpaySign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        }

        sb.append("key=" + API_KEY);

        //算出摘要
        String mysign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toLowerCase();
        String tenpaySign = ((String) packageParams.get("sign")).toLowerCase();

        //System.out.println(tenpaySign + "    " + mysign);
        return tenpaySign.equals(mysign);
    }

    /**
     * description 处理金额
     *
     * @param amount
     * @return java.lang.String
     * @author huangyabing
     */
    public static String yuanToFee(String amount) {
        String currency = amount.replaceAll("\\$|\\￥|\\,", "");  //处理包含, ￥ 或者$的金额
        int index = currency.indexOf(".");
        int length = currency.length();
        Long amLong = 0L;
        if (index == -1) {
            amLong = Long.valueOf(currency + "00");
        } else if (length - index >= 3) {
            amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
        } else if (length - index == 2) {
            amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
        } else {
            amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
        }
        return amLong.toString();
    }

    /**
     * 生成支付二维码
     *
     * @param request
     * @param response
     * @param width
     * @param height
     * @param codeUrl  微信生成预定id时，返回的codeUrl
     */
    public static void getQRcode(HttpServletRequest request, HttpServletResponse response, Integer width, Integer height, String codeUrl) {
        if (width == null) {
            width = 300;
        }
        if (height == null) {
            height = 300;
        }
        String format = "jpg";
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix;
        try {
            // 在Zxing转码之前，手动转码，避免了中文乱码的错误
            bitMatrix = new MultiFormatWriter().encode(codeUrl, BarcodeFormat.QR_CODE, width, height, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, format, response.getOutputStream());
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * description PKCS7Padding解码
     *
     * @param str
     * @param key
     * @return java.lang.String
     * @author huangyabing
     */
    public static String decodePKCS7Padding(byte[] str, byte[] key) {
        final Logger logger = LoggerFactory.getLogger("paymentLog");
        String result = null;
        try {
            logger.info("decode decodeInfoBytes by key* with AES-256-ECB(PKCS7Padding)------>");
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES"); //生成加密解密需要的Key
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] bytes = cipher.doFinal(str);
            logger.info("byte[] after decode----->" + bytes);
            result = new String(bytes, "UTF-8");
            logger.info("result of AES-256-ECB  decode----->" + result);
        } catch (Exception e) {
            logger.info("AES-256-ECB decode failed----->" + result);
        }
        return result;
    }
}
