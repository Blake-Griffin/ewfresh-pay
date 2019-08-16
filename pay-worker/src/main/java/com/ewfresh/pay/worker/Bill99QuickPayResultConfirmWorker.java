package com.ewfresh.pay.worker;

import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.pay.manager.Bill99QuickManager;
import com.ewfresh.pay.model.BankAccount;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.redisService.SendMessageRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * description: Bill快捷支付是否成功确认
 * @author: JiuDongDong
 * date: 2018/9/30.
 */
@Component
public class Bill99QuickPayResultConfirmWorker {
    private Logger logger = LoggerFactory.getLogger(Bill99QuickPayResultConfirmWorker.class);
    @Autowired
    private SendMessageRedisService sendMessageRedisService;
    @Autowired
    private Bill99QuickManager bill99QuickManager;
    @Autowired
    private BankAccountService bankAccountService;
    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Autowired
    private HttpDeal httpDeal;

    /**
     * Description: 快捷支付是否成功确认
     * @author: JiuDongDong
     * date: 2018/9/30 10:27
     */
    @Scheduled(cron = "2/5 * * * * ?")
    @Transactional
    public void confirmPayResult() {
        /* 1. 从Redis查询有没有待确认支付结果的订单 */
        List<OrderInfoVo> orderInfoVoList = sendMessageRedisService.getTradeInfoFromRedis(Constants.QUICK_PAY_TRADE_PUR);
        /* 1.1 没有待确认支付结果的订单 */
        if (CollectionUtils.isEmpty(orderInfoVoList)) {
//            logger.info("There is no pay trade info in Redis now");
            return;
        }

        /* 1.2 有待确认支付结果的订单，处理数据 */
        for (OrderInfoVo orderInfoVo : orderInfoVoList) {
            String externalRefNumber = orderInfoVo.getExternalRefNumber();// 订单号
            String txnType = orderInfoVo.getTxnType();// 交易类型
            String payToken = orderInfoVo.getPayToken();//
            String customerId = orderInfoVo.getCustomerId();// 客户号
            String merchantId = orderInfoVo.getMerchantId();// 商户号
            String entryTime = orderInfoVo.getEntryTime();// 支付时间
            String amount = orderInfoVo.getAmount();// 订单支付金额
            String storablePan = orderInfoVo.getStorableCardNo();// 短卡号
            String interactiveStatus = orderInfoVo.getInteractiveStatus();// 消息状态（默认TR1）
            String isSelfPro = orderInfoVo.getIsSelfPro();// 是否自营商品，0否1是

            if (Constants.TXNTYPE_RFD.equals(txnType)) {
                logger.error("It is a RFD trade, externalRefNumber = " + externalRefNumber);
                continue;
            }
            // 从快钱快捷查询订单支付结果
            ResponseData responseData = new ResponseData();
            bill99QuickManager.queryOrder(responseData, txnType, externalRefNumber, null, isSelfPro);
            String code = responseData.getCode();
            String msg = responseData.getMsg();
            Object entity = responseData.getEntity();
            if (!ResponseStatus.OK.getValue().equals(code)) {
                logger.error("The response code of queryOrder in bill99QuickManager.queryOrder = " + code + ", msg = " + msg);
                logger.error("The externalRefNumber = " + externalRefNumber);
                continue;
            }
            HashMap respXml = (HashMap) entity;
            // 订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起
            // 交易类型为退货则: ’S’—退货申请成功 ‘F’－交易失败 ‘D’—已提交收单行
            String txnStatus = (String) respXml.get(Constants.BILL99_Q_TXN_STATUS);// 交易状态
            logger.info("TxnStatus = " + txnStatus + ", externalRefNumber = " + externalRefNumber);
            if (Constants.BILL99_Q_TXN_STATUS_PAY_F.equals(txnStatus)) {
                logger.error("The externalRefNumber = " + externalRefNumber + " handled failed, pay stopped, txnStatus = " +
                        txnStatus + ", respXml = " + JsonUtil.toJson(respXml));
                // 删除Redis中订单支付信息
                logger.info("delete trade info from redis, externalRefNumber = {}", externalRefNumber);
                sendMessageRedisService.deleteTradeInfoFromRedis(orderInfoVo);
                continue;
            }
            if (Constants.BILL99_Q_TXN_STATUS_PAY_P.equals(txnStatus)) {
                logger.error("The externalRefNumber = " + externalRefNumber + "is still handling, txnStatus = " +
                        txnStatus + ", respXml = " + JsonUtil.toJson(respXml));
                continue;
            }
            if (Constants.BILL99_Q_TXN_STATUS_PAY_S.equals(txnStatus)) {
                // 支付信息持久化
                BankAccount bill99BankInfoByPayToken = bankAccountService.getBill99BankInfoByPayToken(payToken);
                if (null == bill99BankInfoByPayToken) {
                    logger.error("Can not find this payToken from merchant database, customerId = " + customerId
                            + ", payToken = " + payToken + ", respXml = " + JsonUtil.toJson(respXml));
                    continue;
                }
                String bankLogo = null;// 银行id，如中国农业银行：ABC
                if (null != bill99BankInfoByPayToken) {
                    bankLogo = bill99BankInfoByPayToken.getBankLogo();
                }
                respXml.put("interactiveStatus", interactiveStatus);
                respXml.put(Constants.BILL99_Q_PAY_TOKEN, payToken);
                boolean persistOK = bill99QuickManager.persistPayResult2Disk(respXml, customerId, merchantId, entryTime,
                        externalRefNumber, amount, storablePan, bankLogo);// 这里面会修改订单状态
                if (!persistOK) {
                    // 持久化失败
                    logger.error("persist pay result to disk failed, externalRefNumber = " + externalRefNumber + ", respXml = " + JsonUtil.toJson(respXml));
                } else {
                    // 持久化成功
                    logger.info("delete trade info from redis, externalRefNumber = {}", externalRefNumber);
                }
                // 如果持久化成功，删除Redis中订单支付信息；如果持久化失败，bill99QuickManager.persistPayResult2Disk
                // 会调用ifSuccess方法，这个方法里会自动处理持久化失败的数据，这里同样将redis数据删除
                sendMessageRedisService.deleteTradeInfoFromRedis(orderInfoVo);
            }

        }

    }

}
