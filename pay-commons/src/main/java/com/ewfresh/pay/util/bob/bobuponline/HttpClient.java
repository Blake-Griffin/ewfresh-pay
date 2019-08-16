package com.ewfresh.pay.util.bob.bobuponline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HttpClient {
    private URL url;
    private int connectionTimeout;
    private int readTimeOut;
    private String result;

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public HttpClient(String url, int connectionTimeout, int readTimeOut) {
        try {
            this.url = new URL(url);
            this.connectionTimeout = connectionTimeout;
            this.readTimeOut = readTimeOut;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public int send(Map<String, String> data, String encoding)
            throws Exception {
        try {
            HttpURLConnection httpURLConnection = createConnection(encoding);
      /*httpURLConnection.setRequestProperty("User-Agent", "MSIE");*/
            if (null == httpURLConnection) {
                throw new Exception("连接异常");
            }
            requestServer(httpURLConnection, getRequestParamString(data, encoding), encoding);

            this.result = response(httpURLConnection, encoding);
            return httpURLConnection.getResponseCode();
        } catch (Exception e) {
            throw e;
        }
    }

    private void requestServer(URLConnection connection, String message, String encoder)
            throws Exception {
        PrintStream out = null;
        try {
            connection.connect();
            out = new PrintStream(connection.getOutputStream(), false, encoder);
            out.print(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out)
                out.close();
        }
    }

    private String response(HttpURLConnection connection, String encoding) throws URISyntaxException, IOException, Exception {
        InputStream in = null;
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader br = null;
        String temp = null;
        try {
            in = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, encoding));
            while (null != (temp = br.readLine())) {
                sb.append(temp);
            }
            String str1 = sb.toString();

            return str1;
        } catch (Exception e) {
            return "";
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != in) {
                in.close();
            }
            if (null != connection)
                connection.disconnect();
        }
    }

    private HttpURLConnection createConnection(String encoding)
            throws ProtocolException {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) this.url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        httpURLConnection.setConnectTimeout(this.connectionTimeout);
        httpURLConnection.setReadTimeout(this.readTimeOut);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestProperty("Content-type", new StringBuilder().append("application/x-www-form-urlencoded;charset=").append(encoding).toString());

        httpURLConnection.setRequestMethod("POST");
        if ("https".equalsIgnoreCase(this.url.getProtocol())) {
            HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
            BaseHttpSSLSocketFactory factory = new BaseHttpSSLSocketFactory();
            husn.setSSLSocketFactory(factory);
            husn.setHostnameVerifier(new BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier());
            return husn;
        }

        if (httpURLConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(new HostnameVerifier() {

                public boolean verify(String arg0, SSLSession arg1) {

                    return true;
                }

            });
        }

        return httpURLConnection;
    }

    private String getRequestParamString(Map<String, String> requestParam, String coder) {
        if ((null == coder) || ("".equals(coder))) {
            coder = "UTF-8";
        }
        StringBuffer sf = new StringBuffer("");
        String reqstr = "";
        if ((null != requestParam) && (0 != requestParam.size())) {
            for (Map.Entry en : requestParam.entrySet()) {
                try {
                    sf.append(new StringBuilder().append((String) en.getKey()).append("=").append(((null == en.getValue()) || ("".equals(en.getValue()))) ? "" : URLEncoder.encode((String) en.getValue(), coder)).append("&").toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return "";
                }
            }
            reqstr = sf.substring(0, sf.length() - 1);
        }
        System.out.println("发送的数据-->" + "[" + reqstr + "]");
        return reqstr;
    }
}