/*
 * Created on 2008-9-19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.ewfresh.pay.util.bob.bobutil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

/**
* @author GuoDong Ni
*
* To change the template for this generated type comment go to
* Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
*/
public class CertUtils {
	private static final Logger logger = LoggerFactory.getLogger(BOBSdkUtil.class);
	private static final String JKS = "JKS";
	private static final String P12 = "P12";
	private static final String PKCS12 = "PKCS12";
	private static final String JCEKS = "JCEKS";
	private static final String JCK = "JCK";
	private static final String PFX = "PFX";

	static{
		if(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
			System.out.println("security provider BC not found");
			Security.addProvider(new BouncyCastleProvider());
		}
	}
	/**
	 *  获取信息摘要
	 * @param textBytes 原信息
	 * @param algorithm 算法
	 * @return 返回该算法的信息摘要
	 * @throws Exception
	 */
	public static byte[] msgDigest(byte[] textBytes, String algorithm)
		throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
		messageDigest.update(textBytes);
		return messageDigest.digest();
	}

	/**
	 * 通过证书获取公钥
	 * @param certPath 证书的路径
	 * @return 返回公钥
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String certPath) throws Exception {
		logger.info("certPath = {}", certPath);
		InputStream streamCert = new FileInputStream(certPath);
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		Certificate cert = factory.generateCertificate(streamCert);
		if(streamCert!=null){
			logger.error("streamCert!=null");
			streamCert.close();
		}
		logger.info("cert.getPublicKey()=" + cert.getPublicKey());
		return cert.getPublicKey();
	}
	/**
	 * 通过密钥文件或者证书文件获取私钥
	 * @param keyPath  密钥文件或者证书的路径
	 * @param passwd   密钥文件或者证书的密码
	 * @return 返回私钥
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String keyPath, String passwd)
		throws Exception {
		String keySuffix = keyPath.substring(keyPath.indexOf(".") + 1);
		String keyType = JKS;
		if (keySuffix == null || keySuffix.trim().equals("")) {
			keyType = JKS;
		} else {
			keySuffix = keySuffix.trim().toUpperCase();
        }

		if (keySuffix.equals(P12)) {
			keyType = PKCS12;
		} else if (keySuffix.equals(PFX)) {
			keyType = PKCS12;
		} else if (keySuffix.equals(JCK)) {
			keyType = JCEKS;
		} else {
			keyType = JKS;
        }

		return getPrivateKey(keyPath, passwd, keyType);

	}

	/**
	 * 通过证书或者密钥文件获取私钥
	 * @param keyPath  证书或者密钥文件
	 * @param passwd   密钥保存密码
	 * @param keyType  密钥保存类型
	 * @return    返回私钥
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(
		String keyPath,
		String passwd,
		String keyType)
		throws Exception {


		KeyStore ks = KeyStore.getInstance(keyType);
		char[] cPasswd = passwd.toCharArray();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(keyPath);		
			ks.load(fis, cPasswd);		
			fis.close();
		} finally {
			if (fis != null) {
				fis.close();
				fis = null;
			}
		}	
		Enumeration aliasenum = ks.aliases();
		String keyAlias = null;
		PrivateKey key = null;			
		while (aliasenum.hasMoreElements()) {
			keyAlias = (String) aliasenum.nextElement();
			key = (PrivateKey) ks.getKey(keyAlias, cPasswd);
			if (key != null) {
				break;
            }
		}		
		return key;
	}
	/**
	 * 	使用私钥签名 
	 * @param priKey 私钥
	 * @param b 需要签名的byte 数组
	 * @return 返回签名后的byte
	 * @throws Exception
	 */
	public static byte[] sign(PrivateKey priKey, byte[] b) throws Exception {
//		Signature sig = Signature.getInstance(priKey.getAlgorithm());
		Signature sig = Signature.getInstance("SHA1withRSA");
		
		sig.initSign(priKey);
		sig.update(b);
		return sig.sign();
	}
	/**
	 * 	使用公钥验证 
	 * @param pubKey 公钥
	 * @param orgByte 原始数据byte 数组
	 * @param signaByte 签名后的数据byte 数组
	 * @return 是否验证结果
	 * @throws Exception
	 */
	public static boolean verify(PublicKey pubKey,byte[] orgByte,byte[] signaByte)
		throws Exception {
//		Signature sig = Signature.getInstance(pubKey.getAlgorithm());
//		Signature sig = Signature.getInstance("SHA1withRSA");// 原来的
		Signature sig = Signature.getInstance("SHA256withRSA");// 新的
		logger.info("the algorithm: " + pubKey.getAlgorithm());
		logger.info("signature: " + sig);
		sig.initVerify(pubKey);
		sig.update(orgByte);
		return sig.verify(signaByte);
	}
	/**
	 *  使用公钥加密
	 * @param pubKey 公钥 
	 * @param plainText 需要加密的 byte 数组
	 * @return 返回加密的 byte 数据
	 * @throws Exception
	 */
	public static byte[] keyEncode(Key pubKey, byte[] plainText)
		throws Exception {
		return doFinal(pubKey, plainText, Cipher.ENCRYPT_MODE);
	}

	/**
	 *  使用私钥解密
	 * @param priKey 私钥 
	 * @param encrypText 需要解密的 byte 数组
	 * @return 返回解密的 byte 数据
	 * @throws Exception
	 */

	public static byte[] keyDecode(Key priKey, byte[] encrypText)
		throws Exception {
		return doFinal(priKey, encrypText, Cipher.DECRYPT_MODE);
	}

	private static byte[] doFinal(Key key, byte[] textBytes, int MODE)
		throws Exception {
//		Cipher cipher =
//			Cipher.getInstance(key.getAlgorithm(), new BouncyCastleProvider());
		Cipher cipher = Cipher.getInstance(key.getAlgorithm(),BouncyCastleProvider.PROVIDER_NAME);
		
		cipher.init(MODE, key);
		int blockSize = cipher.getBlockSize();
		int textLength = textBytes.length;

		byte[] retBytes = new byte[0];
		int loop = textLength / blockSize;
		int mod = textLength % blockSize;
		if (loop == 0) {
			return cipher.doFinal(textBytes);
        }
		for (int i = 0; i < loop; i++) {
			byte[] dstBytes = new byte[blockSize];
			System.arraycopy(textBytes, i * blockSize, dstBytes, 0, blockSize);
			byte[] encryBytes = cipher.doFinal(dstBytes);
			retBytes = appendArray(retBytes, encryBytes);
		}
		if (mod != 0) {
			int iPos = loop * blockSize;
			int leavingLength = textLength - iPos;
			byte[] dstBytes = new byte[leavingLength];
			System.arraycopy(textBytes, iPos, dstBytes, 0, leavingLength);
			byte[] encryBytes = cipher.doFinal(dstBytes);
			retBytes = appendArray(retBytes, encryBytes);
		}
		return retBytes;

	}
	
	/**
	 *  把 数组1 与数组2 相加。
	 * @param src 开始数组，
	 * @param dst 结束数组
	 * @return 返回 开始数组+结束数据
	 */
	public static byte[] appendArray(byte[] src, byte[] dst)
	{
		byte[] newBytes = new byte[src.length + dst.length];
		System.arraycopy(src, 0, newBytes, 0, src.length);
		System.arraycopy(dst, 0, newBytes, src.length, dst.length);
		return newBytes;
	}
}
