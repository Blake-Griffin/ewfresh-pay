package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.UnionPayQrCodeManager;
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
import java.io.*;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.Constants.SUCCESS;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * description: 银联QrCode接口
 * @author: JiuDongDong
 * date: 2019/5/10.
 */
@Controller
public class UnionPayQrCodeController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UnionPayQrCodeManager unionPayQrCodeManager;
    @Autowired
    private SDKConfig sdkConfig;


    /**
     * Description: 获取二维码
     * @author: JiuDongDong
     * date: 2019/5/10 16:50
     */
    @Adopt
    @RequestMapping(value = "/t/unionpay-QrCode/getQrCode.htm")
    public void getQrCode(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionPayQrCodeController.sendOrder");
        ResponseData responseData = new ResponseData();
        String client = null;
        try {
            // 获取参数并封装
            Map<String, String> params = GetAllRequestParam.getAllRequestParam(request);
            logger.info("GetQrCode params are: {}", JsonUtil.toJson(params));
            String channelType = params.get(param_channelType);//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
            String ip = params.get(ORDER_IP);//持卡人IP地址
            String orderId = params.get(param_orderId);//商户订单号
            String orderAmount = params.get(ORDER_AMOUNT);//订单金额。单位：元，例如 0.01
            params.put(param_txnAmt, orderAmount);//交易金额，单位分，不要带小数点
            client = params.get(CLIENT);//客户端类型：1pc; 2android; 3ios; 4wap

            if (StringUtils.isBlank(channelType) || StringUtils.isBlank(orderId) || StringUtils.isBlank(orderAmount)) {
                logger.warn("The parameter orderNo or payment is empty for UnionPayQrCodeController.sendOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }

            unionPayQrCodeManager.getQrCode(responseData, params);
            if (CLIENT_PC.equals(client) || CLIENT_WAP.equals(client)) {
                if (!ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                    logger.error("Some errors occurred in UnionPayQrCodeController.sendOrder, client = " + client +
                            ", GetQrCode params are: " + JsonUtil.toJson(params));
                    response.sendRedirect(sdkConfig.getFrontFailUrl());
                    return;
                }
            } else {
                if (!ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                    logger.error("Some errors occurred in UnionPayQrCodeController.sendOrder, client = " + client +
                            ", GetQrCode params are: " + JsonUtil.toJson(params));
                    ResponseUtil.responsePrint(response, responseData, logger);
                    return;
                }
            }
            QRCodeUtils.generate((String) responseData.getEntity(), response.getOutputStream());
            logger.info("It is OK in UnionPayQrCodeController.sendOrder");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayQrCodeController.sendOrder", e);
            if (CLIENT_PC.equals(client) || CLIENT_WAP.equals(client)) {
                logger.error("Some errors occurred in UnionPayQrCodeController.sendOrder");
                try {
                    response.sendRedirect(sdkConfig.getFrontFailUrl());
                } catch (IOException e1) {
                    logger.error("Errors occurred in UnionPayQrCodeController.sendOrder when sendRedirect", e1);
                }
            } else {
                ResponseUtil.responsePrint(response, responseData, logger);
            }
        }
    }

    /**
     * Description: 单笔交易查询
     *              1、查询订单支付交易时间必传，billNo必传（订单号、账单号二选一）。订单号为与银联交易的加E加R的订单号。
     *              2、查询退款时billNo、refundSeq（退款订单号）两者必传。
     *              3、查询结果中billStatus的状态，跟结果返回里的billStatus状态是一样的
     * @author: JiuDongDong
     * @param billDate 订单时间（支付回调里，银联C扫B给的账单时间），格式yyyy-MM-dd
     * @param billNo 账单号，也可用merOrderId(支付成功回调，银联给生成的商户订单号)，同样起作用。总之，billNo、orderId最少传一个。
     * @param tradeType 交易类型：1：支付交易  2：退款交易 3：未支付交易
     * @param refundSeq 退款流水号
     * @param mid 未支付完成的时候进行查询，需要携带mid、tid, tradeType=3时
     * @param tid 未支付完成的时候进行查询，需要携带mid、tid, tradeType=3时
     * date: 2019/5/14 15:50
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-QrCode/single-query.htm")
    public void singleQuery (HttpServletResponse response, String billDate, String billNo, String tradeType,
                             String refundSeq, String mid, String tid) {
        logger.info("It is now in UnionPayQrCodeController.singleQuery, the params are: [billDate = {}, " +
                "billNo = {}, tradeType = {}, refundSeq = {}, mid = {}, tid = {}]", billDate, billNo,
                tradeType, refundSeq, mid, tid);
        ResponseData responseData = new ResponseData();
        try {
            // 非空校验
            if (StringUtils.isBlank(billDate) || StringUtils.isBlank(billNo) || StringUtils.isBlank(tradeType)) {
                logger.error("The parameter has empty part for UnionPayQrCodeController.singleQuery, billDate = " +
                        billDate + ", billNo = " + billNo + ", tradeType = " + tradeType + ", refundSeq = " + refundSeq);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            unionPayQrCodeManager.singleQuery(responseData, billDate, billNo, tradeType, refundSeq, mid, tid);
            logger.info("It is OK in UnionPayQrCodeController.singleQuery");
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayQrCodeController.singleQuery", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 支付、退款交易的通知（退款也会通知到此url，注意只有退款成功才会有退款通知。Sun DaWei）
     * @author: JiuDongDong
     * date: 2019/5/13 14:56
     */
    @Adopt
    @RequestMapping(value = "/p/unionpay-qrcode/receiveNotify.htm")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in UnionPayQrCodeController.receiveNotify");
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
            logger.info("Get qrCode pay or refund notify from unionPay is: {}", content.toString());
            unionPayQrCodeManager.receiveNotify(responseData, content.toString());
            //商户收到通知后，需要对通知做出响应：成功收到时响应”SUCCESS”；失败时响应”FAILED”
            response.getWriter().print(SUCCESS);
        } catch (OrderTimeOutException e) {
            try {
                response.getWriter().print(SUCCESS);
            } catch (IOException e1) {
                logger.error("Some errors occurred in UnionPayH5PayController.receiveNotify when response to UnionPayH5Pay", e1);
            }
        } catch (Exception e) {
            logger.error("Errors occurred in UnionPayQrCodeController.receiveNotify", e);
            try {
                response.getWriter().print(FAILED);
            } catch (IOException e1) {
                logger.error("Some errors occurred in UnionPayQrCodeController.receiveNotify when response to UnionPayQrCode", e1);
            }
        }
    }

}
