package com.ewfresh.pay.util;

import org.aspectj.weaver.ast.Var;

import java.math.BigDecimal;

/**
 * 提出的标识
 */
public interface Constants {
    String PAY_UNIONPAYWEBWAP_REFUND_INFO = "{pay}-{unionPayWebWapRefundOrderInfo}";//unionPayWebWap---存储商户退款信息

    String PAY_UNIONPAYQRCODE_REFUND_INFO = "{pay}-{unionPayQrCodeRefundOrderInfo}";//unionPayQrCode---存储商户退款信息

    String PAY_UNIONPAYH5PAY_REFUND_INFO = "{pay}-{unionPayH5PayRefundOrderInfo}";//unionPayH5Pay---存储商户退款信息

    String PAY_UNIONPAYH5_PAY_INFO = "{pay}-{unionPayH5PayPayInfo}";//unionPayH5Pay---存储支付时的订单信息

    String PAY_UNIONPAYQRCODE_PAY_INFO = "{pay}-{unionPayQRCodePayInfo}";//unionPayQrCodePay---存储支付时的订单信息

    String PAY_UPDATE_PAY_FLOW = "{pay}-{unionPayWaitUpdatePayFlow}";//unionPay---存储待修改payFlow信息

    String QUOTA_UNFREEZE = "{pay}-{quotaUnfreeze}"; //用户白条状态

    String ENTITY = "entity";

    String BANLANCE_KEY = "{pay}-{banlanceKeyByUid}";//存储用户最新资金账户流水的key

    String UNFREEZE_BANLANCE = "{pay}-{unfreezeBanlance}";//释放冻结余额的方法

    String UNFREEZE_BANLANCE_ORDER = "{pay}-{unfreezeBanlance}-{order}";//释放冻结余额的方法中用存放冻结accflowID的KEY

    String FREEZE_BANLANCE = "{pay}-{freezeBanlance}";//释放冻结余额的方法

    String PAY_BY_BANLANCE = "{pay}-{payByBanlance}";//使用余额支付的方法

    String ABATEMENT_BALANCE = "{pay}-{abatementBalance}";//内部扣减余额的方法

    String FINISH_ORDER_PAY = "{order}-{finishOrder}-{pay}";//获取完结订单流水的方法名

    String SEND_ORDER_TIME = "{pay}-{sendOrderToBOBTime}";//北京银行---存储商户将订单信息发往BOB时的时间

    String PAY_ORDER_DESC = "{pay}-{orderDesc}";//北京银行---存储订单支付或充值时的订单描述

    String PAY_REFUND_INFO = "{pay}-{refundOrderInfo}";//北京银行---存储商户退款信息

    String UPDATE_ORDER_STATUS = "{pay}-{update-order-status}";

    String PAY_BILL99_REFUND_INFO = "{pay}-{bill99RefundOrderInfo}";//99bill---存储商户退款信息

    String PAY_BILL99_QUICK_REFUND_INFO = "{pay}-{bill99QuickRefundOrderInfo}";//99bill---存储快捷商户退款信息

    String RETURN_AMOUNT_PARAMS = "{order}-{ReturnAmountParamsToRedis}";//订单系统存储退款参数

    String WITHDRAW_TADENO = "{pay}-{WithdrawTadeNoToRedis}";//存储提现outTradeNo

    String STORE_WITHDRAWID_SENDMSG = "{pay}-{StoreWithdrawIdSendMsg}";//提现id 存redis 发短信 key

    String SHOP_ADDSHOP_REDIS = "{shop}{addShopRedis}";//获取redis中的shop信息 key

    String QUERY_BALANCE_HAT = "{pay}{queryBalanceHAT}";//获取redis中的shop信息 key

    String SETTLE_WHIT_HAT = "{pay}-{settleWithHAT}";//获取redis中的shop信息 key

    String BALANCE_CHANGE = "{pay}-{balanceChange}";//余额发生变动的 key

    String BALANCE_WITHDRAWTO = "{pay}-{withdrawto}";//余额发生变动的 key

    String BALANCE_FREEZEN_KEY = "{pay}-{balanceFreezenKey}";//冻结信息暂存的Key

    String REFUND_KEY = "{pay}-{refundKey}";//退款的锁前缀

    String CREATE_TIME = "createTime";

    String ACC_FLOW_ID = "accFlowId";

    String CURRENT_TIME = "currentTime";

    Short DIRECTION_IN = 1; //资金流向之流入

    Short DIRECTION_OUT = 2;//资金流向之流出

    String NICK_NAME = "nickName";//公司名

    String FROZEN_AMOUNT = "冻结金额";
    String BALANCE = "余额";
    String CHANNEL_NAME_ZHIFUBAO = "支付宝";
    String CHANNEL_NAME_WEIXIN = "微信";
    String CHANNEL_NAME_BOC = "中国银行";
    String CHANNEL_NAME_BOB = "北京银行网银";
    String CHANNEL_UNION_PAY = "银联（北京银行）";
    String CHANNEL_KUAIQIAN = "快钱网银";
    String CHANNEL_KUAIQIAN_QUICK = "快钱快捷";
    String BALANCE_KUAIQIAN = "快钱余额混合还款";
    String UNIONPAY = "中国银联";
    String UNIONPAY_WEBWAP = "中国银联WebWap";
    String UNIONPAY_QRCODE = "中国银联QrCode";
    String UNIONPAY_H5Pay = "中国银联H5Pay";
    String UNIONPAY_H5Pay_B2B = "中国银联H5PayB2B";
    String CART_TYPE_DC = "DC";
    String CART_TYPE_CC = "CC";
    String CART_TYPE_SCC = "SCC";
    String CART_TYPE_PC = "PC";
    String WHITE = "白条";

    String BOB_CODE_SUCCESS = "0000";//北京银行交易成功状态码

    Integer HTTP_STATUS_OK = 200;

    Integer TEN = 10;

    String SUNKFA = "顺景发";
    String RECEIVERNAME = "顺景发国际食品(北京)有限公司";
    String ORDER_TO_BOB_ERROR_URL = "http://mall-dev.ewfresh.com/i/paySuccess.html";

    Short ACC_TYPE_1 = 1;//账户类型1:支付宝
    Short ACC_TYPE_2 = 2;//账户类型2:微信
    Short ACC_TYPE_3 = 3;//账户类型3:银行卡
    Short ACC_TYPE_4 = 4;//账户类型4:余额
    Short ACC_TYPE_5 = 5;//账户类型5:快钱
    Short ACC_TYPE_6 = 6;//账户类型6:信用
    Short ACC_TYPE_7 = 7;//账户类型6:银联

    Short BUSI_TYPE_1 = 1;//业务类型1:线上充值
    Short BUSI_TYPE_2 = 2;//业务类型2:提现
    Short BUSI_TYPE_3 = 3;//业务类型3:订单扣款
    Short BUSI_TYPE_4 = 4;//业务类型4:结算扣款
    Short BUSI_TYPE_5 = 5;//业务类型5:销售收款
    Short BUSI_TYPE_6 = 6;//业务类型6:退款收入
    Short BUSI_TYPE_7 = 7;//业务类型7:冻结
    Short BUSI_TYPE_8 = 8;//业务类型7:释放冻结金额
    Short BUSI_TYPE_9 = 9;//业务类型9:配货补款
    Short BUSI_TYPE_10 = 10;//业务类型10:配货退款
    Short BUSI_TYPE_11 = 11;//业务类型11:线下充值
    Short BUSI_TYPE_12 = 12;//业务类型12:索赔退款
    Short BUSI_TYPE_13 = 13;//业务类型13:无货到索赔退款
    Short BUSI_TYPE_14 = 14;//业务类型14:订单退款
    Short BUSI_TYPE_15 = 15;//业务类型15:充值错误提现
    Short BUSI_TYPE_16 = 16;//业务类型16:提现冻结
    Short BUSI_TYPE_17 = 17;//业务类型17:账户
    Short BUSI_TYPE_18 = 18;//业务类型18:红包奖励
    Short BUSI_TYPE_19 = 19;//业务类型19:信用还款
    Short BUSI_TYPE_20 = 20;//业务类型20:被动还款
    Short BUSI_TYPE_21 = 21;//业务类型21:还款冻结
    Short BUSI_TYPE_22 = 22;//业务类型22:退货退款

