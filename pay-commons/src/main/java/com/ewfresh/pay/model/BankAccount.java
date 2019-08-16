package com.ewfresh.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
/**
 * description:用户银行账号
 *
 * @author: wangziyuan
 * @date 2018年4月11日16:16:06
 */
public class BankAccount implements Serializable {
    private Integer id;//ID

    private Long userId;//用户Id或商户id

    private String bankName;//银行名称

    private String bankCode;//银行编号

    private String bankAccName;//银行账户名

    private String cardType;//银行卡类型

    private String cardCode;//银行卡号

    private String mobilePhone;//手机号

    private Short isDef;//是否默认 0:否,1:是

    private String province;//省

    private String city;//市

    private String area;//区

    private String street;//街道
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;//创建时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;//修改时间

    private String bankLogo;//银行logo

    private Short isShop;//是否店铺快钱绑定(0否1是)

    private Short  accountType;//商户银行绑定(0出入金，1白名单1,2白名单2,3白名单3)

    private String deal;

    private Short userCardType;// 0 身份证类型、1 护照类型、2 军官证、3 士兵证、4 港澳台通行证、5 临时身份证、6 户口本、7 其他类型证件、9 警官证、12 外国人居留证、15 回乡证、16 企业营业执照、17 法人代码证、18 台胞证

    private String userCardCode;// 证件号码

    private Short isAble;// 是否启用：0启用，1未启用

    private Short isKuaiQian;// 是否快钱渠道：0否  1是

    private String payToken;// 签约协议号

    private String expiredDate;// 卡效期

    private String cvv;// 安全校验值

    private Short phoneChangedExpired;// 手机号变更失效：0否  1是

    private Short bankCardType;//银行卡类型(0为个体户,1为对公账号)

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode == null ? null : bankCode.trim();
    }

    public String getBankAccName() {
        return bankAccName;
    }

    public void setBankAccName(String bankAccName) {
        this.bankAccName = bankAccName == null ? null : bankAccName.trim();
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType == null ? null : cardType.trim();
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode == null ? null : cardCode.trim();
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone == null ? null : mobilePhone.trim();
    }

    public Short getIsDef() {
        return isDef;
    }

    public void setIsDef(Short isDef) {
        this.isDef = isDef;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street == null ? null : street.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getBankLogo() {
        return bankLogo;
    }

    public void setBankLogo(String bankLogo) {
        this.bankLogo = bankLogo;
    }

    public Short getIsShop() {
        return isShop;
    }

    public void setIsShop(Short isShop) {
        this.isShop = isShop;
    }

    public Short getAccountType() {
        return accountType;
    }

    public void setAccountType(Short accountType) {
        this.accountType = accountType;
    }

    public String getDeal() {
        return deal;
    }

    public void setDeal(String deal) {
        this.deal = deal;
    }

    public Short getUserCardType() {
        return userCardType;
    }

    public void setUserCardType(Short userCardType) {
        this.userCardType = userCardType;
    }

    public String getUserCardCode() {
        return userCardCode;
    }

    public void setUserCardCode(String userCardCode) {
        this.userCardCode = userCardCode;
    }

    public Short getIsAble() {
        return isAble;
    }

    public void setIsAble(Short isAble) {
        this.isAble = isAble;
    }

    public Short getIsKuaiQian() {
        return isKuaiQian;
    }

    public void setIsKuaiQian(Short isKuaiQian) {
        this.isKuaiQian = isKuaiQian;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Short getBankCardType() {
        return bankCardType;
    }

    public void setBankCardType(Short bankCardType) {
        this.bankCardType = bankCardType;
    }

    public Short getPhoneChangedExpired() {
        return phoneChangedExpired;
    }

    public void setPhoneChangedExpired(Short phoneChangedExpired) {
        this.phoneChangedExpired = phoneChangedExpired;
    }
}