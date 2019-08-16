package com.ewfresh.pay.manager;

import com.ewfresh.pay.util.ResponseData; /**
 * Description:
 * @author DuanXiangming
 * Date 2018/6/7 0007
 */
public interface ReceivablesManager {


    void getReceivablesByUid(ResponseData responseData, Long uid, Integer pageSize, Integer pageNumber, String explain, String amount, String startTime, String endTime);

    void getReceivablesList(ResponseData responseData, Integer pageSize, Integer pageNumber, String uname, String receiveTime);
}
