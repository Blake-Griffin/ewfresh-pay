package com.ewfresh.pay.util.unionpayh5pay;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyStore;
import java.util.List;

@SuppressWarnings("all")
public class UnionPayHttpsClient {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String keyStoreTypeJks = "jks";
    private String keyStoreTypeP12 = "PKCS12";
    private String schemeHttps = "https";
    private int httpsPort = 8443;
    private String httpsUrl = "";
    private String keyStorePath = "";
    private String trustStorePath = "";
    private String keyStorePassword = "";
    private String trustStorePassword = "";
    private HttpClient httpClient = null;
    private InputStream ksIn = null;
    private InputStream tsIn = null;

    public UnionPayHttpsClient() {
    }

    public UnionPayHttpsClient(String httpsUrl, String keyStorePath,
                               String trustStorePath, String keyStorePassword,
                               String trustStorePassword) {
        this.httpsUrl = httpsUrl;
        this.keyStorePath = keyStorePath;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.trustStorePassword = trustStorePassword;
    }

    public UnionPayHttpsClient(String httpsUrl, InputStream ksIn,
                               InputStream tsIn, String keyStorePassword,
                               String trustStorePassword) {
        this.httpsUrl = httpsUrl;
        this.ksIn = ksIn;
        this.tsIn = tsIn;
        this.keyStorePassword = keyStorePassword;
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * <p>
     * 功能描述:[注册]
     * </p>
     *
     * @author:yhao
     * @date:2014-8-12/下午4:43:54
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    private void register() {
        InputStream ksIn = null;
        InputStream tsIn = null;
        try {
            httpClient = new DefaultHttpClient();
            KeyStore keyStore = KeyStore.getInstance(keyStoreTypeP12);
            KeyStore trustStore = KeyStore.getInstance(keyStoreTypeJks);
            ksIn = this.ksIn != null ? this.ksIn : new FileInputStream(new File(keyStorePath));
            tsIn = this.tsIn != null ? this.tsIn : new FileInputStream(new File(trustStorePath));
            keyStore.load(ksIn, keyStorePassword.toCharArray());
            trustStore.load(tsIn, trustStorePassword.toCharArray());
            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore,
                    keyStorePassword, trustStore);
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme(schemeHttps, httpsPort, socketFactory);
            httpClient.getConnectionManager().getSchemeRegistry().register(sch);
        } catch (Exception e) {
            logger.error("register occurred error!", e);
        } finally {
            try {
                if (ksIn != null) {
                    ksIn.close();
                }
                if (tsIn != null) {
                    tsIn.close();
                }
            } catch (Exception e2) {
                logger.error("Error occurred!", e2);
            }
        }
    }


    /**
     * @param content
     * @return
     */
    public String httpsPost(String content) {
        String responseMessage = "";
        BufferedReader bufferedReader = null;
        try {
            register();
            HttpPost httpPost = new HttpPost(httpsUrl);
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 0);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 0);
            // 构造最简单的字符串数据    
            StringEntity reqEntity = new StringEntity(content);
            // 设置类型
            reqEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
            // 设置请求的数据
            httpPost.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        entity.getContent()));
                StringBuffer stb = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stb.append(line);
                }
                responseMessage = stb.toString();
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            logger.error("httpsPost occurred error!", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("IOException occurred!", e);
                }
            }
            httpClient.getConnectionManager().shutdown();
        }
        return responseMessage;
    }

    /**
     * <p>
     * 功能描述:[httpsPost]
     * </p>
     *
     * @param nvps
     * @return
     * @author:yhao
     * @date:2014-8-12/下午4:44:21
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */

    public String httpsPost(List<NameValuePair> nvps) {
        String responseMessage = "";
        BufferedReader bufferedReader = null;
        try {
            register();
            HttpPost httpPost = new HttpPost(httpsUrl);
            // 设置请求和传输超时时间 setSocketTimeout:数据传输超时 ；setConnectTimeout：链接超时
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 0);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 0);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        entity.getContent()));
                StringBuffer stb = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stb.append(line);
                }
                responseMessage = stb.toString();
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            logger.error("httpsPost occurred error!", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("IOException occurred!", e);
                }
            }
            httpClient.getConnectionManager().shutdown();
        }
        return responseMessage;
    }

    /**
     * <p>
     * 功能描述:[httpsGet]
     * </p>
     *
     * @param nvps
     * @return
     * @author:yhao
     * @date:2014-8-12/下午4:46:20
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public String httpsGet(String requestContent) {
        String responseMessage = "";
        BufferedReader bufferedReader = null;
        try {
            register();
            logger.info("httpsUrl===============" + httpsUrl);
            HttpGet httpGet = new HttpGet(httpsUrl + "?" + requestContent);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            // 设置请求和传输超时时间 setSocketTimeout:数据传输超时 ；setConnectTimeout：链接超时
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 0);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 0);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
                StringBuffer stb = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stb.append(line);
                }
                responseMessage = stb.toString();
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            logger.error("httpsGet occurred error!", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseMessage;
    }

    public String getKeyStoreTypeJks() {
        return keyStoreTypeJks;
    }

    public void setKeyStoreTypeJks(String keyStoreTypeJks) {
        this.keyStoreTypeJks = keyStoreTypeJks;
    }

    public String getKeyStoreTypeP12() {
        return keyStoreTypeP12;
    }

    public void setKeyStoreTypeP12(String keyStoreTypeP12) {
        this.keyStoreTypeP12 = keyStoreTypeP12;
    }

    public String getSchemeHttps() {
        return schemeHttps;
    }

    public void setSchemeHttps(String schemeHttps) {
        this.schemeHttps = schemeHttps;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getHttpsUrl() {
        return httpsUrl;
    }

    public void setHttpsUrl(String httpsUrl) {
        this.httpsUrl = httpsUrl;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


}
