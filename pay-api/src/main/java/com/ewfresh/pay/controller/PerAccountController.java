package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.AccountFlowManager;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Description:我的账户明细报表
 *
 * @author louzifeng
 * Date 2018/11/13
 */
@Controller
public class PerAccountController {

    @Autowired
    private AccountFlowManager accountFlowManager;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String BUSINO = "busiNo";
    private static final String AMOUNT = "amount";
    private static final String ENDTIME = "endTime";
    private static final String STARTIME = "startTime";
    private static final String USERID = "userId";
    private static String HH_MM_SS = " 23:59:59"; //时分秒

    /**
     * Description: 导出个人中心我的账户明细报表
     *
     * @param response
     * @param title
     * @param startTime
     * @param endTime
     * @author: LouZiFeng
     * @return: void
     * date:2018/11/13
     */
    @Adopt
    @RequestMapping("/t/export_get_transactionFlow.htm")
    public void exportAccountTransaction(HttpServletResponse response, Long userId, String explain, String title, String amount,
                                         String startTime, String endTime) {
        logger.info("get Online AccountTransaction list param is ----->[amount = {},explain = {},startTime = {},endTime = {}]", amount, explain, startTime, endTime);
        ResponseData responseData = new ResponseData();
        ServletOutputStream output = null;
        try {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put(USERID, userId.toString());
            if (StringUtils.isNotBlank(explain))
                stringObjectHashMap.put(BUSINO, explain);
            if (StringUtils.isNotBlank(amount))
                stringObjectHashMap.put(AMOUNT, amount);
            if (StringUtils.isNotBlank(startTime)) {
                stringObjectHashMap.put(STARTIME, startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                if (endTime.length() <= Constants.TEN) {
                    endTime += HH_MM_SS;
                }
                stringObjectHashMap.put(ENDTIME, endTime);
            }
            Workbook workbook = accountFlowManager.exportPersonalAccount(responseData, title, stringObjectHashMap);
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            response.setContentType("application/msexcel");
            workbook.write(output);
            logger.info("It is ok in AccountTransaction");
        } catch (Exception e) {
            logger.error("It is error in AccountTransaction", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Program is wrong!");
            ResponseUtil.responsePrint(response, responseData, logger);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                logger.error("It is ok in AccountTransaction", e);
            }
        }
    }
}
