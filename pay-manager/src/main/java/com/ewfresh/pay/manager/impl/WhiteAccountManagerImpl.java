package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.manager.WhiteAccountManager;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BarDealFlowVo;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.WhiteBarService;
import com.ewfresh.pay.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/15.
 */
@Component
public class WhiteAccountManagerImpl implements WhiteAccountManager{

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BarDealFlowService barDealFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private BalanceManagerImpl balanceManagerImpl;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private WhiteBarService whiteBarService;
    @Autowired
    private HttpDeal httpDeal;

    @Value("${http_update_order}")
    private String ORDER_URL;
    @Value("${http_idgen}")
    private String ID_URL;

    private final static String SELFNAME = "自营";
    private final static BigDecimal ZERO = new BigDecimal(Constants.NULL_BALANCE_STR);
    private static final String FREIGHT = "freight";//第三方支付运费运费
    private static final String BALANCE_FREIGHT = "balanceFreight";//余额运费
    private static final String BENEFIT = "benefit";//ewfresh服务费费率
    private static final String BALANCE_BENEFIT = "balanceBenefit";//余额ewfresh所得服务费
    private static final String PLATFORM_EWFRESH_BENEFIT = "platformEwfreshBenefit";//第三方ewfresh所得服务费

    /**
     * Description: 获取白条账户余额
     * @author: ZhaoQun
     * @param userId
     * date: 2019/3/15 9:04
     */
    @Override
    public void getWhiteAccountByUid(ResponseData responseData, Long userId) {
        logger.info("the get balance by uid param in manager is ----->[userId = {}]", userId);
        WhiteBarVo whiteBarVo = null;
        BigDecimal whiteBalance = ZERO;//白条余额
        //根据uiserId获取白条
        whiteBarVo = whiteBarService.getWhiteBarVoByUid(userId);
        //判断用户是否已开通白条
            /*if (whiteBarVo == null || whiteBarVo.getTotalLimit().compareTo(ZERO)==0) {
                //用户没有白条
                responseData.setCode(ResponseStatus.NOWHITE.getValue());
                responseData.setMsg(ResponseStatus.NOWHITE.name());
                responseData.setEntity(ZERO);
                return;
            }*/
        //未激活(未申请)
        if (whiteBarVo == null){
            responseData.setCode(ResponseStatus.WHITENULL.getValue());
            responseData.setMsg(ResponseStatus.WHITENULL.name());
            responseData.setEntity(ZERO);
            return;
        }
        Short useStatus = whiteBarVo.getUseStatus().shortValue();
        Short apprStatus = whiteBarVo.getApprStatus().shortValue();
        //useStatus = 0  apprStatus = 0  待审核
        if(useStatus == Constants.SHORT_ZERO && apprStatus == Constants.SHORT_ZERO){
            responseData.setCode(ResponseStatus.GOTOWHITE.getValue());
            responseData.setMsg(ResponseStatus.GOTOWHITE.name());
            responseData.setEntity(ZERO);
            return;
        }
        //useStatus = 0  apprStatus = 2  激活失败
        if(useStatus == Constants.SHORT_ZERO && apprStatus == Constants.SHORT_TWO){
            responseData.setCode(ResponseStatus.FAILEDWHITE.getValue());
            responseData.setMsg(ResponseStatus.FAILEDWHITE.name());
            responseData.setEntity(whiteBarVo.getReason());
            return;
        }
        //用户已开通白条
        //获取用户白条已使用额度
        BigDecimal usedLimit = barDealFlowService.getUsedLimitByUid(userId);
        if (usedLimit == null){
            usedLimit = ZERO;
        }
        whiteBarVo.setUsedLimit(usedLimit);
        BigDecimal totalLimit = whiteBarVo.getTotalLimit();//白条总额度
        //计算可用白条余额
        whiteBalance =  totalLimit.subtract(usedLimit);//如果降额之后，消费高于总额度，则可用额度为-负数-
//        if (whiteBalance.compareTo(ZERO) < 0){
//            whiteBalance = ZERO;
//        }
        //获取用户信息,给用户状态赋值
        String userStatus = getUser(userId);
        //用户账号冻结，是否独立状态码？？？
        /*if (userStatus.equalsIgnoreCase(Constants.STR_ONE)){
            return;
        }*/
        if (userStatus != null){
            whiteBarVo.setUserStatus(userStatus);
        }
        whiteBarVo.setWhiteBalance(whiteBalance);
        //useStatus = 2 冻结状态
        if(useStatus == Constants.SHORT_TWO){
            responseData.setCode(ResponseStatus.WHITEFREEZE.getValue());
            responseData.setMsg(ResponseStatus.WHITEFREEZE.name());
            responseData.setEntity(whiteBarVo);
            return;
        }
        //useStatus = 3 违约状态
        if(useStatus == Constants.SHORT_THREE){
            responseData.setCode(ResponseStatus.SHOUDREPAY.getValue());
            responseData.setMsg(ResponseStatus.SHOUDREPAY.name());
            responseData.setEntity(whiteBarVo);
            return;
        }
        responseData.setEntity(whiteBarVo);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

    }

