package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * description:支付流水表
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class PayFlow implements Serializable {
    private Integer payFlowId;//支付流水ID

    private Long orderId;//订单号

    private String channelFlowId;//支付渠道流水号

    private String payerId;//付款人ID

    private String payerName;//付款人名称

    private BigDecimal payerPayAmount;//付款方支付金额

    private BigDecimal payerFee;//付款方手续费

    private String receiverUserId;//收款人ID

    private String receiverName;//收款人名称

    private BigDecimal receiverFee;//收款方手续费

    private String orderIp;//下单IP

    private String orderRefererUrl;//订单来源URL

    private BigDecimal orderAmount;//订单金额

    private BigDecimal platIncome;//平台收入

    private BigDecimal feeRate;//费率

    private String channelCode;//支付渠道编号

    private String channelName;//支付渠道名称

    private String typeCode;//支付类型编号

    private String typeName;//支付类型名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date successTime;//支付成功时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;//完成时间

    private Short isRefund;//是否退款 0:否,1是

    private Short tradeType;//交易类型  1:订单,2,退款,3,线下充值,4线上充值,5:提现,6:商户结算打款,7:平台增值服务收款

    private Short isBlc;//是否对账 0:否,1:是

    private String returnInfo;//返回信息

    private Date blcTime;//对账时间

    private Short status;//状态 0:成功,1:失败
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间

    private String desp;//描述

    private String interactionId;//第三方交互订单号

    private String channelType;//渠道类型07：互联网，08：移动

    private String returnFlowId;//退款流水号

    private Short payerType;//付款账号类型(1个人,2店铺)

    private Short receiverType; //收款账号类型(1个人,2店铺)

    private String uname;       //客户名称

    private String batchNo;       //结算批次号

    private Integer shopId;       //店铺Id

    private Short orderStatus;    //订单状态(0未完结,1已完结)

    private Short settleStatus;   //结算状态(0未审核,1审核通过,2审核不通过,3结算中,4结算完成,5,结算失败)

    private String mid;           //付款商户号

    private String tid;           //付款终端号

    private Integer shopBenefitPercent;//平台分润比例

    private BigDecimal shopBenefitMoney;//平台分润金额

    private BigDecimal freight;//运费

    private static final long serialVersionUID = 1L;

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public Short getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Short orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Short getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(Short settleStatus) {
        this.settleStatus = settleStatus;
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

    public String getChannelFlowId() {
        return channelFlowId;
    }

    public void setChannelFlowId(String channelFlowId) {
        this.channelFlowId = channelFlowId == null ? null : channelFlowId.trim();
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName == null ? null : payerName.trim();
    }

    public BigDecimal getPayerPayAmount() {
        return payerPayAmount;
    }

    public void setPayerPayAmount(BigDecimal payerPayAmount) {
        this.payerPayAmount = payerPayAmount;
    }

    public BigDecimal getPayerFee() {
        return payerFee;
    }

    public void setPayerFee(BigDecimal payerFee) {
        this.payerFee = payerFee;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName == null ? null : receiverName.trim();
    }

    public BigDecimal getReceiverFee() {
        return receiverFee;
    }

    public void setReceiverFee(BigDecimal receiverFee) {
        this.receiverFee = receiverFee;
    }

    public String getOrderIp() {
        return orderIp;
    }

    public void setOrderIp(String orderIp) {
        this.orderIp = orderIp == null ? null : orderIp.trim();
    }

    public String getOrderRefererUrl() {
        return orderRefererUrl;
    }

    public void setOrderRefererUrl(String orderRefererUrl) {
        this.orderRefererUrl = orderRefererUrl == null ? null : orderRefererUrl.trim();
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public BigDecimal getPlatIncome() {
        return platIncome;
    }

    public void setPlatIncome(BigDecimal platIncome) {
        this.platIncome = platIncome;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
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

    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Short getIsRefund() {
        return isRefund;
    }

    public void setIsRefund(Short isRefund) {
        this.isRefund = isRefund;
    }

    public Short getTradeType() {
        return tradeType;
    }

    public void setTradeType(Short tradeType) {
        this.tradeType = tradeType;
    }

    public void setIsBlc(Short isBlc) {
        this.isBlc = isBlc;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(String returnInfo) {
        this.returnInfo = returnInfo == null ? null : returnInfo.trim();
    }

    public Short getIsBlc() {
        return isBlc;
    }

    public Date getBlcTime() {
        return blcTime;
    }

    public void setBlcTime(Date blcTime) {
        this.blcTime = blcTime;
    }

    public Short getStatus() {
        return status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getReturnFlowId() {
        return returnFlowId;
    }

    public void setReturnFlowId(String returnFlowId) {
        this.returnFlowId = returnFlowId;
    }

    public Short getPayerType() {
        return payerType;
    }

    public void setPayerType(Short payerType) {
        this.payerType = payerType;
    }

    public Short getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(Short receiverType) {
        this.receiverType = receiverType;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Integer getShopBenefitPercent() {
        return shopBenefitPercent;
    }

    public void setShopBenefitPercent(Integer shopBenefitPercent) {
        this.shopBenefitPercent = shopBenefitPercent;
    }

    public BigDecimal getShopBenefitMoney() {
        return shopBenefitMoney;
    }

    public void setShopBenefitMoney(BigDecimal shopBenefitMoney) {
        this.shopBenefitMoney = shopBenefitMoney;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }
}