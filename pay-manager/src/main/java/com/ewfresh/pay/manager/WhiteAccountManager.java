package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.util.ResponseData; /**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/15.
 */
public interface WhiteAccountManager {

    /**
     * Description: 获取白条账户余额
     * @author: ZhaoQun
     * @param userId
     * date: 2019/3/15 9:04
     */
    void getWhiteAccountByUid(ResponseData responseData, Long userId);

    /**
     * Description: 白条支付
     * @author: ZhaoQun
     * @param payFlow
     * @param payWay
     * @param timeStamp
     * date: 2019/3/18 09:24
     */
    void payByWhite(ResponseData responseData, PayFlow payFlow, Short payWay, Long timeStamp);
}
