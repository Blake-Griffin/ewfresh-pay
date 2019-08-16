package com.ewfresh.pay.policy.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.service.PayFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangziyuan on 2018/4/23.
 */
@Component
public class AliRefundPolicy implements RefundPolicy {
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String TRADE_NO = "trade_no";
    private static final String REFUND_AMOUNT = "refund_amount";
    private static final String OUT_REQUEST_NO = "out_request_no";
    private static final String TYPE_NAME = "支付宝退款";
    private static Short TARDE_TYPE = 2;

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    private PayFlowService payFlowService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<PayFlow> refund(RefundParam refundParam) {
        logger.info("come aliPayReturn!!");
        if (refundParam.getOutTradeNo() == null && refundParam.getTradeNo() == null || refundParam.getRefundAmount() == null || refundParam.getOutRequestNo() == null) {
            logger.warn("the param is null");
            logger.warn("the param is out_trade_no={},trade_no={},refund_amount={},out_request_no={}", refundParam.getOutTradeNo(),
                    refundParam.getTradeNo(), refundParam.getRefundAmount(), refundParam.getOutRequestNo());
            return null;
        }
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map<String, String> map = new HashMap<>();
        map.put(OUT_TRADE_NO, refundParam.getOutTradeNo());
        map.put(TRADE_NO, refundParam.getTradeNo());
        map.put(REFUND_AMOUNT, refundParam.getRefundAmount());
        map.put(OUT_REQUEST_NO, refundParam.getOutRequestNo());
        request.setBizContent(ItvJsonUtil.toJson(map));
        AlipayTradeRefundResponse response = null;
        PayFlow payFlow = null;
        try {
            response = alipayClient.execute(request);
            if (response.isSuccess()) {
                logger.info("comme in return isSuccess!!!!");
                //金额发生变化则调用查询接口去验证(流水相同,退款请求号相同的情况下如果已经调用再次调用也会进入成功只是金额不会发生变化)
                AlipayTradeFastpayRefundQueryRequest requestQuery = new AlipayTradeFastpayRefundQueryRequest();
                HashMap<String, String> queryMap = new HashMap<>();
                queryMap.put(TRADE_NO, refundParam.getTradeNo());
                queryMap.put(OUT_REQUEST_NO, refundParam.getOutRequestNo());
                requestQuery.setBizContent(ItvJsonUtil.toJson(queryMap));
                AlipayTradeFastpayRefundQueryResponse responseQuery = alipayClient.execute(requestQuery);
                if (responseQuery.isSuccess()) {
                    logger.info("come in return query success");
                    payFlow = payFlowService.getPayFlowPartById(refundParam.getTradeNo());
                    if (payFlow == null) {
                        logger.error("There is no this payFlowId: " + refundParam.getTradeNo());
                        return null;
                    }
                    // 6.2 设置支付流水信息
                    payFlow.setPayFlowId(null);
                   // payFlow.setChannelFlowId(responseQuery.getTradeNo());
                    payFlow.setChannelFlowId(response.getTradeNo());
                    logger.info("the refund----number{}, the query -----number{}",response.getTradeNo(),responseQuery.getTradeNo());
                    payFlow.setPayerPayAmount(new BigDecimal(responseQuery.getRefundAmount()));//付款方支付金额（即商户退款金额） 分转换为元
                    // 收款人id和付款人id对调、收款人名称和付款人名称对调
                    String payerId = payFlow.getPayerId();//付款人id
                    String receiverUserId = payFlow.getReceiverUserId();//收款人id
                    String payerName = payFlow.getPayerName();//付款人名称
                    String receiverName = payFlow.getReceiverName();//收款人名称
                    payFlow.setPayerId(receiverUserId);
                    payFlow.setReceiverUserId(payerId);
                    payFlow.setPayerName(receiverName);
                    payFlow.setReceiverName(payerName);
                    //交易交易类型
                    payFlow.setTradeType(TARDE_TYPE);
                    //支付类型名称
                    payFlow.setTypeName(TYPE_NAME);
                    //退款支付时间
                    payFlow.setSuccessTime(response.getGmtRefundPay());
                } else {
                    return null;
                }
            } else {
                logger.error("return alipay have error!!!");
                return null;
            }
        } catch (AlipayApiException e) {
            logger.error("have an AlipayApiException!!!", e);
            return null;
        }
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;
    }
}
