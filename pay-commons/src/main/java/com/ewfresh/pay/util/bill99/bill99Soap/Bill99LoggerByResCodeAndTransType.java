package com.ewfresh.pay.util.bill99.bill99Soap;

import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *      根据code打印相关信息
 * @author: JiuDongDong
 * date: 2018/8/7.
 */
public final class Bill99LoggerByResCodeAndTransType {
    private static Logger logger = LoggerFactory.getLogger(Bill99LoggerByResCodeAndTransType.class);

    /**
     * Description: 根据code打印相关信息
     * @author: JiuDongDong
     * @param resCode  状态码
     * date: 2018/8/7 14:11
     */
    public static void logInfo(String resCode) {
        if (StringUtils.isBlank(resCode)) {
            logger.warn("The resCode for logInfo is empty");
            return;
        }
        String transTypeDesp = "";
        switch (resCode) {
            case "10000" :
                logger.error("未知错误");
                break;
            case "10002" :
                logger.error("不支持的的的返回类型");
                break;
            case "10003" :
                logger.error("不合法的页面返回地址");
                break;
            case "10004" :
                logger.error("不合法的后台返回地址");
                break;
            case "10005" :
                logger.error("不支持的网关接口版本");
                break;
            case "10006" :
                logger.error("商家 mechantAcctId非法");
                break;
            case "10007" :
                logger.error("输入的查询时间段违法");
                break;
            case "10008" :
                logger.error("不支持的签名类型");
                break;
            case "10009" :
                logger.error("解密验签失败");
                break;
            case "10010" :
                logger.error("版本号不能为空");
                break;
            case "10011" :
                logger.error("不支持的日期类型");
                break;
            case "10012" :
                logger.info("没有数据");
                break;
            case "10013" :
                logger.error("查询出错");
                break;
            case "10014" :
                logger.error("帐户号为空");
                break;
            case "10015" :
                logger.error("验签字段不能为空");
                break;
            case "10016" :
                logger.error("签名类型不能为空");
                break;
            case "10017" :
                logger.error("退款查询时间不能为空");
                break;
            case "10018" :
                logger.error("额外输出参数不正确戒不存在");
        }
    }
}
