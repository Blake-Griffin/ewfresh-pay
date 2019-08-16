package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * t_monthly_sales
 * @author 
 */
public class MonthlySales implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    private Integer barId;//白条id

    /**
     * 用户ID
     */
    private Integer uid;

    /**
     * 公司名称(唯一标识)
     */
    private String nickName;

    /**
     * 自营月交易额
     */
    private BigDecimal selfMonthlySales;

    /**
     * 店铺月交易额
     */
    private BigDecimal shopMonthlySales;

    /**
     * 交易月份
     */
    private String dealMonth;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private BigDecimal totalSum;//上个月使用额度

    private BigDecimal totalLimit;//当前总额度

    private BigDecimal twentyFive ;

    private BigDecimal threeFive ;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public BigDecimal getSelfMonthlySales() {
        return selfMonthlySales;
    }

    public void setSelfMonthlySales(BigDecimal selfMonthlySales) {
        this.selfMonthlySales = selfMonthlySales;
    }

    public BigDecimal getShopMonthlySales() {
        return shopMonthlySales;
    }

    public void setShopMonthlySales(BigDecimal shopMonthlySales) {
        this.shopMonthlySales = shopMonthlySales;
    }

    public String getDealMonth() {
        return dealMonth;
    }

    public void setDealMonth(String dealMonth) {
        this.dealMonth = dealMonth;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(BigDecimal totalSum) {
        this.totalSum = totalSum;
    }

    public BigDecimal getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(BigDecimal totalLimit) {
        this.totalLimit = totalLimit;
    }

    public BigDecimal getTwentyFive() {
        return twentyFive;
    }

    public void setTwentyFive(BigDecimal twentyFive) {
        this.twentyFive = twentyFive;
    }

    public BigDecimal getThreeFive() {
        return threeFive;
    }

    public void setThreeFive(BigDecimal threeFive) {
        this.threeFive = threeFive;
    }

    public Integer getBarId() {
        return barId;
    }

    public void setBarId(Integer barId) {
        this.barId = barId;
    }
}