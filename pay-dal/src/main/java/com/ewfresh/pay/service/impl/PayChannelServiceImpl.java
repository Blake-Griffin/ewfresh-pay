package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.PayChannelDao;
import com.ewfresh.pay.model.PayChannel;
import com.ewfresh.pay.service.PayChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/25.
 */
@Service
public class PayChannelServiceImpl implements PayChannelService {
    @Autowired
    private PayChannelDao payChannelDao;
    @Override
    public List<PayChannel> getPayCkanneByAll() {
        return payChannelDao.getPayCkanneByAll();
    }

    @Override
    public List<PayChannel> getPayChannelByChannelName(String channelName, Short isBorrow, Short isLoan) {
        return payChannelDao.getPayChannelByChannelName(channelName, isBorrow, isLoan);
    }
}
