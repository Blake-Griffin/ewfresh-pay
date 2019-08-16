package com.ewfresh.pay.dao;


import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import com.ewfresh.pay.model.vo.WhiteBarVoOne;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WhiteBarDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WhiteBar record);

    /**
     * @Author: LouZiFeng
     * @Description: 添加额度信息
     * @Param: whiteBar
     * @Date: 2019/3/11
     */
    int insertSelective(WhiteBar record);

    /**
     * @Author LouZiFeng
     * @Description 根据id查询白条额度信息
     * @Param id
     * @Date: 2019/3/11
     */
    WhiteBarVo selectByPrimaryKey(Integer id);

    /**
     * @Author LouZiFeng
     * @Description 修改白条额度信息
     * @Param record
     * @Date: 2019/3/11
     */
    int updateByPrimaryKeySelective(WhiteBar record);

    /**
     *  * @author gaoyongqiang
     *  * @Description 修改白条额度使用状态
     *  * @Date   2019/3/20 10:24
     *  *  @params
     *  * @return 
     **/
    int updateWhiteBar(WhiteBar record);

    /**
     * @Author LouZiFeng
     * @Description 查询白条额度信息
     * @Date: 2019/3/11
     */
    List<WhiteBarVo> getWhiteBarList(@Param("uname") String uname, @Param("apprStatus") Short apprStatus, @Param("start") String start, @Param("end") String end);

    /**
     * @Author LouZiFeng
     * @Description 修改审核状态
     * @Param id
     * @Param apprStatus
     * @Date: 2019/3/11
     */
    void updateApprStatus(@Param("id") Integer id, @Param("apprStatus") Short apprStatus, @Param("reason") String reason);

    /**
     * @Author: zhaoqun
     * @Description: 根据用户获取白条
     * @Param: userId
     * @Date: 2019/3/20
     */
    WhiteBarVo getWhiteBarVoByUid(@Param("uid") Long uid);

    /**
     * @param uid
     * @Author: LouZiFeng
     * @Description: 根据uid查询白条额度信息
     * @Param: uid
     * @Date: 2019/3/21
     */
    WhiteBar getWhiteBarByUid(Long uid);

    /**
     * description: 根据用户id查看白条额度状态
     *
     * @param
     * @author huboyang
     */
    WhiteBar getWhiteBar(Long Uid);

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
    List<WhiteBarVoOne> getWhiteBarName(@Param("id") Integer id, @Param("uname") String uname, @Param("useStatus") Short useStatus, @Param("period") Integer period);

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
    void updateUseStatus(@Param("id") Integer id, @Param("useStatus") Short useStatus);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询审核信息记录
     * @Param: uid
     * @Date: 2019/3/25
     */
    WhiteBar getWhiteBarToUid(Long uid);

    /**
     * @Author LouZiFeng
     * @Description 根据uid查询上个月是否申请过额度
     * @Param: uid
     * @Date: 2019/3/25
     */
    WhiteBarVoOne getWhiteBarMonthTotal(Integer barId);

    /**
     * @Author LouZiFeng
     * @Description 根据id查询信息
     * @Param: id
     * @Date: 2019/3/25
     */
    WhiteBar getById(Integer barId);

    /**
     * @Author: LouZiFeng
     * @Description: 根据uid查询修改审核信息
     * @Param: uid
     * @Param: adjustLimit
     * @Param: adjustAmount
     * @Date: 2019/3/26 15:58
     */
    void updateWhiteBarByUid(@Param("id") Integer id, @Param("period")Integer period, @Param("adjustLimit") BigDecimal adjustLimit,@Param("apprStatus") Short apprStatus, @Param("adjustAmount") BigDecimal adjustAmount);

    void updateWhiteBarNotThrough(@Param("id")Integer id, @Param("apprStatus")Short apprStatus, @Param("reason")String reason);

    /**
    * @Author: LouZiFeng
    * @Description:  根据id查询信息
    * @Param: id
    * @Date: 2019/3/26 16:47
    */
    List<WhiteBar> getWhiteBarById(Integer id);
}