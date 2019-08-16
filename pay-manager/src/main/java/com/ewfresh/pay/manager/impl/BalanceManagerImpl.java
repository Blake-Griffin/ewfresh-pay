package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.manager.BalanceManager;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.WithdrawtosVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:关于余额使用的manager层实现类
 *
 * @author DuanXiangming
 * Date 2018/4/11
 */
@Component
public class BalanceManagerImpl implements BalanceManager {

    private static final Logger logger = LoggerFactory.getLogger(BalanceManagerImpl.class);

    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private RedisLockHandler lockHandler;
    @Autowired
    private WithdrawtoService withdrawtoService;
    @Autowired
    private HttpDeal httpDeal;
    @Autowired
    private MsgClient msgClient;
    private static final String IS_ENOUGH_KEY = "isEnough";

    private static final String BALANCE_PAYMENT = "balanceAmount";

    private static final String SURPLUS_AMOUNT = "surplusAmount";

    private static final String AMOUNT = "amount";

    private static final String FALSE_DESP = "充值目标错误,提现";

    private static final short IS_ENOUGH = 1;    //余额足以支付全部金额

    private static final short IS_NOT_ENOUGH = 2;//余额不足以支付全部金额

    private static final String RESPONSE_CODE = "code";

    private static final String ERR_RESPONSE_CODE = "300";

    private static final String BENEFIT = "benefit";                //店铺服务费费率(分润费率)
    private static final String BALANCE_FREIGHT = "balanceFreight"; //店铺使用余额使用的运费
    private static final String BALANCE_BENEFIT = "balanceBenefit"; //店铺服务费金额
    //已完成
    private String CASHWITHDRAWALAUDITKEY = "CASH_WITHDRAWAL_AUDIT_HAS_BEEN_COMPLTED";
    @Value("${http_update_order}")
    private String ORDER_URL;
    @Value("${http_dispatch_order}")
    private String DISPATCH_URL;
    @Value("${http_idgen}")
    private String ID_URL;
    @Value("${http_msg}")
    private String msgUrl;

