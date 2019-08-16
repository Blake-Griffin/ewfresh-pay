package com.ewfresh.pay.model.vo;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Class description
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
public class InterestFlowVo {
    private Long       id;
    private String     uname;
    private BigDecimal repaidInterest;
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date       repayTime;
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date       billTime;

    public Date getBillTime() {
        return billTime;
    }
    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public BigDecimal getRepaidInterest() {
        return repaidInterest;
    }
    public void setRepaidInterest(BigDecimal repaidInterest) {
        this.repaidInterest = repaidInterest;
    }
    public Date getRepayTime() {
        return repayTime;
    }
    public void setRepayTime(Date repayTime) {
        this.repayTime = repayTime;
    }

    public String getUname() {
        return uname;
    }
    public void setUname(String uname) {
        this.uname = uname;
    }
}

