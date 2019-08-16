package com.ewfresh.pay.util.bob;

import java.math.BigDecimal;

import static com.ewfresh.pay.util.Constants.INTEGER_ONE;
import static com.ewfresh.pay.util.Constants.INTEGER_TWO;

/**
 * description: 分和元相互转化
 * @author: JiuDongDong
 * date: 2018/4/24.
 */
public class FenYuanConvert {

    /**
     * Description: 分转化为元
     * @author: JiuDongDong
     * @param fen  分
     * @return java.lang.String 元
     * date: 2018/4/24 19:48
     */
    public synchronized static BigDecimal fen2Yuan(String fen) {
        BigDecimal amount = new BigDecimal(fen).divide(new BigDecimal("100"));
        return amount;
    }

    /**
     * Description: 分转化为元
     * @author: JiuDongDong
     * @param fen  分
     * @return java.lang.String 元,包含2个小数位
     * date: 2018/4/24 19:48
     */
    public synchronized static String fen2YuanWithStringValue(String fen) {
        BigDecimal amount = new BigDecimal(fen).divide(new BigDecimal("100"));
        String strAmount = String.valueOf(amount);
        String[] split = strAmount.split("\\.");
        strAmount =
                split.length < INTEGER_TWO ? strAmount + ".00" : (split[INTEGER_ONE].length() == INTEGER_ONE ? strAmount + "0" : strAmount);
        return strAmount;
    }

    /**
     * Description: 元转化为分
     * @author: JiuDongDong
     * @param yuan 元
     * @return java.lang.String 分
     * date: 2018/4/24 19:51
     */
    public synchronized static BigDecimal yuan2Fen(String yuan) {
        BigDecimal amount = new BigDecimal(yuan).multiply(new BigDecimal("100"));
        String s = BOBOrderAmountFormat.deleteOrderAmountSUFFIX(amount.toString());
        return new BigDecimal(s);
    }

}
