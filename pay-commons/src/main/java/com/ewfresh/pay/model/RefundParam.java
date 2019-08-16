package com.ewfresh.pay.model;

import java.math.BigDecimal;

/**
 * description:退款请求所用的实体类
 *
 * @author wangziyuan
 */
public class RefundParam {

    private String outTradeNo;//商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)

    private String tradeNo;//交易流水号（支付流水表的channel_flow_id）

    private String refundAmount;//退款金额

    private String totalAmount;//订单金额

    private String orderNo;//父订单号

    private String outRequestNo;//退款订单号(子订单订单号,如果没有该订单没有子订单则该处填商户订单号)

    private String channelType;//类型(07：互联网，08：移动)

    private String channelCode;//交易渠道编码

    private Integer payFlowId; //支付的交易流水Id

    private PayFlow payFlow;   //支付的交易流水对象

    private String receiverUserId;  // 收款人id（shopId）

    private BigDecimal freight;     //应退运费

    private Short tradeType;        //交易类型

    private BigDecimal ewfreshBenefitRefund;//应退服务费

    public RefundParam() {
    }

    public RefundParam(RefundParam refundParam) {
        this.outTradeNo = refundParam.getOutTradeNo();
        this.tradeNo = refundParam.getTradeNo();
        this.refundAmount = refundParam.getRefundAmount();
        this.totalAmount = refundParam.getTotalAmount();
        this.orderNo = refundParam.getOrderNo();
        this.outRequestNo = refundParam.getOutRequestNo();
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getOutRequestNo() {
        return outRequestNo;
    }

    public void setOutRequestNo(String outRequestNo) {
        this.outRequestNo = outRequestNo;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }


    public Integer getPayFlowId() {
        return payFlowId;
    }

    public void setPayFlowId(Integer payFlowId) {
        this.payFlowId = payFlowId;
    }

    public PayFlow getPayFlow() {
        return payFlow;
    }

    public void setPayFlow(PayFlow payFlow) {
        this.payFlow = payFlow;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public Short getTradeType() {
        return tradeType;
    }

    public void setTradeType(Short tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getEwfreshBenefitRefund() {
        return ewfreshBenefitRefund;
    }

    public void setEwfreshBenefitRefund(BigDecimal ewfreshBenefitRefund) {
        this.ewfreshBenefitRefund = ewfreshBenefitRefund;
    }
}
