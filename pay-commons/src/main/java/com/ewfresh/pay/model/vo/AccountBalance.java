package com.ewfresh.pay.model.vo;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/11/1 0001
 */
public class AccountBalance {


    String accountBalanceType; //账户余额类型
    String accountName;        //账户名
    String balance;            //账户余额
    String availableBalance;   //可提现账户中可用部分金额，其他账户类型该字段暂时无意义；单位为分


    public String getAccountBalanceType() {
        return accountBalanceType;
    }

    public void setAccountBalanceType(String accountBalanceType) {
        this.accountBalanceType = accountBalanceType;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }
}