    @Override
    public void freezBalance(ResponseData responseData, BigDecimal amount, Long userId, Long orderId, String targetAcc) {
        logger.info("the freezBalance param in manager is ----->[amount = {}, userId = {}, targetAcc = {}]", amount, userId, targetAcc);
        /*AccountFlow freezeAccFlow = accountFlowService.getFreezeAccFlow(orderId.toString(),amount);
        if (freezeAccFlow != null && freezeAccFlow.getIsBalance().shortValue() == Constants.SHORT_ONE){
            *//*此判断是为了防止订单的定加运金费等于尾款,如果本次冻结所查询的支付流水包含尾款,那*//*
            List<PayFlow> payFlows = payFlowService.getPayFlowByOrderId(orderId);
            if (CollectionUtils.isNotEmpty(payFlows)){
                for (PayFlow payFlow : payFlows) {
                    String interactionId = payFlow.getInteractionId();
                    if (interactionId.contains("R")){
                        responseData.setCode(ResponseStatus.ALREADYPAIED.getValue());
                        responseData.setMsg("order already paied");
                        return;
                    }
                }
            }
        }*/
        /*if (freezeAccFlow != null && freezeAccFlow.getIsBalance().shortValue() == Constants.SHORT_ZERO){
            Map<String, Object> stringObjectMap = setFreezeInfo(amount, freezeAccFlow);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg("commit agian");
            responseData.setEntity(stringObjectMap);
            return;
        }*/
        Map<String, Object> freezenInfo = accountFlowRedisService.getFreezenInfo(orderId);
        if (MapUtils.isNotEmpty(freezenInfo)){
            Object o = freezenInfo.get(Constants.ACC_FLOW_ID);
            if (o == null){
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("commit agian");
                return;
            }
            AccountFlowVo accFlowById = accountFlowService.getAccFlowById(String.valueOf(o));
            Short isBalance = accFlowById.getIsBalance();
            if (isBalance == Constants.SHORT_ONE){
                logger.info("already paied [accFlowById = {}]", ItvJsonUtil.toJson(accFlowById));
                responseData.setCode(ResponseStatus.ALREADYPAIED.getValue());
                responseData.setMsg("this order already paid");
                return;
            }
            if (isBalance == Constants.SHORT_ZERO){
                logger.info("still wait for paid [accFlowById = {}]", ItvJsonUtil.toJson(accFlowById));
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("this order already paid");
                responseData.setEntity(freezenInfo);
                return;
            }
        }

        //查询当前用户的余额,及其可用余额
        AccountFlowVo accountFlow = checkBalanceIsExist(userId.toString());
        //判断用户是否有余额
        if (accountFlow == null) {
            //用户没有余额
            responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
            responseData.setMsg("this user have no banlance");
            responseData.setEntity(Constants.NULL_BALANCE);
            return;
        }
        //账户流水不为空
        //1 获取账户可用余额
        BigDecimal availableBalance = BalanceManagerImpl.getAvailableBalance(accountFlow);
        if(availableBalance.compareTo(BigDecimal.ZERO) != 1){
            //用户没有可用余额
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg("this user banlance is zero");
            responseData.setEntity(Constants.NULL_BALANCE);
            return;
        }
        //2 判断是否为支付冻结
        // freeze available balance for pay  start
        Map<String, Object> map = new HashMap<>();
        //是支付冻结,比较支付金额和可用余额的大小
        map.put(AMOUNT, amount.doubleValue() + "");
        map.put(Constants.ORDER_ID,orderId);
        map.put(Constants.ORDER_AMOUNT,amount);
        if (availableBalance.compareTo(amount) != -1) {
            //可用余额大于支付金额
            BigDecimal freezeAmount = accountFlow.getFreezeAmount();
            accountFlow.setFreezeAmount(freezeAmount.add(amount));
            map.put(IS_ENOUGH_KEY, IS_ENOUGH); //设置是否可以用余额支付全部金额
            map.put(BALANCE_PAYMENT, amount);   //设置用余额支付的金额
            map.put(SURPLUS_AMOUNT, Constants.NULL_BALANCE);         //设置用其他支付渠道支付的金额 ,此处0代表不需要其他支付渠道,余额可以支付
            logger.info(" the freeze amount result for availableBalance greater than amount [map = {}]", ItvJsonUtil.toJson(map));
        } else {
            //可用余额小于于支付金额
            BigDecimal balance = accountFlow.getBalance();//该用户的余额
            accountFlow.setFreezeAmount(balance);
            BigDecimal surplusAmount = amount.subtract(availableBalance);//需要是用三方渠道的金额
            //该笔冻结金额为余额所剩余的金额
            amount = availableBalance;
            map.put(IS_ENOUGH_KEY, IS_NOT_ENOUGH);       //设置是否可以用余额支付全部金额
            map.put(BALANCE_PAYMENT, availableBalance);   //设置用余额支付的金额
            map.put(SURPLUS_AMOUNT, surplusAmount);       //设置用其他支付渠道支付的金额
            logger.info(" the freeze amount result for availableBalance less than amount [map = {}]", ItvJsonUtil.toJson(map));
        }
        accountFlow.setDirection(Constants.DIRECTION_IN);
        accountFlow.setSrcAcc(Constants.BALANCE);
        accountFlow.setTargetAcc(Constants.SUNKFA);
        accountFlow.setSrcAccType(Constants.SHORT_ONE);
        accountFlow.setTargetAccType(Constants.SHORT_ONE);
        accountFlow.setBusiNo(orderId + "");
        accountFlow.setBusiType(Constants.BUSI_TYPE_7);
        accountFlow.setAccType(Constants.ACC_TYPE_4);
        //freeze available balance for pay  end
        accountFlow.setPayFlowId(Constants.INTEGER_ZERO);
        accountFlow.setAmount(amount);
        accountFlowDescUtil.cassExplainByAcc(accountFlow);
        logger.info("the new account flow for this user user [id = {}, accountFlow = {}]", userId, ItvJsonUtil.toJson(accountFlow));
        int accFlow = accountFlowService.addFreezeAccFlow(accountFlow, orderId, map);
        responseData.setEntity(map);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(" freeze amount success");
    }


    private  Map<String, Object>  setFreezeInfo( BigDecimal amount, AccountFlow freezeAccFlow){
        Map<String, Object> map = new HashMap<>();
        //是支付冻结,比较支付金额和可用余额的大小
        BigDecimal freezeAmount = freezeAccFlow.getAmount();
        map.put(AMOUNT, amount.doubleValue() + "");
        if (freezeAmount.compareTo(amount) == -1) {
            //可用 余额小于于支付金额,冻结了所有的余额
            map.put(IS_ENOUGH_KEY, IS_NOT_ENOUGH); //设置是否可以用余额支付全部金额
            map.put(BALANCE_PAYMENT, freezeAmount);   //设置用余额支付的金额
            BigDecimal surplusAmount = amount.subtract(freezeAmount);//需要是用三方渠道的金额
            map.put(SURPLUS_AMOUNT, surplusAmount);         //设置用其他支付渠道支付的金额 ,此处0代表不需要其他支付渠道,余额可以支付
            logger.info(" commit again the freeze amount result for availableBalance less than amount [map = {}]", ItvJsonUtil.toJson(map));
        } else {
            //冻结金额等于amount
            map.put(IS_ENOUGH_KEY, IS_ENOUGH);; //设置是否可以用余额支付全部金额
            map.put(BALANCE_PAYMENT, amount);   //设置用余额支付的金额
            map.put(SURPLUS_AMOUNT, Constants.NULL_BALANCE);       //设置用其他支付渠道支付的金额
            logger.info(" commit again the freeze amount result for availableBalance greater than amount [map = {}]", ItvJsonUtil.toJson(map));
        }
        return map;
    }

