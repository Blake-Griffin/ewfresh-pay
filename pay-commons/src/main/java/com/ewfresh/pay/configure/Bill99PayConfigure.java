package com.ewfresh.pay.configure;

/**
 * description: 99bill的配置
 * @author: JiuDongDong
 * date: 2018/7/31.
 */
public class Bill99PayConfigure {
    private String merId;// 易网聚鲜在99bill注册的商户号(存管)
    private String merIdNotHat;// 易网聚鲜在99bill注册的商户号（自营）
    private String merchantAcctId;// 易网聚鲜在99bill注册的人民币网关账号(存管)
    private String merchantAcctIdNotHat;// 易网聚鲜在99bill注册的人民币网关账号（自营）
    private String merchantCertPath;// 99bill商户私钥路径
    private String merchantCertPss;// 99bill商户私钥密码
    private String refundPss;// 99bill退款查询的密码(存管)
    private String refundPssNotHat;// 99bill退款查询的密码（自营）
    private String refundPassword;// 99bill退款的密码(存管)
    private String refundPasswordNotHat;// 99bill退款的密码（自营）
    private String merchantPubPath;// 商户公钥路径
    private String frontEndUrl;// 前台回调地址
    private String frontFailUrl;// 前台失败回调地址
    private String bgUrl;// 商户支付、退款交易结果后台通知地址
    private String payUrl;// 99bill支付请求地址
    private String refundUrl;// 99bill退款请求地址
    private String refundWebServiceUrl;// 99bill退款WebService请求地址
    private String singleUrl;// 99bill交易状态查询地址
    private String orderAccountUrl;// 99bill对账单查询地址
    private String orderAccUrl;// 商户对账单结果报文后台接收地址
    private String withDrawBgUrl;// 商户提现结果后台通知地址    //add by zhaoqun
    private String settleUrl;// 分账请求地址    //add by zhaoqun
    private String domainName;//99bilde域名
    private String platformCode;//平台商户号 HAT
    private String hatPublicKey;//公钥    add by huansuqing
    private String hatPrivateKey;//私钥

    public String getWithDrawBgUrl() {
        return withDrawBgUrl;
    }

    public void setWithDrawBgUrl(String withDrawBgUrl) {
        this.withDrawBgUrl = withDrawBgUrl;
    }

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

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
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

    public String getMerchantAcctId() {
        return merchantAcctId;
    }

    public void setMerchantAcctId(String merchantAcctId) {
        this.merchantAcctId = merchantAcctId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getRefundPss() {
        return refundPss;
    }

    public void setRefundPss(String refundPss) {
        this.refundPss = refundPss;
    }

    public String getRefundWebServiceUrl() {
        return refundWebServiceUrl;
    }

    public void setRefundWebServiceUrl(String refundWebServiceUrl) {
        this.refundWebServiceUrl = refundWebServiceUrl;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getSettleUrl() {
        return settleUrl;
    }

    public void setSettleUrl(String settleUrl) {
        this.settleUrl = settleUrl;
    }

    public String getMerIdNotHat() {
        return merIdNotHat;
    }

    public void setMerIdNotHat(String merIdNotHat) {
        this.merIdNotHat = merIdNotHat;
    }

    public String getMerchantAcctIdNotHat() {
        return merchantAcctIdNotHat;
    }

    public void setMerchantAcctIdNotHat(String merchantAcctIdNotHat) {
        this.merchantAcctIdNotHat = merchantAcctIdNotHat;
    }

    public String getRefundPssNotHat() {
        return refundPssNotHat;
    }

    public void setRefundPssNotHat(String refundPssNotHat) {
        this.refundPssNotHat = refundPssNotHat;
    }

    public String getRefundPassword() {
        return refundPassword;
    }

    public void setRefundPassword(String refundPassword) {
        this.refundPassword = refundPassword;
    }

    public String getRefundPasswordNotHat() {
        return refundPasswordNotHat;
    }

    public void setRefundPasswordNotHat(String refundPasswordNotHat) {
        this.refundPasswordNotHat = refundPasswordNotHat;
    }

    public String getHatPublicKey() {
        return hatPublicKey;
    }

    public void setHatPublicKey(String hatPublicKey) {
        this.hatPublicKey = hatPublicKey;
    }

    public String getHatPrivateKey() {
        return hatPrivateKey;
    }

    public void setHatPrivateKey(String hatPrivateKey) {
        this.hatPrivateKey = hatPrivateKey;
    }
}
