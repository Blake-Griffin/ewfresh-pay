package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.configure.BOCPayConfigure;
import com.ewfresh.pay.manager.BOCManager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.BOBOrderNoFormat;
import com.ewfresh.pay.util.boc.BOCRefundSeqFormat;
import com.ewfresh.pay.util.boc.BOCP7Sign;
import com.ewfresh.pay.util.boc.BOCP7Verify;
import com.ewfresh.pay.util.boc.HttpsUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * description: BOC订单业务的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/4/8.
 */
@Component
public class BOCManagerImpl implements BOCManager {
    @Autowired
    private BOCPayConfigure bocPayConfigure;
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private PayFlowService payFlowService;
    private static final Integer INTERACTION_ID_LENGTH = 20;//原生第三方交互订单支付订单号长度20位

    private Logger logger = LoggerFactory.getLogger(getClass());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String ORDER_NOS = "orderNos";// 商户订单号
    private static final String ORDER_STATUS = "orderStatus";// 订单状态
    private static final String ORDER_IP = "orderIp";// 客户支付IP地址
    private static final String METHOD_NAME = "methodName";// 方法名
    private static final String ORDER_TIME = "orderTime";// 订单时间: YYYYMMDDHHMISS
    private static final String CUR_CODE = "curCode";// 订单币种 目前只支持001：人民币,固定填001
    private static final String ORDER_AMOUNT = "orderAmount";// 订单金额
    private static final String PAY_TYPE = "payType";// 支付类型：商户支付服务类型 1：网上购物
    private static final String ORDER_NOTE = "orderNote";// 订单说明，最多允许60个汉字长度
    private static final String ORDER_URL = "orderUrl";// 商户接收通知URL
    private static final String ORDER_TIMEOUT_DATE = "orderTimeoutDate";// 超时时间
    private static final String MSG_ID = "msgId";// 报文标识号
    private static final String HDL_STS = "hdlSts";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_SUCC = "A";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_FAIL = "B";// 处理状态 A-成功  B-失败  K-未明
    private static final String HDL_STS_UNDEFINE = "K";// 处理状态 A-成功  B-失败  K-未明
    private static final String RTN_CD = "rtnCd";// 报文处理返回码
    private static final String EXCEPTION = "exception";// 异常
    private static final String HEADER = "header";// 报文头
    private static final String BODY = "body";// 报文体
    private static final String ORDER_TRANS = "orderTrans";// 订单交易信息
    private static final String VISITOR_IP = "visitorIp";// 客户通过网银支付时的IP地址信息
    private static final String VISITOR_REFER = "visitorRefer";// 客户浏览器跳转至网银支付登录界面前所在页面的URL（urlEncode格式）
    private static final String DEAL_STATUS_SUCC = "0";// 0：成功
    private static final String DEAL_STATUS_FAIL = "1";// 1：失败
    private static final String DEAL_STATUS_UNDEFINE = "2";// 2：未明
    private static final String BODY_FLAG = "bodyFlag";// 包体标志 0：有包体数据 1：无包体数据
    private static final String BD_FLG = "bdFlg";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String BD_FLG_YES = "0";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String BD_FLG_NO = "1";// 业务体报文块存在标识 0-有包体 1-无包体
    private static final String HANDLE_TYPE_UPLOAD = "0";// 操作类型 0:上传  1:下载
    private static final String HANDLE_TYPE_DOWNLOAD = "1";// 操作类型 0:上传  1:下载
    private static final String ORDER_STATUS_THROW = "0";// 订单状态：0-未处理 1-支付 4-未明 5-失败
    private static final String ORDER_STATUS_PAYED = "1";// 订单状态：0-未处理 1-支付 4-未明 5-失败
    private static final String ORDER_STATUS_UNDIFINE = "4";// 订单状态：0-未处理 1-支付 4-未明 5-失败
    private static final String ORDER_STATUS_FAIL = "5";// 订单状态：0-未处理 1-支付 4-未明 5-失败
    private static final Integer DELAY_SECONDS = 60;// 订单支付时，允许中国银行的响应时间上限
    private static final String DATE_FORMAT = "yyyyMMdd";



    /**
     * Description: 商户向BOC发送订单支付请求
     * @author: JiuDongDong
     * @param bocRecvOrder 封装商户向BOC发送订单支付请求数据
     * date: 2018/4/11 11:01
     */
    @Override
    public void sendOrder(ResponseData responseData, BOCMerchantSendOrderVo bocRecvOrder) {
        logger.info("It is now in BOCOrderManagerImpl.sendOrder, the parameters are: " + JsonUtil.toJson(bocRecvOrder));
        // 1、从请求和configure中获取参数
        String orderNo = bocRecvOrder.getOrderNo();//订单号
        // 如果是订单支付（订单号为20位）则将E、R去除
        if (orderNo.length() == INTERACTION_ID_LENGTH) {
            orderNo = orderNo.substring(Constants.INTEGER_ZERO, orderNo.length() - Constants.INTEGER_ONE);// 商户订单号
        }
        String orderAmount = bocRecvOrder.getOrderAmount();//订单金额（应付金额）
        orderAmount = FormatBigDecimal.formatBigDecimal(new BigDecimal(orderAmount)).toString();//订单金额格式化为2个小数点
        bocRecvOrder.setMerchantNo(bocPayConfigure.getMerchantNo());//商户号
        bocRecvOrder.setPayType(bocPayConfigure.getPayType());//支付类型：商户支付服务类型 1：网上购物
        bocRecvOrder.setCurCode(bocPayConfigure.getCurCode());//订单币种，固定填001人民币
        Date now = new Date();
        String strOrderTime = sdf.format(now);
        bocRecvOrder.setOrderTime(strOrderTime);//订单时间
        bocRecvOrder.setOrderNote(bocPayConfigure.getOrderNote());//订单说明
        bocRecvOrder.setOrderUrl(bocPayConfigure.getOrderUrl());//商户接收通知URL：客户支付完成后银行向商户发送支付结果，商户系统负责接收银行通知的URL
        bocRecvOrder.setOrderPayUrl(bocPayConfigure.getOrderPayUrl());//中行接收商户订单请求的网关
//        bocRecvOrder.setOrderTimeoutDate(sdf.format(DateUtil.getFutureMountSecondsStart(now, BOCManagerImpl.DELAY_SECONDS)));//超时时间选填 格式：YYYYMMDDHHMISS 其中时间为24小时格式，例:2010年3月2日下午4点5分28秒表示为20100302160528
        // 2、参数进行签名
        String signData = BOCP7Sign.getOrderPaySignData(orderNo, strOrderTime, bocPayConfigure.getCurCode(),  orderAmount , bocPayConfigure.getMerchantNo());
        bocRecvOrder.setSignData(signData);
        // 3、将包装好的订单支付请求参数返给页面
        responseData.setEntity(bocRecvOrder);
        logger.info("The send order parameters have been packaged ok and will send them back to html");
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

//        // TODO  上线时去掉这一步，由页面发起支付请求
//        // 2、发送SSL请求
//        String url = "https://101.231.206.170/PGWPortal/RecvOrder.do";
//        Map<String, String> param = new HashMap<>();
//        param.put(ORDER_NO, orderNo);
//        param.put(ORDER_TIME, strOrderTime);
//        param.put(CUR_CODE, bocPayConfigure.getCurCode());
//        param.put(ORDER_AMOUNT, orderAmount);
//        param.put(MERCHANT_NO, bocPayConfigure.getMerchantNo());
//        param.put(SIGN_DATA, signData);
//        param.put(PAY_TYPE, "1");
//        param.put(ORDER_NOTE, "BuyGoods");
//        param.put(ORDER_URL, "https://pay.sunkfa.com/t/boc/callback.htm");
//        param.put(ORDER_TIMEOUT_DATE, "20180423160528");
//        String xml = null;
//        try {
//            logger.info("Try to send order to BOC start");
//            xml = HttpsUtils.bocHttpsPost(url, param);
//            logger.info("Send order to BOC ok");
//        } catch (Exception e) {
//            logger.info("Send order to BOC failed", e);
//            responseData.setCode(ResponseStatus.ERR.getValue());
//            responseData.setMsg(ResponseStatus.ERR.name());
//            return;
//        }
//        System.out.println(xml);
    }

