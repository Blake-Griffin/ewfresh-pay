package com.ewfresh.pay.model.exception;

/**
 * description: 易网聚鲜向bill99申请退款http错误异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class RefundHttpToBill99FailedException extends Exception {
    public RefundHttpToBill99FailedException() {
        super();
    }

    public RefundHttpToBill99FailedException(String message) {
        super(message);
    }

    public RefundHttpToBill99FailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundHttpToBill99FailedException(Throwable cause) {
        super(cause);
    }

    public RefundHttpToBill99FailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
