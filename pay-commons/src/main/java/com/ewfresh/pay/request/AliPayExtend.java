package com.ewfresh.pay.request;

import java.io.Serializable;

/**
 * @author: wangziyuan on 2018/3/30
 */
public class AliPayExtend implements Serializable{
    private static final long serialVersionUID = 1L;

    private String sys_service_provider_id;

    public String getSys_service_provider_id() {
        return sys_service_provider_id;
    }

    public void setSys_service_provider_id(String sys_service_provider_id) {
        this.sys_service_provider_id = sys_service_provider_id;
    }
}