    Short RECEIVABLES_BUSI_TYPE_1 = 1;//业务类型1:线上充值
    Short RECEIVABLES_BUSI_TYPE_2 = 2;//业务类型2:提现
    Short RECEIVABLES_BUSI_TYPE_3 = 3;//业务类型3:订单扣款
    Short RECEIVABLES_BUSI_TYPE_4 = 4;//业务类型4:结算扣款
    Short RECEIVABLES_BUSI_TYPE_5 = 5;//业务类型5:销售收入
    Short RECEIVABLES_BUSI_TYPE_6 = 6;//业务类型6:退款收入
    Short RECEIVABLES_BUSI_TYPE_7 = 7;//业务类型7:冻结
    Short RECEIVABLES_BUSI_TYPE_8 = 8;//业务类型7:释放冻结金额
    Short RECEIVABLES_BUSI_TYPE_9 = 9;//业务类型9:配货补款
    Short RECEIVABLES_BUSI_TYPE_10 = 10;//业务类型10:配货退款
    Short RECEIVABLES_BUSI_TYPE_11 = 11;//业务类型11:线下充值
    Short RECEIVABLES_BUSI_TYPE_12 = 12;//业务类型12:索赔退款
    Short RECEIVABLES_BUSI_TYPE_13 = 13;//业务类型13:无货到索赔退款
    Short RECEIVABLES_BUSI_TYPE_14 = 14;//业务类型14:订单退款
    Short RECEIVABLES_BUSI_TYPE_15 = 15;//业务类型15:充值错误提现
    Short RECEIVABLES_BUSI_TYPE_16 = 16;//业务类型16:提现冻结
    Short RECEIVABLES_BUSI_TYPE_17 = 17;//业务类型17:订单完结扣款
    Short RECEIVABLES_BUSI_TYPE_18 = 18;//业务类型18:处置费处理
    Short RECEIVABLES_BUSI_TYPE_19 = 19;//业务类型19:红包奖励
    Short RECEIVABLES_BUSI_TYPE_20 = 20;//业务类型20:信用还款
    Short RECEIVABLES_BUSI_TYPE_21 = 21;//业务类型21:信用被动还款
    Short RECEIVABLES_BUSI_TYPE_22 = 22;//业务类型22:退货退款

    Short STATUS_0 = 0;//状态 0:成功,1:失败,2:处理中
    Short STATUS_1 = 1;//状态 0:成功,1:失败,2:处理中
    Short STATUS_2 = 2;//状态 0:成功,1:失败,2:处理中

    Short TRADE_TYPE_0 = 0;  //0:订单支付时，耀辉放Redis的值为0
    Short TRADE_TYPE_1 = 1;  //1:订单
    Short TRADE_TYPE_2 = 2;  //2,取消订单退款
    Short TRADE_TYPE_3 = 3;  //3,线下充值
    Short TRADE_TYPE_4 = 4;  //4,线上充值
    Short TRADE_TYPE_5 = 5;  //5,提现
    Short TRADE_TYPE_6 = 6;  //6,商户结算打款
    Short TRADE_TYPE_7 = 7;  //7,平台增值服务收款
    Short TRADE_TYPE_8 = 8;  //8,配货补款(原配货扣款)
    Short TRADE_TYPE_9 = 9;  //9,配货退款
    Short TRADE_TYPE_10 = 10;//10,索赔退款
    Short TRADE_TYPE_11 = 11;//11,无货到索赔退款
    Short TRADE_TYPE_12 = 12;//12,店铺账户HAT提现
    Short TRADE_TYPE_13 = 13;//13,店铺退货结算
    Short TRADE_TYPE_14 = 14;//14,红包奖励
    Short TRADE_TYPE_15 = 15;//15,信用还款
    Short TRADE_TYPE_16 = 16;//16,店铺保证金
    Short TRADE_TYPE_17 = 17;//17,退货退款
    Short TRADE_TYPE_18 = 18;//18,关闭订单退款


    Short IS_REFUND_YES = 1;//是否退款，0否1是
    Short IS_REFUND_NO = 0; //是否退款，0否1是


    String B2B_FLAG = "_B2B";//B2B支付标识
    String EARNEST_PAYFLOWS = "earnestpayflows";
    String FINAL_PAYFLOWS = "finalPayFlows";
    String DISPATCH_PAYFLOWS = "dispatchPayFlows";
    String EARNEST_REFUND_PAYFLOWS = "earnestRefundPayFlows";
    String FINAL_REFUND_PAYFLOWS = "finalRefundPayFlows";

    String UID_BALANCE = "1000";

    String UID = "uid"; //支付宝1001 微信1002 中国银行1003
    String UID_BOC = "1003"; //支付宝1001 微信1002 中国银行1003 北京银行1004
    String UID_BOB = "1004"; //支付宝1001 微信1002 中国银行1003 北京银行1004
    String UID_BILL = "1006"; //支付宝1001 微信1002 中国银行1003 北京银行1004 快钱1006
    String UID_WHITE = "1065";//支付宝1001 微信1002 中国银行1003 北京银行1004 快钱1006 白条1065
    String UID_UNIONPAY = "1066";//支付宝1001 微信1002 中国银行1003 北京银行1004 快钱1006 白条1065 中国银联1066
    String STR_ZERO = "0";
    String STR_ONE = "1";
    String STR_TWO = "2";
    String STR_THREE = "3";

    Integer INTEGER_ZERO = 0;//Integer类型的0
    Integer INTEGER_ONE = 1;//Integer类型的1
    Integer INTEGER_TWO = 2;//Integer类型的2
    Integer INTEGER_THREE = 3;//Integer类型的3
    Integer INTEGER_FOUR = 4;//Integer类型的4
    Integer INTEGER_FIVE = 5;//Integer类型的5
    Integer INTEGER_SIX = 6;//Integer类型的6
    Integer INTEGER_FORTY_FIVE = 45;//Integer类型的45
    Integer INTEGER_SIXTY_SIX = 66;
    Integer INTEGER_SIXTY_SEVEN = 67;
    Integer INTEGER_SIXTY_EIGHT = 68;
    Integer INTEGER_SIXTY_NINE = 69;
    Integer INTEGER_SEVENTY = 70;
    Integer INTEGER_SEVENTY_ONE = 71;
    int INT_TEN = 10;//int类型的10
    Integer INTEGER_THIRTY = 30;//Integer类型的30, 订单支付超时网络传输误差
    Integer INTEGER_FIFTY = 50;//Integer类型的50, 订单批量查询的数量最大值
    Integer INTEGER_100 = 100;//Integer类型的100
    Integer INTEGER_90 = 90;//Integer类型的90 (微信支付-90s)
    Integer INTEGER_20000 = 20000;//Integer类型的10000 用于Httpclient请求的connectionTimeout时间

    Integer CHANNEL_CODE_ALIPAY = 1001;//支付渠道编号_AliPay
    Integer CHANNEL_CODE_WXPAY = 1002;//支付渠道编号_WXPay
    Integer CHANNEL_CODE_BOC = 1003;//支付渠道编号_BOC
    Integer CHANNEL_CODE_BOB = 1004;//支付渠道编号_BOB
    Integer CHANNEL_CODE_QUICK_PAY = 1005;//QuickPay编号
    Integer CHANNEL_CODE_KUAIQIAN = 1006;//快钱网银编号
    Integer CHANNEL_CODE_KUAIQIAN_ENTERPRISE = 1035;//快钱企业网银
    Integer CHANNEL_CODE_KUAIQIAN_QUICK = 1045;//快钱快捷编号
    Integer CHANNEL_CODE_UNIONPAY_WEBWAP = 1067;//中国银联WebWap
    Integer CHANNEL_CODE_UNIONPAY_QRCODE = 1068;//中国银联QrCode
    Integer CHANNEL_CODE_UNIONPAY_H5Pay = 1069;//中国银联H5Pay
    Integer CHANNEL_CODE_UNIONPAY_H5Pay_B2B = 1070;//中国银联H5PayB2B
    Integer CHANNEL_CODE_BALANCE = 1000; // 余额支付

