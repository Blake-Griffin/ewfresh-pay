package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.redisDao.OrderRedisDao;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * description:获取订单redis中的信息
 * @author: Wangyaohui
 * date:   2018/4/16
 *
 */
@Component
public class OrderRedisServiceImpl implements OrderRedisService{
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderRedisDao orderRedisDao;
    @Override
    public Map<String, String> getPayOrder(String orderId) {
        return orderRedisDao.getPayOrder(orderId);
    }

    /**
     * Description: 根据与第三方交互的订单号（加E加R）从Redis获取订单信息
     * @author: JiuDongDong
     * @param interactionId 与第三方交互的订单号（加E加R）
     * @return 订单信息
     * date: 2019/5/24 14:21
     */
    @Override
    public Map<String, String> getOrderInfoFromRedis(String interactionId) {
        Map<String, String> redisParam = getPayOrder(interactionId);
        if (MapUtils.isEmpty(redisParam)) {
            return null;
        }
        logger.info("the redis param is------>{}", JsonUtil.toJson(redisParam));
        return redisParam;
    }

    @Override
    public void delPayOrder(String orderId) {
        orderRedisDao.delPayOrder(orderId);
    }

    @Override
    public void modifyOrderStatusParam(Map<String, String> map) {
        orderRedisDao.modifyOrderStatusParam(map);
    }

    @Override
    public void supplementModifyDis(Map<String, String> map) {
        orderRedisDao.supplementModifyDis(map);
    }

    @Override
    public void shopBond(Map<String, String> map) {
        orderRedisDao.shopBond(map);
    }
}
