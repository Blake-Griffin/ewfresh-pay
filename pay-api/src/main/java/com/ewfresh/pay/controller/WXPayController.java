package com.ewfresh.pay.controller;


import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.WeiXinPayManager;
import com.ewfresh.pay.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedMap;


/**
 * description
 *
 * @author huangyabing
 * date 2018/3/28 10:17
 */
@Controller
public class WXPayController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private WeiXinPayManager weiXinPayManager;

    /**
     * description 发送post请求获取二维码
     *
     * @param response
     * @param request
     * @param orderNo
     * @param body
     * @param totalFee
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay-get-pay-code.htm")
    public void getPayCode(HttpServletResponse response, HttpServletRequest request, String orderNo, String body, String totalFee, String spbillCreateIp) {

        logger.info("It is get pay code method in controller params of it is -------> [orderNo = {}, body = {}, " +
            "totalFee = {}, spbillCreateIp = {}]", orderNo, body, totalFee, spbillCreateIp);
        ResponseData responseData = new ResponseData();
        //验参
        if (StringUtils.isBlank(orderNo) || StringUtils.isBlank(body) || StringUtils.isBlank(totalFee)
            || StringUtils.isBlank(spbillCreateIp)) {
            logger.warn("the param is null");
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("the param is null---->");
            ResponseUtil.responsePrint(response, responseData, logger);
            return;
        }
        try {
            responseData = weiXinPayManager.getWeiXinPayCode(response, request, responseData, orderNo, body, totalFee, spbillCreateIp);
        } catch (Exception e) {
            logger.error("get pay code is failed----->");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get pay code is failed");
        } finally {
            logger.info("responseData = {}", JsonUtil.toJson(responseData));
            if (responseData!=null){
                ResponseUtil.responsePrint(response, responseData, logger);
            }
        }
    }

    /**
     * description 微信支付的回调方法
     *
     * @param request
     * @param response
     * @return void
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/p/pay/weixinpay/callback.htm")
    public void WeiXinNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("It is method to notify of wxpay in controller------>");
        ResponseData responseData = new ResponseData();
        try {
            weiXinPayManager.weiXinPayCallback(response, request, responseData);
        } catch (Exception e) {
            logger.error("failed tof weiXinPay callback, error code is-----> ");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed tof weiXinPay callback, error code is-----> ");
            responseData.setEntity("FAIL");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * description 订单支付成功查询
     *
     * @param request
     * @param response
     * @param outTradeNo
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay/query-order.htm")
    public void queryOrderByOutTradeNo(HttpServletRequest request, HttpServletResponse response, String outTradeNo) {
        logger.info("It is method to query order of wxpay orderTradeNo is ------->" + outTradeNo);
        ResponseData responseData = new ResponseData();
        if (StringUtils.isBlank(outTradeNo)) {
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("failed to query order of weiXinPay, param is null-----> ");
            return;
        }
        try {
            weiXinPayManager.queryOrderByOutTradeNo(response, request, responseData, outTradeNo);
        } catch (Exception e) {
            logger.error("failed to query order by outTrdeNo, error code is----->");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed to query order by outTrdeNo, error code is-----> ");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * description 下载对账单
     *
     * @param request
     * @param response
     * @param billDate
     * @param billType
     * @return void
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay-download-bill.htm")
    public void downloadBill(HttpServletRequest request, HttpServletResponse response, String billDate, String billType) {
        logger.info("It is method to download bill in controller params are [billDate = {}, billType = {}]", billDate, billType);
        ResponseData responseData = new ResponseData();
        if (StringUtils.isBlank(billDate) || StringUtils.isBlank(billType)) {
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("failed tof weiXinPay callback, param is null-----> ");
            return;
        }
        try {
            weiXinPayManager.downLoadBill(response, request, responseData, billDate, billType);
        } catch (Exception e) {
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed tof weiXinPay callback, error code is-----> ");
            responseData.setEntity("FAIL");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * description 微信退款的回调方法
     *
     * @param request
     * @param response
     * @return void
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/p/weixinpay-refund-callback.htm")
    public void refundNotify(HttpServletResponse response, HttpServletRequest request) {
        logger.info("It is method to refund notify of wxpay in controller ");
        ResponseData responseData = new ResponseData();
        try {
            weiXinPayManager.refundCallBack(response, request, responseData);
        } catch (Exception e) {
            logger.info("failed to refund notify of wxpay");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed to refund notify  of wxpay");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }


    /**
     * description 查询退款订单信息
     *
     * @param response
     * @param request
     * @param outTradeNo
     * @return void
     * @author huangyabing
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay-query-refund.htm")
    public void queryRefund(HttpServletResponse response, HttpServletRequest request, String outTradeNo) {
        logger.info("It is method to query refund of wxpay in controller param is [outTradeNo = {}]", outTradeNo);
        ResponseData responseData = new ResponseData();
        if (StringUtils.isBlank(outTradeNo)) {
            logger.warn("failed to query refund of wxpay, param is null----->");
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("failed to query refund of wxpay, param is null-----> ");
            return;
        }
        try {
            SortedMap<Object, Object> map = weiXinPayManager.queryRefund(outTradeNo);
        } catch (Exception e) {
            logger.info("failed to query refund of wxpay");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed to query refund of wxpay");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /*
     *description 退款请求接口
     *@author huangyabing
     *@param outTradeNo
     *@param outRefundNo
     *@param totalFee
     *@param refundFee
     *@return void
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay-refund.htm")
    public void wxPayRefund(HttpServletResponse response, HttpServletRequest request, String outTradeNo, String outRefundNo, String totalFee, String refundFee) {
        logger.info("refund in controller");
        ResponseData responseData = new ResponseData();
        if (StringUtils.isBlank(outRefundNo) || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(totalFee) || StringUtils.isBlank(refundFee)) {
            logger.warn("failed to refund of wxpay, param is null----->");
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("failed to refund of wxpay, param is null-----> ");
            return;
        }
        try {
            weiXinPayManager.refund(response, request, responseData, outTradeNo, outRefundNo, totalFee, refundFee);
        } catch (Exception e) {
            logger.error("failed to query refund of wxpay");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed to query refund of wxpay");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * description 发起微信支付
     *
     * @param response
     * @param request
     * @param orderNo
     * @param body
     * @param totalFee
     * @param spbillCreateIp
     * @author zhaoqun
     */
    @Adopt
    @RequestMapping("/t/pay/weixinpay-pay-request.htm")
    public void winXinPayRequest(HttpServletResponse response, HttpServletRequest request, String orderNo, String body, String totalFee, String spbillCreateIp) {

        logger.info("It is winXin Pay Request method in controller params of it is -------> [orderNo = {}, body = {}, " +
            "totalFee = {}, spbillCreateIp = {}]", orderNo, body, totalFee, spbillCreateIp);
        ResponseData responseData = new ResponseData();
        //验参
        if (StringUtils.isBlank(orderNo) || StringUtils.isBlank(body) || StringUtils.isBlank(totalFee)
            || StringUtils.isBlank(spbillCreateIp)) {
            logger.warn("the param is null");
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("the param is null---->");
            ResponseUtil.responsePrint(response, responseData, logger);
            return;
        }
        try {
            weiXinPayManager.winXinPayRequest(response, request, responseData, orderNo, body, totalFee, spbillCreateIp);
            logger.info("responseData = {}", JsonUtil.toJson(responseData));
        } catch (Exception e) {
            logger.error("winXin Pay Request is failed----->", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("winXin Pay Request is failed");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

}
