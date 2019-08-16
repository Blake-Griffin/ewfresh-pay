/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/13.
 *  
 **/

package com.ewfresh.pay.service.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.*;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.util.BigInt;

import java.util.*;

/**
 * @descrption TODO
 *  * @author gaoyongqiqng
 *  * @create 2019-03-13
 *  * @Email 1005267839@qq.com
 **/
@Service
public class WhiteBillsServiceImpl implements WhiteBillsService {
    @Autowired
    private BarDealFlowDao barDealFlowDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;
    @Autowired
    private ReceivablesUtils receivablesUtils;
    @Autowired
    private BillDao billDao;
    @Autowired
    private BillIntersetRecordDao billIntersetRecordDao;
    @Autowired
    private WhiteBarDao whiteBarDao;
    @Autowired
    private BillRepayFlowDao billRepayFlowDao;
    @Autowired
    private PayFlowDao payFlowDao;

    @Override
    @Transactional
    public void passivePaymentBatchRecord(List<Bill> upbillList,List<BillRepayFlow>
            addBillRepayFlowList,List<BarDealFlow> addBarDealFlowList,PayFlow payFlow,AccountFlowVo accFlowByPayFlow) {
        barDealFlowDao.addBarDealFlowBatch(addBarDealFlowList);
        billRepayFlowDao.addBillRepayFlowBatch(addBillRepayFlowList);
        billDao.batchUpdateBill(upbillList);

        payFlowDao.addPayFlow(payFlow);
        if (accFlowByPayFlow != null){
            accFlowByPayFlow.setPayFlowId(payFlow.getPayFlowId());
            accountFlowDao.addAccountFlow(accFlowByPayFlow);
            AccountFlowVo accountFlowByUid = accountFlowDao.getAccountFlowByUid(accFlowByPayFlow.getUserId() + "");
            receivablesUtils.addReceivables(accFlowByPayFlow);
            accountFlowRedisDao.setBalanceByUid(accountFlowByUid);
        }
    }

    @Override
    public List<BarDealFlow> getBarDealFlowByPrimaryKey(Date date, Long uid) {
        return barDealFlowDao.getBarDealFlowByPrimaryKey(date, uid);
    }

    @Override
    @Transactional
    public void addBill(Bill bill) {
        billDao.addBill(bill);
    }

    @Override
    @Transactional
    public void updateBillSelective(Bill bill) {
        billDao.updateBillSelective(bill);
    }

    @Override
    @Transactional
    public int addBillIntersetRecord(BillIntersetRecord record) {
        return billIntersetRecordDao.addBillIntersetRecord(record);
    }

    @Override
    public List<Bill> getByRecordingTime() {
        return billDao.getByRecordingTime();
    }

    @Override
    public Map<String, Integer> getperiod(long uid) {
        return billDao.getperiod(uid);
    }

    @Override
    @Transactional
    public int updateWhiteBar(WhiteBar record) {
        return whiteBarDao.updateWhiteBar(record);
    }

    @Override
    @Transactional
    public int updateBarDealFlow(BarDealFlow record) {
        return barDealFlowDao.updateBarDealFlow(record);
    }

//    @Override
//    public List<BarDealFlow> getBarDealFlowByWork(String billFlow) {
//        List<BarDealFlow> list = barDealFlowDao.getBarDealFlow(billFlow);
//        return list;
//    }

    @Override
    public List<BillVo> getBillsByUid(Long uid) {
        Map map = new HashMap();
        List list = new ArrayList();
        list.add(3L);
        list.add(1L);
        map.put("billStatus",list);
        map.put("uid",uid);
        return billDao.getWhiteBillByUid(map);
    }

    @Override
    public Map<String, Long> getUidByBillFlow(String billFlow) {
        return billDao.getUidByBillFlow(billFlow);
    }

    @Override
    public PageInfo<BillRepayFlowVo> getBillRepayByBillid(Integer pageSize, Integer pageNumber, Map map) {
        PageHelper.startPage(pageNumber,pageSize);
        List<BillRepayFlowVo> list = billRepayFlowDao.getBillRepayByBillid(map);
        PageInfo<BillRepayFlowVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public PageInfo<BarDealFlowTwoVo> getBarDealFlow(Integer pageSize,Integer pageNumber,String billFlow) {
        PageHelper.startPage(pageNumber,pageSize);
        List<BarDealFlowTwoVo> list = barDealFlowDao.getBarDealFlow(billFlow);
        PageInfo<BarDealFlowTwoVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public String getUserInfo(Long uid) {
        String userInfo = accountFlowRedisDao.getUserInfo(uid);
        return userInfo;
    }

    @Override
    public PageInfo<BillVo> getWhiteBillByUid(Integer pageSize, Integer pageNumber, Map map) {
        PageHelper.startPage(pageNumber,pageSize);
        List<BillVo> list =  billDao.getWhiteBillByUid(map);
        PageInfo<BillVo> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public List<AutoRepayBillVo> getOvertimeBills(Long userId) {
        return billDao.getOvertimeBills(userId);
    }

//    @Override
//    public PageInfo<BarDealFlowTwoVo> getBillDetailsBybillFlow(Integer pageSize, Integer pageNumber,String billFlow) {
//        PageHelper.startPage(pageNumber,pageSize);
//        List<BarDealFlowTwoVo> list = barDealFlowDao.getBarDealFlowByBillFlow(billFlow);
//        PageInfo<BarDealFlowTwoVo> pageInfo = new PageInfo<>(list);
//        return pageInfo;
//    }

}
