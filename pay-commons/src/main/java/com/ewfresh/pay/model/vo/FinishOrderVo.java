package com.ewfresh.pay.model.vo;

import java.math.BigDecimal;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/8/14 0014
 */
public class FinishOrderVo {


    private Long orderId;
    private Long parentId;
    private Long uid;
    private BigDecimal realAmount;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private String earnestBill;
    private String finalBill;
    private BigDecimal liquidatedDamage;
    private BigDecimal freight;
    private BigDecimal disposalFee;
    private BigDecimal claimAmount;
    private BigDecimal finishAmount;
    private Short isRefund;

    public String getEarnestBill() {
        return earnestBill;
    }

    public void setEarnestBill(String earnestBill) {
        this.earnestBill = earnestBill;
    }

    public String getFinalBill() {
        return finalBill;
    }

    public void setFinalBill(String finalBill) {
        this.finalBill = finalBill;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(BigDecimal realAmount) {
        this.realAmount = realAmount;
    }

    public BigDecimal getLiquidatedDamage() {
        return liquidatedDamage;
    }

    public void setLiquidatedDamage(BigDecimal liquidatedDamage) {
        this.liquidatedDamage = liquidatedDamage;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public BigDecimal getDisposalFee() {
        return disposalFee;
    }

    public void setDisposalFee(BigDecimal disposalFee) {
        this.disposalFee = disposalFee;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }

    public BigDecimal getFinishAmount() {

        finishAmount = this.getRealAmount().subtract(this.getAmount()).add(this.getAmountPaid());
        return finishAmount;
    }

    public void setFinishAmount(BigDecimal finishAmount) {
        this.finishAmount = finishAmount;
    }

    public Short getIsRefund() {
        return isRefund;
    }

    public void setIsRefund(Short isRefund) {
        this.isRefund = isRefund;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
