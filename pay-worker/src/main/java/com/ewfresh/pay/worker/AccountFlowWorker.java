package com.ewfresh.pay.worker;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.AccFlowManager;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.service.BillRepayFlowService;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/4/18
 */
@Component
public class AccountFlowWorker {

    private Logger logger = LoggerFactory.getLogger(AccountFlowWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private BillRepayFlowService billRepayFlowService;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    @Autowired
    private AccFlowManager accFlowManager;

    /**
     * Description: 离线生成资金账户流水的方法
     *
     * @author DuanXiangming
     * Date    2018/4/18
     */
    @Scheduled(cron = "1-59 * * * * ?")
    public void autoAddAccFlow() {
        //获取redis队列中的支付流水对象
        PayFlow payFlowToAcc = accountFlowRedisService.getPayFlowToAcc();
        if (payFlowToAcc == null) {
            return;
        }
        logger.info(" this time for payflow to accflow [ payflow = {} ]", ItvJsonUtil.toJson(payFlowToAcc));
        Short tradeType = payFlowToAcc.getTradeType();
        String uid = getUidByTradeType(tradeType, payFlowToAcc);
        boolean lock = this.balanceAndBarLock.getBalanceAndBarLock(uid);
        if (lock) {
            try {
                AccountFlowVo accFlowByPayFlow = getAccFlowByPayFlow(payFlowToAcc);
                accountFlowDescUtil.cassExplainByAcc(accFlowByPayFlow);
                if (tradeType.shortValue() == Constants.TRADE_TYPE_15) {
                    //白条还款
                    accFlowManager.addAccFlow(accFlowByPayFlow,payFlowToAcc);
                } else if ((tradeType.shortValue() == Constants.TRADE_TYPE_2 || tradeType.shortValue() == Constants.TRADE_TYPE_9 ||
                    tradeType.shortValue() == Constants.TRADE_TYPE_17) && payFlowToAcc.getChannelCode().equalsIgnoreCase(Constants.UID_WHITE)) {
                    //白条退款
                    accFlowManager.dealWhiteReturnFlow(payFlowToAcc, accFlowByPayFlow);
                } else {
                    accountFlowService.addAccountFlow(accFlowByPayFlow);
                }
            } catch (Exception e) {
                logger.error("add accountFlow is error = {}", e);
                this.balanceAndBarLock.releaseLock(uid);
            }
        }
        this.balanceAndBarLock.releaseLock(uid);
        logger.info(" add accountFlow success ");
    }
    private AccountFlowVo getAccFlowByPayFlow(PayFlow payFlow) {
        Short tradeType = payFlow.getTradeType();
        logger.info("tradeType si========= tradeTyp{}", tradeType);
        String uid = getUidByTradeType(tradeType, payFlow);
        logger.info(" the uid  ----------->[uid = {}]", uid);
        AccountFlowVo accountFlowVo = balanceManager.checkBalanceIsExist(uid);
        logger.info(" the old accountFlow for this user [accountFlowVo = {}] ", ItvJsonUtil.toJson(accountFlowVo));
        logger.info(" the payFlow for this user [payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        if (accountFlowVo == null) {
            accountFlowVo = new AccountFlowVo();
            accountFlowVo.setBalance(new BigDecimal(Constants.NULL_BALANCE));
            accountFlowVo.setFreezeAmount(new BigDecimal(Constants.NULL_BALANCE));
        }
        BigDecimal balance = accountFlowVo.getBalance();
        BigDecimal newBalance = null;
        Integer shopId = payFlow.getShopId();
        String channelCode = payFlow.getChannelCode();
        if (tradeType == Constants.TRADE_TYPE_8) {
            //配货扣除补款
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.RECEIVERNAME);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_9);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
            if (channelCode.equals(Constants.UID_BALANCE)){
                if (freezeAmount.compareTo(payerPayAmount) == -1){
                    logger.info("freezeAmount err [ freezeAmount = {},payerPayAmount = {}]" , freezeAmount,payerPayAmount);
                    return null;
                }
                newBalance = balance.subtract(payerPayAmount);
                freezeAmount = freezeAmount.subtract(payerPayAmount);
            }else {
                newBalance = balance;
            }
            accountFlowVo.setFreezeAmount(freezeAmount);
            logger.info(" abatements balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_9) {
            //配货退款
            payFlow.setReceiverName(Constants.BALANCE);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(payFlow.getChannelName());
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_10);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            if(channelCode.equals(Constants.UID_BALANCE)){
                newBalance = balance.add(payerPayAmount);
            }else {
                newBalance = balance;
            }
            logger.info(" return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        //白条还款
        if (tradeType == Constants.TRADE_TYPE_15) {
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setPayFlowId(payFlow.getPayFlowId()); //支付流水id
            logger.info("uid =====", uid);
            accountFlowVo.setUserId(Long.valueOf(uid)); //用户id
            accountFlowVo.setTargetAcc(Constants.SUNKFA);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_19);//业务类型
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);//资金流入流出
            accountFlowVo.setOperator(payFlow.getPayerId());//操作人
            accountFlowVo.setSrcAcc(payFlow.getChannelName());//原账户
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);//目标账户类型
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);//原账户类型

            accountFlowVo.setUname(payFlow.getUname());
            Short accType = getAccType(payFlow);
            accountFlowVo.setAccType(accType);

            accountFlowVo.setAmount(payerPayAmount);
            accountFlowVo.setBusiNo(payFlow.getOrderId() + "");

            //判断是否为余额还款
            if (channelCode.equals(Constants.UID_BALANCE)) {

                //如果此次为余额还款 余额减去此次还款金额
                accountFlowVo.setBalance(balance.subtract(payFlow.getPayerPayAmount()));
                //冻结金额减去还款金额
                accountFlowVo.setFreezeAmount(accountFlowVo.getFreezeAmount().subtract(payFlow.getPayerPayAmount()));

            }
            accountFlowVo.setIsBalance(Constants.IS_REFUND_YES);

            return accountFlowVo;
        }
        if (tradeType == Constants.TRADE_TYPE_4) {
            //线上充值的业务
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setPayFlowId(payFlow.getPayFlowId());
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.SUNKFA);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_1);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(payFlow.getChannelName());
            newBalance = balance.add(payerPayAmount);
            accountFlowVo.setIsBalance(Constants.IS_REFUND_NO);

            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
        }
        /*订单支付的记录,*/
        if (tradeType == Constants.TRADE_TYPE_1) {
            //订单扣款的业务
            if (channelCode.equals(Constants.UID_BALANCE)) {
                newBalance = balance.subtract(payerPayAmount);
                BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
                if (freezeAmount.compareTo(payerPayAmount) != -1) {
                    logger.info("the freezeAmount is lt payerPaymount [ accountFlowVo = {}]", ItvJsonUtil.toJson(accountFlowVo));
                    freezeAmount = freezeAmount.subtract(payerPayAmount);
                }
                accountFlowVo.setFreezeAmount(freezeAmount);
            } else {
                newBalance = accountFlowVo.getBalance();
            }
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.SUNKFA);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_3);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(payFlow.getChannelName());
            accountFlowVo.setIsBalance(Constants.IS_REFUND_NO);
            if (shopId.intValue() != 0) {
                //非自营商品出售
                accountFlowVo.setTargetAccType(Constants.SHORT_TWO);
                accountFlowVo.setSrcAccType(Constants.SHORT_TWO);
            } else {
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            }
        }
        if (tradeType == Constants.TRADE_TYPE_2 || tradeType == Constants.TRADE_TYPE_17) {
            //退款的业务
            accountFlowVo.setUserId(Long.valueOf(payFlow.getReceiverUserId()));
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_14);
            String busiNo = "";
            if (channelCode.equals(Constants.UID_BALANCE)) {//余额
                busiNo = payFlow.getChannelType();
                newBalance = balance.add(payerPayAmount);
            } else {
                busiNo = payFlow.getOrderId() + "";
                newBalance = balance;
            }
            accountFlowVo.setBusiNo(busiNo);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setTargetAcc(payFlow.getChannelName());
            if (shopId.intValue() != 0) {
                //非自营商品出售
                accountFlowVo.setSrcAccType(Constants.SHORT_TWO);
                accountFlowVo.setTargetAccType(Constants.SHORT_TWO);
            } else {
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            }
            accountFlowVo.setIsBalance(Constants.IS_REFUND_YES);
        }
        if (tradeType == Constants.TRADE_TYPE_17) {
            //退款的业务
            accountFlowVo.setUserId(Long.valueOf(payFlow.getReceiverUserId()));
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_22);
            String busiNo = "";
            if (channelCode.equals(Constants.UID_BALANCE)) {//余额
                busiNo = payFlow.getChannelType();
                newBalance = balance.add(payerPayAmount);
            } else {
                busiNo = payFlow.getOrderId() + "";
                newBalance = balance;
            }
            accountFlowVo.setBusiNo(busiNo);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setTargetAcc(payFlow.getChannelName());
            if (shopId.intValue() != 0) {
                //非自营商品出售
                accountFlowVo.setSrcAccType(Constants.SHORT_TWO);
                accountFlowVo.setTargetAccType(Constants.SHORT_TWO);
            } else {
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            }
            accountFlowVo.setIsBalance(Constants.IS_REFUND_YES);
        }
        Short accType = getAccType(payFlow);
        accountFlowVo.setPayFlowId(payFlow.getPayFlowId());
        accountFlowVo.setAccType(accType);
        accountFlowVo.setBalance(newBalance);
        accountFlowVo.setAmount(payerPayAmount);
        accountFlowVo.setBusiNo(payFlow.getOrderId() + "");
        return accountFlowVo;
    }

    public String getUidByTradeType(Short tradeType, PayFlow payFlow) {
        String uid = null;
        switch (tradeType) {
            case (short) 1:
                uid = payFlow.getPayerId();
                break;
            case (short) 2:
                uid = payFlow.getReceiverUserId();
                break;
            case (short) 18:
                uid = payFlow.getReceiverUserId();
                break;
            case (short) 4:
                uid = payFlow.getPayerId();
                break;
            case (short) 15:
                uid = payFlow.getPayerId();
                break;
            case (short) 8:
                uid = payFlow.getPayerId();
                break;
            case (short) 9:
                uid = payFlow.getReceiverUserId();
                break;
            case (short) 17:
                uid = payFlow.getReceiverUserId();
                break;
        }
        return uid;
    }

    public Short getAccType(PayFlow payFlow) {

        Short accType = 3;
        String channelCode = payFlow.getChannelCode();
        switch (channelCode) {
            case "1000":
                accType = 4;
                break;
            case "1001":
                accType = 1;
                break;
            case "1002":
                accType = 2;
                break;
            case "1065":
                accType = 6;
                break;
            case "1066":
                accType = 7;
                break;
            case "1067":
                accType = 7;
                break;
            case "1068":
                accType = 7;
                break;
            case "1069":
                accType = 7;
                break;
            case "1070":
                accType = 7;
                break;
        }

        return accType;
    }


}
