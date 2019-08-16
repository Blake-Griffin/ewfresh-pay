package com.ewfresh.pay.model.unionpaywebwap;

/**
 * description:银联跳转网关支付分账域---按照分账规则 ID 分账
 * @author: JiuDongDong
 * date: 2019/5/23.
 */
public class UnionPayWebWapAccSplitData2 extends UnionPayWebWapAccSplitData {

    private String accSplitRuleId;//分账规则ID。 分账类型为 2 时才出现，15 位字母或数字

    public String getAccSplitRuleId() {
        return accSplitRuleId;
    }

    public void setAccSplitRuleId(String accSplitRuleId) {
        this.accSplitRuleId = accSplitRuleId;
    }
}
