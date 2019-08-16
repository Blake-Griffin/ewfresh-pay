package com.ewfresh.pay.worker;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.JsonUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 银联H5、扫码付：更改payFlow信息
 * @author: JiuDongDong
 * date: 2019/7/9
 */
@Component
public class UpdatePayFlowWorker {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;
    @Autowired
    private PayFlowService payFlowService;

    @Transactional
    @Scheduled(cron = "41 0/1 * * * ?")
    private void updatePayFlow() {
        try {
            // 获取Redis中的payFlow
            Map<Object, Object> payFlowFromRedis = unionPayRedisService.getPayFlowFromRedis(PAY_UPDATE_PAY_FLOW);
            if (MapUtils.isEmpty(payFlowFromRedis)) {
                //logger.info("There is no payFlow in Redis.");
                return;
            }
            Set<Map.Entry<Object, Object>> entries = payFlowFromRedis.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String channelFlowId = entry.getKey().toString();
                logger.info("Now is going to update payFlow, channelFlowId = {}", channelFlowId);
                String value = entry.getValue().toString();
                logger.info("Now is going to update payFlow, value = {}", value);
                PayFlow payFlow = JsonUtil.jsonToObj(value, PayFlow.class);

                // 查询RefundUtils是否已经把这个退款流水insert到t_pay_flow，没放到的话worker轮询
                PayFlow flowDatabase = payFlowService.getPayFlowPartById(channelFlowId);
                logger.info("flowDatabase = {}", JsonUtil.toJson(flowDatabase));
                int updateNum;
                if (null != flowDatabase) {
                    logger.info("updatePayFlow, channelFlowId = {}", channelFlowId);
                    PayFlow newPayFlow = new PayFlow();
                    newPayFlow.setChannelFlowId(payFlow.getChannelFlowId());
                    newPayFlow.setStatus(payFlow.getStatus());
                    newPayFlow.setSuccessTime(payFlow.getSuccessTime());
                    updateNum = payFlowService.updatePayFlow(newPayFlow);
                    // 从Redis中删除payFlow
                    unionPayRedisService.delPayFlowFromRedis(PAY_UPDATE_PAY_FLOW, channelFlowId);
                    logger.info("Update payFlow ok, update num  = {}", updateNum);
                } else {
                    logger.info("The payFlow has not been insert to t_pay_flow. channelFlowId = {}", channelFlowId);
                    continue;
                }
            }
        } catch (Exception e) {
            logger.error("Update payFlow occurred error!", e);
        }

    }

}
