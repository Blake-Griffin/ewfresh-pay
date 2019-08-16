package com.ewfresh.pay.util.boc;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangziyuan on 2018/8/14.
 */
public class MyHttp {
    private static final String USERTOKEN = "x-token";
    private static final String UID = "x-uid";
    private static final String TRACEID = "X-99Bill-TraceId";
    private static final String PLATFORMCODE = "X-99Bill-PlatformCode";
    private static final String SIGNATURE = "X-99Bill-Signature";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Map<String,Object> post(String url, String params, String uuid, String plantForm, String signal) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setConnectionManager(cm)
                .build();
        //实例化post方法
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(TRACEID, uuid);
        httpPost.setHeader(PLATFORMCODE, plantForm);
        httpPost.setHeader(SIGNATURE, signal);
        CloseableHttpResponse response = null;
        String content = "";
        HashMap<String, Object> map = new HashMap<>();
        try {
            //用于封装响应体

            //提交的参数
            StringEntity uefEntity = new StringEntity(params, "UTF-8");
            uefEntity.setContentType("application/json");
            //将参数给post方法
            httpPost.setEntity(uefEntity);
            logger.info("this is entity------>{}", EntityUtils.toString(httpPost.getEntity(), "utf-8"));
            //执行post方法
            response = httpclient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {

                content = EntityUtils.toString(response.getEntity(), "utf-8");
            } else {
                content = String.valueOf(response.getStatusLine().getStatusCode()) + EntityUtils.toString(response.getEntity(), "utf-8");

            }
            map.put("content",content);
            map.put("response",response);
        } catch (Exception e) {
            logger.error("have an exception--->{}", e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error("httpdal have an ioexception--->{}", e);
            }
        }
        return map;
    }

}
