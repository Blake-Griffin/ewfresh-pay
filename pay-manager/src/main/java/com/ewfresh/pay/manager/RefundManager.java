package com.ewfresh.pay.manager;

import com.ewfresh.pay.model.RefundParams;
import com.ewfresh.pay.model.exception.*;
import com.ewfresh.pay.util.ResponseData;
import org.dom4j.DocumentException;

import java.io.UnsupportedEncodingException;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/27 0027
 */
public interface RefundManager {

    /**
     * Description: 用户取消订单退款的方法
     * @author DuanXiangming
     * @param refundParam
     */
    void refund(ResponseData responseData, RefundParams refundParam) throws RefundBill99ResponseNullException,
            UnsupportedEncodingException, RefundParamNullException, RefundBill99HandleException, DocumentException,
            RefundHttpToBill99FailedException, PayFlowFoundNullException, Bill99NotFoundThisOrderException,
            RefundAmountMoreThanOriException, WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, UnionPayHandleRefundException, VerifyUnionPaySignatureException;

}
