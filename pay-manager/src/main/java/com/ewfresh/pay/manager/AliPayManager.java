package com.ewfresh.pay.manager;

import com.alipay.api.AlipayApiException;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.request.AliPayRequest;
import com.ewfresh.pay.util.ResponseData;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface AliPayManager {
    /**
     * description:支付加签接口
     *
     * @param aliPayRequest
     * @return
     * @throws AlipayApiException
     * @author wangziyuan
     */
    ResponseData AliPagePay(AliPayRequest aliPayRequest);

    /**
     * description:支付一比回调接口
     *
     * @param request
     * @return
     * @author wangziyuan
     */
    String AliNotify(HttpServletRequest request);

    /**
     * description:对账账单下载接口
     *
     * @throws Exception
     */
    void DownLoadBillToAdd() throws Exception;
}
