package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.configure.Bill99QuickPayConfigure;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.bill99quick.TransInfo;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99quick.Post;
import com.ewfresh.pay.util.bob.BOBRefundSeqFormat;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: Bill99快捷支付退款策略
 * @author: JiuDongDong
 * date: 2018/9/29.
 */
@Component
public class Bill99QuickRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99QuickPayConfigure bill99QuickPayConfigure;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    private static final String TXN_MSG_CONTENT = "TxnMsgContent";// 消费交易节点
    private static final String ERROR_MSG_CONTENT = "ErrorMsgContent";// 错误信息节点
    private static final String RESPONSE_CODE = "responseCode";// 响应码
    private static final String RESPONSE_TEXT_MESSAGE = "responseTextMessage";// 响应信息
    private static final String ERROR_CODE = "errorCode";// 错误码
    private static final String ERROR_MESSAGE = "errorMessage";// 错误信息
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Description: 传入退款请求参数，向Bill99发送退款请求，返回支付流水对象(线上使用的是RefundController接口退款)
     * @author: JiuDongDong
     * @param refundParam 退款请求参数
     * @return com.ewfresh.pay.model.PayFlow 退款成功后封装的支付流水
     * date: 2018/9/29 11:21
     */
    @Override
    public List<PayFlow> refund(RefundParam refundParam) throws
            RefundParamNullException, PayFlowFoundNullException, UnsupportedEncodingException,
            RefundHttpToBill99FailedException, RefundBill99ResponseNullException,
            DocumentException, RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, TheBankDoNotSupportRefundTheSameDay {
        logger.info("Refund by Bill99 mas system START, the params = {}", JsonUtil.toJson(refundParam));
        /* 0. 首先先查看该订单当前是否存在尚未完成的退款，未完成则终止 */
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id，也就是支付时快钱快捷分配的参考号）
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
        logger.info("ShopId = {}, refundType = {}", receiverUserId, refundType);

        // 1.1.1 该笔订单使用快捷支付的金额
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();

        // 生成28位的退款流水号 + E/R
        String refundSequence =
                BOBRefundSeqFormat.orderNo2UnionPayRefundSequence(outRequestNo) + interactionId.substring(interactionId.length() - 1);

        /* 2. 封装退款请求参数 */
        // 2.1 从bill99QuickPayConfigure获取部分配置信息
//        Map<String, String> params = new HashMap<>();
        String merchantId;// 商户号
        String merId = bill99QuickPayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 商户号(自营)
        String terminalId;// 终端号
        String terminalId1 = bill99QuickPayConfigure.getTerminalId1();// 存管终端号
        String terminalIdNotHat1 = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String isSelfPro;// 是否自营商品，0否1是

        /* 1.1 根据自营和非自营选择商户号、终端号 */
        if (SELF_SHOP_ID.equals(receiverUserId)) {
            merchantId = merIdNotHat;
            terminalId = terminalIdNotHat1;
            isSelfPro = STR_ONE;
        } else {
            merchantId = merId;
            terminalId = terminalId1;
            isSelfPro = STR_ZERO;
        }
        logger.info("The merId is: {}", merId);
        String refundUrl = bill99QuickPayConfigure.getRefundUrl();// 退款请求地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码
        String entryTime = simpleDateFormat.format(new Date());//商户端交易时间
        //设置手机动态鉴权节点
        TransInfo transInfo= new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        String str1Xml="";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<TxnMsgContent>";
        str1Xml += "<interactiveStatus>" + "TR1" + "</interactiveStatus>";
        str1Xml += "<txnType>" + TXNTYPE_RFD + "</txnType>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<entryTime>" + entryTime + "</entryTime>";
        str1Xml += "<amount>" + refundAmount + "</amount>";
        str1Xml += "<externalRefNumber>" + refundSequence + "</externalRefNumber>";// 外部跟踪编号
        str1Xml += "<origRefNumber>" + tradeNo + "</origRefNumber>";// 原检索参考号，对应原交易的检索参考号
        str1Xml += "</TxnMsgContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of refund order to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(refundUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when refund order to bill99 mas for outRequestNo = " + outRequestNo, e);
            throw new RefundHttpToBill99FailedException(e);
        }
        logger.info("The result of refund order from bill99 is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The refund info from Bill99 mas is null!!! refundParam = " + JsonUtil.toJson(refundParam));
            throw new RefundBill99ResponseNullException("The refund info from Bill99 mas is null for outRequestNo = "
                    + outRequestNo);// 退款响应为空
        }
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        logger.info("The responseCode = {}, responseTextMessage = {}", responseCode, responseTextMessage);
        logger.info("The errorCode = {}, errorMessage = {}", errorCode, errorMessage);
        if ("O2".equals(responseCode)) {
            logger.error("Bill99 not found this order: outRequestNo = " + outRequestNo);
            logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            throw new Bill99NotFoundThisOrderException("Bill99 not found this order: outRequestNo = " + outRequestNo);
        }
        if ("OC".equals(responseCode)) {
            logger.error("Error occurred when refund order to bill99 mas for outRequestNo = " + outRequestNo);
            logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            throw new RefundAmountMoreThanOriException("Refund amount is more than original order amount for " +
                    "outRequestNo = " + outRequestNo);// 退款金额大于订单金额
        }
        if ("40".equals(responseCode)) {
            logger.error("Error occurred when refund order to bill99 mas for outRequestNo = " + outRequestNo);
            logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            throw new TheBankDoNotSupportRefundTheSameDay("The bank do not support refund on the same day with pay, " +
                    "outRequestNo = " + outRequestNo);// 农业银行,交通银行,兴业银行,光大银行,中信银行,邮储银行,上海银行
                                                      // 以上这几个银行是不支持当天申请退款的，隔天是可以申请的
        }
        // 交易状态, 如交易类型为退货则，‘F’－交易失败 ‘S’—退货申请成功 ‘D’—已提交收单行
        String txnStatus = (String) respXml.get(BILL99_Q_TXN_STATUS);
        if (BILL99_Q_TXN_STATUS_RETURN_F.equals(txnStatus)) {
            logger.error("Refund failed with txnStatus = " + txnStatus + ", outRequestNo = " + outRequestNo);
            logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            logger.error("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);
            throw new RefundBill99HandleException("refund apply failed with error code: " + responseCode + ", " +
                    "txnStatus = " + txnStatus);
        }
        // 1.2 判断是不是当天全额退款，根据是否当天全额退款及txnStatus设定payFlow的status
        Short successStatus = SHORT_TWO;//状态 0:成功,1:失败,2:处理中
        Date successTime = payFlow.getSuccessTime();// 该笔订单的支付时间
        Date now = new Date();
        Date successTime1 = DateUtil.getFutureMountDaysStartWithOutHMS(successTime, INTEGER_ZERO);
        Date now1 = DateUtil.getFutureMountDaysStartWithOutHMS(now, INTEGER_ZERO);
        if (((!BILL99_Q_TXN_STATUS_RETURN_F.equals(txnStatus))
                && (successTime1.compareTo(now1) == INTEGER_ZERO)
                && (payerPayAmount.compareTo(new BigDecimal(refundAmount)) == INTEGER_ZERO))
                || (RESPONSE_CODE_OK.equals(responseCode) && ("已提交收单行".equals(responseTextMessage)))
                || (RESPONSE_CODE_OK.equals(responseCode) && (BILL99_Q_TXN_STATUS_RETURN_D.equals(txnStatus)))) {
            // 退款成功
            logger.info("Refund success, outRequestNo = {}, refundAmount = {}", outRequestNo, refundAmount);
            successStatus = SHORT_ZERO;
            // 修改订单状态，即使失败了也要将退款流水持久化
            try {
                // 修改订单状态，首先判断是否满足修改订单状态的条件，满足则修改，不满足则会在worker里修改
                boolean ifModifyOrderStatus = ifModifyOrderStatus(orderNo, outRequestNo);
                logger.info("ifModifyOrderStatus = {} of outRequestNo = {}", ifModifyOrderStatus, outRequestNo);
                // 如果满足修改订单状态条件，则修改订单状态为退款成功1500，并删除orderAllowCancelHandler里备份的退单信息
                if (ifModifyOrderStatus) {
                    /* 从订单系统查询该订单的状态 */
                    Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
                    logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
                    // 订单系统的订单状态为0或null，说明订单状态查询有误
                    if (null == orderStatusFromOrder || INTEGER_ZERO == orderStatusFromOrder) {
                        logger.error("Get order status from order system occurred error for outRequestNo: " + outRequestNo);
                    } else {
                        // 2.组织参数
                        Map<String, String> params = new HashMap<>();
                        params.put(BILL99_ID, outRequestNo);
                        // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
                        String finalOrderStatus = null;
                        if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = ORDER_AGREE_RETURN.toString();
                        if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = ORDER_SHUTDOWN.toString();
                        //if (REFUND_TYPE_REFUNDS.equals(refundType)) finalOrderStatus = ORDER_AGREE_REFUND.toString();
                        params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                        params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                        params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ZERO + "");// 是否添加订单操作记录，0否1是
                        params.put(REFUND_TYPE, refundType);

                        // 3.订单修改状态信息放入Redis用worker修改，避免出现数据库死锁
                        if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                            bill99OrderRedisService.putToUpdateStatusOrderInfo(params, QUICK_PAY_MODIFY_ORDER_STATUS);
                        } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                            logger.info("Order system handle.");
                        } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                            logger.info("Order system handle.");
                        }
                        // 4.删除orderAllowCancelHandler里备份的退单信息
                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                    }
                }
            } catch (Exception e) {
                logger.error("Refund is over, money has been return back to customer, but modify order status failed!!! " +
                        "outRequestNo = " + outRequestNo + ", orderNo = " + orderNo, e);
            }
        }

        // todo 其它异常
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Refund failed with responseCode = " + responseCode + ", outRequestNo = " + outRequestNo);
            logger.error("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);
            throw new RefundBill99HandleException(StringUtils.isNotBlank(responseTextMessage) ?
                    responseTextMessage : (StringUtils.isNotBlank(errorMessage) ? errorMessage : "快钱异常"));
        }

        String transTime = (String) respXml.get(BILL99_Q_TRANS_TIME);// 快钱完成时间
        String refNumber = (String) respXml.get(BILL99_Q_REF_NUMBER);// 针对快钱消费、退款快钱返回的检索号

        /* 5. 封装PayFlow信息 */
        // 5.1 设置支付流水信息
        payFlow.setOrderId(Long.valueOf(orderNo));
