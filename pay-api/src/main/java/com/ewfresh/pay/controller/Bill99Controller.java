package com.ewfresh.pay.controller;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.Adopt;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.configure.Bill99PayConfigure;
import com.ewfresh.pay.manager.Bill99Manager;
import com.ewfresh.pay.model.exception.ShouldPayNotEqualsException;
import com.ewfresh.pay.model.vo.BankAccountVo;
import com.ewfresh.pay.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * description: Bill99业务的接入层
 * @author: JiuDongDong
 * date: 2018/7/31.
 */
@Controller
public class Bill99Controller {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Bill99Manager bill99Manager;
    @Autowired
    private Bill99PayConfigure bill99PayConfigure;

    /**
     * Description: 用户请求订单信息
     * @author: JiuDongDong
     * @param payerName        支付人姓名,可以为空。
     * @param payerContactType 支付人联系类型，1 代表电子邮件方式；2 代表手机联系方式。可以为空。
     * @param payerContact     支付人联系方式，与payerContactType设置对应，payerContactType为1，则填写邮箱地址；payerContactType为2，则填写手机号码。可以为空。
     * @param orderNo          商户订单号，不能为空。
     * @param orderAmount      订单金额，该参数必填。
     * @param bankId           银行代码，如果payType为00，该值可以为空；如果payType为10，该值必须填写，具体请参考银行列表。
     * @param payType          支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10，必填。
     * @param orderIp          下单ip，不能为空。
     * @param payerIdType      指定付款人: 0代表不指定 1代表通过商户方 ID 指定付款人  2代表通过快钱账户指定付款人  3代表付款方在商户方的会员编号(当需要支持保存信息功能的快捷支付时，,需上送此项)  4代表企业网银的交通银行直连
     * @param payerId          付款人标识: 交行企业网银的付款方银行账号，当企业网银中的交通银行直连，此值不能为空。当需要支持保存信息功能的快捷支付时，此值不能为空，此参数需要传入付款方在商户方的会员编号
     * date: 2018/7/31 16:17
     */
    @Adopt
    @RequestMapping(value = "/t/bill-merchant-send-order.htm")
    public void sendOrder(HttpServletResponse response, String payerName, String payerContact, String orderNo, String orderAmount,
                          String bankId, String payType, String payerContactType, String orderIp, String payerIdType, String payerId) {
        logger.info("It is now in Bill99Controller.sendOrder, the input parameter are: [payerName = {}, payerContact = {}, orderNo = {}, " +
                        "orderAmount = {}, bankId = {}, payType = {}, payerContactType = {}, orderIp = {}, payerIdType = {}, payerId = {}]",
                payerName, payerContact, orderNo, orderAmount, bankId, payType, payerContactType, orderIp, payerIdType, payerId);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(orderNo) || StringUtils.isBlank(orderAmount) || StringUtils.isBlank(orderIp)) {
                logger.warn("The parameter orderNo or orderAmount or orderIp is empty for Bill99Controller.sendOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            // 银行的代码，仅在银行直连/快捷支付指定银行定制时使用。银行直连:payType=10,10-1,10-2,14（如果商户是
            // P2P 网贷行业，由于行业的特殊性，商户要用银行直连，payType 只能设置 10-1，即是储蓄卡网银直连）
            if ((payType.startsWith(Constants.BILL99_ONLY_BANKCARD) || payType.startsWith(Constants.BILL99_COMPANY_E_BANK)
                    || payType.startsWith(Constants.BILL99_QUICK_PAY)) && StringUtils.isBlank(bankId)) {
                logger.warn("The parameter bankId must bot be empty for Bill99Controller.sendOrder.payType = " + payType);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            // 如果是交行企业网银直联，那么payerId必传
            if (Constants.BILL99_BCOM_B2B.equals(payerIdType) && StringUtils.isBlank(payerId)) {
                logger.error("This pay is BCOM_B2B, payerIdType and payerId must not be null, the payerIdType = " + payerIdType + ", payerId = " + payerId);
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99Manager.sendOrder(responseData, payerName, payerContact, orderNo, orderAmount, bankId, payType, payerContactType, orderIp, payerIdType, payerId);
            logger.info("It is OK in Bill99Controller.sendOrder");
        } catch (ShouldPayNotEqualsException e) {
            logger.error("Web should pay money not equals redis", e);
            responseData.setCode(ResponseStatus.SHOULDPAYNOTEQUALS.getValue());
            responseData.setMsg(ResponseStatus.SHOULDPAYNOTEQUALS.name());
        } catch (Exception e) {
            logger.error("Errors occurred in Bill99Controller.sendOrder", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.name());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 订单支付支付应答
     * @author: JiuDongDong
     * date: 2018/8/1 10:50
     */
    @Adopt
    @RequestMapping(value = "/p/bill/order-pay-callback.htm")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("It is now in Bill99Controller.receiveNotify");
        ResponseData responseData = new ResponseData();
        try {
            String merchantAcctId = request.getParameter(Constants.BILL99_MERCHANT_ACCT_ID);//人民币网关账号，该账号为11位人民币网关商户编号+01,该值与提交时相同。
            String version = request.getParameter(Constants.BILL99_VERSION);//网关版本，固定值：v2.0,该值与提交时相同。
            String language = request.getParameter(Constants.BILL99_LANGUAGE);//语言种类，1代表中文显示，2代表英文显示。默认为1,该值与提交时相同。
            String signType = request.getParameter(Constants.BILL99_SIGN_TYPE);//签名类型,该值为4，代表PKI加密方式,该值与提交时相同。
            String payType = request.getParameter(Constants.BILL99_PAY_TYPE);//支付方式，一般为00，代表所有的支付方式。如果是银行直连商户，该值为10,该值与提交时相同。
            String bankId = request.getParameter(Constants.BILL99_BANK_ID);//银行代码，如果payType为00，该值为空；如果payType为10,该值与提交时相同。
            String orderId = request.getParameter(Constants.BILL99_ORDER_ID);//商户订单号，该值与提交时相同。
            String orderTime = request.getParameter(Constants.BILL99_ORDER_TIME);//订单提交时间，格式：yyyyMMddHHmmss，如：20071117020101,该值与提交时相同。
            String orderAmount = request.getParameter(Constants.BILL99_ORDER_AMOUNT);//订单金额，金额以“分”为单位，商户测试以1分测试即可，切勿以大金额测试,该值与支付时相同。
            String bindCard = request.getParameter(Constants.BILL99_BIND_CARD);//已绑短卡号,信用卡快捷支付绑定卡信息后返回前六后四位信用卡号
            String bindMobile = request.getParameter(Constants.BILL99_BIND_MOBILE);//已绑短手机尾号,信用卡快捷支付绑定卡信息后返回前三位后四位手机号码
            String dealId = request.getParameter(Constants.BILL99_DEAL_ID);// 快钱交易号，商户每一笔交易都会在快钱生成一个交易号。
            String bankDealId = request.getParameter(Constants.BILL99_BANK_DEAL_ID);//银行交易号 ，快钱交易在银行支付时对应的交易号，如果不是通过银行卡支付，则为空
            String dealTime = request.getParameter(Constants.BILL99_DEAL_TIME);//快钱交易时间，快钱对交易进行处理的时间,格式：yyyyMMddHHmmss，如：20071117020101
            String payAmount = request.getParameter(Constants.BILL99_PAY_AMOUNT);//商户实际支付金额 以分为单位。比方10元，提交时金额应为1000。该金额代表商户快钱账户最终收到的金额。
            String fee = request.getParameter(Constants.BILL99_FEE);//费用，快钱收取商户的手续费，单位为分。
            String ext1 = request.getParameter(Constants.BILL99_EXT1);//扩展字段1，该值与提交时相同。
            String ext2 = request.getParameter(Constants.BILL99_EXT2);//扩展字段2，该值与提交时相同。
            String payResult = request.getParameter(Constants.BILL99_PAY_RESULT);//处理结果， 10支付成功，11 支付失败，00订单申请成功，01 订单申请失败
            String errCode = request.getParameter(Constants.BILL99_ERR_CODE);//错误代码 ，请参照《人民币网关接口文档》最后部分的详细解释。
            String signMsg = request.getParameter(Constants.BILL99_SIGN_MSG);//签名字符串
            // 封装响应数据
            Map<String, String> params = new HashMap<>();
            params.put(Constants.BILL99_MERCHANT_ACCT_ID, merchantAcctId);
            params.put(Constants.BILL99_VERSION, version);
            params.put(Constants.BILL99_LANGUAGE, language);
            params.put(Constants.BILL99_SIGN_TYPE, signType);
            params.put(Constants.BILL99_PAY_TYPE, payType);
            params.put(Constants.BILL99_BANK_ID, bankId);
            params.put(Constants.BILL99_ORDER_ID, orderId);
            params.put(Constants.BILL99_ORDER_TIME, orderTime);
            params.put(Constants.BILL99_ORDER_AMOUNT, orderAmount);
            params.put(Constants.BILL99_BIND_CARD, bindCard);
            params.put(Constants.BILL99_BIND_MOBILE, bindMobile);
            params.put(Constants.BILL99_DEAL_ID, dealId);
            params.put(Constants.BILL99_BANK_DEAL_ID, bankDealId);
            params.put(Constants.BILL99_DEAL_TIME, dealTime);
            params.put(Constants.BILL99_PAY_AMOUNT, payAmount);
            params.put(Constants.BILL99_FEE, fee);
            params.put(Constants.BILL99_EXT1, ext1);
            params.put(Constants.BILL99_EXT2, ext2);
            params.put(Constants.BILL99_PAY_RESULT, payResult);
            params.put(Constants.BILL99_ERR_CODE, errCode);
            params.put(Constants.BILL99_SIGN_MSG, signMsg);
            logger.info("The notify info: {}", JsonUtil.toJson(params));
            bill99Manager.receiveNotify(responseData, params);
            String result = (String) responseData.getEntity();
            response2Bill99(response, result);
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.receiveNotify", e);
            String frontEndUrl = bill99PayConfigure.getFrontEndUrl();
            String result = "<result>1</result><redirecturl>" + frontEndUrl + "</redirecturl>";
            try {
                response2Bill99(response, result);
            } catch (Exception e1) {
                logger.error("Some errors occurred in Bill99Controller.receiveNotify when response2Bill99", e1);
            }
        }

    }

    /**
     * Description: 商户查询退款信息
     * @author: JiuDongDong
     * @param startDate      退款开始时间，格式为YYYYMMDD 例如20100811  并且退款开始时间与退款结束时间在3天之内。
     * @param endDate        退款结束时间，格式为YYYYMMDD 例如20100811  20130314
     * @param refundSequence 12位退款订单号, 时间戳  可为空
     * @param rOrderId       原商家订单号  可为空
     * @param requestPage    请求记录集页码, 在查询结果数据总量很大时，快钱会将支付结果分多次返回。本参数表示商户需要得到的记录集页码。默认为1，表示第1 页。  可为空
     * @param status         交易状态：0代表进行中  1代表成功   2代表失败  可为空
     * @param merchantType   商户类型： 1代表自营   2代表非自营  可为空
     * date: 2018/8/2 14:26
     */
    @Adopt
    @RequestMapping(value = "/t/bill/query-refund-order.htm")
    public void queryRefundOrder(HttpServletResponse response, String startDate, String endDate, String refundSequence, String rOrderId,
                                 @RequestParam(value = "requestPage", defaultValue = "1") String requestPage,
                                 @RequestParam(value = "status", defaultValue = "1") String status,
                                 @RequestParam(value = "merchantType", defaultValue = "1") String merchantType) {
        logger.info("It is now in Bill99Controller.queryRefundOrder, the input parameter are " +
                "[startDate = {}, endDate = {}, orderId = {}, rOrderId = {}, requestPage = {}, status = {}, merchantType = {}]",
                startDate, endDate, refundSequence, rOrderId, requestPage, status, merchantType);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
                logger.warn("The parameter startDate or endDate or orderId is empty for Bill99Controller.queryRefundOrder");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99Manager.queryRefundOrder(responseData, startDate, endDate, refundSequence, rOrderId, requestPage, status, merchantType);
            logger.info("It is OK in Bill99Controller.queryRefundOrder");
        } catch (Exception e) {
            logger.error("Some errors occurred in Bill99Controller.queryRefundOrder", e);
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }

    }

    /**
     * Description: 根据退款订单号查询Redis中的退款信息
     * @author: JiuDongDong
     * @param outRequestNo 退款订单号(子订单订单号,如果没有该订单没有子订单则该处填订单号)
     * date: 2018/8/15 15:17
     */
    @Adopt
    @RequestMapping("/t/bill/get-refund-seq.htm")
    public void getRefundSeq(HttpServletResponse response, String outRequestNo) {
        logger.info("It is now in Bill99Controller.getRefundSeq, the input parameter is: [outRequestNo = {}]", outRequestNo);
        ResponseData responseData = new ResponseData();
        try {
            if (StringUtils.isBlank(outRequestNo)) {
                logger.warn("The parameter outRequestNo is empty for Bill99Controller.getRefundSeq");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99Manager.getRefundOrderInfoFromRedis(responseData, outRequestNo);
            logger.info("Get refundSeq is ok");
        } catch (Exception e) {
            logger.error("Get refundSeq is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * Description: 接收回调或对账单信息后向Bill99响应信息
     * @author: JiuDongDong
     * @param message 1 或 0
     * date: 2018/8/1 10:08
     */
    private void response2Bill99(HttpServletResponse response, String message) {
        PrintWriter writer;
        try {
            writer = response.getWriter();
            response.setContentType("text/html;charset=utf-8");
            writer.println(message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("response to Bill99 error");
        }

    }
    /**
     * Description:店铺绑定银行卡
     * @author: wangyaohui
     * @param:bankAccountVo
     * date: 2018/8/1 10:08
     */
    @Adopt
    @RequestMapping("/t/bill/add_bank_Account_shop.htm")
    public void addBankAccountByShop(HttpServletResponse response, BankAccountVo bankAccountVo, String code) {
        logger.info("Add bank account by shop param [bankAccountVo = {}]", ItvJsonUtil.toJson(bankAccountVo));
        ResponseData responseData = new ResponseData();
        try {
            if (bankAccountVo == null || bankAccountVo.getIsPerson() == null) {
                logger.warn("Add bank account by shop param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99Manager.addBankAccountByShop(responseData, bankAccountVo, code);
            logger.info("Add bank account by shop param is ok");
        } catch (Exception e) {
            logger.error("Add bank account by shop param is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

    /**
     * description:给公司开子账户
     *
     * @param response
     * @param shop
     */
    @RequestMapping("/t/bill/add-account.htm")
    public void bindAccount(HttpServletResponse response, String shop) {
        logger.info("the shop msg is------->{}", shop);
        ResponseData responseData = new ResponseData();
        if (StringUtils.isBlank(shop)) {
            responseData.setMsg(ResponseStatus.PARAMNULL.name());
            responseData.setCode(ResponseStatus.PARAMNULL.getValue());
            return;
        }
        try {
            Map<String, String> map = ItvJsonUtil.jsonToObj(shop, new TypeReference<Map<String, String>>() {
            });
            if (map.get("uId") == null || map.get("name") == null || map.get("mobile") == null || map.get("email") == null || map.get("idCardType") == null || map.get("idCardNumber") == null || map.get("address") == null || map.get("registDate") == null || map.get("legalName") == null || map.get("legalId") == null) {
                logger.error("the param is null");
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                return;
            }
            if (map.get("idCardType").equals("101")) {
                //确定是个体户不是企业开户
                bill99Manager.bindAccountOfPerson(responseData, map);
                logger.info("It is OK to open account of Person ");
            } else {
                //企业开户
                bill99Manager.bindAccountOfCompany(responseData, map);
                logger.info("It is OK to open account of company ");
            }

        } catch (Exception e) {
            logger.error("to open account err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }
    /**
     * Description:店铺换绑银行卡
     * @author: wangyaohui
     * @param:bankAccountVo
     * date: 2018/8/1 10:08
     */
    @Adopt
    @RequestMapping("/t/bill/update_bank_Account_shop.htm")
    public void updateBankAccountByShop(HttpServletResponse response, BankAccountVo bankAccountVo, String code) {
        logger.info("update bank account by shop param [bankAccountVo = {}]", ItvJsonUtil.toJson(bankAccountVo));
        ResponseData responseData = new ResponseData();
        try {
            if (bankAccountVo == null || bankAccountVo.getIsPerson() == null) {
                logger.warn("update bank account by shop param is null");
                responseData.setCode(ResponseStatus.PARAMNULL.getValue());
                responseData.setMsg(ResponseStatus.PARAMNULL.name());
                return;
            }
            bill99Manager.updateBankAccountByShop(responseData, bankAccountVo, code);
            logger.info("update bank account by shop param is ok");
        } catch (Exception e) {
            logger.error("update bank account by shop param is err", e);
            responseData.setCode(ResponseStatus.ERR.getValue());
            responseData.setMsg(ResponseStatus.ERR.getValue());
        } finally {
            ResponseUtil.responsePrint(response, responseData, logger);
        }
    }

}
