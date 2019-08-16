package com.ewfresh.pay.redisDao.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.UnionPayH5RedisDao;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.ewfresh.pay.util.Constants.ORDER_ID;

/**
 * description: UnionPayH5相关的订单信息
 * @author: JiuDongDong
 * date: 2019/6/18.
 */
@Component
public class UnionPayH5RedisDaoImpl implements UnionPayH5RedisDao {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Description: 存储商户退款信息
     * @author: JiuDongDong
     * @param refundInfoVo  退款信息
     * @param hashKey  大key
     * date: 2019/6/18 16:49
     */
    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey) {
        String json = JsonUtil.toJson(refundInfoVo);
        String refundSeq = refundInfoVo.getRefundSeq();//msgSrcId + 生成28位的退款流水号
        RefundParam refundParam = refundInfoVo.getRefundParam();
//        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
//        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
//        hashOperations.put(Constants.PAY_BILL99_REFUND_INFO, outRequestNo, json);
        hashOperations.put(hashKey, refundSeq, json);
    }


    /**
     * Description: 从Redis获取退款信息(根据第三方交易订单号)
     * @author: JiuDongDong
     * @param refundSeq  //msgSrcId + 生成28位的退款流水号
     * @return com.ewfresh.pay.model.RefundParam 退款信息
     * date: 2019/6/18 16:50
     */
    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String refundSeq, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String s = (String) hashOperations.get(hashKey, refundSeq);
        //logger.info("hashOperations.keys()" + hashOperations.keys(hashKey));// Constants.PAY_BILL99_REFUND_INFO
        //logger.info("hashOperations.entries(): " + hashOperations.entries(hashKey));
        RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(s, RefundInfoVo.class);
        logger.info("refundInfoVo = {}", JsonUtil.toJson(refundInfoVo));
        return refundInfoVo;
    }

    /**
     * Description: 从Redis获取退款信息（获取所有的）
     * @author: JiuDongDong
     * @return Map<Object, Object> 所有的退款信息集合
     * date: 2019/6/18 16:51
     */
    @Override
    public Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<Object, Object> entries = hashOperations.entries(hashKey);// Constants.PAY_BILL99_REFUND_INFO
        return entries;
    }

    /**
     * Description: 从Redis中删除退款信息
     * @author: JiuDongDong
     * @param outRequestNo  退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * date: 2019/6/18 16:53
     */
    @Override
    public void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(hashKey, outRequestNo);
    }

    /**
     * Description: 存储待修改订单状态信息
     * @author: JiuDongDong
     * @param params 订单状态参数
     * @param hashKey  大key，小key为参数里的订单号
     * date: 2019/6/18 16:56
     */
    @Override
    public void putToUpdateStatusOrderInfo(Map<String, String> params, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String orderNo = params.get(Constants.BILL99_ID);// 订单号，作为小key
        String json = JsonUtil.toJson(params);
        hashOperations.put(hashKey, orderNo, json);
    }

    /**
     * Description: 从Redis获取待修改订单状态信息(根据订单号，小key)
     * @author: JiuDongDong
     * @param orderNo 小key
     * @param hashKey  大key
     * @return java.util.Map<java.lang.String,java.lang.String>  订单信息
     * date: 2019/6/18 16:59
     */
    @Override
    public Map<Object, Object> getToUpdateStatusOrderInfo(String orderNo, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
//        logger.info("hashOperations.keys()" + hashOperations.keys(hashKey));// Constants.PAY_BILL99_REFUND_INFO
//        logger.info("hashOperations.entries(): " + hashOperations.entries(hashKey));
        String s = (String) hashOperations.get(hashKey, orderNo);
        Map map = JsonUtil.jsonToObj(s, Map.class);
        return map;
    }

    /**
     * Description: 从Redis获取待修改订单状态信息（获取所有的）
     * @author: JiuDongDong
     * @param hashKey  大key
     * @return java.util.Map<java.lang.Object,java.lang.Object> 所有的待修改订单状态信息
     * date: 2019/6/18 17:00
     */
    @Override
    public Map<Object, Object> getAllToUpdateStatusOrderInfo(String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<Object, Object> entries = hashOperations.entries(hashKey);
        return entries;
    }

    /**
     * Description: 从Redis中删除待修改订单状态信息
     * @author: JiuDongDong
     * @param orderNo 小key
     * @param hashKey  大key
     * date: 2019/6/18 17:05
     */
    @Override
    public void deleteToUpdateStatusOrderInfoInRedis(String orderNo, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(hashKey, orderNo);
    }

    /**
     * Description: 从redis 删除退款参数
     * @author: JiuDongDong
     * @param orderId
     * @return: java.lang.String
     * date: 2019/6/18 17:08
     */
    @Override
    public void delReturnAmountParams(String orderId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.delete(Constants.RETURN_AMOUNT_PARAMS, orderId);
    }

    /**
     * Description: 从redis 获取退款参数
     * @author: JiuDongDong
     * @param orderId
     * date: 2019/6/18 15:54
     */
    public Map<String, String> getReturnAmountParams(String orderId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        String o = (String) hash.get(Constants.RETURN_AMOUNT_PARAMS, orderId);
        Map<String, String> map = ItvJsonUtil.jsonToObj(o, new TypeReference<Map<String, String>>() {});
        return map;
    }

    /**
     * Description: 使用银联支付时，将订单信息放入Redis
     * @author: JiuDongDong
     * @param hashKey
     * @param paramMap
     * date: 2019/7/4 15:40
     */
    @Override
    public void putPayOrderInfo(String hashKey, Map<String, String> paramMap) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String orderId = paramMap.get(ORDER_ID);
        hashOperations.put(hashKey, orderId, JsonUtil.toJson(paramMap));
    }

    /**
     * Description: 从Redis取出所有使用银联支付时，放入Redis的订单信息
     * @author: JiuDongDong
     * @param hashKey
     * @return java.util.Set<java.lang.String>
     * date: 2019/7/4 15:41
     */
    @Override
    public Map<Object, Object> getPayOrderInfo(String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<Object, Object> paramMap = hashOperations.entries(hashKey);
        return paramMap;
    }

    /**
     * Description: 删除Redis中存储的：使用银联支付时的订单信息
     * @author: JiuDongDong
     * @param hashKey
     * @param orderId
     * date: 2019/7/4 15:42
     */
    @Override
    public void delPayOrderInfo(String hashKey, String orderId) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(hashKey, orderId);
    }
}
