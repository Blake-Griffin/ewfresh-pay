package com.ewfresh.pay.service.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.*;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.OnlineRechargeFlowVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.ewfresh.pay.util.Constants;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by 王耀辉 on 2018/4/12.
 */
@Service
public class PayFlowServiceImpl implements PayFlowService {
    @Autowired
    private PayFlowDao payFlowDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;
    @Autowired
    private WithdrawtoDao withdrawtoDao;
    @Autowired
    private  ReceivablesUtils receivablesUtils;
    @Autowired
    private BarDealFlowDao barDealFlowDao;
    @Autowired
    private BillRepayFlowDao billRepayFlowDao;
    @Autowired
    private BillDao billDao;
    @Override
    public List<PayFlow> getPayFlowByItems(Long orderId, String interactionId, Short tradeType) {
        return payFlowDao.getPayFlowByItems(orderId,interactionId,tradeType);
    }

    @Override
    public String getChannelTypeByChannelFlowId(String channelFlowId) {
        return payFlowDao.getChannelTypeByChannelFlowId(channelFlowId);
    }

    @Override
    public Date getSuccessTimeByInteractionId(String interactionId, Short tradeType) {
        return payFlowDao.getSuccessTimeByInteractionId(interactionId, tradeType);
    }

    @Override
    public Date getSuccessTimeByChannelFlowId(String channelFlowId) {
        return payFlowDao.getSuccessTimeByChannelFlowId(channelFlowId);
    }

    @Override
    @Transactional
    public int addPayFlow(PayFlow payFlow) {
        int row = payFlowDao.addPayFlow(payFlow);
        accountFlowRedisDao.payflowToAccflow(payFlow);
        return row;
    }

    @Override
    public PayFlow getPayFlowById(String channelFlowId) {
        return payFlowDao.getPayFlowById(channelFlowId);
    }

    @Override
    @Transactional
    public int updatePayFlowById(PayFlow record) {
        return payFlowDao.updatePayFlowById(record);
    }

    @Override
    @Transactional
    public void addBatch(List<Map<String, Object>> list) {
        payFlowDao.addBatch(list);
        for (Map<String, Object> map : list) {
            String s = ItvJsonUtil.toJson(map);
            PayFlow payFlow = ItvJsonUtil.jsonToObj(s, new PayFlow().getClass());
            if (payFlow.getChannelCode().equals(Constants.UID_BALANCE) && payFlow.getTradeType().shortValue() == Constants.BUSI_TYPE_1.shortValue()){
                //是余额和三方混合支付,需要更余额的冻结状态
                Long orderId = payFlow.getOrderId();
                Integer accountFlowId = accountFlowRedisDao.getAccountFlowId(orderId);
                accountFlowDao.updateFreezeStatus(accountFlowId, Constants.SHORT_ONE);
            }
            accountFlowRedisDao.payflowToAccflow(payFlow);
        }
    }

    @Override
    public PayFlow getPayFlowPartById(String channelFlowId) {
        return payFlowDao.getPayFlowPartById(channelFlowId);
    }

    @Override
    public PayFlow getPayFlowPartByPayFlowId(Integer payFlowId) {
        return payFlowDao.getPayFlowPartByPayFlowId(payFlowId);
    }

    @Override
    public PayFlow getPayFlowByInteractionId(String interactionId) {
        // 根据第三方交互订单号查询流水信息（北京银行专用）     jiudongdong
        PayFlow payFlow = payFlowDao.getPayFlowByInteractionId(interactionId);
        return payFlow;
    }

    @Override
    public String getChannelFlowIdByOrderNo(Long orderId, String channelCode, Short tradeType, Short status) {
        return payFlowDao.getChannelFlowIdByOrderNo(orderId, channelCode, tradeType, status);
    }

    @Override
    public String getReturnFlowIdByInteractionId(String interactionId) {
        return payFlowDao.getReturnFlowIdByInteractionId(interactionId);
    }

    @Override
    public PayFlow getFlowIdByRefundSequence(String channelFlowId) {
        return payFlowDao.getFlowIdByRefundSequence(channelFlowId);
    }

    @Override
    public int updatePayFlow(PayFlow payFlow) {
        return payFlowDao.updatePayFlow(payFlow);
    }

    @Override
    public List<PayFlow> getPayFlowsByOrderId(Long orderId) {
        return payFlowDao.getPayFlowsByOrderId(orderId);
    }

