package com.ewfresh.pay.model.exception;

/**
 * description: md5失敗异常
 * @author: JiuDongDong
 * date: 2018/8/31.
 */
public class Md5HexFailedException extends Exception {
    public Md5HexFailedException() {
        super();
    }

    public Md5HexFailedException(String message) {
        super(message);
    }

    public Md5HexFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public Md5HexFailedException(Throwable cause) {
        super(cause);
    }

    public Md5HexFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
