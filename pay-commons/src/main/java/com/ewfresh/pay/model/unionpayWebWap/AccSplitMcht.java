package com.ewfresh.pay.model.unionpaywebwap;

/**
 * description: 分账对象子域
 * @author: JiuDongDong
 * date: 2019/5/23.
 */
public class AccSplitMcht {
    private String accSplitMerId;//分账二级商户代码。15位字母或数字

    private String accSplitAmt;//分账入账金额。最大支持9位长度（精确到分，不带小数点），如300 表示分账入账金额为 3 元。

    public String getAccSplitMerId() {
        return accSplitMerId;
    }

    public void setAccSplitMerId(String accSplitMerId) {
        this.accSplitMerId = accSplitMerId;
    }

    public String getAccSplitAmt() {
        return accSplitAmt;
    }

    public void setAccSplitAmt(String accSplitAmt) {
        this.accSplitAmt = accSplitAmt;
    }
}
