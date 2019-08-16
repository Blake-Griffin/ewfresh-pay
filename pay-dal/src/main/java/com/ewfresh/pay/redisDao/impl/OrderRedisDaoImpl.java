package com.ewfresh.pay.redisDao.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.redisDao.OrderRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * description:
 * @author: Wangyaohui
 * date:   2018/4/16
 * 
 */
@Repository
public class OrderRedisDaoImpl implements OrderRedisDao{
    private static final String KEY_ORDER_PAY = "{order}-{sendRecord}-{orderToPay}";
    private static final String KEY_MODIFY_STATUS = "{order}-{modifyOrderStatus}";
    private static final String KEY_MODIFY_SUPPLEMENT = "{order}-{supplementModifyDis}";
    private static final String KEY_SHOP_BOND = "{pay}-{shopBond}";
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public Map<String, String> getPayOrder(String orderId) {
        Object o = redisTemplate.opsForValue().get(KEY_ORDER_PAY+orderId);
        Map<String, String> map = new HashMap<String,String>();
        if(o != null){
            map = ItvJsonUtil.jsonToObj(o.toString(), new TypeReference<Map<String, String>>() {
            });
        }
        return map;
    }

    @Override
    public void delPayOrder(String orderId) {
        redisTemplate.delete(KEY_ORDER_PAY+orderId);
    }

    @Override
    public void modifyOrderStatusParam(Map<String, String> map) {
        redisTemplate.opsForList().leftPush(KEY_MODIFY_STATUS,ItvJsonUtil.toJson(map));
    }

    @Override
    public void supplementModifyDis(Map<String, String> map) {
        redisTemplate.opsForList().leftPush(KEY_MODIFY_SUPPLEMENT,ItvJsonUtil.toJson(map));
    }

    @Override
    public void shopBond(Map<String, String> map) {
        redisTemplate.opsForList().leftPush(KEY_SHOP_BOND,ItvJsonUtil.toJson(map));
    }
}
