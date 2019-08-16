package com.ewfresh.pay.util.unionpayh5pay;

/**
 * description: 商户订单号去掉来源系统
 * @author: JiuDongDong
 * date: 2019/5/27.
 */
public class MerOrderIdDelMsgSrcId {

    /**
     * Description: 商户订单号去掉来源系统
     * @author: JiuDongDong
     * @param oriMerOrderId 商户订单号，以来源系统开头
     * @param msgSrcId  来源系统
     * @return java.lang.String
     * date: 2019/5/27 17:12
     */
    public static String merOrderIdDelMsgSrcId(String oriMerOrderId, String msgSrcId) {
        return oriMerOrderId.substring(msgSrcId.length(), oriMerOrderId.length());
    }
}
