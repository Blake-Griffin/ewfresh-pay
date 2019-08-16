package com.ewfresh.pay.service;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.PayFlow;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description: 对账批次表的服务层
 * @author DuanXiangming
 * Date 2019/6/13 0013
 */
public interface AccCheckService {

    /**
     * Description: 按条件查询所需的对账单
     * @author DuanXiangming
     * @param  paramMap
     * @return java.util.List<com.ewfresh.pay.model.AccCheck>
     * Date    2019/6/13 0013  15:58
     */
    List<AccCheck> getAccCheckByParam(Map<String, Object> paramMap);

    /**
     * Description: 添加一条对账批次表记录
     * @author DuanXiangming
     * @param   accCheck
     * Date    2019/6/18  12:32
     */
    void addAccCheck(AccCheck accCheck);

    /**
     * Description: 查询当前 billDate ,interfaceCode的支付流水
     * @author DuanXiangming
     * @param  billDate        对账日期
     * @param  interfaceCode   渠道编码
     * @return java.util.List<com.ewfresh.pay.model.PayFlow>
     * Date    2019/6/20  14:39
     */
    List<PayFlow> getPayFlowsByItem(Date billDate, String interfaceCode);
}
