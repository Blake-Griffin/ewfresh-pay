package com.ewfresh.pay;

import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * description: 99bill快捷的退单信息操作
 * @author: JiuDongDong
 * date: 2018/10/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test-common.xml"})
public class Bill99QuickRefundOrderInfoRedisTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * Description: 从Redis中删除快捷退款信息
     * @author: JiuDongDong
     * date: 2018/10/10 20:42
     */
    @Test
    public void deleteRefundOrderInfoInRedis() {
        String outRequestNo = "616221";// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String hashKey = "{pay}-{bill99QuickRefundOrderInfo}";
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
//        System.out.println(hashOperations.get("{pay}-{bill99QuickRefundOrderInfo}", outRequestNo));
        hashOperations.delete(hashKey, outRequestNo);
    }

    /**
     * Description: 用于快钱快捷退款时，worker修改订单号的测试。往Redis中放入待修改订单状态的订单号及订单状态。
     * com.ewfresh.pay.worker.UpdateOrderStatusToCancelWorker
     * @author: JiuDongDong
     * date: 2018/12/14 15:48
     */
    @Test
    public void putToUpdateStatusOrderInfo() {
        String orderNo = "616384";
        String token = "61gXj7I3397ef92bI";
        Map<String, String> params = new HashMap<>();
        params.put(Constants.BILL99_ID, orderNo);
        params.put(Constants.BILL99_ORDER_STATUS, 1500 + "");
        params.put(Constants.BILL99_BEFORE_ORDER_STATUS, 2100 + "");
        params.put(Constants.TOKEN, token);// 放置token，用于在worker里http修改订单状态
        params.put(Constants.BILL99_IF_ADD_ORDER_RECORD, Constants.SHORT_ZERO + "");// 是否添加订单操作记录，0否1是
        String json = JsonUtil.toJson(params);

        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(Constants.QUICK_PAY_MODIFY_ORDER_STATUS, orderNo, json);
    }




}
