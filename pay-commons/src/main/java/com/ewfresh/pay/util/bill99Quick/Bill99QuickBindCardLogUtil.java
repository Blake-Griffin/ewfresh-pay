package com.ewfresh.pay.util.bill99quick;

import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 根据code打印相关信息（绑卡时）
 * @author: JiuDongDong
 * date: 2018/10/9.
 */
public final class Bill99QuickBindCardLogUtil {
    private static Logger logger = LoggerFactory.getLogger(Bill99QuickBindCardLogUtil.class);

    /**
     * Description: 打印快钱快捷绑卡、查询卡信息交易结果
     * @author: JiuDongDong
     * @param responseData 封装返回信息
     * @param responseCode 快钱返回的状态码
     * @param responseTextMessage 快钱返回的响应信息
     * @param errorCode 快钱返回的错误码
     * @param errorMessage  快钱返回的错误信息
     * @param cardNo  银行卡号
     * date: 2018/10/9 19:14
     */
    public static void logBindCardInfo(ResponseData responseData, String responseCode, String responseTextMessage,
                               String errorCode, String errorMessage, String cardNo) {
        if ("00".equals(responseCode)) {
            logger.info("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            logger.info("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);
            logger.info("卡号" + cardNo + "：交易成功");
            return;
        }
        // 打印错误信息
        logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
        logger.error("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);
        if (StringUtils.isNotBlank(responseCode)) {
            switch (responseCode) {
                case "W6" :
                    logger.error(cardNo + "：手机号、身份证号码、姓名与开户时登记的不一致");
                    responseData.setCode("W6");
                    responseData.setMsg("手机号、身份证号码、姓名与开户时登记的不一致");
                    break;
                case "HW" :
                    logger.error(cardNo + "：手机号码不符");
                    responseData.setCode("HW");
                    responseData.setMsg("手机号码不符");
                    break;
                case "T3" :
                    logger.error(cardNo + "：一个银行不能绑定多张卡或者未绑定");
                    responseData.setCode("T3");
                    responseData.setMsg("一个银行不能绑定多张卡或者未绑定");
                    break;
                case "T5" :
                    logger.error(cardNo + "：交易信息被篡改");
                    responseData.setCode("T5");
                    responseData.setMsg("交易信息被篡改");
                    break;
                case "T6" :
                    logger.error(cardNo + "：验证码错误，请核对您的验证码信息或重新获取");
                    responseData.setCode("T6");
                    responseData.setMsg("验证码错误，请核对您的验证码信息或重新获取");
                    break;
                case "T8" :
                    logger.error(cardNo + "：解绑失败，未绑定相关信息");
                    responseData.setCode("T8");
                    responseData.setMsg("解绑失败，未绑定相关信息");
                    break;
                case "01" :
                    logger.error(cardNo + "：请联系发卡行，或核对卡信息后重新输入");
                    responseData.setCode("01");
                    responseData.setMsg("请联系发卡行，或核对卡信息后重新输入");
                    break;
                case "HZ" :
                    logger.error(cardNo + "：证件号不符");
                    responseData.setCode("HZ");
                    responseData.setMsg("证件号不符");
                    break;
                case "HX" :
                    logger.error(cardNo + "：姓名不符");
                    responseData.setCode("HX");
                    responseData.setMsg("姓名不符");
                    break;
                case "BA" :
                    logger.error(cardNo + "：卡信息错误次数超限，请联系发卡行");
                    responseData.setCode("BA");
                    responseData.setMsg("卡信息错误次数超限，请联系发卡行");
                    break;
                case "62" :
                    logger.error(cardNo + "：受限制的卡，请换卡重试");
                    responseData.setCode("62");
                    responseData.setMsg("受限制的卡，请换卡重试");
                    break;
                case "68" :
                    logger.error(cardNo + "：无法在正常时间内获得交易应答，请稍后重试");
                    responseData.setCode("68");
                    responseData.setMsg("无法在正常时间内获得交易应答，请稍后重试");
                    break;
                case "02" :
                    logger.error(cardNo + "：请联系快钱公司");
                    responseData.setCode("02");
                    responseData.setMsg("请联系快钱公司");
                    break;
                case "L8" :
                    logger.error(cardNo + "：找不到路由");
                    responseData.setCode("L8");
                    responseData.setMsg("找不到路由");
                    break;
                case "HY" :
                    logger.error(cardNo + "：证件类型不符");
                    responseData.setCode("HY");
                    responseData.setMsg("证件类型不符");
                    break;
                case "W0" :
                    logger.error(cardNo + "：手机号与开户时登记的不一致");
                    responseData.setCode("W0");
                    responseData.setMsg("手机号与开户时登记的不一致");
                    break;
                case "IA" :
                    logger.error(cardNo + "：请提供正确的手机号");
                    responseData.setCode("IA");
                    responseData.setMsg("请提供正确的手机号");
                    break;
                case "Y1" :
                    logger.error(cardNo + "：身份认证失败");
                    responseData.setCode("Y1");
                    responseData.setMsg("身份认证失败");
                    break;
                case "I1" :
                    logger.error(cardNo + "：请提供正确的持卡人姓名");
                    responseData.setCode("I1");
                    responseData.setMsg("请提供正确的持卡人姓名");
                    break;
                case "HU" :
                    logger.error(cardNo + "：有效期不符");
                    responseData.setCode("HU");
                    responseData.setMsg("有效期不符");
                    break;
                case "20" :
                    logger.error(cardNo + "：卡信息提供有误");
                    responseData.setCode("20");
                    responseData.setMsg("卡信息提供有误");
                    break;
                case "CB" :
                    logger.error(cardNo + "：银行系统异常、失效，请稍后重试");
                    responseData.setCode("CB");
                    responseData.setMsg("银行系统异常、失效，请稍后重试");
                    break;
                case "I8" :
                    logger.error(cardNo + "：金额超限或其他");
                    responseData.setCode("I8");
                    responseData.setMsg("金额超限或其他");
                    break;
                case "MR" :
                    logger.error(cardNo + "：商户不支持的卡类型");
                    responseData.setCode("MR");
                    responseData.setMsg("商户不支持的卡类型");
                    break;
                case "14" :
                    logger.error(cardNo + "：无效卡号（无此号），请换卡重试");
                    responseData.setCode("14");
                    responseData.setMsg("无效卡号（无此号），请换卡重试");
                    break;
                case "OB" :
                    logger.error(cardNo + "：不受理的银行卡，请换卡重试");
                    responseData.setCode("OB");
                    responseData.setMsg("不受理的银行卡，请换卡重试");
                    break;
                case "ZG" :
                    logger.error(cardNo + "：请重新签约");
                    responseData.setCode("ZG");
                    responseData.setMsg("请重新签约");
                    break;
                case "T0" :
                    logger.error(cardNo + "：验证码已失效，请重新获取");
                    responseData.setCode("T0");
                    responseData.setMsg("验证码已失效，请重新获取");
                    break;
                case "W4" :
                    logger.error(cardNo + "：姓名与开户时登记的不一致");
                    responseData.setCode("W4");
                    responseData.setMsg("姓名与开户时登记的不一致");
                    break;
                case "I3" :
                    logger.error(cardNo + "：请提供正确的证件号码，必须与申请银行卡时的证件号码一致");
                    responseData.setCode("I3");
                    responseData.setMsg("请提供正确的证件号码，必须与申请银行卡时的证件号码一致");
                    break;
                case "I7" :
                    logger.error(cardNo + "：CVV2 或有效期错");
                    responseData.setCode("I7");
                    responseData.setMsg("CVV2 或有效期错");
                    break;
                case "KJ" :
                    logger.error(cardNo + "：交易失败");
                    responseData.setCode("KJ");
                    responseData.setMsg("交易失败");
                    break;
                case "cc" :
                    logger.error(cardNo + "：此卡未在银行预留绑定手机号，请联系发卡行");
                    responseData.setCode("cc");
                    responseData.setMsg("此卡未在银行预留绑定手机号，请联系发卡行");
                    break;
                case "NH" :
                    logger.error(cardNo + "：卡已锁");
                    responseData.setCode("NH");
                    responseData.setMsg("卡已锁");
                    break;
                case "HV" :
                    logger.error(cardNo + "：CVV2 不符");
                    responseData.setCode("HV");
                    responseData.setMsg("CVV2 不符");
                    break;
                case "12" :
                    logger.error(cardNo + "：无效交易");
                    responseData.setCode("12");
                    responseData.setMsg("无效交易");
                    break;
                case "OR" :
                    logger.error(cardNo + "：订单号重复");
                    responseData.setCode("OR");
                    responseData.setMsg("订单号重复");
                    break;
                case "OS" :
                    logger.error(cardNo + "：找不到对应的结算商户");
                    responseData.setCode("OS");
                    responseData.setMsg("找不到对应的结算商户");
                    break;
                case "I4" :
                    logger.error(cardNo + "：请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                    responseData.setCode("I4");
                    responseData.setMsg("请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                    break;
                case "04" :
                    logger.error(cardNo + "：无效终端");
                    responseData.setCode("04");
                    responseData.setMsg("无效终端");
                    break;
                case "I2" :
                    logger.error(cardNo + "：请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                    responseData.setCode("I2");
                    responseData.setMsg("请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                    break;
                case "LG" :
                    logger.error(cardNo + "：该银行卡未开通银联在线支付业务");
                    responseData.setCode("LG");
                    responseData.setMsg("该银行卡未开通银联在线支付业务");
                    break;
                case "08" :
                    logger.error(cardNo + "：请与银行联系");
                    responseData.setCode("08");
                    responseData.setMsg("请与银行联系");
                    break;
                case "AP" :
                    logger.error(cardNo + "：不支持的证件类型/证件号");
                    responseData.setCode("AP");
                    responseData.setMsg("不支持的证件类型/证件号");
                    break;
                case "91" :
                    logger.error(cardNo + "：请稍候重新交易");
                    responseData.setCode("91");
                    responseData.setMsg("请稍候重新交易");
                    break;
                case "Q2" :
                    logger.error(cardNo + "：有效期错，请核实重输或联系发卡行");
                    responseData.setCode("Q2");
                    responseData.setMsg("有效期错，请核实重输或联系发卡行");
                    break;
                case "30" :
                    logger.error(cardNo + "：卡片故障，请换卡重试");
                    responseData.setCode("30");
                    responseData.setMsg("卡片故障，请换卡重试");
                    break;
                case "96" :
                    logger.error(cardNo + "：系统异常、失效，请稍候重试");
                    responseData.setCode("96");
                    responseData.setMsg("系统异常、失效，请稍候重试");
                    break;
                case "ZH" :
                    logger.error(cardNo + "：商户不支持的短信验证模式");
                    responseData.setCode("ZH");
                    responseData.setMsg("商户不支持的短信验证模式");
                    break;
                case "54" :
                    logger.error(cardNo + "：卡片已过期，请换卡后交易");
                    responseData.setCode("54");
                    responseData.setMsg("卡片已过期，请换卡后交易");
                    break;
                case "CD" :
                    logger.error(cardNo + "：卡状态异常或户名证件号不符");
                    responseData.setCode("CD");
                    responseData.setMsg("卡状态异常或户名证件号不符");
                    break;
                case "M2" :
                    logger.error(cardNo + "：商户状态不匹配");
                    responseData.setCode("M2");
                    responseData.setMsg("商户状态不匹配");
                    break;
                case "53" :
                    logger.error(cardNo + "：无此储蓄卡账户");
                    responseData.setCode("53");
                    responseData.setMsg("无此储蓄卡账户");
                    break;
                case "51" :
                    logger.error(cardNo + "：卡余额不足，请换卡交易");
                    responseData.setCode("51");
                    responseData.setMsg("卡余额不足，请换卡交易");
                    break;
                case "61" :
                    logger.error(cardNo + "：超出取款转账金额限制，联系发卡行");
                    responseData.setCode("61");
                    responseData.setMsg("超出取款转账金额限制，联系发卡行");
                    break;
                case "OT" :
                    logger.error(cardNo + "：交易金额太小");
                    responseData.setCode("OT");
                    responseData.setMsg("交易金额太小");
                    break;
                case "16" :
                    logger.error(cardNo + "：金额超出限制");
                    responseData.setCode("16");
                    responseData.setMsg("金额超出限制");
                    break;
                case "05" :
                    logger.error(cardNo + "：不予承兑");
                    responseData.setCode("05");
                    responseData.setMsg("不予承兑");
                    break;
                case "OG" :
                    logger.error(cardNo + "：单笔或日限额超过上限，请联系银行");
                    responseData.setCode("OG");
                    responseData.setMsg("单笔或日限额超过上限，请联系银行");
                    break;
                case "25" :
                    logger.error(cardNo + "：未找到原始交易，请重新交易");
                    responseData.setCode("25");
                    responseData.setMsg("未找到原始交易，请重新交易");
                    break;
                case "R0" :
                    logger.error(cardNo + "：交易不予承兑，请换卡重试");
                    responseData.setCode("R0");
                    responseData.setMsg("交易不予承兑，请换卡重试");
                    break;
                case "65" :
                    logger.error(cardNo + "：超出取款/消费次数限制");
                    responseData.setCode("65");
                    responseData.setMsg("超出取款/消费次数限制");
                    break;
                case "A4" :
                    logger.error(cardNo + "：授权.找不到授权终端");
                    responseData.setCode("A4");
                    responseData.setMsg("授权.找不到授权终端");
                    break;
                case "BB" :
                    logger.error(cardNo + "：CVV 错误次数超限");
                    responseData.setCode("BB");
                    responseData.setMsg("CVV 错误次数超限");
                    break;
                case "92" :
                    logger.error(cardNo + "：与银行通信网络故障，请稍后重试");
                    responseData.setCode("92");
                    responseData.setMsg("与银行通信网络故障，请稍后重试");
                    break;
                case "G3" :
                    logger.error(cardNo + "：超出系统当日金额限制");
                    responseData.setCode("G3");
                    responseData.setMsg("超出系统当日金额限制");
                    break;
                case "07" :
                    logger.error(cardNo + "：特定条件下没收卡");
                    responseData.setCode("07");
                    responseData.setMsg("特定条件下没收卡");
                    break;
                case "80" :
                    logger.error(cardNo + "：交易拒绝");
                    responseData.setCode("80");
                    responseData.setMsg("交易拒绝");
                    break;
                case "KG" :
                    logger.error(cardNo + "：卡状态、户口无效或不存在，拒绝交易对照");
                    responseData.setCode("KG");
                    responseData.setMsg("卡状态、户口无效或不存在，拒绝交易对照");
                    break;
                case "N9" :
                    logger.error(cardNo + "：请持卡人重新进行卡信息验证");
                    responseData.setCode("N9");
                    responseData.setMsg("请持卡人重新进行卡信息验证");
                    break;
                case "03" :
                    logger.error(cardNo + "：无效商户");
                    responseData.setCode("03");
                    responseData.setMsg("无效商户");
                    break;
                case "EQ" :
                    logger.error(cardNo + "：未找到绑定关系");
                    responseData.setCode("EQ");
                    responseData.setMsg("未找到绑定关系");
                    break;
                case "HI" :
                    logger.error(cardNo + "：当天流水号重复");
                    responseData.setCode("HI");
                    responseData.setMsg("当天流水号重复");
                    break;
                case "41" :
                    logger.error(cardNo + "：此卡为挂失卡");
                    responseData.setCode("41");
                    responseData.setMsg("此卡为挂失卡");
                    break;
                case "OQ" :
                    logger.error(cardNo + "：销售日限额已经用完");
                    responseData.setCode("OQ");
                    responseData.setMsg("销售日限额已经用完");
                    break;
                case "B5" :
                    logger.error(cardNo + "：系统维护中，请稍后再试");
                    responseData.setCode("B5");
                    responseData.setMsg("系统维护中，请稍后再试");
                    break;
                case "57" :
                    logger.error(cardNo + "：不允许持卡人进行的交易");
                    responseData.setCode("57");
                    responseData.setMsg("不允许持卡人进行的交易");
                    break;
                case "36" :
                    logger.error(cardNo + "：受限制的卡");
                    responseData.setCode("36");
                    responseData.setMsg("受限制的卡");
                    break;
                case "BC" :
                    logger.error(cardNo + "：无效卡");
                    responseData.setCode("BC");
                    responseData.setMsg("无效卡");
                    break;
                case "B.MGW.0120" :
                    logger.error(cardNo + "：卡长度不对");
                    responseData.setCode("B.MGW.0120");
                    responseData.setMsg("卡长度不对");
                    break;
                case "B.MGW.0121" :
                    logger.error(cardNo + "：当日重复绑卡次数超限");
                    responseData.setCode("B.MGW.0121");
                    responseData.setMsg("当日重复绑卡次数超限");
                    break;
                case "B.BIN.0005" :
                    logger.error(cardNo + "：卡号格式错误");
                    responseData.setCode("B.BIN.0005");
                    responseData.setMsg("卡号格式错误");
                    break;
                case "L9" :
                    logger.error(cardNo + "：错误的卡号校验位");
                    responseData.setCode("L9");
                    responseData.setMsg("错误的卡号校验位");
                    break;
                case "HT" :
                    logger.error("证件号异常或超过有效期");
                    responseData.setCode("HT");
                    responseData.setMsg("证件号异常或超过有效期");
                    break;
            }
        }


        if (StringUtils.isNotBlank(errorCode)) {
            if ("51".equals(errorCode)) {
                logger.error(cardNo + "：卡余额不足，请换卡交易");
                responseData.setCode("51");
                responseData.setMsg("卡余额不足，请换卡交易");
                return;
            } else if ("I8".equals(errorCode)) {
                logger.error(cardNo + "：金额超限或其他");
                responseData.setCode("I8");
                responseData.setMsg("金额超限或其他");
                return;
            } else if ("T6".equals(errorCode)) {
                logger.error(cardNo + "：验证码错误，请核对您的验证码信息或重新获取");
                responseData.setCode("T6");
                responseData.setMsg("验证码错误，请核对您的验证码信息或重新获取");
                return;
            } else if ("01".equals(errorCode)) {
                logger.error(cardNo + "：请联系发卡行，或核对卡信息后重新输入");
                responseData.setCode("01");
                responseData.setMsg("请联系发卡行，或核对卡信息后重新输入");
                return;
            } else if ("62".equals(errorCode)) {
                logger.error(cardNo + "：受限制的卡，请换卡重试");
                responseData.setCode("62");
                responseData.setMsg("受限制的卡，请换卡重试");
                return;
            } else if ("61".equals(errorCode)) {
                logger.error(cardNo + "：超出取款转账金额限制，联系发卡行");
                responseData.setCode("61");
                responseData.setMsg("超出取款转账金额限制，联系发卡行");
                return;
            } else if ("OT".equals(errorCode)) {
                logger.error(cardNo + "：交易金额太小");
                responseData.setCode("OT");
                responseData.setMsg("交易金额太小");
                return;
            } else if ("68".equals(errorCode)) {
                logger.error(cardNo + "：无法在正常时间内获得交易应答，请稍后重试");
                responseData.setCode("68");
                responseData.setMsg("无法在正常时间内获得交易应答，请稍后重试");
                return;
            } else if ("54".equals(errorCode)) {
                logger.error(cardNo + "：卡片已过期，请换卡后交易");
                responseData.setCode("54");
                responseData.setMsg("卡片已过期，请换卡后交易");
                return;
            } else if ("OR".equals(errorCode)) {
                logger.error(cardNo + "：订单号重复");
                responseData.setCode("OR");
                responseData.setMsg("订单号重复");
                return;
            } else if ("16".equals(errorCode)) {
                logger.error(cardNo + "：金额超出限制");
                responseData.setCode("16");
                responseData.setMsg("金额超出限制");
                return;
            } else if ("Y1".equals(errorCode)) {
                logger.error(cardNo + "：身份认证失败");
                responseData.setCode("Y1");
                responseData.setMsg("身份认证失败");
                return;
            } else if ("KJ".equals(errorCode)) {
                logger.error(cardNo + "：交易失败");
                responseData.setCode("KJ");
                responseData.setMsg("交易失败");
                return;
            } else if ("HW".equals(errorCode)) {
                logger.error(cardNo + "：手机号码不符");
                responseData.setCode("HW");
                responseData.setMsg("手机号码不符");
                return;
            } else if ("96".equals(errorCode)) {
                logger.error(cardNo + "：系统异常、失效");
                responseData.setCode("96");
                responseData.setMsg("系统异常、失效");
                return;
            } else if ("91".equals(errorCode)) {
                logger.error(cardNo + "：请稍候重新交易");
                responseData.setCode("91");
                responseData.setMsg("请稍候重新交易");
                return;
            } else if ("IA".equals(errorCode)) {
                logger.error(cardNo + "：请提供正确的手机号");
                responseData.setCode("IA");
                responseData.setMsg("请提供正确的手机号");
                return;
            } else if ("HU".equals(errorCode)) {
                logger.error(cardNo + "：有效期不符");
                responseData.setCode("HU");
                responseData.setMsg("有效期不符");
                return;
            } else if ("05".equals(errorCode)) {
                logger.error(cardNo + "：不予承兑");
                responseData.setCode("05");
                responseData.setMsg("不予承兑");
                return;
            } else if ("BA".equals(errorCode)) {
                logger.error(cardNo + "：卡信息错误次数超限，请联系发卡行");
                responseData.setCode("BA");
                responseData.setMsg("卡信息错误次数超限，请联系发卡行");
                return;
            } else if ("14".equals(errorCode)) {
                logger.error(cardNo + "：无效卡号（无此号），请换卡重试");
                responseData.setCode("14");
                responseData.setMsg("无效卡号（无此号），请换卡重试");
                return;
            } else if ("OG".equals(errorCode)) {
                logger.error(cardNo + "：单笔或日限额超过上限，请联系银行");
                responseData.setCode("OG");
                responseData.setMsg("单笔或日限额超过上限，请联系银行");
                return;
            } else if ("20".equals(errorCode)) {
                logger.error(cardNo + "：卡信息提供有误");
                responseData.setCode("20");
                responseData.setMsg("卡信息提供有误");
                return;
            } else if ("T0".equals(errorCode)) {
                logger.error(cardNo + "：验证码已失效，请重新获取");
                responseData.setCode("T0");
                responseData.setMsg("验证码已失效，请重新获取");
                return;
            } else if ("ZG".equals(errorCode)) {
                logger.error(cardNo + "：请重新签约");
                responseData.setCode("ZG");
                responseData.setMsg("请重新签约");
                return;
            } else if ("CB".equals(errorCode)) {
                logger.error(cardNo + "：银行系统异常、失效，请稍后重试");
                responseData.setCode("CB");
                responseData.setMsg("银行系统异常、失效，请稍后重试");
                return;
            } else if ("25".equals(errorCode)) {
                logger.error(cardNo + "：未找到原始交易，请重新交易");
                responseData.setCode("25");
                responseData.setMsg("未找到原始交易，请重新交易");
                return;
            } else if ("R0".equals(errorCode)) {
                logger.error(cardNo + "：交易不予承兑，请换卡重试");
                responseData.setCode("R0");
                responseData.setMsg("交易不予承兑，请换卡重试");
                return;
            } else if ("HZ".equals(errorCode)) {
                logger.error(cardNo + "：证件号不符");
                responseData.setCode("HZ");
                responseData.setMsg("证件号不符");
                return;
            } else if ("I7".equals(errorCode)) {
                logger.error(cardNo + "：CVV2 或有效期错");
                responseData.setCode("I7");
                responseData.setMsg("CVV2 或有效期错");
                return;
            } else if ("30".equals(errorCode)) {
                logger.error(cardNo + "：卡片故障，请换卡重试");
                responseData.setCode("30");
                responseData.setMsg("卡片故障，请换卡重试");
                return;
            } else if ("65".equals(errorCode)) {
                logger.error(cardNo + "：超出取款/消费次数限制");
                responseData.setCode("65");
                responseData.setMsg("超出取款/消费次数限制");
                return;
            } else if ("A4".equals(errorCode)) {
                logger.error(cardNo + "：授权.找不到授权终端");
                responseData.setCode("A4");
                responseData.setMsg("授权.找不到授权终端");
                return;
            } else if ("I4".equals(errorCode)) {
                logger.error(cardNo + "：请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                responseData.setCode("I4");
                responseData.setMsg("请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                return;
            } else if ("BB".equals(errorCode)) {
                logger.error(cardNo + "：CVV 错误次数超限");
                responseData.setCode("BB");
                responseData.setMsg("CVV 错误次数超限");
                return;
            } else if ("W4".equals(errorCode)) {
                logger.error(cardNo + "：姓名与开户时登记的不一致");
                responseData.setCode("W4");
                responseData.setMsg("姓名与开户时登记的不一致");
                return;
            } else if ("02".equals(errorCode)) {
                logger.error(cardNo + "：请联系快钱公司");
                responseData.setCode("02");
                responseData.setMsg("请联系快钱公司");
                return;
            } else if ("92".equals(errorCode)) {
                logger.error(cardNo + "：与银行通信网络故障，请稍后重试");
                responseData.setCode("92");
                responseData.setMsg("与银行通信网络故障，请稍后重试");
                return;
            } else if ("G3".equals(errorCode)) {
                logger.error(cardNo + "：超出系统当日金额限制");
                responseData.setCode("G3");
                responseData.setMsg("超出系统当日金额限制");
                return;
            } else if ("07".equals(errorCode)) {
                logger.error(cardNo + "：特定条件下没收卡");
                responseData.setCode("07");
                responseData.setMsg("特定条件下没收卡");
                return;
            } else if ("80".equals(errorCode)) {
                logger.error(cardNo + "：交易拒绝");
                responseData.setCode("80");
                responseData.setMsg("交易拒绝");
                return;
            } else if ("KG".equals(errorCode)) {
                logger.error(cardNo + "：卡状态、户口无效或不存在，拒绝交易对照");
                responseData.setCode("KG");
                responseData.setMsg("卡状态、户口无效或不存在，拒绝交易对照");
                return;
            } else if ("N9".equals(errorCode)) {
                logger.error(cardNo + "：请持卡人重新进行卡信息验证");
                responseData.setCode("N9");
                responseData.setMsg("请持卡人重新进行卡信息验证");
                return;
            } else if ("OB".equals(errorCode)) {
                logger.error(cardNo + "：不受理的银行卡，请换卡重试");
                responseData.setCode("OB");
                responseData.setMsg("不受理的银行卡，请换卡重试");
                return;
            } else if ("08".equals(errorCode)) {
                logger.error(cardNo + "：请与银行联系");
                responseData.setCode("08");
                responseData.setMsg("请与银行联系");
                return;
            } else if ("03".equals(errorCode)) {
                logger.error(cardNo + "：无效商户");
                responseData.setCode("03");
                responseData.setMsg("无效商户");
                return;
            } else if ("EQ".equals(errorCode)) {
                logger.error(cardNo + "：未找到绑定关系");
                responseData.setCode("EQ");
                responseData.setMsg("未找到绑定关系");
                return;
            } else if ("HI".equals(errorCode)) {
                logger.error(cardNo + "：当天流水号重复");
                responseData.setCode("HI");
                responseData.setMsg("当天流水号重复");
                return;
            } else if ("L8".equals(errorCode)) {
                logger.error(cardNo + "：找不到路由");
                responseData.setCode("L8");
                responseData.setMsg("找不到路由");
                return;
            } else if ("I2".equals(errorCode)) {
                logger.error(cardNo + "：请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                responseData.setCode("I2");
                responseData.setMsg("请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                return;
            } else if ("41".equals(errorCode)) {
                logger.error(cardNo + "：此卡为挂失卡");
                responseData.setCode("41");
                responseData.setMsg("此卡为挂失卡");
                return;
            } else if ("W6".equals(errorCode)) {
                logger.error(cardNo + "：手机号、身份证号码、姓名与开户时登记的不一致");
                responseData.setCode("W6");
                responseData.setMsg("手机号、身份证号码、姓名与开户时登记的不一致");
                return;
            } else if ("NH".equals(errorCode)) {
                logger.error(cardNo + "：卡已锁");
                responseData.setCode("NH");
                responseData.setMsg("卡已锁");
                return;
            } else if ("OQ".equals(errorCode)) {
                logger.error(cardNo + "：销售日限额已经用完");
                responseData.setCode("OQ");
                responseData.setMsg("销售日限额已经用完");
                return;
            } else if ("B5".equals(errorCode)) {
                logger.error(cardNo + "：系统维护中，请稍后再试");
                responseData.setCode("B5");
                responseData.setMsg("系统维护中，请稍后再试");
                return;
            } else if ("57".equals(errorCode)) {
                logger.error(cardNo + "：不允许持卡人进行的交易");
                responseData.setCode("57");
                responseData.setMsg("不允许持卡人进行的交易");
                return;
            } else if ("M2".equals(errorCode)) {
                logger.error(cardNo + "：商户状态不匹配");
                responseData.setCode("M2");
                responseData.setMsg("商户状态不匹配");
                return;
            } else if ("36".equals(errorCode)) {
                logger.error(cardNo + "：受限制的卡");
                responseData.setCode("36");
                responseData.setMsg("受限制的卡");
                return;
            } else if ("BC".equals(errorCode)) {
                logger.error(cardNo + "：无效卡");
                responseData.setCode("BC");
                responseData.setMsg("无效卡");
                return;
            } else if ("CD".equals(errorCode)) {
                logger.error(cardNo + "：卡状态异常或户名证件号不符");
                responseData.setCode("CD");
                responseData.setMsg("卡状态异常或户名证件号不符");
                return;
            } else if ("HX".equals(errorCode)) {
                logger.error(cardNo + "：姓名不符");
                responseData.setCode("HX");
                responseData.setMsg("姓名不符");
                return;
            } else if ("HY".equals(errorCode)) {
                logger.error(cardNo + "：证件类型不符");
                responseData.setCode("HY");
                responseData.setMsg("证件类型不符");
                return;
            } else if ("W0".equals(errorCode)) {
                logger.error(cardNo + "：手机号与开户时登记的不一致");
                responseData.setCode("W0");
                responseData.setMsg("手机号与开户时登记的不一致");
                return;
            } else if ("I1".equals(errorCode)) {
                logger.error(cardNo + "：请提供正确的持卡人姓名");
                responseData.setCode("I1");
                responseData.setMsg("请提供正确的持卡人姓名");
                return;
            } else if ("MR".equals(errorCode)) {
                logger.error(cardNo + "：商户不支持的卡类型");
                responseData.setCode("MR");
                responseData.setMsg("商户不支持的卡类型");
                return;
            } else if ("I3".equals(errorCode)) {
                logger.error(cardNo + "：请提供正确的证件号码，必须与申请银行卡时的证件号码一致");
                responseData.setCode("I3");
                responseData.setMsg("请提供正确的证件号码，必须与申请银行卡时的证件号码一致");
                return;
            } else if ("cc".equals(errorCode)) {
                logger.error(cardNo + "：此卡未在银行预留绑定手机号，请联系发卡行");
                responseData.setCode("cc");
                responseData.setMsg("此卡未在银行预留绑定手机号，请联系发卡行");
                return;
            } else if ("HV".equals(errorCode)) {
                logger.error(cardNo + "：CVV2 不符");
                responseData.setCode("HV");
                responseData.setMsg("CVV2 不符");
                return;
            } else if ("12".equals(errorCode)) {
                logger.error(cardNo + "：无效交易");
                responseData.setCode("12");
                responseData.setMsg("无效交易");
                return;
            } else if ("OS".equals(errorCode)) {
                logger.error(cardNo + "：找不到对应的结算商户");
                responseData.setCode("OS");
                responseData.setMsg("找不到对应的结算商户");
                return;
            } else if ("04".equals(errorCode)) {
                logger.error(cardNo + "：无效终端");
                responseData.setCode("04");
                responseData.setMsg("无效终端");
                return;
            } else if ("LG".equals(errorCode)) {
                logger.error(cardNo + "：该银行卡未开通银联在线支付业务");
                responseData.setCode("LG");
                responseData.setMsg("该银行卡未开通银联在线支付业务");
                return;
            } else if ("AP".equals(errorCode)) {
                logger.error(cardNo + "：不支持的证件类型/证件号");
                responseData.setCode("AP");
                responseData.setMsg("不支持的证件类型/证件号");
                return;
            } else if ("Q2".equals(errorCode)) {
                logger.error(cardNo + "：有效期错，请核实重输或联系发卡行");
                responseData.setCode("Q2");
                responseData.setMsg("有效期错，请核实重输或联系发卡行");
                return;
            } else if ("ZH".equals(errorCode)) {
                logger.error(cardNo + "：商户不支持的短信验证模式");
                responseData.setCode("ZH");
                responseData.setMsg("商户不支持的短信验证模式");
                return;
            } else if ("53".equals(errorCode)) {
                logger.error(cardNo + "：无此储蓄卡账户");
                responseData.setCode("53");
                responseData.setMsg("无此储蓄卡账户");
                return;
            } else if ("B.MGW.0120".equals(errorCode)) {
                logger.error(cardNo + "：卡号长度不对");
                responseData.setCode("B.MGW.0120");
                responseData.setMsg("卡号长度不对");
                return;
            } else if ("B.BIN.0005".equals(errorCode)) {
                logger.error(cardNo + "：卡号格式错误");
                responseData.setCode("B.BIN.0005");
                responseData.setMsg("卡号格式错误");
                return;
            } else if ("T3".equals(errorCode)) {
                logger.error(cardNo + "：一个银行不能绑定多张卡或者未绑定");
                responseData.setCode("T3");
                responseData.setMsg("一个银行不能绑定多张卡或者未绑定");
                return;
            } else if ("T5".equals(errorCode)) {
                logger.error(cardNo + "：交易信息被篡改");
                responseData.setCode("T5");
                responseData.setMsg("交易信息被篡改");
                return;
            } else if ("T8".equals(errorCode)) {
                logger.error(cardNo + "：解绑失败，未绑定相关信息");
                responseData.setCode("T8");
                responseData.setMsg("解绑失败，未绑定相关信息");
                return;
            } else if ("L9".equals(errorCode)) {
                logger.error(cardNo + "：错误的卡号校验位");
                responseData.setCode("L9");
                responseData.setMsg("错误的卡号校验位");
                return;
            } else if ("B.MGW.0121".equals(errorCode)) {
                logger.error(cardNo + "：当日重复绑卡次数超限");
                responseData.setCode("B.MGW.0121");
                responseData.setMsg("当日重复绑卡次数超限");
                return;
            } else if ("HT".equals(errorCode)) {
                logger.error("证件号异常或超过有效期");
                responseData.setCode("HT");
                responseData.setMsg("证件号异常或超过有效期");
                return;
            }
        }




    }
}
