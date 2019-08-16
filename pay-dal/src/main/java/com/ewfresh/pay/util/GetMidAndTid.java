package com.ewfresh.pay.util;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.service.PayFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.MID;
import static com.ewfresh.pay.util.Constants.TID;

/**
 * description: 从payFlow获取mid、tid
 * @author: JiuDongDong
 * date: 2019/6/26.
 */
@Component
public class GetMidAndTid {
    @Autowired
    private PayFlowService payFlowService;

    /**
     * Description: 根据InteractionId获取mid、tid（订单支付）
     * @author: JiuDongDong
     * @param interactionId 商户订单号，如：31942052717444700006502R
     * @return java.util.Map<java.lang.String,java.lang.String>
     * date: 2019/6/26 21:48
     */
    public synchronized Map<String, String> getMidAndTidByInteractionId(String interactionId) {
        PayFlow payFlow = payFlowService.getPayFlowByInteractionId(interactionId);
        if (null == payFlow) {
            return null;
        }
        String mid = payFlow.getMid();
        String tid = payFlow.getTid();
        Map<String, String> map = new HashMap<>();
        map.put(MID, mid);
        map.put(TID, tid);
        return map;
    }

    /**
     * Description: 根据refundSequence获取mid、tid（订单退款）
     * @author: JiuDongDong
     * @param refundSequence 退款时生成的退货订单号（msgSrcId + 生成28位的退款流水号,如：56152019070409445616900000007570）
     * @return java.util.Map<java.lang.String,java.lang.String>
     * date: 2019/6/27 10:55
     */
    public synchronized Map<String, String> getMidAndTidByRefundSequence(String refundSequence) {
        PayFlow payFlow = payFlowService.getFlowIdByRefundSequence(refundSequence);
        if (null == payFlow) {
            return null;
        }
        String mid = payFlow.getMid();
        String tid = payFlow.getTid();
        Map<String, String> map = new HashMap<>();
        map.put(MID, mid);
        map.put(TID, tid);
        return map;
    }
}
