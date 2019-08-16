package com.ewfresh.pay.worker;

import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 快钱系统：将订单的状态，由退款中更改为已取消
 * @author: JiuDongDong
 * date: 2018/12/14.
 */
@Component
public class UpdateOrderStatusToCancelWorker {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Value("${httpClient.updateOrderStatus}")
    private String updateOrderStatusUrl;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;

    @Scheduled(cron = "11 0/3 * * * ?")
//    @Scheduled(cron = "0/3 * * * * ?")
    private void updateOrderStatusToCancel() {
        try {
            // 获取所有待修改订单状态的订单（从2100退款中修改为1500已取消）
            Map<Object, Object> allUpdateOrders =
                    bill99OrderRedisService.getAllToUpdateStatusOrderInfo(Constants.QUICK_PAY_MODIFY_ORDER_STATUS);
            if (MapUtils.isEmpty(allUpdateOrders)) {
                return;
            }
            // key为orderNo，value为订单状态参数
            Set<Map.Entry<Object, Object>> entries = allUpdateOrders.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                Long orderNo = Long.parseLong((String) entry.getKey()) ;
                logger.info("Now is going to update order status, orderNo = " + orderNo);
                String value = (String) entry.getValue();
                logger.info("Now is going to update order status, params = {}", value);
                Map<String, String> params = JsonUtil.jsonToObj(value, Map.class);
                // 这个worker目前仅只适用于取消订单退款、关闭订单退款，功能不同，最后更改的订单状态也不相同，取消订单更改为1500，关闭订单修改为1360
                String refundType = params.get(REFUND_TYPE);
                // 修改订单状态
                if (REFUND_TYPE_CANCEL.equals(refundType) || REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                    updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
                } else if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                    logger.info("Order system handle.");
                } else if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                    logger.info("Order system handle.");
                }
                // 从Redis中删除待修改订单状态信息
                bill99OrderRedisService.deleteToUpdateStatusOrderInfoInRedis(orderNo.toString(), Constants.QUICK_PAY_MODIFY_ORDER_STATUS);
                logger.info("update orderStatus from 2100 to 1500, and then delete from redis ok, orderNo = {}", orderNo);
            }
        } catch (Exception e) {
            logger.error("update order status from 2100 to 1500 or 1360 occurred error!", e);
        }

    }

}
