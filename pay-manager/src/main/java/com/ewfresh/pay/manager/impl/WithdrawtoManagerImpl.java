package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.dao.WithdrawApprRecordDao;
import com.ewfresh.pay.manager.WithdrawtoManager;
import com.ewfresh.pay.model.*;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.Bill99WithdrawAccountVo;
import com.ewfresh.pay.model.vo.WithdrawtosVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.util.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Description:
 * @author DuanXiangming
 * Date 2018/4/13
 */
@Component
public class WithdrawtoManagerImpl implements WithdrawtoManager {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawtoManagerImpl.class);

    @Autowired
    private WithdrawtoService withdrawtoService;
    @Autowired
    private BalanceManagerImpl balanceManager;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private AccountFlowDescUtil accountFlowDescUtil;
    @Autowired
    private WithdrawApprRecordDao withdrawApprRecordDao;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private HATWithdrawManagerImpl hatWithdrawManager;
    @Autowired
    private HttpDeal httpDeal;
    @Autowired
    private MsgClient msgClient;
    private String PERSONALAPPROVALKRY = "PERSONAL_APPROVAL_PASSED";
    //审核不通过
    private String CASHWITHDRAWALFAILEDKEY = "CASH_WITHDRAWAL_AUDIT_FAILED";
    private static final BigDecimal POINT = new BigDecimal("100");

    @Value("${http_msg}")
    private String msgUrl;
    @Value("${http_update_order}")
    private String ORDER_URL;
    @Value("${http_idgen}")
    private String ID_URL;
    @Override
    public void withdrawByUid(ResponseData responseData, Withdrawto withdrawto) throws Exception {
        logger.info("withdraw by uid param in manager is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto));
        Long uid = withdrawto.getUid();
        Integer bankAccountId = withdrawto.getBankAccountId();
        //获取用户余额
        AccountFlowVo accountFlow = balanceManager.checkBalanceIsExist(uid.toString());
        //判断用户是否有余额
        if (accountFlow == null){
            //用户没有余额
            responseData.setCode(ResponseStatus.BALANCEZERO.getValue());
            responseData.setMsg("this user have no banlance");
            responseData.setEntity(Constants.NULL_BALANCE);
            return;
        }
        BigDecimal amount = withdrawto.getAmount();//提现金额
        BigDecimal availableBalance = BalanceManagerImpl.getAvailableBalance(accountFlow);//可用余额
        BigDecimal customerFee = BigDecimal.ZERO;
        if (withdrawto.getAccType().equals(Constants.SHORT_TWO)) {
            String mobilePhone = bankAccountService.getMobilePhoneByid(bankAccountId);
            if (mobilePhone != null){
                withdrawto.setPhone(mobilePhone);
            }else {
                withdrawto.setPhone("");
            }
            //获取手续费
            String uId = uid.toString();
            String queryFee = hatWithdrawManager.getQueryFeeMethod (uId, amount.toString());
            String fee = "0";
            if (StringUtils.isNotBlank(queryFee)) {
                Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(queryFee, new TypeReference<Map<String, String>>() {
                });
                String rspCode = stringStringMap.get("rspCode");
                if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                    fee = stringStringMap.get("fee");
                } else {
                    logger.error("getQueryFeeMethod error, queryFeeContent = " + queryFee);
                    throw new Exception("get withraw query fee is err");
                }
            }
            //冻结余额 = 提现金额 + 手续费
            customerFee = new BigDecimal(fee).divide(POINT);//会员自付手续费
            BigDecimal fzAmount = FormatBigDecimal.formatBigDecimal(amount.add(customerFee));
            amount = fzAmount;
        }
        if (availableBalance.compareTo(amount) == -1 && !customerFee.equals(BigDecimal.ZERO)) {
            //店铺提现包含手续费在内 大于可用余额
            responseData.setCode(ResponseStatus.LACKOFBALANCE.getValue());
            responseData.setMsg(" banlance is not enough， There is some customerFee");
            responseData.setEntity(customerFee);
            return;
        }
        if (availableBalance.compareTo(amount) == -1){
            //可用余额小于提现金额
            responseData.setCode(ResponseStatus.BALANCENOTENOUGH.getValue());
            responseData.setMsg(" banlance is not enough");
            responseData.setEntity(accountFlow);
            return;
        }
        /*初始化资金账户流水 start*/
        accountFlow.setAccType(Constants.ACC_TYPE_4);                   //设置该笔账户资金资金流水的账户类型
        accountFlow.setAmount(amount);                                  //设置该笔账户资金资金流水的金额
        accountFlow.setSrcAcc(Constants.BALANCE);                       //设置源账户
        accountFlow.setTargetAcc(bankAccountId + "");   //设置目标账户
        BigDecimal freezeAmount = accountFlow.getFreezeAmount();
        freezeAmount = freezeAmount.add(amount);
        accountFlow.setFreezeAmount(freezeAmount);
        accountFlow.setBusiType(Constants.BUSI_TYPE_16);
        accountFlow.setDirection(Constants.DIRECTION_OUT);
        //提现账户类型(1个人,2店铺)
        if (withdrawto.getAccType().equals(Constants.SHORT_ONE)) {
            String userStr = accountFlowRedisService.getUserInfo(uid);
            if (StringUtils.isNotBlank(userStr)){
                HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userStr, new HashMap<String, Object>().getClass());
                Object phone = hashMap.get(Constants.SHOW_PHONE);
                if (phone != null){
                    String phoneStr =  String.valueOf(phone);
                    withdrawto.setPhone(phoneStr);
                }else {
                    withdrawto.setPhone("");
                }
            }
        }

        /*初始化资金账户流水 end*/
        //插入申请和冻结余额
        withdrawtoService.addWithdrawtoAndFreezeBalance(accountFlow,withdrawto);
        Long withDrawId = withdrawto.getId();
        //将该次提现使用的银行卡设置为默认银行卡     add by  zhaoqun  2019/4/24
        setDefaultCard(responseData, uid, bankAccountId);
        responseData.setEntity(withDrawId);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("add a withdrawto record success");
    }

    public void setDefaultCard(ResponseData responseData, Long uid, Integer bankAccountId) {
        logger.info("It is now in Bill99QuickManagerImpl.getPayChannelByChannelName, the parameter are: " +
            "[uid = {}, bankAccountId = {}]", uid, bankAccountId);
        // 查询该提现银行卡信息
        BankAccount bankAccount = bankAccountService.getBankByCardCode(uid, bankAccountId);
        if (null == bankAccount) {
            logger.error("Can not find this withdraw cardCode from database, uid = " + uid +
                ", bankAccountId = " + bankAccountId);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 查询当前该客户的默认银行卡
        List<BankAccount> defaultBankByUserId = bankAccountService.getDefaultBankByUid(uid);
        // 如果该客户名下有多张默认银行卡，阻断
        if (null != defaultBankByUserId && defaultBankByUserId.size() > Constants.INTEGER_ONE) {
            logger.error("The user has more than one default withdraw card, uid = " + uid + ", default card " +
                "count = " + defaultBankByUserId.size());
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
        // 如果该客户已经将该唯一一张默认银行卡删除，阻断     存在未设置默认银行卡的旧数据，无法进行此判断
//        if (CollectionUtils.isEmpty(defaultBankByUserId)) {
//            logger.error("The user has set this card as not default card, uid = " + uid + ", bankAccountId = "
//                + bankAccountId);
//            responseData.setCode(ResponseStatus.CARDISNOTDEFAULTANYMORE.getValue());
//            responseData.setMsg(Constants.CARDISNOTDEFAULTANYMORE);// 该卡已被解绑，不能设为默认银行卡
//            return;
//        }
        // 设置快钱默认银行卡：将原默认银行卡解除，将新卡设为默认
        List<BankAccount> bankAccountList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultBankByUserId)){
            BankAccount oldBankAccount = new BankAccount();
            oldBankAccount.setId(defaultBankByUserId.get(Constants.INTEGER_ZERO).getId());// 原快钱默认银行卡
            oldBankAccount.setIsDef(Constants.SHORT_ZERO);// 设置为非默认
            bankAccountList.add(oldBankAccount);
        }
        BankAccount newBankAccount = new BankAccount();// 新的默认银行卡
        newBankAccount.setIsDef(Constants.SHORT_ONE);
        newBankAccount.setId(bankAccountId);
        bankAccountList.add(newBankAccount);
        try {
            bankAccountService.updateIsDefaultById(bankAccountList);
        }catch (Exception e){
            logger.error("modify user default bankAccount is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
            return;
        }
    }

    //获取提现业务单号的方法
    public Long getOrderId() {
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        Long uid = context.getUid();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ID_GEN_KEY, Constants.ID_GEN_PAY_VALUE);
        String idStr = httpDeal.post(ID_URL, map, token,uid + "");
        HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(idStr, new HashMap<String, Object>().getClass());
        return Long.valueOf((Integer) hashMap.get(Constants.ENTITY));
    }

    @Override
    public void getWithdrawByUid(ResponseData responseData, Long uid , Long id ,Integer pageNum ,Integer pageSize) {
        logger.info("get withdraw by uid param in manager is ----->[uid = {},id= {}]", uid, id);

        PageHelper.startPage(pageNum,pageSize);
        List<WithdrawtosVo> withdrawtos = withdrawtoService.getWithdrawByUid(uid, id);
        if(CollectionUtils.isEmpty(withdrawtos)){
            responseData.setCode(ResponseStatus.WITHDRAWTOISNULL.getValue());
            responseData.setMsg("no withdrawto for this user");
            return;
        }
        for (WithdrawtosVo vo : withdrawtos) {
            //beforeStatus
            WithdrawApprRecord record = withdrawApprRecordDao.getWithdrawApprRecord(vo.getId());
            logger.info("WithdrawApprRecord record is {}" + ItvJsonUtil.toJson(record));
            if (record == null) {
                continue;
            }
            vo.setBeforeStatus(record.getBeforeStatus());
            vo.setRemark(record.getDesp()
            );
        }
        PageInfo<WithdrawtosVo> pageInfo = new PageInfo<>(withdrawtos);
        responseData.setMsg("get withdraw by uid success");
        setResponseData(pageInfo,responseData);
    }

    @Override
    public void checkWithdraw(ResponseData responseData, Withdrawto withdrawto, PayFlow payFlow, WithdrawApprRecord withdrawApprRecord) throws Exception {
        logger.info("check withdrawparam in manager is ----->[ withdrawto = {}, payFlow = {} ]", ItvJsonUtil.toJson(withdrawto) , ItvJsonUtil.toJson(payFlow));
        short apprStatus = withdrawto.getApprStatus();
        short beforeStatus = withdrawto.getBeforeStatus();
        //最终审核通过  且是商户提现  向快钱发起提现申请
        WithdrawtosVo item = withdrawtoService.getWithdrawByid(withdrawto.getId());
        if (apprStatus == Constants.APPR_STATUS_4 && Constants.SHORT_TWO.equals(item.getAccType())) {
            Bill99WithdrawAccountVo vo = new Bill99WithdrawAccountVo();
            String wId = String.valueOf(withdrawto.getId());
            String uId = String.valueOf(item.getUid());
            String amount = String.valueOf(item.getAmount());
            String bankAcctId = String.valueOf(item.getBankAccount().getCardCode());
            vo.setWithdrawId(wId);//提现ID
            vo.setuId(uId);//shopId
            vo.setAmount(amount);//金额
            vo.setBankAcctId(bankAcctId);//银行卡
            responseData = hatWithdrawManager.accountWithdraw(responseData, vo);
            return;
        }
        AccountFlowVo accountFlow = null;
        //审核不通过
        boolean flag = (apprStatus == Constants.APPR_STATUS_5.shortValue() && beforeStatus != Constants.APPR_STATUS_3.shortValue());
        logger.info("check status flag [ flag = {}]",flag);
        if(flag){
            WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(withdrawto.getId());
            accountFlow = getAccountFlow(withdrawByid);
            logger.info("the new accountFLow [accountFlow = {}]", ItvJsonUtil.toJson(accountFlow));
            if (withdrawByid != null) {
                Short accType = withdrawByid.getAccType();
                //个人审核不通过
                if (accType == Constants.SHORT_ONE) {
                    Long uid = withdrawByid.getUid();
                    //从redis中获取电话号码
                    String userInfo = accountFlowRedisService.getUserInfo(uid);
                    //反序列化
                    HashMap hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                    String showPhone = (String) hashMap.get("showPhone");
                    msgClient.postMsg(msgUrl, showPhone, uid.toString(), CASHWITHDRAWALFAILEDKEY, CASHWITHDRAWALFAILEDKEY);
                }
                //店铺审核不通过
                if (accType == Constants.SHORT_TWO) {
                    Long uid = withdrawByid.getUid();
                    Integer bankAccountId = withdrawByid.getBankAccountId();
                    String mobilePhone = bankAccountService.getMobilePhoneByid(bankAccountId);
                    if (mobilePhone != null) {
                        msgClient.postMsg(msgUrl, mobilePhone, uid.toString(), CASHWITHDRAWALFAILEDKEY, CASHWITHDRAWALFAILEDKEY);
                        logger.info("check withdrawparams success");
                    }
                }
            }
        }
        //审核通过
        WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(withdrawto.getId());
        if (apprStatus == Constants.APPR_STATUS_4 && Constants.SHORT_ONE.equals(withdrawByid.getAccType())) {
            Long uid = withdrawByid.getUid();
            logger.info("uid=" + uid);
            //从redis中获取电话号码
            String userInfo = accountFlowRedisService.getUserInfo(uid);
            logger.info("userInfo =" + userInfo);
            //反序列化
            HashMap hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
            String showPhone = (String) hashMap.get("showPhone");
            logger.info("showPhone =" + showPhone);
            msgClient.postMsg(msgUrl, showPhone, uid.toString(), PERSONALAPPROVALKRY, PERSONALAPPROVALKRY);
        }
        if (payFlow == null || payFlow.getChannelFlowId() == null){
            withdrawtoService.updateApprStatus(withdrawto , null , withdrawApprRecord, accountFlow);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg("check withdrawparam success");
            return;
        }
        if (payFlow == null || payFlow.getChannelFlowId() == null || payFlow.getChannelCode() == null || payFlow.getPayerPayAmount() == null){
            responseData.setCode(ResponseStatus.PAYFLOWPARAMNULL.getValue());
            responseData.setMsg(" payFlow param is null");
            return;
        }
        withdrawtoService.updateApprStatus(withdrawto , payFlow, withdrawApprRecord,accountFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("check withdrawparam and add a payFlow success");
    }

    public static void main(String[] args) {
        short s = 5;
        System.out.println(s == Constants.APPR_STATUS_5);
    }

    @Override
    public void getWithdrawtos(ResponseData responseData, String uname, String nickName, Short status, Short beforeStatus, String startTime, String endTime, Integer pageNum, Integer pageSize) {
        logger.info("get withdrawtos  param is ----->[uname = {}, nickName = {}, status = {} ,  beforeStatus = {},startTime = {} , endTime = {}, pageNum = {}, pageSize = {}]", uname, nickName, status, beforeStatus, startTime,endTime, pageNum, pageSize);
        PageHelper.startPage(pageNum,pageSize);
        List<WithdrawtosVo> withdrawtos = withdrawtoService.getWithdraws(uname,nickName,status,beforeStatus,startTime,endTime);
        //add by zhaoqun  2018/9/19   ATRT
        for (WithdrawtosVo vo : withdrawtos) {
            String userId = String.valueOf(vo.getUid());
            AccountFlowVo accountFlowVo = accountFlowRedisService.getAccountFlowByUid(userId);//获取该用户最新账户流水
            BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();//锁定金额
            vo.setFreezeAmount(freezeAmount);
        }
        //add by zhaoqun  2018/9/19   END
        PageInfo<WithdrawtosVo> pageInfo = new PageInfo<>(withdrawtos);
        responseData.setMsg("get withdraws  success");
        setResponseData(pageInfo,responseData);
    }

    @Override
    public void getWithdrawByid(ResponseData responseData, Long id) {
        logger.info("get withdraw by id in manager param is ----->[id = {}]", id );
        WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(id);
        List<WithdrawApprRecord> recordList = withdrawApprRecordDao.getWdApprRecordList(id);
        if (!recordList.isEmpty()) {
            withdrawByid.setWithdrawApprRecord(recordList);
        }
        //add by zhaoqun  2018/9/19   ATRT
        String userId = String.valueOf(withdrawByid.getUid());
        AccountFlowVo accountFlowVo = accountFlowRedisService.getAccountFlowByUid(userId);//获取该用户最新账户流水
        BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();//锁定金额
        withdrawByid.setFreezeAmount(freezeAmount);
        //add by zhaoqun  2018/9/19   END
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("get withdraw by id success");
        responseData.setEntity(withdrawByid);
    }

    private void  setResponseData(PageInfo pageInfo , ResponseData responseData){
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setEntity(pageInfo.getList());
        responseData.setCurPage(pageInfo.getPageNum());
        responseData.setTotal(pageInfo.getTotal() + "");
        responseData.setPageCount(pageInfo.getPages());
    }

    @Override
    public void cancelWithdrawByid(ResponseData responseData, Withdrawto withdrawto, Short isSelf) throws Exception {
        logger.info("cancel withdraw by id in manager param is ----->[withdrawto = {}]", ItvJsonUtil.toJson(withdrawto) );

        WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(withdrawto.getId());
        short apprStatus = withdrawByid.getApprStatus();
        if (isSelf.shortValue() == Constants.SHORT_ONE.shortValue() ){
            //财务取消
            if (apprStatus != Constants.APPR_STATUS_0) {
                logger.info("withdrawto is not allow cancle now");
                return;
            }
        } else {
            if (apprStatus != Constants.APPR_STATUS_0){
                //如果审核状态为已通过,则无法取消
                logger.info("withdrawto is not allow cancle now");
                responseData.setCode(ResponseStatus.WITHDRAWTOALREADYALLOW.getValue());
                responseData.setMsg("withdrawto already allow");
                return;
            }
        }
        withdrawto.setApprStatus(Constants.APPR_STATUS_7);
        withdrawto.setCancelTime(new Date());
        AccountFlowVo accountFlow = getAccountFlow(withdrawByid);
        if (accountFlow == null){
            responseData.setCode(ResponseStatus.WITHDRAWTOAMOUNTERR.getValue());
            responseData.setMsg("cancel withdraw by id amount is err" );
            return;
        }
        withdrawtoService.cancelWithdrawByid(withdrawto, accountFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("cancel withdraw by id success");
    }

    /**
     * Description: 审核用户提现的方法（出纳审核不通过时，总裁确认不通过）
     * @author zhaoqun
     * @param  withdrawto     封装提现内容的对象
     * Date    2018/11/11
     */
    @Override
    public void checkWithdrawNotAllow(ResponseData responseData, Withdrawto withdrawto) throws Exception {
        AccountFlow accountFlow = null;
        WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(withdrawto.getId());
        accountFlow = getAccountFlow(withdrawByid);
        logger.info("the new accountFLow [accountFlow = {}]", ItvJsonUtil.toJson(accountFlow));
        withdrawtoService.checkWithdrawNotAllow(withdrawto,accountFlow);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg("check withdraw not allow by id success");
    }

    @Override
    public Withdrawto getWithdrawtoInfoById(Long id) {
        return withdrawtoService.getWithdrawtoInfoById(id);
    }

    //根据提现记录生成一条账户资金流水
    public AccountFlowVo getAccountFlow(WithdrawtosVo withdrawto) throws Exception {
        String userId = withdrawto.getUid().toString();
        AccountFlowVo accountFlowVo = balanceManager.checkBalanceIsExist(userId);
        BigDecimal amount = withdrawto.getAmount();
        if (withdrawto.getAccType().equals(Constants.SHORT_TWO)) {
            //店铺提现解除冻结余额  需要考虑手续费
            BigDecimal customerFee = getCustomerFee(userId, amount.toString());
            amount = FormatBigDecimal.formatBigDecimal(amount.add(customerFee));
        }
        accountFlowVo.setAmount(amount);
        BigDecimal freezeAmount = accountFlowVo.getFreezeAmount();//该用户的最新余额
        int freezeFlag = freezeAmount.compareTo(amount);//比较提现金额和冻结金额的大小
        if (freezeFlag == -1){
            //冻结金额小于提现金额
            return null;
        }
        BigDecimal balance = accountFlowVo.getBalance();
        int balanceFlag = balance.compareTo(amount);
        if (balanceFlag == -1){
            //余额金额小于提现金额
            return null;
        }
        BigDecimal newFreeze = freezeAmount.subtract(amount);//新的冻结金额
        accountFlowVo.setFreezeAmount(newFreeze);
        accountFlowVo.setTargetAcc(Constants.BALANCE);
        accountFlowVo.setSrcAcc(Constants.FROZEN_AMOUNT);
        accountFlowVo.setBusiType(Constants.BUSI_TYPE_8);
        accountFlowVo.setBusiNo(withdrawto.getId().toString());
        accountFlowVo.setAccType(Constants.ACC_TYPE_4);
        accountFlowVo.setOperator(withdrawto.getCancelId() + "");
        accountFlowDescUtil.cassExplainByAcc(accountFlowVo);
        return accountFlowVo;

    }

    private BigDecimal getCustomerFee(String uId, String amount) throws Exception {
        //获取手续费
        String queryFee = hatWithdrawManager.getQueryFeeMethod (uId, amount);
        String fee = "0";
        if (StringUtils.isNotBlank(queryFee)) {
            Map<String, String> stringStringMap = ItvJsonUtil.jsonToObj(queryFee, new TypeReference<Map<String, String>>() {
            });
            String rspCode = stringStringMap.get("rspCode");
            if (rspCode.equals(Constants.HAT_WITHDRAWCODE_0000)) {
                fee = stringStringMap.get("fee");
            } else {
                logger.error("getQueryFeeMethod error, queryFeeContent = " + queryFee);
                throw new Exception("get withraw query fee is err");
            }
        }
        //冻结余额 = 提现金额 + 手续费
        BigDecimal customerFee = new BigDecimal(fee).divide(POINT);//会员自付手续费
        return customerFee;
    }
}
