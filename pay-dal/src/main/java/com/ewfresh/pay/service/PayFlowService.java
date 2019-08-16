package com.ewfresh.pay.service;

import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.AutoRepayBillVo;
import com.ewfresh.pay.model.vo.OnlineRechargeFlowVo;
import com.github.pagehelper.PageInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by 王耀辉 on 2018/4/12.
 */
public interface PayFlowService {


    /**
     * Description: 添加一条交易流水记录
     *
     * @param payFlow 分装交易流水记录的对象
     * @author DuanXiangming
     */
    int addPayFlow(PayFlow payFlow);

    PayFlow getPayFlowById(String channelFlowId);

    int updatePayFlowById(PayFlow record);

    void addBatch(List<Map<String, Object>> list);

    PayFlow getPayFlowPartById(String channelFlowId);

    PayFlow getPayFlowPartByPayFlowId(Integer payFlowId);

    // 根据第三方交互订单号查询流水信息（北京银行专用）     jiudongdong
    PayFlow getPayFlowByInteractionId(String interactionId);

    // 根据订单号查询最近一次订单支付交易的支付渠道流水号   jiudongdong
    String getChannelFlowIdByOrderNo(Long orderId, String channelCode, Short tradeType, Short status);

    // 根据第三方交互订单号查询订单退款流水信息（北京银行专用）     jiudongdong
    String getReturnFlowIdByInteractionId(String interactionId);

    // 根据渠道流水id查询订单退款流水信息     jiudongdong
    PayFlow getFlowIdByRefundSequence(String channelFlowId);

    // 更新支付流水，channel_flow_id参数必传，其余随机     jiudongdong
    int updatePayFlow(PayFlow payFlow);

    // 根据父订单号查询快捷支付的退款流水集合    jiudongdong
    List<PayFlow> getPayFlowsByOrderId(Long orderId);

    // 根据父订单号和子订单号查询快钱快捷、快钱网银的支付流水集合    jiudongdong
    List<PayFlow> getBill99PayFlowsByOrderId(Long orderId);

    /**
     * Description:充值或体现的方法
     *
     * @param payFlow    支付流水
     * @param accFlow    账户资金流水
     * @param withdrawto 提现记录
     * @author DuanXiangming
     */
    int withdrawto(PayFlow payFlow, AccountFlowVo accFlow, Withdrawto withdrawto);

    /**
     * Description:充值或体现的方法
     *
     * @param payFlow 支付流水
     * @param accFlow 账户资金流水
     * @author DuanXiangming
     */
    void reCharge(PayFlow payFlow, AccountFlowVo accFlow);

    /**
     * Description: 内部扣减余额的方法
     *
     * @param payFlow 封装交易流水的对象
     *                Date    2018/4/11
     * @author DuanXiangming
     */
    void abatementBalance(PayFlow payFlow, AccountFlowVo accFlowByPayFlow);

    /**
     * Description:根据订单Id查询所有有关支付流水
     *
     * @param orderId 订单Id
     * @return java.util.List<com.ewfresh.pay.model.PayFlow>
     * Date    2018/4/27
     * @author DuanXiangming
     */
    List<PayFlow> getPayFlowByOrderId(Long orderId);

    /**
     * Description: 添加一条交易流水记录和对应的账户流水
     *
     * @param withdrawto       提现信息对象
     * @param payFlow          分装交易流水记录的对象
     * @param accFlowByPayFlow 账户资金流水的内容
     * @author DuanXiangming
     */
    int addPayFlowAndAccFlow(Withdrawto withdrawto, PayFlow payFlow, AccountFlowVo accFlowByPayFlow);

    /**
     * Description: 根据订单ID获取所有余额支付的流水
     *
     * @param orderId 订单ID
     * @author DuanXiangming
     */
    List<PayFlow> getBalancePayFlow(Long orderId);

    /**
     * Description: 根据订单ID获取所有余额退款的流水
     *
     * @param orderId       订单ID
     * @param interactionId
     * @author DuanXiangming
     */
    List<PayFlow> getRefundBalancePayFlow(Long orderId, String interactionId);

    /**
     * Description: 根据订单ID和支付渠道获取所有余额退款的流水
     *
     * @param orderId 订单ID
     * @author DuanXiangming
     */
    List<PayFlow> getRefundFlowByOrderIdAndChannel(Long orderId, String channelCode);

