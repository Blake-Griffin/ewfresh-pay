package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.configure.BOBPayConfigure;
import com.ewfresh.pay.manager.BOBManager;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.BOBOrderNoFormat;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.bob.BOBResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *      BOB业务的接入层
 * @author: JiuDongDong
 * date: 2018/4/20.
 */
@Controller
public class BOBController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BOBManager bobManager;
    @Autowired
    private BOBPayConfigure bobPayConfigure;
    private static final String EARNEST = "E";//定金订单号的后缀
    private static final String TAIL = "R";//全款或尾款的订单号后缀

    /**
     * Description: 商户向BOB发送订单支付请求
     * @author: JiuDongDong
     * date: 2018/4/20 11:58
     */
    @Adopt
    @RequestMapping(value = "/t/bob-send-order.htm")
    public void sendOrder(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.sendOrder");
        ResponseData responseData = new ResponseData();
        try {
            // 获取参数并封装
            Map<String, String> params = new HashMap<>();
            String channelType = request.getParameter(Constants.BOB_CHANNEL_TYPE);// 渠道类型
            String ip = request.getParameter(Constants.ORDER_IP);//持卡人IP地址
            String orderNumber = request.getParameter(Constants.ORDER_NO);//商户订单号
            String orderAmount = request.getParameter(Constants.ORDER_AMOUNT);//订单金额 分为单位
            String payType = request.getParameter(Constants.BOB_PAY_TYPE);//支付类型
            String orderDesc = request.getParameter(Constants.BOB_ORDER_DESC);//订单描述：内容必须为"buyGoods"或"recharge"
            // 非空校验
            if (StringUtils.isBlank(orderAmount) || StringUtils.isBlank(payType) || StringUtils.isBlank(orderNumber) ||
                    StringUtils.isBlank(channelType) || StringUtils.isBlank(ip) || StringUtils.isBlank(orderDesc)) {
                logger.error("The parameter orderAmount or payType or orderNumber or channelType or ip or orderDesc is empty for BOBController.sendOrder, " +
                        "[orderAmount = {}, payType = {}, orderNumber = {}, channelType = {}, ip = {}, orderDesc = {}]", orderAmount, payType, orderNumber, channelType, ip, orderDesc);
//                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
//                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                // 重定向到错误页面
                response.sendRedirect(bobPayConfigure.getFrontFailUrl());
                return;
            }
            params.put(Constants.ORDER_AMOUNT, FenYuanConvert.yuan2Fen(orderAmount).toString());// 元转化为分
            params.put(Constants.BOB_PAY_TYPE, payType);
            params.put(Constants.BOB_CHANNEL_TYPE, channelType);
            params.put(Constants.BOB_IP, ip);
//            params.put(Constants.BOB_ORDER_NUMBER, BOBOrderNoFormat.oriOrderNo2BOB19(orderNumber));
            // 如果是订单支付，则将20位加E加R的订单号转化为银行要求的19位; 如果是充值，订单号不做处理
            String BOBOrderNo = orderDesc.contains(Constants.BUY_GOODS) ? BOBOrderNoFormat.bob20OrderNoTo19WithoutER(orderNumber) : orderNumber;
            params.put(Constants.BOB_ORDER_NUMBER, BOBOrderNo);
            params.put(Constants.BOB_ORDER_DESC, orderDesc);// 订单描述
            // 业务处理
            bobManager.sendOrder(responseData, params);
            if (ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                // 响应页面
                Object entity = responseData.getEntity();
                BOBResponseUtil.responsePrintHTML(response, entity, logger);
            }
            if (!ResponseStatus.OK.getValue().equals(responseData.getCode())) {
                logger.error("Some errors occurred in BOBManagerImpl.sendOrder");
                // 重定向到错误页面
                response.sendRedirect(bobPayConfigure.getFrontFailUrl());
            }
            logger.info("It is OK in BOBController.sendOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.sendOrder", e);
            // 重定向到错误页面
            try {
                response.sendRedirect(bobPayConfigure.getFrontFailUrl());
            } catch (IOException e1) {
                logger.error("Some errors occurred in BOBController.sendOrder when sendRedirect", e1);
            }
        }
    }

    /**
     * Description: 支付、退款应答
     * @author: JiuDongDong
     * date: 2018/4/11 10:01
     */
    @Adopt
    @RequestMapping(value = "/p/bob/callback.htm")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.receiveNotify");
        String serMerId = bobPayConfigure.getMerId();// 商户号
        try {
            Map<String, String> params = new HashMap<>();
            Enumeration enums = request.getParameterNames();// 获取银联返回报文
            // 遍历打印报文信息
            while (enums.hasMoreElements()) {
                String key = (String) enums.nextElement();
                String value = request.getParameter(key);
                params.put(key, value);
            }
            logger.info("The notify info: " + JsonUtil.toJson(params));
            String merId = params.get(Constants.BOB_MER_ID);
            // 校验商户是否正确
            if (!serMerId.equals(merId)) {
                logger.error("The parameter merId do not match real merchant for BOBController.receiveNotify");
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);// 随机返回一个非200的错误码
                response2BOB(response, Constants.FAIL);
//                ResponseUtil.responsePrint(response, handleInfo, logger);
                return;
            }
            Integer code = bobManager.receiveNotify(params);// 0成功 1验签失败 2业务处理失败 3BOB业务处理失败
            // 根据业务处理返回值向北京银行响应
            if (code.intValue() == Constants.INTEGER_ZERO) {
                logger.info("Receive notify and persist to merchant ok with code = {}", code);
//                response.setStatus(HttpServletResponse.SC_OK);
                response2BOB(response, Constants.SUCCESS);
//                ResponseUtil.responsePrint(response, handleInfo, logger);
            }
            if (code.intValue() != Constants.INTEGER_ZERO) {
                logger.warn("Receive notify and serialize to merchant failed with code = {}", code);
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response2BOB(response, Constants.FAIL);
//                BOBResponseUtil.responsePrintHTML(response, handleInfo, logger);
            }
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.receiveNotify", e);
            try {
                response2BOB(response, Constants.FAIL);
            } catch (Exception e1) {
                logger.error("Some errors occurred in BOBController.receiveNotify when response2BOB", e1);
            }
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            BOBResponseUtil.responsePrintHTML(response, Constants.FAIL, logger);
        }

    }

    /**
     * Description: 退款交易
     * @author: JiuDongDong
     * date: 2018/4/21 16:28
     */
    @Adopt
    @RequestMapping(value = "/t/bob/refund.htm")
    public void refundOrder (HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.refundOrder");
        ResponseData responseData = new ResponseData();
        try {
            // 获取参数并封装
            Map<String, String> params = new HashMap<>();
            String channelType = request.getParameter(Constants.BOB_CHANNEL_TYPE);// 渠道类型
            String ip = request.getParameter(Constants.BOB_IP);// //持卡人IP地址
            String merId = request.getParameter(Constants.MERCHANT_NO);//商户号
            String orderNumber = request.getParameter(Constants.ORDER_NO);//商户订单号
            String orderAmount = request.getParameter(Constants.ORDER_AMOUNT);//退款金额 分为单位
            String refundSeq = request.getParameter(Constants.BOB_REFUND_SEQ);// 商户系统产生的交易流水号
            // 非空校验
            if (StringUtils.isBlank(orderAmount) || StringUtils.isBlank(orderNumber) ||
                    StringUtils.isBlank(channelType) || StringUtils.isBlank(ip) || StringUtils.isBlank(merId) ||
                    StringUtils.isBlank(refundSeq)) {
                logger.warn("The parameter orderAmount or orderNumber or channelType or ip or merchantNo or refundSeq is empty for BOBController.refundOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            params.put(Constants.ORDER_AMOUNT, FenYuanConvert.yuan2Fen(orderAmount).toString());
            params.put(Constants.BOB_ORDER_NUMBER, BOBOrderNoFormat.oriOrderNo2BOB19(orderNumber));
            params.put(Constants.BOB_CHANNEL_TYPE, channelType);
            params.put(Constants.BOB_IP, ip);
            params.put(Constants.BOB_REFUND_SEQ, refundSeq);
            logger.info("The params for refund: " + JsonUtil.toJson(params));
            // 业务处理
            bobManager.refundOrder(responseData, params);
            logger.info("It is OK in BOBController.refundOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.refundOrder", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 单笔交易查询
     * @author: JiuDongDong
//     * @param tradeTimeStr 交易时间：查询订单支付信息时，不传；查询退款请求时，为商户向BOB发起退款请求的时间，必传，格式yyyyMMddHHmmss
     * date: 2018/4/22 13:05
     */
    @Adopt
    @RequestMapping(value = "/t/bob/single-query.htm")
//    public void singleQuery (HttpServletRequest request, HttpServletResponse response, String tradeTimeStr) {
    public void singleQuery (HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.singleQuery");
        ResponseData responseData = new ResponseData();
        try {
            // 获取参数
            String transType = request.getParameter(Constants.BOB_TRANS_TYPE);//交易类型：根据要查询的原始交易类型分别填写如下值：01：消费,  35：全额退款  36：部分退款
//            String orderNumber = request.getParameter(Constants.ORDER_NO);//订单号(如果是订单支付，则为19位的不加E不加R的订单号，即t_pay_flow表的20位interaction_id去E去R；如果是充值，则为interaction_id不变)
            String orderNumber = request.getParameter(Constants.ORDER_NO);//转化过后的19位订单号或没有经过转化的充值订单号
//            String orderAmount = request.getParameter(Constants.ORDER_AMOUNT);//订单金额  TODO 文档里面没有，但示例里面有，如果真的需要，做非空校验
            // 非空校验
            if (StringUtils.isBlank(transType) || StringUtils.isBlank(orderNumber)) {
                logger.warn("The parameter transType or orderNumber is empty for BOBController.singleQuery");
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
//            // tradeTime转化为Date
//            Date tradeTime = null;
//            if (StringUtils.isNotBlank(tradeTimeStr)) {
//                tradeTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(tradeTimeStr);
//            }
            // 封装参数
            Map<String, String> params = new HashMap<>();
            params.put(Constants.BOB_TRANS_TYPE, transType);
//            params.put(Constants.BOB_ORDER_NUMBER, BOBOrderNoFormat.oriOrderNo2BOB19(orderNumber));
            orderNumber = orderNumber.endsWith(EARNEST) || orderNumber.endsWith(TAIL) ? orderNumber.substring(Constants.INTEGER_ZERO, orderNumber.length() - Constants.INTEGER_ONE) : orderNumber;
            params.put(Constants.BOB_ORDER_NUMBER, orderNumber);
//            params.put(Constants.ORDER_AMOUNT, orderAmount);//订单金额  TODO 文档里面没有，但示例里面有，如果真的需要，做非空校验
//            String refundSeq;//商户退款流水号 退款交易时必须输入（如果是退款，那么这个退款流水号=orderNo补足12位）
//            if (null != transType && !Constants.BOB_TRANS_TYPE01.equals(transType)) {
//                logger.warn("This is a refund query, the orderNo = {}", orderNumber);
//                refundSeq = BOBRefundSeqFormat.orderNo2BOBRefundString(orderNumber);
//                params.put(Constants.BOB_REFUND_SEQ, refundSeq);
//            }
            logger.info("The singleQuery params: " + JsonUtil.toJson(params));
            // 查询
//            bobManager.singleQuery(responseData, params, tradeTime);
            bobManager.singleQuery(responseData, params);
            logger.info("It is OK in BOBController.singleQuery");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.singleQuery", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 对账单查询
     * @author: JiuDongDong
     * date: 2018/4/22 13:05
     */
    @Adopt
    @RequestMapping(value = "/t/bob/account-query.htm")
    public void orderAccount (HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.orderAccount");
        ResponseData responseData = new ResponseData();
        try {
            // 获取参数
            String date = request.getParameter(Constants.BOB_DATE);//对账单查询的日期，格式：YYYYmmdd
            // 非空校验
            if (StringUtils.isBlank(date)) {
                logger.warn("The parameter date is empty for BOBController.orderAccount");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            // 封装参数
            Map<String, String> params = new HashMap<>();
            params.put(Constants.BOB_DATE, date);
            logger.info("The params for orderAccount: " + JsonUtil.toJson(params));
            // 查询
            bobManager.orderAccount(responseData, params);
            logger.info("It is OK in BOBController.orderAccount");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.orderAccount", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 接收对账单查询信息
     * @author: JiuDongDong
     * date: 2018/4/22 16:44
     */
    @Adopt
    @RequestMapping(value = "/p/bob/receive-account.htm")
    public void receiveAccount (HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in BOBController.receiveAccount");
        String serMerId = bobPayConfigure.getMerId();// 商户号
        try {
            Map<String, String> params = new HashMap<>();
            Enumeration enums = request.getParameterNames();// 获取银联返回报文
            //遍历打印报文信息
            while (enums.hasMoreElements()) {
                String key = (String) enums.nextElement();
                String value = request.getParameter(key);
                params.put(key, value);
            }
            logger.info("The receiveAccount info: " + JsonUtil.toJson(params));
            String merId = params.get(Constants.BOB_MER_ID);
//            String handleInfo;
            // 校验商户是否正确
            if (!serMerId.equals(merId)) {
                logger.error("The parameter merId do not match real merchant for BOBController.receiveAccount");
                response2BOB(response, Constants.FAIL);
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                handleInfo = Constants.FAIL;
//                BOBResponseUtil.responsePrintHTML(response, Constants.FAIL, logger);
            }
            Integer code = bobManager.receiveAccount(params);// 0成功 1验签失败 2业务处理失败 3BOB业务处理失败
            // 根据业务处理返回值向北京银行响应
            if (code.intValue() == Constants.INTEGER_ZERO) {
                logger.info("receiveAccount and serialize to merchant ok with code = {}", code);
                response2BOB(response, Constants.SUCCESS);
//                response.setStatus(HttpServletResponse.SC_OK);
//                handleInfo = Constants.SUCCESS;
//                BOBResponseUtil.responsePrintHTML(response, Constants.SUCCESS, logger);
            }
            if (code.intValue() != Constants.INTEGER_ZERO) {
                logger.warn("receiveAccount and serialize to merchant failed with code = {}", code);
                response2BOB(response, Constants.FAIL);
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                handleInfo = Constants.FAIL;
//                BOBResponseUtil.responsePrintHTML(response, Constants.FAIL, logger);
            }
        } catch (Exception e) {
            logger.error("Some errors occurred in BOBController.receiveAccount", e);
            try {
                response2BOB(response, Constants.FAIL);
            } catch (Exception e1) {
                logger.error("Some errors occurred in BOBController.receiveAccount when response2BOB", e1);
            }
        }

    }

    /**
     * Description: 接收回调或对账单信息后向BOB响应信息
     * @author: JiuDongDong
     * @param message SUCCESS or FAIL
     * date: 2018/5/24 21:08
     */
    private void response2BOB(HttpServletResponse response, String message) {
        PrintWriter writer;
        try {
            writer = response.getWriter();
            response.setContentType("text/html;charset=utf-8");
            writer.println(message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("response to BOB error");
        }

    }

}
