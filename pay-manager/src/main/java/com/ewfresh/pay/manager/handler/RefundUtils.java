package com.ewfresh.pay.manager.handler;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.policy.ChoicePolicy;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/6/27 0027
 */
@Component
public class RefundUtils {

    @Autowired
    private ChoicePolicy choicePolicy;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public RefundParam getByPayFlow(RefundParams refundParams, PayFlow payFlow, BigDecimal refundAmount, BigDecimal freight) {
        if(refundAmount.compareTo(BigDecimal.ZERO) < 1){
            logger.info("the refund amount is 0 or not legitimate");
            return null;
        }
        String parentId = refundParams.getParentId();
        String orderId = refundParams.getOrderId();
        String interactionId = payFlow.getInteractionId();
        RefundParam refundParam = new RefundParam();
        refundParam.setChannelCode(payFlow.getChannelCode());      //支付渠道编码
        refundParam.setTotalAmount(refundParams.getTotalAmount()); //订单金额
        refundParam.setOutTradeNo(interactionId);     //第三方订单ID
        refundParam.setOrderNo(refundParams.getParentId());        //订单ID
        refundParam.setTradeNo(payFlow.getChannelFlowId());        //三方交易流水
        refundParam.setOutRequestNo(StringUtils.isNotBlank(orderId) ? orderId : parentId);
        refundParam.setPayFlowId(payFlow.getPayFlowId());
        refundParam.setPayFlow(payFlow);
        refundParam.setRefundAmount(refundAmount.toString());
        refundParam.setReceiverUserId(payFlow.getReceiverUserId());
        refundParam.setTradeType(refundParams.getTradeType());
        refundParam.setFreight(freight);
        Integer shopBenefitPercent = payFlow.getShopBenefitPercent();
        BigDecimal shopBenefit = refundAmount.multiply(new BigDecimal(shopBenefitPercent)).divide(new BigDecimal(Constants.INTEGER_100));
        refundParam.setEwfreshBenefitRefund(shopBenefit);
        return refundParam;
    }


    public void refund(List<RefundParam> refundParams, ResponseData responseData) throws RefundBill99ResponseNullException,
            UnsupportedEncodingException, RefundParamNullException, PayFlowFoundNullException, DocumentException,
            RefundHttpToBill99FailedException, RefundBill99HandleException, Bill99NotFoundThisOrderException,
            RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        List<PayFlow> payFlows = new ArrayList<>();
        logger.info("the refundParams for this time [refundParams = {}, refundParams = {}]",refundParams.size(), ItvJsonUtil.toJson(refundParams));
        String channelFlowIds = "";
        for (RefundParam refundParam : refundParams) {
            String channelCodeKey = refundParam.getChannelCode();
            RefundPolicy refundPolicy = choicePolicy.getRefundPolicy(channelCodeKey);
            List<PayFlow> refund = refundPolicy.refund(refundParam);
            if (CollectionUtils.isNotEmpty(refund)) {
                for (PayFlow payFlow : refund) {
                    String receiverUserId = payFlow.getReceiverUserId();
                    String nickName = getNickName(receiverUserId);
                    payFlow.setUname(nickName);
                    payFlows.add(payFlow);
                    String channelFlowId = payFlow.getChannelFlowId();
                    if (channelFlowIds.equals("")){
                        channelFlowIds += channelFlowId;
                    }else {
                        channelFlowIds = channelFlowIds + "," + channelFlowId;
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(payFlows)) {
            logger.error("Query error! There is no payFlow for: " + JsonUtil.toJson(refundParams));
            responseData.setCode(ResponseStatus.APPLY99BILLREFUNDFAILED.getValue());
            responseData.setMsg("99bill refused refund");
            return;
        }
        logger.info("the final refund payflows [payFlows = {}]",ItvJsonUtil.toJson(payFlows));
        payFlowService.addPayFlows(payFlows);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setEntity(channelFlowIds);
        responseData.setMsg(" refund success");
    }


    public String getNickName(String userId){
        String userInfo = "";
        String nickName = "";
        try {
            userInfo = accountFlowRedisService.getUserInfo(Long.valueOf(userId));
            if(StringUtils.isNotBlank(userInfo)){
                Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
                });
                Object nick = userInfoMap.get(Constants.NICK_NAME);
                if (nick != null){
                    nickName = (String) nick;
                }
            }
        } catch (Exception e) {
            logger.error("get userInfo failed",e);
        }

        return nickName;
    }


