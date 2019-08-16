package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AccountFlowDao;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.vo.AccountFlowListVo;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.ewfresh.pay.util.Constants;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 关于账户流水的service层实现类
 * @author DuanXiangming
 * Date 2018/4/11
 */
@Service
public class AccountFlowServiceImpl implements AccountFlowService {

    @Autowired
    private AccountFlowDao accountFlowDao;

    @Autowired
    private ReceivablesUtils receivablesUtils;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Override
    public AccountFlowVo getAccountFlowByUid(String userId) {
        return accountFlowDao.getAccountFlowByUid(userId);
    }

    @Override
    @Transactional
    public int addFreezeAccFlow(AccountFlow accountFlow, Long orderId, Map<String, Object> map) {
        int accFlow = accountFlowDao.addFreezeAccFlow(accountFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId() + "");
        receivablesUtils.addReceivables(accountFlowByUid);
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        accountFlowRedisDao.setUnfreezeOrderId(orderId.toString(),accountFlow.getAccFlowId());
        Integer accFlowId = accountFlow.getAccFlowId();
        map.put(Constants.ACC_FLOW_ID,accFlowId);
        accountFlowRedisService.setFreezenInfo(map);
        return accFlow;
    }

    @Override
    public PageInfo<AccountFlow> getAccountsByUid(Integer pageSize,Integer pageNumber,Map<String,Object> map) {
        PageHelper.startPage(pageNumber,pageSize);
        List<AccountFlow> accountsByUid = accountFlowDao.getAccountsByUid(map);
        PageInfo<AccountFlow> pageInfo = new PageInfo<>(accountsByUid);
        return pageInfo;
    }


    @Override
    @Transactional
    public int addAccountFlow(AccountFlow accFlow) {
        int i = accountFlowDao.addAccountFlow(accFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlow.getUserId() + "");
        Short targetAccType = accFlow.getTargetAccType();
        Short srcAccType = accFlow.getSrcAccType();
        Short accType = accFlow.getAccType();
        Short busiType = accFlow.getBusiType();
        if (targetAccType != null && srcAccType != null && targetAccType != 2 && srcAccType != 2){
            receivablesUtils.addReceivables(accountFlowByUid);
        }
        String busiNo = accFlow.getBusiNo();
        if (accType == Constants.ACC_TYPE_4 && (busiType == Constants.BUSI_TYPE_3 || busiType == Constants.BUSI_TYPE_9)){
            Integer accflowId = accountFlowRedisDao.getAccountFlowId(Long.valueOf(busiNo));
            accountFlowDao.updateFreezeStatus(accflowId, Constants.SHORT_ONE);
        }
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        if (accType == Constants.ACC_TYPE_4 && busiType == Constants.BUSI_TYPE_3){
            accountFlowRedisDao.deleteFreezeInfo(Long.valueOf(accFlow.getBusiNo()));
        }
        return i;
    }

    @Override
    public PageInfo<Long> getAccountFlowList(Integer pageSize,Integer pageNumber,Map<String,String> map) {
        PageHelper.startPage(pageNumber,pageSize);
        List<Long> accountFlowList = accountFlowDao.getAccountFlowList(map);
        PageInfo<Long> pageInfo = new PageInfo<>(accountFlowList);
        return pageInfo;
    }

    @Override
    public List<AccountFlowListVo> getAccountFlowIdByParm(List<Long> ids){
        List<AccountFlowListVo> accountFlowIdByParm = accountFlowDao.getAccountFlowIdByParm(ids);
        return accountFlowIdByParm;
    }

    @Override
    public AccountFlow getFreezeAccFlow(String unfreezeOrderId, BigDecimal amount) {
        return accountFlowDao.getFreezeAccFlow(unfreezeOrderId,amount);
    }

    @Override
    public AccountFlowVo getPayAccountFlow(String unfreezeOrderId) {
        return accountFlowDao.getPayAccountFlow(unfreezeOrderId);
    }

    @Override
    public AccountFlowVo getAccFlowById(String unfreezeOAccFlowId) {
        return accountFlowDao.getAccFlowById(unfreezeOAccFlowId);
    }

    @Override
    @Transactional
    public void unfreezeAccFlow(AccountFlowVo unfreezeAccFLow, String unfreezeOAccFlowId) {
        accountFlowDao.updateFreezeStatus(Integer.valueOf(unfreezeOAccFlowId), Constants.SHORT_TWO);
        accountFlowDao.addAccountFlow(unfreezeAccFLow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(unfreezeAccFLow.getUserId() + "");
        receivablesUtils.addReceivables(unfreezeAccFLow);
        accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        accountFlowRedisDao.deleteFreezeInfo(Long.valueOf(unfreezeAccFLow.getBusiNo()));
    }

    @Override
    public List<Long> getAccountFlowLists(HashMap<String, String> paramMap) {
        List<Long> accountFlowList = accountFlowDao.getAccountFlowList(paramMap);
        return accountFlowList;
    }


    @Override
    public List<AccountFlow> getAccountsByUidList(HashMap<String, Object> stringObjectHashMap) {
        List<AccountFlow> accountFlowLists = accountFlowDao.getAccountsByUidList(stringObjectHashMap);
        return accountFlowLists;
    }

    @Override
    public AccountFlowVo getPayAccountFlowAfterFreezen(String busiNo, String unfreezeOAccFlowId) {
        return accountFlowDao.getPayAccountFlowAfterFreezen(busiNo,unfreezeOAccFlowId);
    }
}
