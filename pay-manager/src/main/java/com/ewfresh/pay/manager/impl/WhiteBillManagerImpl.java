/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/20.
 *  
 **/

package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.WhiteBillmanager;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.vo.BarDealFlowTwoVo;
import com.ewfresh.pay.model.vo.BillRepayFlowVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.WhiteBillsService;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @descrption TODO
 * @author gaoyongqiqng
 * @create 2019-03-20
 * @Email 1005267839@qq.com
 **/
@Component
public class WhiteBillManagerImpl implements WhiteBillmanager {
    private final static Long REPAYMENT_ONE = 1L;
    private final static Long REPAYMENT_TWO = 2L;
    private final static Long REPAYMENT_THREE = 3L;
    private final static Long REPAYMENT_FOUR = 4L;
    private final static Long REPAYMENT_FIVE = 5L;
    @Autowired
    private WhiteBillsService whiteBillsService;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;

    @Override
    public void getWhiteBillByUid(ResponseData responseData, Integer pageSize, Integer pageNumber, Long userId, String billStatus, String billTime, String startRepaidTime, String endRepaidTime,String uname) {
        Map map = new HashMap<>();
        List list = new ArrayList();
        if (!StringUtils.isEmpty(billStatus)) {
            if (billStatus.equals("1")) {//未还款账单(待还款与部分还款)
                list.add(REPAYMENT_ONE);
                list.add(REPAYMENT_THREE);
                map.put("billStatus", list);
            }
            if (billStatus.equals("2")) {//正常还款账单+逾期还款账单
                list.add(REPAYMENT_TWO);
                list.add(REPAYMENT_FIVE);
                map.put("billStatus", list);
            }
            if (billStatus.equals("3")) {//待还款
                list.add(REPAYMENT_ONE);
                map.put("billStatus", list);
            }
            if (billStatus.equals("4")) {//部分还款
                list.add(REPAYMENT_THREE);
                map.put("billStatus", list);
            }
            if (billStatus.equals("5")) {//已完结
                list.add(REPAYMENT_FOUR);
                map.put("billStatus", list);
            }
            if (billStatus.equals("6")) {//正常还款账单
                list.add(REPAYMENT_TWO);
                map.put("billStatus", list);
            }
            if (billStatus.equals("7")) {//逾期还款账单
                list.add(REPAYMENT_FIVE);
                map.put("billStatus", list);
            }
        }
        if (!StringUtils.isEmpty(startRepaidTime)) {
            map.put("startRepaidTime", startRepaidTime);
        }
        if (!StringUtils.isEmpty(endRepaidTime)) {
            map.put("endRepaidTime", endRepaidTime);
        }
        if (!StringUtils.isEmpty(userId)) {
            map.put("uid", userId);
        }
        if (!StringUtils.isEmpty(billTime)) {
            map.put("billTime", billTime);
        }
        if (!StringUtils.isEmpty(uname)){
            map.put("uname", uname);
        }
        PageInfo<BillVo> billList = whiteBillsService.getWhiteBillByUid(pageSize, pageNumber, map);
        List<BillVo> lists = billList.getList();
        if(!lists.isEmpty()){
            for (BillVo billVo:lists){
                String userInfo = whiteBillsService.getUserInfo(billVo.getUserId());
                HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                String introducer = hashMap.get("introducer").toString();
                billVo.setIntroducer(introducer);
            }
        }
        responseData.setEntity(lists);
        responseData.setTotal(billList.getTotal() + "");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(billList.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

    }

    @Override
    public void getBillDetailsById(ResponseData responseData, Integer pageSize, Integer pageNumber, String billFlow) {
        PageInfo<BarDealFlowTwoVo> barDealFlowList = whiteBillsService.getBarDealFlow(pageSize, pageNumber, billFlow);
        List<BarDealFlowTwoVo> list = barDealFlowList.getList();
        responseData.setEntity(list);
        responseData.setTotal(barDealFlowList.getTotal() + "");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(barDealFlowList.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public void getBillRepayByBillid(ResponseData responseData, Integer pageSize, Integer pageNumber, String userId, String startTime, String endTime) {
        Map map = new HashMap();
        map.put("billId", userId);
        map.put("repayStarttime", startTime);
        map.put("repayEndtime", endTime);
        PageInfo<BillRepayFlowVo> billRepayFlowList = whiteBillsService.getBillRepayByBillid(pageSize, pageNumber, map);
        List<BillRepayFlowVo> list = billRepayFlowList.getList();
        responseData.setEntity(list);
        responseData.setTotal(billRepayFlowList.getTotal() + "");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(billRepayFlowList.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

//    @Override
//    public void getBillDetailsBybillFlow(ResponseData responseData, Integer pageSize, Integer pageNumber, String billFlow) {
//        PageInfo<BarDealFlowTwoVo> barDealFlowList = whiteBillsService.getBillDetailsBybillFlow(pageSize, pageNumber, billFlow);
//        List<BarDealFlowTwoVo> list = barDealFlowList.getList();
//        responseData.setEntity(list);
//        responseData.setTotal(barDealFlowList.getTotal() + "");
//        responseData.setCurPage(pageNumber);
//        responseData.setPageCount(barDealFlowList.getPages());
//        responseData.setCode(ResponseStatus.OK.getValue());
//        responseData.setMsg(ResponseStatus.OK.name());
//    }


    @Override
    public void getRecentPaymentDateById(ResponseData responseData, Long userId) {
        List<BillVo> billList = whiteBillsService.getBillsByUid(userId);
        if (!billList.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            responseData.setEntity(sdf.format(billList.get(0).getLastRepaidTime()));
        } else {
            responseData.setEntity(null);
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }


}
