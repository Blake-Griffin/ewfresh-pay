package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.UnionPayH5RedisService;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.unionPayRefundParamsPolicy.ChoiceUnionPayRefundParamsPolicy;
import com.ewfresh.pay.unionPayRefundParamsPolicy.UnionPayRefundParamsPolicy;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayb2cwebwap.UnionPayLogUtil;
import com.ewfresh.pay.util.unionpayh5pay.HttpPostToUnionPay;
import com.ewfresh.pay.util.unionpayqrcode.UnionPayQrCodeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 中国银联QRCode退款策略
 * @author: JiuDongDong
 * date: 2019/5/15.
 */
@Component
public class UnionPayQRCodeRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
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

    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Value("${httpClient.getOrderInfo}")
    private String getOrderInfoUrl;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private UnionPayH5RedisService unionPayH5RedisService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private GetBenefitFateUtil getBenefitFateUtil;
    @Autowired
    private ChoiceUnionPayRefundParamsPolicy choiceUnionPayRefundParamsPolicy;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<PayFlow> refund(RefundParam refundParam) throws RefundParamNullException, PayFlowFoundNullException, UnsupportedEncodingException, RefundHttpToBill99FailedException, RefundBill99ResponseNullException, RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        logger.info("Refund by UnionPayQRCode START, the params = {}", JsonUtil.toJson(refundParam));
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        refundAmount = StringUtils.isBlank(refundAmount) ? null : FenYuanConvert.yuan2Fen(refundAmount).toString();
        String totalAmount = refundParam.getTotalAmount();// 订单金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        BigDecimal freight = refundParam.getFreight();// 银联需退的运费
        Short tradeType = refundParam.getTradeType();//交易类型为17时为配货退款（DXM）

        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(orderNo)) {
            logger.error("The param has empty part, tradeNo = " + tradeNo + ", refundAmount = " + refundAmount + ", outRequestNo = " +
                    outRequestNo + ", outTradeNo = " + outTradeNo + ", orderNo = " + orderNo);
            throw new RefundParamNullException("The params tradeNo or refundAmount or outRequestNo or outTradeNo or orderNo is empty");
        }
        // 1.1 根据支付渠道流水号获取部分退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
        if (payFlow == null) {
            logger.error("There is no this payFlowId: " + tradeNo);
            throw new PayFlowFoundNullException("There is no this payFlowId: " + tradeNo);
        }
        String receiverUserId = payFlow.getReceiverUserId();//店铺id
        Date successTime = payFlow.getSuccessTime();//订单时间，格式yyyy-MM-dd
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();//付款方支付金额，单位为元
        Integer shopBenefitPercent = payFlow.getShopBenefitPercent();//店铺分润比例
        String mid = payFlow.getMid();//主商户号
        String tid = payFlow.getTid();//主终端号
        String shopMid = null;//店铺的商户号
        try {
            if (!STR_ZERO.equals(receiverUserId)) {
                shopMid = getBenefitFateUtil.getMid(receiverUserId, STR_ONE);
                logger.info("ShopMid = {}", shopMid);
            }
        } catch (Exception e) {
            logger.error("Get shopMid by shopId from redis occurred error. tradeNo = " + tradeNo +
                    ", shopId = " + receiverUserId, e);
            throw new RuntimeException("Get shopMid by shopId from redis occurred error.");
        }

        /* 从Redis获取退款信息---order项目退款时放入（退货退款、补款退款没放，单独处理） */
        String refundType;
        if (TRADE_TYPE_9.equals(tradeType)) {
            refundType = REFUND_TYPE_SUPPLEMENT;//配货退款("supplement")
        } else if (TRADE_TYPE_17.equals(tradeType)) {
            refundType = REFUND_TYPE_REFUNDS;//退货退款("refunds")
        } else {
            Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            refundType = redisRefundInfo.get(REFUND_TYPE);//退款类型，包括取消订单("cancel")、关闭订单("shutdown")
        }
        logger.info("ShopId  = {}, refundType = {}", receiverUserId, refundType);

        UnionPayRefundParamsPolicy refundParamsPolicy =
                choiceUnionPayRefundParamsPolicy.ChoiceUnionPayRefundParamsPolicy(UNIONPAY_QRCODE, receiverUserId, refundType);
        // 获取请求报文
        List<Map<String, String>> refundParams =
                refundParamsPolicy.getUnionPayRefundParams(refundParam, outTradeNo, mid, tid, shopMid, payerPayAmount, outRequestNo, successTime);

        if (StringUtils.isBlank(APIurl)) {
            logger.error("APIurl is blank for qr code refund");
            throw new RuntimeException("APIurl is blank for qr code refund");
        }

        // 定义返回PayFlow对象
        List<PayFlow> refund = new ArrayList<>();
        // 退款
        for (Map<String, String> paramsMap : refundParams) {
            String refundSequence = paramsMap.get(REFUND_ORDER_ID);//生成32位的退款流水号
            String refundTime = paramsMap.get(REQUEST_TIMESTAMP);//退款请求时间
            //调用银商平台退款接口
            com.alibaba.fastjson.JSONObject respJsonObj;
            try {
                respJsonObj = HttpPostToUnionPay.httpPostToUnionPay(APIurl, paramsMap);
            } catch (Exception e) {
                logger.error("The response data is empty for UnionPayWebWapRefundPolicy, outTradeNo = " + outTradeNo);
                logger.error("Connection is timeout for refund: " + outTradeNo);
                // 网络异常，请稍后重试
                throw new HttpToUnionPayFailedException("Connection is timeout for refund: " + outTradeNo);
            }

            // 验签
            Map<String, String> verifyParams = new HashMap<>();
            for (Map.Entry<String, Object> stringObjectEntry : respJsonObj.entrySet()) {
                String key = stringObjectEntry.getKey();
                Object value = stringObjectEntry.getValue();
                verifyParams.put(key, value.toString());
            }
            logger.info("verifyParams = {}", JsonUtil.toJson(verifyParams));
            boolean checkSign = UnionPayQrCodeUtil.checkSign(key, verifyParams);
            if (!checkSign) {
                logger.error("Verify signature for UnionPayWebWapRefundPolicy response not pass!!! outTradeNo = " +
                        outTradeNo + ", verifyParams = {}", JsonUtil.toJson(verifyParams));
                logger.error("The response data of refund outTradeNo: " + outTradeNo + " is: " + respJsonObj);
                // 校验中国银联签名异常
                throw new VerifyUnionPaySignatureException("Verify signature for UnionPayQrCodeRefundPolicy Exception");
            }
            logger.info("Verify signature success! outTradeNo = {}", outTradeNo);

            //网付系统应答码的处理：
            //验签成功，处理应答码
            String errCode = respJsonObj.getString(ERR_CODE);
            String errMsg = respJsonObj.getString(ERR_MSG);
            ResponseData responseData = new ResponseData();
            UnionPayLogUtil.logTradeInfo(responseData, errCode, errMsg, outTradeNo);
            String code = responseData.getCode();
            if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
                logger.error("UnionPay handle refund failed, outTradeNo = " + outTradeNo);
                logger.error("The response data of refund is: " + respJsonObj);
                // 中国银联处理退款失败
                throw new UnionPayHandleRefundException("UnionPayQRCodeRefundPolicy handle refund Exception");
            }
            logger.info("UnionPay handle refund success, outTradeNo = {}", outTradeNo);
            logger.info("The response data of refund is: {}", respJsonObj);
            // 退款结果status的各种状态：在worker处理了，这里不处理

            //退款结果：SUCCESS成功；FAIL失败；PROCESSING处理中；UNKNOWN异常
            String refundStatus = respJsonObj.getString(REFUND_STATUS);
            if (StringUtils.equalsIgnoreCase(refundStatus, FAIL) || StringUtils.equalsIgnoreCase(refundStatus, STATUS_UNKNOWN)) {
                logger.error("UnionPay handle refund failed, outTradeNo = " + outTradeNo + ", refundStatus = " + refundStatus);
                logger.error("The response data of refund is: " + respJsonObj);
                // 中国银联处理退款失败
                throw new UnionPayHandleRefundException("UnionPayQrCodeRefundPolicy handle refund Exception");
            }
            if (StringUtils.equalsIgnoreCase(refundStatus, PROCESSING)) {
                logger.error("UnionPay handle refund is in processing, outTradeNo = " + outTradeNo + ", refundStatus = " + refundStatus);
                logger.error("The response data of refund is: " + respJsonObj);
                // 退款信息放入Redis, worker轮询
                RefundInfoVo refundInfoVo = new RefundInfoVo();
                refundInfoVo.setRefundParam(refundParam);
                refundInfoVo.setRefundSeq(refundSequence);// msgSrcId + 生成32位的退款流水号
                refundInfoVo.setRefundTime(refundTime);// 退款申请时间
                refundInfoVo.setSuccessTime(successTime);//格式yyyy-MM-dd
                unionPayH5RedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_UNIONPAYQRCODE_REFUND_INFO);
            }

            /* 5. 封装PayFlow信息 */
            // 5.1 设置支付流水信息
            payFlow.setOrderId(Long.valueOf(orderNo));
            payFlow.setMid(mid);
            payFlow.setTid(tid);
            payFlow.setShopBenefitPercent(shopBenefitPercent);
            payFlow.setFreight(freight);
            payFlow.setChannelFlowId(refundSequence);// 上面生成: msgSrcId + 28位的退款流水号
            payFlow.setPayerPayAmount(FenYuanConvert.fen2Yuan(refundAmount));//付款方支付金额
            payFlow.setTradeType(tradeType);//交易类型：退款
            payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
            payFlow.setDesp(refundType);//订单描述
            payFlow.setIsRefund(SHORT_ONE);// 是否退款
            payFlow.setStatus(STATUS_2);//状态 0:成功,1:失败
            // 收款人id和付款人id对调、收款人名称和付款人名称对调
            String payerId = payFlow.getPayerId();//付款人id
            String payerName = payFlow.getPayerName();//付款人名称
            String receiverName = payFlow.getReceiverName();//收款人名称
            payFlow.setPayerId(receiverUserId);
            payFlow.setReceiverUserId(payerId);
            payFlow.setPayerName(receiverName);
            payFlow.setReceiverName(payerName);
            // 收款账号类型(1个人,2店铺)  和 付款账号类型(1个人,2店铺)   对调
            Short payerType = payFlow.getPayerType();
            Short receiverType = payFlow.getReceiverType();
            payFlow.setPayerType(receiverType);
            payFlow.setReceiverType(payerType);
            payFlow.setCreateTime(null);
            // 退款申请时间
            try {
                payFlow.setSuccessTime(sdf.parse(refundTime));
            } catch (ParseException e) {
                logger.error("Error occurred when parse refundTime for outRequestNo = " + outRequestNo + ", refundTime = " + refundTime);
                payFlow.setSuccessTime(new Date());
            }
            logger.info("Refund order apply success, the orderNo = {}", outTradeNo);

            // 5.3 退款信息放入Redis
            RefundInfoVo refundInfoVo = new RefundInfoVo();
            refundInfoVo.setRefundParam(refundParam);
            refundInfoVo.setRefundSeq(refundSequence);//msgSrcId + 生成28位的退款流水号
            refundInfoVo.setRefundTime(refundTime);// 退款申请时间
            refundInfoVo.setSuccessTime(successTime);// 订单支付成功时间
            refundInfoVo.setRefundType(refundType);
            unionPayH5RedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_UNIONPAYQRCODE_REFUND_INFO);
            refund.add(payFlow);
        }

        /* 6. 返回PayFlow对象*/
        logger.info("refund = {}", JsonUtil.toJson(refund));
        return refund;
    }

}
