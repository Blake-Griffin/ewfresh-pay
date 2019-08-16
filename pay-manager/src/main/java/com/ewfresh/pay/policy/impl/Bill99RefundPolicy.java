package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.bill99.MD5Util;
import com.ewfresh.pay.util.bob.BOBRefundSeqFormat;
import com.ewfresh.pay.util.bob.bobuponline.HttpClient;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: Bill99退款策略
 * @author: JiuDongDong
 * date: 2018/8/1.
 */
@Component
public class Bill99RefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Value("${bill99.merchantId}")
    private String strMerchantId;
    @Value("${bill99.refundVersion}")
    private String refundVersion;//退款接口版本号
    private static final String COMMAND_TYPE = "001";//操作类型, 退款
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Description: 传入退款请求参数，向Bill99发送退款请求，返回支付流水对象(线上使用的是RefundController接口退款)
     * @author: JiuDongDong
     * @param refundParam 退款请求参数
     * @return com.ewfresh.pay.model.PayFlow 退款成功后封装的支付流水
     * date: 2018/8/1 20:48
     */
    @Override
    public List<PayFlow> refund(RefundParam refundParam) throws
            RefundParamNullException, PayFlowFoundNullException, UnsupportedEncodingException,
            RefundHttpToBill99FailedException, RefundBill99ResponseNullException,
            DocumentException, RefundBill99HandleException {
        logger.info("Refund by Bill99 START, the params = {}", JsonUtil.toJson(refundParam));
        /* 0. 首先先查看该订单当前是否存在尚未完成的退款，未完成则终止 */
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        String totalAmount = refundParam.getTotalAmount();// 订单金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        BigDecimal freight = refundParam.getFreight();//运费
        Short tradeType = refundParam.getTradeType();//交易类型为17时为配货退款（DXM）

        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(orderNo)) {
            logger.error("The params tradeNo or refundAmount or outRequestNo or outTradeNo or orderNo is empty");
            throw new RefundParamNullException("The params tradeNo or refundAmount or outRequestNo or outTradeNo or orderNo is empty");
        }

        // 1.1 根据支付渠道流水号获取部分退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
        if (payFlow == null) {
            logger.error("There is no this payFlowId: " + tradeNo);
            throw new PayFlowFoundNullException("There is no this payFlowId: " + tradeNo);
        }
        String receiverUserId = payFlow.getReceiverUserId();//店铺id
        Integer shopBenefitPercent = payFlow.getShopBenefitPercent();//店铺分润比例
        String interactionId = payFlow.getInteractionId();

        /* 从Redis获取退款信息---order项目退款时放入（退货退款、补款退款没放，单独处理） */
        String refundType;
        if (TRADE_TYPE_9.equals(tradeType)) {
            refundType = REFUND_TYPE_SUPPLEMENT;
        } else if (TRADE_TYPE_17.equals(tradeType)) {
            refundType = REFUND_TYPE_REFUNDS;
        } else {
            Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            refundType = redisRefundInfo.get(REFUND_TYPE);//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
        }
        logger.info("ShopId  = {}, refundType = {}", receiverUserId, refundType);

        /* 2. 封装退款请求参数 */
        Map<String, String> params = new HashMap<>();
        // 2.1 从bill99PayConfigure获取部分配置信息
        String merId;// 商户号
        String refundPassword;// 退款的密码
        String merIdHat = bill99PayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99PayConfigure.getMerIdNotHat();// 商户号(自营)
        String refundPasswordHat = bill99PayConfigure.getRefundPassword();// 退款的密码(存管)
        String refundPasswordNotHat = bill99PayConfigure.getRefundPasswordNotHat();// 退款的密码(自营)
        if (SELF_SHOP_ID.equals(receiverUserId)) {
            merId = merIdNotHat;
            refundPassword = refundPasswordNotHat;
        } else {
            merId = merIdHat;
            refundPassword = refundPasswordHat;
        }
        logger.info("The merId is: {}, refundPassword = {}", merId, refundPassword);

        String refundActionUrl = bill99PayConfigure.getRefundUrl();// 退款网关
        // 生成28位的退款流水号
        String refundSequence =
                BOBRefundSeqFormat.orderNo2UnionPayRefundSequence(outRequestNo) + interactionId.substring(interactionId.length() - 1);
        // 商户发送退款请求的时间
        Date refundTime = new Date();
        // 2.2 组装退款请求参数（没有对持卡人ip进行封装，此字段非必须）
        //生成加密签名串
        String macVal = "";
        String mac;
        macVal = appendParam(macVal, strMerchantId, merId);
        macVal = appendParam(macVal, BILL99_VERSION, refundVersion);
        macVal = appendParam(macVal, BILL99_COMMAND_TYPE, COMMAND_TYPE);
        macVal = appendParam(macVal, BILL99_ORDER_iD, outTradeNo);//原商户订单号
        macVal = appendParam(macVal, BILL99_AMOUNT, refundAmount);//退款金额，整数或小数，小数位为2位   以人民币元为单位
        macVal = appendParam(macVal, BILL99_POSTDATE, sdf.format(refundTime));//退款提交时间
        macVal = appendParam(macVal, BILL99_TXORDER, refundSequence);//退款流水号  字符串
        macVal = appendParam(macVal, BILL99_MERCHANT_KEY, refundPassword);//加密所需的key值
        mac = MD5Util.md5Hex(macVal.getBytes(UTF_8)).toUpperCase();
        params.put(strMerchantId, merId);
        params.put(BILL99_VERSION, refundVersion);
        params.put(BILL99_COMMAND_TYPE, COMMAND_TYPE);
        params.put(BILL99_TXORDER, refundSequence);//退款流水号  字符串
        params.put(BILL99_AMOUNT, refundAmount);//退款金额，整数或小数，小数位为2位   以人民币元为单位
        params.put(BILL99_POSTDATE, sdf.format(refundTime));//退款提交时间
        params.put(BILL99_ORDER_iD, outTradeNo);//原商户订单号
        params.put(BILL99_MAC, mac);//加密串

        /* 3. 发送退款请求 */
        logger.info("The refund order parameters have been packaged ok and will Httpclient to Bill99");
        HttpClient httpClient = new HttpClient(refundActionUrl, INTEGER_20000, INTEGER_20000);
        Integer status;
        try {
            status = httpClient.send(params, UTF_8);
            logger.info("The status of httpClient Bill99 response to merchant: {}", status);
        } catch (Exception e) {
            logger.error("Connection is timeout for refund: " + outTradeNo, e);
            throw new RefundHttpToBill99FailedException(e);
        }
        String refundRes = httpClient.getResult();
        logger.info("The refund response Bill99 send to merchant: {}", refundRes);
        if (StringUtils.isBlank(refundRes)) {
            logger.error("The refund response Bill99 send to merchant: " + refundRes);
            throw new RefundBill99ResponseNullException("The refund response Bill99 send to merchant: " + refundRes);
        }

        /* 4. 解析响应数据 */
        Document document;
