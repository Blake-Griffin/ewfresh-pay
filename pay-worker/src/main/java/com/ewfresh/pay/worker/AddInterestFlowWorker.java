package com.ewfresh.pay.worker;


import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.model.InterestFlow;
import com.ewfresh.pay.model.vo.RepayFlowVo;
import com.ewfresh.pay.service.InterestFlowService;
import com.ewfresh.pay.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName AddInterestFlowWorker
 * @Description: 每日定时更新逾期流水
 * @Author huboyang
 * @Date 2019/7/25
 **/
@Component
public class AddInterestFlowWorker {
    private static final Logger logger = LoggerFactory.getLogger(AddInterestFlowWorker.class);
    private static String lockName = "{pay}-{AddInterestFlowWorkerLock}";
    @Autowired
    private InterestFlowService interestFlowService;
    @Autowired
    private RedisLockHandler redisLockHandler;

    @Scheduled(cron ="0 0 2 * * ?")
    public void addInterestFlow(){
        logger.info("--------------------------->>>>>AddInterestFlowWorker ");
        Lock lock = null ;
        try {
            //每日查询还款记录表
            //如果有新的记录还逾期费的记录就查出来并且插入到InterestFlow表中
            //现在日期
            lock = new Lock(lockName,lockName);
            boolean lockFlag = redisLockHandler.tryLock(lock);
            if (!lockFlag){
                logger.info("can not  get  Lock ",lockName);
                return;
            }
            List<InterestFlow> interestFlowList = new ArrayList<>();
            Date d=new Date();
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //一天前日期
            Date futureMountDays = DateUtil.getFutureMountDays(d, -1);
            //当天日期
            String startTime = df.format(futureMountDays);
            String endTime = df.format(d);
            logger.info("startTime={},endTime={}", startTime,endTime);
            //查找当前日期前一天的所有还逾期费的流水
            List<RepayFlowVo> repayFlowVoList= interestFlowService.getInterestFlow(startTime,endTime);
            logger.info("------------------->>>> repayFlowVoList={}", ItvJsonUtil.toJson(repayFlowVoList));
            if (CollectionUtils.isNotEmpty(repayFlowVoList)){
                for (RepayFlowVo repayFlow:repayFlowVoList){

                    InterestFlow interestFlow = new InterestFlow();
                    interestFlow.setBillRepayId(repayFlow.getBillRepayId());
                    interestFlow.setBillId(repayFlow.getBillId());
                    interestFlow.setBillFlow(repayFlow.getBillFlow());
                    interestFlow.setUserId(repayFlow.getUserId());
                    interestFlow.setUname(repayFlow.getuName());
                    interestFlow.setTotalInterest(repayFlow.getTotalInterest());
                    interestFlow.setRepaidInterest(repayFlow.getInterestAmount());
                    interestFlow.setRepayChannel(repayFlow.getRepayChannel());
                    interestFlow.setRepayType(repayFlow.getRepayType());
                    interestFlow.setRepayTime(repayFlow.getRepayTime());
                    interestFlow.setBillTime(repayFlow.getBillTime());
                    interestFlowList.add(interestFlow);
                }
                interestFlowService.addInterestFlow(interestFlowList);

            }
        }catch (Exception e){
            logger.info("add InterestFlow is  err ",e);
            try {
                redisLockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("Release lock is Err ", e1);
            }
        }
    }
}