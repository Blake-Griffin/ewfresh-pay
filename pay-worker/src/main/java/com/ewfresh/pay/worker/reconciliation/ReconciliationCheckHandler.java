package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.PayFlow;
import com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo;
import com.ewfresh.pay.service.AccCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Description: 流水对账的处理器
 * @author DuanXiangming
 * Date 2019/6/19
 */
@Component
public class ReconciliationCheckHandler {


    private static final Logger logger = LoggerFactory.getLogger(ReconciliationCheckHandler.class);
    @Autowired
    private AccCheckService accCheckService;

    /**
     * Description: 具体对账的处理
     * @author  DuanXiangming
     * @param   vos
     * @param   accCheck
     * @param   interfaceCode
     * @return void
     * Date    2019/6/19  14:40
     */
    public void check(List<ReconciliationEntityVo> vos, AccCheck accCheck, String interfaceCode) {

        //查询平台当前interfaceCode,billdate所有的交易记录
        Date billDate = accCheck.getBillDate();
        List<PayFlow> payFlows = accCheckService.getPayFlowsByItem(billDate, interfaceCode);




    }
}