    @Override
    public List<PayFlow> getBill99PayFlowsByOrderId(Long orderId) {
        return payFlowDao.getBill99PayFlowsByOrderId(orderId);
    }

    @Override
    @Transactional
    public int withdrawto(PayFlow payFlow, AccountFlowVo accFlow, Withdrawto withdrawto) {
        int flag =  withdrawtoDao.updateApprStatus(withdrawto);
        if (flag != 1){
            return 0;
        }
        payFlowDao.addPayFlow(payFlow);
        accFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accFlow);
        AccountFlowVo accountFlow = accountFlowDao.getAccountFlowByUid(accFlow.getUserId() + "");
        receivablesUtils.addReceivables(accFlow);
        accountFlowRedisDao.setBalanceByUid(accountFlow);
        return flag;
    }

    @Override
    public void reCharge(PayFlow payFlow, AccountFlowVo accFlow) {
        payFlowDao.addPayFlow(payFlow);
        accFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accFlow);
        AccountFlowVo accountFlow = accountFlowDao.getAccountFlowByUid(accFlow.getUserId() + "");
        receivablesUtils.addReceivables(accFlow);
        accountFlowRedisDao.setBalanceByUid(accountFlow);
    }

    @Override
    @Transactional
    public void abatementBalance(PayFlow payFlow, AccountFlowVo accFlowByPayFlow) {
        payFlowDao.addPayFlow(payFlow);
        accFlowByPayFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accFlowByPayFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlowByPayFlow.getUserId() + "");
        receivablesUtils.addReceivables(accFlowByPayFlow);
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
    }

    @Override
    public List<PayFlow> getPayFlowByOrderId(Long orderId) {
        return payFlowDao.getPayFlowByOrderId(orderId);
    }


    @Override
    @Transactional
    public int addPayFlowAndAccFlow(Withdrawto withdrawto, PayFlow payFlow, AccountFlowVo accFlowByPayFlow) {
        withdrawtoDao.updateWithdrawto(withdrawto);
        int num = payFlowDao.addPayFlow(payFlow);
        accFlowByPayFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accFlowByPayFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlowByPayFlow.getUserId() + "");
        receivablesUtils.addReceivables(accFlowByPayFlow);
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        return num;
    }

    @Override
    @Transactional
    public void payByBalance(PayFlow payFlow, AccountFlowVo accFlowByPayFlow) {
        payFlowDao.addPayFlow(payFlow);
        accFlowByPayFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accFlowByPayFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlowByPayFlow.getUserId() + "");
        receivablesUtils.addReceivables(accFlowByPayFlow);
        Long orderId = payFlow.getOrderId();
        Integer accflowId = accountFlowRedisDao.getAccountFlowId(orderId);
        accountFlowDao.updateFreezeStatus(accflowId, Constants.SHORT_ONE);
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        accountFlowRedisDao.deleteFreezeInfo(orderId);
    }

    /**
     * Description: 获取在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param pageSize
     * @param pageNumber
     * @param map
     * date: 2018/9/5 15:29
     */
    @Override
    public PageInfo<OnlineRechargeFlowVo> getOnlineRechargeList(Integer pageSize, Integer pageNumber, Map<String, Object> map) {
        PageHelper.startPage(pageNumber, pageSize);
        List<OnlineRechargeFlowVo> list = payFlowDao.getOnlineRechargeList(map);
        PageInfo<OnlineRechargeFlowVo> pageInfo = new PageInfo<>(list);
        return pageInfo;

    }

    @Override
    public List<OnlineRechargeFlowVo> exportOnlineRechargeList(Map<String, Object> paramMap) {
        List<OnlineRechargeFlowVo> list = payFlowDao.getOnlineRechargeList(paramMap);
        return list;
    }

    @Override
    public List<PayFlow> getBalancePayFlow(Long orderId) {
        return payFlowDao.getBalancePayFlow(orderId);
    }

    @Override
    public List<PayFlow> getRefundBalancePayFlow(Long orderId, String interactionId) {
        return payFlowDao.getRefundBalancePayFlow(orderId,interactionId);
    }

    @Override
    public List<PayFlow> getRefundFlowByOrderIdAndChannel(Long orderId, String channelCode) {
        return payFlowDao.getRefundFlowByOrderIdAndChannel(orderId , channelCode);
    }

    @Override
    @Transactional
    public void addPayFlows(List<PayFlow> payFlows) {
        payFlowDao.addPayFlows(payFlows);
        for (PayFlow payFlow : payFlows) {
            accountFlowRedisDao.payflowToAccflow(payFlow);
        }
    }


    @Override
    public PayFlow getPayFlowPartByIdAndTradeType(String channelFlowId, Short tradeType) {

        return payFlowDao.getPayFlowPartByIdAndTradeType(channelFlowId,tradeType);
    }

    @Override
    public PageInfo<PayFlow> getSettlePayflows(Integer shopId, Integer pageSize, Integer pageNumber, String batchNo, Short orderStatus, Short settleStatus, String successTime) {
        PageHelper.startPage(pageNumber,pageSize);
        List<PayFlow> settlePayflows = payFlowDao.getSettlePayflows(shopId, batchNo, orderStatus, settleStatus, successTime);
        PageInfo<PayFlow> pageInfo = new PageInfo<>(settlePayflows);
        return pageInfo;
    }

    @Override
    public List<PayFlow> getAllSettlePayflows(Integer shopId, Short orderStatus, Short settleStatus, String successTime) {
        return payFlowDao.getSettlePayflows(shopId,"",orderStatus,settleStatus,successTime);
    }

    @Override
    public List<PayFlow> getPayflowsByIds(List<Integer> payflows) {
        return payFlowDao.getPayflowsByIds(payflows);
    }

    @Override
    public PageInfo<PayFlow> getSettleRecordsByBatchNo(String batchNo, Integer pageSize, Integer pageNumber) {
        PageHelper.startPage(pageNumber,pageSize);
        List<PayFlow> settlePayflows = payFlowDao.getSettleRecordsByBatchNo(batchNo);
        PageInfo<PayFlow> pageInfo = new PageInfo<>(settlePayflows);
        return pageInfo;
    }

    @Override
    public void updateOrderStatus(String chanelFlowId) {
        payFlowDao.updateOrderStatus(chanelFlowId);
    }

    @Override
    public int ifUseSpecialBank(Long orderId) {
        return payFlowDao.ifUseSpecialBank(orderId);
    }

    //白条支付添加流水   zhaoqun
    @Override
    @Transactional
    public int payByWhite(PayFlow payFlow, AccountFlowVo accFlowByPayFlow, BarDealFlow barDealFlow, PayFlow balancePayFlow, AccountFlowVo balanceAccFlow) {
        int num = payFlowDao.addPayFlow(payFlow);
        Integer payFlowId = payFlow.getPayFlowId();
        if (accFlowByPayFlow != null){
            accFlowByPayFlow.setPayFlowId(payFlowId);
            accountFlowDao.addAccountFlow(accFlowByPayFlow);
            AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlowByPayFlow.getUserId() + "");
            receivablesUtils.addReceivables(accFlowByPayFlow);
            accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        }
        if (barDealFlow != null){
            barDealFlow.setPayFlowId(payFlowId);
            barDealFlowDao.addBardealFlow(barDealFlow);
        }
        if (balancePayFlow != null && balanceAccFlow != null){
            payFlowDao.addPayFlow(balancePayFlow);
            balanceAccFlow.setPayFlowId(balancePayFlow.getPayFlowId());
            accountFlowDao.addAccountFlow(balanceAccFlow);
            AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(balanceAccFlow.getUserId() + "");
            receivablesUtils.addReceivables(balanceAccFlow);
            accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        }
        return num;
    }

    @Override
    @Transactional
    public void addPayFlowAndAccAndBdfAndBrf(PayFlow payFlow, AccountFlowVo accFlow, List<BarDealFlow> barDealFlow, List<BillRepayFlow> billRepayFlows, List<AutoRepayBillVo> billVos) {
        payFlowDao.addPayFlow(payFlow);
        accountFlowDao.addAccountFlow(accFlow);
        receivablesUtils.addReceivables(accFlow);
        barDealFlowDao.addBardealFlows(barDealFlow);
        billRepayFlowDao.addBillRepayFlowBatch(billRepayFlows);
        billDao.updateBills(billVos);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlow.getUserId().toString());
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        accountFlowRedisDao.setQuotaUnfreeze(accFlow.getUserId());
    }

    @Override
    public List<PayFlow> getPayFlowsByOrderIdAndTradeType(String orderId, Short... tradeTypes ) {
        return payFlowDao.getPayFlowsByOrderIdAndTradeType(orderId, tradeTypes);
    }
    @Override
    public Map<String, Short> checkPayFlowStatus(Short tradeType, String... channelFlowIds) {
        return payFlowDao.checkPayFlowStatus(tradeType , channelFlowIds);
    }
}
