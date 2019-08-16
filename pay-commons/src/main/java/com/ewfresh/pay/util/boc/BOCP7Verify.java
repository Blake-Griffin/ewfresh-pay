package com.ewfresh.pay.util.boc;

import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * description:
 *      商户通过公钥获取签名
 * @author: JiuDongDong
 * date: 2018/4/13.
 */
public class BOCP7Verify {
    // 签名相关静态变量
    public static String rootCertificatePath;// 根证书路径
    public static BOCPKCS7Tool tool;//签名工具
    private static final Logger logger = LoggerFactory.getLogger(BOCP7Verify.class);

    // 获取校验签名工具
    static {
        try {
            // 获取根证书路径
//            rootCertificatePath = BOCP7Verify.class.getResource("/newest-B2B-B2C-stage(T4)-public.cer").getPath();
            rootCertificatePath = BOCP7Verify.class.getResource("/" + Constants.ROOT_CERTIFICATE_PATH).getPath();
            logger.info("The rootCertificatePath is: " + rootCertificatePath);
            // 获取验签工具
            tool = BOCPKCS7Tool.getVerifier(rootCertificatePath);
            logger.info("BOCPKCS7Tool.getVerifier({}) ok!", rootCertificatePath);
        } catch (GeneralSecurityException e) {
            logger.error("BOCPKCS7Tool.getVerifier({}) failed with GeneralSecurityException !!!!!!!!!!!!!!", rootCertificatePath, e);
        } catch (IOException e) {
            logger.error("BOCPKCS7Tool.getVerifier({}) failed with IOException !!!!!!!!!!!!!!", rootCertificatePath, e);
        }
    }

