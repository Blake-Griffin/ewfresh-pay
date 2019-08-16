package com.ewfresh.pay.model.exception;

/**
 * description: 未找到支付流水异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class PayFlowFoundNullException extends Exception {
    public PayFlowFoundNullException() {
        super();
    }

    public PayFlowFoundNullException(String message) {
        super(message);
    }

    public PayFlowFoundNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayFlowFoundNullException(Throwable cause) {
        super(cause);
    }

    public PayFlowFoundNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
