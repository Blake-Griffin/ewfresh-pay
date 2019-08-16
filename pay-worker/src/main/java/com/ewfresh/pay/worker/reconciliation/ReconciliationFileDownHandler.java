package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.worker.reconciliation.fileDown.FileDownInterface;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * Description: 对账文件下载的处理器
 * @author DuanXiangming
 * Date 2019/6/18
 */
@Component
public class ReconciliationFileDownHandler {


    private static final Logger logger = LoggerFactory.getLogger(ReconciliationFileDownHandler.class);
    private static final int DOWNLOAD_TRY_TIMES = 3;// 下载尝试次数
    @Autowired
    private ChannelItemChoiceHandler choiceHandler;
    @Value("${reconiliation_file_dir}")
    private  String fileDir;

    private static final String suffix = "FileDown";
    /**
     * Description: 请求下载对账文件
     * @author DuanXiangming
     * @param  interfaceCode    支付渠道编码
     * @param  billDate         对账日期
     * @return java.io.File
     * Date    2019/6/18  12:01
     */
    public File downReconciliationFile(String interfaceCode, Date billDate) {

        if (StringUtils.isBlank(interfaceCode) || billDate == null){
            logger.warn(" empty inferfaceCode or empty billDate [interfaceCode = {}, billDate = {}]", interfaceCode, billDate);
            return null;
        }

        return this.downFile(interfaceCode, billDate);
    }



    /**
     * Description: 下载对账文件
     * @author DuanXiangming
     * @param  interfaceCode    支付渠道编码
     * @param  billDate         对账日期
     * @return java.io.File
     * Date    2019/6/18  12:01
     */
    private File downFile(String interfaceCode, Date billDate){

        logger.info("this channel request for download reconciliation file [interfaceCode = {}, billDate = {}]", interfaceCode, billDate);

        try {
            File file = null;
            int downloadTimes = 0;
            while (file == null && downloadTimes < DOWNLOAD_TRY_TIMES){
                downloadTimes ++ ;
                try {
                    FileDownInterface choose = (FileDownInterface)choiceHandler.chooseInterface(interfaceCode + suffix);
                    file = choose.fileDown(billDate, fileDir);
                } catch (Exception e) {
                    logger.error("get reconciliation file failed", e);
                    Thread.sleep(10000);
                }
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
