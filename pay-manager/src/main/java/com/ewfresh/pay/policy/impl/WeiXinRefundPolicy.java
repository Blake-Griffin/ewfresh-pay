package com.ewfresh.pay.policy.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.WeiXinPayConfigure;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.WeiXinNotEnoughException;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * description 微信退款策略
 *
 * @author huangyabing
 * date 2018/4/23 16:05
 */
@Component
public class WeiXinRefundPolicy implements RefundPolicy {
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String OUT_REFUND_NO = "out_refund_no";
    private static final String TOTAL_FEE = "total_fee";
    private static final String REFUND_FEE = "refund_fee";
    private static final BigDecimal POINT = new BigDecimal("100");
    private static final String APP_ID = "appid";//商户id
    private static final String MCH_ID = "mch_id";//商户号
    private static final String NONCE_STR = "nonce_str";//随机字符串
    private static final String OP_USER_ID = "op_user_id";//账单日期
    private static final String NOTIFY_URL = "notify_url";//回调地址
    private static final String SIGN = "sign";//签名
    private static final String REFUND_ID = "refund_id_0";//微信的订单号
    private static final String REFUND_FEE_0 = "refund_fee_0";//请求退款金额
    private static final String REFUND_SUCCESS_TIME = "refund_success_time_0";//请求退款金额
    private static final String CHANNEL_CODE = "2";//渠道编号
    private static final String TYPE_CODE = "2";//微信支付的uid
    private static final String TYPE_NAME = "微信退款";
    private static Short TARDE_TYPE = 2;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private WeiXinPayConfigure weiXinPayConfigure;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//("yyyyMMddHHmmss");


