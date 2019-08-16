package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.util.ResponseData;

import java.math.BigDecimal;

/**
 * Description: 关于余额使用的manager层接口
 * @author DuanXiangming
 * Date 2018/4/11
 */
public interface BalanceManager {

    /**
     * Description: 冻结余额的方法,用于支付和体现
     * @author DuanXiangming
     * @param amount      涉及的金额
     * @param userId       用户id
     * @param orderId
     * @param targetAcc   目标账户
     */
    void freezBalance(ResponseData responseData, BigDecimal amount, Long userId, Long orderId, String targetAcc);
    /**
     * Description: 使用余额的支付方法
     * @author DuanXiangming
     * @param  payFlow      封装交易流水的方法
     * Date    2018/4/11
     */
    void payByBalance(ResponseData responseData, PayFlow payFlow, Long timeStamp );
    /**
     * Description: 获取用户余额的方法
     * @author DuanXiangming
     * @param userId       用户id
     * Date    2018/4/11
     */
    void getBalanceByUid(ResponseData responseData, Long userId);
    /**
     * Description:线下充值的方法
     * @author DuanXiangming
     * @param payFlow
     * @param withdrawto
     */
    void reCharge(ResponseData responseData, PayFlow payFlow, Withdrawto withdrawto);
    /**
     * Description: 内部扣减余额的方法
     * @author DuanXiangming
     * @param  payFlow       封装交易流水的对象
     * Date    2018/4/11
     */
    void abatementBalance(ResponseData responseData, PayFlow payFlow);
    /**
     * Description: 充值错误之后提现的方法
     * @author DuanXiangming
     * @param  payFlow
     * @param uname
     */
    void withdarwoFalse(ResponseData responseData, PayFlow payFlow, String uname);
}