    Short SHORT_ZERO = 0;//Short类型的0
    Short SHORT_ONE = 1;//Short类型的1
    Short SHORT_TWO = 2;//Short类型的2
    Short SHORT_THREE = 3;//Short类型的3
    Short SHORT_FOUR = 4;//Short类型的4
    Short SHORT_FIVE = 5;//Short类型的5
    Short SHORT_SEVEN = 7;//Short类型的7

    String FATE_ALI = "0.055";// 阿里费率
    String FATE_WX = "0.00604";// 微信费率
    String FATE_BOC_B2C_CELLPHONE = "0.0025";// 中行B2C手机费率
    String FATE_BOC_B2C_COMPUTER = "0.0025";// 中行B2C PC费率
    String FATE_BOC_YINLIAN_COMPUTER = "0.006";// 中行银联PC费率
    String FATE_BOC_YINLIAN_CELLPHONE = "0.008";// 中行银联手机费率
    String FATE_BOC_B2B_COMPUTER = "？";// TODO 中行B2B费率 每笔2元，跨行6元
    String FATE_BOB_BORROW = "0.0038";// 网银北京银行费率 TODO 问晓佳要  北京银行线上收单借记卡千分之3.8，贷记卡千分之5.8
    String FATE_BOB_CREDIT = "0.0058";// 网银北京银行费率 TODO 问晓佳要  北京银行线上收单借记卡千分之3.8，贷记卡千分之5.8

    String TOKEN = "token";

    String PAY_CHANNEL = "payChannel";//支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）6快钱网银 45快钱快捷 67中国银联WebWap 68中国银联QrCode

    String CHANNEL_FLOW_ID = "channelFlowId";//支付渠道流水号

    String INTERACTION_ID = "interactionId";//第三方交互订单号

    String SHOP_ID = "shopId";//店铺Id

    String SHOP = "shop";//店铺

    String UNION_SHOP_INFO = "unionShopInfo";//店铺

    String FREIGHT = "freight";//运费

    String SHAREBENEFIT = "shareBenefit";//店铺分润

    String SHOP_NAME = "shopName";//店铺Id

    String PAYER_ID = "payerId";//付款人ID

    String PAYER_PAY_AMOUNT = "payerPayAmount";//付款方支付金额

    String PAYER_TYPE = "payerType";//付款账号类型(1个人,2店铺)

    String RECEIVER_TYPE = "receiverType";//收款账号类型(1个人,2店铺)

    String RECEIVER_USER_ID = "receiverUserId";//收款人ID

    String RECEIVER_NAME = "receiverName";//收款人名称

    String RECEIVER_FEE = "receiverFee";//收款方手续费

    String PLATINCOME = "platIncome";//平台收入

    String CHANNEL_CODE = "channelCode";//支付渠道编号

    String CHANNEL_NAME = "channelName";//支付渠道名称

    String TYPE_CODE = "typeCode";//支付类型编号

    String TYPE_NAME = "typeName";//支付类型名称

    String SUCCESS_TIME = "successTime";//支付成功时间

    String IS_REFUND = "isRefund";//是否退款 0:否,1是

    String TRADE_TYPE = "tradeType";//交易类型  1:订单,2,退款,3,线下充值,4线上充值,5:提现,6:商户结算打款,7:平台增值服务收款

    String UNAME = "uname";

    /*2019年6月27日10:24:14*/
    String MID = "mid";//付款商户号
    String TID = "tid";//付款终端号
    String SHOP_BENEFIT_PEERCENT = "shopBenefitPercent";//店铺分润比例
    String SHOP_BENEFIT_MONEY = "shopBenefitMoney";//店铺分润金额
    String FRIGHT = "fright";//运费
    String PLATFORM_EWFRESH_BENEFIT = "platformEwfreshBenefit";//第三方ewfresh所得服务费
    String FEE_RATE = "feeRate";
/****************************************************/

    String BALANCE_REPAY_OK = "BalanceRepayIsOk";
    String RETURN_INFO = "returnInfo";//返回信息

    String DESP = "desp";//描述
    String SUCCESS = "SUCCESS";//成功
    String success = "success";//成功
    String FAIL = "FAIL";//失败
    String BUY_GOODS = "buyGoods";//购买商品
    String BUY_GOODS_OR_RECHARGE = "buyGoodsOrRecharge";//购买商品或充值
    String RECHARGE = "recharge";//充值
    String SETTLERECORD = "settleRecord";//结算
    String IS_RECHARGE = "isRecharge";//充值
    String REFUND_ORDER = "refundOrder";//退款

    String ID_WHITE_ORDER_KEY = "whiteOrderId"; //余额还款的订单ID号

    String ID_GEN_WHITEPAY_BILL = "whitePay_bill";//信用支付的渠道流水号
    String ID_GEN_PAY_VALUE = "pay_bill"; //余额支付的流水号
    String ID_GEN_ORDER_VALUE = "order"; //余额支付的流水号
    String ID_RECHARGE_VALUE = "recharge"; //充值的的流水号
    String ID_GEN_KEY = "firstKey";

    /*redis 中获取order信息的key start*/
    String ID = "id";                    //  订单号
    String ORDER_ID = "orderId";         //处理过后的订单ID
    String ORDER_STATUS = "orderStatus"; // 订单状态
    String BEFORE_ORDER_STATUS = "beforeOrderStatus"; // 订单前置状态
    String PAY_METHOD = "payMethod";     // 支付方式（定金 /全款）
    String PAY_MENT = "payment";         // 应付金额
    String PAY_MODE = "payMode";         // 支付渠道
    String ORDER_IP = "orderIp";         // 下单ip
    String BANLANCE = "balance";         // 余额
    String BILLFLOW = "billFlow";         // 流水号
    String SURPLUS = "surplus";          // 剩余应付金额
    String ORDER_AMOUNT = "orderAmount"; //订单金额
    String PAY_TIMESTAMP = "payTimestamp"; //时间戳
    /*redis 中获取order信息的key end*/
    String FIRSTKEY = "firstKey";//id生成器的参数名
    String PAY_RANDOM_NUM = "{pay}-{checkPayPWD}";

    /* 中国银行公共参数 START */
//    String ROOT_CERTIFICATE_PATH = "bocproperties/BOCCA.cer";// BOC根证书库名称
    String ROOT_CERTIFICATE_PATH = "bocproperties/newest-B2B-B2C-stage(T4)-public.cer";// BOC根证书库名称
    String BOC_CONFIG_PROPERTIES = "bocproperties/boc-config.properties";// BOC配置文件名称
    String B2C_PRIVATE = "bocproperties/B2C_private_mm_1111111a.pfx";// BOC私钥库名称
    String MERCHANT_NO = "merchantNo";// 商户号
    String SIGN_DATA = "signData";// 中行签名数据
    String ORDER_NO = "orderNo";// 商户订单号
    String ORDER_SEQ = "orderSeq";// 银行订单流水号
    String CARD_TYP = "cardTyp";// 银行卡类别
    String PAY_TIME = "payTime";// 支付时间
    String PAY_AMOUNT = "payAmount";// 支付金额
    String ACCT_NO = "acctNo";//支付卡号
    String HOLDER_NAME = "holderName";//持卡人姓名
    String IBKNUM = "ibknum";//支付卡省行联行号
    String ORDER_REFER = "orderRefer";// 客户浏览器Refer信息
    String BANK_TRAN_SEQ = "bankTranSeq";// 银行交易流水号
    String RETURN_ACT_FLAG = "returnActFlag";// 返回操作类型
    String PHONE_NUM = "phoneNum";// 电话号码
    String M_REFUND_SEQ = "mRefundSeq";// 商户系统产生的交易流水号
    String REFUND_AMOUNT = "refundAmount";// 退款总金额
    String TRAN_TIME = "tranTime";// 银行交易时间：YYYYMMDDHHMISS
    String DEAL_STATUS = "dealStatus";// 处理状态0：成功 1：失败 2：未明
    String EXTEND = "extend";// 附加域
    String FILE_DATE = "fileDate";// 需要下载的文件的日期 格式：YYYYMMDD
    String FILE_TYPE = "fileType";// 文件类型
    String HANDLE_TYPE = "handleType";// 操作类型
    String INVALID_TIME = "invalidTime";// 票失效时间 YYYYMMDD24HHMMSS
    String SUBMIT_TIME = "submitTime";// 提交时间 YYYYMMDD24HHMMSS
    String TICKET_ID = "ticketId";// 票号
    String URI = "uri";// URI标识符，上传、下载文件时需要
    String URL = "url";// url
    String METHOD_NAME = "methodName";// 方法名
    String METHOD_SEND_ORDER = "sendOrder";//商户向BOC发送订单支付请求的方法名
    String METHOD_NOTIFY_LOCAL_HANDLE = "notifyLocalHandle";//订单支付回调数据持久化到本地的方法名
    String METHOD_RECEIVE_NOTIFY = "receiveNotify";//商户接收订单支付通知的方法名
    String METHOD_REFUND_ORDER = "refundOrder";//商户退单的方法名
    String METHOD_GET_TICKET = "getTicket";//取票的方法名
    String FILE_TYPE_GS = "GS";//B2C业务对账文件
    String FILE_TYPE_CC = "CC";//B2C清算对账文件
    String FILE_TYPE_RA = "RA";//B2C退货反馈文件
    String FILE_TYPE_TS = "TS";//B2C交易流水文件
    String FILE_TYPE_XY = "XY";//B2C客户签约文件
    String FILE_TYPE_YZ = "YZ";//B2C客户身份认证文件
    String FILE_TYPE_MRA = "MRA";//B2B退货反馈文件
    String FILE_TYPE_PGS = "PGS";//B2B交易流水文件
    String FILE_TYPE_MCC = "MCC";//B2B清算对账文件
    String FILE_TYPE_MRS = "MRS";//B2B跨行汇款退回文件
    String FILE_TYPE_URA = "URA";//B2C商户退货文件
    String FILE_TYPE_UMRA = "UMRA";//B2B商户退货文件
    /* 中国银行公共参数 END */

