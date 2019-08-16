package com.ewfresh.pay.util.boc;

import com.ewfresh.pay.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * description:
 *      商户通过私钥获取签名
 * @author: JiuDongDong
 * date: 2018/3/23.
 */
public class BOCP7Sign {
    // 签名相关静态变量
    public static String keyStorePath;//证书库路径
    public static String keyStorePassword;//证书库口令
    public static String keyPassword;//证书密码
    public static BOCPKCS7Tool tool;//签名工具
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(BOCP7Sign.class);

    // 获取签名工具
    static {
        try {
            // 读取证书库密码及证书密码
            String bocConfigPath = BOCP7Sign.class.getResource("/" + Constants.BOC_CONFIG_PROPERTIES).getPath();
            logger.info("The bocConfigPath is: " + bocConfigPath);
            Properties prop = new Properties();
            prop.load(new FileInputStream(bocConfigPath));
            keyStorePassword = prop.getProperty("keyStorePassword");
            keyPassword = prop.getProperty("keyPassword");
            // 读取证书库路径并获取签名工具
            keyStorePath = BOCP7Sign.class.getResource("/" + Constants.B2C_PRIVATE).getPath();
            logger.info("The keyStorePath is: " + keyStorePath);
            tool = BOCPKCS7Tool.getSigner(keyStorePath, keyStorePassword, keyPassword);//keypassword默认与keystorepassword相同
            logger.info("BOCPKCS7Tool.getSigner({},{},{}) ok!", keyStorePath, keyStorePassword, keyPassword);
        } catch (GeneralSecurityException e) {
            logger.error("BOCPKCS7Tool.getSigner({},{},{}) failed!!!!!!!!!!!!!!", keyStorePath, keyStorePassword, keyPassword, e);
        } catch (IOException e) {
            logger.error("BOCPKCS7Tool.getSigner({},{},{}) failed!!!!!!!!!!!!!!", keyStorePath, keyStorePassword, keyPassword, e);
        }
    }

    /**
     * Description: 订单支付签名
     * @author: JiuDongDong
     * @param orderNo 订单号
     * @param orderTime 订单时间
     * @param curCode 币种
     * @param orderAmount 订单金额
     * @param merchantNo 商户号
     * @return java.lang.String 生成的签名
     * date: 2018/4/10 17:58
     */
    public static String getOrderPaySignData(String orderNo, String orderTime, String curCode, String orderAmount, String merchantNo) {
        // 签名
        String signData = null;
        try {
            // 组装必要数据进行签名，签名格式：商户订单号|订单时间|订单币种|订单金额|商户号      orderNo|orderTime|curCode|orderAmount|merchantNo
            String plainText = orderNo + "|" + orderTime + "|" + curCode + "|" + orderAmount + "|" + merchantNo;
            logger.info("The plainText of getOrderPaySignData is: " + plainText + ", the orderNo is: " + orderNo);
            signData = tool.sign(plainText.getBytes());
            logger.info("Sign data ok, the orderNo is: " + orderNo);
        } catch (Exception e) {
            logger.error("Try to sign order pay failed, the orderNo :" + orderNo + " !!!!!!!!!!!!!!!!!!", e);
        }
        return signData;
    }

    /**
     * Description: 订单查询签名
     * @author: JiuDongDong
     * @param orderNos 订单号
     * @param merchantNo 商户号
     * @return java.lang.String 生成的签名
     * date: 2018/4/11 16:58
     */
    public static String getQueryOrderSignData(String merchantNo, String orderNos) {
        // 签名
        String signData = null;
        try {
            // 组装必要数据进行签名，签名格式：商户订单号|订单时间|订单币种|订单金额|商户号      orderNo|orderTime|curCode|orderAmount|merchantNo
            String plainText = merchantNo + ":" + orderNos;
            logger.info("The plainText of getQueryOrderSignData is: " + plainText + ", the orderNos is: " + orderNos);
            signData = tool.sign(plainText.getBytes());
            logger.info("Sign data ok, the orderNos are: " + orderNos);
        } catch (Exception e) {
            logger.error("Try to sign query order failed, the orderNo :" + orderNos + " !!!!!!!!!!!!!!!!!!", e);
        }
        return signData;
    }

    /**
     * Description: 商户发送B2C退款指令签名
     * @author: JiuDongDong
     * @param merchantNo 商户号
     * @param mRefundSeq 商户退款交易流水号
     * @param curCode 退款币种
     * @param refundAmount 退款金额
     * @param orderNo 商户订单号
     * @return java.lang.String 生成的签名
     * date: 2018/4/11 21:05
     */
    public static String getRefundOrderSignData(String merchantNo, String mRefundSeq, String curCode, String refundAmount, String orderNo) {
        // 签名
        String signData = null;
        try {
            // 组装必要数据进行签名，签名格式：商户号|商户退款交易流水号|退款币种|退款金额|商户订单号   merchantNo|mRefundSeq|curCode|refundAmount|orderNo
            String plainText = merchantNo + "|" + mRefundSeq + "|" + curCode + "|" + refundAmount + "|" + orderNo;
            logger.info("The plainText of getRefundOrderSignData is: " + plainText + ", the orderNo is: " + orderNo);
            signData = tool.sign(plainText.getBytes());
            logger.info("Sign data ok, the orderNo is: " + orderNo);
        } catch (Exception e) {
            logger.error("Try to sign refund order failed, the orderNo :" + orderNo + " !!!!!!!!!!!!!!!!!!", e);
        }
        return signData;
    }

    /**
     * Description: 取票签名
     * @author: JiuDongDong
     * @param extend 附加域
     * @param fileDate 文件日期
     * @param fileType 文件类型
     * @param handleType 操作类型:0上传 1下载
     * @param merchantNo 商户号
     * @param submitTime 提交时间
     * @return java.lang.String 签名数据
     * date: 2018/4/12 14:06
     */
    public static String getTicketSignData(String extend, String fileDate, String fileType, String handleType, String merchantNo, String submitTime) {
        // 签名
        String signData = null;
        try {
            // 组装必要数据进行签名，签名格式：签名原文为非空域按字母升序的排列, 各项数据用管道符分隔：key1=value1|key2=value2|key3=value3…|keyn=valuen
            StringBuilder plainText = new StringBuilder();
            plainText = StringUtils.isBlank(extend) ? plainText : plainText.append("extend=" + extend + "|");
            plainText = StringUtils.isBlank(fileDate) ? plainText : plainText.append("fileDate=" + fileDate + "|");
            plainText = StringUtils.isBlank(fileType) ? plainText : plainText.append("fileType=" + fileType + "|");
            plainText = StringUtils.isBlank(handleType) ? plainText : plainText.append("handleType=" + handleType + "|");
            plainText = StringUtils.isBlank(merchantNo) ? plainText : plainText.append("merchantNo=" + merchantNo);
            plainText = StringUtils.isBlank(submitTime) ? plainText : plainText.append("|submitTime=" + submitTime);
            logger.info("The plainText of getTicketSignData is: " + plainText + ", the fileDate is: " + fileDate);
            signData = tool.sign(plainText.toString().getBytes());
            logger.info("Sign data ok, the fileDate is: " + fileDate);
        } catch (Exception e) {
            logger.error("Try to sign get ticket failed, the fileDate :" + fileDate + " !!!!!!!!!!!!!!!!!!", e);
        }
        return signData;
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
