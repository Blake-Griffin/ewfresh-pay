package com.ewfresh.pay.worker;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.dao.ReceivablesDao;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.FinishOrderVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.ReceivablesService;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Description:异常情况解冻余额的worker
 *
 * @author DuanXiangming
 * Date 2018/8/2 0002
 */
@Component
public class UnfreezeBalanceWorker {


    private static final Logger logger = LoggerFactory.getLogger(UnfreezeBalanceWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private ReceivablesService receivablesService;
    @Autowired
    private ReceivablesDao receivablesDao;
    @Autowired
    private BalanceAndBarLock lockHandler;

    private Long time = 3* 1000L;//线程休息时间

    private static final Long SUB_TIME = 30 * 60 * 1000L;//交易完成时间差


    /**
     * Description: 异常情况解冻余额的定时任务
     *
     * @author DuanXiangming
     * Date    2018/4/18
     */
    @Scheduled(cron = "0/10 * * * * ? ")
    public void autoAddAccFlow(){
        try {
            autoAddAccFlowHandler();
        } catch (Exception e) {
            logger.error("handle add accflow err",e);
        }
    }

    public void autoAddAccFlowHandler() {
        //获取redis队列中的支付流水对象
        while (true) {
            String unfreezeOAccFlowId = accountFlowRedisService.getUnfreezeAccFlowId();
            if (StringUtils.isBlank(unfreezeOAccFlowId)) {
                break;
            }
            AccountFlowVo accountFlow = accountFlowService.getAccFlowById(unfreezeOAccFlowId);
            /*此处涉及自动取消的订单,有可能为空,为空不做处理*/
            if (accountFlow == null) {
                //若查询结果为空,则有异常情况,需记录
                logger.error("the err accflowid = {}",unfreezeOAccFlowId);
                continue;
            }
            String userId = accountFlow.getUserId().toString();
            boolean lockFlag = lockHandler.getBalanceAndBarLock(userId);
            if (!lockFlag){
                logger.info("the redis lock is not allow to use [userId = {}]",userId);
                accountFlowRedisService.setUnfreezeOrderId(null,Integer.valueOf(unfreezeOAccFlowId));
            }
            //判断是否为已支付的冻结流水
            Short isBalance = accountFlow.getIsBalance();
            if (isBalance.shortValue() == Constants.SHORT_ONE.shortValue()){
                //在支付完成后已经将is_balance字段从0改到了1,所以此处isBalance为1证明已经修改过,无需处理
                logger.info("the already unfreeze accflow id = {}" , unfreezeOAccFlowId);
                lockHandler.releaseLock(userId);
                continue;
            }
            /*if (isBalance.shortValue() == Constants.SHORT_ZERO.shortValue()){
                //在支付完成后已经将is_balance字段从0改到了1,所以此处isBalance为0证明未修改过修改过,或者支付进程仍在进行
                //线程程休息3秒
                logger.info("the acc flow haven't unfreeze [unfreezeOAccFlowId = {}]",unfreezeOAccFlowId);
                try {
                    Thread.sleep(time);
                    accountFlow = accountFlowService.getAccFlowById(unfreezeOAccFlowId);
                    isBalance = accountFlow.getIsBalance();
                } catch (Exception e) {
                    logger.error(" unfreeze worker thread sleep err [unfreezeOAccFlowId = " + unfreezeOAccFlowId +"]",e);
                }
                //3秒后仍没有变化
                if(isBalance.shortValue() == Constants.SHORT_ONE.shortValue()){
                    //3秒后订单已支付,且此记录已经被修改
                    logger.info("the accflow is already deduct [unfreezeOAccFlowId = {}]",unfreezeOAccFlowId);
                    lockHandler.releaseLock(lock);
                    return;
                }
            }*/
            //到此仍未解冻
            logger.error(" the freeze acc flow id [unfreezeOAccFlowId = {}] ", unfreezeOAccFlowId);
            String busiNo = accountFlow.getBusiNo();
            //查询此订单是否有在本次冻结之后的支付记录已支付
            AccountFlowVo payAccountFlowVo = accountFlowService.getPayAccountFlowAfterFreezen(busiNo,unfreezeOAccFlowId);
            logger.info("make sure this account flow is pay or not [payAccountFlowVo = {}]", ItvJsonUtil.toJson(payAccountFlowVo));
            if(payAccountFlowVo == null){
                //未支付
                Date occTime = accountFlow.getOccTime();
                Date date = new Date();
                long subTime =date.getTime() - occTime.getTime();
                logger.info("the occTime and now [occTime = {}, now = {}]" , occTime, date);
                if (subTime < SUB_TIME ){
                    logger.info("the subTime lt 1 hour for this opreation [ occTime = {},unfreezeOAccFlowId = {}]",occTime,unfreezeOAccFlowId);
                    //冻结完成时间和当前时间差小于1小时,放回redis 队列等待下次进行 20190806弃用
                    //冻结完成时间和当前时间差小于0.5小时,放回redis 队列等待下次进行 20190806起用
                    accountFlowRedisService.setUnfreezeOrderId(null,Integer.valueOf(unfreezeOAccFlowId));
                    lockHandler.releaseLock(userId);
                    return;
                }else {
                    logger.info("the subTime gt 1 minitue for this opreation [ occTime = {},unfreezeOAccFlowId = {}]",occTime,unfreezeOAccFlowId);
                    //时间差大于1小时分钟
                    AccountFlowVo unfreezeAccFLow = getUnfreezeAccFLow(accountFlow);
                    if (unfreezeAccFLow == null){
                        //只有一种情况会返回空值,冻结金额小于该笔冻结
                        logger.error(" err unfreeze opreation [accountFlow = {},unfreezeOAccFlowId = {}]",ItvJsonUtil.toJson(accountFlow),unfreezeOAccFlowId);
                        continue;
                    }
                    unfreezeAccFLow.setPayFlowId(Integer.valueOf(unfreezeOAccFlowId));
                    accountFlowDescUtil.cassExplainByAcc(unfreezeAccFLow);
                    accountFlowService.unfreezeAccFlow(unfreezeAccFLow,unfreezeOAccFlowId);
                    lockHandler.releaseLock(userId);
                    continue;
                }
            }
            //已支付的情况,且未解冻,可能为多次冻结
            //此情况下,先查询是否已经有冻结金额被修改的记录
            AccountFlowVo unfreezeAccFLow = getUnfreezeAccFLow(accountFlow);
            if (unfreezeAccFLow == null){
                //只有一种情况会返回空值,冻结金额小于该笔冻结
                logger.error(" err unfreeze opreation [accountFlow = {},unfreezeOAccFlowId = {}]",ItvJsonUtil.toJson(accountFlow),unfreezeOAccFlowId);
                lockHandler.releaseLock(userId);
                continue;
            }
            unfreezeAccFLow.setPayFlowId(Integer.valueOf(unfreezeOAccFlowId));
            accountFlowDescUtil.cassExplainByAcc(unfreezeAccFLow);
            accountFlowService.unfreezeAccFlow(unfreezeAccFLow,unfreezeOAccFlowId);
            lockHandler.releaseLock(userId);
        }

    }


    private AccountFlowVo getUnfreezeAccFLow(AccountFlowVo accountFlow){
        logger.info("the account flow is not ");
        BigDecimal amount = accountFlow.getAmount();
        String busiNo = accountFlow.getBusiNo();
        AccountFlowVo accountFlowVo = balanceManager.checkBalanceIsExist(accountFlow.getUserId() + "");
            /*此处需要校验冻结金额与解冻金额的大小*/
        BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
        if (freezeAmount.compareTo(amount) == -1) {
            //冻结金额小于解冻金额,不做处理
            logger.error(" unfreeze balance error [unfreezeOrderId = {}, accountFlowVo = {},accountFlow = {}", busiNo, ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(accountFlow));
            return null;
        }
        accountFlowVo.setPayFlowId(Constants.INTEGER_ZERO);
        accountFlowVo.setFreezeAmount(freezeAmount.subtract(amount));
        accountFlowVo.setBusiNo(busiNo);
        accountFlowVo.setBusiType(Constants.BUSI_TYPE_8);
        accountFlowVo.setSrcAcc(Constants.BALANCE);
        accountFlowVo.setTargetAcc(Constants.BALANCE);
        accountFlowVo.setAmount(amount);
        accountFlowVo.setOperator(Constants.SYSTEM_ID);
        logger.info(" add accountFlow success [unfreezeOrderId = {}, userId = {}]", busiNo, accountFlowVo.getUserId());
        return accountFlowVo;
    }

    /**
     * Description: 财务余额订单完结计算
     * @author DuanXiangming
     * Date    2018/4/18
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void finishOrder() {
        //获取redis队列中的完结订单的信息
        while (true) {
            List<String> finishOrders = accountFlowRedisService.getFinishOrder();
            if (CollectionUtils.isEmpty(finishOrders)) {
                return;
            }
            logger.info("the finsh orders [finishOrders = {}]",finishOrders);
            for (String finishOrder : finishOrders) {
                boolean flag = false;
                String uid = "";
                try {
                    logger.info("the finish order [finishOrder = {}]",finishOrder);
                    //z转换获取订单结算金额
                    FinishOrderVo finishOrderVo = ItvJsonUtil.jsonToObj(finishOrder, FinishOrderVo.class);
                    //获取所需信息
                    uid = finishOrderVo.getUid().toString();
                    if (StringUtils.isBlank(uid)) {
                        logger.info("get uid null [uid = {}]", finishOrder);
                        continue;
                    }
                    boolean balanceAndBarLock = lockHandler.getBalanceAndBarLock(uid);
                    if (!balanceAndBarLock){
                        //未获取到锁,需要从新来过
                        accountFlowRedisService.setFinishOrder(finishOrderVo);
                        continue;
                    }
                    flag = true;
                    Receivables receivables = getReceivablesByFinishOrder(finishOrderVo);
                    if (receivables == null){
                        lockHandler.releaseLock(uid);
                        continue;
                    }
                    receivablesDao.addReceivables(receivables);
                    lockHandler.releaseLock(uid);
                } catch (Exception e) {
                    logger.info(" set finish order err [order = " + finishOrder +"]", e);
                    if (flag){
                        lockHandler.releaseLock(uid);
                    }
                }
            }
        }
    }


    private Receivables getReceivablesByFinishOrder(FinishOrderVo finishOrderVo){
        String uid = String.valueOf(finishOrderVo.getUid());
        //获取该用户最新的财务余额
        Receivables receivables = receivablesService.getReceivablesByUid(uid);
        BigDecimal finalAmount = finishOrderVo.getFinishAmount();
        BigDecimal financialBalance = receivables.getFinancialBalance();
        BigDecimal dueAmout = receivables.getDueAmout();
        BigDecimal disposalFee = finishOrderVo.getDisposalFee();
        Short isRefund = finishOrderVo.getIsRefund();
        String orderId = finishOrderVo.getOrderId().toString();
        logger.info("final this order receivable isRefund [isRefund = {}",isRefund);
        if(isRefund.shortValue() == Constants.SHORT_ONE.shortValue() && disposalFee.compareTo(new BigDecimal(Constants.NULL_BALANCE)) == 0){
            ////退款不包含处置费
            logger.info("refund but not include disposalFee [disposalFee = {}]",disposalFee);
            return  null;
        }
        if (isRefund.shortValue() == Constants.SHORT_ONE.shortValue()){
            //退款包含处置费,需减去同金额的在途
            receivables.setDueAmout(dueAmout.subtract(disposalFee));
            receivables.setDesp("订单:" + orderId + "处置费退还");
            receivables.setAmount(disposalFee);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_18);
        }
        if (isRefund.shortValue() == Constants.SHORT_ZERO.shortValue()){
            //非退款完结
            receivables.setFinancialBalance(financialBalance.subtract(finalAmount));
            receivables.setDueAmout(dueAmout.subtract(finalAmount));
            receivables.setDesp("订单:" + orderId + "完结结算");
            receivables.setAmount(finalAmount);
            receivables.setBusiType(Constants.RECEIVABLES_BUSI_TYPE_17);
        }
        receivables.setBusiNo(orderId);
        receivables.setOperator(Constants.SYSTEM_ID);
        receivables.setPayFlowId(0L);
        receivables.setTargetAcc(null);
        receivables.setSrcAcc(null);
        receivables.setDirection(Constants.SHORT_TWO);
        logger.info(" finish order success [receivables= {}]",receivables);
        return receivables;
    }

}
