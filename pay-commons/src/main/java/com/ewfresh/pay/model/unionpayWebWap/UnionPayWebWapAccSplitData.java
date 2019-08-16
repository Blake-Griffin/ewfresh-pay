package com.ewfresh.pay.model.unionpaywebwap;


/**
 * description: 银联跳转网关支付分账域
 * @author: JiuDongDong
 * date: 2019/5/23.
 */
public class UnionPayWebWapAccSplitData {
    private String accSplitType;//分账类型：1表示按照商户列表分账，联机带入分账入账金额；2表示按照分账规则ID分账，联机带分账决定要素

    public String getAccSplitType() {
        return accSplitType;
    }

    public void setAccSplitType(String accSplitType) {
        this.accSplitType = accSplitType;
    }
}
