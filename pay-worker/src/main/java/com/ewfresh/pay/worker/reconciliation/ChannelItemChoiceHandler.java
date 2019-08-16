package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.worker.reconciliation.fileDown.FileDownInterface;
import com.ewfresh.pay.worker.reconciliation.fileDown.impl.YinLianFileDownImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * Description: 获取对账文件处理的类
 * @author DuanXiangming
 * Date 2019/6/18
 */
@Component
public class ChannelItemChoiceHandler extends ApplicationObjectSupport {



    public Object chooseInterface(String interfaceCode){

        Object bean = null;
        try {
            ApplicationContext applicationContext = this.getApplicationContext();
            bean = applicationContext.getBean(interfaceCode);
        } catch (Exception e) {
            logger.error("get file down impl err ", e);
        }
        return bean;
    }
}
