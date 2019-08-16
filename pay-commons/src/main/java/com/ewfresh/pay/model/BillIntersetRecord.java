package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_bill_interset_record
 * @author 
 */
public class BillIntersetRecord implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 账单ID
     */
    private Integer billId;

    /**
     * 当次计息金额
     */
    private BigDecimal interestBearingAmount;

    /**
     * 当次利息金额
     */
    private BigDecimal interestAmount;

    /**
     * 当次计算利率(0-100)
     */
    private Integer interestRate;

    /**
     * 计息时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date interestTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public BigDecimal getInterestBearingAmount() {
        return interestBearingAmount;
    }

    public void setInterestBearingAmount(BigDecimal interestBearingAmount) {
        this.interestBearingAmount = interestBearingAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public Integer getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Integer interestRate) {
        this.interestRate = interestRate;
    }

    public Date getInterestTime() {
        return interestTime;
    }

    public void setInterestTime(Date interestTime) {
        this.interestTime = interestTime;
    }
}