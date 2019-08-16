package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.configure.BOBPayConfigure;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.BOBOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.bob.BOBOrderNoFormat;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.bob.bobuponline.HttpClient;
import com.ewfresh.pay.util.bob.BOBRefundSeqFormat;
import com.ewfresh.pay.util.bob.bobutil.BOBSdkUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 *      BOB退款策略
 * @author: JiuDongDong
 * date: 2018/4/24.
 */
@Component
public class BOBRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BOBPayConfigure bobPayConfigure;
    @Autowired
    private PayFlowService payFlowService;
//    @Autowired
//    private BOBManager bobManager;
    @Autowired
    private BOBOrderRedisService bobOrderRedisService;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Description: 传入退款请求参数，向BOB发送退款请求，返回支付流水对象(线上使用的是RefundController接口退款)
     * @author: JiuDongDong
     * @param refundParam 退款请求参数
     * @return com.ewfresh.pay.model.PayFlow 退款成功后封装的支付流水
     * date: 2018/4/24 16:14
     */
    @Override
    public List<PayFlow> refund(RefundParam refundParam) {
        logger.info("Refund by BOB START, the params = {}", JsonUtil.toJson(refundParam));
        /* 0. 首先先查看该订单当前是否存在尚未完成的退款，未完成则终止 */
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        RefundInfoVo refundOrderInfo = bobOrderRedisService.getRefundOrderInfoFromRedis(outTradeNo);
        if (null != refundOrderInfo) {
            throw new RuntimeException("This order has refund already and is not over also, please wait for its over");
        }
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        String totalAmount = refundParam.getTotalAmount();// 订单金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(orderNo)) {
            logger.warn("The params tradeNo or refundAmount or outRequestNo or outTradeNo or orderNo is empty");
            return null;
        }
        // 获取渠道类型(07：互联网，08：移动)
        String channelType = payFlowService.getChannelTypeByChannelFlowId(tradeNo);

        /* 2. 封装退款请求参数 */
        Map<String, String> params = new HashMap<>();
        // 2.1 从bobPayConfigure获取部分配置信息
        String merId = bobPayConfigure.getMerId();// 商户号
        String merchantCertPath = bobPayConfigure.getMerchantCertPath();// 私钥路径
        String merchantCertPss = bobPayConfigure.getMerchantCertPss();// 私钥密码
        String refundActionUrl = bobPayConfigure.getRefundUrl();// 退款网关
        String backEndUrl = bobPayConfigure.getBackEndUrl();// 后台通知地址，用于接收交易结果
        // 2.2 组装退款请求参数（没有对持卡人ip进行封装，此字段非必须）
        params.put(Constants.ORDER_AMOUNT, FenYuanConvert.yuan2Fen(refundAmount).toString());// 退款金额 单位为分 这里由元转化为分
        params.put(Constants.BOB_ORDER_NUMBER, BOBOrderNoFormat.bob20OrderNoTo19WithoutER(outTradeNo));// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号，去除E/R)
        params.put(Constants.BOB_CHANNEL_TYPE, channelType);// 类型(07：互联网，08：移动)
        params.put(Constants.BOB_REFUND_SEQ, BOBRefundSeqFormat.orderNo2BOBRefundString(outRequestNo));// 本次退款新生成的商户侧流水号, 这里取outRequestNo:退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号，然后拼接时间戳)
        params.put(Constants.BOB_SIGN_METHOD, Constants.SIGN_METHOD_RSA);// 默认RSA 取值01
        params.put(Constants.BOB_ENCODING, Constants.UTF_8);// 编码 M
        params.put(Constants.BOB_TRANS_TYPE, Constants.BOB_TRANS_TYPE04);// 消费 M
        // 商户发送退款请求的时间
        Date refundTime = new Date();
        params.put(Constants.BOB_ORDER_TIME, sdf.format(refundTime));//商户发送交易时间yyyyMMddHHmmss
        params.put(Constants.BOB_MER_TYPE, Constants.BOB_MER_TYPE_ZERO);// 商户类型
        params.put(Constants.BOB_BACK_END_URL, backEndUrl);//后台通知地址，接收应答报文地址
        params.put(Constants.BOB_ORDER_DESC, Constants.REFUND_ORDER); // 订单描述，退款
        params.put(Constants.BOB_QUERY_ID, tradeNo);// 原消费流水号
        params.put(Constants.BOB_MER_ID, merId);// 商户号
        // 2.3 退款信息放入Redis
        RefundInfoVo refundInfoVo = new RefundInfoVo();
        refundInfoVo.setRefundParam(refundParam);
        refundInfoVo.setRefundSeq(BOBRefundSeqFormat.orderNo2BOBRefundString(outRequestNo));
        refundInfoVo.setRefundTime(sdf.format(refundTime));
        bobOrderRedisService.putRefundOrderInfoToRedis(refundInfoVo);

        /* 3. 签名 */
        logger.info("Now going to signData for refundOrder");
        Map<String,String> signMap = BOBSdkUtil.sign(params, merchantCertPath, merchantCertPss);
        logger.info("signData ok for refundOrder[" + signMap.toString() + "]");

        /* 4. 发送退款请求 */
        logger.info("The refund order parameters have been packaged ok and will Httpclient to BOB");
        HttpClient httpClient = new HttpClient(refundActionUrl, Constants.INTEGER_20000, Constants.INTEGER_20000);
        Integer status;
        try {
            status = httpClient.send(signMap, Constants.UTF_8);
            logger.info("The status of httpClient BOB response to merchant: " + status);
        } catch (Exception e) {
            logger.error("Connection is timeout for refund: " + outTradeNo, e);
            return null;
        }
        String refundRes = httpClient.getResult();
        logger.info("The refund response BOB send to merchant: " + refundRes);
        if (StringUtils.isBlank(refundRes)) {
            throw new RuntimeException("BOB do not handle refund business for YIWANGJUXIAN, the orderNo: " + orderNo);
        }

