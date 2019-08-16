package com.ewfresh.pay.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * description:账单流水表对应实体类
 */
public class BillFlow implements Serializable {
    private Long id;//账单ID

    private String channelFlowId;//支付渠道流水

    private Long orderId;//商户订单编号

    private BigDecimal income;//收入金额

    private BigDecimal expenditure;//支出金额

    private BigDecimal accountBalance;//账户余额

    private String channelName;//交易渠道

    private Short tradeType;//交易类型  1:订单,2,退款,3,线下充值,4线上充值,5:提现,6:商户结算打款,7:平台增值服务收款

    private String desp;//描述

    private Date createTime;//支付时间

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelFlowId() {
        return channelFlowId;
    }

    public void setChannelFlowId(String channelFlowId) {
        this.channelFlowId = channelFlowId == null ? null : channelFlowId.trim();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(BigDecimal expenditure) {
        this.expenditure = expenditure;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    public Short getTradeType() {
        return tradeType;
    }

    public void setTradeType(Short tradeType) {
        this.tradeType = tradeType;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}