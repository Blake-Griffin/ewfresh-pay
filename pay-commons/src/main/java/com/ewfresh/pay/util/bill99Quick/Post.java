package com.ewfresh.pay.util.bill99quick;


import com.ewfresh.pay.model.bill99quick.TransInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;

public class Post {
    private static Logger logger = LoggerFactory.getLogger(Post.class);
    /**
     * 向指定URL发送POST方法的请求
     *
     * @param url   发送请求的URL
     * @param tr1XML 请求参数，请求参数应该是name1=value1&name2=value2的形式。
     * @return URL所代表远程资源的响应
     * @throws Exception
     */
    public static HashMap sendPost(String url, String tr1XML, TransInfo transInfo, String merchantCertPath,
                                   String merchantCertPss, String merIdNotHat) throws Exception {
        //jdk1.7提示：javax.net.ssl.SSLProtocolException: handshake alert: unrecognized_name
        //方案1：设置系统属性：System.setProperty("jsse.enableSNIExtension", "false");
        //方案2：如果是tomcat:
        //                  1. linux，在catalina.sh加上JAVA_OPTS="$JAVA_OPTS -Djsse.enableSNIExtension=false"
        //                  2. win，在catalina.bat加上JAVA_OPTS="$JAVA_OPTS -Djsse.enableSNIExtension=false"
        System.setProperty("jsse.enableSNIExtension", "false");
        ParseUtil parseUtil = new ParseUtil();
        OutputStream out = null;
        HashMap respXml = null;
//        String jksFile = Post.class.getResource("10411004511201290.jks").toURI().getPath();
        String jksFile = Post.class.getResource(merchantCertPath).toURI().getPath();
        //获取证书路径
        File certFile = new File(jksFile);  //测试证书，生产环境需要替换
        //访问Java密钥库，JKS是keytool创建的Java密钥库，保存密钥。
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(certFile), merchantCertPss.toCharArray());
        //创建用于管理JKS密钥库的密钥管理器
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        //初始化证书
        kmf.init(ks, merchantCertPss.toCharArray());

        //同位体验证信任决策源//同位体验证可信任的证书来源
        TrustManager[] tm = {new MyX509TrustManager()};

        //初始化安全套接字
        SSLContext sslContext = SSLContext.getInstance("SSL");
        //初始化SSL环境。第二个参数是告诉JSSE使用的可信任证书的来源，设置为null是从javax.net.ssl.trustStore中获得证书。
        //第三个参数是JSSE生成的随机数，这个参数将影响系统的安全性，设置为null是个好选择，可以保证JSSE的安全性。
        sslContext.init(kmf.getKeyManagers(), tm, null);

        //根据上面配置的SSL上下文来产生SSLSocketFactory,与通常的产生方法不同
        SSLSocketFactory factory = sslContext.getSocketFactory();

        try {
            URL realUrl = new URL(url);
            //打开和URL之间的连接
            HttpsURLConnection conn = (HttpsURLConnection) realUrl.openConnection();
            //创建安全的连接套接字
            conn.setSSLSocketFactory(factory);
            //发送POST请求必须设置如下两行,使用 URL 连接进行输出、入
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //设置URL连接的超时时限
            conn.setReadTimeout(100000);

            //设置通用的请求属性
            String authString = merIdNotHat + ":" + merchantCertPss;//测试帐号，生产环境需要替换生产商户编号
            String auth = "Basic " + Base64Binrary.encodeBase64Binrary(authString.getBytes());
            conn.setRequestProperty("Authorization", auth);

            // 获取URLConnection对象对应的输出流
            out = conn.getOutputStream();
            //发送请求参数
            out.write(tr1XML.getBytes("utf-8"));

            //flush 输出流的缓冲
            out.flush();

            //得到服务端返回
            InputStream is = conn.getInputStream();
            String reqData = "";
            if (is != null && !"".equals(is)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] receiveBuffer = new byte[2048];//缓冲区长度
                int readBytesSize = is.read(receiveBuffer);//读取数据长度，InputStream要读取的数据长度一定要小于等于缓冲区中的字节数

                while (readBytesSize != -1) {//判断流是否位于文件末尾而没有可用的字节
                    bos.write(receiveBuffer, 0, readBytesSize);//从receiveBuffer内存处的0偏移开始写，写与readBytesSize长度相等的字节
                    readBytesSize = is.read(receiveBuffer);
                }
                reqData = new String(bos.toByteArray(), "UTF-8");//编码后的tr2报文
            }
            logger.info("tr2 response xml: " + reqData);
            respXml = parseUtil.parseXML(reqData, transInfo);//给解析XML的函数传递快钱返回的TR2的XML数据流
        } catch (Exception e) {
            logger.error("Post request to bill99 PCI occurred error", e);
            throw e;
        } finally {
            //使用finally块来关闭输出流、输入流
            if (out != null) {
                out.close();
            }
        }
        return respXml;
    }

}