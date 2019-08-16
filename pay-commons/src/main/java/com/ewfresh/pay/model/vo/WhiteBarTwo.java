package com.ewfresh.pay.model.vo;

import java.math.BigDecimal;
/**
  * @Author: LouZiFeng
  * @Description:   总额度调整信息
  */
public class WhiteBarTwo {

    private BigDecimal totalSum;//上个月使用额度

    private BigDecimal totalLimit;//当前总额度

    private BigDecimal twentyFive ;

    private BigDecimal threeFive ;

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
}
