/**
 *  * Copyright (c) 2019 Sunshine Insurance Group Inc
 *  * Created by gaoyongqiang on 2019/3/13.
 *  
 **/

package com.ewfresh.pay.service;

import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.*;
import com.github.pagehelper.PageInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @descrption TODO
 *  * @author gaoyongqiqng
 *  * @create 2019-03-13
 *  * @Email 1005267839@qq.com
 **/
public interface WhiteBillsService {
    /**
     * @Author gaoyongqiqng
     * @Description 根据白条账单批次号查询流水表明细（新增说明信息字段）
     * @Date: 2019/7/26
     */
     //PageInfo<BarDealFlowTwoVo> getBillDetailsBybillFlow(Integer pageSize, Integer pageNumber, String billFlow);

    /**
     * @Author gaoyongqiqng
     * @Description 新增账单
     * @Date: 2019/3/11
     */
    void addBill(Bill bill);

    /**
     * @Author gaoyongqiqng
     * @Description 更新账单
     * @Date: 2019/3/11
     */
    void updateBillSelective(Bill bill);

    /**
     * @Author gaoyongqiqng
     * @Description 添加利息记录表
     * @Date: 2019/3/11
     */
    int addBillIntersetRecord(BillIntersetRecord record);

    /**
     * @Author gaoyongqiqng
     * @Description 根据查询待还款与部分还款的数据
     * @Date: 2019/3/11
     */
    List<Bill> getByRecordingTime();

    /**
     * @Author gaoyongqiqng
     * @Description 查询用户还款账期期限
     * @Date: 2019/3/11
     */
    Map<String, Integer> getperiod(long uid);


    /**
     * @Author gaoyongqiqng
     * @Description 修改白条额度使用状态
     * @Date: 2019/3/11
     */
    int updateWhiteBar(WhiteBar record);


    /**
     * @Author gaoyongqiqng
     * @Description 更新流水交易表账单批次号
     * @Date: 2019/3/11
     */
    int updateBarDealFlow(BarDealFlow record);


    /**
     * @Author gaoyongqiqng
     * @Description 根据日期查询全部白条流水表
     * @Date: 2019/3/11
     */
    List<BarDealFlow> getBarDealFlowByPrimaryKey(Date date, Long uid);

    /**
     * @Author gaoyongqiqng
     * @Description 根据用户id 还款状态查询账单
     * @Date: 2019/3/11
     */
    PageInfo<BillVo> getWhiteBillByUid(Integer pageSize, Integer pageNumber, Map map);

    /**
     * @Author gaoyongqiqng
     * @Description 根据白条账单批次号查询流水表明细
     * @Date: 2019/3/11
     */
    PageInfo<BarDealFlowTwoVo> getBarDealFlow(Integer pageSize, Integer pageNumber, String billFlow);

    /**
     * @Author gaoyongqiqng
     * @Description 根据uid查询用户信息
     * @Date: 2019/3/11
     */
    String getUserInfo(Long uid);

    /**
     * @Author gaoyongqiqng
     * @Description 查询用户全部未还款账单
     * @Date: 2019/3/11
     */
    List<BillVo> getBillsByUid(Long uid);

    /**
     * @Author gaoyongqiqng
     * @Description 根据账单批次号查询用户id
     * @Date: 2019/3/22
     */
    Map<String, Long> getUidByBillFlow(String billFlow);

    /**
     * @Author gaoyongqiqng
     * @Description 还款记录
     * @Date: 2019/3/22
     */
    PageInfo<BillRepayFlowVo> getBillRepayByBillid(Integer pageSize, Integer pageNumber, Map map);
    /**
     * @Author Duanxiangming
     * @Description 获取用户的超时未还款的账单
     * @Date: 2019/3/22
     */
    List<AutoRepayBillVo> getOvertimeBills(Long userId);
    /**
     * @Author gaoyongqiqng
     * @Description 被动还款批量添加和更新表
     * @Date: 2019/3/22
     */
    void passivePaymentBatchRecord(List<Bill> upbillList,List<BillRepayFlow>
            addBillRepayFlowList,List<BarDealFlow> addBarDealFlowList,PayFlow payFlow,AccountFlowVo accountFlow);
}
