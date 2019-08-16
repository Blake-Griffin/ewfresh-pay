package com.ewfresh.pay.redisService;


import com.ewfresh.pay.model.vo.OrderInfoVo;

import java.util.List;
import java.util.Map;

/**
 * description: 支付信息临时放入Redis
 * @author: JiuDongDong
 * date: 2018/9/28.
 */
public interface SendMessageRedisService {
    void putValidCode2Redis(String orderNo, String randomNum, Long expireTime);

    String getValidCodeFromRedis(String orderNo);
    // 快钱---订单快捷支付时，订单信息放入Redis
    void putTradeInfo2Redis(OrderInfoVo orderInfoVo, Long expireTime);
    // 银联---订单支付时，订单信息放入Redis
    void putUnionPayTradeInfo2Redis(Map<String, String> params, Long expireTime);
    // 银联---从Redis中获取银联交易信息
    List<Map<String, String>> getUnionPayTradeInfoFromRedis(String hashKey);

    List<OrderInfoVo> getTradeInfoFromRedis(String hashKey);

    void deleteTradeInfoFromRedis(OrderInfoVo orderInfoVo);

    void deleteUnionPayTradeInfoFromRedis(Map<String, String> orderInfoVo);
}
