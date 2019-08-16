package com.ewfresh.pay.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * description:前台交易构造HTTP POST自动提交表单
 * @author: JiuDongDong
 * date: 2019/8/7.
 */
public class CreateAutoFormHtmlUtil {

    /**
     * Description: 前台交易构造HTTP POST自动提交表单
     * @author: JiuDongDong
     * @param httpUrl 表单提交地址
     * @param reqParam 以MAP形式存储的表单键值
     * @param encoding  上送请求报文域encoding字段的值
     * @param method  请求方式：post、get
     * @return java.lang.String 构造好的HTTP 交易表单
     * date: 2019/8/7 17:32
     */
    public static synchronized String createAutoFormHtml(String httpUrl, Map<String, String> reqParam, String encoding, String method) {
        reqParam = filterBlank(reqParam);
        StringBuffer sf = new StringBuffer();
        sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding + "\"/></head><body>");
        sf.append("<form id = \"kqPay\" action=\"" + httpUrl + "\" method=\"" + method + "\">");
        if (null != reqParam && 0 != reqParam.size()) {
            Set<Map.Entry<String, String>> set = reqParam.entrySet();
            Iterator<Map.Entry<String, String>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> ey = it.next();
                String key = ey.getKey();
                String value = ey.getValue();
                sf.append("<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\"/>");
//                sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + value + "\"/>");
            }
        }
        sf.append("</form>");
        sf.append("</body>");
        sf.append("<script type=\"text/javascript\">");
        sf.append("document.all.kqPay.submit();");
        sf.append("</script>");
        sf.append("</html>");
        return sf.toString();
    }

    /**
     * Description: 过滤请求报文中的空字符串或者空字符串
     * @author: JiuDongDong
     * @param reqParam  请求数据
     * @return java.util.Map<java.lang.String,java.lang.String> 过滤请求报文中的空字符串或者空字符串
     * date: 2019/8/7 17:32
     */
    public static synchronized Map<String, String> filterBlank(Map<String, String> reqParam) {
        Map<String, String> submitFromData = new HashMap<>();
        Set<String> keySet = reqParam.keySet();
        for (String key : keySet) {
            String value = reqParam.get(key);
            if (value != null && !"".equals(value.trim())) {
                // 去除value值前后的空
                submitFromData.put(key, value.trim());
            }
        }
        return submitFromData;
    }
}
