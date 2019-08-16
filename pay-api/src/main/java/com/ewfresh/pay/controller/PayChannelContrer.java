package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.PayChannelManager;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * description:支付渠道的信息的控制层
 *
 * @author: wangyaohui
 * @date 2018年4月21916:16:06
 */
@Controller
public class PayChannelContrer {
    @Autowired
    private PayChannelManager payChannelManager;
    private  Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * description:查询支付渠道的信息
     * @author: wangyaohui
     */
    @Adopt
     @RequestMapping("/p/get_pay_channe_all.htm")
     public void getPayChanneByAll(HttpServletResponse response){
         logger.info("Get pay channel by all");
         ResponseData responseData = new ResponseData();
         try {
             payChannelManager.getPayCkanneByAll(responseData);
             logger.info(responseData.getMsg());
         } catch (Exception e) {
             logger.error("Get pay channel by all is  error", e);
             responseData.setCode(ResponseStatus.ERR.getValue());
             responseData.setMsg("gGet pay channel by all is err");
         } finally {
             ResponseUtil.responsePrint(response, responseData, logger);
         }
     }
}
