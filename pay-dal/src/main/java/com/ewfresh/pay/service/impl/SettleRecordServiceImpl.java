package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AccountFlowDao;
import com.ewfresh.pay.dao.PayFlowDao;
import com.ewfresh.pay.dao.SettleRecordDao;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.SettleRecordService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.ewfresh.pay.util.Constants;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/22 0022
 */
@Service
public class SettleRecordServiceImpl implements SettleRecordService {


    @Autowired
    private SettleRecordDao settleRecordDao;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private PayFlowDao payFlowDao;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private ReceivablesUtils receivablesUtils;

    @Override
    public PageInfo<SettleRecord> getSettleRecordByShopId(Integer shopId, Integer pageSize, Integer pageNumber) {

        PageHelper.startPage(pageNumber,pageSize);
        List<SettleRecord> settleRecords = settleRecordDao.getSettleRecordByShopId(shopId);
        PageInfo<SettleRecord> pageInfo = new PageInfo<>(settleRecords);
        return pageInfo;
    }

    @Override
    @Transactional
    public void addSettleRecord(SettleRecord settleRecord, List<PayFlow> payflows, String batch) {
        settleRecordDao.addSettleRecord(settleRecord);
        payFlowDao.updateSettleStatus(settleRecord.getId(),payflows);
    }

    @Override
    public PageInfo<SettleRecord> getSettleRecords(String shopName, Integer shopId, Short settleStatus, Integer pageSize, Integer pageNumber) {
        PageHelper.startPage(pageNumber,pageSize);
        List<SettleRecord> settleRecords = settleRecordDao.getSettleRecords(shopName,shopId,settleStatus);
        PageInfo<SettleRecord> pageInfo = new PageInfo<>(settleRecords);
        return pageInfo;
    }

    @Override
    @Transactional
    public void apprByBatchNo(SettleRecord settleRecord) {
        settleRecordDao.apprByBatchNo(settleRecord);
        Short settleStatus = settleRecord.getSettleStatus();
        String batchNo = settleRecord.getBatchNo();
        if (settleStatus.shortValue() == Constants.SHORT_ONE.shortValue()){
            //审核通过
            payFlowDao.updateSettleStatusByBatchNo(batchNo,settleStatus,null);
        }
        if (settleStatus.shortValue() == Constants.SHORT_TWO.shortValue()){
            //审核不通过
            payFlowDao.updateSettleStatusByBatchNo(batchNo,Constants.STATUS_0,"");
        }
    }

    @Override
    public SettleRecord getSettleRecordById(Integer id) {

        return settleRecordDao.getSettleRecordById(id);
    }

    @Override
    @Transactional
    public void updateBatchNoById(SettleRecord settleRecord) {
        settleRecordDao.updateBatchNoById(settleRecord);
        Integer id = settleRecord.getId();
        accountFlowRedisService.setQueryBalanceByHAT(id.toString());
    }

    @Override
    @Transactional
    public int updateSettle(SettleRecord settleRecord, PayFlow payFlow, AccountFlowVo accountFlow) {
        int i = settleRecordDao.updateBatchNoById(settleRecord);
        payFlowDao.addPayFlow(payFlow);
        accountFlow.setPayFlowId(payFlow.getPayFlowId());
        accountFlowDao.addAccountFlow(accountFlow);
        receivablesUtils.addReceivables(accountFlow);
        AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId().toString());
        accountFlowRedisService.setBalanceByUid(accountFlowByUid);
        return i;
    }
}
