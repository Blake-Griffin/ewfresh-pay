package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.configure.Bill99SharePayConfigure;
import com.ewfresh.pay.manager.Bill99ShareManager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.bill99share.Bill99ShareDetail;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.redisService.BankAccountRedisService;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bill99.Pkipair;
import com.ewfresh.pay.util.bill99share.Bill99AppendParamUtil;
import com.ewfresh.pay.util.bill99share.CreateShareDetailUtil;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayh5pay.IfShareBenefit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: Bill99订单业务的逻辑处理层
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
@Component
public class Bill99ShareManagerImpl implements Bill99ShareManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99SharePayConfigure bill99SharePayConfigure;
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private OrderRedisService orderRedisService;
    @Autowired
    private BankAccountRedisService bankAccountRedisService;
    @Autowired
    private GetOrderInfoFromRedisUtil getOrderInfoFromRedisUtil;
    @Autowired
    private GetBenefitFateUtil getBenefitFateUtil;

    private static final String INPUT_CHAR_SET = "1";//编码方式，1代表 UTF-8; 2 代表 GBK; 3代表 GB2312 默认为1,该参数必填。
    private static final String VERSION = "v2.0";//网关版本，固定值：v2.0,该参数必填。
    private static final String LANGUAGE = "1";//语言种类，1代表中文显示，2代表英文显示。默认为1,该参数必填。
    private static final String SIGN_TYPE_4 = "4";//签名类型,该值为4，代表PKI加密方式,该参数必填。
    private static final String PAYEE_CONTACT_TYPE = "1";//1 代表 Email 地址。
//
//    private static final String EXT1 = "";//扩展字段1，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
//    private static final String EXT2 = "";//扩展自段2，商户可以传递自己需要的参数，支付完快钱会原值返回，可以为空。
//    private static final String PAY_TYPE = "00";//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
//    private static final String REDO_FLAG = "0";//同一订单禁止重复提交标志，实物购物车填1，虚拟产品用0。1代表只能提交一次，0代表在支付不成功情况下可以再提交。可为空。
//    private static final String KUAIQIAN = "快钱";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private static final String NO_RESULTS = "10012";
//    private static final String POINT = "100";//以分为单位
//    private static final String CREATE_TIME = "createTime";
//    private static final String CURRENT_TIME = "currentTime";
//    private static final String RSPCODE =  "rspCode";
//    private static final String RSPMSG = "rspMsg";

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
     * date: 2019/8/7 15:47
     */
    @Override
    public void sendOrder(ResponseData responseData, String payerName, String payerContact, String orderNo, String orderAmount, String bankId, String payType, String payerContactType, String orderIp, String payerIdType, String payerId) throws ShouldPayNotEqualsException {
        logger.info("It is now in Bill99ShareManagerImpl.sendOrder, the parameters are: [payerName = {}, payerContact = {}, orderNo = {}, " +
                        "orderAmount = {}, bankId = {}, payType = {}, payerContactType = {}, orderIp = {}, payerIdType = {}, payerId = {}]",
                payerName, payerContact, orderNo, orderAmount, bankId, payType, payerContactType, orderIp, payerIdType, payerId);
        /* 1.获取配置信息 */
        String payeeContact = bill99SharePayConfigure.getPayeeContact();
        String bgUrl = bill99SharePayConfigure.getBgUrl();
        String merchantCertPath = bill99SharePayConfigure.getMerchantCertPath();
        String merchantCertPss = bill99SharePayConfigure.getMerchantCertPss();
        String pid = bill99SharePayConfigure.getPid();
        String payUrl = bill99SharePayConfigure.getPayUrl();

        /* 1.1 根据自营和非自营选择人民币账户 */
        Map<String, String> redisParam = getOrderInfoFromRedisUtil.getOrderInfoFromRedis(orderNo);
        String shopIdRedis = redisParam.get(SHOP_ID);// 店铺id（实物订单支付，即自营和店铺商品的订单支付时，会传shopId）
        String isRecharge = redisParam.get(IS_RECHARGE);// 是否充值（实物订单支付时，传0进来，自营充值时，传4进来，白条还款传15）
        logger.info("The shopIdRedis is: {}, isRecharge = {}", shopIdRedis, isRecharge);
        /* 1.2 校验订单支付金额是否正确 */
        String shouldPayMoney = redisParam.get(SURPLUS);
        if (new BigDecimal(orderAmount).compareTo(new BigDecimal(shouldPayMoney)) != INTEGER_ZERO) {
            logger.error("Web should pay money not equals redis, shouldPayMoney from redis = " + shouldPayMoney +
                    ", web orderAmount = " + orderAmount);
            throw new ShouldPayNotEqualsException("Web should pay money not equals redis, shouldPayMoney " +
                    "from redis = " + orderAmount + ", web amount = " + shouldPayMoney);
        }

        /* 1.2 由于订单一次全款支付、定金支付有30分钟时间限制(30分钟取消订单)、尾款支付时虽然没有支付时间限制，但是支付信息只放入Redis30分钟，所以在支付时需校验时间是否超时 */
        String orderTimeOut;//距离支付截止还剩的时间段，以秒为单位
        String orderStatus = redisParam.get(ORDER_STATUS);// 1100为下单状态，1200为支付定金状态，这时需校验30分钟有效期
        // 拦截计算的基准时间
        String baseTime = "";
        // 1.2.1 下单状态时，选取"下单时间"推算超时拦截时间
        if (ORDER_WAIT_PAY.intValue() == Integer.valueOf(orderStatus)) {
            String createTimeStr = redisParam.get(CREATE_TIME);
            baseTime = createTimeStr;
            logger.info("baseTime = {}", DateUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(baseTime))));
        }
        // 1.2.2 订单尾款支付时，则以用户选中银行，点击下一步时放入Redis的“current时间”作为基准计算订单支付超时拦截时间
        if (ORDER_PAID_EARNEST.intValue() == Integer.valueOf(orderStatus)) {
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
            Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -INTEGER_THIRTY);
