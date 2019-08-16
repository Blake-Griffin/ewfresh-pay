package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

import java.util.Map;

/**
 * description:
 *      BOB的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/4/20.
 */
public interface BOBManager {

    void sendOrder(ResponseData responseData, Map<String, String> params);

    Integer receiveNotify(Map<String, String> params);

    void refundOrder(ResponseData responseData, Map<String, String> params);

    void singleQuery(ResponseData responseData, Map<String, String> params);

    void orderAccount(ResponseData responseData, Map<String, String> params);

    Integer receiveAccount(Map<String, String> params);
}
