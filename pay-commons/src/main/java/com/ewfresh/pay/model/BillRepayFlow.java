package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_bill_repay_flow
 *
 * @author
 */
public class BillRepayFlow implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 账单ID
     */
    private Integer billId;
    /**
     * 还款金额
     */
    private BigDecimal repayAmount;

    /**
     * 当次归还本金金额
     */
    private BigDecimal principalAmount;

    /**
     * 当次归还利息金额
     */
    private BigDecimal interestAmount;

    /**
     * 还款渠道(1余额,2块钱,3银联,4混合)
     */
    private Short repayChannel;

    /**
     * 还款方式(0被动扣款,1主动还款)
     */
    private Short repayType;

    /**
     * 还款时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date repayTime;

    /**
     * 操作人
     */
    private String operator;

    private Long orderId;


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public BillRepayFlow setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getBillId() {
        return billId;
    }

    public BillRepayFlow setBillId(Integer billId) {
        this.billId = billId;
        return this;
    }

    public BigDecimal getRepayAmount() {
        return repayAmount;
    }

    public BillRepayFlow setRepayAmount(BigDecimal repayAmount) {
        this.repayAmount = repayAmount;
        return this;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BillRepayFlow setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
        return this;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public BillRepayFlow setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
        return this;
    }

    public Short getRepayChannel() {
        return repayChannel;
    }

    public BillRepayFlow setRepayChannel(Short repayChannel) {
        this.repayChannel = repayChannel;
        return this;
    }

    public Short getRepayType() {
        return repayType;
    }

    public BillRepayFlow setRepayType(Short repayType) {
        this.repayType = repayType;
        return this;
    }

    public Date getRepayTime() {
        return repayTime;
    }

    public BillRepayFlow setRepayTime(Date repayTime) {
        this.repayTime = repayTime;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public BillRepayFlow setOperator(String operator) {
        this.operator = operator;
        return this;
    }
}