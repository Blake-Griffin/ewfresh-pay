/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/4/16.
 **/
 
package com.ewfresh.pay.util;
/**
  * @descrption TODO
 * @author gaoyongqiqng
 * @create 2019-04-16
 * @Email 1005267839@qq.com
  **/
public enum ExportBarDealFlowEnumOne {
    uname("客户名称"), amount("使用额度"),usedLimit("当前使用额度"), explainInfo("说明信息"), occTime("时间");

    private String value;
    ExportBarDealFlowEnumOne(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
