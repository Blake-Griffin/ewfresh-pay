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
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Description:账户资金流水表
 * @author wangyaohui
 * Date 2018/4/11
 */
@Controller
public class AccountFlowController {
   @Autowired
   private AccountFlowManager accountFlowManager;
   private Logger logger = LoggerFactory.getLogger(getClass());


   private static final String BUSINO =   "busiNo";
   private static final String AMOUNT =   "amount";
   private static final String ENDTIME =  "endTime";
   private static final String STARTIME = "startTime";
   private static final String UID = "uid";
   private static final String USERID = "userId";
   private static String HH_MM_SS = " 23:59:59"; //时分秒
    /**
     * Description: 通过用户id查询资金流水记录
     * @author wangyaohui
     * @param  uid     要查询的用户id
     * Date    2018/4/13
     */
   @Adopt
   @RequestMapping("/t/get_accounts_uid.htm")
   public void getAccountsByUid(HttpServletResponse response, Long uid,@RequestParam(defaultValue = "15") Integer pageSize,
                                @RequestParam(defaultValue = "1") Integer pageNumber,String explain,String amount,
                                String endTime,String startTime){
       logger.info("Get Account list param is ----->[uid = {},busiNo={},amount={},endTime={},startTime={}]", uid,explain ,amount,endTime,startTime);
       ResponseData responseData = new ResponseData();
       try {
           if (uid == null ) {
               logger.warn("Get Account list param is null");
               responseData.setCode(ResponseStatus.PARAMNULL.getValue());
               responseData.setMsg("the freezBalance param userId is null");
               return;
           }
           HashMap<String, Object> stringStringHashMap = new HashMap<>();
           stringStringHashMap.put(UID,uid.toString());
           if(StringUtils.isNotBlank(explain))
             stringStringHashMap.put(BUSINO,explain);
           if(StringUtils.isNotBlank(amount))
             stringStringHashMap.put(AMOUNT,amount);
           if(StringUtils.isNotBlank(startTime)) {
               stringStringHashMap.put(STARTIME, startTime);
           }
           if(StringUtils.isNotBlank(endTime)) {
               if(endTime.length() <= Constants.TEN){
                   endTime += HH_MM_SS;
               }
               stringStringHashMap.put(ENDTIME, endTime);
           }
           accountFlowManager.getAccountsByUid(responseData,  uid, pageSize, pageNumber,stringStringHashMap);
           logger.info("Get Account list by uid is ok");
       } catch (Exception e) {
           logger.error("Get Account list by uid is error", e);
           responseData.setCode(ResponseStatus.ERR.getValue());
           responseData.setMsg("Get Account list by uid is error");
       } finally {
           ResponseUtil.responsePrint(response, responseData, logger);
       }
   }
   @RequestMapping("/t/get_accounts_param.htm")
    public void getAccountsByUids(HttpServletResponse response, Long uid,Integer pageSize,Integer pageNumber,String explain,String amount,
                                  String endTime,String startTime){
        getAccountsByUid(response,uid,pageSize,pageNumber,explain,amount,endTime,startTime);
    }
    /**
     * Description: 通过条件查询资金流水记录
     *
     * @author wangyaohui
     * Date    2018/4/13
     */
    @RequestMapping("/t/get_accounts_list.htm")
    public void getAccountFlowList(HttpServletResponse response,Integer pageSize ,Integer pageNumber,String uname,String occTime){
        logger.info("Get Account list param is ----->[uname = {},occTime={}]",uname,occTime);
        ResponseData responseData = new ResponseData();
        try {
            accountFlowManager.getAccountFlowList(responseData,pageSize,pageNumber,uname,occTime);
            logger.info("Get Account list by uid is ok");
        } catch (Exception e) {
            logger.error("Get Account list by uid is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get Account list by uid is error");
        } finally {
         ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 获取在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param response
     * @param pageSize
     * @param pageNumber
     * @param uname         客户名称
     * @param timeStart
     * @param timeEnd
     * @param channelCode   支付渠道
     * @return: void
     * date: 2018/9/5 15:29
     */
    @RequestMapping("/t/pay/get_online_recharge_list_by_condition.htm")
    public void getOnlineRechargeList(HttpServletResponse response,@RequestParam(defaultValue = "15") Integer pageSize,
                                      @RequestParam(defaultValue = "1") Integer pageNumber, String uname, String timeStart,
                                      String timeEnd, String channelCode, Short tradeType){
        logger.info("get Online Recharge list param is ----->[uname = {}, channelCode = {},timeStart = {},timeEnd = {}, tradeType = {}]",
            uname, channelCode, timeStart, timeEnd, tradeType);
        ResponseData responseData = new ResponseData();
        try {
            accountFlowManager.getOnlineRechargeList(responseData,pageSize,pageNumber,uname,channelCode,timeStart,timeEnd,tradeType);
            logger.info("get Online Recharge list is ok");
        } catch (Exception e) {
            logger.error("get Online Recharge list is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get Online Recharge list is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 导出在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param response
     * @param title
     * @param uname         客户名称
     * @param timeStart
     * @param timeEnd
     * @param channelCode   支付渠道
     * @param tradeType
     * @return: void
     * date: 2018/9/5 15:29
     */
    @RequestMapping("/t/pay/export_online_recharge_list_byCondition.htm")
    public void exportOnlineRechargeList(HttpServletResponse response,String title, String uname, String timeStart, String timeEnd, String channelCode, Short tradeType){
        logger.info("get Online Recharge list param is ----->[uname = {}, channelCode = {},timeStart = {},timeEnd = {}, tradeType = {}]",
            uname,channelCode, timeStart, timeEnd, tradeType);
        ResponseData responseData = new ResponseData();
        ServletOutputStream os = null;
        try {
            Workbook workbook = accountFlowManager.exportOnlineRechargeList(responseData,title,uname,channelCode,timeStart,timeEnd,tradeType);
            os = response.getOutputStream();
            response.reset();
            response.setContentType("application/msexcel");
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            //response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xlsx", "UTF-8"));

            workbook.write(os);
            logger.info("export Online Recharge list is ok");
        }catch (Exception e){
            logger.error("export Online Recharge listis error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("export Online Recharge listis error!");
            ResponseUtil.responsePrint(response,responseData,logger);
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                logger.error("It is error when close os",e);
            }
        }
    }


    /**
     * Description: 导出余额日志报表
     * @author: Louzifeng
     * @param response
     * @param title
     * @param occTime
     * @return: void
     * date:2018/9/14 14:24:17
     */
    @RequestMapping("/t/export_get_accounts.htm")
    public void exportAccounts(HttpServletResponse response,String title,String uname,String occTime){
        logger.info("get Online Accounts list param is ----->[uname = {},occTime = {}]",uname, occTime);
        ResponseData responseData = new ResponseData();
        ServletOutputStream output = null;
        try {
            Workbook workbook = accountFlowManager.exportAccountsList(responseData,title,uname,occTime);
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
            ResponseUtil.responsePrint(response,responseData,logger);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                logger.error("It is ok in exportAccountsList",e);
            }
        }
    }

    /**
     * Description: 导出余额日志明细报表
     * @author: Louzifeng
     * @param response
     * @param title
     * @param startTime
     * @param endTime
     * @return: void
     * date:2018/9/18
     */
    @RequestMapping("/t/export_get_accountsFlow.htm")
    public void exportAccountsFlowDetails(HttpServletResponse response,Long userId,String explain, String title, String amount,
                                          String endTime,String startTime){
        logger.info("get Online AccountsFlow list param is ----->[amount = {},explain = {},startTime = {},endTime = {}]",amount,explain, startTime, endTime);
        ResponseData responseData = new ResponseData();
        ServletOutputStream output = null;
        try {
            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
            stringObjectHashMap.put(USERID,userId.toString());
            if(StringUtils.isNotBlank(explain))
                stringObjectHashMap.put(BUSINO,explain);
            if(StringUtils.isNotBlank(amount))
                stringObjectHashMap.put(AMOUNT,amount);
            if(StringUtils.isNotBlank(startTime)) {
                stringObjectHashMap.put(STARTIME, startTime);
            }
            if(StringUtils.isNotBlank(endTime)) {
                if(endTime.length() <= Constants.TEN){
                    endTime += HH_MM_SS;
                }
                stringObjectHashMap.put(ENDTIME, endTime);
            }
            Workbook workbook = accountFlowManager.exportAccountsFlowList(responseData,title,stringObjectHashMap);
            output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(title + ".xls", "UTF-8"));
            response.setContentType("application/msexcel");
            workbook.write(output);
            logger.info("It is ok in AccountsFlowList");
        } catch (Exception e) {
            logger.error("It is error in AccountsFlowList", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Program is wrong!");
            ResponseUtil.responsePrint(response,responseData,logger);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                logger.error("It is ok in AccountsFlowList",e);
            }
        }
    }

    /**
     * Description: 查询是否有未处理的退款信息
     * @author: duanxiangming
     * @param response
     * date:2018/9/18
     */
    @RequestMapping("/t/check-payFlow-status.htm")
    public void checkPayFlowStatus(HttpServletResponse response,String channelFlowIds, Short tradeType){
        logger.info("check payFlow status param is ----->[channelFlowIds = {}, tradeType = {}]",channelFlowIds, tradeType);
        ResponseData responseData = new ResponseData();
        try {
            accountFlowManager.checkPayFlowStatus(channelFlowIds, tradeType, responseData);
            logger.info("check payFlow status");
        } catch (Exception e) {
            logger.error("It is error in check payFlow status", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check payFlow status is wrong!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
