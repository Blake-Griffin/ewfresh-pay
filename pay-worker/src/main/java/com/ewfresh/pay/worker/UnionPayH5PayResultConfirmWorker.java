package com.ewfresh.pay.worker;

import com.alibaba.fastjson.JSONObject;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionPayH5PayManager;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayH5RedisService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: UnionPayH5Pay付款是否完成确认
 * @author: JiuDongDong
 * date: 2019/7/4.
 */
@Component
public class UnionPayH5PayResultConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(UnionPayH5PayResultConfirmWorker.class);
    @Autowired
    private UnionPayH5RedisService unionPayH5RedisService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private UnionPayH5PayManager unionpayH5PayManager;
    @Autowired
    private CommonsManager commonsManager;

    /**
     * Description: 付款是否成功确认
     * @author: JiuDongDong
     * date: 2019/7/4 16:31
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional
    public void confirmRefund() {
        /* 1. 从Redis查询有没有付款订单 */
        Map<Object, Object> paramMap = unionPayH5RedisService.getPayOrderInfo(PAY_UNIONPAYH5_PAY_INFO);
        /* 1.1 没有付款订单 */
        if (MapUtils.isEmpty(paramMap)) {
            //logger.info("There is no H5Pay pay order now");
            return;
        }
        /* 1.2 有付款订单，处理数据 */
        Set<Object> objects = paramMap.keySet();// 获取到所有订单id
        logger.info("All UnionPay H5Pay orders are: {}", JsonUtil.toJson(objects));
        for (Object object : objects) {
            String orderId = (String) object;// 订单号
            logger.info("Current orderId = {}", orderId);
            String strOrderVo = (String) paramMap.get(orderId);
            Map map = JsonUtil.jsonToObj(strOrderVo, Map.class);
            String mid = map.get(MID).toString();
            String tid = map.get(TID).toString();

            // 获取Redis中缓存的支付信息
            Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(orderId);
            if (MapUtils.isEmpty(redisParam)) {
                logger.error("The order has been canceled, because create order is more than 60 minutes ago.");
                // 删除Redis中这个订单
                unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYH5_PAY_INFO, orderId);
                continue;
            }
            String payMode = redisParam.get(PAY_MODE);
            String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）


            /* 2 从UnionPayH5Pay查询付款状态，并根据订单状态进行相应的业务处理 */
            JSONObject rspData;
            try {
                rspData = getPayResultFromUnionPayH5Pay(orderId, mid, tid);
                logger.info("Pay result from UnionPayH5Pay is: {} for orderId: {}", rspData, orderId);
            } catch (ParseException e) {
                logger.error("Get order status from UnionPayH5Pay failed!!! ", e);
                continue;
            }
            if (null == rspData) {
                logger.error("Get pay result from unionPay occurred error! orderId = " + orderId);
                continue;
            }

            // 获取订单支付响应信息
            String totalAmount = rspData.get(TOTAL_AMOUNT).toString();//订单金额，单位分
            String payTime = rspData.get(PAY_TIME).toString();//支付时间，格式yyyy-MM-dd HH:mm:ss
            String seqId = rspData.get(SEQ_ID).toString();//系统交易流水号
            String errCode = rspData.get(ERR_CODE).toString();//错误码
            String errMsg = null == rspData.get(ERR_MSG) ? null : rspData.get(ERR_MSG).toString();//错误信息
            String status = rspData.get(STATUS).toString();//
            logger.info("orderId = {}, errCode = {}, errMsg = {}, status = {}", orderId, errCode, errMsg, status);

            // 确认是否支付成功
            if (!errCode.equals(SUCCESS)) {
                logger.error("Single query pay result failed, continue! orderId = " + orderId + ", rspData = " + rspData);
                continue;
            }
            if (STATUS_WAIT_BUYER_PAY.equals(status)) {
                logger.warn("The customer has not paid, wait a moment! orderId = {}", orderId);
                continue;
            }
            if (!STATUS_TRADE_SUCCESS.equals(status)) {
                logger.warn("The customer has not paid, wait a moment! orderId = {}", orderId + ", rspData = " + rspData);
                continue;
            }

            /* 支付成功，支付流水持久化到本地 */
            // 封装持久化数据
            Map<String, Object> param = new HashMap<>();
            param.put(MID, mid);
            param.put(TID, tid);
            param.put(CHANNEL_FLOW_ID, seqId);//支付渠道流水号(可用于B2B的手工接口退款)
            param.put(PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(totalAmount));//付款方支付金额
            param.put(PAYER_TYPE, STR_ZERO);//付款账号类型(1个人,2店铺)
            param.put(RECEIVER_TYPE, STR_ONE);//收款账号类型(1个人,2店铺)
            param.put(RECEIVER_USER_ID, shopIdRedis);//收款人ID
            param.put(SUCCESS_TIME, payTime);//支付时间，格式yyyy-MM-dd HH:mm:ss
            param.put(IS_REFUND, IS_REFUND_NO + "");//是否退款 0:否,1是
            param.put(RETURN_INFO, null);//返回信息
            param.put(DESP, BUY_GOODS);//描述
            param.put(UID, UID_UNIONPAY);//操作人标识，中国银联
            //渠道类型 07：互联网； 08：移动； 其他：银行编号
            //String bankIdRedis = redisParam.get(BILL99_BANK_ID);// 银行id
            //param.put(BOB_CHANNEL_TYPE, StringUtils.isBlank(bankIdRedis) ? bankId : bankIdRedis);
            param.put(PAYER_ID, INTEGER_ONE + "");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
            param.put(TRADE_TYPE, TRADE_TYPE_1);//交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
            param.put(INTERACTION_ID, orderId);//订单号
            if (payMode.contains(UNIONPAY_H5Pay_B2B)) {
                param.put(PAY_CHANNEL, INTEGER_SEVENTY + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 45快钱快捷 67中国银联WebWap 68中国银联QrCode 69中国银联H5Pay 70中国银联H5PayB2B
            } else {
                param.put(PAY_CHANNEL, INTEGER_SIXTY_NINE + "");
            }
            param.put(TYPE_NAME, UNIONPAY);
            param.put(TYPE_CODE, INTEGER_SIXTY_SIX + "");
            param = CalMoneyByFate.calMoneyByFate(param);
            //param.put(RECEIVER_FEE, FenYuanConvert.fen2YuanWithStringValue(settleAmt));//收款方手续费
            param.put(RECEIVER_FEE, STR_ZERO);//收款方手续费,实际不是0，commonsManager.ifSuccess处理
            //param.put(PLATINCOME, FenYuanConvert.fen2YuanWithStringValue(totalAmount));//平台收入
            param.put(PLATINCOME, STR_ZERO);//平台收入,实际不是全额，commonsManager.ifSuccess处理
            param.put(MID, mid);
            param.put(TID, tid);
            // 3.2 订单支付信息持久化到本地
            boolean ifSuccess = commonsManager.ifSuccess(param);
            logger.info("Receive pay notify and serialize to merchant {} for orderId: {}", ifSuccess, orderId);
            // 删除支付时的缓存
            unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYH5_PAY_INFO, orderId);
        }
    }


    /**
     * Description: 从UnionPayH5Pay查询付款状态
     * @author: JiuDongDong
     * @param orderId 订单号
     * @param mid 支付完成，但银联还没有回调的时候进行查询，需要携带mid、tid
     * @param tid 支付完成，但银联还没有回调的时候进行查询，需要携带mid、tid
     * @return java.lang.Integer  订单状态
     * date: 2019/7/4 16:01
     */
    private JSONObject getPayResultFromUnionPayH5Pay(String orderId, String mid, String tid) throws ParseException {
        ResponseData responseData = new ResponseData();
        // 查询交易结果
        unionpayH5PayManager.singleQuery(responseData, orderId, mid, tid);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.error("Result is null of single query, orderId = " + orderId);
            return null;// 订单查询出错
        }
        JSONObject rspData = (JSONObject) entity;
        //logger.info("Result of single query pay order {} is: {}", orderId, rspData);
        return rspData;
    }

}
