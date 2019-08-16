package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.WithdrawtosVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WithdrawtoDao {
    int deleteByPrimaryKey(Long id);

    int insert(Withdrawto record);

    int insertSelective(Withdrawto record);

    Withdrawto selectByPrimaryKey(Long id);

    int updateByPrimaryKey(Withdrawto record);

    int addWithdrawto(Withdrawto withdrawto);
    /**
     * Description: 根据ID获取用户提现的方法
     * @author DuanXiangming
     * @param  id         提现记录ID
     * Date    2018/4/13
     */
    WithdrawtosVo getWithdrawtoByid(Long id);
    /**
     * Description:
     * @author DuanXiangming
     * @param uname         用户名
     * @param status        审核状态
     * @param startTime     开始时间
     * @param endTime       结束时间
     * Date    2018/4/14
     */
    List<WithdrawtosVo> getWithdrawtos(@Param("uname") String uname, @Param("nickName") String nickName, @Param("status")Short status, @Param("beforeStatus")Short beforeStatus, @Param("startTime") String startTime,@Param("endTime") String endTime);
    /**
     * Description: 根据UID获取用户提现的方法
     * @author DuanXiangming
     * @param  uid          用户ID
     * @param id
     */
    List<WithdrawtosVo> getWithdrawtoByUid(@Param("uid") Long uid, @Param("id") Long id);

    /**
     * Description: 修改提现记录审核状态的方法
     * @author DuanXiangming
     * @param  withdrawto     封装审核数据的方法
     * Date    2018/4/13
     */
    int updateApprStatus(Withdrawto withdrawto);
    /**
     * Description: 后台取消提现的方法
     * @author DuanXiangming
     * @param withdrawto
     * Date    2018/4/14
     */
    int updateCancelWithdrawByid(Withdrawto withdrawto);

    /**
     * Description: 修改提现信息
     * @author: ZhaoQun
     * @param withdrawto
     * @return:
     * date: 2018/8/10 14:48
     */
    void updateWithdrawto(Withdrawto withdrawto);

    /**
     * Description: 根据获取提现信息
     * @author: ZhaoQun
     * @param id
     * @return:
     * date: 2018/8/13 14:48
     */
    Withdrawto getWithdrawtoInfoById(Long id);
}