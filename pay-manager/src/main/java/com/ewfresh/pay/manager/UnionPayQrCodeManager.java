package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

import java.util.Map;

/**
 * description: 银联QrCode逻辑管理类
 * @author: JiuDongDong
 * date: 2019/5/10.
 */
public interface UnionPayQrCodeManager {
    void getQrCode(ResponseData responseData, Map<String, String> params) throws Exception;

    void receiveNotify(ResponseData responseData, String respStr) throws Exception;

    void singleQuery(ResponseData responseData, String billDate, String billNo, String tradeType, String refundSeq, String mid, String tid);
}
