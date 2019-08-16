package com.ewfresh.pay.util.bill99;

/**
 * Description:结算结果对象
 *
 * @author DuanXiangming
 * Date 2018/11/8 0008
 */
public class SettleResult {

    private String outSubOrderNo;//外部分账单子单编号
    private String origOutSubOrderNo;//退货时为对应消费交易的外部子单编号
    private String txnType;//交易类型： 1-消费，2-退货
    private String merchantUid;//当分账的商户是子商户时有值
    private String amount;//分账金额
    private String settlePeriod; //结算周期
    private String settleStatus;//结算状态：0-初始化, 8-结算失败, 9-结算成功


    public String getOutSubOrderNo() {
        return outSubOrderNo;
    }

    public void setOutSubOrderNo(String outSubOrderNo) {
        this.outSubOrderNo = outSubOrderNo;
    }

    public String getOrigOutSubOrderNo() {
        return origOutSubOrderNo;
    }

    public void setOrigOutSubOrderNo(String origOutSubOrderNo) {
        this.origOutSubOrderNo = origOutSubOrderNo;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getMerchantUid() {
        return merchantUid;
    }

    public void setMerchantUid(String merchantUid) {
        this.merchantUid = merchantUid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSettlePeriod() {
        return settlePeriod;
    }

    public void setSettlePeriod(String settlePeriod) {
        this.settlePeriod = settlePeriod;
    }

    public String getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(String settleStatus) {
        this.settleStatus = settleStatus;
    }
}
