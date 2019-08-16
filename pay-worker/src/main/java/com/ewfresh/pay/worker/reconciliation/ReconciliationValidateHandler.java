package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.service.AccCheckService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.worker.ReconciliationWorker;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:检查是否已经对过账的处理器
 * @author DuanXiangming
 * Date 2019/6/12 0012
 */
@Component
public class ReconciliationValidateHandler {


    private static final Logger logger = LoggerFactory.getLogger(ReconciliationValidateHandler.class);

    @Autowired
    private AccCheckService accCheckService;
    /**
     * Description:校验当前支付渠道当前对账时间是否已对账
     * @author DuanXiangming
     * @param  interfaceCode 支付渠道编码
     * @param  billDate      对账日期
     * @return boolean
     * Date    2019/6/13 0013  10:15
     */
    private static final String CHANNEL_CODE = "channelCode";
    private static final String BILL_DATE = "billDate";
    //private static final String HANDLE_STATUS = "handleStatus";


    public boolean isChecked(String interfaceCode, Date billDate) {
        logger.info("start check this channel is checked for this billDate [interfaceCode = {} billDate = {}]", interfaceCode, billDate);
        boolean flag = true;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(CHANNEL_CODE, interfaceCode);
        paramMap.put(BILL_DATE,billDate);
        //查询只有对账错误或者失败才能再次发起对账
        //paramMap.put(HANDLE_STATUS, Constants.SHORT_TWO);
        List<AccCheck> accChecks = accCheckService.getAccCheckByParam(paramMap);
        if (CollectionUtils.isEmpty(accChecks)){
            flag = false;
        }
        int size = accChecks.size();
        if (size > 1){
            throw new RuntimeException("the accCheck number is err");
        }
        short handleStatus = accChecks.get(0).getHandleStatus().shortValue();
        if (handleStatus == Constants.SHORT_TWO){
            flag = false;
        }
        return flag;

    }
}
