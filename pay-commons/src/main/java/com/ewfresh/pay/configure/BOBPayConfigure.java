package com.ewfresh.pay.configure;

/**
 * description:
 *      BOB的配置
 * @author: JiuDongDong
 * date: 2018/4/20.
 */
public class BOBPayConfigure {
    private String merId;// 易网聚鲜在北京银行注册的商户号
    private String merchantCertPath;// 北京银行商户私钥路径
    private String merchantCertPss;// 北京银行商户私钥密码
    private String merchantPubPath;// 商户公钥路径
    private String frontEndUrl;// 前台回调地址
    private String frontFailUrl;// 前台失败回调地址
    private String backEndUrl;// 商户支付、退款交易结果后台通知地址
    private String payUrl;// 北京银行支付请求地址
    private String refundUrl;// 北京银行退款请求地址
    private String singleUrl;// 北京银行交易状态查询地址
    private String orderAccountUrl;// 北京银行对账单查询地址
    private String orderAccUrl;// 商户对账单结果报文后台接收地址

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getMerchantCertPath() {
        return merchantCertPath;
    }

    public void setMerchantCertPath(String merchantCertPath) {
        this.merchantCertPath = merchantCertPath;
    }

    public String getMerchantCertPss() {
        return merchantCertPss;
    }

    public void setMerchantCertPss(String merchantCertPss) {
        this.merchantCertPss = merchantCertPss;
    }

    public String getMerchantPubPath() {
        return merchantPubPath;
    }

    public void setMerchantPubPath(String merchantPubPath) {
        this.merchantPubPath = merchantPubPath;
    }

    public String getFrontEndUrl() {
        return frontEndUrl;
    }

    public void setFrontEndUrl(String frontEndUrl) {
        this.frontEndUrl = frontEndUrl;
    }

    public String getBackEndUrl() {
        return backEndUrl;
    }

    public void setBackEndUrl(String backEndUrl) {
        this.backEndUrl = backEndUrl;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
    }

    public String getSingleUrl() {
        return singleUrl;
    }

    public void setSingleUrl(String singleUrl) {
        this.singleUrl = singleUrl;
    }

    public String getOrderAccountUrl() {
        return orderAccountUrl;
    }

    public void setOrderAccountUrl(String orderAccountUrl) {
        this.orderAccountUrl = orderAccountUrl;
    }

    public String getOrderAccUrl() {
        return orderAccUrl;
    }

    public void setOrderAccUrl(String orderAccUrl) {
        this.orderAccUrl = orderAccUrl;
    }

    public String getFrontFailUrl() {
        return frontFailUrl;
    }

    public void setFrontFailUrl(String frontFailUrl) {
        this.frontFailUrl = frontFailUrl;
    }
}
