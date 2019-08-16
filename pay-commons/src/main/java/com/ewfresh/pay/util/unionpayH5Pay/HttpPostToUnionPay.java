package com.ewfresh.pay.util.unionpayh5pay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * description: 向银联发送post请求
 * @author: JiuDongDong
 * date: 2019/5/17.
 */
public class HttpPostToUnionPay {
    private static Logger logger = LoggerFactory.getLogger(HttpPostToUnionPay.class);

    /**
     * Description: 向银联发送post请求
     * @author: JiuDongDong
     * @param APIurl 请求url
     * @param paramsMap 请求参数（内含sign）
     * @return com.alibaba.fastjson.JSONObject 请求响应
     * date: 2019/5/17 14:09
     */
    public static synchronized JSONObject httpPostToUnionPay(String APIurl, Map<String, String> paramsMap) throws Exception {
//        Map<String, Object> paramsMapObj = new HashMap<>();
//        for (String key : paramsMapObj.keySet()) {
//            paramsMapObj.put(key, paramsMap.get(key));
//        }
//        JSONObject jsonObject = httpPostToUnionPayUtil(APIurl, paramsMapObj);
//        return jsonObject;

        // Map转json
        String strReqJsonStr = JSON.toJSONString(paramsMap);
        strReqJsonStr = strReqJsonStr.replaceAll("\\\\", "");

        // 请求
        HttpURLConnection httpURLConnection = null;
        BufferedReader in;
        PrintWriter out = null;
        try {
            java.net.URL url = new URL(APIurl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content_Type", "application/json");
            httpURLConnection.setRequestProperty("Accept_Charset", "UTF-8");
            httpURLConnection.setRequestProperty("contentType", "UTF-8");
            //发送POST请求参数
            out = new PrintWriter(httpURLConnection.getOutputStream());
//            out = new OutputStreamWriter(httpURLConnection.getOutputStream(),"utf-8");
            out.write(strReqJsonStr);
//            out.println(strReqJsonStr);
            out.flush();

            //读取响应
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer content = new StringBuffer();
                String tempStr;
                in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while ((tempStr = in.readLine()) != null) {
                    content.append(tempStr);
                }
                logger.info("content：{}", content.toString());
                //转换成json对象
                JSONObject respJsonObj = JSON.parseObject(content.toString(), Feature.OrderedField);
                return respJsonObj;
            }
        } catch (Exception e) {
            logger.error("httpPostToUnionPay occurred error", e);
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
            httpURLConnection.disconnect();
        }
        throw new RuntimeException("httpPostToUnionPay occurred error");
    }

    /**
     * Description: 向银联发送post请求
     * @author: JiuDongDong
     * @param APIurl 请求url
     * @param paramsMap 请求参数（内含sign）
     * @return com.alibaba.fastjson.JSONObject 请求响应
     * date: 2019/6/19 17:49
     */
    public static synchronized JSONObject httpPostToUnionPayObject(String APIurl, Map<String, Object> paramsMap) throws Exception {
        JSONObject jsonObject = httpPostToUnionPayUtil(APIurl, paramsMap);
        return jsonObject;
    }

    /**
     * Description: 向银联发送post请求
     * @author: JiuDongDong
     * @param APIurl 请求url
     * @param paramsMap 请求参数（内含sign）
     * @return com.alibaba.fastjson.JSONObject 请求响应
     * date: 2019/6/19 17:59
     */
    private static synchronized JSONObject httpPostToUnionPayUtil(String APIurl, Map<String, Object> paramsMap) throws Exception {
        // Map转json
        String strReqJsonStr = JSON.toJSONString(paramsMap);
        strReqJsonStr = strReqJsonStr.replaceAll("\\\\", "");

        // 请求
        HttpURLConnection httpURLConnection = null;
        BufferedReader in;
        PrintWriter out = null;
        try {
            java.net.URL url = new URL(APIurl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content_Type", "application/json");
            httpURLConnection.setRequestProperty("Accept_Charset", "UTF-8");
            httpURLConnection.setRequestProperty("contentType", "UTF-8");
            //发送POST请求参数
            out = new PrintWriter(httpURLConnection.getOutputStream());
//            out = new OutputStreamWriter(httpURLConnection.getOutputStream(),"utf-8");
            out.write(strReqJsonStr);
//            out.println(strReqJsonStr);
            out.flush();

            //读取响应
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer content = new StringBuffer();
                String tempStr;
                in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while ((tempStr = in.readLine()) != null) {
                    content.append(tempStr);
                }
                logger.info("content：{}", content.toString());
                //转换成json对象
                JSONObject respJsonObj = JSON.parseObject(content.toString(), Feature.OrderedField);
                return respJsonObj;
            }
        } catch (Exception e) {
            logger.error("httpPostToUnionPay occurred error", e);
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
            httpURLConnection.disconnect();
        }
        throw new RuntimeException("httpPostToUnionPay occurred error");
    }
}