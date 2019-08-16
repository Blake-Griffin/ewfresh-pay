package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.vo.BOCMerchantRecvOrderNotifyVo;
import com.ewfresh.pay.model.vo.BOCMerchantSendOrderVo;
import com.ewfresh.pay.util.ResponseData;

/**
 * description:
 *      BOC的逻辑处理层
 * @author: JiuDongDong
 * date: 2018/4/8.
 */
public interface BOCManager {
    void sendOrder(ResponseData responseData, BOCMerchantSendOrderVo bocRecvOrder);

    void receiveNotify(BOCMerchantRecvOrderNotifyVo merchantRecvOrderNotifyVo);

    void queryOrder(ResponseData responseData, String orderNos);

    void commonQueryOrder(ResponseData responseData, String orderNos);

    void refundOrder(ResponseData responseData, RefundParam refundParam);

    void getTicketDownloadFile(ResponseData responseData, String fileDate, String extend);
}
