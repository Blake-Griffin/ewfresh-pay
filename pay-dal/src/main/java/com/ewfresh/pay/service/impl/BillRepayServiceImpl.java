package com.ewfresh.pay.service.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.dao.AccountFlowDao;
import com.ewfresh.pay.dao.BarDealFlowDao;
import com.ewfresh.pay.dao.BillDao;
import com.ewfresh.pay.dao.BillRepayFlowDao;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.BarDealFlow;
import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.vo.AccountFlowVo;
import com.ewfresh.pay.model.vo.BillVo;
import com.ewfresh.pay.redisDao.AccountFlowRedisDao;
import com.ewfresh.pay.redisDao.BillRepayRedisDao;
import com.ewfresh.pay.service.BillRepayService;
import com.ewfresh.pay.service.utils.ReceivablesUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class BillRepayServiceImpl implements BillRepayService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BillRepayService.class);
    @Autowired
    private BillDao billDao;
    @Autowired
    private BarDealFlowDao barDealFlowDao;
    @Autowired
    private AccountFlowDao accountFlowDao;
    @Autowired
    private AccountFlowRedisDao accountFlowRedisDao;
    @Autowired
    private BillRepayFlowDao billRepayFlowDao;
    @Autowired
    private BillRepayRedisDao billRepayRedisDao;
    @Autowired
    private ReceivablesUtils receivablesUtils;

    @Override
    public List<BarDealFlow> getBillFlowByUid(Long uid) {
        return barDealFlowDao.getBillFlowByUid(uid);
    }
    @Override
    public List<BillVo> getBillByUid(Long uid) {
        return billDao.getBillByUid(uid);
    }
    @Override
    public BillVo getBillByBillId(Integer billId) {
        return billDao.getBillByBillId(billId);
    }


    @Override
    @Transactional
    public void addAccountFlow(AccountFlowVo accountFlow  , Map<String,Object> map,List<Integer> list, Long whiteOrderId,String idGenerator){


        //添加一条资金账户流水冻结还款金额
        accountFlowDao.addFreezeAccFlow(accountFlow);

        AccountFlowVo accountFlowVo = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId().toString());

        logger.info("accountFlowVo={}" , ItvJsonUtil.toJson(accountFlowVo));

        receivablesUtils.addReceivables(accountFlow);
        //吧冻结余额的accountFlow放入redis
        accountFlowRedisDao.setBalanceByUid(accountFlowVo);
        accountFlowRedisDao.setUnfreezeOrderId(whiteOrderId.toString(),accountFlow.getAccFlowId());

       //把白条还款的订单放入redis
        billRepayRedisDao.addRepayAmount(map);
        billRepayRedisDao.addRepayOrder(map,map.get(whiteOrderId.toString()).toString());
        //把白条还款的账单号放入redis
        billRepayRedisDao.addRepayBill(list, Long.valueOf(idGenerator));

    }
    @Override
    public void addWhiteOrder( Map<String, Object> map, List<Integer> list,Long whiteOrderId,String idGenerator) {
        /*//添加一条资金账户流水冻结还款金额
        accountFlowDao.addFreezeAccFlow(accountFlow);
        AccountFlowVo accountFlowVo = accountFlowDao.getAccountFlowByUid(accountFlow.getUserId().toString());
        receivablesUtils.addReceivables(accountFlowVo);
        //把白条还款的订单放入redis*/
        billRepayRedisDao.addRepayAmount(map);
        //把白条还款的账单号放入redis
        billRepayRedisDao.addRepayBill(list,Long.valueOf(idGenerator));
    }

    @Override
    public void addWhiteOrderByIds(Map<String, Object> map, String ids) {
        billRepayRedisDao.addRepayOrder(map,ids);
    }

    @Override
    public Map<String, Object> getWhiteOrderIByIds(String key) {
        return billRepayRedisDao.getWhiteRepayOrderByIds(key);
    }

    @Override
    public AccountFlow getAccountFlow(String Uid) {
        AccountFlowVo accountFlow = accountFlowDao.getAccountFlowByUid(Uid);
        return accountFlow;
    }
    @Override
    public Map<String,Object> getWhiteOrder(String orderId){
        return billRepayRedisDao.getWhiteOrder(orderId);
    }

    @Override
    public List<BillVo> getBillsByUid(Long uid) {
        return billDao.getBillsByUid(uid);
    }


}
