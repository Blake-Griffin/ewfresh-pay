package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.manager.WhiteAccountManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * description: 白条支付
 *
 * @author: ZhaoQun
 * date: 2019/3/14.
 */
@Controller
public class WhiteAccountController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WhiteAccountManager whiteAccountManager;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;

    /**
     * Description: 获取白条账户余额
     * @author: ZhaoQun
     * @param userId
     * @return:
     * date: 2019/3/14 14:04
     */
    @Adopt
    @RequestMapping("/t/get-whiteAccount-by-uid.htm")
    public void getWhiteAccountByUid(HttpServletResponse response, Long userId ){
        logger.info("the get balance by uid param is ----->[userId = {}]", userId);
        ResponseData responseData = new ResponseData();
        try {
            if (userId == null ) {
                logger.warn("the get balance by uid param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            whiteAccountManager.getWhiteAccountByUid(responseData,  userId);
            logger.info("get balance by userId success");
        } catch (Exception e) {
            logger.error("get balance by userId error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get balance by userId err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * Description: 白条支付
     * @author: ZhaoQun
     * @param payFlow
     * @param payWay
     * @param timeStamp
     * date: 2019/3/15 16:24
     */
    @Adopt
    @RequestMapping("/t/pay-by-white.htm")
    public void payByWhite(HttpServletResponse response, PayFlow payFlow , Short payWay, Long timeStamp) {
        logger.info("the pay by white param is ----->[payFlow = {} ]", ItvJsonUtil.toJson(payFlow));
        ResponseData responseData = new ResponseData();
        try {
            if (payFlow == null){
                logger.warn("the pay by white param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getOrderAmount() == null ) {
                logger.warn("the pay by white param orderAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getPayerPayAmount() == null ) {
                logger.warn("the pay by white param payerPayAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getPayerId() == null ) {
                logger.warn("the pay by white param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getOrderId() == null ) {
                logger.warn("the pay by white param interactionId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getInteractionId() == null ) {
                logger.warn("the pay by white param interactionId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (timeStamp == null ) {
                logger.warn("the pay by balance param timeStamp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (payFlow.getTradeType() == null ) {
                logger.warn("the pay by balance param tradeType is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            boolean lock = this.balanceAndBarLock.getBalanceAndBarLock(payFlow.getPayerId());//获取分布式锁
            if (lock){
                whiteAccountManager.payByWhite(responseData, payFlow, payWay, timeStamp);
            }
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error(" pay by white error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("pay by white error");
        } finally {
            balanceAndBarLock.releaseLock(payFlow.getPayerId());//释放分布式锁
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

}
