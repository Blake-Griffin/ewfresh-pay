package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.manager.SettleRecordManager;
import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.util.Constants;
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
import java.math.BigDecimal;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/22 0022
 */
@Controller
public class SettleRecordController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SettleRecordManager settleRecordManager;
    @Autowired
    private RedisLockHandler lockHandler;

    /**
     * Description: 通过店铺id查询账单记录
     * @author wangyaohui
     * @param  shopId     要查询的用户id
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/get-SettleRecords-shopId.htm")
    public void getSettleRecordByShopId(HttpServletResponse response, Integer shopId, @RequestParam(defaultValue = "15") Integer pageSize,
                                    @RequestParam(defaultValue = "1") Integer pageNumber, String batchNo, Short orderStatus, Short settleStatus, String successTime){
        logger.info("Get settle records list params  are ----->[shopId = {}, pageSize = {}, pageNumber = {}, batchNo = {}, orderStatus = {}, settleStatus = {}, successTime = {}]", shopId, pageSize, pageNumber,batchNo,orderStatus,settleStatus,successTime);
        ResponseData responseData = new ResponseData();
        try {
            if (shopId == null ) {
                logger.warn("Get settle records list param shopId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param userId is null");
                return;
            }
            settleRecordManager.getSettleRecordByShopId(responseData,shopId, pageSize, pageNumber, batchNo, orderStatus, settleStatus, successTime);
            logger.info("Get settle records list is ok");
        } catch (Exception e) {
            logger.error("Get settle records list is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("Get settle records list is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 添加一条结算记录的方法
     * @author wangyaohui
     * @param  shopId     要添加结算的店铺ID
     * @param  settleFlag 全部结算的标识
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/add-SettleRecord.htm")
    public void addSettleRecord(HttpServletResponse response, Integer shopId, String settleFlag){
        logger.info("add-SettleRecord params  are ----->[shopId = {}, payflows = {}, settleFlag = {}]", shopId, settleFlag);
        ResponseData responseData = new ResponseData();
        try {
            if (shopId == null ) {
                logger.warn("add-SettleRecord param shopId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("add-SettleRecord param userId is null");
                return;
            }
            settleRecordManager.addSettleRecord(responseData,shopId,null, settleFlag,null);
            logger.info("add-SettleRecord is ok");
        } catch (Exception e) {
            logger.error("add-SettleRecord is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("add-SettleRecord is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 获取分账审核列表的方法
     * @author DXM
     * @param  shopName    过滤条件之店铺名称
     * Date    2018/4/13
     */
    @RequestMapping("/t/get-settleRecords.htm")
    public void getSettleRecords(HttpServletResponse response,Integer shopId, String shopName, Short settleStatus, @RequestParam(defaultValue = "15") Integer pageSize,
                                 @RequestParam(defaultValue = "1") Integer pageNumber){
        logger.info("get-settleRecords params  are ----->[shopName = {},shopId = {},settleStatus = {}, pageSize = {}, pageNumber = {}]",shopName, shopId, settleStatus ,pageSize,pageNumber);
        ResponseData responseData = new ResponseData();
        try {
            settleRecordManager.getSettleRecords(responseData,shopName,shopId,settleStatus,pageSize,pageNumber);
            logger.info("get-settleRecords is ok");
        } catch (Exception e) {
            logger.error("get-settleRecords is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get-settleRecords is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 商城获取分账审核列表的方法
     * @author DXM
     * @param  shopId    过滤条件之店铺名称
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/get-settleRecords-shop.htm")
    public void getSettleRecords(HttpServletResponse response, Integer shopId, Short settleStatus, @RequestParam(defaultValue = "15") Integer pageSize,
                                 @RequestParam(defaultValue = "1") Integer pageNumber){
        logger.info("get-settleRecords params  are ----->[shopId = {},settleStatus = {}, pageSize = {}, pageNumber = {}]",shopId,settleStatus ,pageSize,pageNumber);
        ResponseData responseData = new ResponseData();
        try {
            settleRecordManager.getSettleRecords(responseData,null, shopId, settleStatus,pageSize,pageNumber);
            logger.info("get-settleRecords is ok");
        } catch (Exception e) {
            logger.error("get-settleRecords is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get-settleRecords is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 根据批次号获取具体分账流水
     * @author DXM
     * @param  batchNo    分账批次号
     * Date    2018/4/13
     */
    @RequestMapping("/t/get-settleRecords-by-batchNo.htm")
    public void getSettleRecordsByBatchNo(HttpServletResponse response, String batchNo,@RequestParam(defaultValue = "15") Integer pageSize,
                                 @RequestParam(defaultValue = "1") Integer pageNumber){
        logger.info("get-settleRecords-by-batchNo params  are ----->[batchNo = {}, pageSize = {}, pageNumber = {}]",batchNo,pageSize,pageNumber);
        ResponseData responseData = new ResponseData();
        try {
            settleRecordManager.getSettleRecordsByBatchNo(responseData,batchNo,pageSize,pageNumber);
            logger.info("get-settleRecords-by-batchNo is ok");
        } catch (Exception e) {
            logger.error("get-settleRecords-by-batchNo is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get-settleRecords-by-batchNo is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 审核分账内容的接口
     * @author DXM
     * @param  settleRecord   分装审核的内容
     * Date    2018/4/13
     */
    @RequestMapping("/t/appr-by-batchNo.htm")
    public void apprByBatchNo(HttpServletResponse response, SettleRecord settleRecord){
        logger.info("appr-by-batchNo params  are ----->[settleRecord = {}]",ItvJsonUtil.toJson(settleRecord));
        ResponseData responseData = new ResponseData();
        try {
            if (settleRecord == null ) {
                logger.warn("appr-by-batchNo param settleRecord is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("appr-by-batchNo param userId is null");
                return;
            }
            if (settleRecord.getId() == null ) {
                logger.warn("appr-by-batchNo param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("add-SettleRecord param userId is null");
                return;
            }
            if (settleRecord.getOperator() == null ) {
                logger.warn("appr-by-batchNo param operator is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("appr-by-batchNo param operator is null");
                return;
            }
            if (settleRecord.getSettleStatus() == null ) {
                logger.warn("appr-by-batchNo param SettleStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("appr-by-batchNo param SettleStatus is null");
                return;
            }
            settleRecordManager.apprByBatchNo(responseData,settleRecord);
            logger.info("appr-by-batchNo is ok");
        } catch (Exception e) {
            logger.error("appr-by-batchNo is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("appr-by-batchNo is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * Description: 与快钱申请结算的方法
     * @author DXM
     * @param  id   分帐的批次号
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/settle-with-hat.htm")
    public void settleWithHat(HttpServletResponse response, Integer id){
        logger.info("settle-with-hat params  are ----->[id = {}]",id);
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (id == null) {
                logger.warn("settle-with-hat param batchNo is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("settle-with-hat param batchNo is null");
                return;
            }
            String lockName = Constants.SETTLE_WHIT_HAT + id;
            lock = new Lock(lockName,lockName);
            lockHandler.tryLock(lock);
            settleRecordManager.settleWithHat(responseData,id);
            logger.info(responseData.getMsg());
            lockHandler.releaseLock(lock);
        } catch (Exception e) {
            logger.error("appr-by-batchNo is error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("release lock err",e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("appr-by-batchNo is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 根据id查询分账结算记录
     * @author DXM
     * @param  id   分帐的批次号
     * Date    2018/4/13
     */
    @RequestMapping("/t/get-settle-by-id.htm")
    public void getSettleRecordById(HttpServletResponse response, Integer id){
        logger.info("getSettleRecordById params  are ----->[id = {}]",id);
        ResponseData responseData = new ResponseData();
        try {
            if (id == null) {
                logger.warn("getSettleRecordById param batchNo is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("getSettleRecordById param batchNo is null");
                return;
            }
            settleRecordManager.getSettleRecordById(responseData,id);
            logger.info("getSettleRecordById is ok");
        } catch (Exception e) {
            logger.error("getSettleRecordById is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("getSettleRecordById is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 根据id查询分账结算记录
     * @author DXM
     * @param  batchNo   分帐的批次号
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/p/query-settle-by-id.htm")
    public void querySettleRecordById(HttpServletResponse response, String batchNo){
        logger.info("getSettleRecordById params  are ----->[batchNo = {}]",batchNo);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(batchNo)) {
                logger.warn("querySettleRecordById param batchNo is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("querySettleRecordById param batchNo is null");
                return;
            }
            settleRecordManager.querySettleRecordById(responseData,batchNo);
            logger.info("querySettleRecordById is ok");
        } catch (Exception e) {
            logger.error("getSettleRecordById is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("querySettleRecordById is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 退货分账
     * @author DXM
     * @param  id   分帐的id
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/p/settle/refund.htm")
    public void settleRefund(HttpServletResponse response, Integer id , BigDecimal refundAmount){
        logger.info("settleRefund params  are ----->[id = {},refundAmount = {}]",id,refundAmount);
        ResponseData responseData = new ResponseData();
        try {
            if (id == null) {
                logger.warn("settleRefund param batchNo is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("settleRefund param batchNo is null");
                return;
            }
            settleRecordManager.settleRefund(responseData,id, refundAmount);
            logger.info("settleRefund is ok");
        } catch (Exception e) {
            logger.error("getSettleRecordById is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("settleRefund is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    @Autowired
    private AccountFlowRedisService accountFlowRedisService;


    @Adopt
    @RequestMapping("/p/settle/setquery.htm")
    public void setquery(HttpServletResponse response, String id ){
        logger.info("setquery params  are ----->[id = {},refundAmount = {}]",id);
        ResponseData responseData = new ResponseData();
        try {
            if (id == null) {
                logger.warn("setquery param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("setquery param batchNo is null");
                return;
            }
            accountFlowRedisService.setQueryBalanceByHAT(id);
            logger.info("settleRefund is ok");
        } catch (Exception e) {
            logger.error("getSettleRecordById is error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("settleRefund is error");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

}
