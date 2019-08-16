package com.ewfresh.pay.unionPayRefundParamsPolicy.impl;

import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.unionpayh5suborder.SubOrder;
import com.ewfresh.pay.unionPayRefundParamsPolicy.UnionPayRefundParamsPolicy;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 店铺退货退款
 * @author: JiuDongDong
 * date: 2019/6/29.
 */
@Component
public class H5ShopRefundPolicy implements UnionPayRefundParamsPolicy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    //读取资源配置参数
    @Value("${H5Pay.url}")
    private String APIurl;
    @Value("${H5Pay.self.B2B.mid}")
    private String selfB2BMid;//商户号：自营、B2B企业网银
    @Value("${H5Pay.self.B2B.tid}")
    private String selfB2BTid;//终端号：自营、B2B企业网银
    @Value("${H5Pay.shop.B2B.mid}")
    private String shopB2BMid;//商户号：店铺、B2B企业网银
    @Value("${H5Pay.shop.B2B.tid}")
    private String shopB2BTid;//终端号：店铺、B2B企业网银
    @Value("${H5Pay.self.B2C.borrow.mid}")
    private String selfB2CBorrowMid;//商户号：自营、H5线上借记卡、支付宝
    @Value("${H5Pay.self.B2C.borrow.tid}")
    private String selfB2CBorrowTid;//终端号：自营、H5线上借记卡、支付宝
    @Value("${H5Pay.shop.B2C.borrow.mid}")
    private String shopB2CBorrowMid;//商户号：店铺、H5线上借记卡、支付宝
    @Value("${H5Pay.shop.B2C.borrow.tid}")
    private String shopB2CBorrowTid;//终端号：店铺、H5线上借记卡、支付宝
    @Value("${H5Pay.self.B2C.loan.mid}")
    private String selfB2CLoanMid;//商户号：自营、H5线上贷记卡
    @Value("${H5Pay.self.B2C.loan.tid}")
    private String selfB2CLoanTid;//终端号：自营、H5线上贷记卡
    @Value("${H5Pay.shop.B2C.loan.mid}")
    private String shopB2CLoanMid;//商户号：店铺、H5线上贷记卡
    @Value("${H5Pay.shop.B2C.loan.tid}")
    private String shopB2CLoanTid;//终端号：店铺、H5线上贷记卡
    @Value("${freight.shop.mid}")
    private String freightShopMid;//商户号：运费
    @Value("${freight.shop.tid}")
    private String freightShopTid;//终端号：运费
    @Value("${H5Pay.instMid}")
    private String instMid;
    @Value("${H5Pay.msgSrc}")
    private String msgSrc;
    @Value("${H5Pay.msgSrcId}")
    private String msgSrcId;
    @Value("${H5Pay.key}")
    private String md5Key;

    @Value("${H5Pay.msgType_refund}")
    private String msgType_refund;
    @Value("${H5Pay.msgType_query}")
    private String msgType_query;//订单查询（支付）
    @Value("${H5Pay.msgType_refundQuery}")
    private String msgType_refundQuery;//订单查询（退款）

    @Override
    public List<Map<String, String>> getUnionPayRefundParams(RefundParam refundParam, String outTradeNo, String mid, String tid, String shopMid, BigDecimal payerPayAmount, String outRequestNo, Date successTime) {
        String refundAmount = refundParam.getRefundAmount();//总退款金额，单位为元（包含运费，退货退款的运费为0）   总退款金额 = 运费退款金额 + 易网聚鲜平台应退分润金额 + 店铺应退分润金额
        BigDecimal freight = refundParam.getFreight();//运费退款金额，单位为元（退货退款的运费为0）
        BigDecimal ewfreshBenefitRefund = refundParam.getEwfreshBenefitRefund();//易网聚鲜平台应退分润金额
        //计算店铺应退分润金额
        BigDecimal shopBenefitRefund = new BigDecimal(refundAmount).subtract(freight).subtract(ewfreshBenefitRefund);

        //组织请求报文
        JSONObject json = new JSONObject();
        //json.put(MSG_ID, );//消息ID，原样返回
        json.put(MSG_SRC, msgSrc);//消息来源
        json.put(MSG_TYPE, msgType_refund);//消息类型: refund
        String refundTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");// 商户发送退款请求的时间
        json.put(REQUEST_TIMESTAMP, refundTime);//退款请求时间
        //json.put(SRC_RESERVE, "");//请求系统预留字段
        json.put(MER_ORDER_ID, outTradeNo);//商户订单号 原交易订单号
        json.put(INST_MID, instMid);//业务类型 H5DEFAULT
        json.put(MID, mid);//商户号
        json.put(TID, tid);//终端号
        json.put(REFUND_AMOUNT, FenYuanConvert.yuan2Fen(refundAmount));//要退货的金额（是不是全款都需要传）
        // 确认是否退全款，是的话platformAmount、subOrders就不需要再传
        boolean isAllRefund = new BigDecimal(refundAmount).compareTo(payerPayAmount) == 0 ? true : false;
        if (!isAllRefund) {
            logger.info("H5ShopRefundPolicy. This is not a full refund but a part refund. outRequestNo = {}, " +
                    "payerPayAmount = {}, refundAmount = {}", outRequestNo, payerPayAmount, refundAmount);
            // 不是全款，则上送platformAmount、subOrders
            // ******************************自营运费算到主商户里了，不用再分账
//            SubOrder subOrder = new SubOrder();//上送运费退款
//            subOrder.setMid(freightShopMid);// ******************************测试填 898127210280001 或者 898127210280002
//            subOrder.setTotalAmount(freight.toString());

//            List<SubOrder> subOrderList = new ArrayList<>();
//            subOrderList.add(subOrder);
//            json.put(SUB_ORDERS, subOrderList);//子商户分账信息，包括子商户号、分账金额
            // 店铺应该退款金额
            List<SubOrder> subOrderList = new ArrayList<>();
            SubOrder shopSubOrder = new SubOrder();
            shopSubOrder.setMid(shopMid);
            shopSubOrder.setTotalAmount(FenYuanConvert.yuan2Fen(shopBenefitRefund.toString()).toString());
            subOrderList.add(shopSubOrder);
            json.put(SUB_ORDERS, subOrderList);//子商户分账信息，包括子商户号、分账金额
            // 平台退款金额
            BigDecimal platformAmount = FenYuanConvert.yuan2Fen(new BigDecimal(refundAmount).subtract(shopBenefitRefund).toString());
            json.put(PLATFORM_AMOUNT, platformAmount.toString());//分账金额。平台商户分账金额 若分账标记传，则分账金额必传
        }
        //json.put(REFUND_DESC, "");//退货说明
        String refundSequence = msgSrcId + BOBRefundSeqFormat.orderNo2UnionPayRefundSequence(outRequestNo);//msgSrcId + 生成28位的退款流水号
        json.put(REFUND_ORDER_ID, refundSequence);//生成32位的退款流水号
        json.put(SIGN_TYPE, MD5);//签名算法
        Map<String, String> paramsMap = UnionPayQrCodeUtil.jsonToMap(json);
        paramsMap.put(SIGN, UnionPayQrCodeUtil.makeSign(md5Key, paramsMap));
        logger.info("H5ShopRefundPolicy'result paramsMap = {}", paramsMap);

        List<Map<String, String>> paramsMapList = new ArrayList<>();
        paramsMapList.add(paramsMap);
        return paramsMapList;
    }
}
