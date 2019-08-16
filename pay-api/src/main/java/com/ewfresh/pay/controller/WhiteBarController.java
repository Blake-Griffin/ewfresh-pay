package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.WhiteBarManager;
import com.ewfresh.pay.model.WhiteBar;
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
import java.math.BigDecimal;

/**
 * @Author: LouZiFeng
 * @Description: 白条额度
 * @Param:
 * @Date: 2019/3/11
 */
@Controller
public class WhiteBarController {

    @Autowired
    private WhiteBarManager whiteBarManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @Author LouZiFeng
     * @Description 查询白条额度信息
     * @Date: 2019/3/11
     */
    @RequestMapping("/t/whiteBar/get_whiteBar.htm")
    public void getWhiteBarList(HttpServletResponse response, String uname, Short apprStatus, String start, String end,
                                @RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam(defaultValue = "15") Integer pageSize) {
        logger.info("It is method to get a WhiteBar in controller");
        ResponseData responseData = new ResponseData();
        try {
            whiteBarManager.getWhiteBarList(responseData, uname, apprStatus, start, end, pageNumber, pageSize);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author LouZiFeng
     * @Description 根据记录id查询白条额度信息
     * @Date: 2019/3/11
     */
    @RequestMapping("/t/whiteBar/get_whiteBar_byId.htm")
    public void getWhiteBarById(HttpServletResponse response, Integer recordId) {
        logger.info("It is method to get a WhiteBar in controller");
        ResponseData responseData = new ResponseData();
        try {
            if (recordId == null) {
                logger.info("calculate message count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
            }
            whiteBarManager.getWhiteBarById(responseData, recordId);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条额度信息
     * @Date: 2019/3/11
     */
    @Adopt
    @RequestMapping("/t/whiteBar/get_whiteBar_ByUid.htm")
    public void getWhiteBarByUid(HttpServletResponse response, Long uid) {
        logger.info("It is method to get a WhiteBar in controller");
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null) {
                logger.info("calculate message count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
            }
            whiteBarManager.getWhiteBarByUid(responseData, uid);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author: LouZiFeng
     * @Description: 修改审核状态信息
     * @Param: id
     * @Param: apprStatus
     * @Date: 2019/3/11
     */
    @RequestMapping("/t/whiteBar/update_apprStatus.htm")
    public void updateApprStatus(HttpServletResponse response, Integer id, Short apprStatus, String reason) {
        logger.info("It is method to update a whiteBar in controller the param of it is ");
        ResponseData responseData = new ResponseData();
        try {
            if (id == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("The update  a whiteBar is null");
                logger.warn("The param of update a whiteBar is null");
                return;
            }
            whiteBarManager.updateApprStatus(responseData, id, apprStatus, reason);
            logger.info("The method to update a whiteBar is success");
        } catch (Exception e) {
            logger.error("The method to update a whiteBar is failed！", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The method to update whiteBar  is failed！");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author: LouZiFeng
     * @Description: 添加额度信息
     * @Param: whiteBar
     * @Date: 2019/3/11 17:12
     */
    @Adopt
    @RequestMapping("/t/whiteBar/add_whiteBar.htm")
    public void addWhiteBar(HttpServletResponse response, WhiteBar whiteBar) {
        logger.info("It is method to add a whiteBar in controller the param of it is ");
        ResponseData responseData = new ResponseData();
        Long uid = whiteBar.getUid();
        try {
            if (uid == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("The add a whiteBar is null");
                logger.warn("The param of add a whiteBar is null");
                return;
            }
            whiteBarManager.addWhiteBar(responseData, whiteBar);
            logger.info("The method to add a whiteBar is success");
        } catch (Exception e) {
            logger.error("The method to add a whiteBar is failed！", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The method to add whiteBar  is failed！");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author: LouZiFeng
     * @Description: 添加申请额度信息
     * @Param: whiteBar
     * @Date: 2019/3/11
     */
    @Adopt
    @RequestMapping("/t/whiteBar/add_adjustRecord.htm")
    public void addAdjustRecord(HttpServletResponse response, Integer barId, BigDecimal adjustLimit) {
        logger.info("It is method to add a whiteBar in controller the param of it is adjustLimit={}", adjustLimit);
        ResponseData responseData = new ResponseData();
        try {
            if (barId == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("The add a whiteBar is null");
                logger.warn("The param of add a whiteBar is null");
                return;
            }
            whiteBarManager.addAdjustRecord(responseData, barId, adjustLimit);
            logger.info("The method to add a whiteBar is success");
        } catch (Exception e) {
            logger.error("The method to add a whiteBar is failed！", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The method to add whiteBar  is failed！");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author LouZiFeng
     * @Description 查询客户白条信息
     * @Date: 2019/3/22
     */
    @RequestMapping("/t/whiteBar/get_whiteBar_name.htm")
    public void getWhiteBarName(HttpServletResponse response, Integer id, String uname, Short useStatus, Integer period,
                                @RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam(defaultValue = "15") Integer pageSize) {
        logger.info("It is method to get a WhiteBar in controller");
        ResponseData responseData = new ResponseData();
        try {
            whiteBarManager.getWhiteBarName(responseData, id, uname, useStatus, period, pageNumber, pageSize);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }


    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条额度状态和之前额度信息
     * @Date: 2019/3/11
     */
    @Adopt
    @RequestMapping("/t/whiteBar/get_whiteBar_ByAdjustLimit.htm")
    public void getWhiteBarByAdjustLimit(HttpServletResponse response, Integer barId) {
        logger.info("It is method to get a WhiteBar in controller");
        ResponseData responseData = new ResponseData();
        try {
            if (barId == null) {
                logger.info("calculate message count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
            }
            whiteBarManager.getWhiteBarByAdjustLimit(responseData, barId);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author: LouZiFeng
     * @Description: 修改使用状态
     * @Param: useStatus
     * @Param: uid
     * @Date: 2019/3/11 17:12
     */
    @RequestMapping("/t/whiteBar/update_useStatus.htm")
    public void updateUseStatus(HttpServletResponse response, Short useStatus, Integer id) {
        logger.info("It is method to update a whiteBar in controller the param of it is ");
        ResponseData responseData = new ResponseData();
        try {
            if (id == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("The update  a whiteBar is null");
                logger.warn("The param of update a whiteBar is null");
                return;
            }
            whiteBarManager.updateUseStatus(responseData, id, useStatus);
            logger.info("The method to update a whiteBar is success");
        } catch (Exception e) {
            logger.error("The method to update a whiteBar is failed！", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The method to update whiteBar  is failed！");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author LouZiFeng
     * @Description 根据uid修改审核信息
     * @Param: uid
     * @Date: 2019/3/25
     */
    @RequestMapping("/t/whiteBar/update_whiteBar_id.htm")
    public void updateWhiteBarByUid(HttpServletResponse response, Integer recordId, WhiteBar whiteBar) {
        logger.info("It is method to get a WhiteBar in controller is recordId ={}, whiteBar={}", recordId, ItvJsonUtil.toJson(whiteBar));
        ResponseData responseData = new ResponseData();
        try {
            if (recordId == null) {
                logger.info("calculate message count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
                return;
            }
            whiteBarManager.updateWhiteBarByUid(responseData, recordId, whiteBar);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * @Author LouZiFeng
     * @Description 根据uid进行查询上个月所得额度
     * @Param: uid
     * @Date: 2019/3/25
     */
    @Adopt
    @RequestMapping("/t/whiteBar/get_whiteBar_month.htm")
    public void getWhiteBarByMonth(HttpServletResponse response, WhiteBar whiteBar) {
        logger.info("It is method to get a WhiteBar in controller ");
        ResponseData responseData = new ResponseData();
        Long uid = whiteBar.getUid();
        try {
            if (uid == null) {
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("calculate WhiteBar count param is null");
                logger.warn("The WhiteBar push query is null");
                return;
            }
            whiteBarManager.getWhiteBarByMonth(responseData, whiteBar);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!！", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!！");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }


    /**
     * @Author LouZiFeng
     * @Description 根据uid查询上个月是否申请过
     * @Param: uid
     * @Date: 2019/3/25
     */
    @Adopt
    @RequestMapping("/t/whiteBar/get_whiteBar_monthTotal.htm")
    public void getWhiteBarMonthTotal(HttpServletResponse response, Long uid) {
        logger.info("It is method to get a WhiteBar in controller is uid ={}" + uid);
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null) {
                logger.info("calculate message count param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("param is null");
            }
            whiteBarManager.getWhiteBarMonthTotal(responseData, uid);
            logger.info("The WhiteBar push query is success!");
        } catch (Exception e) {
            logger.error("The WhiteBar push query is error!", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("The  WhiteBar push query is error!");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
