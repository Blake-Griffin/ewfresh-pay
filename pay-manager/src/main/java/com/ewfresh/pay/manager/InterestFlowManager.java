package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Interface 逾期费流水的Manager
 *
 *
 * @date    19/08/14
 * @author  huboyang
 */
public interface InterestFlowManager {

    /**
     * Method 导出逾期费流水
     *
     * @date    19/08/14
     * @author  huboyang
     * @param responseData
     * @param uname
     * @param startTime
     * @param endTime
     */
    Workbook exportInterestFlow(ResponseData responseData, String uname, String startTime, String endTime, String titel);

    /**
     * Method 查询逾期费流水列表
     *
     * @date    19/08/14
     * @author  huboyang
     * @param uname
     * @param responseData
     * @param pageNumber
     * @param pageSize
     * @param startTime
     * @param endTime
     */
    void getInterestFlow(String uname, ResponseData responseData, Integer pageNumber, Integer pageSize,
                         String startTime, String endTime);
}


