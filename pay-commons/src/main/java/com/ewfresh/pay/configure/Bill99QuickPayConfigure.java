package com.ewfresh.pay.configure;

/**
 * description: 99bill快捷支付的配置
 * @author: JiuDongDong
 * date: 2018/9/14.
 */
public class Bill99QuickPayConfigure {
    private String merId;// 易网聚鲜在99bill注册的商户号(存管)
    private String merIdNotHat;// 易网聚鲜在99bill注册的商户号（自营）
    private String merchantAcctId;// 易网聚鲜在99bill注册的人民币网关账号(存管)
    private String merchantAcctIdNotHat;// 易网聚鲜在99bill注册的人民币网关账号（自营）
    private String terminalId1;// 易网聚鲜在99bill的第一个终端号(存管)
    private String terminalIdNotHat1;// 易网聚鲜在99bill的第一个终端号（自营）
    private String merchantCertPath;// 99bill商户私钥路径
    private String merchantCertPss;// 99bill商户私钥密码
    private String pciQueryUrl;// pci卡信息查询地址
    private String pciDeleteUrl;// pci解绑地址
    private String tokenUrl;// 卡信息验证-获取动态码
    private String indAuthUrl;// 卡信息验证-不使用动态码
    private String indAuthVerifyUrl;// 卡信息验证-使用动态码
    private String cardQueryUrl;// 卡信息查询url
    private String quickPayCommonUrl;// 99bill一键快捷支付（普通版）请求地址
    private String quickPayCommonTr3Url;// 99bill一键快捷支付（普通版）tr3回调地址
    private String payDynNumUrl;// 订单交易-获取动态码地址
    private String queryOrderUrl;// VPOS_CNP查询交易地址
    private String refundUrl;// 99bill退款请求地址
    private String merchantPubPath;// 商户公钥路径
    //    private String refundPss;// 99bill退款查询的密码(存管)
//    private String refundPssNotHat;// 99bill退款查询的密码（自营）
//    private String refundPassword;// 99bill退款的密码(存管)
//    private String refundPasswordNotHat;// 99bill退款的密码（自营）
//    private String frontEndUrl;// 前台回调地址
//    private String frontFailUrl;// 前台失败回调地址
//    private String bgUrl;// 商户支付、退款交易结果后台通知地址
//    private String refundWebServiceUrl;// 99bill退款WebService请求地址
//    private String singleUrl;// 99bill交易状态查询地址
//    private String orderAccountUrl;// 99bill对账单查询地址
//    private String orderAccUrl;// 商户对账单结果报文后台接收地址
//    private String withDrawBgUrl;// 商户提现结果后台通知地址    //add by zhaoqun


    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getMerIdNotHat() {
        return merIdNotHat;
    }

    public void setMerIdNotHat(String merIdNotHat) {
        this.merIdNotHat = merIdNotHat;
    }

    public String getMerchantAcctId() {
        return merchantAcctId;
    }

    public void setMerchantAcctId(String merchantAcctId) {
        this.merchantAcctId = merchantAcctId;
    }

    public String getMerchantAcctIdNotHat() {
        return merchantAcctIdNotHat;
    }

    public void setMerchantAcctIdNotHat(String merchantAcctIdNotHat) {
        this.merchantAcctIdNotHat = merchantAcctIdNotHat;
    }

    public String getTerminalId1() {
        return terminalId1;
    }

    public void setTerminalId1(String terminalId1) {
        this.terminalId1 = terminalId1;
    }

    public String getTerminalIdNotHat1() {
        return terminalIdNotHat1;
    }

    public void setTerminalIdNotHat1(String terminalIdNotHat1) {
        this.terminalIdNotHat1 = terminalIdNotHat1;
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

    public String getPciQueryUrl() {
        return pciQueryUrl;
    }

    public void setPciQueryUrl(String pciQueryUrl) {
        this.pciQueryUrl = pciQueryUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getIndAuthUrl() {
        return indAuthUrl;
    }

    public void setIndAuthUrl(String indAuthUrl) {
        this.indAuthUrl = indAuthUrl;
    }

    public String getIndAuthVerifyUrl() {
        return indAuthVerifyUrl;
    }

    public void setIndAuthVerifyUrl(String indAuthVerifyUrl) {
        this.indAuthVerifyUrl = indAuthVerifyUrl;
    }

    public String getCardQueryUrl() {
        return cardQueryUrl;
    }

    public void setCardQueryUrl(String cardQueryUrl) {
        this.cardQueryUrl = cardQueryUrl;
    }

    public String getPciDeleteUrl() {
        return pciDeleteUrl;
    }

    public void setPciDeleteUrl(String pciDeleteUrl) {
        this.pciDeleteUrl = pciDeleteUrl;
    }

    public String getQuickPayCommonUrl() {
        return quickPayCommonUrl;
    }

    public void setQuickPayCommonUrl(String quickPayCommonUrl) {
        this.quickPayCommonUrl = quickPayCommonUrl;
    }

    public String getQuickPayCommonTr3Url() {
        return quickPayCommonTr3Url;
    }

    public void setQuickPayCommonTr3Url(String quickPayCommonTr3Url) {
        this.quickPayCommonTr3Url = quickPayCommonTr3Url;
    }

    public String getPayDynNumUrl() {
        return payDynNumUrl;
    }

    public void setPayDynNumUrl(String payDynNumUrl) {
        this.payDynNumUrl = payDynNumUrl;
    }

    public String getQueryOrderUrl() {
        return queryOrderUrl;
    }

    public void setQueryOrderUrl(String queryOrderUrl) {
        this.queryOrderUrl = queryOrderUrl;
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
}
