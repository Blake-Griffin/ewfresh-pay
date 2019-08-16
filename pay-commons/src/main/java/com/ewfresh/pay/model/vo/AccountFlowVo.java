package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.AccountFlow;

import java.math.BigDecimal;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/4/17 0017
 */
public class AccountFlowVo extends AccountFlow {


    private BigDecimal availableBalance ;

    private Integer randomNum ;

    private String userStatus;

    private BigDecimal billInsert;

    public Integer getRandomNum() {
        return randomNum;
    }

    public void setRandomNum(Integer randomNum) {
        this.randomNum = randomNum;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public BigDecimal getAvailableBalance() {
        BigDecimal balance = getBalance();
        BigDecimal freezeAmount = getFreezeAmount();
        if (balance == null || freezeAmount == null){
            return null;
        }
        availableBalance = balance.subtract(freezeAmount);

        return availableBalance.doubleValue() > 0 ? availableBalance : new BigDecimal(0);
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getBillInsert() {
        return billInsert;
    }

    public void setBillInsertt(BigDecimal billInsert) {
        this.billInsert = billInsert;
    }
}
