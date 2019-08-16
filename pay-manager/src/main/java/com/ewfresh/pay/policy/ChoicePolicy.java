package com.ewfresh.pay.policy;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.policy.impl.*;
import org.dom4j.DocumentException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wangziyuan on 2018/4/23.
 */
@Component
public class ChoicePolicy extends ApplicationObjectSupport {
    private static final String Balance = "1000";
    private static final String AliPay = "1001";
    private static final String WeiXinPay = "1002";
    private static final String BOC = "1003";
    private static final String BOB = "1004";
    private static final String QUICK_PAY_BOB = "1005";
    private static final String BILL99 = "1006";// 快钱个人网银
    private static final String BILL99_ENTERPRISE = "1035";// 快钱企业网银
    private static final String BILL99_QUICK = "1045";// 快钱个人快捷
    private static final String White = "1065";// 白条
    private static final String UNIONPAY_WEBWAP = "1067";// 中国银联WebWap
    private static final String UNIONPAY_QRCODE = "1068";// 中国银联QrCode
    private static final String UNIONPAY_H5Pay = "1069";// 中国银联H5Pay
    private static final String UNIONPAY_H5Pay_B2B = "1070";// 中国银联H5PayB2B


    public RefundPolicy getRefundPolicy(String param){
        RefundPolicy refundPolicy = null;
        try {
            ApplicationContext applicationContext = this.getApplicationContext();
            if (param.equals(Balance)) {
                refundPolicy = applicationContext.getBean(BalancePolicy.class);
            }
            if (param.equals(AliPay)) {
                refundPolicy = applicationContext.getBean(AliRefundPolicy.class);
            }
            if (param.equals(WeiXinPay)) {
                refundPolicy = applicationContext.getBean(WeiXinRefundPolicy.class);
            }
            if (param.equals(BOB)) {
                refundPolicy = applicationContext.getBean(BOBRefundPolicy.class);
            }
            if (param.equals(BOC)) {
                refundPolicy = applicationContext.getBean(BOCRefundPolicy.class);
            }
            if (param.equals(QUICK_PAY_BOB)) {
                refundPolicy = applicationContext.getBean(BOBRefundPolicy.class);// 银联暂用BOB的退款功能
            }
            if (param.equals(BILL99)) {
                refundPolicy = applicationContext.getBean(Bill99RefundPolicy.class);
            }
            if (param.equals(BILL99_ENTERPRISE)) {
                refundPolicy = applicationContext.getBean(Bill99RefundPolicy.class);// 快钱企业网银、快钱个人网银都使用Bill99RefundPolicy
            }
            if (param.equals(BILL99_QUICK)) {
                refundPolicy = applicationContext.getBean(Bill99QuickRefundPolicy.class);// 快钱个人快捷使用Bill99QuickRefundPolicy
            }
            if (param.equals(White)) {
                refundPolicy = applicationContext.getBean(WhiteRefundPolicy.class);// 白条
            }
            if (param.equals(UNIONPAY_WEBWAP)) {
                refundPolicy = applicationContext.getBean(UnionPayWebWapRefundPolicy.class);// 中国银联WebWap
            }
            if (param.equals(UNIONPAY_QRCODE)) {
                refundPolicy = applicationContext.getBean(UnionPayQRCodeRefundPolicy.class);// 中国银联QrCode
            }
            if (param.equals(UNIONPAY_H5Pay) || param.equals(UNIONPAY_H5Pay_B2B)) {
                refundPolicy = applicationContext.getBean(UnionPayH5PayRefundPolicy.class);// 中国银联H5Pay、中国银联H5PayB2B
            }
//            if (param.equals(UNIONPAY_H5Pay_B2B)) {
//                refundPolicy = applicationContext.getBean(UnionPayH5PayB2BRefundPolicy.class);// 中国银联H5PayB2B
//            }
        } catch (Exception e) {
            logger.error("failed to process handler",e);
        }
        return refundPolicy;
    }

    public List<PayFlow> refund(RefundParam refundParam,RefundPolicy refundPolicy) throws RefundParamNullException, UnsupportedEncodingException, RefundBill99ResponseNullException, PayFlowFoundNullException, DocumentException, RefundHttpToBill99FailedException, RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException {
        List<PayFlow> refund = refundPolicy.refund(refundParam);
        return refund;
    }
}
