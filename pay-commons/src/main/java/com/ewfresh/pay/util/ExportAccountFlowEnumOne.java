package com.ewfresh.pay.util;

/**
 * description:
 *
 * @author: louzifeng
 * date: 2018/9/14
 */
public enum ExportAccountFlowEnumOne {
    uname("客户名称"), amount("金额"), balance("账户余额"), accType("支付渠道"), desp("说明"),occTime("时间");

    private String value;
    ExportAccountFlowEnumOne(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
