package com.ewfresh.pay.util;

/**
 * description:
 *
 * @author: louzifeng
 * date: 2018/9/14
 */
public enum ExportAccountFlowEnum {
    uid("用户编号"),uname("客户名称"), phone("注册手机号"), balance("当前余额");
    private String value;
    ExportAccountFlowEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
