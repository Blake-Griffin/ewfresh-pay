package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.lock.Lock;
import com.ewfresh.commons.util.lock.RedisLockHandler;
import com.ewfresh.pay.manager.WithdrawToShopManager;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;
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

/**
 * Description: 店铺提现的接口
 * @author: JiuDongDong
 * date: 2019/6/17 20:32
 */
@Controller
public class WithdrawToShopController {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawToShopController.class);
    @Autowired
    private WithdrawToShopManager withdrawToShopManager;
    @Autowired
    private RedisLockHandler lockHandler;

    private static final String SHOP_WITHDRAW = "shop-withdraw";//提现lockName前缀

    /**
     * Description: 店铺查询可提现金额接口（后台）
     * @author JiuDongDong
     * @param  mchntNo  申请提现的商户编号
     * @param  uid 用户id
     * date: 2019/6/17 21:12
     */
    @Adopt
    @RequestMapping("/t/get-can-withdraw-money.htm")
    public void getCanWithdrawMoney(HttpServletResponse response, String mchntNo, Long uid) {
        logger.info("It is now in WithdrawToShopController.getCanWithdrawMoney, the params are: " +
                "[mchntNo = {}, uid = {}]", mchntNo, uid);
        ResponseData responseData = new ResponseData();
        Lock lock = null;
        try {
            // todo uid 从本地变量里取
            if (StringUtils.isBlank(mchntNo) || null == uid) {
                logger.error("The params has empty for WithdrawToShopController.getCanWithdrawMoney" +
                        ", mchntNo = " + mchntNo + ", uid = " + uid);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            withdrawToShopManager.getCanWithdrawMoney(responseData,  mchntNo);
            logger.info("It is OK in WithdrawToShopController.getCanWithdrawMoney");
        } catch (Exception e) {
            logger.error("Errors occurred in WithdrawToShopController.getCanWithdrawMoney", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 店铺提现接口（后台）
     * @author JiuDongDong
     * @param  mchntNo	商户编号
     * @param  sysOrderId  易网聚鲜平台提现订单号
     * @param  withdrawType	0 – CUPS通道商户提现    1 – JF通道商户t0提现     2 – JF通道商户tz提现
     * @param  withdrawAmt 提现金额,单位分
     * @param  uid 用户id
     * date: 2019/6/17 21:12
     */
//    @Adopt
//    @RequestMapping("/t/get-can-withdraw-money.htm")
//    public void getCanWithdrawMoney(HttpServletResponse response, String sysOrderId, String mchntNo,
//                                    Long uid, String withdrawType, String withdrawAmt) {
//        logger.info("It is now in WithdrawToShopController.getCanWithdrawMoney, the params are: " +
//                "[mchntNo = {}, sysOrderId = {}, withdrawType = {}, withdrawAmt = {}, uid = {}]",
//                mchntNo, sysOrderId, withdrawType, withdrawAmt, uid);
//        ResponseData responseData = new ResponseData();
//        Lock lock = null;
//        try {
//            // todo uid 从本地变量里取
//            if (StringUtils.isBlank(mchntNo) || null == uid) {
//                logger.error("The params has empty for WithdrawToShopController.getCanWithdrawMoney" +
//                        ", mchntNo = " + mchntNo + ", uid = " + uid);
//                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
//                responseData.setMsg(ResponseStatus.PARAMNULL.name());
//                return;
//            }
//            String lockName = SHOP_WITHDRAW + Constants.JOINT + uid;
//            lock = new Lock(lockName,lockName);
//            boolean flag = lockHandler.tryLock(lock);
//            if (!flag) {
//                logger.info("A shop withdraw is on processing, [lockName = {}]", lockName);
//                return;
//            }
//            withdrawToShopManager.withdrawByUid(responseData,  withdrawto);
//            lockHandler.releaseLock(lock);
//            logger.info(responseData.getMsg());
//        } catch (Exception e) {
//            logger.error("withdraw by uid error", e);
//            try {
//                lockHandler.releaseLock(lock);
//            } catch (Exception e1) {
//                logger.error("releaseLock failed", e);
//            }
//            responseData.setCode(ResponseStatus.ERR.getValue());
//            responseData.setMsg("withdraw by uid err");
//        } finally {
//            ResponseUtil.responsePrint(response, responseData, logger);
//        }
//
//    }


}
