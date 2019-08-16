package com.ewfresh.pay.util;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.redisService.OrderRedisService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * description: 根据与第三方交互的订单号（加E加R）从Redis获取订单信息
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
@Component
public class GetOrderInfoFromRedisUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private OrderRedisService orderRedisService;

    /**
     * Description: 根据与第三方交互的订单号（加E加R）从Redis获取订单信息
     * @author: JiuDongDong
     * @param interactionId 与第三方交互的订单号（加E加R）
     * @return 支付时放入Redis的订单信息
     * date: 2019/8/7 15:52
     */
    public synchronized Map<String, String> getOrderInfoFromRedis(String interactionId) {
        Map<String, String> redisParam = orderRedisService.getPayOrder(interactionId);
        if (MapUtils.isEmpty(redisParam)) {
            logger.warn("Redis param is null of interactionId: {}", interactionId);
            return null;
        }
        logger.info("the redis param is------>{}", ItvJsonUtil.toJson(redisParam));
        return redisParam;
    }
}
