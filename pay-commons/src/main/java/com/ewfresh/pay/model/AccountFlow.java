package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * description:资金账户流水
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class AccountFlow implements Serializable {

    private Integer accFlowId;//流水ID

    private Integer payFlowId;//支付流水ID

    private Long userId;//用户ID

    private BigDecimal balance;//账户余额

    private BigDecimal freezeAmount;//冻结余额

    private Short direction;//资金流向 1:流入,2:流出

    private String srcAcc;//源账户

    private String targetAcc;//目标账户

    private Short accType;//账户类型 1:支付宝,2:微信,3:银行卡,4:余额,5:快钱,6:白条

    private BigDecimal amount;//涉及金额

    private Short busiType;//业务类型(1:充值,2:提现,3:订单扣款,4:结算扣款,5:销售收入,6:退款收入)

    private String busiNo;//业务流水号

    private Short isInduce;//是否入账 0:否,1:是

    private Short isBalance;//是否对账 0:否,1:是

    private Short isSettle;//是否结算 0:否,1:是
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occTime;//发生时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date balanceTime;//对账时间V
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settleTime;//结算时间

    private Integer riskDays;//风险预存天数
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifyTime;//最后修改时间

    private String operator;//业务操作人

    private String desp;//备注

    private String uname;//   用户名

    private String explain;//说明

    private Short isIndividual; //是否个人账户(0个人账户,1店铺账户)

    private Short srcAccType; //源账户类型(1个人,2店铺)

    private Short targetAccType; //目标账户类型(1个人,2店铺)

    private static final long serialVersionUID = 1L;

    public Integer getAccFlowId() {
        return accFlowId;
    }

    public void setAccFlowId(Integer accFlowId) {
        this.accFlowId = accFlowId;
    }

    public Integer getPayFlowId() {
        return payFlowId;
    }

    public void setPayFlowId(Integer payFlowId) {
        this.payFlowId = payFlowId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    public Short getAccType() {
        return accType;
    }

    public void setAccType(Short accType) {
        this.accType = accType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Short getBusiType() {
        return busiType;
    }

    public void setBusiType(Short busiType) {
        this.busiType = busiType;
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

    public Date getOccTime() {
        return occTime;
    }

    public void setOccTime(Date occTime) {
        this.occTime = occTime;
    }

    public Date getBalanceTime() {
        return balanceTime;
    }

    public void setBalanceTime(Date balanceTime) {
        this.balanceTime = balanceTime;
    }

    public Date getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(Date settleTime) {
        this.settleTime = settleTime;
    }

    public Integer getRiskDays() {
        return riskDays;
    }

    public void setRiskDays(Integer riskDays) {
        this.riskDays = riskDays;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp == null ? null : desp.trim();
    }

    public BigDecimal getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(BigDecimal freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public Short getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(Short isIndividual) {
        this.isIndividual = isIndividual;
    }

    public Short getSrcAccType() {
        return srcAccType;
    }

    public void setSrcAccType(Short srcAccType) {
        this.srcAccType = srcAccType;
    }

    public Short getTargetAccType() {
        return targetAccType;
    }

    public void setTargetAccType(Short targetAccType) {
        this.targetAccType = targetAccType;
    }

}