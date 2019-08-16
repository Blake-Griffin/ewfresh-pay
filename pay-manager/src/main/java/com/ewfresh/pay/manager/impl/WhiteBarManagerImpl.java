package com.ewfresh.pay.manager.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.client.HttpDeal;
import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.commons.util.request.header.RequestHeaderAccessor;
import com.ewfresh.commons.util.request.header.RequestHeaderContext;
import com.ewfresh.pay.manager.WhiteBarManager;
import com.ewfresh.pay.manager.handler.BalanceAndBarLock;
import com.ewfresh.pay.model.AdjustRecord;
import com.ewfresh.pay.model.Emp;
import com.ewfresh.pay.model.WhiteBar;
import com.ewfresh.pay.model.vo.MonthlySales;
import com.ewfresh.pay.model.vo.WhiteBarVo;
import com.ewfresh.pay.model.vo.WhiteBarVoOne;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.AdjustRecordService;
import com.ewfresh.pay.service.BarDealFlowService;
import com.ewfresh.pay.service.WhiteBarService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.DateUtil;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author LouZiFeng
 * @Description 查询白条额度
 * @Date: 2019/3/11
 */
@Component
public class WhiteBarManagerImpl implements WhiteBarManager {

    @Autowired
    private WhiteBarService whiteBarService;
    @Autowired
    private BarDealFlowService barDealFlowService;
    @Autowired
    private BalanceAndBarLock balanceAndBarLock;
    @Autowired
    private AdjustRecordService adjustRecordService;
    @Autowired
    private AccountFlowRedisService accountFlowRedisService;
    @Autowired
    private HttpDeal httpDeal;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final Short ZERO = 0;
    private static final Short TWO = 2;
    private static final Short THREE = 3;
    private static final Integer TEN = 10;
    private static final String ID_INTRODUCER = "idIntroducter";
    private static final String REAL_NAME = "realName";
    BigDecimal bigDecimal = new BigDecimal(0.00);
    BigDecimal bigDecimalOne = new BigDecimal(1000000.00);
    private static final Short INTEGER_ZERO = 0;
    private static final Short INTEGER_ONE = 1;
    private final static String CODESUCCESS = "000";
    private final static String CODEISEMPTY = "628";
    @Value("${monthlySalesUidURL}")
    private String monthlySalesUid;
    @Value("${NDate}")
    private String NDate;
    //申请额度
    private String QUOTAAPPLICATION = "QUOTA_APPLICATION";
    //审核通过
    private String QUOTAAPPROVED = "QUOTA_APPROVED";
    //审核不通过
    private String QUOTAREVIEWFAILED = "QUOTA_REVIEW_FAILED";

    @Value("${http_msg}")
    private String msgUrl;
    BigDecimal Thirty = new BigDecimal(0.30);
    BigDecimal TwentyFive = new BigDecimal(0.25);
    BigDecimal ThreeFive = new BigDecimal(0.35);
    private final static BigDecimal ZER = new BigDecimal(Constants.NULL_BALANCE_STR);
    @Autowired
    private MsgClient msgClient;

