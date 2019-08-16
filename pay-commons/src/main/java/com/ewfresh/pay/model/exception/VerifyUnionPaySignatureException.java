package com.ewfresh.pay.model.exception;

/**
 * description: 校验中国银联签名失败
 * @author: JiuDongDong
 * date: 2019/5/7.
 */
public class VerifyUnionPaySignatureException extends Exception {
    public VerifyUnionPaySignatureException() {
    }

    public VerifyUnionPaySignatureException(String message) {
        super(message);
    }

    public VerifyUnionPaySignatureException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerifyUnionPaySignatureException(Throwable cause) {
        super(cause);
    }

    public VerifyUnionPaySignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
