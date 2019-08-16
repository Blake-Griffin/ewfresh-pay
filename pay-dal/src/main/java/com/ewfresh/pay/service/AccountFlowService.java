package com.ewfresh.pay.service;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.vo.AccountFlowListVo;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 关于账户流水的service层接口
 * @author DuanXiangming
 * Date 2018/4/11
 */
public interface AccountFlowService {

    /**
     * Description: 根据uid获取该用户最新的账户流水
     * @author DuanXiangming
     * @param userId   用户ID
     * Date    2018/4/11
     */
    AccountFlowVo getAccountFlowByUid(String userId);
    /**
     * Description: 添加一条冻结余额的账户流水
     * @author DuanXiangming
     * @param accountFlow
     * @param amount
     */
    int addFreezeAccFlow(AccountFlow accountFlow, Long orderId, Map<String, Object> amount);
    /**
     * Description:通过用户id查询资金流水
     * @author wangyaohui
     * @param uid
     */
    PageInfo<AccountFlow> getAccountsByUid( Integer pageSize, Integer pageNumber,Map<String,Object> map);
    /**
     * Description: 添加一条账户资金流水的方法
     * @author wangyaohui
     * @return List<AccountFlowListVo> 余额日志vo类的集合
     * Date    2018/4/24 0024  下午 4:48
     */
    PageInfo<Long>  getAccountFlowList(Integer pageSize,Integer pageNumber,Map<String,String> map);

    /**
     * Description: 添加一条账户资金流水的方法
     * @author DuanXiangming
     * @param  accFlow
     * @return int
     * Date    2018/4/18
     */
    int addAccountFlow(AccountFlow accFlow);

    List<AccountFlowListVo> getAccountFlowIdByParm(List<Long> ids);

    /**
     * Description: 获取要释放的账户流水
     * @author DuanXiangming
     * @param  unfreezeOrderId  要释放的业务单号
     * Date    2018/4/18
     */
    AccountFlow getFreezeAccFlow(String unfreezeOrderId, BigDecimal amount);
    /**
     * Description: 获取要释放的账户流水
     * @author DuanXiangming
     * @param  unfreezeOrderId  要释放的业务单号
     * @param  amount           支付金额
     * Date    2018/4/18
     */
    AccountFlowVo getPayAccountFlow(String unfreezeOrderId);
    /**
     * Description: 获取要释放的账户流水
     * @author DuanXiangming
     * @param  unfreezeOAccFlowId  d冻结流水的ID
     * Date    2018/4/18
     */
    AccountFlowVo getAccFlowById(String unfreezeOAccFlowId);
    /**
     * Description: 释放冻结金额
     * @author DuanXiangming
     * @param  unfreezeOAccFlowId  d冻结流水的ID
     * Date    2018/4/18
     */
    void unfreezeAccFlow(AccountFlowVo unfreezeAccFLow, String unfreezeOAccFlowId);

    /**
     * Description: 获取要释放的账户流水
     * @author: LouZiFeng
     * @param : List
     * date:2018/9/14 14:24:17
     */
    List<Long> getAccountFlowLists(HashMap<String, String> paramMap);


    List<AccountFlow> getAccountsByUidList(HashMap<String, Object> stringObjectHashMap);
    /**
     * Description: 获取要冻结之后的该订单的余额支付的账户流水
     * @author: duanxiangming
     * date:2018/9/14 14:24:17
     */
    AccountFlowVo getPayAccountFlowAfterFreezen(String busiNo, String unfreezeOAccFlowId);
}