    /* 北京银行公共参数 START */
    String BOB_BACK_END_URL = "backEndUrl";// 后台通知地址，用于接收交易结果
    String BOB_CERT_ID = "certId";// 证书序列号
    String BOB_CHANNEL_TYPE = "channelType";// 07：互联网； 08：移动
    String CONTENT_TYPE_TEXT_HTML = "text/html;charset=utf-8";
    String BOB_ENCODING = "encoding";//	默认取值：UTF-8
    String UTF_8 = "UTF-8";// UTF-8
    String BOB_FRONT_END_URL = "frontEndUrl";// 回调地址，返回商户页面
    String BOB_FRONT_FAIL_URL = "frontFailUrl";//	前台消费交易若商户上送此字段，则在支付失败时，页面跳转至商户该URL（不带交易信息，仅跳转）
    String BOB_IP = "ip";//	持卡人IP地址
    String BOB_MER_ID = "merId";// 北京银行颁发的商户唯一标识
    String BOB_MER_TYPE = "merType";// 0普通商户； 1平台商户； 2平台类需二次清分商户
    String BOB_MER_TYPE_ZERO = "0";// 0普通商户； 1平台商户； 2平台类需二次清分商户
    String BOB_MER_TYPE_ONE = "1";// 0普通商户； 1平台商户； 2平台类需二次清分商户
    String BOB_MER_TYPE_TWO = "2";// 0普通商户； 1平台商户； 2平台类需二次清分商户
    String BOB_ORDER_AMOUNT = "orderAmount";// 以分为单位
    String BOB_ORDER_DESC = "orderDesc";// 订单描述
    String BOB_ORDER_NUMBER = "orderNumber";// 支付交易订单号(请勿重复使用)
    String BOB_ORDER_TIME = "orderTime";// 商户发送交易时间yyyyMMddHHmmss
    String BOB_PAY_TYPE = "payType";// 01银联； 02北京银行
    String BOB_QUERY_ID = "queryId";// 支付或退款返回的北京银行查询流水号
    String BOB_REFUND_SEQ = "refundSeq";// 由商户提供12位
    String BOB_RES_CODE = "resCode";// 见“交易返回码相关说明”章节
    String BOB_RES_DESC = "resDesc";//
    String BOB_SHIPPING_FLAG = "shippingFlag";// 物流及风险信息，详见文档
    String BOB_SIGNATURE = "signature";// 填写对报文摘要的签名
    String BOB_SIGN_METHOD = "signMethod";// 取值：01（表示采用RSA）
    String BOB_SUB_MER_ABBR = "subMerAbbr";// 商户类型为“2”时上送
    String BOB_SUB_MER_ID = "subMerId";// 商户类型为“2”时上送
    String BOB_SUB_MER_NAME = "subMerName";// 商户类型为“2”时上送
    String BOB_TRANS_TYPE = "transType";// 01-消费 04-退款   查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
    String BOB_TRANS_TYPE01 = "01";// 01-消费 04-退款   查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
    String BOB_TRANS_TYPE04 = "04";// 01-消费 04-退款   查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
    String BOB_TRANS_TYPE35 = "35";// 01-消费 04-退款   查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
    String BOB_TRANS_TYPE36 = "36";// 01-消费 04-退款   查询交易根据查询的交易类型确定（交易类型01支付、35全部退款、36部分退款）
    String BOB_DATE = "date";//	对账单清算时间
    String BOB_COUNT = "count";// 当日对账单明细数量
    String BOB_ORDER_ACCOUNT = "orderAccount";// 该域为循环域，需要逐个解析子域account中的内容。
    String BOB_ACCOUNT = "account";// 交易明细
    String BOB_ID = "id";// 订单号
    String BOB_CODE = "code";// 交易码   6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_AMT = "amt";// 对账金额
    String BOB_RS = "rs";// S：成功 N：待入账 其他失败
    String BOB_RS_S = "S";// S：成功 N：待入账 其他失败
    String BOB_RS_N = "N";// S：成功 N：待入账 其他失败
    String BOB_CODE_6001 = "6001";// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_CODE_6011 = "6011";// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_CODE_6005 = "6005";// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_CODE_6015 = "6015";// 交易码 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_DESP_6001 = "银联支付交易";// 描述 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_DESP_6011 = "银联退款";// 描述 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_DESP_6005 = "北京银行支付";// 描述 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_DESP_6015 = "北京银行退款";// 描述 6001：银联支付交易  6011：银联退款  6005：北京银行支付 6015：北京银行退款
    String BOB_CHANNEL_YLBJ = "银联北京银行";// 渠道名称
    String BOB_RESERVE_1 = "reserve1";// 暂未启用
    String BOB_RESERVE_2 = "reserve2";// 暂未启用

    String SIGN_METHOD_RSA = "01";// RSA签名算法
    /* 北京银行公共参数 END */


