package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.configure.BOCPayConfigure;
import com.ewfresh.pay.manager.BOCManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.boc.BOCP7Verify;
import com.ewfresh.pay.util.boc.BOCRefundSeqFormat;
import com.ewfresh.pay.util.boc.BOCP7Sign;
import com.ewfresh.pay.util.boc.HttpsUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 *      BOC退款策略
 * @author: JiuDongDong
 * date: 2018/5/31.
 */
@Component
public class BOCRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BOCPayConfigure bocPayConfigure;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BOCManager bocManager;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String CUR_CODE = "curCode";// 订单币种 目前只支持001：人民币,固定填001
    private static final String HEADER = "header";// 报文头
    private static final String BODY = "body";// 报文体
    private static final String MSG_ID = "msgId";// 报文标识号
    private static final String ORDER_AMOUNT = "orderAmount";// 订单金额
    private static final String METHOD_NAME = "methodName";// 方法名
    private static final String HDL_STS = "hdlSts";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_SUCC = "A";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_FAIL = "B";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_UNDEFINE = "K";// 处理状态 A-成功  B-失败  K-未明
    private static final String BD_FLG = "bdFlg";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String BD_FLG_YES = "0";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String BD_FLG_NO = "1";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String RTN_CD = "rtnCd";// 报文处理返回码
    private static final String EXCEPTION = "exception";// 异常
    private static final String DEAL_STATUS_SUCC = "0";// 0：成功

    /**
     * Description: 传入退款请求参数，向BOC发送退款请求，返回支付流水对象
     * @author: JiuDongDong
     * @param refundParam 退款请求参数
     * @return com.ewfresh.pay.model.PayFlow 退款成功后封装的支付流水
     * date: 2018/5/31 09:14
     */
    @Override
    public List<PayFlow> refund(RefundParam refundParam) {
        logger.info("Refund by BOC START, the params = {}", JsonUtil.toJson(refundParam));
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        String channelType = refundParam.getChannelType();// 类型(07：互联网，08：移动)
        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(channelType) || StringUtils.isBlank(orderNo)) {
            logger.warn("The params tradeNo or refundAmount or outRequestNo or outTradeNo or channelType or orderNo is empty");
            return null;
        }

        /* 2. 封装退款请求参数 */
        // 2.1 从bocPayConfigure获取部分配置信息
        String merchantNo = bocPayConfigure.getMerchantNo();// 商户号
        String curCode = bocPayConfigure.getCurCode();// 币种
        String url = bocPayConfigure.getRefundOrderUrl();
        // 2.2 生成商户系统产生的退款交易流水号
        String mRefundSeq = BOCRefundSeqFormat.orderNo2BOCRefundString(orderNo);
        // 2.3 生成签名
        String signData = BOCP7Sign.getRefundOrderSignData(merchantNo, mRefundSeq, curCode, refundAmount, outTradeNo);// 签名
        // 2.4 封装map参数
        Map<String, String> param = new HashMap<>();
        param.put(Constants.MERCHANT_NO, merchantNo); // 商户号
        param.put(Constants.M_REFUND_SEQ, mRefundSeq); // 商户系统产生的交易流水号
        param.put(CUR_CODE, curCode);// 币种
        param.put(Constants.REFUND_AMOUNT, refundAmount);// 退款金额
        param.put(Constants.ORDER_NO, orderNo);// 订单号
        param.put(Constants.SIGN_DATA, signData);// 签名

        /* 3、发送Httpclient请求退款 */
        String xml = null;
        try {
            logger.info("Try to refund order from BOC start");
            xml = HttpsUtils.bocHttpsPost(url, param);
            logger.info("Refund order from BOC ok");
        } catch (Exception e) {
            logger.error("Try to refund order failed", e);
            return null;
        }

        /* 4、从返回报文获取必要参数，并校验 */
        // 4.1 解析报文，获取参数
        Document document = null;
        try {
            logger.info("Now is going to parse BOC refund xml to Document");
            document = DocumentHelper.parseText(xml);
            logger.info("Parse refund xml OK");
        } catch (DocumentException e) {
            logger.error("Error occurred when parse refund xml to Document, the xml is: " + xml, e);
            return null;
        }
        Element rootElement = document.getRootElement();
        // 获取head
        Element headerElement = rootElement.element(HEADER);
        Element msgIdElement = headerElement.element(MSG_ID);// 报文标识号
        Element hdlStsElement = headerElement.element(HDL_STS);// 处理状态 A-成功  B-失败  K-未明
        // TODO 文档里，处理状态有冲突，测试环境OK后，确认下，到底是“hdlStsName”还是“dealStatus”
        Element dealStatusElement = headerElement.element(Constants.DEAL_STATUS);//处理状态0：成功 1：失败 2：未明
        String dealStatus = dealStatusElement.getTextTrim();//
        Element bdFlgElement = headerElement.element(BD_FLG);// 业务体报文块存在标识 0-有包体 1-无包体
        Element rtnCdElement = headerElement.element(RTN_CD);// 报文处理返回码
        Element exceptionElement = headerElement.element(EXCEPTION);//文档中，并没有写出有Exception，但原始版的有，所以这里也给加上了
        String exception = null;
        if (exceptionElement != null) {
            exception = exceptionElement.getTextTrim();
        }
        // 如果交易失败则打印失败信息并返回
        if (!dealStatus.equals(DEAL_STATUS_SUCC)) {
            logger.error("BOC refund failed, dealStatus = {}, exception = {}", dealStatus, exception);
            return null;
        }

