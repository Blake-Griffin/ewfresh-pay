package com.ewfresh.pay.service;

import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.vo.*;
import com.github.pagehelper.PageInfo;

import java.util.HashMap;
import java.util.List;

import java.math.BigDecimal;
import java.util.Map;

/**
 * description:
 *
 * @author: ZhaoQun
 * date: 2019/3/15.
 */
public interface BarDealFlowService {

    /**
     * @Author gyq
     * @Description 导出根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    List<BarDealFlowTwoVo> exportWhiteLimitbyUid(HashMap<String, Object> stringObjectHashMap);

    /**
     * @Author gyq
     * @Description 根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    PageInfo<BarDealFlowTwoVo> getWhiteLimitbyUid(Map<String, Object> map, Integer pageNumber, Integer pageSize);


    /**
     * Description: 根据uiserId获取白条流水、余额等信息
     *
     * @param userId
     * @author: ZhaoQun
     * @return: BarDealFlowVo
     * date: 2019/3/15 14:11
     */
    BarDealFlowVo getDealFlowByUid(Long userId);

    /**
     * Description: 根据uiserId获取用户白条已使用额度
     * @param uid
     * @author: ZhaoQun
     * @return: BarDealFlowVo
     * date: 2019/3/15 14:11
     */
    BigDecimal getUsedLimitByUid(Long uid);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    PageInfo<BarDealFlow> getBarDealByUid(Map<String, Object> map, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 导出白条交易流水信息
     * @Date: 2019/3/20
     */
    List<BarDealFlow> exportBarDealFlowList(HashMap<String, Object> stringObjectHashMap);

    /**
     * @Author zhaoqun
     * @Description  payFlowId 查询账单批次号 bill_flow
     * @Date: 2019/3/30
     */
    String getBillFlowByPayFlowId(Integer payFlowId);
    /**
     * @Author zhaoqun
     * @Description 根据orderId 查询账单批次号 bill_flow ( 最早的)
     * @Date: 2019/3/30
     */
    String getBillFlowByOrderIdAsc(Long orderId);

    /**
     * @Author zhaoqun
     * @Description 根据orderId 查询账单批次号 bill_flow ( 最晚的)
     * @Date: 2019/3/30
     */
    String getBillFlowByOrderIdDesc(Long orderId);

    /**
     * @Author zhaoqun
     * @Description 根据账单批次号 bill_flow 查询账单信息
     * @Date: 2019/5/15
     */
    BillVo getBillByBillFlow(String billFlow);

    /**
     * description: 查询白条额度使用情况
     * @author  huboyang
     * @param
     */
    PageInfo<BarDealFlow> getBarDealFlow(Map<String, Object> map, Integer pageNumber, Integer pageSize);
    /**
     * description: 下载白条额度流水
     * @author  huboyang
     * @param
     */
    List<BarDealFlowDownLoadVo> getBarDeal(Map<String,Object> map);
}
