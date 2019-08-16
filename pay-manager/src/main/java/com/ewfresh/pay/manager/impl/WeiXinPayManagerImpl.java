package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.WeiXinPayConfigure;
import com.ewfresh.pay.dao.PayFlowDao;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.manager.WeiXinPayManager;
import com.ewfresh.pay.model.BillFlow;
import com.ewfresh.pay.model.vo.WinXinTradeStateVo;
import com.ewfresh.pay.redisService.OrderRedisService;
import com.ewfresh.pay.util.*;
import com.ewfresh.pay.util.bob.BOBOrderNoFormat;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ewfresh.pay.util.bob.BOBOrderNoFormat.bob19OrderNo2OriWithoutER;
import static com.ewfresh.pay.util.bob.BOBOrderNoFormat.bob20OrderNoTo19WithoutER;

/**
 * description
 *
 * @author huangyabing
 * date 2018/4/9 17:53
 */
@Component
public class WeiXinPayManagerImpl implements WeiXinPayManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private CommonsManager commonsManager;
    @Autowired
    private WeiXinPayConfigure weiXinPayConfigure;
    @Autowired
    private PayFlowDao payFlowDao;
    @Autowired
    private OrderRedisService orderRedisService;

    private static final String APP_ID = "appid";//商户id
    private static final String MCH_ID = "mch_id";//商户号
    private static final String NONCE_STR = "nonce_str";//随机字符串
    private static final String BODY = "body";//订单信息
    private static final String PRODUCT_ID = "product_id";//商品订单号
    private static final String OUT_TRADE_NO = "out_trade_no";//交易单号
    private static final String TOTAL_FEE = "total_fee";//总金额
    private static final String SPBILL_CREATE_IP = "spbill_create_ip";//发送请求的ip地址
    private static final String NOTIFY_URL = "notify_url";//回调地址
    private static final String TRADE_TYPE = "trade_type";//交易类型
    private static final String SIGN = "sign";//签名
    private static final String TRANSACTION_ID = "transaction_id";//商家数据包
        private static final String OPEN_ID = "openid";//商家数据包
    private static final String TIME_END = "time_end";//商家数据包
    private static final String BILL_TYPE = "bill_type";//账单类型
    private static final String BILL_DATE = "bill_date";//账单日期
    private static final String OUT_REFUND_NO = "out_refund_no";//退款单号
    private static final String REFUND_FEE = "refund_fee";//退款金额
    private static final String OP_USER_ID = "op_user_id";//账单日期
    private static final Long WXPAY_UID = (long) 1002;//微信支付的uid
    private static final String TYPE_CODE = "2";//微信支付的uid
    private static final String PAY_CHANNEL = "2";//微信支付的uid
    private static final Short ORDER = 1;//订单类型
    private static final Short REFUND = 2;//退款类型
    private static final String REQ_INFO = "req_info";//退款类型
    private static final int DIVIDEND = 100;//分转换成元
    private static final int SCALE = 2;//分转换成元
    private static final BASE64Decoder decoder = new BASE64Decoder();
    private static final String PARTNER_ID = "partnerid";//商户号
    private static final String PREPAY_ID = "prepay_id";//预支付交易会话ID
    private static final String PREPAYID = "prepayid";//预支付交易会话ID
    private static final String PACKAGE = "package";//扩展字段
    private static final String PACKAGE_VALUE = "Sign=WXPay";//扩展字段值
    private static final String NONCESTR = "noncestr";//随机字符串
    private static final String TIMESTAMP = "timestamp";//随机字符串
    private static final String CREATE_TIME = "createTime";
    private static final String CURRENT_TIME = "currentTime";
    private static final String TIME_EXPIRE = "time_expire";//交易结束时间
    private static final String TIME_OUT_PAGE = "<!DOCTYPE html>\n" +
                                                    "<html>\n" +
                                                    "\t<head>\n" +
                                                    "\t\t<meta charset=\"UTF-8\">\n" +
                                                    "\t\t<title>" + Constants.ORDERTIMEOUT + "</title>\n" +
                                                    "\t</head>\n" +
                                                    "\t<body>\n" +
                                                    "\t\t<div style=\"width:260px;height:260px;margin: 0 auto;line-height: 260px;box-sizing: border-box;\">\n" +
                                                    "\t\t\t<p style=\"font-size: 30px;text-align: center;color: #D61A39;\">" + Constants.ORDERTIMEOUT + "</p>\n" +
                                                    "\t\t</div>\n" +
                                                    "\t</body>\n" +
                                                    "</html>\n";//订单微信支付超时页面

    /**
     * description 获取支付二维码
     *
     * @param responseData
     * @param orderNo
     * @param body
     * @param totalFee
     * @param spbillCreateIp
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
    public ResponseData getWeiXinPayCode(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String orderNo, String body, String totalFee, String spbillCreateIp) {
        logger.info("It is get pay code method in controller params of it is -------> [orderNo = {}, body = {}, " +
                "totalFee = {}, spbillCreateIp = {}]", orderNo, body, totalFee, spbillCreateIp);
        String timeExpire = getTimeExpire(responseData, orderNo);//获取交易结束时间
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        if (ResponseStatus.ORDERTIMEOUT.getValue().equals(responseData.getCode())){
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter outputStream = null;
            try {
                outputStream = response.getWriter();
            } catch (IOException e) {
                logger.error("The order timeOut , outputStream err");
            }
            logger.info("orderNo is orderNo={}" ,orderNo);
            outputStream.print(TIME_OUT_PAGE);
            return null;
        }
        if (timeExpire != null){
            paramMap.put(TIME_EXPIRE, timeExpire);
        }
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr() + "");
        paramMap.put(BODY, body);
        paramMap.put(PRODUCT_ID, orderNo);
        paramMap.put(OUT_TRADE_NO, orderNo);
        paramMap.put(TOTAL_FEE, CommonUtils.yuanToFee(totalFee));
        paramMap.put(SPBILL_CREATE_IP, spbillCreateIp);
        paramMap.put(NOTIFY_URL, weiXinPayConfigure.getNotifyUrl());
        paramMap.put(TRADE_TYPE, weiXinPayConfigure.getTradeTypeSm());
        //生成随机数签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);
        //将map转换成xml字符串
        String requestXML = XMLUtil.getRequestXml(paramMap);
        logger.info("request xml ----->" + requestXML);
        System.out.println(requestXML);
        try {
            //发送请求
            String resXml = HttpUtil.postData(weiXinPayConfigure.getGetPrepayUrl(), requestXML);
            logger.info("response xml ----->" + resXml);
            Map map = XMLUtil.doXMLParse(resXml);
            String urlCode = (String) map.get("code_url");
            logger.info("urlCode----->" + urlCode);
            //生成二维码
            CommonUtils.getQRcode(request, response, null, null, urlCode);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg("get pay code success");
        } catch (Exception e) {
            logger.error("get pay code is failed----->");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get pay code is failed");
        }
        return responseData;
    }

    /**
     * description 微信支付回调
     *
     * @param response
     * @param request
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
    public ResponseData weiXinPayCallback(HttpServletResponse response, HttpServletRequest request, ResponseData responseData) {
        logger.info("It is method to notify of wxpay in manager------>");
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        //读取参数并解析成为map格式
        packageParams = getParamMap(request);
        logger.info("message of identity is packageParams------>" + packageParams);
        // 账号信息
        String key = weiXinPayConfigure.getApi(); // key
        //判断签名是否正确(验签)
        if (CommonUtils.isTenpaySign("UTF-8", packageParams, key)) {
            //------------------------------
            logger.info("check sign success----->[packageParams = {}, key = {}]", packageParams, key);
            //处理业务开始
            //------------------------------
            String resXml = "";
            if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
                // 这里是支付成功
                logger.info("receive notify and success------>" + ItvJsonUtil.toJson(packageParams));
                //////////执行自己的业务逻辑////////////////
                //商户订单号（处理后）
                map.put(Constants.INTERACTION_ID, packageParams.get(OUT_TRADE_NO));
                //渠道流水号
                map.put(Constants.CHANNEL_FLOW_ID, packageParams.get(TRANSACTION_ID));
                //付款人id
                map.put(Constants.PAYER_ID, packageParams.get(OPEN_ID));
                //付款方支付金额
                BigDecimal total_fee = new BigDecimal((String) packageParams.get(TOTAL_FEE));
                logger.info("total fee is ---->" + total_fee);
                //将分转成元并保留两位小数
                BigDecimal divide = total_fee.divide(new BigDecimal(DIVIDEND), SCALE, RoundingMode.HALF_UP);
                logger.info("divide is ----->" + divide);
                map.put(Constants.PAYER_PAY_AMOUNT, divide);
                //收款人id
                map.put(Constants.RECEIVER_USER_ID, packageParams.get(APP_ID));
                //支付成功时间
                map.put(Constants.SUCCESS_TIME, packageParams.get(TIME_END));
                //支付类型编号
                map.put(Constants.TYPE_CODE, TYPE_CODE);
                //支付类型名称
                map.put(Constants.TYPE_NAME, packageParams.get(TRADE_TYPE));
                //微信支付uid
                map.put(Constants.UID, WXPAY_UID);
                //支付渠道
                map.put(Constants.PAY_CHANNEL, PAY_CHANNEL);
                //计算税率、税费、收入金额之后的map
                map = CalMoneyByFate.calMoneyByFate(map);
                logger.info("it is the map of service logic ------>" + ItvJsonUtil.toJson(map));
                boolean b = commonsManager.ifSuccess(map);
                logger.info("it is the boolean of service logic ------>" + String.valueOf(b));
                //判断业务逻辑是否已正确处理
                if (b) {
                    logger.info("success of weiXinPay callback----->");
                    //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                    resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                            + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                    responseData.setCode(ResponseStatus.OK.getValue());
                    logger.info("success of weiXinPay callback----->" + resXml);
                    responseData.setMsg("success of weiXinPay callback----->");
                    responseData.setEntity(map);
                }
            } else {
                logger.info("failed tof weiXinPay callback, error code is-----> " + packageParams.get("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("failed tof weiXinPay callback, error code is-----> " + packageParams.get("err_code"));
                responseData.setEntity("FAIL");
            }
            try {
                logger.info("write result of notify----->");
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (Exception e) {
                logger.info("failed to bufferedOutputStream to write with IOException");
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("failed bufferedOutputStream to write of wxpay callback, error code is-----> " + packageParams.get("err_code"));
            }
        } else {
            logger.info("failed to callback of weiXinPay and check sign");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed bufferedOutputStream to write of wxpay callback, error code is-----> " + packageParams.get("err_code"));
        }
        return null;
    }

    /**
     * description 根据订单号查询订单信息
     *
     * @param request
     * @param response
     * @param responseData
     * @param outTradeNo
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
    public ResponseData queryOrderByOutTradeNo(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String outTradeNo) {
        logger.info("It is query order of wxpay method in manager params of it is ------->[outTradeNo = {}] ", outTradeNo);
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr() + "");
        paramMap.put(OUT_TRADE_NO, outTradeNo);
        //生成随机数签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);
        //将map转换成xml字符串
        String requestXML = XMLUtil.getRequestXml(paramMap);
        logger.info("query order's request xml ----->" + requestXML);
        System.out.println(requestXML);
        //发送请求
        try {
            String resXml = HttpUtil.postData(weiXinPayConfigure.getOrderQuery(), requestXML);
            logger.info("response xml ----->" + resXml);
            Map map = XMLUtil.doXMLParse(resXml);
            logger.info("res map is --->" + map);
            String trade_state = (String) map.get("trade_state");
            WinXinTradeStateVo WXTradeStateVo = new WinXinTradeStateVo();
            if (!trade_state.equals("SUCCESS")) {
                logger.warn("failed to query this order is ----->" + ItvJsonUtil.toJson(map));
                WXTradeStateVo.setTradeState(trade_state);
                responseData.setEntity(WXTradeStateVo);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("failed to query this order is ----->");
                return responseData;
            }
            //查询订单号
            String orderNo19 = bob20OrderNoTo19WithoutER(outTradeNo);
            String orderNo = bob19OrderNo2OriWithoutER(orderNo19);
            Long orderId = Long.valueOf(orderNo);//payFlowDao.getOrderId(outTradeNo);
            logger.info("orderId = " + orderId);
            //支付成功状态,返回信息给前台
            //TODO 封装成一个对象
            logger.info("success to query this order the result is ----->" + ItvJsonUtil.toJson(map));
            WXTradeStateVo.setOrderId(orderId);
            WXTradeStateVo.setTradeState(trade_state);
            responseData.setEntity(WXTradeStateVo);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg("success to query this order the result is ----->");
        } catch (Exception e) {
            logger.error("failed to query this order is ----->" + e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed to query this order is ----->");
        }
        return responseData;
    }

    /**
     * description 下载对账单
     *
     * @param response
     * @param request
     * @param responseData
     * @param billDate
     * @param billType
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
    public ResponseData downLoadBill(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String billDate, String billType) {
        logger.info("It is method to download bill of wxpay in manager params are [billDate = {}, billType = {}]------>", billDate, billType);
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        List<BillFlow> billFlows = new ArrayList<>();
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr());
        //下载对账单的日期，格式：20140603，日期不可为当天。
        paramMap.put(BILL_DATE, billDate);
        //bill_type:ALL返回当日所有订单信息,默认值SUCCESS返回当日成功支付的订单。REFUND，返回当日退款订单
        paramMap.put(BILL_TYPE, billType);
        //生成签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);
        //将map转化成xml
        String requestXml = XMLUtil.getRequestXml(paramMap);
        logger.info("request xml ----->" + requestXml);
        //发送请求
        String resXml = null;
        try {
            resXml = HttpUtil.postData(weiXinPayConfigure.getDownLoadBill(), requestXml);
            //返回xml类型数据时，表明下载失败
            if (resXml.startsWith("<xml>")) {//查询日期为当天时，错误信息提示日期无效
                logger.info("there is no bill, response xml ----->" + resXml);
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("failed to download bill");
            } else {//返回文本类型则下载成功
                //删除第一行的表头数据
                String tradeMsg = resXml.substring(resXml.indexOf("`"));
                //去掉汇总数据，并且去掉"`"这个符号
                String tradeInfo = tradeMsg.substring(0, tradeMsg.indexOf("总")).replace("`", "").replace("\r\n", "");
                // 根据%来区分
                String[] tradeArray = tradeInfo.split("%");
                logger.info("wxpay bill is tradeDetailArray------>" + ItvJsonUtil.toJson(tradeArray));
                //遍历所有账单
                for (String tradeDetailInfo : tradeArray) {
                    //获取账单详情
                    String[] tradeDetailArray = tradeDetailInfo.split(",");
                    // tradeDetilInfos.add(tradeDetailInfo);
                    BillFlow billFlow = new BillFlow();
                    //微信订单号
                    billFlow.setChannelFlowId(tradeDetailArray[5]);
                    //商户订单号
                    billFlow.setOrderId(Long.valueOf(tradeDetailArray[6]));
                    //如果退款单号是空
                    if (StringUtils.isBlank(tradeDetailArray[15])) {
                        //交易类型为订单
                        billFlow.setTradeType(ORDER);
                    } else
                        billFlow.setTradeType(REFUND);
                    //总金额
                    billFlow.setIncome(BigDecimal.valueOf(Double.valueOf(tradeDetailArray[12])));
                    //交易类型
                    logger.info("trade type ------->" + tradeDetailArray[8]);
                    //商家数据包
                    billFlow.setDesp(tradeDetailArray[21]);
                    billFlows.add(billFlow);
                    //商户订单号
                    logger.info("ChannelFlowId---->" + tradeDetailArray[5]);
                    //订单号
                    logger.info("orderId---->" + tradeDetailArray[6]);
                    //总金额
                    logger.info("totalFee---->" + tradeDetailArray[12]);
                    //手续费
                    logger.info("poundage ---->" + tradeDetailArray[22]);
                    //用户标识
                    logger.info("tag of user ------>" + tradeDetailArray[7]);
                    //交易类型
                    logger.info("type of trade ------>" + tradeDetailArray[8]);
                    //交易状态
                    logger.info("status of trade ------>" + tradeDetailArray[9]);
                    //微信退款单号
                    logger.info("WXorderId of refund ------>" + tradeDetailArray[14]);
                    //商户退款单号
                    logger.info("orderId of refund ------>" + tradeDetailArray[15]);
                    //退款金额
                    logger.info("num of refund ------>" + tradeDetailArray[16]);
                    //退款类型
                    logger.info("type of refund ------>" + tradeDetailArray[18]);
                    //退款状态
                    logger.info("status of refund ------>" + tradeDetailArray[19]);
                    //商品名称
                    logger.info("name of product ------>" + tradeDetailArray[20]);
                    //商品名称
                    logger.info("body ------>" + tradeDetailArray[21]);
                    //手续费
                    logger.info("poundage of trade ------>" + tradeDetailArray[22]);
                    //费率
                    logger.info("rate of trade ------>" + tradeDetailArray[23]);


                    logger.info("wxpay bill's detail is tradeDetailArray------>" + ItvJsonUtil.toJson(tradeDetailArray));
                }
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg("download bill is success----->");
                responseData.setEntity(ItvJsonUtil.toJson(billFlows));
            }
        } catch (Exception e) {
            logger.error("download bill is failed----->");
        }
        return responseData;
    }

    /**
     * 请求退款服务
     *
     * @param outTradeNo  商户系统内部的订单号,transaction_id 、out_trade_no 二选一，如果同时存在优先级：transaction_id>out_trade_no
     * @param outRefundNo 商户系统内部的退款单号，商户系统内部唯一，同一退款单号多次请求只退一笔
     * @param totalFee    订单总金额，单位为分
     * @param refundFee   退款总金额，单位为分
     */
    @Override
    public ResponseData refund(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String outTradeNo, String outRefundNo, String totalFee, String refundFee) {
        logger.info("It is method to refund of wxpay params are = [outTradeNo = {}, outRefundNo = {}, totalFee = {}, refundFee = {}]", outTradeNo, outRefundNo, totalFee, refundFee);
        String total_fee = CommonUtils.yuanToFee(totalFee);//订单的总金额,以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）
        String refund_fee = CommonUtils.yuanToFee(refundFee);// 退款金额，以分为单位（填错了貌似提示：同一个out_refund_no退款金额要一致）
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        SortedMap<Object, Object> queryRefund = new TreeMap<Object, Object>();
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());//qppid
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());//商户id
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr());//随机字符串
        //paramMap.put("transaction_id", transaction_id);//微信的订单号
        paramMap.put(OUT_TRADE_NO, outTradeNo);
        paramMap.put(OUT_REFUND_NO, outRefundNo);
        paramMap.put(TOTAL_FEE, String.valueOf(total_fee));
        paramMap.put(REFUND_FEE, String.valueOf(refund_fee));
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
        try {
            //携带证书发送请求
            resXml = HttpUtil.doRefund(weiXinPayConfigure.getRefundUrl(), requestXml);
            logger.info(" response string ----->" + resXml);
            //将string类型返回值解析成map
            Map resMap = XMLUtil.doXMLParse(resXml);
            logger.info("resMap is ----->" + resMap);
            //如果返回结果正确，则去查询此次退款
            if (resMap.get("result_code").equals("SUCCESS")) {
                logger.info("result_code is equals with success ----->");
                queryRefund = queryRefund(outRefundNo);
                logger.info("query refund by out_refund_id ----->" + queryRefund);
            }
            logger.info("success to refund");
            responseData.setMsg("success to refund");
            responseData.setEntity(queryRefund);
            responseData.setCode(ResponseStatus.OK.getValue());
        } catch (Exception e) {
            logger.error("failed to refund，resXml = " + resXml);
            responseData.setMsg("failed to refund");
            responseData.setCode(ResponseStatus.ERR.getValue());
        }
        return responseData;
    }

    /**
     * description 将请求中的xml类型转换为map
     *
     * @param request
     * @return java.util.SortedMap<java.lang.Object,java.lang.Object>
     * @author huangyabing
     */
    public SortedMap<Object, Object> getParamMap(HttpServletRequest request) {
        //读取参数
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        String s;
        Map<String, String> m = new HashMap<String, String>();
        //执行业务逻辑方法传入的map
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        try {
            inputStream = request.getInputStream();
            //读取流中的数据 读取结果为xml类型
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            logger.info("data of inputStream with xml type------>" + ItvJsonUtil.toJson(sb));
            in.close();
            inputStream.close();
            //解析xml成map
            m = XMLUtil.doXMLParse(sb.toString());
            logger.info("data after parsing with map type------>" + m);
        } catch (Exception e) {
            logger.error("failed to parsing xml to map with IOException");
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
        return packageParams;
    }

    /**
     * description 微信退款回调(暂时不用)
     *
     * @param response
     * @param request
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
    public ResponseData refundCallBack(HttpServletResponse response, HttpServletRequest request, ResponseData responseData) {
        logger.info("It is method to receive notify of wxpay refund");
        SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        String result;
        //读取参数并解析成为map格式
        packageParams = getParamMap(request);
        logger.info("message of identity is packageParams------>" + packageParams);
        //开始处理返回加密的数据
        logger.info("start to parse req_info------> ");
        try {
            //解码后的返回值
            String decodeInfo = new String(decoder.decodeBuffer((String) packageParams.get(REQ_INFO)), "UTF-8");
            logger.info("base64 decode success----->" + decodeInfo);
            byte[] decodeInfoBytes = decodeInfo.getBytes();
            logger.info("base64 decode success----->" + ItvJsonUtil.toJson(decodeInfoBytes));
            //加密过后的商户密钥
            String key = MD5Util.MD5Encode(weiXinPayConfigure.getApi(), "UTF-8");
            logger.info("md5 encode api key success----->" + key);
            byte[] keyBytes = key.getBytes();
            logger.info("md5 encode api key success----->" + ItvJsonUtil.toJson(keyBytes));
            result = CommonUtils.decodePKCS7Padding(decodeInfoBytes, keyBytes);
        } catch (IOException e) {
            logger.info("base64 decode failed----->" + packageParams);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("base64 decode failed-----> " + packageParams.get("err_code"));
            responseData.setEntity("FAIL");
            return responseData;
        }
        logger.info("end of parse req_info result is ------>" + result);
        //TODO 处理返回结果 将string转成string[]并校验
        // 账号信息
        String key = weiXinPayConfigure.getApi(); // key
        //------------------------------
        logger.info("check sign success----->[packageParams = {}, key = {}]" + packageParams, key);
        //处理业务开始
        //------------------------------
        String resXml = "";
        if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
            // 这里是支付成功
            logger.info("receive notify and success------>" + ItvJsonUtil.toJson(packageParams));
            //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
            resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                    + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
            responseData.setCode(ResponseStatus.OK.getValue());
            logger.info("success of weiXinPay callback----->" + resXml);
            responseData.setMsg("success of wxpay refund callback----->");
            responseData.setEntity("SUCCESS");
        } else {
            logger.info("failed tof weiXinPay callback, error code is-----> " + packageParams.get("err_code"));
            resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                    + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed of wxpay refund callback, error code is-----> " + packageParams.get("err_code"));
            responseData.setEntity("FAIL");
        }
        try {
            logger.info("write result of wxpay refund notify");
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.info("failed to bufferedOutputStream to write ");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("failed bufferedOutputStream to write of wxpay refund callback, error code is-----> " + packageParams.get("err_code"));
        }

        return responseData;
    }


    /**
     * description 查询退款订单信息
     *
     * @param outRefundNo
     * @return com.ewfresh.pay.util.ResponseData
     * @author huangyabing
     */
    @Override
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
            logger.error("data after parsing with map typesi err", e);
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
        if (!packageParams.get("result_code").equals("SUCCESS")) {
            logger.error("failed to query ");
        }
        //TODO 退款成功，更改订单状态
        //TODO 更改状态成功后记流水  若二者有任意一个业务失败，则退款失败
        if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
            logger.info("query refund is success ----->");
        } else {
            logger.info("query refund is failed ----->");
        }
        return packageParams;
    }

    /**
     * description 发起微信支付
     *
     * @param responseData
     * @param orderNo
     * @param body
     * @param totalFee
     * @param spbillCreateIp
     * @return com.ewfresh.pay.util.ResponseData
     * @author zhaoqun
     */
    @Override
    public ResponseData winXinPayRequest(HttpServletResponse response, HttpServletRequest request, ResponseData responseData, String orderNo, String body, String totalFee, String spbillCreateIp) {
        logger.info("It is winXin Pay Request method in controller params of it is -------> [orderNo = {}, body = {}, " +
            "totalFee = {}, spbillCreateIp = {}]", orderNo, body, totalFee, spbillCreateIp);
        String timeExpire = getTimeExpire(responseData, orderNo);//获取交易结束时间
        SortedMap<Object, Object> paramMap = new TreeMap<Object, Object>();
        if (ResponseStatus.ORDERTIMEOUT.getValue().equals(responseData.getCode())){
            return responseData;
        }
        if (timeExpire != null){
            paramMap.put(TIME_EXPIRE, timeExpire);
        }
        paramMap.put(APP_ID, weiXinPayConfigure.getAppId());
        paramMap.put(MCH_ID, weiXinPayConfigure.getMchId());
        paramMap.put(NONCE_STR, CommonUtils.CreateNoncestr() + "");
        paramMap.put(BODY, body);
        paramMap.put(OUT_TRADE_NO, orderNo);
        paramMap.put(TOTAL_FEE, CommonUtils.yuanToFee(totalFee));
        paramMap.put(SPBILL_CREATE_IP, spbillCreateIp);
        paramMap.put(NOTIFY_URL, weiXinPayConfigure.getNotifyUrl());
        paramMap.put(TRADE_TYPE, weiXinPayConfigure.getTradeTypeApp());
        //生成随机数签名
        String sign = CommonUtils.createSign("UTF-8", paramMap, weiXinPayConfigure.getApi());
        paramMap.put(SIGN, sign);
        //将map转换成xml字符串
        String requestXML = XMLUtil.getRequestXml(paramMap);
        logger.info("request xml ----->" + requestXML);
        System.out.println(requestXML);
        try {
            //发送请求
            String resXml = HttpUtil.postData(weiXinPayConfigure.getGetPrepayUrl(), requestXML);
            logger.info("response xml ----->" + resXml);
            Map map = XMLUtil.doXMLParse(resXml);
            logger.info("map ------->" , ItvJsonUtil.toJson(map));
            // 获取预支付交易会话标识 prepay_id
            String prepayId = (String) map.get(PREPAY_ID);
            logger.info("prepayId----->" + prepayId);

            //调起支付接口 参数
            SortedMap<Object, Object> payParamMap = new TreeMap<Object, Object>();
            payParamMap.put(APP_ID, weiXinPayConfigure.getAppId());
            payParamMap.put(PARTNER_ID, weiXinPayConfigure.getMchId());
            payParamMap.put(PREPAYID, prepayId);
            payParamMap.put(PACKAGE, PACKAGE_VALUE);
            payParamMap.put(NONCESTR, CommonUtils.CreateNoncestr() + "");
            payParamMap.put(TIMESTAMP, getTimeStamp());//时间戳（秒）

            //生成随机数签名
            String paySign = CommonUtils.createSign("UTF-8", payParamMap, weiXinPayConfigure.getApi());
            payParamMap.put(SIGN, paySign);
            logger.info("payParamMap ------->" , JsonUtil.toJson(payParamMap));
            //调起支付接口 参数返给前台
            responseData.setEntity(JsonUtil.toJson(payParamMap));
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(" winXin Pay Request  success");
        } catch (Exception e) {
            logger.error(" winXin Pay Request  is failed----->");
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(" winXin Pay Request  is failed");
        }
        return responseData;
    }

    /**
     * description 获取时间戳    时间戳从1970年1月1日00:00:00至今的秒数
     *
     * @author zhaoqun
     */
    private String getTimeStamp() {
        Date d = new Date();
        String timeStamp = String.valueOf(d.getTime() / 1000);     //getTime()得到的是微秒， 需要换算成秒
        return timeStamp;
    }

    /**
     * Description: 根据与第三方交互的订单号（加E加R）从Redis获取订单信息
     *
     * @param interactionId 与第三方交互的订单号（加E加R）
     * @return 订单信息
     * date: 2018/8/16 16:02
     * @author: JiuDongDong
     */
    private Map<String, String> getOrderInfoFromRedis(String interactionId) {
        Map<String, String> redisParam = orderRedisService.getPayOrder(interactionId);
        if (MapUtils.isEmpty(redisParam)) {
            return null;
        }
        logger.info("the redis param is------>{}", ItvJsonUtil.toJson(redisParam));
        return redisParam;
    }

    /**
     * Description: 获取交易结束时间
     * @author: ZhaoQun
     * @param responseData
     * @param orderNo
     * @return: String
     * date: 2018/12/19 14:33
     */
    private String getTimeExpire(ResponseData responseData, String orderNo) {
        Map<String, String> redisParam = getOrderInfoFromRedis(orderNo);
        //支付超时，redis数据已删除
        if (redisParam == null) {
            responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
            responseData.setMsg(Constants.ORDERTIMEOUT);
            return null;
        }
        String orderTimeOut = null;//交易结束时间（格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010）
        String createTimeStr = null;//支付超时拦截计算的基准时间
        String orderStatus = redisParam.get(Constants.ORDER_STATUS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // 如果是非订单尾款支付（如订单一次全款支付、定金支付有60分钟时间限制）则计算订单支付超时时间
        if (Constants.ORDER_WAIT_PAY.intValue() == Integer.valueOf(orderStatus)) {
            createTimeStr = redisParam.get(CREATE_TIME);
        }
        // 如果是订单尾款支付（有redis信息60分钟时间限制）则计算订单支付超时时间
        if (Constants.ORDER_PAID_EARNEST.intValue() == Integer.valueOf(orderStatus)) {
            createTimeStr = redisParam.get(CURRENT_TIME);

        }
        if (StringUtils.isNotBlank(createTimeStr)) {
            // 计算订单支付超时时间
            Date createTimeDate = new Date(Long.valueOf(createTimeStr));
            // 计算下单时间往后推1小时的时间
            Date futureMountHoursStart = DateUtil.getFutureMountMinutesStart(createTimeDate, Constants.INTEGER_THIRTY);//getFutureMountHoursStart(createTimeDate, Constants.INTEGER_ONE);//再减去30s的网络传输误差
            Date timeExpire = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -Constants.INTEGER_THIRTY);//微信结束时间
            // 再减去30s的网络传输误差， 最短失效时间间隔大于1分钟 ==》 90S
            Date futureMountSecondsStart = DateUtil.getFutureMountSecondsStart(futureMountHoursStart, -Constants.INTEGER_90);
            logger.info("++++++++++++++++++++++++++++createTimeDate = {}, futureMountHoursStart={}" ,createTimeDate,futureMountHoursStart);
            // 减去当前时间，得出剩余支付时间长度，转换为秒
            if (futureMountSecondsStart.getTime() < System.currentTimeMillis()) {
                logger.error("WeinxinPay futureMountSecondsStart.getTime() < System.currentTimeMillis(), " +
                    "futureMountSecondsStart.getTime() = " + futureMountSecondsStart.getTime() + ", " +
                    "System.currentTimeMillis() = " + System.currentTimeMillis() + ", orderNo = " + orderNo);
                responseData.setCode(ResponseStatus.ORDERTIMEOUT.getValue());
                responseData.setMsg(Constants.ORDERTIMEOUT);
                return orderTimeOut;
            }
            orderTimeOut = sdf.format(timeExpire);
        }
        logger.info("orderTimeOut = " + orderTimeOut);
        return orderTimeOut;
    }
}
