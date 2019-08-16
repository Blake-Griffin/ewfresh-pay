package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.SettleRecordManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.SettleRecordService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.bill99.SettleData;
import com.ewfresh.pay.util.boc.MyHttp;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/22 0022
 */
@Component
public class SettleRecordManagerImpl implements SettleRecordManager {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SettleRecordService settleRecordService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Value("${http_idgen}")
    private String ID_URL;
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    private static final String SETTLE_FLAG = "1";
    private static final BigDecimal SETTLE_COEFFICIENT = new BigDecimal(100);//结算的金额系数,以分为单位
    @Value("${HAT.settlePeriod}")
    private  String SETTLE_CYCLE;//结算周期
    private static final String SUB_FLAG = "S";//子结算批次号
    @Autowired
    private IdUtils idUtils;


    @Override
    public void getSettleRecordByShopId(ResponseData responseData, Integer shopId, Integer pageSize, Integer pageNumber, String batchNo, Short orderStatus, Short settleStatus, String successTime) {

        logger.info("Get settle records list params  are ----->[shopId = {}, pageSize = {}, pageNumber = {}, batchNo = {}, orderStatus = {}, settleStatus = {}, successTime = {}]", shopId, pageSize, pageNumber,batchNo,orderStatus,settleStatus,successTime);

        PageInfo<PayFlow> pageInfo = payFlowService.getSettlePayflows(shopId, pageSize, pageNumber,batchNo,orderStatus,settleStatus,successTime);
        List<PayFlow> payFlows = pageInfo.getList();
        responseData.setEntity(payFlows);
        responseData.setTotal(pageInfo.getTotal()+"");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(pageInfo.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public void addSettleRecord(ResponseData responseData, Integer shopId, List<Integer> payflows, String settleFlag, String batch) {
        logger.info("add-SettleRecord params  are ----->[shopId = {}, payflows = {}, settleFlag = {}]", shopId, ItvJsonUtil.toJson(payflows), settleFlag);
        BigDecimal paySettleAmount = new BigDecimal(Constants.NULL_BALANCE);
        BigDecimal refundSettleAmount = new BigDecimal(Constants.NULL_BALANCE);
        BigDecimal totalSettleAmount ;
        Short orderStatus = Constants.STATUS_1;
        Short settleStatus = Constants.STATUS_0;
        String successTime = "";
        List<PayFlow> settlePayflows = null;
        //判断是否申请全部的可结算流水
        if(StringUtils.isNotBlank(settleFlag) && settleFlag.equals(SETTLE_FLAG)){
            //证明为全部申请审核
            //1获取所有可结算的payflow
            settlePayflows = payFlowService.getAllSettlePayflows(shopId, orderStatus,settleStatus,null);
        }else {
            settlePayflows = payFlowService.getPayflowsByIds(payflows);
        }
        if (CollectionUtils.isEmpty(settlePayflows)){
            logger.info("get empty payflows [shopId = {}]",shopId);
            responseData.setMsg("empty payflows");
            responseData.setCode(ResponseStatus.NULLPAYFLOWS.getValue());
            return;
        }
        //设置settleRecord的id
        String idStr = idUtils.getId(ID_URL, Constants.SETTLERECORD);
        Integer id = Integer.valueOf(idStr);
        Date now = new Date();
        totalSettleAmount = getTotalSettleAmount(settlePayflows,paySettleAmount,refundSettleAmount,idStr);
        if(totalSettleAmount.compareTo(new BigDecimal(Constants.NULL_BALANCE)) == -1){
            //结算金额小于0
            responseData.setCode(ResponseStatus.SETTLEAMOUNTLT0.getValue());
            responseData.setMsg("settle amount less than 0 ");
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("paySettle",paySettleAmount.toString());
        map.put("refundSettle",refundSettleAmount.toString());
        map.put("totalSettle",totalSettleAmount.toString());
        SettleRecord settleRecord = new SettleRecord();
        settleRecord.setId(id);
        settleRecord.setShopId(shopId);
        String shopName = accountFlowRedisService.getShopInfoFromRedis(Constants.SHOP_ADDSHOP_REDIS, shopId.toString());
        settleRecord.setShopName(shopName);
        settleRecord.setAmount(totalSettleAmount);
        settleRecord.setSettleDate(now);
        BigDecimal settleFe = new BigDecimal(0);
        settleRecord.setSettleFee(settleFe);
        settleRecord.setRemitAmount(totalSettleAmount.subtract(settleFe));
        settleRecordService.addSettleRecord(settleRecord,settlePayflows,batch);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setEntity(map);
        responseData.setMsg("add settle record success");

    }

    

    public String getBatchNo(Date date){
        String format = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    private BigDecimal getTotalSettleAmount(List<PayFlow> settlePayflows, BigDecimal paySettleAmount, BigDecimal refundSettleAmount, String batchNo){

        for (PayFlow settlePayflow : settlePayflows) {
            //获取运算的加减标识
            Short tradeType = settlePayflow.getTradeType();
            boolean flag = getFlag(tradeType);
            BigDecimal payerPayAmount = settlePayflow.getPayerPayAmount();
            logger.info("this payflow trade type [tradeType = {}, flag = {}]",tradeType,flag);
            if (flag){
                paySettleAmount = paySettleAmount.add(payerPayAmount);
            }else {
                refundSettleAmount = refundSettleAmount.add(payerPayAmount);
            }
            settlePayflow.setBatchNo(batchNo);
        }
        BigDecimal totalSettleAmount = paySettleAmount.subtract(refundSettleAmount);
        logger.info("this settle result [paySettleAmount = {}, refundSettleAmount = {},totalSettleAmount = {}]",paySettleAmount,refundSettleAmount,totalSettleAmount);
        return totalSettleAmount;
    }


    public boolean getFlag(Short tradeType){
        boolean flag =true;
        if (tradeType == Constants.TRADE_TYPE_3 || tradeType == Constants.TRADE_TYPE_8 || tradeType == Constants.TRADE_TYPE_1 || tradeType == Constants.TRADE_TYPE_4) {
            flag = true;
        }
        if (tradeType == Constants.TRADE_TYPE_2 || tradeType == Constants.TRADE_TYPE_5 || tradeType == Constants.TRADE_TYPE_9 || tradeType == Constants.TRADE_TYPE_10 || tradeType == Constants.TRADE_TYPE_11) {
            flag = false;
        }
        return flag;
    }


    @Override
    public void getSettleRecords(ResponseData responseData, String shopName, Integer shopId, Short settleStatus, Integer pageSize, Integer pageNumber) {
        logger.info("get-settleRecords params  are ----->[shopName = {}, settleStatus = {}, pageSize = {}, pageNumber = {}]",shopName,pageSize,pageNumber);
        PageInfo<SettleRecord> pageInfo = settleRecordService.getSettleRecords(shopName,shopId,settleStatus,pageSize,pageNumber);
        List<SettleRecord> list = pageInfo.getList();
        responseData.setEntity(list);
        responseData.setTotal(pageInfo.getTotal()+"");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(pageInfo.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public void getSettleRecordsByBatchNo(ResponseData responseData, String batchNo, Integer pageSize, Integer pageNumber) {
        logger.info("get-settleRecords-by-batchNo  params in manager  are ----->[batchNo = {}, pageSize = {}, pageNumber = {}]",batchNo,pageSize,pageNumber);
        PageInfo<PayFlow> pageInfo = payFlowService.getSettleRecordsByBatchNo(batchNo,pageSize,pageNumber);
        List<PayFlow> list = pageInfo.getList();
        responseData.setEntity(list);
        responseData.setTotal(pageInfo.getTotal()+"");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(pageInfo.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

    }

    @Override
    public void apprByBatchNo(ResponseData responseData, SettleRecord settleRecord) {
        logger.info("appr-by-batchNo params in manager are ----->[settleRecord = {}]",ItvJsonUtil.toJson(settleRecord));
        settleRecordService.apprByBatchNo(settleRecord);
        responseData.setMsg("appr success");
        responseData.setCode(ResponseStatus.OK.getValue());
    }


    @Override
    public void settleWithHat(ResponseData responseData,Integer id) throws Exception {
        logger.info("settle-with-hat params in manager are ----->[id = {}]",id);

        SettleRecord settleRecord = settleRecordService.getSettleRecordById(id);
        Short settleStatus = settleRecord.getSettleStatus();
        if (settleStatus.shortValue() != Constants.SHORT_ONE.shortValue()){
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return;
        }
        Date now = new Date();
        String  batchNo = getBatchNo(now);
        Integer shopId = settleRecord.getShopId();
        batchNo = batchNo + shopId;
        settleRecord.setBatchNo(batchNo);
        settleRecord.setRemitTime(now);
        String settleResult = sendSettleRequest(responseData, settleRecord);
        if (StringUtils.isNotBlank(settleResult) && settleResult.equals(Constants.HAT_SUCCESS_0000)){
            settleRecord.setSettleStatus(Constants.APPR_STATUS_3);
        }else {
            settleRecord.setSettleStatus(Constants.APPR_STATUS_5);
        }
        settleRecordService.updateBatchNoById(settleRecord);
    }



    public String sendSettleRequest(ResponseData responseData,SettleRecord settleRecord) throws Exception {
        // 1.获取配置信息
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        Map<String, Object> map = new HashMap<>();
        // 2. 设置请求信息
        SettleData settleData = new SettleData();
        List<SettleData> settleDatas = new ArrayList<>();
        BigDecimal amount = settleRecord.getAmount();
        String amountStr = CommonUtils.yuanToFee(amount.toString());
        settleData.setAmount(amountStr);
        Integer shopId = settleRecord.getShopId();
        String batchNo = settleRecord.getBatchNo();
        String subBatchNo = SUB_FLAG + batchNo;
        settleData.setMerchantUid(shopId.toString());
        settleData.setOutSubOrderNo(subBatchNo);
        settleData.setSettlePeriod(SETTLE_CYCLE);
        settleDatas.add(settleData);
        map.put("outOrderNo", batchNo);//分账批次号
        map.put("platformCode", platformCode);
        map.put("totalAmount",amountStr);
        map.put("feePayerUid",shopId);
        map.put("settleData",settleDatas);
        // 签名
        FinderSignService finderSignService = new FinderSignService();
        String paramStrs = ItvJsonUtil.toJson(map);
        logger.info("this request for settle with hat [paramStrs = {}]",paramStrs);
        String signMsg = finderSignService.sign(platformCode,paramStrs , hatPrivateKey);
        logger.info("the signMsg is ------->{}", ItvJsonUtil.toJson(map));
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/settle/pay", paramStrs, UUID.randomUUID().toString(), platformCode, signMsg);
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
            String rspCode = responseMap.get("rspCode");
            String rspMsg = responseMap.get("rspMsg");
            if (rspCode.equals(Constants.HAT_SUCCESS_0000)) {
                logger.info("settle with hat success [paramStrs = {}]",paramStrs);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("settle success");
                return rspCode;
            } else if (rspCode.equals(Constants.HAT_SETTLE_2000)) {
                responseData.setCode(ResponseStatus.AMOUNTERR.getValue());
                responseData.setMsg("settle amount err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2001)) {
                responseData.setCode(ResponseStatus.ERRSETTLEDATE.getValue());
                responseData.setMsg("settle date err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2003)) {
                responseData.setCode(ResponseStatus.OUTTRADENOEXIST.getValue());
                responseData.setMsg("settle is exist");
            } else{
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(rspCode + rspMsg);
            }
        } else {
            logger.info("the have an http error!!");
        }
        return content;
    }

    @Override
    public void getSettleRecordById(ResponseData responseData, Integer id) {
        logger.info("getSettleRecordById params in manager are ----->[id = {}]",id);
        SettleRecord settleRecordByBatchNo = settleRecordService.getSettleRecordById(id);
        responseData.setEntity(settleRecordByBatchNo);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public void querySettleRecordById(ResponseData responseData, String batchNo) throws Exception {
        logger.info("getSettleRecordById params in manager are ----->[batchNo = {}]",batchNo);
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        Map<String, Object> map = new HashMap<>();
        // 2. 设置请求信息

        map.put("outOrderNo", batchNo);//分账批次号
        map.put("platformCode", platformCode);
        // 签名
        FinderSignService finderSignService = new FinderSignService();
        String paramStrs = ItvJsonUtil.toJson(map);
        logger.info("this request for settle with hat [paramStrs = {}]",paramStrs);
        String signMsg = finderSignService.sign(platformCode,paramStrs , hatPrivateKey);
        logger.info("the signMsg is ------->{}", ItvJsonUtil.toJson(map));
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/settle/detail", paramStrs, UUID.randomUUID().toString(), platformCode, signMsg);
        if (MapUtils.isEmpty(post)) {
            logger.info("post is empty");
            responseData.setMsg("the response is null");
            responseData.setCode(ResponseStatus.OK.getValue());
            return ;
        }
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        String content = String.valueOf(post.get("content")) ;
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        if (StringUtils.isNotEmpty(content)) {
            logger.info("the post is not empty! [content = {}]",content);
            Map<String, String> responseMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            String rspCode = responseMap.get("rspCode");
            String rspMsg = responseMap.get("rspMsg");
            if (rspCode.equals(Constants.HAT_SUCCESS_0000)) {
                logger.info("settle with hat success [paramStrs = {}]",paramStrs);
                responseData.setEntity(content);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("settle success");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2000)) {
                responseData.setCode(ResponseStatus.AMOUNTERR.getValue());
                responseData.setMsg("settle amount err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2001)) {
                responseData.setCode(ResponseStatus.ERRSETTLEDATE.getValue());
                responseData.setMsg("settle date err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2003)) {
                responseData.setCode(ResponseStatus.OUTTRADENOEXIST.getValue());
                responseData.setMsg("settle is exist");
            } else{
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(rspCode + rspMsg);
            }
        } else {
            logger.info("the have an http error!!");
        }
        return ;
    }


    public String settleRefund(ResponseData responseData,SettleRecord settleRecord) throws Exception {
        // 1.获取配置信息
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        Map<String, Object> map = new HashMap<>();
        // 2. 设置请求信息
        SettleData settleData = new SettleData();
        List<SettleData> settleDatas = new ArrayList<>();
        BigDecimal amount = settleRecord.getAmount();
        String amountStr = CommonUtils.yuanToFee(amount.toString());
        settleData.setAmount(amountStr);
        Integer shopId = settleRecord.getShopId();
        String batchNo = settleRecord.getBatchNo();
        String refundBatchNo = getBatchNo(new Date());
        String subBatchNo = SUB_FLAG + refundBatchNo;
        settleData.setMerchantUid(shopId.toString());
        settleData.setOutSubOrderNo(subBatchNo);
        settleData.setOrigOutSubOrderNo(SUB_FLAG + batchNo);
        settleData.setSettlePeriod(SETTLE_CYCLE);
        settleDatas.add(settleData);
        map.put("outOrderNo", refundBatchNo);//退货分账批次号
        map.put("platformCode", platformCode);
        map.put("totalAmount",amountStr);
        map.put("origOutOrderNo",batchNo);//正向分账批次号
        map.put("settleData",settleDatas);
        // 签名
        FinderSignService finderSignService = new FinderSignService();
        String paramStrs = ItvJsonUtil.toJson(map);
        logger.info("this request for settle with hat [paramStrs = {}]",paramStrs);
        String signMsg = finderSignService.sign(platformCode,paramStrs , hatPrivateKey);
        logger.info("the signMsg is ------->{}", ItvJsonUtil.toJson(map));
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/settle/refund", paramStrs, UUID.randomUUID().toString(), platformCode, signMsg);
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
            String rspCode = responseMap.get("rspCode");
            String rspMsg = responseMap.get("rspMsg");
            if (rspCode.equals(Constants.HAT_SUCCESS_0000)) {
                logger.info("settle with hat success [paramStrs = {}]",paramStrs);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("settle success");
                return rspCode;
            } else if (rspCode.equals(Constants.HAT_SETTLE_2000)) {
                responseData.setCode(ResponseStatus.AMOUNTERR.getValue());
                responseData.setMsg("settle amount err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2001)) {
                responseData.setCode(ResponseStatus.ERRSETTLEDATE.getValue());
                responseData.setMsg("settle date err");
            } else if (rspCode.equals(Constants.HAT_SETTLE_2003)) {
                responseData.setCode(ResponseStatus.OUTTRADENOEXIST.getValue());
                responseData.setMsg("settle is exist");
            } else{
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(rspCode + rspMsg);
            }
        } else {
            logger.info("the have an http error!!");
        }
        return content;
    }

    @Override
    public void settleRefund(ResponseData responseData, Integer id, BigDecimal refundAmount) throws Exception {
        logger.info("settleRefund params in manager  are ----->[id = {}]",id);
        SettleRecord settleRecord = settleRecordService.getSettleRecordById(id);
        settleRecord.setAmount(refundAmount);
        settleRefund(responseData,settleRecord);
        accountFlowRedisService.setQueryBalanceByHAT(settleRecord.getShopId().toString());
    }
}
