package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.Bill;

import java.math.BigDecimal;

public class BillVo extends Bill {
    private Short overdueMeg; //1逾期 2没有逾期
    private BigDecimal payableAmount;//应还本金金额
    private BigDecimal payableInterest;//应还利息
    private BigDecimal totalSum;//应还的总金额(利息加本金)
    private Long overdueDays;//逾期天数
    private BigDecimal historicalTotalSum;//历史账单总金额
    private String introducer ;//招商人员

    public String getIntroducer() {
        return introducer;
    }

    public void setIntroducer(String introducer) {
        this.introducer = introducer;
    }

    public BigDecimal getHistoricalTotalSum() {
        return historicalTotalSum;
    }

    public void setHistoricalTotalSum(BigDecimal historicalTotalSum) {
        this.historicalTotalSum = historicalTotalSum;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    public Short getOverdueMeg() {
        return overdueMeg;
    }

    public void setOverdueMeg(Short overdueMeg) {
        this.overdueMeg = overdueMeg;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public BigDecimal getPayableInterest() {
        return payableInterest;
    }

    public void setPayableInterest(BigDecimal payableInterest) {
        this.payableInterest = payableInterest;
    }

    public Long getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Long overdueDays) {
        this.overdueDays = overdueDays;
    }
}