    @Override
    public ResponseData getWhiteBarList(ResponseData responseData, String uname, Short apprStatus, String start, String end, Integer pageNumber, Integer pageSize) {
        PageInfo<WhiteBarVo> whiteBarPageInfo = whiteBarService.getWhiteBarList(uname, apprStatus, start, end, pageNumber, pageSize);
        List<WhiteBarVo> list = whiteBarPageInfo.getList();
        if (CollectionUtils.isNotEmpty(list)) {
            for (WhiteBarVo whiteBar : list) {
                String ids = getUser(whiteBar.getUid());
                if (StringUtils.isNotEmpty(ids)) {
                    Emp emp = accountFlowRedisService.getIntroducterInfo(Long.valueOf(ids));
                    if (emp != null) {
                        String empStr = emp.getRealName() + emp.getPhone();
                        whiteBar.setIntroducer(empStr);
                    }
                }
                String realName = getUserName(whiteBar.getUid());
                whiteBar.setRealName(realName);
            }
        }
        responseData.setEntity(list);
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(whiteBarPageInfo.getPages());
        responseData.setTotal(Long.toString(whiteBarPageInfo.getTotal()));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarById(ResponseData responseData, Integer recordId) {
        WhiteBarVo whiteBar = whiteBarService.getWhiteBarById(recordId);
        if (whiteBar != null) {
            String ids = getUser(whiteBar.getUid());
            if (StringUtils.isNotEmpty(ids)) {
                Emp emp = accountFlowRedisService.getIntroducterInfo(Long.valueOf(ids));
                if (emp != null) {
                    String empStr = emp.getRealName() + emp.getPhone();
                    whiteBar.setIntroducer(empStr);
                }
            }
        }
        responseData.setEntity(whiteBar);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData updateApprStatus(ResponseData responseData, Integer id, Short apprStatus, String reason) {
        whiteBarService.updateApprStatus(id, apprStatus, reason);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData addWhiteBar(ResponseData responseData, WhiteBar whiteBar) {
        Long uid = whiteBar.getUid();
        BigDecimal adjustLimit = whiteBar.getAdjustLimit();
        WhiteBar whiteBarByUid = whiteBarService.getWhiteBarByUid(uid);
        if (whiteBarByUid == null) {
            whiteBar.setTotalLimit(bigDecimal);
            whiteBar.setUseStatus(ZERO);
            whiteBar.setApprStatus(ZERO);
            whiteBar.setPeriod(TEN);
            AdjustRecord adjustRecord = new AdjustRecord();
            adjustRecord.setAdjustAmount(adjustLimit);
            adjustRecord.setType(INTEGER_ZERO);
            adjustRecord.setApprStatus(ZERO);
            whiteBarService.addWhiteBarAndRecord(whiteBar, adjustRecord);
            //额度申请发短信     从redis中获取电话号码
            getUserPhone(uid.longValue(), QUOTAAPPLICATION, QUOTAAPPLICATION);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return responseData;
        }
        Short apprStatus = whiteBarByUid.getApprStatus();
        Short useStatus = whiteBarByUid.getUseStatus();
        if (apprStatus != TWO || useStatus != ZERO) {
            responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
            responseData.setMsg(ResponseStatus.REPEATINDATA.name());
            return responseData;
        }
        if (apprStatus == TWO && useStatus == ZERO) {
            Integer barId = whiteBarByUid.getId();
            AdjustRecord adjustRecord = new AdjustRecord();
            adjustRecord.setBarId(barId);
            adjustRecord.setAdjustAmount(adjustLimit);
            adjustRecord.setType(INTEGER_ZERO);
            adjustRecord.setApprStatus(ZERO);
            whiteBarByUid.setAdjustLimit(adjustLimit);
            whiteBarByUid.setApprStatus(ZERO);
            whiteBarService.upApprStatus(whiteBarByUid, adjustRecord);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        }
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarByUid(ResponseData responseData, Long uid) {
        WhiteBar whiteBar = whiteBarService.getWhiteBarByUid(uid);
        responseData.setEntity(whiteBar);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarName(ResponseData responseData, Integer id, String uname, Short useStatus, Integer period, Integer pageNumber, Integer pageSize) {
        PageInfo<WhiteBarVoOne> whiteBarVoOnePageInfo = whiteBarService.getWhiteBarName(id, uname, useStatus, period, pageNumber, pageSize);
        List<WhiteBarVoOne> list = whiteBarVoOnePageInfo.getList();
        for (WhiteBarVoOne whiteBar : list) {
            WhiteBarVo whiteBarVo = null;
            BigDecimal whiteBalance = ZER;//白条余额
            try {
                //根据uiserId获取白条
                whiteBarVo = whiteBarService.getWhiteBarVoByUid(whiteBar.getUid());
                //判断用户是否已开通白条
                if (whiteBarVo == null || whiteBarVo.getTotalLimit().compareTo(ZER) == 0) {
                    //用户没有白条
                    responseData.setCode(ResponseStatus.NOWHITE.getValue());
                    responseData.setMsg(ResponseStatus.NOWHITE.name());
                    responseData.setEntity(ZER);
                }
                //用户已开通白条
                //获取用户白条已使用额度
                BigDecimal usedLimit = barDealFlowService.getUsedLimitByUid(whiteBar.getUid());
                if (usedLimit == null) {
                    usedLimit = ZER;
                }
                whiteBarVo.setUsedLimit(usedLimit);
                BigDecimal totalLimit = whiteBarVo.getTotalLimit();//白条总额度
                //计算可用白条余额
                whiteBalance = totalLimit.subtract(usedLimit);
                if (whiteBalance.compareTo(ZER) < 0) {
                    whiteBalance = ZER;
                }
            } catch (Exception e) {
                logger.error(" get banlance cache by uid err [uid = {}]", e);
            }
            String ids = getUser(whiteBar.getUid());
            if (StringUtils.isNotEmpty(ids)) {
                Emp emp = accountFlowRedisService.getIntroducterInfo(Long.valueOf(ids));
                if (emp != null) {
                    String empStr = emp.getRealName() + emp.getPhone();
                    whiteBar.setIntroducer(empStr);
                }
            }
            String realName = getUserName(whiteBar.getUid());
            whiteBar.setRealName(realName);
            whiteBar.setTotalSum(whiteBalance);
        }
        responseData.setEntity(whiteBarVoOnePageInfo);
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(whiteBarVoOnePageInfo.getPages());
        responseData.setTotal(Long.toString(whiteBarVoOnePageInfo.getTotal()));
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarByAdjustLimit(ResponseData responseData, Integer barId) {
        WhiteBarVo whiteBarVoOne = whiteBarService.getWhiteBarByAdjustLimit(barId);
        responseData.setEntity(whiteBarVoOne);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData updateUseStatus(ResponseData responseData, Integer id, Short useStatus) {
        whiteBarService.updateUseStatus(id, useStatus);
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }


    @Override
    public ResponseData updateWhiteBarByUid(ResponseData responseData, Integer recordId, WhiteBar whiteBar) {
        AdjustRecord whiteBarById = adjustRecordService.getadjustRecordById(recordId);
        if (whiteBarById == null) {
            return null;
        }
        Integer barId1 = whiteBarById.getBarId();
        WhiteBar whiteBarServiceById = whiteBarService.getById(barId1);
        if (whiteBarServiceById == null) {
            return null;
        }
        Long uid1 = whiteBarServiceById.getUid();
        Short useStatus = whiteBarServiceById.getUseStatus();
        BigDecimal totalLimit = whiteBarServiceById.getTotalLimit();
        Short apprStatus = whiteBar.getApprStatus();
        BigDecimal adjustLimit = whiteBar.getAdjustLimit();
        String reason = whiteBar.getReason();
        Integer period = whiteBar.getPeriod();
        AdjustRecord adjustRecord = new AdjustRecord();
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        Long uid = context.getUid();
        if (StringUtils.isEmpty(uid.toString())) {
            return null;
        }
        boolean balanceAndBarLock = this.balanceAndBarLock.getBalanceAndBarLock(uid1.toString());
        if (!balanceAndBarLock) {
            logger.info("to by lock [balanceAndBarLock = {}]", balanceAndBarLock);
            responseData.setMsg("commit request again");
            responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
            return responseData;
        }
        if (apprStatus == TWO) {
            adjustRecord.setId(recordId);
            adjustRecord.setApprStatus(apprStatus);
            whiteBar.setId(barId1);
            whiteBar.setReason(reason);
            whiteBar.setApprStatus(apprStatus);
            adjustRecord.setAppr(uid.intValue());
            whiteBarService.updateWhiteBarById(whiteBar, adjustRecord);
            getUserPhone(uid1.longValue(), QUOTAREVIEWFAILED, QUOTAREVIEWFAILED);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return responseData;
        }
        if (adjustLimit.compareTo(bigDecimalOne) == 1) {
            responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
            responseData.setMsg(ResponseStatus.REPEATINDATA.name());
            return responseData;
        }
        try {
            if (useStatus == INTEGER_ZERO && apprStatus == INTEGER_ONE && adjustLimit.compareTo(bigDecimal) != -1) {
                //第一次审核
                whiteBar.setUseStatus(INTEGER_ONE);
                whiteBar.setTotalLimit(adjustLimit);
                whiteBar.setAdjustLimit(bigDecimal);
                whiteBar.setId(barId1);
                whiteBar.setPeriod(period);
                whiteBar.setInitialLimit(adjustLimit);
                whiteBar.setApprStatus(apprStatus);
                adjustRecord.setId(recordId);
                adjustRecord.setAdjustAmount(adjustLimit);
                adjustRecord.setQuotaLimit(totalLimit);
                adjustRecord.setApprStatus(apprStatus);
                adjustRecord.setAppr(uid.intValue());
                whiteBarService.updateWhiteBarById(whiteBar, adjustRecord);
                //额度申请通过发短信     从redis中获取电话号码
                String content = String.valueOf(adjustLimit);
                getUserPhone(uid1.longValue(), content, QUOTAAPPROVED);
            }
            if (useStatus == INTEGER_ONE && apprStatus == INTEGER_ONE && adjustLimit.compareTo(bigDecimal) != -1) {
                if (adjustLimit.compareTo(totalLimit) != -1) {
                    //第二次审核
                    whiteBar.setUseStatus(INTEGER_ONE);
                    whiteBar.setTotalLimit(adjustLimit);
                    whiteBar.setAdjustLimit(bigDecimal);
                    whiteBar.setId(barId1);
                    whiteBar.setPeriod(period);
                    whiteBar.setApprStatus(apprStatus);
                    adjustRecord.setId(recordId);
                    adjustRecord.setAdjustAmount(adjustLimit);
                    adjustRecord.setApprStatus(apprStatus);
                    adjustRecord.setType(INTEGER_ONE);
                    adjustRecord.setQuotaLimit(totalLimit);
                    adjustRecord.setAppr(uid.intValue());
                    whiteBarService.updateWhiteBarById(whiteBar, adjustRecord);
                }
                if (adjustLimit.compareTo(totalLimit) == -1) {
                    whiteBar.setUseStatus(INTEGER_ONE);
                    whiteBar.setTotalLimit(adjustLimit);
                    whiteBar.setAdjustLimit(bigDecimal);
                    whiteBar.setId(barId1);
                    whiteBar.setPeriod(period);
                    whiteBar.setApprStatus(apprStatus);
                    adjustRecord.setId(recordId);
                    adjustRecord.setAdjustAmount(adjustLimit);
                    adjustRecord.setApprStatus(apprStatus);
                    adjustRecord.setType(TWO);
                    adjustRecord.setQuotaLimit(totalLimit);
                    adjustRecord.setAppr(uid.intValue());
                    whiteBarService.updateWhiteBarById(whiteBar, adjustRecord);
                }
                String content = String.valueOf(adjustLimit);
                getUserPhone(uid1.longValue(), content, QUOTAAPPROVED);
            }
            this.balanceAndBarLock.releaseLock(uid1.toString());
        } catch (Exception e) {
            e.printStackTrace();
            this.balanceAndBarLock.releaseLock(uid1.toString());
        }
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarByMonth(ResponseData responseData, WhiteBar whiteBar) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String times = sdf.format(calendar.getTime()); //上月
        Long uid = whiteBar.getUid();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", String.valueOf(uid));
        paramMap.put("times", times);
        RequestHeaderAccessor accessor = RequestHeaderAccessor.getInstance();
        RequestHeaderContext context = accessor.getCurrentRequestContext();
        String token = context.getToken();
        logger.info("token =" + token);
        Long uidOne = context.getUid();
        logger.info("uid =" + uidOne);
        String post = httpDeal.post(monthlySalesUid, paramMap, token, uidOne + "");
        Map<String, String> map = ItvJsonUtil.jsonToObj(post, new TypeReference<Map<String, String>>() {
        });
        String code = map.get("code");
        String entity = map.get("entity");
        if (code.equals(CODESUCCESS) || code.equals(CODEISEMPTY)) {
            MonthlySales monthlySales = ItvJsonUtil.jsonToObj(entity, new TypeReference<MonthlySales>() {
            });
            if (monthlySales == null) {
                WhiteBar whiteBarOne = whiteBarService.getWhiteBarByUid(uid);
                responseData.setEntity(whiteBarOne);
                responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
                responseData.setMsg(ResponseStatus.REPEATINDATA.name());
                return responseData;
            }
            BigDecimal selfMonthlySales = monthlySales.getSelfMonthlySales();//上个月的额度
            BigDecimal thirty = selfMonthlySales.multiply(Thirty).setScale(BigDecimal.ROUND_HALF_UP, 0);
            BigDecimal twentyFive = selfMonthlySales.multiply(TwentyFive).setScale(BigDecimal.ROUND_HALF_UP, 0);
            BigDecimal threeFive = selfMonthlySales.multiply(ThreeFive).setScale(BigDecimal.ROUND_HALF_UP, 0);
            monthlySales.setTotalSum(thirty);
            monthlySales.setTwentyFive(twentyFive);
            monthlySales.setThreeFive(threeFive);
            WhiteBar whiteBarOne = whiteBarService.getWhiteBarByUid(uid);
            logger.info("whiteBarOne" + ItvJsonUtil.toJson(whiteBarOne));
            if (whiteBarOne != null) {
                BigDecimal totalLimit = whiteBarOne.getTotalLimit();//当前总额度
                Integer barId = whiteBarOne.getId();
                monthlySales.setBarId(barId);
                monthlySales.setTotalLimit(totalLimit);
            }
            responseData.setEntity(monthlySales);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
        }
        return responseData;
    }

    @Override
    public ResponseData getWhiteBarMonthTotal(ResponseData responseData, Long uid) {
        Date date = new Date();
        Date futureMountDays = DateUtil.getFutureMountDays(date, Integer.valueOf(NDate));
        WhiteBar whiteBarByUid = whiteBarService.getWhiteBarByUid(uid);
        if (whiteBarByUid != null) {
            Short useStatus = whiteBarByUid.getUseStatus();
            if (useStatus == TWO) {
                responseData.setEntity(whiteBarByUid);
                responseData.setCode(ResponseStatus.WHITEFREEZE.getValue());
                responseData.setMsg(ResponseStatus.WHITEFREEZE.name());
                return responseData;
            } else if (useStatus == THREE) {
                responseData.setEntity(whiteBarByUid);
                responseData.setCode(ResponseStatus.SHOUDREPAY.getValue());
                responseData.setMsg(ResponseStatus.SHOUDREPAY.name());
                return responseData;
            }
        }
        Integer barId = whiteBarByUid.getId();
        WhiteBarVoOne whiteBarVos = whiteBarService.getWhiteBarMonthTotal(barId);
        if (whiteBarVos == null) {
            responseData.setEntity(whiteBarVos);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return responseData;
        }
        Date adjustTime = whiteBarVos.getAdjustTime();
        Short type = whiteBarVos.getType();
        if (futureMountDays.getTime() >= adjustTime.getTime()) {
            logger.info("adjustTime=" + adjustTime);
            responseData.setEntity(whiteBarVos);
            responseData.setCode(ResponseStatus.OK.getValue());
            responseData.setMsg(ResponseStatus.OK.name());
            return responseData;
        }
        if (futureMountDays.getTime() < adjustTime.getTime()) {
            logger.info("futureMountDays=" + futureMountDays);
            if (type == ZERO) {
                responseData.setEntity(whiteBarVos);
                responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
                responseData.setMsg(ResponseStatus.REPEATINDATA.name());
                return responseData;
            }
            if (type != ZERO) {
                responseData.setEntity(whiteBarVos);
                responseData.setCode(ResponseStatus.SCHEDULEDAT.getValue());
                responseData.setMsg(ResponseStatus.SCHEDULEDAT.name());
                return responseData;
            }
            responseData.setEntity(whiteBarVos);
            responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
            responseData.setMsg(ResponseStatus.REPEATINDATA.name());
            return responseData;
        }
        return responseData;
    }

    @Override
    public ResponseData addAdjustRecord(ResponseData responseData, Integer barId, BigDecimal adjustLimit) {
        WhiteBar whiteBar = whiteBarService.getById(barId);
        if (whiteBar != null) {
            Date date = new Date();
            Date futureMountDays = DateUtil.getFutureMountDays(date, Integer.valueOf(NDate));
            WhiteBarVoOne whiteBarVos = whiteBarService.getWhiteBarMonthTotal(barId);
            if (whiteBarVos == null) {
                responseData.setEntity(whiteBarVos);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
                return responseData;
            }
            Date adjustTime = whiteBarVos.getAdjustTime();
            Short type = whiteBarVos.getType();
            BigDecimal totalLimit = whiteBar.getTotalLimit();
            if (futureMountDays.getTime() >= adjustTime.getTime()) {
                AdjustRecord adjustRecord = new AdjustRecord();
                adjustRecord.setBarId(barId);
                adjustRecord.setAdjustAmount(adjustLimit);
                adjustRecord.setQuotaLimit(totalLimit);
                adjustRecord.setType(INTEGER_ONE);
                adjustRecord.setApprStatus(INTEGER_ZERO);
                whiteBar.setAdjustLimit(adjustLimit);
                whiteBar.setApprStatus(INTEGER_ZERO);
                whiteBarService.adjustRecord(adjustRecord, whiteBar);
                logger.info("adjustTime=" + adjustTime);
                responseData.setEntity(whiteBarVos);
                responseData.setCode(ResponseStatus.OK.getValue());
                responseData.setMsg(ResponseStatus.OK.name());
                return responseData;
            }
            if (futureMountDays.getTime() < adjustTime.getTime()) {
                logger.info("futureMountDays=" + futureMountDays);
                if (type == ZERO) {
                    responseData.setEntity(whiteBarVos);
                    responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
                    responseData.setMsg(ResponseStatus.REPEATINDATA.name());
                    return responseData;
                }
                if (type != ZERO) {
                    responseData.setEntity(whiteBarVos);
                    responseData.setCode(ResponseStatus.SCHEDULEDAT.getValue());
                    responseData.setMsg(ResponseStatus.SCHEDULEDAT.name());
                    return responseData;
                }
                responseData.setEntity(whiteBarVos);
                responseData.setCode(ResponseStatus.REPEATINDATA.getValue());
                responseData.setMsg(ResponseStatus.REPEATINDATA.name());
                return responseData;
            }
        }
        return responseData;
    }

    /**
     * Description: 从redis获取用户信息
     *
     * @Param userId
     * @Date: 2019/3/15
     */
    private String getUser(Long userId) {
        //从redis获取用户信息
        String userStr = null;
        String introducer = null;
        try {
            userStr = accountFlowRedisService.getUserInfo(userId);
        } catch (Exception e) {
            logger.info(" failed to get user info uid = " + userId, e);
        }
        if (StringUtils.isNotBlank(userStr)) {
            HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userStr, new HashMap<String, Object>().getClass());
            Object idOne = hashMap.get(ID_INTRODUCER);
            introducer = String.valueOf(idOne);
        }
        return introducer;
    }

    private String getUserName(Long userId) {
        //从redis获取用户信息
        String userStr = null;
        String realName = null;
        try {
            userStr = accountFlowRedisService.getUserInfo(userId);
        } catch (Exception e) {
            logger.info(" failed to get user info uid = " + userId, e);
        }
        if (StringUtils.isNotBlank(userStr)) {
            HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userStr, new HashMap<String, Object>().getClass());
            Object realNameOne = hashMap.get(REAL_NAME);
            realName = String.valueOf(realNameOne);
        }
        return realName;
    }

    private void getUserPhone(Long uid, String content, String key) {
        //从redis获取用户信息
        String userStr = null;
        try {
            userStr = accountFlowRedisService.getUserInfo(uid);
        } catch (Exception e) {
            logger.info(" failed to get user info uid = " + uid, e);
        }
        if (StringUtils.isNotBlank(userStr)) {
            try {
                HashMap hashMap = ItvJsonUtil.jsonToObj(userStr, new HashMap<String, Object>().getClass());
                String showPhone = (String) hashMap.get("showPhone");
                msgClient.postMsg(msgUrl, showPhone, uid.toString(), content, key);
            } catch (Exception e) {
                logger.info("Exceptional text messaging", e);
            }
        }
    }
}
