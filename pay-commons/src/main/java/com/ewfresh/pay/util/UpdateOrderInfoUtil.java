package com.ewfresh.pay.util;

import com.ewfresh.commons.client.HttpDeal;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.ewfresh.pay.util.Constants.SYSTEM_ID;

/**
 * description: 调用order项目CRUD订单信息
 * @author: JiuDongDong
 * date: 2019/6/18.
 */
@Component
public class UpdateOrderInfoUtil {
    private Logger logger = LoggerFactory.getLogger(UpdateOrderInfoUtil.class);
    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Value("${httpClient.getOrderInfo}")
    private String getOrderInfoUrl;
    @Autowired
    private HttpDeal httpDeal;

    @Autowired
    GetSysIdToken getSysIdToken;
    /**
     * Description: 调用order项目CRUD订单信息
     * @author: JiuDongDong
     * @param params  httpClient参数
     * @return java.lang.String order系统响应信息
     * date: 2019/5/7 11:43
     */
    public synchronized boolean updateOrderInfo(String url, Map<String, String> params) {
        String token = getSysIdToken.getSysIdToken(userTokenUrl);// 系统的token
        logger.info("token = {}", token);
        if (StringUtils.isBlank(token)) {
            logger.error("token or outRequestNo is null, token = " + token);
            return false;
        }
        String post;
        try {
            post = httpDeal.post(url, params, token, SYSTEM_ID);// 系统操作，uid为10000
            logger.info("http response post = {}", post);
        } catch (Exception e) {
            logger.error("return back to balance http response post", e);
            throw new RuntimeException("http failed");
        }
        ResponseData responseData = JsonUtil.jsonToObj(post, ResponseData.class);
        String code = responseData.getCode();
        if (!ResponseStatus.OK.getValue().equals(code)) {
            logger.error("http response code = " + code);
            throw new RuntimeException("Order module occurred error, check it!");
        }
        logger.info("update order status ok");
        return true;
    }
}
