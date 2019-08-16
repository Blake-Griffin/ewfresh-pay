package com.ewfresh.pay.manager.impl;

import com.ewfresh.pay.manager.PayChannelManager;
import com.ewfresh.pay.model.PayChannel;
import com.ewfresh.pay.service.PayChannelService;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by 王耀辉 on 2018/4/25.
 */
@Component
public class PayChannelManagerImpl implements PayChannelManager {
    @Autowired
    private PayChannelService payChannelService;
    @Override
    public void getPayCkanneByAll(ResponseData responseData) {
        List<PayChannel> payCkanneByAll = payChannelService.getPayCkanneByAll();
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setEntity(payCkanneByAll);
    }
}
