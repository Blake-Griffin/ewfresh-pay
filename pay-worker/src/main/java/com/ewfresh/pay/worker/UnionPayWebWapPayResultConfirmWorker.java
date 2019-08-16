package com.ewfresh.pay.worker;

import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionpayB2CWebWapManager;
import com.ewfresh.pay.model.vo.UidToken;
import com.ewfresh.pay.redisService.SendMessageRedisService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * Description: UnionPayWebWap退款是否通过确认
 * @author: JiuDongDong
 * date: 2019/5/9 17:07
 */
@Component
public class UnionPayWebWapPayResultConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(UnionPayWebWapPayResultConfirmWorker.class);
    @Autowired
    private SendMessageRedisService sendMessageRedisService;
    @Autowired
    private UnionpayB2CWebWapManager unionpayB2CWebWapManager;
    @Autowired
    private CommonsManager commonsManager;
    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Autowired
    private HttpDeal httpDeal;

    /**
     * Description: 银联支付是否成功确认
     * @author: JiuDongDong
     * date: 2019/5/10 9:35
     */
//    @Scheduled(cron = "12 0/2 * * * ? ") TODO
    @Transactional
    public void confirmPayResult() {
        // 首先获取token uid
        String token;
        token = httpDeal.get(userTokenUrl + "?uid=" + Constants.SYSTEM_ID, null);
        UidToken uidToken = JsonUtil.jsonToObj(token, UidToken.class);
        if (null == uidToken) {
            logger.error("Got null from http://urm.ewfresh.com/gen_token");
            return;
        }

        /* 1. 从Redis查询有没有待确认支付结果的订单 */
        List<Map<String, String>> unionPayTradeInfos = sendMessageRedisService.getUnionPayTradeInfoFromRedis(Constants.UNION_PAY_TRADE_PUR);
        /* 1.1 没有待确认支付结果的订单 */
        if (CollectionUtils.isEmpty(unionPayTradeInfos)) {
            logger.info("There is no pay trade info in Redis now");
            return;
        }

        /* 1.2 有待确认支付结果的订单，处理数据 */
        for (Map<String, String> orderInfoVo : unionPayTradeInfos) {
            String orderId = orderInfoVo.get(param_orderId);// 订单号
            String queryId = orderInfoVo.get(param_queryId);// 交易查询流水号，消费交易的流水号，供后续查询用
            String txnTime = orderInfoVo.get(param_txnTime);// 订单发送时间
            // 组织查询参数
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put(param_orderId, orderId);//订单号  TODO orderId 还是 queryId
            queryParams.put(param_txnTime, txnTime);//订单发送时间
            // 发起查询请求
            ResponseData responseData = new ResponseData();
            unionpayB2CWebWapManager.singleQuery(responseData, queryParams);
            Object entity = responseData.getEntity();
            // 查询结果为空，该订单本次查询结束，开始下一个订单查询
            if (null == entity) {
                logger.error("Single query pay result occurred exception once again, now put to worker! orderId = {}", orderId);
                continue;
            }
            // 查询到结果，处理
            Map<String, String> rspData = (Map<String, String>) entity;
            logger.info("Result of single query {} is: {}", orderId, JsonUtil.toJson(rspData));
            // 查询交易成功，处理被查询交易的应答码逻辑
            String respCode = rspData.get(param_respCode);// 查询交易的应答码
            logger.info("The result of this unionPay of order {} is: respCode = {}", orderId, respCode);
            String origRespCode = rspData.get(param_origRespCode);// 支付交易的应答码
            String origRespMsg = rspData.get(param_origRespMsg);// 支付交易的应答信息
            // 00/A6:交易成功   03/04/05:订单处理中，需要稍后发起交易状态查询     其他:交易失败
            logger.info("The result of this unionPay of order {} is: origRespCode = {}, origRespMsg = {}", orderId, origRespCode, origRespMsg);
            // 订单处理中，则该订单本次查询结束，开始下一个订单查询
            if (!RESPONSE_CODE_03.equals(origRespCode) && !RESPONSE_CODE_04.equals(origRespCode) && !RESPONSE_CODE_05.equals(origRespCode)) {
                logger.error("UnionPay is handling this pay trade, put it to redis to query ! orderId = " + orderId);
                continue;
            }
            // 交易失败，则该订单本次查询结束，开始下一个订单查询
            if (!RESPONSE_CODE_OK.equals(origRespCode) && !RESPONSE_CODE_A6.equals(origRespCode)) {
                logger.error("UnionPay handle this pay trade failed, put it to redis! orderId = " + orderId);
                continue;
            }

            /* 支付成功，支付流水持久化到本地 */
            // 获取数据
            String version = rspData.get(param_version);//版本号  R
            String signature = rspData.get(param_signature);//签名  M
            String signMethod = rspData.get(param_signMethod);//签名方法  M
            String txnType = rspData.get(param_txnType);//交易类型
            String txnSubType = rspData.get(param_txnSubType);//交易子类
            String bizType = rspData.get(param_bizType);//产品类型
            String accessType = rspData.get(param_accessType);//接入类型
            String acqInsCode = rspData.get(param_acqInsCode);//收单机构代码  C 接入类型为收单机构接入时需返回
            String merId = rspData.get(param_merId);//商户代码
            String txnAmt = rspData.get(param_txnAmt);//交易金额
            String currencyCode = rspData.get(param_currencyCode);//交易币种  M 默认为 156
            String reqReserved = rspData.get(param_reqReserved);//请求方保留域  R
            String reserved = rspData.get(param_reserved);//保留域  O
            String settleAmt = rspData.get(param_settleAmt);//清算金额
            String settleCurrencyCode = rspData.get(param_settleCurrencyCode);//清算币种
            String settleDate = rspData.get(param_settleDate);//清算日期
            String traceNo = rspData.get(param_traceNo);//系统跟踪号
            String traceTime = rspData.get(param_traceTime);//交易传输时间
            String exchangeDate = rspData.get(param_exchangeDate);//兑换日期，交易成功，交易币种和清算币种不一致的时候返回
            String exchangeRate = rspData.get(param_exchangeRate);//汇率，交易成功，交易币种和清算币种不一致的时候返回
            String accNo = rspData.get(param_accNo);//账号  C 根据商户配置返回
            String payCardType = rspData.get(param_payCardType);//支付卡类型  C 根据商户配置返回
            String payType = rspData.get(param_payType);//支付方式  C 根据商户配置返回
            String payCardNo = rspData.get(param_payCardNo);//支付卡标识  C 移动支付交易时，根据商户配置返回
            String payCardIssueName = rspData.get(param_payCardIssueName);//支付卡名称 C 移动支付交易时，根据商户配置返回
            String bindId = rspData.get(param_bindId);//绑定标识号 R 绑定支付时，根据商户配置返回
            String signPubKeyCert = rspData.get(param_signPubKeyCert);//签名公钥证书 C 使用 RSA 签名方式时必选，此域填写银联签名公钥证书。

            // 封装持久化数据
            Map<String, Object> param = new HashMap<>();
            param.put(CHANNEL_FLOW_ID, queryId);//TODO 支付渠道流水号，取queryId还是traceNo
            param.put(PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(txnAmt));//付款方支付金额
            param.put(RECEIVER_USER_ID, merId);//收款人ID（商户号）
            param.put(SUCCESS_TIME, txnTime);//商户订单提交时间
            param.put(IS_REFUND, IS_REFUND_NO + "");//是否退款 0:否,1是
            param.put(RETURN_INFO, null);//返回信息
            param.put(DESP, BUY_GOODS);//描述
            param.put(UID, UID_UNIONPAY);//操作人标识，中国银联
//        //渠道类型 07：互联网； 08：移动； 其他：银行编号
//        String bankIdRedis = redisParam.get(BILL99_BANK_ID);// 银行id
//        param.put(BOB_CHANNEL_TYPE, StringUtils.isBlank(bankIdRedis) ? bankId : bankIdRedis);
            param.put(PAYER_ID, INTEGER_ONE + "");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
            param.put(TRADE_TYPE, TRADE_TYPE_1);//交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
            param.put(INTERACTION_ID, orderId);//订单号
            param.put(PAY_CHANNEL, INTEGER_SIXTY_SEVEN + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 45快钱快捷 67中国银联WebWap 68中国银联QrCode
            param.put(TYPE_NAME, UNIONPAY);
            param.put(TYPE_CODE, INTEGER_SIXTY_SIX + "");
            param = CalMoneyByFate.calMoneyByFate(param);
            param.put(RECEIVER_FEE, FenYuanConvert.fen2YuanWithStringValue(settleAmt));//收款方手续费 todo 取清算金额吗？
            param.put(PLATINCOME, FenYuanConvert.fen2YuanWithStringValue(new BigDecimal(txnAmt).subtract(new BigDecimal(settleAmt)).toString()));//平台收入 TODO 用清算金额算的吗
            // 3.2 订单支付信息持久化到本地
            boolean ifSuccess = commonsManager.ifSuccess(param);
            logger.info("Confirm union pay trade success and serialize to merchant {} for orderId: {}", ifSuccess, orderId);

            if (!ifSuccess) {
                // 持久化失败
                logger.error("persist pay result to disk failed, orderId = " + orderId);
                continue;
            } else {
                // 持久化成功
                logger.info("delete trade info from redis, orderId = {}", orderId);
                sendMessageRedisService.deleteUnionPayTradeInfoFromRedis(orderInfoVo);
            }
        }
    }
}