    /**
     * Description: 白条支付
     * @author: ZhaoQun
     * @param payFlow
     * @param payWay
     * @param timeStamp
     * date: 2019/3/18 09:24
     */
    @Override
    public void payByWhite(ResponseData responseData, PayFlow payFlow, Short payWay, Long timeStamp) {
        logger.info("the pay by white param in manager is ----->[payFlow = {}]", ItvJsonUtil.toJson(payFlow));
        Long orderId = payFlow.getOrderId();
        Short tradeType = payFlow.getTradeType();
        String interactionId = payFlow.getInteractionId();
        String userId = payFlow.getPayerId();
        Long uid = Long.valueOf(userId);
        BigDecimal payAmount = payFlow.getPayerPayAmount();
        BigDecimal whiteAmount = ZERO;//白条支付金额
        BigDecimal balanceAmount = ZERO;//余额支付金额
        //当前订单已有订单流水
        List<PayFlow> exitPayFlow = payFlowService.getPayFlowByItems(orderId,interactionId,tradeType);
        if (CollectionUtils.isNotEmpty(exitPayFlow)){
            logger.info(" commit order agian by query [payFlow = {}]",ItvJsonUtil.toJson(payFlow));
            responseData.setMsg("commit request agian");
            responseData.setCode(ResponseStatus.COMMITAGAIN.getValue());
            return;
        }
        //查询白条账户
        WhiteBarVo whiteBarVo = null;
        BigDecimal whiteAccount = ZERO;//白条余额
        whiteBarVo = whiteBarService.getWhiteBarVoByUid(uid);
        //判断用户是否已开通白条
        if (whiteBarVo == null || whiteBarVo.getTotalLimit().compareTo(ZERO) == 0) {
            responseData.setEntity(ZERO);
            responseData.setCode(ResponseStatus.NOWHITE.getValue());
            responseData.setMsg(ResponseStatus.NOWHITE.name());
            return;
        }
        //用户已开通白条
        //用户白条状态非正常则不可用
        if (whiteBarVo.getUseStatus().shortValue() != Constants.SHORT_ONE){
            responseData.setEntity(ZERO);
            responseData.setCode(ResponseStatus.WHITEUNAVAILABLE.getValue());
            responseData.setMsg("user white unavailable");
            return;
        }
        //获取用户白条已使用额度
        BigDecimal usedLimit = barDealFlowService.getUsedLimitByUid(uid);
        if (usedLimit == null){
            usedLimit = ZERO;
        }
        BigDecimal totalLimit = whiteBarVo.getTotalLimit();//白条总额度
        //计算可用白条余额
        whiteAccount =  totalLimit.subtract(usedLimit);
        if (whiteAccount.compareTo(ZERO) == -1){
            whiteAccount = ZERO;
        }
        //用户没有可用白条余额
        if (whiteAccount.compareTo(ZERO) != 1){
            responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
            responseData.setMsg("this user have no white");
            responseData.setEntity(whiteAccount);
            return;
        }
        /* payWay=1  仅白条支付    start*/
        if (payWay.equals(Constants.SHORT_ONE)){
            //可用白条额度 小于 支付金额
            if (whiteAccount.compareTo(payAmount) == -1) {
                responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
                responseData.setMsg("This user's white not enough to pay this order");
                responseData.setEntity(whiteAccount);
                return;
            }
            whiteAmount = payAmount;
        }
        //payWay=2 白条+余额支付
        if (payWay.equals(Constants.SHORT_TWO)){
            if (whiteAccount.compareTo(payAmount) >= 0){ //白条额度大于等于支付金额 ，仅用白条支付
                payWay = Constants.SHORT_ONE;
                whiteAmount = payAmount;
            }
            if(whiteAccount.compareTo(payAmount) == -1){ //白条+余额
                //查询当前用户的余额,及其可用余额
                AccountFlowVo accountFlow = accountFlowService.getAccountFlowByUid(userId);
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
                if(availableBalance.compareTo(ZERO) != 1){
                    //用户没有可用余额
                    responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
                    responseData.setMsg("this user banlance is zero");
                    responseData.setEntity(Constants.NULL_BALANCE);
                    return;
                }
                //用户存在可用余额
                BigDecimal totalAvaiable = whiteAccount.add(availableBalance);
                if (totalAvaiable.compareTo(payAmount) == -1){
                    //白条+余额 < 支付金额
                    responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
                    responseData.setMsg("this user account is not enough");
                    responseData.setEntity(totalAvaiable);
                    return;
                }
                whiteAmount = whiteAccount;//白条支付金额
                balanceAmount = FormatBigDecimal.formatBigDecimal(payAmount.subtract(whiteAccount));//余额支付金额
            }
        }
        //用户账户足够支付
        //从redis获取订单信息
        Map<String, String> payOrder = orderRedisService.getPayOrder(payFlow.getInteractionId() + "");
        logger.info("get orderInfo by cache [payOrder = {}]", ItvJsonUtil.toJson(payOrder));
        if (MapUtils.isEmpty(payOrder)) {
            responseData.setCode(ResponseStatus.ORDERINFONULL.getValue());
            responseData.setMsg(" order info is null");
            return;
        }

        /*校验订单信息和支付信息是否一致 start*/
        boolean flag = checkOrderPayInfoByMD5(payFlow, payOrder, timeStamp);
        if (!flag) {
            responseData.setCode(ResponseStatus.ORDERINFONOTMATCH.getValue());
            responseData.setMsg(" order info not match");
            return;
        }
        /*校验订单信息和支付信息是否一致 end*/
        ArrayList<String> payModeList = new ArrayList<>();//支付方式
        ArrayList<String> billFlowList = new ArrayList<>();//支付流水
        String shopId = payOrder.get(Constants.SHOP_ID);//店铺id
        String shopName = payOrder.get(Constants.SHOP_NAME);//店铺名称
        BigDecimal totalFreight = new BigDecimal(payOrder.get(FREIGHT)).add(new BigDecimal(payOrder.get(BALANCE_FREIGHT)));//支付总运费
        BigDecimal freight = totalFreight;//白条运费
        BigDecimal balanceFreight = BigDecimal.ZERO;//余额运费
        BigDecimal benefit = shopId.equalsIgnoreCase(Constants.STR_ZERO) ? BigDecimal.ZERO :
            new BigDecimal(payOrder.get(BENEFIT)).divide(new BigDecimal("100"));
        BigDecimal totalBenefit = shopId.equalsIgnoreCase(Constants.STR_ZERO) ? BigDecimal.ZERO :
            new BigDecimal(payOrder.get(BALANCE_BENEFIT)).add(new BigDecimal(payOrder.get(PLATFORM_EWFRESH_BENEFIT)));//ewfresh所得总服务费
        BigDecimal platfBenefit = shopId.equalsIgnoreCase(Constants.STR_ZERO) ? BigDecimal.ZERO :
            FormatBigDecimal.formatBigDecimal(whiteAmount.multiply(benefit));//白条ewfresh所得服务费
        BigDecimal balanceBenefit = totalBenefit.subtract(platfBenefit);//余额ewfresh所得服务费
        if (whiteAccount.compareTo(totalFreight) == -1){// 白条支付金额 < 总运费
            freight = whiteAccount;
            balanceFreight = totalFreight.subtract(whiteAccount);
        }
        logger.info("$$$$ shopId = " + shopId);
        if (!shopId.equalsIgnoreCase(Constants.STR_ZERO)){
            payFlow.setShopBenefitPercent(Integer.valueOf(payOrder.get(BENEFIT)));
        }
        //payWay=2 白条+余额支付，多添加一份“用户余额”的 payFlow 和 accountFlow
        AccountFlowVo balanceAccFlow = null;
        PayFlow balancePayFlow = null;
        if (payWay.equals(Constants.SHORT_TWO)){
            /*设置交易流水的初始值 start*/
            balancePayFlow = ItvJsonUtil.jsonToObj(ItvJsonUtil.toJson(payFlow),new TypeReference<PayFlow>() {
            });
            balancePayFlow.setReceiverUserId(shopId);
            balancePayFlow.setReceiverName(shopName);
            balancePayFlow.setChannelCode(Constants.UID_BALANCE);
            balancePayFlow.setChannelName(Constants.BALANCE);
            Long id = getChanelFlowId(Constants.ID_GEN_PAY_VALUE);
            balancePayFlow.setChannelFlowId(id + "");
            balancePayFlow.setPayerPayAmount(balanceAmount);//余额支付金额
            balancePayFlow.setFreight(balanceFreight);
            balancePayFlow.setShopBenefitMoney(balanceBenefit);//余额ewfresh所得服务费
            /*设置交易流水的初始值 end*/
            balanceAccFlow = balanceManagerImpl.getAccFlowByPayFlow(balancePayFlow, responseData);
            balancePayFlow.setPayerName(balancePayFlow.getUname());
            //在获取accountFlow方法中冻结余额进行了减法计算，需要加回来
            balanceAccFlow.setFreezeAmount(balanceAccFlow.getFreezeAmount().add(balanceAmount));
            accountFlowDescUtil.cassExplainByAcc(balanceAccFlow);
            //添加流水到数据库
            //int n = payFlowService.payByWhite(payFlow, balanceAccFlow, null);
            payModeList.add(Constants.BALANCE);
            billFlowList.add(id.toString());
        }
        //添加“用户白条”的payFlow 、accountFlow 和 barDealFlow
        /*设置交易流水的初始值 start*/
        payFlow.setReceiverUserId(shopId);
        payFlow.setReceiverName(shopName);
        payFlow.setChannelCode(Constants.UID_WHITE);
        payFlow.setChannelName(Constants.WHITE);
        Long id = getChanelFlowId(Constants.ID_GEN_WHITEPAY_BILL);
        payFlow.setChannelFlowId(id + "");
        payFlow.setPayerPayAmount(whiteAmount);//白条支付金额
        payFlow.setFreight(freight);
        payFlow.setShopBenefitMoney(platfBenefit);//白条ewfresh所得服务费
        /*设置交易流水的初始值 end*/
        AccountFlowVo accFlowByPayFlow = balanceManagerImpl.getAccFlowByPayFlow(payFlow, responseData);
        payFlow.setPayerName(payFlow.getUname());
        accountFlowDescUtil.cassExplainByAcc(accFlowByPayFlow);
        //白条流水
        BarDealFlow barDealFlow = new BarDealFlow();
        barDealFlow.setAmount(whiteAmount);
        barDealFlow.setUid(uid);
        barDealFlow.setUname(payFlow.getUname());
        barDealFlow.setUsedLimit(usedLimit.add(whiteAmount));//已使用额度 = 最后一条流水的已使用额度 + 本次支付金额
        barDealFlow.setDirection(Constants.SHORT_ONE);//资金流向(1流出,2流入)
        barDealFlow.setShopId(Long.valueOf(shopId));//白条支付仅支持自营
        barDealFlow.setShopName(shopName);//白条支付仅支持自营
        barDealFlow.setOrderId(orderId);
        barDealFlow.setDealType(Constants.SHORT_ONE);//交易类型(1订单付款,2订单退款,3还款)
        //barDealFlow.setBillFlow();//生成的账单批次号
        //添加流水到数据库
        logger.info("payflow = {}, balancePayFlow = {}", ItvJsonUtil.toJson(payFlow),ItvJsonUtil.toJson(balancePayFlow));
        int n = payFlowService.payByWhite(payFlow, accFlowByPayFlow, barDealFlow,balancePayFlow,balanceAccFlow);
        if (n>0){
            payModeList.add(Constants.WHITE);
            billFlowList.add(id.toString());
        }
            //修改订单状态
            String payMode = StringUtils.join(payModeList,",");
            String billFlow = StringUtils.join(billFlowList,",");
            payOrder.put(Constants.PAY_MODE, payMode);
            payOrder.put(Constants.BILLFLOW, billFlow);
            payOrder.put(Constants.UID, payFlow.getPayerId() + "");
            payOrder.put(Constants.ORDER_AMOUNT, payAmount + "");
            String responseCode = balanceManagerImpl.updateOrderStatus(payOrder,tradeType);
            if ( StringUtils.isBlank(responseCode) || responseCode.equals(ResponseStatus.ERR.getValue())){
                //修改状态失败,需要添加到redis队列进行尝试
                orderRedisService.modifyOrderStatusParam(payOrder);
            }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(" pay by white success");

    }

