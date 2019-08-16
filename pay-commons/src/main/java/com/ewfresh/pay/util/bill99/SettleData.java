package com.ewfresh.pay.util.bill99;

/**
 * Description:快钱用于分账的类
 * @author DuanXiangming
 * Date 2018/8/16 0016
 */
public class SettleData {

    private String outSubOrderNo;//结算子批次号
    private String merchantUid;  //结算子商户号
    private String amount;       //结算金额
    private String settlePeriod; //格式：T+n 或 D+n； 0<=n<=99。必传
    private String origOutSubOrderNo;//退货时为对应消费交易的外部子单编号,必传


    public String getOutSubOrderNo() {
        return outSubOrderNo;
    }

    public void setOutSubOrderNo(String outSubOrderNo) {
        this.outSubOrderNo = outSubOrderNo;
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

    public String getOrigOutSubOrderNo() {
        return origOutSubOrderNo;
    }

    public void setOrigOutSubOrderNo(String origOutSubOrderNo) {
        this.origOutSubOrderNo = origOutSubOrderNo;
    }
}
