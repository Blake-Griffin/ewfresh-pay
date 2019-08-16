/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/4/26.
 **/
 
package com.ewfresh.pay.worker;

import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
  * @descrption 发短信的worker
 * @author gaoyongqiqng
 * @create 2019-04-26
 * @Email 1005267839@qq.com
  **/
@Component
public class SendMessageWorker {
    private Logger logger = LoggerFactory.getLogger(SendMessageWorker.class);
    private String INTERESTBEARINGMSG = "interestBearingMsg";
    private String OVERDUESMS = "overdueSMS";
    private static long DAY = -3;
    private static long ONEDAY = 1;
    private static long FOURDAY = 4;
    @Value("${http_msg}")
    private String msgUrl;
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private MsgClient msgClient;
    /**
     * @author gaoyongqiang
     * @Description 计息三天前的通知短信
     * @Date   2019/4/26 11:31
     *  @params
     * @return 
      **/
    @Scheduled(cron ="0 0 8 * * ?")
    public void interestBearing() throws Exception{
        List<Bill> bills = whiteBillsService.getByRecordingTime();
        if (!bills.isEmpty()) {
            for (Bill bille : bills) {
                Map<String, Integer> longMap = whiteBillsService.getperiod(bille.getUserId());//还款账期
                Integer period = longMap.get("period");
                long days = getApartDays(bille.getLastRepaidTime());//间隔天数
                if (days==DAY){ //发送短信
                    //从redis中获取电话号码
                    String userInfo = accountFlowRedisService.getUserInfo(bille.getUserId());
                    //反序列化
                    HashMap hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                    String showPhone = (String) hashMap.get("showPhone");
                    String content = getSendMessageMonthDay(bille.getLastRepaidTime());
                    //content参数 金额 + 银行卡尾号
                    content = bille.getBillAmount() + "|" + content;
                    msgClient.postMsg(msgUrl, showPhone, bille.getUserId()+"", content, INTERESTBEARINGMSG);
                }
                if (days==ONEDAY || days==FOURDAY){ //发送短信
                    //从redis中获取电话号码
                    String userInfo = accountFlowRedisService.getUserInfo(bille.getUserId());
                    //反序列化
                    HashMap hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                    String showPhone = (String) hashMap.get("showPhone");
                    String content = getSendMessageMonthDay(bille.getLastRepaidTime());
                    //content参数 金额 + 银行卡尾号
                    content = bille.getBillAmount() + "|" + content;
                    msgClient.postMsg(msgUrl, showPhone, bille.getUserId()+"", content, OVERDUESMS);
                }
            }
        }
        logger.info("Time Task InterestBearingMsg End......");
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
    private String getSendMessageMonthDay(Date arrivalDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("M-d");
        String format = sdf.format(arrivalDate);
        String[] split = format.split("-");
        StringBuilder contentMonthDay = new StringBuilder();
        contentMonthDay = contentMonthDay.append(split[Constants.INTEGER_ZERO]).append("|").append(split[Constants.INTEGER_ONE]);
        return contentMonthDay.toString();
    }

    public static void main(String[] args) throws ParseException{
            System.out.println("");
    }
}
