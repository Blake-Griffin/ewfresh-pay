package com.ewfresh.pay.util;

import com.ewfresh.commons.util.ItvJsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * description: 根据手续费率计算手续费及平台收入
 * @author: JiuDongDong
 * date: 2018/4/17.
 */
public class CalMoneyByFate {
    private static Logger logger = LoggerFactory.getLogger(CalMoneyByFate.class);
    private static BigDecimal fate = BigDecimal.ZERO;//手续费率

    /**
     * Description: 根据手续费率计算手续费及平台收入
     * @author: JiuDongDong
     * @param param 参数必须包含支付渠道和付款方支付金额
     * @return java.util.Map<java.lang.String,java.lang.Object> 将手续费和平台收入计算出来后put进map并返回
     *          如果返回参数为null，表名入参错误
     * date: 2018/4/17 14:37
     */
    public static synchronized Map<String, Object> calMoneyByFate(Map<String, Object> param) {
        logger.info("The parameters for calMoneyByFate is param = {}", ItvJsonUtil.toJson(param));
        BigDecimal receiveFee;// 收款方手续费
        BigDecimal platIncome;// 平台收入
        // 获取计算参数并校验
        try {
            Integer payChannel = Integer.valueOf(String.valueOf(param.get(Constants.PAY_CHANNEL)));// 支付渠道标识, 1支付宝 2微信 3中行 4北京银行网银 5银联（北京银行）
            String strPayment = String.valueOf(param.get(Constants.PAYER_PAY_AMOUNT));// 付款方支付金额
            if (payChannel == null || StringUtils.isBlank(strPayment)) {
                logger.warn("The parameter payChannel or staPayment is null is error");
                return null;
            }
            BigDecimal payment = new BigDecimal(strPayment);
            if (payChannel.intValue() == Constants.INTEGER_ONE) {
                fate = new BigDecimal(Constants.FATE_ALI);
                logger.info("This is a AliPay order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_ALIPAY);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_NAME_ZHIFUBAO);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_TWO) {
                fate = new BigDecimal(Constants.FATE_WX);
                logger.info("This is a WXPay order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_WXPAY);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_NAME_WEIXIN);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_THREE) {
                fate = new BigDecimal(Constants.FATE_BOC_B2C_COMPUTER);
                logger.info("This is a BOC order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_BOC);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_NAME_BOC);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_FOUR) {
                fate = new BigDecimal("0.0025");//  北京银行线上收单借记卡千分之3.8，贷记卡千分之5.8  但现在未知北京银行网银渠道支付的银行卡类别，随机定义为0.0025 TODO
                logger.info("This is a BOB order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_BOB);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_NAME_BOB);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_FIVE) {
                fate = new BigDecimal("0.0025");//  银联（北京银行）费率未知，随机定义为0.0025  TODO
                logger.info("This is a QuickPay order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_QUICK_PAY);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_UNION_PAY);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_SIX) {
                fate = BigDecimal.ZERO;//  快钱网银费率这里设置为0，manager会重新赋值
                logger.info("This is a 99bill order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_KUAIQIAN);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_KUAIQIAN);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_FORTY_FIVE) {
                fate = BigDecimal.ZERO;//  快钱快捷费率这里设置为0，manager会重新赋值
                logger.info("This is a 99QuickBill order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_KUAIQIAN_QUICK);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.CHANNEL_KUAIQIAN_QUICK);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_SIXTY_SEVEN) {
                fate = BigDecimal.ZERO;// 中国银联WebWap费率这里设置为0，manager会重新赋值
                logger.info("This is a UnionPay WebWap order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_UNIONPAY_WEBWAP);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.UNIONPAY_WEBWAP);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_SIXTY_EIGHT) {
                fate = BigDecimal.ZERO;// 中国银联QrCode费率这里设置为0，manager会重新赋值
                logger.info("This is a UnionPay QrCode order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_UNIONPAY_QRCODE);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.UNIONPAY_QRCODE);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_SIXTY_NINE) {
                fate = BigDecimal.ZERO;// 中国银联H5Pay费率这里设置为0，manager会重新赋值
                logger.info("This is a UnionPay H5Pay order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_UNIONPAY_H5Pay);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.UNIONPAY_H5Pay);// 支付渠道名称
            }
            if (payChannel.intValue() == Constants.INTEGER_SEVENTY) {
                fate = BigDecimal.ZERO;// 中国银联H5PayB2B费率这里设置为0，manager会重新赋值
                logger.info("This is a UnionPay H5PayB2B order, the fate = {}", fate + "");
                param.put(Constants.CHANNEL_CODE, Constants.CHANNEL_CODE_UNIONPAY_H5Pay_B2B);// 支付渠道编号
                param.put(Constants.CHANNEL_NAME, Constants.UNIONPAY_H5Pay_B2B);// 支付渠道名称
            }
            // 计算收款方手续费
            if (payChannel.intValue() == Constants.INTEGER_SEVENTY) {
                logger.info("This is a B2B order.");
                receiveFee = new BigDecimal("8");//收款方手续费
            } else {
                logger.info("This is a B2C order.");
                receiveFee = payment.multiply(fate).setScale(Constants.INTEGER_TWO, BigDecimal.ROUND_HALF_UP);//收款方手续费
            }
            // 计算平台收入
            platIncome = payment.subtract(receiveFee).setScale(Constants.INTEGER_TWO, BigDecimal.ROUND_HALF_UP);
            param.put(Constants.RECEIVER_FEE, receiveFee);//收款方手续费
            param.put(Constants.PLATINCOME, platIncome);//平台收入
        } catch (Exception e) {
            logger.error("CalMoneyByFate occurred exception ---->!!!!", e);
            return null;
        }
        return param;
    }


}
