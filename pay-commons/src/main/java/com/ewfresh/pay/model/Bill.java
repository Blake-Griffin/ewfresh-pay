package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_bill
 *
 * @author
 */
public class Bill implements Serializable {
    /**
     * ID
     */
    private Integer id;

    private Long userId;

    private String uname;

    public long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * 账单批次号
     */
    private String billFlow;

    /**
     * 账单金额
     */
    private BigDecimal billAmount;

    /**
     * 已还金额
     */
    private BigDecimal repaidAmount;


    /**
     * 总利息
     */
    private BigDecimal totalInterest;



    /**
     * 已还利息
     */
    private BigDecimal repaidInterest;

    /**
     * 出账日期
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date billTime;

    /**
     * 最后还款日期
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastRepaidTime;

    /**
     * 账单状态(1待还款,2正常已还款,3部分还款,4已完结,5逾期已还款)
     */
    private Short billStatus;

    /**
     * 最后修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifyTime;

    /**
     * 账单生成时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatTime;
    private static final long serialVersionUID = 1L;


    public Integer getId() {
        return id;
    }

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    public Bill setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getBillFlow() {
        return billFlow;
    }

    public Bill setBillFlow(String billFlow) {
        this.billFlow = billFlow;
        return this;
    }

    public BigDecimal getBillAmount() {
        return billAmount;
    }

    public Bill setBillAmount(BigDecimal billAmount) {
        this.billAmount = billAmount;
        return this;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public Bill setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
        return this;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public Bill setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
        return this;
    }

    public BigDecimal getRepaidInterest() {
        return repaidInterest;
    }

    public Bill setRepaidInterest(BigDecimal repaidInterest) {
        this.repaidInterest = repaidInterest;
        return this;
    }

    public Date getBillTime() {
        return billTime;
    }

    public Bill setBillTime(Date billTime) {
        this.billTime = billTime;
        return this;
    }

    public Date getLastRepaidTime() {
        return lastRepaidTime;
    }

    public Bill setLastRepaidTime(Date lastRepaidTime) {
        this.lastRepaidTime = lastRepaidTime;
        return this;
    }

    public Short getBillStatus
            () {
        return billStatus;
    }

    public Bill setBillStatus(Short billStatus) {
        this.billStatus = billStatus;
        return this;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public Bill setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
        return this;
    }
}