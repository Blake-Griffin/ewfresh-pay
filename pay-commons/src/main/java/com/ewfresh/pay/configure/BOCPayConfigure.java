package com.ewfresh.pay.configure;

/**
 * description:
 *      BOC的配置
 * @author: JiuDongDong
 * date: 2018/4/10.
 */
public class BOCPayConfigure {
    private String merchantNo;//银行提供的商户号
    private String payType;//商户支付服务类型： 1：网上购物
    private String curCode;//订单币种，固定填001人民币
    private String orderNote;//订单说明
    private String orderUrl;//商户接收通知URL：客户支付完成后银行向商户发送支付结果，商户系统负责接收银行通知的URL
    private String orderPayUrl;//中行接收商户订单请求的网关
    private String queryOrderUrl;//商户查询订单请求的网关
    private String commonQueryOrderUrl;//商户发送查询订单请求(支持卡户信息判断)
    private String refundOrderUrl;//退款请求的网关
    private String getTicketUrl;//取票网关
    private String merchantUploadFileUrl;//上传文件网关
    private String merchantDownloadFileUrl;//上传文件网关
    private String uploadFileBaseURI;//上传文件基础URI
    private String downloadFileBaseURI;//上传文件基础URI
    private String keyStorePassword;//证书库密码
    private String keyPassword;//加密证书密码

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getCurCode() {
        return curCode;
    }

    public void setCurCode(String curCode) {
        this.curCode = curCode;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getOrderPayUrl() {
        return orderPayUrl;
    }

    public void setOrderPayUrl(String orderPayUrl) {
        this.orderPayUrl = orderPayUrl;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getQueryOrderUrl() {
        return queryOrderUrl;
    }

    public void setQueryOrderUrl(String queryOrderUrl) {
        this.queryOrderUrl = queryOrderUrl;
    }

    public String getRefundOrderUrl() {
        return refundOrderUrl;
    }

    public void setRefundOrderUrl(String refundOrderUrl) {
        this.refundOrderUrl = refundOrderUrl;
    }

    public String getCommonQueryOrderUrl() {
        return commonQueryOrderUrl;
    }

    public void setCommonQueryOrderUrl(String commonQueryOrderUrl) {
        this.commonQueryOrderUrl = commonQueryOrderUrl;
    }

    public String getGetTicketUrl() {
        return getTicketUrl;
    }

    public void setGetTicketUrl(String getTicketUrl) {
        this.getTicketUrl = getTicketUrl;
    }

    public String getMerchantUploadFileUrl() {
        return merchantUploadFileUrl;
    }

    public void setMerchantUploadFileUrl(String merchantUploadFileUrl) {
        this.merchantUploadFileUrl = merchantUploadFileUrl;
    }

    public String getMerchantDownloadFileUrl() {
        return merchantDownloadFileUrl;
    }

    public void setMerchantDownloadFileUrl(String merchantDownloadFileUrl) {
        this.merchantDownloadFileUrl = merchantDownloadFileUrl;
    }

    public String getUploadFileBaseURI() {
        return uploadFileBaseURI;
    }

    public void setUploadFileBaseURI(String uploadFileBaseURI) {
        this.uploadFileBaseURI = uploadFileBaseURI;
    }

    public String getDownloadFileBaseURI() {
        return downloadFileBaseURI;
    }

    public void setDownloadFileBaseURI(String downloadFileBaseURI) {
        this.downloadFileBaseURI = downloadFileBaseURI;
    }
}
