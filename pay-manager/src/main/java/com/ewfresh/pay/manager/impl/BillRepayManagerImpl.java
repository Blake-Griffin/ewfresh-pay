package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.manager.BillRepayManager;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisService.BillRepayRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.BillRepayFlowService;
import com.ewfresh.pay.service.BillRepayService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;



@Component
public class BillRepayManagerImpl implements BillRepayManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BillRepayService billRepayService;
    @Autowired
    private BillRepayRedisService billRepayRedisService;
    @Autowired
    private  BillRepayFlowService billRepayFlowService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private HttpDeal httpDeal;


    private static final String ORDERID = "orderId";          // 订单ID
    private static final String PAY_MENT = "payment";         // 应付金额
    private static final String PAY_MODE = "payMode";         // 支付渠道 : 中国银联H5 ; 微信
    private static final String SURPLUS_AMOUNT = "surplus"; //其他渠道支付金额;
    private static final String BALANCE_PAYMENT = "balance"; //余额使用金额;
    private static final String ORDER_IP = "orderIp";         // 下单ip
    private static final String CREATETIME = "createTime";    // 订单创建时间
    private static final String ORDER_AMOUNT = "orderAmount"; // 订单金额
    private static final String PAY_TIMESTAMP = "payTimestamp";//时间戳
    private static final String UID = "uid"; //用户id
    private static final String INTERACTION_ID = "interactionId";//第三方交互订单号
    private static final BigDecimal Zero = new BigDecimal(0);
    private static final String IS_RECHARGE = "isRecharge"; //是否为充值
    private static final String ORDERSTATUS ="orderStatus"; //订单状态
    private static final String ID="id";
    private static final String UNAME="uname";
    private static final String SUCCESS_TIME="successTime";
    private static final String RECEIVER_USER_ID = "receiverUserId";//收款人id
    private static final String RECEIVER_FEE = "receiverFee";//收款方手续费
    private static final String PAYER_ID = "payerId";//付款人ID
    private static final String PAYER_PAY_AMOUNT = "payerPayAmount";//付款方支付金额
    private static final String PLATINCOME = "platIncome";//平台收入
    private static final String CHANNELTYPE ="channelType"; //渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
    private static final String CLIENT = "client"; // 客户端类型：1pc; 2android; 3ios; 4wap
    private static final String FREIGHT = "freight" ; // 运费
    private static final String CARDTYPE = "cardType"; // 银行卡类型 借记卡: borrow 贷记卡 loan
    private static final String BIZTYPE = "bizType" ; //网银支付类型 :B2B企业网银支付 B2C个人网银支付
    private static final String BIZ_TYPE_B2B = "B2B";//网银支付类型，B2B企业网银支付 B2C个人网银支付
    private static final String BIZ_TYPE_B2C = "B2C";//网银支付类型，B2B企业网银支付 B2C个人网银支付
    private static final String PAY_MODE_H5 = "中国银联H5Pay";
    private static final String PAY_MODE_QR_CODE = "中国银联QrCode";
    private static final String CARD_TYPE_BORROW = "borrow";//银行卡类型：借记卡：borrow   贷记卡：loan
    private static final String CARD_TYPE_LOAN = "loan";//银行卡类型：借记卡：borrow   贷记卡：loan
    private static final String FEE_RATE = "feeRate";//费率
    private static final String SHOPID = "shopId" ;//店铺id
    private static final String TYPE_NAME = "typeName";//支付类型名称
    private static final String TYPE_CODE = "typeCode"; //支付类型编号
    private static final String CHANNEL_FLOW_ID = "channelFlowId";//支付渠道流水号
    @Value("${http_idgen}")
    private String ID_URL;
    @Value("${H5Pay.msgSrcId}")
    private  String H5PAY_SRCID;
    @Value("${QRCode.msgSrcId}")
    private String QRCODE_SRCID;
    @Override
    /**
     * description:展示白条账单
     * @author huboyang
     * @param
     */
    public void getAllBillByUid(ResponseData responseData, Long uid) {
        logger.info("uid ={}",uid);
        BigDecimal amount=null;
        BigDecimal interest=null;
        List<BillVo> billVoList=billRepayService.getBillsByUid(uid);
        logger.info("billVoList={}", JsonUtil.toJson(billVoList));
        List<BillVo> billList = new ArrayList<>();
        for (BillVo billVo : billVoList) {
            amount =billVo.getBillAmount().subtract(billVo.getRepaidAmount());
            interest=billVo.getTotalInterest().subtract(billVo.getRepaidInterest());
            billVo.setPayableAmount(amount);//应还本金金额
            billVo.setPayableInterest(interest);//应还利息
            billVo.setTotalSum(amount.add(interest));
            billList.add(billVo);
        }
        responseData.setEntity(billList);
    }
    /**
     * description: 返回白条还款详情
     *
     * @param
     * @author huboyang
     */
    @Override
    public void getBillDetails(ResponseData responseData, String payMode, String orderIp, String uid, String payTimestamp,
                               String ids, String uname,String payType,String channelType,String client,String bizType,String cardType ) {
        logger.info("payType={},payMode={},orderIp={},uid={},payTimestamp={},ids={},uname={},channelType={},client={}",
                payType,payMode,orderIp,uid,payTimestamp,ids,uname,channelType,client);
        HashMap<String, Object> map = new HashMap<>();
        //查到客户最近的一个资金流 吧需要付款的余额冻结
        AccountFlowVo accountFlowVo = accountFlowService.getAccountFlowByUid(uid);
        List<Integer> billFlowList = new ArrayList<>();
        //id生成器生成whiteOrderId
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
        String idGenerator = getIdGenerator();

        String replace = formatter.format(date).replace("-", "").replace(" ", "").replace(":", "");
        Long whiteOrderId = Long.valueOf(replace+idGenerator);
        logger.info("whiteOrderId={}",whiteOrderId);
        BigDecimal totalbillAmount = new BigDecimal(0);//总金额orderAmount
        BigDecimal payableInterest = new BigDecimal(0);//应还利息
        BigDecimal payableAmount = new BigDecimal(0);//应还金额
        BigDecimal everyShowdPay = new BigDecimal(0);
        String[] split = ids.split(",");
        BigDecimal totalInterest = new BigDecimal(0);
        for (String billId : split) {
            BigDecimal b = new BigDecimal(0);
            BillVo billVo = billRepayService.getBillByBillId(Integer.valueOf(billId));
            billFlowList.add(billVo.getId());
            payableAmount = billVo.getBillAmount().subtract(billVo.getRepaidAmount());
            payableInterest = billVo.getTotalInterest().subtract(billVo.getRepaidInterest());
            totalInterest=totalInterest.add(payableInterest);
            everyShowdPay = payableAmount.add(payableInterest);
            totalbillAmount = totalbillAmount.add(everyShowdPay);
        }
        BigDecimal b = totalbillAmount;
        //查询用户最近的一笔流水得到可用的余额
        BigDecimal blance = accountFlowVo.getBalance().subtract(accountFlowVo.getFreezeAmount());
        if (blance.compareTo(new BigDecimal(0))==1) {
            blance=accountFlowVo.getBalance().subtract(accountFlowVo.getFreezeAmount());
        }
        else{
            blance=new BigDecimal(0);
        }
        //余额支付
        if (payType.equals("余额")) {
            //余额充足
            if(blance.compareTo(totalbillAmount)!=-1){
                PayFlow payFlow = new PayFlow();
                logger.info("payMode={},blance={},payType={}",payMode,blance,payType);
                //冻结金额为原来的冻结金额加上应还的金额
                accountFlowVo.setBillInsertt(totalInterest);
                accountFlowVo.setFreezeAmount(accountFlowVo.getFreezeAmount().add(totalbillAmount));//冻结金额
                accountFlowVo.setDirection(Constants.DIRECTION_OUT);//资金流向
                accountFlowVo.setUname(accountFlowVo.getUname());
                accountFlowVo.setSrcAcc(Constants.BALANCE);//源账户
                accountFlowVo.setTargetAcc(Constants.SUNKFA);//目标账户
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);//源账户类型(1个人,2店铺)
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);//目标账户类型(1个人,2店铺)
                accountFlowVo.setBusiNo(idGenerator);//业务流水号
                accountFlowVo.setBusiType(Constants.BUSI_TYPE_21);//业务类型
                accountFlowVo.setAccType(Constants.ACC_TYPE_4);//账户类型
                //本次应还的金额
                accountFlowVo.setAmount(totalbillAmount);//涉及金额
                accountFlowVo.setOperator(uid);
                accountFlowVo.setPayFlowId(0);
                accountFlowVo.setIsBalance(Constants.SHORT_ZERO);
                //原有余额
                accountFlowVo.setBalance(accountFlowVo.getBalance());//余额
                accountFlowVo.setDesp("订单:"+idGenerator+"冻结本次还款余额");
                logger.info("accountFlowVo={}",ItvJsonUtil.toJson(accountFlowVo));
                payFlow.setOrderId(Long.valueOf(idGenerator));
                payFlow.setChannelFlowId(getBalanceChanelFlow().toString());
                payFlow.setPayerId(uid);
                payFlow.setUname(accountFlowVo.getUname());
                payFlow.setPayerName(accountFlowVo.getUname());
                payFlow.setPayerPayAmount(totalbillAmount);
                payFlow.setPayerFee(new BigDecimal(0));
                payFlow.setPayerType(Constants.SHORT_ONE);
                payFlow.setOrderIp(orderIp);
                payFlow.setOrderAmount(totalbillAmount);
                payFlow.setChannelCode(Constants.UID_BALANCE);
                payFlow.setChannelName(Constants.BALANCE);
                payFlow.setSuccessTime(new Date());
                payFlow.setCompleteTime(new Date());
                payFlow.setTradeType(Constants.TRADE_TYPE_15);
                payFlow.setCreateTime(new Date());
                payFlow.setIsBlc(Constants.SHORT_ONE);
                payFlow.setOrderStatus(Constants.STATUS_0);
                payFlow.setReceiverName(Constants.RECEIVERNAME);
                payFlow.setSettleStatus(Constants.SHORT_ONE);

                logger.info("payFlow={}",ItvJsonUtil.toJson(payFlow));
                billRepayRedisService.addPayFlow(payFlow, accountFlowVo, billFlowList,whiteOrderId,idGenerator);

                responseData.setEntity(Constants.BALANCE_REPAY_OK);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
            }else{
                responseData.setCode(ResponseStatus.BALANCEISNOTENOUGH.getValue());
                responseData.setMsg(ResponseStatus.BALANCEISNOTENOUGH.name());
                responseData.setEntity(Constants.BALANCE_NOT_ENOUGH);
                return;
            }

        }
        //使用银联支付
        if (payType.equals("快钱")) {
           if (StringUtils.isBlank(payMode) || StringUtils.isBlank(client) ){
                logger.info("repay Details param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
           /* BigDecimal unionPayFate = getUnionPayFate(payMode, client, bizType, cardType);
            logger.info("payMode={},payType={},unionPayFate={}",payMode,payType,unionPayFate);*//*
            如果费率不为0 需要计算手续费
            if(unionPayFate.compareTo(BigDecimal.ZERO) != 0){
               BigDecimal multiply = totalbillAmount.multiply(unionPayFate);//手续费
                map.put(FEE_RATE,BigDecimal.ZERO);
            }
            f (payMode.equals("中国银联H5Pay")){
                map.put(ORDERID, idGenerator);//订单id
                map.put(INTERACTION_ID,H5PAY_SRCID+whiteOrderId);//第三方交互订单号
                map.put(PAY_MODE, payType);//支付方式
            }
            if(payMode.equals("中国银联QrCode")){
                map.put(ORDERID,idGenerator);//订单id
                map.put(INTERACTION_ID,QRCODE_SRCID+whiteOrderId);//第三方交互订单号
                map.put(PAY_MODE, payType);
            }
            if(payMode.equals("中国银联H5PayB2B")){
                map.put(ORDERID,idGenerator);//订单id
                map.put(INTERACTION_ID,H5PAY_SRCID+whiteOrderId);//第三方交互订单号
                map.put(PAY_MODE, payType);
            }*/
            map.put(FEE_RATE,BigDecimal.ZERO);
            map.put(ORDERID,idGenerator);//订单id
            map.put(INTERACTION_ID,whiteOrderId);//第三方交互订单号
            map.put(PAY_MODE, payType);
            map.put(ID,idGenerator);
            map.put(TYPE_NAME,payMode);
            map.put(TYPE_CODE,"6");
            map.put(RECEIVER_FEE,Constants.BIGDECIMAL_ZERO);//收款方手续费
            map.put(CHANNEL_FLOW_ID, getBalanceChanelFlow().toString()); //第三方支付流水号
            map.put(CLIENT,client);
            map.put(FREIGHT,Constants.BIGDECIMAL_ZERO);                   //运费
            map.put(CHANNELTYPE,channelType);
            map.put(PAYER_ID,uid);
            map.put(SHOPID,"0");
            map.put(PAYER_PAY_AMOUNT,totalbillAmount.subtract(blance));
            map.put(BIZTYPE,bizType);//网银支付类型 :B2B企业网银支付 B2C个人网银支付
            map.put(CARDTYPE,cardType);// 银行卡类型 借记卡: borrow 贷记卡 loan
            map.put(SUCCESS_TIME,payTimestamp);
            map.put(PLATINCOME,new BigDecimal(0));
            map.put(RECEIVER_USER_ID,"10000");//收款人id
            map.put(PAY_MENT, totalbillAmount);//应付金额
            map.put(ORDER_IP, orderIp);//订单ip
            //创建时间
            map.put(CREATETIME, System.currentTimeMillis());//创建时间
            map.put(SURPLUS_AMOUNT, totalbillAmount);//第三方支付金额
            map.put(BALANCE_PAYMENT, Zero);//余额付款金额
            map.put(ORDER_AMOUNT, totalbillAmount);//订单金额
            map.put(PAY_TIMESTAMP, payTimestamp);//支付时间 就是点下一步的时间
            map.put(UID, uid);//Uid
            map.put(IS_RECHARGE,Constants.TRADE_TYPE_15);
            map.put(ORDERSTATUS,Constants.INTEGER_ZERO);
            map.put(UNAME,accountFlowVo.getUname());
            logger.info("map={}",ItvJsonUtil.toJson(map));
            billRepayService.addWhiteOrder( map, billFlowList,whiteOrderId,idGenerator);
            responseData.setEntity(map);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());

        }
        //混合支付
        if (payType.equals("混合")) {
            logger.info("payMode={},blance={},payType={},client={}",payMode,blance,payType,client);
            //如果可用余额小于应付金额
            if (blance.compareTo(totalbillAmount) == -1) {
                //从redis中查询次批账单是否已经支付过如果没有直接走下面,如果有的话
                if (StringUtils.isBlank(payMode) || StringUtils.isBlank(client)  ){
                    logger.info("repay Details param   is null");
                    responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                    responseData.setMsg(ResponseStatus.PARAMNULL.name());
                    return;
                }
                Map<String, Object> whiteOrderIByIds = billRepayService.getWhiteOrderIByIds(ids);
                logger.info("whiteOrderIByIds={}",JsonUtil.toJson(whiteOrderIByIds));
                if(whiteOrderIByIds!=null){
                    responseData.setEntity(whiteOrderIByIds);
                    responseData.setCode(ResponseStatus.OK.getValue());
                    responseData.setMsg(ResponseStatus.OK.name());
                    return;
                }
                //冻结金额为可用余额加上原来冻结金额
                accountFlowVo.setFreezeAmount(accountFlowVo.getFreezeAmount().add(blance));//冻结金额
                accountFlowVo.setDirection(Constants.DIRECTION_OUT);//资金流向
                accountFlowVo.setSrcAcc(Constants.BALANCE);//源账户
                accountFlowVo.setTargetAcc(Constants.SUNKFA);//目标账户
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);//源账户类型(1个人,2店铺)
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);//目标账户类型(1个人,2店铺)
                accountFlowVo.setBusiNo(idGenerator);//业务流水号
                accountFlowVo.setBusiType(Constants.BUSI_TYPE_21);//业务类型
                accountFlowVo.setAccType(Constants.ACC_TYPE_4);//账户类型
                accountFlowVo.setAmount(blance);//涉及金额为可用余额
                accountFlowVo.setBalance(accountFlowVo.getBalance());//余额
                accountFlowVo.setDesp("订单:"+idGenerator+"冻结本次还款余额");
                accountFlowVo.setUname(accountFlowVo.getUname());
                accountFlowVo.setOperator(uid);
                accountFlowVo.setPayFlowId(0);
                accountFlowVo.setBillInsertt(totalInterest);
                logger.info("accountFlowVo={}",ItvJsonUtil.toJson(accountFlowVo));
              /* 如果费率不为零计算手续费
                BigDecimal unionPayFate = getUnionPayFate(payMode, client, bizType, cardType);
                logger.info("payMode={},payType={},unionPayFate={}",payMode,payType,unionPayFate);*//*
                如果手续费不为0 需要计算手续费
                if(unionPayFate.compareTo(BigDecimal.ZERO) != 0){
                    BigDecimal multiply = totalbillAmount.multiply(unionPayFate);//手续费
                    map.put(RECEIVER_FEE,multiply.toString());
                    map.put(FEE_RATE,unionPayFate);
                }
                if (payMode.equals("中国银联H5Pay")){
                    map.put(PAY_MODE, payType);
                    map.put(ORDERID, idGenerator);//订单id
                    map.put(INTERACTION_ID, H5PAY_SRCID+whiteOrderId);//第三方交互订单号
                }
                if(payMode.equals("中国银联QrCode")){
                    map.put(PAY_MODE, payType);
                    map.put(ORDERID, idGenerator);//订单id
                    map.put(INTERACTION_ID, QRCODE_SRCID+whiteOrderId);//第三方交互订单号
                }
                if(payMode.equals("中国银联H5PayB2B")){
                    map.put(ORDERID,idGenerator);//订单id
                    map.put(INTERACTION_ID,H5PAY_SRCID+whiteOrderId);//第三方交互订单号
                    map.put(PAY_MODE, payType);
                }*/
                map.put(ORDERID,idGenerator);//订单id
                map.put(INTERACTION_ID,whiteOrderId);//第三方交互订单号
                map.put(PAY_MODE, payType);
                map.put(FEE_RATE,BigDecimal.ZERO);
                map.put(ID,idGenerator);
                map.put(TYPE_NAME,payMode);
                map.put(TYPE_CODE,"6");
                map.put(RECEIVER_FEE,Constants.BIGDECIMAL_ZERO);//收款方手续费
                map.put(CHANNEL_FLOW_ID, getBalanceChanelFlow().toString()); //第三方支付流水号
                map.put(PAYER_ID,uid);
                map.put(SHOPID,"0");
                map.put(BIZTYPE,bizType);//网银支付类型 :B2B企业网银支付 B2C个人网银支付
                map.put(CARDTYPE,cardType);// 银行卡类型 借记卡: borrow 贷记卡 loan
                map.put(FREIGHT,Constants.BIGDECIMAL_ZERO);                   //运费
                map.put(PAYER_PAY_AMOUNT,totalbillAmount.subtract(blance));
                map.put(SUCCESS_TIME,payTimestamp);
                map.put(PLATINCOME,new BigDecimal(0));
                map.put(RECEIVER_USER_ID,"10000");//收款人id
                map.put(PAY_MENT, totalbillAmount);//应付金额
                map.put(CLIENT,client);
                map.put(CHANNELTYPE,channelType);
                map.put(ORDER_IP, orderIp);
                map.put(CREATETIME, System.currentTimeMillis());//订单创建时间
                map.put(SURPLUS_AMOUNT, totalbillAmount.subtract(blance));//第三方支付金额
                map.put(BALANCE_PAYMENT, blance);//余额付款金额
                map.put(ORDER_AMOUNT, totalbillAmount);//订单金额
                map.put(PAY_TIMESTAMP, payTimestamp);//点下一步时间
                map.put(UID, uid);
                map.put(UNAME,accountFlowVo.getUname());
                map.put(IS_RECHARGE,Constants.TRADE_TYPE_15);
                map.put(ORDERSTATUS,Constants.INTEGER_ZERO);
                map.put(whiteOrderId.toString(),ids);

                logger.info("map={}",ItvJsonUtil.toJson(map));
                billRepayService.addAccountFlow(accountFlowVo,map, billFlowList,whiteOrderId,idGenerator);
                responseData.setEntity(map);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());

            }
            //余额充足的情况
            if (blance.compareTo(totalbillAmount)!=-1){
                logger.info("payMode={},blance={}",payMode,blance);
                PayFlow payFlow = new PayFlow();
                //涉及金额为应还金额
                accountFlowVo.setAmount(totalbillAmount);
                //冻结金额为原来的冻结金额加上应还金额
                accountFlowVo.setFreezeAmount(accountFlowVo.getFreezeAmount().add(totalbillAmount));//冻结金额
                accountFlowVo.setDirection(Constants.DIRECTION_OUT);//资金流向
                accountFlowVo.setSrcAcc(Constants.BALANCE);//源账户
                accountFlowVo.setTargetAcc(Constants.SUNKFA);//目标账户
                accountFlowVo.setSrcAccType(Constants.SHORT_ONE);//源账户类型(1个人,2店铺)
                accountFlowVo.setTargetAccType(Constants.SHORT_ONE);//目标账户类型(1个人,2店铺)
                accountFlowVo.setBusiNo(idGenerator);//业务流水号
                accountFlowVo.setBusiType(Constants.BUSI_TYPE_21);//业务类型 21
                accountFlowVo.setAccType(Constants.ACC_TYPE_4);//账户类型
                accountFlowVo.setAmount(totalbillAmount);//涉及金额
                accountFlowVo.setBalance(accountFlowVo.getBalance());
                accountFlowVo.setDesp("订单:"+whiteOrderId+"冻结本次还款余额");
                accountFlowVo.setUname(accountFlowVo.getUname());
                accountFlowVo.setOperator(uid);
                accountFlowVo.setPayFlowId(0);
                accountFlowVo.setBillInsertt(totalInterest);
                logger.info("accountFlowVo",ItvJsonUtil.toJson(accountFlowVo));
                payFlow.setOrderId(Long.valueOf(idGenerator));
                payFlow.setPayerName(accountFlowVo.getUname());
                payFlow.setChannelFlowId(getBalanceChanelFlow().toString());
                payFlow.setPayerId(uid);
                payFlow.setUname(accountFlowVo.getUname());
                payFlow.setPayerPayAmount(blance.subtract(totalbillAmount));
                payFlow.setPayerFee(new BigDecimal(0));
                payFlow.setPayerType(Constants.SHORT_ONE);
                payFlow.setOrderIp(orderIp);
                payFlow.setOrderAmount(blance.subtract(totalbillAmount));
                payFlow.setChannelName(Constants.BALANCE);
                payFlow.setSuccessTime(new Date());
                payFlow.setCompleteTime(new Date());
                payFlow.setTradeType(Constants.TRADE_TYPE_15);
                payFlow.setIsBlc(Constants.SHORT_ONE);
                payFlow.setDesp("balanceRepay");
                payFlow.setOrderStatus(Constants.STATUS_1);
                payFlow.setReceiverName(Constants.RECEIVERNAME);
                payFlow.setSettleStatus(Constants.SHORT_ONE);
                logger.info("payFlow={}",ItvJsonUtil.toJson(payFlow));
                billRepayRedisService.addPayFlow(payFlow, accountFlowVo, billFlowList,whiteOrderId,idGenerator);

                responseData.setEntity(Constants.BALANCE_REPAY_OK);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
            }

        }
    }

    @Override
    public void getRepayRecord(ResponseData responseData, String billId, Integer pageSize, Integer pageNumber) {
        PageInfo<BillRepayFlow> billRepayFlow = billRepayFlowService.getBillRepayFlow(billId, pageSize, pageNumber);
        responseData.setEntity(billRepayFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(" param uid is Ok");
    }


    //id生成器
    public String getIdGenerator() {
        HashMap<String, String> param = new HashMap<>();
        param.put(Constants.FIRSTKEY, Constants.ID_WHITE_ORDER_KEY);
        String post = httpDeal.post(ID_URL, param, null, null);
        logger.info("==========post={}",post);
        Map<String, String> map = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String balanceId = map.get("entity");
        return balanceId;
    }

    //ID生成器获取余额支付支付渠道流水的方法
    public Long getBalanceChanelFlow() {
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        Long uid = context.getUid();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, Constants.ID_GEN_PAY_VALUE);
        String idStr = httpDeal.post(ID_URL, map, token, uid + "");
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        return Long.valueOf((Integer) hashMap.get(Constants.ENTITY));
    }

    public  BigDecimal getUnionPayFate(String payMode, String
            client, String bizType, String cardType) {
        logger.info("payMode = {}, client = {}, bizType = {}, cardType = {} ", payMode, client, bizType, cardType);
        if (StringUtils.isBlank(payMode) || StringUtils.isBlank(client)
                || StringUtils.isBlank(bizType) || StringUtils.isBlank(cardType)) {
            logger.error("The param has null part, payMode = " + payMode + ", client = "
                    + client + ", bizType = " + bizType + ", cardType = " + cardType);
            throw new RuntimeException("Params null!");
        }
        // H5支付
        if (payMode.contains(PAY_MODE_H5)) {
            // H5、B2B
            if (BIZ_TYPE_B2B.equals(bizType)) {
                return BigDecimal.ZERO;
            }
            // H5、B2C
            if (BIZ_TYPE_B2C.equals(bizType)) {
                // H5、Android、iOS的微信、支付宝
                if ("2".equals(client) || "3".equals(client)) {
                    return new BigDecimal("0.003");
                }
                // H5、PC的借记卡
                if ("1".equals(client) && cardType.equals(CARD_TYPE_BORROW)) {
                    return new BigDecimal("0.0023");
                }
                // H5、PC的贷记卡
                if ("1".equals(client) && cardType.equals(CARD_TYPE_LOAN)) {
                    return new BigDecimal("0.0048");
                }
            }
        }
        // 扫码付
        if (payMode.contains(PAY_MODE_QR_CODE)) {
            return new BigDecimal("0.003");
        }
        return BigDecimal.ZERO;
    }
}
