package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.BOCManager;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.BOCMerchantRecvOrderNotifyVo;
import com.ewfresh.pay.model.vo.BOCMerchantSendOrderVo;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * description:
 *      BOC业务的接入层
 * @author: JiuDongDong
 * date: 2018/4/8.
 */
@Controller
public class BOCController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BOCManager bocOrderManager;

    /**
     * Description: 商户向BOC发送订单支付请求
     * @author: JiuDongDong
     * @param bocRecvOrder 封装订单支付请求数据
     * date: 2018/4/8 14:58
     */
    @Adopt
    @RequestMapping(value = "/t/boc-merchant-send-order.htm")
    public void sendOrder(HttpServletResponse response, BOCMerchantSendOrderVo bocRecvOrder) {
        logger.info("It is now in BOCOrderController.sendOrder, the input parameter is [bocRecvOrder = {}]", JsonUtil.toJson(bocRecvOrder));
        ResponseData responseData = new ResponseData();
        try {
            if (bocRecvOrder.getOrderNo() == null || bocRecvOrder.getOrderAmount() == null) {
                logger.warn("The parameter orderNo or payment is empty for BOCOrderController.sendOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bocOrderManager.sendOrder(responseData, bocRecvOrder);
            logger.info("It is OK in BOCOrderController.sendOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.sendOrder", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 商户处理BOC返回的订单支付信息
     * @author: JiuDongDong
     * @param merchantRecvOrderNotifyVo  封装BOC返回的订单支付信息
     * date: 2018/4/11 10:56
     */
    @Adopt
    @RequestMapping(value = "/p/boc/callback.htm")
    public void receiveNotify(HttpServletResponse response, BOCMerchantRecvOrderNotifyVo merchantRecvOrderNotifyVo) {
        logger.info("It is now in BOCOrderController.receiveNotify, the input parameter is [merchantRecvOrderNotifyVo = {}]", JsonUtil.toJson(merchantRecvOrderNotifyVo));
        try {
            // TODO 测试成功后，放开
//            if (StringUtils.isBlank(merchantRecvOrderNotifyVo.getMerchantNo()) ||  merchantRecvOrderNotifyVo.getOrderNo() == null
//                    || merchantRecvOrderNotifyVo.getOrderSeq() == null || StringUtils.isBlank(merchantRecvOrderNotifyVo.getCardTyp())
//                    || merchantRecvOrderNotifyVo.getPayTime() == null || StringUtils.isBlank(merchantRecvOrderNotifyVo.getOrderStatus())
//                    || StringUtils.isBlank(merchantRecvOrderNotifyVo.getPayAmount()) || StringUtils.isBlank(merchantRecvOrderNotifyVo.getOrderIp())
//                    || StringUtils.isBlank(merchantRecvOrderNotifyVo.getOrderRefer()) || StringUtils.isBlank(merchantRecvOrderNotifyVo.getBankTranSeq())
//                    || StringUtils.isBlank(merchantRecvOrderNotifyVo.getReturnActFlag()) || StringUtils.isBlank(merchantRecvOrderNotifyVo.getPhoneNum())
//                    || StringUtils.isBlank(merchantRecvOrderNotifyVo.getSignData())) {
//                logger.warn("The notify info of BOC returned for merchant order is empty in BOCOrderController.receiveNotify");
//                // TODO BOC发送的通知信息有empty，商户主动发起查询通知
//                //
//            }

//            String signData = "MIID6QYJKoZIhvcNAQcCoIID2jCCA9YCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCAp0w\n" +
//                    "ggKZMIICAqADAgECAhAzuCO5PUrAcrb2QUmlAWNnMA0GCSqGSIb3DQEBBQUAMFoxCzAJBgNVBAYT\n" +
//                    "AkNOMRYwFAYDVQQKEw1CQU5LIE9GIENISU5BMRAwDgYDVQQIEwdCRUlKSU5HMRAwDgYDVQQHEwdC\n" +
//                    "RUlKSU5HMQ8wDQYDVQQDEwZCT0MgQ0EwHhcNMDkxMjIzMTM1OTA3WhcNMTkxMTAxMTM1OTA3WjA+\n" +
//                    "MQswCQYDVQQGEwJDTjEWMBQGA1UEChMNQkFOSyBPRiBDSElOQTEXMBUGA1UEAx4ObdhbnX9RAFQA\n" +
//                    "RQBTAFQwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALxYi14gAH4cGdIA/B8XjDaNMH8/SqmB\n" +
//                    "g44OKgbtsymjJXGM3eK39YdI89zCIEDSsXVxFFKOmoLCrUEpv3gVcdShdnkSHCm46j5poZtguecl\n" +
//                    "OSRGRFYuX859WuIh07xQOdXNRzouIyrPcKdNz7/l7+mulw8qIOilkfRJO0yeKd9ZAgMBAAGjfDB6\n" +
//                    "MB8GA1UdIwQYMBaAFNEBq4gMK8Rc0rD2ptoD0ddgehqcMCsGA1UdHwQkMCIwIKAeoByGGmh0dHA6\n" +
//                    "Ly8yMi42LjU5LjE2L2NybDEuY3JsMAsGA1UdDwQEAwIGwDAdBgNVHQ4EFgQU9bhkkIXpHUULULFs\n" +
//                    "mEWrZl5QxwQwDQYJKoZIhvcNAQEFBQADgYEAkr0H6oSPQHvRBNaADCHoAse6Ia/Xl+orUntYpZT9\n" +
//                    "KXGEkqGj8hdH5/WHKEw3FbRGNT989F+cBSt0zbEwmFlAyRaaNyB3PUvwFaUN0pGmS+YLx4FYS7Cx\n" +
//                    "FO4/kwSpwjWHzgSNWe+cwLfEZllPMKYvghzf22qXgT4y4oOMPd6kYcIxggEUMIIBEAIBATBuMFox\n" +
//                    "CzAJBgNVBAYTAkNOMRYwFAYDVQQKEw1CQU5LIE9GIENISU5BMRAwDgYDVQQIEwdCRUlKSU5HMRAw\n" +
//                    "DgYDVQQHEwdCRUlKSU5HMQ8wDQYDVQQDEwZCT0MgQ0ECEDO4I7k9SsBytvZBSaUBY2cwCQYFKw4D\n" +
//                    "AhoFADANBgkqhkiG9w0BAQEFAASBgEVSHazo/erS25QB6jxBPuc96oWyLfK9kKpqIuydaUtteBAg\n" +
//                    "xHsMsfQyrJqw6PR0OImWl/So7IlqVIeUKEz6SlD1vAdGBpzVrtCH8BWNn9ebcbLG4ae+kCnTwJtP\n" +
//                    "wBLDnaVcCQPTdiWNoyNwpUeiOuKhwuHyL0DFRmkNbXK01DRJ";
//
//            merchantRecvOrderNotifyVo.setSignData(signData);


            // 商户成功接收通知，校验信息，处理逻辑
            bocOrderManager.receiveNotify(merchantRecvOrderNotifyVo);
            logger.info("It is OK in BOCOrderController.receiveNotify");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.receiveNotify", e);
        }

    }

    /**
     * Description: 商户查询订单信息(原始版)
     * @author: JiuDongDong
     * @param orderNos 待查询状态的订单
     * date: 2018/4/11 16:40
     */
    @Adopt
    @RequestMapping(value = "/t/boc/query-order.htm")
    public void queryOrder (HttpServletResponse response, String orderNos) {
        logger.info("It is now in BOCOrderController.queryOrder, the input parameter are [orderNos = {}]", orderNos);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(orderNos)) {
                logger.warn("The parameter merchantNo or orderNos is empty for BOCOrderController.queryOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bocOrderManager.queryOrder(responseData, orderNos);
            logger.info("It is OK in BOCOrderController.queryOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.queryOrder", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 商户发送查询订单请求(支持卡户信息判断)
     * @author: JiuDongDong
     * @param merchantNo 商户号
     * @param orderNos 待查询状态的订单
     * date: 2018/4/11 20:05
     */
    @Adopt
    @RequestMapping(value = "/t/boc/common-query-order.htm")
    public void commonQueryOrder (HttpServletResponse response, String merchantNo, String orderNos) {
        logger.info("It is now in BOCOrderController.commonQueryOrder, the input parameter are [merchantNo = {}, orderNos = {}]", merchantNo, orderNos);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(merchantNo) || StringUtils.isBlank(orderNos)) {
                logger.warn("The parameter merchantNo or orderNos is empty for BOCOrderController.commonQueryOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bocOrderManager.commonQueryOrder(responseData, orderNos);
            logger.info("It is OK in BOCOrderController.commonQueryOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.commonQueryOrder", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 商户发送B2C退款指令
     * @author: JiuDongDong
//     * @param mRefundSeq 商户退款交易流水号
//     * @param refundAmount 退款金额
//     * @param orderNo 商户订单号: 与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号
     * @param refundParam 封装退款信息
     * date: 2018/4/11 21:14
     */
    @Adopt
    @RequestMapping(value = "/t/boc/refund-order.htm")
//    public void refundOrder (HttpServletResponse response, String mRefundSeq, String refundAmount, String orderNo) {
    public void refundOrder (HttpServletResponse response, RefundParam refundParam) {
//        logger.info("It is now in BOCOrderController.refundOrder, the input parameter are [mRefundSeq = {}, refundAmount = {}, orderNo = {}]", mRefundSeq, refundAmount, orderNo);
        logger.info("It is now in BOCOrderController.refundOrder, the input parameter are [refundParam = {}]", JsonUtil.toJson(refundParam));
        ResponseData responseData = new ResponseData();
        try {
            String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
            String refundAmount = refundParam.getRefundAmount();// 退款金额
            String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
            String orderNo = refundParam.getOrderNo();// 父订单号
            String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
            String channelType = refundParam.getChannelType();// 类型(07：互联网，08：移动)
            if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                    || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(channelType) || StringUtils.isBlank(orderNo)) {
                logger.warn("The params tradeNo or refundAmount or outRequestNo or outTradeNo or channelType or orderNo is empty");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
//            bocOrderManager.refundOrder(responseData, mRefundSeq, refundAmount, orderNo);
            bocOrderManager.refundOrder(responseData, refundParam);
            logger.info("It is OK in BOCOrderController.refundOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.refundOrder", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 商户取票，下载对账文件
     * @author: JiuDongDong
     * @param fileDate 文件日期
     * date: 2018/4/12 11:44
     */
    @Adopt
    @RequestMapping(value = "/t/boc/get-ticket-download-file.htm")
    public void getTicketDownloadFile (HttpServletResponse response, String fileDate, String extend) {
        logger.info("It is now in BOCOrderController.getTicketDownloadFile, the input parameter are [fileDate = {}, extend = {}]", fileDate, extend);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(fileDate) || StringUtils.isBlank(fileDate)) {
                logger.warn("The parameter fileDate or extend is empty for BOCOrderController.getTicketDownloadFile");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bocOrderManager.getTicketDownloadFile(responseData, fileDate, extend);
            logger.info("It is OK in BOCOrderController.getTicketDownloadFile");
        } catch (Exception e) {
            logger.error("Some errors occurred in BOCOrderController.getTicketDownloadFile", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

}
