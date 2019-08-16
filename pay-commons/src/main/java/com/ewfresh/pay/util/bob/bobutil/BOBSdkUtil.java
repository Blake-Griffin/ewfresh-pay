package com.ewfresh.pay.util.bob.bobutil;

import com.ewfresh.pay.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;



public class BOBSdkUtil {
	private static final Logger logger = LoggerFactory.getLogger(BOBSdkUtil.class);


	/**
	 * 将key=value字符串转换为map集合
	 * @param result
	 * @return
	 */
	public static Map<String, String> convertResultStringToMap(String result) {
		logger.info("convertResultStringToMap start for: " + result);
		if (result.contains("{")) {
			String separator = "\\{";
			String[] res = result.split(separator);

			Map map = new HashMap();

			convertResultStringJoinMap(res[0], map);

			for (int i = 1; i < res.length; ++i) {
				int index = res[i].indexOf("}");

				String specialValue = new StringBuilder().append("{")
						.append(res[i].substring(0, index)).append("}")
						.toString();

				int indexKey = res[(i - 1)].lastIndexOf("&");
				String specialKey = res[(i - 1)].substring(indexKey + 1,
						res[(i - 1)].length() - 1);

				map.put(specialKey, specialValue);

				String normalResult = res[i].substring(index + 2,
						res[i].length());

				convertResultStringJoinMap(normalResult, map);
			}
			return map;
		}
		logger.info("convertResultStringToMap ok for: " + result);
		return convertResultString2Map(result);
	}

	private static Map<String, String> convertResultString2Map(String res) {
		logger.info("convertResultString2Map start for: " + res);
		Map map = null;
		if ((null != res) && (!("".equals(res.trim())))) {
			String[] resArray = res.split("&");
			if (0 != resArray.length) {
				map = new HashMap(resArray.length);
				for (String arrayStr : resArray) {
					if (null == arrayStr) {
						continue;
					}
					if ("".equals(arrayStr.trim())) {
						continue;
					}
					int index = arrayStr.indexOf("=");
					if (-1 == index) {
						continue;
					}
					map.put(arrayStr.substring(0, index),
							arrayStr.substring(index + 1));
				}
			}
		}
		logger.info("convertResultString2Map ok for: " + res);
		return map;
	}

