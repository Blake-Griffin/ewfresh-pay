package com.ewfresh.pay.redisDao.impl;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.model.AccountFlow;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.vo.BillRepayFlowUpBillVo;
import com.ewfresh.pay.redisDao.BillRepayRedisDao;
import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * description:
 *
 * @param
 * @author huboyang
 */
@Component
public class BillRepayRedisDaoImpl implements BillRepayRedisDao {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String White_Repay_KEY = "{order}-{sendRecord}-{orderToPay}";
    private String White_Bill_Key="{pay}{WhiteRepayBill}";
    private String Bill_Message_Key="{pay}{BillMessage}";
    @Override
    public void addRepayAmount(Map<String,Object> map) {
        ValueOperations<String,String> stringValue = redisTemplate.opsForValue();
        logger.info("map={}",ItvJsonUtil.toJson(map));
        stringValue.set(White_Repay_KEY+map.get("interactionId"),ItvJsonUtil.toJson(map), 60, TimeUnit.MINUTES);
    }
    @Override
    public Map<String, Object> getWhiteOrder(String orderId){

        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        String s = stringStringValueOperations.get(White_Repay_KEY+orderId);
        return StringUtils.isBlank(s) ? null : ItvJsonUtil.jsonToObj(s, new TypeReference<Map<String,Object>>(){});
    }

    @Override
    public void addRepayBill(List<Integer> billFlowList,Long idGenerator) {
        ValueOperations<String,String> stringValue = redisTemplate.opsForValue();
        logger.info("idGenerator={}",idGenerator);
        stringValue.set(White_Bill_Key+idGenerator,ItvJsonUtil.toJson(billFlowList), 65, TimeUnit.MINUTES);
    }
    @Override
    public List<String> getWhiteRepayBill(String key) {
        List<String> monthlySales = new ArrayList<>();
        ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();
        logger.info("key={}",key);
        String billId = stringValueOperations.get(key);

        monthlySales = ItvJsonUtil.jsonToObj(billId, new TypeReference<ArrayList<String>>() {});
        return monthlySales;
    }
    @Override
    public Map<String, String> getWhiteRepayOrder(String key) {
        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        String s = stringStringValueOperations.get(key);
        return StringUtils.isBlank(s) ? null : ItvJsonUtil.jsonToObj(s, new TypeReference<Map<String,String>>(){});
    }

    @Override
    public AccountFlow getAcountFlow(String key) {

        return null;
    }

    @Override
    public void addPayFlow(PayFlow payFlow) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.PAYFLOW_TO_ACCFLOW,ItvJsonUtil.toJson(payFlow));

    }

    //redis 存BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    @Override
    public void setBillRepayFlowUpBillVo(String s) {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush(Constants.BillRepayFlowUpBillVo_info, s);
    }
    //redis 取BillRepayFlowUpBillVo,离线处理 白条退款还款记录   zhaoqun
    @Override
    public BillRepayFlowUpBillVo getBillRepayFlowUpBillVo() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        String str = list.rightPop(Constants.BillRepayFlowUpBillVo_info);
        return StringUtils.isBlank(str) ? null : ItvJsonUtil.jsonToObj(str ,BillRepayFlowUpBillVo.class);
    }

    @Override
    public void addRepayOrder(Map<String, Object> map, String ids) {
        ValueOperations<String, String> stringValueOperations = redisTemplate.opsForValue();
        stringValueOperations.set(Bill_Message_Key+ids,ItvJsonUtil.toJson(map), 60, TimeUnit.MINUTES);

    }

    @Override
    public Map<String, Object> getWhiteRepayOrderByIds(String key) {
        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        String s = stringStringValueOperations.get(Bill_Message_Key+key);
        return StringUtils.isBlank(s) ? null : ItvJsonUtil.jsonToObj(s, new TypeReference<Map<String,Object>>(){});

    }

}
