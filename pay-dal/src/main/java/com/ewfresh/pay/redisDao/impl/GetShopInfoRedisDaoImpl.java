package com.ewfresh.pay.redisDao.impl;

import com.ewfresh.pay.redisDao.GetShopInfoRedisDao;
import com.ewfresh.pay.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.SHAREBENEFIT;
import static com.ewfresh.pay.util.Constants.SHOP;

/**
 * description: 获取店铺信息
 * @author: JiuDongDong
 * date: 2019/6/25.
 */
@Component
public class GetShopInfoRedisDaoImpl implements GetShopInfoRedisDao {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SHOP_SHAREBENEFIT_INTO = "{shop}{shopAndShareBenefitInfo}";


    /**
     * Description: 获取店铺信息、分润信息
     * @author: JiuDongDong
     * @param shopId  店铺id
     * @return java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>
     * date: 2019/6/25 16:49
     */
    @Override
    public Map<String, Map<String, Object>> getShopInfo(String shopId) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        // 获取店铺信息
        Object o1 = hashOperations.get(SHOP_SHAREBENEFIT_INTO + shopId, SHOP);
        String shopStr = (String) o1;
        Map shop = JsonUtil.jsonToObj(shopStr, Map.class);
        // 获取店铺分润信息
        Object o2 = hashOperations.get(SHOP_SHAREBENEFIT_INTO + shopId, SHAREBENEFIT);
        String shareBenefitStr = (String) o2;
        Map shareBenefit = JsonUtil.jsonToObj(shareBenefitStr, Map.class);

        Map<String, Map<String, Object>> map = new HashMap<>();
        map.put(SHOP, shop);
        map.put(SHAREBENEFIT, shareBenefit);
        logger.info("Shop info of shopId: {} is: {}", shopId, JsonUtil.toJson(map));
        return map;
    }
}
