package com.ewfresh.pay.service;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.BillRepayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillVo;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BillRepayService {
   //通过客户uid查询到用户的账单批号
    List<BarDealFlow> getBillFlowByUid(Long uid);
    //通过uid得到账单表对象
    List<BillVo> getBillByUid(Long id);
    //通过账单批号查询到账单金额
    BillVo getBillByBillId(Integer billId);
    //添加一条冻结余额流水信息
    void addAccountFlow(AccountFlowVo accountFlow, Map<String,Object> map , List<Integer> list, Long whiteOrderId,String idGenerator);
    //查询客户最近的流水信息
    AccountFlow getAccountFlow(String Uid);
    //存入一条白条还款信息
    void addWhiteOrder( Map<String,Object> map ,List<Integer> list,Long whiteOrderId,String idGenerator);
    //根据账单批次号存入一条还款的订单
    void addWhiteOrderByIds(Map<String,Object> map ,String ids);
    //根据账单批次号取出一条还款订单
    Map<String,Object> getWhiteOrderIByIds(String key);

    Map<String,Object> getWhiteOrder(String orderId);
    //根据uid查询所有未还款账单
    List<BillVo> getBillsByUid(Long uid);


}
