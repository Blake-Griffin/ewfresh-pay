package com.ewfresh.pay.util;

import java.math.BigDecimal;

/**
 * description:
 *      格式化数字小数点为2位，规则：
 *      第3个非0小数数字往前进位
 * @author: JiuDongDong
 * date: 2018/4/28.
 */
public class FormatBigDecimal {

    /**
     * Description: 格式化数字小数点为2位
     * @author: JiuDongDong
     * @param bigDecimal  要格式化小数位为2位的参数
     * @return java.math.BigDecimal 格式化后的值
     * date: 2018/4/28 11:52
     */
    public static synchronized BigDecimal formatBigDecimal (BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        BigDecimal decimal = bigDecimal.setScale(3, BigDecimal.ROUND_DOWN).setScale(2, BigDecimal.ROUND_UP);
        return decimal;
    }
}
