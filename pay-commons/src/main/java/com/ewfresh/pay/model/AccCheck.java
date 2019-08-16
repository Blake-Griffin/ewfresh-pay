package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * description:对账批次表
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class AccCheck implements Serializable {
    private Integer id;//ID

    private String batchNo;//批次号
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date billDate;//账单日期

    private String channelCode;//支付渠道编号

    private String channelName;//支付渠道名称

    private Short handleStatus;//处理状态 1:全部成功,2:全部失败,3:部分成功

    private Integer mistakeCount;//差错数

    private Integer tradeCount;//交易笔数

    private Integer channelTradeCount;//渠道交易笔数

    private BigDecimal tradeAmount;//交易金额

    private BigDecimal channelTradeAmount;//渠道交易金额

    private BigDecimal refundAmount;//退款金额

    private BigDecimal channelRefundAmount;//渠道退款金额

    private BigDecimal channelFee;//手续费

    private String billFilePath;//账单文件路径

    private String failMsg;//对账失败消息

    private String channelFailMsg;//银行错误消息
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间

    private Integer creator;//创建人

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
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

    public Integer getMistakeCount() {
        return mistakeCount;
    }

    public void setMistakeCount(Integer mistakeCount) {
        this.mistakeCount = mistakeCount;
    }

    public Integer getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Integer tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Integer getChannelTradeCount() {
        return channelTradeCount;
    }

    public void setChannelTradeCount(Integer channelTradeCount) {
        this.channelTradeCount = channelTradeCount;
    }

    public BigDecimal getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(BigDecimal tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public BigDecimal getChannelTradeAmount() {
        return channelTradeAmount;
    }

    public void setChannelTradeAmount(BigDecimal channelTradeAmount) {
        this.channelTradeAmount = channelTradeAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
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

    public String getBillFilePath() {
        return billFilePath;
    }

    public void setBillFilePath(String billFilePath) {
        this.billFilePath = billFilePath == null ? null : billFilePath.trim();
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg == null ? null : failMsg.trim();
    }

    public String getChannelFailMsg() {
        return channelFailMsg;
    }

    public void setChannelFailMsg(String channelFailMsg) {
        this.channelFailMsg = channelFailMsg == null ? null : channelFailMsg.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public Short getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(Short handleStatus) {
        this.handleStatus = handleStatus;
    }
}