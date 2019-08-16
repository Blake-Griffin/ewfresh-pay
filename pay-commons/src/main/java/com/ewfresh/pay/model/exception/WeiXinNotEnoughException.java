package com.ewfresh.pay.model.exception;

/**
 * description: 微信余额不足异常
 * @author: zhaoqun
 * date: 2018/10/30.
 */
public class WeiXinNotEnoughException extends Exception {
    public WeiXinNotEnoughException() {
        super();
    }

    public WeiXinNotEnoughException(String message) {
        super(message);
    }

    public WeiXinNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeiXinNotEnoughException(Throwable cause) {
        super(cause);
    }

    public WeiXinNotEnoughException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
