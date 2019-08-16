package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.SortedMap;

/**
 * description
 *
 * @author huangyabing
 * date 2018/4/9 17:50
 */
public interface WeiXinPayManager {

    /**
     * description 获取支付二维码
     *
     * @param responseData
     * @param orderNo
     * @param body
     * @param totalFee
     * @param spbillCreateIp
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    ResponseData getWeiXinPayCode(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String orderNo, String body, String totalFee, String spbillCreateIp);

    /**
     * description 微信支付回调
     *
     * @param response
     * @param request
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    ResponseData weiXinPayCallback(HttpServletResponse response, HttpServletRequest request, ResponseData responseData);

    /**
     * description 根据订单号查询订单信息
     *
     * @param request
     * @param response
     * @param responseData
     * @param outTradeNo
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    ResponseData queryOrderByOutTradeNo(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String outTradeNo);

    /**
     * description 下载对账单
     *
     * @param response
     * @param request
     * @param responseData
     * @param billDate
     * @param billType
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    ResponseData downLoadBill(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String billDate, String billType);

    /**
     * 请求退款服务
     *
     * @param outTradeNo  商户系统内部的订单号,transaction_id 、out_trade_no 二选一，如果同时存在优先级：transaction_id>out_trade_no
     * @param outRefundNo 商户系统内部的退款单号，商户系统内部唯一，同一退款单号多次请求只退一笔
     * @param totalFee    订单总金额，单位为分
     * @param refundFee   退款总金额，单位为分
     */
    ResponseData refund(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String outTradeNo, String outRefundNo, String totalFee, String refundFee);

    SortedMap<Object, Object> queryRefund(String outRefundNo);

    /**
     * description 微信退款回调
     *
     * @param response
     * @param request
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    ResponseData refundCallBack(HttpServletResponse response, HttpServletRequest request, ResponseData responseData);


    /**
     * description 发起微信支付
     *
     * @param responseData
     * @param orderNo
     * @param body
     * @param totalFee
     * @param spbillCreateIp
     * @return com.ewfresh.pay.util.ResponseData
     * @author zhaoqun
     */
    ResponseData winXinPayRequest(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String orderNo, String body, String totalFee, String spbillCreateIp);
}
