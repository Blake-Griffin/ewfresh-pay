package com.ewfresh.pay.redisDao.impl;

import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.BOBOrderRedisDao;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * description:
 *      BOB相关的订单信息
 * @author: JiuDongDong
 * date: 2018/6/29.
 */
@Component
public class BOBOrderRedisDaoImpl implements BOBOrderRedisDao {
    private String E = "E";
    private String R = "R";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Description: 将订单描述放入Redis
     * @author: JiuDongDong
     * @param orderNo 订单号
     * @param orderDesc  订单描述
     * date: 2018/6/29 13:48
     */
    @Override
    public void putOrderDesc2Redis(String orderNo, String orderDesc) {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        opsForValue.set(Constants.PAY_ORDER_DESC + orderNo, orderDesc, 30L, TimeUnit.DAYS);
    }

    /**
     * Description: 根据订单号获取订单描述
     * @author: JiuDongDong
     * @param orderNo  发给银行的订单号
     * @return java.lang.String 订单描述
     * date: 2018/6/29 13:49
     */
    @Override
    public String getOrderDescByOrderNo(String orderNo) {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String orderDesc = opsForValue.get(Constants.PAY_ORDER_DESC + orderNo);
        return orderDesc;
    }

    /**
     * Description: 根据订单号删除Redis中的订单描述信息
     * @author: JiuDongDong
     * @param orderNo 订单号
     * date: 2018/6/29 13:49
     */
    @Override
    public void deleteOrderDescFromRedis(String orderNo) {
        redisTemplate.delete(Constants.PAY_ORDER_DESC + orderNo);
    }

    /**
     * Description: 商户发送订单信息到BOB的时间放入Redis
     * @author: JiuDongDong
     * @param orderNo 订单号(19位)
     * @param sendOrder2BOBTime   商户发送订单信息到BOB的时间
     * date: 2018/6/14 15:07
     */
    @Override
    public void putSendOrderToBOBTimeToRedis(String orderNo, String sendOrder2BOBTime) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(Constants.SEND_ORDER_TIME + orderNo, sendOrder2BOBTime);
        valueOperations.set(Constants.SEND_ORDER_TIME + orderNo, sendOrder2BOBTime, 30L, TimeUnit.DAYS);
    }

    /**
     * Description: 使用订单号（19位）获取商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @param orderNo  订单号（19位）
     * @return java.lang.String 商户发送订单信息到BOB的时间
     * date: 2018/6/14 15:08
     */
    @Override
    public String getSendOrderToBOBTimeFromRedis(String orderNo) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String sendOrderToBOBTime = valueOperations.get(Constants.SEND_ORDER_TIME + orderNo);
        return sendOrderToBOBTime;
    }

    /**
     * Description: 从Redis中删除商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @param orderNo 订单号
     * date: 2018/6/14 15:09
     */
    @Override
    public void deleteSendOrderToBOBTimeInRedis(String orderNo) {
        redisTemplate.delete(Constants.SEND_ORDER_TIME + orderNo);
    }

    /**
     * Description: 存储商户退款信息
     * @author: JiuDongDong
     * @param refundInfoVo 退款信息
     * date: 2018/6/30 15:08
     */
    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo) {
        String json = JsonUtil.toJson(refundInfoVo);
        RefundParam refundParam = refundInfoVo.getRefundParam();
        String outTradeNo = refundParam.getOutTradeNo();
        // 处理订单号，如果是20位的加E加R的订单支付订单号，则去除E/R转换成19位，如果是充值，则不处理
        outTradeNo = outTradeNo.endsWith(E) || outTradeNo.endsWith(R) ? outTradeNo.substring(Constants.INTEGER_ZERO, outTradeNo.length() - Constants.INTEGER_ONE) : outTradeNo;
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(Constants.PAY_REFUND_INFO + outTradeNo, json, 30L, TimeUnit.DAYS);
    }

    /**
     * Description: 从Redis获取退款信息
     * @author: JiuDongDong
     * @param orderNo  订单号：可以是20位的加E加R的订单支付订单号，也可以是19位的不加E/R的订单支付订单号，也可以是充值的订单号
     * @return com.ewfresh.pay.model.RefundParam 退款信息
     * date: 2018/6/30 15:17
     */
    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String orderNo) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        orderNo = orderNo.endsWith(E) || orderNo.endsWith(R) ? orderNo.substring(Constants.INTEGER_ZERO, orderNo.length() - Constants.INTEGER_ONE) : orderNo;
        String s = valueOperations.get(Constants.PAY_REFUND_INFO + orderNo);
        RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(s, RefundInfoVo.class);
        return refundInfoVo;
    }

    /**
     * Description: 从Redis中删除退款信息
     * @author: JiuDongDong
     * @param orderNo  订单号：可以是20位的加E加R的订单支付订单号，也可以是19位的不加E/R的订单支付订单号，也可以是充值的订单号
     * date: 2018/6/30 15:19
     */
    @Override
    public void deleteRefundOrderInfoInRedis(String orderNo) {
        orderNo = orderNo.endsWith(E) || orderNo.endsWith(R) ? orderNo.substring(Constants.INTEGER_ZERO, orderNo.length() - Constants.INTEGER_ONE) : orderNo;
        redisTemplate.delete(Constants.PAY_REFUND_INFO + orderNo);
    }

}
