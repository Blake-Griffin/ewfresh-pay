package com.ewfresh.pay.manager;


import com.ewfresh.pay.util.ResponseData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;


public interface BillRepayManager {
    void getAllBillByUid(ResponseData responseData, Long uid)throws ParseException;
    void getBillDetails(ResponseData responseData, String payMode,String orderIp,String uid,String payTimestamp,
                         String ids,String uname,String payType,String channelType,String client,String bizType,String cardType );
    void getRepayRecord(ResponseData responseData,String uid,Integer pageSize,
                        Integer pageNumber);
}
