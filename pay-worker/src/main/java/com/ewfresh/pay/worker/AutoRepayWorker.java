package com.ewfresh.pay.worker;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.BillRepayFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.IdUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2019/3/31 0031
 */
@Component
public class AutoRepayWorker {


    private Logger logger = LoggerFactory.getLogger(AutoRepayWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private BarDealFlowService barDealFlowService;
    @Value("${http_idgen}")
    private String ID_URL;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private IdUtils idUtils;


    @Scheduled(cron = "0-59 * * * * ?")
    public void autoRepayWorker() {

        String accId = accountFlowRedisService.getBalanceChangeInfo();
        if (StringUtils.isBlank(accId)) {
            return;
        }
        AccountFlowVo accFlow = accountFlowService.getAccFlowById(accId);
        if (accFlow == null){
            return;
        }
        Short direction = accFlow.getDirection();//本条账户流水的流向
        if (direction == 2){
            return;
        }
        Long userId = accFlow.getUserId();
        String uid = String.valueOf(userId);
        boolean balanceAndBarLock = this.balanceAndBarLock.getBalanceAndBarLock(uid);
        try {
            if (balanceAndBarLock) {
                List<AutoRepayBillVo> billList = whiteBillsService.getOvertimeBills(userId);//用户所有未还款账单
                if (CollectionUtils.isEmpty(billList)) {
                    logger.debug("the user has no overdue bill [userId = {}]", userId);
                    this.balanceAndBarLock.releaseLock(uid);
                    return;
                }
                //余额发生变动,且有逾期账单,查询余额变化
                AccountFlowVo accountFlowVo = balanceManager.checkBalanceIsExist(uid);
                BigDecimal availableBalance = accountFlowVo.getAvailableBalance();
                if (availableBalance.compareTo(Constants.BIGDECIMAL_ZERO) != 1) {
                    //客户没有余额可以用于还款
                    logger.debug("the user has no balance for repay bills [userId = {}]",userId);
                    this.balanceAndBarLock.releaseLock(uid);
                    return;
                }
                autoRepay(billList,uid,accountFlowVo);
                this.balanceAndBarLock.releaseLock(uid);
            }
        } catch (Exception e) {
            logger.error("auto repay err",e);
            this.balanceAndBarLock.releaseLock(uid);
        }

    }

    private void autoRepay(List<AutoRepayBillVo> billList, String uid, AccountFlowVo accountFlowVo) {
        logger.info("auto repay start [billList = {}, accountFlowVo = {}]", ItvJsonUtil.toJson(billList), ItvJsonUtil.toJson(accountFlowVo));
        List<AutoRepayBillVo> autoRepayBillVos = new ArrayList<>();
        List<BillRepayFlow> billRepayFlows = new ArrayList<>();
        List<BarDealFlow> barDealFlows = new ArrayList<>();
        BigDecimal availableBalance = accountFlowVo.getAvailableBalance();
        String uname = accountFlowVo.getUname();
        BigDecimal totalPayableAmount = new BigDecimal(Constants.INTEGER_ZERO);//待还总额
        String orderId = idUtils.getId(ID_URL, Constants.ID_GEN_PAY_VALUE);
        Long orderIdL = Long.valueOf(orderId);
        Long userId = Long.valueOf(uid);
        BigDecimal totalRepayInterest = BigDecimal.ZERO;
        BarDealFlow newBarDealFlowVo= barDealFlowService.getDealFlowByUid(userId);//最近的交易流水额度
        logger.info("the last barDealFlow is [newBarDealFlowVo = {}]",ItvJsonUtil.toJson(newBarDealFlowVo));
        for (AutoRepayBillVo bill : billList) {
            BigDecimal payableAmount = bill.getPayableAmount();//剩余应还账单金额
            BigDecimal payableInterest = bill.getPayableInterest();//剩余应还利
            BigDecimal totalInterest = bill.getTotalInterest();
            BigDecimal repaidInterest = bill.getRepaidInterest();
            BigDecimal billAmount = bill.getBillAmount();
            BigDecimal repaidAmount = bill.getRepaidAmount();
            autoRepayBillVos.add(bill);
            BillRepayFlow billRepayFlow = new BillRepayFlow();
            //剩余可还余额
            //本次还款总额
            BigDecimal surplusBalance = availableBalance.subtract(totalPayableAmount);
            BigDecimal interestRepay = new BigDecimal(Constants.NULL_BALANCE);//本次归还利息
            BigDecimal repayAmount = BigDecimal.ZERO;//本次归还本金
            if (surplusBalance.compareTo(payableInterest) != -1 ){
                //剩余可还余额大于等于应还利息
                billRepayFlow.setInterestAmount(payableInterest);
                bill.setRepaidInterest(totalInterest);
                surplusBalance = surplusBalance.subtract(payableInterest);
                interestRepay = interestRepay.add(payableInterest);
            }else {
                billRepayFlow.setInterestAmount(surplusBalance);
                bill.setRepaidInterest(repaidInterest.add(surplusBalance));
                interestRepay = interestRepay.add(surplusBalance);
                surplusBalance = Constants.BIGDECIMAL_ZERO;
            }
            if (surplusBalance.compareTo(payableAmount) != -1){
                billRepayFlow.setPrincipalAmount(payableAmount);
                bill.setRepaidAmount(billAmount);
                repayAmount = repayAmount.add(payableAmount);
                if (totalInterest.compareTo(Constants.BIGDECIMAL_ZERO) == 1){
                    //已产生利息
                    bill.setBillStatus(Constants.SHORT_FIVE);
                }else {
                    bill.setBillStatus(Constants.SHORT_TWO);
                }
            }else {
                bill.setRepaidAmount(repaidAmount.add(surplusBalance));
                billRepayFlow.setPrincipalAmount(surplusBalance);
                repayAmount = repayAmount.add(surplusBalance);
                bill.setBillStatus(Constants.SHORT_THREE);
            }
            billRepayFlow.setRepayAmount(repayAmount);
            billRepayFlow.setBillId(bill.getId());
            billRepayFlow.setOperator(Constants.SYSTEM_ID);
            billRepayFlow.setRepayChannel(Constants.SHORT_ONE);
            billRepayFlow.setRepayType(Constants.SHORT_ZERO);
            billRepayFlow.setOrderId(orderIdL);
            billRepayFlows.add(billRepayFlow);
            totalPayableAmount = totalPayableAmount.add(payableAmount).add(payableInterest);
            //白条交易流水
            BarDealFlow barDealFlow = new BarDealFlow();
            barDealFlow.setUid(userId);
            barDealFlow.setUname(uname);  //客户名称
            //最新已使用额度
            BigDecimal usedLimit = newBarDealFlowVo.getUsedLimit();
            usedLimit = usedLimit.subtract(repayAmount);
            barDealFlow.setAmount(repayAmount.add(interestRepay));//本次交易金额
            barDealFlow.setUsedLimit(usedLimit);//已使用额度  +待还金额
            barDealFlow.setDirection(Constants.DIRECTION_OUT);//资金流向(1流出,2流入)
            barDealFlow.setShopId(0L);//进行交易的店铺ID',
            barDealFlow.setShopName(Constants.SELF_SHOPNAME);//店铺名称
            barDealFlow.setOrderId(orderIdL); //交易订单号
            barDealFlow.setDealType(Constants.SHORT_THREE);//交易类型(1订单付款,2订单退款,3还款)
            String billFlow = bill.getBillFlow();
            barDealFlow.setBillFlow(billFlow);
            barDealFlows.add(barDealFlow);
            totalRepayInterest = totalRepayInterest.add(interestRepay);
            newBarDealFlowVo = barDealFlow;
            logger.info("get billRepayFlow by bill and accflow [billRepayFlow = {}, surplusBalance = {}, repayAmount = {}, payableAmount = {}, payableInterest = {}]",ItvJsonUtil.toJson(billRepayFlow), surplusBalance, repayAmount, payableAmount, payableInterest);
            if (totalPayableAmount.compareTo(availableBalance) != -1) {
                break;
            }
        }
        PayFlow payFlow = new PayFlow();
        String channelFlowid = idUtils.getId(ID_URL, Constants.ID_WHITE_ORDER_KEY);
        payFlow.setOrderId(orderIdL);
        payFlow.setChannelFlowId(channelFlowid);
        payFlow.setPayerId(uid);
        if (totalPayableAmount.compareTo(availableBalance) == -1){
            payFlow.setPayerPayAmount(totalPayableAmount);
            payFlow.setOrderAmount(totalPayableAmount);
        }else {
            payFlow.setPayerPayAmount(availableBalance);
            payFlow.setOrderAmount(availableBalance);
        }
        payFlow.setReceiverUserId(Constants.INTEGER_ZERO.toString());
        payFlow.setReceiverName(Constants.RECEIVERNAME);
        payFlow.setChannelCode(Constants.UID_BALANCE);
        payFlow.setChannelName(Constants.BALANCE);
        payFlow.setTradeType(Constants.TRADE_TYPE_15);
        payFlow.setSettleStatus(Constants.STATUS_1);
        payFlow.setUname(uname);
        AccountFlowVo accFlow = balanceManager.getAccFlowByPayFlow(payFlow, null);
        accountFlowVo.setBillInsertt(totalRepayInterest);
        accountFlowDescUtil.cassExplainByAcc(accFlow);
        logger.info("the final payflow and barDealFlow [payflow = {}, barDealFlow = {}]",ItvJsonUtil.toJson(payFlow), ItvJsonUtil.toJson(barDealFlows));
        payFlowService.addPayFlowAndAccAndBdfAndBrf(payFlow,accFlow,barDealFlows,billRepayFlows,autoRepayBillVos);
    }



}
