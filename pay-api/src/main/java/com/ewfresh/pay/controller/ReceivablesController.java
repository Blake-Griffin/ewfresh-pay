package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.ReceivablesManager;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/6/7 0007
 */
@Controller
public class ReceivablesController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReceivablesManager receivablesManager;

    /**
     * Description: 通过用户id查询资金流水记录
     * @author wangyaohui
     * @param  uid     要查询的用户id
     * Date    2018/4/13
     */
    @RequestMapping("/t/get-receivables-uid.htm")
    public void getReceivablesByUid(HttpServletResponse response, Long uid, @RequestParam(defaultValue = "15") Integer pageSize,
                                 @RequestParam(defaultValue = "1") Integer pageNumber, String explain, String amount,
                                 String endTime, String startTime){
        logger.info("Get receivables list params  are ----->[uid = {},explain={},amount={},endTime={},startTime={}]", uid,explain ,amount,endTime,startTime);
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null ) {
                logger.warn("Get receivables list param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param userId is null");
                return;
            }
            receivablesManager.getReceivablesByUid(responseData,  uid, pageSize, pageNumber,explain,amount,startTime,endTime);
            logger.info("Get receivables list by uid is ok");
        } catch (Exception e) {
            logger.error("Get receivables list by uid is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get receivables list by uid is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 通过条件查询财务余额日志
     * @author wangyaohui
     * Date    2018/4/13
     */
    @RequestMapping("/t/get-receivables-list.htm")
    public void getReceivablesList(HttpServletResponse response,@RequestParam(defaultValue = "15")Integer pageSize ,@RequestParam(defaultValue = "1")Integer pageNumber,String uname,String receiveTime){
        logger.info("Get Receivables list param is ----->[uname = {},receiveTime={}]",uname,receiveTime);
        ResponseData responseData = new ResponseData();
        try {
            receivablesManager.getReceivablesList(responseData,pageSize,pageNumber,uname,receiveTime);
            logger.info("Get Receivables list is ok");
        } catch (Exception e) {
            logger.error("Get Receivables list  is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get Receivables is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

}
