package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.UnionPayH5RedisDao;
import com.ewfresh.pay.redisService.UnionPayH5RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * description: UnionPayH5相关的订单信息
 * @author: JiuDongDong
 * date: 2019/6/18.
 */
@Service
public class UnionPayH5RedisServiceImpl implements UnionPayH5RedisService {
    @Autowired
    private UnionPayH5RedisDao unionPayH5RedisDao;

    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey) {
        unionPayH5RedisDao.putRefundOrderInfoToRedis(refundInfoVo, hashKey);
    }

    @Override
    public Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey) {
        return unionPayH5RedisDao.getAllRefundOrderInfoFromRedis(hashKey);
    }

    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String refundSeq, String hashKey) {
        return unionPayH5RedisDao.getRefundOrderInfoFromRedis(refundSeq, hashKey);
    }

    @Override
    public void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey) {
        unionPayH5RedisDao.deleteRefundOrderInfoInRedis(outRequestNo, hashKey);
    }

    @Override
    public void delReturnAmountParams(String orderId) {
        unionPayH5RedisDao.delReturnAmountParams(orderId);
    }

    @Override
    public Map<String, String> getReturnAmountParams(String orderId) {
        Map<String, String> map = unionPayH5RedisDao.getReturnAmountParams(orderId);
        return map;
    }

    @Override
    public void putPayOrderInfo(String hashKey, Map<String, String> paramMap) {
        unionPayH5RedisDao.putPayOrderInfo(hashKey, paramMap);
    }

    @Override
    public Map<Object, Object> getPayOrderInfo(String hashKey) {
        return unionPayH5RedisDao.getPayOrderInfo(hashKey);
    }

    @Override
    public void delPayOrderInfo(String hashKey, String orderId) {
        unionPayH5RedisDao.delPayOrderInfo(hashKey, orderId);
    }
}
