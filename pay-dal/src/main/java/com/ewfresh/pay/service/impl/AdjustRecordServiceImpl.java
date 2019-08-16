package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AdjustRecordDao;
import com.ewfresh.pay.model.AdjustRecord;
import com.ewfresh.pay.service.AdjustRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdjustRecordServiceImpl implements AdjustRecordService {

    @Autowired
    private AdjustRecordDao adjustRecordDao;

    @Override
    public AdjustRecord getadjustRecordById(Integer adjustId) {
        AdjustRecord adjustRecord = adjustRecordDao.selectByPrimaryKey(adjustId);
        return adjustRecord;
    }
}
