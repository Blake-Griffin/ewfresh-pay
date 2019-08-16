package com.ewfresh.pay.util;

/**
 * Created by Administrator on 2017/8/21 0021.
 */

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.IOException;

/**
 * 在java中处理http请求.
 *
 * @author nagsh
 */
public class BindHttpDeal {
    /**
     * 处理get请求.
     *
     * @param url 请求路径
     * @return json
     */
    private static final String USERTOKEN = "x-token";
    private static final String UID = "x-uid";
    private static final String TRACEID = "X-99Bill-TraceId";
    private static final String PLATFORMCODE = "X-99Bill-PlatformCode";
    private static final String SIGNATURE = "X-99Bill-Signature";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String get(String url, String token) {
        //实例化httpclient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //实例化get方法
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(USERTOKEN, token);
        //请求结果
        CloseableHttpResponse response = null;
        String content = "";
        try {
            //执行get方法
            response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == 200) {
                content = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            logger.error("have an exception--->{}", e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error("httpdal have an ioexception--->{}", e);
            }
        }
        return content;
    }

    /**
     * 处理post请求.
     *
     * @param url    请求路径
     * @param params 参数
     * @return json
     */
    public String post(String url, String params, String uuid, String plantForm, String Signal) throws Exception {
        //实例化httpClient
        CloseableHttpClient httpclient = HttpClients.createMinimal();
        //实例化post方法
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(TRACEID, uuid);
        httpPost.setHeader(PLATFORMCODE, plantForm);
        httpPost.setHeader(SIGNATURE, Signal);
        CloseableHttpResponse response = null;
        String content = "";
        try {
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
        } catch (Exception e) {
            logger.error("have an exception--->{}", e);
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                logger.error("httpdal have an ioexception--->{}", e);
            }
        }
        return content;
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                .getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    }

    static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
}