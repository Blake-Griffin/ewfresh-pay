package com.ewfresh.pay.util;

//import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.ItvJsonUtil;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ylm29 on 2017/8/30.
 */
public class ResponseUtil {

    public static void responsePrint(HttpServletResponse response, ResponseData responseData, Logger logger){
        try{
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.getWriter().print(JsonUtil.toJson(responseData));
        }catch (IOException e){
            logger.error("The output of the forward table is error",e);
        }

    }


    /**
     * description: 返回jsonp类型的数据
     *
     * @author: wangyaohui
     * @param: response
     * 响应
     * @param: responseData
     * 封装好的结果集
     * @param: logger
     * 日志
     * date: 2017/10/10
     */
    public static void responsePrintJsonp(HttpServletResponse response, ResponseData responseData,
                                          Logger logger) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(JsonUtil.toJsonp(responseData));
        } catch (Exception e) {
            logger.error("Response exception", e);
        }

    }
}
