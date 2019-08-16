package com.ewfresh.pay.model.exception;

/**
 * description: 余额不足异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class BalanceNotEnoughException extends Exception {
    public BalanceNotEnoughException() {
        super();
    }

    public BalanceNotEnoughException(String message) {
        super(message);
    }

    public BalanceNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }

    public BalanceNotEnoughException(Throwable cause) {
        super(cause);
    }

    public BalanceNotEnoughException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
