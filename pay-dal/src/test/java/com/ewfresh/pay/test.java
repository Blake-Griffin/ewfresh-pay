package com.ewfresh.pay;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.service.InterestFlowService;
import com.ewfresh.pay.util.DateUtil;
import com.ewfresh.pay.util.bill99.FinderSignService;
import com.ewfresh.pay.util.boc.MyHttp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

/**
 * Created by 王耀辉 on 2018/4/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-test-common.xml"})
public class test {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String KEY = "{pay}{WhiteRepayOrder}";
    @Autowired
    private InterestFlowService interestFlowService;

    //    @Autowired
//    private Bill99PayConfigure bill99PayConfigure;
    @Test
    public void testBatch() {
       Short i= 1800;
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(i);
        if (objects.contains(1800)){
            System.out.println( "111111111111111111111111111");
        }else {
            System.out.println("9999999999999999999");
        }
    }


    @Test
    public void test1() throws Exception {
        logger.info("the signMsg is ------->{}", 11);
        String domainName = "https://sandbox.99bill.com/finder";
        String platformCode = "200000000009516";
        String hatPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC9yKIzxszjIRs3uBe/zThYgg48/S96wbDjqzOqj9e4dTZQ5YOHVM2p9nWC3WU4OPcoWxXWxPE3fKsWnwI2NpvRjmuv5+uTQp+w24f9uiKFk7Lpgz5Xz/sokRL7ahP8OooFd28kUp1YlqHeaB+GXytGoVb0K8FoghBgAiPBJJPOUczKIdicOwRubP4xFTk2Vg5ZuAEkqbkAx2vQdW8N9jNNyuYqAdwx2yI0DYMeYER+sH6h5iAqojDbOGF3rN36KsyO5vPXjZ5NG4fAbO4hAVvLi+SvN57pZqyDUMUn3G8gn7BUkf1lm02SXG55mdGUuXX6e+9S68EltUsixASrXDVPAgMBAAECggEBAJT6iInIh1HMzUbiFpKUEKU6At9RiRH+NlOiMz6zCA5exLKgWK3FnmTLedeu4CRQY4Ska/JaS1jZXpy1WdEg8RSBjntZvY2uARmeW5SLp0ngddPlGO1p8KkZqSL/VIztjydXaEHFMIs4Q/0JE6Yx0xDw2gd2ya62WxBi3Zpikjykk3tlQqyHC/0FQ37pGFwvVU+KmQh3MAC1Xl9reV9ip9OhWDG1ZfLpLvQAqekqct2gHNodpcQIYrdiZ3bGpmAftL9V241DUn9hrmwPoU0q0Siec2SbalL+dixWxR129sXM8bokenmcgkQrzW56QTTXrL7sw3QTki2y9qAgKxWTOVECgYEA53AnhGnH2jxbzsO/T1NpkvPRxsrvMtNCj+aH9Sl2bic3R4K7nNX/zvLpFgrJTWeo6rzLygtuR8S0dq2vb2TonXKkWrc86H4m28fZon+EeahcjLQl+mfRWG1HYiE3yGzKYZda9+h6M/P+eYTK9WpcooR+bSGye1vX/dCH2h52QcsCgYEA0ezJ689HGpww4+3QuijKI7mT5o7K5KT6aKngS6aEStZBttJwZY3C5K4H8YBJIQcw2NevUjjje7HX6MrvUvEliYz3Mize3kzjyKyj1EUzXW2KJbeKEb/PdpsxMsG2xd5wn6o0ik9ncVVycMAF8mnA7VfycyZ9e817O0rbRAd82g0CgYEA0kOF03obNP2DV01mANbCYt0bKCV0vdNLdNs3iBe8XgVI5rIyayHNP6JW+0yfaLCVuXX/G7fY4GEop23SpjyiTXE7OqOhNYRvH1vpMQXzQGgidT6yBlbNzi2yHcgaGwAjuhg9cF+Irqys9JAdqGuSb6A6EEH+Gq7tg84xrovgWpMCgYEAl5/YGKKWUPoPthHHeS6NElCwKw9oLM2csIxCnWGgnA01Wb+Vv0QtSM8KnbxzFZAEEBt5XoIIjQdfU52ATwAuhLN0uKEAOMCApilMC74P46OC7MssxrUsPBbL6kVQpoqL/kcJu/yuUoXNKvMHWJhZ6RRk4hci+5DnOWRQC0r9OuECgYBwM0Ey64TilTiqT1rQIt8u6UxtvPKnrjGUe4P9PDMI5TV54XTEEfWyfwDLBNQRd3TPDVUq56XMscA5Wk328aARR67V8O8rpPPM9mP0eo4H4q5FJi9vyoFsvhHfbk7ypIpWEqSTFGhLp9OqtR+WvAKNMA10i/sNjJByWEqI+GceJw==";
        HashMap<String, String> msgMap = new HashMap<>();
        msgMap.put("uId", "1017");
        msgMap.put("platformCode", platformCode);
        msgMap.put("bankAcctId", "10250000001510583");
        //msgMap.put("accountType", "0");
        FinderSignService finderSignService = new FinderSignService();
        String sign = finderSignService.sign(platformCode, ItvJsonUtil.toJson(msgMap), hatPrivateKey);
        MyHttp bindHttpDeal = new MyHttp();
        Map<String, Object> post = bindHttpDeal.post(domainName + "/bankacct/queryStatus", ItvJsonUtil.toJson(msgMap), UUID.randomUUID().toString(), platformCode, sign);
        logger.info("the signMsg is ------->{}", sign);
        logger.info("[post={},b={}]", post);
//        System.out.println(post+"/n"+sign);
    }

    private String appendParam(String returns, String paramId, String paramValue) {
        if (returns != "") {
            if (paramValue != "") {
                returns += "&" + paramId + "=" + paramValue;
            }
        } else {
            if (paramValue != "") {
                returns = paramId + "=" + paramValue;
            }
        }
        return returns;
    }

    /*    @Autowired
        private RedisTemplate redisTemplate;
        @Test
        public void test098765() {
            ValueOperations<String, String> string = redisTemplate.opsForValue();
            String all = string.get("{shop}{addShopRedis}");
            Map<Integer,HashMap> map = ItvJsonUtil.jsonToObj(all,new TypeReference<Map<Integer,HashMap>>(){});
            HashMap shopInfo = map.get(Integer.valueOf(0));

            System.out.println(ItvJsonUtil.toJson(shopInfo));
            System.out.println(shopInfo.get("shopName"));

        }*/
    @Test
    public void  asd(){
        Date d=new Date();
        Date futureMountDays = DateUtil.getFutureMountDays(d, -1);
        System.out.println(futureMountDays );
    }
}


