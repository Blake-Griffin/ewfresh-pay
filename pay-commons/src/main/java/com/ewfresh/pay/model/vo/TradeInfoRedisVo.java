package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description: 快钱快捷支付信息
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
public class TradeInfoRedisVo implements Serializable {
    private String externalRefNumber;// 订单编号

    private String txnType;// 交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易

    public String getExternalRefNumber() {
        return externalRefNumber;
    }

    public void setExternalRefNumber(String externalRefNumber) {
        this.externalRefNumber = externalRefNumber;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }
}
