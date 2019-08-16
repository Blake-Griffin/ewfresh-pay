package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BillRecord;

public interface BillRecordDao {
    int deleteByPrimaryKey(Long id);

    int insert(BillRecord record);

    int insertSelective(BillRecord record);

    BillRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BillRecord record);

    int updateByPrimaryKey(BillRecord record);
}