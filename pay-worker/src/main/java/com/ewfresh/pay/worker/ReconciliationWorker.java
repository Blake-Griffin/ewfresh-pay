package com.ewfresh.pay.worker;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo;
import com.ewfresh.pay.service.AccCheckService;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.DateUtil;
import com.ewfresh.pay.worker.reconciliation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Description: 用于对账的worker
 * @author DuanXiangming
 * Date 2018/8/7 0007
 */
@Component
public class ReconciliationWorker {


    private static final Logger logger = LoggerFactory.getLogger(ReconciliationWorker.class);
    @Autowired
    private ReconciliationValidateHandler validateHandler;
    @Autowired
    private ReconciliationFileDownHandler fileDownHandler;
    @Autowired
    private ReconciliationParserHandler parserHandler;
    @Autowired
    private ReconciliationCheckHandler checkHandler;
    @Autowired
    private AccCheckService accCheckService;
    /**
     * Description: 定时对账的worker
     * @author DuanXiangming
     * Date    2018/4/18
     */
    //@Scheduled(cron = "1-59 * * * * ?")
    public void autoAddAccFlow() {

        String sdf = "yyyyMMdd";

        //取出所有需要对账的支付渠道,因为目前只有银联,直接写死,可以扩展成为数据库方式,
        List<ReconciliationInterface> list = ReconciliationInterface.getInterface();

        for (int i = 0; i < list.size(); i++) {

            ReconciliationInterface reconciliationInterface = list.get(0);
            if (reconciliationInterface == null){
                logger.info("reconciliation info is null");
                continue;
            }
            //获取需要对账的时间
            Date date = new Date();
            Date billDate = DateUtil.getFutureMountDays(date, -reconciliationInterface.getBillDay());
            //获取对账渠道
            String interfaceCode = reconciliationInterface.getInterfaceCode();
            String interfaceName = reconciliationInterface.getInterfaceName();
            AccCheck accCheck = new AccCheck();
            /** step1:判断是否对过账 **/
            try {
                boolean flag =  validateHandler.isChecked(interfaceCode, billDate);
                if (flag){
                    logger.info("this interface had checked already for this billDate [interfaceCode = {}, billDate = {}]", interfaceCode, billDate);
                }
            } catch (Exception e) {
                logger.error("check bill err", e);
                continue;
            }
            accCheck.setCreator(Integer.valueOf(Constants.SYSTEM_ID));
            accCheck.setBillDate(billDate);
            accCheck.setChannelCode(interfaceCode);
            accCheck.setChannelName(interfaceName);
            String dateStr = DateUtil.toString(date, sdf);
            accCheck.setBatchNo(interfaceCode + dateStr);

            /** step2:对账文件下载 **/
            File file = null;
            try {
                logger.info(" download reconiliation file start");
                file = fileDownHandler.downReconciliationFile(interfaceCode, billDate);
                if (file == null){
                    continue;
                }
                logger.info("download reconiliation file end");
            } catch (Exception e) {
                logger.error("down reconciliation file failed",e);
                accCheck.setFailMsg("下载对账文件异常");
                accCheck.setHandleStatus(Constants.SHORT_TWO);
                accCheckService.addAccCheck(accCheck);
                continue;
            }

            /** step3:解析对账文件 **/
            List<ReconciliationEntityVo> vos = null;
            try {
                logger.info(" parser reconciliation file start");
                vos = parserHandler.parser(interfaceCode, file, accCheck, billDate);
                if (vos == null){
                    continue;
                }
                logger.info("parser reconciliation file end");
            } catch (Exception e) {
                logger.error("parser reconciliation file failed",e);
                accCheck.setFailMsg("解析对账文件异常");
                accCheck.setHandleStatus(Constants.SHORT_TWO);
                accCheckService.addAccCheck(accCheck);
                continue;
            }

            /** step4:对账流程 **/
            try {
                checkHandler.check(vos, accCheck, interfaceCode);
            } catch (Exception e) {
                logger.error("check reconciliation vos err",e);
                accCheck.setFailMsg("对账异常");
                accCheck.setHandleStatus(Constants.SHORT_TWO);
                accCheckService.addAccCheck(accCheck);
                continue;
            }

        }

    }


}
