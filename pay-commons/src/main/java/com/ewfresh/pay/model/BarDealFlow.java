package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_bar_deal_flow
 * @author 
 */
public class BarDealFlow implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 客户名称
     */
    private String uname;

    /**
     * 已使用额度
     */
    private BigDecimal usedLimit;

    /**
     * 本次交易金额
     */
    private BigDecimal amount;

    /**
     * 资金流向(1流出,2流入)
     */
    private Short direction;

    /**
     * 进行交易的店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 交易订单号(包括购买订单号和还款订单号两种类型)
     */
    private Long orderId;

    /**
     * 交易类型(1订单付款,2订单退款,3还款)
     */
    private Short dealType;

    /**
     * 交易时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occTime;

    /**
     * 生成的账单批次号
     */
    private String billFlow;

    /**
     * 支付流水ID
     */
    private Integer payFlowId;

    /**
     * 最后修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifyTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    public void setUsedLimit(BigDecimal usedLimit) {
        this.usedLimit = usedLimit;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Short getDirection() {
        return direction;
    }

    public void setDirection(Short direction) {
        this.direction = direction;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Short getDealType() {
        return dealType;
    }

    public void setDealType(Short dealType) {
        this.dealType = dealType;
    }

    public Date getOccTime() {
        return occTime;
    }

    public void setOccTime(Date occTime) {
        this.occTime = occTime;
    }

    public String getBillFlow() {
        return billFlow;
    }

    public void setBillFlow(String billFlow) {
        this.billFlow = billFlow;
    }

    public Integer getPayFlowId() {
        return payFlowId;
    }

    public void setPayFlowId(Integer payFlowId) {
        this.payFlowId = payFlowId;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
}