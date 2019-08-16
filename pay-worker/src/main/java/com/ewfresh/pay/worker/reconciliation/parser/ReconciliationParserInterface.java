package com.ewfresh.pay.worker.reconciliation.parser;

import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2019/6/19
 */
public interface ReconciliationParserInterface {


    /**
     * Description:解析对账文件的接口方法
     * @author DuanXiangming
     * @param  file             对账文件
     * @param  accCheck         对账批次
     * @param  billDate         对账日期
     * @return java.util.List<com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo>
     * Date    2019/6/19  14:25
     */
    List<ReconciliationEntityVo> parser(File file, AccCheck accCheck, Date billDate);
}
