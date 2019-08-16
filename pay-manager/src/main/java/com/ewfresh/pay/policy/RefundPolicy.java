package com.ewfresh.pay.policy;

import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.RefundParam;
import com.ewfresh.pay.model.exception.*;
import org.dom4j.DocumentException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by wangziyuan on 2018/4/23.
 */
public interface RefundPolicy {
    List<PayFlow> refund(RefundParam refundParam) throws RefundParamNullException, PayFlowFoundNullException,
            UnsupportedEncodingException, RefundHttpToBill99FailedException, RefundBill99ResponseNullException,
            DocumentException, RefundBill99HandleException, Bill99NotFoundThisOrderException, RefundAmountMoreThanOriException,
            WeiXinNotEnoughException, TheBankDoNotSupportRefundTheSameDay, HttpToUnionPayFailedException, VerifyUnionPaySignatureException, UnionPayHandleRefundException;
}
