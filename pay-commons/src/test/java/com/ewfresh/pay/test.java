package com.ewfresh.pay;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.request.AliPayRequest;
import org.junit.Test;

/**
 * Created by wangziyuan on 2018/4/19.
 */
public class test
{
    @Test
    public void testvoid(){
        String s ="{out_trade_no:100342E,product_code:'FAST_INSTANT_TRADE_PAY',total_amount: 2880.00,subject:'【易网聚鲜】 订单编号:100342E',body:''}";
        AliPayRequest aliPayRequest = ItvJsonUtil.jsonToObj(s, new AliPayRequest().getClass());
        System.out.println(aliPayRequest);
    }
}
