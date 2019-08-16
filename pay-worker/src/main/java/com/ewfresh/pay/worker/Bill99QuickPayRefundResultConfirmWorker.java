package com.ewfresh.pay.worker;

import com.ewfresh.pay.manager.Bill99QuickManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: Bill退款是否通过确认
 * @author: JiuDongDong
 * date: 2018/9/30.
 */
@Component
public class Bill99QuickPayRefundResultConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(Bill99QuickPayRefundResultConfirmWorker.class);
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private Bill99QuickManager bill99QuickManager;
    @Autowired
    private PayFlowService payFlowService;
    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;
    private SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMddHHmmss");
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * Description: 退款是否成功确认（非支付当天的全额退款）
     * @author: JiuDongDong
     * date: 2018/9/30 17:15
     */
    @Scheduled(cron = "44 0/30 * * * ?")
    //@Transactional
    public void confirmQuickRefund() {
        /* 1. 从Redis查询快捷退款订单 */
        final Map<Object, Object> allRefundOrderInfo =
                bill99OrderRedisService.getAllRefundOrderInfoFromRedis(Constants.PAY_BILL99_QUICK_REFUND_INFO);
        /* 1.1 没有快捷退款订单 */
        if (MapUtils.isEmpty(allRefundOrderInfo)) {
//            logger.info("There is no quick refund order now");
            return;
        }
        /* 1.2 有退款订单，处理数据 */
        Set<Object> objects = allRefundOrderInfo.keySet();
        for (final Object object : objects) {
            Object execute = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    String refundSeq = (String) object;// 退款时生成的28位的退款流水号
                    logger.info("Now is going to confirm this bill99 quick refund order, refundSeq = {}", refundSeq);
                    String strRefundInfoVo = (String) allRefundOrderInfo.get(refundSeq);
                    RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(strRefundInfoVo, RefundInfoVo.class);
                    String refundTime = refundInfoVo.getRefundTime();
                    RefundParam refundParam = refundInfoVo.getRefundParam();// 退款时封装的退款参数
                    String orderNo = refundParam.getOrderNo();// 父订单号
                    String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
                    String refundType = refundInfoVo.getRefundType();//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")、补款退款("supplement")

                    // 是否满足修改订单状态的条件
                    boolean ifModifyOrderStatus = ifModifyOrderStatus(orderNo, outRequestNo);

                    PayFlow payFlow = new PayFlow();
                    try {
                        payFlow.setSuccessTime(sdf14.parse(refundTime));
                    } catch (ParseException e) {
                        logger.error("parse String time to java.util.Date error, String refundTime = " + refundTime, e);
                        return FAIL;
                    }
                    payFlow.setChannelFlowId(refundSeq);// 快钱快捷对该笔退款生成的退款流水号，用于在快钱快捷查询

                    /* 从Redis获取退款信息---order项目退款时放入 */
                    //Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
                    //logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
                    // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
                    String finalOrderStatus = null;
                    if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = ORDER_AGREE_RETURN.toString();
                    if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = ORDER_SHUTDOWN.toString();

                    /* 2.1 从订单系统查询该订单的状态 */
                    Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
                    logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
                    // 订单系统的订单状态为0或null，说明订单状态查询有误
                    if (null == orderStatusFromOrder || INTEGER_ZERO.equals(orderStatusFromOrder)) {
                        logger.error("Get order status from order system occurred error for outRequestNo: " + outRequestNo);
                        return FAIL;
                    }

                    /* 2.2 从99bill快捷查询退单状态，并根据订单状态进行相应的业务处理 */
                    String txnStatus;
                    try {
                        // 订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起
                        // 交易类型为退货则: ‘S’—退货申请成功 ‘F’－交易失败 ‘D’—已提交收单行
                        txnStatus = getOrderStatusFrom99Bill(outRequestNo, refundSeq);
                        logger.info("Order status from 99bill is: {} of outRequestNo: {}", txnStatus, outRequestNo);
                    } catch (Exception e) {
                        logger.error("Get order status from 99Bill quick failed!!! outRequestNo = " + outRequestNo, e);
                        return FAIL;
                    }
                    logger.info("txnStatus: {}", txnStatus);
                    // 如果99bill的订单状态为null，说明订单查询有误
                    if (StringUtils.isEmpty(txnStatus)) {
                        logger.error("Get order status from 99bill is error for refundSeq = " + refundSeq +
                                ", outRequestNo = " + outRequestNo);
                        return FAIL;
                    }
                    // 如果99bill的订单状态为交易失败'F'，删除redis中的退款策略里备份的退单信息，删除orderAllowCancelHandler里备份的退单信息，将订单状态修改为失败
                    if (BILL99_Q_TXN_STATUS_RETURN_F.equals(txnStatus)) {
                        logger.info("The outRequestNo: {} has been refused by bill99Quick, refundSeq = {}, outRequestNo = {}",
                                outRequestNo, refundSeq, outRequestNo);
                        payFlow.setStatus(STATUS_1);
                        payFlowService.updatePayFlow(payFlow);
                        // 将订单系统订单状态改为2200退款失败
                        Map<String, String> params = new HashMap<>();
                        params.put(BILL99_ID, outRequestNo);
                        params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                        params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                        params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                        if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                            updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                        } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                            logger.info("Order system handle.");
                        } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                            logger.info("Order system handle.");
                        }
                        // 删除redis中的退款策略里备份的退单信息
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_QUICK_REFUND_INFO);
                        // 删除orderAllowCancelHandler里备份的退单信息
                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                        return SUCCESS;
                    }
