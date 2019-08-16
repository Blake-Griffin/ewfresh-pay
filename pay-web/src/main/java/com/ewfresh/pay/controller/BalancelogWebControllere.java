package com.ewfresh.pay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * Description: 余额日志的页面跳转接口
 * @author wangyaohui
 * Date 2018/4/13
 */
@Controller
public class BalancelogWebControllere {
    @RequestMapping("/w/BalanceLog.html")
    public String BalanceLog(){
        return "/BalanceLog/BalanceLog";
    }

    @RequestMapping("/w/BalanceLogDetails.html")
    public String BalanceLogDetails(){
        return "/BalanceLog/BalanceLogDetails";
    }

    @RequestMapping("/w/FinancialBalanceLogDetails.html")
    public String FinancialBalanceLogDetails(){
        return "/FinancialBalanceLog/FinancialBalanceLogDetails";
    }

    @RequestMapping("/w/FinancialBalanceLog.html")
    public String FinancialBalanceLog(){
        return "/FinancialBalanceLog/FinancialBalanceLog";
    }
}
