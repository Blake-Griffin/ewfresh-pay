package com.ewfresh.pay.policy.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.handler.RefundUtils;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.policy.RefundPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:余额退款的策略
 * @author DuanXiangming
 * Date 2018/5/21
 */
@Component
public class BalancePolicy implements RefundPolicy {

    @Autowired
    private RefundUtils refundUtils;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<PayFlow> refund(RefundParam refundParam) {
        logger.info("balance refund policy handler start [refundParam = {}]", ItvJsonUtil.toJson(refundParam));
        PayFlow payFlow = refundParam.getPayFlow();
        PayFlow refundPayFlow = refundUtils.getRefundPayFlow(refundParam, payFlow);
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(refundPayFlow);
        return refund;
    }


}
