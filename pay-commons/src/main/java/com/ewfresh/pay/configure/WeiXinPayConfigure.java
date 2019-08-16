package com.ewfresh.pay.configure;

/**
 * Created by huangYaBing on 2018-4-2
 */
public class WeiXinPayConfigure {
    /**
     * 微信公众号的appid
     */
    private String appId;
    /**
     * 微信公众号的SECRET
     */
    private static String secret;
    /**
     * 公众平台申请的商户的商户号
     */
    private String mchId;
    /**
     * 公众平台申请的商户的api秘钥
     */
    private String api;


    /**
     * 微信的统一下单接口：即获取prepayId的接口 （固定）
     */
    private String getPrepayUrl;

    /**
     * 退款接口：即获取prepayId的接口 （固定）
     */
    private String refundUrl;

    /**
     * 下载对账单
     */
    private String downLoadBill;

    /**
     * 查询退款接口
     */
    private String refundQuery;

    /**
     * 微信支付成功后回调的商户接口地址(自定义)
     */
    private String notifyUrl;

    /**
     * 微信支付成功后未收到回调，则主动调用查看订单信息接口
     */
    private String orderQuery;
    /**
     * 微信支付成功后回调的商户接口地址(自定义)
     */
    private String refundNotifyUrl;

    /**
     * 签名时的编码 必须是utf-8
     */
    private String signEncode;

    /**
     * 交易类型  扫码支付
     */
    private String tradeTypeSm;

    /**
     * 交易类型  APP支付
     */
    private String tradeTypeApp;

    /**
     * 设备号
     */
    private String deviceInfo;

    private String subMchId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getGetPrepayUrl() {
        return getPrepayUrl;
    }

    public void setGetPrepayUrl(String getPrepayUrl) {
        this.getPrepayUrl = getPrepayUrl;
    }

    public String getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
    }

    public String getDownLoadBill() {
        return downLoadBill;
    }

    public void setDownLoadBill(String downLoadBill) {
        this.downLoadBill = downLoadBill;
    }

    public String getRefundQuery() {
        return refundQuery;
    }

    public void setRefundQuery(String refundQuery) {
        this.refundQuery = refundQuery;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getOrderQuery() {
        return orderQuery;
    }

    public void setOrderQuery(String orderQuery) {
        this.orderQuery = orderQuery;
    }

    public String getRefundNotifyUrl() {
        return refundNotifyUrl;
    }

    public void setRefundNotifyUrl(String refundNotifyUrl) {
        this.refundNotifyUrl = refundNotifyUrl;
    }

    public String getSignEncode() {
        return signEncode;
    }

    public void setSignEncode(String signEncode) {
        this.signEncode = signEncode;
    }

    public String getTradeTypeSm() {
        return tradeTypeSm;
    }

    public void setTradeTypeSm(String tradeTypeSm) {
        this.tradeTypeSm = tradeTypeSm;
    }
    public String getTradeTypeApp() {
        return tradeTypeApp;
    }

    public void setTradeTypeApp(String tradeTypeApp) {
        this.tradeTypeApp = tradeTypeApp;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getSubMchId() {
        return subMchId;
    }

    public void setSubMchId(String subMchId) {
        this.subMchId = subMchId;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }
}
