package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.UnionPayWebWapOrderRedisDao;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * description: UnionPayWebWap相关的订单信息
 * @author: JiuDongDong
 * date: 2019/5/7.
 */
@Service
public class UnionPayWebWapOrderRedisServiceImpl implements UnionPayWebWapOrderRedisService {
    @Autowired
    private UnionPayWebWapOrderRedisDao unionPayWebWapOrderRedisDao;

    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey) {
        unionPayWebWapOrderRedisDao.putRefundOrderInfoToRedis(refundInfoVo, hashKey);
    }

    @Override
    public Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey) {
        return unionPayWebWapOrderRedisDao.getAllRefundOrderInfoFromRedis(hashKey);
    }

    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String outRequestNo, String hashKey) {
        return unionPayWebWapOrderRedisDao.getRefundOrderInfoFromRedis(outRequestNo, hashKey);
    }

    @Override
    public void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey) {
        unionPayWebWapOrderRedisDao.deleteRefundOrderInfoInRedis(outRequestNo, hashKey);
    }

    @Override
    public void delReturnAmountParams(String orderId) {
        unionPayWebWapOrderRedisDao.delReturnAmountParams(orderId);
    }

    @Override
    public Map<String, String> getReturnAmountParams(String orderId) {
        Map<String, String> map = unionPayWebWapOrderRedisDao.getReturnAmountParams(orderId);
        return map;
    }

    @Override
    public void putPayFlowToRedis(String key, PayFlow payFlow) {
        unionPayWebWapOrderRedisDao.putPayFlowToRedis(key, payFlow);
    }

    @Override
    public Map<Object, Object> getPayFlowFromRedis(String key) {
        return unionPayWebWapOrderRedisDao.getPayFlowFromRedis(key);
    }

    @Override
    public void delPayFlowFromRedis(String key, String channelFlowId) {
        unionPayWebWapOrderRedisDao.delPayFlowFromRedis(key, channelFlowId);
    }
}
