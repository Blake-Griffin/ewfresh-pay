package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description: 快钱 提现申请参数封装类
 *
 * @author: ZhaoQun
 * date: 2018/8/10.
 */
public class Bill99WithdrawAccountVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String withdrawId;//提现ID

    private String outTradeNo;//外部交易号

    private String uId;//平台用户 id

    private String platformCode;//商户平台代码

    private String amount;//提现金额

    private String bgUrl;//通知地址

    private String customerFee;//会员自付手续费

    private String merchantFee;//商户代付手续费

    private String memo;//交易摘要

    private String memberBankAcctId;//银行卡主键 Id 信息

    private String bankAcctId;//银行账号

    private String dealId;//支付渠道流水号

    public String getWithdrawId() {
        return withdrawId;
    }

    public void setWithdrawId(String withdrawId) {
        this.withdrawId = withdrawId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public String getCustomerFee() {
        return customerFee;
    }

    public void setCustomerFee(String customerFee) {
        this.customerFee = customerFee;
    }

    public String getMerchantFee() {
        return merchantFee;
    }

    public void setMerchantFee(String merchantFee) {
        this.merchantFee = merchantFee;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getMemberBankAcctId() {
        return memberBankAcctId;
    }

    public void setMemberBankAcctId(String memberBankAcctId) {
        this.memberBankAcctId = memberBankAcctId;
    }

    public String getBankAcctId() {
        return bankAcctId;
    }

    public void setBankAcctId(String bankAcctId) {
        this.bankAcctId = bankAcctId;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

}
