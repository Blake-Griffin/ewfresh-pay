package com.ewfresh.pay.util.bob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * description:
 *      商户侧orderNo格式化为BOB需要的12位退款流水号
 * @author: JiuDongDong
 * date: 2018/4/25.
 */
public class BOBRefundSeqFormat {
    private static Logger logger = LoggerFactory.getLogger(BOBRefundSeqFormat.class);
    private static final int refundSeqLengthBOB = 12;
    private static final int refundSeqLengthUNION = 28;
    private static final int ONE = 1;
    private static final String PREFIX = "0";
    private static final String EARNEST = "E";//定金订单号的后缀
    private static final String TAIL = "R";//全款或尾款的订单号后缀
    private static final String FORMAT = "mmss";//月日时分秒
    private static final String FORMAT_UNION = "yyyyMMddHHmmssSSS";//年月日时分秒毫秒
    private static SimpleDateFormat simpleDateFormat;
    private static final Integer MINUTE_SECOND_LENGTH_BOB = 4;
    private static final Integer MINUTE_SECOND_LENGTH_UNION = 17;
    private static final Integer ZERO = 0;

    /**
     * Description: 将商户订单号格式化为12位的退款流水号
     * @author: JiuDongDong
     * @param orderNo  商户订单号
     * @return java.lang.String 12位的流水号
     * date: 2018/4/25 11:46
     */
    public static synchronized String orderNo2BOBRefundString(String orderNo) {
        logger.info("orderNo2BOBRefundString, the param: orderNo = {}", orderNo);
        StringBuffer sb = new StringBuffer(orderNo);
        if (orderNo.endsWith(EARNEST) || orderNo.endsWith(TAIL)) {
            sb = sb.deleteCharAt(orderNo.length() - ONE);
        }
        int length = sb.length();
        if (length < refundSeqLengthBOB - MINUTE_SECOND_LENGTH_BOB) {
            for (int i = length; i < refundSeqLengthBOB - MINUTE_SECOND_LENGTH_BOB; i ++) {
                sb = sb.insert(0, PREFIX);
            }
        }
        simpleDateFormat = new SimpleDateFormat(FORMAT);
        String str = simpleDateFormat.format(new Date());
        sb = sb.insert(ZERO, str);
        logger.info("orderNo2BOBRefundString, the result: orderNo = {}", sb.toString());
        return sb.toString();
    }

    /**
     * Description: 将商户订单号格式化为28位的退款流水号
     * @author: JiuDongDong
     * @param orderNo  商户订单号
     * @return java.lang.String 28位的流水号
     * date: 2019/6/27 10:04
     */
    public static synchronized String orderNo2UnionPayRefundSequence(String orderNo) {
        logger.info("orderNo2UnionPayRefundSequence, the param: orderNo = {}", orderNo);
        StringBuffer sb = new StringBuffer(orderNo);
        if (orderNo.endsWith(EARNEST) || orderNo.endsWith(TAIL)) {
            sb = sb.deleteCharAt(orderNo.length() - ONE);
        }
        int length = sb.length();
        if (length < refundSeqLengthUNION - MINUTE_SECOND_LENGTH_UNION) {
            for (int i = length; i < refundSeqLengthUNION - MINUTE_SECOND_LENGTH_UNION; i ++) {
                sb = sb.insert(0, PREFIX);
            }
        }
        simpleDateFormat = new SimpleDateFormat(FORMAT_UNION);
        String str = simpleDateFormat.format(new Date());
        sb = sb.insert(ZERO, str);
        logger.info("orderNo2UnionPayRefundSequence, the result: orderNo = {}", sb.toString());
        return sb.toString();
    }

    /**
     * Description: 将12位的流水号格式化为商户订单号
     * @author: JiuDongDong
     * @param refundSeq  12位的流水号
     * @return java.lang.Integer 商户订单号
     * date: 2018/4/25 14:18
     */
    public static synchronized Integer bob12RefundString2OrderNo(String refundSeq) {
        logger.info("bob19OrderNo2OriWithER, the param: refundSeq = {}", refundSeq);
        String sb1 = refundSeq.substring(5,12);
        StringBuffer sb = new StringBuffer(sb1);
        while (true) {
            if (sb.charAt(0) != 48) {
                break;
            }
            sb = sb.deleteCharAt(0);
        }
        return Integer.parseInt(sb.toString());
    }

}
