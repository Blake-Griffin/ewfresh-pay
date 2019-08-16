package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.UnionPayH5PayManager;
import com.ewfresh.pay.model.exception.OrderTimeOutException;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.unionpayb2cwebwap.GetAllRequestParam;
import com.ewfresh.pay.util.unionpayb2cwebwap.SDKConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.Constants.SUCCESS;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * description: 银联H5Pay接口
 * @author: JiuDongDong
 * date: 2019/5/10.
 */
@Controller
public class UnionPayH5PayController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UnionPayH5PayManager unionPayH5PayManager;
    @Autowired
    private SDKConfig sdkConfig;


    /**
     * Description: 用户请求订单信息（适用于B2C、B2B）
     * @author: JiuDongDong
     * date: 2019/5/16 13:04
     */
    @Adopt
    @RequestMapping(value = "/t/unionpay-H5Pay/sendOrder.htm")
    public void sendOrder(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionPayH5PayController.sendOrder");
        ResponseData responseData = new ResponseData();
        String client = null;//客户端类型：1pc; 2android; 3ios; 4wap
        try {
            // 获取参数
            Map<String, String> params = GetAllRequestParam.getAllRequestParam(request);
            logger.info("It is now in UnionPayH5PayController.sendOrder, the params are: {}", JsonUtil.toJson(params));
            // 校验
            String channelType = params.get(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
            String ip = params.get(ORDER_IP);//持卡人IP地址
            String orderId = params.get(param_orderId);//商户订单号
            String orderAmount = params.get(ORDER_AMOUNT);//订单金额。单位：元，例如 0.01
            String msgType = params.get(MSG_TYPE);//消息类型: 1支付宝H5支付; 2微信H5支付; 3银联在线无卡; 4; 银联云闪付（走银联全渠道）
            String sceneType = params.get(SCENE_TYPE);//业务应用类型：微信H5支付必填。用于苹app应用里值为IOS_SDK；用于安卓app应用里值为AND_SDK；用于手机网站值为IOS_WAP或AND_WAP
            String merAppName = params.get(MER_APP_NAME);//微信H5支付必填。用于苹或安卓app 应用中，传分别 对应在 AppStore和安卓分发市场中的应用名（如：全民付）；用于手机网站，传对应的网站名（如：银联商务官网）
            String merAppId = params.get(MER_APP_ID);//微信H5支付必填。用于苹果或安卓 app 应用中，苹果传 IOS 应用唯一标识(如： com.tencent.wzryIOS )。安卓传包名 (如： com.tencent.tmgp.sgame)。如果是用于手机网站 ，传首页 URL 地址 , (如： https://m.jd.com ) ，支付宝H5支付参数无效
            String name = params.get(NAME);//姓名，无卡支付指定付款人时必传
            String mobile = params.get(MOBILE);//手机号，无卡支付指定付款人时必传
            String certType = params.get(CERT_TYPE);//证件类型。注意：无卡支付目前仅支持身份证。无卡支付指定付款人时必传，证件类型：身份证：IDENTITY_CARD、护照：PASSPORT、军官证：OFFICER_CARD、士兵证：SOLDIER_CARD、户口本：HOKOU。
            String certNo = params.get(CERT_NO);//证件号。无卡支付指定付款人时必传
            String bankCardNo = params.get(BANK_CARD_NO);//卡号。无卡支付指定付款人时必传
            String cardType = params.get(CARD_TYPE);//银行卡类型：借记卡：borrow   贷记卡：loan
            client = params.get(CLIENT);//客户端类型：1pc; 2android; 3ios; 4wap
            String bizType = params.get(BIZ_TYPE);//网银支付类型: B2B企业网银支付 B2C个人网银支付
            bizType = StringUtils.isBlank(bizType) ? BIZ_TYPE_B2C : bizType;
            params.put(BIZ_TYPE, bizType);

            if (StringUtils.isBlank(channelType) || StringUtils.isBlank(orderId) || StringUtils.isBlank(orderAmount)
                    || StringUtils.isBlank(msgType) || StringUtils.isBlank(client) || StringUtils.isBlank(bizType)
                    || StringUtils.isBlank(cardType)) {
                logger.warn("The parameter has empty in UnionPayH5PayController.sendOrder, channelType = {}, " +
                                "orderId = {}, orderAmount = {}, msgType = {}, client = {}, bizType = {}, cardType = {}",
                        channelType, orderId, orderAmount, msgType, client, bizType, cardType);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }

            unionPayH5PayManager.sendOrder(responseData, params);
            // PC端的处理
            if (CLIENT_PC.equals(client) || CLIENT_WAP.equals(client)) {
                if (ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                    response.sendRedirect((String) responseData.getEntity());// 重定向到支付页
                } else {
                    logger.error("Some errors occurred in UnionPayH5PayController.sendOrder");
                    response.sendRedirect(sdkConfig.getFrontFailUrl());// 重定向到错误页面
                }
            } else {
                ResponseUtil.responsePrint(response, responseData, logger);
            }
            logger.info("It is OK in UnionPayH5PayController.sendOrder");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayH5PayController.sendOrder", e);
            if (CLIENT_PC.equals(client) || CLIENT_WAP.equals(client)) {
                logger.error("Some errors occurred in UnionPayH5PayController.sendOrder");
                try {
                    response.sendRedirect(sdkConfig.getFrontFailUrl());
                } catch (IOException e1) {
                    logger.error("Errors occurred in UnionPayH5PayController.sendOrder when sendRedirect", e1);
                }
            } else {
                ResponseUtil.responsePrint(response, responseData, logger);
            }
        }
    }

    /**
     * Description: 支付结果查询接口
     * @author: JiuDongDong
     * @param orderId 商户订单号，如：31942052717444700006502R
     * @param mid 支付完成，但银联还没有回调的时候进行查询，需要携带mid、tid
     * @param tid 支付完成，但银联还没有回调的时候进行查询，需要携带mid、tid
     * date: 2019/5/14 15:50
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-H5Pay/pay-query.htm")
    public void singleQuery (HttpServletResponse response, String orderId, String mid, String tid) {
        logger.info("It is now in UnionPayH5PayController.singleQuery, the params are: " +
                "[orderId = {}, mid = {}, tid = {}]", orderId, mid, tid);
        ResponseData responseData = new ResponseData();
        try {
            // 非空校验
            if (StringUtils.isBlank(orderId)) {
                logger.error("The parameter has empty part for UnionPayH5PayController.singleQuery, " +
                        "orderId = " + orderId);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            String merOrderId = orderId;
            unionPayH5PayManager.singleQuery(responseData, merOrderId, mid, tid);
            logger.info("It is OK in UnionPayH5PayController.singleQuery");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayH5PayController.singleQuery", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 支付、退款交易的通知（退款也会通知到此url，注意只有退款成功才会有退款通知。Sun DaWei）
     * @author: JiuDongDong
     * date: 2019/5/16 16:46
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-H5Pay/receiveNotify.htm")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionPayH5PayController.receiveNotify");
        ResponseData responseData = new ResponseData();
        try {
            InputStream inputStream = request.getInputStream();
            StringBuffer content = new StringBuffer();
            BufferedReader br;
            String tempStr;
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((tempStr = br.readLine()) != null) {
                content.append(tempStr);
            }
            logger.info("H5Pay pay or refund notify info is: {}", content.toString());
            unionPayH5PayManager.receiveNotify(responseData, content.toString());
            //商户收到通知后，需要对通知做出响应：成功收到时响应”SUCCESS”；失败时响应”FAILED”
            response.getWriter().print(SUCCESS);
        } catch (OrderTimeOutException e) {
            try {
                response.getWriter().print(SUCCESS);
            } catch (IOException e1) {
                logger.error("Some errors occurred in UnionPayH5PayController.receiveNotify when response to UnionPayH5Pay", e1);
            }
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayH5PayController.receiveNotify", e);
            try {
                response.getWriter().print(FAILED);
            } catch (IOException e1) {
                logger.error("Some errors occurred in UnionPayH5PayController.receiveNotify when response to UnionPayH5Pay", e1);
            }
        }
    }

    /**
     * Description: B2B手工接口退款的通知----这个不用了，已经使用线上联机退款
     * @author: JiuDongDong
     * date: 2019/6/12 15:36
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-H5Pay-B2B/refundNotify.htm")
    public void receiveB2BRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionPayH5PayController.receiveB2BRefundNotify");
        ResponseData responseData = new ResponseData();
        try {
            InputStream inputStream = request.getInputStream();
            StringBuffer content = new StringBuffer();
            BufferedReader br;
            String tempStr;
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((tempStr = br.readLine()) != null) {
                content.append(tempStr);
            }
            logger.info("Get H5Pay B2B refund notify from unionPay is: {}", content.toString());
            unionPayH5PayManager.receiveB2BRefundNotify(responseData, content.toString());
            //商户收到通知后，需要对通知做出响应：成功收到时响应”SUCCESS”；失败时响应”FAILED”
            response.getWriter().print(success);
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayH5PayController.receiveB2BRefundNotify", e);
            try {
                response.getWriter().print(FAILED);
            } catch (IOException e1) {
                logger.error("Errors occurred in UnionPayH5PayController.receiveB2BRefundNotify when response to UnionPayH5Pay", e1);
            }
        }
    }

    /**
     * Description: 退款结果查询接口（退货查询接口的refundStatus有4种状态：SUCCESS成功、FAIL失败、PROCESSING处理中、UNKNOWN异常）
     * @author: JiuDongDong
     * @param orderId 退货订单号（msgSrcId + 生成28位的退款流水号,如：31942019070216231528400000007488）
     * date: 2019/5/17 13:40
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-H5Pay/refund-query.htm")
    public void refundQuery(HttpServletResponse response, String orderId) {
        logger.info("It is now in UnionPayH5PayController.refundQuery, the params are: [orderId = {}]", orderId);
        ResponseData responseData = new ResponseData();
        try {
            // 非空校验
            if (StringUtils.isBlank(orderId)) {
                logger.error("The parameter has empty part for UnionPayH5PayController.refundQuery, " +
                        "orderId = " + orderId);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            String merOrderId = orderId;
            unionPayH5PayManager.refundQuery(responseData, merOrderId);
            logger.info("It is OK in UnionPayH5PayController.refundQuery");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayH5PayController.refundQuery", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

}
