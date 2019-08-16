package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.AccFlowManager;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.BarDealFlowVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.service.AccFlowService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.BillRepayFlowService;
import com.ewfresh.pay.service.impl.BillRepayFlowServiceImpl;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.IdUtils;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * description:
 *
 * @param
 * @author huboyang
 */
@Component
public class AccFlowManagerImpl implements AccFlowManager {
    private static final Logger logger = LoggerFactory.getLogger(BillRepayFlowServiceImpl.class);
    @Autowired
    private AccFlowService accFlowService;
    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private BarDealFlowService barDealFlowService;
    @Autowired
    private BillRepayFlowService billRepayFlowService;
    @Value("${http_idgen}")
    private String ID_URL;
    @Autowired
    private IdUtils idUtils;

    @Override
    public void addAccFlow(AccountFlowVo accountFlow, PayFlow payFlow) {
        logger.info("accountFlow={},payFlow={}",JsonUtil.toJson(accountFlow),JsonUtil.toJson(payFlow));
        //通过还款订单id查询所有还款记录如果记录过了不再重新记录
        List<BillRepayFlow> billRepayFlowList = accFlowService.selectByOrderId(payFlow.getOrderId());
       List<AccountFlowVo> accFlowList = new ArrayList<AccountFlowVo>();
        logger.info("billRepayFlowList", JsonUtil.toJson(billRepayFlowList));
        if (billRepayFlowList.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            BigDecimal interest = new BigDecimal(0);//应还利息
            BigDecimal amount = new BigDecimal(0);//应还本金


            ArrayList<BarDealFlow> barDealFlowList = new ArrayList<>();
            ArrayList<Bill> repaylist = new ArrayList<>();
            ArrayList<Bill> billList = new ArrayList<>();
            ArrayList<BillRepayFlow> billRepayFlowsList = new ArrayList<>();
            List<String> whiteRepayBill = accFlowService.getWhiteRepayBill(Constants.White_Bill_Key+payFlow.getOrderId());
            logger.info("whiteRepayBill={}", JsonUtil.toJson(whiteRepayBill));
            //重新算本次还款的总金额 判断是否有已经还款的账单如果有吧本次还款的溢出金额退回用户余额
            BigDecimal totalbillAmount = new BigDecimal(0);//总金额本次应还账单的总金额
            BigDecimal totalBillInterest = new BigDecimal(0); //还款的总利息
            for(String billId : whiteRepayBill){
                Bill bill = accFlowService.selectByPrimaryKey(Integer.valueOf(billId));
                BigDecimal b = new BigDecimal(0);
                amount = bill.getBillAmount().subtract(bill.getRepaidAmount());
                interest = bill.getTotalInterest().subtract(bill.getRepaidInterest());
                b = amount.add(interest);
                totalbillAmount = totalbillAmount.add(b);
                totalBillInterest = totalBillInterest.add(interest);
                //把未完成的账单放入待还账单的list
                if(bill.getBillStatus()!= 2 && bill.getBillStatus()!= 5 ) {
                    repaylist.add(bill);
                }
            }
            if(payFlow.getPayerPayAmount().compareTo(totalbillAmount)==1){
                AccountFlowVo accountFlowVo = accountFlow;

                accountFlowVo.setBalance(accountFlow.getBalance().add(payFlow.getPayerPayAmount().subtract(totalbillAmount)));
                accountFlowVo.setTargetAcc(Constants.BALANCE);
                accountFlowVo.setBillInsertt(totalBillInterest);
                accFlowList.add(accountFlowVo);
            }
                Map<String, String> whiteRepayOrder = accFlowService.getWhiteRepayOrder(Constants.White_Repay_KEY+payFlow.getOrderId());
                BigDecimal billInterest = new BigDecimal(0);  //还款总利息,
                BarDealFlow barDealFlow = accFlowService.getOneBarDealFlow(accountFlow.getUserId());
                for (Bill bill : repaylist) {
                    BarDealFlow newBarDealFlow = new BarDealFlow();
                    BillRepayFlow billRepayFlow = new BillRepayFlow();
                    amount = bill.getBillAmount().subtract(bill.getRepaidAmount());//应还本金
                    interest = bill.getTotalInterest().subtract(bill.getRepaidInterest());//应还利息
                    logger.info("bill={}", JsonUtil.toJson(bill));
                    //更改已经使用的额度t_bal_deal 最后修改时间
                    newBarDealFlow.setUid(Long.valueOf(accountFlow.getUserId()));
                    newBarDealFlow.setUname(payFlow.getUname());
                    //流水金额为本次还款的本金金额
                    newBarDealFlow.setAmount(amount.add(interest));
                    logger.info("payFlow",JsonUtil.toJson(payFlow));
                    newBarDealFlow.setUname(payFlow.getUname());
                    newBarDealFlow.setDirection(Constants.SHORT_TWO);
                    newBarDealFlow.setOrderId(payFlow.getOrderId());
                    newBarDealFlow.setDealType(Constants.SHORT_THREE);
                    newBarDealFlow.setBillFlow(bill.getBillFlow());
                    newBarDealFlow.setShopId(Long.valueOf(Constants.SELF_SHOP_ID));
                    newBarDealFlow.setShopName(Constants.SELF_SHOPNAME);
                    newBarDealFlow.setOccTime(new Date());
                    newBarDealFlow.setUname(payFlow.getUname());
                    //已经使用额额度减去本次还款的本金金额
                    newBarDealFlow.setUsedLimit(barDealFlow.getUsedLimit().subtract(amount));
                    newBarDealFlow.setLastModifyTime(new Date());
                    //把每次查到的BarDealFlow放入list
                    barDealFlowList.add(newBarDealFlow);
                    logger.info("barDealFlow={}", JsonUtil.toJson(barDealFlow));
                    barDealFlow=newBarDealFlow;
                    bill.setRepaidAmount(bill.getBillAmount());
                    bill.setRepaidInterest(bill.getTotalInterest());
                    //如果有次账单有利息billStatus为5逾期还款
                    try {
                        Date time1 = sdf.parse(sdf.format(new Date()));//当前日期
                        Date time2 = sdf.parse(sdf.format(bill.getLastRepaidTime()));//最后需要的还款日期
                        long between_days = (time1.getTime() - time2.getTime()) / (1000 * 3600 * 24);//相隔天数是否大于0 得出是否逾期
                        if (between_days>0) {
                            bill.setBillStatus(Constants.SHORT_FIVE);
                        } else {
                            bill.setBillStatus(Constants.SHORT_TWO);
                        }
                    } catch (Exception e) {
                        logger.error("Repayment time ParseException");
                    }
                    bill.setLastModifyTime(new Date());
                    billList.add(bill);
                    billRepayFlow.setBillId(bill.getId()); //账单id
                    billRepayFlow.setInterestAmount(interest); //还款利息金额
                    billRepayFlow.setOperator(payFlow.getPayerId()); //操作人
                    billRepayFlow.setRepayAmount(amount.add(interest));//还款金额
                    if (whiteRepayOrder == null) {
                        billRepayFlow.setRepayChannel(Short.valueOf("1"));//还款渠道
                    } else {
                        billRepayFlow.setRepayChannel(Short.valueOf(whiteRepayOrder.get("payMode")));
                    }
                    billRepayFlow.setRepayTime(new Date());//还款时间
                    billRepayFlow.setRepayType(Constants.SHORT_ONE);//主动 被动还款
                    billRepayFlow.setPrincipalAmount(amount);//还款本金
                    billRepayFlow.setOrderId(payFlow.getOrderId());
                    billRepayFlowsList.add(billRepayFlow);
                    //添加一条还款记录
                    logger.info("billRepayFlow={}", JsonUtil.toJson(billRepayFlow));
                    billInterest = interest.add(billInterest);
                    logger.info("billInterest={}",billInterest);
                }
                accFlowList.add(accountFlow);
                //把本次还款的总利息存入accountFlow;
                accountFlow.setBillInsertt(billInterest);
                accFlowService.updateWhiteBill(barDealFlowList,billList,billRepayFlowsList,accountFlow,payFlow,accFlowList);


        }else{
            accountFlowService.addAccountFlow(accountFlow);
        }
        }

