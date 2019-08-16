package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.RefundManager;
import com.ewfresh.pay.manager.handler.RefundB2BHandler;
import com.ewfresh.pay.manager.handler.RefundBalanceAndCancelHandler;
import com.ewfresh.pay.manager.handler.RefundBalanceHandler;
import com.ewfresh.pay.manager.handler.RefundChanelHandler;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/27 0027
 */
@Component
public class RefundManagerImpl implements RefundManager {

    private static final Logger logger = LoggerFactory.getLogger(RefundManagerImpl.class);

    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private RefundBalanceAndCancelHandler refundBalanceAndCancelHandler;
    @Autowired
    private RefundBalanceHandler refundBalanceHandler;
    @Autowired
    private RefundChanelHandler refundChanelHandler;
    @Autowired
    private RefundB2BHandler refundB2BHandler;


    @Override
    public void refund(ResponseData responseData, RefundParams refundParam) throws RefundBill99ResponseNullException,
            UnsupportedEncodingException, RefundParamNullException, RefundBill99HandleException, DocumentException,
            RefundHttpToBill99FailedException, PayFlowFoundNullException, Bill99NotFoundThisOrderException,
            RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        logger.info("the refund param in manager are -----> [refundParam = {}]", ItvJsonUtil.toJson(refundParam));
        Long orderNo = Long.valueOf(refundParam.getParentId());
        String orderId = refundParam.getOrderId();
        Short tradeType = refundParam.getTradeType();
        BigDecimal bigDecimal = refundParam.getFreight() == null ? BigDecimal.ZERO : refundParam.getFreight();
        refundParam.setFreight(bigDecimal);
        //获取该订单所有是用余额支付的流水
        List<PayFlow> balancePayFlows = payFlowService.getBalancePayFlow(orderNo);
        //获取该订单所有不使用余额的支付流水
        List<PayFlow> payflowList = payFlowService.getPayFlowByOrderId(orderNo);
        //获取该订单的补款记录
        List<PayFlow> disPayFlows = null;
        BigDecimal dispatchAmount = refundParam.getDispatchAmount() == null ? BigDecimal.ZERO : refundParam.getDispatchAmount();
        if (tradeType != null && tradeType == Constants.TRADE_TYPE_17 && dispatchAmount.compareTo(BigDecimal.ZERO) != -1) {
            disPayFlows = payFlowService.getPayFlowsByOrderIdAndTradeType(orderId, Constants.TRADE_TYPE_8);
            if (CollectionUtils.isNotEmpty(disPayFlows)) {
                for (PayFlow disPayFlow : disPayFlows) {
                    String channelCode = disPayFlow.getChannelCode();
                    if (channelCode.equals(Constants.UID_BALANCE)){
                        balancePayFlows.add(disPayFlow);
                        continue;
                    }
                    payflowList.add(disPayFlow);
                }
            }
        }
        Boolean b2bFlag = false;
        for (PayFlow payFlow : payflowList) {
            String channelType = payFlow.getChannelType();
            if (channelType.contains(Constants.B2B_FLAG)){
                b2bFlag = true;
                break;
            }
        }
        if (b2bFlag){
            refundB2BHandler.refund(balancePayFlows, payflowList, refundParam, responseData);
            return;
        }
        if (CollectionUtils.isNotEmpty(balancePayFlows) && CollectionUtils.isNotEmpty(payflowList)) {
            refundBalanceAndCancelHandler.refund(balancePayFlows, payflowList, refundParam, responseData);
            return;
        }
        if (CollectionUtils.isNotEmpty(balancePayFlows) && CollectionUtils.isEmpty(payflowList)) {
            refundBalanceHandler.refund(balancePayFlows, payflowList, refundParam, responseData);
            return;
        }
        if (CollectionUtils.isEmpty(balancePayFlows) && CollectionUtils.isNotEmpty(payflowList)) {
            refundChanelHandler.refund(balancePayFlows, payflowList, refundParam, responseData);
            return;
        }

    }
}
