package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.BillRepayFlow;
import com.ewfresh.pay.model.vo.BillRepayFlowVo;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface BillRepayFlowDao {

    int deleteByPrimaryKey(Integer id);

    //添加一条还款纪录信息
    void addBillRepayFlow(BillRepayFlow record);

    int insertSelective(BillRepayFlow record);

    //根据orderId查询还款记录
    List<BillRepayFlow> selectByOrderId(Long orderId);

    int updateByPrimaryKeySelective(BillRepayFlow record);

    int updateByPrimaryKey(BillRepayFlow record);


    List<BillRepayFlow> selectByBillFlow(Integer billFlow);

    //批量插入
    int addBillRepayFlowBatch(List<BillRepayFlow> billList);

    //还款记录
    List<BillRepayFlowVo> getBillRepayByBillid(Map map);

}