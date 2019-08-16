package com.ewfresh.pay.redisDao.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.Bill99OrderRedisDao;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * description: Bill99相关的订单信息
 * @author: JiuDongDong
 * date: 2018/8/2.
 */
@Component
public class Bill99OrderRedisDaoImpl implements Bill99OrderRedisDao {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Description: 存储商户退款信息
     * @author: JiuDongDong
     * @param refundInfoVo  退款信息
     * @param hashKey  大key
     * date: 2018/8/2 13:32
     */
    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey) {
        String json = JsonUtil.toJson(refundInfoVo);
        RefundParam refundParam = refundInfoVo.getRefundParam();
        String refundSeq = refundInfoVo.getRefundSeq();
//        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
//        hashOperations.put(Constants.PAY_BILL99_REFUND_INFO, outRequestNo, json);
        hashOperations.put(hashKey, refundSeq, json);
    }


    /**
     * Description: 从Redis获取退款信息(根据第三方交易订单号)
     * @author: JiuDongDong
     * @param outRequestNo  退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @return com.ewfresh.pay.model.RefundParam 退款信息
     * date: 2018/8/2 14:16
     */
    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String outRequestNo, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String s = (String) hashOperations.get(hashKey, outRequestNo);
        logger.info("hashOperations.keys()" + hashOperations.keys(hashKey));// Constants.PAY_BILL99_REFUND_INFO
        //logger.info("hashOperations.entries(): " + hashOperations.entries(hashKey));
        RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(s, RefundInfoVo.class);
        return refundInfoVo;
    }

    /**
     * Description: 从Redis获取退款信息（获取所有的）
     * @author: JiuDongDong
     * @return Map<Object, Object> 所有的退款信息集合
     * date: 2018/8/7 20:23
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
     * date: 2018/8/2 13:42
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
     * date: 2018/10/19 14:20
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
     * date: 2018/10/19 14:28
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
     * date: 2018/10/19 14:48
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
     * date: 2018/10/19 14:28
     */
    @Override
    public void deleteToUpdateStatusOrderInfoInRedis(String orderNo, String hashKey) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(hashKey, orderNo);
    }

    /**
     * Description: 从redis 删除退款参数
     * @author: ZhaoQun
     * @param orderId
     * @return: java.lang.String
     * date: 2018/8/8 15:10
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
     * date: 2019/5/29 15:54
     */
    public Map<String, String> getReturnAmountParams(String orderId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        String o = (String) hash.get(Constants.RETURN_AMOUNT_PARAMS, orderId);
        Map<String, String> map = ItvJsonUtil.jsonToObj(o, new TypeReference<Map<String, String>>() {});
        return map;
    }

    /**
     * Description: 存储HAT提现vo
     * @author: zhaoqun
     * @param vo  Bill99WithdrawAccountVo
     * @param hashKey  大key
     * date: 2018/10/24 11:28
     */
    @Override
    public void putHATWithdrawVoMap(Bill99WithdrawAccountVo vo, String hashKey) {
        String json = JsonUtil.toJson(vo);
        String id = vo.getWithdrawId();
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(hashKey, id, json);
    }
    /**
     * Description: 获取HAT提现id
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    @Override
    public Set<String> getHATWithdrawIdMap(String hashKey) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String , Object> entries = hashOperations.entries(hashKey);
        Set<String> keySet = entries.keySet();
        return keySet;
    }
    /**
     * Description: 获取HAT提现vo
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    @Override
    public Bill99WithdrawAccountVo getHATWithdrawVoMap(String hashKey, String key) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String s = (String) hashOperations.get(hashKey, key);
        Bill99WithdrawAccountVo vo = ItvJsonUtil.jsonToObj(s,new TypeReference<Bill99WithdrawAccountVo>(){});
        return vo;
    }
    /**
     * Description: 从redis删除HAT提现类
     * @author:  ZhaoQun
     * @param hashKey  大key
     * @param key       小key
     * @return:
     * date: 2018/10/24 17:45
     */
    @Override
    public void delHATWithdrawVoItem(String hashKey, String key) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(hashKey, key);
    }
    @Override
    public void setWithdrawIdToredis(String withdrawId, String key) {
        if (StringUtils.isBlank(withdrawId)) {
            return;
        }
        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
        listOperations.leftPush(key, withdrawId);
    }

    public List<String > getWithdrawIdFromRedis(String key) {
        ListOperations<String, String> listOperations = stringRedisTemplate.opsForList();
        List<String > list = new ArrayList<>();
        // 一次性取完
        for (;;) {
            String s = listOperations.rightPop(key);
            if (StringUtils.isBlank(s)) {
                break;
            }
            list.add(s);
        }
        return list;
    }


}
