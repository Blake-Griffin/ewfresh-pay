package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.util.ResponseData;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2018/10/18.
 */
public interface HATWithdrawManager {
    /**
     * Description: 商户提现申请
     * @author: zhaoqun
     * @param vo
     * date: 2018/8/7
     */
    ResponseData accountWithdraw(ResponseData responseData, Bill99WithdrawAccountVo vo) throws Exception;

    /**
     * Description: 提现明细查询
     * @author: zhaoqun
     * @param vo
     * date: 2018/8/9
     */
    void withdrawQuery(ResponseData responseData, Bill99WithdrawAccountVo vo) throws Exception;

    /**
     * Description: 获取提现手续费
     *
     * @param uId
     * @param amount
     * date: 2018/10/17
     * @author: zhaoqun
     */
    void getQueryFee(ResponseData responseData, String uId, String amount) throws Exception;

    /**
     * Description: 查询账户余额
     *
     * @param uId
     * date: 2018/10/18
     * @author: zhaoqun
     */
    void getBalanceFee(ResponseData responseData, String uId) throws Exception;
}
