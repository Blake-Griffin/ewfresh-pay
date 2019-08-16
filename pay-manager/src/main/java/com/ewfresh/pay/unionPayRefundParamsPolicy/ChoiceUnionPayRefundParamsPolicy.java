package com.ewfresh.pay.unionPayRefundParamsPolicy;

import com.ewfresh.pay.unionPayRefundParamsPolicy.impl.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;


import static com.ewfresh.pay.util.Constants.*;

/**
 * Description: 选择银联退款参数
 * @author: JiuDongDong
 * date: 2019/6/29 13:36
 */
@Component
public class ChoiceUnionPayRefundParamsPolicy extends ApplicationObjectSupport {

    public UnionPayRefundParamsPolicy ChoiceUnionPayRefundParamsPolicy(String channelType, String shopId, String refundType){
        UnionPayRefundParamsPolicy refundParamsPolicy = null;
        try {
            ApplicationContext applicationContext = this.getApplicationContext();
            //H5的处理
            if (UNIONPAY_H5Pay.equals(channelType)) {
                //自营
                if (STR_ZERO.equals(shopId)) {
                    //取消订单
                    if (REFUND_TYPE_CANCEL.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5EwfreshCancelPolicy.class);
                    }
                    //关闭订单
                    if (REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5EwfreshShutdownPolicy.class);
                    }
                    //退货退款
                    if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5EwfreshRefundPolicy.class);
                    }
                    //退补款
                    if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5EwfreshSupplementPolicy.class);
                    }
                }
                //店铺
                if (!STR_ZERO.equals(shopId)) {
                    //取消订单
                    if (REFUND_TYPE_CANCEL.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5ShopCancelPolicy.class);
                    }
                    //关闭订单
                    if (REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5ShopShutdownPolicy.class);
                    }
                    //退货退款
                    if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5ShopRefundPolicy.class);
                    }
                    //退补款
                    if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(H5ShopSupplementPolicy.class);
                    }
                }
            }

            //QrCode的处理
            if (UNIONPAY_QRCODE.equals(channelType)) {
                //自营
                if (STR_ZERO.equals(shopId)) {
                    //取消订单
                    if (REFUND_TYPE_CANCEL.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeEwfreshCancelPolicy.class);
                    }
                    //关闭订单
                    if (REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeEwfreshShutdownPolicy.class);
                    }
                    //退货退款
                    if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeEwfreshRefundPolicy.class);
                    }
                    //退补款
                    if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeEwfreshSupplementPolicy.class);
                    }
                }
                //店铺
                if (!STR_ZERO.equals(shopId)) {
                    //取消订单
                    if (REFUND_TYPE_CANCEL.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeShopCancelPolicy.class);
                    }
                    //关闭订单
                    if (REFUND_TYPE_SHUTDOWN.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeShopShutdownPolicy.class);
                    }
                    //退货退款
                    if (REFUND_TYPE_REFUNDS.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeShopRefundPolicy.class);
                    }
                    //退补款
                    if (REFUND_TYPE_SUPPLEMENT.equals(refundType)) {
                        refundParamsPolicy = applicationContext.getBean(QrCodeShopSupplementPolicy.class);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("failed to process handler",e);
        }
        return refundParamsPolicy;
    }

}
