package com.ewfresh.pay.policy.impl;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.policy.RefundPolicy;
import com.ewfresh.pay.redisService.UnionPayWebWapOrderRedisService;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.DateUtil;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.bob.BOBRefundSeqFormat;
import com.ewfresh.pay.util.unionpayb2cwebwap.AcpService;
import com.ewfresh.pay.util.unionpayb2cwebwap.SDKConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;

/**
 * description: 中国银联WebWap退款策略
 * @author: JiuDongDong
 * date: 2019/5/6.
 */
@Component
public class UnionPayWebWapRefundPolicy implements RefundPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SDKConfig sdkConfig;
    @Autowired
    private PayFlowService payFlowService;
    @Autowired
    private UnionPayWebWapOrderRedisService unionPayWebWapOrderRedisService;
    @Value("${bill99.merchantId}")
    private String strMerchantId;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public List<PayFlow> refund(RefundParam refundParam) throws RefundParamNullException, PayFlowFoundNullException, UnsupportedEncodingException, RefundHttpToBill99FailedException, RefundBill99ResponseNullException, DocumentException, RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, VerifyUnionPaySignatureException, UnionPayHandleRefundException {
        logger.info("Refund by UnionPayWebWap START, the params = {}", JsonUtil.toJson(refundParam));
        String outTradeNo = refundParam.getOutTradeNo();// 商户订单号(与第三方交互交易的流水号对应表中的interaction_id字段加E加R的订单号)
        /* 1. 校验退款请求参数 */
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）
        String refundAmount = refundParam.getRefundAmount();// 退款金额
        String totalAmount = refundParam.getTotalAmount();// 订单金额
        String outRequestNo = refundParam.getOutRequestNo();// 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
        String orderNo = refundParam.getOrderNo();// 父订单号
        if (StringUtils.isBlank(tradeNo) || StringUtils.isBlank(refundAmount) || StringUtils.isBlank(outRequestNo)
                || StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(orderNo)) {
            logger.error("The param has empty part, tradeNo = " + tradeNo + ", refundAmount = " + refundAmount + ", outRequestNo = " +
                    outRequestNo + ", outTradeNo = " + outTradeNo + ", orderNo = " + orderNo);
            throw new RefundParamNullException("The params tradeNo or refundAmount or outRequestNo or outTradeNo or orderNo is empty");
        }
        // 1.1 根据支付渠道流水号获取部分退款信息
        PayFlow payFlow = payFlowService.getPayFlowPartById(tradeNo);
        if (payFlow == null) {
            logger.error("There is no this payFlowId: " + tradeNo);
            throw new PayFlowFoundNullException("There is no this payFlowId: " + tradeNo);
        }
        String receiverUserId = payFlow.getReceiverUserId();//店铺id
        Date successTime = payFlow.getSuccessTime();//支付交易成功时间

        /* 2. 封装退款请求参数 */
        Map<String, String> params = new HashMap<>();
        // 2.1 从sdkConfig获取部分配置信息
        String merId = sdkConfig.getMerId();// 商户号 TODO 待区分是自营还是店铺
        String version = sdkConfig.getVersion();//版本号
        String encoding = sdkConfig.getEncoding();//字符集编码 可以使用UTF-8,GBK两种方式
        String signMethod = sdkConfig.getSignMethod();//签名方法
        // TODO 退货请求，不用回调功能，主动查询代替
        String backRefundUrl = sdkConfig.getBackRefundUrl();//退货的回调url
        backRefundUrl = "";
        String backRequestUrl = sdkConfig.getBackRequestUrl();
        String backTransUrl = sdkConfig.getBackTransUrl();//

        String origQryId = tradeNo;// TODO 校验正确性
        String txnAmt = refundAmount;// 退款金额
        String refundSequence = BOBRefundSeqFormat.orderNo2UnionPayRefundSequence(outRequestNo);// 生成32位的退款流水号
        Date refundTimeDate = new Date();// 商户发送退款请求的时间
        String refundTime = sdf.format(refundTimeDate);

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        params.put(param_version, version);                        //版本号
        params.put(param_encoding, encoding);                      //字符集编码 可以使用UTF-8,GBK两种方式
        params.put(param_signMethod, signMethod);                  //签名方法
        // 选择是退货交易还是撤销交易
        String txnType = getTxnType(successTime, totalAmount, refundAmount);
        params.put(param_txnType, txnType);                        //交易类型 04-退货 31-消费撤销
        params.put(param_txnSubType, "00");                        //交易子类型  默认00
        params.put(param_bizType, "000201");                       //业务类型 B2C网关支付，手机wap支付
        params.put(param_channelType, "07");                       //渠道类型，07-PC，08-手机

        /***商户接入参数***/
        params.put(param_merId, merId);                 //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        params.put(param_accessType, "0");              //接入类型，商户接入固定填0，不需修改
        params.put(param_orderId, refundSequence);      //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        params.put(param_txnTime, refundTime);          //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        params.put(param_currencyCode, "156");          //交易币种（境内商户一般是156 人民币）
        params.put(param_txnAmt, txnAmt);               //****退货金额，单位分，不要带小数点。退货金额小于等于原消费金额，当小于的时候可以多次退货至退货累计金额等于原消费金额
        //params.put("reqReserved", "透传信息");         //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        params.put(param_backUrl, backRefundUrl);       //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 退货交易 商户通知,其他说明同消费交易的后台通知

        /***要调通交易以下字段必须修改***/
        params.put(param_origQryId, origQryId);         //原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/
        Map<String, String> reqData  = AcpService.sign(params, encoding);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = backTransUrl;//交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        // 发送退货请求
        Map<String, String> rspData = AcpService.post(reqData, url, encoding);//这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        logger.info("The response data of refund outTradeNo: " + outTradeNo + " is: " + JsonUtil.toJson(rspData));
        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(MapUtils.isEmpty(rspData)){
            logger.error("The response data is empty for UnionPayWebWapRefundPolicy, outTradeNo = " + outTradeNo);
            logger.error("Connection is timeout for refund: " + outTradeNo);
            // 网络异常，请稍后重试
            throw new HttpToUnionPayFailedException("Connection is timeout for refund: " + outTradeNo);
        }
        // 验签
        boolean validate = AcpService.validate(rspData, encoding);
        if (!validate) {
            logger.error("Verify signature for UnionPayWebWapRefundPolicy response not pass!!! outTradeNo = " + outTradeNo);
            logger.info("The response data of refund outTradeNo: " + outTradeNo + " is: " + JsonUtil.toJson(rspData));
            // 校验中国银联签名异常
            throw new VerifyUnionPaySignatureException("Verify signature for UnionPayWebWapRefundPolicy Exception");
        }
        logger.info("Verify signature success!!! outTradeNo = {}", outTradeNo);
        logger.info("The response data of UnionPayWebWapRefundPolicy of outTradeNo: {} is: {}", outTradeNo, JsonUtil.toJson(rspData));
        // 应答码
        String respCode = rspData.get(param_respCode);
        if (!RESPONSE_CODE_OK.equals(respCode)) {
            logger.error("UnionPay handle failed, outTradeNo = " + outTradeNo + ", respCode = " + respCode);
            logger.error("The response data of refund outTradeNo: " + outTradeNo + " is: " + JsonUtil.toJson(rspData));
            // 中国银联处理退款失败
            throw new UnionPayHandleRefundException("UnionPayWebWapRefundPolicy handle refund Exception");
        }
        logger.info("Apply unionpay refund success, the final result will be handled in confirm worker, outTradeNo = {}", outTradeNo);

        /* 5. 封装PayFlow信息 */
        // 5.1 设置支付流水信息
        payFlow.setOrderId(Long.valueOf(orderNo));
        payFlow.setChannelFlowId(refundSequence);// 上面生成32位的退款流水号
        payFlow.setPayerPayAmount(new BigDecimal(refundAmount));//付款方支付金额---退款金额
        payFlow.setTradeType(TRADE_TYPE_2);//交易类型：退款
        payFlow.setReceiverFee(BigDecimal.ZERO);//手续费为0
        payFlow.setDesp(REFUND_ORDER);//订单描述：退款
        payFlow.setIsRefund(SHORT_ONE);// 是否退款
        payFlow.setStatus(STATUS_2);// 申请成功，尚未确定是否退款成功，数据库：状态 0:成功,1:失败
        // 收款人id和付款人id对调、收款人名称和付款人名称对调
        String payerId = payFlow.getPayerId();//付款人id
        String payerName = payFlow.getPayerName();//付款人名称
        String receiverName = payFlow.getReceiverName();//收款人名称
        payFlow.setPayerId(receiverUserId);
        payFlow.setReceiverUserId(payerId);
        payFlow.setPayerName(receiverName);
        payFlow.setReceiverName(payerName);
        // 收款账号类型(1个人,2店铺)  和 付款账号类型(1个人,2店铺)   对调
        Short payerType = payFlow.getPayerType();
        Short receiverType = payFlow.getReceiverType();
        payFlow.setPayerType(receiverType);
        payFlow.setReceiverType(payerType);
        // 退款申请时间
        payFlow.setSuccessTime(refundTimeDate);
        logger.info("Refund order apply success, the orderNo = {}", outTradeNo);
        // 5.3 退款信息放入Redis
        RefundInfoVo refundInfoVo = new RefundInfoVo();
        refundInfoVo.setRefundParam(refundParam);
        refundInfoVo.setRefundSeq(refundSequence);// 上面生成的32位的退款流水号
        refundInfoVo.setRefundTime(refundTime);// 退款申请时间
        unionPayWebWapOrderRedisService.putRefundOrderInfoToRedis(refundInfoVo, PAY_UNIONPAYWEBWAP_REFUND_INFO);

        /* 6. 返回PayFlow对象*/
        ArrayList<PayFlow> refund = new ArrayList<>();
        refund.add(payFlow);
        return refund;
    }

    /**
     * Description: 选择消费撤销还是退款。消费撤销仅能对当天的消费做，必须为全额，一般当日或第二日到账，可能存在极少数银行不支持。
     * @author: JiuDongDong
     * @param successTime 支付交易成功时间
     * @param totalAmount 订单金额
     * @param refundAmount 退款金额
     * @return java.lang.String
     * date: 2019/5/8 10:30
     */
    private String getTxnType(Date successTime, String totalAmount, String refundAmount) {
        String txnType;
        //支付交易成功时间当天的24:00
        Date successTimeTwentyFour = DateUtil.getFutureMountDaysStartWithOutHMS(successTime, INTEGER_ZERO);
        //现在时间的24:00（申请退款的时间的24:00）
        Date now = new Date();
        Date applyRefundDayTwentyFour = DateUtil.getFutureMountDaysStartWithOutHMS(now, INTEGER_ZERO);
        if ((successTimeTwentyFour.compareTo(applyRefundDayTwentyFour) == INTEGER_ZERO)
                && new BigDecimal(totalAmount).compareTo(new BigDecimal(refundAmount)) == 0) {
            // 消费当天进行全额退款
            txnType = TXNTYPE_CONSUMEUNDO;
        } else {
            // 隔日退款
            txnType = TXNTYPE_REFUND;
        }
        logger.info("txnType = {}", txnType);
        return txnType;
    }
}
