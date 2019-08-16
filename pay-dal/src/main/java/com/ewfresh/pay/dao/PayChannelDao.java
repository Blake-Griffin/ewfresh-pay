package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.PayChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayChannelDao {
    // 根据渠道名称获取该渠道下的所有启用的支付渠道     jiudongdong
    List<PayChannel> getPayChannelByChannelName(@Param("channelName") String channelName, @Param("isBorrow") Short isBorrow, @Param("isLoan") Short isLoan);

    int deleteByPrimaryKey(Integer id);

    int insert(PayChannel record);

    int insertSelective(PayChannel record);

    PayChannel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PayChannel record);

    int updateByPrimaryKey(PayChannel record);

    /**
     * description:查询支付渠道的信息
     *
     * @return List
     * 返回的结果集
     * @author: wangyaohui
     */
    List<PayChannel> getPayCkanneByAll();
}