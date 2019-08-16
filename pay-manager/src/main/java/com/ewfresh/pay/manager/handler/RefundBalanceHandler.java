package com.ewfresh.pay.manager.handler;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.service.AccountFlowService;
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

/**
 * Description: 只使用余额支付的退款处理器
 *
 * @author DuanXiangming
 * Date 2018/5/22 0022
 */
@Component
public class RefundBalanceHandler {


    private static final Logger logger = LoggerFactory.getLogger(RefundBalanceHandler.class);
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private RefundUtils refundUtils;

    public void refund(List<PayFlow> balancePayFlows, List<PayFlow> payflowList, RefundParams map, ResponseData responseData) {

        //设置可退金额
        BigDecimal balanceFlow = new BigDecimal(Constants.NULL_BALANCE);
        for (PayFlow balancePayFlow : balancePayFlows) {
            BigDecimal payerPayAmount = balancePayFlow.getPayerPayAmount();
            balanceFlow = payerPayAmount.add(balanceFlow);
        }
        Long orderId = Long.valueOf(map.getParentId());
        List<PayFlow> refundPayflows = refundUtils.getRefundPayflows(map);
        if (CollectionUtils.isNotEmpty(refundPayflows)) {
            //已部分退款,减去可退金额
            for (PayFlow refundBalancePayFlow : refundPayflows) {
                BigDecimal payerPayAmount = refundBalancePayFlow.getPayerPayAmount();
                balanceFlow = balanceFlow.subtract(payerPayAmount);
            }
        }
        if (balanceFlow.compareTo(new BigDecimal(Constants.NULL_BALANCE)) == 0){
            logger.info("the surplus balance [balanceFlow = {},refundParams = {}]",balanceFlow, ItvJsonUtil.toJson(map));
            responseData.setCode(ResponseStatus.REFUNDALREADY.getValue());
            responseData.setMsg("the surplus balance is 0");
            return;
        }
        BigDecimal refundAmount = null;                    //应退的订单商品金额
        BigDecimal refundDispatchAmount = null;            //应退的订单补款金额
        String earnestBill = map.getEarnestBill();
        String finalBill = map.getFinalBill();
        Short tradeType = map.getTradeType();
        BigDecimal earnestAmount = map.getEarnestAmount();
        BigDecimal finalAmount = map.getFinalAmount();
        if (StringUtils.isBlank(earnestBill) && StringUtils.isNotBlank(finalBill) && tradeType != null && tradeType != Constants.TRADE_TYPE_17) {
            //直接支付尾款的
            refundAmount = finalAmount;
            logger.info("the only paid finalBill [finalBill = {}, refundAmount = {}]",finalBill,refundAmount);
        } else if (StringUtils.isNotBlank(earnestBill) && StringUtils.isBlank(finalBill)) {
            //只支付了定金的
            refundAmount = earnestAmount;
            logger.info("the only paid earnestBill [earnestBill = {}, refundAmount = {}]",earnestBill,refundAmount);
        } else if (StringUtils.isNotBlank(earnestBill) && StringUtils.isNotBlank(finalBill) && tradeType != null && tradeType != Constants.TRADE_TYPE_17){
            //定金尾款都支付的
            refundAmount = earnestAmount.add(finalAmount);
            logger.info("the  paid earnestBill and finalBill [ earnestBill = {}, finalBill = {}, refundAmount = {}]", earnestBill,finalBill,refundAmount);
        }else if (tradeType != null && tradeType == Constants.TRADE_TYPE_17){
            //涉及订单补款的情况
            logger.info("the  paid earnestBill and finalBill and dispatchAmount [ earnestBill = {}, finalBill = {}, dispatchAmount = {}, refundAmount = {}]", earnestBill,finalBill,refundAmount);
            BigDecimal earnest = earnestAmount == null ? BigDecimal.ZERO : earnestAmount;
            refundDispatchAmount = map.getDispatchAmount();
            refundAmount = earnest.add(finalAmount).add(refundDispatchAmount);
        }
        //判断可退金额和退款金额的大小
        if (balanceFlow.compareTo(refundAmount) == -1) {
            logger.info("the balanceFlow lt refundAmount [balanceFlow = {}, " +
                    " = {}, refundParams = {}]",balanceFlow, refundAmount, ItvJsonUtil.toJson(map));
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg(" there is not enough balance for this refund");
            return;
        }
        //生成payFlow对象
        List<PayFlow> finalRefundPayFlows = getRefundPayFlow(map, balancePayFlows);
        logger.info("the final refund balance payflow [finalRefundPayFlows = {}]",ItvJsonUtil.toJson(finalRefundPayFlows));
        payFlowService.addPayFlows(finalRefundPayFlows);
        String channelFlowIds = "";
        for (PayFlow refundPayflow : finalRefundPayFlows) {
            String channelFlowId = refundPayflow.getChannelFlowId();
            if (channelFlowIds.equals("")){
                channelFlowIds += channelFlowId;
            }else {
                channelFlowIds = channelFlowIds + "," + channelFlowId;
            }
        }
        responseData.setMsg(" order refund success");
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setEntity(channelFlowIds);
    }