//        /* 5. 立即查询BOB退款信息 */
//        // 5.1 封装查询参数
//        Map<String, String> singleQueryParams = new HashMap<>();
//        singleQueryParams.put(Constants.BOB_MER_ID, merId);
//        String transType = Constants.BOB_TRANS_TYPE35;
//        // 判断当前退款是全额退款还是部分退款
//        if (!totalAmount.equals(refundAmount)) {
//            transType = Constants.BOB_TRANS_TYPE36;
//        }
//        logger.info("The current refund transType = {}", transType);
//        singleQueryParams.put(Constants.BOB_TRANS_TYPE, transType);
//        singleQueryParams.put(Constants.BOB_ORDER_NUMBER, BOBOrderNoFormat.oriOrderNo2BOB19(outTradeNo));// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
//        // 根据支付渠道流水号查询订单支付成功时间
//        Date successTime = payFlowService.getSuccessTimeByChannelFlowId(tradeNo);
//        singleQueryParams.put(Constants.BOB_ORDER_TIME, sdf.format(refundTime));// 商户发送退单请求的时间
////            singleQueryParams.put(Constants.ORDER_AMOUNT, orderAmount);//订单金额  TODO 文档里面没有，但示例里面有，如果真的需要，做非空校验
//        singleQueryParams.put(Constants.BOB_REFUND_SEQ, BOBRefundSeqFormat.orderNo2BOBRefundString(outRequestNo));// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
//        logger.info("The refund singleQuery params: " + JsonUtil.toJson(singleQueryParams));
//        // 5.2 查询
//        ResponseData responseData = new ResponseData();
//        bobManager.singleQuery(responseData, singleQueryParams, null);
//        String code = responseData.getCode();
//        logger.info("The code of ResponseData of BOB refund single query is: " + code);
//        // 5.3 结果处理
//        if (!ResponseStatus.OK.getValue().equals(code)) {
//            logger.error("Errors occurred when single query refund order: " + orderNo);
//            return null;
//        }
//
//        /* 6. 获取响应参数封装PayFlow信息 */
//        Map<String, String> singleQueryResponseData = (Map<String, String>) responseData.getEntity();// 订单查询响应结果
//        String orderNumberRes = singleQueryResponseData.get(Constants.BOB_ORDER_NUMBER);// 订单号  商户原支付订单号
//        String orderTimeRes = singleQueryResponseData.get(Constants.BOB_ORDER_TIME);// 商户订单时间  查询订单交易时间
////        String refundSeqRes = singleQueryResponseData.get(Constants.BOB_REFUND_SEQ);// 商户退款流水号 退款交易时出现
//        String orderAmountRes = singleQueryResponseData.get(Constants.BOB_ORDER_AMOUNT);//订单金额  分为单位
////        String merIdRes = singleQueryResponseData.get(Constants.BOB_MER_ID);// 商户号
//        String queryIdRes = singleQueryResponseData.get(Constants.BOB_QUERY_ID);//查询流水号  用于后续查询该笔退款交易
//        // 6.1 根据支付渠道流水号获取退款信息
//        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
//        if (payFlow == null) {
//            logger.error("There is no this payFlowId: " + tradeNo);
//            return null;
//        }
//        // 6.2 设置支付流水信息
//        payFlow.setChannelFlowId(queryIdRes);
//        payFlow.setPayerPayAmount(FenYuanConvert.fen2Yuan(orderAmountRes));//付款方支付金额（即商户退款金额） 分转换为元
//        // 收款人id和付款人id对调、收款人名称和付款人名称对调
//        String payerId = payFlow.getPayerId();//付款人id
//        String receiverUserId = payFlow.getReceiverUserId();//收款人id
//        String payerName = payFlow.getPayerName();//付款人名称
//        String receiverName = payFlow.getReceiverName();//收款人名称
//        payFlow.setPayerId(receiverUserId);
//        payFlow.setReceiverUserId(payerId);
//        payFlow.setPayerName(receiverName);
//        payFlow.setReceiverName(payerName);
//        // 支付成功时间
//        try {
//            logger.info("Try parse orderTime when handle refund param, the orderTime = {}", orderTimeRes);
//            payFlow.setSuccessTime(sdf.parse(orderTimeRes));
//        } catch (ParseException e) {
//            // 事实上，这里catch异常只是语法要求，银行处理成功后，时间格式不会有误，假定这里发生了异常，也仅仅是时间解析异常，银行退款让是成功的，所以继续处理后续业务
//            logger.error("Parse orderTime occurred error, the orderTime = {}", orderTimeRes);
//            payFlow.setSuccessTime(null);
//        }
//        // 是否退款
//        payFlow.setIsRefund(Constants.SHORT_ONE);
//        logger.info("Refund order success, the orderNo = {}", BOBOrderNoFormat.bob19OrderNo2OriWithER(orderNumberRes));

        /* 7. 返回PayFlow对象*/
        return null;
    }
}
