package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description: 商户向快钱发送订单请求的参数封装（分账）
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
public class Bill99MerchantSendOrderShareVo implements Serializable {
    /** 编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。*/
    private String inputCharset;
    /** 接收支付结果的页面地址，该参数一般置为空即可。*/
    private String pageUrl;
    /** 服务器接收支付结果的后台地址，该参数务必填写，不能为空。*/
    private String bgUrl;
    /** 网关版本，固定值：v2.0,该参数必填。*/
    private String version;
    /** 语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。*/
    private String language;
    /** 签名类型,该值为4，代表PKI加密方式,该参数必填。*/
    private String signType;
    /** 主收款方联系方式类型 */
    private String payeeContactType;
    /** 主收款方联系方式 */
    private String payeeContact;
    /** 支付人姓名,可以为空。*/
    private String payerName;
    /** 支付人联系类型，1 代表电子邮件方式 */
    private String payerContactType;
    /** 支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址 */
    private String payerContact;
    /** 付款人 IP */
    private String payerIP;
    /** 是否补款参数 */
    private String payTolerance;
    /** 商户订单号 */
    private String orderId;
    /** 商户订单金额 以“分”为单位*/
    private String orderAmount;
    /** 主收款方应收额 以“分”为单位 */
    private String payeeAmount;
    /** 订单提交时间，格式：yyyyMMddHHmmss，如：20071117020101，不能为空。*/
    private String orderTime;
    /** 商品名称，可以为空。*/
    private String productName;
    /** 商品数量，可以为空。*/
    private String productNum;
    /** 商品描述，可以为空。*/
    private String productDesc;
    /** 扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。*/
    private String ext1;
    /** 扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。*/
    private String ext2;
    /** 支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，12：快钱账户支付 14：代表显示企业网银支付（需要单独开通） 15: 信用卡无卡支付（需要单独开通）必填。*/
    private String payType;
    /** 银行代码，如果payType为00，该值可以为空；如果payType为10、14，该值必须填写，具体请参考银行列表。*/
    private String bankId;
    /** 此分账请求的合作方在快钱的用户编号。不可空*/
    private String pid;
    /** 交易超时时间 */
    private String orderTimeOut;
    /** 分账数据 */
    private String sharingData;
    /** 分账标志。1 代表支付成功立刻分账 0代表异步分账，即不立即把款项分配给相关账户 */
    private String sharingPayFlag;
    /** 签名 */
    private String signMsg;

    public String getInputCharset() {
        return inputCharset;
    }

    public void setInputCharset(String inputCharset) {
        this.inputCharset = inputCharset;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerContactType() {
        return payerContactType;
    }

    public void setPayerContactType(String payerContactType) {
        this.payerContactType = payerContactType;
    }

    public String getPayerContact() {
        return payerContact;
    }

    public void setPayerContact(String payerContact) {
        this.payerContact = payerContact;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductNum() {
        return productNum;
    }

    public void setProductNum(String productNum) {
        this.productNum = productNum;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public void setExt2(String ext2) {
        this.ext2 = ext2;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSignMsg() {
        return signMsg;
    }

    public void setSignMsg(String signMsg) {
        this.signMsg = signMsg;
    }

    public String getOrderTimeOut() {
        return orderTimeOut;
    }

    public void setOrderTimeOut(String orderTimeOut) {
        this.orderTimeOut = orderTimeOut;
    }

    public String getPayeeContactType() {
        return payeeContactType;
    }

    public void setPayeeContactType(String payeeContactType) {
        this.payeeContactType = payeeContactType;
    }

    public String getPayeeContact() {
        return payeeContact;
    }

    public void setPayeeContact(String payeeContact) {
        this.payeeContact = payeeContact;
    }

    public String getPayerIP() {
        return payerIP;
    }

    public void setPayerIP(String payerIP) {
        this.payerIP = payerIP;
    }

    public String getPayTolerance() {
        return payTolerance;
    }

    public void setPayTolerance(String payTolerance) {
        this.payTolerance = payTolerance;
    }

    public String getPayeeAmount() {
        return payeeAmount;
    }

    public void setPayeeAmount(String payeeAmount) {
        this.payeeAmount = payeeAmount;
    }

    public String getSharingData() {
        return sharingData;
    }

    public void setSharingData(String sharingData) {
        this.sharingData = sharingData;
    }

    public String getSharingPayFlag() {
        return sharingPayFlag;
    }

    public void setSharingPayFlag(String sharingPayFlag) {
        this.sharingPayFlag = sharingPayFlag;
    }
}
