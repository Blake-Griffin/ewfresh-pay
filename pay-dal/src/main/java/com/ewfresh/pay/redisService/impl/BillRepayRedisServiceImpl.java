package com.ewfresh.pay.redisService.impl;

import com.ewfresh.pay.dao.AccountFlowDao;
import com.ewfresh.pay.dao.PayFlowDao;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillRepayFlowUpBillVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisDao.BillRepayRedisDao;
import com.ewfresh.pay.redisService.BillRepayRedisService;
import com.ewfresh.pay.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillRepayRedisServiceImpl implements BillRepayRedisService {
    @Autowired
    private BillRepayRedisDao billRepayRedisDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private PayFlowDao payFlowDao;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;
    @Override
    @Transactional
    public void setRepTotalbillAmount(HashMap<String,Object> map) {
        billRepayRedisDao.addRepayAmount(map);
    }




    @Override
    public Map<String, String> getRrpayOrder(String key) {
        Map<String, String> map = billRepayRedisDao.getWhiteRepayOrder(Constants.White_Repay_KEY);
        return map;
    }

    @Override
    public AccountFlow getAcountFlow(String key) {

        return billRepayRedisDao.getAcountFlow(key);
    }
    @Transactional
    @Override
    public void addPayFlow(PayFlow payFlow, AccountFlowVo accountFlow, List<Integer> list , Long whiteOrderId ,String idGenerator ) {
        payFlowDao.addPayFlow(payFlow);
        accountFlowDao.addAccountFlow(accountFlow);
        PayFlow payFlowByPayerId = payFlowDao.getPayFlowByPayerId(payFlow.getPayerId());
        billRepayRedisDao.addPayFlow(payFlowByPayerId );
        accountFlowRedisDao.setUnfreezeOrderId(payFlow.getOrderId().toString(),accountFlow.getAccFlowId());
        billRepayRedisDao.addRepayBill(list ,Long.valueOf(idGenerator));
    }

    //redis 存BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    @Override
    public void setBillRepayFlowUpBillVo(String s) {
        billRepayRedisDao.setBillRepayFlowUpBillVo(s);
    }

    //redis 取BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    @Override
    public BillRepayFlowUpBillVo getBillRepayFlowUpBillVo() {
        return billRepayRedisDao.getBillRepayFlowUpBillVo();
    }
}