    /*使用md5校验订单信息与支付信息是否一致的方法 start*/
    private boolean checkOrderPayInfoByMD5(PayFlow payFlow, Map<String, String> payOrder, Long timeStamp) {
        logger.info(" checkPayFlowByMD5 log step1 [ payFlow = {}, payOrder = {}, timeStamp = {}] ", payFlow, payOrder, timeStamp);
        //订单信息
        String orderInfo = payOrder.get(Constants.INTERACTION_ID) + "-" + payOrder.get(Constants.ORDER_AMOUNT) + "-" + String.valueOf(payOrder.get(Constants.SURPLUS))
            + "-" + payOrder.get(Constants.BANLANCE) + "-" + payOrder.get(Constants.PAY_TIMESTAMP) + "-" + payOrder.get(Constants.UID);
        //支付信息
        String flowInfo = payFlow.getInteractionId() + "-" + payFlow.getOrderAmount() + "-" + payFlow.getPayerPayAmount().setScale(2) + "-"
            + "0-" + timeStamp + "-" + payFlow.getPayerId();
        logger.info(" checkPayFlowByMD5 log step2 [ oderInfo = {}, flowInfo = {}] ", orderInfo, flowInfo);
        //做md5处理
        String orderInfoMd5 = MD5.GetMD5String(orderInfo);
        String flowInfoMd5 = MD5.GetMD5String(flowInfo);
        if (!orderInfoMd5.equals(flowInfoMd5)) {
            return false;
        }
        return true;
    }

