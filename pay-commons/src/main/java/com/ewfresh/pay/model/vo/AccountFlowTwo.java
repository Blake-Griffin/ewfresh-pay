package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
/**
 * Description:前台个人账户vo类
 *
 * @author louzifeng
 * Date 2018/11/13
 **/
public class AccountFlowTwo {

    private Integer accFlowId;//流水ID

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occTime;//发生时间

    private BigDecimal amount;//涉及金额

    private String accType;//支付渠道

    private String busiNo;//业务流水号

    private String desp;//备注

    public Integer getAccFlowId() {
        return accFlowId;
    }

    public void setAccFlowId(Integer accFlowId) {
        this.accFlowId = accFlowId;
    }

    public Date getOccTime() {
        return occTime;
    }

    public void setOccTime(Date occTime) {
        this.occTime = occTime;
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
        this.busiNo = busiNo;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }
}
