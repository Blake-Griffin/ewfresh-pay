package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_adjust_record
 *
 * @author
 */
public class AdjustRecord implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 白条ID
     */
    private Integer barId;

    /**
     * 本次调整幅度
     */
    private BigDecimal adjustAmount;

    /**
     * 调整类型(0提额,1降额)
     */
    private Short type;

    /**
     * 调整时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date adjustTime;

    /**
     * 调整前金额
     */
    private BigDecimal quotaLimit;

    /**
     * (0为审核,1通过,2未通过)
     */
    private Short apprStatus;

    /**
     * 审核人
     */
    private Integer appr;

    /**
     * 最后修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifyTime;

    private static final long serialVersionUID = 1L;


    public Integer getId() {
        return id;
    }

    public AdjustRecord setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getBarId() {
        return barId;
    }

    public AdjustRecord setBarId(Integer barId) {
        this.barId = barId;
        return this;
    }

    public BigDecimal getAdjustAmount() {
        return adjustAmount;
    }

    public AdjustRecord setAdjustAmount(BigDecimal adjustAmount) {
        this.adjustAmount = adjustAmount;
        return this;
    }

    public Short getType() {
        return type;
    }

    public AdjustRecord setType(Short type) {
        this.type = type;
        return this;
    }

    public Date getAdjustTime() {
        return adjustTime;
    }

    public AdjustRecord setAdjustTime(Date adjustTime) {
        this.adjustTime = adjustTime;
        return this;
    }

    public Short getApprStatus() {
        return apprStatus;
    }

    public AdjustRecord setApprStatus(Short apprStatus) {
        this.apprStatus = apprStatus;
        return this;
    }

    public Integer getAppr() {
        return appr;
    }

    public AdjustRecord setAppr(Integer appr) {
        this.appr = appr;
        return this;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public AdjustRecord setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
        return this;
    }

    public void setQuotaLimit(BigDecimal quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public BigDecimal getQuotaLimit() {
        return quotaLimit;
    }
}