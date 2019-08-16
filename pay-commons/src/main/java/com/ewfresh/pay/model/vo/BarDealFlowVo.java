package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.BarDealFlow;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/15.
 */
public class BarDealFlowVo extends BarDealFlow {
    /**
     * 使用状态(0未开始使用,1正常,2冻结)
     */
    private Short useStatus;

    /**
     * 当前总额度
     */
    private BigDecimal totalLimit;

    /**
     * 用户状态(0 可用，1冻结)
     */
    private String userStatus;

    /**
     * 可用额度
     */
    private BigDecimal whiteBalance;

    public Short getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Short useStatus) {
        this.useStatus = useStatus;
    }

    public BigDecimal getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(BigDecimal totalLimit) {
        this.totalLimit = totalLimit;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public BigDecimal getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(BigDecimal whiteBalance) {
        this.whiteBalance = whiteBalance;
    }
}
