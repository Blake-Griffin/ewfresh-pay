package com.ewfresh.pay.model.vo;

import java.io.Serializable;

/**
 * description:
 *      BOB交易明细
 * @author: JiuDongDong
 * date: 2018/4/23.
 */
public class BOBAccountVo implements Serializable {

    private static final long serialVersionUID = 4587511126267778331L;

    private String id;// 商户原支付订单号

    private String code;// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款

    private String amt;// 对账金额

    private String rs;//S：成功 N：待入账

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getRs() {
        return rs;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }
}
