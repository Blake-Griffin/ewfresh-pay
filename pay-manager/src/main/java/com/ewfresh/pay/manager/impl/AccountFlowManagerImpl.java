package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.AccountFlowManager;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.vo.*;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AccountFlowService;
import com.ewfresh.pay.util.AccountFlowDescUtil;
import com.ewfresh.pay.service.PayFlowService;
import com.ewfresh.pay.util.*;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Description:账户资金流水表
 *
 * @author wangyaohui
 * Date 2018/4/11
 */
@Component
public class AccountFlowManagerImpl implements AccountFlowManager {
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private PayFlowService payFlowService;


    private static String HH_MM_SS = " 23:59:59"; //时分秒
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String UNAME = "uname";
    private static final String OCCTIME = "occTime";
    private static final String PHONE = "phone";
    private static final String TIME_START = "timeStart";
    private static final String TIME_END = "timeEnd";
    private static final String CHANNEL_CODE = "channelCode";
    private static final String TRADE_TYPE = "tradeType";

    private static final String SUCCESSSTATUS = "successStatus";
    private static final String FAILEDSTATUS = "failedStatus";
    private static final String WAITSTATUS = "waitStatus";
    @Override
    public void getAccountsByUid(ResponseData responseData, Long uid, Integer pageSize, Integer pageNumber, Map<String, Object> map) {
        PageInfo<AccountFlow> accountsByUid = accountFlowService.getAccountsByUid(pageSize, pageNumber, map);
        List<AccountFlow> list = accountsByUid.getList();
        responseData.setEntity(list);
        responseData.setTotal(accountsByUid.getTotal() + "");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(accountsByUid.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

    }

    @Override
    public void getAccountFlowList(ResponseData responseData, Integer pageSize, Integer pageNumber, String uname, String occTime) {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        if (StringUtils.isNotBlank(uname))
            stringStringHashMap.put(UNAME, uname);
        if (StringUtils.isNotBlank(occTime)) {
            if (occTime.length() <= Constants.TEN) {
                occTime += HH_MM_SS;
            }
            stringStringHashMap.put(OCCTIME, occTime);
        }
        PageInfo<Long> accountFlowList = accountFlowService.getAccountFlowList(pageSize, pageNumber, stringStringHashMap);
        List<Long> list = accountFlowList.getList();
        logger.info("getAccountFlowList -------------[list={},occTime={}]", ItvJsonUtil.toJson(list), occTime);
        if (!CollectionUtils.isEmpty(list)) {
            List<AccountFlowListVo> accountFlowIdByParm = accountFlowService.getAccountFlowIdByParm(list);
            for (AccountFlowListVo accountFlowListVo : accountFlowIdByParm) {
                String userInfo = accountFlowRedisService.getUserInfo(accountFlowListVo.getUid());
                HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                if (!StringUtils.isEmpty(userInfo)) {
                    Object phone = hashMap.get(PHONE);
                    accountFlowListVo.setPhone(phone.toString());
                }
            }
            responseData.setEntity(accountFlowIdByParm);
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        responseData.setTotal(accountFlowList.getTotal() + "");
        responseData.setPageCount(accountFlowList.getPages());
        responseData.setCurPage(pageNumber);
    }

    /**
     * Description: 获取在线充值流水列表（可筛选）
     *
     * @param responseData
     * @param pageSize
     * @param pageNumber
     * @param uname
     * @param timeStart
     * @param timeEnd
     * @param channelCode
     * @author: ZhaoQun
     * @return: void
     * date: 2018/9/5 15:29
     */
    @Override
    public void getOnlineRechargeList(ResponseData responseData, Integer pageSize, Integer pageNumber, String uname,
                                      String channelCode, String timeStart, String timeEnd, Short tradeType) throws ParseException {
        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time1 = null;
        Date time2 = null;
        if (StringUtil.isNotEmpty(timeStart)) {
            time1 = sdf.parse(timeStart);
        }
        if (StringUtil.isNotEmpty(timeEnd)) {
            time2 = sdf.parse(timeEnd);
        }
        map.put(TIME_START, time1);
        map.put(TIME_END, time2);
        map.put(CHANNEL_CODE, channelCode);
        map.put(UNAME, uname);
        map.put(TRADE_TYPE, tradeType);
        PageInfo<OnlineRechargeFlowVo> pageInfo = payFlowService.getOnlineRechargeList(pageSize, pageNumber, map);
        List<OnlineRechargeFlowVo> list = pageInfo.getList();
        responseData.setEntity(list);
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(pageInfo.getPages());
        responseData.setTotal(Long.toString(pageInfo.getTotal()));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }

    @Override
    public Workbook exportOnlineRechargeList(ResponseData responseData, String title, String uname, String channelCode,
                                             String timeStart, String timeEnd, Short tradeType) throws ParseException {
        logger.info("It is in manager.exportListInfo");
        Map<String, Object> paramMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time1 = null;
        Date time2 = null;
        if (StringUtil.isNotEmpty(timeStart)) {
            time1 = sdf.parse(timeStart);
        }
        if (StringUtil.isNotEmpty(timeEnd)) {
            time2 = sdf.parse(timeEnd);
        }
        paramMap.put(TIME_START, time1);
        paramMap.put(TIME_END, time2);
        paramMap.put(CHANNEL_CODE, channelCode);
        paramMap.put(UNAME, uname);
        paramMap.put(TRADE_TYPE, tradeType);
        List<OnlineRechargeFlowVo> list = payFlowService.exportOnlineRechargeList(paramMap);
        List<ExportOnlineRechargeFlowVo> ExportOnlineRechargeFlowVoList = packExclListInfo(list);
        ExportOnlineRechargeFlowEnum[] values = ExportOnlineRechargeFlowEnum.values();
        Workbook workbook = new HSSFWorkbook();//SXSSFWorkbook();
        Map<String, String> map = new HashMap<>();
        for (ExportOnlineRechargeFlowEnum value : values) {
            map.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(map);
        workbook = POIUtil.exportExcel(workbook, ExportOnlineRechargeFlowVoList, title, ExportOnlineRechargeFlowVo.class);
        logger.info("Excl orderDetail list to manager is ok");
        return workbook;
    }

    /**
     * Description: 包装需要导出的流水列表信息
     *
     * @author zhaoqun
     * @Date 2018/04/18 9:44
     */
    public List<ExportOnlineRechargeFlowVo> packExclListInfo(List<OnlineRechargeFlowVo> list) {
        ArrayList<ExportOnlineRechargeFlowVo> ExportOnlineRechargeFlowVoList = new ArrayList<>();
        for (OnlineRechargeFlowVo vo : list) {
            ExportOnlineRechargeFlowVo expVo = new ExportOnlineRechargeFlowVo();

            expVo.setUname(vo.getUname());//客户名称
            expVo.setPayerPayAmount(vo.getPayerPayAmount());//金额
            expVo.setChannelName(vo.getChannelName());//支付渠道
            expVo.setCreateTime(vo.getCreateTime());//充值时间

            ExportOnlineRechargeFlowVoList.add(expVo);
        }
        return ExportOnlineRechargeFlowVoList;
    }

    @Override
    public Workbook exportAccountsList(ResponseData responseData, String title, String uname, String occTime) throws ParseException {
        HashMap<String, String> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(uname))
            paramMap.put(UNAME, uname);
        if (StringUtils.isNotBlank(occTime)) {
            if (occTime.length() <= Constants.TEN) {
                occTime += HH_MM_SS;
            }
            paramMap.put(OCCTIME, occTime);
        }
        Workbook workbook = null;
        List<Long> list = accountFlowService.getAccountFlowLists(paramMap);
        logger.info("getAccount------------[list={},occTime={}]", ItvJsonUtil.toJson(list));
        if (CollectionUtils.isNotEmpty(list)) {
            List<AccountFlowListVo> accountFlowIdByParm = accountFlowService.getAccountFlowIdByParm(list);
            for (AccountFlowListVo accountFlowListVo : accountFlowIdByParm) {
                String userInfo = accountFlowRedisService.getUserInfo(accountFlowListVo.getUid());
                HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                if (!StringUtils.isEmpty(userInfo)) {
                    Object phone = hashMap.get(PHONE);
                    accountFlowListVo.setPhone(phone.toString());
                }
            }
            workbook = new HSSFWorkbook();
            ExportAccountEnum[] values = ExportAccountEnum.values();
            Map<String, String> map = new HashMap<>();
            for (ExportAccountEnum value : values) {
                map.put(value.name(), value.getValue());
            }
            POIUtil.setChineseMap(map);
            workbook = POIUtil.exportExcel(workbook, accountFlowIdByParm, title, AccountFlowListVo.class);
            logger.info("Excl Account list to manager is ok");
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return workbook;
    }

    @Override
    public Workbook exportAccountsFlowList(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap) throws ParseException {
        logger.info("It is in manager.exportAccountsFlowList");
        List<AccountFlow> accountFlows = accountFlowService.getAccountsByUidList(stringObjectHashMap);
        logger.info("It is in manager.exportAccountsFlowList accountFlows={}", ItvJsonUtil.toJson(accountFlows));
        List<AccountFlowOneVo> accountFlowOneVos = ExpAccountFlowListInfo(accountFlows);
        ExportAccountFlowEnumOne[] values = ExportAccountFlowEnumOne.values();
        Workbook workbook = new HSSFWorkbook();
        Map<String, String> map = new HashMap<>();
        for (ExportAccountFlowEnumOne value : values) {
            map.put(value.name(), value.getValue());
        }
        POIUtil.setChineseMap(map);
        workbook = POIUtil.exportExcel(workbook, accountFlowOneVos, title, AccountFlowOneVo.class);
        logger.info("Excl AccountsFlow list to manager is ok");
        return workbook;
    }

    @Override
    public Workbook exportPersonalAccount(ResponseData responseData, String title, HashMap<String, Object> stringObjectHashMap) {
        logger.info("It is in manager.exportAccountsFlowList");
        List<AccountFlow> accountFlows = accountFlowService.getAccountsByUidList(stringObjectHashMap);
        Workbook workbook = new HSSFWorkbook();
        List<AccountFlowTwo> accountFlowTwos = ExpPersonalAccountListInfo(accountFlows);
        if (CollectionUtils.isNotEmpty(accountFlowTwos)) {
            ExportAccountFlowEnumTwo[] values = ExportAccountFlowEnumTwo.values();
            Map<String, String> map = new HashMap<>();
            for (ExportAccountFlowEnumTwo value : values) {
                map.put(value.name(), value.getValue());
            }
            POIUtil.setChineseMap(map);
            workbook = POIUtil.exportExcel(workbook, accountFlowTwos, title, AccountFlowTwo.class);
            logger.info("Excl AccountsFlow list to manager is ok");
        }
        return workbook;
    }

    /**
     * Description: 包装需要导出的余额日志明细
     *
     * @author louzifeng
     * @Date 2018/9/18 14:50
     */
    public List<AccountFlowOneVo> ExpAccountFlowListInfo(List<AccountFlow> list) {
        ArrayList<AccountFlowOneVo> accountFlowVosList = new ArrayList<>();
        for (AccountFlow accountFlow : list) {
            AccountFlowOneVo accountFlowVo = new AccountFlowOneVo();
            accountFlowVo.setUname(accountFlow.getUname());//客户名称
            accountFlowVo.setAmount(accountFlow.getAmount());//金额
            accountFlowVo.setBalance(accountFlow.getBalance());//余额
            accountFlowVo.setDesp(accountFlow.getDesp());//说明信息
            accountFlowVo.setOccTime(accountFlow.getOccTime());//时间
            getAccTypes(accountFlow, accountFlowVo);//支付渠道
            accountFlowVosList.add(accountFlowVo);
        }
        return accountFlowVosList;
    }

    /**
     * Description: 包装需要导出的我的账户明细
     *
     * @author louzifeng
     * @Date 2018/9/18 14:50
     */
    public List<AccountFlowTwo> ExpPersonalAccountListInfo(List<AccountFlow> list) {
        ArrayList<AccountFlowTwo> accountFlowVosList = new ArrayList<>();
        for (AccountFlow accountFlow : list) {
            AccountFlowTwo accountFlowTwo = new AccountFlowTwo();
            accountFlowTwo.setAccFlowId(accountFlow.getAccFlowId());//流水编号
            accountFlowTwo.setOccTime(accountFlow.getOccTime());//时间
            accountFlowTwo.setAmount(accountFlow.getAmount());//金额
            getAccTypeOne(accountFlow, accountFlowTwo);//业务类型
            accountFlowTwo.setBusiNo(accountFlow.getBusiNo());//业务流水号
            accountFlowTwo.setDesp(accountFlow.getDesp());//说明信息
            accountFlowVosList.add(accountFlowTwo);
        }
        return accountFlowVosList;
    }

    public AccountFlowOneVo getAccTypes(AccountFlow accountFlow, AccountFlowOneVo accountFlowVo) {
        Short accType = accountFlow.getAccType();
        if (accType.shortValue() == Constants.ACC_TYPE_1.shortValue()) {
            accountFlowVo.setAccType(Constants.ALIPAY);//支付渠道 支付宝
        } else if (accType.shortValue() == Constants.ACC_TYPE_2.shortValue()) {
            accountFlowVo.setAccType(Constants.WECHATPAYMENT);//支付渠道 微信
        } else if (accType.shortValue() == Constants.ACC_TYPE_3.shortValue()) {
            accountFlowVo.setAccType(Constants.BANKCARD);//支付渠道 银行卡
        } else if (accType.shortValue() == Constants.ACC_TYPE_4.shortValue()) {
            accountFlowVo.setAccType(Constants.BALANCES);//支付渠道 余额
        } else if (accType.shortValue() == Constants.ACC_TYPE_5.shortValue()) {
            accountFlowVo.setAccType(Constants.FASTMONEY);//支付渠道 快钱
        }else if (accType.shortValue() == Constants.ACC_TYPE_6.shortValue()) {
            accountFlowVo.setAccType(Constants.GREDIT);//支付渠道 信用
        }
        return accountFlowVo;
    }

    public AccountFlowTwo getAccTypeOne(AccountFlow accountFlow, AccountFlowTwo accountFlowVo) {
        Short busiType = accountFlow.getBusiType();
        if (busiType.shortValue() == Constants.BUSI_TYPE_1.shortValue()) {
            accountFlowVo.setAccType(Constants.INVEST);//业务类型 线上充值
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_2.shortValue()) {
            accountFlowVo.setAccType(Constants.WITHDTAWALS);//业务类型 提现
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_3.shortValue()) {
            accountFlowVo.setAccType(Constants.ORDERDEDUCTION);//业务类型 订单扣款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_4.shortValue()) {
            accountFlowVo.setAccType(Constants.SETTLEMENTDEDUCTION);//业务类型 结算扣款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_5.shortValue()) {
            accountFlowVo.setAccType(Constants.SALESRECEIPTS);//业务类型 销售收款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_6.shortValue()) {
            accountFlowVo.setAccType(Constants.REFUNDINCOME);//业务类型 退款收入
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_7.shortValue()) {
            accountFlowVo.setAccType(Constants.FREEZE);//业务类型 冻结
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_8.shortValue()) {
            accountFlowVo.setAccType(Constants.RELEASETHEAMOUNT);//业务类型 释放冻结金额
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_9.shortValue()) {
            accountFlowVo.setAccType(Constants.DISTRIBUTIONDEDUCTION);//业务类型 配货扣款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_10.shortValue()) {
            accountFlowVo.setAccType(Constants.REFUNDS);//业务类型 配货退款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_11.shortValue()) {
            accountFlowVo.setAccType(Constants.UNDERLINEINVEST);//业务类型 线下充值
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_12.shortValue()) {
            accountFlowVo.setAccType(Constants.CLAIMREFUND);//业务类型 索赔退款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_13.shortValue()) {
            accountFlowVo.setAccType(Constants.REFUNDOFCLAIM);//业务类型 无货到索赔退款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_14.shortValue()) {
            accountFlowVo.setAccType(Constants.ORDERREFUND);//业务类型 订单退款
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_15.shortValue()) {
            accountFlowVo.setAccType(Constants.RECHARGEREEORCASH);//业务类型 充值错误提现
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_16.shortValue()) {
            accountFlowVo.setAccType(Constants.WITHFRAWALFEEZE);//业务类型 提现冻结
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_17.shortValue()) {
            accountFlowVo.setAccType(Constants.ACCOUNT);//业务类型 账户
        } else if (busiType.shortValue() == Constants.BUSI_TYPE_18.shortValue()) {
            accountFlowVo.setAccType(Constants.REDPACKE);//业务类型 活动红包
        }
        return accountFlowVo;
    }

    @Override
    public void checkPayFlowStatus(String channelFlowIds, Short tradeType, ResponseData responseData) {
        logger.info("check payFlow status param is ----->[channelFlowIds = {}]",channelFlowIds);
        Map<String,Short> statuses;
        if (!channelFlowIds.contains(",")) {
            statuses = payFlowService.checkPayFlowStatus(tradeType,channelFlowIds);
        } else {
            statuses = payFlowService.checkPayFlowStatus(tradeType, channelFlowIds.split(","));
        }
        logger.info("the result for these payFlow status [statuses = {}]", ItvJsonUtil.toJson(statuses));
        if (MapUtils.isEmpty(statuses)){
            responseData.setCode(ResponseStatus.RESULTNULL.getValue());
            responseData.setMsg("get payFlow status result is null");
            return;
        }
        //结果中的三种状态 0代表有,1代表没有
        Short successStatus = statuses.get(SUCCESSSTATUS);
        Short failedStatus = statuses.get(FAILEDSTATUS);
        Short waitStatus = statuses.get(WAITSTATUS);
        Short status = -1;//所有流水的状态  0 成功,1全部失败,2部分失败,3处理中
        if (waitStatus == 0){
            status = 3;
        }else if (failedStatus == 0 && successStatus == 1 && waitStatus == 1){
            status = 1;
        }else if (failedStatus == 1 && successStatus == 0 && waitStatus == 1){
            status = 0;
        }else if (failedStatus == 0 && successStatus == 0 && waitStatus == 1){
            status = 2;
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("get payFlow status ok");
        responseData.setEntity(status);
    }
}
