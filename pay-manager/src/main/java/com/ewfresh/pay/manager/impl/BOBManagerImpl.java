package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.configure.BOBPayConfigure;
import com.ewfresh.pay.manager.BOBManager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.BillFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.BOBAccountVo;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.BOBOrderRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.BillFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.BOBLoggerByResCodeAndTransType;
import com.ewfresh.pay.util.bob.BOBOrderNoFormat;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.bob.bobuponline.HttpClient;
import com.ewfresh.pay.util.bob.bobutil.BOBSdkUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * description:
 *      BOB的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/4/20.
 */
@Component
public class BOBManagerImpl implements BOBManager {
    @Autowired
    private BOBPayConfigure bobPayConfigure;
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private BillFlowService billFlowService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BOBOrderRedisService bobOrderRedisService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String BOB_WANGYIN = "北京银行网银";
    private static final String YINLIAN_BOB = "银联（北京银行）";
    private static final Integer INTERACTION_ID_LENGTH = 19;//第三方交互订单支付订单号长度19位

    /**
     * Description: 订单支付
     * @author: JiuDongDong
     * @param params  前端发送的订单查询参数
     * date: 2018/4/22 13:56
     */
    @Override
    public void sendOrder(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.sendOrder, the parameters are: " + JsonUtil.toJson(params));
        /* 1. 组装订单支付请求数据 */
        // 1.1 获取部分配置信息
        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
        String actionUrl = bobPayConfigure.getPayUrl();// 支付网关
        String frontEndUrl = bobPayConfigure.getFrontEndUrl();// 前台回调地址
        String frontFailUrl = bobPayConfigure.getFrontFailUrl();// 前台回调地址
        String backEndUrl = bobPayConfigure.getBackEndUrl();// 商户支付、退款交易结果后台通知地址
        String merId = bobPayConfigure.getMerId();// 商户号
        String orderNumber = params.get(Constants.BOB_ORDER_NUMBER);// 转化过后的19位订单号或没有经过转化的充值订单号
        String orderDesc = params.get(Constants.BOB_ORDER_DESC);// 订单描述

        // 1.2 补充订单支付的请求参数
        params.put(Constants.BOB_SIGN_METHOD, Constants.SIGN_METHOD_RSA);// 默认RSA 取值01
        params.put(Constants.BOB_ENCODING, Constants.UTF_8);// 编码 UTF-8
        params.put(Constants.BOB_TRANS_TYPE, Constants.BOB_TRANS_TYPE01);// 消费
        Date sendOrder2BOBTime = new Date();
        String sendOrder2BOBTimeStr = sdf.format(sendOrder2BOBTime);
        params.put(Constants.BOB_ORDER_TIME, sendOrder2BOBTimeStr);// 商户发送交易时间

        params.put(Constants.BOB_MER_TYPE, Constants.BOB_MER_TYPE_ZERO);// 商户类型
        params.put(Constants.BOB_FRONT_END_URL, frontEndUrl);// 前台回调地址 交易完毕返回按钮使用
        params.put(Constants.BOB_FRONT_FAIL_URL, frontFailUrl);// 前台失败回调地址
        params.put(Constants.BOB_BACK_END_URL, backEndUrl);// 后台通知地址，接收应答报文地址
        params.put(Constants.BOB_MER_ID, merId);// 商户号
//        params.put(Constants.BOB_ORDER_DESC, Constants.BUY_GOODS); // 订单描述，购买商品
        // 订单支付请求时间放入Redis
        bobOrderRedisService.putSendOrderToBOBTimeToRedis(orderNumber, sendOrder2BOBTimeStr);
        // 订单描述放入Redis
        bobOrderRedisService.putOrderDesc2Redis(orderNumber, orderDesc);
        // 下面这3个参数暂时不需要
        // params.put(Constants.BOB_FRONT_FAIL_URL, ???)// 失败交易前台跳转地址
        // params.put(Constants.BOB_FRONT_FAIL_URL, null);// 交易失败跳转页面 C
        // params.put("shippingFlag", shippingFlag);// 物流标识

        /* 2. 签名 */
        logger.info("Now going to signData for sendOrder");
        Map<String,String> signMap = BOBSdkUtil.sign(params, merchantCertPath, merchantCertPss);
        logger.info("signData ok for sendOrder[" + signMap.toString() + "]");

        /* 3. 向银行发送订单支付请求，获取页面，响应回client*/
        HttpClient httpClient = new HttpClient(actionUrl, Constants.INTEGER_20000, Constants.INTEGER_20000);
        Integer responseCode;//响应状态码
        String result;//响应信息
        try {
            responseCode = httpClient.send(signMap, Constants.UTF_8);
        } catch (Exception e) {
            logger.error("Error occurred when send order pay to BOB1", e);
            responseData.setCode(ResponseStatus.SENDORDERTOBOBERROR.getValue());
            responseData.setMsg(ResponseStatus.SENDORDERTOBOBERROR.name());
            return;
        }
        if (Constants.HTTP_STATUS_OK.intValue() != responseCode) {
            // 响应状态码非200，向浏览器输出响应
            logger.error("Error occurred when send order pay to BOB2");
            responseData.setCode(ResponseStatus.SENDORDERTOBOBERROR.getValue());
            responseData.setMsg(ResponseStatus.SENDORDERTOBOBERROR.name());
            return;
        }
        if (Constants.HTTP_STATUS_OK.intValue() == responseCode) {
            // 响应状态码200，获取响应信息，输出响应
            logger.info("Send order to BOB and response to ewfresh OK");
            result = httpClient.getResult();
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            responseData.setEntity(result);
        }

//        /* 3. 生成支付页面并相应 */
//        String sendOrderHtml = BOBSdkUtil.createHtml(actionUrl, signMap);
//        logger.info("The send order html: " + sendOrderHtml);
//        responseData.setEntity(sendOrderHtml);
//        logger.info("The send order parameters have been packaged ok and will send them back to customer HTML");
    }

