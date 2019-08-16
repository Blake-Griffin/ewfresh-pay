package com.ewfresh.pay.request;

import java.io.Serializable;

/**
 * @author: wangziyuan on 2018/3/30.
 */
public class AliPayRequest implements Serializable{
    private static final long serialVersionUID = 1L;
    private String out_trade_no;
    private String product_code;
    private String total_amount;
    private String subject;
    private String body;
    private AliPayExtend extend_params;

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public AliPayExtend getExtend_params() {
        return extend_params;
    }

    public void setExtend_params(AliPayExtend extend_params) {
        this.extend_params = extend_params;
    }
}
