package com.ewfresh.pay.model.vo;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 *      商户查询订单响应信息
 * @author: JiuDongDong
 * date: 2018/4/11.
 */
public class BOCQueryOrderResponseVo implements Serializable {
//    private static final long serialVersionUID = 4009109747681740905L;

    private String merchantNo;//商户号

    private String exception;//错误码

    private List<BOCQueryOrderResponseBodyVo> queryOrderResponseBodyVoList;//商户查询订单响应体信息

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public List<BOCQueryOrderResponseBodyVo> getQueryOrderResponseBodyVoList() {
        return queryOrderResponseBodyVoList;
    }

    public void setQueryOrderResponseBodyVoList(List<BOCQueryOrderResponseBodyVo> queryOrderResponseBodyVoList) {
        this.queryOrderResponseBodyVoList = queryOrderResponseBodyVoList;
    }
}
