package com.ewfresh.pay.worker;

import com.ewfresh.pay.model.Bill;
import com.ewfresh.pay.model.BillRepayFlow;
import com.ewfresh.pay.model.vo.BillRepayFlowUpBillVo;
import com.ewfresh.pay.redisService.BillRepayRedisService;
import com.ewfresh.pay.service.BillRepayFlowService;
import com.ewfresh.pay.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * description: 离线处理 BillRepayFlowUpBillVo 白条退款还款记录,修改账单信息
 *
 * @author: ZhaoQun
 * date: 2019/3/31.
 */
@Component
public class BillRepayFlowUpdateBillWorker {
    @Autowired
    private BillRepayRedisService billRepayRedisService;
    @Autowired
    private BillRepayFlowService billRepayFlowService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    //@Scheduled(cron = "1-59 * * * * ?")
    public void whiteRefundUpdateBill() {
        //从redis 获取数据
        BillRepayFlowUpBillVo vo = billRepayRedisService.getBillRepayFlowUpBillVo();
        if (vo == null){
            return;
        }
        logger.info("BillRepayFlowUpBillVo is ={}",JsonUtil.toJson(vo));
        BillRepayFlow repayFlow = new BillRepayFlow();
        Bill bill = new Bill();
        bill.setId(vo.getBillId());
        bill.setBillFlow(vo.getBillFlow());//账单批次号
        bill.setBillFlow(vo.getBillFlow());//账单批次号
        bill.setBillStatus(vo.getBillStatus());//账单状态
        bill.setRepaidAmount(vo.getRepaidAmount());//已还金额
        bill.setRepaidInterest(vo.getRepaidInterest());//已还利息
        //t_bill_repay_flow
        repayFlow.setBillId(vo.getBillId());//账单id
        repayFlow.setRepayAmount(vo.getRepayAmount());//还款金额
        repayFlow.setPrincipalAmount(vo.getPrincipalAmount());//当次归还本金金额
        repayFlow.setInterestAmount(vo.getInterestAmount());//当次归还利息金额
        repayFlow.setRepayChannel(vo.getRepayChannel());//还款渠道(1余额,2块钱,3银联,4混合,5白条退款)
        repayFlow.setRepayType(vo.getRepayType());//还款方式(0被动扣款,1主动还款，2白条退款还款）
        repayFlow.setOperator(vo.getOperator());
        repayFlow.setOrderId(vo.getOrderId());//还款订单号
        repayFlow.setRepayTime(new Date());//还款时间
        //todo
        try {
            int n = billRepayFlowService.updateBillAddRepayFlow(bill, repayFlow);
            if (n < 1){
                throw new Exception("updateBillAddRepayFlow is error, n = " + n);
            }
        }catch (Exception e){
            logger.error("updateBillAddRepayFlow is error,billFlow = {},e={}" ,bill.getBillFlow(),e );
            //billRepayRedisService.setBillRepayFlowUpBillVo(JsonUtil.toJson(vo));
        }

    }
}
