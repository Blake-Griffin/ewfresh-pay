/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/25.
 *  
 **/

package com.ewfresh.pay.worker;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * @descrption TODO
 *  * @author gaoyongqiqng
 *  * @create 2019-03-25
 *  * @Email 1005267839@qq.com
 **/
@Component
public class PassiveRepaymentWorker {
    private Logger logger = LoggerFactory.getLogger(PassiveRepaymentWorker.class);

    private static final Short BILL_STATUS_1 = 1;//还款渠道1
    private static final Short REPAYTYPE = 0;//被动还款
    private static final Short USE_STATUS_2 = 2;//白条冻结
    private static final Short USE_STATUS_3 = 3;//白条违约
    private static final Short USE_STATUS_5 = 5;//逾期还款
    private static BigDecimal availableamout = new BigDecimal(0);
    private static BigDecimal ZERO = new BigDecimal(0);
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private BarDealFlowService barDealFlowService;
    @Value("${http_idgen}")
    private String ID_URL;
    @Autowired
    private WhiteBillsWorker wWorker;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private HttpDeal httpDeal;

    /**
     *  * @author gaoyongqiang
     *  * @Description 被动还款
     *  * @Date   2019/3/21 9:24
     *  *  @params
     *  * @return 
     **/
    @Scheduled(cron = "0 0 13 * * ?")
    public void forceRepayment() throws Exception {
        logger.info("Time Task PassiveRepaymentWorker Starts......");
        //得到所有流水单
        List<Bill> whiteBillslists = whiteBillsService.getByRecordingTime();//查询所有未还款账单
        logger.info(" this time for forceRepayment to accflow [ whiteBillslists = {} ]", ItvJsonUtil.toJson(whiteBillslists));
        LinkedHashSet<Long> link = new LinkedHashSet<Long>();//得到用户Uid（去重）

        if (!whiteBillslists.isEmpty()) {
            for (Bill bills : whiteBillslists) { //循环用户全部未还款账单
                long days = wWorker.getApartDays(bills.getLastRepaidTime());//间隔天数
                String billFlow = bills.getBillFlow();
                if (days >= 0) {  //过滤到达最后还款期限的
                    Map<String, Long> map = whiteBillsService.getUidByBillFlow(billFlow);//根据账单批次号查询用户id
                    logger.info(" the uid for this user [map = {}]", ItvJsonUtil.toJson(map));
                    if (map!=null)
                    link.add(map.get("uid"));
                }
            }
        }

        for (Long uid : link) {   //根据用户循环处理未还款账单
            List<Bill> upbillList = new ArrayList<>();
            List<BillRepayFlow> addBillRepayFlowList = new ArrayList<>();
            List<BarDealFlow> addBarDealFlowList = new ArrayList<>();
            Boolean balanceAndBarBoolean = balanceAndBarLock.getBalanceAndBarLock(uid.toString());
            try {
                if (balanceAndBarBoolean) {
                    List<BillVo> billList = whiteBillsService.getBillsByUid(uid);//用户所有未还款账单
                    logger.info(" User's unpaid bills [billList = {}]", ItvJsonUtil.toJson(billList));
                    AccountFlowVo accountFlow = accountFlowService.getAccountFlowByUid(uid + "");//用户资金账户
                    logger.info(" User Available Balance [accountFlow = {}]", ItvJsonUtil.toJson(accountFlow));
                    if (!StringUtils.isEmpty(accountFlow) && !StringUtils.isEmpty(accountFlow.getBalance())) {  //有余额接着走
                        if (!StringUtils.isEmpty(accountFlow.getFreezeAmount())) {
                            availableamout = accountFlow.getBalance().subtract(accountFlow.getFreezeAmount());
                        } else {
                            availableamout = accountFlow.getBalance();//用户资金可用余额
                        }
                        BigDecimal startAmount = availableamout;
                        if (availableamout.compareTo(ZERO) == 1) {
                            //BarDealFlow barDealFlowd = new BarDealFlow();
                            PayFlow payFlow = new PayFlow();
                            BillRepayFlow billRepayFlow = new BillRepayFlow();
                            BigDecimal usedLimit = barDealFlowService.getUsedLimitByUid(uid);//最近的交易流水额度
                            //barDealFlowd.setUsedLimit(usedLimit);//已使用额度  +待还金额
                            BigDecimal totalInterest = new BigDecimal(0);
                            for (Bill bills : billList) {    //处理用户最早账单
                                long days = wWorker.getApartDays(bills.getLastRepaidTime());//间隔天数
                                if (days >= 0 && availableamout.compareTo(ZERO) == 1) {   //过滤到期账单
                                    BarDealFlow barDealFlow = new BarDealFlow();
                                    BigDecimal verduefee = bills.getTotalInterest().subtract(bills.getRepaidInterest());//逾期费
                                    BigDecimal paidAmount = bills.getBillAmount().subtract(bills.getRepaidAmount());//待还金额
                                    BigDecimal surplus = availableamout.subtract(verduefee);//剩余可用的金额
                                    BigDecimal repaidAmount = bills.getRepaidAmount();//已还金额
                                    BigDecimal repaidInterest = bills.getRepaidInterest();//已还利息

                                    if (surplus.compareTo(paidAmount) == -1) { //没还够第一个账单(1.没还够利息2.还够利息)
                                        barDealFlow.setAmount(availableamout);//本次交易金额
                                        fillData(uid, billRepayFlow, bills, barDealFlow, verduefee, paidAmount, surplus);
                                        if (availableamout.compareTo(verduefee) == -1) {
                                            bills.setRepaidInterest(repaidInterest.add(availableamout));
                                            bills.setRepaidAmount(repaidAmount);
                                            billRepayFlow.setRepayAmount(availableamout);
                                            billRepayFlow.setPrincipalAmount(ZERO);
                                            billRepayFlow.setInterestAmount(availableamout);
                                            barDealFlow.setUsedLimit(usedLimit.subtract(ZERO));//已使用额度  +这次还金额
                                            totalInterest = totalInterest.add(availableamout);
                                        } else {
                                            bills.setRepaidInterest(bills.getTotalInterest());
                                            bills.setRepaidAmount(surplus.add(repaidAmount));
                                            billRepayFlow.setRepayAmount(availableamout);
                                            billRepayFlow.setPrincipalAmount(surplus);
                                            billRepayFlow.setInterestAmount(verduefee);
                                            barDealFlow.setUsedLimit(usedLimit.subtract(surplus));//已使用额度  +这次还金额
                                            totalInterest = totalInterest.add(verduefee);
                                        }
                                        availableamout = ZERO;
                                    } else { //还够第一个账单有剩余
                                        barDealFlow.setAmount(paidAmount.add(verduefee));//本次交易金额
                                        fillData(uid, billRepayFlow, bills, barDealFlow, verduefee, paidAmount, surplus);
                                        if (days==0)
                                            bills.setBillStatus(USE_STATUS_2);
                                        else
                                            bills.setBillStatus(USE_STATUS_5);
                                        barDealFlow.setUsedLimit(usedLimit.subtract(paidAmount));//已使用额度  +待还金额
                                        availableamout = surplus.subtract(paidAmount);//还完第一个账单剩余的资金
                                        totalInterest = totalInterest.add(verduefee);
                                    }

                                    usedLimit = barDealFlow.getUsedLimit();
                                    logger.info(" User's unpaid bills [addBarDealFlowList = {}]", ItvJsonUtil.toJson(addBarDealFlowList));
                                    upbillList.add(bills);//需要更新的账单
                                    addBillRepayFlowList.add(billRepayFlow);
                                    addBarDealFlowList.add(barDealFlow);
                                }
                            } //账单循环结束处

                            fillFlowTable(accountFlow, uid, startAmount,payFlow,totalInterest);
                            logger.info(" User's unpaid bills [addBarDealFlowList = {}]", ItvJsonUtil.toJson(addBarDealFlowList));
                            whiteBillsService.passivePaymentBatchRecord(upbillList,addBillRepayFlowList,addBarDealFlowList,payFlow,accountFlow);
                        }
                    }
                    balanceAndBarLock.releaseLock(uid.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                balanceAndBarLock.releaseLock(uid.toString());
            }
        }
        logger.info("Time Task PassiveRepaymentWorker End......");
    }

    //填充t_pay_flow   t_accout_flow
    public void fillFlowTable(AccountFlowVo accountFlow, Long uid, BigDecimal startAmount,PayFlow payFlow,BigDecimal totalInterest) {
       // PayFlow payFlow = new PayFlow();
        String orderId = getIdGenerator();
        String channelFlowid = getBalanceChanelFlow();
        payFlow.setOrderId(Long.parseLong(orderId));
        payFlow.setChannelFlowId(channelFlowid);
        payFlow.setPayerId(uid.toString());
        payFlow.setPayerPayAmount(startAmount.subtract(availableamout));
        payFlow.setReceiverUserId(Constants.UID_BALANCE);
        payFlow.setReceiverName(Constants.RECEIVERNAME);
        payFlow.setOrderAmount(startAmount.subtract(availableamout));
        payFlow.setChannelCode(Constants.UID_BALANCE);
        payFlow.setChannelName(Constants.BALANCE);
        payFlow.setTradeType(Constants.TRADE_TYPE_15);
        payFlow.setCreateTime(new Date());
        payFlow.setSettleStatus(Constants.STATUS_1);
        payFlow.setUname(accountFlow.getUname());
        payFlow.setPayerName(accountFlow.getUname());

        accountFlow.setBalance(availableamout.add(accountFlow.getFreezeAmount()));
        accountFlow.setSrcAcc(Constants.BALANCE);
        accountFlow.setTargetAcc(Constants.SUNKFA);
        accountFlow.setAmount(startAmount.subtract(availableamout));
        accountFlow.setBusiType(Constants.BUSI_TYPE_20);
        accountFlow.setBusiNo(payFlow.getOrderId().toString());
        accountFlow.setOccTime(new Date());
        accountFlow.setBalanceTime(new Date());
        accountFlow.setSettleTime(new Date());
        accountFlow.setLastModifyTime(new Date());
        accountFlow.setOperator(Constants.UID_BALANCE);
        accountFlow.setDesp("订单:" + payFlow.getOrderId().toString() + "还款");
        accountFlow.setDirection(USE_STATUS_2);
        accountFlow.setAccType(Constants.ACC_TYPE_4);
        accountFlow.setBillInsertt(totalInterest);
        if (availableamout.compareTo(ZERO) != -1) {
            accountFlowRedisService.setQuotaUnfreeze(uid);
        }
    }

    //填充数据
    protected void fillData(Long uid, BillRepayFlow billRepayFlow, Bill bills, BarDealFlow barDealFlow,
                            BigDecimal verduefee, BigDecimal paidAmount, BigDecimal surplus) throws ParseException {

        bills.setRepaidInterest(bills.getTotalInterest());
        bills.setRepaidAmount(bills.getBillAmount());
        bills.setId(bills.getId());
        bills.setBillStatus(USE_STATUS_3);
        bills.setLastModifyTime(new Date());

        billRepayFlow.setBillId(bills.getId());
        billRepayFlow.setRepayAmount(paidAmount.add(verduefee));
        billRepayFlow.setPrincipalAmount(paidAmount);
        billRepayFlow.setInterestAmount(verduefee);
        billRepayFlow.setRepayChannel(BILL_STATUS_1);
        billRepayFlow.setRepayType(REPAYTYPE);
        billRepayFlow.setRepayTime(new Date());
        billRepayFlow.setOperator(Constants.SYSTEM_ID);
        billRepayFlow.setOrderId(Long.valueOf(getIdGenerator()));

        barDealFlow.setUid(uid);
        barDealFlow.setBillFlow(bills.getBillFlow());
        barDealFlow.setUname(bills.getUname());  //客户名称
        barDealFlow.setDirection(USE_STATUS_2);//资金流向(1流出,2流入)
        barDealFlow.setShopId(0L);//进行交易的店铺ID',
        barDealFlow.setShopName(Constants.SELF_SHOPNAME);//店铺名称
        barDealFlow.setOrderId(Long.valueOf(getIdGenerator())); //交易订单号
        barDealFlow.setDealType(USE_STATUS_3);//交易类型(1订单付款,2订单退款,3还款)
        barDealFlow.setOccTime(new Date());//交易时间
        barDealFlow.setLastModifyTime(new Date());//最后修改时间
    }

    public String getIdGenerator() {
        HashMap<String, String> param = new HashMap<>();
        param.put(Constants.FIRSTKEY, Constants.ID_WHITE_ORDER_KEY);
        String post = httpDeal.post(ID_URL, param, null, null);
        Map<String, String> map = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String balanceId = map.get("entity");
        return balanceId;
    }

    //ID生成器获取余额支付支付渠道流水的方法
    public String getBalanceChanelFlow() {
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        Long uid = context.getUid();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, Constants.ID_GEN_PAY_VALUE);
        String idStr = httpDeal.post(ID_URL, map, token, uid + "");
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        return hashMap.get(Constants.ENTITY).toString();
    }

    public static void main(String[] args) {
        System.out.print("");
    }
}