    /* 快钱网银公共参数 START */
    String BILL99_MERCHANT_ID = "merchant_id";//人民币网关商户编号
    String BILL99_MERCHANT_ACCT_ID = "merchantAcctId";//人民币网关账号，该账号为11位人民币网关商户编号+01,该值与提交时相同。
    String BILL99_VERSION = "version";//网关版本，固定值：v2.0,该值与提交时相同。
    String BILL99_LANGUAGE = "language";//语言种类，1代表中文显示，2代表英文显示。默认为1,该值与提交时相同。
    String BILL99_SIGN_TYPE = "signType";//签名类型,该值为4，代表PKI加密方式,该值与提交时相同。
    String BILL99_PAY_TYPE = "payType";//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10,该值与提交时相同。
    String BILL99_BANK_ID = "bankId";//银行代码，如果payType为00，该值为空；如果payType为10,该值与提交时相同。
    String BILL99_ORDER_ID = "orderId";//商户订单号，该值与提交时相同。
    String BILL99_ORDER_iD = "orderid";//商户订单号，该值与提交时相同。
    String BILL99_R_ORDER_ID = "rOrderId";//商户支付时的订单号。
    String BILL99_ORDER_TIME = "orderTime";//订单提交时间，格式：yyyyMMddHHmmss，如：20071117020101,该值与提交时相同。
    String BILL99_ORDER_AMOUNT = "orderAmount";//订单金额，金额以“分”为单位，商户测试以1分测试即可，切勿以大金额测试,该值与支付时相同。
    String BILL99_BIND_CARD = "bindCard";//已绑短卡号,信用卡快捷支付绑定卡信息后返回前六后四位信用卡号
    String BILL99_BIND_MOBILE = "bindMobile";//已绑短手机尾号,信用卡快捷支付绑定卡信息后返回前三位后四位手机号码
    String BILL99_DEAL_ID = "dealId";// 快钱交易号，商户每一笔交易都会在快钱生成一个交易号。
    String BILL99_BANK_DEAL_ID = "bankDealId";//银行交易号 ，快钱交易在银行支付时对应的交易号，如果不是通过银行卡支付，则为空
    String BILL99_DEAL_TIME = "dealTime";//快钱交易时间，快钱对交易进行处理的时间,格式：yyyyMMddHHmmss，如：20071117020101
    String BILL99_PAY_AMOUNT = "payAmount";//商户实际支付金额 以分为单位。比方10元，提交时金额应为1000。该金额代表商户快钱账户最终收到的金额。
    String BILL99_AMOUNT = "amount";//
    String BILL99_FEE = "fee";//费用，快钱收取商户的手续费，单位为分。
    String BILL99_EXT1 = "ext1";//扩展字段1，该值与提交时相同。
    String BILL99_EXT2 = "ext2";//扩展字段2，该值与提交时相同。
    String BILL99_PAY_RESULT = "payResult";//处理结果， 10支付成功，11 支付失败，00订单申请成功，01 订单申请失败
    String BILL99_ERR_CODE = "errCode";//错误代码 ，请参照《人民币网关接口文档》最后部分的详细解释。
    String BILL99_SIGN_MSG = "signMsg";//签名字符串
    String BILL99_INPUT_CHARSET = "inputCharset";//固定选择值：1、 2、 3.                 1 代表 UTF-8; 2 代表 GBK; 3 代表 GB2312
    String BILL99_PAGE_URL = "pageUrl";//接 受 支 付 结 果 的 页 面 地 址
    String BILL99_BG_URL = "bgUrl";//服 务 器 接 受 支 付 结 果 的 后台地址
    String BILL99_PAYER_NAME = "payerName";//支付人姓名
    String BILL99_PAYER_CONTACT_TYPE = "payerContactType";//支付人联系方式类型,固定值：1 或者 2.     1 代表电子邮件方式；2 代表手机联系方式
    String BILL99_PAYEE_CONTACT_TYPE = "payeeContactType";//支付人联系方式类型,固定值：1 或者 2.     1 代表电子邮件方式；2 代表手机联系方式
    String BILL99_PAYER_CONTACT = "payerContact";//支 付 人 联 系 方式
    String BILL99_PAYEE_CONTACT = "payeeContact";//支 付 人 联 系 方式
    String BILL99_PAYER_ID_TYPE = "payerIdType";//指定付款人
    String BILL99_PAYER_ID = "payerId";//付款人标识
    String BILL99_PRODUCT_NAME = "productName";//商品名称
    String BILL99_PRODUCT_NUM = "productNum";//商品数量
    String BILL99_PRODUCT_ID = "productId";//商品代码
    String BILL99_PRODUCT_DESC = "productDesc";//商品描述
    String BILL99_REDO_FLAG = "redoFlag";//同一订单禁止重复提交标志,固定选择值：1、0; 1代表同一订单号只允许提交1次；0表示同一订单号在没有支付成功的前提下可重复提交多次。默认为0 ,建议实物购物车结算类商户采用0；虚拟产品类商户采用1；
    String BILL99_PID = "pid";//合 作 伙 伴 在 快 钱 的 用 户 编 号
    String BILL99_ORDER_TIME_OUT = "orderTimeOut";//订单支付超时时间
    String BILL99_START_DATE = "startDate";//退款生成时间起点
    String BILL99_END_DATE = "endDate";//退款生成时间终点
    String BILL99_REQUEST_PAGE = "requestPage";//在查询结果数据总量很大时，快钱会将支付结果分多次返回。本参数表示商户需要得到的记彔集页码。 默认为 1，表示第 1 页
    String BILL99_RECORD_COUNT = "recordCount";//
    String BILL99_PAGE_COUNT = "pageCount";//
    String BILL99_CURRENT_PAGE = "currentPage";//
    String BILL99_PAGE_SIZE = "pageSize";//
    String BILL99_STATUS = "status";//交易状态, 0 代表进行中           1 代表成功            2 代表失败
    String BILL99_KEY = "key";//
    String BILL99_POSTDATE = "postdate";//退款提交时间
    String BILL99_TXORDER = "txOrder";//退款流水号  字符串
    String BILL99_MERCHANT_KEY = "merchant_key";//
    String BILL99_COMMAND_TYPE = "command_type";//
    String BILL99_MAC = "mac";//加密串
    String BILL99_ID = "id";//
    String BILL99_ORDER_STATUS = "orderStatus";//订单状态
    String BILL99_BEFORE_ORDER_STATUS = "beforeOrderStatus";//订单前置状态
    String BILL99_IF_ADD_ORDER_RECORD = "ifAddOrderRecord";//是否添加订单操作记录，0否1是
    String BILL99_GB2312 = "gb2312";
    String BILL99_BCOM_B2B = "BCOM_B2B";//交通银行企业网银bankId
    String BILL99_ONLY_BANKCARD = "10";//支付方式: 10 代表只显示银行卡支付方式；
    String BILL99_COMPANY_E_BANK = "14";//支付方式: 14 代表企业网银；
    String BILL99_QUICK_PAY = "21";//支付方式: 21 代表快捷支付；
    /* 快钱公共参数 END */


    /* 快钱快捷公共参数 START */
    String BILL99_Q_CARD_TYPE = "cardType";// 银行卡类型: 0001 信用卡类型 0002 借记卡类型
    String BILL99_Q_CARD_LOAN = "0001";// 卡类型，0001 信用卡类型 0002 借记卡类型
    String BILL99_Q_CARD_BORROW = "0002";// 卡类型，0001 信用卡类型 0002 借记卡类型
    String BILL99_Q_ISSUER = "issuer";// 银行名称
    String BILL99_Q_BANK_ID = "bankId";// 银行代码
    String BILL99_Q_VALID_FLAG = "validFlag";// 快钱是否支持
    String BILL99_Q_PAY_TOKEN = "payToken";// 签约协议号
    String BILL99_Q_TXN_STATUS = "txnStatus";// 订单支付交易状态: ‘S’－交易成功 ‘F’－交易失败 ‘P’－交易挂起,     交易类型为退货则: ’S’—退货申请成功 ‘F’－交易失败 ‘D’—已提交收单行
    String BILL99_Q_TXN_STATUS_PAY_S = "S";// 订单支付交易状态: ‘S’－交易成功
    String BILL99_Q_TXN_STATUS_PAY_F = "F";// 订单支付交易状态: ‘F’－交易失败
    String BILL99_Q_TXN_STATUS_PAY_P = "P";// 订单支付交易状态: ‘P’－交易挂起
    String BILL99_Q_TXN_STATUS_RETURN_S = "S";// 订单退货交易状态: ‘S’—退货申请成功
    String BILL99_Q_TXN_STATUS_RETURN_F = "F";// 订单退货交易状态: ‘F’－交易失败
    String BILL99_Q_TXN_STATUS_RETURN_D = "D";// 订单退货交易状态: ‘D’—已提交收单行
    String BILL99_Q_PERSONAL_PC = "快钱个人网银";// 渠道名称: 快钱个人网银
    String BILL99_Q_COM_PC = "快钱企业网银";// 渠道名称: 快钱企业网银
    String BILL99_Q_PERSONAL_QUICK = "快钱个人快捷";// 渠道名称: 快钱个人快捷
    String BILL99_Q_RESPONSE_CODE_C0 = "C0";// C0: 快钱内部处理中（未完成）、最终交易结果未知
    String BILL99_Q_RESPONSE_CODE_68 = "68";// 银行内部处理中（未完成）、最终交易结果未知
    String BILL99_Q_REF_NUMBER = "refNumber";// 系统参考号
    String BILL99_Q_TRANS_TIME = "transTime";// 交易时间
    String RESPONSE_CODE_OK = "00";// 响应OK
    String TXNTYPE_PUR = "PUR";// 消费交易
    String TXNTYPE_RFD = "RFD";// 退货交易


