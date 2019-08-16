package com.ewfresh.pay.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2018/9/5.
 */
public enum ExportOnlineRechargeFlowEnum {
    uname("客户名称"), payerPayAmount("金额"), channelName("支付渠道"), createTime("充值时间");

    private String value;
    ExportOnlineRechargeFlowEnum(String value){
        this.value=value;
    }
    public String getValue(){
        return value;
    }
}
