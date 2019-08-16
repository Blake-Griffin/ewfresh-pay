package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      商户查询订单响应体信息
 * @author: JiuDongDong
 * date: 2018/4/11.
 */
public class BOCQueryOrderResponseBodyVo implements Serializable {
    private static final long serialVersionUID = -8133433448481585372L;

    private String orderNo;//商户订单号

    private String orderSeq;//银行订单流水号

    private String orderStatus;//订单状态

    private String cardTyp;//银行卡类别

    private String acctNo;//支付卡号

    private String holderName;//持卡人姓名

    private String ibknum;//支付卡省行联行号

    private String payTime;//支付时间

    private String payAmount;//支付金额

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
}
