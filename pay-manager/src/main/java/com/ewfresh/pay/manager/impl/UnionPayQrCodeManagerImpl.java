package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.JSON;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionPayQrCodeManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.OrderTimeOutException;
import com.ewfresh.pay.model.unionpayh5suborder.SubOrder;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.*;
import com.ewfresh.pay.service.UnionPayService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayb2cwebwap.UnionPayLogUtil;
import com.ewfresh.pay.util.unionpayh5pay.HttpPostToUnionPay;
import com.ewfresh.pay.util.unionpayh5pay.IfShareBenefit;
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
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_txnAmt;

/**
 * description: 银联QrCode逻辑管理类
 * @author: JiuDongDong
 * date: 2019/5/10.
 */
@Component
public class UnionPayQrCodeManagerImpl implements UnionPayQrCodeManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat h5SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //读取资源配置参数
    @Value("${QRCode.url}")
    private String APIurl;//银商平台接口地址
    @Value("${QRCode.self.mid}")
    private String selfMid;//自营商户号
    @Value("${QRCode.self.tid}")
    private String selfTid;//自营终端号
    @Value("${QRCode.shop.mid}")
    private String shopMid;//店铺商户号
    @Value("${QRCode.shop.tid}")
    private String shopTid;//店铺终端号
    @Value("${freight.shop.mid}")
    private String freightShopMid;//商户号：运费
    @Value("${freight.shop.tid}")
    private String freightShopTid;//终端号：运费
    @Value("${QRCode.instMid}")
    private String instMid;//机构商户号
    @Value("${QRCode.msgSrc}")
    private String msgSrc;//来源系统
    @Value("${QRCode.msgSrcId}")
    private String msgSrcId;//来源系统id
    @Value("${QRCode.key}")
    private String key;//通讯秘钥

    @Value("${QRCode.msgType_getQRCode}")
    private String msgType_getQRCode;//消息类型:获取二维码
    @Value("${QRCode.msgType_refund}")
    private String msgType_refund;//消息类型:订单退款
    @Value("${QRCode.msgType_query}")
    private String msgType_query;//消息类型:账单查询
    @Value("${QRCode.msgType_queryLastQRCode}")
    private String msgType_queryLastQRCode;//消息类型:根据商户终端号查询此台终端最后一笔详单情况
    @Value("${QRCode.msgType_queryQRCodeInfo}")
    private String msgType_queryQRCodeInfo;//消息类型:查询二维码静态信息
    @Value("${QRCode.msgType_closeQRCode}")
    private String msgType_closeQRCode;//消息类型:关闭二维码
    @Value("${QRCode.notifyUrl}")
    private String notifyUrl;//支付结果通知地址
    @Value("${QRCode.returnUrl}")
    private String returnUrl;//前台网页跳转地址
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private UnionPayH5RedisService unionPayH5RedisService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private UnionPayService unionPayService;
    @Autowired
    private GetBenefitFateUtil getBenefitFateUtil;
    @Autowired
    private GetMidAndTid getMidAndTid;

    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;

    /**
     * Description: 获取二维码
     * @author: JiuDongDong
     * date: 2019/5/10 17:00
     */
    @Override
    public void getQrCode(ResponseData responseData, Map<String, String> params) throws Exception {
        logger.info("It is now in UnionPayQrCodeManagerImpl.sendOrder, the parameters are: [params = {}]",
                JsonUtil.toJson(params));
        /* 1.获取必要参数 */
        String orderId = params.get(param_orderId);
        String txnAmt = params.get(param_txnAmt);//交易金额，单位为元
        String channelType = params.get(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
        String shopId = params.get(SHOP_ID);//店铺id

        /* 1.2 校验订单支付金额是否正确 */
        Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(orderId);
        if (null == redisParam) {
            logger.error("Get QrCode! The param in redis of this order " + orderId + " is expired, can not pay anymore");
            responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
            responseData.setMsg(ORDERTIMEOUT);// 本次支付已超时！
            return;
        }
        String shouldPayMoney = redisParam.get(SURPLUS);
        if (new BigDecimal(txnAmt).compareTo(new BigDecimal(shouldPayMoney)) != INTEGER_ZERO) {
            logger.error("Get QrCode! Web should pay money not equals redis, shouldPayMoney from redis = " +
                    shouldPayMoney + ", web txnAmt = " + txnAmt);
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
        String isRecharge = redisParam.get(IS_RECHARGE);// 支付类型的判断（实物订单支付时isRecharge=0，自营充值时isRecharge=4，订单补款isRecharge=8，白条还款isRecharge=15）
        logger.info("The shopIdRedis is: {}, isRecharge = {}", shopIdRedis, isRecharge);

        String mid = null;//商户号
        String tid = null;//终端号
        Map<String, String> midAndTid = getMidAndTid(mid, tid, redisParam);
        mid = midAndTid.get(MID);
        tid = midAndTid.get(TID);
        logger.info("The mid = {}, tid = {}", mid, tid);

        //组织请求报文
        JSONObject json = new JSONObject();
        //json.put(MSG_ID, "");//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);//来源系统标识
        json.put(MSG_TYPE, msgType_getQRCode);//消息类型
        json.put(REQUEST_TIMESTAMP, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));//报文请求时间
        //json.put(SRC_RESERVE, "预留字段内容");//	请求系统预留字段
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(INST_MID, instMid);//业务类型
        json.put(BILL_NO, orderId);//账单号
        json.put(BILL_DATE, DateFormatUtils.format(new Date(), "yyyy-MM-dd"));//账单日期，格式yyyy-MM-dd
        //json.put(BILL_DESC, "账单描述");//账单描述
        txnAmt = FenYuanConvert.yuan2Fen(txnAmt).toString();
        json.put(TOTAL_AMOUNT, txnAmt);//支付总金额

        // 是否分账（目前运费流入主商户，所以如果买的是自营商品，则不用分账；如果买的是店铺商品，因为需要分润，所以得分账）
        boolean shareBenefit = IfShareBenefit.ifShareBenefit(shopIdRedis, isRecharge);//是否分润
        shareBenefit = TRADE_TYPE_16.toString().equals(isRecharge) ? false : shareBenefit;
        boolean divisionFlag = shareBenefit;//是否分账
        json.put(DIVISION_FLAG, divisionFlag);//分账标记。暂时只支持微信（WXPay.jsPay）、支付宝（trade.jsPay）支付
        List<SubOrder> subOrderList = new ArrayList<>();
        if ((boolean) json.get(DIVISION_FLAG)) {
            logger.info("Start share benefit and division!");
//            /* 运费分账 */ 因为运费流入主商户，运费不用分了
//            String freight = redisParam.get(FREIGHT);//这个运费是使用银联支付的运费金额
//            SubOrder subOrderFreight = new SubOrder();
//            subOrderFreight.setMid(freightShopMid);// 898127210280001 这是测试环境运费专用商户号
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
            //subOrderShop.setMid("988460101800201");// 这是测试环境虚拟的一个店铺的商户号
            subOrderShop.setMid(shopMid);
            subOrderShop.setTotalAmount(FenYuanConvert.yuan2Fen(unionPayShopBenefit).toString());
            subOrderList.add(subOrderShop);

            // 易网聚鲜平台入账 = 易网聚鲜分润 + 银联通道运费 = 支付总金额 - 店铺分润
            BigDecimal platformAmount = new BigDecimal(unionPayEwfreshBenefit).add(new BigDecimal(freight));
            json.put(PLATFORM_AMOUNT, FenYuanConvert.yuan2Fen(platformAmount.toString()).toString());
            json.put(SUB_ORDERS, subOrderList);//子商户分账信息，包括子商户号、分账金额
        }

        //json.put(GOODS, "商品对象详情");//商品信息
        //json.put(MEMBER_ID, "会员号");//支付通知里原样返回
        //json.put(COUNTER_NO, " 桌号、柜台号、房间号");//支付通知里原样返回
        json.put(EXPIRE_TIME, h5SimpleDateFormat.format(futureMountSecondsStart)); //账单过期时间，为空则不过期，格式yyyy-MM-dd HH:mm:ss。一次性二维码的默认过期时间为30分钟，固定码无期限
        json.put(NOTIFY_URL, notifyUrl);//支付结果通知地址，支付成功后银商平台会将支付通知发至该url
        //json.put(RETURN_URL, returnUrl);//支付成功后用户端会跳转至该url
        //json.put(QRCODE_ID, "");// 二维码ID，针对需要自行生成二维码的情况
        //json.put(SYSTEM_ID_UNION, "");//系统ID
        json.put(SECURE_TRANSACTION, false);//担保交易标识
        json.put(WALLET_OPTION, WALLET_OPTION_MULTIPLE);//钱包选项，说明：1.单一钱包支付传SINGLE, 多钱包支付传MULTIPLE
        json.put(SIGN_TYPE, MD5);//签名算法
        //json.put(NAME, "");//实名认证姓名
        //json.put(MOBILE, "");//实名认证手机号
        //json.put(CERT_TYPE, "");//实名认证证件类型
        //json.put(CERT_NO, "");//实名认证证件号
        //json.put(FIX_BUYER, "");//是否需要实名认证
        //json.put(LIMIT_CREDIT_CARD, "");//是否需要限制信用卡支付。取值：true或false，默认false
        //json.put(PAY_INFO_QUERY_ADDR, "");//支付要素查询地址

        // Json转换成Map
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(key, paramsMap));
        logger.info("paramsMap = {}", paramsMap);
        logger.info("paramsMap = {}", JsonUtil.toJson(paramsMap));

        if (StringUtils.isBlank(APIurl)) {
            logger.error("APIurl is blank for getQrCode");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        //调用银商平台获取二维码接口
        com.alibaba.fastjson.JSONObject respJsonObj = null;
        Map<String, Object> paramsMap1 = UnionPayQrCodeUtil.jsonToMapWithObjectValue(json);
        paramsMap1.put(SIGN, paramsMap.get(SIGN));
        if ((boolean) json.get(DIVISION_FLAG)) {
            paramsMap1.put(SUB_ORDERS, subOrderList);
        }
        try {
            respJsonObj = HttpPostToUnionPay.httpPostToUnionPayObject(APIurl, paramsMap1);
            logger.info("respJsonObj = {}", respJsonObj);
        } catch (Exception e) {
            logger.error("Get qrCode from unionPay occurred error, orderId = " + orderId
                    + ", respJsonObj = " + respJsonObj, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
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
        boolean checkSign = UnionPayQrCodeUtil.checkSign(key, verifyParams);
        if (!checkSign) {
            logger.error("Verify signature of single query failed, respJsonObj = " + respJsonObj);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 处理响应信息
        String errCode = respJsonObj.getString(ERR_CODE);
        String errMsg = respJsonObj.getString(ERR_MSG);
        UnionPayLogUtil.logTradeInfo(responseData, errCode, errMsg, orderId);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("UnionPay qrCode handle this trade failed, respJsonObj = " + respJsonObj + ", orderId = " + orderId);
            return;
        }
        logger.info("Get QrCode OK!");

        // 订单号放入Redis以备接收不到通知时自主查询
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ORDER_ID, orderId);
        paramMap.put(MID, mid);
        paramMap.put(TID, tid);
        String billDate = json.get(BILL_DATE).toString();//账单日期，格式yyyy-MM-dd
        paramMap.put(BILL_DATE, billDate);
        unionPayH5RedisService.putPayOrderInfo(PAY_UNIONPAYQRCODE_PAY_INFO, paramMap);

        String billQRCode = (String) respJsonObj.get(BILL_QRCODE);//二维码
        responseData.setEntity(billQRCode);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }


    /**
     * Description: 支付、退款交易的通知（退款也会通知到此url）
     * @author: JiuDongDong
     * @param respStr 通知字符串
     */
    @Override
    public void receiveNotify(ResponseData responseData, String respStr) throws Exception {
        logger.info("It is now in UnionPayQrCodeManagerImpl.receiveNotify, the params are:" +
                " respStr = {}", respStr);
        String md5Key = key;

        //对支付通知字串进行解码
        String trueColors;
        try {
            trueColors = URLDecoder.decode(respStr, "utf-8");
            trueColors = trueColors.replace("¬","&not");
        } catch (UnsupportedEncodingException e) {
            logger.error("URLDecoder.decode qr code pay notify failed, notify info = " + respStr, e);
            throw e;
        }
        logger.info("The notify info after decoded is: {}", trueColors);

        //将解码后的支付通知字串 转成map
        String[] splitPreStrs = trueColors.split("&");
        String[] subStr;
        String notifySign = "";
        Map<String, String> payNotifyMap = new HashMap<>();
        for (String str : splitPreStrs) {
            subStr = str.split("=");
            if (subStr[0].equals(SIGN) || subStr[0] == SIGN) {
                notifySign = subStr[1];
                //组装待签字串时 去除原有待签字串中的sign
                continue;
            }
            // 网付聚合码
//            if (subStr[0].equals(BILL_QRCODE) || subStr[0] == BILL_QRCODE) {
//                payNotifyMap.put(subStr[0], subStr[1] + "=" + subStr[2]);
//            } else {
//                payNotifyMap.put(subStr[0], subStr[1]);
//            }
            // 银联聚合码
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
        String sign = DigestUtils.md5Hex(UnionPayQrCodeUtil.getContentBytes(preStrNew_md5Key)).toUpperCase();
        logger.info("Signed String = {}", sign);
        // 比较签名是否一致
        boolean equals = StringUtils.equalsIgnoreCase(sign, notifySign);
        if (!equals) {
            logger.error("Verify signature failed, payNotifyMap = " + JsonUtil.toJson(payNotifyMap));
            throw new RuntimeException("Verify signature failed");
        }
        // 验签成功，处理应答信息
        String billStatus = payNotifyMap.get(BILL_STATUS);// 账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
        String billNo = payNotifyMap.get(BILL_NO);//账单号

        logger.info("Verify signature of QrCode pay or refund success! billNo = {}, billStatus = {}", billNo, billStatus);
        if (!BILL_STATUS_PAID.equals(billStatus) && !BILL_STATUS_REFUND.equals(billStatus)) {
            logger.error("Stop! The status of billNo {} is: {}", billNo, billStatus);
            logger.error("payNotifyMap = {}", JsonUtil.toJson(payNotifyMap));
            return;
        }
        // 支付、退款完成
        logger.info("This order has been paid or refund, billNo = {}", billNo);

        // 获取订单响应信息
        String mid = payNotifyMap.get(MID);//商户号	String	true
        String tid = payNotifyMap.get(TID);//终端号	String	true
        String instMid = payNotifyMap.get(INST_MID);//业务类型	String	true	QRPAYDEFAULT
        String billQRCode = payNotifyMap.get(BILL_QRCODE);//账单二维码	String	true
        String billDate = payNotifyMap.get(BILL_DATE);//订单时间，格式yyyy-MM-dd	Date	true
        String createTime = payNotifyMap.get("createTime");//账单创建时间，格式yyyy-MM-dd HH:mm:ss	Date	false
        //String billStatus = payNotifyMap.get(BILL_STATUS);// 账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
        String billDesc = payNotifyMap.get(BILL_DESC);//账单描述	String		false
        String totalAmount = payNotifyMap.get(TOTAL_AMOUNT);//账单总金额	Number	false	若涉及营销联盟优惠，则此为优惠前总金额
        String memberId = payNotifyMap.get(MEMBER_ID);//会员号	String	false
        String counterNo = payNotifyMap.get(COUNTER_NO);//桌号、柜台号、房间号	String	false
        String merName = payNotifyMap.get(MER_NAME);//商户名称	String	false
        String memo = payNotifyMap.get(MEMO);//付款附言	String	false
        String notifyId = payNotifyMap.get(NOTIFY_ID);//支付通知ID	String	true	通知唯一ID，重发通知的notifyId不变
        String secureStatus = payNotifyMap.get(SECURE_STATUS);//担保状态	String	false	取值说明
        String completeAmount = payNotifyMap.get(COMPLETE_AMOUNT);//担保完成金额（分）	Number	false
        String extraBuyerInfo = payNotifyMap.get(EXTRA_BUYER_INFO);//用户额外信息	String	false	Json格式的数据。例如key为cardAttr是借贷记标识
        // billPayment的信息
        String billPaymentStr = payNotifyMap.get(BILL_PAYMENT);
        com.alibaba.fastjson.JSONObject billPaymentJsonObject;
        String status = null;
        String payTime = null;
        String merOrderId = null;
        String paySeqId = null;
        if (StringUtils.isNotBlank(billPaymentStr)) {
            billPaymentJsonObject = JSON.parseObject(billPaymentStr);
            merOrderId = billPaymentJsonObject.get(MER_ORDER_ID).toString();//商户订单号（这个是银联生成的，可以用来对账、查询订单、订单退款）
            String billBizType = null == billPaymentJsonObject.get(BILL_BIZ_TYPE) ? null : billPaymentJsonObject.get(BILL_BIZ_TYPE).toString();//账单业务类型
            paySeqId = null == billPaymentJsonObject.get(PAY_SEQ_ID) ? null : billPaymentJsonObject.get(PAY_SEQ_ID).toString();//交易参考号
            String totalAmount1 = null == billPaymentJsonObject.get(TOTAL_AMOUNT) ? null : billPaymentJsonObject.get(TOTAL_AMOUNT).toString();//账单流水总金额
            String buyerPayAmount = null == billPaymentJsonObject.get(BUYER_PAY_AMOUNT) ? null : billPaymentJsonObject.get(BUYER_PAY_AMOUNT).toString();//实付金额
            String invoiceAmount = null == billPaymentJsonObject.get(INVOICE_AMOUNT) ? null : billPaymentJsonObject.get(INVOICE_AMOUNT).toString();//开票金额
            String discountAmount = null == billPaymentJsonObject.get(DISCOUNT_AMOUNT) ? null : billPaymentJsonObject.get(DISCOUNT_AMOUNT).toString();//折扣金额
            String buyerId = null == billPaymentJsonObject.get(BUYER_ID) ? null : billPaymentJsonObject.get(BUYER_ID).toString();//买家ID
            String receiptAmount = null == billPaymentJsonObject.get(RECEIPT_AMOUNT) ? null : billPaymentJsonObject.get(RECEIPT_AMOUNT).toString();//实收金额
            String couponAmount = null == billPaymentJsonObject.get(COUPON_AMOUNT) ? null : billPaymentJsonObject.get(COUPON_AMOUNT).toString();//网付计算的优惠金额
            String buyerUsername = null == billPaymentJsonObject.get(BUYER_USERNAME) ? null : billPaymentJsonObject.get(BUYER_USERNAME).toString();//买家用户名
            String payDetail = null == billPaymentJsonObject.get(PAY_DETAIL) ? null : billPaymentJsonObject.get(PAY_DETAIL).toString();//支付详情
            payTime = null == billPaymentJsonObject.get(PAY_TIME) ? null : billPaymentJsonObject.get(PAY_TIME).toString();//支付时间，格式yyyy-MM-dd HH:mm:ss
            String settleDate = null == billPaymentJsonObject.get(SETTLE_DATE) ? null : billPaymentJsonObject.get(SETTLE_DATE).toString();//结算时间，格式yyyy-MM-dd
            //status的各种状态：NEW_ORDER:新订单	 UNKNOWN:不明确的交易状态	 TRADE_CLOSED:在指定时间段内未支付时关闭的交易；在交易完成全额退款成功时关闭的交易；支付失败的交易。TRADE_CLOSED的交易不允许进行任何操作。  WAIT_BUYER_PAY:交易创建，等待买家付款。 TRADE_SUCCESS:支付成功	 TRADE_REFUND:订单转入退货流程,退货可能是部分也可能是全部。
            status = null == billPaymentJsonObject.get(STATUS) ? null : billPaymentJsonObject.get(STATUS).toString();//交易状态
            String targetOrderId = null == billPaymentJsonObject.get(TARGET_ORDER_ID) ? null : billPaymentJsonObject.get(TARGET_ORDER_ID).toString();//目标平台单号
            String targetSys = null == billPaymentJsonObject.get(TARGET_SYS) ? null : billPaymentJsonObject.get(TARGET_SYS).toString();//目标系统
        }
        // 获取订单退款响应信息
        String refundTargetOrderId = payNotifyMap.get(REFUND_TARGET_ORDER_ID);//目标系统退货订单号
        String refundSettleDate = payNotifyMap.get("refundSettleDate");//文档没有该字段
        String refundOrderId = payNotifyMap.get(REFUND_ORDER_ID);//退款订单号
        String refundPayTime = payNotifyMap.get(REFUND_PAY_TIME);//退款时间
        String qrCodeType = payNotifyMap.get("qrCodeType");//文档没有该字段
        String subInst = payNotifyMap.get("subInst");//文档没有该字段
        String receiptAmount = payNotifyMap.get(RECEIPT_AMOUNT);//商户实收金额，支付宝会有
        String seqId = payNotifyMap.get(SEQ_ID);//平台流水号
        String refundAmount = payNotifyMap.get(REFUND_AMOUNT);//总退款金额
        String vV = payNotifyMap.get("vV");//文档没有该字段

        /* 订单支付的回调处理 */
        // billStatus=PAID, status=TRADE_SUCCESS 表明支付成功，更新数据库。
        if (BILL_STATUS_PAID.equals(billStatus) && STATUS_TRADE_SUCCESS.equals(status)) {
            logger.info("This is a pay notify. billNo = {}, billStatus = {}, status = {}", billNo, billStatus, status);
            // 获取Redis中缓存的支付信息
            Map<String, String> redisParam = orderRedisService.getOrderInfoFromRedis(billNo);
            if (MapUtils.isEmpty(redisParam)) {
                logger.error("Order is time out! billNo = " + billNo);
                throw new OrderTimeOutException("Order is time out!");
            }
            String payMode = redisParam.get(PAY_MODE);
            String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId；白条还款时，也需要传一个shopId集合）

            /* 支付成功，支付流水持久化到本地 */
            // 封装持久化数据
            Map<String, Object> param = new HashMap<>();
            //param.put(CHANNEL_FLOW_ID, paySeqId);//支付渠道流水号（是银联生成的交易参考号）
            param.put(CHANNEL_FLOW_ID, billNo);//支付渠道流水号
            param.put(PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(totalAmount));//付款方支付金额
            param.put(PAYER_TYPE, STR_ZERO);//付款账号类型(1个人,2店铺)
            param.put(RECEIVER_TYPE, STR_ONE);//收款账号类型(1个人,2店铺)
            param.put(RECEIVER_USER_ID, shopIdRedis);//收款人ID
            param.put(SUCCESS_TIME, payTime);//支付时间，格式yyyy-MM-dd HH:mm:ss
            param.put(IS_REFUND, IS_REFUND_NO + "");//是否退款 0:否,1是
            param.put(RETURN_INFO, billDesc);//返回信息
            param.put(DESP, BUY_GOODS);//描述
            param.put(UID, UID_UNIONPAY);//操作人标识，中国银联
            //渠道类型 07：互联网； 08：移动； 其他：银行编号
            //String bankIdRedis = redisParam.get(BILL99_BANK_ID);// 银行id
            //param.put(BOB_CHANNEL_TYPE, StringUtils.isBlank(bankIdRedis) ? bankId : bankIdRedis);
            param.put(PAYER_ID, INTEGER_ONE + "");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
            param.put(TRADE_TYPE, TRADE_TYPE_1);//交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
            param.put(INTERACTION_ID, billNo);//账单号
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
            logger.info("QRCode. Receive notify and serialize to merchant {} for orderId: {}", ifSuccess, merOrderId);
            // 3.3 删除支付时放在Redis的信息
            if (ifSuccess) {
                unionPayH5RedisService.delPayOrderInfo(PAY_UNIONPAYQRCODE_PAY_INFO, billNo);
                logger.info("QRCode. Del pay order info from Redis ok. billNo = {}", billNo);
            }
            return;
        }

        /* 订单退款的回调处理 */
        if (BILL_STATUS_REFUND.equals(billStatus) && STATUS_TRADE_REFUND.equals(status)) {
            logger.info("This is a refund notify. billNo = {}, billStatus = {}, status = {}", billNo, billStatus, status);
            /* 从Redis获取退款信息---UnionPayQRCodeRefundPolicy放入 */
            RefundInfoVo refundOrderInfoRedis = unionPayH5RedisService.getRefundOrderInfoFromRedis(refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO);
            logger.info("refundOrderInfoRedis = {}", JsonUtil.toJson(refundOrderInfoRedis));
            RefundParam refundParam = refundOrderInfoRedis.getRefundParam();
            String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            logger.info("outRequestNo = {}", outRequestNo);
            /* 从Redis获取退款信息---order项目退款时放入（补款退款、退货退款并不会放） */
            //Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            //logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            //String refundType = redisRefundInfo.get(REFUND_TYPE);//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
            String refundType = refundOrderInfoRedis.getRefundType();//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
            // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
            String finalOrderStatus = null;
            if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_RETURN.toString();
            if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = Constants.ORDER_SHUTDOWN.toString();
            //if (REFUND_TYPE_REFUNDS.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_REFUND.toString();
            logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);

            // 获取订单状态
            Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
            logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
            // 订单系统的订单状态为0或null，说明订单状态查询有误
            if (INTEGER_ZERO == orderStatusFromOrder || null == orderStatusFromOrder) {
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
            //unionPayRedisService.deleteRefundOrderInfoInRedis(refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO);
            //unionPayRedisService.delReturnAmountParams(outRequestNo);
            logger.info("payFlow = {}", JsonUtil.toJson(payFlow));

            if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                logger.info("updateOrderStatusUrl = {}, params = {}", updateOrderStatusUrl, JsonUtil.toJson(params));
                unionPayService.updatePayFlowAndOrder(payFlow, updateOrderStatusUrl, params, refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO, outRequestNo);
            } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO, null);
                logger.info("Order system handle.");
            } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO, null);
                logger.info("Order system handle.");
            } else {
                unionPayService.updatePayFlowAndOrder(payFlow, null, null, refundOrderId, PAY_UNIONPAYQRCODE_REFUND_INFO, null);
            }
            logger.info("Receive refund notify and serialize to merchant ok for orderId: {}", merOrderId);
        }
    }

    /**
     * Description: 单笔交易查询（订单交易时间必传，订单号、账单号二选一必传。订单号为与银联交易的加E加R的订单号。
     *              查询退款时传退款订单号）
     * @author: JiuDongDong
     * @param billDate 订单时间，格式yyyy-MM-dd
     * @param billNo 账单号，也可用merOrderId(支付成功回调，银联给生成的商户订单号)，同样起作用。总之，billNo、orderId最少传一个。
     * @param tradeType 交易类型：1：支付交易  2：退款交易
     * @param refundSeq 退款流水号
     * @param mid 未支付完成的时候进行查询，需要携带mid、tid, tradeType=3时
     * @param tid 未支付完成的时候进行查询，需要携带mid、tid, tradeType=3时
     * date: 2019/5/14 19:50
     */
    @Override
    public void singleQuery(ResponseData responseData, String billDate, String billNo, String tradeType, String refundSeq, String mid, String tid) {
        logger.info("It is now in UnionPayQrCodeManagerImpl.singleQuery, the parameters are: " +
                "[billDate = {}, billNo = {}, tradeType = {}, refundSeq = {}, mid = {}, tid = {}]",
                billDate, billNo, tradeType, refundSeq, mid, tid);
        // 获取mid、tid
        Map<String, String> midAndTid;
        if (H5_TRADE_TYPE_1.equals(tradeType)) {
            logger.info("It is a pay trade query.");
            midAndTid = getMidAndTid.getMidAndTidByInteractionId(billNo);
            mid = midAndTid.get(MID);
            tid = midAndTid.get(TID);
        } else if (H5_TRADE_TYPE_2.equals(tradeType)){
            logger.info("It is a refund trade query.");
            midAndTid = getMidAndTid.getMidAndTidByRefundSequence(refundSeq);
            mid = midAndTid.get(MID);
            tid = midAndTid.get(TID);
        } else if (H5_TRADE_TYPE_3.equals(tradeType)) {
            logger.info("The order has not been paid! billNo = {}", billNo);
        }
        if (StringUtils.isBlank(tid) || StringUtils.isBlank(mid)) {
            logger.error("Mid or Tid is null, Mid = " + mid + ", Tid = " + tid);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        //组织请求报文
        JSONObject json = new JSONObject();
        //json.put(MSG_ID, "");//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);//来源系统标识
        json.put(MSG_TYPE, msgType_query);//消息类型
        json.put(REQUEST_TIMESTAMP, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        //json.put(SRC_RESERVE, "srcReserve");//请求系统预留字段
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(INST_MID, instMid);//业务类型 QRPAYDEFAULT
        json.put(BILL_NO, billNo);//也可填merOrderId(支付成功回调，银联给生成的商户订单号)，同样起作用
        if (H5_TRADE_TYPE_2.equals(tradeType)) {
            json.put(REFUND_ORDER_ID, refundSeq);//当需要当前帐单退货记录的时候上送
        }
        json.put(BILL_DATE, billDate);//订单时间，格式yyyy-MM-dd
        json.put(SIGN_TYPE, MD5);//签名算法，值为：MD5或 SHA256；若不上送默认为MD5

        // 查询请求参数转map
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        // 生成签名
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(key, paramsMap));
        logger.info("paramsMap：{}", paramsMap);

        if (StringUtils.isBlank(APIurl)) {
            logger.error("APIurl is blank for qr code single query");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        //调用银商平台查询接口
        com.alibaba.fastjson.JSONObject respJsonObj = null;
        try {
            respJsonObj = HttpPostToUnionPay.httpPostToUnionPay(APIurl, paramsMap);
        } catch (Exception e) {
            logger.error("Single query qrCode billNo occurred error, billNo = " + billNo, e);
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
        boolean checkSign = UnionPayQrCodeUtil.checkSign(key, verifyParams);
        if (!checkSign) {
            logger.error("Verify signature of single query failed, billNo = "
                    + billNo + ", respJsonObj = " + respJsonObj);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 验签成功，处理应答码
        String errCode = respJsonObj.getString(ERR_CODE);
        String errMsg = respJsonObj.getString(ERR_MSG);
        UnionPayLogUtil.logTradeInfo(responseData, errCode, errMsg, billNo);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("UnionPay qrCode handle this trade failed");
            return;
        }
        // 如果查询无误，将信息全部响应出去
        responseData.setEntity(respJsonObj);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 设置mid、tid
     * @author: JiuDongDong
     * @param mid 商户号
     * @param tid 终端号
     * @param redisParam 下单时存储在Redis的订单信息
     * date: 2019/6/27 13:46
     */
    private Map<String, String> getMidAndTid(String mid, String tid, Map<String, String> redisParam) {
        /* 根据自营和店铺 选择商户号、终端号 */
        String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付、补款，即自营和店铺商品的订单支付、补款时，会传shopId）
        String isRecharge = redisParam.get(IS_RECHARGE);// 支付类型的判断（实物订单支付时isRecharge=0，自营充值时isRecharge=4，订单补款isRecharge=8，白条还款isRecharge=15）
        // 实物订单支付0、订单补款8
        if (TRADE_TYPE_0.toString().equals(isRecharge) || TRADE_TYPE_8.toString().equals(isRecharge)) {
            logger.info("This is a real product order pay or supplement pay.");
            if ((STR_ZERO).equals(shopIdRedis)) {//自营订单
                mid = selfMid;
                tid = selfTid;
                logger.info("This is a self platform order. mid = selfMid = {}, tid = selfTid = {}", selfMid, selfTid);
            } else {//店铺订单
                mid = shopMid;
                tid = shopTid;
                logger.info("This is a shop platform order. mid = shopMid = {}, tid = shopTid = {}", shopMid, shopTid);
            }
        }
        // 充值4（充值到易网聚鲜商户）
        if (TRADE_TYPE_4.toString().equals(isRecharge)) {
            mid = selfMid;
            tid = selfTid;
            logger.info("This is a recharge pay. mid = selfMid = {}, tid = selfTid = {}", selfMid, selfTid);
        }
        // 白条还款15（退还到易网聚鲜商户）
        if (TRADE_TYPE_15.toString().equals(isRecharge)) {
            mid = selfMid;
            tid = selfTid;
            logger.info("This is a white bar repayment. mid = selfMid = {}, tid = selfTid = {}", selfMid, selfTid);
        }
        // 店铺保证金16（充值到易网聚鲜商户）
        if (TRADE_TYPE_16.toString().equals(isRecharge)) {
            mid = selfMid;
            tid = selfTid;
            logger.info("This is a white bar repayment. mid = selfMid = {}, tid = selfTid = {}", selfMid, selfTid);
        }
        Map<String, String> map = new HashMap<>();
        map.put(MID, mid);
        map.put(TID, tid);
        return map;
    }

}
