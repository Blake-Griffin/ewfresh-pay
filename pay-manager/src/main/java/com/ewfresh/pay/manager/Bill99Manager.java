package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.model.vo.BankAccountVo;
import com.ewfresh.pay.util.ResponseData;

import java.util.Map;

/**
 * description: Bill99订单业务的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/7/31.
 */
public interface Bill99Manager {
    void sendOrder(ResponseData responseData, String payerName, String payerContact, String orderNo,
                   String orderAmount, String bankId, String payType, String payerContactType,
                   String orderIp, String payerIdType, String payerId) throws ShouldPayNotEqualsException;

    void receiveNotify(ResponseData responseData, Map<String, String> params);

    void queryRefundOrder(ResponseData responseData, String startDate, String endDate, String refundSequence,
                          String rOrderId, String requestPage, String status, String merchantType);

    void bindAccountOfCompany(ResponseData responseData, Map<String, String> shop) throws Exception;

    void bindAccountOfPerson(ResponseData responseData, Map<String, String> shop) throws Exception;

    void getRefundOrderInfoFromRedis(ResponseData responseData, String outRequestNo);

    void addBankAccountByShop(ResponseData responseData,BankAccountVo bankAccount,String code) throws Exception;

    void updateBankAccountByShop(ResponseData responseData, BankAccountVo bankAccount, String code) throws Exception;

}
