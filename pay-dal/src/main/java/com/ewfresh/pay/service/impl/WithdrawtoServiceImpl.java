package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AccountFlowDao;
import com.ewfresh.pay.dao.WithdrawApprRecordDao;
import com.ewfresh.pay.dao.WithdrawtoDao;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.WithdrawtosVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/13
 */
@Service
public class WithdrawtoServiceImpl implements WithdrawtoService {

    @Autowired
    private WithdrawtoDao withdrawtoDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private WithdrawApprRecordDao withdrawApprRecordDao;
    @Autowired
    private ReceivablesUtils receivablesUtils;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Override
    @Transactional
    public void addWithdrawtoAndFreezeBalance(AccountFlowVo accountFlow, Withdrawto withdrawto) {
        int withdrawtoId = withdrawtoDao.addWithdrawto(withdrawto);
        accountFlow.setBusiNo(withdrawto.getId().toString());
        accountFlowDescUtil.cassExplainByAcc(accountFlow);
        accountFlowDao.addFreezeAccFlow(accountFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
        receivablesUtils.addReceivables(accountFlowByUid);
        accountFlowRedisService.setBalanceByUid(accountFlowByUid);
    }

    @Override
    public WithdrawtosVo getWithdrawByid(Long id) {

        return withdrawtoDao.getWithdrawtoByid(id);
    }

    @Override
    public List<WithdrawtosVo> getWithdraws(String uname, String nickName, Short status, Short beforeStatus,String startTime, String endTime) {
        return withdrawtoDao.getWithdrawtos(uname,nickName,status,beforeStatus,startTime,endTime);
    }

    @Override
    public List<WithdrawtosVo> getWithdrawByUid(Long uid, Long id) {
        return withdrawtoDao.getWithdrawtoByUid(uid, id);
    }

    @Override
    @Transactional
    public void updateApprStatus(Withdrawto withdrawto, PayFlow payFlow, WithdrawApprRecord withdrawApprRecord, AccountFlow accountFlow) {
        int success = withdrawtoDao.updateApprStatus(withdrawto);
        if (success == Constants.INT_ONE) {
            withdrawApprRecordDao.addWdApprRecord(withdrawApprRecord);
        }
        if(accountFlow != null){
            accountFlowDao.addAccountFlow(accountFlow);
            AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
            receivablesUtils.addReceivables((AccountFlowVo) accountFlow);
            accountFlowRedisService.setBalanceByUid(accountFlowByUid);
        }
    }

    @Override
    @Transactional
    public void cancelWithdrawByid(Withdrawto withdrawto, AccountFlowVo accountFlow) {
        int i = withdrawtoDao.updateCancelWithdrawByid(withdrawto);
        if (i == 0){
            return;
        }
        accountFlowDao.addFreezeAccFlow(accountFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
        receivablesUtils.addReceivables(accountFlow);
        accountFlowRedisService.setBalanceByUid(accountFlowByUid);
    }

    /**
     * Description: 修改提现信息
     * @author: ZhaoQun
     * @param withdrawto
     * @return:
     * date: 2018/8/10 14:48
     */
    @Override
    public void updateWithdrawto(Withdrawto withdrawto) {
        withdrawtoDao.updateWithdrawto(withdrawto);
    }

    @Override
    public Withdrawto getWithdrawtoInfoById(Long id) {
        Withdrawto withdrawto = withdrawtoDao.getWithdrawtoInfoById(id);
        return withdrawto;
    }

    @Override
    @Transactional
    public void checkWithdrawNotAllow(Withdrawto withdrawto, AccountFlow accountFlow) {
        withdrawtoDao.updateApprStatus(withdrawto);
        if(accountFlow != null){
            accountFlowDao.addAccountFlow(accountFlow);
            AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
            receivablesUtils.addReceivables(accountFlowByUid);
            accountFlowRedisService.setBalanceByUid(accountFlowByUid);
        }
    }
}
