package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Withdrawto implements Serializable {

    private Long id;               //id

    private Long uid;              //用户id

    private String uname;          //用户名

    private String nickName;      //客户名称

    private String phone;          //用户手机

    private BigDecimal amount;     //提现金额

    private Short accType;         //提现账户类型(1个人,2店铺)

    private Integer bankAccountId; //提现银行编号

    private Short apprStatus;      //审核状态 (0未审核,1一审,2二审,3三审,4四审.5审核不通过.6已完成.7已取消.8提现失败)

    private Short beforeStatus; //前置状态(0未审核,1一审,2二审,3三审,4四审.5审核不通过.6已完成.7已取消.8提现失败)

    private Long approver;         //审核人

    private Integer version;       //版本号

    private Long cancelId;         //取消人
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;       //取消时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifyTime;

    private String remark;        //备注

    private String outTradeNo;   //HAT提现外部交易号

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.uname = uname == null ? null : uname.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Short getAccType() {
        return accType;
    }

    public void setAccType(Short accType) {
        this.accType = accType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Integer bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public Short getApprStatus() {
        return apprStatus;
    }

    public void setApprStatus(Short apprStatus) {
        this.apprStatus = apprStatus;
    }

    public Short getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Short beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Long getApprover() {
        return approver;
    }

    public void setApprover(Long approver) {
        this.approver = approver;
    }

    public Long getCancelId() {
        return cancelId;
    }

    public void setCancelId(Long cancelId) {
        this.cancelId = cancelId;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", uid=").append(uid);
        sb.append(", uname=").append(uname);
        sb.append(", phone=").append(phone);
        sb.append(", amount=").append(amount);
        sb.append(", bankAccountId=").append(bankAccountId);
        sb.append(", apprStatus=").append(apprStatus);
        sb.append(", beforeStatus=").append(beforeStatus);
        sb.append(", approver=").append(approver);
        sb.append(", cancelId=").append(cancelId);
        sb.append(", cancelTime=").append(cancelTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", lastModifyTime=").append(lastModifyTime);
        sb.append(", remark=").append(remark);
        sb.append(", outTradeNo=").append(outTradeNo);
        sb.append(", accType=").append(accType);
        sb.append(", nickName=").append(nickName);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}