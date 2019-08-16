package com.ewfresh.pay.worker.reconciliation;

import com.ewfresh.pay.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 用于查询所有支付渠道的接口
 * @author DuanXiangming
 * Date 2019/6/12 0012
 */
public class ReconciliationInterface {



    /** 接口名称 */
    private String interfaceName;

    /** 接口代码 */
    private String interfaceCode;

    /** 接口描述 */
    private String interfaceDesc;

    /** 是否有效 PublicStatusEnum */
    private Short status;

    /** 对账单周期 **/
    private int billDay;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceCode() {
        return interfaceCode;
    }

    public void setInterfaceCode(String interfaceCode) {
        this.interfaceCode = interfaceCode;
    }

    public String getInterfaceDesc() {
        return interfaceDesc;
    }

    public void setInterfaceDesc(String interfaceDesc) {
        this.interfaceDesc = interfaceDesc;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public int getBillDay() {
        return billDay;
    }

    public void setBillDay(int billDay) {
        this.billDay = billDay;
    }

    public static List<ReconciliationInterface> getInterface() {
        List<ReconciliationInterface> list = new ArrayList<ReconciliationInterface>();
        ReconciliationInterface yinlian = new ReconciliationInterface();
        yinlian.setInterfaceCode("YINLIAN");
        yinlian.setInterfaceName("银联");
        yinlian.setBillDay(1);
        yinlian.setStatus(Constants.SHORT_ONE);
        list.add(yinlian);
        return list;

    }

}
