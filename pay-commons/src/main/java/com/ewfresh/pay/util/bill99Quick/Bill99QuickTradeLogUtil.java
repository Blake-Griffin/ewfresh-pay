package com.ewfresh.pay.util.bill99quick;

import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 根据code打印相关信息（快钱快捷支付时）
 * @author: JiuDongDong
 * date: 2018/10/9.
 */
public final class Bill99QuickTradeLogUtil {
    private static Logger logger = LoggerFactory.getLogger(Bill99QuickTradeLogUtil.class);

    /**
     * Description: 打印快钱快捷处理订单支付交易结果
     * @author: JiuDongDong
     * @param responseData 封装返回信息
     * @param responseCode 快钱返回的状态码
     * @param responseTextMessage 快钱返回的响应信息
     * @param errorCode 快钱返回的错误码
     * @param errorMessage  快钱返回的错误信息
     * @param externalRefNumber  订单号
     * date: 2018/10/9 17:29
     */
    public static void logTradeInfo(ResponseData responseData, String responseCode, String responseTextMessage,
                               String errorCode, String errorMessage, String externalRefNumber) {
        if ("00".equals(responseCode)) {
            logger.info("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
            logger.info("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);
            logger.info(externalRefNumber + "：交易成功");
            return;
        }
        // 打印错误信息
        logger.error("The responseCode = " + responseCode + ", responseTextMessage = " + responseTextMessage);
        logger.error("The errorCode = " + errorCode + ", errorMessage = " + errorMessage);

        if (StringUtils.isNotBlank(responseCode)) {
            switch (responseCode) {
                case "51" :
                    logger.error(externalRefNumber + "：卡余额不足，请换卡交易");
                    responseData.setCode("51");
                    responseData.setMsg("卡余额不足，请换卡交易");
                    break;
                case "I8" :
                    logger.error(externalRefNumber + "：金额超限或其他");
                    responseData.setCode("I8");
                    responseData.setMsg("金额超限或其他");
                    break;
                case "T3" :
                    logger.error(externalRefNumber + "：一个银行不能绑定多张卡或者未绑定");
                    responseData.setCode("T3");
                    responseData.setMsg("一个银行不能绑定多张卡或者未绑定");
                    break;
                case "T6" :
                    logger.error(externalRefNumber + "：验证码错误，请核对您的验证码信息或重新获取");
                    responseData.setCode("T6");
                    responseData.setMsg("验证码错误，请核对您的验证码信息或重新获取");
                    break;
                case "01" :
                    logger.error(externalRefNumber + "：请联系发卡行，或核对卡信息后重新输入");
                    responseData.setCode("01");
                    responseData.setMsg("请联系发卡行，或核对卡信息后重新输入");
                    break;
                case "62" :
                    logger.error(externalRefNumber + "：受限制的卡，请换卡重试");
                    responseData.setCode("62");
                    responseData.setMsg("受限制的卡，请换卡重试");
                    break;
                case "61" :
                    logger.error(externalRefNumber + "：超出取款转账金额限制，联系发卡行");
                    responseData.setCode("61");
                    responseData.setMsg("超出取款转账金额限制，联系发卡行");
                    break;
                case "OT" :
                    logger.error(externalRefNumber + "：交易金额太小");
                    responseData.setCode("OT");
                    responseData.setMsg("交易金额太小");
                    break;
                case "68" :
                    logger.error(externalRefNumber + "：无法在正常时间内获得交易应答，请稍后重试");
                    responseData.setCode("68");
                    responseData.setMsg("无法在正常时间内获得交易应答，请稍后重试");
                    break;
                case "54" :
                    logger.error(externalRefNumber + "：卡片已过期，请换卡后交易");
                    responseData.setCode("54");
                    responseData.setMsg("卡片已过期，请换卡后交易");
                    break;
                case "OR" :
                    logger.error(externalRefNumber + "：订单号重复");
                    responseData.setCode("OR");
                    responseData.setMsg("订单号重复");
                    break;
                case "16" :
                    logger.error(externalRefNumber + "：金额超出限制");
                    responseData.setCode("16");
                    responseData.setMsg("金额超出限制");
                    break;
                case "Y1" :
                    logger.error(externalRefNumber + "：身份认证失败");
                    responseData.setCode("Y1");
                    responseData.setMsg("身份认证失败");
                    break;
                case "KJ" :
                    logger.error(externalRefNumber + "：交易失败");
                    responseData.setCode("KJ");
                    responseData.setMsg("交易失败");
                    break;
                case "HW" :
                    logger.error(externalRefNumber + "：手机号码不符");
                    responseData.setCode("HW");
                    responseData.setMsg("手机号码不符");
                    break;
                case "96" :
                    logger.error(externalRefNumber + "：系统异常、失效");
                    responseData.setCode("96");
                    responseData.setMsg("系统异常、失效");
                    break;
                case "91" :
                    logger.error(externalRefNumber + "：请稍候重新交易");
                    responseData.setCode("91");
                    responseData.setMsg("请稍候重新交易");
                    break;
                case "IA" :
                    logger.error(externalRefNumber + "：请提供正确的手机号");
                    responseData.setCode("IA");
                    responseData.setMsg("请提供正确的手机号");
                    break;
                case "HU" :
                    logger.error(externalRefNumber + "：有效期不符");
                    responseData.setCode("HU");
                    responseData.setMsg("有效期不符");
                    break;
                case "05" :
                    logger.error(externalRefNumber + "：不予承兑");
                    responseData.setCode("05");
                    responseData.setMsg("不予承兑");
                    break;
                case "BA" :
                    logger.error(externalRefNumber + "：卡信息错误次数超限，请联系发卡行");
                    responseData.setCode("BA");
                    responseData.setMsg("卡信息错误次数超限，请联系发卡行");
                    break;
                case "14" :
                    logger.error(externalRefNumber + "：无效卡号（无此号），请换卡重试");
                    responseData.setCode("14");
                    responseData.setMsg("无效卡号（无此号），请换卡重试");
                    break;
                case "OG" :
                    logger.error(externalRefNumber + "：单笔或日限额超过上限，请联系银行");
                    responseData.setCode("OG");
                    responseData.setMsg("单笔或日限额超过上限，请联系银行");
                    break;
                case "20" :
                    logger.error(externalRefNumber + "：卡信息提供有误");
                    responseData.setCode("20");
                    responseData.setMsg("卡信息提供有误");
                    break;
                case "T0" :
                    logger.error(externalRefNumber + "：验证码已失效，请重新获取");
                    responseData.setCode("T0");
                    responseData.setMsg("验证码已失效，请重新获取");
                    break;
                case "ZG" :
                    logger.error(externalRefNumber + "：请重新签约");
                    responseData.setCode("ZG");
                    responseData.setMsg("请重新签约");
                    break;
                case "CB" :
                    logger.error(externalRefNumber + "：银行系统异常、失效，请稍后重试");
                    responseData.setCode("CB");
                    responseData.setMsg("银行系统异常、失效，请稍后重试");
                    break;
                case "25" :
                    logger.error(externalRefNumber + "：未找到原始交易，请重新交易");
                    responseData.setCode("25");
                    responseData.setMsg("未找到原始交易，请重新交易");
                    break;
                case "R0" :
                    logger.error(externalRefNumber + "：交易不予承兑，请换卡重试");
                    responseData.setCode("R0");
                    responseData.setMsg("交易不予承兑，请换卡重试");
                    break;
                case "HZ" :
                    logger.error(externalRefNumber + "：证件号不符");
                    responseData.setCode("HZ");
                    responseData.setMsg("证件号不符");
                    break;
                case "I7" :
                    logger.error(externalRefNumber + "：CVV2 或有效期错");
                    responseData.setCode("I7");
                    responseData.setMsg("CVV2 或有效期错");
                    break;
                case "30" :
                    logger.error(externalRefNumber + "：卡片故障，请换卡重试");
                    responseData.setCode("30");
                    responseData.setMsg("卡片故障，请换卡重试");
                    break;
                case "65" :
                    logger.error(externalRefNumber + "：超出取款/消费次数限制");
                    responseData.setCode("65");
                    responseData.setMsg("超出取款/消费次数限制");
                    break;
                case "A4" :
                    logger.error(externalRefNumber + "：授权.找不到授权终端");
                    responseData.setCode("A4");
                    responseData.setMsg("授权.找不到授权终端");
                    break;
                case "I4" :
                    logger.error(externalRefNumber + "：请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                    responseData.setCode("I4");
                    responseData.setMsg("请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                    break;
                case "BB" :
                    logger.error(externalRefNumber + "：CVV 错误次数超限");
                    responseData.setCode("BB");
                    responseData.setMsg("CVV 错误次数超限");
                    break;
                case "W4" :
                    logger.error(externalRefNumber + "：姓名与开户时登记的不一致");
                    responseData.setCode("W4");
                    responseData.setMsg("姓名与开户时登记的不一致");
                    break;
                case "02" :
                    logger.error(externalRefNumber + "：请联系快钱公司");
                    responseData.setCode("02");
                    responseData.setMsg("请联系快钱公司");
                    break;
                case "92" :
                    logger.error(externalRefNumber + "：与银行通信网络故障，请稍后重试");
                    responseData.setCode("92");
                    responseData.setMsg("与银行通信网络故障，请稍后重试");
                    break;
                case "G3" :
                    logger.error(externalRefNumber + "：超出系统当日金额限制");
                    responseData.setCode("G3");
                    responseData.setMsg("超出系统当日金额限制");
                    break;
                case "07" :
                    logger.error(externalRefNumber + "：特定条件下没收卡");
                    responseData.setCode("07");
                    responseData.setMsg("特定条件下没收卡");
                    break;
                case "80" :
                    logger.error(externalRefNumber + "：交易拒绝");
                    responseData.setCode("80");
                    responseData.setMsg("交易拒绝");
                    break;
                case "KG" :
                    logger.error(externalRefNumber + "：卡状态、户口无效或不存在，拒绝交易对照");
                    responseData.setCode("KG");
                    responseData.setMsg("卡状态、户口无效或不存在，拒绝交易对照");
                    break;
                case "N9" :
                    logger.error(externalRefNumber + "：请持卡人重新进行卡信息验证");
                    responseData.setCode("N9");
                    responseData.setMsg("请持卡人重新进行卡信息验证");
                    break;
                case "OB" :
                    logger.error(externalRefNumber + "：不受理的银行卡，请换卡重试");
                    responseData.setCode("OB");
                    responseData.setMsg("不受理的银行卡，请换卡重试");
                    break;
                case "08" :
                    logger.error(externalRefNumber + "：请与银行联系");
                    responseData.setCode("08");
                    responseData.setMsg("请与银行联系");
                    break;
                case "03" :
                    logger.error(externalRefNumber + "：无效商户");
                    responseData.setCode("03");
                    responseData.setMsg("无效商户");
                    break;
                case "EQ" :
                    logger.error(externalRefNumber + "：未找到绑定关系");
                    responseData.setCode("EQ");
                    responseData.setMsg("未找到绑定关系");
                    break;
                case "HI" :
                    logger.error(externalRefNumber + "：当天流水号重复");
                    responseData.setCode("HI");
                    responseData.setMsg("当天流水号重复");
                    break;
                case "L8" :
                    logger.error(externalRefNumber + "：找不到路由");
                    responseData.setCode("L8");
                    responseData.setMsg("找不到路由");
                    break;
                case "I2" :
                    logger.error(externalRefNumber + "：请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                    responseData.setCode("I2");
                    responseData.setMsg("请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                    break;
                case "41" :
                    logger.error(externalRefNumber + "：此卡为挂失卡");
                    responseData.setCode("41");
                    responseData.setMsg("此卡为挂失卡");
                    break;
                case "W6" :
                    logger.error(externalRefNumber + "：手机号、身份证号码、姓名与开户时登记的不一致");
                    responseData.setCode("W6");
                    responseData.setMsg("手机号、身份证号码、姓名与开户时登记的不一致");
                    break;
                case "NH" :
                    logger.error(externalRefNumber + "：卡已锁");
                    responseData.setCode("NH");
                    responseData.setMsg("卡已锁");
                    break;
                case "OQ" :
                    logger.error(externalRefNumber + "：销售日限额已经用完");
                    responseData.setCode("OQ");
                    responseData.setMsg("销售日限额已经用完");
                    break;
                case "B5" :
                    logger.error(externalRefNumber + "：系统维护中，请稍后再试");
                    responseData.setCode("B5");
                    responseData.setMsg("系统维护中，请稍后再试");
                    break;
                case "57" :
                    logger.error(externalRefNumber + "：不允许持卡人进行的交易");
                    responseData.setCode("57");
                    responseData.setMsg("不允许持卡人进行的交易");
                    break;
                case "" :
                    logger.error(externalRefNumber + "：商户状态不匹配");
                    responseData.setCode("M2");
                    responseData.setMsg("商户状态不匹配");
                    break;
                case "36" :
                    logger.error(externalRefNumber + "：受限制的卡");
                    responseData.setCode("36");
                    responseData.setMsg("受限制的卡");
                    break;
                case "BC" :
                    logger.error(externalRefNumber + "：无效卡");
                    responseData.setCode("BC");
                    responseData.setMsg("无效卡");
                    break;
                case "CD" :
                    logger.error(externalRefNumber + "：卡状态异常或户名证件号不符");
                    responseData.setCode("CD");
                    responseData.setMsg("卡状态异常或户名证件号不符");
                    break;
                case "TC" :
                    logger.error(externalRefNumber + "：商户权限不足");
                    responseData.setCode("TC");
                    responseData.setMsg("商户权限不足");
                    break;
                case "B.MGW.0120" :
                    logger.error(externalRefNumber + "：交易类型编码错误");
                    responseData.setCode("B.MGW.0120");
                    responseData.setMsg("交易类型编码错误");
                    break;
                case "B.MGW.0130" :
                    logger.error(externalRefNumber + "：不支持的交易类型");
                    responseData.setCode("B.MGW.0130");
                    responseData.setMsg("不支持的交易类型");
                    break;
                case "B.MGW.0170" :
                    logger.error(externalRefNumber + "：无此交易");
                    responseData.setCode("B.MGW.0170");
                    responseData.setMsg("无此交易");
                    break;
                case "O2" :
                    logger.error(externalRefNumber + "：未找到原始交易");
                    responseData.setCode("O2");
                    responseData.setMsg("未找到原始交易");
                    break;
                case "OC" :
                    logger.error(externalRefNumber + "：退货金额大于原金额");
                    responseData.setCode("OC");
                    responseData.setMsg("退货金额大于原金额");
                    break;
                case "LG" :
                    logger.error("该银行卡未开通银联在线支付业务");
                    responseData.setCode("LG");
                    responseData.setMsg("该银行卡未开通银联在线支付业务");
                    break;
                case "G0" :
                    logger.error("超出单笔金额上限");
                    responseData.setCode("G0");
                    responseData.setMsg("超出单笔金额上限");
                    break;
                case "G7" :
                    logger.error("超出系统当月金额限制");
                    responseData.setCode("G7");
                    responseData.setMsg("超出系统当月金额限制");
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
                logger.error(externalRefNumber + "：卡余额不足，请换卡交易");
                responseData.setCode("51");
                responseData.setMsg("卡余额不足，请换卡交易");
                return;
            } else if ("I8".equals(errorCode)) {
                logger.error(externalRefNumber + "：金额超限或其他");
                responseData.setCode("I8");
                responseData.setMsg("金额超限或其他");
                return;
            } else if ("T3".equals(errorCode)) {
                logger.error(externalRefNumber + "：一个银行不能绑定多张卡或者未绑定");
                responseData.setCode("T3");
                responseData.setMsg("一个银行不能绑定多张卡或者未绑定");
                return;
            } else if ("T6".equals(errorCode)) {
                logger.error(externalRefNumber + "：验证码错误，请核对您的验证码信息或重新获取");
                responseData.setCode("T6");
                responseData.setMsg("验证码错误，请核对您的验证码信息或重新获取");
                return;
            } else if ("01".equals(errorCode)) {
                logger.error(externalRefNumber + "：请联系发卡行，或核对卡信息后重新输入");
                responseData.setCode("01");
                responseData.setMsg("请联系发卡行，或核对卡信息后重新输入");
                return;
            } else if ("62".equals(errorCode)) {
                logger.error(externalRefNumber + "：受限制的卡，请换卡重试");
                responseData.setCode("62");
                responseData.setMsg("受限制的卡，请换卡重试");
                return;
            } else if ("61".equals(errorCode)) {
                logger.error(externalRefNumber + "：超出取款转账金额限制，联系发卡行");
                responseData.setCode("61");
                responseData.setMsg("超出取款转账金额限制，联系发卡行");
                return;
            } else if ("OT".equals(errorCode)) {
                logger.error(externalRefNumber + "：交易金额太小");
                responseData.setCode("OT");
                responseData.setMsg("交易金额太小");
                return;
            } else if ("68".equals(errorCode)) {
                logger.error(externalRefNumber + "：无法在正常时间内获得交易应答，请稍后重试");
                responseData.setCode("68");
                responseData.setMsg("无法在正常时间内获得交易应答，请稍后重试");
                return;
            } else if ("54".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡片已过期，请换卡后交易");
                responseData.setCode("54");
                responseData.setMsg("卡片已过期，请换卡后交易");
                return;
            } else if ("OR".equals(errorCode)) {
                logger.error(externalRefNumber + "：订单号重复");
                responseData.setCode("OR");
                responseData.setMsg("订单号重复");
                return;
            } else if ("16".equals(errorCode)) {
                logger.error(externalRefNumber + "：金额超出限制");
                responseData.setCode("16");
                responseData.setMsg("金额超出限制");
                return;
            } else if ("Y1".equals(errorCode)) {
                logger.error(externalRefNumber + "：身份认证失败");
                responseData.setCode("Y1");
                responseData.setMsg("身份认证失败");
                return;
            } else if ("KJ".equals(errorCode)) {
                logger.error(externalRefNumber + "：交易失败");
                responseData.setCode("KJ");
                responseData.setMsg("交易失败");
                return;
            } else if ("HW".equals(errorCode)) {
                logger.error(externalRefNumber + "：手机号码不符");
                responseData.setCode("HW");
                responseData.setMsg("手机号码不符");
                return;
            } else if ("96".equals(errorCode)) {
                logger.error(externalRefNumber + "：系统异常、失效");
                responseData.setCode("96");
                responseData.setMsg("系统异常、失效");
                return;
            } else if ("91".equals(errorCode)) {
                logger.error(externalRefNumber + "：请稍候重新交易");
                responseData.setCode("91");
                responseData.setMsg("请稍候重新交易");
                return;
            } else if ("IA".equals(errorCode)) {
                logger.error(externalRefNumber + "：请提供正确的手机号");
                responseData.setCode("IA");
                responseData.setMsg("请提供正确的手机号");
                return;
            } else if ("HU".equals(errorCode)) {
                logger.error(externalRefNumber + "：有效期不符");
                responseData.setCode("HU");
                responseData.setMsg("有效期不符");
                return;
            } else if ("05".equals(errorCode)) {
                logger.error(externalRefNumber + "：不予承兑");
                responseData.setCode("05");
                responseData.setMsg("不予承兑");
                return;
            } else if ("BA".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡信息错误次数超限，请联系发卡行");
                responseData.setCode("BA");
                responseData.setMsg("卡信息错误次数超限，请联系发卡行");
                return;
            } else if ("14".equals(errorCode)) {
                logger.error(externalRefNumber + "：无效卡号（无此号），请换卡重试");
                responseData.setCode("14");
                responseData.setMsg("无效卡号（无此号），请换卡重试");
                return;
            } else if ("OG".equals(errorCode)) {
                logger.error(externalRefNumber + "：单笔或日限额超过上限，请联系银行");
                responseData.setCode("OG");
                responseData.setMsg("单笔或日限额超过上限，请联系银行");
                return;
            } else if ("20".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡信息提供有误");
                responseData.setCode("20");
                responseData.setMsg("卡信息提供有误");
                return;
            } else if ("T0".equals(errorCode)) {
                logger.error(externalRefNumber + "：验证码已失效，请重新获取");
                responseData.setCode("T0");
                responseData.setMsg("验证码已失效，请重新获取");
                return;
            } else if ("ZG".equals(errorCode)) {
                logger.error(externalRefNumber + "：请重新签约");
                responseData.setCode("ZG");
                responseData.setMsg("请重新签约");
                return;
            } else if ("CB".equals(errorCode)) {
                logger.error(externalRefNumber + "：银行系统异常、失效，请稍后重试");
                responseData.setCode("CB");
                responseData.setMsg("银行系统异常、失效，请稍后重试");
                return;
            } else if ("25".equals(errorCode)) {
                logger.error(externalRefNumber + "：未找到原始交易，请重新交易");
                responseData.setCode("25");
                responseData.setMsg("未找到原始交易，请重新交易");
                return;
            } else if ("R0".equals(errorCode)) {
                logger.error(externalRefNumber + "：交易不予承兑，请换卡重试");
                responseData.setCode("R0");
                responseData.setMsg("交易不予承兑，请换卡重试");
                return;
            } else if ("HZ".equals(errorCode)) {
                logger.error(externalRefNumber + "：证件号不符");
                responseData.setCode("HZ");
                responseData.setMsg("证件号不符");
                return;
            } else if ("I7".equals(errorCode)) {
                logger.error(externalRefNumber + "：CVV2 或有效期错");
                responseData.setCode("I7");
                responseData.setMsg("CVV2 或有效期错");
                return;
            } else if ("30".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡片故障，请换卡重试");
                responseData.setCode("30");
                responseData.setMsg("卡片故障，请换卡重试");
                return;
            } else if ("65".equals(errorCode)) {
                logger.error(externalRefNumber + "：超出取款/消费次数限制");
                responseData.setCode("65");
                responseData.setMsg("超出取款/消费次数限制");
                return;
            } else if ("A4".equals(errorCode)) {
                logger.error(externalRefNumber + "：授权.找不到授权终端");
                responseData.setCode("A4");
                responseData.setMsg("授权.找不到授权终端");
                return;
            } else if ("I4".equals(errorCode)) {
                logger.error(externalRefNumber + "：请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                responseData.setCode("I4");
                responseData.setMsg("请提供正确的卡有效期，卡有效期是在卡号下面的 4 位数字");
                return;
            } else if ("BB".equals(errorCode)) {
                logger.error(externalRefNumber + "：CVV 错误次数超限");
                responseData.setCode("BB");
                responseData.setMsg("CVV 错误次数超限");
                return;
            } else if ("W4".equals(errorCode)) {
                logger.error(externalRefNumber + "：姓名与开户时登记的不一致");
                responseData.setCode("W4");
                responseData.setMsg("姓名与开户时登记的不一致");
                return;
            } else if ("02".equals(errorCode)) {
                logger.error(externalRefNumber + "：请联系快钱公司");
                responseData.setCode("02");
                responseData.setMsg("请联系快钱公司");
                return;
            } else if ("92".equals(errorCode)) {
                logger.error(externalRefNumber + "：与银行通信网络故障，请稍后重试");
                responseData.setCode("92");
                responseData.setMsg("与银行通信网络故障，请稍后重试");
                return;
            } else if ("G3".equals(errorCode)) {
                logger.error(externalRefNumber + "：超出系统当日金额限制");
                responseData.setCode("G3");
                responseData.setMsg("超出系统当日金额限制");
                return;
            } else if ("07".equals(errorCode)) {
                logger.error(externalRefNumber + "：特定条件下没收卡");
                responseData.setCode("07");
                responseData.setMsg("特定条件下没收卡");
                return;
            } else if ("80".equals(errorCode)) {
                logger.error(externalRefNumber + "：交易拒绝");
                responseData.setCode("80");
                responseData.setMsg("交易拒绝");
                return;
            } else if ("KG".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡状态、户口无效或不存在，拒绝交易对照");
                responseData.setCode("KG");
                responseData.setMsg("卡状态、户口无效或不存在，拒绝交易对照");
                return;
            } else if ("N9".equals(errorCode)) {
                logger.error(externalRefNumber + "：请持卡人重新进行卡信息验证");
                responseData.setCode("N9");
                responseData.setMsg("请持卡人重新进行卡信息验证");
                return;
            } else if ("OB".equals(errorCode)) {
                logger.error(externalRefNumber + "：不受理的银行卡，请换卡重试");
                responseData.setCode("OB");
                responseData.setMsg("不受理的银行卡，请换卡重试");
                return;
            } else if ("08".equals(errorCode)) {
                logger.error(externalRefNumber + "：请与银行联系");
                responseData.setCode("08");
                responseData.setMsg("请与银行联系");
                return;
            } else if ("03".equals(errorCode)) {
                logger.error(externalRefNumber + "：无效商户");
                responseData.setCode("03");
                responseData.setMsg("无效商户");
                return;
            } else if ("EQ".equals(errorCode)) {
                logger.error(externalRefNumber + "：未找到绑定关系");
                responseData.setCode("EQ");
                responseData.setMsg("未找到绑定关系");
                return;
            } else if ("HI".equals(errorCode)) {
                logger.error(externalRefNumber + "：当天流水号重复");
                responseData.setCode("HI");
                responseData.setMsg("当天流水号重复");
                return;
            } else if ("L8".equals(errorCode)) {
                logger.error(externalRefNumber + "：找不到路由");
                responseData.setCode("L8");
                responseData.setMsg("找不到路由");
                return;
            } else if ("I2".equals(errorCode)) {
                logger.error(externalRefNumber + "：请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                responseData.setCode("I2");
                responseData.setMsg("请提供正确的验证码（CVV2），验证码在卡背面签名栏后的三位数字串");
                return;
            } else if ("41".equals(errorCode)) {
                logger.error(externalRefNumber + "：此卡为挂失卡");
                responseData.setCode("41");
                responseData.setMsg("此卡为挂失卡");
                return;
            } else if ("W6".equals(errorCode)) {
                logger.error(externalRefNumber + "：手机号、身份证号码、姓名与开户时登记的不一致");
                responseData.setCode("W6");
                responseData.setMsg("手机号、身份证号码、姓名与开户时登记的不一致");
                return;
            } else if ("NH".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡已锁");
                responseData.setCode("NH");
                responseData.setMsg("卡已锁");
                return;
            } else if ("OQ".equals(errorCode)) {
                logger.error(externalRefNumber + "：销售日限额已经用完");
                responseData.setCode("OQ");
                responseData.setMsg("销售日限额已经用完");
                return;
            } else if ("B5".equals(errorCode)) {
                logger.error(externalRefNumber + "：系统维护中，请稍后再试");
                responseData.setCode("B5");
                responseData.setMsg("系统维护中，请稍后再试");
                return;
            } else if ("57".equals(errorCode)) {
                logger.error(externalRefNumber + "：不允许持卡人进行的交易");
                responseData.setCode("57");
                responseData.setMsg("不允许持卡人进行的交易");
                return;
            } else if ("M2".equals(errorCode)) {
                logger.error(externalRefNumber + "：商户状态不匹配");
                responseData.setCode("M2");
                responseData.setMsg("商户状态不匹配");
                return;
            } else if ("36".equals(errorCode)) {
                logger.error(externalRefNumber + "：受限制的卡");
                responseData.setCode("36");
                responseData.setMsg("受限制的卡");
                return;
            } else if ("BC".equals(errorCode)) {
                logger.error(externalRefNumber + "：无效卡");
                responseData.setCode("BC");
                responseData.setMsg("无效卡");
                return;
            } else if ("CD".equals(errorCode)) {
                logger.error(externalRefNumber + "：卡状态异常或户名证件号不符");
                responseData.setCode("CD");
                responseData.setMsg("卡状态异常或户名证件号不符");
                return;
            } else if ("TC".equals(errorCode)) {
                logger.error(externalRefNumber + "：商户权限不足");
                responseData.setCode("TC");
                responseData.setMsg("商户权限不足");
                return;
            } else if ("B.MGW.0120".equals(errorCode)) {
                logger.error(externalRefNumber + "：交易类型编码错误");
                responseData.setCode("B.MGW.0120");
                responseData.setMsg("交易类型编码错误");
                return;
            } else if ("B.MGW.0170".equals(errorCode)) {
                logger.error(externalRefNumber + "：无此交易");
                responseData.setCode("B.MGW.0170");
                responseData.setMsg("无此交易");
                return;
            } else if ("O2".equals(errorCode)) {
                logger.error(externalRefNumber + "：未找到原始交易");
                responseData.setCode("O2");
                responseData.setMsg("未找到原始交易");
                return;
            } else if ("OC".equals(errorCode)) {
                logger.error(externalRefNumber + "：退货金额大于原金额");
                responseData.setCode("OC");
                responseData.setMsg("退货金额大于原金额");
                return;
            } else if ("B.MGW.0130".equals(errorCode)) {
                logger.error(externalRefNumber + "：不支持的交易类型");
                responseData.setCode("B.MGW.0130");
                responseData.setMsg("不支持的交易类型");
                return;
            } else if("LG".equals(errorCode)) {
                logger.error("该银行卡未开通银联在线支付业务");
                responseData.setCode("LG");
                responseData.setMsg("该银行卡未开通银联在线支付业务");
                return;
            } else if("G0".equals(errorCode)) {
                logger.error("超出单笔金额上限");
                responseData.setCode("G0");
                responseData.setMsg("超出单笔金额上限");
                return;
            } else if("G7".equals(errorCode)) {
                logger.error("超出系统当月金额限制");
                responseData.setCode("G7");
                responseData.setMsg("超出系统当月金额限制");
                return;
            } else if("HT".equals(errorCode)) {
                logger.error("证件号异常或超过有效期");
                responseData.setCode("HT");
                responseData.setMsg("证件号异常或超过有效期");
                return;
            }
                
        }
    }
}
