package com.ewfresh.pay.redisService;

import java.util.Map;

/**
 * description: 获取用户信息
 * @author: JiuDongDong
 * date: 2018/11/5.
 */
public interface GetUserInfoRedisService {
    Map getUserInfo(String customerId);
}