    /**
     * Description: 获取用户白条余额
     * @author: ZhaoQun
     * @param userId
     * @return: BigDecimal whiteBalance
     * date: 2019/3/18 13:51
     */
    private BigDecimal getWhiteAccount(Long userId){
        BarDealFlowVo barDealFlowVo = null;
        BigDecimal whiteBalance = ZERO;//白条余额
        try {
            //根据uiserId获取白条流水、余额等信息
            barDealFlowVo = barDealFlowService.getDealFlowByUid(userId);
            //判断用户是否已开通白条
            if (barDealFlowVo == null) {
                //用户没有白条
                /*responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
                responseData.setMsg("this user have no white");
                responseData.setEntity(ZERO);*/
                return ZERO;
            }
            //用户已开通白条
            //计算可用白条余额
            BigDecimal totalLimit = barDealFlowVo.getTotalLimit();//白条总额度
            BigDecimal usedLimit = barDealFlowVo.getUsedLimit();//已用额度
            whiteBalance =  totalLimit.subtract(usedLimit);
            if (whiteBalance.compareTo(ZERO) < 0){
                whiteBalance = ZERO;
            }
        } catch (Exception e) {
            logger.error(" get banlance cache by uid err [uid = {}]", e);
        }
        return whiteBalance;
    }

    /**
     * Description: 从redis获取用户信息
     * @author: ZhaoQun
     * @param userId
     * date: 2019/3/15 15:09
     */
    private String getUser(Long userId){
        //从redis获取用户信息
        String userInfo = null;
        String userStatus = null;
        try {
            userInfo = accountFlowRedisService.getUserInfo(userId);
        } catch (Exception e) {
            logger.error("get userInfo failed", e);
        }
        if (StringUtils.isNotBlank(userInfo)) {
            Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
            });
            Object status = userInfoMap.get(Constants.USER_STATUS);
            userStatus = String.valueOf(status);
        }
        return userStatus;
    }

    //ID生成器获取余额支付支付渠道流水的方法
    public Long getChanelFlowId(String key) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, key);
        String idStr = httpDeal.post(ID_URL, map, null,null);
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        return Long.valueOf((Integer) hashMap.get(Constants.ENTITY));
    }

}
