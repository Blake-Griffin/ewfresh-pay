package com.ewfresh.pay.util.bob;

import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *      根据code打印相关信息
 * @author: JiuDongDong
 * date: 2018/4/25.
 */
public final class BOBLoggerByResCodeAndTransType {
    private static Logger logger = LoggerFactory.getLogger(BOBLoggerByResCodeAndTransType.class);

    /**
     * Description: 根据code打印相关信息
     * @author: JiuDongDong
     * @param resCode  状态码
     * @param transType  交易类型 01-消费 04-退款  查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
     * date: 2018/4/25 15:24
     */
    public static void logInfo(String resCode, String transType) {
        if (StringUtils.isBlank(resCode)) {
            logger.warn("The resCode for logInfo is empty");
            return;
        }
        String transTypeDesp = "";
        if (Constants.BOB_TRANS_TYPE01.equals(transType)) {
            transTypeDesp = "订单支付：";
        }
        if (Constants.BOB_TRANS_TYPE04.equals(transType)) {
            transTypeDesp = "商户退款：";
        }
        if (Constants.BOB_TRANS_TYPE35.equals(transType)) {
            transTypeDesp = "全部退款：";
        }
        if (Constants.BOB_TRANS_TYPE36.equals(transType)) {
            transTypeDesp = "部分退款：";
        }
        switch (resCode) {
            case "0000" :
                logger.info(transTypeDesp + "交易成功");
                break;
            case "0001" :
                logger.error(transTypeDesp + "签名验签失败");
                break;
            case "0002" :
                logger.error(transTypeDesp + "发起订单已存在");
                break;
            case "0005" :
                logger.error(transTypeDesp + "商户号错误");
                break;
            case "0006" :
                logger.error(transTypeDesp + "订单原纪录不存在");
                break;
            case "0007" :
                logger.error(transTypeDesp + "商户上送报文格式错误");
                break;
            case "0008" :
                logger.error(transTypeDesp + "订单可退金额不足");
                break;
            case "0009" :
                logger.error(transTypeDesp + "订单信息不符");
                break;
            case "0010" :
                logger.error(transTypeDesp + "该商户不支持移动端交易");
                break;
            case "0011" :
                logger.error(transTypeDesp + "该商户不支持pc端交易");
                break;
            case "0012" :
                logger.error(transTypeDesp + "该二级商户唯一标识不存在或已停用");
                break;
            case "0013" :
                logger.error(transTypeDesp + "上送退款二级商户唯一标识与原消费二级商户唯一标识不符");
                break;
            case "0014" :
                logger.error(transTypeDesp + "原支付订单为普通或平台商户，不支持二次清分商户退款");
                break;
            case "0015" :
                logger.error(transTypeDesp + "原支付订单为二次清分商户，不支持普通或平台商户退款");
                break;
            case "0016" :
                logger.error(transTypeDesp + "无成功缴费记录");
                break;
            case "0017" :
                logger.error(transTypeDesp + "已存在该退款订单");
                break;
            case "0018" :
                logger.error(transTypeDesp + "原交易退款期限已失效");
                break;
            case "0020" :
                logger.error(transTypeDesp + "该一级商户号已停用");
        }
    }
}
