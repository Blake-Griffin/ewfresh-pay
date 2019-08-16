package com.ewfresh.pay.model.exception;

/**
 * description: 退款金额大于订单金额异常
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
public class RefundAmountMoreThanOriException extends Exception {
    public RefundAmountMoreThanOriException() {
        super();
    }

    public RefundAmountMoreThanOriException(String message) {
        super(message);
    }

    public RefundAmountMoreThanOriException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundAmountMoreThanOriException(Throwable cause) {
        super(cause);
    }

    public RefundAmountMoreThanOriException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
