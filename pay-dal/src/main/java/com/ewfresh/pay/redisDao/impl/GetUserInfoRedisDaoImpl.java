package com.ewfresh.pay.redisDao.impl;

import com.ewfresh.pay.redisDao.GetUserInfoRedisDao;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * description: 获取用户信息
 * @author: JiuDongDong
 * date: 2018/11/5.
 */
@Component
public class GetUserInfoRedisDaoImpl implements GetUserInfoRedisDao {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private StringRedisTemplate redisTemplate;
    private String KEY = "{userCentre}{keepUserInfo}";


    /**
     * Description: 获取用户信息
     * @author: JiuDongDong
     * @param customerId 用户id
     * @return java.util.Map 用户信息
     * date: 2018/11/5 11:45
     */
    @Override
    public Map getUserInfo(String customerId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String s = valueOperations.get(KEY + customerId);
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        Map userInfo = JsonUtil.jsonToObj(s, Map.class);
        return userInfo;
    }
}
