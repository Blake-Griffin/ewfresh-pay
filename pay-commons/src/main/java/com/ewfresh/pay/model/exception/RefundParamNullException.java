package com.ewfresh.pay.model.exception;

/**
 * description: 参数为空异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class RefundParamNullException extends Exception {
    public RefundParamNullException() {
        super();
    }

    public RefundParamNullException(String message) {
        super(message);
    }

    public RefundParamNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefundParamNullException(Throwable cause) {
        super(cause);
    }

    public RefundParamNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
