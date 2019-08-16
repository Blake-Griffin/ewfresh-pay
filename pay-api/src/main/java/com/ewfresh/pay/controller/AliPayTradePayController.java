package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.AliPayManager;
import com.ewfresh.pay.request.AliPayExtend;
import com.ewfresh.pay.request.AliPayRequest;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * description:支付宝生成付款码的接入层
 * author: wangziyuan
 */
@Controller
public class AliPayTradePayController {
    @Autowired
    private AliPayManager aliPayManager;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Adopt
    @RequestMapping("/t/aliPay-trade-pay-toAddSign.htm")
    public void toAddSign(HttpServletResponse response, String mainParam, String extendParam) {
        logger.info("the param is ----->mainParam={}extendParam={}", mainParam, extendParam);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isEmpty(mainParam)) {
                logger.warn("the param is mull----->{}", mainParam);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the param is null");
                ResponseUtil.responsePrint(response, responseData, logger);
                return;
            }
            logger.info("ready to jsonToObj-------->>");
            AliPayRequest aliPayRequest = ItvJsonUtil.jsonToObj(mainParam, new AliPayRequest().getClass());
            if (StringUtils.isBlank(aliPayRequest.getOut_trade_no()) || StringUtils.isBlank(aliPayRequest.getTotal_amount()) || StringUtils.isBlank(aliPayRequest.getProduct_code()) ||
                    StringUtils.isBlank(aliPayRequest.getSubject())) {
                logger.warn("the param is null----->out_trade_no={},total_amount={},product_code={},subject={},passback_params={}", aliPayRequest.getOut_trade_no(), aliPayRequest.getTotal_amount(), aliPayRequest.getProduct_code(), aliPayRequest.getSubject());
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the must param is null");
                ResponseUtil.responsePrint(response, responseData, logger);
                return;
            }
            if (StringUtils.isNotBlank(extendParam)) {
                logger.info("hava extendParam ------->>>>");
                aliPayRequest.setExtend_params(ItvJsonUtil.jsonToObj(extendParam, new AliPayExtend().getClass()));
            }
            String form = "";
            response.setContentType("text/html;charset=UTF-8");
            responseData = aliPayManager.AliPagePay(aliPayRequest);
            form = responseData.getEntity().toString();
            logger.info("the form is ---->{}", form);
            if (StringUtils.isBlank(form)) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the must param is null");
                ResponseUtil.responsePrint(response, responseData, logger);
            }
            response.getWriter().print(form);//直接将完整的表单html输出到页面
        } catch (Exception e) {
            logger.error("have an Exception!!!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("have an Exception!!!");
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
