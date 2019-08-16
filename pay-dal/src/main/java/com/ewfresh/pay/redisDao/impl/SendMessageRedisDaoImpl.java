package com.ewfresh.pay.redisDao.impl;

import com.ewfresh.pay.model.vo.OrderInfoVo;
import com.ewfresh.pay.redisDao.SendMessageRedisDao;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_orderId;

/**
 * description: 从Redis中获取快钱快捷支付临时信息
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
@Component
public class SendMessageRedisDaoImpl implements SendMessageRedisDao {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * Description: 订单快捷支付时，验证码放入Redis，时效2分钟
     * @author: JiuDongDong
     * @param orderNo       订单号
     * @param validCode     验证码
     * @param expireTime    失效时间（单位为分）
     * date: 2018/9/28 17:53
     */
    @Override
    public void putValidCode2Redis(String orderNo, String validCode, Long expireTime) {
        logger.info("Put valid code to redis, orderNo = " + orderNo + ", validCode = " + validCode + ", expireTime = " + expireTime);
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(Constants.QUICK_PAY_VALID_CODE_KEY + orderNo, validCode, expireTime, TimeUnit.MINUTES);
    }

    /**
     * Description: 订单快捷支付时，放入Redis的验证码
     * @author: JiuDongDong
     * @param orderNo  订单号
     * @return java.lang.String 订单快捷支付时，放入Redis的验证码
     * date: 2018/9/28 18:39
     */
    public String getValidCodeFromRedis(String orderNo) {
        logger.info("Get valid code from redis, orderNo = " + orderNo);
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String validCode = valueOperations.get(Constants.QUICK_PAY_VALID_CODE_KEY + orderNo);
        logger.info("validCode = " + validCode + " of orderNo = " + orderNo);
        return validCode;
    }

    /**
     * Description: 订单快捷支付时，订单信息放入Redis
     * @author: JiuDongDong
     * @param orderInfoVo 订单快捷支付时的订单信息
     * @param expireTime  过期时间
     * date: 2018/9/29 11:47
     */
    @Override
    public void putTradeInfo2Redis(OrderInfoVo orderInfoVo, Long expireTime) {
        logger.info("SendMessageRedisDaoImpl.putTradeInfo2Redis's params are: orderInfoVo = " + JsonUtil.toJson(orderInfoVo));
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        String externalRefNumber = orderInfoVo.getExternalRefNumber();// 订单号
        String txnType = orderInfoVo.getTxnType();// 交易类型编码，PUR 消费交易，RFD 退货交易
        if (Constants.TXNTYPE_PUR.equals(txnType)) {
            hashOperations.put(Constants.QUICK_PAY_TRADE_PUR, externalRefNumber, JsonUtil.toJson(orderInfoVo));
            logger.info("Put PUR trade info to Redis, tradeInfoRedisVo");
        }
        if (Constants.TXNTYPE_RFD.equals(txnType)) {
            hashOperations.put(Constants.QUICK_PAY_TRADE_RFD, externalRefNumber, JsonUtil.toJson(orderInfoVo));
            logger.info("Put RFD trade info to Redis, tradeInfoRedisVo");
        }
    }

    /**
     * Description: 从Redis中获取快钱交易信息
     * @author: JiuDongDong
     * @param hashKey hash的大key，eg："{quickPayPUR}"  订单快捷支付时，快钱未及时响应的订单支付信息
                                      "{quickPayRFD}"  订单快捷退货时，快钱未及时响应的订单支付信息
     * @return com.ewfresh.pay.model.vo.OrderInfoVo
     * date: 2018/9/29 13:30
     */
    @Override
    public List<OrderInfoVo> getTradeInfoFromRedis(String hashKey) {
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        Map<Object, Object> entries = hashOperations.entries(hashKey);
        if (MapUtils.isEmpty(entries)) {
//            logger.info("There is no pay trade info in Redis now");
            return null;
        }
        logger.info("trade info in Redis is: " + JsonUtil.toJson(entries));
        List<OrderInfoVo> orderInfoVoList = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object value : values) {
            String val = (String) value;
            OrderInfoVo orderInfoVo = JsonUtil.jsonToObj(val, OrderInfoVo.class);
            orderInfoVoList.add(orderInfoVo);
        }
        return orderInfoVoList;
    }

    /**
     * Description: 银联---订单支付时，订单信息放入Redis
     * @author: JiuDongDong
     * @param params 订单支付时的订单信息
     * @param expireTime 过期时间
     * date: 2019/5/9 11:51
     */
    @Override
    public void putUnionPayTradeInfo2Redis(Map<String, String> params, Long expireTime) {
        logger.info("SendMessageRedisDaoImpl.putUnionPayTradeInfo2Redis's params are: params = {}", JsonUtil.toJson(params));
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        String orderId = params.get(param_orderId);// 订单号
        hashOperations.put(Constants.UNION_PAY_TRADE_PUR, orderId, JsonUtil.toJson(params));
        logger.info("Put union pay PUR trade info to Redis, params = {}", JsonUtil.toJson(params));
    }

    /**
     * Description: 从Redis中获取银联交易信息
     * @author: JiuDongDong
     * @param hashKey hash的大key，eg："{unionPayPUR}"  银联支付时，银联响应的信息
     * date: 2019/5/9 17:29
     */
    @Override
    public List<Map<String, String>> getUnionPayTradeInfoFromRedis(String hashKey) {
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        Map<Object, Object> entries = hashOperations.entries(hashKey);
        if (MapUtils.isEmpty(entries)) {
//            logger.info("There is no pay trade info in Redis now");
            return null;
        }
        logger.info("UnionPay trade info in Redis is: {}", JsonUtil.toJson(entries));
        List<Map<String, String>> orderInfoVoList = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object value : values) {
            String val = (String) value;
            Map<String, String> orderInfoVo = JsonUtil.jsonToObj(val, new HashMap<String, String>().getClass());
            orderInfoVoList.add(orderInfoVo);
        }
        return orderInfoVoList;
    }

    /**
     * Description: 删除订单快捷支付时放入Redis的订单信息
     * @author: JiuDongDong
     * @param orderInfoVo  订单快捷支付时的订单信息
     * date: 2018/9/29 11:58
     */
    @Override
    public void deleteTradeInfoFromRedis(OrderInfoVo orderInfoVo) {
        logger.info("SendMessageRedisDaoImpl.deleteTradeInfoFromRedis's params are: tradeInfoRedisVo = " + JsonUtil.toJson(orderInfoVo));
        String externalRefNumber = orderInfoVo.getExternalRefNumber();// 订单号
        String txnType = orderInfoVo.getTxnType();// 交易类型编码，PUR 消费交易，RFD 退货交易
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        if ("PUR".equals(txnType)) {
            hashOperations.delete(Constants.QUICK_PAY_TRADE_PUR, externalRefNumber);
            logger.info("Delete PUR trade info from Redis, tradeInfoRedisVo = " + JsonUtil.toJson(orderInfoVo));
        }
        if ("RFD".equals(txnType)) {
            hashOperations.delete(Constants.QUICK_PAY_TRADE_RFD, externalRefNumber);
            logger.info("Delete RFD trade info from Redis, tradeInfoRedisVo = " + JsonUtil.toJson(orderInfoVo));
        }

    }

    /**
     * Description: 银联---删除银联交易放入Redis的订单信息
     * @author: JiuDongDong
     * @param orderInfoVo
     * date: 2019/5/10 9:13
     */
    @Override
    public void deleteUnionPayTradeInfoFromRedis(Map<String, String> orderInfoVo) {
        logger.info("SendMessageRedisDaoImpl.deleteUnionPayTradeInfoFromRedis's params are: tradeInfoRedisVo = {}", JsonUtil.toJson(orderInfoVo));
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        String orderId = orderInfoVo.get(param_orderId);// 订单号
        hashOperations.delete(Constants.UNION_PAY_TRADE_PUR, orderId);
        logger.info("Delete PUR trade info from Redis, tradeInfoRedisVo = {}", JsonUtil.toJson(orderInfoVo));
    }

