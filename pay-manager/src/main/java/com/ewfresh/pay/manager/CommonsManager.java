package com.ewfresh.pay.manager;

import java.util.Map;

/**
 * description: 阿里.微信.中行公用 逻辑
 * @author wangziyuan
 * @date 2018.4.16
 */
public interface CommonsManager {
    boolean ifSuccess(Map<String,Object> params);
}
