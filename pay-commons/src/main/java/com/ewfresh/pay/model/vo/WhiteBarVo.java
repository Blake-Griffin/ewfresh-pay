package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.ewfresh.pay.model.WhiteBar;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/20.
 */
public class WhiteBarVo extends WhiteBar {

    private Integer barId;//白条id

    private String introducer;//招商人员姓名

    private String realName;//姓名

    private BigDecimal quotaLimit;//调整前金额

    private BigDecimal usedLimit;//已使用额度

    private BigDecimal adjustAmount;//本次调整幅度


    /**
     * 调整时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date adjustTime;

    /**
     * 可用额度
     */
    private BigDecimal whiteBalance;

    /**
     * 用户状态(0 可用，1冻结)
     */
    private String userStatus;

    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    public void setUsedLimit(BigDecimal usedLimit) {
        this.usedLimit = usedLimit;
    }

    public BigDecimal getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(BigDecimal whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
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

    public BigDecimal getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(BigDecimal adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public Date getAdjustTime() {
        return adjustTime;
    }

    public void setAdjustTime(Date adjustTime) {
        this.adjustTime = adjustTime;
    }
}
