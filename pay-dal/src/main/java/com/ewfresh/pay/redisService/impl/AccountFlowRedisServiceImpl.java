package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.Emp;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.FinishOrderVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Description:关于账户流水的redisService层实现类
 *
 * @author DuanXiangming
 * Date 2018/4/11
 */
@Component
public class AccountFlowRedisServiceImpl implements AccountFlowRedisService {

    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;

    @Override
    public void setCacheQuotaUnfreeze(Long uid,Map map) {
        accountFlowRedisDao.setCacheQuotaUnfreeze(uid,map);
    }

    @Override
    public void setQuotaUnfreeze(Long uid) {
        accountFlowRedisDao.setQuotaUnfreeze(uid);
    }

    @Override
    public String getQuotaUnfreeze() {
        return accountFlowRedisDao.getQuotaUnfreeze();
    }

    @Override
    public AccountFlowVo getAccountFlowByUid(String userId) {
        return accountFlowRedisDao.getAccountFlowByUid(userId);
    }

    @Override
    public void setUnfreezeOrderId(String orderid, Integer accFlowId) {
        accountFlowRedisDao.setUnfreezeOrderId(orderid, accFlowId);
    }

    @Override
    public String getUnfreezeAccFlowId() {
        return accountFlowRedisDao.getUnfreezeAccFlowId();
    }

    @Override
    public void setRandomNum(Long userId, Integer randomNum) {
        accountFlowRedisDao.setRandomNum(userId, randomNum);
    }

    @Override
    public Map<String, Object> getTemporaryFrozenAccFlow(Long userId, Long orderId) {
        return accountFlowRedisDao.getTemporaryFrozenAccFlow(userId, orderId);
    }

    @Override
    public PayFlow getPayFlowToAcc() {
        return accountFlowRedisDao.getPayFlowToAcc();
    }

    @Override
    public String getUserInfo(Long uid) {
        return accountFlowRedisDao.getUserInfo(uid);
    }

    @Override
    public void setBalanceByUid(AccountFlowVo accountFlow) {
        accountFlowRedisDao.setBalanceByUid(accountFlow);
    }

    @Override
    public List<String> getFinishOrder() {
        return accountFlowRedisDao.getFinishOrder();
    }

    /**
     * Description: 从redis获取shopName
     *
     * @author: zhaoqun
     * date: 2018/10/25
     */
    @Override
    public String getShopInfoFromRedis(String hashKey, String key) {
        return accountFlowRedisDao.getShopInfoFromRedis(hashKey, key);
    }

    @Override
    public String getUpdateOrderStatus() {
        return accountFlowRedisDao.getUpdateOrderStatus();
    }

    @Override
    public void setQueryBalanceByHAT(String id) {
        accountFlowRedisDao.setQueryBalanceByHAT(id);
    }

    @Override
    public List<String> queryBalanceByHAT() {
        return accountFlowRedisDao.queryBalanceByHAT();
    }

    @Override
    public Integer getRandomNum(Long userId) {
        return accountFlowRedisDao.getRandomNum(userId);
    }

    /**
     * @Author: LouZiFeng
     * @Description: 根据招商人员id取出招商信息
     * @Param:Idintroducer
     * @Date: 2019/3/20 11:01
     */
    @Override
    public Emp getIntroducterInfo(Long Idintroducer) {
        return accountFlowRedisDao.getIntroducterInfo(Idintroducer);
    }

    @Override
    public String getBalanceChangeInfo() {
        return accountFlowRedisDao.getBalanceChangeInfo();
    }

    @Override
    public void setFreezenInfo(Map<String, Object> map) {
        accountFlowRedisDao.setFreezenInfo(map);
    }

    @Override
    public Map<String, Object> getFreezenInfo(Long orderId) {
        return accountFlowRedisDao.getFreezenInfo(orderId);
    }

    @Override
    public void setFinishOrder(FinishOrderVo finishOrderVo) {
        accountFlowRedisDao.setFinishOrder(finishOrderVo);
    }
}
