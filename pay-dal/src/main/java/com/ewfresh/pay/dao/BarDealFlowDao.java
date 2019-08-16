package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.vo.BarDealFlowDownLoadVo;
import com.ewfresh.pay.model.vo.BarDealFlowTwoVo;
import com.ewfresh.pay.model.vo.BarDealFlowVo;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import org.apache.ibatis.annotations.Param;
import sun.security.util.BigInt;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface BarDealFlowDao {
    int deleteByPrimaryKey(Integer id);

    int insert(BarDealFlow record);

    //添加白条流水记录  zhaoqun
    int addBardealFlow(BarDealFlow barDealFlow);

    //批量插入流水记录  gyq
    void addBarDealFlowBatch(List<BarDealFlow> list);

    BarDealFlow selectByPrimaryKey(Integer id);

    //更新账单流水
    int updateBarDealFlow(BarDealFlow record);

    int updateByPrimaryKey(BarDealFlow record);

    //根据日期查询全部白条流水表
    List<BarDealFlow> getBarDealFlowByPrimaryKey(@Param("date") Date date, @Param("uid") Long uid);


    //根据客户uid 查询白条账单批次号
    List<BarDealFlow> getBillFlowByUid(Long uid);

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
     *
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
     * @Date: 2019/3/11
     */
    List<BarDealFlow> getBarDealFlowByUid(Map<String, Object> map);

    /**
     * @Author LouZiFeng
     * @Param stringObjectHashMap
     * @Description 导出白条交易流水信息
     * @Date: 2019/3/20
     */
    List<BarDealFlow> exportBarDealFlowList(HashMap<String, Object> stringObjectHashMap);

    //根据白条账单批次号查询流水表明细
    List<BarDealFlowTwoVo> getBarDealFlow(String billFlow);

    //根据白条账单批次号查询流水表明细(添加新字段说明信息)gyq
    //List<BarDealFlowTwoVo> getBarDealFlowByBillFlow(String billFlow);

    //查询客户白条信息
    WhiteBarVo getWhiteBarByUid(Long uid);

    /**
     * Description: 添加多条白条流水的方法
     *
     * @param barDealFlows
     * @author zhaoqun
     */
    void addBardealFlows(List<BarDealFlow> barDealFlows);


    /**
     * description: 根据billFlow查询用户白条流水最近一条
     *
     * @param
     * @author huboyang
     */
    BarDealFlow getOneBarDealFlow(Long uid);


    /**
     * @Author zhaoqun
     * @Description 根据payFlowId 查询账单批次号 bill_flow
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
     * description: 查询所有白条额度使用状况
     * @author huboyang
     * @param
     */
    List<BarDealFlow>getAllBarDealFlow(Map<String, Object> map);

    /**
     * @Author GaoYongQiang
     * @Description 根据UId查询所有白条使用额度的详情
     * @Param uid
     * @Date: 2019/4/15
     */
    List<BarDealFlowTwoVo> getWhiteLimitbyUid(Map<String, Object> map);

    List<BarDealFlowDownLoadVo> getBarDealFlowMes(Map<String,Object> map);
}