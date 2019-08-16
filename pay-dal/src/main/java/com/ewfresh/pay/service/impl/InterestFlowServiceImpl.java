package com.ewfresh.pay.service.impl;

import java.util.List;

import com.ewfresh.pay.model.vo.RepayFlowVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ewfresh.pay.dao.InterestFlowDao;
import com.ewfresh.pay.model.InterestFlow;
import com.ewfresh.pay.model.vo.InterestFlowVo;
import com.ewfresh.pay.service.InterestFlowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * Class 查询逾期费流水service 实现层
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
@Service
public class InterestFlowServiceImpl implements InterestFlowService {
    @Autowired
    private InterestFlowDao interestFlowDao;

    @Transactional
    @Override
    public void addInterestFlow(List<InterestFlow> list) {
        interestFlowDao.addInterestFlow(list);
    }

    @Override
    public List<InterestFlowVo> exportInterestFlow(String uname, String startTime, String endTime) {
        return interestFlowDao.exportInterestFlow(uname, startTime, endTime);
    }

    @Override
    public List<RepayFlowVo> getInterestFlow(String startTime, String endTime) {
        return interestFlowDao.getInterestFlow(startTime, endTime);
    }

    @Override
    public PageInfo<InterestFlow> getInterestFlowByCondition(String uname, Integer pageNumber, Integer pageSize,
                                                             String startTime, String endTime) {
        PageHelper.startPage(pageNumber, pageSize);

        List<InterestFlow> list = interestFlowDao.getInterestFlowByCondition(uname, startTime, endTime);
        PageInfo<InterestFlow> pageInfo = new PageInfo<>(list);

        return pageInfo;
    }
}

