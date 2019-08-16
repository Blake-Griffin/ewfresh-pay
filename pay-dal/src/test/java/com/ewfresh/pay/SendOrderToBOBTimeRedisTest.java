package com.ewfresh.pay;

import com.ewfresh.pay.util.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * description:
 *      商户发送订单信息到BOB的时间操作
 * @author: JiuDongDong
 * date: 2018/6/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test-common.xml"})
public class SendOrderToBOBTimeRedisTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    /**
     * Description: 商户发送订单信息到BOB的时间放入Redis
     * @author: JiuDongDong
     * date: 2018/6/14 15:07
     */
    @Test
    public void putSendOrderToBOBTimeToRedis() {
        String orderNo = "10000000012111";
        String sendOrder2BOBTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(Constants.SEND_ORDER_TIME + orderNo, sendOrder2BOBTime);
    }

    /**
     * Description: 使用订单号（19位）获取商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @return java.lang.String 商户发送订单信息到BOB的时间
     * date: 2018/6/14 15:08
     */
    @Test
    public void getSendOrderToBOBTimeFromRedis() {
        String orderNo = "10000000012111";
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String sendOrderToBOBTime = valueOperations.get(Constants.SEND_ORDER_TIME + orderNo);
//        System.out.println(sendOrderToBOBTime);
    }

    /**
     * Description: 从Redis中删除商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * date: 2018/6/14 15:09
     */
    @Test
    public void deleteSendOrderToBOBTimeInRedis() {
        String orderNo = "10000000012111";
        stringRedisTemplate.delete(Constants.SEND_ORDER_TIME + orderNo);
    }

}
