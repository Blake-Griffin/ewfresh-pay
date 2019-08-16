package com.ewfresh.pay.redisDao;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.RefundInfoVo;

import java.util.Map;

/**
 * description: UnionPayWebWap相关的订单信息
 * @author: JiuDongDong
 * date: 2019/5/7.
 */
public interface UnionPayWebWapOrderRedisDao {
    // 存储商户退款信息     jiudongdong
    void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey);
    // 从Redis获取退款信息(根据第三方交易订单号)     jiudongdong
    RefundInfoVo getRefundOrderInfoFromRedis(String outRequestNo, String hashKey);
    // 从Redis获取退款信息（获取所有的）     jiudongdong
    Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey);
    // 从Redis中删除退款信息     jiudongdong
    void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey);


    // 存储待修改订单状态信息     jiudongdong
    void putToUpdateStatusOrderInfo(Map<String, String> params, String hashKey);
    // 从Redis获取待修改订单状态信息(根据订单号，小key)     jiudongdong
    Map<Object, Object> getToUpdateStatusOrderInfo(String orderNo, String hashKey);
    // 从Redis获取待修改订单状态信息（获取所有的）     jiudongdong
    Map<Object, Object> getAllToUpdateStatusOrderInfo(String hashKey);
    // 从Redis中删除待修改订单状态信息     jiudongdong
    void deleteToUpdateStatusOrderInfoInRedis(String orderNo, String hashKey);
    // 从redis 删除退款参数
    void delReturnAmountParams(String orderId);
    // 从redis 获取退款参数
    Map<String, String> getReturnAmountParams(String orderId);
    // 放置payFlow信息到Redis
    void putPayFlowToRedis(String key, PayFlow payFlow);
    // 取出Redis中的payFlow
    Map<Object, Object> getPayFlowFromRedis(String key);
    // 删除Redis里的payFlow
    void delPayFlowFromRedis(String key, String channelFlowId);
}
