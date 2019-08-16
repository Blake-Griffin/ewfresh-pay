package com.ewfresh.pay.worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionPayQrCodeManager;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayH5RedisService;
import com.ewfresh.pay.util.CalMoneyByFate;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
 * description: UnionPayQRCode付款是否完成确认
 * @author: JiuDongDong
 * date: 2019/7/5.
 */
@Component
public class UnionPayQRCodePayResultConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(UnionPayQRCodePayResultConfirmWorker.class);
    @Autowired
    private UnionPayH5RedisService unionPayH5RedisService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private UnionPayQrCodeManager unionPayQrCodeManager;
    @Autowired
    private CommonsManager commonsManager;

    /**
     * Description: 付款是否成功确认
     * @author: JiuDongDong
     * date: 2019/7/5 9:07
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional
    public void confirmRefund() {
        /* 1. 从Redis查询有没有付款订单 */
        Map<Object, Object> paramMap = unionPayH5RedisService.getPayOrderInfo(PAY_UNIONPAYQRCODE_PAY_INFO);
        /* 1.1 没有付款订单 */
        if (MapUtils.isEmpty(paramMap)) {
            //logger.info("There is no QRCode pay order now");
            return;
        }
        /* 1.2 有付款订单，处理数据 */
        Set<Object> objects = paramMap.keySet();// 获取到所有订单id
        logger.info("All UnionPay QRCode orders are: {}", JsonUtil.toJson(objects));
        for (Object object : objects) {
            String orderId = (String) object;// 订单号
            logger.info("Current orderId = {}", orderId);
            String strOrderVo = (String) paramMap.get(orderId);
            Map map = JsonUtil.jsonToObj(strOrderVo, Map.class);
            String mid = map.get(MID).toString();
            String tid = map.get(TID).toString();
            String billDate = map.get(BILL_DATE).toString();

            // 获取Redis中缓存的支付信息
            Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(orderId);
            if (MapUtils.isEmpty(redisParam)) {
                logger.error("The order has been canceled, because create order is more than 60 minutes ago.");
                // 删除Redis中这个订单
                unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYQRCODE_PAY_INFO, orderId);
                continue;
            }
            String payMode = redisParam.get(PAY_MODE);
            String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）

            /* 2 从UnionPayQRCode查询付款状态，并根据订单状态进行相应的业务处理 */
            JSONObject rspData;
            try {
                rspData = getPayResultFromUnionPayQRCode(billDate, orderId, H5_TRADE_TYPE_3, null, mid, tid);
                logger.info("Pay result from UnionPayQRCode is: {} for orderId: {}", rspData, orderId);
            } catch (ParseException e) {
                logger.error("Get order status from UnionPayQRCode failed!!! ", e);
                continue;
            }
            if (null == rspData) {
                logger.warn("The customer has not paid, wait a moment! orderId = {}", orderId);
                continue;
            }

            // 获取订单支付响应信息
            String totalAmount = rspData.get(TOTAL_AMOUNT).toString();//订单金额，单位分
            String payTime = rspData.get(CREATE_TIME).toString();//支付时间，格式yyyy-MM-dd HH:mm:ss
            String errCode = rspData.get(ERR_CODE).toString();//错误码
            Object o = rspData.get(BILL_PAYMENT);//订单支付信息
            if (null == o) {
                logger.info("The order has not been paid, orderId = {}", orderId);
                continue;
            }
            String billPaymentStr = o.toString();
            String status = null;
            String paySeqId = null;
            if (StringUtils.isNotBlank(billPaymentStr)) {
                JSONObject billPayment = JSON.parseObject(billPaymentStr);
                status = billPayment.get(STATUS).toString();
                if (null == status || StringUtils.isBlank(status)) {
                    logger.warn("status is null! orderId = {}", orderId);
                    continue;
                }
                paySeqId = billPayment.get(PAY_SEQ_ID).toString();//系统交易流水号
            }

            // 确认是否支付成功
            if (!errCode.equals(SUCCESS)) {
                logger.error("Single query pay result failed, continue! orderId = " + orderId);
                continue;
            }
            if (STATUS_WAIT_BUYER_PAY.equals(status)) {
                logger.warn("The customer has not paid, wait a moment! orderId = {}", orderId);
                continue;
            }
            if (!STATUS_TRADE_SUCCESS.equals(status)) {
                logger.warn("The customer has not paid, wait a moment! orderId = {}", orderId);
                continue;
            }

            /* 支付成功，支付流水持久化到本地 */
            // 封装持久化数据
            Map<String, Object> param = new HashMap<>();
            param.put(CHANNEL_FLOW_ID, orderId);//支付渠道流水号（是银联生成的系统交易流水号）
            param.put(PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(totalAmount));//付款方支付金额
            param.put(PAYER_TYPE, STR_ZERO);//付款账号类型(1个人,2店铺)
            param.put(RECEIVER_TYPE, STR_ONE);//收款账号类型(1个人,2店铺)
            param.put(RECEIVER_USER_ID, shopIdRedis);//收款人ID
            param.put(SUCCESS_TIME, payTime);//订单时间，格式yyyy-MM-dd
            param.put(IS_REFUND, IS_REFUND_NO + "");//是否退款 0:否,1是
            param.put(RETURN_INFO, null);//返回信息
            param.put(DESP, BUY_GOODS);//描述
            param.put(UID, UID_UNIONPAY);//操作人标识，中国银联
            //渠道类型 07：互联网； 08：移动； 其他：银行编号
            //String bankIdRedis = redisParam.get(BILL99_BANK_ID);// 银行id
            //param.put(BOB_CHANNEL_TYPE, StringUtils.isBlank(bankIdRedis) ? bankId : bankIdRedis);
            param.put(PAYER_ID, INTEGER_ONE + "");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
            param.put(TRADE_TYPE, TRADE_TYPE_1);//交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
            param.put(INTERACTION_ID, orderId);//账单号
            param.put(PAY_CHANNEL, INTEGER_SIXTY_EIGHT + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 45快钱快捷 67中国银联WebWap 68中国银联QrCode 69中国银联H5Pay
            param.put(TYPE_NAME, UNIONPAY);
            param.put(TYPE_CODE, INTEGER_SIXTY_SIX + "");
            param = CalMoneyByFate.calMoneyByFate(param);
            //param.put(RECEIVER_FEE, FenYuanConvert.fen2YuanWithStringValue(settleAmt));//收款方手续费
            param.put(RECEIVER_FEE, STR_ZERO);//收款方手续费,实际不是0，commonsManager.ifSuccess处理
            //param.put(PLATINCOME, FenYuanConvert.fen2YuanWithStringValue(totalAmount));//平台收入,实际不是全额
            param.put(PLATINCOME, STR_ZERO);//平台收入,实际不是全额，commonsManager.ifSuccess处理
            param.put(MID, mid);
            param.put(TID, tid);
            // 3.2 订单支付信息持久化到本地
            boolean ifSuccess = commonsManager.ifSuccess(param);
            logger.info("Receive pay notify and serialize to merchant {} for orderId: {}", ifSuccess, orderId);
            // 删除支付时的缓存
            unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYQRCODE_PAY_INFO, orderId);
        }
    }


    /**
     * Description: 从UnionPayQRCode查询付款状态
     * @author: JiuDongDong
     * @param billDate 订单时间（支付回调里，银联C扫B给的账单时间），格式yyyy-MM-dd
     * @param orderId 账单号
     * @param tradeType 交易类型：1：支付交易  2：退款交易
     * @param refundSeq 退款流水号
     * @return java.lang.Integer  订单状态
     * date: 2019/7/5 9:58
     */
    private JSONObject getPayResultFromUnionPayQRCode(String billDate, String orderId, String tradeType, String refundSeq, String mid, String tid) throws ParseException {
        ResponseData responseData = new ResponseData();
        // 查询交易结果
        unionPayQrCodeManager.singleQuery(responseData, billDate, orderId, tradeType, refundSeq, mid, tid);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.warn("Result is null of single query, orderId = " + orderId);
            return null;// 订单查询出错
        }
        JSONObject rspData = (JSONObject) entity;
        logger.info("Result of single query pay order {} is: {}", orderId, rspData);
        return rspData;
    }

}
