package com.ewfresh.pay.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.util.Constants;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class BarDealFlowOneVo extends BarDealFlow {


    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occTime;//交易时间

    private String explain;

    @Override
    public Date getOccTime() {
        return occTime;
    }

    @Override
    public void setOccTime(Date occTime) {
        this.occTime = occTime;
    }

    public String getExplain() {
        Short dealType = super.getDealType();
        Long orderId = super.getOrderId();
        if (dealType == null || orderId == null) {
            return null;
        }
        explain = null;
        if (dealType == Constants.DEAL_TYPE_ONE) {
            explain = "订单支付:";
            explain = explain + orderId;
        }
        if (dealType == Constants.DEAL_TYPE_TWO) {
            explain ="订单退款";
            explain = explain + orderId;
        }
        if (dealType == Constants.DEAL_TYPE_THREE) {
            explain = "还款";
        }
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }
}
