package com.ewfresh.pay.unionPayRefundParamsPolicy.impl;

import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.unionPayRefundParamsPolicy.UnionPayRefundParamsPolicy;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.bob.BOBRefundSeqFormat;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import com.ewfresh.pay.util.unionpayqrcode.UnionPayQrCodeUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 自营补款退款（没运费）
 * @author: JiuDongDong
 * date: 2019/7/2.
 */
@Component
public class QrCodeEwfreshSupplementPolicy implements UnionPayRefundParamsPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    //读取资源配置参数
    @Value("${QRCode.url}")
    private String APIurl;//银商平台接口地址
    @Value("${QRCode.self.mid}")
    private String selfMid;//自营商户号
    @Value("${QRCode.self.tid}")
    private String selfTid;//自营终端号
    @Value("${QRCode.shop.mid}")
    private String shopMid;//店铺商户号
    @Value("${QRCode.shop.tid}")
    private String shopTid;//店铺终端号
    @Value("${freight.shop.mid}")
    private String freightShopMid;//商户号：运费
    @Value("${freight.shop.tid}")
    private String freightShopTid;//终端号：运费
    @Value("${QRCode.instMid}")
    private String instMid;//机构商户号
    @Value("${QRCode.msgSrc}")
    private String msgSrc;//来源系统
    @Value("${QRCode.msgSrcId}")
    private String msgSrcId;//来源系统id
    @Value("${QRCode.key}")
    private String key;//通讯秘钥

    @Value("${QRCode.msgType_getQRCode}")
    private String msgType_getQRCode;//消息类型:获取二维码
    @Value("${QRCode.msgType_refund}")
    private String msgType_refund;//消息类型:订单退款
    @Value("${QRCode.msgType_query}")
    private String msgType_query;//消息类型:账单查询
    @Value("${QRCode.msgType_queryLastQRCode}")
    private String msgType_queryLastQRCode;//消息类型:根据商户终端号查询此台终端最后一笔详单情况
    @Value("${QRCode.msgType_queryQRCodeInfo}")
    private String msgType_queryQRCodeInfo;//消息类型:查询二维码静态信息
    @Value("${QRCode.msgType_closeQRCode}")
    private String msgType_closeQRCode;//消息类型:关闭二维码
    @Value("${QRCode.notifyUrl}")
    private String notifyUrl;//支付结果通知地址
    @Value("${QRCode.returnUrl}")
    private String returnUrl;//前台网页跳转地址

    private SimpleDateFormat sdf8 = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<Map<String, String>> getUnionPayRefundParams(RefundParam refundParam, String outTradeNo, String mid, String tid, String shopMid, BigDecimal payerPayAmount, String outRequestNo, Date successTime) {
        logger.info("Create QrCodeEwfreshSupplementPolicy refundParam START, the refundParam = {}, outTradeNo = {}, " +
                        "mid = {}, tid = {}, payerPayAmount = {}, outRequestNo = {}", JsonUtil.toJson(refundParam),
                outTradeNo, mid, tid, payerPayAmount, outRequestNo);
        String refundAmount = refundParam.getRefundAmount();//总退款金额，单位为元（包含运费，退货退款的运费为0，补款退运费为0）
        BigDecimal freight = refundParam.getFreight();//运费退款金额，单位为元（退货退款的运费为0，补款退运费为0）
        String tradeNo = refundParam.getTradeNo();// 交易流水号（支付流水表的channel_flow_id）

        //组织请求报文
        JSONObject json = new JSONObject();
        //json.put(MSG_ID, );//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);
        json.put(MSG_TYPE, msgType_refund);
        String refundTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");// 商户发送退款请求的时间
        json.put(REQUEST_TIMESTAMP, refundTime);//退款请求时间
        //json.put(SRC_RESERVE, "");//请求系统预留字段
        json.put(BILL_DATE, sdf8.format(successTime));//接收支付通知时，将billDate存在payflow的successTime字段了
        json.put(BILL_NO, tradeNo);//也可填merOrderId(支付成功回调，银联给生成的商户订单号)，同样起作用
        json.put(INST_MID, instMid);
        json.put(MID, mid);
        json.put(TID, tid);
        String refundSequence = msgSrcId + BOBRefundSeqFormat.orderNo2UnionPayRefundSequence(outRequestNo);// msgSrcId + 生成28位的退款流水号
        json.put(REFUND_ORDER_ID, refundSequence);
        json.put(REFUND_AMOUNT, FenYuanConvert.yuan2Fen(refundAmount));
//        json.put(PLATFORM_AMOUNT, platformAmount);//平台商户退款分账金额  注意：单位转化为分
//        json.put(SUB_ORDERS, );// 分账域

        // 确认是否退全款，是的话platformAmount、subOrders就不需要再传
        boolean isAllRefund = new BigDecimal(refundAmount).compareTo(payerPayAmount) == 0 ? true : false;
        if (!isAllRefund) {
            logger.info("This is not a full refund but a part refund. outRequestNo = {}, payerPayAmount = " +
                    "{}, refundAmount = {}", outRequestNo, payerPayAmount, refundAmount);
            BigDecimal platformAmount = FenYuanConvert.yuan2Fen(new BigDecimal(refundAmount).toString());
            json.put(PLATFORM_AMOUNT, platformAmount.toString());//分账金额。平台商户分账金额 若分账标记传，则分账金额必传
        }

        //json.put(REFUND_DESC, "");//退货说明
        json.put(SIGN_TYPE, MD5);//签名算法
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(key, paramsMap));
        logger.info("QrCodeEwfreshSupplementPolicy'result paramsMap：{}", JsonUtil.toJson(paramsMap));

        List<Map<String, String>> paramsMapList = new ArrayList<>();
        paramsMapList.add(paramsMap);
        return paramsMapList;
    }
}
