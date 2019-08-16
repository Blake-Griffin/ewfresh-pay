package com.ewfresh.pay.worker;

import com.ewfresh.pay.manager.UnionpayB2CWebWapManager;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_orderId;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_origRespCode;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_txnTime;

/**
 * description: UnionPayWebWap退款是否通过确认
 * @author: JiuDongDong
 * date: 2019/5/7.
 */
@Component
public class UnionPayWebWapRefundConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(UnionPayWebWapRefundConfirmWorker.class);
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private UnionpayB2CWebWapManager unionpayWebWapManager;
    @Autowired
    private PayFlowService payFlowService;
    @Value("${httpClient.getOrderInfo}")
    private String getOrderInfoUrl;
    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;
    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Autowired
    private GetOrderStatusUtil getOrderStatusUtil;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;
    private SimpleDateFormat sdf14 = new SimpleDateFormat("yyyyMMddHHmmss");


    /**
     * Description: 退款是否成功确认
     * @author: JiuDongDong
     * date: 2019/5/7 11:00
     */
//    @Scheduled(cron = "40 0/11 * * * ?") TODO
    @Transactional
    public void confirmRefund() {
        /* 1. 从Redis查询有没有退款订单 */
        Map<Object, Object> allRefundOrderInfo = unionPayRedisService.getAllRefundOrderInfoFromRedis(PAY_UNIONPAYWEBWAP_REFUND_INFO);
        /* 1.1 没有退款订单 */
        if (MapUtils.isEmpty(allRefundOrderInfo)) {
            logger.info("There is no refund order now");
            return;
        }
        /* 1.2 有退款订单，处理数据 */
        Set<Object> objects = allRefundOrderInfo.keySet();// 获取到所有退单
        for (Object object : objects) {
            String outRequestNo = (String) object;// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            String strRefundInfoVo = (String) allRefundOrderInfo.get(outRequestNo);
            RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(strRefundInfoVo, RefundInfoVo.class);
            String refundTime = refundInfoVo.getRefundTime();
            String refundSeq = refundInfoVo.getRefundSeq();// 退款时生成的32位的退款流水号
            PayFlow payFlow = new PayFlow();
            try {
                payFlow.setSuccessTime(sdf14.parse(refundTime));
            } catch (ParseException e) {
                logger.error("parse String time to java.util.Date error, String refundTime = " + refundTime, e);
                continue;
            }
            payFlow.setChannelFlowId(refundSeq);// 退款时生成的32位退款流水号

            /* 从Redis获取退款信息---order项目退款时放入 */
            Map<String, String> redisRefundInfo = unionPayRedisService.getReturnAmountParams(outRequestNo);
            logger.info("redisRefundInfo of {} is: {}", outRequestNo, redisRefundInfo);
            String refundType = redisRefundInfo.get(REFUND_TYPE);//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
            // 如果是取消订单，则修改为1500；如果是关闭订单，则修改为1360；如果是退货退款，则修改为1900
            String finalOrderStatus = null;
            if (REFUND_TYPE_CANCEL.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_RETURN.toString();
            if (REFUND_TYPE_SHUTDOWN.equals(refundType)) finalOrderStatus = Constants.ORDER_SHUTDOWN.toString();
            if (REFUND_TYPE_REFUNDS.equals(refundType)) finalOrderStatus = Constants.ORDER_AGREE_REFUND.toString();

            /* 2.1 从订单系统查询该订单的状态 */
            Integer orderStatusFromOrder = getOrderStatusUtil.getOrderStatusFromOrder(outRequestNo);
            logger.info("orderStatus = {} of outRequestNo: {}", orderStatusFromOrder, outRequestNo);
            // 订单系统的订单状态为0或null，说明订单状态查询有误
            if (null == orderStatusFromOrder || INTEGER_ZERO == orderStatusFromOrder) {
                logger.error("Get order status from order system occurred error for outRequestNo: " + outRequestNo);
                continue;
            }
            /* 2.2 从UnionPayWebWap查询退单状态，并根据订单状态进行相应的业务处理 */
            Integer orderStatusFromUnionPay;
            try {
                orderStatusFromUnionPay = getOrderStatusFromUnionPayWebWap(outRequestNo, refundSeq);
                logger.info("Order status from UnionPayWebWap is: {} for outRequestNo: {}", orderStatusFromUnionPay, outRequestNo);
            } catch (ParseException e) {
                logger.error("Get order status from UnionPayWebWap failed!!! ", e);
                continue;
            }
            logger.info("orderStatusFromUnionPay: {}", orderStatusFromUnionPay);
            // 如果UnionPayWebWap的订单状态为-2，说明订单查询有误
            if (- INTEGER_TWO == orderStatusFromUnionPay) {
                logger.error("Get order status from UnionPayWebWap is error for outRequestNo: " + outRequestNo);
                continue;
            }
            // 如果订单系统订单状态为1500~2000，说明退款已处理完毕，删除redis中的退款策略里备份的退单信息，删除orderAllowCancelHandler里备份的退单信息
            if (ORDER_AGREE_RETURN.intValue() <= orderStatusFromOrder && orderStatusFromOrder <= ORDER_REFUSE_REFUND.intValue()) {
                logger.info("The outRequestNo: {} is refund by bank successful", outRequestNo);
                unionPayRedisService.deleteRefundOrderInfoInRedis(outRequestNo, PAY_UNIONPAYWEBWAP_REFUND_INFO);
                unionPayRedisService.delReturnAmountParams(outRequestNo);
                continue;
            }
            // 如果订单状态为2100（取消订单时置为2100）或者1350（关闭订单时没有置为1350），银联为0（用户申请了退款，运营审核通过了，且银联正在退款中）, 则不用处理
            if ((ORDER_REFUNDING.intValue() == orderStatusFromOrder.intValue() || ORDER_DISTRIBUTING.intValue() == orderStatusFromOrder.intValue())
                    && orderStatusFromUnionPay == INTEGER_ZERO) {
                logger.info("The outRequestNo: {} is approve by operator and apply UnionPayWebWap to refund", outRequestNo);
                continue;
            }
            // 如果订单状态为2100（取消订单时置为2100）或者1350（关闭订单时没有置为1350），且UnionPayWebWap为1，说明退款已经到账
            if ((ORDER_REFUNDING.intValue() == orderStatusFromOrder.intValue() || ORDER_DISTRIBUTING.intValue() == orderStatusFromOrder.intValue())
                    && orderStatusFromUnionPay == INTEGER_ONE) {
                logger.info("The outRequestNo: {} has been refund by bank successfully", outRequestNo);
                // 银联退款成功，删除redis中的退款信息，将订单状态改为1500，支付流水表的status由失败改为成功
                payFlow.setStatus(STATUS_0);
                payFlowService.updatePayFlow(payFlow);
                Map<String, String> params = new HashMap<>();
                params.put(ID, outRequestNo);
                params.put(ORDER_STATUS, finalOrderStatus);
                params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                params.put(REFUND_TYPE, refundType);
                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                unionPayRedisService.deleteRefundOrderInfoInRedis(outRequestNo, PAY_UNIONPAYWEBWAP_REFUND_INFO);
                unionPayRedisService.delReturnAmountParams(outRequestNo);
                continue;
            }
            // TODO 有拒绝退款吗
//            // 如果订单状态为2100，且UnionPayWebWap为2，说明退款申请被银行拒绝
//            if ((ORDER_REFUNDING.intValue() == orderStatusFromOrder.intValue() || ORDER_DISTRIBUTING.intValue() == orderStatusFromOrder.intValue())
//                  && orderStatusFromUnionPay == INTEGER_TWO) {
//                logger.info("The outRequestNo: " + outRequestNo + " has been refused by bank");
//                // bill99已经调用银行退款，但银行没通过，将订单系统订单状态改为2200退款失败
//                Map<String, String> params = new HashMap<>();
//                params.put(ID, outRequestNo);
//                params.put(ORDER_STATUS, ORDER_REFUND_FAILED + "");
//                params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
//                params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
//                params.put(REFUND_TYPE, refundType);
//                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
//                continue;
//            }
            // 如果订单状态为2200，且UnionPayWebWap为0，说明这不是第一次退款，且后面的这一次还在退款中，这时需将订单状态更改为退款中
            if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() && orderStatusFromUnionPay == INTEGER_ZERO) {
                logger.info("The outRequestNo: {} applied refund for more time and operator has approved for the lase " +
                        "time", outRequestNo);
                Map<String, String> params = new HashMap<>();
                params.put(ID, outRequestNo);
                params.put(ORDER_STATUS, ORDER_REFUNDING + "");
                params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                params.put(REFUND_TYPE, refundType);
                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                continue;
            }
            // 如果订单状态为2200，且UnionPayWebWap为1，说明这不是第一次退款，这次退款成功，删除redis中的退款信息，将订单状态改为1500，支付流水表的status由失败改为成功
            if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() && orderStatusFromUnionPay == INTEGER_ONE) {
                logger.info("The outRequestNo: {} applied refund for more time and bank has refund successfully " +
                        "for the last time", outRequestNo);
                Map<String, String> params = new HashMap<>();
                params.put(ID, outRequestNo);
                params.put(ORDER_STATUS, finalOrderStatus);
                params.put(BEFORE_ORDER_STATUS, orderStatusFromOrder + "");
                params.put(IF_ADD_ORDER_RECORD, SHORT_ONE + "");
                params.put(REFUND_TYPE, refundType);
                payFlow.setStatus(STATUS_0);
                payFlowService.updatePayFlow(payFlow);
                updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                unionPayRedisService.deleteRefundOrderInfoInRedis(outRequestNo, PAY_UNIONPAYWEBWAP_REFUND_INFO);
                unionPayRedisService.delReturnAmountParams(outRequestNo);
                continue;
            }
            // TODO 有拒绝退款吗
//            // 如果订单状态为2200，且UnionPayWebWap为2，说明不是第一次退款，且再次退款失败，不进行处理
//            if (ORDER_REFUND_FAILED.intValue() == orderStatusFromOrder.intValue() && orderStatusFromUnionPay == INTEGER_TWO) {
//                logger.info("The outRequestNo: {} refund failed with consecutive times", outRequestNo);
//                continue;
//            }
        }
    }


    /**
     * Description: 从UnionPayWebWap查询退单状态
     * @author: JiuDongDong
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @param refundSeq    退款时生成的32位的退款流水号
     * @return java.lang.Integer  订单状态
     * date: 2019/5/7 14:55
     */
    private Integer getOrderStatusFromUnionPayWebWap(String outRequestNo, String refundSeq) throws ParseException {
        ResponseData responseData = new ResponseData();
        // 获取退款申请时退款策略UnionPayWebWapRefundPolicy中往redis中保存的退款信息
        RefundInfoVo refundInfoFromRedis = unionPayRedisService.getRefundOrderInfoFromRedis(outRequestNo, PAY_UNIONPAYWEBWAP_REFUND_INFO);
        logger.info("refundInfoFromRedis = {}", JsonUtil.toJson(refundInfoFromRedis));
        String refundTime = refundInfoFromRedis.getRefundTime();//退款申请时间
        RefundParam refundParam = refundInfoFromRedis.getRefundParam();
        String receiverUserId = refundParam.getReceiverUserId();//店铺id
        String merchantType;
        if (SELF_SHOP_ID.equals(receiverUserId)) {
            merchantType = MERCHANT_TYPE_SELF;
            logger.info("This is a sunkfa self order!!!");
        } else {
            merchantType = MERCHANT_TYPE_OTHER;
            logger.info("This is a shop order!!!");
        }
        // 组织查询参数
        Map<String, String> params = new HashMap<>();
        params.put(param_orderId, refundSeq);//订单号? // TODO 不确定用2个形参的哪一个，refundSeq对的可能性大，这里就暂时用这个，如果测试成功，就说明这个是对的
        params.put(param_txnTime, refundTime);//退款申请时间
        // 查询交易结果
        unionpayWebWapManager.singleQuery(responseData, params);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.error("Result is null of single query, outRequestNo = " + outRequestNo);
            return -INTEGER_TWO;// 订单查询出错
        }
        Map<String, String> rspData = (Map<String, String>) entity;
        logger.info("Result of single query {} is: {}", outRequestNo, JsonUtil.toJson(rspData));
        // 查询交易成功，处理被查询交易的应答码逻辑
        String origRespCode = rspData.get(param_origRespCode);
        if (RESPONSE_CODE_OK.equals(origRespCode)) {
            return INTEGER_ONE;
        } else if ("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)) {
            // 银联处理中，需再次发起交易状态查询交易
            return INTEGER_ZERO;// 退款处理中
        } else {
            // 其他应答码为失败请排查原因
            return -INTEGER_TWO;// 订单查询出错
        }
    }

}
