package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/15
 */
public class WithdrawtosVo extends Withdrawto {

    private BankAccount bankAccount;

    private List<WithdrawApprRecord> withdrawApprRecord;

    private BigDecimal freezeAmount;//冻结余额

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public List<WithdrawApprRecord> getWithdrawApprRecord() {
        return withdrawApprRecord;
    }

    public void setWithdrawApprRecord(List<WithdrawApprRecord> withdrawApprRecord) {
        this.withdrawApprRecord = withdrawApprRecord;
    }

    public BigDecimal getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(BigDecimal freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

}
