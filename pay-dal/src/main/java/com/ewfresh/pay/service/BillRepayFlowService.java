package com.ewfresh.pay.service;

import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.github.pagehelper.PageInfo;

/**
 * description:
 *
 * @param
 * @author
 */
public interface BillRepayFlowService {

    //查询用户还款信息
    PageInfo<BillRepayFlow> getBillRepayFlow(String billId, Integer pageNumber, Integer pageSize);

    int updateBillAddRepayFlow(Bill bill, BillRepayFlow repayFlow);

    /**
     * Description: 白条退款（还款）
     * @author: ZhaoQun
     *
     * date: 2019/4/1 17:45
     */
    void addWhiteReturnFlow(AccountFlowVo whiteAccFlow, AccountFlowVo balanceAccFlow, BarDealFlow barDealFlow, Bill bill,
                            BillRepayFlow repayFlow, Long userId);
}
