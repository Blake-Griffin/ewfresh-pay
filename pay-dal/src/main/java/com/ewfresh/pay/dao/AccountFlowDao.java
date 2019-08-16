package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.vo.AccountFlowListVo;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AccountFlowDao {
    int deleteByPrimaryKey(Integer accFlowId);

    int insert(AccountFlow record);

    int insertSelective(AccountFlow record);

    AccountFlow selectByPrimaryKey(Integer accFlowId);

    int updateByPrimaryKeySelective(AccountFlow record);

    int updateByPrimaryKey(AccountFlow record);

    AccountFlowVo getAccountFlowByUid(String userId);

    int addFreezeAccFlow(AccountFlow record);
    /**
     * Description:通过用户id查询资金流水
     * @author wangyaohui
     * @param map
     *
     */
    List<AccountFlow> getAccountsByUid(Map<String,Object> map);
    /**
     * Description: 添加一条账户资金流水的方法
     * @author DuanXiangming
     * @param  accFlow
     * @return int
     * Date    2018/4/24 0024  下午 4:48
     */
    int addAccountFlow(AccountFlow accFlow);
    /**
     * Description: 添加一条账户资金流水的方法
     * @author wangyaohui
     * @return List<AccountFlowListVo> 余额日志vo类的集合
     * Date    2018/4/24 0024  下午 4:48
     */
    List<Long>  getAccountFlowList(Map<String,String> map);

    List<AccountFlowListVo>     getAccountFlowIdByParm( List<Long> ids);
    /**
     * Description: 获取要释放的账户流水
     * @author DuanXiangming
     * @param  unfreezeOrderId  要释放的业务单号
     * @param amount
     */
    AccountFlow getFreezeAccFlow(@Param("unfreezeOrderId")String unfreezeOrderId, @Param("amount")BigDecimal amount);

    List<AccountFlowVo> getAll();
    /**
     * Description: 获取要释放的账户流水
     * @author DuanXiangming
     * @param  accflowId  要释放的业务单号
     * Date    2018/4/18
     */
    int updateFreezeStatus( @Param("accFlowId")Integer accflowId, @Param("unfreezeStatus")Short unfreezeStatus);

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
     * Description:通过用户id查询资金流水
     * @author louzifeng
     *
     */
    List<AccountFlow> getAccountsByUidList(HashMap<String, Object> stringObjectHashMap);
    /**
     * Description: 获取要冻结之后的该订单的余额支付的账户流水
     * @author: duanxiangming
     * date:2018/9/14 14:24:17
     */
    AccountFlowVo getPayAccountFlowAfterFreezen(@Param("busiNo") String busiNo, @Param("unfreezeOAccFlowId")String unfreezeOAccFlowId);
}