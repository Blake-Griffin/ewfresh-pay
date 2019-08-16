package com.ewfresh.pay.util;

public enum  ExportAccountEnum {
    uid("用户编号"),uname("客户名称"), phone("注册手机号"), balance("账户余额");

    private String value;
    ExportAccountEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}


