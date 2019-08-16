package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

/**
 * description: 店铺提现的逻辑管理层
 * @author: JiuDongDong
 * date: 2019/6/17.
 */
public interface WithdrawToShopManager {
    void getCanWithdrawMoney(ResponseData responseData, String mchntNo) throws Exception;
}
