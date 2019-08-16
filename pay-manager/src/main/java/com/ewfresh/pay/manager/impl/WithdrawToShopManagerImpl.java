package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.ewfresh.pay.manager.WithdrawToShopManager;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.shopWithdraw.RSACoder;
import com.ewfresh.pay.util.unionpayb2cwebwap.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.UTF_8;
import static com.ewfresh.pay.util.shopWithdraw.RSACoder.ALGORITHM_SHA256WITHRSA;

/**
 * description: 店铺提现的逻辑管理层
 * @author: JiuDongDong
 * date: 2019/6/17.
 */
@Component
public class WithdrawToShopManagerImpl implements WithdrawToShopManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${union.sysId}")
    private String sysId;//商户编号
    @Value("${union.qrywithdrawbalance}")
    private String qrywithdrawbalance;//获取账户余额

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");



    /**
     * Description:店铺查询可提现金额接口（后台）
     * @author: JiuDongDong
     * @param mchntNo 商编
     * date: 2019/6/17 21:50
     */
    @Override
    public void getCanWithdrawMoney(ResponseData responseData, String mchntNo) throws Exception {
        logger.info("It is now in UnionPayH5PayManagerImpl.sendOrder, the parameters are: " +
                "[mchntNo = {}, uid = {}]", mchntNo);
        /* 1.组装必要参数 */
        Map<String, String> params = new HashMap<>();
        params.put("sysId", sysId);
        params.put("mchntNo", mchntNo);
        params.put("mchntNo", "898460115200115");
        params.put("timestamp", sdf.format(new Date()));

        /* 生成签名 */
        //外部平台私钥  2048位
        String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDAAbXA7tnnMuWV6WO1bKEKGyKZRFcRCGDW+UfGFZdx0MEm6+fKTNGTsalEFwFPK36YtQFXcN0FciGmQp1wGts4IqLaEzk9az5UcxIfR00JxpNsTYSZb82ZMpj52WF0FHtyj7qE+K3k/FcKCcEHQ3Soa9LL/GQpyVO0yhxeV5tiRfnHoJxxCRHp3jM//VnlCd6koTwPNPFKsf8Vf+dtGA3/BZPFoHvFP0+vfLmEi2TIHO1f1CZDg67TiC/Kkusl0XBiUAXhjAz1SleNQzKHtUVPbVluBtG8ydk8kcLpqDDST6WPnHg5NgHt9qXp0vOLO7ABkQz+dGCXKm+tWXHnRjj1AgMBAAECggEAMm7AjMKwHZgy0aOR+w9jZUInXlai/+hRd2XWwmLdepm4gj6ojWyMB908dpQMVf04rWetyIfupgWKbR9GNzH2rtH6MImoGUfYAVqQQgL6azzrcCEUWTESsdCmecntXQ4cNsUl2tNu6ZyWSB6zwvKm664Wmlna/VbSU8RamzUrrS362gxNFdcEyvCstlnNx1aC5VCRFHm6uBO2uFSYR4dh4e0KpEmYsPT41k93Z4KQHFTCwcvuQu+or5WKL44hKYHyXoZbWiZhfvBRXBVDT7TLmObkeQXAr/Uu1SO5AkblYqG0dyYVy1ea2xANjt8Mw+OP4kRu8IFikALwWp4iYdyAaQKBgQD5mrsx6KrRHbtpBZzHBcEL+/tttA8YAskH2NlkEcGDPcwwULdEYRUUEvX5J+9sTwZHLnE6O9YKI0UtbEBLmDGDfoHy9OM0NCHEaxmZJvmizzfGGc4u/+EtrNCZh1nXUvz1cPip6tywpuHuIgcdAczm0kD9vMWcKHWJSZv+gdClpwKBgQDE7StEIc/c4/fYjIzUaMh6NHqGGB/SpqpfyKHdNZG1xhacN3zY44fO4X8U93uV+thhY3a66TjvvfC2kQAidWWwn4DzmOgWCSPPtuYuI5vouqW/HL8rLKf3hV2rGCC6fz9PDgjy1tl6ZxI10YsA0VjWuHQ9OcQjBl/ypoGfTCJ4AwKBgQCvMhwSe+zpuqTAol/YkgFeGA/ygF/XypywFVUBGDVrmQSpJP590GarIGPl7lHvA8i0TbTL2xPxKbB0oXa/mKOoWDN+BMU07yKEa2gcR28RB8FuGs7NzmyPUq1YFdjJekZzQEhJe8BLfdc2/ktf4NOhcBKOBuHtKbjWFASaLyP0IQKBgHjRSYozdGQBOT4SfRSUdOsE52b9xghnWIALh8M/6nWrYpPVNzOZ5Oh4UI98hsYtcDPP4jgqflQYJGbd70c0337NXUAWv81FLkNx4ybLkgvm92mZKXBDpYmmuSEPXIUPLLhD1Bmo1yTRt8ptFOsbhXW3FRm7JyqV7qfgoAYrn7ohAoGAT2YHHABe8UHfo8ZnLKjjC3FfUcrGd87LTB8EbADVb+Vuak7/8/FTGRDGxygeH3/haB86Dv1nRQJ2Jp1fS9HrWfX/cart1H6Ef/FKT6Td3aCZAwM6kTLWkDepX+2qWW3pnKytrnp1rHFu9XIR+iFlG2hFOg+ppzUKfX3L3A57xDU=";
        //银商系统公钥 2048位
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu13Ykr8Q4ACqnYMfLL5kgV2JsUC7TQEeWR70Zpulqq6JeujD6dCupnYnGhnMmePasgBZT0rIKGvoUEe5tMS1sfYo6dMqaAwcVfe4XOQaPSQs10XDSMB689+ImZmhECEBJkbKs7K+BBJXBOZGkgHBZsd4pn3vlF4E2yPTrfrcn9OEXZAKrUb/jZm6suzHoXSljqtHWwT7OwQoIX+Q/27gYA6PuGpFFmr4Xtc4a/AqIHeCC4TinbgboD8HqfL0ZoC4NG6Xm2KJ9wK66MbS7sYRiK+7pctZkZLxIJ47Ro5Psuxs4owTdtY7b1aHun9GoUT6Wm4mRO0asvBv0XKn05qn9wIDAQAB";
        //开始做签名  SHA256withRSA
        String signString = RSACoder.generateSortString(params, false);//构建签名字符串
        String s = JsonUtil.toJson(signString);
//        String sign = RSACoder.sign(signString.getBytes(), privateKey, RSACoder.ALGORITHM_SHA256WITHRSA);
        String sign = RSACoder.sign(s.getBytes(), privateKey, ALGORITHM_SHA256WITHRSA);
        // TODO  到底是上面的转Json 还是下面这一行不转Json
        //String sign = RSACoder.sign(RSACoder.generateSortString(params, true).getBytes(), privateKey, RSACoder.ALGORITHM_SHA256WITHRSA);
        params.put("sign", sign);
        //开始做加密
//        String content = RSACoder.toHexString(RSACoder.encryptByPublicKey(JsonUtil.toJson(params).getBytes(), publicKey, 245));
        String content = RSACoder.toHexString(RSACoder.encryptByPublicKey(JsonUtil.toJson(params).getBytes(), publicKey, 117));// todo 117 245
        logger.info("content = {}", content);

        /* 发送请求*/
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("content", content);
        //发送后台请求数据
        String resultString = null;
        HttpClient hc = new HttpClient(qrywithdrawbalance, 30000, 30000);//连接超时时间，读超时时间（可自行判断，修改）
        try {
            int status = hc.send(paramsMap, UTF_8);
            if (200 == status) {
                resultString = hc.getResult();
            } else {
                logger.info("返回http状态码[" + status + "]，请检查请求报文或者请求地址是否正确");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

//        if(MapUtils.isEmpty(rspData)){
//            logger.error("The response data is empty for get can withdraw money, mchntNo = " + mchntNo);
//            responseData.setCode(ResponseStatus.UNIONCONNECTEX.getValue());//未获取到返回报文或返回http状态码非200
//            responseData.setMsg(ResponseStatus.UNIONCONNECTEX.name());
//            return;
//        }
        // 响应字符串转换为JsonObject
        JSONObject respJsonObj = JSON.parseObject(resultString, Feature.OrderedField);
        // 状态码处理
        String responseCode = respJsonObj.getString("responseCode");
        String responseDesc = respJsonObj.getString("responseDesc");
        if ("999999".equals(responseCode)) {
            logger.error("Get can withdraw money occurred error, responseCode = " + responseCode +
                    ", responseDesc = " + responseDesc + ", respJsonObj = " + respJsonObj);
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());
            responseData.setMsg(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.name());
            return;
        }

        /* 获取响应参数 */
        String signResp = respJsonObj.getString("sign");
        String sysIdResp = respJsonObj.getString("sysId");
        String t0WithdrawAmtPublic = respJsonObj.getString("t0WithdrawAmtPublic");//JF通道商户t0可提现金额；JF通道商户该字段不为空，CUPS通道商户返回该字段为0
        String tzWithdrawAmtPublic = respJsonObj.getString("tzWithdrawAmtPublic");//JF通道商户tz可提现金额；JF通道商户该字段不为空，CUPS通道商户返回该字段为0
        String withdrawAmtCan = respJsonObj.getString("withdrawAmtCan");//对私商户可提现金额，JF通道商户返回该字段为0，仅当响应码为成功时返回

        /* 验签 */
//        Map<String, String> verifyMap = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("sign", signResp);
        jsonObject.put("sysId", sysIdResp);
        jsonObject.put("responseCode", responseCode);
        jsonObject.put("responseDesc", responseDesc);
        jsonObject.put("t0WithdrawAmtPublic", t0WithdrawAmtPublic);
        jsonObject.put("tzWithdrawAmtPublic", tzWithdrawAmtPublic);
        jsonObject.put("withdrawAmtCan", withdrawAmtCan);

        String verifyStr = jsonObject.toJSONString();
        byte[] rowData = verifyStr.getBytes();//待签名数据
        String signStr = RSACoder.sign(rowData, privateKey, ALGORITHM_SHA256WITHRSA);
        logger.info("signStr = {}", signStr);//签名结果数据（16进制字符串）
        //验证签名
        boolean verifyPass = RSACoder.verify(rowData, publicKey, signStr, ALGORITHM_SHA256WITHRSA);
        if (!verifyPass) {
            logger.error("Verify signature of get can withdraw money not pass!!! mchntNo = "
                    + mchntNo + ", respJsonObj = " + respJsonObj);
            responseData.setEntity(null);
            responseData.setCode(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.getValue());//验证签名失败
            responseData.setMsg(ResponseStatus.VERIFYUNIONPAYSIGNATUREEX.name());
            return;
        }

        /* 响应给页面 */
        Map<String, String> verifyMap = new HashMap<>();
        verifyMap.put("t0WithdrawAmtPublic", t0WithdrawAmtPublic);
        verifyMap.put("tzWithdrawAmtPublic", tzWithdrawAmtPublic);
        verifyMap.put("withdrawAmtCan", withdrawAmtCan);
        responseData.setEntity(verifyMap);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }
}
