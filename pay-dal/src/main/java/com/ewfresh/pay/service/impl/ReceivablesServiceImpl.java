package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.ReceivablesDao;
import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.ReceivablesVo;
import com.ewfresh.pay.service.ReceivablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/6/6 0006
 */
@Service
public class ReceivablesServiceImpl implements ReceivablesService {

    @Autowired
    private ReceivablesDao receivablesDao;


    @Override
    public Receivables getReceivablesByUid(String uid) {

        return receivablesDao.getReceivablesByUid(uid);
    }


    @Override
    public List<Receivables> getReceivablesListByUid(Long uid, Integer pageSize, Integer pageNumber, String explain, String amount, String startTime, String endTime) {

        return receivablesDao.getReceivablesListByUid(uid, explain,amount,startTime,endTime);
    }

    @Override
    public List<Long> getReceivablesList(String uname, String receiveTime) {

        return receivablesDao.getReceivablesList(uname,receiveTime);
    }

    @Override
    public List<ReceivablesVo> getReceivablesListByParm(List<Long> ids) {
        return receivablesDao.getReceivablesListByParm(ids);
    }
}
