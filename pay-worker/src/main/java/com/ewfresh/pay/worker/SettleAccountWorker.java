package com.ewfresh.pay.worker;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.Bill99Manager;
import com.ewfresh.pay.manager.impl.BalanceManagerImpl;
import com.ewfresh.pay.manager.impl.SettleRecordManagerImpl;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.SettleRecordService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.bill99.SettleResult;
import com.ewfresh.pay.util.boc.MyHttp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Description: 定时分账的worker
 * @author DuanXiangming
 * Date 2018/8/13 0013
 */
@Component
public class SettleAccountWorker {



    private static final Logger logger = LoggerFactory.getLogger(SettleAccountWorker.class);
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private SettleRecordService settleRecordService;
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private Bill99Manager bill99Manager;
    private static final BigDecimal zero = new BigDecimal(Constants.NULL_BALANCE);
    private static final String SUB_FLAG = "S";//子结算批次号
    private static final String SETTLE_STATUS_0 = "0";//结算状态,初始化
    private static final String SETTLE_STATUS_2 = "2";//结算状态,分账中
    private static final String SETTLE_STATUS_3 = "3";//结算状态,等待变更可提现金额
    private static final String SETTLE_STATUS_4 = "4";//结算状态,变更可提现金额中
    private static final String SETTLE_STATUS_9 = "9";//结算状态,成功
    private static final String SETTLE_STATUS_8 = "8";//结算状态,失败

    /**
     * Description: 定时分账的worker
     * @author DuanXiangming
     * Date    2018/4/18
     */
    @Scheduled(cron = "0 4/5 * * * ? ")
    public void settleAccount() {
        //用于查询HAT余额的方法
        List<String> settleRecordIds = accountFlowRedisService.queryBalanceByHAT();
        if (CollectionUtils.isEmpty(settleRecordIds)){
            return;
        }
        for (String settleRecordId : settleRecordIds) {
            try {
                SettleRecord settleRecord = settleRecordService.getSettleRecordById(Integer.valueOf(settleRecordId));
                String settleStatus = querySettleResult(settleRecord);
                if (StringUtils.isBlank(settleStatus)){
                    logger.error(" query settle result is empty [settleRecordId = {}]",settleRecordId);
                }
                //HAT处理中,系统暂时无需处理
                if (settleStatus.equals(SETTLE_STATUS_0) || settleStatus.equals(SETTLE_STATUS_2) || settleStatus.equals(SETTLE_STATUS_3) || settleStatus.equals(SETTLE_STATUS_4) ){
                    logger.info("this settle is in hand [settleRecordId = {}]",settleRecordId);
                    accountFlowRedisService.setQueryBalanceByHAT(settleRecordId);
                }else if(settleStatus.equals(SETTLE_STATUS_8)){
                    //HAT处理失败,本系统将结算记录修改为打款失败
                    logger.error("this settle is failed in HAT [settleRecordId = {}]",settleRecordId);
                    settleRecord.setSettleStatus(Constants.APPR_STATUS_5);
                    settleRecordService.apprByBatchNo(settleRecord);
                }else if(settleStatus.equals(SETTLE_STATUS_9)){
                    //HAT处理成功,本系统添加流水记录
                    logger.info("this settle is success in HAT [settleRecordId = {}]",settleRecordId);
                    settleRecord.setSettleStatus(Constants.APPR_STATUS_4);
                    PayFlow payFlow = getPayFlowBySettle(settleRecord);
                    settleRecord.setRemitConfirm(new Date());
                    String batchNo = payFlow.getChannelFlowId();
                    Short tradeType = payFlow.getTradeType();
                    logger.info("the param for get payflow [batchNo = {},tradeType = {}]",batchNo,tradeType);
                    PayFlow payFlowExit = payFlowService.getPayFlowPartByIdAndTradeType(batchNo,tradeType);
                    logger.info("this payFlowExit is [payFlowExit = {}]",ItvJsonUtil.toJson(payFlowExit));
                    if (payFlowExit != null){
                        logger.error("this settle payflow is exit [payFlowExit = {}]", ItvJsonUtil.toJson(payFlowExit));
                        continue;
                    }
                    AccountFlowVo accountFlow = getAccountFlowByAccountBalance(payFlow);
                    settleRecordService.updateSettle(settleRecord,payFlow,accountFlow);
                }else {
                    logger.error("unable to handle settleStatus [settleStatus = {}]",settleStatus);
                    accountFlowRedisService.setQueryBalanceByHAT(settleRecordId);
                }

            } catch (Exception e) {
                logger.error("query balance err settleRecordId = " + settleRecordId,e);
            }
        }


    }

