package com.ewfresh.pay.util.bill99quick;


import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MyX509TrustManager implements X509TrustManager { 



    public MyX509TrustManager() throws Exception { 
	
    } 

    /* 
     * Delegate to the default trust manager. 
     */ 
    public void checkClientTrusted(X509Certificate[] chain, String authType) 
                throws CertificateException { 

    } 

    /* 
     * Delegate to the default trust manager. 
     */ 
    public void checkServerTrusted(X509Certificate[] chain, String authType) 
                throws CertificateException { 

    } 

    /* 
     * Merely pass this through. 
     */ 
    public X509Certificate[] getAcceptedIssuers() { 	
        return new X509Certificate[0];
    } 
}
