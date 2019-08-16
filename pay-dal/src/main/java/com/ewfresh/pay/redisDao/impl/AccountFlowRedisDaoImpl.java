package com.ewfresh.pay.redisDao.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.Emp;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.model.vo.FinishOrderVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Description: 关于账户流水的redisDao层实现类
 * @author DuanXiangming
 * Date 2018/4/11
 */
@Component
public class AccountFlowRedisDaoImpl implements AccountFlowRedisDao {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final  long TIME_LIMIT = 5 * 60 ;//随机数过期时间 5分钟

    private static final  long FREEZEN_INFO = 60 * 60 ;//随机数过期时间 60分钟

    private static final  long FREEZE_TIME = 5 * 60;//冻结记录保存时间 5分钟

    private final String KEY_4 = "{urm}{allUser}";
    private final String PAYKey = "{pay}-{quoUnfreeze}";

    @Override
    public void setCacheQuotaUnfreeze(Long uid,Map map) {
        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();
         stringValue.set(PAYKey + uid,ItvJsonUtil.toJson(map));
    }

    @Override
    public void setQuotaUnfreeze(Long uid) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(PAYKey,uid.toString());
    }

    @Override
    public String getQuotaUnfreeze() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String userId = list.rightPop(PAYKey);
        return StringUtils.isBlank(userId) ? null : ItvJsonUtil.jsonToObj(userId ,String.class);
    }

    @Override
    public AccountFlowVo getAccountFlowByUid(String userId) {

        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();

        String accountFlowStr = stringValue.get(Constants.BANLANCE_KEY + userId);

        if (StringUtils.isBlank(accountFlowStr)){
            return null;
        }
        return ItvJsonUtil.jsonToObj(accountFlowStr,AccountFlowVo .class);
    }


    @Override
    public void setRandomNum(Long userId, Integer randomNum) {
        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();

        stringValue.set(Constants.PAY_RANDOM_NUM + userId, randomNum + "" , TIME_LIMIT , TimeUnit.SECONDS);
    }


    @Override
    public void setTemporaryFrozenAccFlow(Long userId, Long orderId, Map<String, Object> map) {

        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();

        stringValue.set(Constants.TEMPORARY_FROZEN_ACCFLOW + userId + "-" + orderId, ItvJsonUtil.toJson(map));
    }

    @Override
    public Map<String,Object> getTemporaryFrozenAccFlow(Long userId, Long orderId) {

        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();
        String mapStr = stringValue.get(Constants.TEMPORARY_FROZEN_ACCFLOW + userId + "-" + orderId);
        return StringUtils.isBlank(mapStr) ? null : ItvJsonUtil.jsonToObj(mapStr, new TypeReference<Map<String,Object>>(){});
    }

 
    @Override
    public void payflowToAccflow(PayFlow payFlow) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.PAYFLOW_TO_ACCFLOW,ItvJsonUtil.toJson(payFlow));
    }

    @Override
    public PayFlow getPayFlowToAcc() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String payFlowStr = list.rightPop(Constants.PAYFLOW_TO_ACCFLOW);
        return StringUtils.isBlank(payFlowStr) ? null : ItvJsonUtil.jsonToObj(payFlowStr ,PayFlow.class);
    }

    @Override
    public String getUserInfo(Long uid) {
        ValueOperations<String, String> string = redisTemplate.opsForValue();
        String userStr = string.get(Constants.USER_KEY_PREF + uid);
        return userStr;
    }

    @Override
    public void setBalanceByUid(AccountFlowVo accountFlow) {
        ValueOperations<String, String> string = redisTemplate.opsForValue();
        string.set(Constants.BANLANCE_KEY + accountFlow.getUserId(), ItvJsonUtil.toJson(accountFlow));
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.BALANCE_CHANGE,accountFlow.getAccFlowId() + "");

    }

    @Override
    public void setUnfreezeOrderId(String orderid, int accFlowId) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.UNFREEZE_BANLANCE,accFlowId + "");
        if (StringUtils.isNotBlank(orderid)){
            ValueOperations<String, String> string = redisTemplate.opsForValue();
            string.set(Constants.UNFREEZE_BANLANCE_ORDER + orderid, accFlowId + "",FREEZE_TIME,TimeUnit.MINUTES);
        }
}

    @Override
    public String getUnfreezeAccFlowId() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String accFlowId = list.rightPop(Constants.UNFREEZE_BANLANCE);
        return StringUtils.isBlank(accFlowId) ? null : accFlowId;
    }

    @Override
    public List<String> getFinishOrder() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        List<String> orders = null;
        while (true){
            String order = list.rightPop(Constants.FINISH_ORDER_PAY);
            if (StringUtils.isBlank(order)){
                break;
            }
            orders = new ArrayList<>();
            orders.add(order);
        }
        return orders;
    }

    @Override
    public Integer getAccountFlowId(Long orderId) {
        ValueOperations<String, String> string = redisTemplate.opsForValue();
        String accFlowId = string.get(Constants.UNFREEZE_BANLANCE_ORDER + orderId);
        return  StringUtils.isBlank(accFlowId) ? null : Integer.valueOf(accFlowId);
    }

    /**
     * Description: 从redis获取shopName
     * @author: zhaoqun
     * date: 2018/10/25
     */
    @Override
    public String getShopInfoFromRedis(String hashKey, String key) {
        ValueOperations<String, String> string = redisTemplate.opsForValue();
        String all = string.get(hashKey);
        Map<Integer,HashMap> map = ItvJsonUtil.jsonToObj(all,new TypeReference<Map<Integer,HashMap>>(){});
        HashMap shopInfo = map.get(Integer.valueOf(key));
        String info = String.valueOf(shopInfo.get("shopName"));
        return info;
    }

    @Override
    public String getUpdateOrderStatus() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String orderInfo = list.rightPop(Constants.UPDATE_ORDER_STATUS);
        if (StringUtils.isBlank(orderInfo)){
            return null;
        }
        return orderInfo;
    }

    @Override
    public void setQueryBalanceByHAT(String id) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.QUERY_BALANCE_HAT,id);
    }

    @Override
    public List<String> queryBalanceByHAT() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        List<String> ids = new ArrayList<>();
        while (true){
            String id = list.rightPop(Constants.QUERY_BALANCE_HAT);
            if (StringUtils.isBlank(id)){
                break;
            }
            ids.add(id);
        }
        return ids;
    }

    @Override
    public Integer getRandomNum(Long userId) {
        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();
        String randomNum = stringValue.get(Constants.PAY_RANDOM_NUM + userId);
        return StringUtils.isNotBlank(randomNum) ? Integer.valueOf(randomNum) : null;
    }

    /**
     * @Author: LouZiFeng
     * @Description: 根据招商人员id取出招商信息
     * @Param: Idintroducer
     * @Date: 2019/3/20
     */
    @Override
    public Emp getIntroducterInfo(Long idintroducer) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        String string = (String) hash.get(KEY_4, idintroducer + "");
        Emp emp = ItvJsonUtil.jsonToObj(string, Emp.class);
        return emp;
    }

    @Override
    public String getBalanceChangeInfo() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String accId = list.rightPop(Constants.BALANCE_CHANGE);
        return accId;
    }

    @Override
    public void setFreezenInfo(Map<String, Object> map) {
        Object o = map.get(Constants.ORDER_ID);
        Long orderId = o instanceof Long ? ((Long) o) : null;
        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();
        stringValue.set(Constants.BALANCE_FREEZEN_KEY + orderId , ItvJsonUtil.toJson(map), FREEZEN_INFO, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, Object> getFreezenInfo(Long orderId) {
        ValueOperations<String, String> stringValue = redisTemplate.opsForValue();
        String freezenInfoStr = stringValue.get(Constants.BALANCE_FREEZEN_KEY + orderId);
        return StringUtils.isBlank(freezenInfoStr) ? null : ItvJsonUtil.jsonToObj(freezenInfoStr, new HashMap<String,Object>().getClass());
    }

    @Override
    public void deleteFreezeInfo(Long orderId) {
        redisTemplate.delete(Constants.BALANCE_FREEZEN_KEY + orderId);
    }

    @Override
    public void setFinishOrder(FinishOrderVo finishOrderVo) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.FINISH_ORDER_PAY,ItvJsonUtil.toJson(finishOrderVo));
    }
}