    /**
     * Description: 支付、退款应答
     * @author: JiuDongDong
     * @param params  BOB返回的订单支付结果信息
     * @return java.lang.Integer 0成功 1验签失败 2业务处理失败 3BOB业务处理失败
     * date: 2018/4/21 10:44
     */
    @Override
    public Integer receiveNotify(Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.receiveNotify, the received parameters are: " + JsonUtil.toJson(params));
        /* 1. 验签 */
        String merchantPubPath = bobPayConfigure.getMerchantPubPath();// 公钥路径
//        String merchantPubPath = "E:\\sunkfa\\ewfresh-pay\\ewfresh-pay\\pay-commons\\src\\main\\resources\\bobproperties\\0518.cer";// 公钥路径
        boolean result = BOBSdkUtil.validate(params, merchantPubPath);
        logger.info("Verify data of order pay or refund notify：" + result);
        // 验签失败返回状态码1
        if (!result) {
//            logger.error("Verify data of order pay or refund notify failed");
            return Constants.INTEGER_ONE;
        }
//        logger.info("Verify data of order pay or refund notify ok");

        /* 2. 验签成功, 再根据状态码判断是否支付成功 */
        // 2.1 获取状态码和交易类型
        String resCode = params.get(Constants.BOB_RES_CODE);// 状态码
        String transType = params.get(Constants.BOB_TRANS_TYPE);// 交易类型
        BOBLoggerByResCodeAndTransType.logInfo(resCode, transType);
        // 2.2 BOB处理失败，返回
        if (!Constants.BOB_CODE_SUCCESS.equals(resCode)) {
            logger.error("BOB order pay or refund handle failed with resCode = {}", resCode);
            return Constants.INTEGER_THREE;// BOB业务处理失败
        }
        String resDesc = params.get(Constants.BOB_RES_DESC);
        logger.info("The resDesc of receiveNotify = {}", resDesc);

        /* 3. 支付成功，获取参数并处理业务 */
        // 3.1 从应答报文获取信息
        String orderNo = params.get(Constants.BOB_ORDER_NUMBER);//订单号(19位的原支付交易订单号或非固定位数的充值订单号)
        String merchantNo = params.get(Constants.BOB_MER_ID);//商户号
        String channelType = params.get(Constants.BOB_CHANNEL_TYPE);//渠道类型 07：互联网； 08：移动
        String orderTime = params.get(Constants.BOB_ORDER_TIME);//支付订单时，商户生成的支付请求时间; 退款时，退款申请时间
        String payAmount = params.get(Constants.BOB_ORDER_AMOUNT);//付款方支付金额 / 退款金额，单位为分

        // 3.2 对支付或退款的应答信息持久化到本地
        // 3.2.1 支付交易应答信息持久化到本地
        if (Constants.BOB_TRANS_TYPE01.equals(transType)) {
            logger.info("This is a order pay trade in receiveNotify for orderNo：" + orderNo);
            String orderSeq = params.get(Constants.BOB_QUERY_ID);//查询流水号
            // 3.2.1.1 封装本地持久化数据
            Map<String, Object> param = new HashMap<>();
            param.put(Constants.CHANNEL_FLOW_ID, orderSeq);//支付渠道流水号
            param.put(Constants.PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(payAmount));//付款方支付金额
            param.put(Constants.RECEIVER_USER_ID, merchantNo);//收款人ID（商户号）
//            param.put(Constants.SUCCESS_TIME, orderTime);//支付订单时，商户生成的支付请求时间
            param.put(Constants.IS_REFUND, Constants.IS_REFUND_NO + "");//是否退款 0:否,1是
            param.put(Constants.RETURN_INFO, null);//返回信息
//            param.put(Constants.DESP, Constants.BUY_GOODS);//描述
            param.put(Constants.UID, Constants.UID_BOB);//操作人标识
            param.put(Constants.BOB_CHANNEL_TYPE, channelType);//渠道类型 07：互联网； 08：移动
            param.put(Constants.PAYER_ID, "123");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
            // 订单描述
            String orderDesc = bobOrderRedisService.getOrderDescByOrderNo(orderNo);
            logger.info("The orderDesc is : " + orderDesc + " of orderNo: " + orderNo);
            orderDesc = StringUtils.isBlank(orderDesc) ? Constants.BUY_GOODS_OR_RECHARGE : orderDesc;
            param.put(Constants.DESP, orderDesc);
            bobOrderRedisService.deleteOrderDescFromRedis(orderNo);
            // 交易类型：  1:订单,2,退款,3,线下充值,4线上充值,5:提现,6:商户结算打款,7:平台增值服务收款
            Short tradeType = orderDesc.equals(Constants.RECHARGE) ? Constants.TRADE_TYPE_4 : Constants.TRADE_TYPE_1;
            param.put(Constants.TRADE_TYPE, tradeType);//交易类型
//            Map<String, String> redisParam = orderRedisService.getPayOrder(BOBOrderNoFormat.bob20OrderNoToOriWithER(param.get(Constants.INTERACTION_ID).toString()));
            // 订单号：如果是订单支付，则由19位还原为20位（数据库保存的是加E加R的20位订单号）；如果是充值，不做改动
            String selectOrderNo = orderDesc.equals(Constants.RECHARGE) ? orderNo : BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo);
//            param.put(Constants.INTERACTION_ID, BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo));//订单号
            param.put(Constants.INTERACTION_ID, selectOrderNo);//订单号
            Map<String, String> redisParam = orderRedisService.getPayOrder(selectOrderNo);
            if (redisParam.isEmpty()) {
                logger.warn("the redisParam is null---->");
                logger.info("return false!!!!!!!!!!!!");
                return Constants.INTEGER_TWO;
            }
            String payMode = redisParam.get(Constants.PAY_MODE);
            logger.info("redisParam: " + JsonUtil.toJson(redisParam));
            logger.info("payMode = " + payMode + " for orderNo: " + orderNo);
            // 获取type_name和type_code
            // 区分银联还是北京银行
            if (StringUtils.isNotBlank(payMode) && payMode.contains(BOB_WANGYIN)) {
                param.put(Constants.PAY_CHANNEL, Constants.INTEGER_FOUR + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）
                param.put(Constants.TYPE_NAME, BOB_WANGYIN);
                param.put(Constants.TYPE_CODE, Constants.INTEGER_FOUR + "");
                // 请求时间
                param.put(Constants.SUCCESS_TIME, bobOrderRedisService.getSendOrderToBOBTimeFromRedis(orderNo));//支付订单时，商户生成的支付请求时间，这个时间取Redis中存的请求时间，北京银行网银返回的时间是错的，没卵用
            }
            if (StringUtils.isNotBlank(payMode) && payMode.contains(YINLIAN_BOB)) {
                param.put(Constants.PAY_CHANNEL, Constants.INTEGER_FIVE + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）
                param.put(Constants.TYPE_NAME, YINLIAN_BOB);
                param.put(Constants.TYPE_CODE, Constants.INTEGER_FIVE + "");
                // 请求时间
                param.put(Constants.SUCCESS_TIME, orderTime);//支付订单时，商户生成的支付请求时间，这个是银联返回的订单支付请求时间
            }

            // 计算手续费及平台收入
            param = CalMoneyByFate.calMoneyByFate(param);
            if (param == null) {
                logger.error("CalMoneyByFate error for BOB");
                return Constants.INTEGER_TWO;
            }
            // 3.2.1.2 订单支付信息持久化到本地
            boolean ifSuccess = commonsManager.ifSuccess(param);
            if (!ifSuccess) {
                logger.error("Serialize order notify info to local occurred error for BOB");
                return Constants.INTEGER_TWO;
            }
            // 3.2.1.3 订单信息持久化成功之后，把Redis中存储的订单支付申请时间删除
            bobOrderRedisService.deleteSendOrderToBOBTimeInRedis(orderNo);
        }
        // 3.2.2 退款交易应答信息持久化到本地
        if (Constants.BOB_TRANS_TYPE04.equals(transType) || Constants.BOB_TRANS_TYPE35.equals(transType) || Constants.BOB_TRANS_TYPE36.equals(transType)) {
            logger.info("This is a refund trade in receiveNotify for orderNo：" + orderNo + ", the transType = " + transType);
            String refundSeq = params.get(Constants.BOB_REFUND_SEQ);//退款订单生成的商户侧流水号
            String orderAmount = params.get(Constants.BOB_ORDER_AMOUNT);//退款金额，单位为分
            String queryId = params.get(Constants.BOB_QUERY_ID);//查询流水号, 用于后续查询该笔退款交易
            logger.info("refundSeq = {}, orderAmount = {}, queryId = {}", refundSeq, orderAmount, queryId);
            // 根据第三方交互订单号查询流水信息
            PayFlow payFlow = payFlowService.getPayFlowByInteractionId(BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo));
            if (payFlow == null) {
                logger.error("There is no payFlow of this orderNo: " + orderNo);
                return Constants.INTEGER_TWO;
            }
            // 设置支付流水信息
            payFlow.setChannelFlowId(queryId);//支付渠道流水号
            payFlow.setPayerPayAmount(FenYuanConvert.fen2Yuan(orderAmount));//退款金额，单位为分 分转换为元
            payFlow.setTradeType(Constants.TRADE_TYPE_2);//交易类型：退款
            payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
            payFlow.setDesp(Constants.REFUND_ORDER);//订单描述：退款
            payFlow.setReturnFlowId(refundSeq);//退款流水号
            // 收款人id和付款人id对调、收款人名称和付款人名称对调
            String receiverName = payFlow.getReceiverName();//收款人名称
            String payerId = payFlow.getPayerId();//付款人id
            String receiverUserId = payFlow.getReceiverUserId();//收款人id
            String payerName = payFlow.getPayerName();//付款人名称
            payFlow.setPayerId(receiverUserId);
            payFlow.setReceiverUserId(payerId);
            payFlow.setPayerName(receiverName);
            payFlow.setReceiverName(payerName);
            // 支付成功时间
            try {
                logger.info("Try parse orderTime when handle refund param, the orderTime = {}", orderTime);
                payFlow.setSuccessTime(sdf.parse(orderTime));
            } catch (ParseException e) {
                // 事实上，这里catch异常只是语法要求，银行处理成功后，时间格式不会有误，假定这里发生了异常，也仅仅是时间解析异常，银行退款让是成功的，所以继续处理后续业务
                logger.error("Parse orderTime occurred error, the orderTime = {}", orderTime);
                payFlow.setSuccessTime(null);
            }
            // 是否退款
            payFlow.setIsRefund(Constants.SHORT_ONE);
            logger.info("Refund order success, the orderNo = {}", orderNo);
            // 创建时间
            payFlow.setCreateTime(null);
            // 持久化退款流水
            payFlowService.addPayFlow(payFlow);
            logger.info("Refund for order ok: " + orderNo);
            // 持久化退款流水ok之后，把退款信息从Redis中删除
            String interactionId = payFlow.getInteractionId();// 订单号：20位的加E加R的订单支付订单号
            bobOrderRedisService.deleteRefundOrderInfoInRedis(interactionId);
            return Constants.INTEGER_ZERO;
        }

        // 持久化成功后，向北京银行响应
        logger.info("Receive notify and serialize to merchant ok");
        return Constants.INTEGER_ZERO;
    }


