package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.AdjustRecord;
import org.apache.ibatis.annotations.Param;


public interface AdjustRecordDao {
    int deleteByPrimaryKey(Integer id);

    int insert(AdjustRecord record);

    int insertSelective(AdjustRecord record);

    AdjustRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdjustRecord record);

    int updateByPrimaryKey(AdjustRecord record);

    //更新状态
    void updateApprStatus(@Param("recordId")Integer recordId,@Param("apprStatus") Short apprStatus);
}