    public PayFlow getRefundPayFlow(RefundParam map, PayFlow payPayFlow) {
        PayFlow payFlow = new PayFlow();
        Short tradeType = map.getTradeType();
        Long balanceChanelFlow = balanceManager.getBalanceChanelFlow();
        payFlow.setChannelFlowId(balanceChanelFlow + "");
        payFlow.setChannelCode(Constants.UID_BALANCE);
        payFlow.setChannelName(Constants.BALANCE);
        payFlow.setTradeType(tradeType);
        String receiverUserId = payPayFlow.getReceiverUserId();
        String receiverName = payPayFlow.getReceiverName();
        String payerId = payPayFlow.getPayerId();
        String payerName = payPayFlow.getPayerName();
        payFlow.setPayerId(receiverUserId);
        payFlow.setPayerName(receiverName);
        payFlow.setReceiverUserId(payerId);
        payFlow.setReceiverName(payerName);
        String orderId = map.getOutRequestNo();
        String parentId = map.getOrderNo();
        Short payTradeType = payPayFlow.getTradeType();
        String payFlowOrderId = payTradeType == Constants.TRADE_TYPE_8 ? orderId : parentId;
        payFlow.setChannelType(StringUtils.isBlank(orderId) ? parentId : orderId);
        BigDecimal orderAmount = payPayFlow.getOrderAmount();
        payFlow.setOrderAmount(orderAmount);
        String refundAmountStr = map.getRefundAmount();
        BigDecimal refundAmount = new BigDecimal(refundAmountStr);
        payFlow.setPayerPayAmount(refundAmount);
        payFlow.setOrderId(Long.valueOf(payFlowOrderId));
        payFlow.setInteractionId(payPayFlow.getInteractionId());
        payFlow.setIsRefund(Constants.IS_REFUND_YES);
        Integer shopBenefitPercent = payPayFlow.getShopBenefitPercent();
        BigDecimal freight = map.getFreight();
        BigDecimal shopBenefit = refundAmount.subtract(freight).multiply(new BigDecimal(shopBenefitPercent)).divide(new BigDecimal(Constants.INTEGER_100));
        payFlow.setShopBenefitPercent(shopBenefitPercent);
        payFlow.setShopBenefitMoney(shopBenefit);
        payFlow.setMid(payPayFlow.getMid());
        payFlow.setTid(payPayFlow.getTid());
        payFlow.setShopId(payPayFlow.getShopId());
        payFlow.setFreight(freight);
        return payFlow;
    }
    /**
     * Description: 校验本次退款金额是否合法
     * @author DuanXiangming
     * @param refundParams
     * @return  boolean    true可退, false 退款金额大于可退金额 不可退
     * Date    2019/7/2  15:46
     */
    public boolean checkIsEnough(List<PayFlow> balancePayFlows, List<PayFlow> payflowList, RefundParams refundParams, List<PayFlow> refundPayflows){

        boolean flag = true;
        if (CollectionUtils.isEmpty(refundPayflows)){
            return flag;
        }
        BigDecimal paidAmount = BigDecimal.ZERO;         //父订单的总支付金额
        if (CollectionUtils.isNotEmpty(balancePayFlows)){
            for (PayFlow balancePayFlow : balancePayFlows) {
                BigDecimal payerPayAmount = balancePayFlow.getPayerPayAmount();
                paidAmount = paidAmount.add(payerPayAmount);
            }
        }
        if (CollectionUtils.isNotEmpty(payflowList)){
            for (PayFlow payflow : payflowList) {
                BigDecimal payerPayAmount = payflow.getPayerPayAmount();
                paidAmount = paidAmount.add(payerPayAmount);
            }
        }
        BigDecimal refundedAmount = BigDecimal.ZERO;     //涉及的父订单退款金额
        if (CollectionUtils.isNotEmpty(refundPayflows)){
            for (PayFlow payflow : refundPayflows) {
                BigDecimal payerPayAmount = payflow.getPayerPayAmount();
                refundedAmount = refundedAmount.add(payerPayAmount);
            }
        }
        BigDecimal allowToRefundAmount = paidAmount.subtract(refundedAmount); //剩余可退的金额
        BigDecimal earnestAmount = refundParams.getEarnestAmount() == null ? BigDecimal.ZERO : refundParams.getEarnestAmount();
        BigDecimal finalAmount = refundParams.getFinalAmount()== null ? BigDecimal.ZERO : refundParams.getFinalAmount();
        BigDecimal dispatchAmount = refundParams.getDispatchAmount()== null ? BigDecimal.ZERO : refundParams.getDispatchAmount();
        BigDecimal freight = refundParams.getFreight()== null ? BigDecimal.ZERO : refundParams.getFreight();
        BigDecimal refundAmount = earnestAmount.add(finalAmount).add(dispatchAmount).add(freight);//本次要退的总金额
        if (allowToRefundAmount.compareTo(refundAmount) == -1){
            logger.info("not allow to refund this time [allowToRefundAmount = {}, earnestAmount = {}," +
                    " finalAmount = {}, dispatchAmount = {}, refundAmount = {}]", allowToRefundAmount, earnestAmount, finalAmount, dispatchAmount, refundAmount);
            flag = false;
        }
        return flag;
    }
    /**
     * Description: 获取该订单所有的退款记录
     * @author DuanXiangming
     * @param refundParams
     * @return java.util.List<com.ewfresh.pay.model.PayFlow>
     * Date    2019/7/2  15:46
     */
    public List<PayFlow> getRefundPayflows(RefundParams refundParams, Short... tradeTypes){
        logger.info("this time for query refund params [refundParams = {}, tradeTypes = {}]",ItvJsonUtil.toJson(refundParams), ItvJsonUtil.toJson(tradeTypes));
        List<PayFlow> refundPayflows = null;
        String orderIdParam = refundParams.getParentId();
        String orderId = StringUtils.isBlank(orderIdParam) ? refundParams.getParentId() : orderIdParam;
        String finalBill = refundParams.getFinalBill();
        if (ArrayUtils.isNotEmpty(tradeTypes)){
            refundPayflows = payFlowService.getPayFlowsByOrderIdAndTradeType(orderId, tradeTypes);
            return refundPayflows;
        }
        if (StringUtils.isBlank(finalBill)){
            tradeTypes = new Short[]{Constants.TRADE_TYPE_2};
        }else {
            tradeTypes = new Short[]{Constants.TRADE_TYPE_2, Constants.TRADE_TYPE_9, Constants.TRADE_TYPE_17};
        }
        refundPayflows = payFlowService.getPayFlowsByOrderIdAndTradeType(orderId, tradeTypes);
        logger.info("this time for query refund result [refundPayflows = {}, tradeTypes = {}]",ItvJsonUtil.toJson(refundPayflows), ItvJsonUtil.toJson(tradeTypes));
        return refundPayflows;
    }

