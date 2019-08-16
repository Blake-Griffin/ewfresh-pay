/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/3/20.
 **/
 
package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;

/**
  * @descrption TODO
 * @author gaoyongqiqng
 * @create 2019-03-20
 * @Email 1005267839@qq.com
  **/
public interface WhiteBillmanager {
    //得到用户账单（已还款或未还款）
    void getWhiteBillByUid(ResponseData responseData,Integer pageSize,Integer pageNumber, Long userId,String billStatus,String billTime,String startRepaidTime,String endRepaidTime,String uname);
    //得到账单明细
    void getBillDetailsById(ResponseData responseData,Integer pageSize,Integer pageNumber, String billFlow);
    //根据用户id获取最近还款日
    void getRecentPaymentDateById(ResponseData responseData,Long userId);
    //还款记录
    void getBillRepayByBillid(ResponseData responseData,Integer pageSize,Integer pageNumber, String userId,String startTime,String endTime);
    //得到账单明细
    //void getBillDetailsBybillFlow(ResponseData responseData,Integer pageSize,Integer pageNumber, String billFlow);
}
