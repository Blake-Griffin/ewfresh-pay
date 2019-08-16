package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.RefundManager;
import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.util.*;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * Description:关于退款的controller
 * @author DuanXiangming
 * Date 2018/4/27 0027
 */
@Controller
public class  RefundController {

    private static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    @Autowired
    private RefundManager refundManager;
    @Autowired
    private RedisLockHandler lockHandler;

    @RequestMapping("/t/refund.htm")
    public void refund(HttpServletResponse response, RefundParams refundParam){
        logger.info("the refund param is -----> [refundParam = {}]", ItvJsonUtil.toJson(refundParam));
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (refundParam == null ) {
                logger.warn("the refund param orderId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the refund param orderId is null");
                return;
            }
            if (refundParam.getTradeType() == null){
                logger.warn("the refund param trade type is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the refund param trade type is null");
                return;
            }
            String lockName = Constants.REFUND_KEY + Constants.JOINT + refundParam.getParentId();
            lock = new Lock(lockName,lockName);
            boolean flag = lockHandler.tryLock(lock);
            if (!flag) {
                logger.info(" can't get refund lock  [lockName = {}]", lockName);
                responseData.setMsg("can't get refund lock");
                responseData.setCode(ResponseStatus.OK.getValue());
                return;
            }
            refundManager.refund(responseData,refundParam);
            logger.info(responseData.getMsg());
        } catch (RefundBill99ResponseNullException e) {
            logger.error("Ewfresh refund apply success, but 99bill response null", e);
            responseData.setCode(ResponseStatus.REFUNDBILL99RESPONSENULL.getValue());
            responseData.setMsg("Ewfresh refund apply success, but 99bill response null");
        } catch (UnsupportedEncodingException e) {
            logger.error("Refund failed because of UnsupportedEncodingException", e);
            responseData.setCode(ResponseStatus.UNSUPPORTEDENCODING.getValue());
            responseData.setMsg("Refund failed because of UnsupportedEncodingException");
        } catch (RefundParamNullException e) {
            logger.error("Refund param is not enough", e);
            responseData.setCode(ResponseStatus.REFUNDPARAMNULL.getValue());
            responseData.setMsg("Refund param is not enough");
        } catch (RefundBill99HandleException e) {
            logger.error("Ewfresh refund apply success, but 99bill handle failed", e);
            responseData.setCode(ResponseStatus.REFUNDBILL99HANDLEEXCEPTION.getValue());
            responseData.setMsg("Ewfresh refund apply success, but 99bill handle failed");
        } catch (DocumentException e) {
            logger.error("Parse xml occurred error", e);
            responseData.setCode(ResponseStatus.DOCUMENTEXCEPTION.getValue());
            responseData.setMsg("Parse xml occurred error");
        } catch (RefundHttpToBill99FailedException e) {
            logger.error("Error occurred when http to 99bill", e);
            responseData.setCode(ResponseStatus.REFUNDHTTPTOBILL99FAILED.getValue());
            responseData.setMsg("Error occurred when http to 99bill");
        } catch (PayFlowFoundNullException e) {
            logger.error("Not found pay flow", e);
            responseData.setCode(ResponseStatus.PAYFLOWFOUNDNULL.getValue());
            responseData.setMsg("Not found pay flow");
        } catch (Bill99NotFoundThisOrderException e) {
            logger.error("Bill99 not found this order", e);
            responseData.setCode(ResponseStatus.BILL99HASNOTHISTRADE.getValue());
            responseData.setMsg("Bill99 not found this order");
        } catch (RefundAmountMoreThanOriException e) {
            logger.error("Refund amount more than original order amount", e);
            responseData.setCode(ResponseStatus.REFUNDAMOUNTMORETHANORI.getValue());
            responseData.setMsg("Refund amount more than original order amount");
        } catch (TheBankDoNotSupportRefundTheSameDay e) {
            logger.error("The bank do not support refund on the same day with pay", e);
            responseData.setCode(ResponseStatus.THEBANKDONOTSUPPORTREFUNDTHESAMEDAY.getValue());
            responseData.setMsg("Refund amount more than original order amount");
        } catch (WeiXinNotEnoughException e) {
            logger.error("WeiXin not enough balance", e);
            responseData.setCode(ResponseStatus.WEIXINBALANCENOTENOUGH.getValue());
            responseData.setMsg("WeiXin not enough balance");
        } catch (HttpToUnionPayFailedException e) {
            logger.error("Connection is timeout for UnionpayWebWap", e);
            responseData.setCode(ResponseStatus.UNIONCONNECTEX.getValue());
            responseData.setMsg("Connection is timeout for UnionpayWebWap");
        } catch (VerifyUnionPaySignatureException e) {
            logger.error("Verify signature for UnionPayWebWapRefundPolicy Exception", e);
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());
            responseData.setMsg("Verify signature for UnionPayWebWapRefundPolicy Exception");
        } catch (UnionPayHandleRefundException e) {
            logger.error("UnionPayWebWapRefundPolicy handle refund Exception", e);
            responseData.setCode(ResponseStatus.UNIONPAYREFUNDEX.getValue());
            responseData.setMsg("UnionPayWebWapRefundPolicy handle refund Exception");
        } catch (Exception e) {
            logger.error("the refund error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("the refund err");
        } finally {
            if (lock != null){
                try {
                    lockHandler.releaseLock(lock);
                } catch (Exception e) {
                    logger.error(" release refund lock err", e);
                }
            }
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    @Adopt
    @RequestMapping("/t/shop/refund.htm")
    public void shopRefund(HttpServletResponse response, RefundParams refundParam){
        logger.info("the refund param is -----> [refundParam = {}]", ItvJsonUtil.toJson(refundParam));
        ResponseData responseData = new ResponseData();
        try {
            if (refundParam == null ) {
                logger.warn("the shopRefund param orderId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the shopRefund param orderId is null");
                return;
            }
            refundManager.refund(responseData,refundParam);
            logger.info(responseData.getMsg());
        } catch (RefundBill99ResponseNullException e) {
            logger.error("Ewfresh refund apply success, but 99bill response null", e);
            responseData.setCode(ResponseStatus.REFUNDBILL99RESPONSENULL.getValue());
            responseData.setMsg("Ewfresh refund apply success, but 99bill response null");
        } catch (UnsupportedEncodingException e) {
            logger.error("Refund failed because of UnsupportedEncodingException", e);
            responseData.setCode(ResponseStatus.UNSUPPORTEDENCODING.getValue());
            responseData.setMsg("Refund failed because of UnsupportedEncodingException");
        } catch (RefundParamNullException e) {
            logger.error("Refund param is not enough", e);
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            responseData.setMsg("Refund param is not enough");
        } catch (RefundBill99HandleException e) {
            logger.error("Ewfresh refund apply success, but 99bill handle failed", e);
            responseData.setCode(ResponseStatus.REFUNDBILL99HANDLEEXCEPTION.getValue());
            responseData.setMsg("Ewfresh refund apply success, but 99bill handle failed");
        } catch (DocumentException e) {
            logger.error("Parse xml occurred error", e);
            responseData.setCode(ResponseStatus.DOCUMENTEXCEPTION.getValue());
            responseData.setMsg("Parse xml occurred error");
        } catch (RefundHttpToBill99FailedException e) {
            logger.error("Error occurred when http to 99bill", e);
            responseData.setCode(ResponseStatus.REFUNDHTTPTOBILL99FAILED.getValue());
            responseData.setMsg("Error occurred when http to 99bill");
        } catch (PayFlowFoundNullException e) {
            logger.error("Not found pay flow", e);
            responseData.setCode(ResponseStatus.PAYFLOWFOUNDNULL.getValue());
            responseData.setMsg("Not found pay flow");
        } catch (WeiXinNotEnoughException e) {
            logger.error("WeiXin not enough balance", e);
            responseData.setCode(ResponseStatus.WEIXINBALANCENOTENOUGH.getValue());
            responseData.setMsg("WeiXin not enough balance");
        } catch (HttpToUnionPayFailedException e) {
            logger.error("UnionPay refund connection is timeout", e);
            responseData.setCode(ResponseStatus.UNIONCONNECTEX.getValue());
            responseData.setMsg("UnionPay refund connection is timeout");
        } catch (UnionPayHandleRefundException e) {
            logger.error("UnionPayH5PayRefundPolicy handle refund Exception", e);
            responseData.setCode(ResponseStatus.UNIONPAYREFUNDEX.getValue());
            responseData.setMsg("UnionPayH5PayRefundPolicy handle refund Exception");
        } catch (VerifyUnionPaySignatureException e) {
            logger.error("Verify signature for UnionPayWebWapRefundPolicy response not pass!!!", e);
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());
            responseData.setMsg("Verify signature for UnionPayWebWapRefundPolicy response not pass!!!");
        } catch (Exception e) {
            logger.error("the shopRefund error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("the shopRefund err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    @RequestMapping("/t/shop/refund11.htm")
    public void test(HttpServletResponse response) throws Exception {

        ResponseData responseData = new ResponseData();
//        String s = sendSettleRequest();
//        responseData.setEntity(s);
        ResponseUtil.responsePrint(response, responseData, logger);

    }

    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
//    public String sendSettleRequest() throws Exception {
//
//        // 1.获取配置信息
//        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
//        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
//        String merchantPubPath = bill99PayConfigure.getMerchantPubPath();
//        String domainName = bill99PayConfigure.getDomainName();
//        String platformCode = bill99PayConfigure.getPlatformCode();
//
//        Map<String, String> map = new HashMap<>();
//        String outTradeNo = "1";
//        // 2. 设置请求信息
//        SettleData settleData = new SettleData();
//        List<SettleData> settleDatas = new ArrayList<>();
//        settleData.setAmount("100");
//        settleData.setMerchantUid("24");
//        settleData.setOutSubOrderNo("101");
//        settleData.setSettlePeriod("D+0");
//        settleDatas.add(settleData);
//        map.put("outTradeNo", "1");//提现id
//        map.put("platformCode", platformCode);
//        map.put("totalAmount","100");
//        String settleDatasStr = ItvJsonUtil.toJson(settleDatas);
//        map.put("settleData",settleDatasStr);
//        // 签名
//        String signMsgVal = "";
//        signMsgVal = setParam(signMsgVal, "outTradeNo", outTradeNo);
//        signMsgVal = setParam(signMsgVal, "platformCode", platformCode);
//        signMsgVal = setParam(signMsgVal,"totalAmount","100");
//        signMsgVal = setParam(signMsgVal,"settleData",settleDatasStr);
//        logger.info("signMsgVal:" + signMsgVal);
//        Pkipair pki = new Pkipair();
//        String signMsg = pki.signMsg(signMsgVal, merchantCertPath, merchantCertPss);
//        logger.info("the signMsg is ------->{}", signMsg);
//        BindHttpDeal bindHttpDeal = new BindHttpDeal();
//        logger.info("to post  http--------->");
//        MyHttp myHttp = new MyHttp();
//        String post = myHttp.post(domainName + "/settle/pay", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, signMsg);
//        if (StringUtils.isBlank(post)) {
//            logger.info("post is empty");
//            return null;
//        }
//        logger.info("post: " + post);
//        return post;
//    }

    /**
     * Description: 聚合签名数据(发送订单支付请求时加签用)
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/8/13 0013
     * @author: DXM
     */
    private String setParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != "") {
                returns += "&" + paramId + Constants.PARAM_FLAG + paramValue;
            }
        } else {
            if (paramValue != "") {
                returns = paramId + Constants.PARAM_FLAG + paramValue;
            }
        }
        return returns;
    }
}