    public List<PayFlow> getRefundPayFlow(RefundParams map, List<PayFlow> balancePayFlows) {
        String earnestBill = map.getEarnestBill();
        BigDecimal earnestAmount = map.getEarnestAmount() == null ? BigDecimal.ZERO : map.getEarnestAmount();
        BigDecimal finalAmount = map.getFinalAmount()  == null ? BigDecimal.ZERO : map.getFinalAmount();
        List<PayFlow> refundPayFlows = new ArrayList<>();
        for (PayFlow balancePayFlow : balancePayFlows) {
            PayFlow payFlow = new PayFlow();
            Long balanceChanelFlow = balanceManager.getBalanceChanelFlow();
            payFlow.setChannelFlowId(balanceChanelFlow + "");
            payFlow.setChannelCode(Constants.UID_BALANCE);
            payFlow.setChannelName(Constants.BALANCE);
            payFlow.setTradeType(map.getTradeType());
            String receiverUserId = balancePayFlow.getReceiverUserId();
            String receiverName = balancePayFlow.getReceiverName();
            String payerId = balancePayFlow.getPayerId();
            String payerName = balancePayFlow.getPayerName();
            payFlow.setPayerId(receiverUserId);
            payFlow.setPayerName(receiverName);
            payFlow.setReceiverUserId(payerId);
            payFlow.setReceiverName(payerName);
            String orderId = map.getOrderId();
            String parentId = map.getParentId();
            payFlow.setChannelType(StringUtils.isBlank(orderId) ? parentId : orderId);
            BigDecimal orderAmount = balancePayFlow.getOrderAmount();
            payFlow.setOrderAmount(orderAmount);
            String interactionId = balancePayFlow.getInteractionId();
            BigDecimal refundAmount = BigDecimal.ZERO;
            BigDecimal refundFreight = BigDecimal.ZERO;
            Short tradeType = map.getTradeType();
            if (interactionId.contains("E")){
                //此处E代表为定金支付的流水
                refundAmount = earnestAmount;
                refundFreight = map.getFreight();
            }
            if (interactionId.contains("R")){
                //此处R代表为尾款支付的流水
                refundAmount = finalAmount;
                if (StringUtils.isBlank(earnestBill)){
                    refundFreight = map.getFreight();
                }
            }
            payFlow.setOrderId(Long.valueOf(parentId));
            Short paidTradeType = balancePayFlow.getTradeType();
            if (paidTradeType == Constants.TRADE_TYPE_8 && tradeType == Constants.TRADE_TYPE_17){
                //此处R代表为尾款支付的流水
                refundAmount = map.getDispatchAmount();
                payFlow.setOrderId(balancePayFlow.getOrderId());
            }
            payFlow.setInteractionId(interactionId);
            payFlow.setUname(balancePayFlow.getUname());
            payFlow.setIsRefund(Constants.IS_REFUND_YES);
            Integer shopBenefitPercent = balancePayFlow.getShopBenefitPercent();
            BigDecimal shopBenefit = refundAmount.multiply(new BigDecimal(shopBenefitPercent)).divide(new BigDecimal(Constants.INTEGER_100));
            payFlow.setShopBenefitPercent(shopBenefitPercent);
            payFlow.setShopBenefitMoney(shopBenefit);
            payFlow.setPayerPayAmount(refundAmount.add(refundFreight));
            payFlow.setFreight(refundFreight);
            payFlow.setMid(balancePayFlow.getMid());
            payFlow.setTid(balancePayFlow.getTid());
            payFlow.setShopId(balancePayFlow.getShopId());
            refundPayFlows.add(payFlow);
        }
        return refundPayFlows;
    }

}