    /**
     * Description: 校验银行返回的signData
     * @author: JiuDongDong
     * @param params  明文参数
     * @return java.lang.Boolean 成功返回true，否则false
     * date: 2018/4/18 15:40
     */
    public static Boolean verifySignData(Map<String,Object> params) {
        String plainText = null;// 签名串

        // TODO DN
        String dn = null;
//      dn = "CN=淘宝网TEST, O=BANK OF CHINA, C=CN";//银行签名证书DN，如果为空则不验证DN

        String methodName = (String) params.get(Constants.METHOD_NAME);//方法名
        logger.info("Method: " + methodName + "is going to verify signData");
        // 获取校验参数
        String merchantNo = (String) params.get(Constants.MERCHANT_NO);// 商户号
        String signData = (String) params.get(Constants.SIGN_DATA);// 中行签名数据
        String orderNo = (String) params.get(Constants.ORDER_NO);// 商户订单号
        String orderSeq = (String) params.get(Constants.ORDER_SEQ);// 银行订单流水号
        String cardTyp = (String) params.get(Constants.CARD_TYP);// 银行卡类别
        String payTime = (String) params.get(Constants.PAY_TIME);// 支付时间
        String orderStatus = (String) params.get(Constants.ORDER_STATUS);// 订单状态
        String payAmount = (String) params.get(Constants.PAY_AMOUNT);// 支付金额
        String acctNo = (String) params.get(Constants.ACCT_NO);//支付卡号
        String holderName = (String) params.get(Constants.HOLDER_NAME);//持卡人姓名
        String ibknum = (String) params.get(Constants.IBKNUM);//支付卡省行联行号
        String orderIp = (String) params.get(Constants.ORDER_IP);// 客户支付IP地址
        String orderRefer = (String) params.get(Constants.ORDER_REFER);// 客户浏览器Refer信息
        String bankTranSeq = (String) params.get(Constants.BANK_TRAN_SEQ);// 银行交易流水号
        String returnActFlag = (String) params.get(Constants.RETURN_ACT_FLAG);// 返回操作类型
        String phoneNum = (String) params.get(Constants.PHONE_NUM);// 电话号码
        String orderAmount = (String) params.get(Constants.ORDER_AMOUNT);// 订单金额
        String mRefundSeq = (String) params.get(Constants.M_REFUND_SEQ);// 商户退款交易流水号
        String refundAmount = (String) params.get(Constants.REFUND_AMOUNT);// 退款金额
        String tranTime = (String) params.get(Constants.TRAN_TIME);// 银行交易时间
        String dealStatus = (String) params.get(Constants.DEAL_STATUS);// 退款处理状态
        String extend = (String) params.get(Constants.EXTEND);// 附加域
        String fileDate = (String) params.get(Constants.FILE_DATE);// 文件日期
        String fileType = (String) params.get(Constants.FILE_TYPE);// 文件类型
        String handleType = (String) params.get(Constants.HANDLE_TYPE);// 操作类型
        String invalidTime = (String) params.get(Constants.INVALID_TIME);// 票失效时间
        String submitTime = (String) params.get(Constants.SUBMIT_TIME);// 提交时间
        String ticketId = (String) params.get(Constants.TICKET_ID);// 票号
        String uri = (String) params.get(Constants.URI);// URI标识符，上传、下载文件时需要

        // 拼接plainText
        switch (methodName) {
            case Constants.METHOD_RECEIVE_NOTIFY :
                // 明文串拼接
                // 1.不允许查看卡户信息的商户：商户号|商户订单号|银行订单流水号|银行卡类别|支付时间|订单状态|支付金额 merchantNo|orderNo|orderSeq|cardTyp|payTime|orderStatus|payAmount
                // 2.  允许查看卡户信息的商户：商户号|商户订单号|银行订单流水号|银行卡类别|支付时间|订单状态|支付金额|支付卡号|持卡人姓名|支付卡省行联行号   merchantNo|orderNo|orderSeq|cardTyp|payTime|orderStatus|payAmount|acctNo|holderName|ibknum
                plainText = merchantNo + "|" + orderNo + "|" + orderSeq + "|" + cardTyp + "|" + payTime + "|" + orderStatus + "|" + payAmount;
                if (!StringUtils.isBlank(acctNo) && !StringUtils.isBlank(holderName) && !StringUtils.isBlank(ibknum)) {
                    plainText = merchantNo + "|" + orderNo + "|" + orderSeq + "|" + cardTyp + "|" + payTime + "|" + orderStatus + "|" + payAmount + "|" + acctNo + "|" + holderName + "|" + ibknum;
                }
                break;
            case Constants.METHOD_REFUND_ORDER :
                // 明文串拼接
                // 商户号|商户退款交易流水号|退款金额|商户订单号|银行订单流水号|订单金额|银行交易流水号|银行交易时间|退款处理状态
                // merchantNo|mRefundSeq|refundAmount|orderNo|orderSeq|orderAmount|bankTranSeq|tranTime|dealStatus
                plainText = merchantNo + "|" + mRefundSeq + "|" + refundAmount + "|" + orderNo + "|" + orderSeq + "|" + orderAmount + "|" + bankTranSeq + "|" + tranTime + "|" + dealStatus;
                break;
            case Constants.METHOD_GET_TICKET :
                // 明文串拼接
                // 非空域按字母升序的排列, 各项数据用管道符分隔, 格式如下
                // extend=${extend}|fileDate=$fileDate}|fileType=${fileType}|handleType=${handleType}|invalidTime=${invalidTime}|merchantNo=${merchantNo}|submitTime=${submitTime}|ticketId=${ticketId}|uri=${ticketId}
                StringBuffer text = new StringBuffer("");
                text = text.append(StringUtils.isBlank(extend) ? null : Constants.EXTEND + "=" + extend + "|");
                text = text.append(StringUtils.isBlank(fileDate) ? null : Constants.FILE_DATE + "=" + fileDate + "|");
                text = text.append(StringUtils.isBlank(fileType) ? null : Constants.FILE_TYPE + "=" + fileType + "|");
                text = text.append(StringUtils.isBlank(handleType) ? null : Constants.HANDLE_TYPE + "=" + handleType + "|");
                text = text.append(StringUtils.isBlank(invalidTime) ? null : Constants.INVALID_TIME + "=" + invalidTime + "|");
                text = text.append(StringUtils.isBlank(merchantNo) ? null : Constants.MERCHANT_NO + "=" + merchantNo + "|");
                text = text.append(StringUtils.isBlank(submitTime) ? null : Constants.SUBMIT_TIME + "=" + submitTime + "|");
                text = text.append(StringUtils.isBlank(ticketId) ? null : Constants.TICKET_ID + "=" + ticketId + "|");
                text = text.append(StringUtils.isBlank(uri) ? null : Constants.URI + "=" + uri);
                plainText = text.toString();
        }
        logger.info("The plainText of " + methodName + " is: " + plainText + ", the orderNo is: " + orderNo);

//        // TODO 本地测试用，测试及生产环境delete
//        plainText = "1|1970-01-01 08:00:00.0|001|1.00|1";
//        signData = "MIID6QYJKoZIhvcNAQcCoIID2jCCA9YCAQExCzAJBgUrDgMCGgUAMAsGCSqGSIb3DQEHAaCCAp0w\n" +
//                    "ggKZMIICAqADAgECAhAzuCO5PUrAcrb2QUmlAWNnMA0GCSqGSIb3DQEBBQUAMFoxCzAJBgNVBAYT\n" +
//                    "AkNOMRYwFAYDVQQKEw1CQU5LIE9GIENISU5BMRAwDgYDVQQIEwdCRUlKSU5HMRAwDgYDVQQHEwdC\n" +
//                    "RUlKSU5HMQ8wDQYDVQQDEwZCT0MgQ0EwHhcNMDkxMjIzMTM1OTA3WhcNMTkxMTAxMTM1OTA3WjA+\n" +
//                    "MQswCQYDVQQGEwJDTjEWMBQGA1UEChMNQkFOSyBPRiBDSElOQTEXMBUGA1UEAx4ObdhbnX9RAFQA\n" +
//                    "RQBTAFQwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALxYi14gAH4cGdIA/B8XjDaNMH8/SqmB\n" +
//                    "g44OKgbtsymjJXGM3eK39YdI89zCIEDSsXVxFFKOmoLCrUEpv3gVcdShdnkSHCm46j5poZtguecl\n" +
//                    "OSRGRFYuX859WuIh07xQOdXNRzouIyrPcKdNz7/l7+mulw8qIOilkfRJO0yeKd9ZAgMBAAGjfDB6\n" +
//                    "MB8GA1UdIwQYMBaAFNEBq4gMK8Rc0rD2ptoD0ddgehqcMCsGA1UdHwQkMCIwIKAeoByGGmh0dHA6\n" +
//                    "Ly8yMi42LjU5LjE2L2NybDEuY3JsMAsGA1UdDwQEAwIGwDAdBgNVHQ4EFgQU9bhkkIXpHUULULFs\n" +
//                    "mEWrZl5QxwQwDQYJKoZIhvcNAQEFBQADgYEAkr0H6oSPQHvRBNaADCHoAse6Ia/Xl+orUntYpZT9\n" +
//                    "KXGEkqGj8hdH5/WHKEw3FbRGNT989F+cBSt0zbEwmFlAyRaaNyB3PUvwFaUN0pGmS+YLx4FYS7Cx\n" +
//                    "FO4/kwSpwjWHzgSNWe+cwLfEZllPMKYvghzf22qXgT4y4oOMPd6kYcIxggEUMIIBEAIBATBuMFox\n" +
//                    "CzAJBgNVBAYTAkNOMRYwFAYDVQQKEw1CQU5LIE9GIENISU5BMRAwDgYDVQQIEwdCRUlKSU5HMRAw\n" +
//                    "DgYDVQQHEwdCRUlKSU5HMQ8wDQYDVQQDEwZCT0MgQ0ECEDO4I7k9SsBytvZBSaUBY2cwCQYFKw4D\n" +
//                    "AhoFADANBgkqhkiG9w0BAQEFAASBgEVSHazo/erS25QB6jxBPuc96oWyLfK9kKpqIuydaUtteBAg\n" +
//                    "xHsMsfQyrJqw6PR0OImWl/So7IlqVIeUKEz6SlD1vAdGBpzVrtCH8BWNn9ebcbLG4ae+kCnTwJtP\n" +
//                    "wBLDnaVcCQPTdiWNoyNwpUeiOuKhwuHyL0DFRmkNbXK01DRJ";

        byte[] data = plainText.getBytes();
        // 校验
        try {
            tool.verify(signData, data, dn);
            logger.info("Verify notify data ok, the orderNo is: " + orderNo);
        } catch (Exception e) {
            logger.error("Try to sign verify notify failed, the orderNo :" + orderNo + " !!!!!!!!!!!!!!!!!!", e);
            return false;
        }
        return true;
    }

