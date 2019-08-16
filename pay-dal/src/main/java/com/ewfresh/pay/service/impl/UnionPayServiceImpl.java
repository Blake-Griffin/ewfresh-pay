package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.PayFlowDao;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.UnionPayService;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.UpdateOrderInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.ewfresh.pay.util.Constants.PAY_UPDATE_PAY_FLOW;

/**
 * description: 银联
 * @author: JiuDongDong
 * date: 2019/7/5.
 */
@Service
public class UnionPayServiceImpl implements UnionPayService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PayFlowDao payFlowDao;
    @Autowired
    private UpdateOrderInfoUtil updateOrderInfoUtil;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayRedisService;

    @Transactional
    @Override
    public void updatePayFlowAndOrder(PayFlow payFlow, String updateOrderStatusUrl, Map<String, String> params, String refundOrderId, String hashKey, String outRequestNo) {
        logger.info("updateOrderStatusUrl = {}, params = {}, refundOrderId = {}, hashKey = {}, " +
                "outRequestNo = {}, payFlow = {}", updateOrderStatusUrl, JsonUtil.toJson(params),
                refundOrderId, hashKey, outRequestNo, JsonUtil.toJson(payFlow));
        if (StringUtils.isNotBlank(updateOrderStatusUrl)) {
            logger.info("params = {}", JsonUtil.toJson(params));
            updateOrderInfoUtil.updateOrderInfo(updateOrderStatusUrl, params);
        }
        if (null != payFlow) {
            // 查询RefundUtils是否已经把这个退款流水insert到t_pay_flow，没放到的话worker轮询
            String channelFlowId = payFlow.getChannelFlowId();
            PayFlow flowDatabase = payFlowDao.getPayFlowPartById(channelFlowId);
            logger.info("flowDatabase = {}", JsonUtil.toJson(flowDatabase));
            if (null != flowDatabase) {
                logger.info("updatePayFlow, channelFlowId = {}", channelFlowId);
                int updateNum = payFlowDao.updatePayFlow(payFlow);
                logger.info("Update payFlow ok, update num  = {}", updateNum);
            } else {
                logger.info("Put payFlow to Redis.");
                unionPayRedisService.putPayFlowToRedis(PAY_UPDATE_PAY_FLOW, payFlow);
            }
        }
        try {
            if (StringUtils.isNotBlank(refundOrderId)) {
                logger.info("refundOrderId = {}", refundOrderId);
                unionPayRedisService.deleteRefundOrderInfoInRedis(refundOrderId, hashKey);
            }
            if (StringUtils.isNotBlank(outRequestNo)) {
                logger.info("outRequestNo = {}", outRequestNo);
                unionPayRedisService.delReturnAmountParams(outRequestNo);
            }
        } catch (Exception e) {
            logger.error("Delete refund info and order info from Redis failed! refundOrderId = " +
                    refundOrderId + ", hashKey = " + hashKey + ", outRequestNo = " + outRequestNo, e);
        }
    }
}
