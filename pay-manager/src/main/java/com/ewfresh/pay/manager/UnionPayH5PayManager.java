package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.exception.OrderTimeOutException;
import com.ewfresh.pay.util.ResponseData;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * description: 银联H5Pay逻辑管理类
 * @author: JiuDongDong
 * date: 2019/5/16.
 */
public interface UnionPayH5PayManager {
    void sendOrder(ResponseData responseData, Map<String, String> params) throws Exception;

    void singleQuery(ResponseData responseData, String merOrderId, String mid, String tid);

    void receiveNotify(ResponseData responseData, String respStr) throws UnsupportedEncodingException, OrderTimeOutException;

    void refundQuery(ResponseData responseData, String merOrderId);

    void receiveB2BRefundNotify(ResponseData responseData, String respStr) throws Exception;
}
