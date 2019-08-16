package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.Bill99Manager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.redisService.BankAccountRedisService;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99.*;
import com.ewfresh.pay.util.bill99.MD5Util;
import com.ewfresh.pay.util.bill99.bill99Soap.Bill99LoggerByResCodeAndTransType;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.bill99.bill99Soap.GatewayRefundQueryServiceLocator;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.boc.MyHttp;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.rpc.ServiceException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.CALCULATE;

/**
 * description: Bill99订单业务的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/7/31.
 */
@Component
public class Bill99ManagerImpl implements Bill99Manager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private BankAccountRedisService bankAccountRedisService;
    @Autowired
    private GetOrderInfoFromRedisUtil getOrderInfoFromRedisUtil;

    private static final String INPUT_CHAR_SET = "1";//编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。
    private static final String VERSION = "v2.0";//网关版本，固定值：v2.0,该参数必填。
    private static final String LANGUAGE = "1";//语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。
    private static final String SIGN_TYPE_1 = "1";//签名类型	,数字串1 代表MD5 加密签名方式
    private static final String SIGN_TYPE_4 = "4";//签名类型,该值为4，代表PKI加密方式,该参数必填。

    private static final String EXT1 = "";//扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
    private static final String EXT2 = "";//扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
    private static final String PAY_TYPE = "00";//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
    private static final String REDO_FLAG = "0";//同一订单禁止重复提交标志，实物购物车填1，虚拟产品用0。1代表只能提交一次，0代表在支付不成功情况下可以再提交。可为空。
    private static final String KUAIQIAN = "快钱";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String NO_RESULTS = "10012";
    private static final String POINT = "100";//以分为单位
    private static final String CREATE_TIME = "createTime";
    private static final String CURRENT_TIME = "currentTime";
    private static final String RSPCODE =  "rspCode";
    private static final String RSPMSG = "rspMsg";


    /**
     * Description: 用户请求订单信息
     * @author: JiuDongDong
     * @param payerName        支付人姓名,可以为空。
     * @param payerContactType 支付人联系类型，1 代表电子邮件方式；2 代表手机联系方式。可以为空。
     * @param payerContact     支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址；payerContactType为2，则填写手机号码。可以为空。
     * @param orderNo          商户订单号，不能为空。
     * @param orderAmount      订单金额，该参数必填。
     * @param bankId           银行代码，如果payType为00，该值可以为空；如果payType为10，该值必须填写，具体请参考银行列表。
     * @param payType          支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
     * @param orderIp          下单ip，不能为空。
     * @param payerIdType      指定付款人: 0代表不指定 1代表通过商户方 ID 指定付款人  2代表通过快钱账户指定付款人  3代表付款方在商户方的会员编号(当需要支持保存信息功能的快捷支付时，,需上送此项)  4代表企业网银的交通银行直连
     * @param payerId          付款人标识: 交行企业网银的付款方银行账号，当企业网银中的交通银行直连，此值不能为空。当需要支持保存信息功能的快捷支付时，此值不能为空，此参数需要传入付款方在商户方的会员编号
     * date: 2018/8/10 10:10
     */
    @Override
    public void sendOrder(ResponseData responseData, String payerName, String payerContact, String orderNo, String orderAmount,
                          String bankId, String payType, String payerContactType, String orderIp, String payerIdType, String payerId)
            throws ShouldPayNotEqualsException {
        logger.info("It is now in Bill99ManagerImpl.sendOrder, the parameters are: [payerName = {}, payerContact = {}, orderNo = {}, " +
                "orderAmount = {}, bankId = {}, payType = {}, payerContactType = {}, orderIp = {}, payerIdType = {}, payerId = {}]",
            payerName, payerContact, orderNo, orderAmount, bankId, payType, payerContactType, orderIp, payerIdType, payerId);
        /* 1.获取配置信息 */
        String merchantAcct;//人民币账户
        String merchantAcctIdNotHat = bill99PayConfigure.getMerchantAcctIdNotHat();//自营人民币账户
        String merchantAcctId = bill99PayConfigure.getMerchantAcctId();//非自营人民币账户
        String bgUrl = bill99PayConfigure.getBgUrl();
        String payUrl = bill99PayConfigure.getPayUrl();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        /* 1.1 根据自营和非自营选择人民币账户 */
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(orderNo);
        String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        if ((Constants.INTEGER_ZERO + "").equals(shopId)
                || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
            merchantAcct = merchantAcctIdNotHat;
        } else {
            merchantAcct = merchantAcctId;
        }
        logger.info("The shopId is: {}, isRecharge = {}", shopId, isRecharge);
        logger.info("The merchantAcct is: {}", merchantAcct);
        /* 1.2 校验订单支付金额是否正确 */
        String shouldPayMoney = redisParam.get(Constants.SURPLUS);
        if (new BigDecimal(orderAmount).compareTo(new BigDecimal(shouldPayMoney)) != Constants.INTEGER_ZERO) {
            logger.error("Web should pay money not equals redis, shouldPayMoney from redis = " + shouldPayMoney +
                    ", web orderAmount = " + orderAmount);
            throw new ShouldPayNotEqualsException("Web should pay money not equals redis, shouldPayMoney " +
                    "from redis = " + orderAmount + ", web amount = " + shouldPayMoney);
        }

        /* 1.2 由于订单一次全款支付、定金支付有30分钟时间限制(30分钟取消订单)、尾款支付时虽然没有支付时间限制，但是支付信息只放入Redis30分钟，所以在支付时需校验时间是否超时 */
        String orderTimeOut;//距离支付截止还剩的时间段，以秒为单位
        String orderStatus = redisParam.get(Constants.ORDER_STATUS);// 1100为下单状态，1200为支付定金状态，这时需校验30分钟有效期
        // 拦截计算的基准时间
        String baseTime = "";
        // 1.2.1 下单状态时，选取"下单时间"推算超时拦截时间
        if (Constants.ORDER_WAIT_PAY.intValue() == Integer.valueOf(orderStatus)) {
            String createTimeStr = redisParam.get(CREATE_TIME);
            baseTime = createTimeStr;
            logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
        }
        // 1.2.2 订单尾款支付时，则以用户选中银行，点击下一步时放入Redis的“current时间”作为基准计算订单支付超时拦截时间
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
            // 再减去30s的网络传输误差
            Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -Constants.INTEGER_THIRTY);
