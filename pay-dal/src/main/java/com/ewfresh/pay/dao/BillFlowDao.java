package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BillFlow;

import java.util.List;

public interface BillFlowDao {
    int delBillFlowById(Long id);

    int addBillFlow(BillFlow record);

    BillFlow getBillFlowById(Long id);

    int updateBillFlowById(BillFlow record);

    void addBillFlowBach(List<BillFlow> billFlows);
}