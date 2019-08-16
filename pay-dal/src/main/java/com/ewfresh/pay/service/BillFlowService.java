package com.ewfresh.pay.service;

import com.ewfresh.pay.model.BillFlow;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/12.
 */
public interface BillFlowService {

    int delBillFlowById(Long id);

    int addBillFlow(BillFlow record);

    BillFlow getBillFlowById(Long id);

    int updateBillFlowById(BillFlow record);

    void addBillFlowBach(List<BillFlow> billFlows);
}
