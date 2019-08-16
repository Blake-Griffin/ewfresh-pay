package com.ewfresh.pay.util;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * description:回调处理业务http处理类
 * @author wangziyuan
 */
public class GetParam {
    private String getbillIDURL;
    private String modifyOrderStatusURL;
    private String supplementModifyURL;
    private String shopBond;
    private final String UID = "uid";
    private static Logger logger = LoggerFactory.getLogger(GetParam.class);
    @Autowired
    private HttpDeal httpDeal;

    public String getToken(String uid) {
        Map<String, String> param = new HashMap<>();
        param.put(UID, uid);
        String post = httpDeal.get("http://urm-stage.ewfresh.com/gen_token" + "?uid=" + uid, null);
        Map<String, Object> get = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, Object>>() {
        });
        String token = (String) get.get("token");
        return token;
    }

    public String getBillId(String token, String uid) {
        logger.info("to get bill id--------->!!!!!");
        HashMap<String, String> param = new HashMap<>();
        param.put("firstKey", "pay_bill");
        String post = httpDeal.post(getbillIDURL, param, token, uid);
        Map<String, String> map = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        logger.info("get billId success----->{}", ItvJsonUtil.toJson(map));
        String balanceId = map.get("entity");
        return balanceId;
    }

    public String modifyOrderStatus(Map<String, String> map, String token, String uid) {
        logger.info("to modify order status the param is----->{}", ItvJsonUtil.toJson(map));
        String post = httpDeal.post(modifyOrderStatusURL, map, token, uid);
        logger.info("the modify result is---->{}", post);
        Map<String, String> response = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String code = response.get("code");
        return code;
    }

    public String supplementToModifyDis(Map<String, String> map, String token, String uid) {
        logger.info("supplementToModifydis the param is----->{}", ItvJsonUtil.toJson(map));
        String post = httpDeal.post(supplementModifyURL, map, token, uid);
        logger.info("the modify result is---->{}", post);
        Map<String, String> response = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String code = response.get("code");
        return code;
    }

    public String shopBond(Map<String, String> map, String token, String uid) {
        logger.info("shopBond the param is----->{}", ItvJsonUtil.toJson(map));
        String post = httpDeal.post(shopBond, map, token, uid);
        logger.info("the modify result is---->{}", post);
        Map<String, String> response = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String code = response.get("code");
        return code;
    }

    public String getGetbillIDURL() {
        return getbillIDURL;
    }

    public void setGetbillIDURL(String getbillIDURL) {
        this.getbillIDURL = getbillIDURL;
    }

    public String getModifyOrderStatusURL() {
        return modifyOrderStatusURL;
    }

    public void setModifyOrderStatusURL(String modifyOrderStatusURL) {
        this.modifyOrderStatusURL = modifyOrderStatusURL;
    }

    public String getSupplementModifyURL() {
        return supplementModifyURL;
    }

    public void setSupplementModifyURL(String supplementModifyURL) {
        this.supplementModifyURL = supplementModifyURL;
    }

    public String getShopBond() {
        return shopBond;
    }

    public void setShopBond(String shopBond) {
        this.shopBond = shopBond;
    }

    public HttpDeal getHttpDeal() {
        return httpDeal;
    }

    public void setHttpDeal(HttpDeal httpDeal) {
        this.httpDeal = httpDeal;
    }
}
