package com.ewfresh.pay.service.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.*;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.BillRepayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisDao.BillRepayRedisDao;
import com.ewfresh.pay.service.BillRepayFlowService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



/**
 * description:
 *
 * @param
 * @author
 */

@Service
public class BillRepayFlowServiceImpl implements BillRepayFlowService {
    private static final Logger logger = LoggerFactory.getLogger(BillRepayFlowServiceImpl.class);
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
    @Value("${http_idgen}")
    private String ID_URL;
    @Override
    public PageInfo<BillRepayFlow> getBillRepayFlow(String billId, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<BillRepayFlow> billRepayFlows = billRepayFlowDao.selectByBillFlow(Integer.valueOf(billId));
        PageInfo<BillRepayFlow> pageInfo = new PageInfo<>(billRepayFlows);
        return pageInfo;
    }

    @Transactional
    @Override
    public int updateBillAddRepayFlow(Bill bill, BillRepayFlow repayFlow) {
        int i = billDao.updateBillSelective(bill);
        billRepayFlowDao.addBillRepayFlow(repayFlow);
        return i;
    }

    /**
     * Description: 处理白条退款数据（还款）
     * @author: ZhaoQun
     *
     * date: 2019/4/1 17:45
     */
    @Transactional
    @Override
    public void addWhiteReturnFlow(AccountFlowVo whiteAccFlow, AccountFlowVo balanceAccFlow, BarDealFlow barDealFlow,
                                   Bill bill, BillRepayFlow repayFlow, Long userId) {
        if (whiteAccFlow != null){
            logger.info("whiteAccFlow = {}", ItvJsonUtil.toJson(whiteAccFlow));
            addAccountFlow(whiteAccFlow);//添加accountFlow
        }
        if (balanceAccFlow != null){
            logger.info("balanceAccFlow = {}", ItvJsonUtil.toJson(balanceAccFlow));
            addAccountFlow(balanceAccFlow);//添加accountFlow
        }
        if (barDealFlow != null){
            barDealFlowDao.addBardealFlow(barDealFlow);//添加白条流水
        }
        if (bill != null && repayFlow != null){
            billDao.updateBillSelective(bill);//修改账单状态
            billRepayFlowDao.addBillRepayFlow(repayFlow);//添加还款记录
            accountFlowRedisDao.setQuotaUnfreeze(userId);//uid 存redis
        }
    }


    /**
     * Description: 添加资金账户流水
     * @author: ZhaoQun
     * @param accFlow
     * date: 2019/4/9 10:09
     */
    private int addAccountFlow(AccountFlowVo accFlow){
        int i = accountFlowDao.addAccountFlow(accFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlow.getUserId() + "");
        Short targetAccType = accFlow.getTargetAccType();
        Short srcAccType = accFlow.getSrcAccType();
        if (targetAccType != null && srcAccType != null && targetAccType != 2 && srcAccType != 2) {
            receivablesUtils.addReceivables(accFlow);
        }
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        return i;
    }

}
