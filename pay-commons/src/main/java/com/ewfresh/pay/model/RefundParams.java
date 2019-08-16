package com.ewfresh.pay.model;

import java.math.BigDecimal;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/6/12 0012
 */
public class RefundParams {

    private String earnestBill;       //定金交易流水号（支付流水表的channel_flow_id）（可为空）

    private String finalBill;         //全款或尾款流水号（支付流水表的channel_flow_id）（可为空）

    private String totalAmount;       //订单金额（不为空）

    private String orderId;           //订单号（子订单号）（未拆单时为空）

    private String parentId;          //父订单号（不可为空）

    private BigDecimal earnestAmount; //应退定金金额（可为空）

    private BigDecimal finalAmount;   //应退尾款或全款金额（可为空）

    private BigDecimal freight;       //应退运费

    private Short tradeType;          //交易类型

    private BigDecimal dispatchAmount;     //应退配货金额

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

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public BigDecimal getEarnestAmount() {
        return earnestAmount;
    }

    public void setEarnestAmount(BigDecimal earnestAmount) {
        this.earnestAmount = earnestAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public Short getTradeType() {
        return tradeType;
    }

    public void setTradeType(Short tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getDispatchAmount() {
        return dispatchAmount;
    }

    public void setDispatchAmount(BigDecimal dispatchAmount) {
        this.dispatchAmount = dispatchAmount;
    }
}
