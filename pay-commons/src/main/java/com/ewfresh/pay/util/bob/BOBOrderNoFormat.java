package com.ewfresh.pay.util.bob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:
 *      商户侧orderNo格式化为BOB需要的19位订单号，或由银行返回的19位订单号格式化为商户原订单号
 * @author: JiuDongDong
 * date: 2018/5/30.
 */
public class BOBOrderNoFormat {
    private static Logger logger = LoggerFactory.getLogger(BOBOrderNoFormat.class);
    private static final int finalOrderNoLength = 19;//19位长度
    private static final String EARNEST = "E";//定金订单号的后缀
    private static final String TAIL = "R";//全款或尾款的订单号后缀
    private static final String EARNEST_PREFIX = "1";//定金订单号的前缀
    private static final String TAIL_PREFIX = "2";//全款或尾款的订单号前缀
    private static final String STR_ZERO = "0";//不足19位补0
    private static final Integer INTEGER_ZERO = 0;//0
    private static final Integer INTEGER_ONE = 1;//1
    private static final Short SHORT_ZERO = '0';//0
    private static final String FORMAT = "MMddHHmmss";//月日时分秒
    private static SimpleDateFormat simpleDateFormat;


    /**
     * Description: 将商户订单号(加E加R)格式化为19位的订单号
     * @author: JiuDongDong
     * @param oriOrderNo  商户订单号
     * @return java.lang.String 19位的订单号
     * date: 2018/5/30 11:22
     */
    public static synchronized String oriOrderNo2BOB19(String oriOrderNo) {
        logger.info("oriOrderNo2BOB19, the param: oriOrderNo = {}", oriOrderNo);
        if (oriOrderNo.length() == finalOrderNoLength) {
            return oriOrderNo;
        }
        // 去除末位的E或R
        String sb1 = oriOrderNo.substring(INTEGER_ZERO, oriOrderNo.length() - INTEGER_ONE);
        StringBuffer sb = new StringBuffer();
        // 首位加上E或R标识符
        if (oriOrderNo.endsWith(TAIL)) {
            logger.info("This is an all or tail order pay: " + oriOrderNo);
            sb = sb.append(TAIL_PREFIX);
        }
        if (oriOrderNo.endsWith(EARNEST)) {
            logger.info("This is an earnest order pay: " + oriOrderNo);
            sb = sb.append(EARNEST_PREFIX);
        }
        // 订单号加上时间戳（10位），放于首位E或R标识符之后
        simpleDateFormat = new SimpleDateFormat(FORMAT);
        String str = simpleDateFormat.format(new Date());
        sb = sb.append(str);
        int length = sb.length() + sb1.length();
        if (length < finalOrderNoLength) {
            for (int i = length; i < finalOrderNoLength; i++) {
                sb = sb.append(STR_ZERO);
            }
        }
        sb = sb.append(sb1);
        logger.info("oriOrderNo2BOB19, the 19OrderNo = {}", sb.toString());
        return sb.toString();
    }

    /**
     * Description: 将19位的订单号格式化为商户原始订单号(加E加R)
     * @author: JiuDongDong
     * @param orderNo19  19位的订单号
     * @return java.lang.Integer 商户订单号
     * date: 2018/5/30 14:33
     */
    public static synchronized String bob19OrderNo2OriWithER(String orderNo19) {
        logger.info("bob19OrderNo2OriWithER, the param: orderNo19 = {}", orderNo19);
        StringBuffer sb = new StringBuffer(orderNo19);
        String suffix = "";
        if (orderNo19.startsWith(EARNEST_PREFIX)) {
            suffix = EARNEST;
        }
        if (orderNo19.startsWith(TAIL_PREFIX)) {
            suffix = TAIL;
        }
        // 去除首位类型标识和时间戳（10位）
        sb = sb.delete(0, 11);
        while (true) {
            if (sb.charAt(INTEGER_ZERO) != (int) SHORT_ZERO) {
                break;
            }
            sb = sb.deleteCharAt(INTEGER_ZERO);
        }
        sb = sb.append(suffix);
        return sb.toString();
    }

    /**
     * Description: 将19位的订单号格式化为商户原始订单号(不加E不加R)
     * @author: JiuDongDong
     * @param orderNo19  19位的订单号
     * @return java.lang.Integer 商户订单号
     * date: 2018/6/12 13:33
     */
    public static synchronized String bob19OrderNo2OriWithoutER(String orderNo19) {
        logger.info("bob19OrderNo2OriWithoutER, the param: orderNo19 = {}", orderNo19);
        String oriOrderNoWithER = bob19OrderNo2OriWithER(orderNo19);
        StringBuffer sb = new StringBuffer(oriOrderNoWithER);
        sb = sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Description: 将19位的订单号格式化为20位的加E加R的订单号
     * @author: JiuDongDong
     * @param orderNo19  19位的订单号
     * @return java.lang.Integer 20位的订单号
     * date: 2018/6/26 14:46
     */
    public static synchronized String bob19OrderNoTo20WithER(String orderNo19) {
        logger.info("bob19OrderNoTo20WithER, the param: orderNo19 = {}", orderNo19);
        // 末位加上E或R标识符
        StringBuffer sb = new StringBuffer(orderNo19);
        String suffix = "";
        if (orderNo19.startsWith(EARNEST_PREFIX)) {
            suffix = EARNEST;
        }
        if (orderNo19.startsWith(TAIL_PREFIX)) {
            suffix = TAIL;
        }
        sb = sb.append(suffix);
        return sb.toString();
    }

    /**
     * Description: 将20位的订单号格式化为19位的不加E加R的订单号
     * @author: JiuDongDong
     * @param orderNo20  20位的订单号
     * @return java.lang.Integer 19位的订单号
     * date: 2018/6/26 14:50
     */
    public static synchronized String bob20OrderNoTo19WithoutER(String orderNo20) {
        logger.info("bob20OrderNoTo19WithoutER, the param: orderNo20 = {}", orderNo20);
        // 删除末位上的E或R标识符
        orderNo20 = orderNo20.substring(INTEGER_ZERO, orderNo20.length() - INTEGER_ONE);
        return orderNo20;
    }

    /**
     * Description: 将20位的订单号格式化为原始的加E加R的订单号
     * @author: JiuDongDong
     * @param orderNo20  20位的订单号
     * @return java.lang.Integer 原始的订单号
     * date: 2018/6/26 15:50
     */
    public static synchronized String bob20OrderNoToOriWithER(String orderNo20) {
        logger.info("bob20OrderNoToOriWithER, the param: orderNo20 = {}", orderNo20);
        String orderNo19 = bob20OrderNoTo19WithoutER(orderNo20);
        String oriOrderNo = bob19OrderNo2OriWithER(orderNo19);
        return oriOrderNo;
    }

}
