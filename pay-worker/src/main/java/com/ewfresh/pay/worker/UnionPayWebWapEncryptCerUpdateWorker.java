package com.ewfresh.pay.worker;

import com.ewfresh.pay.util.unionpayb2cwebwap.AcpService;
import com.ewfresh.pay.util.unionpayb2cwebwap.DemoBase;
import com.ewfresh.pay.util.unionpayb2cwebwap.SDKConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.CERT_TYPE;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.*;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_orderId;
import static com.ewfresh.pay.util.unionpayb2cwebwap.SDKConstants.param_txnTime;

/**
 * Description:
 * 银联加密公钥更新查询(只适用于使用RSA证书加密的方式<即signMethod=01>，其他signMethod=11，12密钥加密用不到此交易)
 * 商户定期（1天1次）向银联全渠道系统发起获取加密公钥信息交易.
 * @author: JiuDongDong
 * date: 2019/5/10 11:08
 */
@Component
public class UnionPayWebWapEncryptCerUpdateWorker {
	private Logger logger = LoggerFactory.getLogger(UnionPayWebWapEncryptCerUpdateWorker.class);
	@Autowired
	private SDKConfig sdkConfig;

//	@Scheduled(cron = "46 46 3 * * ? ") TODO
	public void updateWebWapEncryptCer() {
		// 获取配置信息
		String version = sdkConfig.getVersion();
		String encoding = sdkConfig.getEncoding();
		String signMethod = sdkConfig.getSignMethod();
		String backRequestUrl = sdkConfig.getBackRequestUrl();
		SDKConfig.getConfig().getEncryptCertPath();
		String encryptCertPath = sdkConfig.getEncryptCertPath();// 本地证书路径
		String merId = sdkConfig.getMerId();//TODO 上线多个商户，处理商户id

		// 组装请求参数
		Map<String, String> contentData = new HashMap<String, String>();
		contentData.put(param_version, version);//版本号
		contentData.put(param_encoding, encoding);//字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put(param_signMethod, signMethod);//签名方法  01:RSA证书方式  11：支持散列方式验证SHA-256 12：支持散列方式验证SM3
		contentData.put(param_txnType, "95");//交易类型 95-银联加密公钥更新查询
		contentData.put(param_txnSubType, "00");//交易子类型  默认00
		contentData.put(param_bizType, "000000");//业务类型  默认
		contentData.put(param_channelType, "07");//渠道类型
		contentData.put(CERT_TYPE, "01");//01：敏感信息加密公钥(只有01可用)
		// TODO 上线更新商户号
		contentData.put(param_merId, merId); //商户号码（商户号码777290058110097仅做为测试调通交易使用，该商户号配置了需要对敏感信息加密）测试时请改成自己申请的商户号，【自己注册的测试777开头的商户号不支持代收产品】
		contentData.put(param_accessType, "0");//TODO 这个待确认。接入类型，商户接入固定填0，不需修改
		// TODO 订单号？
		contentData.put(param_orderId, DemoBase.getOrderId());                         //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
		contentData.put(param_txnTime, DemoBase.getCurrentTime());                     //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效                         //账号类型
		String requestBackUrl = backRequestUrl;                               //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

		Map<String, String> reqData = AcpService.sign(contentData, encoding);     //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。


		Map<String, String> rspData = AcpService.post(reqData, requestBackUrl, encoding);  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
		if (!rspData.isEmpty()) {
			if (AcpService.validate(rspData, encoding)) {
				logger.info("Verify signature success!!! txnTime = {}", contentData.get(param_txnTime));
				String respCode = rspData.get("respCode");
				if (("00").equals(respCode)) {
					int resultCode = AcpService.updateEncryptCert(rspData, "UTF-8", encryptCertPath);
					if (resultCode == 1) {
						logger.info("Update certificate success!!! txnTime = {}", contentData.get(param_txnTime));
					} else if (resultCode == 0) {
						logger.info("The certificate in unionPay not change, do not need to update local cer!!! txnTime = {}", contentData.get(param_txnTime));
					} else {
						logger.error("Update certificate failed!!! txnTime = {}", contentData.get(param_txnTime));
					}
				} else {
					//TODO 其他应答码为失败请排查原因
				}
			} else {
				//TODO 检查验证签名失败的原因
				logger.error("Verify signature failed!!! txnTime = {}", contentData.get(param_txnTime));
			}
		} else {
			//未返回正确的http状态
			logger.error("未获取到返回报文或返回http状态码非200");
		}
	}
}