//                Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -0);
            // 如果截止时间在当前时间之前，则必需拦截此支付
            if (futureMountSecondsStart.getTime() < System.currentTimeMillis()) {
                logger.error("futureMountSecondsStart.getTime() < System.currentTimeMillis(), " +
                        "futureMountSecondsStart.getTime() = " + futureMountSecondsStart.getTime() + ", " +
                        "System.currentTimeMillis() = " + System.currentTimeMillis() + ", orderNo = " + orderNo);
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

        /* 2. 设置请求信息 */
        Bill99MerchantSendOrderVo bill99OrderVo = new Bill99MerchantSendOrderVo();
        bill99OrderVo.setMerchantAcctId(merchantAcct);
        bill99OrderVo.setInputCharset(INPUT_CHAR_SET);
        bill99OrderVo.setPageUrl("");
        bill99OrderVo.setBgUrl(bgUrl);
        bill99OrderVo.setPayUrl(payUrl);
        bill99OrderVo.setVersion(VERSION);
        bill99OrderVo.setLanguage(LANGUAGE);
        bill99OrderVo.setSignType(SIGN_TYPE_4);
        bill99OrderVo.setPayerName(payerName);
        bill99OrderVo.setPayerContactType(StringUtils.isNotBlank(payerContactType) ? payerContactType : "");
        bill99OrderVo.setPayerContact(StringUtils.isNotBlank(payerContact) ? payerContact : "");
        bill99OrderVo.setPayerIdType(StringUtils.isNotBlank(payerIdType) ? payerIdType : "");
        bill99OrderVo.setPayerId(StringUtils.isNotBlank(payerId) ? payerId : "");
        bill99OrderVo.setOrderId(orderNo);
        bill99OrderVo.setOrderAmount(FenYuanConvert.yuan2Fen(orderAmount).toString());
        bill99OrderVo.setOrderTime(sdf.format(new Date()));
        bill99OrderVo.setExt1(orderIp);//orderIp设置进ext字段
        bill99OrderVo.setExt2(EXT2);
        bill99OrderVo.setPayType(StringUtils.isNotBlank(payType) ? payType : PAY_TYPE);
        bill99OrderVo.setBankId(StringUtils.isBlank(bankId) ? "" : bankId);
        bill99OrderVo.setRedoFlag(REDO_FLAG);
        bill99OrderVo.setPid("");// merId 快钱-江章悦让置空
        if (StringUtils.isNotBlank(orderTimeOut)) bill99OrderVo.setOrderTimeOut(orderTimeOut);

        // 签名
        String signMsgVal = "";
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_INPUT_CHARSET, INPUT_CHAR_SET);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAGE_URL, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_BG_URL, bgUrl);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_VERSION, VERSION);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_LANGUAGE, LANGUAGE);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_SIGN_TYPE, SIGN_TYPE_4);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_MERCHANT_ACCT_ID, merchantAcct);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAYER_NAME, payerName);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAYER_CONTACT_TYPE, bill99OrderVo.getPayerContactType());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAYER_CONTACT, bill99OrderVo.getPayerContact());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAYER_ID_TYPE, bill99OrderVo.getPayerIdType());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAYER_ID, bill99OrderVo.getPayerId());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_ORDER_ID, orderNo);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_ORDER_AMOUNT, FenYuanConvert.yuan2Fen(orderAmount).toString());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_ORDER_TIME, bill99OrderVo.getOrderTime());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PRODUCT_NAME, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PRODUCT_NUM, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PRODUCT_ID, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PRODUCT_DESC, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_EXT1, bill99OrderVo.getExt1());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_EXT2, "");
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PAY_TYPE, StringUtils.isNotBlank(payType) ? payType : PAY_TYPE);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_BANK_ID, bill99OrderVo.getBankId());
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_REDO_FLAG, REDO_FLAG);
        signMsgVal = appendParam(signMsgVal, Constants.BILL99_PID, bill99OrderVo.getPid());
        if (StringUtils.isNotBlank(orderTimeOut))
            signMsgVal = appendParam(signMsgVal, Constants.BILL99_ORDER_TIME_OUT, bill99OrderVo.getOrderTimeOut());

        logger.info("signMsgVal: {}", signMsgVal);
        Pkipair pki = new Pkipair();
        String signMsg = pki.signMsg(signMsgVal, merchantCertPath, merchantCertPss);
        bill99OrderVo.setSignMsg(signMsg);

        /* 3. 将包装好的订单支付请求参数返给页面 */
        responseData.setEntity(bill99OrderVo);
        logger.info("The send order parameters have been packaged ok and will send them back to html");
        logger.info("The response order pay info: {}", JsonUtil.toJson(responseData));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    /**
     * Description: 订单支付支付应答
     * @author: JiuDongDong
     * @param params 封装响应数据
     * date: 2018/8/10 10:11
     */
    @Override
    public void receiveNotify(ResponseData responseData, Map<String, String> params) {
        logger.info("It is now in Bill99ManagerImpl.receiveNotify, the received parameters are: {}", JsonUtil.toJson(params));
        /* 1.获取配置信息 */
        String merId = bill99PayConfigure.getMerId();// 商户号(存管)
        String merIdNotHat = bill99PayConfigure.getMerIdNotHat();// 商户号(自营)
        String frontEndUrl = bill99PayConfigure.getFrontEndUrl();
        String frontFailUrl = bill99PayConfigure.getFrontFailUrl();
        String merchantPubPath = bill99PayConfigure.getMerchantPubPath();
        /* 1.1 根据自营和非自营选择商户号 */
        String orderId = params.get(Constants.BILL99_ORDER_ID);//商户订单号，该值与提交时相同。
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(orderId);
        String shopId = redisParam.get(Constants.SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(Constants.IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        if ((Constants.INTEGER_ZERO + "").equals(shopId)
                || Constants.TRADE_TYPE_4.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_15.toString().equals(isRecharge)
                || Constants.TRADE_TYPE_16.toString().equals(isRecharge)) {
            merId = merIdNotHat;
        }

        /* 2. 验签 */
        String merchantAcctId = params.get(Constants.BILL99_MERCHANT_ACCT_ID);//人民币网关账号，该账号为11位人民币网关商户编号+01,该值与提交时相同。
        String version = params.get(Constants.BILL99_VERSION);//网关版本，固定值：v2.0,该值与提交时相同。
        String language = params.get(Constants.BILL99_LANGUAGE);//语言种类，1代表中文显示，2代表英文显示。默认为1,该值与提交时相同。
        String signType = params.get(Constants.BILL99_SIGN_TYPE);//签名类型,该值为4，代表PKI加密方式,该值与提交时相同。
        String payType = params.get(Constants.BILL99_PAY_TYPE);//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10,该值与提交时相同。
        String bankId = params.get(Constants.BILL99_BANK_ID);//银行代码，如果payType为00，该值为空；如果payType为10,该值与提交时相同。
        String orderTime = params.get(Constants.BILL99_ORDER_TIME);//订单提交时间，格式：yyyyMMddHHmmss，如：20071117020101,该值与提交时相同。
        String orderAmount = params.get(Constants.BILL99_ORDER_AMOUNT);//订单金额，金额以“分”为单位，商户测试以1分测试即可，切勿以大金额测试,该值与支付时相同。
        String bindCard = params.get(Constants.BILL99_BIND_CARD);//已绑短卡号,信用卡快捷支付绑定卡信息后返回前六后四位信用卡号
        String bindMobile = params.get(Constants.BILL99_BIND_MOBILE);//已绑短手机尾号,信用卡快捷支付绑定卡信息后返回前三位后四位手机号码
        String dealId = params.get(Constants.BILL99_DEAL_ID);// 快钱交易号，商户每一笔交易都会在快钱生成一个交易号。
        String bankDealId = params.get(Constants.BILL99_BANK_DEAL_ID);//银行交易号 ，快钱交易在银行支付时对应的交易号，如果不是通过银行卡支付，则为空
        String dealTime = params.get(Constants.BILL99_DEAL_TIME);//快钱交易时间，快钱对交易进行处理的时间,格式：yyyyMMddHHmmss，如：20071117020101
        String payAmount = params.get(Constants.BILL99_PAY_AMOUNT);//商户实际支付金额 以分为单位。比方10元，提交时金额应为1000。该金额代表商户快钱账户最终收到的金额。
        String fee = params.get(Constants.BILL99_FEE);//费用，快钱收取商户的手续费，单位为分。
        String ext1 = params.get(Constants.BILL99_EXT1);//扩展字段1，该值与提交时相同。
        String ext2 = params.get(Constants.BILL99_EXT2);//扩展字段2，该值与提交时相同。
        String payResult = params.get(Constants.BILL99_PAY_RESULT);//处理结果， 10支付成功，11 支付失败，00订单申请成功，01 订单申请失败
        String errCode = params.get(Constants.BILL99_ERR_CODE);//错误代码 ，请参照《人民币网关接口文档》最后部分的详细解释。
        String signMsg = params.get(Constants.BILL99_SIGN_MSG);//签名字符串

        String merchantSignMsgVal = "";
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_MERCHANT_ACCT_ID, merchantAcctId);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_VERSION, version);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_LANGUAGE, language);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_SIGN_TYPE, signType);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_PAY_TYPE, payType);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_BANK_ID, bankId);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_ORDER_ID, orderId);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_ORDER_TIME, orderTime);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_ORDER_AMOUNT, orderAmount);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_BIND_CARD, StringUtils.isBlank(bindCard) ? "" : bindCard);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_BIND_MOBILE, StringUtils.isBlank(bindMobile) ? "" : bindMobile);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_DEAL_ID, dealId);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_BANK_DEAL_ID, bankDealId);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_DEAL_TIME, dealTime);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_PAY_AMOUNT, payAmount);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_FEE, fee);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_EXT1, ext1);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_EXT2, ext2);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_PAY_RESULT, payResult);
        merchantSignMsgVal = verifyAppendParam(merchantSignMsgVal, Constants.BILL99_ERR_CODE, errCode);

        Pkipair pki = new Pkipair();
        boolean flag = pki.enCodeByCer(merchantSignMsgVal, signMsg, merchantPubPath);
        int rtnOK;// 接收到回调信息后，向99bill响应的信息
        String rtnUrl;// 接收到回调信息后，向99bill响应的信息

        /* 3. 验签成功后，处理业务数据*/
        if (flag) {
            switch (Integer.parseInt(payResult)) {
                case Constants.INT_TEN:
                    rtnOK = Constants.INTEGER_ONE;
                    rtnUrl = frontEndUrl + "?msg=success";
                    /* 3.1 支付成功，支付流水持久化到本地 */
                    // 封装数据
                    Map<String, Object> param = new HashMap<>();
                    param.put(Constants.CHANNEL_FLOW_ID, dealId);//支付渠道流水号
                    param.put(Constants.PAYER_PAY_AMOUNT, FenYuanConvert.fen2YuanWithStringValue(orderAmount));//付款方支付金额
                    param.put(Constants.RECEIVER_USER_ID, merId);//收款人ID（商户号）
                    param.put(Constants.SUCCESS_TIME, orderTime);//商户订单提交时间
                    param.put(Constants.IS_REFUND, Constants.IS_REFUND_NO + "");//是否退款 0:否,1是
                    param.put(Constants.RETURN_INFO, null);//返回信息
                    param.put(Constants.DESP, Constants.BUY_GOODS);//描述
                    param.put(Constants.UID, Constants.UID_BILL);//操作人标识
                    //渠道类型 07：互联网； 08：移动； 其他：银行编号
                    String bankIdRedis = redisParam.get(Constants.BILL99_BANK_ID);// 银行id
                    param.put(Constants.BOB_CHANNEL_TYPE, StringUtils.isBlank(bankIdRedis) ? bankId : bankIdRedis);
                    param.put(Constants.PAYER_ID, Constants.INTEGER_ONE + "");// 付款人id随便填，只是CommonsManagerImpl.ifSuccess()会校验非空，在该方法内会重新从Redis中取出付款人id赋值
                    param.put(Constants.TRADE_TYPE, Constants.TRADE_TYPE_1);//交易类型，这里随便填，CommonsManagerImpl.ifSuccess()会从Redis里信息重新赋值
                    param.put(Constants.INTERACTION_ID, orderId);//订单号
                    param.put(Constants.PAY_CHANNEL, Constants.INTEGER_SIX + "");//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 7中国银联
                    param.put(Constants.TYPE_NAME, KUAIQIAN);
                    param.put(Constants.TYPE_CODE, Constants.INTEGER_SIX + "");
                    param = CalMoneyByFate.calMoneyByFate(param);
                    param.put(Constants.RECEIVER_FEE, FenYuanConvert.fen2YuanWithStringValue(StringUtils.isBlank(fee) ? "0" : fee));//收款方手续费
                    param.put(Constants.PLATINCOME, FenYuanConvert.fen2YuanWithStringValue(new BigDecimal(orderAmount).subtract(new BigDecimal(StringUtils.isBlank(fee) ? "0" : fee)) + ""));//平台收入
                    // 3.2 订单支付信息持久化到本地
                    boolean ifSuccess = commonsManager.ifSuccess(param);
                    logger.info("Receive notify and serialize to merchant {} for orderId: {}", ifSuccess, orderId);
                    break;
                default:
                    logger.error("customer pay to 99bill failed for orderId: " + orderId);
                    rtnOK = Constants.INTEGER_ZERO;
                    rtnUrl = frontFailUrl;
                    break;
            }
        } else {
            rtnOK = Constants.INTEGER_ZERO;
            rtnUrl = frontFailUrl;
        }
        String result = "<result>" + rtnOK + "</result><redirecturl>" + rtnUrl + "</redirecturl>";
        logger.info("result: {}", result);
        responseData.setEntity(result);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return;
    }

    /**
     * Description: 退款申请是否成功查询
     * @author: JiuDongDong
     * @param startDate      退款开始时间，格式为YYYYMMDD 例如20100811  并且退款开始时间与退款结束时间在3天之内。
     * @param endDate        退款结束时间，格式为YYYYMMDD 例如20100811  20130314
     * @param refundSequence 12位退款订单号, 时间戳
     * @param rOrderId       原商家订单号
     * @param requestPage    请求记录集页码, 在查询结果数据总量很大时，快钱会将支付结果分多次返回。本参数表示商户需要得到的记录集页码。默认为1，表示第1 页。
     * @param status         交易状态：0代表进行中  1代表成功   2代表失败  可为空
     * @param merchantType   店铺类型：1自营 2非自营
     * date: 2018/8/2 14:41
     */
    @Override
    public void queryRefundOrder(ResponseData responseData, String startDate, String endDate, String refundSequence,
                                 String rOrderId, String requestPage, String status, String merchantType) {
        logger.info("It is now in Bill99ManagerImpl.queryRefundOrder, the parameters are: [startDate = {}, endDate = {}," +
                " refundSequence = {}, rOrderId = {}, requestPage = {}, status = {}, merchantType = {}]",
            startDate, endDate, refundSequence, rOrderId, requestPage, status, merchantType);
        // 获取配置信息
        String merchantAcctId = bill99PayConfigure.getMerchantAcctId();//人民币账号(存管)
        String merchantAcctIdNotHat = bill99PayConfigure.getMerchantAcctIdNotHat();//人民币账号(自营)
        String refundPssHat = bill99PayConfigure.getRefundPss();
        String refundPssNotHat = bill99PayConfigure.getRefundPssNotHat();
        // 根据店铺类型选择人民币账户和密码
        String merchantAcct;// 人民币账号
        String refundPss;// 退款查询密码
        if (Constants.MERCHANT_TYPE_SELF.equals(merchantType)) {
            merchantAcct = merchantAcctIdNotHat;
            refundPss = refundPssNotHat;
        } else {
            merchantAcct = merchantAcctId;
            refundPss = refundPssHat;
        }
        logger.info("merchantAcct = {}, refundPss = {}, orderId = {}", merchantAcct, refundPss, rOrderId);

        //签名字符串
        String signMsgVal = "";
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_VERSION, VERSION);//查询接口版本号固定值：v2.0注意为小写字母
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_SIGN_TYPE, SIGN_TYPE_1);//签名类型	数字串1 代表MD5 加密签名方式
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_MERCHANT_ACCT_ID, merchantAcct);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_START_DATE, startDate);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_END_DATE, endDate);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_ORDER_ID, refundSequence);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_REQUEST_PAGE, requestPage);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_R_ORDER_ID, rOrderId);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_STATUS, status);
        signMsgVal = refundQueryAppendParam(signMsgVal, Constants.BILL99_KEY, refundPss);
        String signMsg = "";
        try {
            signMsg = MD5Util.md5Hex(signMsgVal.getBytes(Constants.UTF_8)).toUpperCase();
//            signMsg = MD5Util.md5Hex(signMsgVal.getBytes("gb2312")).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            logger.error("MD5Util.md5Hex error for: " + signMsgVal);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
            return;
        }

        /* 2. 封装退款查询请求参数 */
        Map<String, String> params = new HashMap<>();
        params.put(Constants.BILL99_VERSION, VERSION);
        params.put(Constants.BILL99_SIGN_TYPE, SIGN_TYPE_1);
        params.put(Constants.BILL99_MERCHANT_ACCT_ID, merchantAcct);
        params.put(Constants.BILL99_START_DATE, startDate);
        params.put(Constants.BILL99_END_DATE, endDate);
        params.put(Constants.BILL99_ORDER_ID, StringUtils.isBlank(refundSequence) ? "" : refundSequence);
        params.put(Constants.BILL99_REQUEST_PAGE, requestPage);
        params.put(Constants.BILL99_R_ORDER_ID, rOrderId);
        params.put(Constants.BILL99_STATUS, status);
        params.put(Constants.BILL99_SIGN_MSG, signMsg);//加密串

        /* 2. 封装退款查询请求参数 */
        GatewayRefundQueryRequest queryRequestBean = new GatewayRefundQueryRequest();
        queryRequestBean.setVersion(VERSION);
        queryRequestBean.setSignType(SIGN_TYPE_1);
        queryRequestBean.setMerchantAcctId(merchantAcct);
        queryRequestBean.setStartDate(startDate);
        queryRequestBean.setEndDate(endDate);
        queryRequestBean.setOrderId(refundSequence);
        queryRequestBean.setRequestPage(requestPage);
        queryRequestBean.setROrderId(rOrderId);
        queryRequestBean.setStatus(status);
        queryRequestBean.setSignMsg(signMsg);

        /* 3. 退款查询 */
        int recordCount = Constants.INTEGER_ZERO; // 查询结果总数
        int pageCount = -Constants.INTEGER_ONE; // 总页数
        String currentPageStr = -Constants.INTEGER_ONE + ""; // 记录集当前页码
        int pageSize; // 当前页记录条数
        List<GatewayRefundQueryResultDto> refundResultDtoList = new ArrayList<>();//结果对象集合
        //  while (pageCount == -1 || recordCount != refundResultDtoList.size()) {
        logger.info("pageCount = {}, recordCount = {}, refundResultDtoList.size = {}", pageCount, recordCount, refundResultDtoList.size());
        try {
            queryRefundByPage(queryRequestBean, recordCount, pageCount, currentPageStr, refundResultDtoList, refundPss);
        } catch (RemoteException e) {
            logger.error("GatewayRefundQueryServiceLocator.getgatewayRefundQuery() occurred RemoteException: ", e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
            return;
        } catch (ServiceException e) {
            logger.error("GatewayRefundQueryServiceLocator.getgatewayRefundQuery() occurred ServiceException: " + e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
            return;
        } catch (UnsupportedEncodingException e) {
            logger.error("GatewayRefundQueryServiceLocator.getgatewayRefundQuery() occurred UnsupportedEncodingException: " + e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
            return;
        } catch (Exception e) {
            logger.error("GatewayRefundQueryServiceLocator.getgatewayRefundQuery() occurred Exception: " + e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
            return;
        }
//        }
        logger.info("refundResultDtoList: {}", JsonUtil.toJson(refundResultDtoList));
        /* 5. 响应查询结果 */
        responseData.setEntity(refundResultDtoList);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * Description: 退款查询
     * @author: JiuDongDong
     * @param queryRequestBean 封装查询参数
     * @param recordCount      记录总条数
     * @param pageCount        总页数
     * @param currentPage      当前页
     * @param key              密钥
     * @return results  结果对象集合
     * date: 2018/8/7 14:51
     */
    private List<GatewayRefundQueryResultDto> queryRefundByPage(
        GatewayRefundQueryRequest queryRequestBean, int recordCount, int pageCount, String currentPage,
        List<GatewayRefundQueryResultDto> refundResultDtoList, String key)
        throws Exception {
        String refundWebServiceUrl = bill99PayConfigure.getRefundWebServiceUrl();
        GatewayRefundQueryServiceLocator locator = new GatewayRefundQueryServiceLocator(refundWebServiceUrl);
        logger.info("locator: {}", locator);
        GatewayRefundQueryResponse queryResponse = locator.getgatewayRefundQuery().query(queryRequestBean);
        logger.info("queryResponse: {}", JsonUtil.toJson(queryResponse));
        /* 验签字符串 */
        String rSignMsgVal = "";
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_VERSION, queryResponse.getVersion());
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_SIGN_TYPE, queryResponse.getSignType());
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_MERCHANT_ACCT_ID, queryResponse.getMerchantAcctId());
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_RECORD_COUNT, String.valueOf(queryResponse.getRecordCount()));
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_PAGE_COUNT, String.valueOf(queryResponse.getPageCount()));
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_CURRENT_PAGE, queryResponse.getCurrentPage());
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_PAGE_SIZE, String.valueOf(queryResponse.getPageSize()));
        rSignMsgVal = refundQueryResponseAppendParam(rSignMsgVal, Constants.BILL99_KEY, key);

        String rsignMsg2 = MD5Util.md5Hex(rSignMsgVal.getBytes(Constants.BILL99_GB2312)).toUpperCase();
        String rsignMsg = queryResponse.getSignMsg();
        logger.info("RsignMsg : {}", rsignMsg);
        logger.info("RsignMsg2: {}", rsignMsg2);
        if (!rsignMsg.equals(rsignMsg2)) {
            logger.error("Verify signData of Bill99 not equals");
            throw new Exception("verify signature Exception");
        }
        String errCode = queryResponse.getErrCode();
        Bill99LoggerByResCodeAndTransType.logInfo(errCode);
        if (StringUtils.isNotBlank(errCode)) {
            if (NO_RESULTS.equals(errCode)) {
                logger.info(errCode);
                logger.warn("There is no result of queryRequestBean: {}", queryRequestBean.getOrderId());
                return refundResultDtoList;
            }
            logger.error(errCode);
            throw new Exception(errCode);
        }
        recordCount += queryResponse.getRecordCount();
        currentPage = queryResponse.getCurrentPage();
        pageCount = queryResponse.getPageCount();
        logger.info("recordCount = {}, currentPage = {}, pageCount = {}", recordCount, currentPage, pageCount);
        GatewayRefundQueryResultDto[] results1 = queryResponse.getResults();
        logger.info("current queryResponse.getResults().length = {}", results1.length);
        for (GatewayRefundQueryResultDto dto : results1) {
            refundResultDtoList.add(dto);
        }
        logger.info("refundResultDtoList = {}", (CollectionUtils.isEmpty(refundResultDtoList) ? "" : JsonUtil.toJson(refundResultDtoList)));
        return refundResultDtoList;
    }

    /**
     * Description: 根据退款订单号查询Redis中的退款信息
     * @author: JiuDongDong
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * date: 2018/8/15 15:19
     */
    @Override
    public void getRefundOrderInfoFromRedis(ResponseData responseData, String outRequestNo) {
        logger.info("It is now in Bill99ManagerImpl.getRefundOrderInfoFromRedis, the parameters is: [outRequestNo = {}]", outRequestNo);
        RefundInfoVo refundOrderInfoFromRedis = bill99OrderRedisService.getRefundOrderInfoFromRedis(outRequestNo, Constants.PAY_BILL99_REFUND_INFO);
        responseData.setEntity(refundOrderInfoFromRedis);
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setCode(ResponseStatus.OK.getValue());
    }

    /**
     * description:给公司店铺开子商户(根据是否为公司开店来判断)
     *
     * @param responseData
     * @param shop
     * @throws Exception
     * @author wangziyuan
     */
    @Override
    public void bindAccountOfCompany(ResponseData responseData, Map<String, String> shop) throws Exception {
        logger.info("the shop param is ------>{}", ItvJsonUtil.toJson(shop));
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String domainName = bill99PayConfigure.getDomainName();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("uId", shop.get("uId"));//店铺id
        msgMap.put("name", shop.get("name"));//店铺名称
        msgMap.put("mobile", shop.get("mobile"));
        msgMap.put("email", shop.get("email"));
        msgMap.put("idCardType", shop.get("idCardType"));
        msgMap.put("idCardNumber", shop.get("idCardNumber"));
        msgMap.put("address", shop.get("address"));
        msgMap.put("registDate", shop.get("registDate"));
        msgMap.put("legalName", shop.get("legalName"));
        msgMap.put("legalId", shop.get("legalId"));
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        Map<String, Object> post = bindHttpDeal.post(domainName + "/merchant/register", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isNotEmpty(content)) {
            logger.info("the post is not empty!");
            Map<String, String> map = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            if (map.get("rspCode").equals("0000") || map.get("rspCode").equals("5008")) {
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("open account success!!");
                responseData.setEntity(map);
            } else {
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("there have an outException!");
                logger.info("the have an out error of 99bill !!!!");
            }
        } else {
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("there have an outException!");
            logger.info("the have an http error!!");
        }
    }

    /**
     * description:给个体户店铺开子账户(根据是否为公司开店来判断)
     *
     * @param responseData
     * @param shop
     * @throws Exception
     * @author wangziyuan
     */
    @Override
    public void bindAccountOfPerson(ResponseData responseData, Map<String, String> shop) throws Exception {
        logger.info("the shop param is ------>{}", ItvJsonUtil.toJson(shop));
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("uId", shop.get("uId"));
        msgMap.put("idCardType", shop.get("idCardType"));
        msgMap.put("idCardNumber", shop.get("idCardNumber"));
        msgMap.put("name", shop.get("name"));
        msgMap.put("mobile", shop.get("mobile"));
        msgMap.put("email", shop.get("email"));
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        Map<String, Object> post = bindHttpDeal.post(domainName + "/person/register", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        String content = String.valueOf(post.get("content"));
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isNotEmpty(content) && verify) {
            logger.info("the post is not empty!");
            Map<String, String> map = ItvJsonUtil.jsonToObj(content, new TypeReference<Map<String, String>>() {
            });
            if (map.get("rspCode").equals("0000") || map.get("rspCode").equals("5008")) {
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("open account success!!");
                responseData.setEntity(map);
            } else {
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("there have an outException!");
                logger.info("the have an out error of 99bill !!!!");
            }
        } else {
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("there have an outException!");
            logger.info("the have an http error!!");
        }
    }

    @Override
    public void addBankAccountByShop(ResponseData responseData, BankAccountVo bankAccount, String code) throws Exception {
        String bankCode = bankAccountRedisService.getBankCode(bankAccount.getUid());
        if (code.equals(bankCode)) {
            String post = null;
            if (bankAccount.getIsPerson() == 1) {
                post = personBankcard(bankAccount);
            }
            if (bankAccount.getIsPerson() == 0) {
                post = merchantBind(bankAccount);
            }
            if (StringUtils.isBlank(post)) {
                responseData.setCode(ResponseStatus.ERR.getValue());
            } else {
                Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
                });
                String rspCode = stringStringMap.get(RSPCODE);
                String rspMsg = stringStringMap.get(RSPMSG);
                if (rspCode.equals(Constants.BILL99_RSPCODE_0000) || rspCode.equals(Constants.BILL99_RSPCODE_5010)) {
                    bankAccountService.addBankAccount(bankAccount);
                    responseData.setCode(ResponseStatus.OK.getValue());
                    responseData.setMsg(ResponseStatus.OK.name());
                } else {
                    responseData.setCode(rspCode);
                    responseData.setMsg(rspMsg);
                }
            }
        } else {
            responseData.setCode(ResponseStatus.CODEERR.getValue());
            responseData.setMsg(ResponseStatus.CODEERR.name());
        }
    }

    @Override
    public void updateBankAccountByShop(ResponseData responseData, BankAccountVo bankAccount, String code) throws Exception {
        String bankCode = bankAccountRedisService.getBankCode(bankAccount.getUid());
        if (code.equals(bankCode)) {
            String post = null;
            if (bankAccount.getIsPerson() == 1) {
                post = updatePersonBankcard(bankAccount);
            }
            if (bankAccount.getIsPerson() == 0) {
                post = updateMerchantBankcard(bankAccount);
            }
            if (StringUtils.isBlank(post)) {
                responseData.setCode(ResponseStatus.ERR.getValue());
            } else {
                Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
                });
                String rspCode = stringStringMap.get(RSPCODE);
                String rspMsg = stringStringMap.get(RSPMSG);
                if (rspCode.equals(Constants.BILL99_RSPCODE_0000)) {
                    bankAccountService.updateBankAccountById(bankAccount);
                    responseData.setCode(ResponseStatus.OK.getValue());
                    responseData.setMsg(ResponseStatus.OK.name());
                } else {
                    responseData.setCode(rspCode);
                    responseData.setMsg(rspMsg);
                }
            }
        } else {
            responseData.setCode(ResponseStatus.CODEERR.getValue());
            responseData.setMsg(ResponseStatus.CODEERR.name());
        }
    }

    public String personBankcard(BankAccountVo bankAccount) throws Exception {
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        HashMap<String, String> msgMap = new HashMap<>();

        msgMap.put("uId", bankAccount.getUserId().toString());//用户id
        msgMap.put("platformCode", platformCode);//商户号
        msgMap.put("bankAcctId", bankAccount.getCardCode());//银行卡号
        msgMap.put("mobile", bankAccount.getMobilePhone());
        msgMap.put("secondAcct", bankAccount.getSecondAcct().toString());
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/person/bankcard/bind", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isBlank(content)) {
            logger.info("the result of account withdraw request content is empty");
            return null;
        }
        return content;
    }

    public String merchantBind(BankAccountVo bankAccount) throws Exception {
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        HashMap<String, String> msgMap = new HashMap<>();

        msgMap.put("uId", bankAccount.getUserId().toString());//用户id如是对公账户则是店铺id
        msgMap.put("platformCode", platformCode);//商户号
        msgMap.put("bankId", bankAccount.getBankLogo());//
        msgMap.put("bankAcctId", bankAccount.getCardCode());//
        msgMap.put("name", bankAccount.getName());
        msgMap.put("mobile", bankAccount.getMobilePhone());
        msgMap.put("bankName", bankAccount.getBankName());
        msgMap.put("accountType", bankAccount.getAccountType().toString());
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/merchant/bankcard/bind", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isBlank(content)) {
            logger.info("the result of account withdraw request content is empty");
            return null;
        }
        return content;
    }

    public String updatePersonBankcard(BankAccountVo bankAccount) throws Exception {
        // 1.获取配置信息
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        HashMap<String, String> msgMap = new HashMap<>();

        msgMap.put("uId", bankAccount.getUserId().toString());
        msgMap.put("platformCode", platformCode);
        msgMap.put("bankAcctId", bankAccount.getCardCode());
        msgMap.put("mobile", bankAccount.getMobilePhone());
        msgMap.put("secondAcct", bankAccount.getSecondAcct().toString());

        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
       /* BindHttpDeal bindHttpDeal = new BindHttpDeal();*/
        logger.info("to post  http--------->");
        Map<String, Object> post = bindHttpDeal.post(domainName + "/person/bankcard/rebind", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isBlank(content)) {
            logger.info("the result of account withdraw request content is empty");
            return null;
        }
        return content;
    }


    public String updateMerchantBankcard(BankAccountVo bankAccount) throws Exception {
        String merId = bill99PayConfigure.getMerId();
        String merchantCertPath = bill99PayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99PayConfigure.getMerchantCertPss();
        String hatPrivateKey = bill99PayConfigure.getHatPrivateKey();
        String hatPublicKey = bill99PayConfigure.getHatPublicKey();
        String domainName = bill99PayConfigure.getDomainName();
        String platformCode = bill99PayConfigure.getPlatformCode();
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("uId", bankAccount.getUserId().toString());
        msgMap.put("platformCode", platformCode);
        msgMap.put("bankId", bankAccount.getBankLogo());
        msgMap.put("bankAcctId", bankAccount.getCardCode());
        msgMap.put("name", bankAccount.getName());
        msgMap.put("mobile", bankAccount.getMobilePhone());
        msgMap.put("bankName", bankAccount.getBankName());
        msgMap.put("accountType", bankAccount.getAccountType().toString());
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        Map<String, Object> post = bindHttpDeal.post(domainName + "/merchant/bankcard/rebind", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the msg is -------->{}", post);
        String content = String.valueOf(post.get("content"));
        CloseableHttpResponse response1 = (CloseableHttpResponse) post.get("response");
        boolean verify = finderSignService.verify(response1, content, hatPublicKey);
        logger.info("the res is --------------------->{}", verify);
        if (StringUtils.isBlank(content)) {
            logger.info("the result of account withdraw request content is empty");
            return null;
        }
        return content;
    }

    /**
     * Description: 聚合签名数据(发送订单支付请求时加签用)
     * @author: JiuDongDong
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/7/31 17:47
     */
    private String appendParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != "") {
                returns += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (paramValue != "") {
                returns = paramId + "=" + paramValue;
            }
        }
        return returns;
    }

    /**
     * Description: 聚合签名数据(订单支付支付应答时验签用)
     * @author: JiuDongDong
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/8/1 17:03
     */
    private String verifyAppendParam(String returns, String paramId, String paramValue) {
        if (!returns.equals("")) {
            if (!paramValue.equals("")) {
                returns += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (!paramValue.equals("")) {
                returns = paramId + "=" + paramValue;
            }
        }
        return returns;
    }

    /**
     * Description: 退款请求时加签用
     * @author: JiuDongDong
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/8/9 16:34
     */
    private String refundQueryAppendParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != null && !paramValue.equals("")) {
                returns += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (paramValue != null && !paramValue.equals("")) {
                returns = paramId + "=" + paramValue;
            }
        }

        return returns;
    }

    /**
     * Description: 退款响应时验签用
     * @author: JiuDongDong
     * @param returns    返回值
     * @param paramId    参数key
     * @param paramValue 参数value
     * @return java.lang.String 聚合的数据
     * date: 2018/8/9 16:37
     */
    private String refundQueryResponseAppendParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != null && !paramValue.equals("")) {
                returns += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (paramValue != null && !paramValue.equals("")) {
                returns = paramId + "=" + paramValue;
            }
        }
        return returns;
    }
}
