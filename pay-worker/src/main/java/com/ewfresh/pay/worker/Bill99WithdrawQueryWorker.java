package com.ewfresh.pay.worker;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.manager.impl.HATWithdrawManagerImpl;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.FormatBigDecimal;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * description: 提现明细查询
 *
 * @author: ZhaoQun
 * date: 2018/8/10.
 */
@Component
public class Bill99WithdrawQueryWorker {
    @Autowired
    private HATWithdrawManagerImpl hatWithdrawManagerImpl;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private WithdrawtoService withdrawtoService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private BalanceManagerImpl balanceManagerImpl;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String KUAIQIAN = "快钱";
    private static final String STATUS = "status";
    private static final String AMOUNT = "amount";
    private static final String CUSTOMERFEE = "customerFee";
    private static final String MERCHANTFEE = "merchantFee";
    private static final String MEMBERBANKACCTID = "memberBankAcctId";
    private static final String BANKACCTID = "bankAcctId";
    private static final String TRADEDESCRIPTION = "tradeDescription";
    private static final BigDecimal POINT = new BigDecimal("100");

    /**
     * Description: HAT 提现明细查询， 提现处理中查询其提现结果
     *
     * @author: ZhaoQun
     * date: 2018/10/18 13:14
     */
    @Scheduled(cron = "3/5 * * * * ?")
    @Transactional
    public void withdrawQuery () throws Exception {
        //获取redis 中的提现交易
        Set<String> withdrawIdMap = bill99OrderRedisService.getHATWithdrawIdMap(Constants.WITHDRAW_TADENO);
        if (CollectionUtils.isEmpty(withdrawIdMap)){;
            return;
        }
        logger.info("This is withdrawQueryWorker List withdrawIdMap = {}" , JsonUtil.toJson(withdrawIdMap));
        for (String id : withdrawIdMap) {
            //获取 Bill99WithdrawAccountVo 信息
            Bill99WithdrawAccountVo vo = bill99OrderRedisService.getHATWithdrawVoMap(Constants.WITHDRAW_TADENO,id);
            String outTradeNo = vo.getOutTradeNo();//外部交易号
            String content = hatWithdrawManagerImpl.withdrawQueryRequest(outTradeNo);//发起明细查询请求
            Long withdrawtoId = Long.valueOf(id);// 提现ID
            String dealId = vo.getDealId();//支付渠道流水号
            if (StringUtils.isNotBlank(content)) {
                Map<String, String> info = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
                });
                String rspCode = info.get(Constants.RSPCODE);
                String rspMsg = info.get(Constants.RSPMSG);
                if (rspCode.equals(Constants.BILL99_RSPCODE_0000)) {
                    String status = info.get(STATUS);//订单状态 1-成功，2-失败，3-处理中
                    String amount = info.get(AMOUNT);//提现金额
                    String customerFee = info.get(CUSTOMERFEE);//会员自付手续费
//                    String merchantFee = info.get(MERCHANTFEE);//商户代付手续费
//                    String memberBankAcctId = info.get(MEMBERBANKACCTID);//银行卡主键 Id
                    String bankAcctId = info.get(BANKACCTID);//银行账户
                    String tradeDescription = ItvJsonUtil.toJson(info.get(TRADEDESCRIPTION));//订单交易描述
                    Withdrawto withdrawto = new Withdrawto();
                    switch (status) {
                        case "1":
                            logger.info("bill99 trade accountWithdraw is success, tradeDescription is " + tradeDescription);
                            //修改提现状态
                            withdrawto.setId(withdrawtoId);
                            withdrawto.setApprStatus(Constants.APPR_STATUS_6);//修改状态为 提现成功
                            withdrawto.setBeforeStatus(Constants.APPR_STATUS_4);//前置状态
                            withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                            //交易流水
                            String uid = vo.getuId();
                            BigDecimal oriReceiverFee = new BigDecimal(amount).divide(POINT);
                            BigDecimal receiverFee = FormatBigDecimal.formatBigDecimal(oriReceiverFee);
                            BigDecimal payerFee = new BigDecimal(customerFee).divide(POINT);//会员自付手续费
                            payerFee = FormatBigDecimal.formatBigDecimal(payerFee);
                            BigDecimal oriPayerFee = receiverFee.add(payerFee);//付款方支付金额 = 收款金额 + 手续费
                            BigDecimal payerPayAmount = FormatBigDecimal.formatBigDecimal(oriPayerFee);
                            String shopName = accountFlowRedisService.getShopInfoFromRedis(Constants.SHOP_ADDSHOP_REDIS, uid);
                            PayFlow payFlow = new PayFlow();
                            payFlow.setChannelFlowId(dealId);//支付渠道流水号
                            payFlow.setPayerId(uid);//付款人ID
                            payFlow.setPayerPayAmount(payerPayAmount);//付款方支付金额
                            payFlow.setPayerFee(payerFee);//付款方手续费(提现方自付手续费)
                            payFlow.setPayerType(Constants.SHORT_TWO);//付款账号类型(1个人,2店铺)
                            payFlow.setReceiverFee(receiverFee);//收款金额
                            payFlow.setReceiverUserId(bankAcctId);//收款人ID
                            payFlow.setOrderId(withdrawtoId);
                            payFlow.setOrderAmount(receiverFee);//订单金额
                            payFlow.setChannelCode(String.valueOf(Constants.CHANNEL_CODE_KUAIQIAN));//支付渠道编号
                            payFlow.setChannelName(KUAIQIAN);//支付渠道名称
                            payFlow.setTradeType(Constants.TRADE_TYPE_12);//交易类型
                            payFlow.setStatus(Constants.STATUS_0);//状态
                            payFlow.setInteractionId(outTradeNo);//第三方交互订单号
                            payFlow.setUname(shopName);
                            payFlow.setShopId(Integer.valueOf(uid));//shop_id
                            //资金账户流水
                            ResponseData responseData = new ResponseData();
                            AccountFlowVo accountFlow = balanceManagerImpl.getAccFlowByPayFlow(payFlow, responseData);
                            int num = 0;
                            if (accountFlow != null) {
                                num = payFlowService.addPayFlowAndAccFlow(withdrawto,payFlow,accountFlow);
                            }
                            //删除redis信息
                            if (num > 0) {
                                bill99OrderRedisService.delHATWithdrawVoItem(Constants.WITHDRAW_TADENO,id);
                                //提现成功 id 存redis 发短信
                                bill99OrderRedisService.setWithdrawIdToredis(vo.getWithdrawId(), Constants.STORE_WITHDRAWID_SENDMSG);
                            }
                            break;
                        case "2":
                            logger.error("HAT trade accountWithdraw is failed, withdraw ID = {},tradeDescription = {}",withdrawtoId,  tradeDescription);
                            //提现失败 修改提现状态 至 四审
                            withdrawto.setId(withdrawtoId);
                            withdrawto.setApprStatus(Constants.APPR_STATUS_3);
                            withdrawto.setBeforeStatus(Constants.APPR_STATUS_2);//前置状态
                            withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                            withdrawto.setRemark(KUAIQIAN);
                            withdrawtoService.updateWithdrawto(withdrawto);
                            //删除redis信息
                            bill99OrderRedisService.delHATWithdrawVoItem(Constants.WITHDRAW_TADENO,id);
                            break;
                        case "3":
                            logger.info("bill99 trade accountWithdraw is dealing, tradeDescription is " + tradeDescription);
                            break;
                        default:
                            logger.error(" HAT accountWithdraw status = {}, withdraw ID = {}, tradeDescription = {}",withdrawtoId, status, tradeDescription);
                    }
                } else if (rspCode.equals(Constants.BILL99_RSPCODE_5002)) {
                    logger.error("The outTradeNum is not EXIST (交易号不存在)  HAT account withdraw failed, withdrawtoId = {}, rspCode={}, rspMsg={}", withdrawtoId, rspCode,rspMsg);
                } else {
                    logger.error("bill99 HAT accountWithdraw is failed. withdrawtoId = {}, rspCode = {},rspMsg={} " , withdrawtoId, rspCode,rspMsg);
                    logger.error("HAT account withdraw failed, content={}", content);
                }
            }
        }
    }

}
