package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description: 商户向快钱发送订单请求的参数封装
 * @author: JiuDongDong
 * date: 2018/7/31.
 */
public class Bill99MerchantSendOrderVo implements Serializable {

    //人民币网关账号，该账号为11位人民币网关商户编号+01,该参数必填。
    private String merchantAcctId;
    //编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。
    private String inputCharset;
    //支付页面从易网聚鲜获取支付参数后，往99bill的支付网关发送请求的地址。
    private String payUrl;
    //接收支付结果的页面地址，该参数一般置为空即可。
    private String pageUrl;
    //服务器接收支付结果的后台地址，该参数务必填写，不能为空。
    private String bgUrl;
    //网关版本，固定值：v2.0,该参数必填。
    private String version;
    //语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。
    private String language;
    //签名类型,该值为4，代表PKI加密方式,该参数必填。
    private String signType;
    //支付人姓名,可以为空。
    private String payerName;
    //支付人联系类型，1 代表电子邮件方式；2 代表手机联系方式。可以为空。
    private String payerContactType;
    //支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址；payerContactType为2，则填写手机号码。可以为空。
    private String payerContact;
    //指定付款人: 0代表不指定 1代表通过商户方 ID 指定付款人  2代表通过快钱账户指定付款人  3代表付款方在商户方的会员编号(当需要支持保存信息功能的快捷支付时，,需上送此项)  4代表企业网银的交通银行直连
    private String payerIdType;
    //付款人标识: 交行企业网银的付款方银行账号，当企业网银中的交通银行直连，此值不能为空。当需要支持保存信息功能的快捷支付时，此值不能为空，此参数需要传入付款方在商户方的会员编号
    private String payerId;
    //商户订单号，以下采用时间来定义订单号，商户可以根据自己订单号的定义规则来定义该值，不能为空。
    private String orderId;
    //订单金额，金额以“分”为单位，商户测试以1分测试即可，切勿以大金额测试。该参数必填。
    private String orderAmount;
    //订单提交时间，格式：yyyyMMddHHmmss，如：20071117020101，不能为空。
    private String orderTime;
    //商品名称，可以为空。
    private String productName;
    //商品数量，可以为空。
    private String productNum;
    //商品代码，可以为空。
    private String productId;
    //商品描述，可以为空。
    private String productDesc;
    //扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
    private String ext1;
    //扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
    private String ext2;
    //支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
    private String payType;
    //银行代码，如果payType为00，该值可以为空；如果payType为10，该值必须填写，具体请参考银行列表。
    private String bankId;
    //同一订单禁止重复提交标志，实物购物车填1，虚拟产品用0。1代表只能提交一次，0代表在支付不成功情况下可以再提交。可为空。
    private String redoFlag;
    //快钱合作伙伴的帐户号，即商户编号，可为空。
    private String pid;
    // 签名
    private String signMsg;
    // 交易超时时间
    private String orderTimeOut;

    public String getMerchantAcctId() {
        return merchantAcctId;
    }

    public void setMerchantAcctId(String merchantAcctId) {
        this.merchantAcctId = merchantAcctId;
    }

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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getRedoFlag() {
        return redoFlag;
    }

    public void setRedoFlag(String redoFlag) {
        this.redoFlag = redoFlag;
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

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getPayerIdType() {
        return payerIdType;
    }

    public void setPayerIdType(String payerIdType) {
        this.payerIdType = payerIdType;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getOrderTimeOut() {
        return orderTimeOut;
    }

    public void setOrderTimeOut(String orderTimeOut) {
        this.orderTimeOut = orderTimeOut;
    }
}
