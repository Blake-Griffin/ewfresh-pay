package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData;
import org.apache.poi.ss.usermodel.Workbook;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * description:资金流水逻辑层
 *
 * @author: wangyaohui
 * date: 2018/4/20.
 */
public interface AccountFlowManager {
    /**
     * Description: 通过用户id查询资金流水记录
     * @author wangyaohui
     * @param  uid     要查询的用户id
     * Date    2018/4/13
     */
    void getAccountsByUid(ResponseData responseData,Long uid,Integer pageSize,Integer pageNumber,Map<String,Object> map);
    /**
     * Description: 添加一条账户资金流水的方法
     * @author wangyaohui
     * @return void
     * Date    2018/4/24 0024  下午 4:48
     */
    void getAccountFlowList(ResponseData responseData,Integer pageSize,Integer pageNumber,String uname,String occTime);

    /**
     * Description: 获取在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param responseData
     * @param pageSize
     * @param pageNumber
     * @param uname
     * @param timeStart
     * @param timeEnd
     * @param channelCode
     * @param tradeType
     * @return: void
     * date: 2018/9/5 15:29
     */
    void getOnlineRechargeList(ResponseData responseData, Integer pageSize, Integer pageNumber, String uname,
                               String channelCode, String timeStart, String timeEnd, Short tradeType) throws ParseException;

    /**
     * Description: 导出在线充值流水列表（可筛选）
     * @author: ZhaoQun
     * @param title
     * @param uname         客户名称
     * @param timeStart
     * @param timeEnd
     * @param channelCode   支付渠道
     * @param tradeType     交易类型
     * @return: void
     * date: 2018/9/5 15:29
     */
    Workbook exportOnlineRechargeList(ResponseData responseData, String title, String uname, String channelCode,
                                      String timeStart, String timeEnd, Short tradeType) throws ParseException;

    /**
     * Description: 导出余额日志明细报表
     * @author: LouZiFeng
     * @param title
     * @return: void
     * date: 2018/9/5 15:29
     */
    Workbook exportAccountsFlowList(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap)throws ParseException;

    /**
     * Description: 导出余额日志报表
     * @author: LouZiFeng
     * @param title
     * @return: void
     * date: 2018/9/5 15:29
     */
    Workbook exportAccountsList(ResponseData responseData, String title, String uname, String occTime)throws ParseException;

    /**
     * Description: 导出个人中心我的账户明细报表
     * @author: LouZiFeng
     * @param title
     * @return: void
     * date: 2018/11/13 15:29
     */
    Workbook exportPersonalAccount(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap);
    /**
     * Description: 查询是否有未处理的退款信息
     * @author: duanxiangming
     * date:2018/9/18
     */
    void checkPayFlowStatus(String channelFlowIds, Short tradeType, ResponseData responseData);
}
