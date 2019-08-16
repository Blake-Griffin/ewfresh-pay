package com.ewfresh.pay.util.unionpayb2cwebwap;

import com.ewfresh.pay.util.ResponseData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 根据code打印相关信息
 * @author: JiuDongDong
 * date: 2019/5/8 17:21
 */
public final class UnionPayLogUtil {
    private static Logger logger = LoggerFactory.getLogger(UnionPayLogUtil.class);

    /**
     * Description: 打印银联处理订单支付交易结果
     * @author: JiuDongDong
     * @param responseData 封装返回信息
     * @param respCode 银联返回的状态码
     * @param respMsg 银联返回的响应信息
     * @param externalRefNumber  订单号
     * date: 2019/5/8 17:31
     */
    public static void logTradeInfo(ResponseData responseData, String respCode,
                                    String respMsg, String externalRefNumber) {
        if (null == responseData) {
            responseData = new ResponseData();
        }
        // 打印b2cWebWap成功信息 start
        if ("00".equals(respCode)) {
            logger.info("The respCode = {}, respMsg = {}", respCode, respMsg);
            logger.info(externalRefNumber + "：交易成功");
            return;
        }
        if ("A6".equals(respCode)) {
            logger.warn("The respCode = " + respCode + ", respMsg = " + respMsg);
            logger.warn(externalRefNumber + "：有缺陷的成功");
            return;
        }
        // 打印b2cWebWap成功信息 end

        // 打印qrCode成功信息 start
        if ("SUCCESS".equals(respCode)) {
            logger.info("The errCode = {}, errMsg = {}, orderId = {}", respCode, respMsg, externalRefNumber);
            logger.info(externalRefNumber + "：交易成功");
            return;
        }
        // 打印qrCode成功信息 end

        // 打印错误信息
        logger.error("The respCode = " + respCode + ", respMsg = " + respMsg);
        if (StringUtils.isBlank(respCode)) {
            logger.error("The respCode is null");
            return;
        }
        switch (respCode) {
            // b2cWebWap start
            case "01":
                logger.error(externalRefNumber + "：交易失败。详情请咨询95516");
                responseData.setCode("01");
                responseData.setMsg("交易失败。详情请咨询95516");
                break;
            case "02":
                logger.error(externalRefNumber + "：系统未开放或暂时关闭，请稍后再试");
                responseData.setCode("02");
                responseData.setMsg("系统未开放或暂时关闭，请稍后再试");
                break;
            case "03":
                logger.error(externalRefNumber + "：交易通讯超时，请发起查询交易");
                responseData.setCode("03");
                responseData.setMsg("交易通讯超时，请发起查询交易");
                break;
            case "05":
                logger.error(externalRefNumber + "：交易已受理，请稍后查询交易结果");
                responseData.setCode("05");
                responseData.setMsg("交易已受理，请稍后查询交易结果");
                break;
            case "06":
                logger.error(externalRefNumber + "：系统繁忙，请稍后再试");
                responseData.setCode("06");
                responseData.setMsg("系统繁忙，请稍后再试");
                break;
            case "10":
                logger.error(externalRefNumber + "：报文格式错误");
                responseData.setCode("10");
                responseData.setMsg("报文格式错误");
                break;
            case "11":
                logger.error(externalRefNumber + "：验证签名失败");
                responseData.setCode("11");
                responseData.setMsg("验证签名失败");
                break;
            case "12":
                logger.error(externalRefNumber + "：重复交易");
                responseData.setCode("12");
                responseData.setMsg("重复交易");
                break;
            case "13":
                logger.error(externalRefNumber + "：报文交易要素缺失");
                responseData.setCode("13");
                responseData.setMsg("报文交易要素缺失");
                break;
            case "14":
                logger.error(externalRefNumber + "：批量文件格式错误");
                responseData.setCode("14");
                responseData.setMsg("批量文件格式错误");
                break;
            case "30":
                logger.error(externalRefNumber + "：交易未通过，请尝试使用其他银联卡支付或联系95516");
                responseData.setCode("30");
                responseData.setMsg("交易未通过，请尝试使用其他银联卡支付或联系95516");
                break;
            case "31":
                logger.error(externalRefNumber + "：商户状态不正确");
                responseData.setCode("31");
                responseData.setMsg("商户状态不正确");
                break;
            case "32":
                logger.error(externalRefNumber + "：无此交易权限");
                responseData.setCode("32");
                responseData.setMsg("无此交易权限");
                break;
            case "33":
                logger.error(externalRefNumber + "：交易金额超限");
                responseData.setCode("33");
                responseData.setMsg("交易金额超限");
                break;
            case "34":
                logger.error(externalRefNumber + "：查无此交易");
                responseData.setCode("34");
                responseData.setMsg("查无此交易");
                break;
            case "35":
                logger.error(externalRefNumber + "：原交易不存在或状态不正确");
                responseData.setCode("35");
                responseData.setMsg("原交易不存在或状态不正确");
                break;
            case "36":
                logger.error(externalRefNumber + "：与原交易信息不符");
                responseData.setCode("36");
                responseData.setMsg("与原交易信息不符");
                break;
            case "37":
                logger.error(externalRefNumber + "：已超过最大查询次数或操作过于频繁");
                responseData.setCode("37");
                responseData.setMsg("已超过最大查询次数或操作过于频繁");
                break;
            case "38":
                logger.error(externalRefNumber + "：银联风险受限");
                responseData.setCode("38");
                responseData.setMsg("银联风险受限");
                break;
            case "39":
                logger.error(externalRefNumber + "：交易不在受理时间范围内");
                responseData.setCode("39");
                responseData.setMsg("交易不在受理时间范围内");
                break;
            case "40":
                logger.error(externalRefNumber + "：绑定关系检查失败");
                responseData.setCode("40");
                responseData.setMsg("绑定关系检查失败");
                break;
            case "41":
                logger.error(externalRefNumber + "：批量状态不正确，无法下载");
                responseData.setCode("41");
                responseData.setMsg("批量状态不正确，无法下载");
                break;
            case "42":
                logger.error(externalRefNumber + "：扣款成功但交易超过规定支付时间");
                responseData.setCode("42");
                responseData.setMsg("扣款成功但交易超过规定支付时间");
                break;
            case "43":
                logger.error(externalRefNumber + "：无此业务权限，详情请咨询95516");
                responseData.setCode("43");
                responseData.setMsg("无此业务权限，详情请咨询95516");
                break;
            case "44":
                logger.error(externalRefNumber + "：输入号码错误或暂未开通此项业务，详情请咨询95516");
                responseData.setCode("44");
                responseData.setMsg("输入号码错误或暂未开通此项业务，详情请咨询95516");
                break;
            case "45":
                logger.error(externalRefNumber + "：原交易已被成功退货或已被成功撤销");
                responseData.setCode("45");
                responseData.setMsg("原交易已被成功退货或已被成功撤销");
                break;
            case "46":
                logger.error(externalRefNumber + "：交易已被成功冲正");
                responseData.setCode("46");
                responseData.setMsg("交易已被成功冲正");
                break;
            case "60":
                logger.error(externalRefNumber + "：交易失败，详情请咨询您的发卡行");
                responseData.setCode("60");
                responseData.setMsg("交易失败，详情请咨询您的发卡行");
                break;
            case "61":
                logger.error(externalRefNumber + "：输入的卡号无效，请确认后输入");
                responseData.setCode("61");
                responseData.setMsg("输入的卡号无效，请确认后输入");
                break;
            case "62":
                logger.error(externalRefNumber + "：交易失败，发卡银行不支持该商户，请更换其他银行卡");
                responseData.setCode("62");
                responseData.setMsg("交易失败，发卡银行不支持该商户，请更换其他银行卡");
                break;
            case "63":
                logger.error(externalRefNumber + "：卡状态不正确");
                responseData.setCode("63");
                responseData.setMsg("卡状态不正确");
                break;
            case "64":
                logger.error(externalRefNumber + "：卡上的余额不足");
                responseData.setCode("64");
                responseData.setMsg("卡上的余额不足");
                break;
            case "65":
                logger.error(externalRefNumber + "：输入的密码、有效期或CVN2有误，交易失败");
                responseData.setCode("65");
                responseData.setMsg("输入的密码、有效期或CVN2有误，交易失败");
                break;
            case "66":
                logger.error(externalRefNumber + "：持卡人身份信息或手机号输入不正确，验证失败");
                responseData.setCode("66");
                responseData.setMsg("持卡人身份信息或手机号输入不正确，验证失败");
                break;
            case "67":
                logger.error(externalRefNumber + "：密码输入次数超限");
                responseData.setCode("67");
                responseData.setMsg("密码输入次数超限");
                break;
            case "68":
                logger.error(externalRefNumber + "：您的银行卡暂不支持该业务，请向您的银行或95516咨询");
                responseData.setCode("68");
                responseData.setMsg("您的银行卡暂不支持该业务，请向您的银行或95516咨询");
                break;
            case "69":
                logger.error(externalRefNumber + "：您的输入超时，交易失败");
                responseData.setCode("69");
                responseData.setMsg("您的输入超时，交易失败");
                break;
            case "70":
                logger.error(externalRefNumber + "：交易已跳转，等待持卡人输入");
                responseData.setCode("70");
                responseData.setMsg("交易已跳转，等待持卡人输入");
                break;
            case "71":
                logger.error(externalRefNumber + "：动态口令或短信验证码校验失败");
                responseData.setCode("71");
                responseData.setMsg("动态口令或短信验证码校验失败");
                break;
            case "72":
                logger.error(externalRefNumber + "：您尚未在银行网点柜面或个人网银签约加办银联无卡支付业务，请去柜面开通");
                responseData.setCode("72");
                responseData.setMsg("您尚未在银行网点柜面或个人网银签约加办银联无卡支付业务，请去柜面开通");
                break;
            case "73":
                logger.error(externalRefNumber + "：支付卡已超过有效期");
                responseData.setCode("73");
                responseData.setMsg("支付卡已超过有效期");
                break;
            case "74":
                logger.error(externalRefNumber + "：扣款成功，销账未知");
                responseData.setCode("74");
                responseData.setMsg("扣款成功，销账未知");
                break;
            case "75":
                logger.error(externalRefNumber + "：扣款成功，销账失败");
                responseData.setCode("75");
                responseData.setMsg("扣款成功，销账失败");
                break;
            case "76":
                logger.error(externalRefNumber + "：需要验密开通");
                responseData.setCode("76");
                responseData.setMsg("需要验密开通");
                break;
            case "77":
                logger.error(externalRefNumber + "：银行卡未开通认证支付");
                responseData.setCode("77");
                responseData.setMsg("银行卡未开通认证支付");
                break;
            case "78":
                logger.error(externalRefNumber + "：发卡行交易权限受限，详情请咨询您的发卡行");
                responseData.setCode("78");
                responseData.setMsg("发卡行交易权限受限，详情请咨询您的发卡行");
                break;
            case "79":
                logger.error(externalRefNumber + "：此卡可用，但发卡行暂不支持短信验证");
                responseData.setCode("79");
                responseData.setMsg("此卡可用，但发卡行暂不支持短信验证");
                break;
            case "80":
                logger.error(externalRefNumber + "：交易失败，Token 已过期");
                responseData.setCode("80");
                responseData.setMsg("交易失败，Token 已过期");
                break;
            case "81":
                logger.error(externalRefNumber + "：月累计交易笔数(金额)超限");
                responseData.setCode("81");
                responseData.setMsg("月累计交易笔数(金额)超限");
                break;
            case "82":
                logger.error(externalRefNumber + "：需要校验密码");
                responseData.setCode("82");
                responseData.setMsg("需要校验密码");
                break;
            case "83":
                logger.error(externalRefNumber + "：发卡行（渠道）处理中");
                responseData.setCode("83");
                responseData.setMsg("发卡行（渠道）处理中");
                break;
            case "85":
                logger.error(externalRefNumber + "：交易失败，营销规则不满足");
                responseData.setCode("85");
                responseData.setMsg("交易失败，营销规则不满足");
                break;
            case "86":
                logger.error(externalRefNumber + "：二维码状态错误");
                responseData.setCode("86");
                responseData.setMsg("二维码状态错误");
                break;
            case "87":
                logger.error(externalRefNumber + "：支付次数超限");
                responseData.setCode("87");
                responseData.setMsg("支付次数超限");
                break;
            case "88":
                logger.error(externalRefNumber + "：无此二维码");
                responseData.setCode("88");
                responseData.setMsg("无此二维码");
                break;
            case "89":
                logger.error(externalRefNumber + "：无此Token、TR状态无效或者Token状态无效");
                responseData.setCode("89");
                responseData.setMsg("无此Token、TR状态无效或者Token状态无效");
                break;
            case "90":
                logger.error(externalRefNumber + "：账户余额不足");
                responseData.setCode("90");
                responseData.setMsg("账户余额不足");
                break;
            case "91":
                logger.error(externalRefNumber + "：认证失败");
                responseData.setCode("91");
                responseData.setMsg("认证失败");
                break;
            case "92":
                logger.error(externalRefNumber + "：营业执照过期");
                responseData.setCode("92");
                responseData.setMsg("营业执照过期");
                break;
            case "93":
                logger.error(externalRefNumber + "：营业执照吊销");
                responseData.setCode("93");
                responseData.setMsg("营业执照吊销");
                break;
            case "94":
                logger.error(externalRefNumber + "：营业执照注销");
                responseData.setCode("94");
                responseData.setMsg("营业执照注销");
                break;
            case "95":
                logger.error(externalRefNumber + "：营业执照迁出");
                responseData.setCode("95");
                responseData.setMsg("营业执照迁出");
                break;
            case "96":
                logger.error(externalRefNumber + "：营业执照撤销");
                responseData.setCode("96");
                responseData.setMsg("营业执照撤销");
                break;
            case "98":
                logger.error(externalRefNumber + "：文件不存在");
                responseData.setCode("98");
                responseData.setMsg("文件不存在");
                break;
            case "99":
                logger.error(externalRefNumber + "：通用错误");
                responseData.setCode("99");
                responseData.setMsg("通用错误");
                break;
            // b2cWebWap end


            // qrCode start
            case "INTERNAL_ERROR":
                logger.error(externalRefNumber + "：中国银联系统错误，请联系技术支持。");
                responseData.setCode("INTERNAL_ERROR");
                responseData.setMsg("中国银联系统错误，请联系技术支持。");
                break;
            case "BAD_REQUEST":
                logger.error(externalRefNumber + "：报文格式或字段值有误");
                responseData.setCode("BAD_REQUEST");
                responseData.setMsg("报文格式或字段值有误");
                break;
            case "NO_SERVICE":
                logger.error(externalRefNumber + "：msgType错误，请检查文档，msgType是否拼写正确。");
                responseData.setCode("NO_SERVICE");
                responseData.setMsg("msgType错误，请检查文档，msgType是否拼写正确。");
                break;
            case "TIMEOUT":
                logger.error(externalRefNumber + "：中国银联处理超时，建议重试");
                responseData.setCode("TIMEOUT");
                responseData.setMsg("中国银联处理超时，建议重试");
                break;
            case "NO_ORDER":
                logger.error(externalRefNumber + "：找不到请求的原始订单");
                responseData.setCode("NO_ORDER");
                responseData.setMsg("找不到请求的原始订单");
                break;
            case "OPERATION_NOT_ALLOWED":
                logger.error(externalRefNumber + "：订单已经关闭，不能执行退货等操作。");
                responseData.setCode("OPERATION_NOT_ALLOWED");
                responseData.setMsg("订单已经关闭，不能执行退货等操作。");
                break;
            case "TARGET_FAIL":
                logger.error(externalRefNumber + "：支付宝或者微信方处理业务失败。");
                responseData.setCode("TARGET_FAIL");
                responseData.setMsg("支付宝或者微信方处理业务失败。");
                break;
            case "DUP_ORDER":
                logger.error(externalRefNumber + "：重复的订单请求。");
                responseData.setCode("DUP_ORDER");
                responseData.setMsg("重复的订单请求。");
                break;
            case "NET_ERROR":
                logger.error(externalRefNumber + "：网络通讯异常请重试。");
                responseData.setCode("NET_ERROR");
                responseData.setMsg("网络通讯异常请重试。");
                break;
            case "NO_MERCHANT":
                logger.error(externalRefNumber + "：找不到请求指定的商户。");
                responseData.setCode("NO_MERCHANT");
                responseData.setMsg("找不到请求指定的商户。");
                break;
            case "ORDER_PROCESSING":
                logger.error(externalRefNumber + "：订单处于锁定状态，请等待一分钟后再试。");
                responseData.setCode("ORDER_PROCESSING");
                responseData.setMsg("订单处于锁定状态，请等待一分钟后再试。");
                break;
            case "INACTIVE_MERCHANT":
                logger.error(externalRefNumber + "：交易商户在网付前置被冻结。");
                responseData.setCode("INACTIVE_MERCHANT");
                responseData.setMsg("交易商户在网付前置被冻结。");
                break;
            case "ABNORMAL_REQUEST_TIME":
                logger.error(externalRefNumber + "：请求终端或者平台的系统时间不正常，请检查系统时间。");
                responseData.setCode("ABNORMAL_REQUEST_TIME");
                responseData.setMsg("请求终端或者平台的系统时间不正常，请检查系统时间。");
                break;
            case "TXN_DISCARDED":
                logger.error(externalRefNumber + "：系统负载过大，交易被丢弃，请联系中国银联。");
                responseData.setCode("TXN_DISCARDED");
                responseData.setMsg("系统负载过大，交易被丢弃，请联系中国银联。");
                break;
            case "BAD_SIGN":
                logger.error(externalRefNumber + "：签名错误。");
                responseData.setCode("BAD_SIGN");
                responseData.setMsg("签名错误。");
                break;
            case "INVALID_MSGSRC":
                logger.error(externalRefNumber + "：商户来源错误。");
                responseData.setCode("INVALID_MSGSRC");
                responseData.setMsg("商户来源错误。");
                break;
            case "INVALID_ORDER":
                logger.error(externalRefNumber + "：订单信息异常。");
                responseData.setCode("INVALID_ORDER");
                responseData.setMsg("订单信息异常。");
                break;
            case "NO_CROSS_DAY_TRADING":
                logger.error(externalRefNumber + "：该渠道不支持跨日撤销，建议做退货。");
                responseData.setCode("NO_CROSS_DAY_TRADING");
                responseData.setMsg("该渠道不支持跨日撤销，建议做退货。");
                break;
            case "DENIED_IP":
                logger.error(externalRefNumber + "：IP不在白名单中，不允许此IP交易。");
                responseData.setCode("DENIED_IP");
                responseData.setMsg("IP不在白名单中，不允许此IP交易。");
                break;
            case "INVLID_MERCHANT_CONFIG":
                logger.error(externalRefNumber + "：错误的商户配置。");
                responseData.setCode("INVLID_MERCHANT_CONFIG");
                responseData.setMsg("错误的商户配置。");
                break;
            case "INVALID_RESPONSE":
                logger.error(externalRefNumber + "：无效的应答报文。");
                responseData.setCode("INVALID_RESPONSE");
                responseData.setMsg("无效的应答报文。");
                break;
            case "NO_BILL":
                logger.error(externalRefNumber + "：无法找到指定的账单。");
                responseData.setCode("NO_BILL");
                responseData.setMsg("无法找到指定的账单。");
                break;
            case "SYSTEM_BUSY":
                logger.error(externalRefNumber + "：系统繁忙，请稍候再试");
                responseData.setCode("SYSTEM_BUSY");
                responseData.setMsg("系统繁忙，请稍候再试");
                break;
            // qrCode end
        }
    }
}
