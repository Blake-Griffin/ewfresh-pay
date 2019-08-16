package com.ewfresh.pay.model;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * description:支付渠道表
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class PayChannel implements Serializable {
    private Integer id;//ID

    private String channelCode;//渠道编号

    private String parentChannelCode;//父渠道编号

    private String channelName;//渠道名称

    private String typeCode;//支付类型编号

    private String typeName;//支付类型名称

    private BigDecimal payRate;//商户支付费率

    private Short isEnabled;//是否启用(0未启用,1已启用)

    private Short isRecharge;//是否可充值(0不可充值,1可充值)

    private Integer sorted;//排序

    private String bankId;//银行代码

    private Short isBorrow;//是否支持借记卡

    private Short isLoan;//是否支持贷记卡

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode == null ? null : typeCode.trim();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName == null ? null : typeName.trim();
    }

    public BigDecimal getPayRate() {
        return payRate;
    }

    public void setPayRate(BigDecimal payRate) {
        this.payRate = payRate;
    }

    public Short getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Short isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Short getIsRecharge() {
        return isRecharge;
    }

    public void setIsRecharge(Short isRecharge) {
        this.isRecharge = isRecharge;
    }

    public Integer getSorted() {
        return sorted;
    }

    public void setSorted(Integer sorted) {
        this.sorted = sorted;
    }

    public String getParentChannelCode() {
        return parentChannelCode;
    }

    public void setParentChannelCode(String parentChannelCode) {
        this.parentChannelCode = parentChannelCode;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public Short getIsBorrow() {
        return isBorrow;
    }

    public void setIsBorrow(Short isBorrow) {
        this.isBorrow = isBorrow;
    }

    public Short getIsLoan() {
        return isLoan;
    }

    public void setIsLoan(Short isLoan) {
        this.isLoan = isLoan;
    }
}