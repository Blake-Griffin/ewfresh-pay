/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/20.
 *  
 **/

package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.WhiteBillmanager;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

/**
 * @descrption 白条账单
 * @author gaoyongqiqng
 * @create 2019-03-20
 * @Email 1005267839@qq.com
 **/
@Controller
public class WhiteBillController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private WhiteBillmanager whiteBillmanager;

    /**
      * @author gaoyongqiang
      * @Description 根据用户id查询未还款账单与历史还款账单
      * @Date   2019/3/20 11:29
      *  @params
      * @return 
     **/
    @Adopt
    @RequestMapping("/t/get-whiteBill-by-uid.htm")
    public void getWhiteBillByUid(HttpServletResponse response,@RequestParam(defaultValue = "15") Integer pageSize,
                                  @RequestParam(defaultValue = "1") Integer pageNumber, Long userId,String billStatus,String billTime,String startRepaidTime,String endRepaidTime,String uname) {
        logger.info("the param is -->[userId = {},billStatus = {},billTime = {},startRepaidTime = {},endRepaidTime = {}]", userId,billStatus,billTime,startRepaidTime,endRepaidTime);
        ResponseData responseData = new ResponseData();
        try {
//            userId = 10000694L;
//            billStatus = "1";
//            startRepaidTime ="2019-07-01";
//            endRepaidTime = "2019-07-26";
//            if (userId == null) {
//                logger.warn("white bill param userId is null");
//                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
//                responseData.setMsg("white bill param userId is null");
//                return;
//            }
            whiteBillmanager.getWhiteBillByUid(responseData,pageSize,pageNumber,userId,billStatus,billTime,startRepaidTime,endRepaidTime,uname);
            logger.info("white bill is success");
        } catch (Exception e) {
            logger.error("white bill is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("white bill is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
      * @author gaoyongqiang
      * @Description 根据账单批次号查看账单明细
      * @Date   2019/3/20 11:56
      *  @params
      * @return 
     **/
    @Adopt
    @RequestMapping("/t/get-billDetails-by-id.htm")
    public void getBillDetailsById(HttpServletResponse response,@RequestParam(defaultValue = "15") Integer pageSize,
                                   @RequestParam(defaultValue = "1") Integer pageNumber, String billFlow) {
        logger.info("the get balance by uid param is ----->[billFlow = {}]", billFlow);
        ResponseData responseData = new ResponseData();
        try {
            if (billFlow == null) {
                logger.warn("white bill details param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("white bill details param userId is null");
                return;
            }
            whiteBillmanager.getBillDetailsById(responseData,pageSize,pageNumber,billFlow);
            logger.info("white bill details success");
        } catch (Exception e) {
            logger.error("white bill details error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("white bill details error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
      * @author gaoyongqiang
      * @Description 根据账单批次号查看账单明细
      * @Date   2019/3/20 11:56
      *  @params
      * @return 
     **/
    @RequestMapping("/t/get-billDetails-by-billFlow.htm")
    public void getBillDetailsBybillFlow(HttpServletResponse response,@RequestParam(defaultValue = "15") Integer pageSize,
                                   @RequestParam(defaultValue = "1") Integer pageNumber, String billFlow) {
        logger.info("the get balance by uid param is ----->[billFlow = {}]", billFlow);
        ResponseData responseData = new ResponseData();
        try {
            if (billFlow == null) {
                logger.warn("white bill details param userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("white bill details param userId is null");
                return;
            }
            whiteBillmanager.getBillDetailsById(responseData,pageSize,pageNumber,billFlow);
            logger.info("white bill details success");
        } catch (Exception e) {
            logger.error("white bill details error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("white bill details error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
/**
 * @author gaoyongqiang
 * @Description 根据用户id获取最近还款日
 * @Date   2019/3/25 11:30
 *  @params
 * @return 
  **/
    @Adopt
    @RequestMapping("/t/get-RecentPaymentDate-by-id.htm")
    public void getRecentPaymentDateById(HttpServletResponse response,Long userId) {
        logger.info("the get balance by uid param is ----->[userId = {}]", userId);
        ResponseData responseData = new ResponseData();
        try {
            if (userId == null) {
                logger.warn("Get the latest repayment date based on userId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("Get the latest repayment date based on userId is null");
                return;
            }
            whiteBillmanager.getRecentPaymentDateById(responseData,userId);
            logger.info("Get the latest repayment date success");
        } catch (Exception e) {
            logger.error("Get the latest repayment date error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get the latest repayment date error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
      * @author gaoyongqiang
      * @Description 查看还款记录
      * @Date   2019/3/25 11:30
      *  @params
      * @return 
     **/
    @Adopt
    @RequestMapping("/t/get_repayRecords.htm")
    public void getBillRepayByBillid(HttpServletResponse response,@RequestParam(defaultValue = "15") Integer pageSize,
                               @RequestParam(defaultValue = "1") Integer pageNumber,String billId,String startTime,String endTime) {
        logger.info("the get balance by uid param is ----->[billId = {}]", billId);
        ResponseData responseData = new ResponseData();
        try {
            if (billId == null) {
                logger.warn("Get the latest repayment date based on billId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("Get the latest repayment date based on billId is null");
                return;
            }
            whiteBillmanager.getBillRepayByBillid(responseData,pageSize,pageNumber,billId,startTime,endTime);
            logger.info("Get the  repayment  success");
        } catch (Exception e) {
            logger.error("Get the  repayment  error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get the  repayment  error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
