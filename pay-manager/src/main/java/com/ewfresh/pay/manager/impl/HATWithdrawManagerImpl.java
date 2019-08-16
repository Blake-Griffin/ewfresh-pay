package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.HATWithdrawManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.boc.MyHttp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.ewfresh.pay.util.bob.BOBOrderNoFormat.oriOrderNo2BOB19;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2018/10/18.
 */
@Component
public class HATWithdrawManagerImpl implements HATWithdrawManager{
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private WithdrawtoService withdrawtoService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private BalanceManagerImpl balanceManagerImpl;

    private static final String KUAIQIAN = "快钱";

    /**
     * Description: 商户提现申请
     *
     * @param vo date: 2018/8/7
     * @author: zhaoqun
     */
    @Override
    public ResponseData accountWithdraw(ResponseData responseData, Bill99WithdrawAccountVo vo) throws Exception {
        //获取手续费
        String queryFee = getQueryFeeMethod (vo.getuId(), vo.getAmount());
        String fee = "0";
        if (StringUtils.isNotBlank(queryFee)) {
            Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(queryFee, new TypeReference<Map<String, String>>() {
            });
            String rspCode = stringStringMap.get("rspCode");
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                fee = stringStringMap.get("fee");
            } else {
                logger.error("getQueryFeeMethod error, queryFeeContent = " + queryFee);
                throw new Exception("get withraw query fee is err");
            }
        }
        vo.setCustomerFee(fee);//会员自付手续费
        vo.setMerchantFee("0");

        String withdrawId = vo.getWithdrawId();
        Long withdrawtoId = Long.valueOf(withdrawId);// 提现ID
        String outTradeNo = oriOrderNo2BOB19(withdrawId + "E");//将提现订单号(加E加R)格式化为19位的订单号
        vo.setOutTradeNo(outTradeNo);

        //发起提现请求
        String content = accountWithdrawRequest(vo);

