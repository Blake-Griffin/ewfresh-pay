/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/13.
 *  
 **/

package com.ewfresh.pay.worker;


import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.util.CommonUtils;
import com.ewfresh.pay.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @descrption 白条账单
 *  * @author gaoyongqiqng
 *  * @create 2019-03-11
 *  * @Email 1005267839@qq.com
 **/

@Component
public class WhiteBillsWorker {
    private Logger logger = LoggerFactory.getLogger(WhiteBillsWorker.class);

    private static final Short BILL_STATUS_1 = 1;//待还款  还款渠道1
    private static BigDecimal ZERO = new BigDecimal(0);
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private RedisLockHandler lockHandler;
    private static final String autoWhiteBill = "{pay}-{autoWhiteBill}";

    /**
     *  * @author gaoyongqiang
     *  * @Description 生成账单 更新账单并计息
     *  * @Date   2019/3/15 10:57
     *  *  @params
     *  * @return 
     **/
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoWhiteBill() throws Exception {
        Lock lock = new Lock(autoWhiteBill, autoWhiteBill);
        boolean booleanAutoWhiteBill = lockHandler.tryLock(lock, 0);
        if (!booleanAutoWhiteBill) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String retStrFormatNowDate = sdf.format(new Date());
        logger.info("Time Task WhiteBill Starts......", ItvJsonUtil.toJson(retStrFormatNowDate));
        Date dates = DateUtil.getFutureMountDaysStartWithOutHMS(new Date(), -1);//前推一天
        //得到所有流水单
        List<BarDealFlow> whiteBillslists = whiteBillsService.getBarDealFlowByPrimaryKey(dates, null);
        logger.info(" this time  [ BarDealFlow = {} ]", ItvJsonUtil.toJson(whiteBillslists));
        //得到所有的用户Uid（去重）
        LinkedHashSet<Long> link = new LinkedHashSet<Long>();
        for (BarDealFlow barDealFlow : whiteBillslists) {
            link.add(barDealFlow.getUid());
        }
        logger.info(" this time  [ link = {} ]", ItvJsonUtil.toJson(link));
        Bill billSet = new Bill();
        if (!link.isEmpty()) {
            //遍历所有用户流水交易表
            for (Long uid : link) {
                logger.info(" this time  [ uid = {} ]", ItvJsonUtil.toJson(uid));
                BigDecimal billAmount = new BigDecimal(0);
                //根据用户id，日期查询流水交易数据
                List<BarDealFlow> whiteBillslist = whiteBillsService.getBarDealFlowByPrimaryKey(dates, uid);
                //还款账期
                Map<String, Integer> longMap = whiteBillsService.getperiod(uid);
                Integer period = longMap.get("period");
                logger.info(" this time for payflow to accflow [ whiteBillslists = {} ]", ItvJsonUtil.toJson(whiteBillslist));
                logger.info(" this time for payflow to accflow [ period = {} ]", ItvJsonUtil.toJson(period));
                Date lastRepaidTime = DateUtil.getFutureMountDaysStartWithOutHMS(dates, period.intValue());
                //生成账单
                for (BarDealFlow barDealFlows : whiteBillslist) {

                    if (barDealFlows.getDealType() == 1) {
                        billAmount = billAmount.add(barDealFlows.getAmount());
                    } else {
                        billAmount = billAmount.subtract(barDealFlows.getAmount());
                    }

                }

                if (billAmount.compareTo(ZERO) != 0) {
                    String billFlow = getBillFlow(uid);
                    billSet.setBillFlow(billFlow);
                    billSet.setBillAmount(billAmount);
                    billSet.setTotalInterest(new BigDecimal(0));
                    billSet.setBillTime(dates);//账单生成时间就是流水交易时间
                    billSet.setUserId(uid);
                    billSet.setUname(whiteBillslist.get(0).getUname());
                    billSet.setLastRepaidTime(lastRepaidTime);//最后还款日期
                    billSet.setBillStatus(BILL_STATUS_1);
//                    billSet.setLastModifyTime(new Date());
                    whiteBillsService.addBill(billSet);
                    logger.info(" add bill success ");
                    BarDealFlow barDealFlow = new BarDealFlow();
                    barDealFlow.setBillFlow(billFlow);
                    barDealFlow.setOccTime(dates);
                    barDealFlow.setUid(uid);
                    whiteBillsService.updateBarDealFlow(barDealFlow);
                    logger.info(" update barDealFlow success ");
                }
            }
        }
        logger.info("Time Task WhiteBill End......");
        lockHandler.releaseLock(lock);
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

    //生成账单批次号
    public String getBillFlow(Long userId) {
        String billFlow = userId + "";
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        billFlow = formatter.format(date) + billFlow;
        billFlow = billFlow.replace("-", "").replace(" ", "").replace(":", "");
        return billFlow;
    }

    public static void main(String[] args) {
        String INTEREST_RATE = "0.05";
        INTEREST_RATE = CommonUtils.yuanToFee(INTEREST_RATE);
        System.out.println(INTEREST_RATE);
    }
}
