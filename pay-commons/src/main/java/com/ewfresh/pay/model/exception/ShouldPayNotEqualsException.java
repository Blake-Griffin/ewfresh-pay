package com.ewfresh.pay.model.exception;

/**
 * description: 支付金额不匹配异常（Redis和web）
 * @author: JiuDongDong
 * date: 2019/03/21.
 */
public class ShouldPayNotEqualsException extends Exception {
    public ShouldPayNotEqualsException() {
    }

    public ShouldPayNotEqualsException(String message) {
        super(message);
    }

    public ShouldPayNotEqualsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShouldPayNotEqualsException(Throwable cause) {
        super(cause);
    }

    public ShouldPayNotEqualsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
