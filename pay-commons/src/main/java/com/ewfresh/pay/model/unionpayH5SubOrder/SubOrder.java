package com.ewfresh.pay.model.unionpayh5suborder;

/**
 * description: 银联H5分账
 * @author: JiuDongDong
 * date: 2019/5/24.
 */
public class SubOrder {
    private String mid;// 子商户号

    private String totalAmount;// 子商户分账金额

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
