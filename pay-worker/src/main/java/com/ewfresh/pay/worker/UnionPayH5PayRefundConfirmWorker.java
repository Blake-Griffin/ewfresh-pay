package com.ewfresh.pay.worker;

import com.alibaba.fastjson.JSONObject;
import com.ewfresh.pay.manager.UnionPayH5PayManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: UnionPayH5Pay退款是否通过确认（只适用于取消订单、关闭订单。退货退款、补款退款等所有其它情况不适用，需相关开发人员自行根据自己业务逻辑开发）
 * @author: JiuDongDong
 * date: 2019/5/17.
 */
@Component
public class UnionPayH5PayRefundConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(UnionPayH5PayRefundConfirmWorker.class);
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private UnionPayH5PayManager unionpayH5PayManager;
    @Autowired
    private PayFlowService payFlowService;
    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;
    private SimpleDateFormat sdf14 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * Description: 退款是否成功确认
     * @author: JiuDongDong
     * date: 2019/5/17 15:19
     */
    @Scheduled(cron = "33 0/11 * * * ?")
    @Transactional
    public void confirmRefund() {
        /* 1. 从Redis查询有没有退款订单 */
        Map<Object, Object> allRefundOrderInfo = unionPayRedisService.getAllRefundOrderInfoFromRedis(PAY_UNIONPAYH5PAY_REFUND_INFO);
        /* 1.1 没有退款订单 */
        if (MapUtils.isEmpty(allRefundOrderInfo)) {
            //logger.info("There is no H5Pay refund order now");
            return;
        }
        /* 1.2 有退款订单，处理数据 */
        Set<Object> objects = allRefundOrderInfo.keySet();// 获取到所有退单
        logger.info("All UnionPay H5Pay orders are: {}", JsonUtil.toJson(objects));
        for (Object object : objects) {
            String refundSeq = (String) object;// 退款时生成的32位的退款流水号
            String strRefundInfoVo = (String) allRefundOrderInfo.get(refundSeq);
            RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(strRefundInfoVo, RefundInfoVo.class);
            String refundTime = refundInfoVo.getRefundTime();
            RefundParam refundParam = refundInfoVo.getRefundParam();
            String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            String refundType = refundInfoVo.getRefundType();//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")、补款退款("supplement")
            logger.info("Current refund orderNo = {}", outRequestNo);

            PayFlow payFlow = new PayFlow();
            try {
                payFlow.setSuccessTime(sdf14.parse(refundTime));
            } catch (ParseException e) {
                logger.error("Parse String time to java.util.Date error, String refundTime = " + refundTime, e);
                continue;
            }
            payFlow.setChannelFlowId(refundSeq);// 退款时生成的32位退款流水号

            /* 从Redis获取退款信息---order项目退款时放入（补款退款、退货退款并不会放） */
            //Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            //logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
            String finalOrderStatus = null;
            if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_RETURN.toString();
            if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = Constants.ORDER_SHUTDOWN.toString();
            //if (REFUND_TYPE_REFUNDS.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_REFUND.toString();


            /* 2.1 从订单系统查询该订单的状态 */
            Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
            logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
            // 订单系统的订单状态为0或null，说明订单状态查询有误
            if (null == orderStatusFromOrder || INTEGER_ZERO == orderStatusFromOrder) {
                logger.error("Get order status from order system occurred error for outRequestNo: " + outRequestNo);
                continue;
            }
            /* 2.2 从UnionPayH5Pay查询退单状态，并根据订单状态进行相应的业务处理 */
            Integer orderStatusFromUnionPay;
            try {
                orderStatusFromUnionPay = getOrderStatusFromUnionPayH5Pay(outRequestNo, refundSeq);
                logger.info("Order status from UnionPayH5Pay is: {} for outRequestNo: {}", orderStatusFromUnionPay, outRequestNo);
            } catch (ParseException e) {
                logger.error("Get order status from UnionPayH5Pay failed!!! ", e);
                continue;
            }
            logger.info("orderStatusFromUnionPay: {}", orderStatusFromUnionPay);
            // 如果UnionPayH5Pay的订单状态为-2，说明订单查询有误
            if (- INTEGER_TWO == orderStatusFromUnionPay) {
                logger.error("Get order status from UnionPayH5Pay is error for outRequestNo: " + outRequestNo);
                continue;
            }
            // 如果UnionPayH5Pay的订单状态为-3，说明银联处理失败，停止轮询
            if (- INTEGER_THREE == orderStatusFromUnionPay) {
                logger.error("UnionPay QRCode refund failed! outRequestNo = " + outRequestNo);
                // 更新payFlow的状态为失败
                payFlow.setStatus(STATUS_1);
                payFlowService.updatePayFlow(payFlow);
                // 删除Redis中的退款策略放入的退款信息
                unionPayRedisService.deleteRefundOrderInfoInRedis(outRequestNo, PAY_UNIONPAYH5PAY_REFUND_INFO);
                // 删除Redis中order放入的退款信息
                unionPayRedisService.delReturnAmountParams(outRequestNo);
                continue;
            }
            // 如果UnionPayH5Pay为1，说明退款已经到账
            if (orderStatusFromUnionPay.intValue() == INTEGER_ONE.intValue()) {
                logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);
                // 银联退款成功，删除redis中的退款信息，支付流水表的status由退款中改为成功，修改取消订单和关闭订单的订单状态，其他退款操作不作处理
                payFlow.setStatus(STATUS_0);
                payFlowService.updatePayFlow(payFlow);
                Map<String, String> params = new HashMap<>();
                params.put(ID, outRequestNo);
                params.put(ORDER_STATUS, finalOrderStatus);
                params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                params.put(REFUND_TYPE, refundType);
                if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                    updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                    logger.info("Order system handle.");
                } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                    logger.info("Order system handle.");
                }
                unionPayRedisService.deleteRefundOrderInfoInRedis(refundSeq, PAY_UNIONPAYH5PAY_REFUND_INFO);
                unionPayRedisService.delReturnAmountParams(outRequestNo);
                continue;
            }
        }
    }


    /**
     * Description: 从UnionPayH5Pay查询退单状态
     * @author: JiuDongDong
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @param refundSeq    退款时生成的退货订单号（msgSrcId + 生成28位的退款流水号,如：56152019070409445616900000007570）
     * @return java.lang.Integer  订单状态
     * date: 2019/5/7 14:55
     */
    private Integer getOrderStatusFromUnionPayH5Pay(String outRequestNo, String refundSeq) throws ParseException {
        ResponseData responseData = new ResponseData();
        // 获取退款申请时退款策略UnionPayH5PayRefundPolicy中往redis中保存的退款信息
        RefundInfoVo refundInfoFromRedis = unionPayRedisService.getRefundOrderInfoFromRedis(refundSeq, PAY_UNIONPAYH5PAY_REFUND_INFO);
        logger.info("refundInfoFromRedis = {}", JsonUtil.toJson(refundInfoFromRedis));
        String refundTime = refundInfoFromRedis.getRefundTime();//退款申请时间
        RefundParam refundParam = refundInfoFromRedis.getRefundParam();
        String receiverUserId = refundParam.getReceiverUserId();//店铺id
        // 查询交易结果
        unionpayH5PayManager.refundQuery(responseData, refundSeq);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.error("Result is null of single query, outRequestNo = " + outRequestNo);
            return -INTEGER_TWO;// 订单查询出错
        }
        JSONObject rspData = (JSONObject) entity;
        logger.info("Result of single query refund order {} is: {}", outRequestNo, rspData);
        // （退货查询接口的refundStatus有4种状态：SUCCESS成功、FAIL失败、PROCESSING处理中、UNKNOWN异常）
        String refundStatus = (String) rspData.get(REFUND_STATUS);//响应报文里的退款状态，退款只有成功和失败，没有临界状态
        String status = (String) rspData.get(STATUS);//响应报文里的退款状态
        if (SUCCESS.equals(refundStatus)) {
            return INTEGER_ONE;
        } else if (FAIL.equals(refundStatus)) {
            return -INTEGER_THREE;//银联处理失败
        } else {
            return -INTEGER_TWO;// 其他应答码为失败请排查原因
        }
    }

}
