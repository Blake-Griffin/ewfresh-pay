package com.ewfresh.pay.redisService;

/**
 * Created by 王耀辉 on 2018/5/8.
 */
public interface BankAccountRedisService {

    /**
     * Description:      添加校验绑定银行卡所需的验证码
     * @author wangyaohui
     * @param uid     用户ID
     * Date    2018/4/17
     */
    String getBankCode(Long uid);

    void addBankCodeByRedis(Long uid, String code);
}