    private PayFlow getPayFlowBySettle(SettleRecord settleRecord) {

        PayFlow payFlow = new PayFlow();
        Integer id = settleRecord.getId();
        payFlow.setOrderId(Long.valueOf(id));
        Integer shopId = settleRecord.getShopId();
        payFlow.setShopId(shopId);
        String shopName = "";
        try {
            shopName = accountFlowRedisService.getShopInfoFromRedis(Constants.SHOP_ADDSHOP_REDIS, shopId.toString());
            logger.info(" this shopName is own of user [shopName = {}]", shopName);
        } catch (Exception e) {
            logger.error("get userInfo failed",e);
        }
        if(StringUtils.isNotBlank(shopName)){
            payFlow.setUname(shopName);
        }
        payFlow.setTradeType(Constants.TRADE_TYPE_6);
        BigDecimal settleFee = settleRecord.getSettleFee();
        payFlow.setFeeRate(settleFee);                    //手续费
        payFlow.setPlatIncome(settleFee);
        BigDecimal amount = settleRecord.getAmount();     //结算金额
        payFlow.setOrderAmount(amount);
        BigDecimal remitAmount = settleRecord.getRemitAmount();//打款金额
        payFlow.setPayerPayAmount(remitAmount);
        payFlow.setPayerId(Constants.SYSTEM_ID);
        payFlow.setPayerName("存管账户");
        payFlow.setReceiverName(shopName);
        payFlow.setReceiverUserId(shopId.toString());
        payFlow.setChannelCode(Constants.HAT_CHANEL_CODE);
        payFlow.setChannelFlowId(settleRecord.getBatchNo());
        payFlow.setChannelName("HAT结算");
        return payFlow;
    }

    private AccountFlowVo getAccountFlowByAccountBalance(PayFlow payFlow) {
        Long shopId = Long.valueOf(payFlow.getShopId());
        AccountFlowVo accountFlow = accountFlowService.getAccountFlowByUid(shopId.toString());
        if (accountFlow == null){
            //需要新生成一条资金账户流水对象
            accountFlow = new AccountFlowVo();
            accountFlow.setBalance(zero);
            accountFlow.setFreezeAmount(zero);
        }
        BigDecimal balance = accountFlow.getBalance();
        logger.info("this shop for change balance [accountFlow = {}]", ItvJsonUtil.toJson(accountFlow));
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        Short tradeType = payFlow.getTradeType();
        if (tradeType.shortValue() == Constants.TRADE_TYPE_6){
            balance = balance.add(payerPayAmount);
            accountFlow.setBusiType(Constants.BUSI_TYPE_5);
            accountFlow.setDesp("结算收入");
            accountFlow.setDirection(Constants.SHORT_ONE);
            accountFlow.setTargetAcc("店铺余额");
            accountFlow.setSrcAcc("存管账户");
        }
        if (tradeType.shortValue() == Constants.TRADE_TYPE_13){
            balance = balance.subtract(payerPayAmount);
            accountFlow.setBusiType(Constants.BUSI_TYPE_5);
            accountFlow.setDesp("退货结算");
            accountFlow.setDirection(Constants.SHORT_TWO);
            accountFlow.setSrcAcc("店铺余额");
            accountFlow.setTargetAcc("存管账户");
        }
        accountFlow.setAmount(payerPayAmount);
        accountFlow.setBalance(balance);
        accountFlow.setUserId(shopId);
        accountFlow.setUname(payFlow.getUname());
        accountFlow.setAccType(Constants.ACC_TYPE_5);
        return accountFlow;

    }