        if (StringUtils.isBlank(content)) {
            responseData.setCode(ResponseStatus.ERR.getValue());
        } else {
            Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            String rspCode = stringStringMap.get("rspCode");
            String rspMsg = stringStringMap.get("rspMsg");
            String status = stringStringMap.get("status");
            String dealId = stringStringMap.get("dealId");
            vo.setDealId(dealId);//支付渠道流水号
            Withdrawto withdrawto = new Withdrawto();

            if (!rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)){
                logger.error("HAT account withdraw failed, content={}", content);
                logger.error("HAT trade accountWithdraw is failed, withdraw ID = {}",withdrawtoId);
                //提现失败 修改提现状态 至 四审
                withdrawto.setId(withdrawtoId);
                withdrawto.setApprStatus(Constants.APPR_STATUS_3);
                withdrawto.setBeforeStatus(Constants.APPR_STATUS_2);//前置状态
                withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                withdrawto.setRemark(KUAIQIAN);
                withdrawtoService.updateWithdrawto(withdrawto);
            }
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                //status 订单状态 1-成功，2-失败，3-处理中
                switch (status){
                    case "1":
                        logger.info("HAT trade accountWithdraw is success dealing");
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_9);//提现
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_4);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        withdrawto.setRemark(KUAIQIAN);
                        withdrawtoService.updateWithdrawto(withdrawto);
                        //处理中 提现参数Vo类 存redis 以便查询提现结果
                        bill99OrderRedisService.putHATWithdrawVoMap(vo, Constants.WITHDRAW_TADENO);
                        responseData.setCode(ResponseStatus.HATACCOUNTWITHDRAWDEALING.getValue());
                        responseData.setMsg(ResponseStatus.HATACCOUNTWITHDRAWDEALING.name());
                        break;
                    case "2":
                        logger.error("HAT trade accountWithdraw is failed, withdraw ID = {}",withdrawtoId);
                        //提现失败 修改提现状态 至 四审
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_3);
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_2);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        withdrawto.setRemark(KUAIQIAN);
                        withdrawtoService.updateWithdrawto(withdrawto);
                        responseData.setCode(ResponseStatus.HATACCOUNTWITHDRAWFAIL.getValue());
                        responseData.setMsg(ResponseStatus.HATACCOUNTWITHDRAWFAIL.name());
                        break;
                    case "3":
                        logger.info("HAT trade accountWithdraw is dealing");
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_9);//提现
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_4);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        withdrawto.setRemark(KUAIQIAN);
                        withdrawtoService.updateWithdrawto(withdrawto);
                        //处理中 提现参数Vo类 存redis 以便查询提现结果
                        bill99OrderRedisService.putHATWithdrawVoMap(vo, Constants.WITHDRAW_TADENO);
                        responseData.setCode(ResponseStatus.HATACCOUNTWITHDRAWDEALING.getValue());
                        responseData.setMsg(ResponseStatus.HATACCOUNTWITHDRAWDEALING.name());
                        break;
                    default:
                        logger.error(" HAT accountWithdraw status = {}, withdraw ID = {}",withdrawtoId, status);
                }
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5001)) {
                responseData.setCode(ResponseStatus.OUTTRADENOEXIST.getValue());
                responseData.setMsg(rspMsg);
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5004)) {
                responseData.setCode(ResponseStatus.AMOUNTERR.getValue());
                responseData.setMsg(rspMsg);
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5006)) {
                responseData.setCode(ResponseStatus.LACKOFBALANCE.getValue());
                responseData.setMsg(rspMsg);
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5009)) {
                responseData.setCode(ResponseStatus.BANKACCOUNTERR.getValue());
                responseData.setMsg(rspMsg);
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5012)) {
                responseData.setCode(ResponseStatus.CHARGEERR.getValue());
                responseData.setMsg(rspMsg);
            } else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5014)) {
                responseData.setCode(ResponseStatus.HATFEEERR.getValue());
                responseData.setMsg(rspMsg);
            }  else if (rspCode.equals(Constants.HAT_WITHDRAWCODE_5050)) {
                responseData.setCode(ResponseStatus.HATWITHDRAWERR.getValue());
                responseData.setMsg(rspMsg);
            } else{
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(rspCode + rspMsg);
            }
        }
        return responseData;
    }

    /**
     * Description: 提现明细查询
     *
     * @param vo
     * date: 2018/8/9
     * @author: zhaoqun
     */
    @Override
    public void withdrawQuery(ResponseData responseData, Bill99WithdrawAccountVo vo) throws Exception {
        String outTradeNo = vo.getOutTradeNo();//外部交易号
        String content = withdrawQueryRequest(outTradeNo);
        Long withdrawtoId = Long.valueOf(vo.getWithdrawId());// 提现ID
        String dealId = vo.getDealId();//支付渠道流水号
        if (StringUtils.isBlank(content)) {
            responseData.setCode(ResponseStatus.ERR.getValue());
        } else {
            Map<String, String> info = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            String rspCode = info.get("rspCode");
            String status = info.get("status");
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                Withdrawto withdrawto = new Withdrawto();
                switch (info.get("status")) {
                    case "1":
                        logger.info("bill99 trade accountWithdraw is success");
                        //修改提现状态
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_6);//修改状态为 提现成功
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_4);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        //提现成功 id 存redis 发短信
                        bill99OrderRedisService.setWithdrawIdToredis(vo.getWithdrawId(), Constants.STORE_WITHDRAWID_SENDMSG);
                        //交易流水
                        String uid = vo.getuId();
                        BigDecimal amount = FormatBigDecimal.formatBigDecimal(new BigDecimal(vo.getAmount()));
                        BigDecimal payerFee = new BigDecimal(vo.getCustomerFee()).divide(new BigDecimal("100"));
                        payerFee = FormatBigDecimal.formatBigDecimal(payerFee);
                        BigDecimal payerPayAmount = FormatBigDecimal.formatBigDecimal(amount.add(payerFee));
                        String shopName = accountFlowRedisService.getShopInfoFromRedis(Constants.SHOP_ADDSHOP_REDIS, uid);
                        PayFlow payFlow = new PayFlow();
                        payFlow.setChannelFlowId(dealId);//支付渠道流水号
                        payFlow.setPayerId(uid);//付款人ID
                        payFlow.setPayerPayAmount(payerPayAmount);//付款方支付金额
                        payFlow.setPayerFee(payerFee);//付款方手续费(提现方自付手续费)
                        payFlow.setPayerType(Constants.SHORT_TWO);//付款账号类型(1个人,2店铺)
                        payFlow.setReceiverFee(amount);//收款金额
                        payFlow.setReceiverUserId(vo.getBankAcctId());//收款人ID
                        payFlow.setOrderId(withdrawtoId);
                        payFlow.setOrderAmount(amount);//订单金额
                        payFlow.setChannelCode(String.valueOf(Constants.CHANNEL_CODE_KUAIQIAN));//支付渠道编号
                        payFlow.setChannelName(KUAIQIAN);//支付渠道名称
                        payFlow.setTradeType(Constants.TRADE_TYPE_12);//交易类型
                        payFlow.setStatus(Constants.STATUS_0);//状态
                        payFlow.setInteractionId(outTradeNo);//第三方交互订单号
                        payFlow.setUname(shopName);
                        payFlow.setShopId(Integer.valueOf(uid));//shop_id
                        //资金账户流水
                        AccountFlowVo accountFlow = balanceManagerImpl.getAccFlowByPayFlow(payFlow, responseData);
                        if (accountFlow != null) {
                            payFlowService.addPayFlowAndAccFlow(withdrawto,payFlow,accountFlow);
                        }
                        responseData.setCode(ResponseStatus.OK.getValue());
                        responseData.setMsg(ResponseStatus.OK.name());
                        break;
                    case "2":
                        logger.error("HAT trade accountWithdraw is failed, withdraw ID = {}",withdrawtoId);
                        //提现失败 修改提现状态 至 四审
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_3);
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_2);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        withdrawto.setRemark(KUAIQIAN);
                        withdrawtoService.updateWithdrawto(withdrawto);
                        responseData.setCode(ResponseStatus.HATACCOUNTWITHDRAWFAIL.getValue());
                        responseData.setMsg(ResponseStatus.HATACCOUNTWITHDRAWFAIL.name());
                        break;
                    case "3":
                        logger.info("bill99 trade accountWithdraw is dealing");
                        withdrawto.setId(withdrawtoId);
                        withdrawto.setApprStatus(Constants.APPR_STATUS_9);//提现
                        withdrawto.setBeforeStatus(Constants.APPR_STATUS_4);//前置状态
                        withdrawto.setOutTradeNo(outTradeNo);//外部交易号
                        withdrawto.setRemark(KUAIQIAN);
                        withdrawtoService.updateWithdrawto(withdrawto);
                        responseData.setCode(ResponseStatus.HATACCOUNTWITHDRAWDEALING.getValue());
                        responseData.setMsg(ResponseStatus.HATACCOUNTWITHDRAWDEALING.name());
                        break;
                    default:
                        logger.error(" HAT accountWithdraw status = {}, withdraw ID = {}",withdrawtoId, status);
                }
            } else if (rspCode.equals(Constants.BILL99_RSPCODE_5002)) {
                responseData.setCode(ResponseStatus.ERR.getValue());
            } else {
                logger.info("code = " + rspCode);
                responseData.setCode(ResponseStatus.ERR.getValue());
            }
        }

    }

    /**
     * Description: 提现明细查询请求方法
     * @author: ZhaoQun
     * @param outTradeNo
     * @return: String content
     * date: 2018/10/18 8:42
     */
    public String withdrawQueryRequest(String outTradeNo) throws Exception {
        // 1.获取配置信息
        String con = null;
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String merchantPubPath = bill99PayConfigure.getMerchantPubPath();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();

        HashMap<String, String> map = new HashMap<>();
        // 2. 设置请求信息
        map.put("outTradeNo", outTradeNo);//提现id
        map.put("platformCode", platformCode);

        // 签名
        /*String signMsgVal = "";
        signMsgVal = appendParam(signMsgVal, "outTradeNo", outTradeNo);
        signMsgVal = appendParam(signMsgVal, "platformCode", platformCode);
        logger.info("signMsgVal:" + signMsgVal);
        Pkipair pki = new Pkipair();
        String signMsg = pki.signMsg(signMsgVal, merchantCertPath, merchantCertPss);
        logger.info("the signMsg is ------->{}", signMsg);*/
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(map), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/withdraw/query", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, sign);
        //String post = bindHttpDeal.post(domainName + "/withdraw/query", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, signMsg);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        logger.info("content: " + content);
        if (StringUtils.isNotEmpty(content) && verify) {
            con = content;
        }
        return con;
    }

    /**
     * Description: 商户提现申请请求方法
     *
     * @param vo date: 2018/8/7
     * @author: zhaoqun
     */
    private String accountWithdrawRequest(Bill99WithdrawAccountVo vo) throws Exception {
        logger.info("the accountWithdrawRequest param Bill99WithdrawAccountVo vo is ------>{}", ItvJsonUtil.toJson(vo));
        String con = null;

        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantAcctId = bill99PayConfigure.getMerchantAcctId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String money = CommonUtils.yuanToFee(vo.getAmount()); //提现金额 要求单位为 "分"
        HashMap<String, String> map = new HashMap<>();

        // 2. 设置请求信息
        map.put("outTradeNo", vo.getOutTradeNo());//提现id
        map.put("uId", vo.getuId());// shopId
        map.put("platformCode", platformCode);
        map.put("amount", money);
        map.put("customerFee", vo.getCustomerFee());
        map.put("merchantFee", CommonUtils.yuanToFee(vo.getMerchantFee()));
        map.put("bankAcctId", vo.getBankAcctId());
        //map.put("memberBankAcctId", vo.getMemberBankAcctId());
        map.put("memo", "withdraw");

        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(map), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/account/withdraw", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        logger.info("content: " + content);
        //boolean b = pki.enCodeByCer(signMsgVal, post, merchantPubPath);
        //logger.info("[post={},b={}]",post,b);
        if (StringUtils.isNotEmpty(content) && verify) {
            con = content;
        }
        return con;
    }

    /**
     * Description: 获取提现手续费请求方法
     *
     * @param uId
     * @param amount
     * date: 2018/10/17
     * @author: zhaoqun
     */
    public String getQueryFeeMethod (String uId, String amount) throws Exception {
        logger.info("the getQueryFee param is uId={},platformCode={},amount={}", uId, amount);
        String con = null;
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantAcctId = bill99PayConfigure.getMerchantAcctId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String money = CommonUtils.yuanToFee(amount);//String.valueOf(Double.valueOf(vo.getAmount()) * POINT); //提现金额 要求单位为 "分"
        HashMap<String, String> map = new HashMap<>();

        // 2. 设置请求信息
        map.put("uId", uId);// shopId
        map.put("platformCode", platformCode);
        map.put("amount", money);


        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(map), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/withdraw/queryFee", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        logger.info("content: " + content);
        //boolean b = pki.enCodeByCer(signMsgVal, post, merchantPubPath);
        //logger.info("[post={},b={}]",post,b);
        if (StringUtils.isNotEmpty(content) && verify) {
            con = content;
        }
        return con;
    }

    /**
     * Description: 查询账户余额请求方法
     *
     * @param uId
     * date: 2018/10/18
     * @author: zhaoqun
     */
    private String getBalanceFee (String uId) throws Exception {
        logger.info("the getQueryFee param is uId={},platformCode={}", uId);
        String con = null;

        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantAcctId = bill99PayConfigure.getMerchantAcctId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        HashMap<String, String> map = new HashMap<>();

        // 2. 设置请求信息
        map.put("uId", uId);// shopId
        map.put("platformCode", platformCode);

        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(map), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/account/balance/query", ItvJsonUtil.toJson(map), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}",verify);
        logger.info("content: " + content);
        //boolean b = pki.enCodeByCer(signMsgVal, post, merchantPubPath);
        //logger.info("[post={},b={}]",post,b);
        if (StringUtils.isNotEmpty(content) && verify) {
            con = content;
        }
        return con;
    }

    /**
     * Description: 获取提现手续费
     *
     * @param uId
     * @param amount
     * date: 2018/10/17
     * @author: zhaoqun
     */
    @Override
    public void getQueryFee(ResponseData responseData, String uId, String amount) throws Exception {
        String content = getQueryFeeMethod ( uId,  amount);
        String fee = "0";
        if (StringUtils.isBlank(content)) {
            responseData.setCode(ResponseStatus.ERR.getValue());
        } else {
            Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            String rspCode = stringStringMap.get("rspCode");
            String rspMsg = stringStringMap.get("rspMsg");
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                fee = stringStringMap.get("fee");
                responseData.setEntity(fee);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
            } else {
                logger.info("code = " + rspCode);
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(ResponseStatus.ERR.name());
            }
        }
    }

    /**
     * Description: 查询账户余额
     *
     * @param uId
     * date: 2018/10/18
     * @author: zhaoqun
     */
    @Override
    public void getBalanceFee(ResponseData responseData, String uId) throws Exception {
        String balanceFee = getBalanceFee(uId);//余额
        if (StringUtils.isNotBlank(balanceFee)) {
            Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(balanceFee, new TypeReference<Map<String, String>>() {
            });
            String rspCode = stringStringMap.get("rspCode");
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                balanceFee = stringStringMap.get("accountBalanceList");
                responseData.setEntity(balanceFee);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
            } else {
                logger.info("code = " + rspCode);
                throw new Exception("get withraw query fee is err");
            }
        }
    }


}