    /**
     * Description: 商户发送退款请求（按最新设计方案，此功能由这个类替换：com.ewfresh.pay.policy.impl.BOBRefundPolicy）
     * @author: JiuDongDong
     * @param params  退款参数
     * date: 2018/4/21 17:59
     */
    @Override
    public void refundOrder(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.refundOrder, the parameters are: " + JsonUtil.toJson(params));
        // 获取部分配置信息
        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
        String actionUrl = bobPayConfigure.getRefundUrl();// 退款网关
        String backEndUrl = bobPayConfigure.getBackEndUrl();// 后台通知地址，用于接收交易结果
        String orderNumber = params.get(Constants.ORDER_NO);//商户订单号，原支付交易订单号（父订单号）
        String merId = bobPayConfigure.getMerId();// 商户号

        // 1、补充退款的请求参数
        params.put(Constants.BOB_SIGN_METHOD, Constants.SIGN_METHOD_RSA);// 默认RSA 取值01
        params.put(Constants.BOB_ENCODING, Constants.UTF_8);// 编码
        params.put(Constants.BOB_TRANS_TYPE, Constants.BOB_TRANS_TYPE04);// 01-消费 04-退款
        params.put(Constants.BOB_ORDER_TIME, sdf.format(new Date()));//商户发送交易时间yyyyMMddHHmmss
        params.put(Constants.BOB_MER_TYPE, Constants.BOB_MER_TYPE_ZERO);// 商户类型
        params.put(Constants.BOB_BACK_END_URL, backEndUrl);//后台通知地址，接收应答报文地址
        params.put(Constants.BOB_ORDER_DESC, Constants.REFUND_ORDER); // 订单描述，退款
//        params.put(Constants.BOB_QUERY_ID, tradeNo + "");// 原消费流水号
        params.put(Constants.BOB_MER_ID, merId);// 商户号

        // 签名
        logger.info("Now going to signData for refundOrder");
        Map<String,String> signMap = BOBSdkUtil.sign(params, merchantCertPath, merchantCertPss);
        logger.info("signData ok for refundOrder[" + signMap.toString() + "]");

        // 生成退款页面
        String refundOrderHtml = BOBSdkUtil.createHtml(actionUrl, signMap);
        logger.info("The refund order html: " + refundOrderHtml);
        responseData.setEntity(refundOrderHtml);
        logger.info("The refund order parameters have been packaged ok and will send them back to BOB");
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 单笔交易查询
     * @author: JiuDongDong
     * @param params 前端发送的订单查询参数
     * date: 2018/4/22 13:54
     */
    @Override
    public void singleQuery(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.singleQuery, the parameters are: " + JsonUtil.toJson(params));
//        if (null != tradeTime) logger.info("The tradeTime = " + sdf.format(tradeTime));
        /* 1. 组装请求参数 */
        // 1.1 获取部分配置信息
        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
        String merchantPubPath = bobPayConfigure.getMerchantPubPath();// 公钥路径
        String actionUrl = bobPayConfigure.getSingleUrl();// 交易状态查询网关
        String merId = bobPayConfigure.getMerId();// 商户号
//        String orderNumber = params.get(Constants.BOB_ORDER_NUMBER);// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号，转化过后的19位订单号)
        String orderNumber = params.get(Constants.BOB_ORDER_NUMBER);// 转化过后的19位订单号或没有经过转化的充值订单号
        // 1.2 补充订单支付的请求参数
        params.put(Constants.BOB_SIGN_METHOD, Constants.SIGN_METHOD_RSA);// 默认RSA 取值01
        params.put(Constants.BOB_ENCODING, Constants.UTF_8);// 编码 UTF-8
        params.put(Constants.BOB_MER_ID, merId);// 商户号
        // 1.2.1 获取订单交易时间
        Date tradeTime = null;
        String transType = params.get(Constants.BOB_TRANS_TYPE);
        if (transType.equals(Constants.BOB_TRANS_TYPE01)) {
            // 1.2.1.1 订单支付交易的申请时间获取
            tradeTime = getSendOrderToBOBTimeFromRedis(orderNumber);
            logger.info("The orderPay tradeTime is: " + tradeTime + " for orderNumber: " + orderNumber);
        }
        // 1.2.1.2 订单退款的请求时间和退款流水号的获取
        if (transType.equals(Constants.BOB_TRANS_TYPE04) || transType.equals(Constants.BOB_TRANS_TYPE35) || transType.equals(Constants.BOB_TRANS_TYPE36)) {
            tradeTime = getRefundOrderToBOBTimeFromRedis(orderNumber);
            logger.info("The refund tradeTime is: " + tradeTime + " for orderNumber: " + orderNumber);
        }
        if (tradeTime == null) {
            logger.error("There is no successTime for orderNumber: " + orderNumber);
            responseData.setCode(ResponseStatus.SUCCESSTIMENULL.getValue());
            responseData.setMsg(ResponseStatus.SUCCESSTIMENULL.name());
            return;
        }
        logger.info("The tradeTime = {}", tradeTime);
        params.put(Constants.BOB_ORDER_TIME, sdf.format(tradeTime));
//        try {
//            // 1.3.1 查询退款的订单（BOBRefundPolicy里退款时，直接调取次方法时，这个时间封装在参数里传递过来）
//            String refundTime = params.get(Constants.BOB_ORDER_TIME);// 商户发送交易时间（指商户发送订单支付时间或商户发送退单请求时间）
//            if (StringUtils.isNotBlank(refundTime)) {
//                successTime = sdf.parse(refundTime);
//            }
//        } catch (ParseException e) {
//            logger.error("Try to parse tradeTime from refundOrder occurred error of orderNumber: " + orderNumber);
//            responseData.setCode(ResponseStatus.ERR.getValue());
//            responseData.setMsg(ResponseStatus.ERR.name());
//            return;
//        }
//        String refundTime = params.get(Constants.BOB_ORDER_TIME);// BOBRefundPolicy传过来的发起退单的时间
//        Date successTime = payFlowService.getSuccessTimeByInteractionId(BOBOrderNoFormat.bob19OrderNo2OriWithER(orderNumber));

//        if (StringUtils.isBlank(params.get(Constants.BOB_ORDER_TIME))) {
//            params.put(Constants.BOB_ORDER_TIME, sdf.format(successTime));// 商户发送交易时间（指商户发送订单支付时间或商户发送退单请求时间）
//        }
        // 1.2.2 获取退款流水号
        String refundSeq = null;
        if (transType.equals(Constants.BOB_TRANS_TYPE04) || transType.equals(Constants.BOB_TRANS_TYPE35) || transType.equals(Constants.BOB_TRANS_TYPE36)) {
            refundSeq = getRefundSeqFromRedis(orderNumber);
        }
        if (StringUtils.isNotBlank(refundSeq)) {
            params.put(Constants.BOB_REFUND_SEQ, refundSeq);
        }

        /* 2. 签名 */
        logger.info("Now going to signData for singleQuery");
        Map<String, String> signMap = BOBSdkUtil.sign(params, merchantCertPath, merchantCertPss);
        logger.info("signData ok for singleQuery[" + signMap.toString() + "]");

        /* 3. 向BOB发起查询请求并根据相应状态进行相应处理 */
        logger.info("The singleQuery parameters have been packaged ok and will send them to BOB");
        HttpClient httpClient = new HttpClient(actionUrl, Constants.INTEGER_20000, Constants.INTEGER_20000);
        Integer status;
        try {
            status = httpClient.send(signMap, Constants.UTF_8);
        } catch (Exception e) {
            logger.error("Connection is timeout for singleQuery: " + orderNumber, e);
            responseData.setCode(ResponseStatus.CONNECTIONTIMEOUT.getValue());
            responseData.setMsg(ResponseStatus.CONNECTIONTIMEOUT.name());
            return;
        }
        if (HttpServletResponse.SC_OK != status.intValue()) {
            logger.error("SingleQuery http to BOB failed, status = {}", status);
            responseData.setCode(ResponseStatus.HTTPTOBANKFAILED.getValue());
            responseData.setMsg(ResponseStatus.HTTPTOBANKFAILED.name());
            return;
        }
        logger.info("The status of httpClient BOB response to merchant: " + status);
        String res = httpClient.getResult();
        logger.info("The response data BOB response to merchant: " + res);

        /* 4. 处理BOB响应数据 */
        // 4.1 将订单查询响应结果key=value字符串转换为map集合
        Map<String, String> singleQueryResponseData = BOBSdkUtil.convertResultStringToMap(res);
        // 4.2 判断交易是否成功
        String resCodeRes = singleQueryResponseData.get(Constants.BOB_RES_CODE);//应答码  见“交易返回码相关说明”章节
        BOBLoggerByResCodeAndTransType.logInfo(resCodeRes, null);
        //  如果银行端处理失败(此时没有签名)，直接返回
        if (!Constants.BOB_CODE_SUCCESS.equals(resCodeRes)) {
            logger.error("SingleQuery failed with resCode = {}", resCodeRes);
            responseData.setEntity(singleQueryResponseData);
            responseData.setCode(ResponseStatus.BOBHANDLEFAIL.getValue());
            responseData.setMsg(ResponseStatus.BOBHANDLEFAIL.name());
            return;
        }
        String resDesc = singleQueryResponseData.get(Constants.BOB_RES_DESC);
        logger.info("The resDesc of singleQuery = {}", resDesc);
        // 4.3 同步应答验签, 验签不成功则来源数据不明
        boolean result = BOBSdkUtil.validate(singleQueryResponseData, merchantPubPath);
        if (!result) {
            String signature = singleQueryResponseData.get(Constants.BOB_SIGNATURE);// BOB签名域
            logger.error("Verify signData of BOB failed, care of the signData: " + signature);
            responseData.setCode(ResponseStatus.SIGNDATAERROR.getValue());
            responseData.setMsg(ResponseStatus.SIGNDATAERROR.name());
            return;
        }

        // TODO 当前，貌似并不需要将这些参数拿出来处理 START
        String merIdRes = singleQueryResponseData.get(Constants.BOB_MER_ID);// 商户号
        String orderNumberRes = singleQueryResponseData.get(Constants.BOB_ORDER_NUMBER);// 订单号  商户原支付订单号
        String orderTimeRes = singleQueryResponseData.get(Constants.BOB_ORDER_TIME);// 商户订单时间  查询订单交易时间
        String refundSeqRes = singleQueryResponseData.get(Constants.BOB_REFUND_SEQ);// 商户退款流水号 退款交易时出现
        String orderAmountRes = singleQueryResponseData.get(Constants.BOB_ORDER_AMOUNT);//订单金额  分为单位
        String queryIdRes = singleQueryResponseData.get(Constants.BOB_QUERY_ID);//查询流水号  原支付、退款返回的查询流水号
        // TODO 当前，貌似并不需要将这些参数拿出来处理 END

        // 4.4 交易成功，则将BOB响应信息返回至html
        logger.info("SingleQuery ok with resCode = {}", resCodeRes);
        responseData.setEntity(singleQueryResponseData);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 对账单查询
     * @author: JiuDongDong
     * @param params 对账单查询的参数，包括商户号和日期
     * date: 2018/4/22 15:23
     */
    @Override
    public void orderAccount(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.orderAccount, the parameters are: " + JsonUtil.toJson(params));
        /* 1. 封装请求参数 */
        // 1.1 获取部分配置信息
        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
        String actionUrl = bobPayConfigure.getOrderAccountUrl();// 北京银行对账单查询地址
        String backEndUrl = bobPayConfigure.getOrderAccUrl();// 对账单结果返回地址
        String merId = bobPayConfigure.getMerId();// 商户号
        String date = params.get(Constants.BOB_DATE);// 对账的日期
        // 1.2 补充订单支付的请求参数
        params.put(Constants.BOB_SIGN_METHOD, Constants.SIGN_METHOD_RSA);// 默认RSA 取值01
        params.put(Constants.BOB_ENCODING, Constants.UTF_8);// 编码 UTF-8
        params.put(Constants.BOB_BACK_END_URL, backEndUrl);// 对账单结果返回地址
        params.put(Constants.BOB_MER_ID, merId);// 商户号

        /* 2. 签名 */
        logger.info("Now going to signData for orderAccount");
        Map<String,String> signMap = BOBSdkUtil.sign(params, merchantCertPath, merchantCertPss);
        logger.info("signData ok for orderAccount[" + signMap.toString() + "]");

        /* 3. 向BOB发起查询请求 */
        logger.info("The orderAccount parameters have been packaged ok and will send them to BOB");
        HttpClient httpClient = new HttpClient(actionUrl, Constants.INTEGER_20000, Constants.INTEGER_20000);
        Integer status;
        String res;
        try {
            status = httpClient.send(signMap, Constants.UTF_8);
            res = httpClient.getResult();
            logger.info("The result of orderAccount is: status = {}, res = {}", status, res);
        } catch (Exception e) {
            logger.error("Error occurred when http to BOB", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        if (HttpServletResponse.SC_OK != status.intValue()) {
            logger.error("OrderAccount http to BOB occurred error");
            responseData.setCode(ResponseStatus.HTTPTOBANKFAILED.getValue());
            responseData.setMsg(ResponseStatus.HTTPTOBANKFAILED.name());
            return;
        }
        logger.info("OrderAccount from BOB ok for date: " + date);
        responseData.setEntity(res);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }
//
//    /**
//     * Description: 对账单查询应答
//     * @author: JiuDongDong
//     * @param params  BOB返回的对账信息
//     * @return java.lang.Integer  0成功 1验签失败 2业务处理失败
//     * date: 2018/7/27 16:56
//     */
//    @Override
//    public Integer receiveAccount(Map<String, String> params) {
//        logger.info("It is now in BOBManagerImpl.receiveAccount, the parameters are: " + JsonUtil.toJson(params));
//        /* 1. 解密 */
//        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
//        String merchantPubPath = bobPayConfigure.getMerchantPubPath();// 公钥路径
//        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
//        String encoding = params.get(Constants.BOB_ENCODING);
//        String sign = BOBSdkUtil.dataDeciphering(merchantCertPath, merchantCertPss, Constants.BOB_SIGNATURE, encoding);
//        logger.info("sign: " + sign);
//        String[] res = sign.split("&");
//        logger.info("res: " + res);
//        Map<String, String> decryptParams = new HashMap<>();
//        for (String s : res) {
//            decryptParams.put(s.split("=")[0], s.split("=")[1]);
//        }
//        logger.info("decryptParams" + JsonUtil.toJson(decryptParams));
////        boolean result = BOBSdkUtil.validate(params, merchantPubPath);
//        // 验签失败返回状态码1
////        if (!result) {
////            logger.error("Verify signData of receiveAccount failed");
////            return Constants.INTEGER_ONE;
////        }
//        logger.info("Verify signData of receiveAccount ok");
//        /* 2. 验签成功后处理对账信息 */
//        // 2.1 获取对账信息
//        String merId = decryptParams.get(Constants.BOB_MER_ID);// 商户号
//        String date = decryptParams.get(Constants.BOB_DATE);// 清算时间
//        String count = decryptParams.get(Constants.BOB_COUNT);// 交易明细域中具体明细的数量
//        logger.info("The account result : merId = {}, date = {}, count = {}", merId, date, count);
//        // 如果没有交易明细，返回
//        if (Integer.parseInt(count) == Constants.INTEGER_ZERO) {
//            logger.info("There is no account detail of date = {}", date);
//            return Constants.INTEGER_ZERO;
//        }
//        // 2.2 交易明细域中获取交易明细, 并封装到BOBAccountVo
//        String orderAccountXml = decryptParams.get(Constants.BOB_ORDER_ACCOUNT);// 交易明细集合
//        Document orderAccountDocument;
//        try {
//            logger.info("Begin to parse orderAccountXml to org.dom4j.Document");
//            orderAccountDocument = DocumentHelper.parseText(orderAccountXml);
//            logger.info("Parse orderAccountXml to org.dom4j.Document ok");
//        } catch (DocumentException e) {
//            logger.error("Parse orderAccountXml to org.dom4j.Document failed, xml = {}", orderAccountXml, e);
//            return Constants.INTEGER_TWO;
//        }
//        Element orderAccountElement = orderAccountDocument.getRootElement();
//        List<Element> accountElementList = orderAccountElement.elements(Constants.BOB_ACCOUNT);
//        List<BOBAccountVo> bobAccountVoList = new ArrayList<>();
//        for (Element accountElement : accountElementList) {
//            BOBAccountVo bobAccountVo = new BOBAccountVo();
//            // 获取响应参数
//            Element idElement = accountElement.element(Constants.BOB_ID);// 商户原支付订单号
//            String id = idElement.getTextTrim();
//            Element codeElement = accountElement.element(Constants.BOB_CODE);// 交易码   6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
//            String code = codeElement.getTextTrim();
//            Element amtElement = accountElement.element(Constants.BOB_AMT);// 对账金额
//            String amt = amtElement.getTextTrim();
//            Element rsElement = accountElement.element(Constants.BOB_RS);// S：成功 N：待入账 其他失败
//            String rs = rsElement.getTextTrim();
//            // 设置交易明细
//            bobAccountVo.setId(id);
//            bobAccountVo.setCode(code);
//            bobAccountVo.setAmt(amt);
//            bobAccountVo.setRs(rs);
//            bobAccountVoList.add(bobAccountVo);
//        }
//        // 2.3 将交易明细转换成支付渠道对账的账单数据
//        List<BillFlow> billFlowList = convertAccount2BillFlow(bobAccountVoList);
//        if (CollectionUtils.isEmpty(billFlowList)) {
//            logger.error("convertAccount2BillFlow failed, bobAccountVoList = {}", bobAccountVoList);
//            return Constants.INTEGER_TWO;
//        }
//        /* 3. 持久化到数据库 */
//        billFlowService.addBillFlowBach(billFlowList);
//        logger.info("Handle orderAccount ok");
//
//        /* 4. 持久化成功后，向北京银行响应 */
//        return Constants.INTEGER_ZERO;
//    }

    /**
     * Description: 对账单查询应答
     * @author: JiuDongDong
     * @param params  BOB返回的对账信息
     * @return java.lang.Integer  0成功 1验签失败 2业务处理失败
     * date: 2018/4/22 16:56
     */
    @Override
    public Integer receiveAccount(Map<String, String> params) {
        logger.info("It is now in BOBManagerImpl.receiveAccount, the parameters are: " + JsonUtil.toJson(params));
        /* 1. 验签 */
        String merchantPubPath = bobPayConfigure.getMerchantPubPath();// 公钥路径
        boolean result = BOBSdkUtil.validate(params, merchantPubPath);
        // 验签失败返回状态码1
        if (!result) {
            logger.error("Verify signData of receiveAccount failed");
            return Constants.INTEGER_ONE;
        }
        logger.info("Verify signData of receiveAccount ok");
        /* 2. 验签成功后处理对账信息 */
        // 2.1 获取对账信息
        String merId = params.get(Constants.BOB_MER_ID);// 商户号
        String date = params.get(Constants.BOB_DATE);// 清算时间
        String count = params.get(Constants.BOB_COUNT);// 交易明细域中具体明细的数量
        logger.info("The account result : merId = {}, date = {}, count = {}", merId, date, count);
        // 如果没有交易明细，返回
        if (Integer.parseInt(count) == Constants.INTEGER_ZERO) {
            logger.info("There is no account detail of date = {}", date);
            return Constants.INTEGER_ZERO;
        }
        // 2.2 交易明细域中获取交易明细, 并封装到BOBAccountVo
        String orderAccountXml = params.get(Constants.BOB_ORDER_ACCOUNT);// 交易明细集合
        Document orderAccountDocument;
        try {
            logger.info("Begin to parse orderAccountXml to org.dom4j.Document");
            orderAccountDocument = DocumentHelper.parseText(orderAccountXml);
            logger.info("Parse orderAccountXml to org.dom4j.Document ok");
        } catch (DocumentException e) {
            logger.error("Parse orderAccountXml to org.dom4j.Document failed, xml = {}", orderAccountXml, e);
            return Constants.INTEGER_TWO;
        }
        Element orderAccountElement = orderAccountDocument.getRootElement();
        List<Element> accountElementList = orderAccountElement.elements(Constants.BOB_ACCOUNT);
        List<BOBAccountVo> bobAccountVoList = new ArrayList<>();
        for (Element accountElement : accountElementList) {
            BOBAccountVo bobAccountVo = new BOBAccountVo();
            // 获取响应参数
            Element idElement = accountElement.element(Constants.BOB_ID);// 商户原支付订单号
            String id = idElement.getTextTrim();
            Element codeElement = accountElement.element(Constants.BOB_CODE);// 交易码   6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
            String code = codeElement.getTextTrim();
            Element amtElement = accountElement.element(Constants.BOB_AMT);// 对账金额
            String amt = amtElement.getTextTrim();
            Element rsElement = accountElement.element(Constants.BOB_RS);// S：成功 N：待入账 其他失败
            String rs = rsElement.getTextTrim();
            // 设置交易明细
            bobAccountVo.setId(id);
            bobAccountVo.setCode(code);
            bobAccountVo.setAmt(amt);
            bobAccountVo.setRs(rs);
            bobAccountVoList.add(bobAccountVo);
        }
        // 2.3 将交易明细转换成支付渠道对账的账单数据
        List<BillFlow> billFlowList = convertAccount2BillFlow(bobAccountVoList);
        if (CollectionUtils.isEmpty(billFlowList)) {
            logger.error("convertAccount2BillFlow failed, bobAccountVoList = {}", bobAccountVoList);
            return Constants.INTEGER_TWO;
        }
        /* 3. 持久化到数据库 */
        billFlowService.addBillFlowBach(billFlowList);
        logger.info("Handle orderAccount ok");

        /* 4. 持久化成功后，向北京银行响应 */
        return Constants.INTEGER_ZERO;
    }

    /**
     * Description: 将交易明细转换成支付渠道对账的账单数据
     * @author: JiuDongDong
     * @param bobAccountVoList 交易流水
     * @return com.ewfresh.pay.model.BillFlow 支付渠道对账的账单数据
     * date: 2018/4/23 20:00
     */
    private List<BillFlow> convertAccount2BillFlow(List<BOBAccountVo> bobAccountVoList) {
        logger.info("Start convert Account obj to BillFlow obj");
        if (CollectionUtils.isEmpty(bobAccountVoList)) {
            logger.error("The bobAccountVoList has no obj");
            return null;
        }
        List<BillFlow> billFlowList = new ArrayList<>();
        for (BOBAccountVo bobAccountVo : bobAccountVoList) {
            BillFlow billFlow = new BillFlow();
            String orderId = bobAccountVo.getId();// 订单号
            // 将19位的订单号格式化为20位的加E加R的订单号（充值订单号不做处理）
            billFlow.setOrderId(orderId.length() == INTERACTION_ID_LENGTH ? Long.parseLong(BOBOrderNoFormat.bob19OrderNoTo20WithER(orderId)) : Long.parseLong(orderId));
            String code = bobAccountVo.getCode();// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
            String amt = bobAccountVo.getAmt();// 对账金额
            String rs = bobAccountVo.getRs();// S：成功   N：待入账  其他失败
            if (Constants.BOB_RS_S.equals(rs)) {
                if (Constants.BOB_CODE_6001.equals(code)) {
                    billFlow.setDesp(Constants.BOB_DESP_6001);
                    billFlow.setIncome(FenYuanConvert.fen2Yuan(amt));// 分转化为元
                    billFlow.setChannelName(Constants.BOB_CHANNEL_YLBJ);
                    billFlow.setTradeType(Constants.TRADE_TYPE_1);
                }
                if (Constants.BOB_CODE_6011.equals(code)) {
                    billFlow.setDesp(Constants.BOB_DESP_6011);
                    billFlow.setExpenditure(FenYuanConvert.fen2Yuan(amt));// 分转化为元
                    billFlow.setChannelName(Constants.BOB_CHANNEL_YLBJ);
                    billFlow.setTradeType(Constants.TRADE_TYPE_2);
                }
                if (Constants.BOB_CODE_6005.equals(code)) {
                    billFlow.setDesp(Constants.BOB_DESP_6005);
                    billFlow.setIncome(FenYuanConvert.fen2Yuan(amt));// 分转化为元
                    billFlow.setChannelName(Constants.CHANNEL_NAME_BOB);
                    billFlow.setTradeType(Constants.TRADE_TYPE_1);
                }
                if (Constants.BOB_CODE_6015.equals(code)) {
                    billFlow.setDesp(Constants.BOB_DESP_6015);
                    billFlow.setExpenditure(FenYuanConvert.fen2Yuan(amt));// 分转化为元
                    billFlow.setChannelName(Constants.CHANNEL_NAME_BOB);
                    billFlow.setTradeType(Constants.TRADE_TYPE_2);
                }
            } else if (Constants.BOB_RS_N.equals(rs)) {
                // TODO  待入账
            } else {
                // TODO  失败
            }
            billFlowList.add(billFlow);
        }
        return billFlowList;
    }

    /**
     * Description: 使用订单号获取商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @param orderNo  订单号（订单支付的订单号为19位，充值的订单号长度不定）
     * @return java.lang.String 商户发送订单信息到BOB的时间
     * date: 2018/6/14 15:53
     */
    private Date getSendOrderToBOBTimeFromRedis(String orderNo) {
        Date time = null;
        // 从Redis中获取
        String sendOrderToBOBTime = bobOrderRedisService.getSendOrderToBOBTimeFromRedis(orderNo);
        if (StringUtils.isNotBlank(sendOrderToBOBTime)) {
            try {
                time = sdf.parse(sendOrderToBOBTime);
            } catch (ParseException e) {
                logger.error("Try to parse String sendOrderToBOBTime to Date occurred error: " + sendOrderToBOBTime);
            }
            if (null != time) {
                logger.info("The time of BOBManagerImpl.getSendOrderToBOBTimeFromRedis is: " + time);
                return time;
            }
        }
        // 如果从Redis中获取不到，从pay_flow中获取（success_time字段）
        if (StringUtils.isBlank(sendOrderToBOBTime)) {
            // 如果为订单支付订单号，则将19位订单号转化为原始的加E加R的订单号；充值订单号则不改动
            if (orderNo.length() == INTERACTION_ID_LENGTH) {
                orderNo = BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo);
            }
            String interactionId = orderNo;
            time = payFlowService.getSuccessTimeByInteractionId(interactionId, Constants.TRADE_TYPE_1);
            logger.info("The tradeTime of BOBManagerImpl.getSendOrderToBOBTimeFromRedis is: " + time);
            return time;
        }
        return time;
    }

    /**
     * Description: 使用订单号获取商户发送的退款请求时间
     * @author: JiuDongDong
     * @param orderNo  订单号（订单支付的订单号为19位，充值的订单号长度不定）
     * @return java.util.Date 退款请求时间
     * date: 2018/6/30 17:47
     */
    private Date getRefundOrderToBOBTimeFromRedis(String orderNo) {
        logger.info("The orderNo for BOBManagerImpl.getRefundOrderToBOBTimeFromRedis is: " + orderNo);
        Date time = null;
        // 从Redis中获取
        RefundInfoVo refundOrderInfo = bobOrderRedisService.getRefundOrderInfoFromRedis(orderNo);
        String refundTime = null;
        if (null != refundOrderInfo) {
            refundTime = refundOrderInfo.getRefundTime();
        }
        if (StringUtils.isNotBlank(refundTime)) {
            try {
                time = sdf.parse(refundTime);
            } catch (ParseException e) {
                logger.error("Try to parse String refundTime to Date occurred error: " + refundTime);
            }
            if (null != time) {
                logger.info("The refundTime of BOBManagerImpl.getRefundOrderToBOBTimeFromRedis is: " + refundTime);
                return time;
            }
        }
        // 如果从Redis中获取不到，从pay_flow中获取（success_time字段）
        if (StringUtils.isBlank(refundTime)) {
            // 如果为订单支付订单号，则将19位订单号转化为原始的加E加R的订单号；充值订单号则不改动
            if (orderNo.length() == INTERACTION_ID_LENGTH) {
                orderNo = BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo);
            }
            String interactionId = orderNo;
            time = payFlowService.getSuccessTimeByInteractionId(interactionId, Constants.TRADE_TYPE_2);
            if (null != time) logger.info("The refundTime of BOBManagerImpl.getRefundOrderToBOBTimeFromRedis is: " + sdf.format(time));
            if (null == time) logger.info("The refundTime of BOBManagerImpl.getRefundOrderToBOBTimeFromRedis is: " + time);
            return time;
        }
        return time;
    }

    /**
     * Description: 使用订单号获取商户发送的退款流水号
     * @author: JiuDongDong
     * @param orderNo  订单号（订单支付的订单号为19位，充值的订单号长度不定）
     * @return java.lang.String 退款流水号
     * date: 2018/6/30 17:47
     */
    private String getRefundSeqFromRedis(String orderNo) {
        logger.info("The orderNo for BOBManagerImpl.getRefundSeqFromRedis is: " + orderNo);
        String refundSeq = null;
        // 从Redis中获取
        RefundInfoVo refundOrderInfo = bobOrderRedisService.getRefundOrderInfoFromRedis(orderNo);
        if (null != refundOrderInfo) {
            refundSeq = refundOrderInfo.getRefundSeq();
        }
        if (StringUtils.isNotBlank(refundSeq)) {
            logger.info("The returnFlowId of BOBManagerImpl.getRefundSeqFromRedis is: " + refundSeq);
            return refundSeq;
        }
        // 如果从Redis中获取不到，从pay_flow中获取
        // 如果为订单支付订单号，则将19位订单号转化为原始的加E加R的订单号；充值订单号则不改动
        if (orderNo.length() == INTERACTION_ID_LENGTH) {
            orderNo = BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo);
        }
        String interactionId = orderNo;
        String returnFlowId = payFlowService.getReturnFlowIdByInteractionId(interactionId);
        logger.info("The returnFlowId of BOBManagerImpl.getRefundSeqFromRedis is: " + returnFlowId);
        return StringUtils.isBlank(returnFlowId) ? null : returnFlowId;
    }
}
