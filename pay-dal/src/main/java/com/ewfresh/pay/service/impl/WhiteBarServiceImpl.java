package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.AdjustRecordDao;
import com.ewfresh.pay.dao.WhiteBarDao;
import com.ewfresh.pay.model.AdjustRecord;
import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import com.ewfresh.pay.model.vo.WhiteBarVoOne;
import com.ewfresh.pay.service.WhiteBarService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WhiteBarServiceImpl implements WhiteBarService {

    @Autowired
    private WhiteBarDao whiteBarDao;
    @Autowired
    private AdjustRecordDao adjustRecordDao;


    @Override
    public PageInfo<WhiteBarVo> getWhiteBarList(String uname, Short apprStatus, String start, String end, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<WhiteBarVo> logisticsCompanyList = whiteBarDao.getWhiteBarList(uname, apprStatus, start, end);
        PageInfo<WhiteBarVo> companyPageInfo = new PageInfo<>(logisticsCompanyList);
        return companyPageInfo;
    }

    @Override
    public WhiteBarVo getWhiteBarById(Integer recordId) {
        WhiteBarVo whiteBar = whiteBarDao.selectByPrimaryKey(recordId);
        return whiteBar;
    }

    @Override
    @Transactional
    public void updateApprStatus(Integer id, Short apprStatus, String reason) {
        whiteBarDao.updateApprStatus(id, apprStatus, reason);
    }

    /**
     * @Author: zhaoqun
     * @Description: 根据用户获取白条
     * @Param: userId
     * @Date: 2019/3/20
     */
    @Override
    public WhiteBarVo getWhiteBarVoByUid(Long userId) {
        return whiteBarDao.getWhiteBarVoByUid(userId);
    }

    @Override
    public WhiteBar getWhiteBarByUid(Long uid) {
        WhiteBar whiteBarByUid = whiteBarDao.getWhiteBarByUid(uid);
        return whiteBarByUid;
    }


    @Override
    public PageInfo<WhiteBarVoOne> getWhiteBarName(Integer id, String uname, Short useStatus, Integer period, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<WhiteBarVoOne> whiteBarVoOnes = whiteBarDao.getWhiteBarName(id, uname, useStatus, period);
        PageInfo<WhiteBarVoOne> whiteBarVoOnePageInfo = new PageInfo<>(whiteBarVoOnes);
        return whiteBarVoOnePageInfo;
    }

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条额度状态和之前额度信息
     * @Param barId
     * @Date: 2019/3/22
     */
    @Override
    public WhiteBarVo getWhiteBarByAdjustLimit(Integer barId) {
        WhiteBarVo whiteBarVoOne = whiteBarDao.getWhiteBarByAdjustLimit(barId);
        return whiteBarVoOne;
    }

    /**
     * @param id
     * @param useStatus
     * @Author: LouZiFeng
     * @Description: 修改使用状态
     * @Param: id
     * @Param: useStatus
     * @Date: 2019/3/11
     */
    @Override
    @Transactional
    public void updateUseStatus(Integer id, Short useStatus) {
        whiteBarDao.updateUseStatus(id, useStatus);
    }

    @Override
    public WhiteBarVoOne getWhiteBarMonthTotal(Integer barId) {
        WhiteBarVoOne whiteBarVoList = whiteBarDao.getWhiteBarMonthTotal(barId);
        return whiteBarVoList;
    }

    @Override
    @Transactional
    public void updateWhiteBarById(WhiteBar whiteBar, AdjustRecord adjustRecord) {
        whiteBarDao.updateByPrimaryKeySelective(whiteBar);
        adjustRecordDao.updateByPrimaryKeySelective(adjustRecord);
    }

    @Override
    public WhiteBar getById(Integer barId) {
        WhiteBar whiteBarServiceById = whiteBarDao.getById(barId);
        return whiteBarServiceById;
    }

    @Override
    @Transactional
    public void addWhiteBarAndRecord(WhiteBar whiteBar, AdjustRecord adjustRecord) {
        whiteBarDao.insertSelective(whiteBar);
        Integer barId = whiteBar.getId();
        adjustRecord.setBarId(barId);
        adjustRecordDao.insertSelective(adjustRecord);
    }

    @Override
    @Transactional
    public void upApprStatus(WhiteBar whiteBarByUid, AdjustRecord adjustRecord) {
        whiteBarDao.updateByPrimaryKeySelective(whiteBarByUid);
        adjustRecordDao.insertSelective(adjustRecord);
    }

    @Override
    @Transactional
    public void adjustRecord(AdjustRecord adjustRecord, WhiteBar whiteBar) {
        whiteBarDao.updateByPrimaryKeySelective(whiteBar);
        adjustRecordDao.insertSelective(adjustRecord);
    }
}
