package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.BankAccount;

import java.util.Date;

/**
 * Created by 王耀辉 on 2018/8/7.
 */
public class BankAccountVo extends BankAccount {

    private Short secondAcct;//是否二类账户(0否,1是)

    private String name;

    private Short isPerson;//是否个体(0否,1是)

    private Long uid;//用户ID

    private String wholeBankName;//加密的银行卡

    private String status;//块钱的银行卡审核状态
    @Override
    public Integer getId() {
        return super.getId();
    }

    @Override
    public void setId(Integer id) {
        super.setId(id);
    }

    @Override
    public Long getUserId() {
        return super.getUserId();
    }

    @Override
    public void setUserId(Long userId) {
        super.setUserId(userId);
    }

    @Override
    public String getBankName() {
        return super.getBankName();
    }

    @Override
    public void setBankName(String bankName) {
        super.setBankName(bankName);
    }

    @Override
    public String getBankCode() {
        return super.getBankCode();
    }

    @Override
    public void setBankCode(String bankCode) {
        super.setBankCode(bankCode);
    }

    @Override
    public String getBankAccName() {
        return super.getBankAccName();
    }

    @Override
    public void setBankAccName(String bankAccName) {
        super.setBankAccName(bankAccName);
    }

    @Override
    public String getCardType() {
        return super.getCardType();
    }

    @Override
    public void setCardType(String cardType) {
        super.setCardType(cardType);
    }

    @Override
    public String getCardCode() {
        return super.getCardCode();
    }

    @Override
    public void setCardCode(String cardCode) {
        super.setCardCode(cardCode);
    }

    @Override
    public String getMobilePhone() {
        return super.getMobilePhone();
    }

    @Override
    public void setMobilePhone(String mobilePhone) {
        super.setMobilePhone(mobilePhone);
    }

    @Override
    public Short getIsDef() {
        return super.getIsDef();
    }

    @Override
    public void setIsDef(Short isDef) {
        super.setIsDef(isDef);
    }

    @Override
    public String getProvince() {
        return super.getProvince();
    }

    @Override
    public void setProvince(String province) {
        super.setProvince(province);
    }

    @Override
    public String getCity() {
        return super.getCity();
    }

    @Override
    public void setCity(String city) {
        super.setCity(city);
    }

    @Override
    public String getArea() {
        return super.getArea();
    }

    @Override
    public void setArea(String area) {
        super.setArea(area);
    }

    @Override
    public String getStreet() {
        return super.getStreet();
    }

    @Override
    public void setStreet(String street) {
        super.setStreet(street);
    }

    @Override
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    @Override
    public void setCreateTime(Date createTime) {
        super.setCreateTime(createTime);
    }

    @Override
    public Date getModifyTime() {
        return super.getModifyTime();
    }

    @Override
    public void setModifyTime(Date modifyTime) {
        super.setModifyTime(modifyTime);
    }

    @Override
    public String getBankLogo() {
        return super.getBankLogo();
    }

    @Override
    public void setBankLogo(String bankLogo) {
        super.setBankLogo(bankLogo);
    }

    @Override
    public Short getIsShop() {
        return super.getIsShop();
    }

    @Override
    public void setIsShop(Short isShop) {
        super.setIsShop(isShop);
    }

    public Short getSecondAcct() {
        return secondAcct;
    }

    public void setSecondAcct(Short secondAcct) {
        this.secondAcct = secondAcct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getIsPerson() {
        return isPerson;
    }

    public void setIsPerson(Short isPerson) {
        this.isPerson = isPerson;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Override
    public Short getAccountType() {
        return super.getAccountType();
    }

    @Override
    public void setAccountType(Short accountType) {
        super.setAccountType(accountType);
    }

    @Override
    public String getDeal() {
        return super.getDeal();
    }

    @Override
    public void setDeal(String deal) {
        super.setDeal(deal);
    }

    public String getWholeBankName() {
        return wholeBankName;
    }

    public void setWholeBankName(String wholeBankName) {
        this.wholeBankName = wholeBankName;
    }

    @Override
    public Short getUserCardType() {
        return super.getUserCardType();
    }

    @Override
    public void setUserCardType(Short userCardType) {
        super.setUserCardType(userCardType);
    }

    @Override
    public String getUserCardCode() {
        return super.getUserCardCode();
    }

    @Override
    public void setUserCardCode(String userCardCode) {
        super.setUserCardCode(userCardCode);
    }

    @Override
    public Short getIsAble() {
        return super.getIsAble();
    }

    @Override
    public void setIsAble(Short isAble) {
        super.setIsAble(isAble);
    }

    @Override
    public Short getIsKuaiQian() {
        return super.getIsKuaiQian();
    }

    @Override
    public void setIsKuaiQian(Short isKuaiQian) {
        super.setIsKuaiQian(isKuaiQian);
    }

    @Override
    public String getPayToken() {
        return super.getPayToken();
    }

    @Override
    public void setPayToken(String payToken) {
        super.setPayToken(payToken);
    }

    @Override
    public String getExpiredDate() {
        return super.getExpiredDate();
    }

    @Override
    public void setExpiredDate(String expiredDate) {
        super.setExpiredDate(expiredDate);
    }

    @Override
    public String getCvv() {
        return super.getCvv();
    }

    @Override
    public void setCvv(String cvv) {
        super.setCvv(cvv);
    }

    @Override
    public Short getBankCardType() {
        return super.getBankCardType();
    }

    @Override
    public void setBankCardType(Short bankCardType) {
        super.setBankCardType(bankCardType);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Short getPhoneChangedExpired() {
        return super.getPhoneChangedExpired();
    }
}