//                Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -0);
            // 如果截止时间在当前时间之前，则必需拦截此支付
            if (futureMountSecondsStart.getTime() < System.currentTimeMillis()) {
                logger.error("futureMountSecondsStart.getTime() < System.currentTimeMillis(), " +
                        "futureMountSecondsStart.getTime() = " + futureMountSecondsStart.getTime() + ", " +
                        "System.currentTimeMillis() = " + System.currentTimeMillis() + ", orderNo = " + orderNo);
                responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
                responseData.setMsg(ORDERTIMEOUT);// 本次支付已超时！
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

        // 支付总金额 = 易网聚鲜分润 + 银联通道运费 + 店铺分润
        String ewfreshBenefit = redisParam.get(BANK_EWFRESH_BENEFIT);//使用第三方支付通道支付,ewfresh所得分润
        String shopBenefit = redisParam.get(BANK_SHOP_BENEFIT);//使用第三方支付通道支付,shop所得分润
        String freight = redisParam.get(FREIGHT);//使用第三方支付通道支付的运费
        freight = StringUtils.isBlank(freight) ? STR_ZERO : freight;

        // 签名
        String signMsgVal = "";
        Map<String, String> reqParam = new HashMap<>();
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_INPUT_CHARSET, INPUT_CHAR_SET);
        reqParam.put(BILL99_INPUT_CHARSET, INPUT_CHAR_SET);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_BG_URL, bgUrl);
        reqParam.put(BILL99_BG_URL, bgUrl);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_VERSION, VERSION);
        reqParam.put(BILL99_VERSION, VERSION);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_LANGUAGE, LANGUAGE);
        reqParam.put(BILL99_LANGUAGE, LANGUAGE);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_SIGN_TYPE, SIGN_TYPE_4);
        reqParam.put(BILL99_SIGN_TYPE, SIGN_TYPE_4);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_PAYEE_CONTACT_TYPE, PAYEE_CONTACT_TYPE);
        reqParam.put(BILL99_PAYEE_CONTACT_TYPE, PAYEE_CONTACT_TYPE);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_PAYEE_CONTACT, payeeContact);
        reqParam.put(BILL99_PAYEE_CONTACT, payeeContact);
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "payTolerance", "");
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_ORDER_ID, orderNo);
        reqParam.put(BILL99_ORDER_ID, orderNo);
        //订单金额由元转为分
        String orderAmountFen = FenYuanConvert.yuan2Fen(orderAmount).toString();
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_ORDER_AMOUNT, orderAmountFen);
        reqParam.put(BILL99_ORDER_AMOUNT, orderAmountFen);
        //主收款方应收金额 = ewfreshBenefit + freight
        BigDecimal payeeAmount = new BigDecimal(ewfreshBenefit).add(new BigDecimal(freight));
        String payeeAmountStr = FenYuanConvert.yuan2Fen(payeeAmount.toString()).toString();
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "payeeAmount", payeeAmountStr);
        reqParam.put("payeeAmount", payeeAmountStr);
        String format = sdf.format(new Date());
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_ORDER_TIME, format);
        reqParam.put(BILL99_ORDER_TIME, format);
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "productName", productName);
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "productNum", productNum);
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_PRODUCT_DESC, "");
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "ext1", ext1);
        //signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "ext2", ext2);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_PAY_TYPE, payType);
        reqParam.put(BILL99_PAY_TYPE, payType);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_BANK_ID, bankId);
        reqParam.put(BILL99_BANK_ID, bankId);
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_PID, pid);
        reqParam.put(BILL99_PID, pid);
        if (StringUtils.isNotBlank(orderTimeOut)) {
            signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, BILL99_ORDER_TIME_OUT, orderTimeOut);
            reqParam.put(BILL99_ORDER_TIME_OUT, orderTimeOut);
        }

        // 创建分账信息
        List<Bill99ShareDetail> shareDetailList = new ArrayList<>();
        // 2019年8月13日 杰总确认，运费入账到自营账户 start
        // 运费分账
        //Bill99ShareDetail shareDetailFreight = new Bill99ShareDetail();
        //shareDetailFreight.setSharingContactType("1");
        //shareDetailFreight.setSharingContact("线上运费店铺的Email");
        //shareDetailFreight.setSharingApplyAmount(FenYuanConvert.yuan2Fen(freight).toString());
        //shareDetailFreight.setSharingFeeRate("0");
        //shareDetailFreight.setSharingDesc("freight");
        //shareDetailList.add(shareDetailFreight);
        // 2019年8月13日 杰总确认，运费入账到自营账户 end

        // 店铺分账
        boolean divisionFlag = IfShareBenefit.ifShareBenefit(shopIdRedis, isRecharge);//是否分账
        if (divisionFlag) {
            Bill99ShareDetail shareDetailShop = new Bill99ShareDetail();
            shareDetailShop.setSharingContactType("1");
            String shopEmail = getBenefitFateUtil.getEmail(shopIdRedis);
            shareDetailShop.setSharingContact(shopEmail);
            shareDetailShop.setSharingApplyAmount(FenYuanConvert.yuan2Fen(shopBenefit).toString());
            shareDetailShop.setSharingFeeRate("0");
            shareDetailShop.setSharingDesc("shop");
            shareDetailList.add(shareDetailShop);
        }
        String shareDetail = CreateShareDetailUtil.createShareDetail(shareDetailList);
        if (StringUtils.isNotBlank(shareDetail)) {
            signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "sharingData", shareDetail);
            reqParam.put("sharingData", shareDetail);
        }
        signMsgVal = Bill99AppendParamUtil.appendParam(signMsgVal, "sharingPayFlag", "1");
        reqParam.put("sharingPayFlag", "1");
        //String signMsg = MD5Util.md5Hex(signMsgVal.getBytes("gb2312")).toUpperCase();
        logger.info("signMsgVal: {}", signMsgVal);
        Pkipair pki = new Pkipair();
        String signMsg = pki.signMsg(signMsgVal, merchantCertPath, merchantCertPss);
        reqParam.put("signMsg", signMsg);

        String autoFormHtml = CreateAutoFormHtmlUtil.createAutoFormHtml(payUrl, reqParam, UTF_8, HTTP_METHOD_POST);
        logger.info("The send order parameters have been packaged ok and will send them back to html");
        responseData.setEntity(autoFormHtml);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }
}
