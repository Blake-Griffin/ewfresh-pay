package com.ewfresh.pay.util.boc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *      商户侧orderNo格式化为BOC需要的12位退款流水号
 * @author: JiuDongDong
 * date: 2018/5/31.
 */
public class BOCRefundSeqFormat {
    private static Logger logger = LoggerFactory.getLogger(BOCRefundSeqFormat.class);
    private static final int refundSeqLength = 12;
    private static final String PREFIX = "0";

    /**
     * Description: 将商户订单号格式化为12位的退款流水号
     * @author: JiuDongDong
     * @param orderNo  商户订单号
     * @return java.lang.String 12位的流水号
     * date: 2018/5/31 10:26
     */
    public static synchronized String orderNo2BOCRefundString(String orderNo) {
        logger.info("orderNo2BOCRefundString, the param: orderNo = {}", orderNo);
        StringBuffer sb = new StringBuffer(orderNo);
        int length = orderNo.length();
        if (length < refundSeqLength) {
            for (int i = length; i < refundSeqLength; i++) {
                sb = sb.insert(0, PREFIX);
            }
        }
        logger.info("orderNo2BOCRefundString, the result: orderNo = {}", sb.toString());
        return sb.toString();
    }

    /**
     * Description: 将12位的流水号格式化为商户订单号
     * @author: JiuDongDong
     * @param refundSeq  12位的流水号
     * @return java.lang.Integer 商户订单号
     * date: 2018/5/31 10:29
     */
    public static synchronized Integer boc12RefundString2OrderNo(String refundSeq) {
        logger.info("boc12OrderNo2Ori, the param: refundSeq = {}", refundSeq);
        StringBuffer sb = new StringBuffer(refundSeq);
        while (true) {
            if (sb.charAt(0) != 48) {
                break;
            }
            sb = sb.deleteCharAt(0);
        }
        return Integer.parseInt(sb.toString());
    }

}
