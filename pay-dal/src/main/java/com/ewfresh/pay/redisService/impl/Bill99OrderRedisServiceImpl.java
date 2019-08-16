package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.Bill99OrderRedisDao;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * description: Bill99相关的订单信息
 * @author: JiuDongDong
 * date: 2018/8/2.
 */
@Service
public class Bill99OrderRedisServiceImpl implements Bill99OrderRedisService {
    @Autowired
    private Bill99OrderRedisDao bill99OrderRedisDao;


    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey) {
        bill99OrderRedisDao.putRefundOrderInfoToRedis(refundInfoVo, hashKey);
    }

    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String outRequestNo, String hashKey) {
        return bill99OrderRedisDao.getRefundOrderInfoFromRedis(outRequestNo, hashKey);
    }

    @Override
    public Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey) {
        return bill99OrderRedisDao.getAllRefundOrderInfoFromRedis(hashKey);
    }

    @Override
    public void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey) {
        bill99OrderRedisDao.deleteRefundOrderInfoInRedis(outRequestNo, hashKey);

    }

    @Override
    public void putToUpdateStatusOrderInfo(Map<String, String> params, String hashKey) {
        bill99OrderRedisDao.putToUpdateStatusOrderInfo(params, hashKey);
    }

    @Override
    public Map<Object, Object> getToUpdateStatusOrderInfo(String orderNo, String hashKey) {
        Map<Object, Object> orderInfo = bill99OrderRedisDao.getToUpdateStatusOrderInfo(orderNo, hashKey);
        return orderInfo;
    }

    @Override
    public Map<Object, Object> getAllToUpdateStatusOrderInfo(String hashKey) {
        Map<Object, Object> allToUpdateStatusOrderInfo = bill99OrderRedisDao.getAllToUpdateStatusOrderInfo(hashKey);
        return allToUpdateStatusOrderInfo;
    }

    @Override
    public void deleteToUpdateStatusOrderInfoInRedis(String orderNo, String hashKey) {
        bill99OrderRedisDao.deleteToUpdateStatusOrderInfoInRedis(orderNo, hashKey);
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
        bill99OrderRedisDao.delReturnAmountParams(orderId);
    }

    /**
     * Description: 从redis 获取退款参数
     * @author: JiuDongDong
     * @param orderId
     * date: 2019/5/29 15:54
     */
    public Map<String, String> getReturnAmountParams(String orderId) {
        Map<String, String> map = bill99OrderRedisDao.getReturnAmountParams(orderId);
        return map;
    }

    @Override
    public void setWithdrawIdToredis(String withdrawId, String key) {
        bill99OrderRedisDao.setWithdrawIdToredis(withdrawId, key);
    }

    public List<String> getWithdrawIdFromRedis(String key) {
        return bill99OrderRedisDao.getWithdrawIdFromRedis(key);
    }

    /**
     * Description: 获取HAT提现id
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    @Override
    public Set getHATWithdrawIdMap(String hashKey) {
        return bill99OrderRedisDao.getHATWithdrawIdMap(hashKey);
    }

    /**
     * Description: 获取HAT提现vo
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    @Override
    public Bill99WithdrawAccountVo getHATWithdrawVoMap(String hashKey, String key) {
        return bill99OrderRedisDao.getHATWithdrawVoMap(hashKey, key);
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
        bill99OrderRedisDao.putHATWithdrawVoMap(vo, hashKey);
    }

    /**
     * Description: 从redis删除HAT提现类
     * @author: ZhaoQun
     * @param hashKey
     * @param key
     * @return:
     * date: 2018/10/24 17:45
     */
    @Override
    public void delHATWithdrawVoItem(String hashKey, String key) {
        bill99OrderRedisDao.delHATWithdrawVoItem(hashKey, key);
    }

}
