package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;

/**
 * description:
 *
 * @param
 * @author huboyang
 */
public interface AccFlowManager {
    //添加accFlow
    void addAccFlow (AccountFlowVo accountFlow, PayFlow payFlow);

    /**
     * Description: 白条退款（还款）
     * @author: ZhaoQun
     * date: 2019/5/15 20:11
     */
    void dealWhiteReturnFlow(PayFlow payFlow, AccountFlowVo accFlow);
}
