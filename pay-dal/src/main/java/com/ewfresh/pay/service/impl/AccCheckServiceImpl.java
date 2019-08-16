package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AccCheckDao;
import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.service.AccCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * @author DuanXiangming
 * Date 2019/6/13 0013
 */
@Service
public class AccCheckServiceImpl implements AccCheckService {



    @Autowired
    private AccCheckDao accCheckDao;


    @Override
    public List<AccCheck> getAccCheckByParam(Map<String, Object> paramMap) {


        return accCheckDao.getAccCheckByParam(paramMap);
    }


    @Override
    public void addAccCheck(AccCheck accCheck) {

    }

    @Override
    public List<PayFlow> getPayFlowsByItem(Date billDate, String interfaceCode) {
        return null;
    }
}
