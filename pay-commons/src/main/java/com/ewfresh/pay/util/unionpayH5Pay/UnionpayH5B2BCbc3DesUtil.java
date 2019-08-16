package com.ewfresh.pay.util.unionpayh5pay;


import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@SuppressWarnings("all")
public class UnionpayH5B2BCbc3DesUtil {

	//加密方式
	private static String algorithm = "DES/CBC/PKCS5Padding";
	//偏移量 必须是8位数字
	private static String ivKey = "66553214";
	//8位key
	private static String keyAlgorithm = "!@#$%^&*";
	
	private static String type = "UTF-8";
	
	/**
	 * 
	 * <p>功能描述:[添加PKCS7Padding支持]</p>
	 * @author:yhao
	 * @date:2014-5-20/下午2:24:49
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	private  static void initProvider()
	{
		// 添加PKCS7Padding支持
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	/**
	 * 
	 * <p>功能描述:[解密方法]</p>
	 * @param decryptString
	 * @return
	 * @throws Exception
	 * @author:yhao
	 * @date:2014-5-20/下午2:43:18
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	public static String decrypt(String decryptString)
			throws Exception {
		if(decryptString == null || "".equals(decryptString))
		{
			return "";
		}
		
		initProvider();
		IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
		SecretKeySpec key = new SecretKeySpec(keyAlgorithm.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		return new String(cipher.doFinal(Base64.decode(decryptString)), type);
	}

	/**
	 * 
	 * <p>功能描述:[加密算法]</p>
	 * @param encryptString
	 * @return
	 * @throws Exception
	 * @author:yhao
	 * @date:2014-5-20/下午2:43:35
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	public static String encrypt(String encryptString)
			throws Exception {
		if (encryptString == null || "".equals(encryptString)) {
			return "";
		}

		initProvider();
		IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
		DESKeySpec dks = new DESKeySpec(keyAlgorithm.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] ciphers = cipher.doFinal(encryptString.getBytes(type));
		return new String(Base64.encode(ciphers));
	}

	public static String getAlgorithm() {
		return algorithm;
	}

	public static void setAlgorithm(String algorithm) {
		UnionpayH5B2BCbc3DesUtil.algorithm = algorithm;
	}

	public static String getIvKey() {
		return ivKey;
	}

	public static void setIvKey(String ivKey) {
		UnionpayH5B2BCbc3DesUtil.ivKey = ivKey;
	}

	public static String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public static void setKeyAlgorithm(String keyAlgorithm) {
		UnionpayH5B2BCbc3DesUtil.keyAlgorithm = keyAlgorithm;
	}
	
}
