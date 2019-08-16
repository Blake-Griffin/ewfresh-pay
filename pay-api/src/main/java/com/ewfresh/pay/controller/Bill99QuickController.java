package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.manager.Bill99QuickManager;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static com.ewfresh.pay.util.Constants.QUICK_PAY_UNBIND_CARD;
import static com.ewfresh.pay.util.Constants.QUICK_PAY_VALID_CODE;
import static com.ewfresh.pay.util.Constants.STR_ONE;

/**
 * description: Bill99快捷业务的接入层
 * @author: JiuDongDong
 * date: 2018/9/13.
 */
@Controller
public class Bill99QuickController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99QuickManager bill99QuickManager;
    @Autowired
    private RedisLockHandler lockHandler;

    /**
     * Description: 卡信息验证-获取动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param storablePan       缩略卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param expiredDate       有效期（贷记卡必传）
     * @param cvv2              卡校验码（贷记卡必传）
     * @param bindType          接入方式
     * @param phoneNO           手机号码
     * date: 2018/9/18 9:32
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-token-before-bind.htm")
    public void getTokenBeforeBind(HttpServletResponse response, String customerId, String pan, String storablePan,
                                   String cardHolderName, String idType, String cardHolderId, String expiredDate,
                                   String cvv2, String bindType, String phoneNO) {
        logger.info("It is now in Bill99QuickController.getTokenBeforeBind, the input parameter is: " +
                        "[customerId = {}, pan = {}, storablePan = {}, cardHolderName = {}, idType = {}, cardHolderId = {}, " +
                        "expiredDate = {}, cvv2 = {}, bindType = {}, phoneNO = {}]",
                customerId, pan, storablePan, cardHolderName, idType, cardHolderId, expiredDate, cvv2, bindType, phoneNO);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(pan) || StringUtils.isBlank(phoneNO) ||
            StringUtils.isBlank(cardHolderName) || StringUtils.isBlank(idType) || StringUtils.isBlank(cardHolderId)) {
                logger.error("The parameter customerId or pan or phoneNO or cardHolderName, idType, cardHolderId has " +
                        "null part, check out it!!! customerId = " + customerId + ", pan = " + pan + ", phoneNO = " + phoneNO);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getTokenBeforeBind(responseData, customerId, pan, storablePan,
                    cardHolderName, idType, cardHolderId, expiredDate, cvv2, bindType, phoneNO);
            logger.info("It is OK in Bill99QuickController.getTokenBeforeBind");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getTokenBeforeBind", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 卡信息验证-使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param validCode         验证码
     * @param token             安全校验值
     * @param externalRefNumber 外部参考号
     * @param phoneNO           手机号码
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param expiredDate       有效期（贷记卡必传）
     * @param cvv2              卡校验码（贷记卡必传）
     * date: 2018/9/17 17:12
     */
    @Adopt
    @RequestMapping("/t/bill/quick/bind-card-with-code.htm")
    public void bindCardWithDynamicCode(HttpServletResponse response, String customerId, String pan, String validCode,
                                        String token, String externalRefNumber, String phoneNO, String cardHolderName,
                                        String idType, String cardHolderId, String expiredDate, String cvv2) {
        logger.info("It is now in Bill99QuickController.bindCardWithDynamicCode, the input parameter is: " +
                        "[customerId = {}, pan = {}, validCode = {}, token = {}, externalRefNumber = {}, phoneNO = {}," +
                        "cardHolderName = {}, idType = {}, cardHolderId = {}, expiredDate = {}, cvv2 = {}]",
                customerId, pan, validCode, token, externalRefNumber, phoneNO, cardHolderName, idType, cardHolderId,
                expiredDate, cvv2);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(pan) || StringUtils.isBlank(validCode) ||
                    StringUtils.isBlank(token) || StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(phoneNO) ||
                    StringUtils.isBlank(cardHolderName) || StringUtils.isBlank(idType) || StringUtils.isBlank(cardHolderId)) {
                logger.error("The parameter of Bill99QuickController.bindCardWithDynamicCode has null part, check them!!!");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.bindCardWithDynamicCode(responseData, customerId, pan, validCode, token, externalRefNumber,
                    phoneNO, cardHolderName, idType, cardHolderId, expiredDate, cvv2);
            logger.info("It is OK in Bill99QuickController.bindCardWithDynamicCode");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.bindCardWithDynamicCode", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 卡信息验证---不使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param phoneNO           手机号码
     * date: 2018/9/14 17:03
     */
    @Adopt
    @RequestMapping("/t/bill/quick/bind-card-without-code.htm")
    public void bindCardWithoutDynamicCode(HttpServletResponse response, String customerId, String pan, String cardHolderName,
                                   String idType, String cardHolderId, String phoneNO) {
        logger.info("It is now in Bill99QuickController.bindCardWithoutDynamicCode, the input parameter is: " +
                "[customerId = {}, pan = {}, cardHolderName = {}, idType = {}, cardHolderId = {}, phoneNO = {}]",
                customerId, pan, cardHolderName, idType, cardHolderId, phoneNO);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(pan) || StringUtils.isBlank(cardHolderName) ||
                    StringUtils.isBlank(idType) || StringUtils.isBlank(cardHolderId) || StringUtils.isBlank(phoneNO)) {
                logger.warn("The parameter of Bill99QuickController.bindCardWithoutDynamicCode has null part, check out it!!!");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.bindCardWithoutDynamicCode(responseData, customerId, pan, cardHolderName, idType, cardHolderId, phoneNO);
            logger.info("It is OK in Bill99QuickController.bindCardWithoutDynamicCode");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.bindCardWithoutDynamicCode", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 卡信息查询
     * @author: JiuDongDong
     * @param cardNo    卡号
     * @param txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param customerId  客户号
     * date: 2018/9/17 10:39
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-card-info.htm")
    public void getCardInfo(HttpServletResponse response, String cardNo, @RequestParam(defaultValue = "PUR") String txnType,
                            String customerId) {
        logger.info("It is now in Bill99QuickController.getCardInfo, the input parameter is: [cardNo = {}, txnType = {}," +
                " customerId = {}]", cardNo, txnType, customerId);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(cardNo)) {
                logger.warn("The parameter cardNo or customerId is empty for Bill99QuickController.getCardInfo");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getCardInfo(responseData, cardNo, txnType, customerId);
            logger.info("It is OK in Bill99QuickController.getCardInfo");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getCardInfo", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: PCI查询卡信息
     * @author: JiuDongDong
     * @param customerId    客户号
     * @param cardType      卡类型，非必填: 0001 信用卡类型 0002 借记卡类型，当卡类型不输入时，默认卡类型为信用卡
     * @param storablePan   缩略卡号
     * @param bankId        银行代码
     * date: 2018/9/14 9:43
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-pci-card-info.htm")
    public void pciCardInfo(HttpServletResponse response, String customerId, @RequestParam(defaultValue = "0001")
            String cardType, String storablePan, String bankId) {
        logger.info("It is now in Bill99QuickController.pciCardInfo, the input parameter is: " + "[customerId = {}"
                + ", cardType = {}, storablePan = {}, bankId = {}]", customerId, cardType, storablePan, bankId);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId)) {
                logger.warn("The parameter customerId is empty for Bill99QuickController.pciCardInfo");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getPciCardInfo(responseData, customerId, cardType, storablePan, bankId);
            logger.info("It is OK in Bill99QuickController.pciCardInfo");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.pciCardInfo", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: PCI数据删除（解绑接口）
     * @author: JiuDongDong
     * @param customerId    客户号
     * @param pan           卡号
     * @param storablePan   缩略卡号
     * @param bankId        银行代码
     * @param validCode     手机验证码
     * date: 2018/9/18 16:58
     */
    @Adopt
    @RequestMapping("/t/bill/quick/pci-delete-card-info.htm")
    public void pciDeleteCardInfo(HttpServletResponse response, String customerId, String pan, String storablePan,
                                  String bankId, String validCode) {
        logger.info("It is now in Bill99QuickController.pciDeleteCardInfo, the input parameter is: [customerId = {}"
                + ", pan = {}, storablePan = {}, bankId = {}, validCode = {}]", customerId, pan, storablePan, bankId
                , validCode);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(storablePan) || StringUtils.isBlank(bankId) ||
                    StringUtils.isBlank(validCode)) {
                logger.error("The parameter customerId or storablePan or bankId or validCode is empty for " +
                        "Bill99QuickController.pciDeleteCardInfo");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.pciDeleteCardInfo(responseData, customerId, pan, storablePan, bankId, validCode);
            logger.info("It is OK in Bill99QuickController.pciDeleteCardInfo");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.pciDeleteCardInfo", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 获取银行代码
     * @author: JiuDongDong
     * @param channelCode   渠道代码：1 快钱个人网银  2 快钱企业网银  3 快钱个人快捷
     * @param cardType		卡类型，0001 信用卡类型 0002 借记卡类型，不传表示所有类型
     * date: 2018/9/20 9:35
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-bank-code.htm")
    public void getPayChannelByChannelName(HttpServletResponse response, String channelCode, String cardType) {
        logger.info("It is now in Bill99QuickController.getPayChannelByChannelName, the input parameter are: " +
                "[channelCode = {}, cardType = {}]", channelCode, cardType);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(channelCode)) {
                logger.error("The parameter channelCode is empty for Bill99QuickController.getPayChannelByChannelName");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            if (!"1".equals(channelCode) && !"2".equals(channelCode) && !"3".equals(channelCode) &&
                    !"4".equals(channelCode) && !"5".equals(channelCode) && !"6".equals(channelCode)) {
                logger.error("The parameter channelName is not correct for Bill99QuickController." +
                        "getPayChannelByChannelName, channelCode = ", channelCode);
                responseData.setCode(ResponseStatus.PARAMERR.getValue());
                responseData.setMsg(ResponseStatus.PARAMERR.name());
                return;
            }
            if (StringUtils.isNotBlank (cardType) && !"0001".equals(cardType) && !"0002".equals(cardType)) {
                logger.error("The parameter cardType is not correct for Bill99QuickController.getPayChannelByChannelName,"
                        + " cardType = ", cardType);
                responseData.setCode(ResponseStatus.PARAMERR.getValue());
                responseData.setMsg(ResponseStatus.PARAMERR.name());
                return;
            }
            bill99QuickManager.getPayChannelByChannelName(responseData, channelCode, cardType);
            logger.info("It is OK in Bill99QuickController.getPayChannelByChannelName");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getPayChannelByChannelName", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 查询用户在快钱绑定的的所有有效的快捷银行卡
     * @author: JiuDongDong
     * @param customerId  客户号
     * date: 2018/9/21 16:48
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-all-cards.htm")
    public void getAllAbleBanksByUserId(HttpServletResponse response, String customerId) {
        logger.info("It is now in Bill99QuickController.getAllAbleBanksByUserId, the input parameter is: " +
                "[customerId = {}]", customerId);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId)) {
                logger.error("The parameter customerId is empty for Bill99QuickController.getAllAbleBanksByUserId");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getAllAbleBanksByUserId(responseData, customerId);
            logger.info("It is OK in Bill99QuickController.getAllAbleBanksByUserId");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getAllAbleBanksByUserId", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 设置快钱快捷默认银行卡
     * @author: JiuDongDong
     * @param customerId    客户号
     * @param storablePan   含*全卡号，后台处理*号
     * date: 2018/9/22 16:48
     */
    @Adopt
    @RequestMapping("/t/bill/quick/set-default-card.htm")
    public void setDefaultCard(HttpServletResponse response, String customerId, String storablePan) {
        logger.info("It is now in Bill99QuickController.setDefaultCard, the input parameter are: " +
                "[customerId = {}, storablePan = {}]", customerId, storablePan);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(storablePan)) {
                logger.error("The parameter customerId or storablePan is empty for Bill99QuickController" +
                        ".setDefaultCard");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.setDefaultCard(responseData, customerId, storablePan);
            logger.info("It is OK in Bill99QuickController.setDefaultCard");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.setDefaultCard", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 一键支付（普通版）
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param payToken          签约协议号
     * @param amount            交易金额,以元为单位，小数点后最多两位
     * @param spFlag            特殊交易标志（默认QPay02）
     * @param interactiveStatus 消息状态（默认TR1）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param validCode         手机验证码
     * @param bindCardFlag      绑卡标志，值为0或者1。1表示当前一键支付流程为：绑卡 + 一键支付（此时使用银行发送的验证码）。0表示使用已绑定的银行卡支付（此时使用易网聚鲜发送的验证码）
     * date: 2018/9/23 13:42
     */
    @Adopt
    @RequestMapping("/t/bill/quick/quick-pay-common.htm")
    public void quickPayCommon(HttpServletResponse response, String customerId, String payToken, String amount,
                               @RequestParam(defaultValue = "QPay02") String spFlag, @RequestParam(defaultValue = "TR1")
                               String interactiveStatus, String txnType, String externalRefNumber, String validCode,
                               String bindCardFlag) {
        logger.info("It is now in Bill99QuickController.quickPayCommon, the input parameters are: " +
                "[customerId = {}, payToken = {}, amount = {}, spFlag = {}, interactiveStatus = {}, txnType = {}, " +
                        "externalRefNumber = {}, validCode = {}, bindCardFlag = {}]", customerId, payToken, amount,
                spFlag, interactiveStatus, txnType, externalRefNumber, validCode, bindCardFlag);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(payToken) || StringUtils.isBlank(amount) ||
                    StringUtils.isBlank(spFlag) || StringUtils.isBlank(interactiveStatus) || StringUtils.isBlank(txnType) ||
                    StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(validCode) || StringUtils.isBlank(bindCardFlag)) {
                logger.error("The parameters for Bill99QuickController.quickPayCommon has null part, check them: " +
                        "customerId = " + customerId + ", payToken = " + payToken + ", amount = " + amount + ", spFlag = "
                        + spFlag + ", interactiveStatus = " + interactiveStatus + ", txnType = " + txnType + ", " +
                        "externalRefNumber = " + externalRefNumber + ", validCode = " + validCode + ", bindCardFlag = "
                        + bindCardFlag);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.quickPayCommon(responseData, customerId, payToken, amount, spFlag, interactiveStatus,
                    txnType, externalRefNumber, validCode, bindCardFlag);
            logger.info("It is OK in Bill99QuickController.quickPayCommon");
        } catch (ShouldPayNotEqualsException e) {
            logger.error("Web should pay money not equals redis", e);
            responseData.setCode(ResponseStatus.SHOULDPAYNOTEQUALS.getValue());
            responseData.setMsg(ResponseStatus.SHOULDPAYNOTEQUALS.name());
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.quickPayCommon", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 接收tr3信息（支付）
     * @author: JiuDongDong
     * date: 2018/9/23 13:42
     */
    @Adopt
    @RequestMapping("/t/bill/quick/receive-tr3.htm")
    public void receiveTR3ToTR4(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in Bill99QuickController.receiveTR3ToTR4");
        ResponseData responseData = new ResponseData();
        try {
            InputStream is = request.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("TR3信息：" + sb.toString());
            String signedResponseInfo = sb.toString();
            if (StringUtils.isBlank(signedResponseInfo)) {
                logger.error("Bill99 send tr3 to merchant is null");
                response2Bill99Quick(response,"00");// TODO 暂定00，找快钱确认如何返回应答码
                return;
            }
            bill99QuickManager.receiveTR3ToTR4(responseData, signedResponseInfo);
            logger.info("It is OK in Bill99QuickController.receiveTR3ToTR4");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.receiveTR3ToTR4", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 支付时获取动态码，易网聚鲜发短信（【标准快捷API支付】）
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param storablePan       加*卡号
     * @param amount            交易金额
     * @param functionType      功能类型，1：支付	2：解绑快捷银行卡
     * date: 2018/9/28 16:31
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-dynamic-valid-code-self.htm")
    public void getDynamicValidCodeSelf(HttpServletResponse response, String customerId, String externalRefNumber,
                                    String storablePan, String amount, String functionType) {
        logger.info("It is now in Bill99QuickController.getDynamicValidCodeSelf, the input parameter are: " +
                        "[customerId = {}, externalRefNumber = {}, storablePan = {}, amount = {}, functionType = {}]",
                customerId, externalRefNumber, storablePan, amount, functionType);
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(storablePan)
                    || StringUtils.isBlank(amount) || StringUtils.isBlank(functionType)) {
                logger.error("The parameter customerId or externalRefNumber or storablePan or amount or functionType is " +
                        "empty for Bill99QuickController.getDynamicValidCodeSelf");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            // 分布式锁
            String lockName;
            if (STR_ONE.equals(functionType)) {
                lockName = externalRefNumber + QUICK_PAY_VALID_CODE;
            } else {
                lockName = storablePan + QUICK_PAY_UNBIND_CARD;
            }
            lock = new Lock(lockName, lockName);
            boolean lockFlag = lockHandler.getLock(lock, 60*1000L, 200L, 60*1000L);
            if (!lockFlag) {
                logger.info("You are applying to get dynamic valid code for more than one time at the same time");
                bill99QuickManager.getRandomNum(responseData, customerId);
            } else {
                bill99QuickManager.getDynamicValidCodeSelf(responseData, customerId, externalRefNumber, storablePan, amount,
                        functionType);
            }
            logger.info("It is OK in Bill99QuickController.getDynamicValidCodeSelf");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getDynamicValidCodeSelf", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("Release lock failed", e1);
            }
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 支付时获取动态码，快钱发短信（【认证支付、协议支付】）
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param storablePan       加*卡号
     * @param amount            交易金额
     * date: 2018/9/19 13:23
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-dynamic-valid-code.htm")
    public void getDynamicValidCode(HttpServletResponse response, String customerId, String externalRefNumber,
                                    String storablePan, String amount) {
        logger.info("It is now in Bill99QuickController.getDynamicValidCode, the input parameter are: [customerId = {}, " +
                "externalRefNumber = {}, storablePan = {}, amount = {}]", customerId, externalRefNumber, storablePan, amount);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(storablePan) ||
                    StringUtils.isBlank(amount)) {
                logger.error("The parameter customerId or externalRefNumber or storablePan or amount is empty " +
                        "for Bill99QuickController.getDynamicValidCode");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getDynamicValidCode(responseData, customerId, externalRefNumber, storablePan, amount);
            logger.info("It is OK in Bill99QuickController.getDynamicValidCode");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getDynamicValidCode", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 动态码支付
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param interactiveStatus 消息状态（默认TR1）
     * @param spFlag            特殊交易标志（默认QuickPay）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param amount            交易金额,以元为单位，小数点后最多两位
     * @param storablePan       加*卡号
     * @param validCode         手机验证码
     * @param token             手机验证码令牌
     * @param payToken          签约协议号
     * @param payBatch          快捷支付批次, 1首次支付, 2再次支付
     * @param savePciFlag       是否保存鉴权信息, 0不保存, 1保存
     * date: 2018/9/27 10:11
     */
    @Adopt
    @RequestMapping("/t/bill/quick/dynamic-code-pay.htm")
    public void dynamicCodePay(HttpServletResponse response, String customerId, @RequestParam(defaultValue = "TR1")
            String interactiveStatus, @RequestParam(defaultValue = "QuickPay") String spFlag, String txnType,
            String externalRefNumber, String amount, String storablePan, String validCode, String token,
            String payToken, String payBatch, @RequestParam(defaultValue = "0") String savePciFlag) {
        logger.info("It is now in Bill99QuickController.dynamicCodePay, the input parameter are: [customerId = {}, " +
                "interactiveStatus = {}, spFlag = {}, txnType = {}, externalRefNumber = {}, amount = {}, storablePan = {}, " +
                "validCode = {}, token = {}, payToken = {}, payBatch = {}, savePciFlag = {}]",customerId,
                interactiveStatus, spFlag, txnType, externalRefNumber, amount, storablePan, validCode, token,
                payToken, payBatch, savePciFlag);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId) || StringUtils.isBlank(interactiveStatus) || StringUtils.isBlank(spFlag) ||
                    StringUtils.isBlank(txnType) || StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(amount) ||
                    StringUtils.isBlank(storablePan) || StringUtils.isBlank(validCode) || StringUtils.isBlank(token) ||
                    StringUtils.isBlank(payToken) || StringUtils.isBlank(payBatch) || StringUtils.isBlank(savePciFlag)) {
                logger.error("The parameter has null for Bill99QuickController.dynamicCodePay, check them");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.dynamicCodePay(responseData, customerId, interactiveStatus, spFlag, txnType, externalRefNumber,
                    amount, storablePan, validCode, token, payToken, payBatch, savePciFlag);
            logger.info("It is OK in Bill99QuickController.dynamicCodePay");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.dynamicCodePay", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: VPOS_CNP查询交易
     * @author: JiuDongDong
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号
     * @param refNumber         检索参考号
     * @param isSelfPro			是否自营商品，0否1是
     * date: 2018/9/27 17:34
     */
    @Adopt
    @RequestMapping("/t/bill/quick/query-order.htm")
    public void queryOrder(HttpServletResponse response, @RequestParam(defaultValue = "PUR") String txnType,
                           String externalRefNumber, String refNumber, String isSelfPro) {
        logger.info("It is now in Bill99QuickController.queryOrder, the input parameter are: [txnType = {}, " +
                        "externalRefNumber = {}, refNumber = {}, isSelfPro = {}]", txnType, externalRefNumber,
                refNumber, isSelfPro);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(txnType) || (StringUtils.isBlank(externalRefNumber) && StringUtils.isBlank(refNumber))) {
                logger.error("The parameter has null for Bill99QuickController.queryOrder, check them, txnType = "
                        + txnType + ", externalRefNumber = " + externalRefNumber + ", refNumber = " + refNumber);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.queryOrder(responseData, txnType, externalRefNumber, refNumber, isSelfPro);
            logger.info("It is OK in Bill99QuickController.queryOrder");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.queryOrder", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 退货交易
     * @author: JiuDongDong
     * @param interactiveStatus 消息状态（默认TR1）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param entryTime         商户端交易时间
     * @param amount            退款金额,以元为单位，小数点后最多两位
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param origRefNumber     原检索参考号，对应原交易的检索参考号
     * date: 2018/9/28 14:52
     */
    @Adopt
    @RequestMapping("/t/bill/quick/refund-order.htm")
    public void refundOrder(HttpServletResponse response, @RequestParam(defaultValue = "TR1") String interactiveStatus,
                            @RequestParam(defaultValue = "RFD") String txnType, String entryTime, String amount,
                            String externalRefNumber, String origRefNumber) {
        logger.info("It is now in Bill99QuickController.refundOrder, the input parameter are: [interactiveStatus = {}" +
                        ", txnType = {}, entryTime = {}, amount = {}, externalRefNumber = {}, origRefNumber = {}]",
                interactiveStatus, txnType, entryTime, amount, externalRefNumber, origRefNumber);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(interactiveStatus) || StringUtils.isBlank(txnType) || StringUtils.isBlank(entryTime)
                || StringUtils.isBlank(amount) || StringUtils.isBlank(externalRefNumber) || StringUtils.isBlank(origRefNumber)) {
                logger.error("The parameter has null for Bill99QuickController.refundOrder, check them, txnType = " + txnType +
                        ", externalRefNumber = " + externalRefNumber + ", interactiveStatus = " + interactiveStatus + ", " +
                        "entryTime = " + entryTime + ", amount = " + amount + ", origRefNumber = " + origRefNumber);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.refundOrder(responseData, interactiveStatus, txnType, entryTime, amount, externalRefNumber,
                    origRefNumber);
            logger.info("It is OK in Bill99QuickController.refundOrder");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.refundOrder", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 获取注册手机号
     * @author: JiuDongDong
     * @param customerId        客户号
     * date: 2018/11/5 11:15
     */
    @Adopt
    @RequestMapping("/t/bill/quick/get-phone.htm")
    public void getPhnoeCheckCodeByUid(HttpServletResponse response, String customerId) {
        logger.info("It is now in Bill99QuickController.getPhnoeCheckCodeByUid, the input parameter is: " +
                        "[customerId = {}]", customerId);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(customerId)) {
                logger.error("The parameter has null for Bill99QuickController.getPhnoeCheckCodeByUid, " +
                        "check it, customerId = " + customerId);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.getPhnoeCheckCodeByUid(responseData, customerId);
            logger.info("It is OK in Bill99QuickController.getPhnoeCheckCodeByUid");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.getPhnoeCheckCodeByUid", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 将用户某个手机号下绑定的快钱快捷银行卡置为失效
     * @author: JiuDongDong
     * @param userId    用户id
     * @param newMobilePhone   新手机号
     * @param oldMobilePhone   旧手机号
     * date: 2018/11/5 15:11
     */
    @Adopt
    @RequestMapping("/t/bill/quick/update-phone-expired.htm")
    public void updatePhoneChangedExpired(HttpServletResponse response, Long userId, String newMobilePhone,
                                          String oldMobilePhone) {
        logger.info("It is now in Bill99QuickController.updatePhoneChangedExpired, the input parameter is: " +
                " [userId = {}, newMobilePhone = {}, oldMobilePhone = {}]", userId, newMobilePhone, oldMobilePhone);
        ResponseData responseData = new ResponseData();
        try {
            if (null == userId || StringUtils.isBlank(newMobilePhone) || StringUtils.isBlank(oldMobilePhone)) {
                logger.error("The parameter has null for Bill99QuickController.updatePhoneChangedExpired, " +
                        "check them, userId = " + userId + ", newMobilePhone = " + newMobilePhone + ", " +
                        "oldMobilePhone = " + oldMobilePhone);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.updatePhoneChangedExpired(responseData, userId, newMobilePhone, oldMobilePhone);
            logger.info("It is OK in Bill99QuickController.updatePhoneChangedExpired");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.updatePhoneChangedExpired", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 查询今天是否使用快钱快捷的农业银行,交通银行,兴业银行,光大银行,中信银行,邮储银行,上海银行进行支付
                    以上这几个银行是不支持当天申请退款的，隔天是可以申请的
     * @author: JiuDongDong
     * @param orderId   父订单号
     * date: 2019/1/23 16:28
     */
    @Adopt
    @RequestMapping("/p/bill/quick/if-use-special-bank.htm")
    public void ifUseSpecialBank(HttpServletResponse response, Long orderId) {
        logger.info("It is now in Bill99QuickController.ifUseSpecialBank, the input parameter is: " +
                " [orderId = {}]", orderId);
        ResponseData responseData = new ResponseData();
        try {
            if (null == orderId) {
                logger.error("The parameter orderId is null for Bill99QuickController.ifUseSpecialBank!");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99QuickManager.ifUseSpecialBank(responseData, orderId);
            logger.info("It is OK in Bill99QuickController.ifUseSpecialBank");
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99QuickController.ifUseSpecialBank", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 接收回调或对账单信息后向Bill99响应信息
     * @author: JiuDongDong
     * @param message 1 或 0, 0成功  1失败
     * date: 2018/9/26 13:18
     */
    private void response2Bill99Quick(HttpServletResponse httpServletResponse, String message) {
        BufferedWriter bufferedWriter;
        PrintWriter writer;
        try {
            writer = httpServletResponse.getWriter();
            bufferedWriter = new BufferedWriter(writer);
//            httpServletResponse.setContentType("text/html;charset=utf-8");
            // TODO 写出响应信息，内容待定
            bufferedWriter.write(message);// 写出响应信息，内容待定
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("response to Bill99Quick error");
        }

    }

}
