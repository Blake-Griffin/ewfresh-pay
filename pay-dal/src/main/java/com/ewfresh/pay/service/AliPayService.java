package com.ewfresh.pay.service;


import com.ewfresh.pay.model.BillFlow;

import java.util.List;

public interface AliPayService {
    List<BillFlow> ReadCvsToObject(List<String[]> list);
}
