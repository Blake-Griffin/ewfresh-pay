package com.ewfresh.pay.service;

import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.WithdrawApprRecord;
import com.ewfresh.pay.model.Withdrawto;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.WithdrawtosVo;

import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/13
 */
public interface WithdrawtoService {

    /**
     * Description: 插入提现申请和冻结余额的方法
     * @author DuanXiangming
     * @param accountFlow   账户资金流水
     * @param withdrawto    提现申请
     * Date    2018/4/13
     */
    void addWithdrawtoAndFreezeBalance(AccountFlowVo accountFlow, Withdrawto withdrawto);

    /**
     * Description: 根据ID获取用户提现记录的方法
     * @author DuanXiangming
     * @param  id         提现申请ID
     * Date    2018/4/13
     */
    WithdrawtosVo getWithdrawByid(Long id);
    /**
     * Description:
     * @author DuanXiangming
     * @param uname         用户名
     * @param status        审核状态
     * @param startTime     开始时间
     * @param endTime       结束时间
     */
    List<WithdrawtosVo> getWithdraws(String uname , String nickName, Short status ,Short beforeStatus, String startTime, String endTime);
    /**
     * Description: 根据UID获取用户提现的方法
     * @author DuanXiangming
     * @param  uid          用户ID
     * @param id
     */
    List<WithdrawtosVo> getWithdrawByUid(Long uid, Long id);
    /**
     * Description: 修改提现记录审核状态的方法
     * @author DuanXiangming
     * @param  withdrawto     封装审核数据的方法
     * @param payFlow
     * @param withdrawApprRecord
     * @param accountFlow
     */
    void updateApprStatus(Withdrawto withdrawto, PayFlow payFlow, WithdrawApprRecord withdrawApprRecord, AccountFlow accountFlow);
    /**
     * Description: 后台取消提现的方法
     * @author DuanXiangming
     * @param withdrawto
     * @param accountFlow
     */
    void cancelWithdrawByid(Withdrawto withdrawto, AccountFlowVo accountFlow);

    /**
     * Description: 修改提现信息
     * @author: ZhaoQun
     * @param withdrawto
     * @return:
     * date: 2018/8/10 14:48
     */
    void updateWithdrawto(Withdrawto withdrawto);

    Withdrawto getWithdrawtoInfoById(Long id);

    /**
     * Description: 审核用户提现的方法（出纳审核不通过时，总裁确认不通过）
     * @author zhaoqun
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/11/11
     */
    void checkWithdrawNotAllow(Withdrawto withdrawto, AccountFlow accountFlow);
}
