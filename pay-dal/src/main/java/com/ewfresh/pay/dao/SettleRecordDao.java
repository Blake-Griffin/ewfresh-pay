package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.SettleRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SettleRecordDao {
    int deleteByPrimaryKey(Integer id);

    int insert(SettleRecord record);

    int insertSelective(SettleRecord record);

    SettleRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(SettleRecord record);
    /**
     * Description: 通过店铺id查询账单记录
     * @author wangyaohui
     * @param  shopId     要查询的用户id
     */
    List<SettleRecord> getSettleRecordByShopId(@Param("shopId") Integer shopId);

    int addSettleRecord(SettleRecord settleRecord);
    /**
     * Description: 获取分账审核列表的方法
     * @author DXM
     * @param  shopName    过滤条件之店铺名称
     * @param shopId
     */
    List<SettleRecord> getSettleRecords(@Param("shopName") String shopName, @Param("shopId")Integer shopId, @Param("settleStatus") Short settleStatus);
    /**
     * Description: 审核分账内容的接口
     * @author DXM
     * @param  settleRecord   分装审核的内容
     */
    int apprByBatchNo(SettleRecord settleRecord);

    /**
     * Description: 根据id查询对应结算记录的方法
     * @author DXM
     * @param  id   分装审核的内容
     * Date    2018/4/13
     */
    SettleRecord getSettleRecordById(Integer id);
    /**
     * Description: 根据ID修改对应结算记录的方法
     * @author DXM
     * @param  settleRecord   分装的修改内容
     */
    int updateBatchNoById(SettleRecord settleRecord);
}