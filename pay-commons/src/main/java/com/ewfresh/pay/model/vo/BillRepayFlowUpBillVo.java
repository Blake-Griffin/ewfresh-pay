package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.BillRepayFlow;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/31.
 */
public class BillRepayFlowUpBillVo extends BillRepayFlow{
    /**
     * 账单批次号
     */
    private String billFlow;
    /**
     * 已还金额
     */
    private BigDecimal repaidAmount;
    /**
     * 已还利息
     */
    private BigDecimal repaidInterest;
    /**
     * 账单状态(1待还款,2已还款,3部分还款,4已完结)
     */
    private Short billStatus;

    public String getBillFlow() {
        return billFlow;
    }

    public void setBillFlow(String billFlow) {
        this.billFlow = billFlow;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public BigDecimal getRepaidInterest() {
        return repaidInterest;
    }

    public void setRepaidInterest(BigDecimal repaidInterest) {
        this.repaidInterest = repaidInterest;
    }

    public Short getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(Short billStatus) {
        this.billStatus = billStatus;
    }
}
