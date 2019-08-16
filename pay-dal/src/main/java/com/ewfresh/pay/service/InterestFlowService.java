package com.ewfresh.pay.service;

import java.util.List;

import com.ewfresh.pay.model.InterestFlow;
import com.ewfresh.pay.model.vo.InterestFlowVo;
import com.ewfresh.pay.model.vo.RepayFlowVo;

import com.github.pagehelper.PageInfo;

/**
 * Interface 逾期费流水service
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
public interface InterestFlowService {

    /**
     * Method description
     *
     * @date    19/08/14
     * @author  huboyang
     * @param list
     */
    void addInterestFlow(List<InterestFlow> list);

    /**
     * Method 导出逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param uname
     * @param startTime
     * @param endTime
     * @return
     */
    List<InterestFlowVo> exportInterestFlow(String uname, String startTime, String endTime);

    /**
     * Method 查询逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param startTime
     * @param endTime
     * @return
     */
    List<RepayFlowVo> getInterestFlow(String startTime, String endTime);

    /**
     * Method 根据条件查询逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param uname
     * @param pageNumber
     * @param pageSize
     * @param startTime
     * @param endTime
     * @return
     */
    PageInfo<InterestFlow> getInterestFlowByCondition(String uname, Integer pageNumber, Integer pageSize,
                                                      String startTime, String endTime);
}

