package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.manager.BillRepayManager;
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
 * description:白条还款接口
 * @author huboyang
 * @param
 */

@Controller
public class BillRepayController {
    private Logger logger= LoggerFactory.getLogger(getClass());
    @Autowired
    private BillRepayManager billRepayManager;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    @RequestMapping("/t/get_whiteBill.htm")
    @Adopt
    //返回用户需要还款白条账单信息
    public void getAllBillByUid(HttpServletResponse response,Long uid){
        logger.info("the param is ----->uid={}",uid);
        ResponseData responseData = new ResponseData();
        try{
            if(uid==null){
                logger.warn("Get Bill  by uid param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }

            billRepayManager.getAllBillByUid(responseData,uid);
            logger.info("Get All Bill by uid param is ok");
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        }catch(Exception e){
            logger.error("Get All Bill by uid param is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        }finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    @Adopt
    @RequestMapping("/t/get_whiteRepay.htm")
    //返回用户还款详情信息
    public void getRepayBillOrder(HttpServletResponse response,String payType,String payMode,String orderIp,String uid,String payTimestamp,
                                 String ids,String uname,String channelType,String client,String bizType,String cardType ){
        logger.info("payType={},payMode={},orderIp={},uid={},payTimestamp={},ids={},uname={},channelType={},client={},bizType={},cardType={}",
                payType,payMode,orderIp,uid,payTimestamp,ids,uname,channelType,payType,client,bizType,cardType);
        ResponseData responseData = new ResponseData();
        try{
            if(uid == null ||payType == null||orderIp == null||payTimestamp == null||ids == null||channelType == null){
                logger.info("repay Details param  is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            boolean balanceAndBarLock = this.balanceAndBarLock.getBalanceAndBarLock(uid);
            if(!balanceAndBarLock){
                responseData.setMsg("commit request agian");
                responseData.setCode(ResponseStatus.OK.getValue());
                return;
            }
            billRepayManager.getBillDetails(responseData,payMode,orderIp,uid,payTimestamp,ids,uname,payType,channelType,client,bizType,cardType );
            this.balanceAndBarLock.releaseLock(uid);
        }catch (Exception e){
            logger.error("repay Details param----->whiteRepayDetails is err", e);
            balanceAndBarLock.releaseLock(uid);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        }finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    @Adopt
    @RequestMapping("/t/get_repayRecord.htm")
    public void getRepayRecord(HttpServletResponse response,String billId,@RequestParam(defaultValue = "15") Integer pageSize,
                               @RequestParam(defaultValue = "1") Integer pageNumber){
        logger.info("param is billId---------billId={}",billId);
        ResponseData responseData = new ResponseData();
            try {
                if (billId == null) {
                    logger.warn("uid is null");
                    responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                    responseData.setMsg(" param uid is null");
                    return;
                }
                billRepayManager.getRepayRecord(responseData,billId,pageSize, pageNumber);
            } catch (Exception e) {
                logger.error(" getRepayRecord is error", e);
                responseData.setCode(ResponseStatus.ERR.getValue());
                responseData.setMsg("getRepayRecord is error");
            } finally {
                ResponseUtil.responsePrint(response, responseData, logger);
            }
        }

}