    /**
     * Description: 商户处理BOC返回的订单支付信息
     * @author: JiuDongDong
     * @param merchantRecvOrderNotifyVo 封装BOC返回的订单支付信息
     * date: 2018/4/11 10:55
     */
    @Override
    public void receiveNotify(BOCMerchantRecvOrderNotifyVo merchantRecvOrderNotifyVo) {
        logger.info("It is now in BOCOrderManagerImpl.receiveNotify, the parameters are: " + JsonUtil.toJson(merchantRecvOrderNotifyVo));
        /* 1. 从中行响应信息中获取支付信息 */
        String methodName = Thread.currentThread().getStackTrace()[Constants.INTEGER_ONE].getMethodName();
        Map<String, Object> params = getMapParams(methodName, merchantRecvOrderNotifyVo);
        String orderNo = merchantRecvOrderNotifyVo.getOrderNo();//订单号
        /* 2. 验签 */
        Boolean verifySuccess = BOCP7Verify.verifySignData(params);
        // 2.1 如果验签成功，处理业务逻辑
        if (verifySuccess) {
            logger.info("Verify notify data ok, the orderNo is: " + orderNo);
            // 如果是订单支付（订单号为19位）则转换为加E加R
            if (orderNo.length() == INTERACTION_ID_LENGTH) {
                merchantRecvOrderNotifyVo.setOrderNo(BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo));
            }
            // 订单处理成功则将订单支付回调数据持久化到本地
            notifyLocalHandle(merchantRecvOrderNotifyVo);
            return;
        }
        // 2.2 如果验签不成功，发起主动查询
        if (!verifySuccess) {
            ResponseData responseData = new ResponseData();
            logger.info("Verify order notify failed with signData error, commonQueryOrder******************  START");
            commonQueryOrder(responseData, orderNo);
            logger.info("Verify order notify failed with signData error, commonQueryOrder******************  END");
            String code = responseData.getCode();
            // 2.2.1 主动查询失败则返回
            if (!ResponseStatus.OK.equals(code)) {
                logger.warn("Notify failed, and commonQueryOrder is failed, too, the orderNo = {}", orderNo);
                return;
            }
            CommQueryOrderResVo commonResponseVo = (CommQueryOrderResVo) responseData.getEntity();
            if (commonResponseVo == null) {
                // 从中行查询不到订单信息，则返回
                logger.error("CommonQueryOrder failed, the orderNo = {}", orderNo);
                return;
            }
            /* 2.2.2 查询订单信息成功则处理业务逻辑 */
            String hdlSts = commonResponseVo.getHdlSts();//订单处理状态信息
            String bdFlg = commonResponseVo.getBdFlg();//0-有包体 1-无包体
            // 如果查询交易成功, 且有包体
            if (hdlSts.equals(HDL_STS_SUCC) && BD_FLG_YES.equals(bdFlg)) {
                logger.info("CommonQueryOrder success, the orderNo = {}", orderNo);
                // 获取当前订单信息
                List<BOCCommQueryOrderResBodyVo> bodyVoList = commonResponseVo.getCommQueryOrderResBodyVoList();
                BOCCommQueryOrderResBodyVo responseBodyVo = bodyVoList.get(Constants.INTEGER_ZERO);
                // 获取订单处理状态
                String status = responseBodyVo.getOrderStatus();
                logger.info("The order status is: " + status);
                if (!ORDER_STATUS_PAYED.equals(status)) {
                    logger.warn("CommonQueryOrder success, but the order status is not correct, the orderNo = {}", orderNo);
                    return;
                }
                // 订单处理成功则将订单支付回调数据持久化到本地
                notifyLocalHandle(responseBodyVo);
                return;
            }
            // TODO 其实如果这里仍然查询不成功，需要运营页面去查询订单支付状态进行后续操作
        }

    }

    /**
     * Description: 订单支付回调数据持久化到本地
     * @author: JiuDongDong
     * @param obj 可以为BOCMerchantRecvOrderNotifyVo或BOCCommQueryOrderResBodyVo类型
     *          merchantRecvOrderNotifyVo 商户接收BOC反馈的订单处理结果信息
     *          responseBodyVo  商户发送查询订单请求(支持卡户信息判断)的响应体信息封装
     * @return java.lang.Boolean 处理成功返回true，否则返回false
     * date: 2018/4/17 17:51
     */
