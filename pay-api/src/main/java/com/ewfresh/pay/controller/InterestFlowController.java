package com.ewfresh.pay.controller;

import java.net.URLEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.InterestFlowManager;
import com.ewfresh.pay.util.DateUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;

/**
 * Class description
 *
 *
 * @date    19/08/15
 * @author  huboyang
 */
@Controller
public class InterestFlowController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Long TIMEDIFFERENCE = 31L;
    @Autowired
    private InterestFlowManager interestFlowManager;

    /**
     * Method description
     *
     * @date    19/08/15
     * @author  huboyang
     * @param response
     * @param title
     * @param uname
     * @param startTime
     * @param endTime
     */
    @Adopt
    @RequestMapping("/t/export_interestFlow.htm")
    public void exportInterestFlow(HttpServletResponse response, String title, String uname, String startTime, String endTime) {
        logger.info("------------------>> uname={},startTime={},endTime={}", uname, startTime, endTime);
        ResponseData responseData = new ResponseData();
        ServletOutputStream output = null;
        try {
            if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
                logger.info("  param startTime or endTime is null  !!!");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 如果导出时间大于31天 返回
            Long timeDifference = (df.parse(startTime).getTime() - df.parse(startTime).getTime()) / (1000 * 60 * 60 * 24);
            logger.info("--------------------->timeDifference={}", timeDifference);
            if (timeDifference > TIMEDIFFERENCE) {
                logger.info("  too much time apart  !!! ");
                responseData.setCode(ResponseStatus.TOOMUCHTIMEAPART.getValue());
                responseData.setMsg(ResponseStatus.TOOMUCHTIMEAPART.name());
                return;
            }
            Workbook workbook = interestFlowManager.exportInterestFlow(responseData, uname, startTime, endTime, title);
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            response.setContentType("application/msexcel");
            workbook.write(output);
            logger.error("Export   InterestFlow is  OK");
        } catch (Exception e) {
            logger.error("Export   InterestFlow is  err", e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Method description
     *
     * @date    19/08/15
     * @author  huboyang
     * @param response
     * @param uname
     * @param pageNumber
     * @param pageSize
     * @param startTime
     * @param endTime
     */
    @Adopt
    @RequestMapping("/t/get_interestFlow.htm")
    public void getInterestFlow(HttpServletResponse response, String uname,
                                @RequestParam(defaultValue = "1") Integer pageNumber,
                                @RequestParam(defaultValue = "15") Integer pageSize, String startTime, String endTime) {
        logger.info("---------------------------->uname={},pageNumber={},pageSize={},startTim={},endTime={}", uname, pageNumber, pageSize, startTime, endTime);
        ResponseData responseData = new ResponseData();
        Date date = new Date();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd ");
            if (StringUtils.isEmpty(startTime)) {
                Date futureMountDays = DateUtil.getFutureMountDays(date, -31);
                startTime = df.format(futureMountDays);
            }
            if (StringUtils.isEmpty(endTime)) {
                endTime = df.format(date);
            }
            logger.info("-------------------> startTim={},endTime={}", startTime, endTime);
            interestFlowManager.getInterestFlow(uname, responseData, pageNumber, pageSize, startTime, endTime);
            responseData.setMsg(ResponseStatus.OK.name());
            responseData.setCode(ResponseStatus.OK.getValue());
            logger.info("getInterestFlow is  ok ");
        } catch (Exception e) {
            logger.error("getInterestFlow is  err", e);
            responseData.setMsg(ResponseStatus.ERR.name());
            responseData.setCode(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}

