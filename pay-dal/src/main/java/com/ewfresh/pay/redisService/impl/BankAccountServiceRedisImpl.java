package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.redisDao.BankAccountRedisDao;
import com.ewfresh.pay.redisService.BankAccountRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 王耀辉 on 2018/5/8.
 */
@Component
public class BankAccountServiceRedisImpl implements BankAccountRedisService {
    @Autowired
    private BankAccountRedisDao bankAccountRedisDao;

    @Override
    public String getBankCode(Long uid) {
        return bankAccountRedisDao.getBankCode(uid);
    }

    @Override
    public void addBankCodeByRedis(Long uid, String code) {
        bankAccountRedisDao.addBankCodeByRedis(uid,code);
    }
}