    /**
     * Description: 白条退款（还款）
     * @author: ZhaoQun
     * date: 2019/5/15 20:11
     */
    @Override
    public void dealWhiteReturnFlow(PayFlow payFlow, AccountFlowVo accFlow) {
        logger.info("dealWhiteReturnPlow's params  accFlow = {}", ItvJsonUtil.toJson(accFlow));
        Long orderId = payFlow.getOrderId();//支付流水对应的订单号
        //根据orderId 查询 t_bar_deal_flow 账单批次号 bill_flow
        String interactionId = payFlow.getInteractionId();//第三方交互订单号
        String lastLetter = interactionId.substring(interactionId.length()-1);
        String billFlow = "";
        if (lastLetter.equalsIgnoreCase("E")){//定金
            billFlow = barDealFlowService.getBillFlowByOrderIdAsc(orderId);
        }
        if (lastLetter.equalsIgnoreCase("R")){//尾款
            billFlow = barDealFlowService.getBillFlowByOrderIdDesc(orderId);
        }

        //该订单未生成账单，直接还款到白条，只生成一条 AccountFlow 、 barDealFlow
        if (StringUtils.isBlank(billFlow)){
            //生成bar_deal_flow
            BarDealFlow barDealFlow = getBarDealFlow(payFlow,billFlow,payFlow.getPayerPayAmount());
            billRepayFlowService.addWhiteReturnFlow(accFlow, null, barDealFlow, null, null,null);
            return;
        }
        if (StringUtils.isNotBlank(billFlow)){
            BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
            BigDecimal balance = accFlow.getBalance();//当前账户余额
            BigDecimal newBalance = balance;//账户余额

            //根据账单批次号 bill_flow 查询账单信息
            BillVo billVo = barDealFlowService.getBillByBillFlow(billFlow);
            Short billStatus = billVo.getBillStatus();//账单状态

            //如果该账单已还款，金额直接全部退款到余额, 只生成一条 AccountFlow
            if(billStatus == 2 || billStatus == 5){
                //退款到账户余额
                newBalance = balance.add(payerPayAmount);
                accFlow.setBalance(newBalance);
                accFlow.setTargetAcc(Constants.BALANCE);
                billRepayFlowService.addWhiteReturnFlow(accFlow, null, null, null, null,null);
                return;
            }
            BigDecimal refundBalance = BigDecimal.ZERO;//余额退款金额
            BigDecimal refundWhite = payerPayAmount;//白条退款金额
            BigDecimal principalAmount = BigDecimal.ZERO;//当次归还本金金额
            BigDecimal interestAmount = BigDecimal.ZERO;//当次归还利息金额
            BigDecimal repaidInterest = billVo.getRepaidInterest();//已还利息
            BigDecimal repaidAmount = billVo.getRepaidAmount();//已还金额
            BigDecimal totalInterest = billVo.getTotalInterest();//总利息
            BigDecimal billAmount = billVo.getBillAmount();
            BigDecimal needBillAmount = billAmount.subtract(repaidAmount);//剩余未还本金
            BigDecimal interest = billVo.getTotalInterest().subtract(repaidInterest);//剩余未还利息
            BigDecimal billTotal = needBillAmount.add(interest);//账单剩余未还总额

            AccountFlowVo balanceAccFlow = null;
            //退款金额 >  账单未还金额  accountFlow 、 barDealFlow、 repayFlow、 billStatus
            if (payerPayAmount.compareTo(billTotal) == 1){
                refundBalance = payerPayAmount.subtract(billTotal);//应退账户余额
                refundWhite = billTotal;//白条退款金额
                principalAmount = needBillAmount;//当次归还本金金额
                interestAmount = interest;//当次归还利息金额
                //退款到账户余额,  accountFlow
                newBalance = balance.add(refundBalance);
                balanceAccFlow = ItvJsonUtil.jsonToObj(ItvJsonUtil.toJson(accFlow),new TypeReference<AccountFlowVo>() {
                });
                balanceAccFlow.setBalance(newBalance);
                balanceAccFlow.setTargetAcc(Constants.BALANCE);
                balanceAccFlow.setAmount(refundBalance);
            }
            //获取白条退款payFlow
            PayFlow whitePayFlow = payFlow;
            whitePayFlow.setPayerPayAmount(refundWhite);
            AccountFlowVo whiteAccFlow = accFlow;
            whiteAccFlow.setAmount(refundWhite);
            Bill bill = new Bill();//账单
            bill.setId(billVo.getId());
            bill.setBillFlow(billFlow);//账单批次号
            //  refundWhite > interest , 还账单利息 + 账单金额
            if (refundWhite.compareTo(interest) == 1) {
                interestAmount = interest;
                principalAmount = refundWhite.subtract(interest);//还利息之后剩余金额
                bill.setRepaidInterest(totalInterest);//已还利息
                whiteAccFlow.setBillInsertt(interest);//本次还款利息
                // needBillAmount > principalAmount 还账单利息 + 部分账单金额
                if (principalAmount.compareTo(needBillAmount) == -1) {
                    bill.setRepaidAmount(repaidAmount.add(principalAmount));//已还金额
                    bill.setBillStatus(Constants.SHORT_THREE);//部分还款
                }
                // needBillAmount = principalAmount 还账单利息 + 账单金额
                if (principalAmount.compareTo(needBillAmount) == 0) {
                    bill.setRepaidAmount(billAmount);//已还金额
                    bill.setBillStatus(Constants.SHORT_TWO);//已还款
                    //如果还款时间已逾期
                    if (new Date().getTime() > billVo.getLastRepaidTime().getTime()){
                        bill.setBillStatus(Constants.SHORT_FIVE);//逾期还款
                    }
                }
            }
            // refundAmount <= interest 只还账单利息
            if (refundWhite.compareTo(interest) != 1) {
                interestAmount = refundWhite;
                principalAmount = BigDecimal.ZERO;//还利息之后剩余金额
                bill.setRepaidInterest(refundWhite.add(repaidInterest));//已还利息
                bill.setBillStatus(Constants.SHORT_THREE);//部分还款
                whiteAccFlow.setBillInsertt(refundWhite);//本次还款利息
            }
            //生成bar_deal_flow
            BigDecimal barDealFlowAmount = principalAmount;//白条流水金额(当次归还本金金额)
            BarDealFlow barDealFlow = getBarDealFlow(whitePayFlow,billFlow,barDealFlowAmount);
            // 生成还款记录t_bill_repay_flow
            String RepayFlowId = idUtils.getId(ID_URL, Constants.ID_WHITE_ORDER_KEY);//还款记录orderId
            BillRepayFlow repayFlow = new BillRepayFlow();
            repayFlow.setBillId(billVo.getId());//账单id
            repayFlow.setRepayAmount(refundWhite);//还款金额
            repayFlow.setPrincipalAmount(principalAmount);//当次归还本金金额
            repayFlow.setInterestAmount(interestAmount);//当次归还利息金额
            repayFlow.setRepayChannel(Constants.SHORT_FIVE);//还款渠道(1余额,2块钱,3银联,4混合,5白条退款)
            repayFlow.setRepayType(Constants.SHORT_TWO);//还款方式(0被动扣款,1主动还款，2白条退款还款）
            repayFlow.setOperator(Constants.SYSTEM_ID);
            repayFlow.setOrderId(Long.valueOf(RepayFlowId));//还款订单号
            repayFlow.setRepayTime(new Date());

            Long userId = Long.valueOf(payFlow.getReceiverUserId());
            billRepayFlowService.addWhiteReturnFlow(whiteAccFlow, balanceAccFlow, barDealFlow, bill, repayFlow,userId);

        }
    }

