package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.util.ResponseData;

import java.math.BigDecimal;


public interface WhiteBarManager {
    /**
     * @Author LouZiFeng
     * @Description 查询白条额度信息
     * @Date: 2019/3/11
     */
    ResponseData getWhiteBarList(ResponseData responseData, String uname, Short apprStatus, String start, String end, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 根据id查询白条额度信息
     * @Param: id
     * @Date: 2019/3/11
     */
    ResponseData getWhiteBarById(ResponseData responseData, Integer recordId);

    /**
     * @Author: LouZiFeng
     * @Description: 修改审核状态
     * @Param: id
     * @Param: apprStatus
     * @Date: 2019/3/11
     */
    ResponseData updateApprStatus(ResponseData responseData, Integer id, Short apprStatus, String reason);

    /**
     * @Author: LouZiFeng
     * @Description: 添加额度信息
     * @Param: whiteBar
     * @Date: 2019/3/11
     */
    ResponseData addWhiteBar(ResponseData responseData, WhiteBar whiteBar);

    /**
     * @Author: LouZiFeng
     * @Description: 根据uid查询白条额度信息
     * @Param: uid
     * @Date: 2019/3/21
     */
    ResponseData getWhiteBarByUid(ResponseData responseData, Long uid);

    /**
     * @Author LouZiFeng
     * @Description 查询客户白条信息
     * @Param id
     * @Param uname
     * @Param apprStatus
     * @Param period
     * @Param pageNumber
     * @Param pageSize
     * @Date: 2019/3/22
     */
    ResponseData getWhiteBarName(ResponseData responseData, Integer id, String uname, Short useStatus, Integer period, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条额度状态和之前额度信息
     * @Param uid
     * @Date: 2019/3/22
     */
    ResponseData getWhiteBarByAdjustLimit(ResponseData responseData, Integer barId);

    /**
     * @Author: LouZiFeng
     * @Description: 修改使用状态
     * @Param: id
     * @Param: useStatus
     * @Date: 2019/3/11
     */
    ResponseData updateUseStatus(ResponseData responseData, Integer id, Short useStatus);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询修改审核信息
     * @Param: uid
     * @Date: 2019/3/25
     */
    ResponseData updateWhiteBarByUid(ResponseData responseData, Integer recordId, WhiteBar whiteBar);

    /**
     * @Author LouZiFeng
     * @Description 根据uid进行查询上个月所得额度
     * @Param: uid
     * @Date: 2019/3/25
     */
    ResponseData getWhiteBarByMonth(ResponseData responseData, WhiteBar whiteBar);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询上个月是否申请过
     * @Param: uid
     * @Date: 2019/3/25
     */
    ResponseData getWhiteBarMonthTotal(ResponseData responseData, Long uid);

 /**
   * @Author: LouZiFeng
   * @Description: 确定申请
   * @Param:
   * @Date: 2019/4/1 11:53
   */
    ResponseData addAdjustRecord(ResponseData responseData, Integer uid, BigDecimal adjustLimit);
}
