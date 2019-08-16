package com.ewfresh.pay.manager.impl;

import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.manager.ReceivablesManager;
import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.ReceivablesVo;
import com.ewfresh.pay.redisService.AccountFlowRedisService;
import com.ewfresh.pay.service.ReceivablesService;
import com.ewfresh.pay.util.ResponseData;
import com.ewfresh.pay.util.ResponseStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/6/7 0007
 */
@Component
public class ReceivablesManagerImpl implements ReceivablesManager{

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReceivablesService receivablesService;

    @Autowired
    private AccountFlowRedisService accountFlowRedisService;

    private static  final  String PHONE = "phone";

    @Override
    public void getReceivablesByUid(ResponseData responseData, Long uid, Integer pageSize, Integer pageNumber, String explain, String amount, String startTime, String endTime) {
        logger.info("Get receivables list params  are ----->[uid = {},explain={},amount={},endTime={},startTime={}]", uid,explain ,amount,endTime,startTime);
        PageHelper.startPage(pageNumber,pageSize);
        List<Receivables> list = receivablesService.getReceivablesListByUid(uid, pageSize, pageNumber,explain,amount,startTime,endTime);
        PageInfo<Receivables> receivablesPageInfo = new PageInfo<>(list);
        List<Receivables> list1 = receivablesPageInfo.getList();
        responseData.setEntity(list1);
        responseData.setTotal(receivablesPageInfo.getTotal()+"");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(receivablesPageInfo.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());
    }


    @Override
    public void getReceivablesList(ResponseData responseData, Integer pageSize, Integer pageNumber, String uname, String receiveTime) {
        logger.info("Get Receivables list param is ----->[uname = {},receiveTime={}]",uname,receiveTime);
        PageHelper.startPage(pageNumber,pageSize);
        List<Long> ids = receivablesService.getReceivablesList(uname, receiveTime);
        PageInfo<Long> longPageInfo = new PageInfo<>(ids);
        if(CollectionUtils.isNotEmpty(ids)) {
            List<ReceivablesVo> receivablesList = receivablesService.getReceivablesListByParm(ids);
            for (ReceivablesVo receivables : receivablesList) {
                try {
                    String userInfo = accountFlowRedisService.getUserInfo(receivables.getUserId());
                    HashMap<String, Object> hashMap = ItvJsonUtil.jsonToObj(userInfo, new HashMap<String, Object>().getClass());
                    if(!StringUtils.isEmpty(userInfo)) {
                        Object phone = hashMap.get(PHONE);
                        receivables.setPhone(phone.toString());
                    }
                } catch (Exception e) {
                    logger.error(" get user info cache err",e);
                }
            }
            responseData.setEntity(receivablesList);
        }
        responseData.setTotal(longPageInfo.getTotal()+"");
        responseData.setCurPage(pageNumber);
        responseData.setPageCount(longPageInfo.getPages());
        responseData.setCode(ResponseStatus.OK.getValue());
        responseData.setMsg(ResponseStatus.OK.name());

    }
}
