package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.redisDao.GetShopInfoRedisDao;
import com.ewfresh.pay.redisService.GetShopInfoRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * description: 获取店铺信息
 * @author: JiuDongDong
 * date: 2019/6/25.
 */
@Service
public class GetShopInfoRedisServiceImpl implements GetShopInfoRedisService {
    @Autowired
    private GetShopInfoRedisDao getShopInfoDao;


    /**
     * Description: 获取店铺信息、分润信息
     * @author: JiuDongDong
     * @param shopId  店铺id
     * @return java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>
     * date: 2019/6/25 16:54
     */
    @Override
    public Map<String, Map<String, Object>> getShopInfo(String shopId) {
        return getShopInfoDao.getShopInfo(shopId);
    }
}
