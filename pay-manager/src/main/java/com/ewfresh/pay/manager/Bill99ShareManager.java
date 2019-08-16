package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.util.ResponseData;

/**
 * description: Bill99订单业务的逻辑处理层
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
public interface Bill99ShareManager {
    void sendOrder(ResponseData responseData, String payerName, String payerContact, String orderNo,
                   String orderAmount, String bankId, String payType, String payerContactType,
                   String orderIp, String payerIdType, String payerId) throws ShouldPayNotEqualsException;
}
