package com.ewfresh.pay.util;

/**
 * description:
 *
 * @author: louzifeng
 * date: 2018/11/13
 */
public enum ExportAccountFlowEnumTwo {
    accFlowId("交易编号"),occTime("交易时间"), amount("金额"), accType("交易类型"),busiNo("业务单号"), desp("备注"),;

    private String value;
    ExportAccountFlowEnumTwo(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
