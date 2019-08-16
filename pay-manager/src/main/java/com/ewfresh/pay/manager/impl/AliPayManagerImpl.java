package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Configure;
import com.ewfresh.pay.manager.AliPayManager;
import com.ewfresh.pay.manager.CommonsManager;
import com.ewfresh.pay.model.BillFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.request.AliPayRequest;
import com.ewfresh.pay.response.PayResponse;
import com.ewfresh.pay.service.AliPayService;
import com.ewfresh.pay.service.BillFlowService;
import com.ewfresh.pay.util.*;
import com.opencsv.CSVReader;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * Created by wangziyuan on 2018/4/4.
 */
@Component
public class AliPayManagerImpl implements AliPayManager {
    private static final String TYPE_NAME = "pc电脑网站支付";
    private static final String UID = "1001";
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String TRADE_NO = "trade_no";
    private static final String BILL_TYPE = "bill_type";
    private static final String TRADE_STATUS = "trade_status";
    private static final String BUYER_ID = "buyer_id";
    private static final String TOTAL_AMOUNT = "total_amount";
    private static final String SELLER_ID = "seller_id";
    private static final String GMT_PAYMENT = "gmt_payment";
    private static final String BILL_DATE = "bill_date";
    private static final String SIGNCUSTOMER = "signcustomer";
    private static final String SUMMARY = "汇总";
    private static final String PAY_CHANNEL = "1";
    private static final String TYPE_CODE = "1";
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ZIP = ".zip";
    private static final String GBK = "GBK";
    private static final int DAYS = 1;
    @Autowired
    private Configure configure;
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private BillFlowService billFlowService;
    @Autowired
    private CommonsManager commonsManager;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ResponseData AliPagePay(AliPayRequest aliPayRequest) {
        logger.info("come AlipagePay------->>>");
        ResponseData responseData = new ResponseData();
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(configure.getReturnUrl());
        alipayRequest.setNotifyUrl(configure.getNotifyUrl());//在公共参数中设置回跳和通知地址
        logger.info("tojson alipayRequest------>");
        alipayRequest.setBizContent(ItvJsonUtil.toJson(aliPayRequest));
        String form = null; //调用SDK生成表单
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
            responseData.setEntity(form);
        } catch (AlipayApiException e) {
            logger.info("have an AlipayApiException!!!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("have an AlipayApiException!!!");
        }
        return responseData;
    }

    @Override
    public String AliNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("GBK"), configure.getCharset());
            logger.info("!!!!!!!!!!!!!---->name={},value={}", name, valueStr);
            params.put(name, valueStr);
        }
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, configure.getPublicKey(), configure.getCharset(), configure.getSignType());
        } catch (AlipayApiException e) {
            logger.error("there have AlipayApiException--->", e);
            return PayResponse.FAIL;
        }
        //1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        //2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        //3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        //4、验证app_id是否为该商户本身。
        logger.info("signVerified={}", signVerified);
        if (signVerified) {//验证成功
            logger.info("check sign success!!!!");
            if (params.get(TRADE_STATUS).equals("TRADE_FINISHED")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                return PayResponse.SUCCESS;
            } else if (params.get(TRADE_STATUS).equals(TRADE_SUCCESS)) {
                logger.info("to prepare param ----->");
                Map<String, Object> map = new HashMap<>();
                //第三方交易订单号
                map.put(Constants.INTERACTION_ID, params.get(OUT_TRADE_NO));
                //支付渠道流水号(支付宝交易号)
                map.put(Constants.CHANNEL_FLOW_ID, params.get(TRADE_NO));
                //付款人id
                map.put(Constants.PAYER_ID, params.get(BUYER_ID));
                //付款方金额(暂时使用的是订单金额,因为不能使用红包.使用红包的情况下会有receipt_amount实收金额)
                map.put(Constants.PAYER_PAY_AMOUNT, params.get(TOTAL_AMOUNT));
                //收款人id(卖家支付宝用户号)
                map.put(Constants.RECEIVER_USER_ID, params.get(SELLER_ID));
                //支付成功时间
                map.put(Constants.SUCCESS_TIME, params.get(GMT_PAYMENT));
                //支付类型编号(暂时未知)
                map.put(Constants.TYPE_CODE, TYPE_CODE);
                //支付类型名称
                map.put(Constants.TYPE_NAME, TYPE_NAME);
                //支付渠道标识
                map.put(Constants.PAY_CHANNEL, PAY_CHANNEL);
                //uid
                map.put(Constants.UID, UID);
                //TODO 调用一次处理方法
                logger.info("ready to calMoneyByFate------------->");
                Map<String, Object> map1 = CalMoneyByFate.calMoneyByFate(map);
                //TODO 调用统一封装方法做业务逻辑的判断
                logger.info("for ifSuccess------------->");
                if (commonsManager.ifSuccess(map1)) {
                    logger.info("PayResponse SUCCESS");
                    return PayResponse.SUCCESS;
                }
            }
        } else {//验证失败
            logger.info("PayResponse FAIL------->");
            return PayResponse.FAIL;
        }
        logger.info("PayResponse FAIL------->");
        return PayResponse.FAIL;
    }

    @Override
    public void DownLoadBillToAdd() throws Exception {
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        JSONObject json = new JSONObject();
        json.put(BILL_TYPE, SIGNCUSTOMER);
        //昨天的数据
        json.put(BILL_DATE, new DateTime().minusDays(4).toString(DATE_FORMAT));
        request.setBizContent(json.toString());
        AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            // 获取下载地址url
            String url = response.getBillDownloadUrl();
            logger.info("get bill down url success--->", url);
            // 设置下载后生成Zip目录
            String filePath = "D:/down/";
            String newZip = filePath + new Date().getTime() + ZIP;
            // 开始下载
            FileUtil.downloadNet(url, newZip);
            // 解压到指定目录
            FileUtil.unZip(newZip, "D:/zip/");
            // 遍历文件 获取需要的汇整csv
            File[] fs = new File("D:/zip/").listFiles();
            // RealIncom income = null;//自己封装的实体类
            for (File file : fs) {
                if (!file.getAbsolutePath().contains(SUMMARY)) {
                    File absoluteFile = file.getAbsoluteFile();
                    //以下方法封装到ReadCvsToInsert方法中(读取文件信息存入数据库)
                    Reader gbk = new InputStreamReader(new FileInputStream(absoluteFile), GBK);
                    CSVReader reader = new CSVReader(gbk);
                    List<String[]> list = reader.readAll();
                    //调用方法获得对象集合
                    List<BillFlow> billFlows = aliPayService.ReadCvsToObject(list);
                    billFlowService.addBillFlowBach(billFlows);
                    gbk.close();
                }
                file.delete();
            }
            System.out.println(JSON.toJSONString(response));
        } else {
            //如果调用失败  账单不存在 则另做处理
        }
    }

}