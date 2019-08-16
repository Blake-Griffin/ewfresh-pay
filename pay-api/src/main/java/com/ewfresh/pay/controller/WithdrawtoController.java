package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.manager.WithdrawtoManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.util.Constants;
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
import java.util.zip.CheckedOutputStream;

/**
 * Description: 用户提现的方法
 * @author DuanXiangming
 * Date 2018/4/13
 */
@Controller
public class WithdrawtoController {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawtoController.class);
    @Autowired
    private WithdrawtoManager withdrawtoManager;
    @Autowired
    private RedisLockHandler lockHandler;

    private static final String WITHDRAWBYUID = "witdraw-by-uid";//提现lockName前缀
    private static final String FINALAPPROVE = "witdraw-final-approve";//提现四审lockName前缀

    /**
     * Description: 用户申请提现的方法
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/withdraw-by-uid.htm")
    public void withdrawByUid(HttpServletResponse response, Withdrawto withdrawto) {
        logger.info("withdraw by uid param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto));
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (withdrawto == null ) {
                logger.warn("the withdraw by uid param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param withdrawto is null");
                return;
            }
            if (withdrawto.getUid() == null ) {
                logger.warn("the withdraw by uid param uid is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param uid is null");
                return;
            }
            if (withdrawto.getUname() == null ) {
                logger.warn("the withdraw by uid param uname is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param uname is null");
                return;
            }
            if (withdrawto.getAmount() == null ) {
                logger.warn("the withdraw by uid param amount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param amount is null");
                return;
            }
            if (withdrawto.getBankAccountId() == null ) {
                logger.warn("the withdraw by uid param BankAccount is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param BankAccount is null");
                return;
            }
            if (withdrawto.getNickName() == null ) {
                logger.warn("the withdraw by uid param NickName is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param NickName is null");
                return;
            }
            if (withdrawto.getAccType() == null ) {
                logger.warn("the withdraw by uid param accType is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("the freezBalance param accType is null");
                return;
            }
            String lockName = WITHDRAWBYUID + Constants.JOINT + withdrawto.getUid() + withdrawto.getAmount();
            lock = new Lock(lockName,lockName);
            boolean flag = lockHandler.tryLock(lock);
            if (!flag) {
                logger.info(" checkWithdrawFourth agian by lock [lockName = {}]", lockName);
                return;
            }
            withdrawtoManager.withdrawByUid(responseData,  withdrawto);
            lockHandler.releaseLock(lock);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("withdraw by uid error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("releaseLock failed", e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("withdraw by uid err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 审核用户提现的方法（一审）
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    @RequestMapping("/t/check-withdraw-first.htm")
    public void checkWithdrawFirst(HttpServletResponse response, Withdrawto withdrawto , PayFlow payFlow , WithdrawApprRecord withdrawApprRecord) {
        logger.info("check withdraw param is ----->[withdrawto = {}, payFlow = {}, withdrawApprRecord = {}]",
            ItvJsonUtil.toJson(withdrawto),ItvJsonUtil.toJson(payFlow),ItvJsonUtil.toJson(withdrawApprRecord));
        ResponseData responseData = new ResponseData();
        try {
            if (withdrawto == null ) {
                logger.warn("check withdraw  param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param withdrawto is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("check withdraw  param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param id is null");
                return;
            }
            if (withdrawto.getApprover() == null) {
                logger.warn("check withdraw  param approver is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param approver is null");
                return;
            }
            if (withdrawto.getApprStatus() == null ) {
                logger.warn("check withdraw  param apprStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param apprStatus is null");
                return;
            }
            if (withdrawto.getApprStatus() == Constants.APPR_STATUS_6 && payFlow == null) {
                logger.warn("check withdraw  param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param payFlow is null");
                return;
            }
            if (withdrawto.getBeforeStatus() != Constants.APPR_STATUS_0 ||  (withdrawto.getApprStatus() != Constants.APPR_STATUS_1 && withdrawto.getApprStatus() != Constants.APPR_STATUS_5 )) {
                logger.warn("check withdraw  param beforeStatus or apprStatus is err");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param beforeStatus or apprStatus is err");
                return;
            }
            if (withdrawApprRecord.getBeforeStatus() == null) {
                logger.warn("withdrawApprRecord  param beforeStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param beforeStatus is null");
                return;
            }
            if (withdrawApprRecord.getDesp() == null) {
                logger.warn("withdrawApprRecord  param desp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param desp is null");
                return;
            }
            withdrawApprRecord.setWithdtoId(withdrawto.getId());
            withdrawtoManager.checkWithdraw(responseData,  withdrawto, payFlow, withdrawApprRecord);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("check withdraw  error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check withdraw  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    /**
     * Description: 审核用户提现的方法（二审）  2019年8月后废弃，提现审核流程只有 “一审===》四审”
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    @RequestMapping("/t/check-withdraw-second.htm")
    public void checkWithdrawSecond(HttpServletResponse response, Withdrawto withdrawto , PayFlow payFlow , WithdrawApprRecord withdrawApprRecord) {
        logger.info("check withdraw param is ----->[withdrawto = {}, payFlow = {}, withdrawApprRecord = {}]",
            ItvJsonUtil.toJson(withdrawto),ItvJsonUtil.toJson(payFlow),ItvJsonUtil.toJson(withdrawApprRecord));
        ResponseData responseData = new ResponseData();
        try {
            if (withdrawto == null ) {
                logger.warn("check withdraw  param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param withdrawto is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("check withdraw  param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param id is null");
                return;
            }
            if (withdrawto.getApprover() == null ) {
                logger.warn("check withdraw  param approver is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param approver is null");
                return;
            }
            if (withdrawto.getApprStatus() == null ) {
                logger.warn("check withdraw  param apprStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param apprStatus is null");
                return;
            }
            if (withdrawto.getApprStatus() == Constants.APPR_STATUS_6 && payFlow == null) {
                logger.warn("check withdraw  param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param payFlow is null");
                return;
            }
            if (withdrawto.getBeforeStatus() != Constants.APPR_STATUS_1 ||  (withdrawto.getApprStatus() != Constants.APPR_STATUS_2 && withdrawto.getApprStatus() != Constants.APPR_STATUS_5 )) {
                logger.warn("check withdraw  param beforeStatus or apprStatus is err");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param beforeStatus or apprStatus is err");
                return;
            }
            if (withdrawApprRecord.getBeforeStatus() == null) {
                logger.warn("withdrawApprRecord  param beforeStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param beforeStatus is null");
                return;
            }
            if (withdrawApprRecord.getDesp() == null) {
                logger.warn("withdrawApprRecord  param desp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param desp is null");
                return;
            }
            withdrawApprRecord.setWithdtoId(withdrawto.getId());
            withdrawtoManager.checkWithdraw(responseData,  withdrawto, payFlow, withdrawApprRecord);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("check withdraw  error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check withdraw  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    /**
     * Description: 审核用户提现的方法（三审）  2019年8月后废弃，提现审核流程只有 “一审===》四审”
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    @RequestMapping("/t/check-withdraw-third.htm")
    public void checkWithdrawThird(HttpServletResponse response, Withdrawto withdrawto , PayFlow payFlow , WithdrawApprRecord withdrawApprRecord) {
        logger.info("check withdraw param is ----->[withdrawto = {}, payFlow = {}, withdrawApprRecord = {}]",
            ItvJsonUtil.toJson(withdrawto),ItvJsonUtil.toJson(payFlow),ItvJsonUtil.toJson(withdrawApprRecord));
        ResponseData responseData = new ResponseData();
        try {
            if (withdrawto == null ) {
                logger.warn("check withdraw  param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param withdrawto is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("check withdraw  param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param id is null");
                return;
            }
            if (withdrawto.getApprover() == null ) {
                logger.warn("check withdraw  param approver is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param approver is null");
                return;
            }
            if (withdrawto.getApprStatus() == null ) {
                logger.warn("check withdraw  param apprStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param apprStatus is null");
                return;
            }
            if (withdrawto.getApprStatus() == Constants.APPR_STATUS_6 && payFlow == null) {
                logger.warn("check withdraw  param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param payFlow is null");
                return;
            }
            if (withdrawto.getBeforeStatus() != Constants.APPR_STATUS_2 ||  (withdrawto.getApprStatus() != Constants.APPR_STATUS_3 && withdrawto.getApprStatus() != Constants.APPR_STATUS_5 )) {
                logger.warn("check withdraw  param beforeStatus or apprStatus is err");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param beforeStatus or apprStatus is err");
                return;
            }
            if (withdrawApprRecord.getBeforeStatus() == null) {
                logger.warn("withdrawApprRecord  param beforeStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param beforeStatus is null");
                return;
            }
            if (withdrawApprRecord.getDesp() == null) {
                logger.warn("withdrawApprRecord  param desp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param desp is null");
                return;
            }
            withdrawApprRecord.setWithdtoId(withdrawto.getId());
            withdrawtoManager.checkWithdraw(responseData,  withdrawto, payFlow, withdrawApprRecord);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("check withdraw  error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check withdraw  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    /**
     * Description: 审核用户提现的方法（四审）
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    @RequestMapping("/t/check-withdraw-fourth.htm")
    public void checkWithdrawFourth(HttpServletResponse response, Withdrawto withdrawto , PayFlow payFlow , WithdrawApprRecord withdrawApprRecord) {
        logger.info("check withdraw param is ----->[withdrawto = {}, payFlow = {}, withdrawApprRecord = {}]",
            ItvJsonUtil.toJson(withdrawto),ItvJsonUtil.toJson(payFlow),ItvJsonUtil.toJson(withdrawApprRecord));
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        Short beforeStatus = withdrawto.getBeforeStatus();
        try {
            if (withdrawto == null ) {
                logger.warn("check withdraw  param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param withdrawto is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("check withdraw  param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param id is null");
                return;
            }
            if (withdrawto.getApprover() == null ) {
                logger.warn("check withdraw  param approver is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param approver is null");
                return;
            }
            if (withdrawto.getApprStatus() == null ) {
                logger.warn("check withdraw  param apprStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param apprStatus is null");
                return;
            }
            if (withdrawto.getApprStatus() == Constants.APPR_STATUS_6 && payFlow == null) {
                logger.warn("check withdraw  param payFlow is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param payFlow is null");
                return;
            }
            if (beforeStatus != Constants.APPR_STATUS_1||  (withdrawto.getApprStatus() != Constants.APPR_STATUS_4 && withdrawto.getApprStatus() != Constants.APPR_STATUS_5 )) {
                logger.warn("check withdraw  param beforeStatus or apprStatus is err");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param beforeStatus or apprStatus is err");
                return;
            }
            if (withdrawApprRecord.getBeforeStatus() == null) {
                logger.warn("withdrawApprRecord  param beforeStatus is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param beforeStatus is null");
                return;
            }
            if (withdrawApprRecord.getDesp() == null) {
                logger.warn("withdrawApprRecord  param desp is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("withdrawApprRecord  param desp is null");
                return;
            }
            String lockName = FINALAPPROVE + Constants.JOINT + withdrawto.getId() + beforeStatus;
            lock = new Lock(lockName,lockName);
            boolean flag = lockHandler.tryLock(lock);
            if (!flag) {
                logger.info(" checkWithdrawFourth agian by lock [lockName = {}]", lockName);
                responseData.setMsg("checkWithdrawFourth request agian");
                responseData.setCode(ResponseStatus.OK.getValue());
                return;
            }
            Withdrawto wd = withdrawtoManager.getWithdrawtoInfoById(withdrawto.getId());
            if(!beforeStatus.equals(wd.getApprStatus())) {
                logger.info(" checkWithdrawFourth agian, status not match");
                responseData.setMsg("checkWithdrawFourth request agian");
                responseData.setCode(ResponseStatus.OK.getValue());
                return;
            }
            withdrawApprRecord.setWithdtoId(withdrawto.getId());
            withdrawtoManager.checkWithdraw(responseData,  withdrawto, payFlow, withdrawApprRecord);
            lockHandler.releaseLock(lock);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("check withdraw  error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("releaseLock failed", e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check withdraw  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }
    /**
     * Description: 审核用户提现的方法（出纳审核不通过时，总裁确认不通过）
     * @author zhaoqun
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/11/11
     */
    @RequestMapping("/t/check-withdraw-fifth.htm")
    public void checkWithdrawFifth(HttpServletResponse response, Withdrawto withdrawto) {
        logger.info("check withdraw param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto));
        ResponseData responseData = new ResponseData();
        try {
            if (withdrawto.getId() == null ) {
                logger.warn("check withdraw  param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param id is null");
                return;
            }
            if (withdrawto.getApprover() == null ) {
                logger.warn("check withdraw  param approver is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param approver is null");
                return;
            }
            if (withdrawto.getVersion() == null ) {
                logger.warn("check withdraw  param version is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("check withdraw  param version is null");
                return;
            }
            withdrawto.setApprStatus(Constants.APPR_STATUS_10);//总裁确认不通过
            withdrawto.setBeforeStatus(Constants.APPR_STATUS_5);
            logger.info("check withdraw param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto));
            withdrawtoManager.checkWithdrawNotAllow(responseData,  withdrawto);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("check withdraw  error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("check withdraw  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 根据UID获取用户提现的方法
     * @author DuanXiangming
     * @param  uid          用户ID
     * Date    2018/4/13
     */
    @Adopt
    @RequestMapping("/t/get-withdraw-by-uid.htm")
    public void getWithdrawByUid(HttpServletResponse response, Long uid , Long id, @RequestParam(defaultValue = "1") Integer pageNum ,@RequestParam(defaultValue = "15") Integer pageSize) {
        logger.info("get withdraw by uid param is ----->[uid = {}, id = {}, pageNum = {}, pageSize = {}]", uid , id, pageNum, pageSize);
        ResponseData responseData = new ResponseData();
        try {
            if (uid == null ) {
                logger.warn("the withdraw by uid param uid is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            withdrawtoManager.getWithdrawByUid(responseData, uid , id, pageNum,pageSize);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("get withdraw by uid error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get withdraw by uid err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 后台取消提现的方法
     * @author DuanXiangming
     * @param withdrawto
     * @param isSelf  前台 0  后台1
     * Date    2018/4/14
     */
    @RequestMapping("/t/cancel-withdraw-by-id-back.htm")
    public void cancelWithdrawByidBack(HttpServletResponse response, Withdrawto withdrawto, Short isSelf  ) {
        logger.info("cancel withdraw by id param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto) );
        ResponseData responseData = new ResponseData();
        try {
            if (withdrawto == null ) {
                logger.warn("the withdraw by uid param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("the withdraw by uid param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            if (withdrawto.getCancelId() == null ) {
                logger.warn("the withdraw by uid param cancelId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param cancelId is null");
                return;
            }
            if (isSelf == null ) {
                logger.warn("the withdraw by uid param isSelf is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param cancelId is null");
                return;
            }
            withdrawtoManager.cancelWithdrawByid(responseData, withdrawto,isSelf);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("get withdraw by id error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get withdraw by id err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 前台取消提现的方法
     * @author DuanXiangming
     * @param withdrawto
     * Date    2018/4/14
     */
    @Adopt
    @RequestMapping("/t/cancel-withdraw-by-id.htm")
    public void cancelWithdrawByid(HttpServletResponse response, Withdrawto withdrawto ) {
        logger.info("cancel withdraw by id param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto) );
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            if (withdrawto == null ) {
                logger.warn("the withdraw by uid param withdrawto is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            if (withdrawto.getId() == null ) {
                logger.warn("the withdraw by uid param id is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            Long cancelId = withdrawto.getCancelId();
            if ( cancelId == null ) {
                logger.warn("the withdraw by uid param cancelId is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param cancelId is null");
                return;
            }
            String lockName = Constants.BALANCE_WITHDRAWTO + cancelId;
            lock = new Lock( lockName,cancelId.toString());
            boolean flag = lockHandler.tryLock(lock);
            if (!flag){
                logger.info(" commit cancel withdrawto agian by lock [lockName = {}]", lockName);
                responseData.setMsg("commit request agian");
                responseData.setCode(ResponseStatus.OK.getValue());
            }
            withdrawtoManager.cancelWithdrawByid(responseData, withdrawto,Constants.SHORT_ZERO);
            logger.info(responseData.getMsg());
            lockHandler.releaseLock(lock);
        } catch (Exception e) {
            logger.error("get withdraw by id error", e);
            try {
                lockHandler.releaseLock(lock);
            } catch (Exception e1) {
                logger.error("release lock err lock = " + ItvJsonUtil.toJson(lock),e);
            }
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get withdraw by id err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * Description:  获取提现记录详情的方法
     * @author DuanXiangming
     * @param id
     * Date    2018/4/14
     */
    @RequestMapping("/t/get-withdraw-by-id.htm")
    public void getWithdrawByid(HttpServletResponse response, Long id  ) {
        logger.info("get withdraw by id param is ----->[id = {}]", id );
        ResponseData responseData = new ResponseData();

        try {
            if (id == null ) {
                logger.warn("the withdraw by uid param uid is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg("get withdraw by uid param uid is null");
                return;
            }
            withdrawtoManager.getWithdrawByid(responseData, id);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("get withdraw by id error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get withdraw by id err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description:       根据条件查询提现记录列表
     * @author DuanXiangming
     * @param uname       用户名
     * @param status      审核状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * Date    2018/4/14
     */
    @RequestMapping("/t/get-withdrawtos.htm")
    public void getWithdrawtos(HttpServletResponse response, String uname , String nickName, Short status, Short beforeStatus, String startTime, String endTime,
                               @RequestParam(defaultValue = "1") Integer pageNum ,@RequestParam(defaultValue = "15") Integer pageSize) {
        logger.info("get withdrawtos  param is ----->[uname = {}, nickName = {}, status = {} , beforeStatus = {}, startTime = {} , endTime = {}, pageNum = {}, pageSize = {}]",
            uname, nickName, status,beforeStatus,startTime,endTime, pageNum, pageSize);
        ResponseData responseData = new ResponseData();
        try {
            withdrawtoManager.getWithdrawtos(responseData, uname, nickName, status, beforeStatus, startTime, endTime, pageNum, pageSize);
            logger.info(responseData.getMsg());
        } catch (Exception e) {
            logger.error("get withdrawtos error", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg("get withdrawtos  err");
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }



}
