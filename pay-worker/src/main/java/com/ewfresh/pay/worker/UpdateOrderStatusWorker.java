package com.ewfresh.pay.worker;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.vo.FinishOrderVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.PayFlowService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/8/24 0024
 */
@Component
public class UpdateOrderStatusWorker {


    private static final Logger logger = LoggerFactory.getLogger(AccountFlowWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private PayFlowService payFlowService;
    @Scheduled(cron = "1-59 * * * * ?")
    public void getInfo(){
        String orderInfo = accountFlowRedisService.getUpdateOrderStatus();
        if(StringUtils.isBlank(orderInfo)){
            return;
        }
        FinishOrderVo finishOrderVo = ItvJsonUtil.jsonToObj(orderInfo, FinishOrderVo.class);
        String earnestBill = finishOrderVo.getEarnestBill();
        String finalBill = finishOrderVo.getFinalBill();
        payFlowService.updateOrderStatus(finalBill);
        logger.info("update order status success [earnestBill = {},finalBill = {}]",earnestBill,finalBill);
    }

}
