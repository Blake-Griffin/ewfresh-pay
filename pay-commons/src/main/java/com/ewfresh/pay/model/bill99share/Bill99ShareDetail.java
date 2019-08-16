package com.ewfresh.pay.model.bill99share;

/**
 * description: 快钱人民币支付分账
 * @author: JiuDongDong
 * date: 2019/8/8.
 */
public class Bill99ShareDetail {
    /** 固定选择值：1。1 代表 Email 地址 */
    private String sharingContactType;
    /** 填写 Email */
    private String sharingContact;
    /** 填写应该分账的金额。单位为分。如 100 代表 1 元 */
    private String sharingApplyAmount;
    /** 本版本只支持 sharingFeeRate 固定为 0，即费用均由主收款方承担。 */
    private String sharingFeeRate;
    /** 分账备注说明。中文或英文字符串。 */
    private String sharingDesc;
    /** 异步半同步标志，可为空，固定值为sync。表示异步分账（sharingPayFlag=0）的时候，此分账明细支持同步分账 */
    private String SharingSyncFlag;

    public String getSharingContactType() {
        return sharingContactType;
    }

    public void setSharingContactType(String sharingContactType) {
        this.sharingContactType = sharingContactType;
    }

    public String getSharingContact() {
        return sharingContact;
    }

    public void setSharingContact(String sharingContact) {
        this.sharingContact = sharingContact;
    }

    public String getSharingApplyAmount() {
        return sharingApplyAmount;
    }

    public void setSharingApplyAmount(String sharingApplyAmount) {
        this.sharingApplyAmount = sharingApplyAmount;
    }

    public String getSharingFeeRate() {
        return sharingFeeRate;
    }

    public void setSharingFeeRate(String sharingFeeRate) {
        this.sharingFeeRate = sharingFeeRate;
    }

    public String getSharingDesc() {
        return sharingDesc;
    }

    public void setSharingDesc(String sharingDesc) {
        this.sharingDesc = sharingDesc;
    }

    public String getSharingSyncFlag() {
        return SharingSyncFlag;
    }

    public void setSharingSyncFlag(String sharingSyncFlag) {
        SharingSyncFlag = sharingSyncFlag;
    }
}
