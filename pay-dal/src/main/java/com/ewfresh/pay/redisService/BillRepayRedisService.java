package com.ewfresh.pay.redisService;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillRepayFlowUpBillVo;
import com.ewfresh.pay.model.vo.BillVo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @param
 * @author
 */

public interface BillRepayRedisService {
    //存储还款金额
    void setRepTotalbillAmount(HashMap<String, Object> map);

    //redis取出还款订单信息
    Map<String,String> getRrpayOrder(String key);
    //redis取出 冻结金额流水信息
    AccountFlow getAcountFlow(String key);
    void  addPayFlow(PayFlow payFlow, AccountFlowVo accountFlowVo, List<Integer> list, Long whiteOrderId,String idGenerator);

    //redis 存BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    void setBillRepayFlowUpBillVo(String s);

    //redis 取BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    public BillRepayFlowUpBillVo getBillRepayFlowUpBillVo();
}
