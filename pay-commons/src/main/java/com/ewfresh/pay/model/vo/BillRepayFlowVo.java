/**
 * Copyright (c) 2019 Sunshine Insurance Group Inc
 * Created by gaoyongqiang on 2019/3/27.
 **/
 
package com.ewfresh.pay.model.vo;

import com.ewfresh.pay.model.BillRepayFlow;

import java.util.Date;

/**
  * @descrption TODO
 * @author gaoyongqiqng
 * @create 2019-03-27
 * @Email 1005267839@qq.com
  **/
public class BillRepayFlowVo extends BillRepayFlow {
    /**
     * 账单批次号 gyq
     */
    private String billFlow;
    /**
     * 出账时间 gyq
     */
    private String billTime;

    public String getBillTime() {
        return billTime;
    }

    public void setBillTime(String billTime) {
        this.billTime = billTime;
    }

    public String getBillFlow() {
        return billFlow;
    }

    public void setBillFlow(String billFlow) {
        this.billFlow = billFlow;
    }
}