    /**
     * Description: 添加多条支付流水的方法
     *
     * @param payFlows 订单ID
     * @author DuanXiangming
     */
    void addPayFlows(List<PayFlow> payFlows);

    List<PayFlow> getPayFlowByItems(Long orderId, String interactionId, Short tradeType);

    /**
     * Description: 根据支付渠道流水号查询渠道类型
     *
     * @param channelFlowId 支付渠道流水号
     * @return 渠道类型
     * date: 2018/6/12 20:50
     * @author: JiuDongDong
     */
    String getChannelTypeByChannelFlowId(String channelFlowId);

    /**
     * Description: 根据第三方交互订单号查询订单支付成功时间（北京银行或北京银行银联专用）
     *
     * @param interactionId 第三方交互订单号
     * @param tradeType     交易类型
     * @return 订单支付成功时间
     * date: 2018/6/13 13:44
     * @author: JiuDongDong
     */
    Date getSuccessTimeByInteractionId(String interactionId, Short tradeType);

    /**
     * Description: 根据支付渠道流水号查询订单支付成功时间（北京银行专用）
     *
     * @param channelFlowId 支付渠道流水号
     * @return 订单支付成功时间
     * date: 2018/6/13 15:14
     * @author: JiuDongDong
     */
    Date getSuccessTimeByChannelFlowId(String channelFlowId);

    /**
     * Description: 根据三方交易流水和交易类型查询交易流水
     *
     * @param channelFlowId
     * @param tradeType     Date    2018/6/27 0027  上午 10:48
     * @author DuanXiangming
     */
    PayFlow getPayFlowPartByIdAndTradeType(String channelFlowId, Short tradeType);

    /**
     * Description: 添加一条交易流水记录和对应的账户流水
     *
     * @param payFlow          分装交易流水记录的对象
     * @param accFlowByPayFlow 账户资金流水的内容
     * @author DuanXiangming
     */
    void payByBalance(PayFlow payFlow, AccountFlowVo accFlowByPayFlow);

    /**
     * Description: 获取在线充值流水列表（可筛选）
     *
     * @param pageSize
     * @param pageNumber
     * @param map        date: 2018/9/5 15:29
     * @author: ZhaoQun
     */
    PageInfo<OnlineRechargeFlowVo> getOnlineRechargeList(Integer pageSize, Integer pageNumber, Map<String, Object> map);

    List<OnlineRechargeFlowVo> exportOnlineRechargeList(Map<String, Object> paramMap);
    /**
     * Description: 获取结算流水（可筛选）
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    PageInfo<PayFlow> getSettlePayflows(Integer shopId, Integer pageSize, Integer pageNumber, String batchNo, Short orderStatus, Short settleStatus, String successTime);
    /**
     * Description: 获取全部可结算流水
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    List<PayFlow> getAllSettlePayflows(Integer shopId, Short orderStatus, Short settleStatus, String successTime);
    /**
     * Description: 获取全部可结算流水
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    List<PayFlow> getPayflowsByIds(List<Integer> payflows);
    /**
     * Description: 根据批次号获取具体分账流水
     * @author DXM
     * @param  batchNo    分账批次号
     * Date    2018/4/13
     */
    PageInfo<PayFlow> getSettleRecordsByBatchNo(String batchNo, Integer pageSize, Integer pageNumber);

    void updateOrderStatus(String chanelFlowId);

    int ifUseSpecialBank(Long orderId);

    //白条支付添加流水   zhaoqun
    int payByWhite(PayFlow payFlow, AccountFlowVo accFlowByPayFlow, BarDealFlow barDealFlow,PayFlow balancePayFlow, AccountFlowVo balanceAccFlow);
    /**
     * Description: 白条自动还款的方法
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    void addPayFlowAndAccAndBdfAndBrf(PayFlow payFlow, AccountFlowVo accFlowByPayFlow, List<BarDealFlow> barDealFlow, List<BillRepayFlow> billRepayFlows, List<AutoRepayBillVo> autoRepayBillVos);
    /**
     * Description: 获取所有配货补款的流水
     * @author: Duanxianming
     * date: 2018/9/5 15:29
     */
    List<PayFlow> getPayFlowsByOrderIdAndTradeType(String orderId, Short... tradeTypes);
    /**
     * Description: 查询是否有未处理的退款信息
     * @author: duanxiangming
     * date:2018/9/18
     */
    Map<String, Short> checkPayFlowStatus(Short tradeType, String... channelFlowIds);
}