    String CARDQUERYNULL = "卡信息查询为空";
    String PARAMERR = "参数错误";
    String CARDQUERYERROR = "卡信息查询失败";
    String BINDCARDWITHOUTCODERETURNNULL = "绑卡失败(响应为空)";
    String BINDCARDWITHOUTCODEERROR = "绑卡失败";
    String NOTORIGINCUSTOMERBINDCARD = "该卡已被其他人绑定";
    String CARDHASBENNBOUND = "该卡已绑定，无需重复绑卡";
    String GETTOKENBEFOREBINDCARDERROR = "获取验证码失败";
    String BINDCARDWITHCODERETURNNULL = "绑卡失败";
    String BINDCARDWITHCODEERROR = "首次绑定银行卡失败";
    String PCIQUERYNULL = "绑卡信息为空";
    String PCIQUERYERROR = "查询卡信息发生异常";
    String VALIDCODEEXPIRED = "验证码过期";
    String VALIDCODEERROR = "验证码错误";
    String PCIDELETERROR = "解绑失败请重试";
    String CARDISNOTDEFAULTANYMORE = "该卡已被解绑，不能设为默认银行卡";
    String QUICKPAYCOMMONRESNULL = "一键支付响应为空";
    String DYNAMICPAYBILL99ERROR = "系统异常请稍后重试";
    String KUAIQIANPAYHANDLING = "快钱正在处理中";
    String BANKPAYHANDLING = "银行正在处理中";
    String BILL99HANGUP = "快钱将交易挂起";
    String QUICKPAYCOMMONBILL99ERROR = "快钱发生异常，请联系客服";
    String GETDYNUMRESNULLFORDYPAY = "获取支付动态码发生错误";
    String QUICKPAYDYNAMICCODERESNULL = "动态码支付响应为空";
    String QUERYORDERRESNULL = "订单查询响应为空";
    String QUERYORDERERROR = "订单查询快钱发生错误";
    String REFUNDORDERRESNULL = "退款响应为空";
    String SAMECARDTYPEONLYALLOWONE = "同一银行只能绑定一张同一类型的银行卡";
    String PHONEHASCHANGED = "该手机号与注册手机号不符，请填写注册手机号";
    //    String PHONECHANGEDOLDCARDCANNOTPAY = "该银行卡绑定的手机号与平台注册手机号不符，交易有风险，请解绑或联系发卡银行更换手机号";
    String PHONECHANGEDOLDCARDCANNOTPAY = "该银行卡绑定的手机号与平台注册手机号不符，将会影响交易，请解绑并联系发卡银行更换手机号，再绑定使用。";
    String ORDERTIMEOUT = "本次支付已超时！";
    /* 快钱快捷公共参数 END */


    /* UnionPayB2CWebWap公共参数 START */
    String TXNTYPE_CONSUMEUNDO = "31";
    String TXNTYPE_REFUND = "04";
    String RESPONSE_CODE_A6 = "A6";// 银联交易有缺陷的成功
    String RESPONSE_CODE_03 = "03";// 银联交易处理中
    String RESPONSE_CODE_04 = "04";// 银联交易处理中
    String RESPONSE_CODE_05 = "05";// 银联交易处理中
    String UNION_PAY_TRADE_PUR = "{unionPayPUR}";// 银联支付交易未及时成功，将响应信息放入Redis
    /* UnionPayB2CWebWap公共参数 END */

