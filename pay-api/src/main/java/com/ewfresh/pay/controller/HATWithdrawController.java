package com.ewfresh.pay.controller;

import com.ewfresh.commons.Adopt;
import com.ewfresh.pay.manager.HATWithdrawManager;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.ewfresh.pay.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * description: HAT 提现相关控制层
 *
 * @author: ZhaoQun
 * date: 2018/10/18.
 */
@Controller
public class HATWithdrawController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private HATWithdrawManager hatWithdrawManager;
    /**
     * Description: 商户提现申请
     *
     * @param vo date: 2018/8/7
     * @author: zhaoqun
     */
    @Adopt
    @RequestMapping(value = "/t/bill-merchant-account-withdraw.htm")
    public void accountWithdraw(HttpServletResponse response, Bill99WithdrawAccountVo vo) {
        ResponseData responseData = new ResponseData();
        try {
            responseData = hatWithdrawManager.accountWithdraw(responseData, vo);
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.accountWithdraw", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }


    /**
     * Description: 提现明细查询
     *
     * @param vo date: 2018/8/9
     * @author: zhaoqun
     */
    @Adopt
    @RequestMapping(value = "/p/bill/merchant-account-withdraw-callback.htm")
    public void withdrawQuery(HttpServletResponse response, Bill99WithdrawAccountVo vo) {
        ResponseData responseData = new ResponseData();
        try {
            hatWithdrawManager.withdrawQuery(responseData, vo);
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.withdrawQuery", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }


    /**
     * Description: 获取提现手续费
     *
     * @param uId
     * @param amount
     * date: 2018/10/17
     * @author: zhaoqun
     */
    @Adopt
    @RequestMapping(value = "/t/bill-merchant-withdraw-queryFee.htm")
    public void getQueryFee(HttpServletResponse response, String uId, String amount) {
        ResponseData responseData = new ResponseData();
        try {
            hatWithdrawManager.getQueryFee(responseData, uId, amount);
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.getQueryFee", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * Description: 查询账户余额
     * @param uId
     * date: 2018/10/18
     * @author: zhaoqun
     */
    @Adopt
    @RequestMapping(value = "/p/bill-merchant-withdraw-balanceFee.htm")
    public void getBalanceFee(HttpServletResponse response, String uId) {
        ResponseData responseData = new ResponseData();
        try {
            hatWithdrawManager.getBalanceFee(responseData, uId);
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.getBalanceFee", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
}
