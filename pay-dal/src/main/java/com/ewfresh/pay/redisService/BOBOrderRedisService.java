package com.ewfresh.pay.redisService;

import com.ewfresh.pay.model.vo.RefundInfoVo;

/**
 * description:
 *      BOB相关的订单信息
 * @author: JiuDongDong
 * date: 2018/6/27.
 */
public interface BOBOrderRedisService {
    void putOrderDesc2Redis(String orderNo, String orderDesc);

    String getOrderDescByOrderNo(String orderNo);

    void deleteOrderDescFromRedis(String orderNo);

    void putSendOrderToBOBTimeToRedis(String orderNo, String sendOrder2BOBTime);

    String getSendOrderToBOBTimeFromRedis(String orderNo);

    void deleteSendOrderToBOBTimeInRedis(String orderNo);

    void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo);

    RefundInfoVo getRefundOrderInfoFromRedis(String orderNo);

    void deleteRefundOrderInfoInRedis(String orderNo);
}