    /* UnionPayH5Pay公共参数 START */
    String MSG_TYPE = "msgType";//消息类型: 支付宝H5支付：1; 微信H5支付：2; 银联在线无卡：3; 银联云闪付（走银联全渠道）：4
    String SCENE_TYPE = "sceneType";//业务应用类型：微信H5支付必填。用于苹app应用里值为IOS_SDK；用于安卓app应用里值为AND_SDK；用于手机网站值为IOS_WAP或AND_WAP
    String MER_APP_NAME = "merAppName";//微信H5支付必填。用于苹或安卓app 应用中，传分别 对应在 AppStore和安卓分发市场中的应用名（如：全民付）；用于手机网站，传对应的网站名（如：银联商务官网）
    String MER_APP_ID = "merAppId";//微信H5支付必填。用于苹果或安卓 app 应用中，苹果传 IOS 应用唯一标识(如： com.tencent.wzryIOS )。安卓传包名 (如： com.tencent.tmgp.sgame)。如果是用于手机网站 ，传首页 URL 地址 , (如： https://m.jd.com ) ，支付宝H5支付参数无效
    String CLIENT = "client";//客户端类型：1pc; 2android; 3ios; 4wap
    String CLIENT_PC = "1";//客户端类型：1pc; 2android; 3ios; 4wap
    String CLIENT_ANDROID = "2";//客户端类型：1pc; 2android; 3ios; 4wap
    String CLIENT_IOS = "3";//客户端类型：1pc; 2android; 3ios; 4wap
    String CLIENT_WAP = "4";//客户端类型：1pc; 2android; 3ios; 4wap
    //    String UNION_PAY_TRADE_PUR = "{unionPayPUR}";// 银联支付交易未及时成功，将响应信息放入Redis
    String MER_ORDER_ID = "merOrderId";//订单号
    String MSG_SRC_ID = "msgSrcId";//系统来源编号
    String RESP_CODE = "respCode";//H5B2B手工接口退款应答码
    String SERIAL_NO = "serialNo";//H5B2B手工接口退款的序列号
    String APP_ID = "appid";//H5B2B手工接口退款的应用id，银联数字王府井提供
    String ERR_CODE = "errCode";
    String ERR_MSG = "errMsg";
    String FAILED = "FAILED";
    String MSG_SRC = "msgSrc";//来源系统标识
    String REQUEST_TIMESTAMP = "requestTimestamp";//报文请求时间
    String SRC_RESERVE = "srcReserve";//	请求系统预留字段
    String INST_MID = "instMid";//业务类型
    String BILL_NO = "billNo";//账单号
    String BILL_DATE = "billDate";//账单日期，格式yyyy-MM-dd
    String BILL_DESC = "billDesc";//账单描述
    String TOTAL_AMOUNT = "totalAmount";//支付总金额
    String DIVISION_FLAG = "divisionFlag";//分账标记
    String PLATFORM_AMOUNT = "platformAmount";//平台商户分账金额
    String GOODS = "goods";//商品信息
    String SUB_ORDERS = "subOrders";//子商户分账信息，包括子商户号、分账金额
    String MEMBER_ID = "memberId";//会员号
    String COUNTER_NO = "counterNo";//桌号、柜台号、房间号
    String EXPIRE_TIME = "expireTime";//账单过期时间
    String NOTIFY_URL = "notifyUrl";//支付结果通知地址
    String RETURN_URL = "returnUrl";//支付成功后用户端会跳转至该url
    String QRCODE_ID = "qrCodeId";//二维码ID
    String SYSTEM_ID_UNION = "systemId";//系统ID
    String SECURE_TRANSACTION = "secureTransaction";//担保交易标识
    String WALLET_OPTION = "walletOption";//	钱包选项，说明：1.单一钱包支付传SINGLE, 多钱包支付传MULTIPLE
    String WALLET_OPTION_SINGLE = "SINGLE";//	钱包选项，说明：1.单一钱包支付传SINGLE, 多钱包支付传MULTIPLE
    String WALLET_OPTION_MULTIPLE = "MULTIPLE";//	钱包选项，说明：1.单一钱包支付传SINGLE, 多钱包支付传MULTIPLE
    String SIGN_TYPE = "signType";//签名算法
    String NAME = "name";//实名认证姓名
    String MOBILE = "mobile";//实名认证手机号
    String CERT_TYPE = "certType";//实名认证证件类型
    String CERT_NO = "certNo";//实名认证证件号
    String FIX_BUYER = "fixBuyer";//是否需要实名认证
    String SIGN = "sign";//签名
    String BILL_QRCODE = "billQRCode";//二维码链接
    String LIMIT_CREDIT_CARD = "limitCreditCard";//是否需要限制信用卡支付
    String PAY_INFO_QUERY_ADDR = "payInfoQueryAddr";
    String MER_NAME = "merName";//商户名称
    String MEMO = "memo";//付款附言
    String NOTIFY_ID = "notifyId";//支付通知ID
    String SECURE_STATUS = "secureStatus";//担保状态
    String COMPLETE_AMOUNT = "completeAmount";//担保完成金额（分）
    String BILL_PAYMENT = "billPayment";//支付账单详情
    String EXTRA_BUYER_INFO = "extraBuyerInfo";//用户额外信息
    String BILL_BIZ_TYPE = "billBizType";//账单业务类型
    String PAY_SEQ_ID = "paySeqId";//交易参考号
    String BUYER_PAY_AMOUNT = "buyerPayAmount";//实付金额
    String INVOICE_AMOUNT = "invoiceAmount";//开票金额
    String DISCOUNT_AMOUNT = "discountAmount";//折扣金额
    String BUYER_ID = "buyerId";//买家ID
    String BUYER_USERNAME = "buyerUsername";//买家用户名
    String PAY_DETAIL = "payDetail";//支付详情
    String SETTLE_DATE = "settleDate";//结算时间，格式yyyy-MM-dd
    String TARGET_ORDER_ID = "targetOrderId";//目标平台单号
    String TARGET_SYS = "targetSys";//目标系统
    String MSG_ID = "msgId";//消息ID
    String MD5 = "MD5";//
    String H5_TRADE_TYPE = "tradeType";//tradeType 交易类型：1：支付交易  2：退款交易
    String H5_TRADE_TYPE_1 = "1";//tradeType 交易类型：1：支付交易  2：退款交易
    String H5_TRADE_TYPE_2 = "2";//tradeType 交易类型：1：支付交易  2：退款交易
    String H5_TRADE_TYPE_3 = "3";//tradeType 交易类型：1：支付交易  2：退款交易 3: 未完成的支付交易
    String ATTACHED_DATA = "attachedData";//商户附加数据
    String ORDER_DESC = "orderDesc";//账单描述
    String GOODS_TAG = "goodsTag";//商品标记，用于优惠活动
    String ORIGINAL_AMOUNT = "originalAmount";//订单原始金额，单位分，用于记录前端系统打折前的金额
    String BANK_CARD_NO = "bankCardNo";//支付银行信息
    String CARD_TYPE = "cardType";//银行卡类型：借记卡：borrow   贷记卡：loan
    String CARD_TYPE_BORROW = "borrow";//银行卡类型：借记卡：borrow   贷记卡：loan
    String CARD_TYPE_LOAN = "loan";//银行卡类型：借记卡：borrow   贷记卡：loan
    String BANK_INFO = "bankInfo";//银行信息
    String BILL_FUNDS = "billFunds";//资金渠道
    String BILL_FUNDS_DESC = "billFundsDesc";//资金渠道说明
    String COUPON_AMOUNT = "couponAmount";//网付计算的优惠金额
    String RECEIPT_AMOUNT = "receiptAmount";//实收金额，支付宝会有
    String REF_ID = "refId";//支付银行卡参考号
    String REFUND_DESC = "refundDesc";//退款说明
    String SEQ_ID = "seqId";//系统交易流水号
    String REFUND_ORDER_ID = "refundOrderId";//退款流水号
    String SUB_BUYER_ID = "subBuyerId";//卖家子ID
    String REFUND_TYPE = "refundType";//退款类型，包括取消订单("cancel")、关闭订单("shutdown")、退货退款("refunds")
    String REFUND_TYPE_CANCEL = "cancel";//退款类型，包括取消订单("cancel")、关闭订单("shutdown")
    String REFUND_TYPE_SHUTDOWN = "shutdown";//退款类型，包括取消订单("cancel")、关闭订单("shutdown")
    String REFUND_TYPE_REFUNDS = "refunds";//退货退款
    String REFUND_TYPE_SUPPLEMENT = "supplement";//配货补款退款
    String REFUND_BILL_PAYMENT = "refundBillPayment";//退款账单详情
    String BIZ_TYPE = "bizType";//网银支付类型: B2B企业网银支付 B2C个人网银支付
    String BIZ_TYPE_B2B = "B2B";//网银支付类型，B2B企业网银支付 B2C个人网银支付
    String BIZ_TYPE_B2C = "B2C";//网银支付类型，B2B企业网银支付 B2C个人网银支付
    String CHANNEL_TYPE = "channelType";//支付渠道选择 PC：PC端支付 PHONE：移动端支付   默认为移动端
    String CHANNEL_TYPE_PC = "PC";//支付渠道选择 PC：PC端支付 PHONE：移动端支付   默认为移动端
    String CHANNEL_TYPE_PHONE = "PHONE";//支付渠道选择 PC：PC端支付 PHONE：移动端支付   默认为移动端
    String REFUND_STATUS = "refundStatus";//H5订单查询接口响应报文里的订单退款状态
    String REFUND_TARGET_ORDER_ID = "refundTargetOrderId";//目标系统退货订单号
    String REFUND_PAY_TIME = "refundPayTime";//退款时间
    String BENEFIT = "benefit";//分润比列
    String BALANCE_SHOP_BENEFIT =   "balanceShopBenefit";//使用余额支付,shop所得分润
    String BALANCE_EWFRESH_BENEFIT  = "balanceEwfreshBenefit";//使用余额支付,ewfresh所得分润
    String BANK_SHOP_BENEFIT = "platfBenefit";//使用第三方支付通道支付,shop所得分润
    String BANK_EWFRESH_BENEFIT = "platformEwfreshBenefit";//使用第三方支付通道支付,ewfresh所得分润

    //status的各种状态：	 	  	 
    String STATUS = "status";//交易状态
    String STATUS_NEW_ORDER = "NEW_ORDER";//NEW_ORDER:新订单
    String STATUS_UNKNOWN = "UNKNOWN";//UNKNOWN:不明确的交易状态
    String PROCESSING = "PROCESSING";//处理中
    String STATUS_TRADE_CLOSED = "TRADE_CLOSED";//TRADE_CLOSED:在指定时间段内未支付时关闭的交易；在交易完成全额退款成功时关闭的交易；支付失败的交易。TRADE_CLOSED的交易不允许进行任何操作。
    String STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";//WAIT_BUYER_PAY:交易创建，等待买家付款。
    String STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";//TRADE_SUCCESS:支付成功
    String STATUS_TRADE_REFUND = "TRADE_REFUND";//TRADE_REFUND:订单转入退货流程,退货可能是部分也可能是全部。


    String BILL_STATUS = "billStatus";//账单状态
    String BILL_STATUS_PAID = "PAID";//账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
    String BILL_STATUS_UNPAID = "UNPAID";//账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
    String BILL_STATUS_REFUND = "REFUND";//账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
    String BILL_STATUS_CLOSED = "CLOSED";//账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN
    String BILL_STATUS_UNKNOWN = "UNKNOWN";//账单状态: PAID、UNPAID、REFUND、CLOSED、UNKNOWN

    /* UnionPayH5Pay公共参数 END */


    //白条还款信息Key
    String White_Repay_KEY = "{order}-{sendRecord}-{orderToPay}";
    //白条还款账单的Key
    String White_Bill_Key = "{pay}{WhiteRepayBill}";

    String TEMPORARY_FROZEN_ACCFLOW = "{pay}-{temporaryFrozenAccFlow}";

    String PAYFLOW_TO_ACCFLOW = "{pay}-{payflowtoaccflow}";

    Double NULL_BALANCE = 0.00;

    String BALANCE_NOT_ENOUGH = "Balance not enough";

    String NULL_BALANCE_STR = "0.00";

    String USER_KEY_PREF = "{userCentre}{keepUserInfo}";//获取redis中用户信息的key

    String USER_STATUS = "userStatus";

    String SHOW_PHONE = "showPhone";

    String BillRepayFlowUpBillVo_info = "{pay}{BillRepayFlowUpBillVo}";//redis 存BillRepayFlowUpBillVo白条退款还款记录


    Short APPR_STATUS_0 = 0;//未审核
    Short APPR_STATUS_1 = 1;//一审
    Short APPR_STATUS_2 = 2;//二审
    Short APPR_STATUS_3 = 3;//三审
    Short APPR_STATUS_4 = 4;//四审
    Short APPR_STATUS_5 = 5;//审核不通过
    Short APPR_STATUS_6 = 6;//完成
    Short APPR_STATUS_7 = 7;//已取消
    Short APPR_STATUS_8 = 8;//提现失败
    Short APPR_STATUS_9 = 9;//处理中
    Short APPR_STATUS_10 = 10;//确认不通过

