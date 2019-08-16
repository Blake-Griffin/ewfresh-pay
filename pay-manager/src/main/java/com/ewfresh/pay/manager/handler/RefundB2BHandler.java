package com.ewfresh.pay.manager.handler;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: 处理B2B退款的处理器
 * @author DuanXiangming
 * Date 2019/7/17
 */
@Component
public class RefundB2BHandler {

    private static final Logger logger = LoggerFactory.getLogger(RefundB2BHandler.class);
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private RefundUtils refundUtils;
    /**
     * Description: 处理B2B退款的方法
     * @author DuanXiangming
     * @param balancePayFlows
     * @param payflowList
     * @param refundParam
     * @return void
     * Date    2019/7/17  9:36
     */
    public void refund(List<PayFlow> balancePayFlows, List<PayFlow> payflowList, RefundParams refundParam, ResponseData responseData) {
        logger.info(" there are refund b2b params  [balancePayFlows = {} , payflowList = {}]" , ItvJsonUtil.toJson(balancePayFlows), ItvJsonUtil.toJson(payflowList));
        //校验本次退款是否合法
        List<PayFlow> refundPayflows = refundUtils.getRefundPayflows(refundParam);
        boolean flag = refundUtils.checkIsEnough(balancePayFlows, payflowList, refundParam, refundPayflows);
        if (!flag){
            //退款金额校验未通过
            responseData.setCode(ResponseStatus.REFUNDAMOUNTERR.getValue());
            responseData.setMsg("refund amount is too large");
            return;
        }
        Map<String, List<PayFlow>> payFlowsMap = refundUtils.groupingPayflow(payflowList, balancePayFlows, refundPayflows);
        List<PayFlow> payFlows = new ArrayList<>();
        List<PayFlow> earnestpayflows = payFlowsMap.get(Constants.EARNEST_PAYFLOWS);
        PayFlow refundPayFlow = null;
        if (CollectionUtils.isNotEmpty(earnestpayflows)) {
            refundPayFlow = getRefundPayFlow(refundParam, earnestpayflows);
        }
        List<PayFlow> finalPayFlows = payFlowsMap.get(Constants.FINAL_PAYFLOWS);
        if (refundPayFlow == null){
            refundPayFlow = getRefundPayFlow(refundParam, finalPayFlows);
        }
        if (refundPayFlow != null){
            payFlows.add(refundPayFlow);
        }
        BigDecimal dispatchAmount = refundParam.getDispatchAmount() == null ? BigDecimal.ZERO : refundParam.getDispatchAmount();
        Short tradeType = refundParam.getTradeType();
        if (tradeType == Constants.TRADE_TYPE_17 && dispatchAmount.compareTo(BigDecimal.ZERO) == 1) {
            List<PayFlow> dispatchPayFlows = payFlowsMap.get(Constants.DISPATCH_PAYFLOWS);
            PayFlow disRefundPayFlow = getRefundPayFlow(refundParam, dispatchPayFlows);
            payFlows.add(disRefundPayFlow);
        }
        payFlowService.addPayFlows(payFlows);
        String channelFlowIds = "";
        for (PayFlow payFlow : payFlows) {
            String channelFlowId = payFlow.getChannelFlowId();
            if (channelFlowIds.equals("")){
                channelFlowIds += channelFlowId;
            }else {
                channelFlowIds = channelFlowIds + "," + channelFlowId;
            }
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("B2B refund need offline operation");
        responseData.setEntity(channelFlowIds);
        return;
    }


    public PayFlow getRefundPayFlow(RefundParams map, List<PayFlow> payflowList) {
        BigDecimal earnestAmount = map.getEarnestAmount() == null ? BigDecimal.ZERO : map.getEarnestAmount();
        BigDecimal finalAmount = map.getFinalAmount()  == null ? BigDecimal.ZERO : map.getFinalAmount();
        String payChannelType = "";
        for (PayFlow payPayFlow : payflowList) {
            String channelType = payPayFlow.getChannelType();
            Short paiedTradeType = payPayFlow.getTradeType();
            if (!channelType.contains(Constants.B2B_FLAG) && paiedTradeType == Constants.TRADE_TYPE_1){
                continue;
            }
            PayFlow payFlow = new PayFlow();
            Long balanceChanelFlow = balanceManager.getBalanceChanelFlow();
            payFlow.setChannelFlowId(balanceChanelFlow + "");
            payFlow.setChannelCode(Constants.UID_BALANCE.toString());
            payFlow.setChannelName(Constants.BALANCE);
            payFlow.setTradeType(map.getTradeType());
            String receiverUserId = payPayFlow.getReceiverUserId();
            String receiverName = payPayFlow.getReceiverName();
            String payerId = payPayFlow.getPayerId();
            String payerName = payPayFlow.getPayerName();
            payFlow.setPayerId(receiverUserId);
            payFlow.setPayerName(receiverName);
            payFlow.setReceiverUserId(payerId);
            payFlow.setReceiverName(payerName);
            String orderId = map.getOrderId();
            String parentId = map.getParentId();
            payFlow.setChannelType(StringUtils.isBlank(orderId) ? parentId : orderId);
            BigDecimal orderAmount = payPayFlow.getOrderAmount();
            payFlow.setOrderAmount(orderAmount);
            BigDecimal refundAmount = BigDecimal.ZERO;
            BigDecimal refundFreight = BigDecimal.ZERO;
            Short tradeType = map.getTradeType();
            String interactionId = payPayFlow.getInteractionId();
            if (paiedTradeType == Constants.TRADE_TYPE_1){
                refundFreight = map.getFreight()== null ? BigDecimal.ZERO : map.getFreight();
                refundAmount = earnestAmount.add(finalAmount);
                payFlow.setOrderId(Long.valueOf(parentId));
                payChannelType = channelType;
            }
            if (paiedTradeType == Constants.TRADE_TYPE_8 && tradeType == Constants.TRADE_TYPE_17){
                //配货补款需单独退款,生成Payflow
                refundAmount = map.getDispatchAmount();
                payFlow.setOrderId(payPayFlow.getOrderId());
            }
            payFlow.setInteractionId(interactionId);
            payFlow.setUname(payPayFlow.getUname());
            payFlow.setIsRefund(Constants.IS_REFUND_YES);
            payFlow.setChannelType(payChannelType);
            Integer shopBenefitPercent = payPayFlow.getShopBenefitPercent();
            BigDecimal shopBenefit = refundAmount.multiply(new BigDecimal(shopBenefitPercent)).divide(new BigDecimal(Constants.INTEGER_100));
            payFlow.setShopBenefitPercent(shopBenefitPercent);
            payFlow.setShopBenefitMoney(shopBenefit);
            payFlow.setPayerPayAmount(refundAmount.add(refundFreight));
            payFlow.setFreight(refundFreight);
            payFlow.setMid(payPayFlow.getMid());
            payFlow.setTid(payPayFlow.getTid());
            payFlow.setShopId(payPayFlow.getShopId());
            return payFlow;
        }
        return null;
    }
}
