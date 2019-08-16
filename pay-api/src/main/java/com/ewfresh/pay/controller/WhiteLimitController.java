/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/4/15.
 **/
 
package com.ewfresh.pay.controller;


import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.BarDealFlowManager;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
  * @descrption 根据uid查询白条交易流水信息
 * @author gaoyongqiqng
 * @create 2019-04-15
 * @Email 1005267839@qq.com
  **/
@Controller
public class WhiteLimitController {
    @Autowired
    private BarDealFlowManager barDealFlowManager;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String UID = "uid";
    private static final String ENDTIME = "end";
    private static final String STARTIME = "start";
    private static String HH_MM_SS = " 23:59:59"; //时分秒

    /**
     * @Author gaoyongqiqng
     * @Description 根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    @RequestMapping("/t/get-useFlowLimit-by-uid.htm")
    public void getBarDealFlowByUid(HttpServletResponse response, Long userId, String start, String end,
                                    @RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam(defaultValue = "15") Integer pageSize) {
        logger.info("It is method to get a barDealFlow in controller");
        ResponseData responseData = new ResponseData();
        try {
            if (userId == null) {
                logger.info("calculate barDealFlow count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
            }
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(UID,userId.toString());
            if(StringUtils.isNotBlank(start)) {
                hashMap.put(STARTIME, start);
            }
            if (StringUtils.isNotBlank(end)) {
                if (end.length() <= Constants.TEN) {
                    end += HH_MM_SS;
                }
                hashMap.put(ENDTIME, end);
            }
            barDealFlowManager.getWhiteLimitbyUid(responseData, hashMap, pageNumber, pageSize);
            logger.info("The barDealFlow push query is success!");
        } catch (Exception e) {
            logger.error("The barDealFlow push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  barDealFlow push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author: gaoyongqiqng
     * @Description: 导出白条交易流水
     * @Param title
     * @Param occTime
     * @Date: 2019/3/20
     */
    @RequestMapping("/t/export/get-useFlowLimit-by-uid.htm")
    public void exportBarDealFlow(HttpServletResponse response, Long userId, String title, String endTime, String startTime) {
        logger.info("get Online Accounts list param is ----->[uid = {},startTime = {} = {},endTime = {}]", userId, startTime, endTime);
        ResponseData responseData = new ResponseData();
        ServletOutputStream output = null;
        try {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put(UID, userId.toString());
            if (StringUtils.isNotBlank(startTime)) {
                stringObjectHashMap.put(STARTIME, startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                if (endTime.length() <= Constants.TEN) {
                    endTime += HH_MM_SS;
                }
                stringObjectHashMap.put(ENDTIME, endTime);
            }
            Workbook workbook = barDealFlowManager.exportWhiteLimitbyUid(responseData, title, stringObjectHashMap);
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            response.setContentType("application/msexcel");
            workbook.write(output);
            logger.info("It is ok in exportAccountsList");
        } catch (Exception e) {
            logger.error("It is error in exportAccountsList", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Program is wrong!");
            ResponseUtil.responsePrint(response, responseData, logger);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                logger.error("It is ok in exportAccountsList", e);
            }
        }
    }
    /**
     * description: 查询白条额度流水情况
     * @author huboyang
     * @param
     */
    @RequestMapping("/t/get_barBillFlow.htm")
    public void  getBarBillFlow(HttpServletResponse response, String uname, String end,
                                @RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam(defaultValue = "15") Integer pageSize){
        logger.info("Param is uname={},end={},pageNumber={},pageSize={}",uname,end,pageNumber,pageSize);
        ResponseData responseData = new ResponseData();
        HashMap<String, Object> hashMap = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(uname)) {
                hashMap.put("uname",uname);
            }
            if(StringUtils.isNotBlank(end)) {
                hashMap.put(STARTIME, end);
            }
            if (StringUtils.isNotBlank(end)) {
                if (end.length() <= Constants.TEN) {
                    end += HH_MM_SS;
                }
                hashMap.put(ENDTIME, end);
            }
            barDealFlowManager.getBarDealFlow(responseData, hashMap, pageNumber, pageSize);
            logger.info("The barDealFlow push query is success!");
        } catch (Exception e) {
            logger.error("The barDealFlow push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  barDealFlow push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * description: 下载白条额度流水情况
     * @author huboyang
     * @param
     */
    @RequestMapping("/t/export/Download_barBillFlow.htm")
    public void  DownLoadBarBillFlow(HttpServletResponse response, String uname, String end,String title){
        logger.info("Param is uname={},end={},title={}",uname,end,title);
        ResponseData responseData = new ResponseData();
        HashMap<String, Object> hashMap = new HashMap<>();
        try {
            if (StringUtils.isNotBlank(uname)) {
                hashMap.put("uname",uname);
            }
            if(StringUtils.isNotBlank(end)) {
                hashMap.put(STARTIME, end);
            }
            if (StringUtils.isNotBlank(end)) {
                if (end.length() <= Constants.TEN) {
                    end += HH_MM_SS;
                }
                hashMap.put(ENDTIME, end);
            }
           barDealFlowManager.exportBarDealFlow(responseData, title, hashMap,response);
        } catch (Exception e) {
            logger.error("It is error in exportBarDealFlowList", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Program is wrong!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
            }
        }
}
