package com.ewfresh.pay.util.bob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 订单金额去掉末尾的“.00”、“.0”
 * @author: JiuDongDong
 * date: 2018/5/30.
 */
public class BOBOrderAmountFormat {
    private static Logger logger = LoggerFactory.getLogger(BOBOrderAmountFormat.class);
    private static final String SUFFIX1 = ".0";
    private static final String SUFFIX2 = ".00";

    /**
     * Description: 去除订单金额末尾的“.00”、“.0”
     * @author: JiuDongDong
     * @param orderAmount  订单金额
     * @return java.lang.String 去除订单金额末尾的“.00”、“.0”的订单金额
     * date: 2018/5/30 11:22
     */
    public static synchronized String deleteOrderAmountSUFFIX(String orderAmount) {
        logger.info("deleteOrderAmountSUFFIX, the param: orderAmount = {}", orderAmount);
        String realAmount = "";
        if (orderAmount.endsWith(SUFFIX1)) {
            realAmount = orderAmount.replace(SUFFIX1, "");
        } else if (orderAmount.endsWith(SUFFIX2)) {
            realAmount = orderAmount.replace(SUFFIX2, "");
        } else {
            realAmount = orderAmount;
        }
        logger.info("deleteOrderAmountSUFFIX, the orderAmount = {}", realAmount);
        return realAmount;
    }

}
