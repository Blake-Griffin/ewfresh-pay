package com.ewfresh.pay.util;

import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.util.ItvJsonUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/25 0025
 */
public class IdUtils {

    private static Logger logger = LoggerFactory.getLogger(IdUtils.class);

    private static HttpDeal httpDeal;
    /**
     * Description: 从ID生成器获取id的共用方法
     * @author DuanXiangming
     * @param idUrl
     * @param key
     * @return java.lang.String
     * Date    2018/10/25 0025  下午 4:21
     */
    public  String getId(String idUrl,String key){
        logger.info("get id params are [idUrl = {},key = {}]",idUrl,key);
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, key);
        String idStr = httpDeal.post(idUrl, map, null,null);
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        boolean empty = MapUtils.isEmpty(hashMap);
        if (empty){
            return null;
        }
        Object entity = hashMap.get(Constants.ENTITY);
        return String.valueOf(entity);
    }

    public static HttpDeal getHttpDeal() {
        return httpDeal;
    }

    public  void setHttpDeal(HttpDeal httpDeal) {
        this.httpDeal = httpDeal;
    }
}
