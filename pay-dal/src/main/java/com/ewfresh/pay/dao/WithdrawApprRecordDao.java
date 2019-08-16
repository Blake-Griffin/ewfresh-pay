package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.vo.WithdrawtosVo;

import java.util.List;

public interface WithdrawApprRecordDao {
    int deleteByPrimaryKey(Long id);

    //添加提现审批记录  zhaoqun
    int addWdApprRecord(WithdrawApprRecord record);

    //获取最新beforeStatus
    Long getBeforeStatus(Long withdtoId);

    //获取最新一条审核记录
    WithdrawApprRecord getWithdrawApprRecord(Long id);

    //获取某提现记录的审核记录
    List<WithdrawApprRecord> getWdApprRecordList(Long id);

    int updateByPrimaryKeySelective(WithdrawApprRecord record);

    int updateByPrimaryKey(WithdrawApprRecord record);

}