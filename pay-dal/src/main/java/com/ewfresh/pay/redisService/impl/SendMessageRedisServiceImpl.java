package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.vo.OrderInfoVo;
import com.ewfresh.pay.model.vo.TradeInfoRedisVo;
import com.ewfresh.pay.redisDao.SendMessageRedisDao;
import com.ewfresh.pay.redisService.SendMessageRedisService;
import com.ewfresh.pay.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *      从Redis中获取待发送短信的订单信息
 * @author: JiuDongDong
 * date: 2018/5/9.
 */
@Component
public class SendMessageRedisServiceImpl implements SendMessageRedisService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    SendMessageRedisDao sendMessageRedisDao;


    /**
     * Description: 订单快捷支付时，验证码放入Redis，时效2分钟
     * @author: JiuDongDong
     * @param orderNo       订单号
     * @param validCode     验证码
     * @param expireTime    失效时间（单位为分）
     * date: 2018/9/28 18:45
     */
    @Override
    public void putValidCode2Redis(String orderNo, String validCode, Long expireTime) {
        sendMessageRedisDao.putValidCode2Redis(orderNo, validCode, expireTime);
    }

    /**
     * Description: 订单快捷支付时，放入Redis的验证码
     * @author: JiuDongDong
     * @param orderNo  订单号
     * @return java.lang.String 订单快捷支付时，放入Redis的验证码
     * date: 2018/9/28 18:39
     */
    public String getValidCodeFromRedis(String orderNo) {
        String validCode = sendMessageRedisDao.getValidCodeFromRedis(orderNo);
        return validCode;
    }

    /**
     * Description: 订单快捷支付时，订单信息放入Redis
     * @author: JiuDongDong
     * @param orderInfoVo 订单快捷支付时的订单信息
     * @param expireTime  过期时间
     * date: 2018/9/29 13:27
     */
    @Override
    public void putTradeInfo2Redis(OrderInfoVo orderInfoVo, Long expireTime) {
        sendMessageRedisDao.putTradeInfo2Redis(orderInfoVo, expireTime);
    }

    /**
     * Description: 银联---订单支付时，订单信息放入Redis
     * @author: JiuDongDong
     * @param params 订单支付时的订单信息
     * @param expireTime  过期时间
     * date: 2019/5/9 11:43
     */
    public void putUnionPayTradeInfo2Redis(Map<String, String> params, Long expireTime) {
        sendMessageRedisDao.putUnionPayTradeInfo2Redis(params, expireTime);
    }

    /**
     * Description: 从Redis中获取银联交易信息
     * @author: JiuDongDong
     * @param hashKey
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
     * date: 2019/5/9 17:27
     */
    @Override
    public List<Map<String, String>> getUnionPayTradeInfoFromRedis(String hashKey) {
        List<Map<String, String>> tradeInfoFromRedis = sendMessageRedisDao.getUnionPayTradeInfoFromRedis(hashKey);
        return tradeInfoFromRedis;
    }

    /**
     * Description: 从Redis中获取快钱交易信息
     * @author: JiuDongDong
     * @param hashKey hash的大key，eg："{quickPayPUR}"  订单快捷支付时，快钱未及时响应的订单支付信息
                                      "{quickPayRFD}"  订单快捷退货时，快钱未及时响应的订单支付信息
     * @return com.ewfresh.pay.model.vo.OrderInfoVo
     * date: 2018/9/29 13:58
     */
    @Override
    public List<OrderInfoVo> getTradeInfoFromRedis(String hashKey) {
        List<OrderInfoVo> orderInfoVoList = sendMessageRedisDao.getTradeInfoFromRedis(hashKey);
        return orderInfoVoList;
    }

    /**
     * Description: 删除订单快捷支付时放入Redis的订单信息
     * @author: JiuDongDong
     * @param orderInfoVo  订单快捷支付时的订单信息
     * date: 2018/9/29 13:28
     */
    @Override
    public void deleteTradeInfoFromRedis(OrderInfoVo orderInfoVo) {
        sendMessageRedisDao.deleteTradeInfoFromRedis(orderInfoVo);
    }

    /**
     * Description: 删除银联交易放入Redis的订单信息
     * @author: JiuDongDong
     * @param orderInfoVo
     * date: 2019/5/10 9:11
     */
    @Override
    public void deleteUnionPayTradeInfoFromRedis(Map<String, String> orderInfoVo) {
        sendMessageRedisDao.deleteUnionPayTradeInfoFromRedis(orderInfoVo);
    }

}
