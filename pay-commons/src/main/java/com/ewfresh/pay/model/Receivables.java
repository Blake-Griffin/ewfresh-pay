package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Receivables implements Serializable {

    
    private Integer id;     //Id

    private Long payFlowId; //支付流水

    private Long userId; //用户id

    private String uname;   //客户名称(nickName)

    private BigDecimal financialBalance; //财务余额

    private BigDecimal dueAmout;         //在途订单支付金额
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;            //收款时间

    private BigDecimal settleFee;

    private Short direction; //资金流向

    private String srcAcc;   //源账户

    private String targetAcc;//目标账户

    private Short isBalance; // 是否对账

    private Short isSettle;  // 是否结算

    private Short isInduce;  // 是否入账

    private BigDecimal amount; //涉及金额

    private String busiNo;   //业务单号

    private Short busiType;  //业务类型

    private String operator;//操作人

    private String desp;     //描述
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;
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

    public Long getPayFlowId() {
        return payFlowId;
    }

    public void setPayFlowId(Long payFlowId) {
        this.payFlowId = payFlowId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname == null ? null : uname.trim();
    }

    public BigDecimal getFinancialBalance() {
        return financialBalance;
    }

    public void setFinancialBalance(BigDecimal financialBalance) {
        this.financialBalance = financialBalance;
    }

    public BigDecimal getDueAmout() {
        return dueAmout;
    }

    public void setDueAmout(BigDecimal dueAmout) {
        this.dueAmout = dueAmout;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public BigDecimal getSettleFee() {
        return settleFee;
    }

    public void setSettleFee(BigDecimal settleFee) {
        this.settleFee = settleFee;
    }

    public Short getDirection() {
        return direction;
    }

    public void setDirection(Short direction) {
        this.direction = direction;
    }

    public String getSrcAcc() {
        return srcAcc;
    }

    public void setSrcAcc(String srcAcc) {
        this.srcAcc = srcAcc == null ? null : srcAcc.trim();
    }

    public String getTargetAcc() {
        return targetAcc;
    }

    public void setTargetAcc(String targetAcc) {
        this.targetAcc = targetAcc == null ? null : targetAcc.trim();
    }

    public Short getIsBalance() {
        return isBalance;
    }

    public void setIsBalance(Short isBalance) {
        this.isBalance = isBalance;
    }

    public Short getIsSettle() {
        return isSettle;
    }

    public void setIsSettle(Short isSettle) {
        this.isSettle = isSettle;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getBusiNo() {
        return busiNo;
    }

    public void setBusiNo(String busiNo) {
        this.busiNo = busiNo == null ? null : busiNo.trim();
    }

    public Short getIsInduce() {
        return isInduce;
    }

    public void setIsInduce(Short isInduce) {
        this.isInduce = isInduce;
    }

    public Short getBusiType() {
        return busiType;
    }

    public void setBusiType(Short busiType) {
        this.busiType = busiType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", payFlowId=").append(payFlowId);
        sb.append(", userId=").append(userId);
        sb.append(", uname=").append(uname);
        sb.append(", financialBalance=").append(financialBalance);
        sb.append(", dueAmout=").append(dueAmout);
        sb.append(", receiveTime=").append(receiveTime);
        sb.append(", settleFee=").append(settleFee);
        sb.append(", direction=").append(direction);
        sb.append(", srcAcc=").append(srcAcc);
        sb.append(", targetAcc=").append(targetAcc);
        sb.append(", isBalance=").append(isBalance);
        sb.append(", isSettle=").append(isSettle);
        sb.append(", amount=").append(amount);
        sb.append(", busiNo=").append(busiNo);
        sb.append(", isInduce=").append(isInduce);
        sb.append(", busiType=").append(busiType);
        sb.append(", operator=").append(operator);
        sb.append(", desp=").append(desp);
        sb.append(", operateTime=").append(operateTime);
        sb.append(", lastModifyTime=").append(lastModifyTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}