    @Override
    public List<PayFlow> refund(RefundParam refundParam) throws WeiXinNotEnoughException {
        logger.info("It is method of wxpay refund");
        if (refundParam.getOutTradeNo() == null && refundParam.getTradeNo() == null || refundParam.getRefundAmount() == null || refundParam.getOutRequestNo() == null) {
            logger.warn("the param is null");
            logger.warn("the param is out_trade_no={},trade_no={},refund_amount={},out_request_no={}", refundParam.getOutTradeNo(),
                    refundParam.getTradeNo(), refundParam.getRefundAmount(), refundParam.getOutRequestNo());
            return null;
        }
        String outTradeNo = refundParam.getOutTradeNo();//商户订单号
        String outRequestNo = UUID.randomUUID().toString().replace("-","");//refundParam.getOutRequestNo();//商户退款请求号
        String totalFee = refundParam.getPayFlow().getPayerPayAmount().toString();
        String refundFee = refundParam.getRefundAmount();
        String total_fee = CommonUtils.yuanToFee(totalFee);//订单的总金额,以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）
        String refund_fee = CommonUtils.yuanToFee(refundFee);// 退款金额，以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）

        //创建发送退款申请的map
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        SortedMap<Object, Object> queryRefund = new TreeMap<>();
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());//qppid
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());//商户id
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr());//随机字符串
        //paramMap.put("transaction_id", transaction_id);//微信的订单号
        paramMap.put(OUT_TRADE_NO, outTradeNo);//订单号
        paramMap.put(OUT_REFUND_NO, outRequestNo);//退款请求号
        paramMap.put(TOTAL_FEE, total_fee);
        paramMap.put(REFUND_FEE, refund_fee);
        paramMap.put(OP_USER_ID, weiXinPayConfigure.getMchId());
        //给定退款回调，以防止退款时回调支付的回调接口
        paramMap.put(NOTIFY_URL, weiXinPayConfigure.getRefundNotifyUrl());
        //根据API给的签名规则进行签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);//把签名数据设置到Sign这个属性中
        //将map转化成xml
        String requestXml = XMLUtil.getRequestXml(paramMap);
        logger.info("request xml ----->" + requestXml);
        String resXml = null;
        //携带证书发送请求
        Map resMap = null;
        try {
            resXml = HttpUtil.doRefund(weiXinPayConfigure.getRefundUrl(), requestXml);
            logger.info(" response string ----->" + resXml);
            //将string类型返回值解析成map
            resMap = XMLUtil.doXMLParse(resXml);
            logger.info("resMap is ----->" + resMap);
        } catch (Exception e) {
            logger.error("failed to post refund request of wxpay outTradeNo is {},refundNo id {}------>", outTradeNo, outRequestNo);
            return null;
        }
        //weinxin 余额不足以进行退款  异常
        if (resMap.get("result_code").equals("FAIL") && resMap.get("err_code").equals("NOTENOUGH")) {
            throw new WeiXinNotEnoughException("refund apply failed with error WeiXin not enough balance" );
        }
        //如果返回结果正确，则去查询此次退款
        if (!resMap.get("result_code").equals("SUCCESS")) {
            logger.info("success to refund");
            return null;
        }
        logger.info("result_code is equals with success ----->");
        queryRefund = queryRefund(outRequestNo);
        if (!queryRefund.get("result_code").equals("SUCCESS")) {
            logger.info("failed to query refund of wxpay ------>" + ItvJsonUtil.toJson(queryRefund));
            return null;
        }
        logger.info("query refund by out_refund_id success ----->" + ItvJsonUtil.toJson(queryRefund));
        //TODO 维护数据

        // 根据商户退款交易流水号获取部分退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(refundParam.getTradeNo());
        if (payFlow == null) {
            logger.error("There is no this payFlowId------>" + refundParam.getTradeNo());
            return null;
        }
        logger.info("get payFlow by tradeNo success ----->" + ItvJsonUtil.toJson(payFlow));
        String payerId = payFlow.getPayerId();//付款人id
        String payerName = payFlow.getPayerName();//付款人名称
        String receiverUserId = payFlow.getReceiverUserId();//收款人id
        String receiverName = payFlow.getReceiverName();//收款人姓名
        String wxRefundId = (String) queryRefund.get(REFUND_ID);//微信退款单号
        String refund_fee_0 = (String) queryRefund.get(REFUND_FEE_0);//申请退款金额，单位分
        String refundSuccessTime = (String) queryRefund.get(REFUND_SUCCESS_TIME);//退款成功时间
        //设置退款流水信息
        payFlow.setPayFlowId(null);
        payFlow.setChannelFlowId(wxRefundId);//微信支付渠道流水号
        payFlow.setChannelCode(String.valueOf(Constants.CHANNEL_CODE_WXPAY));//支付渠道编号
        payFlow.setPayerPayAmount(FenYuanConvert.fen2Yuan(refund_fee_0));//转换分成元
        payFlow.setTypeCode(TYPE_CODE);//支付类型编号
        payFlow.setPayerId(receiverUserId);//付款人ID
        payFlow.setPayerName(receiverName);//付款人姓名
        payFlow.setReceiverUserId(payerId);//收款人ID
        payFlow.setReceiverName(payerName);//收款人姓名
        //交易交易类型
        payFlow.setTradeType(refundParam.getTradeType());
        payFlow.setShopBenefitPercent(refundParam.getPayFlow().getShopBenefitPercent());
        payFlow.setShopBenefitMoney(refundParam.getEwfreshBenefitRefund());
        payFlow.setFreight(refundParam.getFreight());
        //支付类型名称
        payFlow.setTypeName(TYPE_NAME);
        //退款支付时间
        //payFlow.setSuccessTime(response.getGmtRefundPay());
        try {
            payFlow.setSuccessTime(sdf.parse(refundSuccessTime));
        } catch (Exception e) {
            // 事实上，这里catch异常只是语法要求，银行处理成功后，时间格式不会有误，假定这里发生了异常，也仅仅是时间解析异常，银行退款让是成功的，所以继续处理后续业务
            logger.error("Parse orderTime occurred error, the orderTime = {}", refundSuccessTime);
            payFlow.setSuccessTime(null);
        }
        payFlow.setCreateTime(null);
        // 是否退款
        payFlow.setIsRefund(Constants.SHORT_ONE);
        logger.info("Refund order success, the orderNo = {}", outTradeNo);
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;
    }

    /**
     * description 查询退款订单信息
     *
     * @param outRefundNo
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    public SortedMap<Object, Object> queryRefund(String outRefundNo) {
        logger.info("It is method to query refund of wxpay in controller params is -------> [outRefundNo = {}]", outRefundNo);
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        Map<String, String> m = new HashMap<String, String>();
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());//qppid
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());//商户id
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr());//随机字符串
        paramMap.put(OUT_REFUND_NO, outRefundNo);
        //根据API给的签名规则进行签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);//把签名数据设置到Sign这个属性中
        //将map转化成xml
        String requestXml = XMLUtil.getRequestXml(paramMap);
        logger.info("request xml ----->" + requestXml);
        String resXml = null;
        try {
            resXml = HttpUtil.postData(weiXinPayConfigure.getRefundQuery(), requestXml);
            m = XMLUtil.doXMLParse(resXml);
            logger.info("data after parsing with map type------>" + m);
        } catch (Exception e) {
            logger.error("Data after parsing with map type is err",e);
        }
        //过滤空 设置 TreeMap
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            String parameter = (String) it.next();
            String parameterValue = m.get(parameter);
            String v = "";
            if (null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }
        logger.info("result of query refund is ------>" + packageParams);
        return packageParams;
    }

}
