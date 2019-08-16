package com.ewfresh.pay.util.bill99;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class Pkipair {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public String signMsg( String signMsg, String merchantCertPath, String merchantCertPss) {
		logger.info("start to signature!!!");
		String base64 = "";
		try {
			// 密钥仓库
			KeyStore ks = KeyStore.getInstance("PKCS12");

			// 读取密钥仓库
//			FileInputStream ksfis = new FileInputStream("e:/99bill-rsa.pfx");

			// 读取密钥仓库（相对路径）
//			String file = Pkipair.class.getResource("99bill.pfx").getPath().replaceAll("%20", " ");
			String file = Pkipair.class.getResource(merchantCertPath).toURI().getPath();
			logger.info("file = {}", file);
//			String file = "E:\\sunkfa\\ewfresh-pay\\ewfresh-pay\\pay-commons\\src\\main\\resources\\bill99shareproperties\\10012138843.pfx";
//			String file = merchantCertPath;
			logger.info("private key path: {}", merchantCertPath);
			logger.info("private key pwd: {}", merchantCertPss);

			FileInputStream ksfis = new FileInputStream(file);

			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);

//			char[] keyPwd = "123456".toCharArray();
			char[] keyPwd = merchantCertPss.toCharArray();
			//char[] keyPwd = "YaoJiaNiLOVE999Year".toCharArray();
			ks.load(ksbufin, keyPwd);
			// 从密钥仓库得到私钥
			PrivateKey priK = (PrivateKey) ks.getKey("test-alias", keyPwd);
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(priK);
			signature.update(signMsg.getBytes("utf-8"));
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			base64 = encoder.encode(signature.sign());

		} catch(FileNotFoundException e){
			logger.error("can not fount pfx file");
		} catch (Exception ex) {
			logger.error("error occurred!", ex);
		}
		logger.info("signMsg: {}", base64);
		return base64;
	}


	public boolean enCodeByCer( String val, String msg, String merchantPubPath) {
		logger.info("start to verify message");
		boolean flag = false;
		try {
			//获得文件(绝对路径)
			//InputStream inStream = new FileInputStream("e:/99bill[1].cert.rsa.20140803.cer");

			//获得文件(相对路径)
//			String file = Pkipair.class.getResource("99bill.cer").toURI().getPath();
//			String file = "D:\\mygit\\ewfresh-pay\\pay-commons\\src\\main\\resources\\bill99properties\\99bill.cer";
			String file = Pkipair.class.getResource(merchantPubPath).toURI().getPath();
//			String file = merchantPubPath;
			logger.info("private key path: {}", merchantPubPath);
			FileInputStream inStream = new FileInputStream(file);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			//获得公钥
			PublicKey pk = cert.getPublicKey();
			//签名
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(pk);
			signature.update(val.getBytes());
			//解码
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			logger.info("decode origin sign info: {}", new String(decoder.decodeBuffer(msg)));
			flag = signature.verify(decoder.decodeBuffer(msg));
			logger.info("verify result: {}", flag);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error occurred when verify signature info");
		}
		return flag;
	}
}
