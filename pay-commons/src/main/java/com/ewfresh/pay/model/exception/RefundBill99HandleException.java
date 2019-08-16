package com.ewfresh.pay.model.exception;

/**
 * description: bill99处理退款异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class RefundBill99HandleException extends Exception {
    public RefundBill99HandleException() {
        super();
    }

    public RefundBill99HandleException(String message) {
        super(message);
    }

    public RefundBill99HandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundBill99HandleException(Throwable cause) {
        super(cause);
    }

    public RefundBill99HandleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
