package com.ewfresh.pay.redisDao;

/**
 * Description:关于绑定银行卡的redis
 * @author Wangyaohui
 * Date 2018/4/16 0016
 */
public interface BankAccountRedisDao {
    /**
     * Description:      添加校验绑定银行卡所需的验证码
     * @author wangyaohui
     * @param uid     用户ID
     * Date    2018/4/17
     */
     String getBankCode(Long uid);

    void addBankCodeByRedis(Long uid, String code);
}
