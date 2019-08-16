package com.ewfresh.pay.model.exception;

/**
 * description: 农业银行,交通银行,兴业银行,光大银行,中信银行,邮储银行,上海银行以上这几个银行是不支持当天申请退款的，隔天是可以申请的
 * @author: JiuDongDong
 * date: 2019/1/23.
 */
public class TheBankDoNotSupportRefundTheSameDay extends Exception {
    public TheBankDoNotSupportRefundTheSameDay() {
        super();
    }

    public TheBankDoNotSupportRefundTheSameDay(String message) {
        super(message);
    }

    public TheBankDoNotSupportRefundTheSameDay(String message, Throwable cause) {
        super(message, cause);
    }

    public TheBankDoNotSupportRefundTheSameDay(Throwable cause) {
        super(cause);
    }

    public TheBankDoNotSupportRefundTheSameDay(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
