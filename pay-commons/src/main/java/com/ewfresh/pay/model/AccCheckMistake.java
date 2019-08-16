package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * description:对账差错表
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class AccCheckMistake implements Serializable {
    private Integer id;//ID

    private String accNo;//对账批次号
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date billDate;//账单日期

    private Integer payFlowId;//支付流水号

    private Short type;//差错类型 1:金额不一致,2:渠道缺失流水,3:平台缺失流水

    private Long orderId;//订单ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradeTime;//交易时间

    private BigDecimal tradeAmount;//交易金额

    private BigDecimal refundAmount;//退款金额

    private Short tradeStatus;//交易状态 0:成功,1:失败

    private BigDecimal fee;//手续费

    private String channelCode;//渠道编号

    private String channelName;//渠道名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date chnanelTradeTime;//渠道交易时间

    private String channelTradeFlow;//渠道交易流水号

    private Short channelTradeStatus;//渠道交易状态  0:成功,1:失败

    private BigDecimal channelTradeAmount;//渠道交易金额

    private BigDecimal channelRefundAmount;//渠道退款金额

    private BigDecimal channelFee;//渠道手续费

    private Short handleStatus;//处理状态 0:待处理,1:已处理

    private BigDecimal handleAmount;//处理金额

    private Integer handler;//处理人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;//处理时间

    private String desp;//描述

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo == null ? null : accNo.trim();
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public Integer getPayFlowId() {
        return payFlowId;
    }

    public void setPayFlowId(Integer payFlowId) {
        this.payFlowId = payFlowId;
    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public BigDecimal getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(BigDecimal tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public Short getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(Short tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public void setChannelTradeStatus(Short channelTradeStatus) {
        this.channelTradeStatus = channelTradeStatus;
    }

    public void setHandleStatus(Short handleStatus) {
        this.handleStatus = handleStatus;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode == null ? null : channelCode.trim();
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    public Date getChnanelTradeTime() {
        return chnanelTradeTime;
    }

    public void setChnanelTradeTime(Date chnanelTradeTime) {
        this.chnanelTradeTime = chnanelTradeTime;
    }

    public String getChannelTradeFlow() {
        return channelTradeFlow;
    }

    public void setChannelTradeFlow(String channelTradeFlow) {
        this.channelTradeFlow = channelTradeFlow == null ? null : channelTradeFlow.trim();
    }

    public Short getChannelTradeStatus() {
        return channelTradeStatus;
    }

    public BigDecimal getChannelTradeAmount() {
        return channelTradeAmount;
    }

    public void setChannelTradeAmount(BigDecimal channelTradeAmount) {
        this.channelTradeAmount = channelTradeAmount;
    }

    public BigDecimal getChannelRefundAmount() {
        return channelRefundAmount;
    }

    public void setChannelRefundAmount(BigDecimal channelRefundAmount) {
        this.channelRefundAmount = channelRefundAmount;
    }

    public BigDecimal getChannelFee() {
        return channelFee;
    }

    public void setChannelFee(BigDecimal channelFee) {
        this.channelFee = channelFee;
    }

    public Short getHandleStatus() {
        return handleStatus;
    }

    public BigDecimal getHandleAmount() {
        return handleAmount;
    }

    public void setHandleAmount(BigDecimal handleAmount) {
        this.handleAmount = handleAmount;
    }

    public Integer getHandler() {
        return handler;
    }

    public void setHandler(Integer handler) {
        this.handler = handler;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }
}