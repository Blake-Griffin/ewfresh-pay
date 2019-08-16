package com.ewfresh.pay.service;

import com.ewfresh.pay.model.Receivables;
import com.ewfresh.pay.model.vo.ReceivablesVo;

import java.util.List;

/**
 * Description:
 *
 * @author DuanXiangming
 * Date 2018/6/6 0006
 */
public interface ReceivablesService {


    Receivables getReceivablesByUid(String uid);

    List<Receivables> getReceivablesListByUid(Long uid, Integer pageSize, Integer pageNumber, String explain, String amount, String startTime, String endTime);

    List<Long> getReceivablesList(String uname, String receiveTime);

    List<ReceivablesVo> getReceivablesListByParm(List<Long> ids);
}
