package com.ewfresh.pay.util.bill99share;

/**
 * description: 将变量值不为空的参数组成字符串
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
public class Bill99AppendParamUtil {

    /**
     * Description: 将变量值不为空的参数组成字符串
     * @author: JiuDongDong
     * @param returnStr
     * @param paramId
     * @param paramValue
     * @return java.lang.String
     * date: 2019/8/7 17:47
     */
    public static synchronized String appendParam(String returnStr, String paramId, String paramValue) {
        if (!returnStr.equals("")) {
            if (!paramValue.equals("")) {
                returnStr += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (!paramValue.equals("")) {
                returnStr = paramId + "=" + paramValue;
            }
        }
        return returnStr;
    }

}
