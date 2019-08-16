package com.ewfresh.pay.service.impl;


import com.ewfresh.pay.model.BillFlow;
import com.ewfresh.pay.service.AliPayService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangziyuan on 2018/4/4.
 */
@Service
public class AliPayServiceImpl implements AliPayService {

    private static final Short ONE = 1;

    private static final Short TWE = 2;

    private static final Short EIGHT = 8;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<BillFlow> ReadCvsToObject(List<String[]> list) {
        List<BillFlow> billFlows = new ArrayList<BillFlow>();
        for (int i = 5; i < list.size() - 4; i++) {
            BillFlow billFlow = new BillFlow();
            //获取支付渠道流水
            String channelFlowId = list.get(i)[1].replace("\t", "").replace("\n", "");
            //获取商户订单ID
            String orderId = list.get(i)[2].replace("\t", "").replace("\n", "");
            //获取支付成功实践
            String createTime = list.get(i)[4].replace("\t", "").replace("\n", "");
            //获取收入金额
            String income = list.get(i)[6].replace("\t", "").replace("\n", "");
            //获取支出金额
            String expenditure = list.get(i)[7].replace("\t", "").replace("\n", "");
            //获取账户余额
            String accountBalance = list.get(i)[8].replace("\t", "").replace("\n", "");
            //获取交易渠道
            String channelName = list.get(i)[9].replace("\t", "").replace("\n", "");
            /**
             * 获取交易类型 进行判断 如果为'在线支付'则为枚举类型1(订单)
             * 如果交易类型为 '交易退款' 则为枚举类型2(退款)
             */
            String tradeType = list.get(i)[10].replace("\t", "").replace("\n", "");
            if (tradeType.equals("在线支付")) {
                billFlow.setTradeType(ONE);
            } else if (tradeType.equals("交易退款")) {
                billFlow.setTradeType(TWE);
            } else if (tradeType.equals("收费")) {
                billFlow.setTradeType(EIGHT);
            }
            //获取备注
            String desp = list.get(i)[11].replace("\t", "");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                billFlow.setCreateTime(sdf.parse(createTime));
            } catch (ParseException e) {
                logger.error("Read cvs Object is err",e);
            }
            BigDecimal bigDecimal = new BigDecimal(income);
            billFlow.setIncome(bigDecimal);
            BigDecimal bigDecimal1 = new BigDecimal(expenditure);
            billFlow.setExpenditure(bigDecimal1);
            BigDecimal bigDecimal2 = new BigDecimal(accountBalance);
            billFlow.setAccountBalance(bigDecimal2);
            billFlow.setChannelName(channelName);
            if (StringUtils.isNotBlank(desp))
                billFlow.setDesp(desp);
            billFlow.setChannelFlowId(channelFlowId);
            billFlow.setOrderId(Long.parseLong(orderId));
            billFlows.add(billFlow);
        }
        return billFlows;
    }
}
