package com.ewfresh.pay.policy.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.handler.RefundUtils;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.exception.RefundAmountMoreThanOriException;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * description: 白条退款
 *
 * @author: ZhaoQun
 * date: 2019/3/19.
 */
@Component
public class WhiteRefundPolicy  implements RefundPolicy{

    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private RefundUtils refundUtils;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final static String SELF_SHOPNAME = "自营";

    public List<PayFlow> refund(RefundParam refundParam) throws RefundAmountMoreThanOriException {
        logger.info("white refund policy handler start [refundParam = {}]", ItvJsonUtil.toJson(refundParam));
        ArrayList<PayFlow> refund = new ArrayList<>();//退款流水
        PayFlow payFlow = refundParam.getPayFlow();
        BigDecimal refundAmount = new BigDecimal(refundParam.getRefundAmount());//退款金额
        if (refundAmount.compareTo(payFlow.getPayerPayAmount()) == 1){
            throw new RefundAmountMoreThanOriException("in white refund policy, refundAmount is more than payFlow.getPayerPayAmount()");
        }
        //获取白条退款payFlow
        PayFlow whitePayFlow = getPayFlow(refundParam);
        whitePayFlow.setChannelCode(Constants.UID_WHITE);
        whitePayFlow.setChannelName(Constants.WHITE);
        whitePayFlow.setPayerPayAmount(refundAmount);
        refund.add(whitePayFlow);
        return refund;
    }

    //获取payFlow
    private PayFlow getPayFlow(RefundParam refundParam) {
        PayFlow payPayFlow = refundParam.getPayFlow();
        PayFlow payFlow = new PayFlow();
        Long balanceChanelFlow = balanceManager.getBalanceChanelFlow();
        payFlow.setChannelFlowId(balanceChanelFlow + "");
        payFlow.setTradeType(refundParam.getTradeType());
        String receiverUserId = payPayFlow.getReceiverUserId();
        String receiverName = payPayFlow.getReceiverName();
        String payerId = payPayFlow.getPayerId();
        String payerName = payPayFlow.getPayerName();
        payFlow.setPayerId(receiverUserId);
        payFlow.setPayerName(receiverName);
        payFlow.setReceiverUserId(payerId);
        payFlow.setReceiverName(payerName);
        String orderId = refundParam.getOutRequestNo();
        String parentId = refundParam.getOrderNo();
        payFlow.setChannelType(StringUtils.isBlank(orderId) ? parentId : orderId);
        BigDecimal orderAmount = payPayFlow.getOrderAmount();
        payFlow.setOrderAmount(orderAmount);
        payFlow.setOrderId(payPayFlow.getOrderId());
        payFlow.setInteractionId(payPayFlow.getInteractionId());
        payFlow.setIsRefund(Constants.IS_REFUND_YES);
        payFlow.setShopBenefitPercent(payPayFlow.getShopBenefitPercent());
        payFlow.setShopBenefitMoney(refundParam.getEwfreshBenefitRefund());
        payFlow.setFreight(refundParam.getFreight());
        return payFlow;
    }

}
