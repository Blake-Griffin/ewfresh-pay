package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.manager.BalanceManager;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * Description: 关于余额使用的controller
 * @author DuanXiangming
 * Date 2018/4/11
 */
@Controller
public class BalanceController {

    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    @Autowired
    private BalanceManager balanceManager;

    @Autowired
    private RedisLockHandler lockHandler;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;

    /**
     * Description:线下充值或提现的方法
     * @author DuanXiangming
     * @param  payFlow
     * Date    2018/4/19 0019  上午 8:01
     */
    @RequestMapping("/t/reCharge-or-withdrawto.htm")
    public void reCharge(HttpServletResponse response, PayFlow payFlow , Withdrawto withdrawto) {
        logger.info("the reCharge param is -----> [payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        ResponseData responseData = new ResponseData();
        try {
            if (payFlow == null ) {
                logger.warn("the get balance by uid param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payFlow is null");
                return;
            }
            if (payFlow.getPayerPayAmount() == null ) {
                logger.warn("the get balance by uid param payerPayAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerPayAmount is null");
                return;
            }
             if (payFlow.getPayerId() == null && payFlow.getReceiverUserId() == null) {
                logger.warn("the get balance by uid param payerId and receiverUserId are null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerId and receiverUserId is null");
                return;
            }
            if (payFlow.getChannelCode() == null ) {
                logger.warn("the get balance by uid param channelCode is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param channelCode is null");
                return;
            }
            if (payFlow.getChannelName() == null ) {
                logger.warn("the get balance by uid param channelName is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param channelName is null");
                return;
            }
            if (payFlow.getChannelFlowId() == null ) {
                logger.warn("the get balance by uid param channelFlowId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param hannelFlowId is null");
                return;
            }
            if (payFlow.getTradeType() == null ) {
                logger.warn("the get balance by uid param tradeType is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param tradeType is null");
                return;
            }
            balanceManager.reCharge(responseData, payFlow ,withdrawto);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("the reCharge error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("the reCharge err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 充值错误之后提现的方法
     * @author DuanXiangming
     * @param  payFlow
     * Date    2018/4/19 0019  上午 8:01
     */
    @RequestMapping("/t/withdarwo-false.htm")
    public void withdarwoFalse(HttpServletResponse response, PayFlow payFlow ,String uname) {
        logger.info("the withdarwoFalse param is -----> [payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        ResponseData responseData = new ResponseData();
        try {
            if (payFlow == null ) {
                logger.warn("the withdarwoFalse param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payFlow is null");
                return;
            }
            if (payFlow.getPayerPayAmount() == null ) {
                logger.warn("the withdarwoFalse param payerPayAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerPayAmount is null");
                return;
            }
            if (payFlow.getPayerId() == null && payFlow.getReceiverUserId() == null) {
                logger.warn("the withdarwoFalse param payerId and receiverUserId are null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerId and receiverUserId is null");
                return;
            }
            if (payFlow.getChannelCode() == null ) {
                logger.warn("the withdarwoFalse param channelCode is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param channelCode is null");
                return;
            }
            if (payFlow.getChannelName() == null ) {
                logger.warn("the withdarwoFalse param channelName is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param channelName is null");
                return;
            }
            if (payFlow.getChannelFlowId() == null ) {
                logger.warn("the withdarwoFalse param channelFlowId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param hannelFlowId is null");
                return;
            }
            if (payFlow.getTradeType() == null ) {
                logger.warn("the withdarwoFalse param tradeType is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param tradeType is null");
                return;
            }
            balanceManager.withdarwoFalse(responseData, payFlow , uname);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("the reCharge error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("the reCharge err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 获取用户余额的方法
     * @author DuanXiangming
     * @param userId       用户id
     * Date    2018/4/11
     */
    @Adopt
    @RequestMapping("/t/get-balance-by-uid.htm")
    public void getBalanceByUid(HttpServletResponse response,  Long userId ) {
        logger.info("the get balance by uid param is ----->[userId = {}]", userId);
        ResponseData responseData = new ResponseData();
        try {
            if (userId == null ) {
                logger.warn("the get balance by uid param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param userId is null");
                return;
            }
            balanceManager.getBalanceByUid(responseData,  userId);
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
     * Description: urm获取用户余额的方法
     * @author DuanXiangming
     * @param userId       用户id
     * Date    2018/4/11
     */
    @RequestMapping("/t/get-balance-by-uid-back.htm")
    public void getBalanceByUidBack(HttpServletResponse response,  Long userId ) {
        logger.info("the get balance by uid param is ----->[userId = {}]", userId);
        ResponseData responseData = new ResponseData();
        try {
            if (userId == null ) {
                logger.warn("the get balance by uid param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param userId is null");
                return;
            }
            balanceManager.getBalanceByUid(responseData,  userId);
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
     * Description: 冻结余额的方法,用于支付和体现
     * @author DuanXiangming
     * @param amount      涉及的金额
     * @param userId       用户id
     * @param targetAcc   目标账户
     * Date    2018/4/11
     */
    @Adopt
    @RequestMapping("/t/freez-balance.htm")
    public void freezBalance(HttpServletResponse response, BigDecimal amount, Long userId , Long orderId, String targetAcc ) {
        logger.info("the freezBalance param is ----->[amount = {}, userId = {}, orderId = {}]", amount, userId, orderId);
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (amount == null ) {
                logger.warn("the freezBalance param amount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param amount is null");
                return;
            }
            if (userId == null ) {
                logger.warn("the freezBalance param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param userId is null");
                return;
            }
            if (orderId == null ) {
                logger.warn("the freezBalance param orderId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param orderId is null");
                return;
            }
            String lockName = Constants.FREEZE_BANLANCE + Constants.JOINT + userId;
            lock = new Lock(lockName,lockName);
            boolean flag = lockHandler.tryLock(lock);
            if (!flag) {
                logger.info(" commit order agian by lock [lockName = {}]", lockName);
                responseData.setMsg("commit request agian");
                responseData.setCode(ResponseStatus.OK.getValue());
                return;
            }
            balanceManager.freezBalance(responseData, amount, userId, orderId, targetAcc);
            lockHandler.releaseLock(lock);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("freez balance error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("releaseLock failed", e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("freez balance error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 使用余额的支付方法
     * @author DuanXiangming
     * @param  payFlow       封装交易流水的对象
     * Date    2018/4/11
     */
    @Adopt
    @RequestMapping("/t/pay-by-balance.htm")
    public void payByBalance(HttpServletResponse response, PayFlow payFlow , Long timeStamp) {
        logger.info("the pay by balance param is ----->[payFlow = {} ]", ItvJsonUtil.toJson(payFlow));
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (payFlow == null){
                logger.warn("the pay by balance param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payFlow is null");
                return;
            }
            if (payFlow.getOrderAmount() == null ) {
                logger.warn("the pay by balance param orderAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param orderAmount is null");
                return;
            }
            if (payFlow.getPayerPayAmount() == null ) {
                logger.warn("the pay by balance param payerPayAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerPayAmount is null");
                return;
            }
            if (payFlow.getPayerId() == null ) {
                logger.warn("the pay by balance param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance userId is null");
                return;
            }
            if (payFlow.getOrderId() == null ) {
                logger.warn("the pay by balance param interactionId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance interactionId is null");
                return;
            }
            if (payFlow.getInteractionId() == null ) {
                logger.warn("the pay by balance param interactionId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance interactionId is null");
                return;
            }
            if (timeStamp == null ) {
                logger.warn("the pay by balance param timeStamp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance timeStamp is null");
                return;
            }
            /*set pay lock start*/
            String lockName = Constants.PAY_BY_BANLANCE + Constants.JOINT + payFlow.getPayerId();
            lock = new Lock(lockName,lockName);
            boolean lockFlag = lockHandler.tryLock(lock);
            if (!lockFlag) {
                logger.info(" commit order agian by lock [lockName = {}]", lockName);
                responseData.setMsg("commit request agian");
                responseData.setCode(ResponseStatus.COMMITAGAIN.getValue());
                return;
            }
            /*set pay lock end*/
            /*set freeze lock start*/
            balanceManager.payByBalance(responseData, payFlow, timeStamp);
            lockHandler.releaseLock(lock);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error(" pay by balance error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error(" failed to release pay lock", e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("pay by balance error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }


    /**
     * Description: 内部扣减余额的方法
     * @author DuanXiangming
     * @param  payFlow       封装交易流水的对象
     * Date    2018/4/11
     */
    @RequestMapping("/t/abatement-balance.htm")
    public void abatementBalance(HttpServletResponse response, PayFlow payFlow) {
        logger.info("the abatement balance param is ----->[payFlow = {} ]", ItvJsonUtil.toJson(payFlow));
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (payFlow == null){
                logger.warn("the pay by balance param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payFlow is null");
                return;
            }
            if (payFlow.getOrderId() == null ) {
                logger.warn("the pay by balance param orderId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param orderId is null");
                return;
            }
            if (payFlow.getOrderAmount() == null ) {
                logger.warn("the pay by balance param orderAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param orderAmount is null");
                return;
            }
            if (payFlow.getPayerPayAmount() == null ) {
                logger.warn("the pay by balance param payerPayAmount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param payerPayAmount is null");
                return;
            }
            if (payFlow.getPayerId() == null ) {
                logger.warn("the pay by balance param payerId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance payerId is null");
                return;
            }
            if (payFlow.getReceiverUserId() == null ) {
                logger.warn("the pay by balance param receiverUserId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance receiverUserId is null");
                return;
            }
            if (payFlow.getTradeType() == null ) {
                logger.warn("the pay by balance param tradeType is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the pay by balance receiverUserId is null");
                return;
            }
            /*set pay lock start*/
            Short tradeType = payFlow.getTradeType();
            String uid = accountFlowDescUtil.getUid(tradeType, payFlow);
            String lockName = Constants.ABATEMENT_BALANCE + Constants.JOINT + uid;
            lock = new Lock(lockName,lockName);
            boolean lockFlag = lockHandler.tryLock(lock);
            if (!lockFlag) {
                logger.info(" commit order agian by lock [lockName = {}]", lockName);
                responseData.setMsg("commit request agian");
                responseData.setCode(ResponseStatus.COMMITAGAIN.getValue());
                return;
            }
            /*set pay lock end*/
            balanceManager.abatementBalance(responseData, payFlow);
            lockHandler.releaseLock(lock);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error(" abatement balance error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error(" failed to release pay lock", e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("abatement balance error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    @Adopt
    @RequestMapping("/p/testHeader.htm")
    public void getAccountFlowDescUtil(HttpServletRequest request) {
        String ip = null;

        String ipAddresses = request.getHeader("X-Forwarded-For");

        System.out.println(ipAddresses);

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            System.out.println(1);
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            System.out.println(2);
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            System.out.println(3);
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            System.out.println(4);
            ipAddresses = request.getHeader("X-Real-IP");
        }

        if (ipAddresses != null && ipAddresses.length() != 0) {
            System.out.println(5);
            ip = ipAddresses.split(",")[0];
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            System.out.println(6);
            ip = request.getRemoteAddr();
        }
        System.out.println(ip);
    }
}
