package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.UnionpayB2CWebWapManager;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.redisService.SendMessageRedisService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayb2cwebwap.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * description: 银联B2C，web、wap的业务逻辑管理层
 * @author: JiuDongDong
 * date: 2019/4/25.
 */
@Component
public class UnionpayB2CWebWapManagerImpl implements UnionpayB2CWebWapManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private SDKConfig sdkConfig;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private SendMessageRedisService sendMessageRedisService;



    @Override
    public void sendOrder(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in UnionpayB2CWebWapManagerImpl.sendOrder, the parameters are: [params = {}]",
                JsonUtil.toJson(params));
        /* 1.获取必要参数 */
        String orderId = params.get(param_orderId);
        String txnAmt = params.get(param_txnAmt);//交易金额，单位元
        String channelType = params.get(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
        String customerIp = params.get(param_customerIp);//持卡人IP

        // TODO stage放开 start
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
            logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
        }
        // 2.订单尾款支付时，则以用户选中银行，点击下一步时放入Redis的“current时间”作为基准计算订单支付超时拦截时间
        if (ORDER_PAID_EARNEST.intValue() == Integer.valueOf(orderStatus)) {
            String currentTime = redisParam.get(CURRENT_TIME);
            baseTime = currentTime;
            logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
        }
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
        // TODO stage放开 end

        /* 2.获取配置信息 */
        String version = sdkConfig.getVersion();//版本号，全渠道默认值
        String encoding = sdkConfig.getEncoding();//字符集编码 可以使用UTF-8,GBK两种方式
        String signMethod = sdkConfig.getSignMethod();//签名方法
        String merId = sdkConfig.getMerId();//TODO 上线多个商户，处理商户id
        String frontUrl = sdkConfig.getFrontUrl();//前台通知地址，支付成功后的页面 点击“返回商户”按钮的时候将异步通知报文post到该地址
        String backUrl = sdkConfig.getBackUrl();//后台通知地址，http https均可。收单后台通知后需要10秒内返回http200或302状态码 //如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
        String frontRequestUrl = sdkConfig.getFrontRequestUrl();
        String frontTransUrl = sdkConfig.getFrontTransUrl();
        String frontFailUrl = sdkConfig.getFrontFailUrl();//失败交易前台跳转地址

        Map<String, String> requestData = new HashMap<>();
        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        requestData.put(param_version, version);
        requestData.put(param_encoding, encoding);
        requestData.put(param_signMethod, signMethod);
        requestData.put(param_txnType, "01");       //交易类型 ，01：消费
        requestData.put(param_txnSubType, "01");    //交易子类型， 01：自助消费
        requestData.put(param_bizType, "000201");   //产品类型
        requestData.put(param_channelType, channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机

        /***商户接入参数***/
        requestData.put(param_merId, merId);        //商户号码
        requestData.put(param_accessType, "0");     //TODO 接入类型，0：普通商户直连接入 1：收单机构接入 2：平台类商户接入
//        requestData.put(param_subMerId, "");//TODO 二级商户代码。商户类型为平台类商户接入时必须上送
//        requestData.put(param_subMerName, "");//TODO 二级商户全称。商户类型为平台类商户接入时必须上送
//        requestData.put(param_subMerAbbr, "");//TODO 二级商户简称。商户类型为平台类商户接入时必须上送
        requestData.put(param_orderId, orderId);     //商户订单号，8-40位数字字母，不能含“-”或“_”
        requestData.put(param_txnTime, sdf.format(new Date()));// 订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        requestData.put(param_currencyCode, "156"); //交易币种（境内商户一般是156 人民币）
        requestData.put(param_txnAmt, FenYuanConvert.yuan2Fen(txnAmt).toString());//交易金额，单位分，不要带小数点
        //requestData.put(param_reqReserved, "透传字段");//请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        //requestData.put("riskRateInfo", "{commodityName=测试商品名称}");//风控信息域
        requestData.put(param_frontUrl, frontUrl);//前台通知地址，支付成功后的页面 点击“返回商户”按钮的时候将异步通知报文post到该地址
        requestData.put(param_backUrl, backUrl);//后台通知地址，http https均可。收单后台通知后需要10秒内返回http200或302状态码 //如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
        requestData.put(param_frontFailUrl, frontFailUrl);//支付失败时，页面跳转至商户该URL
        /* 订单超时时间。 */
        // 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。 跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户。
        // 此时间建议取支付时的北京时间加15分钟。
        // 超过超时时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败。
        String payTimeOut = null;// 订单支付超时时间
        if (futureMountSecondsStart != null) payTimeOut = sdf.format(futureMountSecondsStart);
        if (futureMountSecondsStart == null) payTimeOut = sdf.format(new Date().getTime() + 60 * 60 * 1000);//给60分钟的时间
        requestData.put(param_payTimeout, payTimeOut);
        requestData.put(param_customerIp, customerIp);//持卡人ip
//        requestData.put(param_orderDesc, orderDesc);//TODO 订单描述，移动支付上送

//        // 分账域的处理 TODO
//        UnionPayWebWapAccSplitData1 unionPayWebWapAccSplitData1 = new UnionPayWebWapAccSplitData1();
//        unionPayWebWapAccSplitData1.setAccSplitType("1");
//        List<AccSplitMcht> accSplitMchts = new ArrayList<>();
//        // TODO 如果有多个分账子商户，则继续加到List
//        AccSplitMcht accSplitMcht = new AccSplitMcht();
//        accSplitMcht.setAccSplitMerId("todo ");
//        accSplitMcht.setAccSplitAmt("1");
//        unionPayWebWapAccSplitData1.setAccSplitMchts(accSplitMchts);
//        String json = (String) JSON.toJSON(unionPayWebWapAccSplitData1);
//        byte[] base64 = Base64.encodeBase64(json.getBytes());
//        String s = new String(base64);
//        logger.info("s = {}", s);
//        requestData.put(param_accSplitData, s);//分账域，该域需整体做 Base64 编码。

//        分账测试号
//        主商户 898201612345678
//        子商户 898127210280001  988460101800201  988460101800202  988460101800203  988460101800204  988460101800205

        /**请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面**/
        Map<String, String> submitFromData = AcpService.sign(requestData, encoding);  //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String requestFrontUrl = frontTransUrl;//请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.frontTransUrl
        String html = AcpService.createAutoFormHtml(requestFrontUrl, submitFromData, encoding);   //生成自动跳转的Html表单

        LogUtil.writeLog("打印请求HTML，此为请求报文，为联调排查问题的依据："+html);
        //将生成的html写到浏览器中完成自动跳转打开银联支付页面；这里调用signData之后，将html写到浏览器跳转到银联页面之前均不能对html中的表单项的名称和值进行修改，如果修改会导致验签不通过
//        response.getWriter().write(html);

        responseData.setEntity(html);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 支付交易的通知
     * @author: JiuDongDong
     * @param params 支付交易的通知信息
     * date: 2019/5/9 16:01
     */
    @Override
    public void receivePayNotify(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in UnionpayB2CWebWapManagerImpl.receivePayNotify, the params are: {}", JsonUtil.toJson(params));
        String encoding = params.get(param_encoding);
        // 验证签名前不要修改params中的键值对的内容，否则会验签不过
        if (!AcpService.validate(params, encoding)) {
            LogUtil.writeLog("验证签名结果[失败].");
            logger.error("verify signature failed!!! Response params are: " + JsonUtil.toJson(params));
            return;
            // todo 验签失败，需解决验签问题
        }
        // 响应码判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
        String respCode = params.get(param_respCode);
        // 响应信息
        String respMsg = params.get(param_respMsg);
        logger.info("respCode = {}, respMsg = {}", respCode, respMsg);
        String orderId = params.get(param_orderId);
        // 打印日志
        UnionPayLogUtil.logTradeInfo(responseData, respCode, respMsg, orderId);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("UnionPay handle this pay trade failed, now put to worker to query again! params = " + JsonUtil.toJson(params));
            sendMessageRedisService.putUnionPayTradeInfo2Redis(params, null);
            return;
        }
        /* 对于涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。*/
        // 组织查询参数
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(param_orderId, orderId);//订单号  TODO orderId 还是 queryId
        String txnTime = params.get(param_txnTime);//订单发送时间
        queryParams.put(param_txnTime, txnTime);//订单发送时间
        // 查询交易结果
        singleQuery(responseData, queryParams);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.warn("Single query pay result occurred exception, now query again!, orderId = {}", orderId);
            // 如果第一次没查询到，则进行第2次查询
            singleQuery(responseData, queryParams);
            entity = responseData.getEntity();
        }
        // 再次查询结果仍为空，放入Redis进行后续查询确认
        if (null == entity) {
            logger.error("Single query pay result occurred exception once again, now put to worker! orderId = {}", orderId);
            sendMessageRedisService.putUnionPayTradeInfo2Redis(params, null);
            return;
        }
        // 查询到结果，处理
        Map<String, String> rspData = (Map<String, String>) entity;
        logger.info("Result of single query {} is: {}", orderId, JsonUtil.toJson(rspData));
        // 查询交易成功，处理被查询交易的应答码逻辑
        String origRespCode = rspData.get(param_origRespCode);
        String origRespMsg = rspData.get(param_origRespMsg);
        // 00/A6:交易成功   03/04/05:订单处理中，需要稍后发起交易状态查询     其他:交易失败
        logger.info("The result of this unionPay of order {} is: origRespCode = {}, origRespMsg = {}", orderId, origRespCode, origRespMsg);

        // 交易失败
        if (RESPONSE_CODE_03.equals(origRespCode) || RESPONSE_CODE_04.equals(origRespCode) || RESPONSE_CODE_05.equals(origRespCode)) {
            logger.error("UnionPay is handling this pay trade, put it to redis to query ! orderId = " + orderId);
            // TODO 放入Redis，确认是否有必要
        } else if (RESPONSE_CODE_OK.equals(origRespCode) || RESPONSE_CODE_A6.equals(origRespCode)) {
            logger.info("UnionPay handle this pay trade success! orderId = {}", orderId);
            /* 支付成功，支付流水持久化到本地 */
            // 获取数据
            String version = params.get(param_version);//版本号  R
            String signature = params.get(param_signature);//签名  M
            String signMethod = params.get(param_signMethod);//签名方法  M
            String txnType = params.get(param_txnType);//交易类型
            String txnSubType = params.get(param_txnSubType);//交易子类
            String bizType = params.get(param_bizType);//产品类型
            String accessType = params.get(param_accessType);//接入类型
            String acqInsCode = params.get(param_acqInsCode);//收单机构代码  C 接入类型为收单机构接入时需返回
            String merId = params.get(param_merId);//商户代码
            String txnAmt = params.get(param_txnAmt);//交易金额
            String currencyCode = params.get(param_currencyCode);//交易币种  M 默认为 156
            String reqReserved = params.get(param_reqReserved);//请求方保留域  R
            String reserved = params.get(param_reserved);//保留域  O
            String queryId = params.get(param_queryId);//交易查询流水号，消费交易的流水号，供后续查询用
            String settleAmt = params.get(param_settleAmt);//清算金额
            String settleCurrencyCode = params.get(param_settleCurrencyCode);//清算币种
            String settleDate = params.get(param_settleDate);//清算日期
            String traceNo = params.get(param_traceNo);//系统跟踪号
            String traceTime = params.get(param_traceTime);//交易传输时间
            String exchangeDate = params.get(param_exchangeDate);//兑换日期，交易成功，交易币种和清算币种不一致的时候返回
            String exchangeRate = params.get(param_exchangeRate);//汇率，交易成功，交易币种和清算币种不一致的时候返回
            String accNo = params.get(param_accNo);//账号  C 根据商户配置返回
            String payCardType = params.get(param_payCardType);//支付卡类型  C 根据商户配置返回
            String payType = params.get(param_payType);//支付方式  C 根据商户配置返回
            String payCardNo = params.get(param_payCardNo);//支付卡标识  C 移动支付交易时，根据商户配置返回
            String payCardIssueName = params.get(param_payCardIssueName);//支付卡名称 C 移动支付交易时，根据商户配置返回
            String bindId = params.get(param_bindId);//绑定标识号 R 绑定支付时，根据商户配置返回
            String signPubKeyCert = params.get(param_signPubKeyCert);//签名公钥证书 C 使用 RSA 签名方式时必选，此域填写银联签名公钥证书。

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
            logger.info("Receive notify and serialize to merchant {} for orderId: {}", ifSuccess, orderId);
        } else {
            logger.error("UnionPay handle this pay trade failed! orderId = " + orderId);
        }
    }

    @Override
    public void receiveRefundNotify(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in UnionpayB2CWebWapManagerImpl.receiveRefundNotify, the params are: " + JsonUtil.toJson(params));
        String encoding = params.get(param_encoding);
        // 验证签名前不要修改params中的键值对的内容，否则会验签不过
        if (!AcpService.validate(params, encoding)) {
            LogUtil.writeLog("验证签名结果[失败].");
            logger.error("verify signature failed!!!.");
            logger.error("params are: " + JsonUtil.toJson(params));
            // todo 验签失败，需解决验签问题
        }
        // 响应码判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
        String respCode = params.get(param_respCode);
        // 响应信息
        String respMsg = params.get(param_respMsg);
        logger.info("respCode = {}, respMsg = {}", respCode, respMsg);
        // todo 对于涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
        // TODO 查询接口
        String version = params.get(param_version);//版本号  R
        String signature = params.get(param_signature);//签名  M
        String signMethod = params.get(param_signMethod);//签名方法  M
        String txnType = params.get(param_txnType);//交易类型
        String txnSubType = params.get(param_txnSubType);//交易子类
        String bizType = params.get(param_bizType);//产品类型
        String accessType = params.get(param_accessType);//接入类型
        String acqInsCode = params.get(param_acqInsCode);//收单机构代码  C 接入类型为收单机构接入时需返回
        String merId = params.get(param_merId);//商户代码
        String orderId = params.get(param_orderId);//商户订单号
        String txnTime = params.get(param_txnTime);//订单发送时间
        String txnAmt = params.get(param_txnAmt);//交易金额
        String currencyCode = params.get(param_currencyCode);//交易币种  M 默认为 156
        String reqReserved = params.get(param_reqReserved);//请求方保留域  R
        String reserved = params.get(param_reserved);//保留域  O
        String queryId = params.get(param_queryId);//交易查询流水号，消费交易的流水号，供后续查询用
        String settleAmt = params.get(param_settleAmt);//清算金额
        String settleCurrencyCode = params.get(param_settleCurrencyCode);//清算币种
        String settleDate = params.get(param_settleDate);//清算日期
        String traceNo = params.get(param_traceNo);//系统跟踪号
        String traceTime = params.get(param_traceTime);//交易传输时间
        String exchangeDate = params.get(param_exchangeDate);//兑换日期，交易成功，交易币种和清算币种不一致的时候返回
        String exchangeRate = params.get(param_exchangeRate);//汇率，交易成功，交易币种和清算币种不一致的时候返回
        String accNo = params.get(param_accNo);//账号  C 根据商户配置返回
        String payCardType = params.get(param_payCardType);//支付卡类型  C 根据商户配置返回
        String payType = params.get(param_payType);//支付方式  C 根据商户配置返回
        String payCardNo = params.get(param_payCardNo);//支付卡标识  C 移动支付交易时，根据商户配置返回
        String payCardIssueName = params.get(param_payCardIssueName);//支付卡名称 C 移动支付交易时，根据商户配置返回
        String bindId = params.get(param_bindId);//绑定标识号 R 绑定支付时，根据商户配置返回
        String signPubKeyCert = params.get(param_signPubKeyCert);//签名公钥证书 C 使用 RSA 签名方式时必选，此域填写银联签名公钥证书。

        LogUtil.writeLog("验证签名结果[成功].");




        /* 支付成功，支付流水持久化到本地 */
        // 封装数据
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
        logger.info("Receive notify and serialize to merchant {} for orderId: {}", ifSuccess, orderId);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 获取对账文件
     * @author: JiuDongDong
     * @param merId 商户号
     * @param settleDate 清算日期
     * date: 2019/5/8 11:24
     */
    @Override
    public void fileTransfer(ResponseData responseData, String merId, String settleDate) {
        logger.info("It is now in UnionpayB2CWebWapManagerImpl.fileTransfer, the param are: " +
                "[merId = {}, settleDate = {}]", merId, settleDate);
        Map<String, String> data = new HashMap<>();
        // 获取配置参数
        String version = sdkConfig.getVersion();
        String encoding = sdkConfig.getEncoding();
        String signMethod = sdkConfig.getSignMethod();
        String fileTransUrl = sdkConfig.getFileTransUrl();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put(param_version, version);               //版本号 全渠道默认值
        data.put(param_encoding, encoding);             //字符集编码 可以使用UTF-8,GBK两种方式
        data.put(param_signMethod, signMethod);         //签名方法
        data.put(param_txnType, "76");                  //交易类型 76-对账文件下载
        data.put(param_txnSubType, "01");               //交易子类型 01-对账文件下载
        data.put(param_bizType, "000000");              //业务类型，固定

        /***商户接入参数***/
        data.put(param_accessType, "0");                         //接入类型，商户接入填0，不需修改
        data.put(param_merId, merId);                	         //商户代码，请替换正式商户号测试，如使用的是自助化平台注册的777开头的商户号，该商户号没有权限测文件下载接口的，请使用测试参数里写的文件下载的商户号和日期测。如需777商户号的真实交易的对账文件，请使用自助化平台下载文件。
        data.put(param_settleDate, settleDate);                  //清算日期，如果使用正式商户号测试则要修改成自己想要获取对账文件的日期， 测试环境如果使用700000000000001商户号则固定填写0119
        data.put(param_txnTime, sdf.format(new Date()));         //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put(param_fileType, "00");                          //文件类型，一般商户填写00即可

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/

        Map<String, String> reqData = AcpService.sign(data, encoding);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = fileTransUrl;//获取请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.fileTransUrl
        Map<String, String> rspData =  AcpService.post(reqData, url, encoding);

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(MapUtils.isEmpty(rspData)){
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
            logger.error("The response data is empty for fileTransfer, merId = " + merId + ", settleDate = " + settleDate);
            // 未获取到返回报文或返回http状态码非200，请稍后重试
            responseData.setCode(ResponseStatus.UNIONCONNECTEX.getValue());
            responseData.setMsg(ResponseStatus.UNIONCONNECTEX.name());
            return;
        }
        // 验签
        boolean validate = AcpService.validate(rspData, encoding);
        if (!validate) {
            logger.error("Verify signature for fileTransfer response not pass!!! merId = " + merId + ", settleDate = " + settleDate);
            // 校验中国银联签名异常
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());
            responseData.setMsg(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.name());
            return;
        }
        LogUtil.writeLog("验证签名成功");
        String fileContentDispaly = "";
        // 应答码
        String respCode = rspData.get(param_respCode);
        if (!RESPONSE_CODE_OK.equals(respCode)) {
            logger.error("UnionPay handle file transfer failed, merId = " + merId + ", settleDate = " + settleDate);
            responseData.setCode(ResponseStatus.UNIONRESPCODEERROR.getValue());//中国银联响应码异常
            responseData.setMsg(ResponseStatus.UNIONRESPCODEERROR.name());
            return;
        }

        // TODO 暂放 D 盘
        String outPutDirectory = "d:\\";
        // 交易成功，解析返回报文中的fileContent并落地
        String zipFilePath = AcpService.deCodeFileContent(rspData, outPutDirectory, encoding);
        //对落地的zip文件解压缩并解析
        List<String> fileList = DemoBase.unzip(zipFilePath, outPutDirectory);
        //解析ZM，ZME文件
        fileContentDispaly = "<br>获取到商户对账文件，并落地到" + outPutDirectory + ",并解压缩 <br>";
        for (String file : fileList) {
            if (file.indexOf("ZM_") != -1) {
                List<Map> ZmDataList = DemoBase.parseZMFile(file);
                fileContentDispaly = fileContentDispaly + DemoBase.getFileContentTable(ZmDataList, file);
            } else if (file.indexOf("ZME_") != -1) {
                DemoBase.parseZMEFile(file);
            }
        }
        // TODO 文件已下载，后续怎么处理
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        responseData.setEntity("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+fileContentDispaly);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 单笔交易查询。订单号、订单交易时间必传。
     *              订单支付交易查询，订单号为与银联交易的加E加R的订单号。
     *              TODO 如果想查消费撤销/退货是否成功，需要用消费撤销和退货的orderId和txnTime来查。
     * @author: JiuDongDong
     * date: 2019/5/23 17:14
     */
    @Override
    public void singleQuery(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in UnionpayB2CWebWapManagerImpl.singleQuery, the params are: {}", JsonUtil.toJson(params));
        // 请求参数
        String orderId = params.get(param_orderId);
        String txnTime = params.get(param_txnTime);
        // 从配置文件获取请求参数
        String version = sdkConfig.getVersion();
        String encoding = sdkConfig.getEncoding();
        String signMethod = sdkConfig.getSignMethod();
        String singleQueryUrl = sdkConfig.getSingleQueryUrl();
        // TODO merId 区分自营还是非自营
        String merId = sdkConfig.getMerId();//TODO 上线多个商户，处理商户id

        /* 组织请求参数 */
        Map<String, String> data = new HashMap<>();
        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put(param_version, version);//版本号
        data.put(param_encoding, encoding);//字符集编码 可以使用UTF-8,GBK两种方式
        data.put(param_signMethod, signMethod);//签名方法
        data.put(param_txnType, "00");//交易类型 00-默认
        data.put(param_txnSubType, "00");//交易子类型  默认00
        data.put(param_bizType, "000201");//业务类型 B2C网关支付，手机wap支付
        // 商户接入参数
        data.put(param_merId, merId);//TODO 商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put(param_accessType, "0");//TODO 这个待银联反馈。接入类型，商户接入固定填0，不需修改

        /***要调通交易以下字段必须修改***/
        data.put(param_orderId, orderId);                   //商户订单号，每次发交易测试需修改为被查询的交易的订单号
        data.put(param_txnTime, txnTime);                   //订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间

        // 签名
        Map<String, String> reqData = AcpService.sign(data, encoding);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        //这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String, String> rspData = AcpService.post(reqData, singleQueryUrl, encoding);

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(MapUtils.isEmpty(rspData)){
            logger.error("The response data is empty for single query, orderId = " + orderId + ", txnTime = " + txnTime);
            responseData.setCode(ResponseStatus.UNIONCONNECTEX.getValue());//未获取到返回报文或返回http状态码非200
            responseData.setMsg(ResponseStatus.UNIONCONNECTEX.name());
            return;
        }
        // 验签
        boolean validate = AcpService.validate(rspData, encoding);
        if (!validate) {
            logger.error("Verify signature for single query response not pass!!! orderId = " + orderId);
            responseData.setEntity(reqData);
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());//验证签名失败
            responseData.setMsg(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.name());
            return;
        }
        LogUtil.writeLog("验证签名成功");
        logger.info("The response data of single query of orderId: {} is: {}", orderId, JsonUtil.toJson(rspData));
        //应答码
        String respCode = rspData.get(param_respCode);
        if (!RESPONSE_CODE_OK.equals(respCode)) {
            logger.error("UnionPay handle failed, orderId = " + orderId + ", respCode = " + respCode);
            responseData.setEntity(reqData);
            responseData.setCode(ResponseStatus.UNIONRESPCODEERROR.getValue());
            responseData.setMsg(ResponseStatus.UNIONRESPCODEERROR.name());
            return;
        }
        // 查询交易成功，处理被查询交易的应答码逻辑
        String origRespCode = rspData.get(param_origRespCode);
        if (RESPONSE_CODE_OK.equals(origRespCode)) {
            //交易成功，更新商户订单状态
            //TODO
        } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)) {
            //需再次发起交易状态查询交易
            //TODO
        } else {
            //其他应答码为失败请排查原因
            //TODO
        }
        logger.info("origRespCode of orderId {} is: {}", orderId, origRespCode);
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        responseData.setEntity(rspData);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

}
