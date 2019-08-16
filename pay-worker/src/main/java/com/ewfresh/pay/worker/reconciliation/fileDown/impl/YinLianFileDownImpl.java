package com.ewfresh.pay.worker.reconciliation.fileDown.impl;

import com.ewfresh.pay.worker.reconciliation.fileDown.FileDownInterface;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2019/6/18
 */
@Component("YINLIANFileDown")
public class YinLianFileDownImpl implements FileDownInterface {


    @Override
    public File fileDown(Date fileDate, String dir) throws IOException, Exception {


        return null;
    }
}
