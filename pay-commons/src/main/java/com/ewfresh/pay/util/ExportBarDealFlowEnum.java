package com.ewfresh.pay.util;

/**
  * @Author: LouZiFeng
  * @Date: 2019/3/20
  */
public enum ExportBarDealFlowEnum {
    id("交易编号"), occTime("交易时间"), amount("金额"), dealType("交易类型"), orderId("业务单号");

    private String value;
    ExportBarDealFlowEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
