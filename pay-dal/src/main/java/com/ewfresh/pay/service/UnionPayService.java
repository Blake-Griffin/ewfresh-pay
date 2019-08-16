package com.ewfresh.pay.service;

import com.ewfresh.pay.model.PayFlow;

import java.util.Map;

/**
 * description: 银联
 * @author: JiuDongDong
 * date: 2019/7/5.
 */
public interface UnionPayService {
    void updatePayFlowAndOrder(PayFlow payFlow, String updateOrderStatusUrl, Map<String, String> params, String refundOrderId, String hashKey, String outRequestNo);
}
