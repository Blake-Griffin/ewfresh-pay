package com.ewfresh.pay.model.exception;

/**
 * description: 易网聚鲜向bill99申请退款成功，但bill99返回null异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class RefundBill99ResponseNullException extends Exception {
    public RefundBill99ResponseNullException() {
        super();
    }

    public RefundBill99ResponseNullException(String message) {
        super(message);
    }

    public RefundBill99ResponseNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundBill99ResponseNullException(Throwable cause) {
        super(cause);
    }

    public RefundBill99ResponseNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
