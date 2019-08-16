package com.ewfresh.pay.util.bob;

import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;
import com.ewfresh.pay.util.ResponseData;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * description:
 *      向BOB响应信息
 * @author: JiuDongDong
 * date: 2018/4/21.
 */
public class BOBResponseUtil {

    public static void responsePrintHTML(HttpServletResponse response, Object obj, Logger logger) {
        try {
            response.setCharacterEncoding(Constants.UTF_8);
            response.setContentType(Constants.CONTENT_TYPE_TEXT_HTML);
            PrintWriter writer = response.getWriter();
            writer.print(obj);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("The BOB output of the forward table is error", e);
        }

    }

    public static void responsePrint(HttpServletResponse response, ResponseData responseData, Logger logger) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.getWriter().print(JsonUtil.toJson(responseData));
        } catch (IOException e) {
            logger.error("The output of the forward table is error", e);
        }

    }
}
