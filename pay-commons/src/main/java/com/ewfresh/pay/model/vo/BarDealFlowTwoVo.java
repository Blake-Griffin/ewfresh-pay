/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/4/16.
 **/
 
package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.ewfresh.pay.model.BarDealFlow;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
  * @descrption TODO
 * @author gaoyongqiqng
 * @create 2019-04-16
 * @Email 1005267839@qq.com
  **/
public class BarDealFlowTwoVo extends BarDealFlow {
    /**
     * 客户名称
     */
    private String uname;
    /**
     * 本次交易金额
     */
    private BigDecimal amount;

    /**
     * 已使用额度
     */
    private BigDecimal usedLimit;

    /**
     * 说明信息
     */
    private String explainInfo ;
    /**
     * 交易时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occTime;

    @Override
    public String getUname() {
        return uname;
    }

    @Override
    public void setUname(String uname) {
        this.uname = uname;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public BigDecimal getUsedLimit() {
        return usedLimit;
    }

    @Override
    public void setUsedLimit(BigDecimal usedLimit) {
        this.usedLimit = usedLimit;
    }

    public String getExplainInfo() {
        return explainInfo;
    }

    public void setExplainInfo(String explainInfo) {
        this.explainInfo = explainInfo;
    }

    @Override
    public Date getOccTime() {
        return occTime;
    }

    @Override
    public void setOccTime(Date occTime) {
        this.occTime = occTime;
    }
}
