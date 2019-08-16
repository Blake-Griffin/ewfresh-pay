package com.ewfresh.pay.unionPayRefundParamsPolicy;

import com.ewfresh.pay.model.RefundParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * description: 组装银联退款的请求参数
 * @author: JiuDongDong
 * date: 2019/6/29.
 */
public interface UnionPayRefundParamsPolicy {

    /**
     * Description: 组装银联退款的请求参数
     * @author: JiuDongDong
     * @param refundParam  封装退款参数
     * @param outTradeNo  商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
     * @param mid  主商户号
     * @param tid  主商户mid
     * @param shopMid  第三方卖家的mid
     * @param payerPayAmount  买家支付订单时使用银联支付的金额
     * @param outRequestNo  退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @param successTime  接收支付通知时的billDate
     * @return  请求银联的参数
     * date: 2019/7/1 14:30
     */
    List<Map<String, String>> getUnionPayRefundParams(RefundParam refundParam, String outTradeNo, String mid, String tid, String shopMid, BigDecimal payerPayAmount, String outRequestNo, Date successTime);
}
