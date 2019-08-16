package com.ewfresh.pay.controller;

import com.ewfresh.pay.manager.AliPayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * description:支付宝异步回调接口
 * author:wangziyuan
 */
@Controller
public class AliPayNotifyController {
    @Autowired
    private AliPayManager aliPayManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/p/pay/alipay/callback.htm")
    public void toCheckTheSign(HttpServletRequest request, HttpServletResponse response) {
        logger.info("get aliNotiry request");
        String notify = "";
        notify = aliPayManager.AliNotify(request);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(notify);
        } catch (IOException e) {
            logger.error("have some ioException", e);
        }
    }
}