//                    // 如果订单系统订单状态为1500~2000，说明退款已处理完毕，删除redis中的退款策略里备份的退单信息，
//                    // 删除orderAllowCancelHandler里备份的退单信息
//                    if (ORDER_AGREE_RETURN.intValue() <= orderStatusFromOrder &&
//                            orderStatusFromOrder <= ORDER_REFUSE_REFUND.intValue()) {
//                        logger.info("The outRequestNo: {} is refund by bank successful", outRequestNo);
//                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_QUICK_REFUND_INFO);
//                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
//                        continue;
//                    }
                    // 如果txnStatus = ‘S’（用户申请了退款，快钱快捷审核通过了，但尚未提交到银行进行退款）, 则不用处理
                    if (BILL99_Q_TXN_STATUS_RETURN_S.equals(txnStatus)) {
                        logger.info("The outRequestNo: {} is approved pass by operator and 99bill, " +
                                "and then 99billQuick will apply refunding to bank for this trade", outRequestNo);
                        return null;
                    }
                    // 如果订单状态为2100（取消订单时置为2100）或者1350（关闭订单时置为1350），txnStatus = ‘D’，说明退款已经到账
                    if (BILL99_Q_TXN_STATUS_RETURN_D.equals(txnStatus)) {
                        logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);
                        // bill99已经调用银行退款成功，删除redis中的退款信息，将订单状态改为1500，支付流水表的status由失败改为成功
                        payFlow.setStatus(STATUS_0);
                        payFlowService.updatePayFlow(payFlow);// 要在修改订单状态之前执行
                        // 如果满足修改订单状态条件，则修改订单状态，并删除orderAllowCancelHandler里备份的退单信息
                        ifModifyOrderStatus = ifModifyOrderStatus(orderNo, outRequestNo);// 重新确认是否满足修改订单状态的条件
                        if (ifModifyOrderStatus) {
                            logger.info("Now begin to modify order status for outRequestNo = {}", outRequestNo);
                            Map<String, String> params = new HashMap<>();
                            params.put(BILL99_ID, outRequestNo);
                            params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                            params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                            params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                            if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                            } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                                logger.info("Order system handle.");
                            } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                                logger.info("Order system handle.");
                            }
                            // 删除orderAllowCancelHandler里备份的退单信息
                            bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                        }
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_QUICK_REFUND_INFO);
                        return SUCCESS;
                    }

                    // 如果订单状态为2200，txnStatus = ‘S’，说明这不是第一次退款，且后面的这一次还在退款中
                    if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() &&
                            txnStatus.equals(BILL99_Q_TXN_STATUS_RETURN_S)) {
                        logger.info("The outRequestNo: {} applied refund quick for more time and operator " +
                                "has approved for the lase time", outRequestNo);
                        payFlow.setStatus(STATUS_2);
                        payFlowService.updatePayFlow(payFlow);
                        // 如果满足修改订单状态条件，则修改订单状态
                        if (ifModifyOrderStatus) {
                            Map<String, String> params = new HashMap<>();
                            params.put(BILL99_ID, outRequestNo);
                            params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                            params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                            params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                            if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                            } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                                logger.info("Order system handle.");
                            } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                                logger.info("Order system handle.");
                            }
                        }
                        return null;
                    }
                    // 如果订单状态为2200，txnStatus = ‘D’，说明这不是第一次退款，这次退款成功，删除redis中的退款信息，修改订单状态，支付流水表的status由处理中改为成功
                    if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() &&
                            txnStatus.equals(BILL99_Q_TXN_STATUS_RETURN_D)) {
                        logger.info("The outRequestNo: {} applied refund for more time and bank has refund " +
                                "successfully for the last time", outRequestNo);
                        // 如果满足修改订单状态条件，则修改订单状态，并删除orderAllowCancelHandler里备份的退单信息
                        if (ifModifyOrderStatus) {
                            Map<String, String> params = new HashMap<>();
                            params.put(BILL99_ID, outRequestNo);
                            params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                            params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                            params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                            if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                            } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                                logger.info("Order system handle.");
                            } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                                logger.info("Order system handle.");
                            }
                            // 删除退款时orderAllowCancelHandler里备份的退单信息
                            bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                        }
                        payFlow.setStatus(STATUS_0);
                        payFlowService.updatePayFlow(payFlow);
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_QUICK_REFUND_INFO);
                        return SUCCESS;
                    }
                    return null;
                }
            });
            String outRequestNo = (String) object;// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            String result = null == execute ? null : execute.toString();
            if (SUCCESS.equals(result)) {
                logger.info("Refund ok of outRequestNo: {}", outRequestNo);
            } else if (FAIL.equals(result)) {
                logger.error("Refund failed of outRequestNo: " + outRequestNo);
            } else {
                logger.info("Refund is in processing of outRequestNo: {}", outRequestNo);
            }
        }
    }

    /**
     * Description: 从99bill快捷查询退单状态
     * @author: JiuDongDong
     * @param outRequestNo  退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @param refundSeq     自己生成的28位的退款流水号
     * @return java.lang.Integer 订单状态
     * date: 2018/8/8 09:03
     */
    private String getOrderStatusFrom99Bill(String outRequestNo, String refundSeq) {
        // 获取退款申请时退款策略Bill99RefundPolicy中往redis中保存的退款信息
        RefundInfoVo refundInfoFromRedis =
                bill99OrderRedisService.getRefundOrderInfoFromRedis(refundSeq, PAY_BILL99_QUICK_REFUND_INFO);
        logger.info("refundInfoFromRedis = {}", JsonUtil.toJson(refundInfoFromRedis));
        String refundTime = refundInfoFromRedis.getRefundTime();//退款申请时间
        RefundParam refundParam = refundInfoFromRedis.getRefundParam();
        String receiverUserId = refundParam.getReceiverUserId();//店铺id
        String isSelfPro;// 是否自营商品，0否1是
        if (SELF_SHOP_ID.equals(receiverUserId)) {
            isSelfPro = STR_ONE;
            logger.info("The outRequestNo is a self order!!! outRequestNo = {}, isSelfPro = {}", outRequestNo, isSelfPro);
        } else {
            isSelfPro = STR_ZERO;
            logger.info("The outRequestNo is a shop order!!! outRequestNo = {}, isSelfPro = {}", outRequestNo, isSelfPro);
        }
        // 查询退款状态
        ResponseData responseData = new ResponseData();
        bill99QuickManager.queryOrder(responseData, "RFD", refundSeq, null, isSelfPro);
        // 处理查询结果
        String code = responseData.getCode();
        if (!ResponseStatus.OK.getValue().equals(code)) {
            logger.error("Fail to query refund info from bill99 quick, refundSeq = " + refundSeq + ", " +
                    "outRequestNo = " + outRequestNo);
            return null;
        }
        Object entity = responseData.getEntity();
        logger.info("com.ewfresh.pay.worker.Bill99QuickPayRefundResultConfirmWorker.getOrderStatusFrom99Bill, " +
                "responseData.getEntity() = " + ((null == entity || MapUtils.isEmpty((Map) entity)) ? "has no " +
                "query result" : JsonUtil.toJson(entity)));
        if (null == entity || MapUtils.isEmpty((Map) entity)) {
            logger.error("Can not find refund info from bill99 quick, refundSeq = " + refundSeq + ", outRequestNo" +
                    " = " + outRequestNo);
            return null;
        }
        Map respXml = (Map) entity;
        // 订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起
        // 交易类型为退货则: ‘S’—退货申请成功 ‘F’－交易失败 ‘D’—已提交收单行
        String txnStatus = (String) respXml.get(BILL99_Q_TXN_STATUS);// 交易状态
        logger.info("txnStatus = {}", txnStatus);
        return txnStatus;
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
    @Transactional(propagation = Propagation.REQUIRED)
    private boolean ifModifyOrderStatus(String orderNo, String outRequestNo) {
        // 1、如果orderId 和 outRequestNo，则表示该订单只是支付了定金，还未拆单，因此payFlow表只有1条记录，直接修改订单状态即可
        if (StringUtils.isNotBlank(orderNo) && orderNo.equals(outRequestNo)) {
            logger.info("The order payed earnest only, now is going to modify orderStatus, orderNo = {}", orderNo);
            return true;
        }
        Long orderId = Long.parseLong(orderNo);

        // 根据父订单号和子订单号查询快钱快捷、快钱网银的支付流水集合
        List<PayFlow> payFlowList = payFlowService.getBill99PayFlowsByOrderId(orderId);
        // 2、如只有1条关于快钱（网银、快捷）的支付流水，说明定金、尾款仅使用了1次快钱支付，直接修改订单状态即可
        if (CollectionUtils.isNotEmpty(payFlowList) && payFlowList.size() == INTEGER_ONE) {
            logger.info("The order only used bill99 payed one of earnest or tail, now is going to modify orderStatus, orderNo = {}", orderNo);
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