    /**
     * Description: 根据payFlow 获取barDealFlow
     *
     * @param payFlow
     * @param billFlow
     * @author: ZhaoQun
     * @return: BarDealFlow
     * date: 2019/4/1 17:45
     */
    private BarDealFlow getBarDealFlow(PayFlow payFlow, String billFlow, BigDecimal barDealFlowAmount) {
        Long orderId = payFlow.getOrderId();//支付流水对应的订单号

        //查询用户最多可退白条额度（即已用白条额度）
        Long uid = Long.valueOf(payFlow.getReceiverUserId());
        BigDecimal refundAmount = payFlow.getPayerPayAmount();
        BarDealFlowVo barDealFlowVo = barDealFlowService.getDealFlowByUid(uid);
        BigDecimal usedLimit = barDealFlowVo.getUsedLimit();//已用额度
        //白条流水
        BarDealFlow barDealFlow = new BarDealFlow();
        barDealFlow.setBillFlow(billFlow);//账单批次号
        barDealFlow.setAmount(refundAmount);
        barDealFlow.setUid(uid);
        barDealFlow.setUname(payFlow.getUname());
        barDealFlow.setUsedLimit(usedLimit.subtract(barDealFlowAmount));//已使用额度 = 最后一条流水的已使用额度 - 本次退款金额(含有利息时只操作本金金额)
        barDealFlow.setDirection(Constants.SHORT_TWO);//资金流向(1流出,2流入)
        barDealFlow.setShopId(Long.valueOf(payFlow.getPayerId()));
        barDealFlow.setShopName(payFlow.getPayerName());
        barDealFlow.setOrderId(payFlow.getOrderId());
        barDealFlow.setPayFlowId(payFlow.getPayFlowId());
        barDealFlow.setDealType(Constants.SHORT_TWO);
//        if (payFlow.getTradeType() == Constants.TRADE_TYPE_2){//订单退款
//            barDealFlow.setDealType(Constants.SHORT_TWO);
//        }else if (payFlow.getTradeType() == Constants.TRADE_TYPE_9){//配货退款
//            barDealFlow.setDealType(Constants.SHORT_FOUR);
//        }else if (payFlow.getTradeType() == Constants.TRADE_TYPE_17){//退货退款
//            barDealFlow.setDealType(Constants.SHORT_FIVE);
//        }
        //交易类型(1订单付款,2订单退款,3还款)
        return barDealFlow;
    }


}

