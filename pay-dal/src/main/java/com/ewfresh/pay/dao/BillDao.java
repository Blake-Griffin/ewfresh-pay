package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.BillVo;

import java.util.List;

import java.util.Map;


public interface BillDao {
    int deleteByPrimaryKey(Integer id);
    //新增账单
    int addBill(Bill record);

    int insert(Bill record);

    Bill selectByPrimaryKey(Integer id);
    //更新账单
    int updateBillSelective(Bill record);

    int updateByPrimaryKey(Bill record);

    //根据账单批次号查询 账单
    Bill getBillBybillFlow(String billFlow);

    //根据用户id查询 账单
    List<BillVo> getBillByUid(Long uid);

    BillVo getBillByBillFlow(String billFlow);
    //根据账单批次号查询账单金额


    BillVo getBillByBillId(Integer billId);
    //删除一条账单信息
    void delBill(String billFlow);
    //根据uid查看用户账单
    List<Bill> selectBillByUid(String uid);

    //得到未还款账单
    List<Bill> getByRecordingTime();

    //根据用户id获取还款账期
    Map<String,Integer> getperiod(long uid);
    //获取账期
    List<BillVo> getWhiteBillByUid(Map map);
    //根据账单批次号查询用户id
    Map<String, Long> getUidByBillFlow(String billFlow);
    //批量更新账单
    void batchUpdateBill(List<Bill> billList);
    //DXM 根据用户ID获取用户超时未还款的账单
    List<AutoRepayBillVo> getOvertimeBills(Long uid);
    //批量更新账单
    void updateBills(List<AutoRepayBillVo> billVos);
    List<BillVo> getBillsByUid(Long uid);
}