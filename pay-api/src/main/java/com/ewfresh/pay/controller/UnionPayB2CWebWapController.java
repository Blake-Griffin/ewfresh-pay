package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.UnionpayB2CWebWapManager;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.BOBResponseUtil;
import com.ewfresh.pay.util.unionpayb2cwebwap.GetAllRequestParam;
import com.ewfresh.pay.util.unionpayb2cwebwap.LogUtil;
import com.ewfresh.pay.util.unionpayb2cwebwap.SDKConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * description: 银联B2C，web、wap接口
 * @author: JiuDongDong
 * date: 2019/4/24.
 */
@Controller
public class UnionPayB2CWebWapController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UnionpayB2CWebWapManager unionpayB2CWebWapManager;
    @Autowired
    private SDKConfig sdkConfig;

    /**
     * Description: 用户请求订单信息
     * @author: JiuDongDong
     * date: 2019/5/6 10:11
     */
    @Adopt
    @RequestMapping(value = "/t/unionpay-b2c-web-wap/sendOrder.htm")
    public void sendOrder(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionpayB2CWebWapController.sendOrder");
        ResponseData responseData = new ResponseData();
        try {
            // 获取参数并封装
            Map<String, String> params = new HashMap<>();
            String channelType = request.getParameter(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
            String ip = request.getParameter(Constants.ORDER_IP);//持卡人IP地址
            String orderId = request.getParameter(param_orderId);//商户订单号
            String orderAmount = request.getParameter(Constants.ORDER_AMOUNT);//订单金额，传入单位为元

            if (StringUtils.isBlank(channelType) || StringUtils.isBlank(orderId) || StringUtils.isBlank(orderAmount)) {
                logger.warn("The parameter orderNo or payment is empty for UnionpayB2CWebWapController.sendOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }

            params.put(param_channelType, channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
            params.put(param_orderId, orderId);//商户订单号
            params.put(param_txnAmt, orderAmount);//交易金额，单位元
            params.put(param_customerIp, ip);//持卡人ip

            unionpayB2CWebWapManager.sendOrder(responseData, params);
            if (ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                // 响应页面
                Object entity = responseData.getEntity();
                BOBResponseUtil.responsePrintHTML(response, entity, logger);
            }
            if (!ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                logger.error("Some errors occurred in UnionpayB2CWebWapController.sendOrder");
                // 重定向到错误页面
                response.sendRedirect(sdkConfig.getFrontFailUrl());
            }
            logger.info("It is OK in UnionpayB2CWebWapController.sendOrder");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionpayB2CWebWapController.sendOrder", e);
            // 重定向到错误页面
            try {
                response.sendRedirect(sdkConfig.getFrontFailUrl());
            } catch (IOException e1) {
                logger.error("Errors occurred in UnionpayB2CWebWapController.sendOrder when sendRedirect", e1);
            }
        }
    }

    /**
     * Description: 单笔交易查询。订单号、订单交易时间必传。
     *              订单支付交易查询，订单号为与银联交易的加E加R的订单号。
     *              TODO 如果想查消费撤销/退货是否成功，需要用消费撤销和退货的orderId和txnTime来查。
     * @author: JiuDongDong
     * date: 2019/5/6 15:11
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-b2c-web-wap/single-query.htm")
    public void singleQuery (HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionpayB2CWebWapController.singleQuery");
        ResponseData responseData = new ResponseData();
        try {
            Map<String, String> params = GetAllRequestParam.getAllRequestParam(request);
            LogUtil.printRequestLog(params);
//            logger.info("The singleQuery params = {}", JsonUtil.toJson(params));
            // 获取参数
            String orderId = params.get(param_orderId);//订单号
            String txnTime = params.get(param_txnTime);//订单发送时间
            // 非空校验
            if (StringUtils.isBlank(orderId) || StringUtils.isBlank(txnTime)) {
                logger.error("The parameter orderId or txnTime is empty for UnionpayB2CWebWapController.singleQuery, orderId = " + orderId + ", txnTime = " + txnTime);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
//            // 订单号必须加E加R
//            if (!orderNumber.endsWith(EARNEST) && !orderNumber.endsWith(TAIL)) {
//                logger.warn("The parameter orderNumber must endsWith E or R");
//                responseData.setCode(ResponseStatus.PARAMERR.getValue());
//                responseData.setMsg(ResponseStatus.PARAMERR.name());
//                return;
//            }
            unionpayB2CWebWapManager.singleQuery(responseData, params);
            logger.info("It is OK in UnionpayB2CWebWapController.singleQuery");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionpayB2CWebWapController.singleQuery", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 支付交易的通知
     * @author: JiuDongDong
     * date: 2019/5/6 17:28
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-b2c-web-wap/receivePayNotify.htm")
    public void receivePayNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionpayB2CWebWapController.receivePayNotify");
        ResponseData responseData = new ResponseData();
        try {
            // 获取银联通知服务器发送的后台通知参数
            Map<String, String> params = GetAllRequestParam.getAllRequestParam(request);
            LogUtil.printRequestLog(params);
//            logger.info("The notify info: " + JsonUtil.toJson(params));
            unionpayB2CWebWapManager.receivePayNotify(responseData, params);
            //返回给银联服务器http 200  状态码
            response.getWriter().print("ok");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionpayB2CWebWapController.receivePayNotify", e);
            try {
                response.getWriter().print("ok");
            } catch (IOException e1) {
                logger.error("Some errors occurred in UnionpayB2CWebWapController.receivePayNotify when response to UnionpayB2CWebWap", e1);
            }
        }
    }

    /**
     * Description: 退货交易的通知 TODO 弃用，暂用主动查询
     * @author: JiuDongDong
     * date: 2019/5/6 17:29
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-b2c-web-wap/receiveRefundNotify.htm")
    public void receiveRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionpayB2CWebWapController.receiveRefundNotify");
        ResponseData responseData = new ResponseData();
        try {
            // 获取银联通知服务器发送的后台通知参数
            Map<String, String> params = GetAllRequestParam.getAllRequestParam(request);
            LogUtil.printRequestLog(params);
//            logger.info("The notify info: " + JsonUtil.toJson(params));
            unionpayB2CWebWapManager.receiveRefundNotify(responseData, params);
            String result = (String) responseData.getEntity();
            //返回给银联服务器http 200  状态码
            // todo response2Bill99(response, result);
            response.getWriter().print("ok");
        } catch (Exception e) {
            logger.error("Some errors occurred in UnionpayB2CWebWapController.receiveRefundNotify", e);
            String frontEndUrl = sdkConfig.getFrontFailUrl();
            String result = "<result>1</result><redirecturl>" + frontEndUrl + "</redirecturl>";
            try {
                // todo response2Bill99(response, result);
            } catch (Exception e1) {
                logger.error("Some errors occurred in UnionpayB2CWebWapController.receiveRefundNotify when response2UnionpayB2CWebWap", e1);
            }
        }

    }

    /**
     * Description: 获取对账文件
     * @author: JiuDongDong
     * @param merId 商户号
     * @param settleDate 清算日期
     * date: 2019/5/8 11:20
     */
    @RequestMapping(value = "/t/unionpay-b2c-web-wap/file-transfer.htm")
    public void fileTransfer (HttpServletRequest request, HttpServletResponse response, String merId,
                              String settleDate) {
        logger.info("It is now in UnionpayB2CWebWapController.fileTransfer, the param are: " +
                "[merId = {}, settleDate = {}]", merId, settleDate);
        ResponseData responseData = new ResponseData();
        try {
            Map<String, String> params = new HashMap<>();
            // 非空校验
            if (StringUtils.isBlank(merId) || StringUtils.isBlank(settleDate)) {
                logger.error("The parameter has empty part, merId = " + merId + ", settleDate = " + settleDate);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            unionpayB2CWebWapManager.fileTransfer(responseData, merId, settleDate);
            logger.info("It is OK in UnionpayB2CWebWapController.fileTransfer");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionpayB2CWebWapController.fileTransfer", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

}
