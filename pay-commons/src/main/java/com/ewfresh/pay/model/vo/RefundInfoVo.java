package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.RefundParam;

import java.util.Date;

/**
 * description: 退款信息
 * @author: JiuDongDong
 * date: 2018/6/30.
 */
public class RefundInfoVo {
    private String refundSeq;// 商户侧生成的退款流水号

    private String refundTime;// 退款申请时间

    private Date successTime;// 银联QrCode接收支付通知时，将billDate存在这个successTime字段了, 格式yyyy-MM-dd

    private RefundParam refundParam;// 退款请求所用的实体类

    private String isSelfPro;// 是否自营商品，0否1是

    private String refundType;//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")

    public String getRefundType() {
        return refundType;
    }

    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }

    public String getRefundSeq() {
        return refundSeq;
    }

    public void setRefundSeq(String refundSeq) {
        this.refundSeq = refundSeq;
    }

    public String getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(String refundTime) {
        this.refundTime = refundTime;
    }

    public RefundParam getRefundParam() {
        return refundParam;
    }

    public void setRefundParam(RefundParam refundParam) {
        this.refundParam = refundParam;
    }

    public String getIsSelfPro() {
        return isSelfPro;
    }

    public void setIsSelfPro(String isSelfPro) {
        this.isSelfPro = isSelfPro;
    }

    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }
}
