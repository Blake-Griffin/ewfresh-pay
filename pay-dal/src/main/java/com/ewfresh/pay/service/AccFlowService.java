package com.ewfresh.pay.service;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.BillVo;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @param
 * @author  huboyang
 */
public interface AccFlowService {
    //通过还款订单id查询所有还款记录
    List<BillRepayFlow> selectByOrderId(Long orderId);
    //从redis中取出此次还款的账单批次号
    List<String> getWhiteRepayBill(String key);
    //根据uid查询所有账单

    //从redis中取出此次还款的订单详情
    Map<String,String> getWhiteRepayOrder(String key);
    //通过账单id号查询账单
    Bill selectByPrimaryKey(Integer id);
    //通过uid查询最近的白条流水表
    BarDealFlow getOneBarDealFlow(Long uid);
    //添加还款记录 添加白条流水 修改账单状态
    void updateWhiteBill(List<BarDealFlow> barDealFlowList, List<Bill> billList, List<BillRepayFlow> billRepayFlowList, AccountFlowVo accountFlow, PayFlow payFlow,List<AccountFlowVo> accFlowList);

}