    @Override
    public void payByBalance(ResponseData responseData, PayFlow payFlow, Long timeStamp) {
        logger.info("the pay by balance param in manager is ----->[payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        Long orderId = payFlow.getOrderId();
        String interactionId = payFlow.getInteractionId();
        Map<String, String> payOrder = orderRedisService.getPayOrder(payFlow.getInteractionId() + "");
        String tradeTypeStr = payOrder.get(Constants.IS_RECHARGE);
        Short tradeType = Short.valueOf(tradeTypeStr) == Constants.TRADE_TYPE_8 ? Constants.TRADE_TYPE_8 : Constants.TRADE_TYPE_1;
        List<PayFlow> exitPayFlow = payFlowService.getPayFlowByItems(orderId,interactionId,tradeType);
        if (CollectionUtils.isNotEmpty(exitPayFlow)){
            logger.info(" commit order agian by query [payFlow = {}]",ItvJsonUtil.toJson(payFlow));
            responseData.setMsg("commit request agian");
            responseData.setCode(ResponseStatus.COMMITAGAIN.getValue());
            return;
        }
        payFlow.setTradeType(tradeType);
        AccountFlowVo accountFlowVo = checkBalanceIsExist(payFlow.getPayerId());
        BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        if (freezeAmount.compareTo(payerPayAmount) == -1) {
            logger.info(" the result for [freezeAmount = {}, payerPayAmount = {} ]" , freezeAmount,payerPayAmount);
            //该订单金额未冻结
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg(" Banlance is not freezen");
            return;
        }
        logger.info("get orderInfo by cache [payOrder = {}]", ItvJsonUtil.toJson(payOrder));
        if (MapUtils.isEmpty(payOrder)) {
            responseData.setCode(ResponseStatus.ORDERINFONULL.getValue());
            responseData.setMsg(" order info is null");
            return;
        }
        /*校验订单信息和支付信息是否一致 start*/
        boolean flag = checkPayFlowByMD5(payFlow, payOrder, timeStamp);
        if (!flag) {
            responseData.setCode(ResponseStatus.ORDERINFONOTMATCH.getValue());
            responseData.setMsg(" order info not match");
            return;
        }
        /*校验订单信息和支付信息是否一致 end*/
        /*设置交易流水的初始值 start*/
        payFlow.setChannelCode(Constants.UID_BALANCE); // TODO: 待初始化
        payFlow.setChannelName(Constants.BALANCE);
        //设置运费,店铺ID,店铺服务费费率,店铺服务费金额start
        String shopId = payOrder.get(Constants.SHOP_ID);
        payFlow.setShopId(Integer.valueOf(shopId));
        String benefit = payOrder.get(BENEFIT);
        Integer benefitPercent = Integer.valueOf(StringUtils.isBlank(benefit) ? "0" : benefit);
        payFlow.setShopBenefitPercent(benefitPercent);
        BigDecimal balanceFreight = StringUtils.isBlank(payOrder.get(BALANCE_FREIGHT)) ? BigDecimal.ZERO : new BigDecimal(payOrder.get(BALANCE_FREIGHT));
        payFlow.setFreight(balanceFreight);
        BigDecimal balanceBenefit = StringUtils.isBlank(payOrder.get(BALANCE_BENEFIT)) ? BigDecimal.ZERO : new BigDecimal(payOrder.get(BALANCE_BENEFIT));
        payFlow.setShopBenefitMoney(balanceBenefit);
        //设置运费,店铺ID,店铺服务费费率,店铺服务费金额end
        Long id = getBalanceChanelFlow();
        payFlow.setChannelFlowId(id + "");
        /*设置交易流水的初始值 end*/
        //添加流水到数据库
        AccountFlowVo accFlowByPayFlow = getAccFlowByPayFlow(payFlow, responseData);
        accountFlowDescUtil.cassExplainByAcc(accFlowByPayFlow);
        payFlowService.payByBalance(payFlow, accFlowByPayFlow);
        //修改订单状态
        payOrder.put(Constants.PAY_MODE, Constants.BALANCE);
        payOrder.put(Constants.BILLFLOW, id + "");
        payOrder.put(Constants.UID, payFlow.getPayerId() + "");
        payOrder.put(Constants.ORDER_AMOUNT, payFlow.getPayerPayAmount().toString());
        String responseCode = updateOrderStatus(payOrder, tradeType);
        if (StringUtils.isBlank(responseCode) || responseCode.equals(ERR_RESPONSE_CODE )){
            //修改状态失败,需要添加到redis队列进行尝试
            orderRedisService.modifyOrderStatusParam(payOrder);
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(" pay by balance success");
    }


    //支付成功后修改订单状态的方法
    public String updateOrderStatus(Map<String, String> payOrder,Short tradeType) {
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        Long uid = context.getUid();
        payOrder.remove(Constants.CREATE_TIME);
        logger.info("the context is [ context = {} ]", ItvJsonUtil.toJson(context));
        String updateUrl = tradeType.shortValue() == Constants.TRADE_TYPE_8 ? DISPATCH_URL : ORDER_URL;
        String reponseDataStr = httpDeal.post(updateUrl, payOrder, token,uid + "");
        logger.info("the update url = {}",updateUrl);
        HashMap<String, String> hashMap = ItvJsonUtil.jsonToObj(reponseDataStr, new HashMap<String, String>().getClass());
        logger.info(" Update order status  result [reponseDataStr = {}] ---------> ", reponseDataStr);
        return MapUtils.isEmpty(hashMap) ? null : hashMap.get(RESPONSE_CODE);
    }

    /*使用md5校验订单信息与支付信息是否一致的方法 start*/
    //含白条支付时，balance=0 固定传值
    private boolean checkPayFlowByMD5(PayFlow payFlow, Map<String, String> payOrder, Long timeStamp) {
        logger.info(" checkPayFlowByMD5 log step1 [ payFlow = {}, payOrder = {}, timeStamp = {}] ", payFlow, payOrder, timeStamp);
        //订单信息
        String orderInfo = payOrder.get(Constants.INTERACTION_ID) + "-" + payOrder.get(Constants.ORDER_AMOUNT) + "-" + String.valueOf(payOrder.get(Constants.SURPLUS))
                + "-" + payOrder.get(Constants.BANLANCE) + "-" + payOrder.get(Constants.PAY_TIMESTAMP) + "-" + payOrder.get(Constants.UID);
        //支付信息
        String flowInfo = payFlow.getInteractionId() + "-" + payFlow.getOrderAmount() + "-" + Constants.NULL_BALANCE_STR + "-" + payFlow.getPayerPayAmount() + "-" + timeStamp + "-" + payFlow.getPayerId();
        logger.info(" checkPayFlowByMD5 log step2 [ oderInfo = {}, flowInfo = {}] ", orderInfo, flowInfo);
        //做md5处理
        String orderInfoMd5 = MD5.GetMD5String(orderInfo);
        String flowInfoMd5 = MD5.GetMD5String(flowInfo);
        if (!orderInfoMd5.equals(flowInfoMd5)) {
            return false;
        }
        return true;
    }
    /*使用md5校验订单信息与支付信息是否一致的方法 end*/



    public static BigDecimal getAvailableBalance(AccountFlow accountFlow) {
        BigDecimal balance = accountFlow.getBalance();//该用户的余额
        BigDecimal freezeAmount = accountFlow.getFreezeAmount();//该用户余额中的冻结金额
        BigDecimal availableBalance = balance.subtract(freezeAmount);//该用户的可用余额
        return availableBalance;
    }

    //ID生成器获取余额支付支付渠道流水的方法
    public Long getRechargeId() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, Constants.ID_RECHARGE_VALUE);
        String idStr = httpDeal.post(ID_URL, map, null,null);
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        Object o = hashMap.get(Constants.ENTITY);
        return o == null ? null : Long.valueOf(String.valueOf(o));
    }


