package com.ewfresh.pay.model.vo;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2018/7/25.
 */
public class WinXinTradeStateVo {

    private String tradeState;//是否成功

    private Long orderId;//订单id

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}
