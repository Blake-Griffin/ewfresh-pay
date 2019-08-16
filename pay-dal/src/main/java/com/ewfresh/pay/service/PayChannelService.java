package com.ewfresh.pay.service;

import com.ewfresh.pay.model.PayChannel;

import java.util.List;

/**
 * Description:查询支付渠道的信息的事务层
 * @author wangyaohui
 * Date 2018/4/11
 */
public interface PayChannelService {
    /**
     * description:查询支付渠道的信息
     *
     * @return List
     * 返回的结果集
     * @author: wangyaohui
     */
    List<PayChannel> getPayCkanneByAll();

    // 根据渠道名称获取该渠道下的所有启用的支付渠道     jiudongdong
    List<PayChannel> getPayChannelByChannelName(String channelName, Short isBorrow, Short isLoan);
}