    /**
     * Description: 支付流水分组的方法
     * @author DuanXiangming
     * @param 
     * @return  
     * Date    2019/7/17  11:12
     */                    
    public Map<String, List<PayFlow>> groupingPayflow(List<PayFlow> payflowList, List<PayFlow> balancePayFlows, List<PayFlow> refundPayflows) {

        Map<String, List<PayFlow>> payflowGroups = new HashMap<>();

        List<PayFlow> earnestPayFlows = new ArrayList<>();
        List<PayFlow> finalPayFlows = new ArrayList<>();
        List<PayFlow> dispatchPayFlows = new ArrayList<>();
        List<PayFlow> earnestRefundPayFlows = new ArrayList<>();
        List<PayFlow> finalRefundPayFlows = new ArrayList<>();
        //余额支付的流水
        for (PayFlow balancePayFlow : balancePayFlows) {
            String interactionId = balancePayFlow.getInteractionId();
            Short tradeType = balancePayFlow.getTradeType();
            if (interactionId.contains("E")) {
                //此处E代表为定金支付的流水
                earnestPayFlows.add(balancePayFlow);
                continue;
            }
            if (interactionId.contains("R") && tradeType != Constants.TRADE_TYPE_8) {
                //此处R代表为尾款支付的流水
                finalPayFlows.add(balancePayFlow);
                continue;
            }
            if (tradeType == Constants.TRADE_TYPE_8) {
                //配货补款的情况
                dispatchPayFlows.add(balancePayFlow);
            }
        }
        //三方支付的流水
        for (PayFlow payFlow : payflowList) {
            String interactionId = payFlow.getInteractionId();
            Short tradeType = payFlow.getTradeType();
            if (interactionId.contains("E")) {
                //此处E代表为定金支付的流水
                earnestPayFlows.add(payFlow);
                continue;
            }
            if (interactionId.contains("R") && tradeType != Constants.TRADE_TYPE_8) {
                //此处R代表为尾款支付的流水
                finalPayFlows.add(payFlow);
                continue;
            }
            if (tradeType == Constants.TRADE_TYPE_8) {
                //配货补款的情况
                dispatchPayFlows.add(payFlow);
            }
        }
        //退款的流水
        if (CollectionUtils.isNotEmpty(refundPayflows)) {
            for (PayFlow refundPayflow : refundPayflows) {
                String interactionId = refundPayflow.getInteractionId();
                if (interactionId.contains("E")) {
                    earnestRefundPayFlows.add(refundPayflow);
                    continue;
                }
                if (interactionId.contains("R")) {
                    //此处R代表为尾款支付的流水
                    finalRefundPayFlows.add(refundPayflow);
                    continue;
                }
            }
        }
        payflowGroups.put(Constants.EARNEST_PAYFLOWS, earnestPayFlows);
        payflowGroups.put(Constants.FINAL_PAYFLOWS, finalPayFlows);
        payflowGroups.put(Constants.DISPATCH_PAYFLOWS, dispatchPayFlows);
        payflowGroups.put(Constants.EARNEST_REFUND_PAYFLOWS, earnestRefundPayFlows);
        payflowGroups.put(Constants.FINAL_REFUND_PAYFLOWS, finalRefundPayFlows);
        return payflowGroups;
    }
}
