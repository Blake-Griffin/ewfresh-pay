package com.ewfresh.pay.util.unionpayh5pay;

import static com.ewfresh.pay.util.Constants.TRADE_TYPE_2;
import static com.ewfresh.pay.util.Constants.TRADE_TYPE_9;
import static com.ewfresh.pay.util.Constants.TRADE_TYPE_17;
import static com.ewfresh.pay.util.Constants.TRADE_TYPE_18;

/**
 * description: 获取交易类型
 * @author: JiuDongDong
 * date: 2019/7/1.
 */
public class GetTradeType {

    public synchronized static Short getTradeType(String refundType) {
        switch (refundType) {
            case "cancel" :
                return TRADE_TYPE_2;//取消订单("cancel")
            case "supplement" :
                return TRADE_TYPE_9;//配货补款退款("supplement")
            case "refunds" :
                return TRADE_TYPE_17;//退货退款("refunds")
            case "shutdown" :
                return TRADE_TYPE_18;//关闭订单("shutdown")
        }
        return null;
    }



}
