package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * description:资金账户表
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class  Account implements Serializable {
    private Long id;//ID

    private String induceDate;//日期

    private Long userId;//用户ID

    private BigDecimal totalIncome;//总收入

    private BigDecimal totalExpend;//总支出

    private BigDecimal todayIncome;//当日收入

    private BigDecimal todayExpend;//当日支出

    private Short type;//账户类型

    private Short status;//账户状态

    private Integer inducer;//归纳人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date induceTime;//归纳时间

    private Integer approver;//审核人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date apprTime;//审核时间

    private String desp;//描述

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInduceDate() {
        return induceDate;
    }

    public void setInduceDate(String induceDate) {
        this.induceDate = induceDate == null ? null : induceDate.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpend() {
        return totalExpend;
    }

    public void setTotalExpend(BigDecimal totalExpend) {
        this.totalExpend = totalExpend;
    }

    public BigDecimal getTodayIncome() {
        return todayIncome;
    }

    public void setTodayIncome(BigDecimal todayIncome) {
        this.todayIncome = todayIncome;
    }

    public BigDecimal getTodayExpend() {
        return todayExpend;
    }

    public void setTodayExpend(BigDecimal todayExpend) {
        this.todayExpend = todayExpend;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getInducer() {
        return inducer;
    }

    public void setInducer(Integer inducer) {
        this.inducer = inducer;
    }

    public Date getInduceTime() {
        return induceTime;
    }

    public void setInduceTime(Date induceTime) {
        this.induceTime = induceTime;
    }

    public Integer getApprover() {
        return approver;
    }

    public void setApprover(Integer approver) {
        this.approver = approver;
    }

    public Date getApprTime() {
        return apprTime;
    }

    public void setApprTime(Date apprTime) {
        this.apprTime = apprTime;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }
}