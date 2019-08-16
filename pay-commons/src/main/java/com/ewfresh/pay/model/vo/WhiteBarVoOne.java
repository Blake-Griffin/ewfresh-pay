package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.ewfresh.pay.model.WhiteBar;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: LouZiFeng
 * @Description: 客户白条封装类
 * @Date: 2019/3/22
 */
public class WhiteBarVoOne extends WhiteBar {

    private Integer barId;//白条id

    private BigDecimal totalSum;//已使用额度

    private BigDecimal quotaLimit;//调整前金额

    private BigDecimal totalAmount;//总额度

    private String introducer;//招商人员姓名

    private String realName;//姓名

    private Short type;//调整类型

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date adjustTime; //  调整时间

    private BigDecimal availableQuota;//可用额度

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    public String getIntroducer() {
        return introducer;
    }

    public void setIntroducer(String introducer) {
        this.introducer = introducer;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public BigDecimal getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(BigDecimal availableQuota) {
        this.availableQuota = availableQuota;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getAdjustTime() {
        return adjustTime;
    }

    public void setAdjustTime(Date adjustTime) {
        this.adjustTime = adjustTime;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public Integer getBarId() {
        return barId;
    }

    public void setBarId(Integer barId) {
        this.barId = barId;
    }

    public BigDecimal getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(BigDecimal quotaLimit) {
        this.quotaLimit = quotaLimit;
    }
}