//        try {
            logger.info("Now is going to parse xml to Document");
            document = DocumentHelper.parseText(refundRes);
            logger.info("Parse xml OK");
//        } catch (DocumentException e) {
//            logger.error("Error occurred when parse xml to Document, the xml is: " + refundRes, e);
//            throw new RuntimeException("Error occurred when parse xml to Document, the xml is: " + refundRes);
//        }
        Element rootElement = document.getRootElement();// 根节点
        Element merchantElement = rootElement.element("MERCHANT");
        Element orderIdElement = rootElement.element("ORDERID");
        Element txOrderElement = rootElement.element("TXORDER");
        Element amountElement = rootElement.element("AMOUNT");
        Element resultElement = rootElement.element("RESULT");
        Element codeElement = rootElement.element("CODE");
        String merchant = merchantElement.getTextTrim();
        String orderId = orderIdElement.getTextTrim();// 与提交时原商户订单号保持一致
        String txOrder = txOrderElement.getTextTrim();// 与提交时退款流水号保持一致
        String amount = amountElement.getTextTrim();// 退款金额
        String result = resultElement.getTextTrim();// 退款申请结果，固定值：Y、N。  Y 表示退款申请成功；N 表示退款申请失败，后续退款状态（退款成功还是失败）需要调查询接口轮询下
        String code = codeElement.getTextTrim();// 错误信息
        // 如果发生错误，打印错误信息、如果申请失败，打印日志，程序停止
        if (StringUtils.isNotBlank(code) || !"Y".equals(result)) {
            logger.error("refund apply failed with error code: " + code);
            throw new RefundBill99HandleException("refund apply failed with error code: " + code);
        }

        /* 5. 封装PayFlow信息 */
        // 5.1 设置支付流水信息
        payFlow.setOrderId(Long.valueOf(orderNo));
        payFlow.setShopBenefitPercent(shopBenefitPercent);
        payFlow.setFreight(freight);
        payFlow.setChannelFlowId(txOrder);
        payFlow.setPayerPayAmount(new BigDecimal(amount));//付款方支付金额
        payFlow.setTradeType(tradeType);//交易类型
        payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
        payFlow.setDesp(refundType);//订单描述
        payFlow.setIsRefund(SHORT_ONE);// 是否退款
        payFlow.setStatus(STATUS_2);// 申请成功，尚未确定是否退款成功，数据库：状态 0:成功,1:失败,2处理中
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
        payFlow.setSuccessTime(refundTime);
        // 第三方交互订单号
        payFlow.setInteractionId(refundSequence);
        logger.info("Refund order apply success, the orderNo = {}", outTradeNo);
        // 5.3 退款信息放入Redis
        RefundInfoVo refundInfoVo = new RefundInfoVo();
        refundInfoVo.setRefundParam(refundParam);
        refundInfoVo.setRefundSeq(refundSequence);// 生成的28位的退款流水号
        refundInfoVo.setRefundTime(sdf.format(refundTime));
        refundInfoVo.setRefundType(refundType);// 退款类型
        bill99OrderRedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_BILL99_REFUND_INFO);

        /* 6. 返回PayFlow对象*/
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;
    }

    /**
     * Description: 封装退款请求参数
     * @author: JiuDongDong
     * @param returns 初始值
     * @param paramId key
     * @param paramValue  value
     * @return java.lang.String 请求参数
     * date: 2018/8/16 17:13
     */
    private String appendParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != "") {
                returns +=  paramId + "=" + paramValue;
            }
        } else {
            if (paramValue != "") {
                returns = paramId + "=" + paramValue;
            }
        }
        return returns;
    }
}
