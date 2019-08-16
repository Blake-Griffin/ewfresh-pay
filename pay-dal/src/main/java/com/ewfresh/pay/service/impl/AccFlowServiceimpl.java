package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.*;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisDao.BillRepayRedisDao;
import com.ewfresh.pay.service.AccFlowService;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.ewfresh.pay.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @param
 * @author
 */
@Service
public class AccFlowServiceimpl implements AccFlowService {
    @Autowired
    private BillRepayRedisDao billRepayRedisDao;
    @Autowired
    private BillDao billDao;
    @Autowired
    private BillRepayFlowDao billRepayFlowDao;
    @Autowired
    private BarDealFlowDao barDealFlowDao;
    @Autowired
    private WhiteBarDao whiteBarDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private ReceivablesUtils receivablesUtils;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;


    @Override
    public List<BillRepayFlow> selectByOrderId(Long orderId) {
        return billRepayFlowDao.selectByOrderId(orderId);
    }

    @Override
    public List<String> getWhiteRepayBill(String key) {
        return billRepayRedisDao.getWhiteRepayBill(key);
    }


    @Override
    public Map<String, String> getWhiteRepayOrder(String key) {
        return billRepayRedisDao.getWhiteRepayOrder(key);
    }

    @Override
    public Bill selectByPrimaryKey(Integer id) {
        return billDao.selectByPrimaryKey(id);
    }

    @Override
    public BarDealFlow getOneBarDealFlow(Long uid) {
        return barDealFlowDao.getOneBarDealFlow(uid);
    }

    @Transactional
    @Override
    public void updateWhiteBill(List<BarDealFlow> barDealFlowList, List<Bill> billList, List<BillRepayFlow> billRepayFlowList, AccountFlowVo accountFlow,PayFlow payFlow,List<AccountFlowVo> accFlowList) {
        for (AccountFlowVo accountFlowVo:accFlowList){
            accountFlowDao.addAccountFlow(accountFlowVo);
        }
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
        Short targetAccType = accountFlow.getTargetAccType();
        Short srcAccType = accountFlow.getSrcAccType();
        if (targetAccType != null && srcAccType != null && targetAccType != 2 && srcAccType != 2) {

            receivablesUtils.addReceivables((AccountFlowVo) accountFlow);
        }
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        barDealFlowDao.addBarDealFlowBatch(barDealFlowList);
        billRepayFlowDao.addBillRepayFlowBatch(billRepayFlowList);
        billDao.batchUpdateBill(billList);
        Integer accountFlowId = accountFlowRedisDao.getAccountFlowId(payFlow.getOrderId());
        accountFlowDao.updateFreezeStatus(accountFlowId, Constants.SHORT_ONE);
        accountFlowRedisDao.setQuotaUnfreeze(accountFlow.getUserId());
    }
}
