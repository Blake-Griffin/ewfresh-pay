package com.ewfresh.pay.util;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.ReceivablesService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * description:用于封装资金流水的说明
 *
 * @author: wangyaohui
 * @date 2018年5月4日9:16:06
 */
@Component
public class AccountFlowDescUtil {

    private static final Logger logger = LoggerFactory.getLogger(AccountFlowDescUtil.class);
    @Autowired
    private  AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private  AccountFlowService accountFlowService;
    @Autowired
    private ReceivablesService receivablesService;


    public  AccountFlow cassExplainByAcc(AccountFlow accountFlow){
        if(accountFlow == null){
            return null;
        }
        Short busiType = accountFlow.getBusiType();
        if(busiType == Constants.BUSI_TYPE_1){
            accountFlow.setExplain("余额在线充值");
        }
        if(busiType == Constants.BUSI_TYPE_19){
            if(accountFlow.getAccType()==Constants.ACC_TYPE_7){
                accountFlow.setExplain("银联信用还款");
            }
            if(accountFlow.getAccType()==Constants.SHORT_FOUR){
                accountFlow.setExplain("余额信用还款");
            }
            if(accountFlow.getAccType()==Constants.ACC_TYPE_5 || accountFlow.getAccType()==Constants.ACC_TYPE_3){
                accountFlow.setExplain("快钱信用还款");
            }
        }
        if(busiType == Constants.BUSI_TYPE_20){
            accountFlow.setExplain("信用自动还款");
            accountFlow.setOperator(Constants.SYSTEM_ID);
        }
        if(busiType == Constants.BUSI_TYPE_22){
            accountFlow.setExplain("订单退款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_11){
            accountFlow.setExplain("余额手动充值");
        }
        if(busiType == Constants.BUSI_TYPE_2){
            accountFlow.setExplain("用户余额提现");
        }
        if(busiType == Constants.BUSI_TYPE_3){
            String busiNo = accountFlow.getBusiNo();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("订单:");
            stringBuffer.append(busiNo);
            Short accType = accountFlow.getAccType();
            String accType1 = getAccType(accType);
            stringBuffer.append(accType1);
            stringBuffer.append("支付");
            accountFlow.setExplain(stringBuffer.toString());
        }
        if(busiType == Constants.BUSI_TYPE_4){
            accountFlow.setExplain("结算扣款");
        }
        if(busiType == Constants.BUSI_TYPE_5){
            accountFlow.setExplain("销售收入");
        }
        if(busiType == Constants.BUSI_TYPE_6){
            accountFlow.setExplain("退款收入");
        }
        if(busiType == Constants.BUSI_TYPE_7){
            accountFlow.setExplain("支付冻结");
        }
        if(busiType == Constants.BUSI_TYPE_8){
            accountFlow.setExplain("释放冻结金额");
        }
        if(busiType == Constants.BUSI_TYPE_9){
            accountFlow.setExplain("配货补款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_10){
            accountFlow.setExplain("配货退款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_12){
            accountFlow.setExplain("索赔退款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_13){
            accountFlow.setExplain("无货到索赔退款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_14){
            accountFlow.setExplain("订单退款");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_15){
            accountFlow.setExplain("充值错误提现");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_16){
            accountFlow.setExplain("提现冻结");
            getBusiNos(accountFlow);
        }
        if(busiType == Constants.BUSI_TYPE_18){
            accountFlow.setExplain("活动红包");
        }
        accountFlow.setDesp(accountFlow.getExplain());
        String userInfo = null;
        Long userId = accountFlow.getUserId();
        logger.info(" this accountFlow is own of user [userId = {}]", userId + "");
        try {
            userInfo = accountFlowRedisService.getUserInfo(userId);
            logger.info(" this userInfo is own of user [userInfo = {}]", userInfo);
        } catch (Exception e) {
            logger.error("get userInfo failed",e);
        }
        if(StringUtils.isNotBlank(userInfo)){
            Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
            });
            Object nick = userInfoMap.get(Constants.NICK_NAME);
            if (nick != null){
                String nickName = (String) nick;
                accountFlow.setUname(nickName);
            }

        }else {
            accountFlow.setUname("");
        }

        return accountFlow;

    }

    public final static String getAccType(Short type){
        if(type != null) {
            switch (type) {
                case 1:
                    return "支付宝";
                case 2:
                    return "微信";
                case 3:
                    return "银行卡";
                case 4:
                    return "余额";
                case 5:
                    return "快钱";
                case 6:
                    return "白条";
                case 7:
                    return "银联";

            }
        }
        return null;
    }


    public Receivables checkReceivablesIsExist(String uid){
        Receivables receivables =  receivablesService.getReceivablesByUid(uid);
        return receivables;
    }

    public static BigDecimal getAvailableFinancialBalance(Receivables receivables) {
        BigDecimal balance = receivables.getFinancialBalance();//该用户的余额
        BigDecimal freezeAmount = receivables.getFinancialBalance();//该用户余额中的冻结金额
        BigDecimal availableBalance = balance.subtract(freezeAmount);//该用户的可用余额
        return availableBalance;
    }

    public String getUid(Short tradeType, PayFlow payFlow){
        String uid = null;
        if (tradeType == Constants.TRADE_TYPE_15 || tradeType == Constants.TRADE_TYPE_14 ||tradeType == Constants.TRADE_TYPE_3 || tradeType == Constants.TRADE_TYPE_8 || tradeType == Constants.TRADE_TYPE_1 || tradeType == Constants.TRADE_TYPE_4 || tradeType == Constants.TRADE_TYPE_12) {
            uid = payFlow.getPayerId();
        }
        if (tradeType == Constants.TRADE_TYPE_18 || tradeType == Constants.TRADE_TYPE_2 || tradeType == Constants.TRADE_TYPE_5 || tradeType == Constants.TRADE_TYPE_9 || tradeType == Constants.TRADE_TYPE_10 || tradeType == Constants.TRADE_TYPE_11) {
            uid = payFlow.getReceiverUserId();
        }
        return uid;
    }

    public Receivables getReceivables(PayFlow payFlow) {
        String uid = null;
        Short tradeType = payFlow.getTradeType();
        if (tradeType == Constants.TRADE_TYPE_3 || tradeType == Constants.TRADE_TYPE_8 || tradeType == Constants.TRADE_TYPE_1) {
            uid = payFlow.getPayerId();
        }
        if (tradeType == Constants.TRADE_TYPE_2 || tradeType == Constants.TRADE_TYPE_5 || tradeType == Constants.TRADE_TYPE_9 || tradeType == Constants.TRADE_TYPE_10 || tradeType == Constants.TRADE_TYPE_11) {
            uid = payFlow.getReceiverUserId();
        }
        logger.info(" the uid ----------->[uid = {}]", uid);
        Receivables receivables = checkReceivablesIsExist(uid);
        logger.info(" the new accountFlow for this user [accountFlowVo = {}]", ItvJsonUtil.toJson(receivables));
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        if (receivables == null) {
            receivables = new Receivables();
            receivables.setFinancialBalance(new BigDecimal(Constants.NULL_BALANCE));
            receivables.setDueAmout(new BigDecimal(Constants.NULL_BALANCE));
        }
        BigDecimal balance = receivables.getFinancialBalance();
        BigDecimal dueAmout = receivables.getDueAmout();
        BigDecimal newBalance = null;
        BigDecimal newDueAmout = null;
        if (tradeType == Constants.TRADE_TYPE_2) {
            //退款到财务余额
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_14);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setOperator(payFlow.getPayerId());
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            newDueAmout = dueAmout.subtract(payerPayAmount);
            receivables.setDueAmout(newDueAmout);
            logger.info(" order refund balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_10) {
            //索赔退款到余额
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_12);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setOperator(payFlow.getPayerId());
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            newDueAmout = dueAmout.subtract(payerPayAmount);
            receivables.setDueAmout(newDueAmout);
            logger.info(" claim return balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_11) {
            //无货到索赔退款到余额
            payFlow.setReceiverName(Constants.BALANCE);
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setBusiType(Constants.BUSI_TYPE_13);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setOperator(payFlow.getPayerId());
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            newBalance = balance.add(payerPayAmount);
            logger.info(" not recieve claim return balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_9) {
            //配货退款到余额
            payFlow.setReceiverName(Constants.BALANCE);
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setBusiType(Constants.BUSI_TYPE_10);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setOperator(payFlow.getPayerId());
            receivables.setSrcAcc(Constants.RECEIVERNAME);
            newBalance = balance.add(payerPayAmount);
            logger.info(" return balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_8) {
            //配货扣除余额
            // TODO: 2018/6/6 0006
            BigDecimal availableFinancialBalance = getAvailableFinancialBalance(receivables);
            if (availableFinancialBalance.compareTo(payerPayAmount) == -1) {
                logger.info("  availableFinancialBalance status for distribution abatement param [ availableFinancialBalance = {},payerPayAmount = {}]", availableFinancialBalance, payerPayAmount);
                //可用余额小于扣除金额
                return null;
            }
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.RECEIVERNAME);
            receivables.setBusiType(Constants.BUSI_TYPE_9);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setOperator(payFlow.getReceiverUserId());
            receivables.setSrcAcc(Constants.BALANCE);
            availableFinancialBalance = availableFinancialBalance.subtract(payerPayAmount);
            receivables.setDueAmout(availableFinancialBalance);
            logger.info(" abatements balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_3) {
            //线下充值的业务
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.BALANCE);
            receivables.setBusiType(Constants.BUSI_TYPE_11);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setOperator(payFlow.getReceiverUserId());
            receivables.setSrcAcc(null);
            newBalance = balance.add(payerPayAmount);
            logger.info(" recharge balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        if (tradeType == Constants.TRADE_TYPE_5) {
            //提现的业务
            receivables.setUserId(Long.valueOf(uid));
            receivables.setOperator(payFlow.getPayerId());
            receivables.setBusiType(Constants.BUSI_TYPE_2);
            receivables.setDirection(Constants.DIRECTION_OUT);
            receivables.setSrcAcc(Constants.BALANCE);
            receivables.setTargetAcc(null);
            BigDecimal freezeAmount = receivables.getDueAmout();
            if (balance.compareTo(payerPayAmount) == -1 || (freezeAmount.compareTo(payerPayAmount) == -1)) {
                logger.info("the judge param [balance = {} , payerPayAmount = {}, freezeAmount = {}]", balance, payerPayAmount, freezeAmount);
                return null;
            }
            newBalance = balance.subtract(payerPayAmount);
            if (freezeAmount.compareTo(payerPayAmount) == -1){
                freezeAmount = freezeAmount.subtract(payerPayAmount);
            }
            receivables.setDueAmout(freezeAmount);
            logger.info(" withdrawto balance result [receivables = {},newBalance = {}]", ItvJsonUtil.toJson(receivables), newBalance);
        }
        String channelCode = payFlow.getChannelCode();
        if (tradeType == Constants.TRADE_TYPE_1) {
            //订单扣款的业务
            if (channelCode.equals(Constants.UID_BALANCE)) {
                newBalance = balance.subtract(payerPayAmount);
                BigDecimal freezeAmount = receivables.getDueAmout();
                freezeAmount = freezeAmount.subtract(payerPayAmount);
                receivables.setDueAmout(freezeAmount);
            }
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            receivables.setUserId(Long.valueOf(uid));
            receivables.setTargetAcc(Constants.SUNKFA);
            receivables.setBusiType(Constants.BUSI_TYPE_3);
            receivables.setDirection(Constants.DIRECTION_IN);
            receivables.setOperator(payFlow.getReceiverUserId());
            receivables.setSrcAcc(payFlow.getChannelName());
            receivables.setIsBalance(Constants.IS_REFUND_NO);
        }
        receivables.setFinancialBalance(newBalance);
        receivables.setAmount(payerPayAmount);
        receivables.setIsBalance(Constants.IS_REFUND_YES);
        receivables.setBusiNo(payFlow.getOrderId() + "");
        return null;
    }

    public AccountFlow getBusiNos(AccountFlow accountFlow) {
        if (accountFlow == null) {
            return null;
        }
        String busiNo = accountFlow.getBusiNo();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("订单:");
        stringBuffer.append(busiNo);
        stringBuffer.append(accountFlow.getExplain());
        accountFlow.setExplain(stringBuffer.toString());
        return accountFlow;
    }
}
