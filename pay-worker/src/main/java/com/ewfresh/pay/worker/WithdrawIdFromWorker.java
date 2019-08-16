package com.ewfresh.pay.worker;

import com.ewfresh.commons.client.MsgClient;
import com.ewfresh.pay.manager.impl.WithdrawtoManagerImpl;
import com.ewfresh.pay.model.vo.WithdrawtosVo;
import com.ewfresh.pay.redisService.Bill99OrderRedisService;
import com.ewfresh.pay.service.BankAccountService;
import com.ewfresh.pay.service.WithdrawtoService;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description: 提现已完成发短信
 *
 * @author: LouZiFeng
 * date: 2018/10/19
 */
@Component
public class WithdrawIdFromWorker {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawtoManagerImpl.class);

    @Autowired
    private WithdrawtoService withdrawtoService;
    @Autowired
    private Bill99OrderRedisService bill99OrderRedisService;
    @Autowired
    private BankAccountService bankAccountService;
    private String CASHWITHDRAWALAUDITKEY = "CASH_WITHDRAWAL_AUDIT_HAS_BEEN_COMPLTED";

    @Value("${http_msg}")
    private String msgUrl;
    @Autowired
    private MsgClient msgClient;
    @Scheduled(cron = "20 0/1 * * * ?")
    public void WithdrawIdFroms() {
        List<String> withdrawIdFromRedis = bill99OrderRedisService.getWithdrawIdFromRedis(Constants.STORE_WITHDRAWID_SENDMSG);
        if (CollectionUtils.isEmpty(withdrawIdFromRedis)) {
            return;
        }
        for (String ss : withdrawIdFromRedis) {
            Long id = Long.valueOf(ss);
            //已完成
            WithdrawtosVo withdrawByid = withdrawtoService.getWithdrawByid(id);
            Short apprStatus = withdrawByid.getApprStatus();
            if (apprStatus == Constants.APPR_STATUS_6.shortValue()) {
                if (withdrawByid != null) {
                    Short accType = withdrawByid.getAccType();
                    Long uid = withdrawByid.getUid();
                    String amount = String.valueOf(withdrawByid.getAmount());
                    String cardCode = String.valueOf(withdrawByid.getBankAccount().getCardCode());
                    //店铺
                    if (accType == Constants.SHORT_TWO) {
                        if (StringUtils.isNotBlank(cardCode)) {
                            int length = cardCode.length();
                            String cardCodes = cardCode.substring(length - 4);
                            Integer bankAccountId = withdrawByid.getBankAccountId();
                            String mobilePhone = bankAccountService.getMobilePhoneByid(bankAccountId);
                            if (StringUtils.isNotBlank(mobilePhone)) {
                                //content参数 金额 + 银行卡尾号
                                String content = amount + "|" + cardCodes;
                                msgClient.postMsg(msgUrl, mobilePhone, uid.toString(), content, CASHWITHDRAWALAUDITKEY);
                                logger.info("check withdrawparams success");
                            }
                        }
                    }
                }
            }
        }
    }
}
