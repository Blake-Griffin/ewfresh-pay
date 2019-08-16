package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo;
import com.ewfresh.pay.worker.ReconciliationWorker;
import com.ewfresh.pay.worker.reconciliation.parser.ReconciliationParserInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Description: 解析对账文件的处理器
 * @author DuanXiangming
 * Date 2019/6/19
 */
@Component
public class ReconciliationParserHandler {


    private static final Logger logger = LoggerFactory.getLogger(ReconciliationWorker.class);
    @Autowired
    private ChannelItemChoiceHandler choiceHandler;

    private static final String suffix = "Parser";
    /**
     * Description:解析对账文件
     * @author DuanXiangming
     * @param  interfaceCode  支付渠道编码
     * @param  file           对账文件
     * @param accCheck
     * @param billDate
     * @return java.util.List<com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo>
     * Date    2019/6/19  11:42
     */
    public List<ReconciliationEntityVo> parser(String interfaceCode, File file, AccCheck accCheck, Date billDate) {

        logger.info("parser reconciliation file start [interfaceCode = {}]", interfaceCode);
        List<ReconciliationEntityVo> vos = null;
        ReconciliationParserInterface parserInterface = null;
        try {
            //根据
            parserInterface = (ReconciliationParserInterface)choiceHandler.chooseInterface(interfaceCode + suffix);
        } catch (Exception e) {
            logger.error("get parserInterface err interfaceCode = " + interfaceCode ,e);
        }
        vos = parserInterface.parser(file, accCheck, billDate);
        return vos;
    }

}
