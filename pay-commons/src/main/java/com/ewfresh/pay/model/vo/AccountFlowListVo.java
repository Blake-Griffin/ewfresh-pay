package com.ewfresh.pay.model.vo;

import java.math.BigDecimal;

/**
 * Description:余额日志vo类
 *
 * @author wangyaohui
 * Date 2018/5/2
 **/
public class AccountFlowListVo {
    private Long uid;

    private String uname;

    private String phone;

    private BigDecimal balance;

    private Short accType;//账户类型 1:支付宝,2:微信,3:银行卡,4:余额,5:快钱

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Short getAccType() {
        return accType;
    }

    public void setAccType(Short accType) {
        this.accType = accType;
    }
}
