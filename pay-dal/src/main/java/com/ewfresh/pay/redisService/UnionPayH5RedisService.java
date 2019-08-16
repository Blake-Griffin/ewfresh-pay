package com.ewfresh.pay.redisService;

import com.ewfresh.pay.model.vo.RefundInfoVo;

import java.util.Map;

/**
 * description: UnionPayH5相关的订单信息
 * @author: JiuDongDong
 * date: 2019/6/18.
 */
public interface UnionPayH5RedisService {
    // 存储商户退款信息     jiudongdong
    void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey);
    // 从Redis获取退款信息（获取所有的）     jiudongdong
    Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey);
    // 从Redis获取退款信息(根据退款流水号)     jiudongdong
    RefundInfoVo getRefundOrderInfoFromRedis(String refundSeq, String hashKey);
    // 从Redis中删除退款信息     jiudongdong
    void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey);
    // 从redis 删除退款参数     jiudongdong
    void delReturnAmountParams(String orderId);
    // 从redis 获取退款参数
    Map<String, String> getReturnAmountParams(String orderId);

    // 使用银联支付时，将订单信息放入Redis     jiudongdong
    void putPayOrderInfo(String hashKey, Map<String, String> paramMap);
    // 从Redis取出所有使用银联支付时，放入Redis的订单信息    jiudongdong
    Map<Object, Object> getPayOrderInfo(String hashKey);
    // 删除Redis中存储的：使用银联支付时的订单信息    jiudongdong
    void delPayOrderInfo(String hashKey, String orderId);
}
