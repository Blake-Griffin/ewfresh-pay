package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_white_bar
 *
 * @author
 */
public class WhiteBar implements Serializable {
    /**
     * ID
     */
    private Integer id;

    /**
     * 用户uid
     */
    private Long uid;

    /**
     * 客户名称
     */
    private String uname;

    /**
     * 初始额度
     */
    private BigDecimal initialLimit;

    /**
     * 当前总额度
     */
    private BigDecimal totalLimit;

    /**
     * 调整额度
     */
    private BigDecimal adjustLimit;

    /**
     * 使用状态(0未开始使用,1正常,2冻结)
     */
    private Short useStatus;

    /**
     * 还款账期
     */
    private Integer period;

    /**
     * 调额审核状态(0待审核,1通过,2不通过)
     */
    private Short apprStatus;

    /**
     * 拒绝原因
     */
    private String reason;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后调额申请时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastAdjustTime;

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

    public void setId(Integer id) {
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
        this.uname = uname;
    }

    public BigDecimal getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(BigDecimal totalLimit) {
        this.totalLimit = totalLimit;
    }

    public BigDecimal getAdjustLimit() {
        return adjustLimit;
    }

    public void setAdjustLimit(BigDecimal adjustLimit) {
        this.adjustLimit = adjustLimit;
    }

    public Short getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Short useStatus) {
        this.useStatus = useStatus;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Short getApprStatus() {
        return apprStatus;
    }

    public void setApprStatus(Short apprStatus) {
        this.apprStatus = apprStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastAdjustTime() {
        return lastAdjustTime;
    }

    public void setLastAdjustTime(Date lastAdjustTime) {
        this.lastAdjustTime = lastAdjustTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getInitialLimit() {
        return initialLimit;
    }

    public void setInitialLimit(BigDecimal initialLimit) {
        this.initialLimit = initialLimit;
    }
}