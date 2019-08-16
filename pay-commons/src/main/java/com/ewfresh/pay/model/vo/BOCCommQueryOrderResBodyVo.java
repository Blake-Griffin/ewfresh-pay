package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      商户发送查询订单请求(支持卡户信息判断)的响应体信息封装
 * @author: JiuDongDong
 * date: 2018/4/16.
 */
public class BOCCommQueryOrderResBodyVo implements Serializable{
//    private static final long serialVersionUID = -3039624064668475143L;
    private String merchantNo;// 商户号

    private String orderNo;// 商户订单号

    private String orderSeq;// 银行订单流水号

    private String orderStatus;// 订单状态：0-未处理 1-支付 4-未明 5-失败

    private String cardTyp;// 银行卡类别

    private String acctNo;// 支付卡号

    private String holderName;// 持卡人姓名

    private String ibknum;// 支付卡省行联行号

    private String payTime;// 支付交易的日期时间 格式：YYYYMMDDHHMISS

    private String payAmount;// 支付金额，格式：整数位不前补零,小数位补齐2位 即：不超过10位整数位+1位小数点+2位小数 无效格式如123，.10，1.1,有效格式如1.00，0.10

    private String visitorIp;// 访问者IP 客户通过网银支付时的IP地址信息，例如：192.168.0.1

    private String visitorRefer;// 访问者Refer信息，客户浏览器跳转至网银支付登录界面前所在页面的URL

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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCardTyp() {
        return cardTyp;
    }

    public void setCardTyp(String cardTyp) {
        this.cardTyp = cardTyp;
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

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(String payAmount) {
        this.payAmount = payAmount;
    }

    public String getVisitorIp() {
        return visitorIp;
    }

    public void setVisitorIp(String visitorIp) {
        this.visitorIp = visitorIp;
    }

    public String getVisitorRefer() {
        return visitorRefer;
    }

    public void setVisitorRefer(String visitorRefer) {
        this.visitorRefer = visitorRefer;
    }
}