    public String querySettleResult(SettleRecord settleRecord) throws Exception {

        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String batchNo = settleRecord.getBatchNo();
        Map<String, Object> map = new HashMap<>();
        // 2. 设置请求信息
        map.put(Constants.HAT_PLATFORMCODE,platformCode);
        map.put("outOrderNo",batchNo);
        map.put("outSubOrderNo",SUB_FLAG + batchNo);
        String params = ItvJsonUtil.toJson(map);
        FinderSignService finderSignService = new FinderSignService();
        String signMsg = finderSignService.sign(platformCode, params, hatPrivateKey);
        logger.info("the signMsg is ------->{}", ItvJsonUtil.toJson(map));
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/settle/detail", params, UUID.randomUUID().toString(), platformCode, signMsg);
        if (MapUtils.isEmpty(post)) {
            logger.info("post is empty");
            return null;
        }
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        String content = String.valueOf(post.get("content")) ;
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        if (StringUtils.isNotEmpty(content)) {
            logger.info("the post is not empty! [content = {}]",content);
            Map<String, String> responseMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            String rspCode = responseMap.get(Constants.RSPCODE);
            if (rspCode.equals(Constants.HAT_SUCCESS_0000)) {
                logger.info("settle with hat success [paramStrs = {}]",params);
                String settleResultStr = responseMap.get(Constants.SETTLERESULT);
                logger.info("this settle result [settleResultStr = {}]",settleResultStr);
                ArrayList<SettleResult> settleResults = ItvJsonUtil.jsonToObj(settleResultStr, new TypeReference<ArrayList<SettleResult>>() {
                });
                if (CollectionUtils.isEmpty(settleResults)){
                    logger.error("query settle result is empty");
                    return null;
                }
                SettleResult settleResult = settleResults.get(0);
                String settleStatus = settleResult.getSettleStatus();
                return settleStatus;
            } else if (rspCode.equals(Constants.HAT_SETTLE_2000)) {
                logger.error("this order is not exist [settleRecord = {]] ",ItvJsonUtil.toJson(settleRecord));
            }
        } else {
            logger.info("the have an http error!!");
        }
        return null;
    }

    public Map<String, String> sendSettleRequest(String shopId) throws Exception {

        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();

        Map<String, Object> map = new HashMap<>();
        // 2. 设置请求信息
        map.put(Constants.HAT_PLATFORMCODE,platformCode);
        map.put(Constants.HAT_UID,shopId);
        String params = ItvJsonUtil.toJson(map);
        FinderSignService finderSignService = new FinderSignService();
        String signMsg = finderSignService.sign(platformCode, params, hatPrivateKey);
        logger.info("the signMsg is ------->{}", ItvJsonUtil.toJson(map));
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/account/balance/query", params, UUID.randomUUID().toString(), platformCode, signMsg);
        if (MapUtils.isEmpty(post)) {
            logger.info("post is empty");
            return null;
        }
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        String content = String.valueOf(post.get("content")) ;
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        if (StringUtils.isNotEmpty(content)) {
            logger.info("the post is not empty! [content = {}]",content);
            Map<String, String> responseMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            return responseMap;
        } else {
            logger.info("the have an http error!!");
        }
        return null;
    }



    /**
     * Description: 聚合签名数据(发送订单支付请求时加签用)
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/8/13 0013
     * @author: DXM
     */
    private String setParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != "") {
                returns += "&" + paramId + Constants.PARAM_FLAG + paramValue;
            }
        } else {
            if (paramValue != "") {
                returns = paramId + Constants.PARAM_FLAG + paramValue;
            }
        }
        return returns;
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-*.xml");
        SettleRecordManagerImpl bean = context.getBean(SettleRecordManagerImpl.class);
        try {
            bean.settleWithHat(new ResponseData(), 100019);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
