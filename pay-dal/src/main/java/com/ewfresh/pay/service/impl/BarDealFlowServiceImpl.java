package com.ewfresh.pay.service.impl;

import com.ewfresh.pay.dao.BarDealFlowDao;
import com.ewfresh.pay.dao.BillDao;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.service.BarDealFlowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/15.
 */
@Service
public class BarDealFlowServiceImpl implements BarDealFlowService {

    @Autowired
    private BarDealFlowDao barDealFlowDao;
    @Autowired
    private BillDao billDao;

    @Override
    public  List<BarDealFlowTwoVo> exportWhiteLimitbyUid(HashMap<String, Object> stringObjectHashMap) {
        List<BarDealFlowTwoVo> logisticsCompanyList = barDealFlowDao.getWhiteLimitbyUid(stringObjectHashMap);
        return logisticsCompanyList;
    }

    @Override
    public PageInfo<BarDealFlowTwoVo> getWhiteLimitbyUid(Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<BarDealFlowTwoVo> logisticsCompanyList = barDealFlowDao.getWhiteLimitbyUid(map);
        PageInfo<BarDealFlowTwoVo> companyPageInfo = new PageInfo<>(logisticsCompanyList);
        return companyPageInfo;
    }


    /**
     * Description: 根据uiserId获取白条流水、余额等信息
     *
     * @param userId
     * @author: ZhaoQun
     * @return: BarDealFlowVo
     * date: 2019/3/15 14:11
     */
    @Override
    public BarDealFlowVo getDealFlowByUid(Long userId) {
        return barDealFlowDao.getDealFlowByUid(userId);
    }

    /**
     * Description: 根据uiserId获取用户白条已使用额度
     *
     * @param uid
     * @author: ZhaoQun
     * @return: BarDealFlowVo
     * date: 2019/3/15 14:11
     */
    @Override
    public BigDecimal getUsedLimitByUid(Long uid) {
        return barDealFlowDao.getUsedLimitByUid(uid);
    }

    @Override
    public PageInfo<BarDealFlow> getBarDealByUid(Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<BarDealFlow> logisticsCompanyList = barDealFlowDao.getBarDealFlowByUid(map);
        PageInfo<BarDealFlow> companyPageInfo = new PageInfo<>(logisticsCompanyList);
        return companyPageInfo;
    }

    @Override
    public List<BarDealFlow> exportBarDealFlowList(HashMap<String, Object> stringObjectHashMap) {
        List<BarDealFlow> barDealFlows = barDealFlowDao.exportBarDealFlowList(stringObjectHashMap);
        return barDealFlows;
    }

    /**
     * @Author zhaoqun
     * @Description 根据orderId 查询账单批次号 bill_flow
     * @Date: 2019/3/30
     */
    @Override
    public String getBillFlowByPayFlowId(Integer payFlowId) {
        return barDealFlowDao.getBillFlowByPayFlowId(payFlowId);
    }

    /**
     * @Author zhaoqun
     * @Description 根据orderId 查询账单批次号 bill_flow ( 最早的)
     * @Date: 2019/3/30
     */
    @Override
    public String getBillFlowByOrderIdAsc(Long orderId) {
        return barDealFlowDao.getBillFlowByOrderIdAsc(orderId);
    }

    /**
     * @Author zhaoqun
     * @Description 根据orderId 查询账单批次号 bill_flow ( 最晚的)
     * @Date: 2019/3/30
     */
    @Override
    public String getBillFlowByOrderIdDesc(Long orderId) {
        return barDealFlowDao.getBillFlowByOrderIdDesc(orderId);
    }

    @Override
    public BillVo getBillByBillFlow(String billFlow) {
        return billDao.getBillByBillFlow(billFlow);
    }
    @Override
    public PageInfo<BarDealFlow> getBarDealFlow(Map<String, Object> map, Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<BarDealFlow> logisticsCompanyList = barDealFlowDao.getAllBarDealFlow(map);
        PageInfo<BarDealFlow> companyPageInfo = new PageInfo<>(logisticsCompanyList);
        return companyPageInfo;
    }

    @Override
    public List<BarDealFlowDownLoadVo> getBarDeal(Map<String, Object> map) {

        return barDealFlowDao.getBarDealFlowMes(map);
    }


}