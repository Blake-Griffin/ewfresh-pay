package com.ewfresh.pay.redisDao;

import com.ewfresh.pay.model.Emp;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.FinishOrderVo;

import java.util.List;
import java.util.Map;

/**
 * Description:关于账户流水的redisDao层接口
 *
 * @author DuanXiangming
 * Date 2018/4/16 0016
 */
public interface AccountFlowRedisDao {

    /**
      * @author gaoyongqiang
      * @Description 根据uid缓存
      * @Date   2019/4/4 13:41
     **/
    void setCacheQuotaUnfreeze(Long uid,Map map);

    /**
     * @author gaoyongqiang
     * @Description 根据uid存入该用户最新的账户状态
     * @Date   2019/4/4 13:41
      **/
    void setQuotaUnfreeze(Long uid);

    /**
      * @author gaoyongqiang
      * @Description 根据uid获取该用户最新的账户状态
      * @Date   2019/4/4 13:41
     **/
    String getQuotaUnfreeze();

    /**
     * Description: 根据uid获取缓存中该用户最新的账户流水
     *
     * @param userId 用户ID
     *               Date    2018/4/11
     * @author DuanXiangming
     */
    AccountFlowVo getAccountFlowByUid(String userId);

    /**
     * Description:      添加校验支付密码所需的随机数
     *
     * @param userId    用户ID
     * @param randomNum 随机数
     *                  Date    2018/4/17
     * @author DuanXiangming
     */
    void setRandomNum(Long userId, Integer randomNum);

    /**
     * Description: 设置一条流水记录防止重复提交
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param map     Date    2018/4/12
     * @author DuanXiangming
     */
    void setTemporaryFrozenAccFlow(Long userId, Long orderId, Map<String, Object> map);

    /**
     * Description: 获取redis中临时存储的冻结金额
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return java.lang.Double  冻结的值
     * Date    2018/4/12
     * @author DuanXiangming
     */
    Map<String, Object> getTemporaryFrozenAccFlow(Long userId, Long orderId);

    /**
     * Description: 添加一条交易流水记录到redis队列
     *
     * @param payFlow Date    2018/4/12
     * @author DuanXiangming
     */
    void payflowToAccflow(PayFlow payFlow);

    /**
     * Description: 获取redis队列中的支付流水对象
     *
     * @author DuanXiangming
     * Date    2018/4/12
     */
    PayFlow getPayFlowToAcc();

    /**
     * Description:从redis获取user信息的方法
     *
     * @param uid 用户Id
     *            Date    2018/2/1
     * @author DuanXiangming
     */
    String getUserInfo(Long uid);

    /**
     * Description: 在redis中放置余额的方法
     *
     * @param accountFlow Date    2018/4/16
     * @author DuanXiangming
     */
    void setBalanceByUid(AccountFlowVo accountFlow);

    /**
     * Description: 放置需要解冻的订单号
     *
     * @param orderid   订单Id
     * @param accFlowId
     * @author DuanXiangming
     */
    void setUnfreezeOrderId(String orderid, int accFlowId);

    /**
     * Description: 获取需要解冻的订单号
     *
     * @author DuanXiangming
     * Date    2018/4/11
     */
    String getUnfreezeAccFlowId();

    /**
     * Description: 获取订单完结信息通知的方法
     *
     * @author: DuanXiangming
     * date: 2018/5/7 12:20
     */
    List<String> getFinishOrder();

    /**
     * Description: 获取需要解冻的资金账户流水
     *
     * @author DuanXiangming
     * Date    2018/4/11
     */
    Integer getAccountFlowId(Long orderId);

    /**
     * Description: 从redis获取shopName
     *
     * @author: zhaoqun
     * date: 2018/10/25
     */
    String getShopInfoFromRedis(String hashKey, String key);

    /**
     * Description: 从redis获取需要修改订单状态的数据
     *
     * @author: Duanxiangming
     * date: 2018/10/25
     */
    String getUpdateOrderStatus();

    /**
     * Description: redis设置查询HAT余额的方法
     *
     * @author: Duanxiangming
     * date: 2018/10/25
     */
    void setQueryBalanceByHAT(String id);

    /**
     * Description: 获取需要查询HAT余额的店铺id集合
     *
     * @author: Duanxiangming
     * date: 2018/10/25
     */
    List<String> queryBalanceByHAT();

    /**
     * Description:      获取校验支付密码所需的随机数
     *
     * @param userId 用户ID
     *               Date    2018/4/17
     * @author DuanXiangming
     */
    Integer getRandomNum(Long userId);

    /**
     * @Author: LouZiFeng
     * @Description: 根据招商人员id取出招商信息
     * @Param: Idintroducer
     * @Date: 2019/3/20
     */
    Emp getIntroducterInfo(Long idintroducer);
    /**
     * Description: 获取余额变动的信息
     * @author DuanXiangming
     * Date    2018/4/17
     */
    String getBalanceChangeInfo();
    /**
     * Description: 放置订单冻结金额的方法
     * @author DuanXiangming
     * Date    2018/4/17
     */
    void setFreezenInfo(Map<String, Object> map);
    /**
     * Description: 获取订单冻结金额的方法
     * @author DuanXiangming
     * Date    2018/4/17
     */
    Map<String, Object> getFreezenInfo(Long orderId);
    /**
     * Description: 删除订单冻结金额的方法
     * @author DuanXiangming
     * Date    2018/4/17
     */
    void deleteFreezeInfo(Long orderId);

    void setFinishOrder(FinishOrderVo finishOrderVo);
}
