package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BillIntersetRecord;

public interface BillIntersetRecordDao {
    int deleteByPrimaryKey(Integer id);

    int insert(BillIntersetRecord record);
    //添加利息记录表
    int addBillIntersetRecord(BillIntersetRecord record);
    //根据账单id获取
    BillIntersetRecord getBillIntersetRecordByBillId(Integer id);
    //更新利息记录表
    int updateBillIntersetRecord(BillIntersetRecord record);

    int updateByPrimaryKey(BillIntersetRecord record);
}