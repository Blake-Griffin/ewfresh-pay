package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

import java.util.Map;

/**
 * description: 银联B2C，web、wap的业务逻辑管理层
 * @author: JiuDongDong
 * date: 2019/4/25.
 */
public interface UnionpayB2CWebWapManager {

    void sendOrder(ResponseData responseData, Map<String, String> params);

    void singleQuery(ResponseData responseData, Map<String, String> params);

    void receivePayNotify(ResponseData responseData, Map<String, String> params);

    void receiveRefundNotify(ResponseData responseData, Map<String, String> params);

    void fileTransfer(ResponseData responseData, String merId, String settleDate);
}
