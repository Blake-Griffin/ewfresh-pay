/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/4/4.
 *  
 **/

package com.ewfresh.pay.worker;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.WhiteBillsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @descrption TODO
 *  * @author gaoyongqiqng
 *  * @create 2019-04-04
 *  * @Email 1005267839@qq.com
 **/
@Component
public class QuotaUnfreezeWorker {
    private Logger logger = LoggerFactory.getLogger(QuotaUnfreezeWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    private static final Short USE_STATUS_1 = 1;//白条正常
    private static final Short USE_STATUS_2 = 2;//白条冻结
    private static final Short USE_STATUS_3 = 3;//白条违约

    /**
     *  * @author gaoyongqiang
     *  * @Description 用户额度解冻和冻结
     *  * @Date   2019/4/4 10:45
     *  *  @params
     *  * @return 
     **/
    @Scheduled(cron = "0/10 * * * * ?")
    public void updateUserQuota() throws ParseException {
        //logger.info("Time Task QuotaUnfreezeWorker Starts......");
        String userId = accountFlowRedisService.getQuotaUnfreeze();
        if (StringUtils.isBlank(userId)) {
            return;
        }
        //拿到uid判断用户是否违约和冻结 查询全部未还款账单
        List<BillVo> billList = whiteBillsService.getBillsByUid(Long.valueOf(userId));//用户所有未还款账单
        logger.info(" User's unpaid bills [billList = {}]", ItvJsonUtil.toJson(billList));
        //冻结 和 违约
        List<BillVo> frozenList = new ArrayList<>();
        List<BillVo> defaultList = new ArrayList<>();
        WhiteBar whiteBar = new WhiteBar();

        Boolean balanceAndBarBoolean = balanceAndBarLock.getBalanceAndBarLock(userId);
        if (balanceAndBarBoolean) {
            try {
                if (!billList.isEmpty()) {
                    for (BillVo billVo : billList) {
                        long days = getApartDays(billVo.getLastRepaidTime());//间隔天数
                        Map<String, Integer> longMap = whiteBillsService.getperiod(billVo.getUserId());//还款账期
                        Integer period = longMap.get("period");
                        if (days > 0 && days <= period) {
                            frozenList.add(billVo);
                        }
                        if (days > period) {
                            defaultList.add(billVo);
                        }
                    }
                    Map map = new HashMap();
                    if (!defaultList.isEmpty() || !frozenList.isEmpty()) {
                        if (defaultList.isEmpty() && !frozenList.isEmpty()) {
                            whiteBar.setUseStatus(USE_STATUS_2);
                            whiteBar.setUid(Long.valueOf(userId));
                            whiteBar.setLastModifyTime(new Date());
                            map.put(userId, USE_STATUS_2);
                            accountFlowRedisService.setCacheQuotaUnfreeze(Long.valueOf(userId), map);
                        }
                        if (!defaultList.isEmpty()) {
                            whiteBar.setUseStatus(USE_STATUS_3);
                            whiteBar.setUid(Long.valueOf(userId));
                            whiteBar.setLastModifyTime(new Date());
                            map.put(userId, USE_STATUS_3);
                            accountFlowRedisService.setCacheQuotaUnfreeze(Long.valueOf(userId), map);
                        }
                    } else {
                        whiteBar.setUseStatus(USE_STATUS_1);
                        whiteBar.setUid(Long.valueOf(userId));
                        whiteBar.setLastModifyTime(new Date());
                    }
                }else {
                    whiteBar.setUseStatus(USE_STATUS_1);
                    whiteBar.setUid(Long.valueOf(userId));
                    whiteBar.setLastModifyTime(new Date());
                }
                whiteBillsService.updateWhiteBar(whiteBar);
                logger.info(" update whiteBar success ");
                balanceAndBarLock.releaseLock(userId);
            } catch (Exception e) {
                e.printStackTrace();
                balanceAndBarLock.releaseLock(userId);
            }
        }
        logger.info("Time Task QuotaUnfreezeWorker End......");

    }

    //计算间隔时间
    public long getApartDays(Date billTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(new Date());
        Date time1 = sdf.parse(s);
        String b = sdf.format(billTime);
        Date time2 = sdf.parse(b);
        long between_days = (time1.getTime() - time2.getTime()) / (1000 * 3600 * 24);
        return between_days;
    }
}