    /**
     * Description: 银行发送B2C支付结果通知(主动通知)
     * @author: JiuDongDong
     * @param merchantNo 商户号
     * @param orderNo 订单号
     * @param orderSeq 银行订单流水号
     * @param cardTyp 银行卡类别
     * @param payTime 支付时间
     * @param orderStatus 订单状态
     * @param payAmount 支付金额
     * @param acctNo 支付卡号
     * @param holderName 持卡人姓名
     * @param ibknum 支付卡省行联行号
     * @param signData 签名数据
     * @return java.lang.Boolean
     * date: 2018/4/13 10:08
     */
    public static Boolean orderNotifyVerify(String merchantNo, String orderNo, String orderSeq, String cardTyp, String payTime, String orderStatus, String payAmount, String acctNo, String holderName, String ibknum, String signData) {
        // 明文串拼接
        // 1.不允许查看卡户信息的商户：商户号|商户订单号|银行订单流水号|银行卡类别|支付时间|订单状态|支付金额 merchantNo|orderNo|orderSeq|cardTyp|payTime|orderStatus|payAmount
        // 2.  允许查看卡户信息的商户：商户号|商户订单号|银行订单流水号|银行卡类别|支付时间|订单状态|支付金额|支付卡号|持卡人姓名|支付卡省行联行号   merchantNo|orderNo|orderSeq|cardTyp|payTime|orderStatus|payAmount|acctNo|holderName|ibknum
        String plainText = merchantNo + "|" + orderNo + "|" + orderSeq + "|" + cardTyp + "|" + payTime + "|" + orderStatus + "|" + payAmount;
        if (!StringUtils.isBlank(acctNo) && !StringUtils.isBlank(holderName) && !StringUtils.isBlank(ibknum)) {
            plainText = merchantNo + "|" + orderNo + "|" + orderSeq + "|" + cardTyp + "|" + payTime + "|" + orderStatus + "|" + payAmount + "|" + acctNo + "|" + holderName + "|" + ibknum;
        }
        logger.info("The plainText of orderNotifyVerify is: " + plainText + ", the orderNo is: " + orderNo);
        try {
            byte[] data = plainText.getBytes();

            // TODO DN
            String dn = null;
//          dn = "CN=淘宝网TEST, O=BANK OF CHINA, C=CN";//银行签名证书DN，如果为空则不验证DN
            tool.verify(signData, data, dn);
            logger.info("Verify notify data ok, the orderNo is: " + orderNo);
        } catch (Exception e) {
            logger.error("Try to sign order pay failed, the orderNo :" + orderNo + " !!!!!!!!!!!!!!!!!!", e);
            return false;
        }
        return true;
    }

//    /**
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//        //格式：  orderNo|orderTime|curCode|orderAmount|merchantNo
////        String text = "100|20180326160528|001|1.22|104630070110030";
//        String signData = BOCP7Sign.getOrderPaySignData("100", "20180326160528", "001", "1.22", "104630070110030");
//        System.out.println(signData);
//    }

}
