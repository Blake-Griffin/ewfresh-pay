package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.model.vo.RefundInfoVo;
import com.ewfresh.pay.redisDao.BOBOrderRedisDao;
import com.ewfresh.pay.redisService.BOBOrderRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 *      BOB相关的订单信息
 * @author: JiuDongDong
 * date: 2018/6/27.
 */
@Service
public class BOBOrderRedisServiceImpl implements BOBOrderRedisService {
    @Autowired
    private BOBOrderRedisDao bobOrderRedisDao;

    /**
     * Description: 将订单描述放入Redis
     * @author: JiuDongDong
     * @param orderNo 订单号
     * @param orderDesc  订单描述
     * date: 2018/6/27 15:52
     */
    @Override
    public void putOrderDesc2Redis(String orderNo, String orderDesc) {
        bobOrderRedisDao.putOrderDesc2Redis(orderNo, orderDesc);
    }

    /**
     * Description: 根据订单号获取订单描述
     * @author: JiuDongDong
     * @param orderNo  发给银行的订单号
     * @return java.lang.String 订单描述
     * date: 2018/6/27 15:48
     */
    @Override
    public String getOrderDescByOrderNo(String orderNo) {
        String orderDescByOrderNo = bobOrderRedisDao.getOrderDescByOrderNo(orderNo);
        return orderDescByOrderNo;
    }

    /**
     * Description: 根据订单号删除Redis中的订单描述信息
     * @author: JiuDongDong
     * @param orderNo 订单号
     * date: 2018/6/27 15:55
     */
    @Override
    public void deleteOrderDescFromRedis(String orderNo) {
        bobOrderRedisDao.deleteOrderDescFromRedis(orderNo);
    }

    /**
     * Description: 商户发送订单信息到BOB的时间放入Redis
     * @author: JiuDongDong
     * @param orderNo 订单号(19位)
     * @param sendOrder2BOBTime   商户发送订单信息到BOB的时间
     * date: 2018/6/29 13:59
     */
    @Override
    public void putSendOrderToBOBTimeToRedis(String orderNo, String sendOrder2BOBTime) {
        bobOrderRedisDao.putSendOrderToBOBTimeToRedis(orderNo, sendOrder2BOBTime);
    }

    /**
     * Description: 使用订单号（19位）获取商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @param orderNo  订单号（19位）
     * @return java.lang.String 商户发送订单信息到BOB的时间
     * date: 2018/6/29 14:01
     */
    @Override
    public String getSendOrderToBOBTimeFromRedis(String orderNo) {
        String sendOrderToBOBTime = bobOrderRedisDao.getSendOrderToBOBTimeFromRedis(orderNo);
        return sendOrderToBOBTime;
    }

    /**
     * Description: 从Redis中删除商户发送订单信息到BOB的时间
     * @author: JiuDongDong
     * @param orderNo 订单号
     * date: 2018/6/29 14:02
     */
    @Override
    public void deleteSendOrderToBOBTimeInRedis(String orderNo) {
        bobOrderRedisDao.deleteSendOrderToBOBTimeInRedis(orderNo);
    }

    /**
     * Description: 存储商户退款信息
     * @author: JiuDongDong
     * @param refundInfoVo 退款信息
     * date: 2018/6/30 15:16
     */
    @Override
    public void putRefundOrderInfoToRedis(RefundInfoVo refundInfoVo) {
        bobOrderRedisDao.putRefundOrderInfoToRedis(refundInfoVo);
    }

    /**
     * Description: 从Redis获取退款信息
     * @author: JiuDongDong
     * @param orderNo  父订单号
     * @return com.ewfresh.pay.model.RefundInfoVo 退款信息
     * date: 2018/6/30 15:18
     */
    @Override
    public RefundInfoVo getRefundOrderInfoFromRedis(String orderNo) {
        RefundInfoVo refundInfoVo = bobOrderRedisDao.getRefundOrderInfoFromRedis(orderNo);
        return refundInfoVo;
    }

    /**
     * Description: 从Redis中删除退款信息
     * @author: JiuDongDong
     * @param orderNo  父订单号
     * date: 2018/6/30 15:19
     */
    @Override
    public void deleteRefundOrderInfoInRedis(String orderNo) {
        bobOrderRedisDao.deleteRefundOrderInfoInRedis(orderNo);
    }

}
