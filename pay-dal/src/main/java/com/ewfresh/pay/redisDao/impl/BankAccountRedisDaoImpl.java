package com.ewfresh.pay.redisDao.impl;

import com.ewfresh.pay.redisDao.BankAccountRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by 王耀辉 on 2018/5/8.
 */
@Component
public class BankAccountRedisDaoImpl implements BankAccountRedisDao {
    private final String BANKCODE_KEY = "{pay}-{bankCode}";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public String getBankCode(Long uid) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        Object o = hash.get(BANKCODE_KEY, uid.toString());
        if(o != null) {
            return o.toString();
        }else {
            return null;
        }
    }
    public void addBankCodeByRedis(Long uid, String code) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(BANKCODE_KEY, uid.toString(), code);
        redisTemplate.expire(BANKCODE_KEY, 15, TimeUnit.MINUTES);
    }

}
