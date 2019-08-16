package com.ewfresh.pay.worker;

import com.ewfresh.pay.manager.Bill99Manager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.GatewayRefundQueryResultDto;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.Constants.REFUND_TYPE_SUPPLEMENT;

/**
 * description: Bill退款是否通过确认
 * @author: JiuDongDong
 * date: 2018/8/9.
 */
@Component
public class Bill99RefundConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(Bill99RefundConfirmWorker.class);
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private Bill99Manager bill99Manager;
    @Autowired
    private PayFlowService payFlowService;
    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;
    private SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMddHHmmss");
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * Description: 退款是否成功确认
     * @author: JiuDongDong
     * date: 2018/8/10 14:48
     */
    @Scheduled(cron = "30 0/30 * * * ?")
    @Transactional
    public void confirmRefund() {
        /* 1. 从Redis查询退款订单 */
        final Map<Object, Object> allRefundOrderInfo =
                bill99OrderRedisService.getAllRefundOrderInfoFromRedis(PAY_BILL99_REFUND_INFO);
        /* 1.1 没有退款订单 */
        if (MapUtils.isEmpty(allRefundOrderInfo)) {
            //logger.info("There is no refund order now");
            return;
        }
        /* 1.2 有退款订单，处理数据 */
        Set<Object> objects = allRefundOrderInfo.keySet();
        for (final Object object : objects) {
            Object execute = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    String refundSeq = (String) object;// 退款时生成的28位的退款流水号
                    logger.info("Now is going to confirm this bill99 refund order, refundSeq = {}", refundSeq);
                    String strRefundInfoVo = (String) allRefundOrderInfo.get(refundSeq);
                    RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(strRefundInfoVo, RefundInfoVo.class);
                    String refundTime = refundInfoVo.getRefundTime();
                    RefundParam refundParam = refundInfoVo.getRefundParam();// 退款时封装的退款参数
                    String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
                    //退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")、补款退款("supplement")
                    String refundType = refundInfoVo.getRefundType();

                    PayFlow payFlow = new PayFlow();
                    try {
                        payFlow.setSuccessTime(sdf14.parse(refundTime));
                    } catch (ParseException e) {
                        logger.error("parse String time to java.util.Date error, String refundTime = " + refundTime, e);
                        return FAIL;
                    }
                    payFlow.setChannelFlowId(refundSeq);// 退款时生成的28位的退款流水号

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
                    /* 2.2 从99bill查询退单状态，并根据订单状态进行相应的业务处理 */
                    Integer orderStatusFrom99Bill;
                    try {
                        orderStatusFrom99Bill = getOrderStatusFrom99Bill(outRequestNo, refundSeq);
                        logger.info("Order status from 99bill is: {} for outRequestNo: {}", orderStatusFrom99Bill, outRequestNo);
                    } catch (ParseException e) {
                        logger.error("Get order status from 99Bill failed!!! outRequestNo = " + outRequestNo, e);
                        return FAIL;
                    } catch (Exception e) {
                        logger.error("Get order status from 99Bill failed!!! outRequestNo = " + outRequestNo, e);
                        return FAIL;
                    }
                    logger.info("orderStatusFrom99Bill: {}", orderStatusFrom99Bill);
                    // 如果99bill的订单状态为-2，说明订单查询有误
                    if (-INTEGER_TWO == orderStatusFrom99Bill) {
                        logger.error("Get order status from 99bill is error for outRequestNo: " + outRequestNo);
                        return FAIL;
                    }
                    // 如果bill99为0, 快钱正在处理中，不用处理
                    if (orderStatusFrom99Bill == INTEGER_ZERO) {
                        logger.info("The outRequestNo: {} is approve by operator and apply 99bill to refund", outRequestNo);
                        return null;
                    }
                    // 如果99bill状态为1，说明快钱已申请银行退款
                    if (orderStatusFrom99Bill == INTEGER_ONE) {
                        logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);
                        // bill99已经调用银行退款成功，删除redis中的退款信息，将订单状态改为1500，支付流水表的status由失败改为成功
                        payFlow.setStatus(STATUS_0);
                        payFlowService.updatePayFlow(payFlow);
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
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_REFUND_INFO);
                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                        return SUCCESS;
                    }
                    // 如果99bill状态为2，说明退款申请被银行拒绝
                    if (orderStatusFrom99Bill == INTEGER_TWO) {
                        logger.error("The outRequestNo: " + outRequestNo + " has been refused by bank");
                        // bill99已经调用银行退款，但银行没通过，将订单系统订单状态改为2200退款失败
                        payFlow.setStatus(STATUS_1);
                        payFlowService.updatePayFlow(payFlow);
                        Map<String, String> params = new HashMap<>();
                        params.put(BILL99_ID, outRequestNo);
                        params.put(BILL99_ORDER_STATUS, ORDER_REFUND_FAILED + "");
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
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_REFUND_INFO);
                        // 删除orderAllowCancelHandler里备份的退单信息
                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
                        return FAIL;
                    }
                    //目前订单系统没有对2200的订单状态做处理，包括取消订单、关闭订单、退货退款、补款退款
                    // 如果订单状态为2200，且99bill为1，说明这不是第一次退款，这次退款成功，删除redis中的退款信息，修改订单状态，支付流水表的status由处理中改为成功
                    if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() && orderStatusFrom99Bill == INTEGER_ONE) {
                        logger.info("The outRequestNo: {} applied refund for more time and bank has refund successfully for the last time", outRequestNo);
                        Map<String, String> params = new HashMap<>();
                        params.put(BILL99_ID, outRequestNo);
                        params.put(BILL99_ORDER_STATUS, finalOrderStatus);
                        params.put(BILL99_BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                        params.put(BILL99_IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                        payFlow.setStatus(STATUS_0);
                        payFlowService.updatePayFlow(payFlow);
                        if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                            updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                        } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                            logger.info("Order system handle.");
                        } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                            logger.info("Order system handle.");
                        }
                        bill99OrderRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_BILL99_REFUND_INFO);
                        bill99OrderRedisService.delReturnAmountParams(outRequestNo);
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
     * Description: 从99bill查询退单状态
     * @author: JiuDongDong
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @param refundSeq    退款时生成的28位的退款流水号
     * @return java.lang.Integer 订单状态
     * date: 2018/8/8 09:03
     */
    private Integer getOrderStatusFrom99Bill(String outRequestNo, String refundSeq) throws ParseException {
        ResponseData responseData = new ResponseData();
        // 获取退款申请时退款策略Bill99RefundPolicy中往redis中保存的退款信息
        RefundInfoVo refundInfoFromRedis = bill99OrderRedisService.getRefundOrderInfoFromRedis(refundSeq, PAY_BILL99_REFUND_INFO);
        logger.info("refundInfoFromRedis = {}", JsonUtil.toJson(refundInfoFromRedis));
        String refundTime = refundInfoFromRedis.getRefundTime();//退款申请时间
        RefundParam refundParam = refundInfoFromRedis.getRefundParam();
        String receiverUserId = refundParam.getReceiverUserId();//店铺id
        String merchantType;
        if (SELF_SHOP_ID.equals(receiverUserId)) {
            merchantType = MERCHANT_TYPE_SELF;
            logger.info("The outRequestNo is a ewfresh order!!!");
        } else {
            merchantType = MERCHANT_TYPE_OTHER;
            logger.info("The outRequestNo is a shop order!!!");
        }
        // 计算退款生成时间起点和终点
        String startTime = sdf8.format(DateUtil.getFutureMountDaysStartWithOutHMS(sdf14.parse(refundTime), - INTEGER_ONE));
        String endTime = sdf8.format(DateUtil.getFutureMountDaysStartWithOutHMS(sdf14.parse(refundTime), INTEGER_ONE));
        logger.info("startTime: {}", startTime);
        logger.info("endTime: {}", endTime);
        logger.info("Send to Bill99ManagerImpl.queryRefundOrder params: refundSeq = {}, requestPage = 1, status = 1, merchantType = {}", refundSeq, merchantType);
        bill99Manager.queryRefundOrder(responseData, startTime, endTime, refundSeq,"", "" + INTEGER_ONE, "" + INTEGER_ONE, merchantType);
        Object entity = responseData.getEntity();
        logger.info("Bill99RefundConfirmWorker.getOrderStatusFrom99Bill, responseData.getEntity() = "
                + ((null == entity || CollectionUtils.isEmpty((Collection) entity)) ? "has no query result" : JsonUtil.toJson(entity)));
        if (null == entity || CollectionUtils.isEmpty((Collection) entity)) return - INTEGER_TWO;
        List<GatewayRefundQueryResultDto> refundResultDtoList = (List<GatewayRefundQueryResultDto>) entity;
        GatewayRefundQueryResultDto gatewayRefundQueryResultDto = refundResultDtoList.get(refundResultDtoList.size() - INTEGER_ONE);
        String status = gatewayRefundQueryResultDto.getStatus();
        logger.info("status: {}", status);
        return Integer.valueOf(status);
    }

}
