package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.BillFlowDao;
import com.ewfresh.pay.model.BillFlow;
import com.ewfresh.pay.service.BillFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/12.
 */
@Service
public class BillFlowServiceImpl implements BillFlowService {
    @Autowired
    private BillFlowDao billFlowDao;

    @Override
    @Transactional
    public int delBillFlowById(Long id) {
        return billFlowDao.delBillFlowById(id);
    }

    @Override
    @Transactional
    public int addBillFlow(BillFlow record) {
        return billFlowDao.addBillFlow(record);
    }

    @Override
    public BillFlow getBillFlowById(Long id) {
        return billFlowDao.getBillFlowById(id);
    }

    @Override
    @Transactional
    public int updateBillFlowById(BillFlow record) {
        return billFlowDao.updateBillFlowById(record);
    }

    @Override
    @Transactional
    public void addBillFlowBach(List<BillFlow> billFlows) {
        billFlowDao.addBillFlowBach(billFlows);
    }
}
