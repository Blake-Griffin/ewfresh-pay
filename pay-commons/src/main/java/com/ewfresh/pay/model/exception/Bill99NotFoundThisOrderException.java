package com.ewfresh.pay.model.exception;

/**
 * description: 退款金额大于订单金额异常
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
public class Bill99NotFoundThisOrderException extends Exception {
    public Bill99NotFoundThisOrderException() {
        super();
    }

    public Bill99NotFoundThisOrderException(String message) {
        super(message);
    }

    public Bill99NotFoundThisOrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public Bill99NotFoundThisOrderException(Throwable cause) {
        super(cause);
    }

    public Bill99NotFoundThisOrderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