//    private Boolean notifyLocalHandle(BOCMerchantRecvOrderNotifyVo merchantRecvOrderNotifyVo, BOCCommQueryOrderResBodyVo responseBodyVo) {
    private Boolean notifyLocalHandle(Object obj) {
//        if (merchantRecvOrderNotifyVo != null || responseBodyVo != null) {
//            logger.error("Notify or commonQueryOrder must has one empty");
//            return false;
//        }
        // 封装本地处理数据
        String methodName = Thread.currentThread().getStackTrace()[Constants.INTEGER_ONE].getMethodName();
        Map<String, Object> param = getMapParams(methodName, obj);
        if (param == null) {
            return false;
        }
        // 计算手续费及平台收入
        param = CalMoneyByFate.calMoneyByFate(param);
        if (param == null) {
            logger.error("CalMoneyByFate error for BOC");
            return false;
        }
        // TODO 调用子源接口
        boolean ifSuccess = commonsManager.ifSuccess(param);
        logger.info("Serial to merchant " + ifSuccess);
        return ifSuccess;
    }

    /**
     * Description: 将传入的对象的属性值封装为map类型的参数
     * @author: JiuDongDong
     * @param obj 需要将参数封装为map的对象
     * @return java.util.Map<java.lang.String,java.lang.Object> map类型的参数
     * date: 2018/4/17 19:36
     */
    private Map<String, Object> getMapParams(String methodName, Object ... obj) {
        Map<String,Object> params = new HashMap<>();// 封装参数
        // 根据obj的类型封装不同的参数
        if (methodName.equals(Constants.METHOD_RECEIVE_NOTIFY)) {
            logger.info(methodName + " will get map params");
            // 如果是receiveNotify方法，则只会传递一个对象进来
            Object paramObj = obj[Constants.INTEGER_ZERO];
            if (paramObj instanceof BOCMerchantRecvOrderNotifyVo) {
                logger.info("This is a BOCMerchantRecvOrderNotifyVo obj");
                // 这里封装的参数主要用来校验签名
                BOCMerchantRecvOrderNotifyVo notifyVo = (BOCMerchantRecvOrderNotifyVo) paramObj;
                String merchantNo = notifyVo.getMerchantNo();// 商户号
                String orderNo = notifyVo.getOrderNo();// 商户订单号
                String orderSeq = notifyVo.getOrderSeq();// 银行订单流水号
                String cardTyp = notifyVo.getCardTyp();// 银行卡类别
                String payTime = notifyVo.getPayTime();// 支付时间
                String orderStatus = notifyVo.getOrderStatus();// 订单状态
                String payAmount = notifyVo.getPayAmount();// 支付金额
                String acctNo = notifyVo.getAcctNo();//支付卡号
                String holderName = notifyVo.getHolderName();//持卡人姓名
                String ibknum = notifyVo.getIbknum();//支付卡省行联行号
                String orderIp = notifyVo.getOrderIp();// 客户支付IP地址
                String orderRefer = notifyVo.getOrderRefer();// 客户浏览器Refer信息
                String bankTranSeq = notifyVo.getBankTranSeq();// 银行交易流水号
                String returnActFlag = notifyVo.getReturnActFlag();// 返回操作类型
                String phoneNum = notifyVo.getPhoneNum();// 电话号码
                String signData = notifyVo.getSignData();// 中行签名数据

                params.put(Constants.MERCHANT_NO, merchantNo);// 商户号
//        params.put(MERCHANT_NO, "1");// 商户号
                params.put(Constants.SIGN_DATA, signData);// 中行签名数据
                // 如果是订单支付（订单号为19位）则转换为加E加R
                if (orderNo.length() == INTERACTION_ID_LENGTH) {
                    params.put(Constants.ORDER_NO, BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo));// 商户订单号
                }
                params.put(Constants.ORDER_SEQ, orderSeq);// 银行订单流水号
                params.put(Constants.CARD_TYP, cardTyp);// 银行卡类别
                params.put(Constants.PAY_TIME, payTime);// 支付时间
                params.put(ORDER_STATUS, orderStatus);// 订单状态
                params.put(Constants.PAY_AMOUNT, payAmount);// 支付金额
//        params.put(PAY_AMOUNT, "1.00");// 支付金额
                if (!StringUtils.isBlank(acctNo)) params.put(Constants.ACCT_NO, acctNo);//支付卡号
                if (!StringUtils.isBlank(holderName)) params.put(Constants.HOLDER_NAME, holderName);//持卡人姓名
                if (!StringUtils.isBlank(ibknum)) params.put(Constants.IBKNUM, ibknum);//支付卡省行联行号
                params.put(ORDER_IP, orderIp);// 客户支付IP地址
                params.put(Constants.ORDER_REFER, orderRefer);// 客户浏览器Refer信息
                params.put(Constants.BANK_TRAN_SEQ, bankTranSeq);// 银行交易流水号
                params.put(Constants.RETURN_ACT_FLAG, returnActFlag);// 返回操作类型
                params.put(Constants.PHONE_NUM, phoneNum);// 电话号码
                // 加入方法名
//                params.put(METHOD_NAME, Thread.currentThread().getStackTrace()[Constants.INTEGER_ONE].getMethodName());
                params.put(METHOD_NAME, methodName);

                return params;
            }

        }

        if (methodName.equals(Constants.METHOD_NOTIFY_LOCAL_HANDLE)) {
            logger.info(methodName + " will get map params");
            // notifyLocalHandle方法里，会传递2个参数进来，但是必有一个为null
            Object paramObj = new Object();
            for (Object o : obj) {
                if (o != null) {
                    paramObj = o;
                    break;
                }
            }
            String merchantNo = "";// 商户号
            String orderNo = "";// 商户订单号
            String orderSeq = "";// 银行订单流水号
            String payTime = "";// 支付时间
            String payAmount = "";// 支付金额
            String acctNo = "";//支付卡号

            // 封装参数
            if (paramObj instanceof BOCCommQueryOrderResBodyVo) {
                logger.info("This is a BOCCommQueryOrderResBodyVo obj");
                // 这里封装的参数主要用来对订单支付成功后，回调失败，进行common查询到的支付信息封装为map，为后续持久化准备
                BOCCommQueryOrderResBodyVo responseBodyVo = (BOCCommQueryOrderResBodyVo) paramObj;
                merchantNo = responseBodyVo.getMerchantNo();// 商户号
                orderNo = responseBodyVo.getOrderNo();// 商户订单号
                orderSeq = responseBodyVo.getOrderSeq();// 银行订单流水号
                payTime = responseBodyVo.getPayTime();// 支付时间
                payAmount = responseBodyVo.getPayAmount();// 支付金额
                acctNo = responseBodyVo.getAcctNo();//支付卡号
            }
            if (paramObj instanceof BOCMerchantRecvOrderNotifyVo) {
                logger.info("This is a BOCMerchantRecvOrderNotifyVo obj");
                // 这里封装的参数主要用来对订单支付成功后，将notify里接收到的支付信息封装为map，为后续持久化准备
                BOCMerchantRecvOrderNotifyVo responseBodyVo = (BOCMerchantRecvOrderNotifyVo) paramObj;
                merchantNo = responseBodyVo.getMerchantNo();// 商户号
                orderNo = responseBodyVo.getOrderNo();// 商户订单号
                orderSeq = responseBodyVo.getOrderSeq();// 银行订单流水号
                payTime = responseBodyVo.getPayTime();// 支付时间
                payAmount = responseBodyVo.getPayAmount();// 支付金额
                acctNo = responseBodyVo.getAcctNo();//支付卡号
            }
            if (StringUtils.isBlank(merchantNo) || StringUtils.isBlank(orderNo) || StringUtils.isBlank(orderSeq)
                    || StringUtils.isBlank(payTime) || StringUtils.isBlank(payAmount)) {
                logger.warn("The param has empty");
                return null;
            }
            // 如果是订单支付（订单号为19位）则转换为加E加R
            if (orderNo.length() == INTERACTION_ID_LENGTH) {
                params.put(Constants.ORDER_NO, BOBOrderNoFormat.bob19OrderNoTo20WithER(orderNo));// 商户订单号
            }
            params.put(Constants.ORDER_ID, orderNo);//订单号
            params.put(Constants.CHANNEL_FLOW_ID, orderSeq);//支付渠道流水号
            if (!StringUtils.isBlank(acctNo)) params.put(Constants.PAYER_ID, acctNo);//付款人ID（支付卡号）
            params.put(Constants.PAYER_PAY_AMOUNT, payAmount);//付款方支付金额
            params.put(Constants.RECEIVER_USER_ID, merchantNo);//收款人ID（商户号）
            params.put(Constants.SUCCESS_TIME, payTime);//支付成功时间
            params.put(Constants.IS_REFUND, Constants.IS_REFUND_NO + "");//是否退款 0:否,1是
            params.put(Constants.TRADE_TYPE, Constants.TRADE_TYPE_1 + "");//交易类型  1:订单,2,退款,3,线下充值,4线上充值,5:提现,6:商户结算打款,7:平台增值服务收款
            params.put(Constants.RETURN_INFO, null);//返回信息
            params.put(Constants.DESP, null);//描述
            params.put(Constants.PAY_CHANNEL, Constants.INTEGER_THREE + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行
            params.put(Constants.UID, Constants.UID_BOC);//操作人标识, 中行1003
            return params;
        }

        return null;
    }
    
    /**
     * Description: 商户查询订单信息(原始版)
     * @author: JiuDongDong
     * @param orderNos 待查询状态的订单
     * date: 2018/4/11 16:42
     */
    @Override
    public void queryOrder(ResponseData responseData, String orderNos) {
        logger.info("It is now in BOCOrderManagerImpl.queryOrder, the parameters are [orderNos = {}]", orderNos);
        String merchantNo = bocPayConfigure.getMerchantNo();
        // 校验最多50个订单
        Boolean orderNoNum = calOrderNoNum(orderNos);
        if (!orderNoNum) {
            logger.error("Order number is more than 50");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        String signData = BOCP7Sign.getQueryOrderSignData(merchantNo, orderNos);
        // 发送SSL请求
        String url = bocPayConfigure.getQueryOrderUrl();
        Map<String, String> param = new HashMap<>();
        param.put(Constants.MERCHANT_NO, merchantNo);
        param.put(ORDER_NOS, orderNos);
        param.put(Constants.SIGN_DATA, signData);
        String xml = null;
        try {
            logger.info("Try to obtain order info from BOC start");
            xml = HttpsUtils.bocHttpsPost(url, param);
            logger.info("Obtain order info from BOC ok");
        } catch (Exception e) {
            logger.info("Try to https to BOC failed", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

//        BasicNameValuePair basicNameValuePair = new BasicNameValuePair("12","da");
//        System.out.println(xml);
//        Document document;
//        try {
//            document = DocumentHelper.parseText(xml);
//            Element rootElement = document.getRootElement();
//            // 获取head
//            Element headerElement = rootElement.element(HEADER);
//            Element merchantNoElement = headerElement.element("merchantNo");
//            Element exceptionElement = headerElement.element("exception");
//
//            String merchantNo1 = merchantNoElement.getTextTrim();
//            System.out.println(merchantNo1);
//            if (exceptionElement != null) {
//                String exception = exceptionElement.getTextTrim();
//                System.out.println(exception);
//            }
//
//            // 获取body
//            Element bodyElement = rootElement.element(BODY);
//            List<Element> orderTranList = bodyElement.elements("orderTrans");
//            for (Element orderTrans : orderTranList) {
//                Element orderNoElement = orderTrans.element("orderNo");
//                String orderNo = orderNoElement.getTextTrim();
//                System.out.println(orderNo);
//                Element orderSeqElement = orderTrans.element("orderSeq");
//                String orderSeq = orderSeqElement.getTextTrim();
//                System.out.println(orderSeq);
//                Element orderStatusElement = orderTrans.element("orderStatus");
//                String orderStatus = orderStatusElement.getTextTrim();
//                System.out.println(orderStatus);
//                Element cardTypElement = orderTrans.element("cardTyp");
//                String cardTyp = cardTypElement.getTextTrim();
//                System.out.println(cardTyp);
//                Element acctNoElement = orderTrans.element("acctNo");
//                if (acctNoElement != null) {
//                    String acctNo = acctNoElement.getTextTrim();
//                    System.out.println(acctNo);
//                }
//                Element holderNameElement = orderTrans.element("holderName");
//                if (holderNameElement != null) {
//                    String holderName = holderNameElement.getTextTrim();
//                    System.out.println(holderName);
//                }
//                Element ibknumElement = orderTrans.element("ibknum");
//                if (ibknumElement != null) {
//                    String ibknum = ibknumElement.getTextTrim();
//                    System.out.println(ibknum);
//                }
//                Element payTimeElement = orderTrans.element("payTime");
//                String payTime = payTimeElement.getTextTrim();
//                System.out.println(payTime);
//                Element payAmountElement = orderTrans.element("payAmount");
//                String payAmount = payAmountElement.getTextTrim();
//                System.out.println(payAmount);
//
//            }
//
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Description: 商户发送查询订单请求(支持卡户信息判断)
     * @author: JiuDongDong
     * @param orderNos 待查询状态的订单
     * date: 2018/4/11 20:08
     */
    @Override
    public void commonQueryOrder(ResponseData responseData, String orderNos) {
        logger.info("It is now in BOCOrderManagerImpl.queryOrder, the parameters are [orderNos = {}]", orderNos);
        String merchantNo = bocPayConfigure.getMerchantNo();// 商户号
        /* 1. 校验最多50个订单 */
        Boolean orderNoNum = calOrderNoNum(orderNos);
        if (!orderNoNum) {
            logger.error("Order number is more than 50, check orderNos number");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        /* 2. 签名 */
        String signData = BOCP7Sign.getQueryOrderSignData(merchantNo, orderNos);

        /* 3. 发送查询请求 */
        String url = bocPayConfigure.getCommonQueryOrderUrl();
        // 3.1 封装查询参数
        Map<String, String> param = new HashMap<>();
        param.put(Constants.MERCHANT_NO, merchantNo);
        param.put(ORDER_NOS, orderNos);
        param.put(Constants.SIGN_DATA, signData);
        // 3.2 请求
        String xml = null;
        try {
            logger.info("Try to obtain orders info from BOC start");
            xml = HttpsUtils.bocHttpsPost(url, param);
            logger.info("Obtain orders info from BOC ok");
        } catch (Exception e) {
            logger.info("Try to https to BOC failed", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        /* 4. 解析响应数据 */
        Document document;
        try {
            logger.info("Now is going to parse xml to Document");
            document = DocumentHelper.parseText(xml);
            logger.info("Parse xml OK");
        } catch (DocumentException e) {
            logger.error("Error occurred when parse xml to Document, the xml is: " + xml, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        Element rootElement = document.getRootElement();// 根节点
        // 获取head
        Element headerElement = rootElement.element(HEADER);// 业务头报文块
        Element msgIdElement = headerElement.element(MSG_ID);// 报文标识号
        Element hdlStsElement = headerElement.element(HDL_STS);// 处理状态 A-成功  B-失败  K-未明
        Element bdFlgElement = headerElement.element(BD_FLG);// 业务体报文块存在标识 0-有包体 1-无包体
        Element rtnCdElement = headerElement.element(RTN_CD);// 报文处理返回码
        Element exceptionElement = headerElement.element(EXCEPTION);//文档中，并没有写出有Exception，但原始版的有，所以这里也给加上了
        String msgId = msgIdElement.getTextTrim();
        String hdlSts = hdlStsElement.getTextTrim();
        String bdFlg = bdFlgElement.getTextTrim();
        String rtnCd = rtnCdElement.getTextTrim();
        String exception = null;
        if (exceptionElement != null) {
            exception = exceptionElement.getTextTrim();
        }
        // 4.1 如果处理失败，返回失败原因
        if (!hdlSts.equals(HDL_STS_SUCC)) {
            logger.error("Common query order failed with exception = {}", exception);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 4.2 查询成功但没有查询到订单信息，则返回空
        Element bodyElement = rootElement.element(BODY);
        if (bdFlg.equals(BD_FLG_NO)) {
            logger.error("Common query order success but has no order info");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 4.3 查询成功则查询到订单信息（即有包体）从body中获取订单信息
        List<Element> orderTranList = bodyElement.elements(ORDER_TRANS);
        CommQueryOrderResVo commonResponseVo = new CommQueryOrderResVo();
        List<BOCCommQueryOrderResBodyVo> bodyVoList = commonResponseVo.getCommQueryOrderResBodyVoList();
        for (Element orderTrans : orderTranList) {
            BOCCommQueryOrderResBodyVo commonResponseBodyVo = new BOCCommQueryOrderResBodyVo();
            Element merchantNoElement = orderTrans.element(Constants.MERCHANT_NO);
            String merchantNo1 = merchantNoElement.getTextTrim();
            commonResponseBodyVo.setMerchantNo(merchantNo1);
            Element orderNoElement = orderTrans.element(Constants.ORDER_NO);
            String orderNo = orderNoElement.getTextTrim();
            commonResponseBodyVo.setOrderNo(orderNo);
            Element orderSeqElement = orderTrans.element(Constants.ORDER_SEQ);
            String orderSeq = orderSeqElement.getTextTrim();
            commonResponseBodyVo.setOrderSeq(orderSeq);
            Element orderStatusElement = orderTrans.element(ORDER_STATUS);
            String orderStatus = orderStatusElement.getTextTrim();
            commonResponseBodyVo.setOrderStatus(orderStatus);
            Element cardTypElement = orderTrans.element(Constants.CARD_TYP);
            String cardTyp = cardTypElement.getTextTrim();
            commonResponseBodyVo.setCardTyp(cardTyp);
            Element acctNoElement = orderTrans.element(Constants.ACCT_NO);
            if (acctNoElement != null) {
                String acctNo = acctNoElement.getTextTrim();
                commonResponseBodyVo.setAcctNo(acctNo);
            }
            Element holderNameElement = orderTrans.element(Constants.HOLDER_NAME);
            if (holderNameElement != null) {
                String holderName = holderNameElement.getTextTrim();
                commonResponseBodyVo.setHolderName(holderName);
            }
            Element ibknumElement = orderTrans.element(Constants.IBKNUM);
            if (ibknumElement != null) {
                String ibknum = ibknumElement.getTextTrim();
                commonResponseBodyVo.setIbknum(ibknum);
            }
            Element payTimeElement = orderTrans.element(Constants.PAY_TIME);
            String payTime = payTimeElement.getTextTrim();
            commonResponseBodyVo.setPayTime(payTime);
            Element payAmountElement = orderTrans.element(Constants.PAY_AMOUNT);
            String payAmount = payAmountElement.getTextTrim();
            commonResponseBodyVo.setPayAmount(payAmount);
            Element visitorIpElement = orderTrans.element(VISITOR_IP);// 客户通过网银支付时的IP地址信息
            String visitorIp = visitorIpElement.getTextTrim();
            commonResponseBodyVo.setVisitorIp(visitorIp);
            Element visitorReferElement = orderTrans.element(VISITOR_REFER);// 客户浏览器跳转至网银支付登录界面前所在页面的URL（urlEncode格式）
            String visitorRefer = visitorReferElement.getTextTrim();
            commonResponseBodyVo.setVisitorRefer(visitorRefer);
            // 包体加入list
            bodyVoList.add(commonResponseBodyVo);
        }
        commonResponseVo.setCommQueryOrderResBodyVoList(bodyVoList);

        /* 5.返回订单信息 */
        logger.info("Common query order success");
        responseData.setEntity(commonResponseVo);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 商户发送B2C退款指令(按最新设计方案，此功能由这个类替换：com.ewfresh.pay.policy.impl.BOCRefundPolicy)
     * @author: JiuDongDong
     * @param refundParam 封装退款信息
     * date: 2018/4/11 21:16
     */
    @Override
//    public void refundOrder(ResponseData responseData, String mRefundSeq, String refundAmount, String orderNo) {
    public void refundOrder(ResponseData responseData, RefundParam refundParam) {
        logger.info("It is now in BOCOrderManagerImpl.refundOrder, the parameters are [refundParam = {}]", JsonUtil.toJson(refundParam));
        /* 1、准备中行请求参数 */
        String merchantNo = bocPayConfigure.getMerchantNo();// 商户号
        String curCode = bocPayConfigure.getCurCode();// 币种
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        String channelType = refundParam.getChannelType();// 类型(07：互联网，08：移动)
        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(channelType) || StringUtils.isBlank(orderNo)) {
            logger.warn("The params tradeNo or refundAmount or outRequestNo or outTradeNo or channelType or orderNo is empty");
            return;
        }
        // 生成商户系统产生的退款交易流水号
        String mRefundSeq = BOCRefundSeqFormat.orderNo2BOCRefundString(orderNo);
        String signData = BOCP7Sign.getRefundOrderSignData(merchantNo, mRefundSeq, curCode, refundAmount, orderNo);// 签名
        String url = bocPayConfigure.getRefundOrderUrl();
        // 封装map参数
        Map<String, String> param = new HashMap<>();
        param.put(Constants.MERCHANT_NO, merchantNo); // 商户号
        param.put(Constants.M_REFUND_SEQ, mRefundSeq); // 商户系统产生的交易流水号
        param.put(CUR_CODE, curCode);// 币种
        param.put(Constants.REFUND_AMOUNT, refundAmount);// 退款金额
        param.put(Constants.ORDER_NO, orderNo);// 订单号
        param.put(Constants.SIGN_DATA, signData);// 签名

        /* 2、发送Httpclient请求退款 */
        String xml = null;
        try {
            logger.info("Try to refund order from BOC start");
            xml = HttpsUtils.bocHttpsPost(url, param);
            logger.info("Refund order from BOC ok");
        } catch (Exception e) {
            logger.error("Try to refund order failed", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        /* 3、从返回报文获取必要参数，并校验 */
        // 3.1 解析报文，获取参数
        Document document = null;
        try {
            logger.info("Now is going to parse refund xml to Document");
            document = DocumentHelper.parseText(xml);
            logger.info("Parse refund xml OK");
        } catch (DocumentException e) {
            logger.error("Error occurred when parse refund xml to Document, the xml is: " + xml, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        Element rootElement = document.getRootElement();
        // 获取head
        Element headerElement = rootElement.element(HEADER);
        Element msgIdElement = headerElement.element(MSG_ID);// 报文标识号
        Element hdlStsElement = headerElement.element(HDL_STS);// 处理状态 A-成功  B-失败  K-未明
        // TODO 文档里，处理状态有冲突，测试环境OK后，确认下，到底是“hdlStsName”还是“dealStatus”
        Element dealStatusElement = headerElement.element(Constants.DEAL_STATUS);//处理状态0：成功 1：失败 2：未明
        String dealStatus = dealStatusElement.getTextTrim();//
        Element bdFlgElement = headerElement.element(BD_FLG);// 业务体报文块存在标识 0-有包体 1-无包体
        Element rtnCdElement = headerElement.element(RTN_CD);// 报文处理返回码
        Element exceptionElement = headerElement.element(EXCEPTION);//文档中，并没有写出有Exception，但原始版的有，所以这里也给加上了
        String exception = null;
        if (exceptionElement != null) {
            exception = exceptionElement.getTextTrim();
        }
        // 如果交易失败则打印失败信息并返回
        if (!dealStatus.equals(DEAL_STATUS_SUCC)) {
            logger.error("BOC refund failed, dealStatus = {}, exception = {}", dealStatus, exception);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

//        Element returnActFlagElement = headerElement.element(RETURN_ACT_FLAG);//返回操作类型,银行返回的操作类型（该交易结果返回为3）1：支付结果通知 2：电话订单结果通知 3：退款结果通知 4：订单查询结果
//        Element dealStatusElement = headerElement.element(dealStatusName);//处理状态0：成功 1：失败 2：未明
//        String dealStatus = dealStatusElement.getTextTrim();//
//        Element bodyFlagElement = headerElement.element(bodyFlagName);//包体标志 0：有包体数据 1：无包体数据

        // 获取body（交易成功，则一定会有包体）
        Element bodyElement = rootElement.element(BODY);

        Element merchantNoElement = bodyElement.element(Constants.MERCHANT_NO);//商户号
        String merchantNo1 = merchantNoElement.getTextTrim();//中行返回的商户号
        Element mRefundSeqElement = bodyElement.element(Constants.M_REFUND_SEQ);//商户系统产生的交易流水号
        String mRefundSeq1 = mRefundSeqElement.getTextTrim();
        Element refundAmountElement = bodyElement.element(Constants.REFUND_AMOUNT);//退款金额
        String refundAmount1 = refundAmountElement.getTextTrim();
        Element orderNoElement = bodyElement.element(Constants.ORDER_NO);//商户订单号
        String orderNo1 = orderNoElement.getTextTrim();
        Element orderSeqElement = bodyElement.element(Constants.ORDER_SEQ);//银行的订单流水号（银行产生的订单唯一标识）
        String orderSeq = orderSeqElement.getTextTrim();
        Element orderAmountElement = bodyElement.element(ORDER_AMOUNT);//订单金额
        String orderAmount = orderAmountElement.getTextTrim();
        Element bankTranSeqElement = bodyElement.element(Constants.BANK_TRAN_SEQ);//银行交易流水号
        String bankTranSeq = bankTranSeqElement.getTextTrim();
        Element tranTimeElement = bodyElement.element(Constants.TRAN_TIME);//银行交易时间：YYYYMMDDHHMISS
        String tranTime = tranTimeElement.getTextTrim();
        Element signDataElement = bodyElement.element(Constants.SIGN_DATA);//中行签名数据
        String signDataReturn = signDataElement.getTextTrim();
        // 3.2 验签
        // 商户号|商户退款交易流水号|退款金额|商户订单号|银行订单流水号|订单金额|银行交易流水号|银行交易时间|退款处理状态
        // merchantNo|mRefundSeq|refundAmount|orderNo|orderSeq|orderAmount|bankTranSeq|tranTime|dealStatus
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.MERCHANT_NO, merchantNo1);
        params.put(Constants.M_REFUND_SEQ, mRefundSeq);
        params.put(Constants.REFUND_AMOUNT, refundAmount);
        params.put(Constants.ORDER_NO, orderNo);
        params.put(Constants.ORDER_SEQ, orderSeq);
        params.put(ORDER_AMOUNT, orderAmount);
        params.put(Constants.BANK_TRAN_SEQ, bankTranSeq);
        params.put(Constants.TRAN_TIME, tranTime);
        params.put(Constants.DEAL_STATUS, dealStatus);// TODO dealStatus和halSts留其一即可
//        params.put(HDL_STS, hdlSts);
        params.put(Constants.SIGN_DATA, signDataReturn);
        params.put(METHOD_NAME, Thread.currentThread().getStackTrace()[Constants.INTEGER_ONE].getMethodName());// 当前方法名
        // 验签
        Boolean verifySuccess = BOCP7Verify.verifySignData(params);
        // 验签失败则返回
        if (!verifySuccess) {
            logger.error("Verify BOC refund sign data failed");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        /* 4、银行退款成功，进行商户业务处理 */
        // 根据支付渠道流水号获取退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
        String payerId = payFlow.getPayerId();//付款人id
        String receiverUserId = payFlow.getReceiverUserId();//收款人id
        String payerName = payFlow.getPayerName();//付款人名称
        String receiverName = payFlow.getReceiverName();//收款人名称
        payFlow.setPayerPayAmount(new BigDecimal(refundAmount1));//付款方支付金额（即商户退款金额）
        // 收款人id和付款人id对调、收款人名称和付款人名称对调
        payFlow.setPayerId(receiverUserId);
        payFlow.setReceiverUserId(payerId);
        payFlow.setPayerName(receiverName);
        payFlow.setReceiverName(payerName);
        // 支付成功时间
        try {
            logger.info("Try parse tranTime when handle refund param, the tranTime = {}", tranTime);
            payFlow.setSuccessTime(sdf.parse(tranTime));
        } catch (ParseException e) {
            // 事实上，这里catch异常只是语法要求，银行处理成功后，时间格式不会有误，假定这里发生了异常，也仅仅是时间解析异常，银行退款让是成功的，所以继续处理后续业务
            logger.error("Parse tranTime occurred error, the tranTime = {}", tranTime);
            payFlow.setSuccessTime(null);
        }
        // 是否退款
        payFlow.setIsRefund(Constants.SHORT_ONE);
        // 插入记录
        payFlowService.addPayFlow(payFlow);
        logger.info("Refund order success, the orderNo = {}", orderNo1);
        responseData.setEntity(payFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 商户取票
     * @author: JiuDongDong
     * @param fileDate 文件日期
     * @param extend 扩展域名
     * @param handleType  必填 0:上传 1:下载
     * @param fileType  下载文件类型：CC	B2C清算对账文件
                                    GS	B2C业务对账文件
                                    RA	B2C退货反馈文件
                                    TS	B2C交易流水文件
                                    XY	B2C客户签约文件
                                    YZ	B2C客户身份认证文件
                        上传文件类型：URA	B2C商户退货文件
                                    UMRA	B2B商户退货文件
     * date: 2018/4/12 17:48
     */
    private Map<String, String> getTicket(String fileDate, String extend, String handleType, String fileType) throws Exception{
        logger.info("It is now in BOCOrderManagerImpl.getTicket, the parameters are [fileDate = {}, extend = {}, handleType = {}, fileType = {}]", fileDate, extend, handleType, fileType);
        /* 1.取票请求 */
        String merchantNo = bocPayConfigure.getMerchantNo();
        Date now = new Date();
        String submitTime = sdf.format(now);
        // 生成签名
        String signData = BOCP7Sign.getTicketSignData(extend, fileDate, fileType, handleType, merchantNo, submitTime);
        // 获取取票url
        String ticketUrl = bocPayConfigure.getGetTicketUrl();
        // 发送SSL请求取票
        Map<String, String> param = new HashMap<>();
        if (!StringUtils.isBlank(extend)) param.put(Constants.EXTEND, extend);// 附加域
        if (!StringUtils.isBlank(fileDate)) param.put(Constants.FILE_DATE, fileDate);// 需要下载的文件的日期 格式：YYYYMMDD
        param.put(Constants.FILE_TYPE, fileType);// 文件类型
        param.put(Constants.HANDLE_TYPE, handleType);// 操作类型 0:上传  1:下载
        param.put(Constants.MERCHANT_NO, merchantNo);
        param.put(Constants.SUBMIT_TIME, submitTime);// 提交时间 YYYYMMDD24HHMMSS
        param.put(Constants.SIGN_DATA, signData);
        logger.info("Https to obtain ticket for " + fileDate + ", " + handleType + ", " + fileType);
        String xml = HttpsUtils.bocHttpsPost(ticketUrl, param);

        /* 2.处理返回信息*/
        logger.info("Now is going to parse xml to Document");
        Document document = DocumentHelper.parseText(xml);
        Element rootElement = document.getRootElement();
        // 获取head
        Element headerElement = rootElement.element(HEADER);
        Element msgIdElement = headerElement.element(MSG_ID);//报文标识号
        Element hdlStsElement = headerElement.element(HDL_STS);//A-成功  B-失败  K-未明
        String hdlSts = hdlStsElement.getTextTrim();
        Element bdFlgElement = headerElement.element(BD_FLG);//0-有包体 1-无包体
        Element rtnCdElement = headerElement.element(RTN_CD);//报文处理返回码 文件不存在、取票失败等，具体错误码参见错误码定义章节
        String rtnCdTrim = rtnCdElement.getTextTrim();

        /* 2.1 如果取票不成功，返回null */
        if (!hdlSts.equals(HDL_STS_SUCC)) {
            logger.warn("Error occurred when download account file, the hdlSts is: " + hdlSts + ", the rtnCd is: " + rtnCdTrim);
            return null;
        }

        /* 2.2 取票成功，处理获取票信息 */
        // 获取body
        Element bodyElement = rootElement.element(BODY);
        Element merchantNoElement = bodyElement.element(Constants.MERCHANT_NO);
        String merchantNoRes = merchantNoElement.getTextTrim();
        Element handleTypeElement = bodyElement.element(Constants.HANDLE_TYPE);
        String handleTypeRes = handleTypeElement.getTextTrim();
        Element fileTypeElement = bodyElement.element(Constants.FILE_TYPE);
        String fileTypeRes = fileTypeElement.getTextTrim();
        Element fileDateElement = bodyElement.element(Constants.FILE_DATE);
        String fileDateRes = fileDateElement.getTextTrim();
        Element submitTimeElement = bodyElement.element(Constants.SUBMIT_TIME);
        String submitTimeRes = submitTimeElement.getTextTrim();
        Element extendElement = bodyElement.element(Constants.EXTEND);
        String extendRes = extendElement.getTextTrim();
        Element uriElement = bodyElement.element(Constants.URI);
        String uriRes = uriElement.getTextTrim();
        Element ticketIdElement = bodyElement.element(Constants.TICKET_ID);
        String ticketIdRes = ticketIdElement.getTextTrim();
        Element invalidTimeElement = bodyElement.element(Constants.INVALID_TIME);// 票失效时间 YYYYMMDD24HHMMSS
        String strInvalidTime = invalidTimeElement.getTextTrim();
        logger.info("The ticket " + ticketIdRes + " invalid time is: " + strInvalidTime);
        Date dateInvalidTime = sdf.parse(strInvalidTime);
        Element signDataElement = bodyElement.element(Constants.SIGN_DATA);
        String signDataRes = signDataElement.getTextTrim();

        /* 2.3 验签 */
        Map<String,Object> params = new HashMap<>();
        params.put(Constants.EXTEND, extendRes);// 附加域
        params.put(Constants.FILE_DATE, fileDateRes);// 文件日期
        params.put(Constants.FILE_TYPE, fileTypeRes);// 文件类型
        params.put(Constants.HANDLE_TYPE, handleTypeRes);// 操作类型
        params.put(Constants.INVALID_TIME, strInvalidTime);// 票失效时间
        params.put(Constants.MERCHANT_NO, merchantNoRes);// 商户号
        params.put(Constants.SUBMIT_TIME, submitTimeRes);// 提交时间
        params.put(Constants.TICKET_ID, ticketIdRes);// 票号
        params.put(Constants.URI, uriRes);// URI标识符，上传、下载文件时需要
        params.put(Constants.SIGN_DATA, signDataRes);// BOC签名

        Boolean verifySuccess = BOCP7Verify.verifySignData(params);
        // 如果验签不成功，返回null
        if (!verifySuccess) {
            logger.error("Verify ticket signData failed");
            return null;
        }

        /* 2.4 如果验签成功，确认票是否过期 */
        if (dateInvalidTime.after(now)) {
            logger.error("The ticket is invalid, please retry to obtain");
            return null;
        }

        /* 2.5 无误返回票号信息 */
        if (verifySuccess) {
            logger.info("Verify ticket signData ok");
            Map<String, String> map = new HashMap<>();
            map.put(Constants.URI, uriRes);
            map.put(Constants.TICKET_ID, ticketIdRes);
            return map;
        }
        return null;
    }

    /**
     * Description: 商户取票，下载对账文件
     * @author: JiuDongDong
     * @param fileDate 文件日期
     * date: 2018/4/12 11:48
     */
    @Override
    public void getTicketDownloadFile(ResponseData responseData, String fileDate, String extend) {
        String handleType = Constants.INTEGER_ONE + "";
        String fileType = Constants.FILE_TYPE_GS;
        if (StringUtils.isBlank(fileDate)) {
            fileDate = new DateTime().minusDays(4).toString(DATE_FORMAT);
        }
        // 取票
        Map<String, String> ticketIdAndUri;
        try {
            logger.info("Now is going to obtain uri and ticket, the fileDate is: " + fileDate + ", the extend is:" + extend + ", the handleType is:" + handleType + ", the fileType is:" + fileType);
            ticketIdAndUri = getTicket(fileDate, extend, handleType, fileType);
            if (ticketIdAndUri == null) {
                // 有可能是因为票过期，重新取一次
                ticketIdAndUri = getTicket(fileDate, extend, handleType, fileType);
            }
        } catch (Exception e) {
            logger.error("Error occurred when obtain ticketAndUri, the fileDate is: " + fileDate + ", the extend is:" + extend + ", the handleType is:" + handleType + ", the fileType is:" + fileType, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 如果取到的票为null，说明取票系统出错，或网络连接异常导致取不到票，return
        if (ticketIdAndUri == null) {
            logger.warn("The obtained ticket is null, the fileDate is: " + fileDate + ", the extend is:" + extend + ", the handleType is:" + handleType + ", the fileType is:" + fileType);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        String uri = ticketIdAndUri.get(Constants.URI);//下载文件uri
        String ticketId = ticketIdAndUri.get(Constants.TICKET_ID);//票

        // 下载对账文件
        String downloadFileUrl = bocPayConfigure.getMerchantDownloadFileUrl();
        String downloadResponseXml = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.URL, downloadFileUrl);//取票网关
            params.put(Constants.TICKET_ID, ticketId);//票
            params.put(Constants.URI, downloadFileUrl);//下载文件uri
            String destFileName = "D:/down/boc/account/zip/" + fileDate + "_account.zip";
            params.put("destFileName", destFileName);//目标文件
            HttpsUtils.httpPostDownloadFile(uri, params, destFileName);
            // 解压到指定目录
            FileUtil.unZip(destFileName, "D:/down/boc/account/uzip/" + fileDate + "/");
            // 遍历文件,判断是否下载失败,失败则返回
            File[] fs = new File("D:/down/boc/account/uzip/" + fileDate + "/").listFiles();
//            List<T> list = new ArrayList<>();
            for (File file : fs) {
                File currFile = file.getAbsoluteFile();
                FileInputStream fis = new FileInputStream(currFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String str = "";
                // 下载失败则log信息并返回
                if (file.getAbsolutePath().contains("error")) {
                    while((str = br.readLine()) != null){
                        sb = sb.append(str);
                    }
                    br.close();
                    logger.error("Download account file failed, error reason = {}", sb.toString());
                    responseData.setCode(ResponseStatus.ERR.getValue());
                    responseData.setMsg(ResponseStatus.ERR.name());
                    return;
                }
//                // TODO 下载成功，则处理业务数据
//                while((str = br.readLine()) != null){
//                    String[] split = str.split("\\|");
//                    T t = new T();
//                    t.set(split[0]);
//                    t.set(split[1]);
//                    t.set(split[2]);
//                    t.set(split[3]);...
//                    list.add(t);
//                }
//                br.close();
//                file.delete();
            }
            // TODO 数据持久化
            System.out.println(downloadResponseXml);
        } catch (Exception e) {
            logger.error("Error occurred when download account file", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

    }

    /**
     * Description: 校验订单个数
     * @author: JiuDongDong
     * @param orderNos 订单拼接的字符串
     * @return java.lang.Boolean 超过上限返回false
     * date: 2018/4/13 20:04
     */
    private Boolean calOrderNoNum(String orderNos) {
        // BOC要求订单查询每次最多50个
        String[] split = orderNos.split("\\|");
        if (split.length > Constants.INTEGER_FIFTY) {
            return false;
        }
        return true;
    }

}
