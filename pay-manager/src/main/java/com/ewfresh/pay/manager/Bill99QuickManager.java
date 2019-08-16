package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.util.ResponseData;

import java.util.HashMap;


/**
 * Description: Bill99快捷支付的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/9/14 9:47
 */
public interface Bill99QuickManager {
    void getPciCardInfo(ResponseData responseData, String customerId, String cardType, String storablePan, String bankId);

    void getTokenBeforeBind(ResponseData responseData, String customerId, String pan, String storablePan,
                            String cardHolderName, String idType, String cardHolderId, String expiredDate,
                            String cvv2, String bindType, String phoneNO);

    void bindCardWithoutDynamicCode(ResponseData responseData, String customerId, String pan, String cardHolderName,
                                    String idType, String cardHolderId, String phoneNO);

    void bindCardWithDynamicCode(ResponseData responseData, String customerId, String pan, String validCode, String token,
                                 String externalRefNumber, String phoneNO, String cardHolderName, String idType,
                                 String cardHolderId, String expiredDate, String cvv2);

    void getCardInfo(ResponseData responseData, String cardNo, String txnType, String customerId);

    void pciDeleteCardInfo(ResponseData responseData, String customerId, String pan, String storablePan, String bankId,
                           String validCode);

    void getPayChannelByChannelName(ResponseData responseData, String channelCode, String cardType);

    void setDefaultCard(ResponseData responseData, String customerId, String storablePan);

    void getAllAbleBanksByUserId(ResponseData responseData, String customerId);

    void quickPayCommon(ResponseData responseData, String customerId, String payToken, String amount, String spFlag,
                        String interactiveStatus, String txnType, String externalRefNumber, String validCode,
                        String bindCardFlag) throws ShouldPayNotEqualsException;

    void receiveTR3ToTR4(ResponseData responseData, String signedResponseInfo);

    void getDynamicValidCodeSelf(ResponseData responseData, String customerId, String externalRefNumber, String storablePan,
                                 String amount, String functionType);

    void getRandomNum(ResponseData responseData, String customerId);

    void getDynamicValidCode(ResponseData responseData, String customerId, String externalRefNumber, String storablePan,
                             String amount);

    void dynamicCodePay(ResponseData responseData, String customerId, String interactiveStatus, String spFlag, String txnType,
                        String externalRefNumber, String amount, String storablePan, String validCode, String token,
                        String payToken, String payBatch, String savePciFlag);

    boolean persistPayResult2Disk(HashMap respXml, String customerId, String merchantId, String entryTime,
                                  String externalRefNumber, String amount, String storablePan, String bankLogo);

    void queryOrder(ResponseData responseData, String txnType, String externalRefNumber, String refNumber, String isSelfPro);

    void refundOrder(ResponseData responseData, String interactiveStatus, String txnType, String entryTime, String amount,
                     String externalRefNumber, String origRefNumber);

    BankAccount getAllAbleBanksByUserId(String customerId, String storablePan);

    void getPhnoeCheckCodeByUid(ResponseData responseData, String customerId);

    void updatePhoneChangedExpired(ResponseData responseData, Long userId, String newMobilePhone, String oldMobilePhone);

    void ifUseSpecialBank(ResponseData responseData, Long orderId);
}
