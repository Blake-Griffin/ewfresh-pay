package com.ewfresh.pay.redisDao;


import com.ewfresh.pay.model.vo.OrderInfoVo;
import com.ewfresh.pay.model.vo.TradeInfoRedisVo;

import java.util.List;
import java.util.Map;

/**
 * description: 快钱快捷支付信息临时放入Redis
 * @author: JiuDongDong
 * date: 2018/9/28.
 */
public interface SendMessageRedisDao {
    void putValidCode2Redis(String orderNo, String randomNum, Long expireTime);

    String getValidCodeFromRedis(String orderNo);

    void putTradeInfo2Redis(OrderInfoVo orderInfoVo, Long expireTime);

    List<OrderInfoVo> getTradeInfoFromRedis(String hashKey);
    // 银联---订单支付时，订单信息放入Redis
    void putUnionPayTradeInfo2Redis(Map<String, String> params, Long expireTime);
    // 银联---从Redis中获取银联交易信息
    List<Map<String, String>> getUnionPayTradeInfoFromRedis(String hashKey);

    void deleteTradeInfoFromRedis(OrderInfoVo orderInfoVo);
    // 银联---删除银联交易放入Redis的订单信息
    void deleteUnionPayTradeInfoFromRedis(Map<String, String> orderInfoVo);
}