    //ID生成器获取余额支付支付渠道流水的方法
    public Long getBalanceChanelFlow() {
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        Long uid = context.getUid();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, Constants.ID_GEN_PAY_VALUE);
        String idStr = httpDeal.post(ID_URL, map, token,uid + "");
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        return Long.valueOf((Integer) hashMap.get(Constants.ENTITY));
    }


    @Override
    public void getBalanceByUid(ResponseData responseData, Long userId) {
        logger.info("the get balance by uid param in manager is ----->[userId = {}]", userId);
        //查询当前用户的余额,及其可用余额
        AccountFlowVo accountFlow = checkBalanceIsExist(userId.toString());
        Integer exitRandomNum = accountFlowRedisService.getRandomNum(userId);
        Integer randomNum = exitRandomNum == null ? RandomUtils.getRandomNum() : exitRandomNum;
        //获取用户状态
        String userStatus = getUserInfoByKey(userId, Constants.USER_STATUS);
        //判断用户是否有余额
        if (accountFlow == null) {
            //用户没有余额
            responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
            responseData.setMsg("this user have no banlance");
            AccountFlowVo accountFlowVo = new AccountFlowVo();
            accountFlowVo.setUserStatus(userStatus);
            accountFlowVo.setRandomNum(randomNum);
            responseData.setEntity(accountFlowVo);
            if (exitRandomNum == null){
                accountFlowRedisService.setRandomNum(userId, randomNum);
            }
            return;
        }
        accountFlow.setUserStatus(userStatus);
        if (exitRandomNum == null){
            accountFlowRedisService.setRandomNum(userId, randomNum);
        }
        accountFlow.setRandomNum(randomNum);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("get balance success");
        responseData.setEntity(accountFlow);
    }


    private String getUserInfoByKey(Long userId,String key){

        String userInfo = null;
        try {
            userInfo = accountFlowRedisService.getUserInfo(userId);
        } catch (Exception e) {
            logger.error("get userInfo failed", e);
        }
        if (StringUtils.isNotBlank(userInfo)) {
            Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
            });
            Object status = userInfoMap.get(key);
            String keyInfo = String.valueOf(status);
            if (StringUtils.isNotBlank(keyInfo)) {
                return keyInfo;
            }
        }
        return  null;
    }

    public AccountFlowVo checkBalanceIsExist(String userId) {
        AccountFlowVo accountFlowVo = null;
        try {
            //accountFlow = accountFlowRedisService.getAccountFlowByUid(userId);
        } catch (Exception e) {
            logger.error(" get banlance cache by uid err [uid = {}]", e);
        }
        //判断缓存中是否有流水记录
        if (accountFlowVo == null) {
            //缓存中没有,数据库中获取
            AccountFlowVo accountFlow = accountFlowService.getAccountFlowByUid(userId);
            if (accountFlowVo == null && accountFlow != null) {
                accountFlowRedisService.setBalanceByUid(accountFlow);
                accountFlowVo = accountFlow;
            }
        }
        return accountFlowVo;
    }

    @Override
    public void reCharge(ResponseData responseData, PayFlow payFlow, Withdrawto withdrawto) {
        logger.info("the reCharge param in manager is -----> [payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        Short tradeType = payFlow.getTradeType();
        Long id = null;
        if (withdrawto != null && tradeType.shortValue() == Constants.TRADE_TYPE_5.shortValue()){
            id = withdrawto.getId();
            WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(id);
            Long balanceChanelFlow = getBalanceChanelFlow();
            BigDecimal amount = withdrawByid.getAmount();
            payFlow.setPayerPayAmount(amount);
            payFlow.setChannelFlowId(balanceChanelFlow + "");
        }
        AccountFlowVo accFlowByPayFlow = getAccFlowByPayFlow(payFlow, responseData);
        if (accFlowByPayFlow == null) {
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg("balance have some exception");
            return;
        }
        logger.info(" the new accountFlow for this user [accFlowByPayFlow = {}]", ItvJsonUtil.toJson(accFlowByPayFlow));
        accountFlowDescUtil.cassExplainByAcc(accFlowByPayFlow);
        if (withdrawto != null && tradeType == Constants.TRADE_TYPE_5) {
            withdrawto.setApprStatus(Constants.APPR_STATUS_6);
            payFlow.setOrderId(id);
            accFlowByPayFlow.setBusiNo(id.toString());
            int i = payFlowService.withdrawto(payFlow, accFlowByPayFlow, withdrawto);
            if (i == Constants.INTEGER_ONE) {
                WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(withdrawto.getId());
                if (withdrawByid != null) {
                    Short apprStatus = withdrawByid.getApprStatus();
                    if (apprStatus == Constants.APPR_STATUS_6.shortValue()) {
                        Long uid = withdrawByid.getUid();
                        String amount = String.valueOf(withdrawByid.getAmount());
                        String cardCode = String.valueOf(withdrawByid.getBankAccount().getCardCode());
                        if (StringUtils.isNotBlank(cardCode)) {
                            int length = cardCode.length();
                            String cardCodes = cardCode.substring(length - 4);
                            //从redis中获取电话号码
                            String userInfo = accountFlowRedisService.getUserInfo(uid);
                            //反序列化
                            HashMap hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                            String showPhone = (String) hashMap.get("showPhone");
                            //content参数 金额 + 银行卡尾号
                            String content = amount + "|" + cardCodes;
                            msgClient.postMsg(msgUrl, showPhone, uid.toString(), content, CASHWITHDRAWALAUDITKEY);
                            logger.info("check withdrawparams success");
                        }
                    }
                }
            }
        }
        if (tradeType.shortValue() == Constants.TRADE_TYPE_3.shortValue() || tradeType.shortValue() == Constants.TRADE_TYPE_14.shortValue()) {
            //线下充值的业务
            //需获取订单号
            Long rechargeId = getRechargeId();
            payFlow.setOrderId(rechargeId);
            accFlowByPayFlow.setBusiNo(rechargeId.toString());
            payFlowService.reCharge(payFlow, accFlowByPayFlow);
        }

        responseData.setCode(ResponseStatus.OK.getValue());
    }


    @Override
    public void withdarwoFalse(ResponseData responseData, PayFlow payFlow, String uname) {
        logger.info("the withdarwoFalse param in manager is -----> [payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        AccountFlowVo accountFlowVo = checkBalanceIsExist(payFlow.getReceiverUserId());
        if (accountFlowVo == null){
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg("AccountFlowVo accountFlowVo is null");
            return;
        }
        BigDecimal availableBalance = accountFlowVo.getAvailableBalance();
        if (availableBalance.compareTo(payFlow.getPayerPayAmount()) == -1){
            logger.info(" the old account [accountFlowVo = {}]", ItvJsonUtil.toJson(accountFlowVo));
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg("balance have some exception");
            return;
        }
        Long balanceChanelFlow = getBalanceChanelFlow();
        payFlow.setChannelFlowId(balanceChanelFlow + "");
        BigDecimal balance = accountFlowVo.getBalance();
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        String uid = payFlow.getReceiverUserId();
        //提现的业务
        accountFlowVo.setUserId(Long.valueOf(uid));
        accountFlowVo.setOperator(payFlow.getPayerId());
        accountFlowVo.setBusiType(Constants.BUSI_TYPE_15);
        accountFlowVo.setDirection(Constants.DIRECTION_OUT);
        accountFlowVo.setAmount(payerPayAmount);
        accountFlowVo.setSrcAcc(Constants.BALANCE);
        accountFlowVo.setTargetAcc(null);
        balance = balance.subtract(payerPayAmount);
        accountFlowVo.setBalance(balance);
        logger.info(" withdrawto balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(balance));
        accountFlowVo.setUname(uname);
        String desp = payFlow.getDesp();
        if (StringUtils.isNotBlank(desp)){
            accountFlowVo.setDesp(desp);
        }else {
            payFlow.setDesp(FALSE_DESP);
            accountFlowVo.setDesp(FALSE_DESP);
        }
        payFlowService.abatementBalance(payFlow,accountFlowVo);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(" withdrawto success");
    }

    @Override
    public void abatementBalance(ResponseData responseData, PayFlow payFlow) {
        logger.info("the abatement balance in manager param is ----->[payFlow = {} ]", ItvJsonUtil.toJson(payFlow));
        Short tradeType = payFlow.getTradeType();
        if (tradeType != Constants.TRADE_TYPE_8 && tradeType != Constants.TRADE_TYPE_9 && tradeType != Constants.TRADE_TYPE_10 && tradeType != Constants.TRADE_TYPE_11) {
            responseData.setCode(ResponseStatus.TRADETYPEERR.getValue());
            responseData.setMsg(" trade type is wrong");
            return;
        }
        payFlow.setChannelCode(Constants.UID_BALANCE);
        payFlow.setChannelName(Constants.BALANCE);
        if (tradeType == Constants.TRADE_TYPE_8){
            payFlow.setMid("1");
        }
        AccountFlowVo accFlowByPayFlow = getAccFlowByPayFlow(payFlow, responseData);
        if (accFlowByPayFlow == null) {
            return;
        }
        payFlow.setMid(null);
        accountFlowDescUtil.cassExplainByAcc(accFlowByPayFlow);
        payFlowService.abatementBalance(payFlow, accFlowByPayFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
    }



    //根据支付流水生成一条账户资金流水
    public AccountFlowVo getAccFlowByPayFlow(PayFlow payFlow, ResponseData responseData) {
        String uid = null;
        Short tradeType = payFlow.getTradeType();
        uid = accountFlowDescUtil.getUid(tradeType,payFlow);
        logger.info(" the uid ----------->[uid = {}]", uid);
        AccountFlowVo accountFlowVo = checkBalanceIsExist(uid);
        logger.info(" the new accountFlow for this user [accountFlowVo = {}]", ItvJsonUtil.toJson(accountFlowVo));
        BigDecimal payerPayAmount = payFlow.getPayerPayAmount();
        String channelCode = payFlow.getChannelCode();
        if (accountFlowVo == null) {
            accountFlowVo = new AccountFlowVo();
            accountFlowVo.setBalance(new BigDecimal(Constants.NULL_BALANCE));
            accountFlowVo.setFreezeAmount(new BigDecimal(Constants.NULL_BALANCE));
        }
        BigDecimal balance = accountFlowVo.getBalance();
        BigDecimal newBalance = null;
        if (tradeType == Constants.TRADE_TYPE_15) {
            //白条还款
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_20);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            newBalance = balance.subtract(payerPayAmount);
            logger.info(" white bar repay result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_2 || tradeType == Constants.TRADE_TYPE_18) {
            //订单退款
            payFlow.setReceiverName(Constants.BALANCE);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_14);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            newBalance = balance.add(payerPayAmount);
            responseData.setMsg(" order refund success");
            logger.info(" order refund balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_10) {
            //索赔退款到余额
            payFlow.setReceiverName(Constants.BALANCE);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_12);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            newBalance = balance.add(payerPayAmount);
            responseData.setMsg(" return balance success");
            logger.info(" claim return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_11) {
            //无货到索赔退款到余额
            payFlow.setReceiverName(Constants.BALANCE);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_13);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            newBalance = balance.add(payerPayAmount);
            responseData.setMsg(" not recieve return balance success");
            logger.info(" not recieve claim return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_9) {
            //配货退款
            payFlow.setReceiverName(Constants.BALANCE);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_10);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setSrcAcc(Constants.RECEIVERNAME);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            if(channelCode.equals(Constants.UID_BALANCE)){
                newBalance = balance.add(payerPayAmount);
            }
            responseData.setMsg(" return balance success");
            logger.info(" return balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_8) {
            //配货扣除余额
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.RECEIVERNAME);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_9);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
            String mid = payFlow.getMid();
            if (StringUtils.isNotBlank(mid) && mid.equals("1")){
                //旧有配货扣除余额
                BigDecimal availableBalance = accountFlowVo.getAvailableBalance();
                if (availableBalance.compareTo(payerPayAmount) == -1){
                    logger.info(" balance is not enough [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
                    responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
                    responseData.setMsg(" balance is not enough");
                    return null;
                }
                newBalance = balance.subtract(payerPayAmount);
            }else{
                if (channelCode.equals(Constants.UID_BALANCE)){
                    if (freezeAmount.compareTo(payerPayAmount) == -1){
                        logger.info("freezeAmount err [ freezeAmount = {},payerPayAmount = {}]" , freezeAmount,payerPayAmount);
                        return null;
                    }
                    newBalance = balance.subtract(payerPayAmount);
                    freezeAmount = freezeAmount.subtract(payerPayAmount);
                }else {
                    newBalance = balance;
                }
            }
            accountFlowVo.setFreezeAmount(freezeAmount);
            responseData.setMsg(" abatement balance success");
            logger.info(" abatements balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_3) {
            //线下充值的业务
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.ACC_TYPE_3);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_11);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(null);
            newBalance = balance.add(payerPayAmount);
            responseData.setMsg(" recharge success");
            logger.info(" recharge balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_14) {
            //红包奖励的业务
            payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.BALANCE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_18);
            accountFlowVo.setDirection(Constants.DIRECTION_IN);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(null);
            newBalance = balance.add(payerPayAmount);
            responseData.setMsg(" hongbao success");
            logger.info(" hongbao balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_5) {
            //提现的业务
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_2);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setSrcAcc(Constants.BALANCE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setTargetAcc(null);
            BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
            if (balance.compareTo(payerPayAmount) == -1 || (freezeAmount.compareTo(payerPayAmount) == -1)) {
                logger.info("the judge param [balance = {} , payerPayAmount = {}, freezeAmount = {}]", balance, payerPayAmount, freezeAmount);
                return null;
            }
            newBalance = balance.subtract(payerPayAmount);
            if (freezeAmount.compareTo(payerPayAmount) == -1){
                logger.info("freezeAmount err [ freezeAmount = {},payerPayAmount = {}]" , freezeAmount,payerPayAmount);
                responseData.setMsg("freezeAmount err ");
                return null;
            }
            freezeAmount = freezeAmount.subtract(payerPayAmount);
            accountFlowVo.setFreezeAmount(freezeAmount);
            responseData.setMsg(" withdrawto success");
            logger.info(" withdrawto balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        if (tradeType == Constants.TRADE_TYPE_1) {
            //订单扣款的业务
            if (channelCode.equals(Constants.UID_BALANCE)) {
                newBalance = balance.subtract(payerPayAmount);
                BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
                freezeAmount = freezeAmount.subtract(payerPayAmount);
                accountFlowVo.setFreezeAmount(freezeAmount);
            }
            //白条支付
            if (channelCode.equals(Constants.UID_WHITE)) {
                newBalance = balance;
                BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
                accountFlowVo.setFreezeAmount(freezeAmount);
            }
            ///payFlow.setReceiverName(Constants.RECEIVERNAME);
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setTargetAcc(Constants.SUNKFA);
            accountFlowVo.setTargetAccType(Constants.SHORT_ONE);
            accountFlowVo.setSrcAccType(Constants.SHORT_ONE);
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_3);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setOperator(payFlow.getReceiverUserId());
            accountFlowVo.setSrcAcc(payFlow.getChannelName());
            accountFlowVo.setIsBalance(Constants.IS_REFUND_NO);
        }
        if (tradeType == Constants.TRADE_TYPE_12) {
            //HAT提现的业务
            accountFlowVo.setUname(payFlow.getUname());
            accountFlowVo.setUserId(Long.valueOf(uid));
            accountFlowVo.setOperator(payFlow.getPayerId());
            accountFlowVo.setBusiType(Constants.BUSI_TYPE_2);
            accountFlowVo.setDirection(Constants.DIRECTION_OUT);
            accountFlowVo.setSrcAcc(Constants.BALANCE);
            accountFlowVo.setTargetAcc(payFlow.getReceiverUserId());
            accountFlowVo.setTargetAccType(Constants.SHORT_TWO);
            accountFlowVo.setSrcAccType(Constants.SHORT_TWO);
            accountFlowVo.setDesp("提现扣款");
            BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();
            if (balance.compareTo(payerPayAmount) == -1 || (freezeAmount.compareTo(payerPayAmount) == -1)) {
                logger.info("the judge param [balance = {} , payerPayAmount = {}, freezeAmount = {}]", balance, payerPayAmount, freezeAmount);
                return null;
            }
            newBalance = balance.subtract(payerPayAmount);
            if (freezeAmount.compareTo(payerPayAmount) == -1){
                logger.info("freezeAmount err [ freezeAmount = {},payerPayAmount = {}]" , freezeAmount,payerPayAmount);
                responseData.setMsg("freezeAmount err ");
                return null;
            }
            freezeAmount = freezeAmount.subtract(payerPayAmount);
            accountFlowVo.setFreezeAmount(freezeAmount);
            responseData.setMsg(" withdrawto success");
            logger.info(" withdrawto balance result [accountFlowVo = {},newBalance = {}]", ItvJsonUtil.toJson(accountFlowVo), ItvJsonUtil.toJson(newBalance));
        }
        accountFlowVo.setBalance(newBalance);
        accountFlowVo.setAccType(Constants.ACC_TYPE_4);
        accountFlowVo.setAmount(payerPayAmount);
        accountFlowVo.setIsBalance(Constants.IS_REFUND_YES);
        accountFlowVo.setBusiNo(payFlow.getOrderId() + "");
        setAccType(payFlow,accountFlowVo);
        String userInfo = null;
        Long userId =Long.valueOf(uid) ;
        logger.info(" this accountFlow is own of user [userId = {}]", userId + "");
        if (tradeType != Constants.TRADE_TYPE_12) {
            try {
                userInfo = accountFlowRedisService.getUserInfo(userId);
                logger.info(" this userInfo is own of user [userInfo = {}]", userInfo);
            } catch (Exception e) {
                logger.error("get userInfo failed",e);
            }
            if(StringUtils.isNotBlank(userInfo)){
                Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
                });
                Object nick = userInfoMap.get(Constants.NICK_NAME);
                if (nick != null){
                    String nickName = (String) nick;
                    payFlow.setUname(nickName);
                }
            }else {
                payFlow.setUname("");
            }
        }
        return accountFlowVo;
    }


    public AccountFlowVo setAccType(PayFlow payFlow, AccountFlowVo accountFlowVo) {
        //支付渠道
        String channelCode = payFlow.getChannelCode();
        logger.info("It is in channelCode= {}",ItvJsonUtil.toJson(channelCode));
        // 支付宝
        if (channelCode.equals(Constants.CHANNEL_CODE_ALIPAY.toString())) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_1);
        }
        //微信
        if (channelCode.equals(Constants.CHANNEL_CODE_WXPAY.toString())) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_2);
        }
        //银行卡_BOC 银行卡_BOB  银联
        if (channelCode.equals(Constants.CHANNEL_CODE_BOC.toString()) || channelCode.equals(Constants.CHANNEL_CODE_BOB.toString()) ||
                channelCode.equals(Constants.CHANNEL_CODE_QUICK_PAY.toString())) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_3);
        }
        //余额
        if (channelCode.equals(Constants.UID_BALANCE)) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_4);
        }
        //快钱网银 快钱企业网银  快钱快捷
        if (channelCode.equals(Constants.CHANNEL_CODE_KUAIQIAN.toString()) || channelCode.equals(Constants.CHANNEL_CODE_KUAIQIAN_ENTERPRISE.toString()) ||
                channelCode.equals(Constants.CHANNEL_CODE_KUAIQIAN_QUICK.toString())) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_5);
        }
        // 白条
        if (channelCode.equals(Constants.UID_WHITE)) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_6);
        }
        if (channelCode.equals(Constants.CHANNEL_CODE_UNIONPAY_WEBWAP) || channelCode.equals(Constants.CHANNEL_CODE_UNIONPAY_QRCODE) || channelCode.equals(Constants.CHANNEL_CODE_UNIONPAY_H5Pay) || channelCode.equals(Constants.CHANNEL_CODE_UNIONPAY_H5Pay_B2B)) {
            accountFlowVo.setAccType(Constants.ACC_TYPE_7);
        }
        return accountFlowVo;
    }


}
