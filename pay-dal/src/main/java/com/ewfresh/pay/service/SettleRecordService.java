package com.ewfresh.pay.service;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/22 0022
 */
public interface SettleRecordService {

    /**
     * Description: 通过店铺id查询账单记录
     * @author wangyaohui
     * @param  shopId     要查询的用户id
     */
    PageInfo<SettleRecord> getSettleRecordByShopId(Integer shopId, Integer pageSize, Integer pageNumber);
    /**
     * Description: 添加一条settle记录的方法
     * @author DXM
     */
    void addSettleRecord(SettleRecord settleRecord, List<PayFlow> payflows, String batch);
    /**
     * Description: 获取分账审核列表的方法
     * @author DXM
     * @param  shopName    过滤条件之店铺名称
     * @param shopId
     * @param settleStatus
     */
    PageInfo<SettleRecord> getSettleRecords(String shopName, Integer shopId, Short settleStatus, Integer pageSize, Integer pageNumber);
    /**
     * Description: 审核分账内容的接口
     * @author DXM
     * @param  settleRecord   分装审核的内容
     * Date    2018/4/13
     */
    void apprByBatchNo(SettleRecord settleRecord);
    /**
     * Description: 根据批次号查询对应结算记录的方法
     * @author DXM
     * @param  id   分装审核的内容
     */
    SettleRecord getSettleRecordById(Integer id);
    /**
     * Description: 根据ID修改对应结算记录的方法
     * @author DXM
     * @param  settleRecord   分装的修改内容
     */
    void updateBatchNoById(SettleRecord settleRecord);
    /**
     * Description: 更新结算状态,并添加流水的方法
     * @author DXM
     * @param  settleRecord   分装的修改内容
     */
    int updateSettle(SettleRecord settleRecord, PayFlow payFlow, AccountFlowVo accountFlow);
}
