package com.ewfresh.pay.redisService;

import java.util.Map;

/**
 * description: 获取店铺信息
 * @author: JiuDongDong
 * date: 2019/6/25.
 */
public interface GetShopInfoRedisService {
    Map<String, Map<String, Object>> getShopInfo(String shopId);
}
