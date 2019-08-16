package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_Interest_flow
 * @author huboyang
 */
public class InterestFlow implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 账单id
     */
    private Integer billId;

    /**
     * 还款记录表id
     */
    private Integer billRepayId;

    /**
     * 账单批次号
     */
    private String billFlow;

    /**
     * 用户名称
     */
    private String uname;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 总利息
     */
    private BigDecimal totalInterest;

    /**
     * 已还利息
     */
    private BigDecimal repaidInterest;

    /**
     * 还款时间
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date repayTime;

    /**
     * 还款方式(0被动扣款,1主动还款，2白条退款还款)
     */
    private Short repayType;

    /**
     * 还款渠道(1余额,2块钱,3银联,4混合)
     */
    private Short repayChannel;

    public Date getBillTime() {
        return billTime;
    }

    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date billTime;

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBillFlow() {
        return billFlow;
    }

    public void setBillFlow(String billFlow) {
        this.billFlow = billFlow;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getRepaidInterest() {
        return repaidInterest;
    }

    public void setRepaidInterest(BigDecimal repaidInterest) {
        this.repaidInterest = repaidInterest;
    }

    public Date getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(Date repayTime) {
        this.repayTime = repayTime;
    }

    public Short getRepayType() {
        return repayType;
    }

    public void setRepayType(Short repayType) {
        this.repayType = repayType;
    }

    public Short getRepayChannel() {
        return repayChannel;
    }

    public void setRepayChannel(Short repayChannel) {
        this.repayChannel = repayChannel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}