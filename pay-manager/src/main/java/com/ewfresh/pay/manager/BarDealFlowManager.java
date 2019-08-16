package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;
import org.apache.http.HttpResponse;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LouZiFeng
 * @Description: 白条交易流水接口
 * @Date: 2019/3/20
 */
public interface BarDealFlowManager {

    /**
     * @Author gyq
     * @Description 导出根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    Workbook exportWhiteLimitbyUid(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap);

    /**
     * @Author gyq
     * @Description 根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    ResponseData getWhiteLimitbyUid(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    ResponseData getBarDealFlowByUid(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 导出白条交易流水信息
     * @Param uid
     * @Date: 2019/3/20
     */
    Workbook exportBarDealFlowList(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap);
    /**
     * description: huboyang
     * @author  查询用户白条交易情况
     * @param
     */
    void getBarDealFlow(ResponseData responseData, Map<String, Object> map, Integer pageNumber, Integer pageSize);
    /**
     * description: 下载白条流水信息
     * @author  huboyang
     * @param
     */
    void exportBarDealFlow(ResponseData responseData, String title, HashMap<String,Object> map , HttpServletResponse response);
}
