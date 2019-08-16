package com.ewfresh.pay.configure;

/**
 * description: 99bill的配置
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
public class Bill99SharePayConfigure {
    /** 主收款方联系方式 */
    private String payeeContact;
    /** 主收款方联系方式 */
    private String shopPayeeContact;
    /** 99bill商户私钥路径 */
    private String merchantCertPath;
    /** 99bill商户私钥密码 */
    private String merchantCertPss;
    /** 商户支付、退款交易结果后台通知地址 */
    private String bgUrl;
    /** 合作伙伴用户编号 */
    private String pid;
    /** 支付请求地址 */
    private String payUrl;
    /** 商户公钥路径 */
    private String merchantPubPath;
    /** 退款请求地址*/
    private String refundUrl;
    /** 前台失败回调地址 */
    private String frontFailUrl;

//    private String refundPss;// 99bill退款查询的密码(存管)
//    private String refundPssNotHat;// 99bill退款查询的密码（自营）
//    private String refundPassword;// 99bill退款的密码(存管)
//    private String refundPasswordNotHat;// 99bill退款的密码（自营）
    //
//    private String frontEndUrl;// 前台回调地址

    // 99bill
    //
//    private String refundWebServiceUrl;// 99bill退款WebService请求地址
//    private String singleUrl;// 99bill交易状态查询地址
//    private String orderAccountUrl;// 99bill对账单查询地址
//    private String orderAccUrl;// 商户对账单结果报文后台接收地址

    public String getMerchantCertPss() {
        return merchantCertPss;
    }

    public void setMerchantCertPss(String merchantCertPss) {
        this.merchantCertPss = merchantCertPss;
    }

    public String getPayeeContact() {
        return payeeContact;
    }

    public String getShopPayeeContact() {
        return shopPayeeContact;
    }

    public void setShopPayeeContact(String shopPayeeContact) {
        this.shopPayeeContact = shopPayeeContact;
    }

    public String getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
    }

    public String getMerchantPubPath() {
        return merchantPubPath;
    }

    public void setMerchantPubPath(String merchantPubPath) {
        this.merchantPubPath = merchantPubPath;
    }

    public void setPayeeContact(String payeeContact) {
        this.payeeContact = payeeContact;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public String getMerchantCertPath() {
        return merchantCertPath;
    }

    public void setMerchantCertPath(String merchantCertPath) {
        this.merchantCertPath = merchantCertPath;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getFrontFailUrl() {
        return frontFailUrl;
    }

    public void setFrontFailUrl(String frontFailUrl) {
        this.frontFailUrl = frontFailUrl;
    }
}
