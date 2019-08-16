package com.ewfresh.pay.service;

import com.ewfresh.pay.model.AdjustRecord;
import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import com.ewfresh.pay.model.vo.WhiteBarVoOne;
import com.github.pagehelper.PageInfo;


public interface WhiteBarService {
    /**
     * @Author LouZiFeng
     * @Description 查询白条额度信息
     * @Date: 2019/3/11
     */
    PageInfo<WhiteBarVo> getWhiteBarList(String uname, Short apprStatus, String start, String end, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 根据id查询白条额度信息
     * @Param id
     * @Date: 2019/3/11
     */
    WhiteBarVo getWhiteBarById(Integer recordId);


    /**
     * @Author LouZiFeng
     * @Description 修改审核状态
     * @Param id
     * @Param apprStatus
     * @Date: 2019/3/11
     */
    void updateApprStatus(Integer id, Short apprStatus, String reason);

    /**
     * @Author: zhaoqun
     * @Description: 根据用户获取白条
     * @Param: userId
     * @Date: 2019/3/20
     */
    WhiteBarVo getWhiteBarVoByUid(Long userId);

    /**
     * @param uid
     * @Author: LouZiFeng
     * @Description: 根据uid查询白条额度信息
     * @Param: uid
     * @Date: 2019/3/21
     */
    WhiteBar getWhiteBarByUid(Long uid);

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
    PageInfo<WhiteBarVoOne> getWhiteBarName(Integer id, String uname, Short useStatus, Integer period, Integer pageNumber, Integer pageSize);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询白条额度状态和之前额度信息
     * @Param uid
     * @Date: 2019/3/22
     */
    WhiteBarVo getWhiteBarByAdjustLimit(Integer barId);

    /**
     * @Author: LouZiFeng
     * @Description: 修改使用状态
     * @Param: id
     * @Param: useStatus
     * @Date: 2019/3/11
     */
    void updateUseStatus(Integer id, Short useStatus);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询上个月是否申请过
     * @Param: uid
     * @Date: 2019/3/25
     */
    WhiteBarVoOne getWhiteBarMonthTotal(Integer barId);

    /**
     * @Author LouZiFeng
     * @Description 根据uid修改审核信息
     * @Param: uid
     * @Date: 2019/3/25
     */
    void updateWhiteBarById(WhiteBar whiteBar, AdjustRecord adjustRecord);

    /**
     * @Author: LouZiFeng
     * @Description: 根据id修改审核信息
     * @Param: barId
     * @Date: 2019/3/27 11:04
     */
    WhiteBar getById(Integer barId);

    /**
     * @Author: LouZiFeng
     * @Description: 添加纪录表
     * @Param: adjustRecord
     * @Date: 2019/3/27 11:04
     */
    void addWhiteBarAndRecord(WhiteBar whiteBar, AdjustRecord adjustRecord);

    /**
     * @Author: LouZiFeng
     * @Description: 更新数据
     * @Param: adjustRecord
     * @Date: 2019/3/27 11:04
     */
    void upApprStatus(WhiteBar whiteBarByUid, AdjustRecord adjustRecord);

    /**
      * @Author: LouZiFeng
      * @Description:  前台添加申请额度信息
      * @Param: adjustRecord
      * @Date: 2019/4/1
      */
    void adjustRecord(AdjustRecord adjustRecord, WhiteBar whiteBar);
}
