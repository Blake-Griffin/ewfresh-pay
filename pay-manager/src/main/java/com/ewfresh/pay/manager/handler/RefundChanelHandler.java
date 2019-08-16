package com.ewfresh.pay.manager.handler;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/5/22 0022
 */
@Component
public class RefundChanelHandler {

    private static final Logger logger = LoggerFactory.getLogger(RefundChanelHandler.class);
    @Autowired
    private RefundUtils refundUtils;

    public void refund(List<PayFlow> balancePayFlows, List<PayFlow> payflowList, RefundParams map, ResponseData responseData) throws UnsupportedEncodingException, RefundParamNullException, RefundBill99HandleException, RefundBill99ResponseNullException, DocumentException, RefundHttpToBill99FailedException, PayFlowFoundNullException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        logger.info(" handle one way pay for a order [payflowList = {},map = {}]",ItvJsonUtil.toJson(payflowList), ItvJsonUtil.toJson(map));
        BigDecimal refundAmount = null;
        String earnestBill = map.getEarnestBill();
        String finalBill = map.getFinalBill();
        Short tradeType = map.getTradeType();
        BigDecimal earnestAmount = map.getEarnestAmount();
        BigDecimal finalAmount = map.getFinalAmount();
        BigDecimal dispatchAmount = map.getDispatchAmount();
        BigDecimal freight = map.getFreight() == null ? BigDecimal.ZERO : map.getFreight();
        List<RefundParam> refundParams = new ArrayList<>();
        if (StringUtils.isBlank(earnestBill) && StringUtils.isNotBlank(finalBill) && tradeType != Constants.TRADE_TYPE_17) {
            //支付全款且不是退货退款的情况
            logger.info(" handle one way pay for a order in only pay for final [ earnestBill = {},finalBill = {}]",earnestBill, finalBill);
            refundAmount = map.getFinalAmount().add(freight);
            PayFlow payFlow = payflowList.get(0);
            getRefundParamsByPayFlow(refundParams, map, payFlow, refundAmount, freight);
        } else if (StringUtils.isNotBlank(earnestBill) && StringUtils.isBlank(finalBill)) {
            logger.info(" handle one way pay for a order in only pay for earnest [ earnestBill = {},finalBill = {}]",earnestBill, finalBill);
            refundAmount = map.getEarnestAmount().add(freight);
            PayFlow payFlow = payflowList.get(0);
            getRefundParamsByPayFlow(refundParams, map, payFlow, refundAmount, freight);
        } else if (StringUtils.isNotBlank(finalBill) && StringUtils.isNotBlank(earnestBill) || tradeType == Constants.TRADE_TYPE_17){
            logger.info("already paid earnest and final map = {}", ItvJsonUtil.toJson(map));
            for (PayFlow payFlow : payflowList) {
                String channelFlowId = payFlow.getChannelFlowId();
                if (earnestBill.equals(channelFlowId)) {
                    refundAmount = earnestAmount.add(freight);
                }
                if (finalBill.equals((channelFlowId))) {
                    if (StringUtils.isNotBlank(earnestBill)){
                        freight = BigDecimal.ZERO;
                    }
                    refundAmount = finalAmount.add(freight);
                }
                if (payFlow.getTradeType() == Constants.TRADE_TYPE_8 && dispatchAmount.compareTo(BigDecimal.ZERO) > 0){
                    refundAmount = dispatchAmount;
                    freight = BigDecimal.ZERO;
                }
                logger.info("paied payFlow for this time [refundAmount = {}, freight = {}]", refundAmount, freight);
                if (refundAmount.compareTo(BigDecimal.ZERO) < 1){
                    logger.info("the refund amount is 0 or not legitimate [payflow = {},map = {}]", ItvJsonUtil.toJson(payFlow), ItvJsonUtil.toJson(map));
                    continue;
                }
                getRefundParamsByPayFlow(refundParams, map, payFlow, refundAmount, freight);
                refundAmount = BigDecimal.ZERO;
            }
        }
        refundUtils.refund(refundParams,responseData);
    }


    public List<RefundParam> getRefundParamsByPayFlow(List<RefundParam> refundParams, RefundParams map, PayFlow payFlow, BigDecimal refundAmount, BigDecimal freight) {
        RefundParam refundParam = refundUtils.getByPayFlow(map, payFlow,refundAmount, freight);
        refundParams.add(refundParam);
        return refundParams;
    }




}
