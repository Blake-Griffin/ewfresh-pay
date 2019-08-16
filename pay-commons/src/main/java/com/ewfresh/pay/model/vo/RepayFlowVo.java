package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName RepayFlowVo
 * @Description: 还款记录vo实体类
 * @Author huboyang
 * @Date 2019/7/25
 **/
public class RepayFlowVo {
    /**
     * 账单Id
     */
    private Integer billId;
    /**
     * 还款记录id
     */
    private Integer billRepayId;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户名称
     */
    private String uName;
    /**
     * 账单批次号
     */
    private String billFlow;
    /**
     * 总利息
     */
    private BigDecimal totalInterest;
    /**
     *  本次还款利息
     */
    private BigDecimal interestAmount;
    /**
     * 还款时间
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date repayTime;
    /**
     * 还款渠道
     */
    private Short repayChannel;
    /**
     * 还款方式
     */
    private Short repayType;
    /**
     * 还款的订单Id
     */
    private Long orderId;

    public Date getBillTime() {
        return billTime;
    }

    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }

    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date billTime;

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getBillRepayId() {
        return billRepayId;
    }

    public void setBillRepayId(Integer billRepayId) {
        this.billRepayId = billRepayId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getBillFlow() {
        return billFlow;
    }

    public void setBillFlow(String billFlow) {
        this.billFlow = billFlow;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public Date getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(Date repayTime) {
        this.repayTime = repayTime;
    }

    public Short getRepayChannel() {
        return repayChannel;
    }

    public void setRepayChannel(Short repayChannel) {
        this.repayChannel = repayChannel;
    }

    public Short getRepayType() {
        return repayType;
    }

    public void setRepayType(Short repayType) {
        this.repayType = repayType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
