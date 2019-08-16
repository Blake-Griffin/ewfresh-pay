package com.ewfresh.pay.manager.handler;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.CollectionUtils;
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
 * Description: 处理使用余额和其他支付渠道共同支付的处理器
 *
 * @author DuanXiangming
 * Date 2018/5/22 0022
 */
@Component
public class RefundBalanceAndCancelHandler {


    private static final Logger logger = LoggerFactory.getLogger(RefundBalanceAndCancelHandler.class);

    @Autowired
    private RefundUtils refundUtils;

    public void refund(List<PayFlow> balancePayFlows, List<PayFlow> payflowList, RefundParams refundParams, ResponseData responseData) throws UnsupportedEncodingException, RefundParamNullException, RefundBill99HandleException, RefundBill99ResponseNullException, DocumentException, RefundHttpToBill99FailedException, PayFlowFoundNullException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        logger.info("the refund param in handler pay for use balance and other paychanel  -----> [refundParam = {}]", ItvJsonUtil.toJson(refundParams));
        String earnestBill = refundParams.getEarnestBill();
        String finalBill = refundParams.getFinalBill();
        Short tradeType = refundParams.getTradeType();
        BigDecimal dispatchAmount = refundParams.getDispatchAmount();
        List<RefundParam> refundParamList = new ArrayList<>();
        List<PayFlow> refundPayflows = refundUtils.getRefundPayflows(refundParams);
        boolean flag = refundUtils.checkIsEnough(balancePayFlows, payflowList, refundParams, refundPayflows);
        if (!flag) {
            //退款金额校验未通过
            responseData.setCode(ResponseStatus.REFUNDAMOUNTERR.getValue());
            responseData.setMsg("refund amount is too large");
            return;
        }
        Map<String, List<PayFlow>> payFlowsMap = refundUtils.groupingPayflow(payflowList, balancePayFlows, refundPayflows);
        /*支付共分三种情况,1只支付定金,2只支付尾款即全款支付,3定金尾款都已支付*/
        if (StringUtils.isBlank(earnestBill) && StringUtils.isNotBlank(finalBill) && (tradeType != Constants.TRADE_TYPE_17 || dispatchAmount == null || dispatchAmount.compareTo(BigDecimal.ZERO) != 1)) {
            //只支付了尾款(即全款)的情况 且不含配货退款
            logger.info("only final not have dispatchamount");
            List<PayFlow> finalPayFlows = payFlowsMap.get(Constants.FINAL_PAYFLOWS);
            List<PayFlow> finalRefundPayFlows = payFlowsMap.get(Constants.FINAL_REFUND_PAYFLOWS);
            getFinalRefundParams(finalPayFlows, finalRefundPayFlows, refundParams, refundParamList);
        } else if (StringUtils.isBlank(finalBill) && StringUtils.isNotBlank(earnestBill)) {
            //只支付了定金的情况
            logger.info("only earnest paied");
            List<PayFlow> earnestpayflows = payFlowsMap.get(Constants.EARNEST_PAYFLOWS);
            List<PayFlow> earnestRefundPayFlows = payFlowsMap.get(Constants.EARNEST_REFUND_PAYFLOWS);
            getOnlyEarnestRefundParams(earnestpayflows, earnestRefundPayFlows, refundParams, refundParamList);
        } else if ((StringUtils.isNotBlank(finalBill) && StringUtils.isNotBlank(earnestBill)) || tradeType == Constants.TRADE_TYPE_17) {
            getAllRefundParams(payFlowsMap, refundParams, refundParamList);
        }
        if (CollectionUtils.isEmpty(refundParamList)) {
            responseData.setCode(ResponseStatus.REFUNDNULL.getValue());
            responseData.setMsg(" no refundParams err");
            return;
        }
        logger.info("ths final refundParamList [refundParamList = {}]", ItvJsonUtil.toJson(refundParamList));
        refundUtils.refund(refundParamList, responseData);
    }

    private List<RefundParam> getAllRefundParams(Map<String, List<PayFlow>> payFlowsMap, RefundParams refundParams, List<RefundParam> refundParamList) {
        //获取定金的三方交易流水号
        Short tradeType = refundParams.getTradeType();
        List<PayFlow> earnestpayflows = payFlowsMap.get(Constants.EARNEST_PAYFLOWS);
        List<PayFlow> earnestRefundPayFlows = payFlowsMap.get(Constants.EARNEST_REFUND_PAYFLOWS);
        logger.info("get earnestPayFlows [earnestPayFlows = {} , earnestRefundPayFlows = {}]", ItvJsonUtil.toJson(earnestpayflows), ItvJsonUtil.toJson(earnestRefundPayFlows));
        List<RefundParam> earnestRefundParams = getEarnestRefundParams(earnestpayflows, earnestRefundPayFlows, refundParams, refundParamList);
        logger.info("get earnestRefundParams result [earnestRefundParams = {}]", ItvJsonUtil.toJson(earnestRefundParams));
        //获取尾款的三方交易流水
        List<PayFlow> finalPayFlows = payFlowsMap.get(Constants.FINAL_PAYFLOWS);
        List<PayFlow> finalRefundPayFlows = payFlowsMap.get(Constants.FINAL_REFUND_PAYFLOWS);
        List<RefundParam> finalRefundParams = getFinalRefundParams(finalPayFlows, finalRefundPayFlows, refundParams, refundParamList);
        logger.info("get finalRefundParams result [finalRefundParams = {},finalRefundPayFlows = {}]", ItvJsonUtil.toJson(finalRefundParams), ItvJsonUtil.toJson(finalRefundPayFlows));
        BigDecimal dispatchAmount = refundParams.getDispatchAmount();
        List<RefundParam> dispatchRefundParam = null;
        if (tradeType == Constants.TRADE_TYPE_17 && dispatchAmount.compareTo(BigDecimal.ZERO) == 1) {
            List<PayFlow> dispatchPayFlows = payFlowsMap.get(Constants.DISPATCH_PAYFLOWS);
            dispatchRefundParam = getDispatchRefundParams(dispatchPayFlows, refundParamList, refundParams);
        }
        if (CollectionUtils.isNotEmpty(earnestRefundParams)) {
            int size = earnestRefundParams.size();
            for (int i = 0; i < size; i++) {
                refundParamList.add(earnestRefundParams.remove(0));
            }
        }
        if (CollectionUtils.isNotEmpty(finalRefundParams)) {
            int size = finalRefundParams.size();
            for (int i = 0; i < size; i++) {
                refundParamList.add(finalRefundParams.remove(0));
            }
        }
        if (CollectionUtils.isNotEmpty(dispatchRefundParam)) {
            int size = dispatchRefundParam.size();
            for (int i = 0; i < size; i++) {
                refundParamList.add(dispatchRefundParam.remove(0));
            }
        }
        return refundParamList;
    }

    private List<RefundParam> getDispatchRefundParams(List<PayFlow> dispatchPayFlows, List<RefundParam> refundParamList, RefundParams refundParams) {

        BigDecimal dispatchAmount = refundParams.getDispatchAmount();
        List<RefundParam> byBill = getRefundParamList(dispatchPayFlows, null, dispatchAmount, BigDecimal.ZERO, refundParamList, refundParams);
        return byBill;
    }


    //已支付定金和尾款的情况下,获取退款的流水
    private List<RefundParam> getEarnestRefundParams(List<PayFlow> earnestpayflows, List<PayFlow> earnestRefundPayFlows, RefundParams refundParams, List<RefundParam> refundParamList) {

        BigDecimal freight = refundParams.getFreight();
        BigDecimal earnestAmount = refundParams.getEarnestAmount();
        List<RefundParam> byBill = getRefundParamList(earnestpayflows, earnestRefundPayFlows, earnestAmount, freight, refundParamList, refundParams);
        return byBill;
    }

    //只支付定金的情况
    private List<RefundParam> getOnlyEarnestRefundParams(List<PayFlow> earnestpayflows, List<PayFlow> earnestRefundPayFlows, RefundParams refundParams, List<RefundParam> refundParamList) {

        BigDecimal earnestAmount = refundParams.getEarnestAmount();//应退定金(只有货款,不涉及运费)
        BigDecimal freight = refundParams.getFreight();            //应退运费
        for (PayFlow payFlow : earnestpayflows) {
            BigDecimal payerPayAmount = payFlow.getPayerPayAmount();       //支付金额(包含活货款和运费)
            BigDecimal subFreight = payFlow.getFreight();                  //本条支付流水的运费
            BigDecimal goodsPayment = payerPayAmount.subtract(subFreight); //本次可退货款
            BigDecimal refundAmount = earnestAmount.compareTo(goodsPayment) == -1 ? earnestAmount : goodsPayment;
            earnestAmount = earnestAmount.subtract(refundAmount);
            BigDecimal refundFreight = BigDecimal.ZERO;
            if (freight != null && freight.compareTo(BigDecimal.ZERO) == 1) {
                refundFreight = freight.compareTo(subFreight) == -1 ? freight : subFreight;
                freight = freight.subtract(refundFreight);
            }
            refundAmount = refundAmount.add(refundFreight);
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                RefundParam byPayFlow = refundUtils.getByPayFlow(refundParams, payFlow, payerPayAmount, refundFreight);
                refundParamList.add(byPayFlow);
            }
        }
        return refundParamList;
    }


    private List<RefundParam> getRefundParamList(List<PayFlow> paiedPayFlows, List<PayFlow> refundPayFlows, BigDecimal refundAmount, BigDecimal freight, List<RefundParam> refundParamList, RefundParams refundParams) {
        logger.info("The refund request needs to be processed this time [paiedPayFlows = {}, refundPayFlows = {}, refundAmount = {}, freight = {}]", ItvJsonUtil.toJson(paiedPayFlows), ItvJsonUtil.toJson(refundPayFlows), refundAmount, freight);
        //计算剩余可退余额
        BigDecimal surplusProductBalance = BigDecimal.ZERO;// 剩余可退货款余额
        BigDecimal surplusFreightBalance = BigDecimal.ZERO;// 剩余可退运费余额
        BigDecimal paiedFreight = BigDecimal.ZERO;
        boolean flag = false;
        for (PayFlow payFlow : paiedPayFlows) {
            String channelCode = payFlow.getChannelCode();
            BigDecimal subFreight = payFlow.getFreight();
            if (channelCode.equals(Constants.UID_BALANCE)) {
                surplusProductBalance = surplusProductBalance.add(payFlow.getPayerPayAmount().subtract(subFreight));
                surplusFreightBalance = surplusFreightBalance.add(subFreight);
            }
            if (channelCode.equals(Constants.CHANNEL_CODE_KUAIQIAN_QUICK) || channelCode.equals(Constants.CHANNEL_CODE_KUAIQIAN_ENTERPRISE)) {
                flag = true;
            }
            paiedFreight = paiedFreight.add(subFreight);
        }
        if (flag && paiedFreight.compareTo(BigDecimal.ZERO) == 0 && freight.compareTo(BigDecimal.ZERO) > 0){
            //旧版块钱处理方式:并未单独处理运费,合并处理
            refundAmount = refundAmount.add(freight);
            freight = BigDecimal.ZERO;
        }
        //获取该次支付对应的余额退款流水
        if (CollectionUtils.isNotEmpty(refundPayFlows)) {
            for (PayFlow payFlow : refundPayFlows) {
                String channelCode = payFlow.getChannelCode();
                if (channelCode.equals(Constants.UID_BALANCE)) {
                    BigDecimal subFreight = payFlow.getFreight();
                    surplusProductBalance = surplusProductBalance.subtract(payFlow.getPayerPayAmount());
                    surplusFreightBalance = surplusFreightBalance.subtract(subFreight);
                }
            }
        }
        if (surplusProductBalance.compareTo(BigDecimal.ZERO) < 0 || surplusFreightBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("surplusProductBalance or surplusFreightBalance err [surplusProductBalance = {}, surplusFreightBalance = {}]", surplusProductBalance, surplusFreightBalance);
            return null;
        }
        logger.info("The final surplusBalance for this refund [surplusProductBalance = {}, surplusFreightBalance = {}]", surplusProductBalance, surplusFreightBalance);
        if (surplusProductBalance.compareTo(BigDecimal.ZERO) == 0 && surplusFreightBalance.compareTo(BigDecimal.ZERO) == 0) {
            //所有余额货款和运费已经退完
            for (PayFlow payFlow : paiedPayFlows) {
                String channelCode = payFlow.getChannelCode();
                if (!channelCode.equals(Constants.UID_BALANCE)) {
                    RefundParam byPayFlow = refundUtils.getByPayFlow(refundParams, payFlow, refundAmount, freight);
                    byPayFlow.setRefundAmount(refundAmount.toString());
                    refundParamList.add(byPayFlow);
                    return refundParamList;
                }
            }
        }
        if (surplusProductBalance.compareTo(refundAmount) >= 0 && surplusFreightBalance.compareTo(freight) >= 0) {
            logger.info("only refund for balance refund [surplusProductBalance = {} , refundAmount = {}, surplusFreightBalance = {}, freight = {}]", surplusProductBalance, refundAmount, surplusFreightBalance, freight);
            //余额可以退换所有
            for (PayFlow payFlow : paiedPayFlows) {
                String channelCode = payFlow.getChannelCode();
                if (channelCode.equals(Constants.UID_BALANCE)) {
                    RefundParam byPayFlow = refundUtils.getByPayFlow(refundParams, payFlow, refundAmount, freight);
                    refundParamList.add(byPayFlow);
                    return refundParamList;
                }
            }

        }
        logger.info("the surplusBalance is more than refund amount [surplusProductBalance = {} , refundAmount = {}, surplusFreightBalance = {}, freight = {}]", surplusProductBalance, refundAmount, surplusFreightBalance, freight);
        BigDecimal needRefundProductAmount;
        BigDecimal needRefundFreightAmount = BigDecimal.ZERO;
        for (PayFlow payFlow : paiedPayFlows) {
            String channelCode = payFlow.getChannelCode();
            if (channelCode.equals(Constants.UID_BALANCE)) {
                needRefundProductAmount = surplusProductBalance.compareTo(refundAmount) != -1 ? refundAmount : surplusProductBalance;
                if (freight.compareTo(BigDecimal.ZERO) > 0) {
                    needRefundFreightAmount = surplusFreightBalance.compareTo(freight) != -1 ? freight : surplusFreightBalance;
                }
            } else {
                BigDecimal refundAmountSubtract = refundAmount.subtract(surplusProductBalance);
                needRefundProductAmount = refundAmountSubtract.compareTo(BigDecimal.ZERO) < 1 ? BigDecimal.ZERO : refundAmountSubtract ;
                if (freight.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal refundFreightSubtract = freight.subtract(surplusFreightBalance);
                    needRefundFreightAmount = refundFreightSubtract.compareTo(BigDecimal.ZERO) < 1 ? BigDecimal.ZERO : refundFreightSubtract;
                }
            }
            if (needRefundProductAmount.compareTo(BigDecimal.ZERO) < 0 || needRefundFreightAmount.compareTo(BigDecimal.ZERO) < 0) {
                logger.error("refund amount err [needRefundProductAmount = {} , needRefundFreightAmount = {}]", needRefundProductAmount, needRefundFreightAmount);
                continue;
            }
            BigDecimal totalRefundAmount = needRefundProductAmount.add(needRefundFreightAmount);
            if (totalRefundAmount.compareTo(BigDecimal.ZERO) == 1) {
                RefundParam byPayFlow = refundUtils.getByPayFlow(refundParams, payFlow, totalRefundAmount, needRefundFreightAmount);
                refundParamList.add(byPayFlow);
            }
        }
        return refundParamList;
    }


    private List<RefundParam> getFinalRefundParams(List<PayFlow> finalPayFlows, List<PayFlow> finalRefundPayFlows, RefundParams refundParams, List<RefundParam> refundParamList) {

        BigDecimal finalAmount = refundParams.getFinalAmount();//应退尾款(只有货款,不涉及运费)
        BigDecimal freight = BigDecimal.ZERO;                  //应退运费
        String earnestBill = refundParams.getEarnestBill();
        if (StringUtils.isBlank(earnestBill)) {
            //没有支付定金的情况才会有运费
            freight = refundParams.getFreight();
        }
        List<RefundParam> byBill = getRefundParamList(finalPayFlows, finalRefundPayFlows, finalAmount, freight, refundParamList, refundParams);
        return byBill;
    }

    /**
     * Description: 只支付过尾款的情况
     *
     * @param payFlows
     * @param refundParams
     * @param refundParamList
     * @return java.util.List<com.ewfresh.pay.model.RefundParam>
     * Date    2019/7/2  16:42
     * @author DuanXiangming
     */
    private List<RefundParam> getFinalBills(List<PayFlow> payFlows, RefundParams refundParams, List<RefundParam> refundParamList) {
        BigDecimal finalAmount = refundParams.getFinalAmount();//应退定金(只有货款,不涉及运费)
        BigDecimal freight = BigDecimal.ZERO;                  //应退运费
        String earnestBill = refundParams.getEarnestBill();
        if (StringUtils.isBlank(earnestBill)) {
            freight = refundParams.getFreight();
        }
        return getBillsByItems(payFlows, finalAmount, freight, refundParamList, refundParams);
    }

    private List<RefundParam> getBillsByItems(List<PayFlow> payFlows, BigDecimal refund, BigDecimal freight, List<RefundParam> refundParamList, RefundParams refundParams) {
        logger.info("need to RefundParams convert to RefundParam  [refundParams = {}, refund = {}, freight = {}]", ItvJsonUtil.toJson(refundParams), refund, freight);
        for (PayFlow payFlow : payFlows) {
            BigDecimal payerPayAmount = payFlow.getPayerPayAmount();//支付金额(包含活货款和运费)
            BigDecimal subFreight = payFlow.getFreight();           //本条支付流水的运费
            BigDecimal goodsPayment = payerPayAmount.subtract(subFreight); //本次可退货款
            BigDecimal refundAmount = refund.compareTo(goodsPayment) == -1 ? refund : goodsPayment;
            refund = refund.subtract(refundAmount);
            BigDecimal refundFreight = BigDecimal.ZERO;
            if (freight != null && freight.compareTo(BigDecimal.ZERO) == 1) {
                refundFreight = freight.compareTo(subFreight) == -1 ? freight : subFreight;
                freight = freight.subtract(refundFreight);
            }
            refundAmount = refundAmount.add(refundFreight);
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                RefundParam byPayFlow = refundUtils.getByPayFlow(refundParams, payFlow, refundAmount, refundFreight);
                refundParamList.add(byPayFlow);
            }
        }
        return refundParamList;
    }

    //支付流水分组的方法



}