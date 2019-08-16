package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      商户查询订单信息（普通订单查询和支持卡户信息判断的订单查询都使用这个类封装参数）
 * @author: JiuDongDong
 * date: 2018/4/11.
 */
public class BOCQueryOrderVo implements Serializable {
    private static final long serialVersionUID = 5359234116700296665L;

    private String merchantNo;//商户号

    private String orderNos;//商户订单号字符串

    private String signData;//商户签名信息

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getOrderNos() {
        return orderNos;
    }

    public void setOrderNos(String orderNos) {
        this.orderNos = orderNos;
    }

    public String getSignData() {
        return signData;
    }

    public void setSignData(String signData) {
        this.signData = signData;
    }

}
