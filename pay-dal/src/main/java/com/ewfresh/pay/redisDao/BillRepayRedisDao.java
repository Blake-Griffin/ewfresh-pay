package com.ewfresh.pay.redisDao;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
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
 * @author huboyang
 */
public interface BillRepayRedisDao {
    void addRepayAmount(Map<String,Object> map);
    void addRepayBill(List<Integer> billFlowList,Long idGenerator);
    List<String> getWhiteRepayBill(String key);
    Map<String,String> getWhiteRepayOrder(String key);
    AccountFlow getAcountFlow(String key);
    void addPayFlow(PayFlow payFlow);

    //redis 存BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    void setBillRepayFlowUpBillVo(String s);

    //redis 取BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    public BillRepayFlowUpBillVo getBillRepayFlowUpBillVo();

    void addRepayOrder(Map<String,Object> map,String ids);
    Map<String,Object> getWhiteRepayOrderByIds(String key);
    Map<String,Object> getWhiteOrder(String orderId);

}
