package com.ewfresh.pay.model.unionpayh5suborder;

/**
 * description: 白条还款金额
 * @author: JiuDongDong
 * date: 2019/6/26.
 */
public class WhiteBarRepaymentShopAmount {
    String shopId;//店铺id

    String totalAmount;//该店铺白条还款金额

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
