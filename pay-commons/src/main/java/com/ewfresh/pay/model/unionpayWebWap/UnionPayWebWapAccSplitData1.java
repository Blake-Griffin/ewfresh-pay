package com.ewfresh.pay.model.unionpaywebwap;

import java.util.List;

/**
 * description: 银联跳转网关支付分账域---按照商户列表分账
 * @author: JiuDongDong
 * date: 2019/5/23.
 */
public class UnionPayWebWapAccSplitData1 extends UnionPayWebWapAccSplitData {

    private List<AccSplitMcht> accSplitMchts;//分账对象组。分账类型为 1 时才出现，accSplitMcht数组，最多支持 5 个分账对象 accSplitMcht

    public List<AccSplitMcht> getAccSplitMchts() {
        return accSplitMchts;
    }

    public void setAccSplitMchts(List<AccSplitMcht> accSplitMchts) {
        this.accSplitMchts = accSplitMchts;
    }
}
