package com.ewfresh.pay.util;

import org.apache.commons.lang.StringUtils;

/**
 * description: 银行卡号码转换
 * @author: JiuDongDong
 * date: 2018/9/28.
 */
public class BankCardSwitchUtil {

    /**
     * Description: 初始银行卡号截取前6后4，共10位
     * @author: JiuDongDong
     * @param originCardCode      初始银行卡号
     * @return java.lang.String   截取后的10位短卡号
     * date: 2018/9/28 13:21
     */
    public static String originToShort(String originCardCode) {
        if (StringUtils.isBlank(originCardCode)) {
            return "";
        }
        if (originCardCode.length() < 10) {
            return "";
        }
        // 截取前6后4
        String shortCardCode = originCardCode.substring(0, 6) +
                originCardCode.substring(originCardCode.length() - 4, originCardCode.length());
        return shortCardCode;
    }

    /**
     * Description: 银行卡号截取前6后4，中间被截掉的字符用“*”代替
     * @author: JiuDongDong
     * @param originCardCode      初始银行卡号
     * @return java.lang.String   初始银行卡号截取前6后4，中间被截掉的字符用“*”代替
     * date: 2018/9/28 13:29
     */
    public static String cipherOriginCardCode(String originCardCode) {
        if (StringUtils.isBlank(originCardCode)) {
            return "";
        }
        if (originCardCode.length() < 10) {
            return "";
        }
        // “*”的个数
        int num = originCardCode.length() - 10;
        String append = "";
        for (int i = 0; i < num; i++) {
            append += "*";
        }
        // 截取前6后4
        String shortCardCode = originCardCode.substring(0, 6) + append +
                originCardCode.substring(originCardCode.length() - 4, originCardCode.length());
        return shortCardCode;
    }

    /**
     * Description: 加“*”的卡号去除“*”
     * @author: JiuDongDong
     * @param cipherCardCode       加“*”的卡号
     * @return java.lang.String   去除“*”的短卡号
     * date: 2018/9/28 13:29
     */
    public static String deleteCipheredCodeFromShortCode(String cipherCardCode) {
        if (StringUtils.isBlank(cipherCardCode)) {
            return "";
        }
        if (cipherCardCode.length() < 10) {
            return "";
        }
        // 去除“*”
        String replace = StringUtils.replace(cipherCardCode, "*", "");
        return replace;
    }

    /**
     * Description: 获取银行卡尾号（后4位）
     * @author: JiuDongDong
     * @param cardCode            卡号（全卡号、短卡号、加*卡号都适用）
     * @return java.lang.String   银行卡尾号（后4位）
     * date: 2018/9/28 17:24
     */
    public static String getFinal4OfCardCode(String cardCode) {
        if (StringUtils.isBlank(cardCode)) {
            return "";
        }
        if (cardCode.length() < 4) {
            return "";
        }
        cardCode = cardCode.substring(cardCode.length() - 4, cardCode.length());
        return cardCode;
    }

    public static void main(String[] args) {
        String s = originToShort("6225768329489395");
        System.out.println(s);

        String s1 = cipherOriginCardCode("6225768329489395");
        System.out.println("6225768329489395");
        System.out.println(s1);

        String s2 = deleteCipheredCodeFromShortCode("622576******9395");
        System.out.println(s2);

        String final4OfCardCode = getFinal4OfCardCode("622576******9395");
        System.out.println(final4OfCardCode);
    }
}