//        payFlow.setChannelFlowId(refNumber);
        payFlow.setShopBenefitPercent(shopBenefitPercent);
        payFlow.setFreight(freight);
        payFlow.setChannelFlowId(refundSequence);
        payFlow.setPayerPayAmount(new BigDecimal(refundAmount));//付款方支付金额
        payFlow.setTradeType(tradeType);//交易类型
        payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
        payFlow.setDesp(refundType);//订单描述：退款
        payFlow.setIsRefund(SHORT_ONE);// 是否退款
        payFlow.setStatus(successStatus);// 申请成功，尚未确定是否退款成功，数据库：状态 0:成功,1:失败,2处理中
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
        // 退款申请时间，快钱退款完成时间
        try {
            payFlow.setSuccessTime(sdf.parse(transTime));
        } catch (ParseException e) {
            logger.error("Error occurred when parse entryTime for outRequestNo = " +
                    outRequestNo + ", entryTime = " + entryTime + ", transTime = " + transTime);
            payFlow.setSuccessTime(new Date());
        }
        // 银行退款流水号
        payFlow.setReturnFlowId(refNumber);
        // 第三方交互订单号
        payFlow.setInteractionId(refundSequence);

        logger.info("Refund order apply success, the orderNo = {}", outTradeNo);
        // 5.3 如果退款流程尚未完成，退款信息放入Redis轮询
        if (SHORT_ONE.shortValue() == successStatus || SHORT_TWO.shortValue() == successStatus) {
            RefundInfoVo refundInfoVo = new RefundInfoVo();
            refundInfoVo.setRefundParam(refundParam);
            refundInfoVo.setRefundSeq(refundSequence);// 自己生成的28位的退款流水号
//        refundInfoVo.setRefundSeq(refNumber);// 快钱快捷对该笔退款生成的退款流水号，用于在快钱快捷查询
//        refundInfoVo.setRefundSeq(outRequestNo);// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            refundInfoVo.setRefundTime(entryTime);
            refundInfoVo.setIsSelfPro(isSelfPro);// 是否自营
            refundInfoVo.setRefundType(refundType);// 退款类型
            bill99OrderRedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_BILL99_QUICK_REFUND_INFO);
        }

        /* 6. 返回PayFlow对象*/
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;
    }

    /**
     * Description: 根据payFlow表的退款流水的status判断是否更改订单状态。最多有2条快捷退款流水（即支付定金用了快捷，尾款也用了快捷）。
     *              如果该笔订单的所有退款流水都成功，则返回true。
     *              如果orderId 和 outRequestNo相同，则表示该订单只是支付了定金，还未拆单，因此payFlow表只有1条记录，直接修改订单状态即可。
     * @author: JiuDongDong
     * @param orderNo  父订单号
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @return boolean 是否要修改订单状态
     * date: 2018/10/17 17:10
     */
    private boolean ifModifyOrderStatus(String orderNo, String outRequestNo) {
        // 1、如果orderId 和 outRequestNo，则表示该订单只是支付了定金，还未拆单，因此payFlow表只有1条记录，直接修改订单状态即可
        if (StringUtils.isNotBlank(orderNo) && orderNo.equals(outRequestNo)) {
            logger.info("The order payed earnest only, modify orderStatus to 1500, orderNo = {}", orderNo);
            return true;
        }
        Long orderId = Long.parseLong(orderNo);

        // 根据父订单号和子订单号查询快钱快捷、快钱网银的支付流水集合
        List<PayFlow> payFlowList = payFlowService.getBill99PayFlowsByOrderId(orderId);
        // 2、如只有1条关于快钱（网银、快捷）的支付流水，说明定金、尾款仅使用了1次快钱支付，直接修改订单状态即可
        if (CollectionUtils.isNotEmpty(payFlowList) && payFlowList.size() == INTEGER_ONE) {
            logger.info("The order only used bill99 payed one of earnest or tail, modify orderStatus to 1500, orderNo = {}", orderNo);
            return true;
        }
        // 3、如果有2条关于快钱（网银、快捷）的支付流水，说明定金、尾款都使用了快钱支付，这时要进行2次退款，这时需要
        // 遍历每条流水（最多2条，1条定金、1条尾款）判断是否都已退款成功，只要有1条不成功就不改订单状态
        for (PayFlow payFlow : payFlowList) {
            Short status = payFlow.getStatus();// 状态 0:成功,1:失败,2处理中
            if (SHORT_ONE.shortValue() == status || SHORT_TWO.shortValue() == status) {
                return false;
            }
        }
        return true;
    }

}
