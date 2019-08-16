package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.util.ResponseData;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/13
 */
public interface WithdrawtoManager {

    /**
     * Description: 用户申请提现的方法
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/4/13
     */
    void withdrawByUid(ResponseData responseData, Withdrawto withdrawto) throws Exception;
    /**
     * Description: 根据UID获取用户提现的方法
     * @author DuanXiangming
     * @param  uid          用户ID
     * Date    2018/4/13
     */
    void getWithdrawByUid(ResponseData responseData, Long uid , Long id ,Integer pageNum ,Integer pageSize);
    /**
     * Description: 审核用户提现的方法
     * @author DuanXiangming
     * @param  withdrawto     封装提现内容的对象
     * @param payFlow
     * @param withdrawApprRecord
     */
    void checkWithdraw(ResponseData responseData, Withdrawto withdrawto, PayFlow payFlow, WithdrawApprRecord withdrawApprRecord) throws Exception;
    /**
     * Description:       根据条件查询提现记录列表
     * @author DuanXiangming
     * @param uname       用户名
     * @param status      审核状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * Date    2018/4/14
     */
    void getWithdrawtos(ResponseData responseData, String uname, String nickName, Short status, Short beforeStatus, String startTime, String endTime, Integer pageNum, Integer pageSize);
    /**
     * Description:  获取提现记录详情的方法
     * @author DuanXiangming
     * @param id
     * Date    2018/4/14
     */
    void getWithdrawByid(ResponseData responseData, Long id);
    /**
     * Description: 后台取消提现的方法
     * @author DuanXiangming
     * @param withdrawto
     * Date    2018/4/14
     */
    void cancelWithdrawByid(ResponseData responseData, Withdrawto withdrawto, Short isSelf) throws Exception;

    /**
     * Description: 审核用户提现的方法（出纳审核不通过时，总裁确认不通过）
     * @author zhaoqun
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/11/11
     */
    void checkWithdrawNotAllow(ResponseData responseData, Withdrawto withdrawto) throws Exception;

    Withdrawto getWithdrawtoInfoById(Long id);
}
