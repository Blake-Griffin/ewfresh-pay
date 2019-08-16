package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.pay.configure.Bill99QuickPayConfigure;
import com.ewfresh.pay.manager.Bill99QuickManager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.bill99quick.TransInfo;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.model.vo.OrderInfoVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.GetUserInfoRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.redisService.SendMessageRedisService;
import com.ewfresh.pay.service.*;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99quick.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.BILL99_Q_RESPONSE_CODE_68;
import static com.ewfresh.pay.util.Constants.BILL99_Q_RESPONSE_CODE_C0;
import static com.ewfresh.pay.util.Constants.BILL99_Q_TXN_STATUS_PAY_P;

/**
 * Description: Bill99快捷支付的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/9/14 9:47
 */
@Component
public class Bill99QuickManagerImpl implements Bill99QuickManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99QuickPayConfigure bill99QuickPayConfigure;
    @Autowired
    private PayChannelService payChannelService;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private SendMessageRedisService sendMessageRedisService;
    @Autowired
    private GetUserInfoRedisService getUserInfoRedisService;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private GetOrderInfoFromRedisUtil getOrderInfoFromRedisUtil;
    @Value("${http_msg}")
    private String msgUrl;
    @Autowired
    private MsgClient msgClient;
    private String SHOW_PHONE = "showPhone";
    private static final String RANDOM = "random";// 解密支付密码的随机数
    private static final String CREATE_TIME = "createTime";
    private static final String CURRENT_TIME = "currentTime";
    private static final int CALCULATE = 1000;
    private static final String PCI_VERSION = "1.0";//PCI版本，固定值：v2.0,该参数必填。

    private static final String KUAIQIAN = "快钱";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final String QRY_CARD_CONTENT = "QryCardContent";// 卡信息查询节点
    private static final String CARD_INFO_CONTENT = "CardInfoContent";// 卡信息节点
    private static final String ERROR_MSG_CONTENT = "ErrorMsgContent";// 错误信息节点
    private static final String IND_AUTH_CONTENT = "indAuthContent";// 卡信息验证节点
    private static final String IND_AUTH_DYN_VERIFY_CONTENT = "indAuthDynVerifyContent";// 动态码卡信息验证节点
    private static final String PCI_QUERY_CONTENT = "PciQueryContent";// PCI卡信息查询节点
    private static final String PCI_INFOS = "pciInfos";// PCI卡信息节点
    private static final String PCI_DELETE_CONTENT = "PciDeleteContent";// PCI卡信息删除节点
    private static final String TXN_MSG_CONTENT = "TxnMsgContent";// 消费交易节点
    private static final String GET_DYN_NUM_CONTENT = "GetDynNumContent";// 支付时获取动态码节点
    private static final String QRY_TXN_MSG_CONTENT = "QryTxnMsgContent";// 查询订单支付信息节点

    private static final String RESPONSE_CODE = "responseCode";// 响应码
    private static final String RESPONSE_TEXT_MESSAGE = "responseTextMessage";// 响应信息
    private static final String ERROR_CODE = "errorCode";// 错误码
    private static final String ERROR_MESSAGE = "errorMessage";// 错误信息

    private static final String RESPONSE_CODE_OK = "00";// 响应OK
    private static final String TXN_TYPE_PUR = "PUR";// 交易类型编码，PUR 消费交易
    private static final String TXN_TYPE_INP = "INP";// 交易类型编码，INP 分期消费交易
    private static final String TXN_TYPE_PRE = "PRE";// 交易类型编码，PRE 预授权交易
    private static final String TXN_TYPE_CFM = "CFM";// 交易类型编码，CFM 预授权完成交易
    private static final String TXN_TYPE_VTX = "VTX";// 交易类型编码，VTX 撤销交易
    private static final String TXN_TYPE_RFD = "RFD";// 交易类型编码，RFD 退货交易
    private static final String TXN_TYPE_CIV = "CIV";// 交易类型编码，CIV 卡信息验证交易
    private static final String VALID_FLAG_YES = "1";// 快钱是否支持，1：快钱支持 2：快钱不支持
    private static final String VALID_FLAG_NO = "2";// 快钱是否支持，1：快钱支持 2：快钱不支持


    /**
     * Description: 卡信息查询
     * @author: JiuDongDong
     * @param cardNo        卡号
     * @param txnType       交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param customerId    客户号
     * date: 2018/9/17 10:45
     */
    @Override
    public void getCardInfo(ResponseData responseData, String cardNo, String txnType, String customerId) {
        logger.info("It is now in Bill99QuickManagerImpl.getCardInfo, the parameters are: [cardNo = {}, txnType = {}," +
                        " customerId = {}]", cardNo, txnType, customerId);
        // 获取配置信息
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号
        String cardQueryUrl = bill99QuickPayConfigure.getCardQueryUrl();// 卡信息查询地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码
        HashMap respXml;
        try {
            respXml = getCardInfoFromBill99(cardQueryUrl, txnType, cardNo, merIdNotHat,
                    merchantCertPath, merchantCertPss);
        } catch (Exception e) {
            logger.error("Error occurred when query card info from bill99 for cardNo: " + cardNo, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of query card info from bill99 is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of get card info from bill99 is null, the cardNo = " + cardNo);
            responseData.setCode(ResponseStatus.CARDQUERYNULL.getValue());
            responseData.setMsg(Constants.CARDQUERYNULL);// 卡信息查询为空
            return;
        }
        // 卡信息查询是否正常
        String responseCode =
                StringUtils.isBlank((String) respXml.get(RESPONSE_CODE)) ? null : (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage =
                StringUtils.isBlank((String) respXml.get(RESPONSE_TEXT_MESSAGE)) ? null : (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode =
                StringUtils.isBlank((String) respXml.get(ERROR_CODE)) ? null : (String) respXml.get(ERROR_CODE);
        String errorMessage =
                StringUtils.isBlank((String) respXml.get(ERROR_MESSAGE)) ? null : (String) respXml.get(ERROR_MESSAGE);
        if (null != errorCode) {
            Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                    errorCode, errorMessage, cardNo);
            String code = responseData.getCode();
            if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
                return;
            }
            logger.error("The result of get card info from bill99 is error, errorCode = " + errorCode + ", errorMessage = " +
                    errorMessage + ", cardNo = " + cardNo);
            responseData.setCode(ResponseStatus.CARDQUERYERROR.getValue());
            responseData.setMsg(Constants.CARDQUERYERROR);// 卡信息查询失败
            return;
        }

        // 同一银行只能绑定一张同一类型的银行卡，判断当前同一银行是否已绑定一张同一类型的银行卡
        String bankId = (String) respXml.get(Constants.BILL99_Q_BANK_ID);// 银行id，如BOC
        String cardType = (String) respXml.get(Constants.BILL99_Q_CARD_TYPE);// 卡类型，0001 信用卡类型 0002 借记卡类型

        boolean ifAlready = ifTheSameBankAndCardTypeHasCardAlready(responseData, customerId, cardNo, bankId, cardType);
        if (ifAlready) {
            return;
        }

        //如果TR2获取的应答码responseCode的值为00时，成功，进行数据库的逻辑操作，比如更新数据库或插入记录。
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 卡信息查询
     * @author: JiuDongDong
     * @param cardQueryUrl  卡信息查询rul
     * @param cardNo        卡号
     * @param txnType       交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @return java.util.HashMap Map返回
     * date: 2018/9/17 10:53
     */
    private HashMap getCardInfoFromBill99(String cardQueryUrl, String txnType, String cardNo, String merIdNotHat,
                                          String merchantCertPath, String merchantCertPss) throws Exception {
        // 封装上送信息
        TransInfo transInfo = new TransInfo();
        // 设置解析节点
        transInfo.setRecordeText_1(QRY_CARD_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);
        transInfo.setRecordeText_3(CARD_INFO_CONTENT);
        transInfo.setRecordeText_4(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<QryCardContent>";
        str1Xml += "<txnType>" + txnType + "</txnType>";
        str1Xml += "<cardNo>" + cardNo + "</cardNo>";
        str1Xml += "</QryCardContent>";
        str1Xml += "</MasMessage>";
        logger.info("Card info get use tr1 xml: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml = Post.sendPost(cardQueryUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merIdNotHat);
        return respXml;
    }

    /**
     * Description: 卡信息验证---不使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param phoneNO           手机号码
     * date: 2018/9/14 17:06
     */
    @Override
    public void bindCardWithoutDynamicCode(ResponseData responseData, String customerId, String pan,
                                   String cardHolderName, String idType, String cardHolderId, String phoneNO) {
        logger.info("It is now in Bill99QuickManagerImpl.bindCardWithoutDynamicCode, the input parameter is: " +
                "[customerId = {}, pan = {}, cardHolderName = {}, idType = {}, cardHolderId = {}, phoneNO = {}]",
                customerId, pan, cardHolderName, idType, cardHolderId, phoneNO);
        // 校验手机号是否是注册手机号
        getPhnoeCheckCodeByUid(responseData, customerId);
        Object entity = responseData.getEntity();
        if (null == entity) {
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        String phone = (String) entity;
        if (!phoneNO.equals(phone)) {
            logger.error("The phone for bind card without dynamic code is error, customerId: " + customerId +
                    ", original phone is: " + phone, ", input phone is: " + phoneNO);
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }

        // 获取配置信息
        String merchantId = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号（商户编号）
        String terminalId = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String indAuthUrl = bill99QuickPayConfigure.getIndAuthUrl();// 绑卡前获取动态码
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码
        String externalRefNumber = "";//外部跟踪号
        if (StringUtils.isBlank(externalRefNumber)) {
            externalRefNumber = simpleDateFormat.format(new java.util.Date());
        }
        //   String expiredDate = request.getParameter("expiredDate");    //卡有效期
        //   String cvv2 = request.getParameter("cvv2");    //卡校验码

        HashMap respXml;
        try {
            respXml = getHashMapWithoutDynamicCode(customerId, pan, cardHolderName, idType, cardHolderId, phoneNO,
                    merchantId, terminalId, indAuthUrl, merchantCertPath, merchantCertPss, externalRefNumber);
        } catch (Exception e) {
            logger.error("Error occurred when get token before bind card for customerId: " + customerId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of bind card without dynamic code is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of bind card without dynamic code is null");
            responseData.setCode(ResponseStatus.BINDCARDWITHOUTCODERETURNNULL.getValue());
            responseData.setMsg(Constants.BINDCARDWITHOUTCODERETURNNULL);// 绑卡失败(响应为空)
            return;
        }
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        logger.info("The responseCode = {}, responseTextMessage = {}", responseCode, responseTextMessage);
        logger.info("The errorCode = {}, errorMessage = {}", errorCode, errorMessage);
        // 获取并打印错误信息
        Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, pan);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Error occurred when bind card without dynamic code, the error code: " + responseCode);
            responseData.setCode(ResponseStatus.BINDCARDWITHOUTCODEERROR.getValue());
            responseData.setMsg(Constants.BINDCARDWITHOUTCODEERROR);// 绑卡失败
            return;
        }
         /* 2.PCI绑卡成功后，本地绑卡 */
        //如果TR2获取的应答码responseCode的值为00时，成功，进行数据库的逻辑操作，比如更新数据库或插入记录。
        boolean ifBindCard2Merchant = bindCard2Merchant(responseData, customerId, pan, phoneNO, cardHolderName,
                idType, cardHolderId, "", "", respXml);
        if (!ifBindCard2Merchant) {
            logger.error("Error occurred when bind card to merchant, pan = " + pan + ", customerId = " + customerId);
            return;
        }
        responseData.setEntity(respXml);
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 卡信息验证---不使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param phoneNO           手机号码
     * @param merchantId        商户号
     * @param terminalId        终端号
     * @param indAuthUrl        获取验证码地址
     * @param merchantCertPath  证书名称
     * @param merchantCertPss   证书密码
     * @param externalRefNumber 外部检索参考号
     * @return java.util.HashMap 获取到的数据
     * date: 2018/9/14 17:34
     */
    private HashMap getHashMapWithoutDynamicCode(String customerId, String pan, String cardHolderName, String idType,
                                                 String cardHolderId, String phoneNO, String merchantId, String terminalId,
                                                 String indAuthUrl, String merchantCertPath, String merchantCertPss,
                                                 String externalRefNumber) throws Exception {
        //设置手机动态鉴权节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(IND_AUTH_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<indAuthContent>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<pan>" + pan + "</pan>";
        str1Xml += "<cardHolderName>" + cardHolderName + "</cardHolderName>";
        str1Xml += "<idType>" + idType + "</idType>";
        str1Xml += "<cardHolderId>" + cardHolderId + "</cardHolderId>";
        //  if(!"".equals(expiredDate) && !"".equals(cvv2)){
        //  str1Xml += "<expiredDate>" + expiredDate + "</expiredDate>";
        // str1Xml += "<cvv2>" + cvv2 + "</cvv2>";
        //  }
        str1Xml += "<phoneNO>" + phoneNO + "</phoneNO>";
        str1Xml += "</indAuthContent>";
        str1Xml += "</MasMessage>";
        logger.info("The param of get token for bind card is: {}", str1Xml);

        //TR2接收的数据
        return Post.sendPost(indAuthUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
    }

    /**
     * Description: 卡信息验证-获取动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param storablePan       缩略卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param expiredDate       有效期（贷记卡必传）
     * @param cvv2              卡校验码（贷记卡必传）
     * @param bindType          接入方式
     * @param phoneNO           手机号码
     * @return java.util.HashMap 获取到的数据
     * date: 2018/9/18 9:37
     */
    @Override
    public void getTokenBeforeBind(ResponseData responseData, String customerId, String pan, String storablePan,
                String cardHolderName, String idType, String cardHolderId, String expiredDate,
                String cvv2, String bindType, String phoneNO) {
        logger.info("It is now in Bill99QuickManagerImpl.getTokenBeforeBind, the input parameter is: " +
                        "[customerId = {}, pan = {}, storablePan = {}, cardHolderName = {}, idType = {}, cardHolderId = {}"
                        + ", expiredDate = {}, cvv2 = {}, bindType = {}, phoneNO = {}]",
                customerId, pan, storablePan, cardHolderName, idType, cardHolderId, expiredDate, cvv2, bindType, phoneNO);
        // 校验手机号是否是注册手机号
        getPhnoeCheckCodeByUid(responseData, customerId);
        Object entity = responseData.getEntity();
        if (null == entity) {
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        String phone = (String) entity;
        if (!phoneNO.equals(phone)) {
            logger.error("The phone of customerId: " + customerId + " is: " + phone, ", input phone is: " + phoneNO);
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        // 获取配置信息
        String merchantId = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号（商户编号）
        String terminalId = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String tokenUrl = bill99QuickPayConfigure.getTokenUrl();// 绑卡前获取动态码
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码
        String externalRefNumber = "";//外部跟踪号
        if (StringUtils.isBlank(externalRefNumber)) {
            externalRefNumber = simpleDateFormat.format(new java.util.Date());
        }

        // 放置解密支付密码的随机数
        Integer exitRandomNum = accountFlowRedisService.getRandomNum(Long.parseLong(customerId));
        Integer random = exitRandomNum == null ? RandomUtils.getRandomNum() : exitRandomNum;
        accountFlowRedisService.setRandomNum(Long.parseLong(customerId), random);

        // 从数据库查询该卡是否曾经绑定过，如果绑定过，确认是不是本人绑定，不是则阻断；如果该卡是本人绑定，且现在是有效状态，仍然阻断
        BankAccount bankAccount = bankAccountService.getBill99BankInfoByCardCode(pan);
        if (null != bankAccount) {
            logger.info("This card has been bound ever, now confirm this customer is or not original, pan = {}", pan);
            String userId = bankAccount.getUserId().toString();
            if (!customerId.equals(userId)) {
                logger.error("Not the customer " + userId + " himself is binding this card " + pan + ", current customer"
                        + " = " + customerId);
                responseData.setCode(ResponseStatus.NOTORIGINCUSTOMERBINDCARD.getValue());
                responseData.setMsg(Constants.NOTORIGINCUSTOMERBINDCARD);// 该卡已被其他人绑定
                return;
            }
            Short isAble = bankAccount.getIsAble();// 是否有效
            if (Constants.SHORT_ONE.shortValue() == isAble) {
                logger.error("This card " + pan + " has been bound before and now it is able also, you do not need to " +
                        "bind any more, current customer = " + customerId + ", original customer = " + userId);
                responseData.setCode(ResponseStatus.CARDHASBENNBOUND.getValue());
                responseData.setMsg(Constants.CARDHASBENNBOUND);// 该卡已绑定，无需重复绑卡
                return;
            }
        }

        // 发送请求
        HashMap respXml;
        try {
            respXml = getHashMapToken(customerId, pan, storablePan, cardHolderName, idType,
                    cardHolderId, expiredDate, cvv2, bindType, phoneNO,
                    merchantId, terminalId, tokenUrl, merchantCertPath,
                    merchantCertPss, externalRefNumber);
        } catch (Exception e) {
            logger.error("Error occurred when  get token before bind card for customerId: " + customerId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of get token before bind card is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of get token before bind card is null, cardNo = " + pan);
            responseData.setCode(ResponseStatus.GETTOKENBEFOREBINDCARDERROR.getValue());
            responseData.setMsg(Constants.GETTOKENBEFOREBINDCARDERROR);// 获取验证码失败
            return;
        }

        // 记录处理结果
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, pan);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        // 处理异常，返回
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Error occurred when get token before bind card, the error code: " +
                    responseCode + ", cardNo = " + pan);
            responseData.setCode(ResponseStatus.GETTOKENBEFOREBINDCARDERROR.getValue());
            responseData.setMsg(Constants.GETTOKENBEFOREBINDCARDERROR);// 首次绑卡获取验证码失败
            return;
        }
        //如果TR2获取的应答码responseCode的值为00时，成功，进行数据库的逻辑操作，比如更新数据库或插入记录。
        respXml.put(RANDOM, random);
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 同一银行只能绑定一张同一类型的银行卡，判断当前同一银行是否已绑定一张同一类型的银行卡
     * @author: JiuDongDong
     * @param customerId 客户号
     * @param pan  新卡号（全卡号）
     * @param newBankId  新卡号的银行id，如BOC
     * @param newCardType  新卡号的银行卡类型，0001 信用卡类型 0002 借记卡类型
     * @return boolean 当前同一银行是否已绑定一张同一类型的银行卡
     * date: 2018/10/18 16:03
     */
    private boolean ifTheSameBankAndCardTypeHasCardAlready(ResponseData responseData, String customerId, String pan,
                                                           String newBankId, String newCardType) {
        // 判断当前，客户是否曾经绑过同一个银行的同一种类型（借记卡、贷记卡）的卡，有的话，不允许
//        getCardInfo(responseData, pan, Constants.TXNTYPE_PUR, customerId);
//        logger.info("responseData = " + JsonUtil.toJson(responseData));
//        if (!ResponseStatus.OK.getValue().equals(responseData.getCode())) {
//            logger.error("Get card info from bill99 PCI error, card = " + pan + ", current customer = " + customerId);
//            responseData.setCode(ResponseStatus.ERR.getValue());
//            responseData.setMsg(ResponseStatus.ERR.name());
//            return true;
//        }
//        HashMap cardInfoMap = (HashMap) responseData.getEntity();

        // 遍历判断
        List<BankAccount> allAbleBanks = bankAccountService.getAllAbleBanksByUserId(Long.parseLong(customerId));
        for (BankAccount bank : allAbleBanks) {
            String oldCardCode = bank.getCardCode();// 卡号
            String oldBankLogo = bank.getBankLogo();// 银行id，如BOC
            String oldCardType = bank.getCardType();// 卡类型，0001 信用卡类型 0002 借记卡类型
            if (pan.equals(oldCardCode)) {
                logger.info("oldCardCode = {}, newCardCode = {}, continue!", oldCardCode, pan);
                continue;
            }
            if (newBankId.equals(oldBankLogo) && newCardType.equals(oldCardType)) {
                logger.error("The same bank only allow bind one piece the same type card, bankId = " + newBankId + ", " +
                        "cardType = " + newCardType + ", card = " + pan + ", current customer = " + customerId);
                responseData.setCode(ResponseStatus.SAMECARDTYPEONLYALLOWONE.getValue());
                responseData.setMsg(Constants.SAMECARDTYPEONLYALLOWONE);// 同一银行只能绑定一张同一类型的银行卡
                return true;
            }
        }
        return false;
    }

    /**
     * Description: 卡信息验证-使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param storablePan       缩略卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param expiredDate       有效期
     * @param cvv2              卡校验码
     * @param bindType          接入方式
     * @param phoneNO           手机号码
     * @param merchantId        商户号
     * @param terminalId        终端号
     * @param tokenUrl          获取验证码地址
     * @param merchantCertPath  证书名称
     * @param merchantCertPss   证书密码
     * @param externalRefNumber 外部检索参考号
     * @return java.util.HashMap 获取到的数据
     * date: 2018/9/17 17:34
     */
    private HashMap getHashMapToken(String customerId, String pan, String storablePan, String cardHolderName, String idType,
                                    String cardHolderId, String expiredDate, String cvv2, String bindType, String phoneNO,
                                    String merchantId, String terminalId, String tokenUrl, String merchantCertPath,
                                    String merchantCertPss, String externalRefNumber) throws Exception {
        //设置获取动态码节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(IND_AUTH_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<indAuthContent>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        if (StringUtils.isNotBlank(terminalId)) str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<pan>" + pan + "</pan>";
        if (StringUtils.isNotBlank(storablePan)) str1Xml += "<storablePan>" + storablePan + "</storablePan>";
        if (StringUtils.isNotBlank(cardHolderName)) str1Xml += "<cardHolderName>" + cardHolderName + "</cardHolderName>";
        if (StringUtils.isNotBlank(idType)) str1Xml += "<idType>" + idType + "</idType>";
        if (StringUtils.isNotBlank(cardHolderId)) str1Xml += "<cardHolderId>" + cardHolderId + "</cardHolderId>";
        if (StringUtils.isNotBlank(expiredDate)) str1Xml += "<expiredDate>" + expiredDate + "</expiredDate>";
        if (StringUtils.isNotBlank(cvv2)) str1Xml += "<cvv2>" + cvv2 + "</cvv2>";
        str1Xml += "<phoneNO>" + phoneNO + "</phoneNO>";
        if (StringUtils.isNotBlank(bindType)) str1Xml += "<bindType>" + bindType + "</bindType>";
        str1Xml += "</indAuthContent>";
        str1Xml += "</MasMessage>";

        logger.info("The tr1 param of getHashMapToken send to bill99 is: {}", str1Xml);

        //TR2接收的数据
        return Post.sendPost(tokenUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
    }

    /**
     * Description: 卡信息验证-使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param validCode         验证码
     * @param token             安全校验值
     * @param externalRefNumber 外部参考号
     * @param phoneNO           手机号码
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param expiredDate       有效期（贷记卡必传）
     * @param cvv2              卡校验码（贷记卡必传）
     * @return java.util.HashMap 获取到的数据
     * date: 2018/9/17 17:36
     */
    @Override
    public void bindCardWithDynamicCode(ResponseData responseData, String customerId, String pan, String validCode,
                                        String token, String externalRefNumber, String phoneNO, String cardHolderName,
                                        String idType, String cardHolderId, String expiredDate, String cvv2) {
        logger.info("It is now in Bill99QuickManagerImpl.bindCardWithDynamicCode, the input parameter is: [customerId = {}, "
                        + "pan = {}, validCode = {}, token = {}, externalRefNumber = {}, phoneNO = {}, cardHolderName = {}, "
                        + "idType = {}, cardHolderId = {}, expiredDate = {}, cvv2 = {}]", customerId, pan, validCode, token,
                externalRefNumber, phoneNO, cardHolderName, idType, cardHolderId, expiredDate, cvv2);
        // 校验手机号是否是注册手机号
        getPhnoeCheckCodeByUid(responseData, customerId);
        Object entity = responseData.getEntity();
        if (null == entity) {
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        String phone = (String) entity;
        if (!phoneNO.equals(phone)) {
            logger.error("The phone for bind card with dynamic code is error, customerId: " + customerId + ", original phone" +
                    " is: " + phone, ", input phone is: " + phoneNO);
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }

        /* 1. PCI绑卡*/
        // 获取配置信息
        String merchantId = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号（商户编号）
        String terminalId = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String indAuthVerifyUrl = bill99QuickPayConfigure.getIndAuthVerifyUrl();// 动态码绑卡
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        // 发送请求绑卡
        HashMap respXml;
        try {
            respXml = getHashMapWithDynamicCode(customerId, pan, validCode, token, phoneNO, merchantId,
                    terminalId, indAuthVerifyUrl, merchantCertPath, merchantCertPss, externalRefNumber,
                    cardHolderName, idType, cardHolderId);
        } catch (Exception e) {
            logger.error("Error occurred when  get token before bind card for customerId: " + customerId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of bind card with dynamic code is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of bind card with dynamic code is null, pan = " + pan);
            responseData.setCode(ResponseStatus.BINDCARDWITHCODERETURNNULL.getValue());
            responseData.setMsg(Constants.BINDCARDWITHCODERETURNNULL);// 绑卡失败
            return;
        }
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, pan);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode) || StringUtils.isNotBlank(errorCode)) {
            logger.error("Error occurred when bind card to bill99 for card = " + pan + ", responseCode = "
                    + responseCode + ", responseTextMessage = " + responseTextMessage + ", errorCode = " +
                    errorCode + ", errorMessage = " + errorMessage);
            responseData.setCode(ResponseStatus.BINDCARDWITHCODEERROR.getValue());
            responseData.setMsg(Constants.BINDCARDWITHCODEERROR);// 首次绑定银行卡失败
            return;
        }
        /* 2.PCI绑卡成功后，本地绑卡 */
        boolean ifBindCard2Merchant = bindCard2Merchant(responseData, customerId, pan, phoneNO,
                cardHolderName, idType, cardHolderId, expiredDate, cvv2, respXml);
        if (!ifBindCard2Merchant) {
            logger.error("Bind card failed because persistence failed, pan = " + pan + ", customerId = " + customerId);
            return;
        }
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: PCI绑卡成功后本地绑卡
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @param phoneNO           手机号码
     * @param respXml           快钱的绑卡响应信息
     * @return boolean          是否持久化到商户本地成功
     * date: 2018/9/25 15:37
     */
    private boolean bindCard2Merchant(ResponseData responseData, String customerId, String pan, String phoneNO,
                                      String cardHolderName, String idType, String cardHolderId, String expiredDate,
                                      String cvv2, HashMap respXml) {
        // 查询该卡在商户的绑卡信息
        BankAccount bill99BankInfo = bankAccountService.getBill99BankInfoByCardCode(pan);
        // 确认客户有没有其它的有效卡（如果有，那么一定有了一张默认卡）
        boolean setDefault;
        List<BankAccount> allAbleBanks = bankAccountService.getAllAbleBanksByUserId(Long.parseLong(customerId));
        if (CollectionUtils.isEmpty(allAbleBanks)) {
            setDefault = true;
        } else {
            setDefault = false;
        }
        if (null != bill99BankInfo) {
            // 说明该卡曾经绑定过，如果该卡状态为失效，则将银行卡置为有效，如果该卡状态为有效则更新部分信息
            logger.info("This card has been bind before, now re bind it, pan = {}", pan);
            Short isAble = bill99BankInfo.getIsAble();// 是否有效
            if (Constants.SHORT_ZERO == isAble) {
                logger.info("This card is not able, now re able it, cardNo = {}", pan);
                BankAccount bankAccount = new BankAccount();
                bankAccount.setCardCode(pan);
                bankAccount.setIsDef(setDefault ? Constants.SHORT_ONE : Constants.SHORT_ZERO);
                bankAccount.setIsAble(Constants.SHORT_ONE);
                bankAccount.setMobilePhone(phoneNO);
                bankAccount.setPhoneChangedExpired(Constants.SHORT_ZERO);
                bankAccountService.updateBothStatus(bankAccount, null);
            }
            if (Constants.SHORT_ONE == isAble) {
                BankAccount bankAccount = new BankAccount();
                bankAccount.setMobilePhone(phoneNO);
                bankAccount.setPhoneChangedExpired(Constants.SHORT_ZERO);
                bankAccountService.updateBothStatus(bankAccount, null);
                logger.info("This card is able, do nothing for it, cardNo = {}", pan);
            }
            responseData.setEntity(respXml);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return true;
        }
        if (null == bill99BankInfo) {
            // 说明该卡从未绑定过，新插入数据库
            logger.info("This card never been bind, now bind it, pan = {}", pan);
            bill99BankInfo = new BankAccount();
            bill99BankInfo.setUserId(Long.valueOf(customerId));// 用户ID
            bill99BankInfo.setCardCode(pan);// 银行卡号
            bill99BankInfo.setMobilePhone(phoneNO); // 手机号
            bill99BankInfo.setIsAble(Constants.SHORT_ONE);// 启用
            bill99BankInfo.setIsKuaiQian(Constants.SHORT_ONE);// 是快钱渠道
            bill99BankInfo.setExpiredDate(expiredDate);// 卡效期
            bill99BankInfo.setCvv(null);// 校验值置空，不保存
            // 是否默认，如果该客户从来没有绑过任何银行的卡，则该卡置为默认，否则非默认
            // 查询该客户是否在快钱已绑过银行卡（有效的）
            bill99BankInfo.setIsDef(setDefault ? Constants.SHORT_ONE : Constants.SHORT_ZERO);// 是否默认
            bill99BankInfo.setUserCardType(Short.valueOf(idType));//0 身份证类型、1 护照类型、2 军官证、3 士兵证、4 港澳台通行证、5 临时身份证、6 户口本、7 其他类型证件、9 警官证、12 外国人居留证、15 回乡证、16 企业营业执照、17 法人代码证、18 台胞证
            bill99BankInfo.setBankAccName(cardHolderName);// 银行账户名
            bill99BankInfo.setUserCardCode(cardHolderId);// 证件号码
            // 卡信息查询
            ResponseData cardInfoResponseData = new ResponseData();
            getCardInfo(cardInfoResponseData, pan, TXN_TYPE_PUR, customerId);// 交易类型编码，PUR 消费交易
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with PUR response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_INP, customerId);// 交易类型编码，INP 分期消费交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with INP response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_PRE, customerId);// 交易类型编码，PRE 预授权交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with PRE response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_CFM, customerId);// 交易类型编码，CFM 预授权完成交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with CFM response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_VTX, customerId);// 交易类型编码，VTX 撤销交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with VTX response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_RFD, customerId);// 交易类型编码，RFD 退货交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.info("Query card info from PCI with RFD response null, check it, pan = {}", pan);
                getCardInfo(cardInfoResponseData, pan, TXN_TYPE_CIV, customerId);// 交易类型编码，CIV 卡信息验证交易
            }
            if (null == cardInfoResponseData.getEntity()) {
                logger.error("Query card info from PCI with all txnType response null, check it, pan = " + pan);
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(ResponseStatus.ERR.name());
                return true;
            }
            // 获取卡信息
            HashMap<String, String> hashMap = (HashMap<String, String>) cardInfoResponseData.getEntity();
            String cardType = hashMap.get(Constants.BILL99_Q_CARD_TYPE);// 银行卡类型: 0001 信用卡类型 0002 借记卡类型
            bill99BankInfo.setCardType(cardType);
            String issuer = hashMap.get(Constants.BILL99_Q_ISSUER);// 银行名称
            bill99BankInfo.setBankName(issuer);
            String bankId = hashMap.get(Constants.BILL99_Q_BANK_ID);// 银行代码
            bill99BankInfo.setBankLogo(bankId);
            String validFlag = hashMap.get(Constants.BILL99_Q_VALID_FLAG);// 快钱是否支持
            if (!VALID_FLAG_YES.equals(validFlag)) {
                logger.error("Bill99 do not support this kind of card, check it, pan = " + pan);
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg(ResponseStatus.ERR.name());
                return false;
            }
            // payToken
            String payToken = (String) respXml.get(Constants.BILL99_Q_PAY_TOKEN);// 签约协议号
            bill99BankInfo.setPayToken(payToken);
            // 插入表
            bankAccountService.insertBankAccount(bill99BankInfo);
        }
        return true;
    }


    /**
     * Description: 卡信息验证-使用动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param pan               卡号
     * @param validCode         验证码
     * @param token             安全校验值
     * @param phoneNO           手机号码
     * @param merchantId        商户号
     * @param terminalId        终端号
     * @param indAuthVerifyUrl  动态码绑卡地址
     * @param merchantCertPath  证书名称
     * @param merchantCertPss   证书密码
     * @param externalRefNumber 外部检索参考号
     * @param cardHolderName    持卡人户名
     * @param idType            证件类型
     * @param cardHolderId      证件号码
     * @return java.util.HashMap 获取到的数据
     * date: 2018/9/17 17:34
     */
    private HashMap getHashMapWithDynamicCode(String customerId, String pan, String validCode, String token,
                  String phoneNO, String merchantId, String terminalId, String indAuthVerifyUrl,
                  String merchantCertPath,String merchantCertPss, String externalRefNumber, String cardHolderName,
                  String idType, String cardHolderId) throws Exception {
        //设置手机动态鉴权节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(IND_AUTH_DYN_VERIFY_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<indAuthDynVerifyContent>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        if (StringUtils.isNotBlank(terminalId)) {
            str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        }
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<pan>" + pan + "</pan>";
        // 这3行一定不要
        // str1Xml += "<cardHolderName>" + cardHolderName + "</cardHolderName>";
        // str1Xml += "<idType>" + idType + "</idType>";
        // str1Xml += "<cardHolderId>" + cardHolderId + "</cardHolderId>";
        str1Xml += "<validCode>" + validCode + "</validCode>";
        str1Xml += "<token>" + token + "</token>";
        str1Xml += "<phoneNO>" + phoneNO + "</phoneNO>";
        str1Xml += "</indAuthDynVerifyContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of getHashMapWithDynamicCode send to bill99 is: {}", str1Xml);

        //TR2接收的数据
        return Post.sendPost(indAuthVerifyUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
    }



    /**
     * Description: PCI查询卡信息
     * @author: JiuDongDong
     * @param customerId    客户号
     * @param cardType      卡类型，非必填: 0001 信用卡类型 0002 借记卡类型
     * @param storablePan   缩略卡号
     * @param bankId        银行代码
     * date: 2018/9/14 9:50
     */
    @Override
    public void getPciCardInfo(ResponseData responseData, String customerId, String cardType, String storablePan,
                               String bankId) {
        logger.info("It is now in Bill99QuickManagerImpl.getPciCardInfo, the parameters are: [customerId = {}, " +
                "cardType = {}, storablePan = {}, bankId = {}]", customerId, cardType, storablePan, bankId);
        // 获取配置信息
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号
        String pciQueryUrl = bill99QuickPayConfigure.getPciQueryUrl();// PCI查询地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        // 发送查询请求
        HashMap respXml;
        try {
            respXml = getPciCardInfoFromBill99(customerId, cardType, storablePan, bankId,
                    merIdNotHat, pciQueryUrl, merchantCertPath, merchantCertPss);
        } catch (Exception e) {
            logger.error("Error occurred when query card info from bill99 PCI for customerId: " + customerId, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of query card info from bill99 PCI is respXml: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of query card info from bill99 is null, storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.PCIQUERYNULL.getValue());
            responseData.setMsg(Constants.PCIQUERYNULL);// 绑卡信息为空
            return;
        }
        // 响应信息
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, storablePan);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("The result of query card info from bill99 is null, the responseCode = " +
                    responseCode + ", responseTextMessage = " + responseTextMessage + ", errorCode = "
                    + errorCode + ", errorMessage = " + errorMessage + ", storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.PCIQUERYERROR.getValue());
            responseData.setMsg(Constants.PCIQUERYERROR);// 查询卡信息发生异常
            return;
        }
        // 如果TR2获取的应答码responseCode的值为00时，成功，进行数据库的逻辑操作，比如更新数据库或插入记录。
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: PCI查询卡信息
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param cardType          卡类型
     * @param storablePan       卡缩略号
     * @param bankId            银行卡代码
     * @param merIdNotHat       自营商户号
     * @param pciQueryUrl       PCI查询rul
     * @param merchantCertPath  商户密钥名称
     * @param merchantCertPss   密钥密码
     * @return java.util.HashMap Map返回
     * date: 2018/9/14 15:08
     */
    private HashMap getPciCardInfoFromBill99(String customerId, String cardType, String storablePan,
                                             String bankId, String merIdNotHat, String pciQueryUrl,
                                             String merchantCertPath, String merchantCertPss) throws Exception {
        // 封装上送信息
        TransInfo transInfo= new TransInfo();
        // 设置节点
        transInfo.setRecordeText_1(PCI_QUERY_CONTENT);
//        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);
        transInfo.setRecordeText_2(PCI_INFOS);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<PciQueryContent>";
        str1Xml += "<merchantId>" + merIdNotHat + "</merchantId>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        if (StringUtils.isNotBlank(cardType)) str1Xml += "<cardType>" + cardType + "</cardType>";
        if (StringUtils.isNotBlank(storablePan)) str1Xml += "<storablePan>" + storablePan + "</storablePan>";
        if (StringUtils.isNotBlank(bankId)) str1Xml += "<bankId>" + bankId + "</bankId>";
        str1Xml += "</PciQueryContent>";
        str1Xml += "</MasMessage>";
        logger.info("Pci card info get use tr1 xml: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml = Post.sendPost(pciQueryUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merIdNotHat);
        return respXml;
    }

    /**
     * Description: PCI数据删除（解绑接口）
     * @author: JiuDongDong
     * @param storablePan       缩略卡号
     * @param bankId            银行代码
     * @param customerId        客户号
     * @param pan               卡号
     * date: 2018/9/18 19:03
     */
    @Override
    public void pciDeleteCardInfo(ResponseData responseData, String customerId, String pan, String storablePan,
                                  String bankId, String validCode) {
        logger.info("It is now in Bill99QuickManagerImpl.pciDeleteCardInfo, the parameters are: [customerId = {}, pan " +
                "= {}, storablePan = {}, bankId = {}, validCode = {}]", customerId, pan, storablePan, bankId, validCode);
        // 卡号去* (10位不含*）
        storablePan = BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan);
        logger.info("storablePan = {}", storablePan);

        // 校验验证码是否过期
        String validCodeFromRedis = sendMessageRedisService.getValidCodeFromRedis(storablePan);
        if (StringUtils.isBlank(validCodeFromRedis)) {
            logger.error("The valid code is expired of storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.VALIDCODEEXPIRED.getValue());
            responseData.setMsg(Constants.VALIDCODEEXPIRED);// 验证码过期
            return;
        }
        // 校验验证码是否正确
        if (!validCode.equals(validCodeFromRedis)) {
            logger.error("The valid code is incorrect, validCodeFromRedis = " + validCodeFromRedis + ", this validCode" +
                    " = " + validCode);
            responseData.setCode(ResponseStatus.VALIDCODEERROR.getValue());
            responseData.setMsg(Constants.VALIDCODEERROR);// 验证码错误
            return;
        }

        // 获取配置信息
        String merchantId = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号
        String pciDeleteUrl = bill99QuickPayConfigure.getPciDeleteUrl();// PCI解绑地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        // 设置解析节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(PCI_DELETE_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        // 获取全卡号
        BankAccount bankAccount = getAllAbleBanksByUserId(customerId, storablePan);
        if (null == bankAccount) {
            logger.error("Can not find this storablePan from merchant database, customerId = " + customerId +
                    ", storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        pan = bankAccount.getCardCode();// 全卡号
        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<PciDeleteContent>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<pan>" + pan + "</pan>";
        str1Xml += "<storablePan>" + storablePan + "</storablePan>";
        str1Xml += "<bankId>" + bankId + "</bankId>";
        str1Xml += "</PciDeleteContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of pciDeleteCardInfo send to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(pciDeleteUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when unbind card info from bill99 for storablePan = " + storablePan, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of delete card info from bill99 PCI is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of unbind card info from bill99 is null for storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.PCIDELETENULL.getValue());
            responseData.setMsg(ResponseStatus.PCIDELETENULL.name());// 解绑响应失败
            return;
        }
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickBindCardLogUtil.logBindCardInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, storablePan);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            // 即使快钱发生错误了，本地也要解绑
            ResponseData data = unbindFromMerchantCard(pan);
            if (null != data) {
                responseData.setCode(data.getCode());
                responseData.setMsg(data.getMsg());
                return;
            }
            responseData.setCode(ResponseStatus.PCIDELETERROR.getValue());
            responseData.setMsg(Constants.PCIDELETERROR);// 解绑失败请重试
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("The result of unbind card info from bill99 occurred error, the responseCode code: " +
                    responseCode);
            responseData.setCode(ResponseStatus.PCIDELETERROR.getValue());
            responseData.setMsg(Constants.PCIDELETERROR);// 解绑失败请重试
            // 即使快钱发生错误了，本地也要解绑
            unbindFromMerchantCard(pan);
            return;
        }
        logger.info("PCI delete ok for storablePan = {}", storablePan);

        /* 商户数据库卡解绑 */
        ResponseData data = unbindFromMerchantCard(pan);
        if (null != data) {
            responseData.setCode(data.getCode());
            responseData.setMsg(data.getMsg());
            return;
        } else {
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        }

    }

    /**
     * Description: 商户解绑用户银行卡
     * @author: JiuDongDong
     * @param pan  全卡号
     * @return  responseData  发生错误时返回该值
     * date: 2018/10/15 15:48
     */
    private ResponseData unbindFromMerchantCard(String pan) {
        ResponseData responseData = new ResponseData();
        // 获取商户保存的卡信息
        BankAccount bill99BankInfo = bankAccountService.getBill99BankInfoByCardCode(pan);
        if (null == bill99BankInfo) {
            logger.error("Merchant has no this bank info, cardNo = " + pan);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return responseData;
        }
        Short isDef = bill99BankInfo.getIsDef();// 是否默认
        // 如果该卡为非默认，则直接置为失效即可
        if (Constants.SHORT_ZERO == isDef) {
            bankAccountService.updateIsAbleByCardCode(Constants.SHORT_ZERO, pan);
            return null;
        }
        // 如果该卡为默认，则需：
        // 1、置为失效，置为非默认（防止重新绑定时，导致有2张默认银行卡）
        // 2、如果还有绑定的其他银行卡，则选取一张置为默认
        if (Constants.SHORT_ONE == isDef) {
            // 获取该用户绑定的其他有效的银行卡
            List<BankAccount> allAbleBanks = bankAccountService.getAllAbleBanksByUserId(bill99BankInfo.getUserId());
            BankAccount bankAccountAnother;// 用户绑定的另外一张有效银行卡，用于稍后设置为默认银行卡
            // 如果没有其它绑定的有效银行卡，则bankAccountAnother仍置空，否则设为默认
            if (allAbleBanks.size() == Constants.INTEGER_ONE) {
                bankAccountAnother = null;
            } else {
                // 取另外的其中一张置为默认
                bankAccountAnother = new BankAccount();
                String anotherCardCode = "";
                for (BankAccount allAbleBank : allAbleBanks) {
                    String cardCode = allAbleBank.getCardCode();
                    if (!pan.equals(cardCode)) {
                        anotherCardCode = cardCode;
                        break;
                    }
                }
                bankAccountAnother.setCardCode(anotherCardCode);
                bankAccountAnother.setIsDef(Constants.SHORT_ONE);
                bankAccountAnother.setIsAble(Constants.SHORT_ONE);
            }
            // 将该张卡置为失效
            BankAccount bankAccountThis = new BankAccount();
            bankAccountThis.setCardCode(pan);
            bankAccountThis.setIsDef(Constants.SHORT_ZERO);
            bankAccountThis.setIsAble(Constants.SHORT_ZERO);
            bankAccountService.updateBothStatus(bankAccountThis, bankAccountAnother);
        }
        return null;
    }

    /**
     * Description: 获取银行代码
     * @author: JiuDongDong
     * @param channelCode  渠道名称：1快钱个人网银借记卡  2快钱个人网银贷记卡  3快钱快捷个人借记卡  4快钱快捷个人贷记卡  5快钱企业网银  6快钱快捷企业网银
     * @param cardType				卡类型，0001 信用卡类型 0002 借记卡类型，不传表示所有类型
     * date: 2018/9/20 10:43
     */
    @Override
    public void getPayChannelByChannelName(ResponseData responseData, String channelCode, String cardType) {
        logger.info("It is now in Bill99QuickManagerImpl.getPayChannelByChannelName, the parameter are: " +
                "[channelCode = {}, cardType = {}]", channelCode, cardType);
        // 枚举转换
        String channelName = "";// 渠道名称
        if (Constants.STR_ONE.equals(channelCode)) channelName = Constants.BILL99_Q_PERSONAL_PC;
        if (Constants.STR_TWO.equals(channelCode)) channelName = Constants.BILL99_Q_COM_PC;
        if (Constants.STR_THREE.equals(channelCode)) channelName = Constants.BILL99_Q_PERSONAL_QUICK;
        if (StringUtils.isBlank(channelName)) {
            logger.error("The parameter channelName is not correct for Bill99QuickManagerImpl." +
                    "getPayChannelByChannelName, channelCode = ", channelCode);
            responseData.setCode(ResponseStatus.PARAMERR.getValue());
            responseData.setMsg(Constants.PARAMERR);// 参数错误
            return;
        }
        // 卡类型设置
        Short isBorrow, isLoan;// 借记卡、贷记卡
        if (StringUtils.isBlank(cardType)) {
            isBorrow = null;
            isLoan = null;
        } else if (Constants.BILL99_Q_CARD_LOAN.equals(cardType)) {
            isBorrow = Constants.SHORT_ZERO;
            isLoan = Constants.SHORT_ONE;
        } else {
            isBorrow = Constants.SHORT_ONE;
            isLoan = Constants.SHORT_ZERO;
        }
        // 查询银行
        List<PayChannel> payChannelByChannelName =
                payChannelService.getPayChannelByChannelName(channelName, isBorrow, isLoan);
        responseData.setEntity(payChannelByChannelName);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 设置快钱默认银行卡
     * @author: JiuDongDong
     * @param customerId    客户号
     * @param storablePan	含*全卡号，需转换为缩略卡号（10位不含*）
     * date: 2018/9/22 16:55
     */
    @Override
    public void setDefaultCard(ResponseData responseData, String customerId, String storablePan) {
        logger.info("It is now in Bill99QuickManagerImpl.getPayChannelByChannelName, the parameter are: " +
                "[customerId = {}, storablePan = {}]", customerId, storablePan);
        // 含*全卡号，转换为缩略卡号（10位不含*）
        storablePan = BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan);
        // 获取全卡号
        String cardCode;
        // 获取全卡号
        BankAccount bankAccount = getAllAbleBanksByUserId(customerId, storablePan);
        if (null == bankAccount) {
            logger.error("Can not find this cardCode from merchant database, customerId = " + customerId +
                    ", storablePan = " + storablePan);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        cardCode = bankAccount.getCardCode();// 全卡号
        // 查询当前该客户的默认银行卡
        List<BankAccount> defaultBankByUserId = bankAccountService.getDefaultBankByUserId(Long.valueOf(customerId));
        // 如果该客户名下有多张快钱默认银行卡，阻断
        if (null != defaultBankByUserId && defaultBankByUserId.size() > Constants.INTEGER_ONE) {
            logger.error("The user has more than one default card, customerId = " + customerId + ", default card " +
                    "count = " + defaultBankByUserId.size());
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 如果该客户已经将该唯一一张默认银行卡删除，阻断
        if (CollectionUtils.isEmpty(defaultBankByUserId)) {
            logger.error("The user has set this card as not default card, customerId = " + customerId + ", cardCode = "
                    + cardCode);
            responseData.setCode(ResponseStatus.CARDISNOTDEFAULTANYMORE.getValue());
            responseData.setMsg(Constants.CARDISNOTDEFAULTANYMORE);// 该卡已被解绑，不能设为默认银行卡
            return;
        }
        // 设置快钱默认银行卡：将原默认银行卡解除，将新卡设为默认
        BankAccount oldBankAccount = defaultBankByUserId.get(Constants.INTEGER_ZERO);// 原快钱默认银行卡
        oldBankAccount.setIsDef(Constants.SHORT_ZERO);// 设置为非默认
        BankAccount newBankAccount = new BankAccount();// 新的默认银行卡
        newBankAccount.setIsDef(Constants.SHORT_ONE);
        newBankAccount.setCardCode(cardCode);
        List<BankAccount> bankAccountList = new ArrayList<>();
        bankAccountList.add(oldBankAccount);
        bankAccountList.add(newBankAccount);
        bankAccountService.updateIsDefaultByCardCodes(bankAccountList);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 查询用户在快钱绑定的的所有有效的快捷银行卡
     * @author: JiuDongDong
     * @param customerId  用户id
     * date: 2018/9/22 15:59
     */
    @Override
    public void getAllAbleBanksByUserId(ResponseData responseData, String customerId) {
        logger.info("It is now in Bill99QuickManagerImpl.getAllAbleBanksByUserId, the parameter is: " +
                "[customerId = {}]", customerId);
        List<BankAccount> allAbleBanksByUserId = bankAccountService.getAllAbleBanksByUserId(Long.valueOf(customerId));
        // 卡号脱敏、手机号置空、身份证置空
        for (BankAccount bankAccount : allAbleBanksByUserId) {
            String cardCode = bankAccount.getCardCode();
            cardCode = BankCardSwitchUtil.cipherOriginCardCode(cardCode);
            bankAccount.setCardCode(cardCode);
            bankAccount.setMobilePhone("");
            bankAccount.setUserCardCode("");
        }
        responseData.setEntity(allAbleBanksByUserId);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 一键支付（普通版）
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param payToken          签约协议号
     * @param amount            交易金额,以元为单位，小数点后最多两位
     * @param spFlag            特殊交易标志（默认QPay02）
     * @param interactiveStatus 消息状态（默认TR1）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param validCode         手机验证码
     * @param bindCardFlag      绑卡标志，值为0或者1。1表示当前一键支付流程为：绑卡 + 一键支付（此时使用银行发送的验证码）。0表示使用已绑定的银行卡支付（此时使用易网聚鲜发送的验证码）
     * date: 2018/9/23 13:49
     */
    @Override
    public void quickPayCommon(ResponseData responseData, String customerId, String payToken, String amount,
                               String spFlag, String interactiveStatus, String txnType, String externalRefNumber,
                               String validCode, String bindCardFlag) throws ShouldPayNotEqualsException {
        logger.info("It is now in Bill99QuickManagerImpl.quickPayCommon, the input parameters are: " +
                        "[customerId = {}, payToken = {}, amount = {}, spFlag = {}, interactiveStatus = {}, " +
                        "txnType = {}, externalRefNumber = {}, validCode = {}, bindCardFlag = {}]", customerId,
                payToken, amount, spFlag, interactiveStatus, txnType, externalRefNumber, validCode, bindCardFlag);
        // 如果是第一次绑卡并支付，不涉及到验证码，如果使用已绑定的银行卡支付，则需校验过期
        if (Constants.STR_ONE.equals(bindCardFlag)) {
            // 这是第一次绑卡并支付，不涉及到验证码，不做处理
            logger.info("This is bind card and pay combination, customerId = {}, externalRefNumber = {}, " +
                            "amount = {}", customerId, externalRefNumber, amount);
        }
        if (Constants.STR_ZERO.equals(bindCardFlag)) {
            // 使用已绑定的银行卡支付（此时使用易网聚鲜发送的验证码），校验验证码是否正确、是否过期
            logger.info("This is single already bound card pay, customerId = {}, externalRefNumber = {}, " +
                    "amount = {}", customerId, externalRefNumber, amount);
            String validCodeFromRedis = sendMessageRedisService.getValidCodeFromRedis(externalRefNumber);
            if (StringUtils.isBlank(validCodeFromRedis)) {
                logger.error("The valid code is expired of orderNo = " + externalRefNumber);
                responseData.setCode(ResponseStatus.VALIDCODEEXPIRED.getValue());
                responseData.setMsg(Constants.VALIDCODEEXPIRED);// 验证码过期
                return;
            }
            // 校验验证码是否正确
            if (!validCode.equals(validCodeFromRedis)) {
                logger.error("The valid code is incorrect, validCodeFromRedis = " + validCodeFromRedis + ", " +
                        "this validCode = " + validCode);
                responseData.setCode(ResponseStatus.VALIDCODEERROR.getValue());
                responseData.setMsg(Constants.VALIDCODEERROR);// 验证码错误
                return;
            }
        }

        // 校验订单支付金额是否正确
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(externalRefNumber);
        String shouldPayMoney = redisParam.get(Constants.SURPLUS);
        if (new BigDecimal(amount).compareTo(new BigDecimal(shouldPayMoney)) != Constants.INTEGER_ZERO) {
            logger.error("Web should pay money not equals redis, shouldPayMoney from redis = " + shouldPayMoney +
                            ", web amount = " + amount);
            throw new ShouldPayNotEqualsException("Web should pay money not equals redis, shouldPayMoney " +
                    "from redis = " + amount + ", web amount = " + shouldPayMoney);
        }

        // 获取卡信息
        BankAccount bill99BankInfoByPayToken = bankAccountService.getBill99BankInfoByPayToken(payToken);
        if (null == bill99BankInfoByPayToken) {
            logger.error("Can not find this payToken from merchant database, customerId = " + customerId + ", " +
                    "payToken = " + payToken);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        // 校验手机号是否是注册手机号
        String mobilePhone = bill99BankInfoByPayToken.getMobilePhone();// 绑卡时的手机号
        getPhnoeCheckCodeByUid(responseData, customerId);
        Object entity = responseData.getEntity();
        if (null == entity) {
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        String phone = (String) entity;
        if (!mobilePhone.equals(phone)) {
            logger.error("The phone in user center has been changed, this card can not been used to pay common any " +
                    "more, the customerId is: " + customerId + ", original phone is: " + mobilePhone, ", newest " +
                    "user center phone is: " + phone);
            responseData.setCode(ResponseStatus.PHONECHANGEDOLDCARDCANNOTPAY.getValue());
            responseData.setMsg(Constants.PHONECHANGEDOLDCARDCANNOTPAY);// 该银行卡绑定的手机号与平台注册手机号不符，交易有风险，请解绑或联系发卡银行更换手机号
            return;
        }

        // 将该次支付使用的银行卡设置为默认银行卡
        String cardCode = bill99BankInfoByPayToken.getCardCode();// 全卡号
        String storablePan = BankCardSwitchUtil.originToShort(cardCode);// 缩略卡号（10位不含*）
        setDefaultCard(new ResponseData(), customerId, storablePan);// 将该次支付使用的银行卡设置为默认银行卡

        // 获取配置信息
        String merchantId;// 商户号
        String merId = bill99QuickPayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 商户号(自营)
        String terminalId;// 终端号
        String terminalId1 = bill99QuickPayConfigure.getTerminalId1();// 存管终端号
        String terminalIdNotHat1 = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String isSelfPro;// 是否自营商品，0否1是

        /* 1.1 根据自营和非自营选择商户号 */
        String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        if ((Constants.STR_ZERO).equals(shopId)
                || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
            merchantId = merIdNotHat;
            terminalId = terminalIdNotHat1;
            isSelfPro = Constants.STR_ONE;
        } else {
            merchantId = merId;
            terminalId = terminalId1;
            isSelfPro = Constants.STR_ZERO;
        }

        String quickPayCommonUrl = bill99QuickPayConfigure.getQuickPayCommonUrl();// 99bill一键快捷支付（普通版）请求地址
        String quickPayCommonTr3Url = bill99QuickPayConfigure.getQuickPayCommonTr3Url();// 99bill一键快捷支付（普通版）tr3回调地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        //消费信息
        String entryTime = simpleDateFormat.format(new Date());//商户端交易时间

        //设置消费交易的两个节点
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";

        str1Xml += "<TxnMsgContent>";
        str1Xml += "<interactiveStatus>" + interactiveStatus + "</interactiveStatus>";
        str1Xml += "<spFlag>" + spFlag + "</spFlag>";
        str1Xml += "<txnType>" + txnType + "</txnType>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<entryTime>" + entryTime + "</entryTime>";
        str1Xml += "<amount>" + amount + "</amount>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<payToken>" + payToken + "</payToken>";
        // str1Xml += "<tr3Url>" + quickPayCommonTr3Url + "</tr3Url>";// TR3功能弃用

        str1Xml += "<extMap>";
        str1Xml += "<extDate><key>phone</key><value></value></extDate>";
        str1Xml += "<extDate><key>validCode</key><value></value></extDate>";
        str1Xml += "<extDate><key>savePciFlag</key><value>0</value></extDate>";
        str1Xml += "<extDate><key>token</key><value></value></extDate>";
        str1Xml += "<extDate><key>payBatch</key><value>2</value></extDate>";
        str1Xml += "</extMap>";

        str1Xml += "</TxnMsgContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of quickPayCommon send to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(quickPayCommonUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss,
                    merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when quick pay without valid code to bill99 for externalRefNumber = "
                    + externalRefNumber, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of quick pay without valid code to bill99 is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of quick pay without valid code to bill99 is null for externalRefNumber = "
                    + externalRefNumber);
            responseData.setCode(ResponseStatus.QUICKPAYCOMMONRESNULL.getValue());
            responseData.setMsg(Constants.QUICKPAYCOMMONRESNULL);// 一键支付响应为空
            return;
        }
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);

        // 订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起
        String txnStatus = (String) respXml.get(Constants.BILL99_Q_TXN_STATUS);
        logger.info("TxnStatus = {}, externalRefNumber = {}", txnStatus, externalRefNumber);
        if (Constants.BILL99_Q_TXN_STATUS_PAY_F.equals(txnStatus)) {
            // 其实支付时，并不会响应txnStatus这个字段，只要responseCode=00即表示支付成功。在订单支付查询时，responseCode=00只表示
            // 响应成功，还需要看txnStatus的实际值去判断支付状态或退款状态
            logger.error("Error occurred when common pay to bill99, externalRefNumber = " + externalRefNumber +
                    ", the responseCode = " + responseCode + ", txnStatus = " + txnStatus);
            responseData.setCode(ResponseStatus.DYNAMICPAYBILL99ERROR.getValue());
            responseData.setMsg(Constants.DYNAMICPAYBILL99ERROR);// 银行系统错误，请重试
            return;
        }
        if (BILL99_Q_RESPONSE_CODE_C0.equals(responseCode) || BILL99_Q_RESPONSE_CODE_68.equals(responseCode)
                || BILL99_Q_TXN_STATUS_PAY_P.equals(txnStatus)) {
            logger.error("Bill99 or bank is still handling this common pay, externalRefNumber = " + externalRefNumber +
                    ", responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage + ", txnStatus = "
                    + txnStatus);
            // C0: 快钱内部处理中（未完成）、最终交易结果未知，将支付信息放入Redis
            // 68: 银行内部处理中（未完成）、最终交易结果未知，将支付信息放入Redis
            // P:  交易挂起，将支付信息放入Redis
            OrderInfoVo orderInfoVo = new OrderInfoVo(respXml);
            orderInfoVo.setIsSelfPro(isSelfPro);// 设置是否自营商品
            sendMessageRedisService.putTradeInfo2Redis(orderInfoVo, null);
            if (BILL99_Q_RESPONSE_CODE_C0.equals(responseCode)) {
                responseData.setCode(ResponseStatus.KUAIQIANPAYHANDLING.getValue());// 快钱正在处理中
                responseData.setMsg(Constants.KUAIQIANPAYHANDLING);// 快钱正在处理中
            }
            if (BILL99_Q_RESPONSE_CODE_68.equals(responseCode)) {
                responseData.setCode(ResponseStatus.BANKPAYHANDLING.getValue());// 银行正在处理中
                responseData.setMsg(Constants.BANKPAYHANDLING);// 银行正在处理中
            }
            if (BILL99_Q_TXN_STATUS_PAY_P.equals(responseCode)) {
                responseData.setCode(ResponseStatus.BILL99HANGUP.getValue());// 快钱将交易挂起
                responseData.setMsg(Constants.BILL99HANGUP);// 快钱将交易挂起
            }
            return;
        }
        // 获取并打印错误信息
        Bill99QuickTradeLogUtil.logTradeInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, externalRefNumber);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Error occurred when quick pay without valid code to bill99, externalRefNumber " +
                    "= " + externalRefNumber + ",  the responseCode = " + responseCode);
            responseData.setCode(ResponseStatus.QUICKPAYCOMMONBILL99ERROR.getValue());
            responseData.setMsg(Constants.QUICKPAYCOMMONBILL99ERROR);// 快钱发生异常，请联系客服
            return;
        }
        // 一键支付成功，处理商户数据
        // 订单支付成功信息持久化到本地
        String bankLogo = null;// 银行id，如中国农业银行：ABC
        if (null != bill99BankInfoByPayToken) {
            bankLogo = bill99BankInfoByPayToken.getBankLogo();
        }
        boolean persistOK = persistPayResult2Disk(respXml, customerId, merchantId, entryTime, externalRefNumber,
                amount, storablePan, bankLogo);
        if (!persistOK) {
            logger.error("Pay success but persist pay result to disk occurred error, externalRefNumber = " +
                    externalRefNumber);
            logger.error("respXml = " + JsonUtil.toJson(respXml));
        }
        responseData.setEntity(respXml);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * Description: 接收快钱tr3信息并处理
     * @author: JiuDongDong
     * @param signedResponseInfo  tr3信息
     * date: 2018/9/23 14:02
     */
    @Override
    public void receiveTR3ToTR4(ResponseData responseData, String signedResponseInfo) {
        logger.info("It is now in Bill99QuickManagerImpl.receiveTR3ToTR4, the input parameters is: " +
                        "[signedResponseInfo = {}]", signedResponseInfo);
        String merchantPubPath = bill99QuickPayConfigure.getMerchantPubPath();// 公钥
        ParseUtil parseXML = new ParseUtil();
        // 验签
        boolean veriSignForXml = SignUtil.veriSignForXml(signedResponseInfo, merchantPubPath);
        if (!veriSignForXml) {
            logger.error("The result of verify sign data of tr3 from bill99 quick is false, signedResponseInfo  = " +
                    signedResponseInfo);
            // TODO 返回一个状态码，用于响应给快钱
            return;
        }
        // 验签成功后处理信息
        TransInfo transInfo = new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);//返回TR3后的第一个标志字段
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);//返回TR3后的错误标志字段
        transInfo.setFLAG(true);//设置最后的解析方式

        //开始接收TR3
        //将获取的数据传入DOM解析函数中
        HashMap respXml = parseXML.parseXML(signedResponseInfo, transInfo);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The tr3 info from bill99 quick is null, check it, respXml = " + respXml);
            // TODO 返回一个状态码，用于响应给快钱
            return;
        }
        // 处理数据
        String version = (String) respXml.get("version");//接口版本号（version）
        String txnType = (String) respXml.get("txnType");//交易类型编码（txnType）
        String interactiveStatus = (String) respXml.get("interactiveStatus");//消息状态（interactiveStatus）
        String amount = (String) respXml.get("amount");//交易金额（amount）
        String merchantId = (String) respXml.get("merchantId");//商户编号
        String settleMerchantId = (String) respXml.get("settleMerchantId");//商户编号
        String terminalId = (String) respXml.get("terminalId");//终端编号（terminalId）
        String externalRefNumber = (String) respXml.get("externalRefNumber");//外部检索参考号（externalRefNumber）
        String customerId = (String) respXml.get("customerId");//客户号（customerId）
        String refNumber = (String) respXml.get(Constants.BILL99_Q_REF_NUMBER);//检索参考号（refNumber）
        String responseCode = (String) respXml.get(RESPONSE_CODE);//应答码（responseCode）
        //String responseTextMessage=(String)respXml.get(RESPONSE_TEXT_MESSAGE);//应答文本信息（responseTextMessage）
        String transTime = (String) respXml.get("transTime");//交易传输时间（transTime）
        String entryTime = (String) respXml.get("entryTime");//客户端交易时间（entryTime）
        String cardOrg = (String) respXml.get("cardOrg");//发卡组织编号（cardOrg）
        String issuer = (String) respXml.get(Constants.BILL99_Q_ISSUER);//发卡银行名称（issuer）
        String storableCardNo = (String) respXml.get("storableCardNo");//缩略卡号（storableCardNo）
        String authorizationCode = (String) respXml.get("authorizationCode");//授权码（authorizationCode）
        String signature = (String) respXml.get("signature");//报文数字签名（signature）
        //TR3接收完毕

        //当应答码responseCode的值为00时，交易成功 ,txnType :PUR是消费
        if (RESPONSE_CODE_OK.equals(responseCode)) {
            //进行数据库的逻辑操作，比如更新数据库或插入记录。

        }
        //输出TR4
        StringBuffer tr4XML = new StringBuffer();
        tr4XML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\"><version>1.0</version><TxnMsgContent>");
        tr4XML.append("<txnType>").append(txnType).append("</txnType>");
        tr4XML.append("<interactiveStatus>TR4</interactiveStatus>");
        tr4XML.append("<merchantId>").append(merchantId).append("</merchantId>");
        tr4XML.append("<terminalId>").append(terminalId).append("</terminalId>");
        tr4XML.append("<refNumber>").append(refNumber).append("</refNumber>");
        tr4XML.append("</TxnMsgContent></MasMessage>");

        responseData.setEntity(tr4XML);
    }

    /**
     * Description: 1、支付时获取动态码，易网聚鲜发短信（【标准快捷API支付】）
     *              2、解绑快钱快捷时，发送给客户动态码
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param storablePan       加*卡号
     * @param amount            交易金额
     * @param functionType      功能类型，1：支付	2：解绑快捷银行卡
     * date: 2018/9/28 16:41
     */
    @Override
    public void getDynamicValidCodeSelf(ResponseData responseData, String customerId, String externalRefNumber,
                                        String storablePan, String amount, String functionType) {
        logger.info("It is now in Bill99QuickManagerImpl.getDynamicValidCode, the input parameters are: [customerId" +
                        " = {}, externalRefNumber = {}, storablePan = {}, amount = {}, functionType = {}]", customerId,
                externalRefNumber, storablePan, amount, functionType);
        /* 首先从Redis中获取1分钟以内是否已经发过短信，如果已经发过，则不用再发*/
        String validCodeFromRedis;
        if (Constants.STR_ONE.equals(functionType)) {
            validCodeFromRedis = sendMessageRedisService.getValidCodeFromRedis(externalRefNumber);
        } else {
            validCodeFromRedis = sendMessageRedisService.getValidCodeFromRedis(BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan));
        }
        if (StringUtils.isNotBlank(validCodeFromRedis)) {
            logger.warn("Have send a message less than a minute ago, abandon this! externalRefNumber = {}, storablePan = {}",
                    externalRefNumber, storablePan);
            getRandomNum(responseData, customerId);
            return;
        }

        /* 1.2 由于订单一次全款支付、定金支付有30分钟时间限制(30分钟取消订单)、尾款支付时虽然没有支付时间限制，但是支付信息只放入Redis30分钟，所以在支付时需校验时间是否超时
           但是对于PC端的尾款支付，点击发送验证码时才会将信息放入Redis，而验证码有效期仅为2分钟，所以一定不会出现放入Redis信息过期的情况；
           对于App端来说，流程跟PC端不一样，用户选中快捷银行卡，点击下一步按钮跳到发送验证码页面时，将信息放入Redis，这时如果客户在发送验证码页面长时间停留，会出现Redis支付信息过期现象*/
        if (Constants.STR_ONE.equals(functionType)) {
            Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(externalRefNumber);
            String orderTimeOut;//距离支付截止还剩的时间段，以秒为单位
            String orderStatus = redisParam.get(Constants.ORDER_STATUS);// 1100为下单状态，1200为支付定金状态，这时需校验30分钟有效期
            // 拦截计算的基准时间
            String baseTime = "";
            // 1.2.1 （PC、App）下单状态时，选取"下单时间"推算超时拦截时间
            if (Constants.ORDER_WAIT_PAY.intValue() == Integer.valueOf(orderStatus)) {
                String createTimeStr = redisParam.get(CREATE_TIME);
                baseTime = createTimeStr;
                logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
            }
            // 1.2.2 （App）订单尾款支付时，则以用户选中银行，点击下一步时放入Redis的“current时间”作为基准计算订单支付超时拦截时间
            if (Constants.ORDER_PAID_EARNEST.intValue() == Integer.valueOf(orderStatus)) {
                String currentTime = redisParam.get(CURRENT_TIME);
                baseTime = currentTime;
                logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
            }

            if (StringUtils.isNotBlank(baseTime)) {
                // 计算订单支付超时时间
                Date createTimeDate = new Date(Long.valueOf(baseTime));
                // 计算下单时间往后推30分钟的时间
                Date futureMountHoursStart = DateUtil.getFutureMountMinutesStart(createTimeDate, 30);
//                Date futureMountHoursStart = DateUtil.getFutureMountSecondsStart(createTimeDate, 25);
                // 再考虑到短信的2分钟有效期，以及30s的网络传输误差，共150秒
                Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -150);
//                Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -0);
                // 如果截止时间在当前时间之前，则必需拦截此支付
                if (futureMountSecondsStart.getTime() < System.currentTimeMillis()) {
                    logger.error("futureMountSecondsStart.getTime() < System.currentTimeMillis(), " +
                            "futureMountSecondsStart.getTime() = " + futureMountSecondsStart.getTime() + ", " +
                            "System.currentTimeMillis() = " + System.currentTimeMillis() + ", orderNo = " + externalRefNumber);
                    responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
                    responseData.setMsg(Constants.ORDERTIMEOUT);// 本次支付已超时！
                    return;
                }
                // 减去当前时间，得出剩余支付时间长度，转换为秒
                Long time = (futureMountSecondsStart.getTime() - System.currentTimeMillis());
                BigDecimal bigDecimal = new BigDecimal(time).divide(new BigDecimal(CALCULATE)).setScale(0, RoundingMode.DOWN);
                orderTimeOut = String.valueOf(bigDecimal);
            } else {
                // 获取不到则置空
                orderTimeOut = null;
            }
            logger.info("orderTimeOut = {}", orderTimeOut);
        }

        // 短卡号去* (10位不含*）
        String pan = BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan);// 全卡号
        // 根据卡号查询该用户绑定的手机号
        BankAccount bankAccountCurrent = getAllAbleBanksByUserId(customerId, pan);
        if (null == bankAccountCurrent) {
            logger.error("Can not find storablePan from merchant, pan = " + pan);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());// 商户找不到该卡号
            return;
        }
        String mobilePhone = bankAccountCurrent.getMobilePhone();// 该用户绑定的手机号

        // 支付时校验手机号是否是注册手机号
        getPhnoeCheckCodeByUid(responseData, customerId);
        Object entity = responseData.getEntity();
        if (null == entity) {
            logger.error("Got phone is null of customerId = " + customerId);
            responseData.setCode(ResponseStatus.PHONEHASCHANGED.getValue());
            responseData.setMsg(Constants.PHONEHASCHANGED);// 该手机号与注册手机号不符，请填写注册手机号
            return;
        }
        String phone = (String) entity;
        if (!mobilePhone.equals(phone) && Constants.STR_ONE.equals(functionType)) {
            logger.error("The phone in user center has been changed, this card can not been used to pay any more, " +
                    "the customerId is: " + customerId + ", original phone is: " + mobilePhone, ", newest center " +
                    "phone is: " + phone);
            responseData.setCode(ResponseStatus.PHONECHANGEDOLDCARDCANNOTPAY.getValue());
            responseData.setMsg(Constants.PHONECHANGEDOLDCARDCANNOTPAY);// 该银行卡绑定的手机号与平台注册手机号不符，交易有风险，请解绑或联系发卡银行更换手机号
            return;
        }

        // 生成6位验证码
        String validCode = RandomUtils.getRandomNum() + "";
        // 发送的各个字段进行拼接
        String content = "";
        // 短信模板key
        String messageKey = "";
        if (Constants.STR_ONE.equals(functionType)) {
            // 支付时发送短信内容
            content = validCode + "|" + BankCardSwitchUtil.getFinal4OfCardCode(pan) + "|" + amount;
            messageKey = Constants.QUICK_PAY_VALID_CODE;
        } else if (Constants.STR_TWO.equals(functionType)) {
            // 快捷解绑银行卡时发送短信内容
            content = validCode + "|" + BankCardSwitchUtil.getFinal4OfCardCode(pan);
            messageKey = Constants.QUICK_PAY_UNBIND_CARD;
        }
        // 发送短信
        String sendMessageResult = msgClient.postMsg(msgUrl, mobilePhone, customerId, content, messageKey);
        logger.info("Send message result = {} of orderNo = {}", sendMessageResult, externalRefNumber);
        // 将支付信息、解绑银行卡信息放入Redis，时效2分钟
        if (Constants.STR_ONE.equals(functionType)) {
            sendMessageRedisService.putValidCode2Redis(externalRefNumber, validCode, Constants.VALID_CODE_EXPIRED);
        } else if ("2".equals(functionType)) {
            sendMessageRedisService.putValidCode2Redis(pan, validCode, Constants.VALID_CODE_EXPIRED);
        }
        // 放置解密支付密码的随机数
        Integer exitRandomNum = accountFlowRedisService.getRandomNum(Long.parseLong(customerId));
        Integer randomNum = exitRandomNum == null ? RandomUtils.getRandomNum() : exitRandomNum;
        accountFlowRedisService.setRandomNum(Long.parseLong(customerId), randomNum);
        responseData.setEntity(randomNum);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 获取Redis中已存在的用于解密支付密码的随机数
     *              使用场景：前台连点获取验证码按钮，导致后台连续处理2次发送验证码的请求，现在在处理第二次请求的时候，不发送验证码，只返回用于解密支付密码的随机数
     * @author: JiuDongDong
     * @param customerId        客户号
     * date: 2019/7/19 17:01
     */
    @Override
    public void getRandomNum(ResponseData responseData, String customerId) {
        logger.info("It is now in Bill99QuickManagerImpl.getRandomNum, the input parameters are: " +
                "[customerId = {}]", customerId);
        // 获取解密支付密码的随机数
        Integer exitRandomNum = accountFlowRedisService.getRandomNum(Long.parseLong(customerId));
        Integer randomNum = exitRandomNum == null ? RandomUtils.getRandomNum() : exitRandomNum;
        accountFlowRedisService.setRandomNum(Long.parseLong(customerId), randomNum);
        responseData.setEntity(randomNum);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 支付时获取动态码，快钱发短信（【认证支付、协议支付】）
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param storablePan       加*卡号
     * @param amount            交易金额
     * date: 2018/9/26 14:43
     */
    @Override
    public void getDynamicValidCode(ResponseData responseData, String customerId, String externalRefNumber,
                                    String storablePan, String amount) {
        logger.info("It is now in Bill99QuickManagerImpl.getDynamicValidCode, the input parameters are: " +
                "[customerId = {}, externalRefNumber = {}, storablePan = {}, amount = {}]", customerId,
                externalRefNumber, storablePan, amount);
        // 获取配置信息
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 自营商户号
        String payDynNumUrl = bill99QuickPayConfigure.getPayDynNumUrl();// 订单交易-获取动态码地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        // 短卡号去*
        storablePan = BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan);
        BankAccount bankAccountCurrent = getAllAbleBanksByUserId(customerId, storablePan);
        if (null == bankAccountCurrent) {
            logger.error("Can not find this card from merchant, cardNo = " + storablePan);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }

        // 获取请求
        TransInfo transInfo = new TransInfo();
//        //快捷支付首次支付时，卡号pan、手机号码phoneNO必须填写
//        String pan = request.getParameter("pan");    //卡号
//        String phoneNO = request.getParameter("phoneNO");    //手机号码
//        if (externalRefNumber == null || "".equals(externalRefNumber)) {
//            externalRefNumber = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
//        }
//        String cardHolderName = bankAccountCurrent.getBankAccName();//持卡人姓名
        String phoneNO = bankAccountCurrent.getMobilePhone();//手机号
//        String idType = request.getParameter("idType");//证件类型
//        String cardHolderId = request.getParameter("cardHolderId");    //证件号
//        String bankId = request.getParameter("bankId");    //银行代码
//        String expiredDate = request.getParameter("expiredDate");    //卡有效期
//        String cvv2 = request.getParameter("cvv2");    //卡校验码
//        String payBatch = request.getParameter("payBatch");    //快捷支付批次

        //设置手机动态鉴权节点
        transInfo.setRecordeText_1(GET_DYN_NUM_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";

        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<GetDynNumContent>";
        str1Xml += "<merchantId>" + merIdNotHat + "</merchantId>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        //str1Xml += "<cardHolderName>" + cardHolderName + "</cardHolderName>";
        //str1Xml += "<idType>" + idType + "</idType>";
        //str1Xml += "<cardHolderId>" + cardHolderId + "</cardHolderId>";
        //str1Xml += "<pan>" + pan + "</pan>";
        str1Xml += "<storablePan>" + storablePan + "</storablePan>";
        //str1Xml += "<bankId>" + bankId + "</bankId>";
        //str1Xml += "<expiredDate>" + expiredDate + "</expiredDate>";
        //str1Xml += "<cvv2>" + cvv2 + "</cvv2>";
        str1Xml += "<phoneNO>" + phoneNO + "</phoneNO>";
        str1Xml += "<amount>" + amount + "</amount>";
        str1Xml += "</GetDynNumContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of getDynamicValidCode send to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(payDynNumUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merIdNotHat);
        } catch (Exception e) {
            logger.error("Error occurred when get dynamic code for dynamic pay for externalRefNumber = " + externalRefNumber, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of get dynamic code for dynamic pay to bill99 is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of get dynamic code for dynamic pay to bill99 is null for externalRefNumber = "
                    + externalRefNumber);
            responseData.setCode(ResponseStatus.GETDYNUMRESNULLFORDYPAY.getValue());
            responseData.setMsg(Constants.GETDYNUMRESNULLFORDYPAY);// 获取支付动态码发生错误
            return;
        }
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickTradeLogUtil.logTradeInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, externalRefNumber);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Error occurred when get dynamic code for dynamic pay, externalRefNumber = " + externalRefNumber + ",  the responseCode = " + responseCode);
            responseData.setCode(ResponseStatus.DYNAMICPAYBILL99ERROR.getValue());
            responseData.setMsg(Constants.DYNAMICPAYBILL99ERROR);// 银行系统异常请重试
            return;
        }
        //如果TR2获取的应答码responseCode的值为00时，成功
        if (RESPONSE_CODE_OK.equals(responseCode)) {
            /*************************************
             *进行数据库的逻辑操作，比如更新数据库或插入记录。
             *************************************/
            logger.info("获取动态码成功");
        }
        responseData.setEntity(respXml);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * Description: 动态码支付
     * @author: JiuDongDong
     * @param customerId        客户号
     * @param interactiveStatus 消息状态（默认TR1）
     * @param spFlag            特殊交易标志（默认QPay02）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param amount            交易金额,以元为单位，小数点后最多两位
     * @param storablePan       加*卡号
     * @param validCode         手机验证码
     * @param token             手机验证码令牌
     * @param payToken          签约协议号
     * @param payBatch          快捷支付批次, 1首次支付, 2再次支付
     * @param savePciFlag       是否保存鉴权信息, 0不保存, 1保存
     * date: 2018/9/27 13:05
     */
    @Override
    public void dynamicCodePay(ResponseData responseData, String customerId, String interactiveStatus,
                               String spFlag, String txnType, String externalRefNumber, String amount, String storablePan,
                               String validCode, String token, String payToken, String payBatch, String savePciFlag) {
        logger.info("It is now in Bill99QuickManagerImpl.dynamicCodePay, the input parameters are: [customerId = {}, " +
                "interactiveStatus = {}, spFlag = {}, txnType = {}, externalRefNumber = {}, amount = {}, storablePan = {}, " +
                "validCode = {}, token = {}, payToken = {}, payBatch = {}, savePciFlag = {}]", customerId, interactiveStatus,
                spFlag, txnType, externalRefNumber, amount, storablePan, validCode, token, payToken, payBatch, savePciFlag);
        // 获取配置信息
        String merchantId;// 商户号
        String merId = bill99QuickPayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 商户号(自营)
        String terminalId;// 终端号
        String terminalId1 = bill99QuickPayConfigure.getTerminalId1();// 存管终端号
        String terminalIdNotHat1 = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String isSelfPro;// 是否自营商品，0否1是
        /* 1.1 根据自营和非自营选择商户号 */
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(externalRefNumber);
        String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        if ((Constants.STR_ZERO).equals(shopId)
                || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
            merchantId = merIdNotHat;
            terminalId = terminalIdNotHat1;
            isSelfPro = Constants.STR_ONE;
        } else {
            merchantId = merId;
            terminalId = terminalId1;
            isSelfPro = Constants.STR_ZERO;
        }

        String quickPayCommonUrl = bill99QuickPayConfigure.getQuickPayCommonUrl();// 99bill一键快捷支付（普通版）请求地址
        String tr3Url = bill99QuickPayConfigure.getQuickPayCommonTr3Url();// 99bill一键快捷支付（普通版）tr3回调地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码
        String entryTime = simpleDateFormat.format(new Date());//商户端交易时间
        // 短卡号去*
        storablePan = BankCardSwitchUtil.deleteCipheredCodeFromShortCode(storablePan);

        //设置消费交易的两个节点
        TransInfo transInfo= new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        //Tr1报文拼接
        String str1Xml = "";

        //消费交易（再次）
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";

        str1Xml += "<TxnMsgContent>";
        str1Xml += "<interactiveStatus>" + interactiveStatus + "</interactiveStatus>";
        str1Xml += "<spFlag>" + spFlag + "</spFlag>";
        str1Xml += "<txnType>" + txnType + "</txnType>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<entryTime>" + entryTime + "</entryTime>";
        str1Xml += "<amount>" + amount + "</amount>";
        //str1Xml += "<cardNo>" + cardNo + "</cardNo>";
        str1Xml += "<storableCardNo>" + storablePan + "</storableCardNo>";
        //str1Xml += "<expiredDate>" + expiredDate + "</expiredDate>";
        //str1Xml += "<cvv2>" + cvv2 + "</cvv2>";
        str1Xml += "<customerId>" + customerId + "</customerId>";
        //str1Xml += "<cardHolderName>" + cardHolderName + "</cardHolderName>";
        //str1Xml += "<idType>" + idType + "</idType>";
        //str1Xml += "<cardHolderId>" + cardHolderId + "</cardHolderId>";
//        str1Xml += "<tr3Url>"+tr3Url+"</tr3Url>"; // TR3功能弃用

        str1Xml += "<extMap>";
        //str1Xml += "<extDate><key>phone</key><value>" + phone + "</value></extDate>";
        str1Xml += "<extDate><key>validCode</key><value>" + validCode + "</value></extDate>";
        str1Xml += "<extDate><key>savePciFlag</key><value>" + savePciFlag + "</value></extDate>";
        str1Xml += "<extDate><key>token</key><value>" + token + "</value></extDate>";
        str1Xml += "<extDate><key>payBatch</key><value>" + payBatch + "</value></extDate>";
        str1Xml += "</extMap>";
        str1Xml += "</TxnMsgContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of dynamic code pay to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(quickPayCommonUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when dynamic code pay to bill99 for externalRefNumber = " + externalRefNumber, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of dynamic code pay to bill99 is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of dynamic code pay to bill99 is null for externalRefNumber = " + externalRefNumber);
            responseData.setCode(ResponseStatus.QUICKPAYDYNAMICCODERESNULL.getValue());
            responseData.setMsg(Constants.QUICKPAYDYNAMICCODERESNULL);// 动态码支付响应为空
            return;
        }
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 交易状态:订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起
        String txnStatus = (String) respXml.get(Constants.BILL99_Q_TXN_STATUS);
        // 交易类型为退货则: ’S’—退货申请成功 ‘F’－交易失败 ‘D’—已提交收单行
        logger.info("TxnStatus = {}, externalRefNumber = {}", txnStatus, externalRefNumber);
        if (Constants.BILL99_Q_TXN_STATUS_PAY_F.equals(txnStatus)) {
            logger.error("Error occurred when dynamic code pay to bill99, externalRefNumber = " + externalRefNumber +
                    ",  the responseCode = " + responseCode + ", txnStatus = " + txnStatus);
            responseData.setCode(ResponseStatus.DYNAMICPAYBILL99ERROR.getValue());
            responseData.setMsg(Constants.DYNAMICPAYBILL99ERROR);// 系统异常请稍后重试
            return;
        }
        if (BILL99_Q_RESPONSE_CODE_C0.equals(responseCode) || BILL99_Q_RESPONSE_CODE_68.equals(responseCode)
                || BILL99_Q_TXN_STATUS_PAY_P.equals(txnStatus)) {
            logger.error("Bill99 or bank is still handling this valid code pay, externalRefNumber = " + externalRefNumber +
                    ", responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage + ", txnStatus = "
                    + txnStatus);
            // C0: 快钱内部处理中（未完成）、最终交易结果未知，将支付信息放入Redis
            // 68: 快钱内部处理中（未完成）、最终交易结果未知，将支付信息放入Redis
            // P:  交易挂起，将支付信息放入Redis
            OrderInfoVo orderInfoVo = new OrderInfoVo(respXml);
            orderInfoVo.setIsSelfPro(isSelfPro);// 设置是否自营商品
            sendMessageRedisService.putTradeInfo2Redis(orderInfoVo, null);
            if (BILL99_Q_RESPONSE_CODE_C0.equals(responseCode)) {
                responseData.setCode(ResponseStatus.KUAIQIANPAYHANDLING.getValue());// 快钱将该交易挂起
                responseData.setMsg(Constants.KUAIQIANPAYHANDLING);
            }
            if (BILL99_Q_RESPONSE_CODE_68.equals(responseCode)) {
                responseData.setCode(ResponseStatus.BANKPAYHANDLING.getValue());// 银行将该交易挂起
                responseData.setMsg(Constants.BANKPAYHANDLING);
            }
            if (BILL99_Q_TXN_STATUS_PAY_P.equals(responseCode)) {
                responseData.setCode(ResponseStatus.BILL99HANGUP.getValue());// 快钱将交易挂起
                responseData.setMsg(Constants.BILL99HANGUP);
            }
            return;
        }
        // 获取并打印错误信息
        Bill99QuickTradeLogUtil.logTradeInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, externalRefNumber);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("Error occurred when dynamic code pay to bill99, externalRefNumber = " + externalRefNumber
                    + ", the responseCode = " + responseCode);
            responseData.setCode(ResponseStatus.DYNAMICPAYBILL99ERROR.getValue());
            responseData.setMsg(Constants.DYNAMICPAYBILL99ERROR);// 快钱发生错误
            return;
        }
        // 订单支付成功信息持久化到本地
        boolean persistOK = persistPayResult2Disk(respXml, customerId, merchantId, entryTime, externalRefNumber,
                amount, storablePan, null);
        if (!persistOK) {
            logger.error("Pay success but persist pay result to disk occurred error, externalRefNumber = " + externalRefNumber);
        }
        responseData.setEntity(respXml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 订单支付成功信息持久化到本地
     * @author: JiuDongDong
     * @param respXml           TR2接收的数据
     * @param customerId        客户号
     * @param merchantId        商户号
     * @param entryTime         商户订单交易时间
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param amount            交易金额,以元为单位，小数点后最多两位
     * @param storablePan       缩略卡号（10位不含*）
     * @return boolean          是否持久化成功
     * date: 2018/9/27 15:36
     */
    public boolean persistPayResult2Disk(HashMap respXml, String customerId, String merchantId, String entryTime,
                                          String externalRefNumber, String amount, String storablePan, String bankLogo) {
        // 支付成功，处理商户数据
//        String interactiveStatusRes = (String) respXml.get("interactiveStatus");// TR2
//        String authorizationCode = (String) respXml.get("authorizationCode");// 授权码   190991
        String refNumber = (String) respXml.get(Constants.BILL99_Q_REF_NUMBER);//系统参考号 110037975111
//        String issuer = (String) respXml.get(Constants.BILL99_Q_ISSUER);// 发卡银行名称  建设银行
//        String transTime = (String) respXml.get("transTime");// 交易传输时间  20180927131310

        // 封装数据
        Map<String, Object> param = new HashMap<>();
        param.put(Constants.CHANNEL_FLOW_ID, refNumber);//支付渠道流水号
        param.put(Constants.PAYER_PAY_AMOUNT, amount);//付款方支付金额
        param.put(Constants.RECEIVER_USER_ID, merchantId);//收款人ID（商户号）
        param.put(Constants.SUCCESS_TIME, entryTime);//商户订单提交时间
        param.put(Constants.IS_REFUND, Constants.IS_REFUND_NO + "");//是否退款 0:否,1是
        param.put(Constants.RETURN_INFO, null);//返回信息
        param.put(Constants.DESP, Constants.BUY_GOODS);//描述
        param.put(Constants.UID, Constants.UID_BILL);//操作人标识
        if (StringUtils.isBlank(bankLogo)) {
            BankAccount bankAccountCurrent = getAllAbleBanksByUserId(customerId, storablePan);
            if (null == bankAccountCurrent) {
                logger.error("Can not find this card from merchant, storablePan = " + storablePan);
                return false;
            }
            bankLogo = bankAccountCurrent.getBankLogo();
        }
        param.put(Constants.BOB_CHANNEL_TYPE, bankLogo);//渠道类型 07：互联网； 08：移动； 其他：银行编号
        param.put(Constants.PAYER_ID, Constants.STR_ONE);// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
        param.put(Constants.TRADE_TYPE, Constants.TRADE_TYPE_1);// 交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
        param.put(Constants.INTERACTION_ID, externalRefNumber);//订单号
        param.put(Constants.PAY_CHANNEL, Constants.INTEGER_FORTY_FIVE + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 45快钱快捷
        param.put(Constants.TYPE_NAME, KUAIQIAN);
        param.put(Constants.TYPE_CODE, Constants.INTEGER_FORTY_FIVE + "");
        param = CalMoneyByFate.calMoneyByFate(param);
        param.put(Constants.RECEIVER_FEE, Constants.STR_ZERO);//收款方手续费
        param.put(Constants.PLATINCOME, amount);//平台收入
        // 3.2 订单支付成功信息持久化到本地
        logger.info("Receive notify and serialize to merchant for orderId: {} start!!!", externalRefNumber);
        boolean ifSuccess = commonsManager.ifSuccess(param);
        if (ifSuccess) {
            logger.info("Receive notify and serialize to merchant {} for orderId: {}", ifSuccess, externalRefNumber);
            return true;
        } else {
            logger.error("Receive notify and serialize to merchant " + ifSuccess + " for orderId: " + externalRefNumber);
            // 订单支付成功，但持久化错误，这时仍然告诉客户支付成功，服务器需做补救处理(ifSuccess方法会自动调用worker补救)
            return false;
        }

    }

    /**
     * Description: VPOS_CNP查询交易
     * @author: JiuDongDong
     * @param txnType  交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param externalRefNumber 外部跟踪编号
     * @param refNumber         检索参考号
     * @param isSelfPro			是否自营商品，0否1是
     * date: 2018/9/28 10:13
     */
    @Override
    public void queryOrder(ResponseData responseData, String txnType, String externalRefNumber, String refNumber,
                           String isSelfPro) {
        logger.info("It is now in Bill99QuickManagerImpl.queryOrder, the input parameters are: [txnType = {}, " +
                "externalRefNumber = {}, refNumber = {}, isSelfPro = {}]", txnType, externalRefNumber, refNumber,
                isSelfPro);
        // 获取配置信息
        String merchantId = "";// 商户号
        String merId = bill99QuickPayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 商户号(自营)
        String terminalId = "";// 终端号
        String terminalId1 = bill99QuickPayConfigure.getTerminalId1();// 存管终端号
        String terminalIdNotHat1 = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        logger.info("merId = {}, merIdNotHat = {}, terminalId1 = {}, terminalIdNotHat1 = {}",
                merId, merIdNotHat, terminalId1, terminalIdNotHat1);
        /* 1.1 根据自营和非自营选择商户号 */
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(externalRefNumber);
        if (MapUtils.isNotEmpty(redisParam)) {
            String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
            String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
            if ((Constants.INTEGER_ZERO + "").equals(shopId)
                    || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                    || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                    || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
                merchantId = merIdNotHat;
                terminalId = terminalIdNotHat1;
            } else {
                merchantId = merId;
                terminalId = terminalId1;
            }
            logger.info("merId = {}, merIdNotHat = {}, terminalId1 = {}, terminalIdNotHat1 = {}",
                    merId, merIdNotHat, terminalId1, terminalIdNotHat1);
        }
        if (StringUtils.isNotBlank(isSelfPro)) {
            if (Constants.STR_ONE.equals(isSelfPro)) {
                merchantId = merIdNotHat;
                terminalId = terminalIdNotHat1;
            } else {
                merchantId = merId;
                terminalId = terminalId1;
            }
            logger.info("merId = {}, merIdNotHat = {}, terminalId1 = {}, terminalIdNotHat1 = {}",
                    merId, merIdNotHat, terminalId1, terminalIdNotHat1);
        }
        logger.info("merchantId = {}, terminalId = {}", merchantId, terminalId);
        String queryOrderUrl = bill99QuickPayConfigure.getQueryOrderUrl();// VPOS_CNP查询交易地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        //设置手机动态鉴权节点
        TransInfo transInfo= new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);
        transInfo.setRecordeText_3(QRY_TXN_MSG_CONTENT);
        transInfo.setRecordeText_4(ERROR_MSG_CONTENT);

        String str1Xml="";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<QryTxnMsgContent>";
        if (StringUtils.isNotBlank(externalRefNumber)) str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        if (StringUtils.isNotBlank(refNumber)) str1Xml += "<refNumber>" + refNumber + "</refNumber>";
        str1Xml += "<txnType>" + txnType + "</txnType>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "</QryTxnMsgContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of query order to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(queryOrderUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when query order to bill99 for externalRefNumber = " + externalRefNumber, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of query order is: {}", respXml);
        if (MapUtils.isEmpty(respXml)) {
            logger.error("The result of query order to bill99 is null for externalRefNumber = " + externalRefNumber);
            responseData.setCode(ResponseStatus.QUERYORDERRESNULL.getValue());
            responseData.setMsg(Constants.QUERYORDERRESNULL);// 订单查询响应为空
            return;
        }
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 其实支付时，并不会响应txnStatus这个字段，只要responseCode=00即表示支付成功。在订单支付查询时，responseCode=00只表示
        // 响应成功，还需要看txnStatus的实际值去判断支付状态或退款状态

        // 获取并打印错误信息
        Bill99QuickTradeLogUtil.logTradeInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, externalRefNumber);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            logger.error("Bill99 handle failed, respXml = " + respXml);
//            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode) && (!BILL99_Q_RESPONSE_CODE_68.equals(responseCode)) && !BILL99_Q_RESPONSE_CODE_C0.equals(responseCode)) {
            logger.error("Error occurred when query order to bill99, externalRefNumber = " + externalRefNumber + ", " +
                    "the responseCode = " + responseCode + ", errorCode = " + errorCode + ", errorMessage = " + errorMessage);
            responseData.setCode(ResponseStatus.QUERYORDERERROR.getValue());
            responseData.setMsg(Constants.QUERYORDERERROR);// 订单查询快钱发生错误
            return;
        }
        //如果TR2获取的应答码responseCode的值为00时，成功
        if (RESPONSE_CODE_OK.equals(responseCode)) {
            logger.info("This trade is ok, go on next process, externalRefNumber = {}, refNumber = {}",
                    externalRefNumber, refNumber);
        }
        responseData.setEntity(respXml);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * Description: 退货交易
     * @author: JiuDongDong
     * @param interactiveStatus 消息状态（默认TR1）
     * @param txnType           txnType   交易类型编码，PUR 消费交易，INP 分期消费交易，PRE 预授权交易，CFM 预授权完成交易，VTX 撤销交易，RFD 退货交易，CIV 卡信息验证交易
     * @param entryTime         商户端交易时间
     * @param amount            退款金额,以元为单位，小数点后最多两位
     * @param externalRefNumber 外部跟踪编号（订单号）
     * @param origRefNumber     原检索参考号，对应原交易的检索参考号
     * date: 2018/9/28 17:52
     */
    @Override
    public void refundOrder(ResponseData responseData, String interactiveStatus, String txnType, String entryTime,
                            String amount, String externalRefNumber, String origRefNumber) {
        logger.info("It is now in Bill99QuickManagerImpl.refundOrder, the input parameter are: [interactiveStatus = {}" +
                        ", txnType = {}, entryTime = {}, amount = {}, externalRefNumber = {}, origRefNumber = {}]",
                interactiveStatus, txnType, entryTime, amount, externalRefNumber, origRefNumber);
        // 获取配置信息
        String merchantId;// 商户号
        String merId = bill99QuickPayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99QuickPayConfigure.getMerIdNotHat();// 商户号(自营)
        String terminalId;// 终端号
        String terminalId1 = bill99QuickPayConfigure.getTerminalId1();// 存管终端号
        String terminalIdNotHat1 = bill99QuickPayConfigure.getTerminalIdNotHat1();// 自营终端号
        String isSelfPro;// 是否自营商品，0否1是
        /* 1.1 根据自营和非自营选择商户号 */
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(externalRefNumber);
        String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        if ((Constants.STR_ZERO).equals(shopId)
                || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
            merchantId = merIdNotHat;
            terminalId = terminalIdNotHat1;
            isSelfPro = Constants.STR_ONE;
        } else {
            merchantId = merId;
            terminalId = terminalId1;
            isSelfPro = Constants.STR_ZERO;
        }
        String refundUrl = bill99QuickPayConfigure.getRefundUrl();// 退款请求地址
        String merchantCertPath = bill99QuickPayConfigure.getMerchantCertPath();// 商户私钥名字
        String merchantCertPss = bill99QuickPayConfigure.getMerchantCertPss();// 私钥密码

        //设置手机动态鉴权节点
        TransInfo transInfo= new TransInfo();
        transInfo.setRecordeText_1(TXN_MSG_CONTENT);
        transInfo.setRecordeText_2(ERROR_MSG_CONTENT);

        String str1Xml="";
        str1Xml += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        str1Xml += "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
        str1Xml += "<version>1.0</version>";
        str1Xml += "<TxnMsgContent>";
        str1Xml += "<interactiveStatus>" + interactiveStatus + "</interactiveStatus>";
        str1Xml += "<txnType>" + txnType + "</txnType>";
        str1Xml += "<merchantId>" + merchantId + "</merchantId>";
        str1Xml += "<terminalId>" + terminalId + "</terminalId>";
        str1Xml += "<entryTime>" + entryTime + "</entryTime>";
        str1Xml += "<amount>" + amount + "</amount>";
        str1Xml += "<externalRefNumber>" + externalRefNumber + "</externalRefNumber>";
        str1Xml += "<origRefNumber>" + origRefNumber + "</origRefNumber>";
        str1Xml += "</TxnMsgContent>";
        str1Xml += "</MasMessage>";
        logger.info("The tr1 param of refund order to bill99 is: {}", str1Xml);

        //TR2接收的数据
        HashMap respXml;
        try {
            respXml = Post.sendPost(refundUrl, str1Xml, transInfo, merchantCertPath, merchantCertPss, merchantId);
        } catch (Exception e) {
            logger.error("Error occurred when refund order to bill99 for externalRefNumber = " + externalRefNumber, e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        logger.info("The result of refund order from bill99 is: {}", respXml);
        if (respXml == null) {
            logger.error("The result of refund order from bill99 is null for externalRefNumber = " + externalRefNumber);
            responseData.setCode(ResponseStatus.REFUNDORDERRESNULL.getValue());
            responseData.setMsg(Constants.REFUNDORDERRESNULL);// 退款响应为空
            return;
        }
        String responseTextMessage = (String) respXml.get(RESPONSE_TEXT_MESSAGE);
        String responseCode = (String) respXml.get(RESPONSE_CODE);
        String errorCode = (String) respXml.get(ERROR_CODE);
        String errorMessage = (String) respXml.get(ERROR_MESSAGE);
        // 获取并打印错误信息
        Bill99QuickTradeLogUtil.logTradeInfo(responseData, responseCode, responseTextMessage,
                errorCode, errorMessage, externalRefNumber);
        String code = responseData.getCode();
        if (null != code && !ResponseStatus.OK.getValue().equals(code)) {
            return;
        }
        if (!RESPONSE_CODE_OK.equals(responseCode)) {
            logger.error("The card length is not enough, responseCode = " + responseCode + ", errorMessage " +
                    "= " + errorMessage + ", responseTextMessage = " + responseTextMessage);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(responseTextMessage);
            return;
        }
        responseData.setEntity(respXml);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * Description: 根据短卡号查询用户在快钱绑定的有效的快捷银行卡信息
     * @author: JiuDongDong
     * @param customerId 用户id
     * @param storablePan 缩略卡号（10位不含*）
     * @return com.ewfresh.pay.model.BankAccount 用户在快钱绑定的有效的快捷银行卡信息
     * date: 2018/9/27 15:01
     */
    @Override
    public BankAccount getAllAbleBanksByUserId(String customerId, String storablePan) {
        // 获取该用户绑过的所有卡信息
        List<BankAccount> allAbleBanksByUserId = bankAccountService.getAllAbleBanksByUserId(Long.valueOf(customerId));
        // 遍历匹配该卡
        BankAccount bankAccountCurrent = null;
        for (BankAccount bankAccount : allAbleBanksByUserId) {
            String cardCode = bankAccount.getCardCode();// 银行卡号
            String shortCardCode =
                    cardCode.substring(Constants.INTEGER_ZERO, Constants.INTEGER_SIX) +
                            cardCode.substring(cardCode.length() - Constants.INTEGER_FOUR, cardCode.length());// 截取前6后4
            if (shortCardCode.equals(storablePan)) {
                bankAccountCurrent = bankAccount;
                break;
            }
        }
        return bankAccountCurrent;
    }

    /**
     * Description: 获取注册手机号
     * @author: JiuDongDong
     * @param customerId        客户号
     * @return java.lang.String 手机号
     * date: 2018/11/5 11:20
     */
    @Override
    public void getPhnoeCheckCodeByUid(ResponseData responseData, String customerId) {
        logger.info("It is now in Bill99QuickManagerImpl.getPhnoeCheckCodeByUid, the input parameter is:" +
                        " [customerId = {}]", customerId);
        Map userInfo = getUserInfoRedisService.getUserInfo(customerId);
        if (MapUtils.isNotEmpty(userInfo)) {
            String phone = (String) userInfo.get(SHOW_PHONE);
            logger.info("The phone of customer: {} is: {}", customerId, phone);
            responseData.setEntity(phone);
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 将用户某个手机号下绑定的快钱快捷银行卡置为失效
     * @author: JiuDongDong
     * @param userId    用户id
     * @param newMobilePhone   新手机号
     * @param oldMobilePhone   旧手机号
     * date: 2018/11/5 15:05
     */
    @Override
    public void updatePhoneChangedExpired(ResponseData responseData, Long userId, String newMobilePhone, String oldMobilePhone) {
        logger.info("It is now in Bill99QuickManagerImpl.updatePhoneChangedExpired, the input parameter is:" +
                " [userId = {}, newMobilePhone = {}, oldMobilePhone = {}]", userId, newMobilePhone, oldMobilePhone);
        // phoneChangedExpired   手机号变更失效：0否  1是
        // 将老手机号对应的所有银行卡置为phoneChangedExpired=1，新手机号对应的所有银行卡置为phoneChangedExpired=0
        bankAccountService.updatePhoneChangedExpired(userId, newMobilePhone, oldMobilePhone, Constants.SHORT_ZERO, Constants.SHORT_ONE);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 查询今天是否使用快钱快捷的农业银行,交通银行,兴业银行,光大银行,中信银行,邮储银行,上海银行进行支付
                    以上这几个银行是不支持当天申请退款的，隔天是可以申请的
     * @author: JiuDongDong
     * @param orderId   父订单号
     * date: 2019/01/23 16:41
     */
    @Override
    public void ifUseSpecialBank(ResponseData responseData, Long orderId) {
        logger.info("It is now in Bill99QuickManagerImpl.ifUseSpecialBank, the input parameter is:" +
                " [orderId = {}]", orderId);
        // 查询定金、尾款、全款符合条件的支付流水数量
        int num = payFlowService.ifUseSpecialBank(orderId);
        responseData.setEntity(num);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

}
