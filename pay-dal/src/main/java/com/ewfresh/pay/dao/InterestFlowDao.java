package com.ewfresh.pay.dao;


import java.util.List;


import com.ewfresh.pay.model.vo.RepayFlowVo;
import org.apache.ibatis.annotations.Param;
import com.ewfresh.pay.model.InterestFlow;
import com.ewfresh.pay.model.vo.InterestFlowVo;

/**
 * Interface 逾期费流水dao
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
public interface InterestFlowDao {

    /**
     * Method 添加逾期费流水
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
    List<InterestFlowVo> exportInterestFlow(@Param("uname") String uname, @Param("startTime") String startTime,
                                            @Param("endTime") String endTime);

    /**
     * Method 查询逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param startTime
     * @param endTime
     * @return
     */
    List<RepayFlowVo> getInterestFlow(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * Method 根据条件查询逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param uname
     * @param startTime
     * @param endTime
     * @return
     */
    List<InterestFlow> getInterestFlowByCondition(@Param("uname") String uname, @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);
}

