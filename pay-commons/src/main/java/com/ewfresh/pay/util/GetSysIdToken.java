package com.ewfresh.pay.util;

import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.pay.model.vo.UidToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * description: 获取系统id的token
 * @author: JiuDongDong
 * date: 2018/12/19.
 */
public class GetSysIdToken {
    private static Logger logger = LoggerFactory.getLogger(GetSysIdToken.class);

    private static Lock lock = new ReentrantLock(false);

    private static HttpDeal httpDeal;
    /**
     * Description: 获取系统token，每一次都获取最新的token
     * @author: JiuDongDong
     * @param userTokenUrl 获取token的地址
     * @return java.lang.String token
     * date: 2018/12/19 10:01
     */
    public static String getSysIdToken(String userTokenUrl) {
        if (StringUtils.isBlank(userTokenUrl)) {
            return null;
        }
        String token;
        try {
            lock.tryLock();
            token = httpDeal.get(userTokenUrl + "?uid=" + Constants.SYSTEM_ID, null );
            UidToken uidToken = JsonUtil.jsonToObj(token, UidToken.class);
            token = uidToken.getToken();
            logger.info("The token of 10000 = " + token);
            if (StringUtils.isBlank(token)) {
                throw new RuntimeException("The token of 10000 is null");
            }
        } finally {
            lock.unlock();
        }
        return token;
    }

    public  HttpDeal getHttpDeal() {
        return httpDeal;
    }

    public void setHttpDeal(HttpDeal httpDeal) {
        this.httpDeal = httpDeal;
    }

}
