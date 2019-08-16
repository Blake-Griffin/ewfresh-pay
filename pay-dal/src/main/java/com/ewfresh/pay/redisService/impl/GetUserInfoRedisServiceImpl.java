package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.redisDao.GetUserInfoRedisDao;
import com.ewfresh.pay.redisService.GetUserInfoRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * description: 获取用户信息
 * @author: JiuDongDong
 * date: 2018/11/5.
 */
@Component
public class GetUserInfoRedisServiceImpl implements GetUserInfoRedisService {
    private Logger logger = LoggerFactory.getLogger(GetUserInfoRedisServiceImpl.class);
    @Autowired
    private GetUserInfoRedisDao getUserInfoRedisDao;

    /**
     * Description: 获取用户信息
     * @author: JiuDongDong
     * @param customerId 用户id
     * @return java.util.Map 用户信息
     * date: 2018/11/5 13:19
     */
    @Override
    public Map getUserInfo(String customerId) {
        Map userInfo = getUserInfoRedisDao.getUserInfo(customerId);
        return userInfo;
    }
}
