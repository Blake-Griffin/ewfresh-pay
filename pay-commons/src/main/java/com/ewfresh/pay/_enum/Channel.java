package com.ewfresh.pay._enum;

/**
 * @author: <a href="mailto:liujing@sunkfa.com">LiuJing</a>
 */
public enum Channel {
    WxPay("1"),Alipay("2");
    private String value;

    Channel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
