package com.ewfresh.pay.dao;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.OnlineRechargeFlowVo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PayFlowDao {
    int delPayFlowById(Integer payFlowId);

    int addPayFlow(PayFlow payFlow);

    PayFlow getPayFlowById(String channelFlowId);

    int updatePayFlowById(PayFlow record);

    void addBatch(List<Map<String,Object>> list);
    // 根据支付渠道流水号查询流水信息    jiudongdong
    PayFlow getPayFlowPartById(String channelFlowId);
    // 根据订单号查询最近一次订单支付交易的支付渠道流水号   jiudongdong
    String getChannelFlowIdByOrderNo(@Param("orderId") Long orderId, @Param("channelCode") String channelCode, @Param("tradeType") Short tradeType, @Param("status") Short status);
    // 根据支付流水ID查询流水信息   jiudongdong
    PayFlow getPayFlowPartByPayFlowId(Integer payFlowId);
    // 根据第三方交互订单号查询流水信息（北京银行专用）     jiudongdong
    PayFlow getPayFlowByInteractionId(@Param("interactionId") String interactionId);
    // 根据第三方交互订单号查询最新的1条订单退款流水信息（北京银行专用）     jiudongdong
    String getReturnFlowIdByInteractionId(@Param("interactionId") String interactionId);
    // 根据渠道流水id查询订单退款流水信息     jiudongdong
    PayFlow getFlowIdByRefundSequence(String channelFlowId);
    // 更新支付流水，channel_flow_id参数必传，其余随机     jiudongdong
    int updatePayFlow(PayFlow payFlow);
    // 根据父订单号查询快捷支付的退款流水集合    jiudongdong
    List<PayFlow> getPayFlowsByOrderId(Long orderId);
    // 根据父订单号和子订单号查询快钱快捷、快钱网银的支付流水集合    jiudongdong
    List<PayFlow> getBill99PayFlowsByOrderId(@Param("orderId") Long orderId);
    // 查询今天是否使用快钱快捷的农业银行,交通银行,兴业银行,光大银行,中信银行,邮储银行,上海银行进行支付
    int ifUseSpecialBank(Long orderId);
    /**
     * Description:根据订单Id查询所有有关支付流水
     * @author DuanXiangming
     * @param  orderId    订单Id
     * @return java.util.List<com.ewfresh.pay.model.PayFlow>
     * Date    2018/4/27
     */
    List<PayFlow> getPayFlowByOrderId(Long orderId);
    /**
     * Description:根据订单Id查询所有退款有关支付流水
     * @author DuanXiangming
     * @param  orderId    订单Id
     * @return java.util.List<com.ewfresh.pay.model.PayFlow>
     * Date    2018/4/27
     */
    List<PayFlow> getRefundFlowByOrderId(Long orderId);
    /**
     * Description: 根据订单ID获取所有余额支付的流水
     * @author DuanXiangming
     * @param  orderId    订单ID
     */
    List<PayFlow> getBalancePayFlow(Long orderId);
    /**
     * Description: 根据订单ID获取所有余额退款的流水
     * @author DuanXiangming
     * @param  orderId    订单ID
     */
    List<PayFlow> getRefundBalancePayFlow(@Param("orderId") Long orderId, @Param("interactionId") String interactionId);
    /**
     * Description: 根据订单ID和支付渠道获取所有余额退款的流水
     * @author DuanXiangming
     * @param  orderId    订单ID
     */
    List<PayFlow> getRefundFlowByOrderIdAndChannel(@Param("orderId") Long orderId ,@Param("channelCode") String Channel);
    /**
     * Description: 添加多条支付流水的方法
     * @author DuanXiangming
     * @param  payFlows    订单ID
     */
    void addPayFlows(List<PayFlow> payFlows);

    List<PayFlow> getPayFlowByItems(@Param("orderId") Long orderId, @Param("interactionId")String interactionId,@Param("tradeType") Short tradeType);

    // 根据支付渠道流水号查询渠道类型     jiudongdong
    String getChannelTypeByChannelFlowId(@Param("channelFlowId") String channelFlowId);

    // 根据第三方交互订单号查询订单支付成功时间（北京银行或北京银行银联专用）     jiudongdong
    Date getSuccessTimeByInteractionId(@Param("interactionId") String interactionId, @Param("tradeType") Short tradeType);

    // 根据支付渠道流水号查询订单支付成功时间（北京银行专用）     jiudongdong
    Date getSuccessTimeByChannelFlowId(@Param("channelFlowId") String channelFlowId);
    //根据支付渠道流水号和交易类型查询流水信息
    PayFlow getPayFlowPartByIdAndTradeType( @Param("channelFlowId")String channelFlowId, @Param("tradeType")Short tradeType);

    //根据第三方交互订单号和建议类型查询订单号  微信支付   zhaoqun
    Long getOrderId(String outTradeNo);

    /**
     * Description: 获取在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param map
     * date: 2018/9/5 15:29
     */
    List<OnlineRechargeFlowVo> getOnlineRechargeList(Map<String, Object> map);
    /**
     * Description: 获取结算流水（可筛选）
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    List<PayFlow> getSettlePayflows(@Param("shopId")Integer shopId, @Param("batchNo")String batchNo, @Param("orderStatus")Short orderStatus, @Param("settleStatus")Short settleStatus, @Param("successTime")String successTime);
    /**
     * Description: 根据id批量获取交易流水
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    List<PayFlow> getPayflowsByIds(List<Integer> payflows);
    /**
     * Description: 批量更新支付流水的分账批次号
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     * @param oldBatchNo
     * @param payflows
     */
    void updateSettleStatus(@Param("oldBatchNo")Integer oldBatchNo, @Param("list")List<PayFlow> payflows);
    /**
     * Description: 批量更新支付流水的分账批次号
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    void updateSettleStatusByBatchNo(@Param("oldBatchNo")String oldBatchNo, @Param("settleStatus")Short settleStatus, @Param("newBatchNo")String newBatchNo);
    /**
     * Description: 根据批次号获取具体分账流水
     * @author DXM
     * @param  batchNo    分账批次号
     * Date    2018/4/13
     */
    List<PayFlow> getSettleRecordsByBatchNo(String batchNo);
    /**
     * Description: 修改订单状态的方法
     * @author DXM
     * @param  chanelFlowId    分账批次号
     * Date    2018/4/13
     */
    int updateOrderStatus(String chanelFlowId);
    /**
     * description: 查询最近的用户id查询最近的一条payflow
     * @author  huboyang
     * @param
     */
    PayFlow getPayFlowByPayerId(String payerId);

    List<PayFlow> getPayFlowsByOrderIdAndTradeType(@Param("orderId") String orderId, @Param("tradeType")Short [] tradeType);
    /**
     * Description: 查询是否有未处理的退款信息
     * @author: duanxiangming
     * date:2018/9/18
     */
    Map<String, Short> checkPayFlowStatus(@Param("tradeType") Short tradeType, @Param("channelFlowIds") String[] channelFlowIds);
}