package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class WithdrawApprRecord {
    private Long id;

    private Long withdtoId;//提现ID

    private Short beforeStatus;//前置状态(0未审核,1一审,2二审,3三审,4四审.5审核不通过.6已完成.7已取消)

    private Short apprStatus;      //审核状态

    private Long approver;//审批人

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date apprTime;//审批时间

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间

    private String desp;//备注

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWithdtoId() {
        return withdtoId;
    }

    public void setWithdtoId(Long withdtoId) {
        this.withdtoId = withdtoId;
    }

    public Short getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Short beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Short getApprStatus() {
        return apprStatus;
    }

    public void setApprStatus(Short apprStatus) {
        this.apprStatus = apprStatus;
    }

    public Long getApprover() {
        return approver;
    }

    public void setApprover(Long approver) {
        this.approver = approver;
    }

    public Date getApprTime() {
        return apprTime;
    }

    public void setApprTime(Date apprTime) {
        this.apprTime = apprTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }
}