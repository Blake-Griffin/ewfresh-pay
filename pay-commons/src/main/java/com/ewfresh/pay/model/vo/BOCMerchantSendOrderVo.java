package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      商户向中行发送订单请求的参数封装
 * @author: JiuDongDong
 * date: 2018/4/8.
 */
public class BOCMerchantSendOrderVo implements Serializable {
    private static final long serialVersionUID = 3292721651962873932L;

    private String merchantNo;// 银行提供的商户号

    private String payType;// 商户支付服务类型： 1：网上购物

    private String orderNo;// 商户系统产生的订单号

    private String id;// 商户系统产生的订单号：页面以id来传递订单号

    private String curCode;// 订单币种，固定填001人民币

    private String orderAmount;// 订单金额

    private String payment;// 订单金额：页面以payment来传递订单金额

    private String orderTime;// 订单时间

    private String orderNote;// 订单说明

    private String orderUrl;// 商户接收通知URL：客户支付完成后银行向商户发送支付结果，商户系统负责接收银行通知的URL

    private String orderPayUrl;// 中行接收商户订单请求的网关

    private String orderTimeoutDate;// 超时时间，选填

    private String signData;//商户签名数据，必填，商户签名数据串格式，各项数据用管道符分隔：商户订单号|订单时间|订单币种|订单金额|商户号      id|orderTime|curCode|orderAmount|merchantNo

    private String orderStatus;//订单状态

    private String payMethod;// 支付方式（定金 /全款）

    private String payMode;// 支付渠道

    private String orderIp;// 下单ip

    private String balance;//  余额

    private String surplus;// 剩余应付金额

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurCode() {
        return curCode;
    }

    public void setCurCode(String curCode) {
        this.curCode = curCode;
    }

    public String getOrderAmount() {
        return this.orderAmount == null ? this.payment : this.orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getOrderTimeoutDate() {
        return orderTimeoutDate;
    }

    public void setOrderTimeoutDate(String orderTimeoutDate) {
        this.orderTimeoutDate = orderTimeoutDate;
    }

    public String getSignData() {
        return signData;
    }

    public void setSignData(String signData) {
        this.signData = signData;
    }

    public String getOrderPayUrl() {
        return orderPayUrl;
    }

    public void setOrderPayUrl(String orderPayUrl) {
        this.orderPayUrl = orderPayUrl;
    }

    public String getOrderNo() {
        return this.orderNo == null ? this.id : this.orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getOrderIp() {
        return orderIp;
    }

    public void setOrderIp(String orderIp) {
        this.orderIp = orderIp;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getSurplus() {
        return surplus;
    }

    public void setSurplus(String surplus) {
        this.surplus = surplus;
    }
}
