package com.ewfresh.pay.model.exception;

/**
 * description: 中国银联处理退款失败异常
 * @author: JiuDongDong
 * date: 2019/5/7.
 */
public class UnionPayHandleRefundException extends Exception {
    public UnionPayHandleRefundException() {
    }

    public UnionPayHandleRefundException(String message) {
        super(message);
    }

    public UnionPayHandleRefundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnionPayHandleRefundException(Throwable cause) {
        super(cause);
    }

    public UnionPayHandleRefundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
