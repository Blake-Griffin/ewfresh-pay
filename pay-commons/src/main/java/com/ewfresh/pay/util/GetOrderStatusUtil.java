package com.ewfresh.pay.util;

import com.ewfresh.commons.client.HttpDeal;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.ID;
import static com.ewfresh.pay.util.Constants.SYSTEM_ID;

/**
 * description: 从order系统查询退单状态
 * @author: JiuDongDong
 * date: 2019/6/18.
 */
@Component
public class GetOrderStatusUtil {
    private Logger logger = LoggerFactory.getLogger(GetOrderStatusUtil.class);
    @Value("${httpClient.getToken}")
    private String userTokenUrl;
    @Value("${httpClient.getOrderInfo}")
    private String getOrderInfoUrl;
    @Autowired
    private HttpDeal httpDeal;
    @Autowired
    private GetSysIdToken getSysIdToken;
    /**
     * Description: 从order系统查询退单状态
     * @author: JiuDongDong
     * @param outRequestNo  退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * @return java.lang.Integer 订单状态
     * date: 2019/6/18 16:32
     */
    public synchronized Integer getOrderStatusFromOrder(String outRequestNo) {
        String token = getSysIdToken.getSysIdToken(userTokenUrl);// 系统的token
        logger.info("token: {}, outRequestNo: {}", token, outRequestNo);
        if (StringUtils.isBlank(token) || StringUtils.isBlank(outRequestNo)) {
            logger.error("token or outRequestNo is null, token = " + token + ", outRequestNo = " + outRequestNo);
            return null;
        }
        // 封装订单查询请求参数
        Map<String, String> paramsQuery = new HashMap<>();
        paramsQuery.put(ID, outRequestNo);
        String post;
        try {
            post = httpDeal.post(getOrderInfoUrl, paramsQuery, token, SYSTEM_ID);// 系统操作，uid为10000
            logger.info("post = {} of outRequestNo: {}", post, outRequestNo);
        } catch (Exception e) {
            logger.error("return back to balance http response post", e);
            throw new RuntimeException("http failed");
        }
        ResponseData responseData = JsonUtil.jsonToObj(post, ResponseData.class);
        String code = responseData.getCode();
        if (!ResponseStatus.OK.getValue().equals(code)) {
            logger.error("http response post = " + post);
            throw new RuntimeException("Order module occurred error, check it!");
        }
        Object entity = responseData.getEntity();
        logger.info("" + entity);
        Integer orderStatus = Integer.valueOf("" + entity);
        logger.info("the orderStatus of {} is: {}", outRequestNo, orderStatus);
        return orderStatus;
    }
}
