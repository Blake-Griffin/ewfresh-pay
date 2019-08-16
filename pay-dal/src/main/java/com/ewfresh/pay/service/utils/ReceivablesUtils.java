package com.ewfresh.pay.service.utils;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.ReceivablesDao;
import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.service.ReceivablesService;
import com.ewfresh.pay.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/8/6 0006
 */
@Component
public class ReceivablesUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReceivablesUtils.class);
    @Autowired
    private ReceivablesService receivablesService;
    @Autowired
    private ReceivablesDao receivablesDao;

    public Receivables  getReceivablesByAcc(AccountFlowVo accountFlow){

        Long userId = accountFlow.getUserId();

        logger.info(" the uid ----------->[uid = {}]", userId);
        Receivables receivables = checkReceivablesIsExist(userId.toString());
        logger.info(" the new accountFlow for this user [receivables = {},accountFlow = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(accountFlow));
        BigDecimal payerPayAmount = accountFlow.getAmount();
        Short busiType = accountFlow.getBusiType();
        if (busiType == Constants.BUSI_TYPE_7 || busiType == Constants.BUSI_TYPE_8 || busiType == Constants.BUSI_TYPE_21){
            return null;
        }
        Short accType = accountFlow.getAccType();
        if (receivables == null) {
            logger.info("new account for this user [userid = {}]" , userId);
            receivables = new Receivables();
            receivables.setFinancialBalance(new BigDecimal(Constants.NULL_BALANCE));
            receivables.setDueAmout(new BigDecimal(Constants.NULL_BALANCE));
            receivables.setBusiType(busiType);
        }
        BigDecimal financialBalance = receivables.getFinancialBalance();
        BigDecimal dueAmount = receivables.getDueAmout();
        if (busiType == Constants.BUSI_TYPE_22) {
            //订单退货退款
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_22);
            if (accType.shortValue() != Constants.ACC_TYPE_4.shortValue() && accType.shortValue() != Constants.ACC_TYPE_6.shortValue()){
                financialBalance = financialBalance.subtract(payerPayAmount);
            }
            if (accType.shortValue() == Constants.ACC_TYPE_6.shortValue()){
                financialBalance = financialBalance.add(payerPayAmount);
            }
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.BUSI_TYPE_19 || busiType == Constants.BUSI_TYPE_20) {
            //白条还款
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_20);
            if(accType != Constants.ACC_TYPE_4){
                //归还的时候只能增加本金的财务余额
                BigDecimal billInsert = accountFlow.getBillInsert();            //本次归还的利息
                if (billInsert == null){
                    logger.info("the billInsert is null");
                }else {

                    payerPayAmount = payerPayAmount.subtract(billInsert);     //本次归还的本金
                }
                financialBalance = financialBalance.add(payerPayAmount);
            }
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_14) {
            //订单退款
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            dueAmount = dueAmount.subtract(payerPayAmount);
            receivables.setBusiType(busiType);
            if (accType.shortValue() != Constants.ACC_TYPE_4.shortValue() && accType.shortValue() != Constants.ACC_TYPE_6.shortValue()){
                financialBalance = financialBalance.subtract(payerPayAmount);
            }
            if (accType.shortValue() == Constants.ACC_TYPE_6.shortValue()){
                financialBalance = financialBalance.add(payerPayAmount);
            }
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_1) {
            //线上充值
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            financialBalance = financialBalance.add(payerPayAmount);
            receivables.setBusiType(busiType);
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_15) {
            //充值错误提现
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            financialBalance = financialBalance.subtract(payerPayAmount);
            receivables.setBusiType(busiType);
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_12) {
            //索赔退款到余额
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            financialBalance = financialBalance.add(payerPayAmount);
            receivables.setBusiType(busiType);
            logger.info(" claim return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_13) {
            //无货到索赔退款到余额
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            financialBalance = financialBalance.add(payerPayAmount);
            receivables.setBusiType(busiType);
            logger.info(" not recieve claim return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_10) {
            //配货退款
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            dueAmount = dueAmount.subtract(payerPayAmount);
            receivables.setBusiType(busiType);
            logger.info(" return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_9) {
            //配货补款
            receivables.setTargetAcc(Constants.RECEIVERNAME);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setSrcAcc(Constants.BALANCE);
            receivables.setBusiType(busiType);
            if (accType != Constants.ACC_TYPE_4){
                financialBalance = financialBalance.add(payerPayAmount);
            }
            dueAmount = dueAmount.add(payerPayAmount);
            logger.info(" abatements balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(dueAmount));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_11) {
            //线下充值的业务
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(null);
            receivables.setBusiType(busiType);
            financialBalance = financialBalance.add(payerPayAmount);
            logger.info(" recharge balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(financialBalance));
        }
        if (busiType == Constants.BUSI_TYPE_18) {
            //红包活动的业务
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setSrcAcc(null);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_19);
            financialBalance = financialBalance.add(payerPayAmount);
            logger.info(" recharge balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(financialBalance));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_2) {
            //提现的业务
            receivables.setBusiType(Constants.BUSI_TYPE_2);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setSrcAcc(Constants.BALANCE);
            receivables.setBusiType(busiType);
            receivables.setTargetAcc(null);
            financialBalance = financialBalance.subtract(payerPayAmount);
            logger.info(" withdrawto balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), ItvJsonUtil.toJson(financialBalance));
        }
        if (busiType == Constants.RECEIVABLES_BUSI_TYPE_3) {
            //订单扣款的业务
            receivables.setTargetAcc(Constants.SUNKFA);
            receivables.setBusiType(busiType);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setSrcAcc(accountFlow.getSrcAcc());
            dueAmount = dueAmount.add(payerPayAmount);
            logger.info("pay accType for this time [accType = {}",accType);
            if (accType.shortValue() != Constants.SHORT_FOUR.shortValue() && accType.shortValue() != Constants.ACC_TYPE_6){
                logger.info("pay accType for this time [accType = {}",accType);
                //涉及到三方支付,所以需要在财务余额中加上支付的金额
                financialBalance = financialBalance.add(payerPayAmount);
            }
        }
        receivables.setUserId(userId);
        receivables.setFinancialBalance(financialBalance);
        receivables.setDueAmout(dueAmount);
        receivables.setOperator(accountFlow.getOperator());
        receivables.setBusiNo(accountFlow.getBusiNo());
        receivables.setUname(accountFlow.getUname());
        receivables.setAmount(payerPayAmount);
        receivables.setDesp(accountFlow.getDesp());
        logger.info("the new receivables [receivables = {}]",ItvJsonUtil.toJson(receivables));
        return receivables;
    }


    public  Receivables cassExplainByAcc(Receivables receivables){
        if(receivables == null){
            return null;
        }
        Short busiType = receivables.getBusiType();
        if(busiType == Constants.BUSI_TYPE_1){
            receivables.setDesp("余额在线充值");
        }
        if(busiType == Constants.BUSI_TYPE_11){
            receivables.setDesp("余额手动充值");
        }
        if(busiType == Constants.BUSI_TYPE_2){
            receivables.setDesp("用户余额提现");
        }
        if(busiType == Constants.BUSI_TYPE_3){
            String busiNo = receivables.getBusiNo();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("订单:");
            stringBuffer.append(busiNo);
            stringBuffer.append("线上");
            stringBuffer.append("支付");
            receivables.setDesp(stringBuffer.toString());
        }
        if(busiType == Constants.BUSI_TYPE_4){
            receivables.setDesp("结算扣款");
        }
        if(busiType == Constants.BUSI_TYPE_5){
            receivables.setDesp("销售收入");
        }
        if(busiType == Constants.BUSI_TYPE_6){
            receivables.setDesp("退款收入");
        }
        if(busiType == Constants.BUSI_TYPE_7){
            receivables.setDesp("支付冻结");
        }
        if(busiType == Constants.BUSI_TYPE_8){
            receivables.setDesp("释放冻结金额");
        }
        if(busiType == Constants.BUSI_TYPE_9){
            receivables.setDesp("配货扣款");
        }
        if(busiType == Constants.BUSI_TYPE_10){
            receivables.setDesp("配货退款");
        }
        if(busiType == Constants.BUSI_TYPE_12){
            receivables.setDesp("索赔退款");
        }
        if(busiType == Constants.BUSI_TYPE_13){
            receivables.setDesp("无货到索赔退款");
        }
        if(busiType == Constants.BUSI_TYPE_14){
            receivables.setDesp("订单退款");
        }
        if(busiType == Constants.BUSI_TYPE_15){
            receivables.setDesp("充值错误提现");
        }
        if(busiType == Constants.BUSI_TYPE_16){
            receivables.setDesp("提现冻结");
        }
        if(busiType == Constants.BUSI_TYPE_19 || busiType == Constants.BUSI_TYPE_20){
            receivables.setDesp("信用还款");
        }
        if(busiType == Constants.RECEIVABLES_BUSI_TYPE_22){
            receivables.setDesp("订单退款");
        }
        return receivables;

    }

    //根据UID查询最新财务余额日志的方法
    public Receivables checkReceivablesIsExist(String userId) {

        Receivables receivablesByUid = receivablesService.getReceivablesByUid(userId);
        //判断缓存中是否有流水记录
        return receivablesByUid;
    }

    public int addReceivables(AccountFlowVo accountFlow) {

        Receivables receivablesByAcc = getReceivablesByAcc(accountFlow);
        if (receivablesByAcc == null){
            return 0;
        }
        int i = receivablesDao.addReceivables(receivablesByAcc);
        return i;
    }

    public static void main(String[] args) {

        BigDecimal bigDecimal = new BigDecimal(Constants.NULL_BALANCE);
        Receivables receivables = new Receivables();
        bigDecimal = receivables.getFinancialBalance().subtract(new BigDecimal(2.2));
        System.out.println(bigDecimal);
    }
}
