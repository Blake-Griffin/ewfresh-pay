package com.ewfresh.pay.redisService;

import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.model.vo.RefundInfoVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * description: Bill99相关的订单信息
 * @author: JiuDongDong
 * date: 2018/8/2.
 */
public interface Bill99OrderRedisService {
    // 存储商户退款信息     jiudongdong
    void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo, String hashKey);
    // 从Redis获取退款信息(根据第三方交易订单号)     jiudongdong
    RefundInfoVo getRefundOrderInfoFromRedis(String outRequestNo, String hashKey);
    // 从Redis获取退款信息（获取所有的）     jiudongdong
    Map<Object, Object> getAllRefundOrderInfoFromRedis(String hashKey);
    // 从Redis中删除退款信息     jiudongdong
    void deleteRefundOrderInfoInRedis(String outRequestNo, String hashKey);

    // 存储待修改订单状态信息     jiudongdong
    void putToUpdateStatusOrderInfo(Map<String, String> params, String hashKey);
    // 从Redis获取待修改订单状态信息(根据订单号，小key)     jiudongdong
    Map<Object, Object> getToUpdateStatusOrderInfo(String orderNo, String hashKey);
    // 从Redis获取待修改订单状态信息（获取所有的）     jiudongdong
    Map<Object, Object> getAllToUpdateStatusOrderInfo(String hashKey);
    // 从Redis中删除待修改订单状态信息     jiudongdong
    void deleteToUpdateStatusOrderInfoInRedis(String orderNo, String hashKey);
    // 从redis 删除退款参数
    void delReturnAmountParams(String orderId);
    // 从redis 获取退款参数
    Map<String, String> getReturnAmountParams(String orderId);

    void setWithdrawIdToredis(String withdrawId, String key);

    List<String> getWithdrawIdFromRedis(String key);

    /**
     * Description: 获取HAT提现id
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    Set getHATWithdrawIdMap(String hashKey);

    /**
     * Description: 获取HAT提现vo
     * @author: ZhaoQun
     * date: 2018/10/24 11:28
     */
    Bill99WithdrawAccountVo getHATWithdrawVoMap(String hashKey, String key);

    /**
     * Description: 存储HAT提现vo
     * @author: zhaoqun
     * @param vo  Bill99WithdrawAccountVo
     * @param hashKey  大key
     * date: 2018/10/24 11:28
     */
    void putHATWithdrawVoMap(Bill99WithdrawAccountVo vo, String hashKey);
    /**
     * Description: 从redis删除HAT提现类
     * @author: ZhaoQun
     * @param hashKey
     * @param key
     * @return:
     * date: 2018/10/24 17:45
     */
    void delHATWithdrawVoItem(String hashKey, String key);
}
