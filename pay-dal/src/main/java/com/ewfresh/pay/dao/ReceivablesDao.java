package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.ReceivablesVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReceivablesDao {


    int addReceivables(Receivables receivables);
    //获取最新的财务余额日志
    Receivables getReceivablesByUid(String uid);
    //根据uid获取该按用户的财务余额日志
    List<Receivables> getReceivablesListByUid(@Param("uid") Long uid, @Param("explain")String explain, @Param("amount")String amount, @Param("startTime")String startTime, @Param("endTime")String endTime);

    List<Long> getReceivablesList(@Param("uname")String uname, @Param("receiveTime")String receiveTime);


    List<ReceivablesVo> getReceivablesListByParm(List<Long> ids);
}