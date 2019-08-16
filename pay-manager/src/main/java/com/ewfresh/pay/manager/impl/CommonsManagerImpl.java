package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.GetParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 阿里.微信.中行公用 逻辑实现类
 *
 * @author wangziyuan
 * @date 2018.4.16
 */
@Component
public class CommonsManagerImpl implements CommonsManager {
    private static final Integer ZERO = 0;
    private static final Integer SUPPLEMENT = 8;
    private static final String BALANCE = "balance";
    private static final String ORDER_ID = "orderId";
    private static final String ORDER_IP = "orderIp";
    private static final String PAYER_PAY_AMOUNT = "payerPayAmount";
    private static final String RECEIVER_FEE = "receiverFee";
    private static final String SIRPLUS = "surplus";
    private static final String PAYMODE = "payMode";
    private static final String UID = "uid";
    private static final String ID = "id";
    private static final String CHANNEL_FLOW_ID = "channelFlowId";
    private static final String APPEND = ",";
    private static final String BILL_FLOW = "billFlow";
    private static final String ORDER_AMOUNT = "orderAmount";
    private static final Short TRADE_TYPE = 1;
    private static final Short CHANNEL_CODE = 1000;
    private static final String CHANNEL_NAME = "余额";
    private static final String ERROR = "300";
    private static final String IS_RECHARHE = "isRecharge";
    private static final String TOKEN = "token";
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String LOCK = "lock";
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private GetParam getParam;
    @Autowired
    private RedisTemplate redisTemplate;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param params 要插入流水表的数据
     * @return
     */
    @Override
    public boolean ifSuccess(Map<String, Object> params) {
        logger.info("come in if Success!!!");
        if (params.isEmpty()) {
            logger.error("the params is null");
            logger.info("return false!!!!!!!!!!!!");
            return false;
        }
        try {
            //从redis中获取对应的订单信息(判断订单金额)
            Map<String, String> redisParam = orderRedisService.getPayOrder(params.get(Constants.INTERACTION_ID).toString());
            if (redisParam.isEmpty()) {
                logger.warn("the redisParam is null---->");
                logger.info("return false!!!!!!!!!!!!");
                return false;
            }
            logger.info("the redis param is------>{}", ItvJsonUtil.toJson(redisParam));
            logger.info("the param is -------->{}", ItvJsonUtil.toJson(params));
            if (redisParam.get(Constants.ID) == null || redisParam.get(Constants.ORDER_IP) == null || redisParam.get(Constants.ORDER_AMOUNT) == null || redisParam.get(Constants.UNAME) == null ||
                    params.get(Constants.SUCCESS_TIME) == null || params.get(Constants.TYPE_NAME) == null ||
                    params.get(Constants.TYPE_CODE) == null || params.get(Constants.PLATINCOME) == null || params.get(Constants.RECEIVER_FEE) == null ||
                    params.get(Constants.RECEIVER_USER_ID) == null || params.get(Constants.PAYER_PAY_AMOUNT) == null || params.get(Constants.PAYER_ID) == null ||
                    params.get(Constants.CHANNEL_FLOW_ID) == null || redisParam.get(Constants.INTERACTION_ID) == null) {
                logger.warn("the param is null");
                logger.warn("the param is orderid={},orderIp={},orderAmount={},uname={},successTime={},typeName={},typeCode={},platIncome={}" +
                                "receiverFee={},receiverUserId={},payerPayAmount={},payerId={},channelFlowId={},interactionId={}"
                        , redisParam.get(Constants.ID), redisParam.get(Constants.ORDER_IP), redisParam.get(Constants.ORDER_AMOUNT), redisParam.get(Constants.UNAME)
                        , params.get(Constants.SUCCESS_TIME), params.get(Constants.TYPE_NAME), params.get(Constants.TYPE_CODE), params.get(Constants.PLATINCOME), params.get(Constants.RECEIVER_FEE)
                        , params.get(Constants.RECEIVER_USER_ID), params.get(Constants.PAYER_PAY_AMOUNT), params.get(Constants.PAYER_ID)
                        , params.get(Constants.CHANNEL_FLOW_ID), redisParam.get(Constants.INTERACTION_ID));
                logger.info("return false!!!!!!!!!!!!");
                return false;
            }
            //对下面插流水 修改订单号的流程进行加锁操作 锁为流水号
            if (toLock(String.valueOf(params.get(CHANNEL_FLOW_ID)))) {
                //判断流水号是否已经插入
                PayFlow payFlowPartById = payFlowService.getPayFlowPartById(String.valueOf(params.get(CHANNEL_FLOW_ID)));
                if (payFlowPartById != null) {
                    logger.info("had insert payFlow there is second to insert we refuse it");
                    return true;
                }
                List<Map<String, Object>> maps = new ArrayList<>();
                //判断余额
                String balanceId = null;
                String payModes = null;
                String userId = redisParam.get(UID);
                String userInfo = "";
                String nickName = "";
                userInfo = accountFlowRedisService.getUserInfo(Long.valueOf(userId));
                logger.info(" this userInfo is own of user [userInfo = {}]", userInfo);
                if (StringUtils.isNotBlank(userInfo)) {
                    Map<String, Object> userInfoMap = ItvJsonUtil.jsonToObj(userInfo, new TypeReference<Map<String, Object>>() {
                    });
                    Object nick = userInfoMap.get(Constants.NICK_NAME);
                    if (nick != null) {
                        nickName = (String) nick;
                    }
                }
                if (!redisParam.get(BALANCE).equals(String.valueOf(ZERO))) {
                    logger.info("customer use balance!!!!!!");
                    //用余额的情况(需要维护 余额的流水,所以从id生成器拿余额流水的ID)
                    balanceId = getParam.getBillId(redisParam.get(redisParam.get(TOKEN)), redisParam.get(UID));
                    logger.info("the balance id come from idgenerator is--->{}", balanceId);
                    Map<String, Object> useBalance = new HashMap<>();
                    useBalance.putAll(params);
                    //插入订单号
                    logger.info("the order id is------>{}", redisParam.get(ID));
                    useBalance.put(ORDER_ID, redisParam.get(ID));
                    //将流水插入
                    logger.info("the channelFlowId is------>{}", balanceId);
                    useBalance.put(CHANNEL_FLOW_ID, balanceId);
                    //将使用的余额插入
                    logger.info("the payerPayAmount is------>{}", redisParam.get(BALANCE));
                    useBalance.put(PAYER_PAY_AMOUNT, redisParam.get(BALANCE));
                    //将收款方手续费置空
                    logger.info("the receive fee is------>{}", ZERO);
                    useBalance.put(RECEIVER_FEE, ZERO);
                    //放入下单ip
                    logger.info("the order ip is------>{}", redisParam.get(ORDER_IP));
                    useBalance.put(Constants.ORDER_IP, redisParam.get(ORDER_IP));
                    //放入订单金额
                    logger.info("the order amount is------>{}", redisParam.get(ORDER_AMOUNT));
                    useBalance.put(Constants.ORDER_AMOUNT, redisParam.get(ORDER_AMOUNT));
                    //收款人名称
                    logger.info("the receiver name is------>{}", Constants.RECEIVERNAME);
                    useBalance.put(Constants.RECEIVER_NAME, Constants.RECEIVERNAME);
                    if (Constants.TRADE_TYPE_15.equals(Short.valueOf(redisParam.get(IS_RECHARHE)))) {
                        //判断是否是白条还款 如果isRecharge为15 说明是白条还款
                        logger.info("the trade type is------>{}", redisParam.get(IS_RECHARHE));
                        useBalance.put(Constants.TRADE_TYPE, Constants.TRADE_TYPE_15);
                    } else if (Constants.TRADE_TYPE_8.equals(Short.valueOf(redisParam.get(IS_RECHARHE)))) {
                        //订单补款
                        logger.info("the trade type is------>{}", TRADE_TYPE);
                        useBalance.put(Constants.TRADE_TYPE, Constants.TRADE_TYPE_8);
                    } else {
                        //交易类型
                        logger.info("the trade type is------>{}", TRADE_TYPE);
                        useBalance.put(Constants.TRADE_TYPE, TRADE_TYPE);
                    }
                    //渠道类型
                    useBalance.put(Constants.CHANNEL_CODE, CHANNEL_CODE);
                    //渠道名称
                    useBalance.put(Constants.CHANNEL_NAME, CHANNEL_NAME);
                    //付款人id
                    useBalance.put(Constants.PAYER_ID, redisParam.get(UID));
                    //与第三方交互id
                    useBalance.put(Constants.INTERACTION_ID, redisParam.get(Constants.INTERACTION_ID));
                    //收款人id也就是店铺id
                    useBalance.put(Constants.RECEIVER_USER_ID, redisParam.get(Constants.SHOP_ID));
                    //typeName
                    useBalance.put(Constants.TYPE_NAME, Constants.BALANCE);
                    //typeCode
                    useBalance.put(Constants.TYPE_CODE, "");
                    //channelType
                    useBalance.put(Constants.BOB_CHANNEL_TYPE, "");
                    //用户名
                    useBalance.put(Constants.UNAME, nickName);
                    //2019年6月27日15:49:54 分润相关(付款终,服务费比例,余额店铺服务费,余额运费)
                    useBalance.put(Constants.SHOP_BENEFIT_PEERCENT, redisParam.get("benefit"));
                    useBalance.put(Constants.SHOP_BENEFIT_MONEY, redisParam.get("balanceBenefit"));
                    useBalance.put(Constants.FRIGHT, redisParam.get("balanceFright"));
                    payModes = Constants.BALANCE;
                    maps.add(useBalance);
                }
                logger.info("SIRPLUS={},PAYER_PAY_AMOUNT={}", redisParam.get(SIRPLUS), params.get(PAYER_PAY_AMOUNT));
                logger.info("true or false", redisParam.get(SIRPLUS).equals(String.valueOf(params.get(PAYER_PAY_AMOUNT))));
                String uid = params.get(UID).toString();
                logger.info("the uid is------>{}", uid);
                String token = redisParam.get(TOKEN);
                logger.info("the token is------>{}", token);
                //将两个ID进行拼接(第三方流水和账户余额流水)
                String billFlow;
                //将两个支付渠道进行拼接
                String payMode;
                if (balanceId != null) {
                    billFlow = balanceId + APPEND + params.get(CHANNEL_FLOW_ID).toString();
                    payMode = payModes + APPEND + params.get(Constants.CHANNEL_NAME);
                } else {
                    billFlow = params.get(CHANNEL_FLOW_ID).toString();
                    payMode = params.get(Constants.CHANNEL_NAME).toString();
                }
                //业务操作时需要用到的参数(余额和三方拼接)
                redisParam.put(BILL_FLOW, billFlow);
                redisParam.put(PAYMODE, payMode);
                //放入订单id
                logger.info("the order id is------>{}", redisParam.get(ID));
                params.put(ORDER_ID, redisParam.get(ID));
                //放入下单ip
                logger.info("the order ip is------>{}", redisParam.get(ORDER_IP));
                params.put(Constants.ORDER_IP, redisParam.get(ORDER_IP));
                //放入订单金额(用户通过个支付渠道支付的实际金额)
                //如果是微信支付则
                logger.info("the order amount is------>{}", redisParam.get(ORDER_AMOUNT));
                params.put(Constants.ORDER_AMOUNT, redisParam.get(ORDER_AMOUNT));
                //收款人名称
                logger.info("the receiver name is------>{}", Constants.RECEIVERNAME);
                params.put(Constants.RECEIVER_NAME, Constants.RECEIVERNAME);
                //交易类型
                if (redisParam.get(IS_RECHARHE).equals(String.valueOf(ZERO))) {
                    logger.info("the trade type is------>{}", TRADE_TYPE);
                    params.put(Constants.TRADE_TYPE, TRADE_TYPE);
                } else {
                    //交易类型
                    logger.info("the trade type is------>{}", redisParam.get(IS_RECHARHE));
                    params.put(Constants.TRADE_TYPE, redisParam.get(IS_RECHARHE));
                }
                //付款人id
                params.put(Constants.PAYER_ID, redisParam.get(UID));
                //付款人平台用户名
                params.put(Constants.UNAME, redisParam.get(Constants.UNAME));
                //与第三方交互id
                params.put(Constants.INTERACTION_ID, redisParam.get(Constants.INTERACTION_ID));
                //放入店铺Id
                params.put(Constants.RECEIVER_USER_ID, redisParam.get(Constants.SHOP_ID));
                //放入店铺Id
                params.put(Constants.SHOP_ID, redisParam.get(Constants.SHOP_ID));
                //放入店铺Id
                params.put(Constants.UNAME, nickName);
                //手续费费率
                params.put(Constants.FEE_RATE, redisParam.get(Constants.FEE_RATE));
                //手续费
                params.put(Constants.RECEIVER_FEE, redisParam.get(Constants.RECEIVER_FEE));
                // 2019年6月27日15:49:54 分润相关(服务费比例,三方店铺服务费,三方运费)
                params.put(Constants.SHOP_BENEFIT_PEERCENT, redisParam.get("benefit"));
                params.put(Constants.SHOP_BENEFIT_MONEY, redisParam.get(Constants.PLATFORM_EWFRESH_BENEFIT));
                params.put(Constants.FRIGHT, redisParam.get("freight"));
                maps.add(params);
                if (String.valueOf(params.get(PAYER_PAY_AMOUNT)).equals(redisParam.get(SIRPLUS))) {
                    //如果金额相等
                    if (redisParam.get(IS_RECHARHE).equals(String.valueOf(ZERO))) {
                       /*判断是否为充值 0 不是充值 4为充值*/
                        logger.info("to modify order status");
                        payFlowService.addBatch(maps);
                        logger.info("add batch success------>!!!!!!!");
                        redisParam.remove(Constants.CREATE_TIME);
                        String code = null;
                        try {
                            code = getParam.modifyOrderStatus(redisParam, redisParam.get(TOKEN), redisParam.get(UID));
                        } catch (Exception e) {
                            logger.error("to send modifyOrderStatus the http status is not 200");
                            //放入失败队列等待处理(订单)
                            orderRedisService.modifyOrderStatusParam(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                        if (code.equals(ERROR) || code.equals(null) || "".equals(code)) {
                            //放入失败队列等待处理(订单)
                            orderRedisService.modifyOrderStatusParam(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                    } else if (redisParam.get(IS_RECHARHE).equals(String.valueOf(Constants.TRADE_TYPE_8))) {
                        logger.info("to supplementTo order status");
                        payFlowService.addBatch(maps);
                        logger.info("add batch success------>!!!!!!!");
                        String code = null;
                        try {
                            code = getParam.supplementToModifyDis(redisParam, redisParam.get(TOKEN), redisParam.get(UID));
                        } catch (Exception e) {
                            logger.error("to send supplementToModifyDis the http status is not 200");
                            orderRedisService.supplementModifyDis(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                        if (code.equals(ERROR) || code.equals(null) || "".equals(code)) {
                            //放入失败队列等待处理(订单补款)
                            orderRedisService.supplementModifyDis(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                        return true;
                    } else if (redisParam.get(IS_RECHARHE).equals(String.valueOf(Constants.TRADE_TYPE_16))) {
                        //处理店铺保证金回调
                        logger.info("to shopBond");
                        payFlowService.addBatch(maps);
                        logger.info("add batch success------>!!!!!!!");
                        String code = null;
                        try {
                            code = getParam.shopBond(redisParam, redisParam.get(TOKEN), redisParam.get(UID));
                        } catch (Exception e) {
                            logger.error("to send shopBond the http status is not 200");
                            //放入失败队列等待处理(店铺保证金)
                            orderRedisService.shopBond(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                        if (code.equals(ERROR) || code.equals(null) || "".equals(code)) {
                            //放入失败队列等待处理(店铺保证金)
                            orderRedisService.shopBond(redisParam);
                            logger.error("modify fail----->!!!!!", code);
                            return true;
                        }
                    } else {//充值 插入流水返回成功
                        payFlowService.addBatch(maps);
                        logger.info("add batch success------>!!!!!!!");
                        logger.info("return true  this is Recharge!!!!!");
                        return true;
                    }
                } else {
                    logger.info("to eles --------->!!!!!");
                    // 入交易流水 但是不更新订单状态 打日志
                    payFlowService.addBatch(maps);
                    //存在异常的情况下打日志,存交易异常记录
                    logger.error("the money is not match !!!!!!!!!!");
                    // 插入异常记录
                    logger.info("return false!!!!!!!!!!!!");
                    return true;
                }
            }
            Long aLong = toReleaseLock(String.valueOf(params.get(CHANNEL_FLOW_ID)));
            logger.info("have release the key number is----->{}", aLong);
            return true;
        } catch (Exception e) {
            logger.error("have an exception---->", e);
            logger.info("return false!!!!!!!!!!!!");
            Long aLong = toReleaseLock(String.valueOf(params.get(CHANNEL_FLOW_ID)));
            logger.info("have release the key number is----->{}", aLong);
            return false;
        }
    }

    /**
     * description:redis的加锁操作(暂时没有设置失效时间)
     *
     * @param lock 流水号
     * @return //如果aBoolean为  true 说明锁设置成功(说明是第一次回调进来) 反之 如果 aBoolean为 false 则设置锁失败(说明回调已经进来过了)
     * @author wangziyuan
     */
    private Boolean toLock(String lock) {
        String delLock = lock + LOCK;
        Boolean aBoolean = redisTemplate.getConnectionFactory().getClusterConnection().setNX(delLock.getBytes(), lock.getBytes());
        return aBoolean;
    }

    /**
     * description: redis释放锁的操作
     *
     * @param lock 要释放的锁
     * @return 释放锁的个数
     * @author wangziyuan
     */
    private Long toReleaseLock(String lock) {
        String delLock = lock + LOCK;
        Long delKeyNumber = redisTemplate.getConnectionFactory().getClusterConnection().del(delLock.getBytes());
        return delKeyNumber;
    }

}