    String ORDERID = "orderId";
    String SUBORDERID = "subOrderId";
    String REFUNDAMOUNT = "refundAmount";

    String EARNEST_SUFFIX = "E";
    String RESIDUUM_SUFFIX = "R";

    BigDecimal EARNEST_PROPORTION = new BigDecimal(0.30);
    BigDecimal RESIDUUM_PROPORTION = new BigDecimal(0.70);
    BigDecimal BIGDECIMAL_ZERO = new BigDecimal(0);

    String METHOD_PRF = "{";
    String METHOD_SUF = "}";
    String JOINT = "-";

    String SYSTEM_ID = "10000";
    String SELF_SHOP_ID = "0";//自营店铺id
    String SELF_SHOPNAME = "自营";
    String MERCHANT_TYPE_SELF = "1";//店铺类型：自营
    String MERCHANT_TYPE_OTHER = "2";//店铺类型：非自营
    String IS_RECHARGE_4 = "4";//充值

    String BILL99_RSPCODE_0000 = "0000";//成功
    String BILL99_RSPCODE_5002 = "5002";//交易号不存在
    String BILL99_RSPCODE_1000 = "1000";//参数校验异常
    String BILL99_RSPCODE_5005 = "5005";//会员不存在
    String BILL99_RSPCODE_5006 = "5006";//不支持的银行卡
    String BILL99_RSPCODE_5010 = "5010";//绑卡成功,待银行审核
    String BILL99_RSPCODE_5007 = "5007";//会员状态异常
    String BILL99_RSPCODE_5009 = "5009";//重复绑卡
    String BILL99_RSPCODE_9902 = "9902";//生成绑卡ID异常
    String BILL99_RSPCODE_9903 = "9903";//加解密异常
    String BILL99_RSPCODE_9999 = "9999";//系统异常

    Integer ORDER_WAIT_COMMIT = 1000;//用户下单
    Integer ORDER_WAIT_PAY = 1100;//未付款
    Integer ORDER_PAID_EARNEST = 1200;//已付定金
    Integer ORDER_PAID = 1300;//已付全款
    Integer ORDER_DISTRIBUTING = 1350;//备货中
    Integer ORDER_SHUTDOWN = 1360;//关闭订单
    Integer ORDER_APPLY_RETURN = 1400;//申请退单
    Integer ORDER_AGREE_RETURN = 1500;//同意退单
    Integer ORDER_SEND_GOODS = 1600;//已发货
    Integer ORDER_RECEIVE_GOODS = 1700;//已收货
    Integer ORDER_APPLY_REFUND = 1800;//申请退款
    Integer ORDER_AGREE_REFUND = 1900;//同意退款
    Integer ORDER_REFUSE_REFUND = 2000;//拒绝退款
    Integer ORDER_REFUNDING = 2100;//退款中
    Integer ORDER_REFUND_FAILED = 2200;//退款失败

    String PARAM_FLAG = "=";
    String FINISH_AMOUNT = "finishAmount";

    Short ORDER_PAYMENT = 1;//交易类型  1订单付款
    Short ORDER_REFUND = 2;//交易类型  2订单退款
    Short ORDER_REPAYMENT = 3;//交易类型  3还款

    int INT_ONE = 1;// int 类型的1

    String QUICK_PAY_VALID_CODE = "QUICK_PAY_VALID_CODE";// 快钱快捷支付时的短信模板key
    String QUICK_PAY_UNBIND_CARD = "QUICK_PAY_UNBIND_CARD";// 快钱快捷解绑银行卡的短信模板key
    String QUICK_PAY_VALID_CODE_KEY = "{quickPayValidCode}-";// 订单快捷支付时，验证码放入Redis，时效2分钟
    String QUICK_PAY_TRADE_PUR = "{quickPayPUR}";// 订单快捷支付时，快钱未及时响应的订单支付信息
    String QUICK_PAY_TRADE_RFD = "{quickPayRFD}";// 订单快捷退货时，快钱未及时响应的订单支付信息
    String QUICK_PAY_MODIFY_ORDER_STATUS = "{quickPayModifyOrderStatus}";// 订单快捷退货时，修改订单状态的消息
    Long VALID_CODE_EXPIRED = 2L;// 快捷支付时的手机动态码有效时长


    String ALIPAY = "支付宝";
    String WECHATPAYMENT = "微信";
    String BANKCARD = "银行卡";
    String BALANCES = "余额";
    String FASTMONEY = "快钱";
    String GREDIT = "白条";

    String REMENT = "订单付款";
    String REFUND = "订单退款";
    String REPAYMENT = "还款";

    String INVEST = "充值";
    String UNDERLINEINVEST = "线下充值";
    String WITHDTAWALS = "提现";
    String ORDERDEDUCTION = "订单扣款";
    String SETTLEMENTDEDUCTION = "结算扣款";
    String SALESRECEIPTS = "销售收款";
    String WITHFRAWALFEEZE = "提现冻结";
    String REFUNDS = "配货退款";
    String CLAIMREFUND = "索赔退款";
    String FREEZE = "冻结";
    String REFUNDINCOME = "退款收入";
    String RELEASETHEAMOUNT = "释放冻结金额";
    String DISTRIBUTIONDEDUCTION = "配货扣款";
    String REFUNDOFCLAIM = "无货到索赔退款";
    String ORDERREFUND = "订单退款";
    String RECHARGEREEORCASH = "充值错误提现";
    String ACCOUNT = "账户";
    String REDPACKE = "活动红包";

    String HAT_WITHDRAWCODE_0000 = "0000";//成功
    String HAT_WITHDRAWCODE_5001 = "5001";// 外部交易号重复
    String HAT_WITHDRAWCODE_5003 = "5003";// 会员状态异常
    String HAT_WITHDRAWCODE_5004 = "5004";//提现金额异常
    String HAT_WITHDRAWCODE_5005 = "5005";// 会员账户状态异常
    String HAT_WITHDRAWCODE_5006 = "5006";// 会员账户余额不足
    String HAT_WITHDRAWCODE_5007 = "5007";// 平台商户账户状态异常
    String HAT_WITHDRAWCODE_5008 = "5008";//平台商户账户余额不足
    String HAT_WITHDRAWCODE_5009 = "5009";//会员银行卡异常
    String HAT_WITHDRAWCODE_5010 = "5010";//平台路由异常
    String HAT_WITHDRAWCODE_5011 = "5011";//路由配置异常
    String HAT_WITHDRAWCODE_5012 = "5012";//计费服务异常
    String HAT_WITHDRAWCODE_5014 = "5014";//手续费不匹配
    String HAT_WITHDRAWCODE_5015 = "5015";//成本费计算异常
    String HAT_WITHDRAWCODE_5050 = "5050";// 提现请求异常:（根据具体异常返回信息）

    String HAT_SUCCESS_0000 = "0000";//成功
    String HAT_SETTLE_2000 = "2000";//分账总金额与分账数据不符
    String HAT_SETTLE_2001 = "2001";// 分账结算日期不得早于或等于当天
    String HAT_SETTLE_2003 = "2003";// 交易已存在

    String HAT_PLATFORMCODE = "platformCode";//HAT商编
    String HAT_UID = "uId";//HAT子商户编码
    String RSPCODE = "rspCode";
    String RSPMSG = "rspMsg";
    String SETTLERESULT = "settleResult";
    String ACCOUNTBALANCELIST = "accountBalanceList";
    String HAT_CHANEL_CODE = "999";
    String SPAW0001 = "SPAW0001";//可用提现余额账户标识
    String SPAD0001 = "SPAD0001";//应付待分账资金账户

    Short DEAL_TYPE_ONE = 1;//交易类型: 1订单付款
    Short DEAL_TYPE_TWO = 2;//交易类型: 2订单退款
    Short DEAL_TYPE_THREE = 3;//交易类型:3还款

    String IF_ADD_ORDER_RECORD = "ifAddOrderRecord";//是否添加订单操作记录，0否1是

    Integer CALCULATE = 1000;
    String HTTP_METHOD_GET = "get";
    String HTTP_METHOD_POST = "post";
}