//        Element returnActFlagElement = headerElement.element(RETURN_ACT_FLAG);//返回操作类型,银行返回的操作类型（该交易结果返回为3）1：支付结果通知 2：电话订单结果通知 3：退款结果通知 4：订单查询结果
//        Element dealStatusElement = headerElement.element(dealStatusName);//处理状态0：成功 1：失败 2：未明
//        String dealStatus = dealStatusElement.getTextTrim();//
//        Element bodyFlagElement = headerElement.element(bodyFlagName);//包体标志 0：有包体数据 1：无包体数据

        // 获取body（交易成功，则一定会有包体）
        Element bodyElement = rootElement.element(BODY);

        Element merchantNoElement = bodyElement.element(Constants.MERCHANT_NO);//商户号
        String merchantNo1 = merchantNoElement.getTextTrim();//中行返回的商户号
        Element mRefundSeqElement = bodyElement.element(Constants.M_REFUND_SEQ);//商户系统产生的交易流水号
        String mRefundSeq1 = mRefundSeqElement.getTextTrim();
        Element refundAmountElement = bodyElement.element(Constants.REFUND_AMOUNT);//退款金额
        String refundAmount1 = refundAmountElement.getTextTrim();
        Element orderNoElement = bodyElement.element(Constants.ORDER_NO);//商户订单号
        String orderNo1 = orderNoElement.getTextTrim();
        Element orderSeqElement = bodyElement.element(Constants.ORDER_SEQ);//银行的订单流水号（银行产生的订单唯一标识）
        String orderSeq = orderSeqElement.getTextTrim();
        Element orderAmountElement = bodyElement.element(ORDER_AMOUNT);//订单金额
        String orderAmount = orderAmountElement.getTextTrim();
        Element bankTranSeqElement = bodyElement.element(Constants.BANK_TRAN_SEQ);//银行交易流水号
        String bankTranSeq = bankTranSeqElement.getTextTrim();
        Element tranTimeElement = bodyElement.element(Constants.TRAN_TIME);//银行交易时间：YYYYMMDDHHMISS
        String tranTime = tranTimeElement.getTextTrim();
        Element signDataElement = bodyElement.element(Constants.SIGN_DATA);//中行签名数据
        String signDataReturn = signDataElement.getTextTrim();
        // 4.2 验签
        // 商户号|商户退款交易流水号|退款金额|商户订单号|银行订单流水号|订单金额|银行交易流水号|银行交易时间|退款处理状态
        // merchantNo|mRefundSeq|refundAmount|orderNo|orderSeq|orderAmount|bankTranSeq|tranTime|dealStatus
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.MERCHANT_NO, merchantNo1);
        params.put(Constants.M_REFUND_SEQ, mRefundSeq);
        params.put(Constants.REFUND_AMOUNT, refundAmount);
        params.put(Constants.ORDER_NO, orderNo);
        params.put(Constants.ORDER_SEQ, orderSeq);
        params.put(ORDER_AMOUNT, orderAmount);
        params.put(Constants.BANK_TRAN_SEQ, bankTranSeq);
        params.put(Constants.TRAN_TIME, tranTime);
        params.put(Constants.DEAL_STATUS, dealStatus);// TODO dealStatus和halSts留其一即可
//        params.put(HDL_STS, hdlSts);
        params.put(Constants.SIGN_DATA, signDataReturn);
        params.put(METHOD_NAME, Thread.currentThread().getStackTrace()[Constants.INTEGER_ONE].getMethodName());// 当前方法名
        // 验签
        Boolean verifySuccess = BOCP7Verify.verifySignData(params);
        // 验签失败则返回
        if (!verifySuccess) {
            logger.error("Verify BOC refund sign data failed");
            return null;
        }

        /* 5、银行退款成功，进行商户业务处理 */
        // 根据支付渠道流水号获取退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
        String payerId = payFlow.getPayerId();//付款人id
        String receiverUserId = payFlow.getReceiverUserId();//收款人id
        String payerName = payFlow.getPayerName();//付款人名称
        String receiverName = payFlow.getReceiverName();//收款人名称
        payFlow.setPayerPayAmount(new BigDecimal(refundAmount1));//付款方支付金额（即商户退款金额）
        // 收款人id和付款人id对调、收款人名称和付款人名称对调
        payFlow.setPayerId(receiverUserId);
        payFlow.setReceiverUserId(payerId);
        payFlow.setPayerName(receiverName);
        payFlow.setReceiverName(payerName);
        // 支付成功时间
        try {
            logger.info("Try parse tranTime when handle BOC refund param, the tranTime = {}", tranTime);
            payFlow.setSuccessTime(sdf.parse(tranTime));
        } catch (ParseException e) {
            // 事实上，这里catch异常只是语法要求，银行处理成功后，时间格式不会有误，假定这里发生了异常，也仅仅是时间解析异常，银行退款让是成功的，所以继续处理后续业务
            logger.error("Parse tranTime occurred error, the tranTime = {}", tranTime);
            payFlow.setSuccessTime(null);
        }
        // 是否退款
        payFlow.setIsRefund(Constants.SHORT_ONE);

        /* 6、 返回PayFlow对象*/
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;

    }
}
