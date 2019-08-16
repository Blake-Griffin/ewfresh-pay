package com.ewfresh.pay.util.unionpayh5payb2b;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 根据code打印相关信息
 * @author: JiuDongDong
 * date: 2019/6/12 15:23
 */
public final class UnionPayB2BLogUtil {
    private static Logger logger = LoggerFactory.getLogger(UnionPayB2BLogUtil.class);

    /**
     * Description: 打印银联处理订单支付交易结果
     * @author: JiuDongDong
     * @param respCode 银联返回的状态码
     * @param outTradeNo  订单号
     * date: 2019/6/12 15:31
     */
    public static void logTradeInfo(String respCode, String outTradeNo) {
        // 打印成功信息 start
        if ("00".equals(respCode)) {
            logger.info("The respCode = {}", respCode);
            logger.info(outTradeNo + "：交易成功");
            return;
        }
        // 打印成功信息 end

        // 打印错误信息
        logger.error("The respCode = " + respCode);
        if (StringUtils.isBlank(respCode)) {
            logger.error("The respCode is null");
            return;
        }
        switch (respCode) {
            case "01":
                logger.error(outTradeNo + "：受理失败");
                break;
            case "10":
                logger.error(outTradeNo + "：请求数据不符合要求");
                break;
            case "11":
                logger.error(outTradeNo + "：商户ID不符合要求");
                break;
            case "12":
                logger.error(outTradeNo + "：不为白名单IP");
                break;
            case "13":
                logger.error(outTradeNo + "：验证签名失败");
                break;
            case "14":
                logger.error(outTradeNo + "：流水号重复");
                break;
            case "15":
                logger.error(outTradeNo + "：验证商户号失败");
                break;
        }
    }
}
