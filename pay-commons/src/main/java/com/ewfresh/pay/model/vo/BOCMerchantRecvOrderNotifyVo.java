package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      商户接收BOC反馈的订单处理结果信息
 * @author: JiuDongDong
 * date: 2018/4/8.
 */
public class BOCMerchantRecvOrderNotifyVo implements Serializable {
    private static final long serialVersionUID = 387669953105067756L;

    private String merchantNo;// 商户号

    private String orderNo;// 商户订单号

    private String orderSeq;// 银行订单流水号

    private String cardTyp;// 银行卡类别

    private String payTime;// 支付时间

    private String orderStatus;// 订单状态

    private String payAmount;// 支付金额

    private String acctNo;//支付卡号

    private String holderName;//持卡人姓名

    private String ibknum;//支付卡省行联行号

    private String orderIp;// 客户支付IP地址

    private String orderRefer;// 客户浏览器Refer信息

    private String bankTranSeq;// 银行交易流水号

    private String returnActFlag;// 返回操作类型

    private String phoneNum;// 电话号码

    private String signData;// 中行签名数据

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(String orderSeq) {
        this.orderSeq = orderSeq;
    }

    public String getCardTyp() {
        return cardTyp;
    }

    public void setCardTyp(String cardTyp) {
        this.cardTyp = cardTyp;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(String payAmount) {
        this.payAmount = payAmount;
    }

    public String getOrderIp() {
        return orderIp;
    }

    public void setOrderIp(String orderIp) {
        this.orderIp = orderIp;
    }

    public String getOrderRefer() {
        return orderRefer;
    }

    public void setOrderRefer(String orderRefer) {
        this.orderRefer = orderRefer;
    }

    public String getBankTranSeq() {
        return bankTranSeq;
    }

    public void setBankTranSeq(String bankTranSeq) {
        this.bankTranSeq = bankTranSeq;
    }

    public String getReturnActFlag() {
        return returnActFlag;
    }

    public void setReturnActFlag(String returnActFlag) {
        this.returnActFlag = returnActFlag;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getSignData() {
        return signData;
    }

    public void setSignData(String signData) {
        this.signData = signData;
    }

    public String getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getIbknum() {
        return ibknum;
    }

    public void setIbknum(String ibknum) {
        this.ibknum = ibknum;
    }
}