	private static void convertResultStringJoinMap(String res, Map<String, String> map) {
		logger.info("convertResultStringJoinMap start for: res = {}, map = {}", res, JsonUtil.toJson(map));
		if ((null != res) && (!("".equals(res.trim())))) {
			String[] resArray = res.split("&");
			if (0 != resArray.length)
				for (String arrayStr : resArray) {
					if (null == arrayStr)
						continue;
					if ("".equals(arrayStr.trim())) {
						continue;
					}
					int index = arrayStr.indexOf("=");
					if (-1 == index) {
						continue;
					}
					map.put(arrayStr.substring(0, index),
							arrayStr.substring(index + 1));
				}
		}
		logger.info("convertResultStringJoinMap ok for: res = {}, map = {}", res, JsonUtil.toJson(map));
	}
	
	
	/**
	 * map排序并转换为key=value&key=value字符串,不包含signature
	 * @param data
	 * @return
	 */
	public static String coverMap2String(Map<String, String> data) {
		logger.info("coverMap2String start for: data = {}", JsonUtil.toJson(data));
		TreeMap tree = new TreeMap();
		Iterator it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry en = (Entry) it.next();
			if ("signature".equals(((String) en.getKey()).trim())) {
				continue;
			}
			tree.put(en.getKey(), en.getValue());
		}
		it = tree.entrySet().iterator();
		StringBuffer sf = new StringBuffer();
		while (it.hasNext()) {
			Entry en = (Entry) it.next();
			sf.append(new StringBuilder().append((String) en.getKey())
					.append("=").append((String) en.getValue()).append("&")
					.toString());
		}
		logger.info("coverMap2String ok for: data = {}", JsonUtil.toJson(data));
		return sf.substring(0, sf.length() - 1);
	}
	
	/**
	 * map排序并转换为key=value&key=value字符串
	 * @param data
	 * @return
	 */
	public static String coverMapString(Map<String, String> data) {
		logger.info("coverMap2String start for: coverMapString = {}", JsonUtil.toJson(data));
		TreeMap tree = new TreeMap();
		Iterator it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry en = (Entry) it.next();
			tree.put(en.getKey(), en.getValue());
		}
		it = tree.entrySet().iterator();
		StringBuffer sf = new StringBuffer();
		while (it.hasNext()) {
			Entry en = (Entry) it.next();
			sf.append(new StringBuilder().append((String) en.getKey())
					.append("=").append((String) en.getValue()).append("&")
					.toString());
		}
		logger.info("coverMap2String ok for: coverMapString = {}", JsonUtil.toJson(data));
		return sf.substring(0, sf.length() - 1);
	}

	/**
	 * 获得证书序列号
	 * @param pfxFile 证书路径
	 * @param password 证书密码
	 * @return
	 */
	public static String getCertId(String pfxFile,String password) {
		logger.info("getCertId start for: pfxFile = {}, password = {}", pfxFile, password);
		FileInputStream fis=null;
		String certId=null;
		try {
			fis = new FileInputStream(pfxFile);
			
//		String jdkVendor = System.getProperty("java.vm.vendor");
//		String javaVersion = System.getProperty("java.version");	
//		if (null != jdkVendor && jdkVendor.startsWith("IBM")) {
//			// 如果使用IBM 1.4JDK,则强制设置BouncyCastleProvider的指定位置,解决使用IBMJDK时兼容性问题
//			Security.insertProviderAt(
//					new org.bouncycastle.jce.provider.BouncyCastleProvider(),
//					1);
//		}else{
//			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//		}
//		ks = KeyStore.getInstance("PKCS12");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		KeyStore ks = KeyStore.getInstance("PKCS12","BC");
		char[] nPassword = null == password || "".equals(password.trim()) ? null
				: password.toCharArray();
		logger.info("nPassword: " + nPassword);
		if (null != ks) {
			ks.load(fis, nPassword);
		}
		Enumeration<String> aliasenum = ks.aliases();
		String keyAlias = null;
		if (aliasenum.hasMoreElements()) {
			keyAlias = aliasenum.nextElement();
		}
		X509Certificate cert = (X509Certificate) ks.getCertificate(keyAlias);
		certId= cert.getSerialNumber().toString(16);// 得到序列号
		} catch (FileNotFoundException e) {
			logger.error("getCertId error with FileNotFoundException for: pfxFile = {}, password = {}", pfxFile, password, e);// 私钥获取失败
		} catch (Exception e) {
			logger.error("getCertId error with Exception for: pfxFile = {}, password = {}", pfxFile, password, e);// 私钥解析失败
		}finally{
			if (null!=fis) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("getCertId ok for: pfxFile = {}, password = {}", pfxFile, password);
		return certId;
	} 
	
	/**
	 * 签名
	 * @param map 报文map
	 * @param pfxFile 私钥路径
	 * @param password 私钥密码
	 * @return
	 */
	public static Map<String,String> sign(Map<String,String> map,String pfxFile,String password){
		logger.info("sign data start for: pfxFile = {}, password = {}, map = {}", pfxFile, password, JsonUtil.toJson(map));
		String certId=getCertId(pfxFile, password);
		map.put("certId", certId);
		String sign=coverMap2String(map);
		PrivateKey key;
		try {
			key = CertUtils.getPrivateKey(pfxFile,password);
			byte[] by = CertUtils.sign(key, sign.getBytes("UTF-8"));
			sign = new String(Base64.encode(by));
			map.put("signature", sign);
		} catch (Exception e) {
			logger.error("sign error with Exception for: pfxFile = {}, password = {}, ,map = {}", pfxFile, password, map, e);
		}
		logger.info("sign data ok for: pfxFile = {}, password = {}, map = {}", pfxFile, password, JsonUtil.toJson(map));
		return map;
	}
	
	/**
	 * 私钥报文解密
	 * @param pfxFile 私钥路径
	 * @param password 密码
	 * @param data 加密报文数据
	 * @param encoding 编码
	 * @return
	 */
	public static String dataDeciphering(String pfxFile, String password, String data, String encoding) {
		logger.info("dataDeciphering start for: pfxFile = {}, password = {}, data = {}, encoding = {}", pfxFile, password, data, encoding);
		try {
			PrivateKey key = CertUtils.getPrivateKey(pfxFile, password);
			byte[] bs = Base64.decode(data.getBytes(encoding));
			byte[] org = CertUtils.keyDecode(key, bs);
			String result = new String(org, encoding);
			logger.info("dataDeciphering ok for: pfxFile = {}, password = {}, data = {}, encoding = {}", pfxFile, password, data, encoding);
			logger.info("result: " + result);
			return result;
		} catch (UnsupportedEncodingException e) {
			logger.error("dataDeciphering error with UnsupportedEncodingException for: pfxFile = {}, password = {}, data = {}, encoding = {}", pfxFile, password, data, encoding);
			return "编码转换失败";
		} catch (Exception e) {
			logger.error("dataDeciphering error with Exception for: pfxFile = {}, password = {}, data = {}, encoding = {}", pfxFile, password, data, encoding);
			return "解密失败";
		}
	}
	
	/**
	 * 公钥验签
	 * @param map 报文map
	 * @param cerFile 公钥路径
	 * @return
	 */
	public static boolean validate(Map<String,String> map,String cerFile){
		logger.info("validate start for: cerFile = {}, map = {}", cerFile, JsonUtil.toJson(map));
		String sign =coverMap2String(map);
		logger.info("sign = {}", sign);
		String stringSign = map.get("signature");
		logger.info("stringSign = {}", stringSign);
		if (null==stringSign) {
			logger.error("param is null for validate: cerFile = {}, map = {}", cerFile, JsonUtil.toJson(map));
			return false;
		}
		boolean re=false;
		try {
			byte[] bs = Base64.decode(stringSign.getBytes());
			PublicKey pubKey = CertUtils.getPublicKey(cerFile);
			logger.info("pubKey = {}", pubKey);
			re = CertUtils.verify(pubKey, sign.getBytes(), bs);
			logger.info("re = {}", re);
		} catch (Exception e) {
			logger.error("validate error with Exception for: cerFile = {}, ,map = {}", cerFile, map, e);
		}
		return re;
	}
	
	/**
	 * 构造HTTP POST交易表单的方法示例
	 * 
	 * @param action
	 *            表单提交地址
	 * @param hiddens
	 *            以MAP形式存储的表单键值
	 * @return 构造好的HTTP POST交易表单
	 */
	public static String createHtml(String action, Map<String, String> hiddens) {
		logger.info("createHtml start for: action = {}, hiddens = {}", action, JsonUtil.toJson(hiddens));
		StringBuffer sf = new StringBuffer();
		sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>");
		sf.append("<form id = \"pay_form\" action=\"" + action
				+ "\" method=\"post\">");
		if (null != hiddens && 0 != hiddens.size()) {
			Set<Entry<String, String>> set = hiddens.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> ey = it.next();
				String key = ey.getKey();
				String value = ey.getValue();
				sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\""
						+ key + "\" value=\"" + value + "\"/>");
			}
		}
		sf.append("</form>");
		sf.append("</body>");
		sf.append("<script type=\"text/javascript\">");
		sf.append("document.all.pay_form.submit();");
		sf.append("</script>");
		sf.append("</html>");
		logger.info("createHtml ok for: action = {}, hiddens = {}", action, JsonUtil.toJson(hiddens));
		return sf.toString();
	}
	
}
