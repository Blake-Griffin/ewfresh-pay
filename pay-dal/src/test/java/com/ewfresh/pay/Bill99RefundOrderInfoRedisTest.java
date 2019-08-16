package com.ewfresh.pay;

import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.RefundInfoVo;
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

import java.text.SimpleDateFormat;

/**
 * description:
 *      99bill的退单信息操作
 * @author: JiuDongDong
 * date: 2018/8/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test-common.xml"})
public class Bill99RefundOrderInfoRedisTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    StringRedisTemplate redisTemplate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Description: 存储商户退款信息
     * @author: JiuDongDong
     * date: 2018/8/9 13:32
     */
    @Test
    public void putRefundOrderInfoToRedis() throws Exception {
        RefundParam refundParam = new RefundParam();
        refundParam.setOutTradeNo("1299");//商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        refundParam.setOutRequestNo("1299");//退款订单号(子订单订单号,如果没有该订单没有子订单则该处填商户订单号)
        refundParam.setRefundAmount("6850");//退款金额
        refundParam.setChannelCode("1006");//交易渠道编码
        refundParam.setTotalAmount("6850");//订单金额

        RefundInfoVo refundInfoVo = new RefundInfoVo();// 退款信息
        // 退款信息放入Redis
        refundInfoVo.setRefundParam(refundParam);
        refundInfoVo.setRefundSeq(refundParam.getOutRequestNo());
        refundInfoVo.setRefundTime("20180809110210");
        String json = JsonUtil.toJson(refundInfoVo);
//        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(Constants.PAY_BILL99_REFUND_INFO, outRequestNo, json);
    }

    /**
     * Description: 从Redis获取退款信息(根据第三方交易订单号)
     * @author: JiuDongDong
     * @return com.ewfresh.pay.model.RefundParam 退款信息
     * date: 2018/8/9 14:16
     */
    @Test
    public void getRefundOrderInfoFromRedis() {
        String outRequestNo = "1299";// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        String s = (String) hashOperations.get(Constants.PAY_BILL99_REFUND_INFO, outRequestNo);
        logger.info("refundInfoVo: " + s);
        // TODO shan
        logger.info("hashOperations.keys()" + hashOperations.keys(Constants.PAY_BILL99_REFUND_INFO));
        logger.info("hashOperations.entries(): " + hashOperations.entries(Constants.PAY_BILL99_REFUND_INFO));
        logger.info("hashOperations.values(): " + hashOperations.values(Constants.PAY_BILL99_REFUND_INFO));
        RefundInfoVo refundInfoVo = JsonUtil.jsonToObj(s, RefundInfoVo.class);
    }
}
