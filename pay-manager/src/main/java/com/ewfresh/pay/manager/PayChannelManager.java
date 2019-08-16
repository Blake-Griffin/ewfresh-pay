package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

/**
 * Description:查询支付渠道的信息的逻辑层
 * @author wangyaohui
 * Date 2018/4/11
 */
public interface PayChannelManager {
    /**
     * description:查询支付渠道的信息
     *
     * 返回的结果集
     * @author: wangyaohui
     */
    void getPayCkanneByAll(ResponseData responseData);

}
