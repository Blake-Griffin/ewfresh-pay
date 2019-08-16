package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.SettleRecord;
import com.ewfresh.pay.util.ResponseData;

import java.math.BigDecimal;
import java.util.List; /**
 * Description:
 * @author DuanXiangming
 * Date 2018/10/22 0022
 */
public interface SettleRecordManager {

    /**
     * Description: 通过店铺id查询账单记录
     * @author wangyaohui
     * @param  shopId     要查询的用户id
     * @param pageSize
     * @param pageNumber
     */
    void getSettleRecordByShopId(ResponseData responseData, Integer shopId, Integer pageSize,Integer pageNumber, String batchNo, Short orderStatus, Short settleStatus, String successTime);
    /**
     * Description: 添加一条结算记录的方法
     * @author wangyaohui
     * @param  shopId     要查询的用户id
     * @param settleFlag
     * @param batch
     */
    void addSettleRecord(ResponseData responseData, Integer shopId, List<Integer> payflows, String settleFlag, String batch);
    /**
     * Description: 获取分账审核列表的方法
     * @author DXM
     * @param  shopName    过滤条件之店铺名称
     * @param shopId
     * @param settleStatus
     * @param pageSize
     * @param pageNumber
     */
    void getSettleRecords(ResponseData responseData, String shopName, Integer shopId, Short settleStatus, Integer pageSize, Integer pageNumber);
    /**
     * Description: 根据批次号获取具体分账流水
     * @author DXM
     * @param  batchNo    分账批次号
     * Date    2018/4/13
     */
    void getSettleRecordsByBatchNo(ResponseData responseData, String batchNo, Integer pageSize, Integer pageNumber);
    /**
     * Description: 审核分账内容的接口
     * @author DXM
     * @param  settleRecord   分装审核的内容
     * Date    2018/4/13
     */
    void apprByBatchNo(ResponseData responseData, SettleRecord settleRecord);
    /**
     * Description: 与快钱申请结算的方法
     * @author DXM
     * @param  id       申请结算的id
     * Date    2018/4/13
     */
    void settleWithHat(ResponseData responseData, Integer id) throws Exception;
    /**
     * Description: 根据id查询分账结算记录
     * @author DXM
     * @param  id   分帐的批次号
     * Date    2018/4/13
     */
    void getSettleRecordById(ResponseData responseData, Integer id);

    void querySettleRecordById(ResponseData responseData, String batchNo) throws  Exception;
    /**
     * Description: 退货分账
     * @author DXM
     * @param  id   分帐的id
     * @param refundAmount
     */
    void settleRefund(ResponseData responseData, Integer id, BigDecimal refundAmount) throws Exception;
}
