package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionPayH5PayManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.OrderTimeOutException;
import com.ewfresh.pay.model.exception.VerifyUnionPaySignatureException;
import com.ewfresh.pay.model.unionpayh5suborder.SubOrder;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayH5RedisService;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.UnionPayService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayb2cwebwap.UnionPayLogUtil;
import com.ewfresh.pay.util.unionpayh5pay.HttpPostToUnionPay;
import com.ewfresh.pay.util.unionpayh5pay.IfShareBenefit;
import com.ewfresh.pay.util.unionpayh5pay.UnionpayH5B2BCbc3DesUtil;
import com.ewfresh.pay.util.unionpayh5pay.UnionpayH5B2BMD5Util;
import com.ewfresh.pay.util.unionpayqrcode.UnionPayQrCodeUtil;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_channelType;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_orderId;

/**
 * description: 银联H5Pay逻辑管理类
 * @author: JiuDongDong
 * date: 2019/5/16.
 */
@Component
public class UnionPayH5PayManagerImpl implements UnionPayH5PayManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat h5SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private UnionPayH5RedisService unionPayH5RedisService;
    @Autowired
    private UnionPayService unionPayService;
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private GetBenefitFateUtil getBenefitFateUtil;
    @Autowired
    private GetMidAndTid getMidAndTid;

    //读取资源配置参数
    @Value("${H5Pay.url}")
    private String APIurl;
    @Value("${H5Pay.self.B2B.mid}")
    private String selfB2BMid;//商户号：自营、B2B企业网银
    @Value("${H5Pay.self.B2B.tid}")
    private String selfB2BTid;//终端号：自营、B2B企业网银
    @Value("${H5Pay.shop.B2B.mid}")
    private String shopB2BMid;//商户号：店铺、B2B企业网银
    @Value("${H5Pay.shop.B2B.tid}")
    private String shopB2BTid;//终端号：店铺、B2B企业网银
    @Value("${H5Pay.self.B2C.borrow.mid}")
    private String selfB2CBorrowMid;//商户号：自营、H5线上借记卡、支付宝
    @Value("${H5Pay.self.B2C.borrow.tid}")
    private String selfB2CBorrowTid;//终端号：自营、H5线上借记卡、支付宝
    @Value("${H5Pay.shop.B2C.borrow.mid}")
    private String shopB2CBorrowMid;//商户号：店铺、H5线上借记卡、支付宝
    @Value("${H5Pay.shop.B2C.borrow.tid}")
    private String shopB2CBorrowTid;//终端号：店铺、H5线上借记卡、支付宝
    @Value("${H5Pay.self.B2C.loan.mid}")
    private String selfB2CLoanMid;//商户号：自营、H5线上贷记卡
    @Value("${H5Pay.self.B2C.loan.tid}")
    private String selfB2CLoanTid;//终端号：自营、H5线上贷记卡
    @Value("${H5Pay.shop.B2C.loan.mid}")
    private String shopB2CLoanMid;//商户号：店铺、H5线上贷记卡
    @Value("${H5Pay.shop.B2C.loan.tid}")
    private String shopB2CLoanTid;//终端号：店铺、H5线上贷记卡
    @Value("${freight.shop.mid}")
    private String freightShopMid;//商户号：运费
    @Value("${freight.shop.tid}")
    private String freightShopTid;//终端号：运费
    @Value("${H5Pay.instMid}")
    private String instMid;
    @Value("${H5Pay.msgSrc}")
    private String msgSrc;
    @Value("${H5Pay.msgSrcId}")
    private String msgSrcId;
    @Value("${H5Pay.key}")
    private String md5Key;
    @Value("${H5Pay.B2B.key}")
    private String B2BKey;//B2B通讯秘钥

    @Value("${H5Pay.msgType_refund}")
    private String msgType_refund;
    @Value("${H5Pay.msgType_secureCancel}")
    private String msgType_secureCancel;
    @Value("${H5Pay.msgType_secureComplete}")
    private String msgType_secureComplete;
    @Value("${H5Pay.msgType_close}")
    private String msgType_close;
    @Value("${H5Pay.msgType_query}")
    private String msgType_query;//订单查询（支付）
    @Value("${H5Pay.msgType_refundQuery}")
    private String msgType_refundQuery;//订单查询（退款）
    @Value("${H5Pay.notifyUrl}")
    private String notifyUrl;//支付结果通知地址
    @Value("${H5Pay.returnUrl}")
    private String returnUrl;//前台网页跳转地址
    @Value("${H5Pay.apiUrl_makeOrder}")
    private String apiUrl_makeOrder;//向银联发送支付请求的url

    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;

    /**
     * Description: 用户请求订单信息
     * 备注：在传分账标记的情况下，接口中goods 和 subOrders二者必传其一；若传goods则分账信息会按goods中每个商品的总额占支付总额减平台分账金额等比例生成；
     * 若传subOrders，则分账信息则严格按subOrders里的分账方案生成。
     * @author: JiuDongDong
     * date: 2019/5/16 13:15
     */
    @Override
    public void sendOrder(ResponseData responseData, Map<String, String> params) throws Exception {
        logger.info("It is now in UnionPayH5PayManagerImpl.sendOrder, the parameters are: [params = {}]",
                JsonUtil.toJson(params));
        /* 1.获取必要参数 */
        String orderId = params.get(param_orderId);
        String txnAmt = params.get(ORDER_AMOUNT);//交易金额，单位为元
        String channelType = params.get(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
        String msgType = params.get(MSG_TYPE);//msgType		消息类型: 1支付宝H5支付; 2微信H5支付; 3银联在线无卡; 4; 银联云闪付（走银联全渠道）
        //String sceneType = params.get(SCENE_TYPE);//业务应用类型：微信H5支付必填。用于苹app应用里值为IOS_SDK；用于安卓app应用里值为AND_SDK；用于手机网站值为IOS_WAP或AND_WAP
        //String merAppName = params.get(MER_APP_NAME);//微信H5支付必填。用于苹或安卓app 应用中，传分别 对应在 AppStore和安卓分发市场中的应用名（如：全民付）；用于手机网站，传对应的网站名（如：银联商务官网）
        //String merAppId = params.get(MER_APP_ID);//微信H5支付必填。用于苹果或安卓 app 应用中，苹果传 IOS 应用唯一标识(如： com.tencent.wzryIOS )。安卓传包名 (如： com.tencent.tmgp.sgame)。如果是用于手机网站 ，传首页 URL 地址 , (如： https://m.jd.com ) ，支付宝H5支付参数无效
        String name = params.get(NAME);//姓名，无卡支付指定付款人时必传，Base64编码
        String mobile = params.get(MOBILE);//手机号，无卡支付指定付款人时必传，Base64编码
        String certType = params.get(CERT_TYPE);//证件类型。注意：无卡支付目前仅支持身份证。无卡支付指定付款人时必传，证件类型：身份证：IDENTITY_CARD、护照：PASSPORT、军官证：OFFICER_CARD、士兵证：SOLDIER_CARD、户口本：HOKOU。
        String certNo = params.get(CERT_NO);//证件号。无卡支付指定付款人时必传，Base64编码
        String bankCardNo = params.get(BANK_CARD_NO);//卡号	String	max=19	false	无卡支付指定付款人时必传，Base64编码
        String client = params.get(CLIENT);//客户端类型：1pc; 2android; 3ios; 4wap
        String bizType = params.get(BIZ_TYPE);//网银支付类型: B2B:企业网银支付 B2C:个人网银支付
        String cardType = params.get(CARD_TYPE);//银行卡类型：借记卡：borrow   贷记卡：loan
//        String shopIdWeb = params.get(SHOP_ID);//前端传的店铺id（白条还款时会传）

        /* 1.2 校验订单支付金额是否正确 */
        Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(orderId);
        if (null == redisParam) {
            logger.error("The param in redis of this order " + orderId + " is expired, can not pay anymore");
            responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
            responseData.setMsg(ORDERTIMEOUT);// 本次支付已超时！
            return;
        }
        String shouldPayMoney = redisParam.get(SURPLUS);
        if (new BigDecimal(txnAmt).compareTo(new BigDecimal(shouldPayMoney)) != INTEGER_ZERO) {
            logger.error("Web should pay money not equals redis, shouldPayMoney from redis = " + shouldPayMoney +
                    ", web txnAmt = " + txnAmt);
            responseData.setCode(ResponseStatus.SHOULDPAYNOTEQUALS.getValue());
            responseData.setMsg(ResponseStatus.SHOULDPAYNOTEQUALS.name());
            return;
        }
        /* 由于订单一次全款支付、定金支付有60分钟时间限制(6o分钟取消订单)、尾款支付时虽然没有支付时间限制，但是支付信息只放入Redis60分钟，所以在支付时需校验时间是否超时 */
        String orderStatus = redisParam.get(ORDER_STATUS);// 1100为下单状态，1200为支付定金状态，这时需校验60分钟有效期
        // 计算拦截支付请求的时间
        String baseTime = "";
        // 1.下单状态时，选取"下单时间"推算超时拦截时间
        if (ORDER_WAIT_PAY.intValue() == Integer.valueOf(orderStatus)) {
            String createTimeStr = redisParam.get(CREATE_TIME);
            baseTime = createTimeStr;
        } else if (ORDER_PAID_EARNEST.intValue() == Integer.valueOf(orderStatus)) {
            // 2.订单尾款支付时，则以用户选中银行，点击下一步时放入Redis的“current时间”作为基准计算订单支付超时拦截时间
            String currentTime = redisParam.get(CURRENT_TIME);
            baseTime = currentTime;
        } else {
            baseTime = System.currentTimeMillis() + "";
        }
        logger.info("baseTime = {}", h5SimpleDateFormat.format(new Date(Long.valueOf(baseTime))));
        // 如有必要进行支付拦截
        Date futureMountSecondsStart = null;
        if (StringUtils.isNotBlank(baseTime)) {
            // 计算订单支付超时时间
            Date createTimeDate = new Date(Long.valueOf(baseTime));
            // 计算下单时间往后推1小时的时间
            Date futureMountHoursStart = DateUtil.getFutureMountHoursStart(createTimeDate, INTEGER_ONE);
            // 再减去30s的网络传输误差
            futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -INTEGER_THIRTY);
            // 如果截止时间在当前时间之前，则必需拦截此支付
            if (futureMountSecondsStart.getTime() < System.currentTimeMillis()) {
                logger.error("futureMountSecondsStart.getTime() < System.currentTimeMillis(), " +
                        "futureMountSecondsStart.getTime() = " + futureMountSecondsStart.getTime() + ", " +
                        "System.currentTimeMillis() = " + System.currentTimeMillis() + ", orderId = " + orderId);
                responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
                responseData.setMsg(ORDERTIMEOUT);// 本次支付已超时！
                return;
            }
        }
        String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）
        String isRecharge = redisParam.get(IS_RECHARGE);// 支付类型的判断（实物订单支付0，自营充值4，订单补款8，白条还款15，店铺保证金16）
        logger.info("The shopIdRedis is: {}, isRecharge = {}", shopIdRedis, isRecharge);

        String mid = null;//商户号
        String tid = null;//终端号
        Map<String, String> midAndTid = getMidAndTid(mid, tid, redisParam, bizType, cardType);
        mid = midAndTid.get(MID);
        tid = midAndTid.get(TID);
        logger.info("The mid = {}, tid = {}", mid, tid);

        //组织请求报文
        JSONObject json = new JSONObject();
        // json.put(MSG_ID, "");//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);//来源系统标识
        json.put(REQUEST_TIMESTAMP, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        json.put(MER_ORDER_ID, orderId);// 商户订单号
        //json.put(SRC_RESERVE, "");//	请求系统预留字段
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(INST_MID, instMid);//业务类型,固定值：H5DEFAULT
        //json.put(GOODS, goodsList);//商品详情，Json串数组，eg：[{"goodsCategory":"zhushi","goodsId":"1","goodsName":"niuroumian","price":2,"quantity":1,"weight":"0.5kg"},{"goodsCategory":"peicai","goodsId":"2","goodsName":"lujidan","price":2,"quantity":2,"weight":"0.01kg"}]
        //json.put(ATTACHED_DATA, "");//商户附加数据
        //json.put(ORDER_DESC, "");//账单描述
        //json.put(GOODS_TAG, "");//商品标记，用于优惠活动
        //json.put(ORIGINAL_AMOUNT, "");//订单原始金额，单位分，用于记录前端系统打折前的金额
        json.put(TOTAL_AMOUNT, FenYuanConvert.yuan2Fen(txnAmt).toString());//支付总金额，单位为分
        json.put(EXPIRE_TIME, h5SimpleDateFormat.format(futureMountSecondsStart));// 订单过期时间
        json.put(NOTIFY_URL, notifyUrl);//支付结果通知地址，支付成功后银商平台会将支付通知发至该url
        returnUrl = CLIENT_PC.equals(client) || CLIENT_WAP.equals(client)? returnUrl : null;//App端不支持支付成功后用户端会跳转至该url
        if (StringUtils.isNotBlank(returnUrl)) json.put(RETURN_URL, returnUrl);//支付成功后用户端会跳转至该url
        //json.put(SYSTEM_ID_UNION, "");//系统ID
        json.put(SIGN_TYPE, MD5);//签名算法，值为：MD5或 SHA256；若不上送默认为MD5
        //json.put(LIMIT_CREDIT_CARD, false);//是否需要限制信用卡支付。取值：true或false，默认false

        // 是否分账（目前运费流入主商户，所以如果买的是自营商品，则不用分账；如果买的是店铺商品，因为需要分润，所以得分账）
        boolean shareBenefit = IfShareBenefit.ifShareBenefit(shopIdRedis, isRecharge);//是否分润
        shareBenefit = TRADE_TYPE_16.toString().equals(isRecharge) ? false : shareBenefit;
        boolean divisionFlag = shareBenefit;//是否分账
        json.put(DIVISION_FLAG, divisionFlag);//分账标记。暂时只支持微信（WXPay.jsPay）、支付宝（trade.jsPay）支付
        if ((boolean) json.get(DIVISION_FLAG)) {
            logger.info("Start share benefit and division!");
//            /* 运费分账 */ 因为运费流入主商户，运费不用分了
//            String freight = redisParam.get(FREIGHT);//这个运费是使用银联支付的运费金额
//            SubOrder subOrderFreight = new SubOrder();
//            subOrderFreight.setMid(freightShopMid);// 898127210280001 这是运费专用商户号
//            subOrderFreight.setTotalAmount(FenYuanConvert.yuan2Fen(freight).toString());
//            subOrderList.add(subOrderFreight);

            /* 分润 */
            // 支付总金额 = 易网聚鲜分润 + 银联通道运费 + 店铺分润
            String unionPayEwfreshBenefit = redisParam.get(BANK_EWFRESH_BENEFIT);//使用第三方支付通道支付,ewfresh所得分润
            String unionPayShopBenefit = redisParam.get(BANK_SHOP_BENEFIT);//使用第三方支付通道支付,shop所得分润
            String freight = redisParam.get(FREIGHT);//使用第三方支付通道支付的运费
            freight = StringUtils.isBlank(freight) ? STR_ZERO : freight;

            // 店铺分润
            SubOrder subOrderShop = new SubOrder();
            String shopMid = getBenefitFateUtil.getMid(shopIdRedis, STR_ONE);
            //subOrderShop.setMid("898127210280002");// 这是测试环境虚拟的一个店铺的商户号
            subOrderShop.setMid(shopMid);
            subOrderShop.setTotalAmount(FenYuanConvert.yuan2Fen(unionPayShopBenefit).toString());
            List<SubOrder> subOrderList = new ArrayList<>();
            subOrderList.add(subOrderShop);

            // 易网聚鲜平台入账 = 易网聚鲜分润 + 银联通道运费 = 支付总金额 - 店铺分润
            BigDecimal platformAmount = new BigDecimal(unionPayEwfreshBenefit).add(new BigDecimal(freight));
            json.put(PLATFORM_AMOUNT, FenYuanConvert.yuan2Fen(platformAmount.toString()).toString());
            json.put(SUB_ORDERS, subOrderList);// 子商户分账信息，包括子商户号、分账金额
        }

        //json.put(NAME, name);//无卡支付指定付款人时必传，Base64编码
        //json.put(MOBILE, mobile);//无卡支付指定付款人时必传，Base64编码
        //json.put(CERT_TYPE, certType);//实名认证证件类型，证件类型，微信支持身份证、支付宝支持身份证：IDENTITY_CARD、护照：PASSPORT、军官证：OFFICER_CARD、士兵证：SOLDIER_CARD、户口本：HOKOU
        //json.put(CERT_NO, certNo);//实名认证证件号 False	Base64编码
        //json.put(BANK_CARD_NO, bankCardNo);//卡号。无卡支付指定付款人时必传，Base64编码

        if (BIZ_TYPE_B2C.equals(bizType)) {
            logger.info("It is a B2C order! ");
            json.put(MSG_TYPE, getMsgType(msgType));//消息类型: 支付宝H5支付：trade.h5Pay; 微信H5支付：WXPay.h5Pay; 银联在线无卡：qmf.h5Pay; 银联云闪付（走银联全渠道）：uac.order
            //WXPay.h5Pay相关参数的处理
            if (STR_TWO.equals(msgType)) {
                // 业务应用类型：微信H5支付必填。用于苹app应用里值为IOS_SDK；用于安卓app应用里值为AND_SDK；用于手机网站值为IOS_WAP或AND_WAP
                String sceneType = getSceneType(client);
                if (StringUtils.isNotBlank(sceneType)) {
                    json.put(SCENE_TYPE, sceneType);//业务应用类型：微信H5支付必填。用于苹app应用里值为IOS_SDK ；用于安卓app 应用里值为AND_SDK；用于手机网站，值为IOS_WAP 或AND_WAP
                }
                // 应用名称：微信H5支付必填。用于苹或安卓app 应用中，传分别 对应在 AppStore和安卓分发市场中的应用名（如：全民付）；用于手机网站，传对应的网站名（如：银联商务官网）
                String merAppName = getMerAppName(client);
                if (StringUtils.isNotBlank(sceneType)) {
                    json.put(MER_APP_NAME, merAppName);
                }
                // 应用标识：微信H5支付必填。
                String merAppId = getMerAppId(client);
                if (StringUtils.isNotBlank(merAppId)) {
                    json.put(MER_APP_ID, merAppId);
                    //json.put(MER_APP_ID, "http://www.chinaums.com");//银联测试环境的merAppId
                }
            }
            json.put(SECURE_TRANSACTION, false);//是否担保交易
        }
        if (BIZ_TYPE_B2B.equals(bizType)) {
            logger.info("It is a B2B order! ");
            json.put(MSG_TYPE, "uac.order");//消息类型: 支付宝H5支付：trade.h5Pay; 微信H5支付：WXPay.h5Pay; 银联在线无卡：qmf.h5Pay; 银联云闪付（走银联全渠道）：uac.order
            json.put(BIZ_TYPE, BIZ_TYPE_B2B);
            json.put(CHANNEL_TYPE, CHANNEL_TYPE_PC);//支付渠道选择：PC：PC端支付PHONE：移动端支付。默认为移动端
        }

        // 订单号放入Redis以备接收不到通知时自主查询
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ORDER_ID, orderId);
        paramMap.put(MID, mid);
        paramMap.put(TID, tid);
        unionPayH5RedisService.putPayOrderInfo(PAY_UNIONPAYH5_PAY_INFO, paramMap);

        // 生成url
        String orderRequestUrl = UnionPayQrCodeUtil.makeOrderRequest(json, md5Key, apiUrl_makeOrder);
        logger.info("Get orderRequestUrl of orderId {} is: {}", orderId, orderRequestUrl);
        responseData.setEntity(orderRequestUrl);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 单笔交易查询（订单交易时间必传，订单号、账单号二选一必传。订单号为与银联交易的加E加R的订单号）
     * @author: JiuDongDong
     * @param merOrderId 商户订单号，如：31942052717444700006502R
     * date: 2019/5/16 15:28
     */
    @Override
    public void singleQuery(ResponseData responseData, String merOrderId, String mid, String tid) {
        logger.info("It is now in UnionPayH5PayManagerImpl.singleQuery, the parameters are: " +
                "[merOrderId = {}, mid = {}, tid = {}]", merOrderId, mid, tid);
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(tid)) {
            logger.info("This a single query after unionPay notify! orderId = {}", merOrderId);
            // 获取mid、tid
            Map<String, String> midAndTid = getMidAndTid.getMidAndTidByInteractionId(merOrderId);
            if (MapUtils.isEmpty(midAndTid)) {
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
                return;
            }
            mid = midAndTid.get(MID);
            tid = midAndTid.get(TID);
        } else {
            logger.info("This is a single query before unionPay notify! orderId = {}", merOrderId);
        }

        //组织请求报文
        JSONObject json = new JSONObject();
        //json.put(MSG_ID, "");//消息ID，原样返回
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(MSG_TYPE, msgType_query);//消息类型
        json.put(MSG_SRC, msgSrc);//来源系统标识
        json.put(INST_MID, instMid);//业务类型:H5DEFAULT
        json.put(MER_ORDER_ID, merOrderId);//商户订单号
        json.put(REQUEST_TIMESTAMP, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));//报文请求时间
        json.put(SIGN_TYPE, MD5);//签名算法，值为：MD5或 SHA256；若不上送默认为MD5
        //json.put(SRC_RESERVE, "srcReserve");//请求系统预留字段

        // 查询请求参数转map
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        // 生成签名
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(md5Key, paramsMap));
        logger.info("paramsMap：{}", paramsMap);

        if (StringUtils.isBlank(APIurl)) {
            logger.error("APIurl is blank for H5Pay single query pay result");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        com.alibaba.fastjson.JSONObject respJsonObj = null;
        //调用银商平台查询接口
        try {
            respJsonObj = HttpPostToUnionPay.httpPostToUnionPay(APIurl, paramsMap);
        } catch (Exception e) {
            logger.error("Single query H5Pay pay result occurred error, orderId = " + merOrderId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        }

        // 验签
        Map<String, String> verifyParams = new HashMap<>();
        for (Map.Entry<String, Object> stringObjectEntry : respJsonObj.entrySet()) {
            String key = stringObjectEntry.getKey();
            Object value = stringObjectEntry.getValue();
            //logger.info("key = " + key);
            //logger.info("value = " + value);
            verifyParams.put(key, value.toString());
        }
        boolean checkSign = UnionPayQrCodeUtil.checkSign(md5Key, verifyParams);
        if (!checkSign) {
            logger.error("Verify signature of single query failed, merOrderId = " + merOrderId);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        //验签成功，打印应答码
        String errCode = respJsonObj.getString(ERR_CODE);
        String errMsg = respJsonObj.getString(ERR_MSG);
        UnionPayLogUtil.logTradeInfo(responseData, errCode, errMsg, merOrderId);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("UnionPay qrCode handle this trade failed, merOrderId = " + merOrderId);
            //return;
        }
        // 如果查询无误，将信息全部响应出去
        responseData.setEntity(respJsonObj);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 支付、退款交易的通知（退款也会通知到此url）
     * @author: JiuDongDong
     * @param respStr 通知的字符串
     * date: 2019/5/16 17:41
     */
    @Override
    public void receiveNotify(ResponseData responseData, String respStr) throws UnsupportedEncodingException, OrderTimeOutException {
        logger.info("It is now in UnionPayH5PayManagerImpl.receiveNotify, the params are: respStr = {}", respStr);
        //对支付、退款通知字串进行解码
        String trueColors;
        try {
            trueColors = URLDecoder.decode(respStr, "utf-8");
            trueColors = trueColors.replace("¬","&not");
        } catch (UnsupportedEncodingException e) {
            logger.error("URLDecoder.decode H5 pay notify failed, notify info = " + respStr, e);
            throw e;
        }
        logger.info("The notify info after decoded is: {}", trueColors);

        //将解码后的支付、退款通知字串 转成map
        String[] splitPreStrs = trueColors.split("&");
        String[] subStr;
        String notifySign = "";
        Map<String, String> payNotifyMap = new HashMap<>();
        for (String str : splitPreStrs) {
            subStr = str.split("=");
            if (subStr[0].equals(SIGN) || subStr[0] == SIGN) {
                logger.info("The sign of receiveNotify info = {}", subStr[0] + "：" + subStr[1]);
                notifySign = subStr[1];
                //组装待签字串时 去除原有待签字串中的sign
                continue;
            }
            payNotifyMap.put(subStr[0], subStr[1]);
        }
        //得到一个Map，订单信息仍然为json
        logger.info("Decode notify info to Map = {}", JsonUtil.toJson(payNotifyMap));

        /* 校验签名*/
        // 生成待签字串
        String preStrNew = UnionPayQrCodeUtil.buildSignString(payNotifyMap); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String preStrNew_md5Key = preStrNew + md5Key;
        logger.info("To sign String = {}", preStrNew_md5Key);
        // 生成签名
        String sign1 = DigestUtils.md5Hex(UnionPayQrCodeUtil.getContentBytes(preStrNew_md5Key)).toUpperCase();
        logger.info("sign1 = {}", sign1);
        // 比较签名是否一致
        boolean equals = StringUtils.equalsIgnoreCase(sign1, notifySign);
        if (!equals) {
            logger.error("Verify signature failed! To sign String = " + preStrNew_md5Key + ", sign1 = "
                    + sign1 + ", payNotifyMap = " + JsonUtil.toJson(payNotifyMap));
            throw new RuntimeException("Verify signature failed");
        }
        // 验签成功，处理应答信息
        //status的各种状态：NEW_ORDER:新订单	 UNKNOWN:不明确的交易状态	 
        // TRADE_CLOSED:在指定时间段内未支付时关闭的交易；在交易完成全额退款成功时关闭的交易；支付失败的交易。TRADE_CLOSED的交易不允许进行任何操作。
        // WAIT_BUYER_PAY:交易创建，等待买家付款。
        // TRADE_SUCCESS:支付成功	 
        // TRADE_REFUND:订单转入退货流程,退货可能是部分也可能是全部。
        String status = payNotifyMap.get(STATUS);
        String merOrderId = payNotifyMap.get(MER_ORDER_ID);//商户订单号
        logger.info("Verify signature of H5 pay or refund success! merOrderId = {}, status = {}", merOrderId, status);
        if (!STATUS_TRADE_SUCCESS.equals(status) && !STATUS_TRADE_REFUND.equals(status)) {
            logger.error("Stop! The status of merOrderId {} is: {}", merOrderId, status);
            logger.error("payNotifyMap = {}", JsonUtil.toJson(payNotifyMap));
            return;
        }
        // 支付、退款完成
        logger.info("This order has been paid or refund, merOrderId = {}", merOrderId);

        // 获取订单支付响应信息
        String mid = payNotifyMap.get(MID);//商户号
        String tid = payNotifyMap.get(TID);//终端号
        String instMid = payNotifyMap.get(INST_MID);//业务类型 H5DEFAULT
        String attachedData = payNotifyMap.get(ATTACHED_DATA);//附加数据
        String bankCardNo = payNotifyMap.get(BANK_CARD_NO);//支付银行信息
        String bankInfo = payNotifyMap.get(BANK_INFO);//银行信息
        String billFunds = payNotifyMap.get(BILL_FUNDS);//资金渠道
        String billFundsDesc = payNotifyMap.get(BILL_FUNDS_DESC);//资金渠道说明
        String buyerId = payNotifyMap.get(BUYER_ID);//卖家ID
        String buyerUsername = payNotifyMap.get(BUYER_USERNAME);//买家用户名
        String couponAmount = payNotifyMap.get(COUPON_AMOUNT);//网付计算的优惠金额
        String buyerPayAmount = payNotifyMap.get(BUYER_PAY_AMOUNT);//实付金额
        String totalAmount = payNotifyMap.get(TOTAL_AMOUNT);//订单金额，单位分
        String invoiceAmount = payNotifyMap.get(INVOICE_AMOUNT);//开票金额
        //String merOrderId = payNotifyMap.get(MER_ORDER_ID);//	商户订单号
        String payTime = payNotifyMap.get(PAY_TIME);//支付时间，格式yyyy-MM-dd HH:mm:ss
        String receiptAmount = payNotifyMap.get(RECEIPT_AMOUNT);//实收金额
        String refId = payNotifyMap.get(REF_ID);//支付银行卡参考号
        String refundAmount = payNotifyMap.get(REFUND_AMOUNT);//退款金额
        String refundDesc = payNotifyMap.get(REFUND_DESC);//退款说明
        String seqId = payNotifyMap.get(SEQ_ID);//系统交易流水号
        String settleDate = payNotifyMap.get(SETTLE_DATE);//结算日期，格式yyyy-MM-dd
        String subBuyerId = payNotifyMap.get(SUB_BUYER_ID);//卖家子ID
        String targetOrderId = payNotifyMap.get(TARGET_ORDER_ID);//渠道订单号
        String targetSys = payNotifyMap.get(TARGET_SYS);//支付渠道
        String secureStatus = payNotifyMap.get(SECURE_STATUS);//担保状态
        String completeAmount = payNotifyMap.get(COMPLETE_AMOUNT);//担保完成金额（分）
        String notifyId = payNotifyMap.get(NOTIFY_ID);//支付通知ID 通知唯一ID，重发通知的notifyId不变
        String sign = payNotifyMap.get(SIGN);//签名
        // 获取订单退款响应信息
        String refundTargetOrderId = payNotifyMap.get(REFUND_TARGET_ORDER_ID);//目标系统退货订单号
        String refundSettleDate = payNotifyMap.get("refundSettleDate");//文档没有该字段
        String refundOrderId = payNotifyMap.get(REFUND_ORDER_ID);//msgSrcId + 生成28位的退款流水号
        String refundPayTime = payNotifyMap.get(REFUND_PAY_TIME);//退款时间

        /* 订单支付的回调处理 */
        if (STATUS_TRADE_SUCCESS.equals(status)) {
            logger.info("This is a pay notify. merOrderId = {}, status = {}", merOrderId, status);
            // 获取Redis中缓存的支付信息
            Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(merOrderId);
            if (MapUtils.isEmpty(redisParam)) {
                logger.error("Order is time out! orderId = " + merOrderId);
                throw new OrderTimeOutException("Order is time out!");
            }
            String payMode = redisParam.get(PAY_MODE);
            String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）

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
            param.put(INTERACTION_ID, merOrderId);//订单号
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
            logger.info("H5Pay. Receive pay notify and serialize to merchant {} for orderId: {}", ifSuccess, merOrderId);
            // 3.3 删除支付时放在Redis的信息
            if (ifSuccess) {
                unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYH5_PAY_INFO, merOrderId);
                logger.info("H5Pay. Del pay order info from Redis ok. orderId = {}", merOrderId);
            }
        }

        /* 订单退款的回调处理 */
        if (STATUS_TRADE_REFUND.equals(status)) {
            logger.info("This is a refund notify. merOrderId = {}, status = {}", merOrderId, status);
            /* 从Redis获取退款信息---UnionPayH5PayRefundPolicy放入 */
            RefundInfoVo refundOrderInfoRedis = unionPayH5RedisService.getRefundOrderInfoFromRedis(refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO);
            logger.info("refundOrderInfoRedis = {}", JsonUtil.toJson(refundOrderInfoRedis));
            RefundParam refundParam = refundOrderInfoRedis.getRefundParam();
            String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            /* 从Redis获取退款信息---order项目退款时放入 */
            //Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            //logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            //String refundType = redisRefundInfo.get(REFUND_TYPE);//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
            String refundType = refundOrderInfoRedis.getRefundType();//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
            // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
            String finalOrderStatus = null;
            if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = ORDER_AGREE_RETURN.toString();
            if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = ORDER_SHUTDOWN.toString();
            //if (REFUND_TYPE_REFUNDS.equals(refundType)) finalOrderStatus = ORDER_AGREE_REFUND.toString();
            logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);

            // 获取订单状态
            Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
            logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
            // 订单系统的订单状态为0或null，说明订单状态查询有误
            if (Objects.equals(INTEGER_ZERO, orderStatusFromOrder) || null == orderStatusFromOrder) {
                logger.error("Get order status from order system occurred error for outRequestNo: " + outRequestNo);
            }
            // PayFlow数据更新
            PayFlow payFlow = new PayFlow();
            payFlow.setChannelFlowId(refundOrderId);
            // 银联退款成功，删除redis中的退款信息，支付流水表的status由失败改为成功，将订单状态改为:如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360
            payFlow.setStatus(STATUS_0);
            try {
                payFlow.setSuccessTime(h5SimpleDateFormat.parse(refundPayTime));
            } catch (ParseException e) {
                logger.error("PayFlow set successTime error! refundPayTime = " + refundPayTime, e);
                payFlow.setSuccessTime(new Date());
            }
            //payFlowService.updatePayFlow(payFlow);
            Map<String, String> params = new HashMap<>();
            params.put(ID, outRequestNo);
            params.put(ORDER_STATUS, finalOrderStatus);
            params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
            params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
            params.put(REFUND_TYPE, refundType);
            //updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
            //unionPayRedisService.deleteRefundOrderInfoInRedis(refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO);
            //unionPayRedisService.delReturnAmountParams(outRequestNo);
            logger.info("payFlow = {}", JsonUtil.toJson(payFlow));

            if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                logger.info("updateOrderStatusUrl = {}, params = {}", updateOrderStatusUrl, JsonUtil.toJson(params));
                unionPayService.updatePayFlowAndOrder(payFlow, updateOrderStatusUrl, params, refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO, outRequestNo);
            } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO, null);
                logger.info("Order system handle.");
            } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO, null);
                logger.info("Order system handle.");
            } else {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYH5PAY_REFUND_INFO, null);
            }
            logger.info("Receive refund notify and serialize to merchant ok for orderId: {}", merOrderId);
        }
    }

    /**
     * Description: 退款结果查询接口
     * @author: JiuDongDong
     * @param merOrderId 32位退货订单号
     * date: 2019/5/17 15:08
     */
    @Override
    public void refundQuery(ResponseData responseData, String merOrderId) {
        logger.info("It is now in UnionPayH5PayManagerImpl.refundQuery, the parameters are: " +
                "[merOrderId = {}]", merOrderId);
        // 获取mid、tid
        Map<String, String> midAndTid = getMidAndTid.getMidAndTidByRefundSequence(merOrderId);
        if (MapUtils.isEmpty(midAndTid)) {
            logger.info("midAndTid is empty.");
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return;
        }
        String mid = midAndTid.get(MID);
        String tid = midAndTid.get(TID);

        //组织请求报文
        JSONObject json = new JSONObject();
        // json.put(MSG_ID, "");//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);//来源系统标识
        json.put(MSG_TYPE, msgType_refundQuery);//消息类型：退款查询
        json.put(REQUEST_TIMESTAMP, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        // json.put(SRC_RESERVE, "srcReserve");//请求系统预留字段
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(INST_MID, instMid);//业务类型:H5DEFAULT
        json.put(MER_ORDER_ID, merOrderId);//退货订单号
        json.put(SIGN_TYPE, MD5);//签名算法，值为：MD5或 SHA256；若不上送默认为MD5

        // 查询请求参数转map
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        // 生成签名
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(md5Key, paramsMap));
        logger.info("paramsMap：{}", JsonUtil.toJson(paramsMap));

        if (StringUtils.isBlank(APIurl)) {
            logger.error("APIurl is blank for H5Pay single query refund order");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        // 向银联发送post请求
        com.alibaba.fastjson.JSONObject respJsonObj = null;
        try {
            respJsonObj = HttpPostToUnionPay.httpPostToUnionPay(APIurl, paramsMap);
        } catch (Exception e) {
            logger.error("Single query qrCode billNo occurred error, orderId = " + merOrderId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        }
        // 验签
        Map<String, String> verifyParams = new HashMap<>();
        for (Map.Entry<String, Object> stringObjectEntry : respJsonObj.entrySet()) {
            String key = stringObjectEntry.getKey();
            Object value = stringObjectEntry.getValue();
            //logger.info("key = " + key);
            //logger.info("value = " + value);
            verifyParams.put(key, value.toString());
        }
        boolean checkSign = UnionPayQrCodeUtil.checkSign(md5Key, verifyParams);
        if (!checkSign) {
            logger.error("Verify signature of single query refund order failed, merOrderId = " +
                    merOrderId + ", respJsonObj = " + respJsonObj);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        //验签成功，处理应答码
        String errCode = respJsonObj.getString(ERR_CODE);
        String errMsg = respJsonObj.getString(ERR_MSG);
        UnionPayLogUtil.logTradeInfo(responseData, errCode, errMsg, merOrderId);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("UnionPay qrCode handle this trade failed, respJsonObj = " + respJsonObj);
            return;
        }
        // 如果查询无误，将信息全部响应出去
        responseData.setEntity(respJsonObj);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: B2B退货交易的通知----这个不用了，已经使用线上联机退款
     * @author: JiuDongDong
     * date: 2019/6/12 15:51
     */
    @Override
    public void receiveB2BRefundNotify(ResponseData responseData, String respStr) throws Exception {
        logger.info("It is now in UnionPayH5PayManagerImpl.receiveB2BRefundNotify, the params are: respStr = {}", respStr);
        respStr = UnionpayH5B2BCbc3DesUtil.decrypt(respStr);
        logger.info("The result of refund of h5 b2b is: {}", respStr);

        //转换成json对象
        com.alibaba.fastjson.JSONObject respJsonObj = JSON.parseObject(respStr.toString(), Feature.OrderedField);
        String respCode = respJsonObj.getString(RESP_CODE);//应答码
        String serialNo = respJsonObj.getString(SERIAL_NO);//自定义的退款序列号，其实就是msgSrcId + 生成28位的退款流水号
        String appid = respJsonObj.getString(APP_ID);//H5B2B手工接口退款的应用id，银联数字王府井提供
        String signResult = respJsonObj.getString(SIGN);//H5B2B手工接口退款的签名
        String oReferNo = respJsonObj.getString("o_referno");//H5B2B手工接口退款的原交易参考号,与请求中o_referno一致。其实就是订单支付时响应的seqId
        String refundDate = respJsonObj.getString("refundDate")	;//退费完成时间
        String mernum = respJsonObj.getString("mernum")	;//商户号，与请求中mernum一致，就是mid

        // 验签
        String calSign = UnionpayH5B2BMD5Util.getMD5Str(respCode + serialNo + appid + mernum + B2BKey);
        logger.info("Result sign = {}", signResult);
        logger.info("Self cal sign = {}", calSign);
        boolean checkSign = calSign.equalsIgnoreCase(signResult);
        if (!checkSign) {
            logger.error("Verify signature for UnionPayH5PayB2BRefundPolicy response not pass!!! serialNo = " + serialNo);
            logger.error("The response data of refund serialNo: " + serialNo + " is: " + respJsonObj);
            // 校验中国银联签名异常
            throw new VerifyUnionPaySignatureException("Verify signature for UnionPayH5RefundPolicy Exception");
        }
        logger.info("Verify signature success! serialNo = {}", serialNo);

//        // 验签成功，处理应答信息
//        //status的各种状态：NEW_ORDER:新订单	 UNKNOWN:不明确的交易状态	 TRADE_CLOSED:在指定时间段内未支付时关闭的交易；在交易完成全额退款成功时关闭的交易；支付失败的交易。TRADE_CLOSED的交易不允许进行任何操作。  WAIT_BUYER_PAY:交易创建，等待买家付款。 TRADE_SUCCESS:支付成功	 TRADE_REFUND:订单转入退货流程,退货可能是部分也可能是全部。
//        String status = payNotifyMap.get(STATUS);
//        String merOrderId = payNotifyMap.get(MER_ORDER_ID);//商户订单号
//        logger.info("Verify signature of H5 pay success! merOrderId = {}, status = {}", merOrderId, status);
//        // 是否是只有PAID的时候需要处理，其它情况不用处理 问银联
//        if (!STATUS_TRADE_SUCCESS.equals(status)) {
//            logger.error("Stop! The status of merOrderId {} is: {}", merOrderId, status);
//            return;
//        }

        // B2B退余额了，这里没有继续写了（后续：修改订单状态，修改payFlow信息）
    }


    /**
     * Description: 获取消息类型
     * @author: JiuDongDong
     * @param typeCode 前端消息类型编码
     * @return java.lang.String 消息类型
     * date: 2019/5/27 10:23
     */
    private String getMsgType(String typeCode) {
        switch (typeCode) {
            case "1":
                return "trade.h5Pay";//1支付宝H5支付
            case "2":
                return "WXPay.h5Pay";//2微信H5支付
            case "3":
                return "qmf.h5Pay";//3银联在线无卡(卡耐基：这个不开通)
            case "4":
                return "uac.order";//银联云闪付（走银联全渠道）
        }
        return null;
    }

    /**
     * Description: 获取业务应用类型
     * @author: JiuDongDong
     * @param client		客户端类型：1pc; 2android; 3ios; 4wap
     * @return java.lang.String 消息类型
     * date: 2019/5/27 11:33
     */
    private String getSceneType(String client) {
        switch (client) {
            case "1":
                return null;//1PC
            case "2":
                return "AND_SDK";//2安卓app 应用
            case "3":
                return "IOS_SDK";//3苹app应用
            case "4":
                return "IOS_WAP";//手机网站:IOS_WAP 或AND_WAP
        }
        return null;
    }

    /**
     * Description: 获取应用名称：微信H5支付必填。
     *      用于苹或安卓app 应用中，传分别 对应在 AppStore和安卓分发市场中的应用名（如：全民付）；
     *      用于手机网站，传对应的网站名（如：银联商务官网）
     * @author: JiuDongDong
     * @param client		客户端类型：1pc; 2android; 3ios; 4wap
     * @return java.lang.String 消息类型
     * date: 2019/5/27 11:53
     */
    private String getMerAppName(String client) {
        switch (client) {
            case "1":
                return "易网聚鲜官网-全球生鲜B2B交易平台";//1PC
            case "2":
                return "易网聚鲜";//2安卓app应用
            case "3":
                return "易网聚鲜";//3苹app应用
            case "4":
                return "易网聚鲜官网-全球生鲜B2B交易平台";//手机网站
        }
        return null;
    }

    /**
     * Description: 获取应用标识：微信H5支付必填。
     * @author: JiuDongDong
     * @param client		客户端类型：1pc; 2android; 3ios; 4wap
     * @return java.lang.String 消息类型
     * date: 2019/5/27 13:33
     */
    private String getMerAppId(String client) {
        switch (client) {
            case "1":
                return "http://www.ewfresh.com";//1PC
            case "2":
                return "com.ewfresh.app";//2安卓app应用
            case "3":
                return "com.app.ewfresh";//3苹app应用
            case "4":
                return "http://www.ewfresh.com";//手机网站
        }
        return null;
    }

    /**
     * Description: 设置mid、tid
     * @author: JiuDongDong
     * @param mid 商户号
     * @param tid 终端号
     * @param redisParam 下单时存储在Redis的订单信息
     * @param bizType 网银支付类型: B2B:企业网银支付 B2C:个人网银支付
     * @param cardType 银行卡类型：借记卡：borrow   贷记卡：loan
     * date: 2019/6/26 17:06
     */
    private Map<String, String> getMidAndTid(String mid, String tid, Map<String, String> redisParam, String bizType, String cardType) {
        /* 根据自营和店铺、B2B和B2C、借记卡和贷记卡 选择商户号、终端号 */
        String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）
        String isRecharge = redisParam.get(IS_RECHARGE);// 支付类型的判断（实物订单支付时isRecharge=0，自营充值时isRecharge=4，订单补款isRecharge=8，白条还款isRecharge=15）
        // 实物订单支付0、订单补款8
        if (TRADE_TYPE_0.toString().equals(isRecharge) || TRADE_TYPE_8.toString().equals(isRecharge)) {
            logger.info("This is a real product order pay or supplement pay.");
            if ((STR_ZERO).equals(shopIdRedis)) {//自营订单
                logger.info("This is a self platform order.");
                if (BIZ_TYPE_B2C.equals(bizType)) {//网银支付类型: B2C:个人网银支付
                    logger.info("This is a B2C order");
                    if (CARD_TYPE_BORROW.equals(cardType)) {
                        mid = selfB2CBorrowMid;
                        tid = selfB2CBorrowTid;
                        logger.info("This is a borrow card. mid = selfB2CBorrowMid = {}, tid = selfB2CBorrowTid = {}", selfB2CBorrowMid, selfB2CBorrowTid);
                    } else {
                        mid = selfB2CLoanMid;
                        tid = selfB2CLoanTid;
                        logger.info("This is a loan card. mid = selfB2CLoanMid = {}, tid = selfB2CLoanTid = {}", selfB2CLoanMid, selfB2CLoanTid);
                    }
                } else {
                    logger.info("This is a B2B order");
                    mid = selfB2BMid;
                    tid = selfB2BTid;
                    logger.info("This is a B2B order. mid = selfB2BMid = {}, tid = selfB2BTid = {}", selfB2BMid, selfB2BTid);
                }
            } else {//店铺订单
                logger.info("This is a shop platform order.");
                if (BIZ_TYPE_B2C.equals(bizType)) {//网银支付类型: B2C:个人网银支付
                    logger.info("This is a B2C order");
                    if (CARD_TYPE_BORROW.equals(cardType)) {
                        mid = shopB2CBorrowMid;
                        tid = shopB2CBorrowTid;
                        logger.info("This is a borrow card. mid = shopB2CBorrowMid = {}, tid = shopB2CBorrowTid = {}", shopB2CBorrowMid, shopB2CBorrowTid);
                    } else {
                        mid = shopB2CLoanMid;
                        tid = shopB2CLoanTid;
                        logger.info("This is a loan card. mid = shopB2CLoanMid = {}, tid = shopB2CLoanTid = {}", shopB2CLoanMid, shopB2CLoanTid);
                    }
                } else {
                    logger.info("This is a B2B order");
                    mid = shopB2BMid;
                    tid = shopB2BTid;
                    logger.info("This is a B2B order. mid = shopB2BMid = {}, tid = shopB2BTid = {}", shopB2BMid, shopB2BTid);
                }
            }
        }
        // 充值4（充值到易网聚鲜商户）
        if (TRADE_TYPE_4.toString().equals(isRecharge)) {
            logger.info("This is a recharge pay.");
            if (BIZ_TYPE_B2C.equals(bizType)) {//网银支付类型: B2C:个人网银支付
                mid = selfB2CBorrowMid;
                tid = selfB2CBorrowTid;
                logger.info("This is a B2C recharge. mid = selfB2CBorrowMid = {}, tid = selfB2CBorrowTid = {}", selfB2CBorrowMid, selfB2CBorrowTid);
            } else {
                mid = selfB2BMid;
                tid = selfB2BTid;
                logger.info("This is a B2B recharge. mid = selfB2BMid = {}, tid = selfB2BTid = {}", selfB2BMid, selfB2BTid);
            }
        }
        // 白条还款15（退还到易网聚鲜商户）
        if (TRADE_TYPE_15.toString().equals(isRecharge)) {
            logger.info("This is a white bar repayment.");
            if (BIZ_TYPE_B2C.equals(bizType)) {//网银支付类型: B2C:个人网银支付
                logger.info("This is a B2C white bar repayment");
                if (CARD_TYPE_BORROW.equals(cardType)) {
                    mid = selfB2CBorrowMid;
                    tid = selfB2CBorrowTid;
                    logger.info("This is a borrow card. mid = selfB2CBorrowMid = {}, tid = selfB2CBorrowTid = {}", selfB2CBorrowMid, selfB2CBorrowTid);
                } else {
                    mid = selfB2CLoanMid;
                    tid = selfB2CLoanTid;
                    logger.info("This is a loan card. mid = selfB2CLoanMid = {}, tid = selfB2CLoanTid = {}", selfB2CLoanMid, selfB2CLoanTid);
                }
            } else {
                logger.info("This is a B2B white bar repayment");
                mid = selfB2BMid;
                tid = selfB2BTid;
                logger.info("This is a B2B white bar repayment. mid = selfB2BMid = {}, tid = selfB2BTid = {}", selfB2BMid, selfB2BTid);
            }
        }
        // 店铺保证金16（充值到易网聚鲜商户）
        if (TRADE_TYPE_16.toString().equals(isRecharge)) {
            logger.info("This is a shop assure pay.");
            if (BIZ_TYPE_B2C.equals(bizType)) {//网银支付类型: B2C:个人网银支付
                logger.info("This is a B2C shop assure pay.");
                if (CARD_TYPE_BORROW.equals(cardType)) {
                    mid = selfB2CBorrowMid;
                    tid = selfB2CBorrowTid;
                    logger.info("This is a borrow card. mid = selfB2CBorrowMid = {}, tid = selfB2CBorrowTid = {}", selfB2CBorrowMid, selfB2CBorrowTid);
                } else {
                    mid = selfB2CLoanMid;
                    tid = selfB2CLoanTid;
                    logger.info("This is a loan card. mid = selfB2CLoanMid = {}, tid = selfB2CLoanTid = {}", selfB2CLoanMid, selfB2CLoanTid);
                }
            } else {
                logger.info("This is a B2B shop assure pay.");
                mid = selfB2BMid;
                tid = selfB2BTid;
                logger.info("This is a B2B shop assure pay. mid = selfB2BMid = {}, tid = selfB2BTid = {}", selfB2BMid, selfB2BTid);
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put(MID, mid);
        map.put(TID, tid);
        return map;
    }

}
