package com.ewfresh.pay.worker.reconciliation.parser.impl;


import com.ewfresh.pay.model.AccCheck;
import com.ewfresh.pay.model.reconiliationVo.ReconciliationEntityVo;
import com.ewfresh.pay.worker.reconciliation.parser.ReconciliationParserInterface;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Description:
 * @author DuanXiangming
 * Date 2019/6/19
 */
@Component("YINLIANParser")
public class YinlianParserImpl implements ReconciliationParserInterface {


    @Override
    public List<ReconciliationEntityVo> parser(File file, AccCheck accCheck, Date billDate) {
        return null;
    }
}
