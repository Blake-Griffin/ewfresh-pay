/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/4/11.
 *  
 **/

package com.ewfresh.pay.worker;


import com.ewfresh.commons.util.ItvJsonUtil;

import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.BillIntersetRecord;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.WhiteBillsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @descrption 计算利息
 *  * @author gaoyongqiqng
 *  * @create 2019-04-11
 *  * @Email 1005267839@qq.com
 **/
@Component
public class InterestBearingWorker {
    private Logger logger = LoggerFactory.getLogger(InterestBearingWorker.class);
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Value("${interest_rate}")
    private String INTEREST_RATE;//利率
    private static BigDecimal totalInterest = new BigDecimal(0);
    private static BigDecimal tenthousand = new BigDecimal(10000);

    /**
     *  * @author gaoyongqiang
     *  * @Description 计算利息
     *  * @Date   2019/4/11 15:00
     *  *  @params
     *  * @return 
     **/
    //自动更新  没有流水交易但更新账单利息表(待还款的与部分还款的)
    @Scheduled(cron = "0 0 1 * * ?")
    private void update() throws Exception {
        logger.info("Time Task InterestBearingWorker Start......");
        List<Bill> bills = whiteBillsService.getByRecordingTime();
        if (!bills.isEmpty()) {
            for (Bill bille : bills) {
                Map<String, Integer> longMap = whiteBillsService.getperiod(bille.getUserId());//还款账期
                Integer period = longMap.get("period");
                long days = getApartDays(bille.getLastRepaidTime());//间隔天数
                if (days > 0 && days <= period) {
                    totalInterest = bille.getBillAmount().subtract(bille.getRepaidAmount()).multiply(new BigDecimal(INTEREST_RATE)).divide(tenthousand);//总利息

                    bille.setTotalInterest(bille.getTotalInterest().add(totalInterest));
                    bille.setId(bille.getId());
                    bille.setLastModifyTime(new Date());
                    whiteBillsService.updateBillSelective(bille);
                    BillIntersetRecord billIntersetRecords = new BillIntersetRecord();
                    billIntersetRecords.setBillId(bille.getId());
                    billIntersetRecords.setInterestBearingAmount(bille.getBillAmount().subtract(bille.getRepaidAmount()));
                    billIntersetRecords.setInterestAmount(totalInterest);
                    billIntersetRecords.setInterestRate(Integer.valueOf(INTEREST_RATE).intValue());
                    logger.info("billIntersetRecords =" + ItvJsonUtil.toJson(billIntersetRecords));
                    whiteBillsService.addBillIntersetRecord(billIntersetRecords);
                    logger.info(" add billIntersetRecord success ");
                }
                accountFlowRedisService.setQuotaUnfreeze(bille.getUserId());
                logger.info(" this time for accountFlowRedisService [ uid = {} ]", bille.getUserId());
            }
        }
        logger.info("Time Task InterestBearingWorker End......");
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
