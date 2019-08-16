package com.ewfresh.pay.model.exception;

/**
 * description: 易网聚鲜向银联http错误异常
 * @author: JiuDongDong
 * date: 2019/5/6.
 */
public class HttpToUnionPayFailedException extends Exception {
    public HttpToUnionPayFailedException() {
    }

    public HttpToUnionPayFailedException(String message) {
        super(message);
    }

    public HttpToUnionPayFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpToUnionPayFailedException(Throwable cause) {
        super(cause);
    }

    public HttpToUnionPayFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
