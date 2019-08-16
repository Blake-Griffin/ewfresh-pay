package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.Bill;

import java.math.BigDecimal;

/**
 * Description:
 * @author DuanXiangming
 * Date 2019/4/1 0001
 */
public class AutoRepayBillVo extends Bill {


    private BigDecimal payableAmount;//应还金额

    private BigDecimal payableInterest;//应还利息


    public BigDecimal getPayableAmount() {
        BigDecimal billAmount = getBillAmount();
        BigDecimal repaidAmount = getRepaidAmount();
        if (billAmount == null || repaidAmount ==null){
            return null;
        }
        BigDecimal payableAmount = billAmount.subtract(repaidAmount);
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public BigDecimal getPayableInterest() {
        BigDecimal totalInterest = getTotalInterest();
        BigDecimal repaidInterest = getRepaidInterest();
        if (totalInterest == null || repaidInterest ==null){
            return null;
        }
        BigDecimal payableInterest = totalInterest.subtract(repaidInterest);
        return payableInterest;
    }

    public void setPayableInterest(BigDecimal payableInterest) {
        this.payableInterest = payableInterest;
    }
}
