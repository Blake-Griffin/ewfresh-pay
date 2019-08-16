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
import com.ewfresh.pay.util.GetBenefitFateUtil;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
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
 * description: 中国银联H5Pay退款策略
 * @author: JiuDongDong
 * date: 2019/5/16.
 */
@Component
public class UnionPayH5PayRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
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

    @Value("${H5Pay.msgType_refund}")
    private String msgType_refund;
    @Value("${H5Pay.msgType_query}")
    private String msgType_query;//订单查询（支付）
    @Value("${H5Pay.msgType_refundQuery}")
    private String msgType_refundQuery;//订单查询（退款）

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
    public List<PayFlow> refund(RefundParam refundParam) throws RefundParamNullException, PayFlowFoundNullException,
            UnsupportedEncodingException, RefundHttpToBill99FailedException, RefundBill99ResponseNullException,
            RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException,
            WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException,
            UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        logger.info("Refund by UnionPayH5Pay START, the params = {}", JsonUtil.toJson(refundParam));
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额，单位为元（包含运费，退货退款的运费为0）
        refundAmount = StringUtils.isBlank(refundAmount) ? null : FenYuanConvert.yuan2Fen(refundAmount).toString();
        String totalAmount = refundParam.getTotalAmount();// 订单金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        BigDecimal freight = refundParam.getFreight();// 银联需退的运费
        Short tradeType = refundParam.getTradeType();//交易类型为17时为配货退款（DXM）

        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(orderNo)) {
            logger.error("The param has empty part, tradeNo = " + tradeNo + ", refundAmount = " + refundAmount +
                    ", outRequestNo = " + outRequestNo + ", outTradeNo = " + outTradeNo + ", orderNo = " + orderNo);
            throw new RefundParamNullException("The params tradeNo or refundAmount or outRequestNo or outTradeNo " +
                    "or orderNo is empty");
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
               choiceUnionPayRefundParamsPolicy.ChoiceUnionPayRefundParamsPolicy(UNIONPAY_H5Pay, receiverUserId, refundType);
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
            com.alibaba.fastjson.JSONObject respJsonObj = null;
            try {
                respJsonObj = HttpPostToUnionPay.httpPostToUnionPay(APIurl, paramsMap);
            } catch (Exception e) {
                logger.error("The response data is empty for UnionPayH5PayRefundPolicy, outTradeNo = " +
                        outTradeNo + ", respJsonObj = " + respJsonObj);
                logger.error("Connection is timeout for refund: " + outTradeNo);
                // 网络异常，请稍后重试
                throw new HttpToUnionPayFailedException("Connection is timeout for refund: " + outTradeNo);
            }
            // 验签
            Map<String, String> verifyParams = new HashMap<>();
            for (Map.Entry<String, Object> stringObjectEntry : respJsonObj.entrySet()) {
                String key = stringObjectEntry.getKey();
                Object value = stringObjectEntry.getValue();
                logger.info("key = {}, value = {}", key, value);
                verifyParams.put(key, value.toString());
            }
            boolean checkSign = UnionPayQrCodeUtil.checkSign(md5Key, verifyParams);
            if (!checkSign) {
                logger.error("Verify signature for UnionPayH5PayRefundPolicy response not pass!!! outTradeNo = " + outTradeNo);
                logger.error("The response data of refund outTradeNo: " + outTradeNo + " is: " + respJsonObj);
                // 校验中国银联签名异常
                throw new VerifyUnionPaySignatureException("Verify signature for UnionPayH5RefundPolicy Exception");
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
                throw new UnionPayHandleRefundException("UnionPayH5PayRefundPolicy handle refund Exception");
            }
            logger.info("UnionPay handle refund success, outTradeNo = {}", outTradeNo);
            logger.info("The response data of refund is: {}", respJsonObj);

            /* 5. 封装PayFlow信息 */
            // 5.1 设置支付流水信息
            payFlow.setOrderId(Long.valueOf(orderNo));
            payFlow.setMid(mid);
            payFlow.setTid(tid);
            payFlow.setShopBenefitPercent(shopBenefitPercent);
            payFlow.setFreight(freight);
            payFlow.setChannelFlowId(refundSequence);//msgSrcId + 生成28位的退款流水号
            payFlow.setPayerPayAmount(FenYuanConvert.fen2Yuan(refundAmount));//付款方支付金额
            payFlow.setTradeType(tradeType);//交易类型
            payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
            payFlow.setDesp(refundType);//订单描述
            payFlow.setIsRefund(SHORT_ONE);// 是否退款
            payFlow.setStatus(STATUS_2);//状态 0:成功,1:失败,2处理中
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
            refundInfoVo.setRefundType(refundType);// 退款类型
            unionPayH5RedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_UNIONPAYH5PAY_REFUND_INFO);
            refund.add(payFlow);
        }

        /* 6. 返回PayFlow对象*/
        logger.info("refund = {}", JsonUtil.toJson(refund));
        return refund;
    }

}