//    /**
//     * Description: 从Redis中（支付定金时放入）获取待发送短信的订单
//     * @author: JiuDongDong
//     * @return com.ewfresh.order.model.vo.SendMessageRedisVo 待发送短信的订单信息
//     * date: 2018/5/9 17:54
//     */
//    @Override
//    public List<SendMessageRedisVo> getSendMessageOrder() {
//        logger.info("Now is going to get send tail money message from redis");
//        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
//        List<SendMessageRedisVo> sendMessageRedisVoList = new ArrayList<>();
//        // 一次性取完
//        for (;;) {
//            String s = listOperations.rightPop(Constants.SEND_MESSAGE_TIME);
//            if (StringUtils.isBlank(s)) {
//                break;
//            }
//            SendMessageRedisVo sendMessageRedisVo = JsonUtil.jsonToObj(s, SendMessageRedisVo.class);
//            sendMessageRedisVoList.add(sendMessageRedisVo);
//        }
//        logger.info("The number of send tail money message is: " + sendMessageRedisVoList.size());
//        return sendMessageRedisVoList;
//    }
//
//    /**
//     * Description: 往Redis中存放待发送短信的订单（临时存储，用完即删）
//     * @author: JiuDongDong
//     * @param sendMessageRedisVo redis中存放待发短信的订单信息
//     * @param sendMessageTime 发送短信时间
//     * date: 2018/5/9 17:57
//     */
//    @Override
//    public void putSendMessageOrder2Redis(Date sendMessageTime, SendMessageRedisVo sendMessageRedisVo) {
//        if (sendMessageRedisVo == null) {
//            return;
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
//        String strKey = sdf.format(sendMessageTime);
//        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
//        listOperations.leftPush(Constants.SEND_MESSAGE_TEMP + strKey, JsonUtil.toJson(sendMessageRedisVo));
//    }
//
//    /**
//     * Description: 从Redis中获取待发送短信的订单（putSendMessageOrder2Redis备份的数据）
//     * @author: JiuDongDong
//     * @param time 发短信的时间
//     * date: 2018/5/11 17:27
//     */
//    @Override
//    public List<SendMessageRedisVo> getSendMessageOrderFromRedis(String time) {
//        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
//        List<SendMessageRedisVo> sendMessageRedisVoList = new ArrayList<>();
//        while (true) {
//            String s = listOperations.rightPop(Constants.SEND_MESSAGE_TEMP + time);
//            if (StringUtils.isBlank(s)) {
//                break;
//            }
//            SendMessageRedisVo sendMessageRedisVo = JsonUtil.jsonToObj(s, SendMessageRedisVo.class);
//            sendMessageRedisVoList.add(sendMessageRedisVo);
//        }
//        return sendMessageRedisVoList;
//    }

}
