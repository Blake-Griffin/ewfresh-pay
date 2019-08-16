package com.ewfresh.pay.worker.reconciliation.fileDown;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Description: 对账文件下载的接口,获取文件输出
 * @author DuanXiangming
 * Date 2019/6/18
 */
public interface FileDownInterface {



    /**
     * Description: 获取对账文件的接口方法
     * @author DuanXiangming
     * @param  fileDate     对账文件日期
     * @param  dir          下载保存地址
     * @return java.io.File
     * Date    2019/6/18  11:48
     */
    File fileDown(Date fileDate, String dir) throws Exception;

}
