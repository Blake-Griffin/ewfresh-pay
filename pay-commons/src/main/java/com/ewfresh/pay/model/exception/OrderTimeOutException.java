package com.ewfresh.pay.model.exception;

/**
 * description: 订单支付超时异常（超过60分钟）
 * @author: JiuDongDong
 * date: 2019/7/6.
 */
public class OrderTimeOutException extends Exception {
    public OrderTimeOutException() {
    }

    public OrderTimeOutException(String message) {
        super(message);
    }

    public OrderTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderTimeOutException(Throwable cause) {
        super(cause);
    }

    public OrderTimeOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
