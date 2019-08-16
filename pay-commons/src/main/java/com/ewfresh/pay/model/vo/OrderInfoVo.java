package com.ewfresh.pay.model.vo;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/**
 * description: 封装订单信息
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
public class OrderInfoVo {
    private String txnType;
    private String interactiveStatus;
    private String amount;
    private String merchantId;
    private String terminalId;
    private String entryTime;
    private String externalRefNumber;
    private String customerId;
    private String transTime;
    private String refNumber;
    private String responseCode;
    private String responseTextMessage;
    private String cardOrg;
    private String issuer;
    private String storableCardNo;
    private String authorizationCode;
    private String payToken;
    private String voidFlag;
    private String txnStatus;
    private String isSelfPro;// 是否自营商品，0否1是

    public OrderInfoVo() {
    }

    public OrderInfoVo(HashMap respXml) {
        if (null != respXml.get("txnType")) this.txnType = (String) respXml.get("txnType");
        if (null != respXml.get("interactiveStatus")) this.interactiveStatus = (String) respXml.get("interactiveStatus");
        if (null != respXml.get("amount")) this.amount = (String) respXml.get("amount");
        if (null != respXml.get("merchantId")) this.merchantId = (String) respXml.get("merchantId");
        if (null != respXml.get("terminalId")) this.terminalId = (String) respXml.get("terminalId");
        if (null != respXml.get("entryTime")) this.entryTime = (String) respXml.get("entryTime");
        if (null != respXml.get("externalRefNumber")) this.externalRefNumber = (String) respXml.get("externalRefNumber");
        if (null != respXml.get("customerId")) this.customerId = (String) respXml.get("customerId");
        if (null != respXml.get("transTime")) this.transTime = (String) respXml.get("transTime");
        if (null != respXml.get("refNumber")) this.refNumber = (String) respXml.get("refNumber");
        if (null != respXml.get("responseCode")) this.responseCode = (String) respXml.get("responseCode");
        if (null != respXml.get("responseTextMessage")) this.responseTextMessage = (String) respXml.get("responseTextMessage");
        if (null != respXml.get("cardOrg")) this.cardOrg = (String) respXml.get("cardOrg");
        if (null != respXml.get("issuer")) this.issuer = (String) respXml.get("issuer");
        if (null != respXml.get("storableCardNo")) this.storableCardNo = (String) respXml.get("storableCardNo");
        if (null != respXml.get("storablePan")) this.storableCardNo = (String) respXml.get("storablePan");
        if (null != respXml.get("authorizationCode")) this.authorizationCode = (String) respXml.get("authorizationCode");
        if (null != respXml.get("payToken")) this.payToken = (String) respXml.get("payToken");
        if (null != respXml.get("voidFlag")) this.voidFlag = (String) respXml.get("voidFlag");
        if (null != respXml.get("txnStatus")) this.txnStatus = (String) respXml.get("txnStatus");
        if (null != respXml.get("isSelfPro")) this.isSelfPro = (String) respXml.get("isSelfPro");
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getInteractiveStatus() {
        return interactiveStatus;
    }

    public void setInteractiveStatus(String interactiveStatus) {
        this.interactiveStatus = interactiveStatus;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getExternalRefNumber() {
        return externalRefNumber;
    }

    public void setExternalRefNumber(String externalRefNumber) {
        this.externalRefNumber = externalRefNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseTextMessage() {
        return responseTextMessage;
    }

    public void setResponseTextMessage(String responseTextMessage) {
        this.responseTextMessage = responseTextMessage;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg) {
        this.cardOrg = cardOrg;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getStorableCardNo() {
        return storableCardNo;
    }

    public void setStorableCardNo(String storableCardNo) {
        this.storableCardNo = storableCardNo;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

    public String getVoidFlag() {
        return voidFlag;
    }

    public void setVoidFlag(String voidFlag) {
        this.voidFlag = voidFlag;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public String getIsSelfPro() {
        return isSelfPro;
    }

    public void setIsSelfPro(String isSelfPro) {
        this.isSelfPro = isSelfPro;
    }
}
