package com.ewfresh.pay.util.boc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 *      根据错误码打印错误信息，并返回错误描述
 * @author: JiuDongDong
 * date: 2018/4/25.
 */
public final class BOCLoggerByErrorCode {
    private static Logger logger = LoggerFactory.getLogger(BOCLoggerByErrorCode.class);

    /**
     * Description: 根据BOC返回的错误状态码记录错误信息，并返回错误描述
     * @author: JiuDongDong
     * @param errorCode  错误码
     * @return java.lang.String 错误描述
     * date: 2018/4/25 15:24
     */
    public static String bOCLoggerByErrorCode(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            logger.warn("The errorCode for bOCLoggerByErrorCode is empty");
            return null;
        }
        switch (errorCode) {
            case "E00000001" :
                logger.error("ErrorCode: E00000001, desp: 退货错误，总退货金额超过当日收入");
                return "退货错误，总退货金额超过当日收入";
            case "E00000002" :
                logger.error("ErrorCode: E00000002, desp: FTP的源文件为空");
                return "FTP的源文件为空";
            case "E00000003" :
                logger.error("ErrorCode: E00000003, desp: FTP时发生异常");
                return "FTP时发生异常";
            case "E00000004" :
                logger.error("ErrorCode: E00000004, desp: 没有生成退货交易");
                return "没有生成退货交易";
            case "E00000005" :
                logger.error("ErrorCode: E00000005, desp: 未找到原交易");
                return "未找到原交易";
            case "E00000006" :
                logger.error("ErrorCode: E00000006, desp: 退货日期超期");
                return "退货日期超期";
            case "E00000007" :
                logger.error("ErrorCode: E00000007, desp: 非人民币交易");
                return "非人民币交易";
            case "E00000008" :
                logger.error("ErrorCode: E00000008, desp: 支付卡号为空");
                return "支付卡号为空";
            case "E00000009" :
                logger.error("ErrorCode: E00000009, desp: 网关不存在该笔订单");
                return "网关不存在该笔订单";
            case "E00000010" :
                logger.error("ErrorCode: E00000010, desp: 订单状态异常");
                return "订单状态异常";
            case "E00000011" :
                logger.error("ErrorCode: E00000011, desp: 网络忙，请稍后再试");
                return "网络忙，请稍后再试";
            case "E00000012" :
                logger.error("ErrorCode: E00000012, desp: 退商户交易限额失败");
                return "退商户交易限额失败";
            case "E00000013" :
                logger.error("ErrorCode: E00000013, desp: 支付失败");
                return "支付失败";
            case "E00000014" :
                logger.error("ErrorCode: E00000014, desp: 退货信息不符合原交易");
                return "退货信息不符合原交易";
            case "E00000015" :
                logger.error("ErrorCode: E00000015, desp: 退货金额大于订单金额");
                return "退货金额大于订单金额";
            case "E00000016" :
                logger.error("ErrorCode: E00000016, desp: 该商户消费限额为空");
                return "该商户消费限额为空";
            case "E00000017" :
                logger.error("ErrorCode: E00000017, desp: 订单状态不可退货");
                return "订单状态不可退货";
            case "E00000018" :
                logger.error("ErrorCode: E00000018, desp: 交易金额超过该商户对所选卡类型的单笔限额设置，或该商户不支持所选卡类型进行支付。请选择其它类型的中行卡重新支付订单！");
                return "交易金额超过该商户对所选卡类型的单笔限额设置，或该商户不支持所选卡类型进行支付。请选择其它类型的中行卡重新支付订单！";
            case "E00000019" :
                logger.error("ErrorCode: E00000019, desp: BOCNET上送数据非法");
                return "BOCNET上送数据非法";
            case "E00000020" :
                logger.error("ErrorCode: E00000020, desp: 支付时间超时");
                return "支付时间超时";
            case "E00000021" :
                logger.error("ErrorCode: E00000021, desp: 退货失败");
                return "退货失败";
            case "E00000022" :
                logger.error("ErrorCode: E00000022, desp: 查退款金额超过原订单可退款金额");
                return "查退款金额超过原订单可退款金额";
            case "E00000023" :
                logger.error("ErrorCode: E00000023, desp: 商户状态非‘开通’");
                return "商户状态非‘开通’";
            case "E00000024" :
                logger.error("ErrorCode: E00000024, desp: 商户交易流水号重复");
                return "商户交易流水号重复";
            case "E00000030" :
                logger.error("ErrorCode: E00000030, desp: 系统故障");
                return "系统故障";
            case "E00000051" :
                logger.error("ErrorCode: E00000051, desp: 卡号不是理财直付服务卡号");
                return "卡号不是理财直付服务卡号";
            case "E00000052" :
                logger.error("ErrorCode: E00000052, desp: 证件类型，证件号与登录信息不符");
                return "证件类型，证件号与登录信息不符";
            case "E00000061" :
                logger.error("ErrorCode: E00000061, desp: 系统内不存在此商户记录");
                return "系统内不存在此商户记录";
            case "E00000062" :
                logger.error("ErrorCode: E00000062, desp: 商户支付类型(payType)不支持本次操作");
                return "商户支付类型(payType)不支持本次操作";
            case "E00000063" :
                logger.error("ErrorCode: E00000063, desp: 商户状态不支持本次操作");
                return "商户状态不支持本次操作";
            case "E00000064" :
                logger.error("ErrorCode: E00000064, desp: 商户支付大类服务状态不支持本次操作");
                return "商户支付大类服务状态不支持本次操作";
            case "E00000065" :
                logger.error("ErrorCode: E00000065, desp: 商户渠道类型不支持本次操作");
                return "商户渠道类型不支持本次操作";
            case "E00000066" :
                logger.error("ErrorCode: E00000066, desp: 系统内已存在相同订单号订单信息，本次操作与上次订单金额不符");
                return "系统内已存在相同订单号订单信息，本次操作与上次订单金额不符";
            case "E00000067" :
                logger.error("ErrorCode: E00000067, desp: 系统内已存在相同订单号订单信息，且其支付状态不允许再次支付");
                return "系统内已存在相同订单号订单信息，且其支付状态不允许再次支付";
            case "E00000068" :
                logger.error("ErrorCode: E00000068, desp: 系统内已存在此认证信息，且其认证状态不允许再次认证");
                return "系统内已存在此认证信息，且其认证状态不允许再次认证";
            case "E00000069" :
                logger.error("ErrorCode: E00000069, desp: 验证签名失败");
                return "验证签名失败";
            case "E00000070" :
                logger.error("ErrorCode: E00000070, desp: 签名失败");
                return "签名失败";
            case "E00000071" :
                logger.error("ErrorCode: E00000071, desp: 金额域不符合要求格式");
                return "金额域不符合要求格式";
            case "E00000073" :
                logger.error("ErrorCode: E00000073, desp: 时间域不符合要求格式");
                return "时间域不符合要求格式";
            case "E00000074" :
                logger.error("ErrorCode: E00000074, desp: 查询结果为空");
                return "查询结果为空";
            case "E00000075" :
                logger.error("ErrorCode: E00000075, desp: 商户绑定类型不支持本次操作");
                return "商户绑定类型不支持本次操作";
            case "E00000077" :
                logger.error("ErrorCode: E00000077, desp: 系统内不存在此协议记录");
                return "系统内不存在此协议记录";
            case "E00000078" :
                logger.error("ErrorCode: E00000078, desp: 签约失败！您所输入的商户端账户在商户系统无法验证，请稍候重试！");
                return "签约失败！您所输入的商户端账户在商户系统无法验证，请稍候重试！";
            case "E00000079" :
                logger.error("ErrorCode: E00000079, desp: 系统内协议状态不支持本次操作");
                return "系统内协议状态不支持本次操作";
            case "E00000081" :
                logger.error("ErrorCode: E00000081, desp: 您所提交的交易无法完成：交易金额超过我行设置的中银快付笔限额 ，请检查");
                return "您所提交的交易无法完成：交易金额超过我行设置的中银快付笔限额 ，请检查";
            case "E00000082" :
                logger.error("ErrorCode: E00000082, desp: 您所提交的交易无法完成：交易金额超过我行设置的中银快付日累计限额 ，请检查");
                return "您所提交的交易无法完成：交易金额超过我行设置的中银快付日累计限额 ，请检查";
            case "E00000083" :
                logger.error("ErrorCode: E00000083, desp: 有用户使用该银行卡正在其他页面进行中银快付交易，请确认！");
                return "有用户使用该银行卡正在其他页面进行中银快付交易，请确认！";
            case "E00000084" :
                logger.error("ErrorCode: E00000084, desp: 批量退货，对于跨行收单订单，不允许此操作");
                return "批量退货，对于跨行收单订单，不允许此操作";
            case "E00000085" :
                logger.error("ErrorCode: E00000085, desp: 没有相应签约信息");
                return "没有相应签约信息";
            case "E00000086" :
                logger.error("ErrorCode: E00000086, desp: 商户的支付大类不支持本次操作");
                return "商户的支付大类不支持本次操作";
            case "E00000087" :
                logger.error("ErrorCode: E00000087, desp: 中银快付签约途径不能为空");
                return "中银快付签约途径不能为空";
            case "E00000088" :
                logger.error("ErrorCode: E00000088, desp: 商户无权限进行本次操作");
                return "商户无权限进行本次操作";
            case "E00000089" :
                logger.error("ErrorCode: E00000089, desp: 商户支持的协议操作渠道不允许该操作");
                return "商户支持的协议操作渠道不允许该操作";
            case "E00000090" :
                logger.error("ErrorCode: E00000090, desp: 商户不支持该卡类型");
                return "商户不支持该卡类型";
            case "E00000091" :
                logger.error("ErrorCode: E00000091, desp: 系统内已存在此协议信息，且协议状态不允许再次签约");
                return "系统内已存在此协议信息，且协议状态不允许再次签约";
            case "E00000092" :
                logger.error("ErrorCode: E00000092, desp: 查询数量超过系统支持的最大值");
                return "查询数量超过系统支持的最大值";
            case "E00000093" :
                logger.error("ErrorCode: E00000093, desp: 您所提交的交易无法完成：交易金额超过该商户所设单笔限额，请检查");
                return "您所提交的交易无法完成：交易金额超过该商户所设单笔限额，请检查";
            case "E00000094" :
                logger.error("ErrorCode: E00000094, desp: 您所提交的交易无法完成：交易金额超过我行设置的协议笔限额 ，请检查");
                return "您所提交的交易无法完成：交易金额超过我行设置的协议笔限额 ，请检查";
            case "E00000095" :
                logger.error("ErrorCode: E00000095, desp: 您所提交的交易无法完成：交易金额超过您所设置的协议日限额，请检查");
                return "您所提交的交易无法完成：交易金额超过您所设置的协议日限额，请检查";
            case "E00000096" :
                logger.error("ErrorCode: E00000096, desp: 您所提交的交易无法完成：交易金额超过商户所设每日交易限额，请检查");
                return "您所提交的交易无法完成：交易金额超过商户所设每日交易限额，请检查";
            case "E00000097" :
                logger.error("ErrorCode: E00000097, desp: 签约失败！您所输入的商户端账户有误，请检查！");
                return "签约失败！您所输入的商户端账户有误，请检查！";
            case "E00000098" :
                logger.error("ErrorCode: E00000098, desp: 签约失败！您在商户网站的证件信息与您注册网银的证件信息不符，请检查！");
                return "签约失败！您在商户网站的证件信息与您注册网银的证件信息不符，请检查！";
            case "E00000099" :
                logger.error("ErrorCode: E00000099, desp: 您的交易提交过于频繁，请稍后重试");
                return "您的交易提交过于频繁，请稍后重试";
            case "E00000100" :
                logger.error("ErrorCode: E00000100, desp: 文件类型错误，请检查！");
                return "文件类型错误，请检查！";
            case "E00000101" :
                logger.error("ErrorCode: E00000101, desp: 文件类型与操作类型不符，请检查！");
                return "文件类型与操作类型不符，请检查！";
            case "E00000102" :
                logger.error("ErrorCode: E00000102, desp: 您下载的文件不存在，请稍后重试！");
                return "您下载的文件不存在，请稍后重试！";
            case "E00000103" :
                logger.error("ErrorCode: E00000103, desp: 您上传的文件为空，请检查！");
                return "您上传的文件为空，请检查！";
            case "E00000104" :
                logger.error("ErrorCode: E00000104, desp: 您上传的文件名为空，请检查！");
                return "您上传的文件名为空，请检查！";
            case "E00000105" :
                logger.error("ErrorCode: E00000105, desp: 您上传的文件格式非txt，请检查！");
                return "您上传的文件格式非txt，请检查！";
            case "E00000106" :
                logger.error("ErrorCode: E00000106, desp: 您上传的文件大小所超过允许的最大长度，请检查！");
                return "您上传的文件大小所超过允许的最大长度，请检查！";
            case "E00000107" :
                logger.error("ErrorCode: E00000107, desp: 您上传的文件非法(文件不存在或者格式有误)，请检查！");
                return "您上传的文件非法(文件不存在或者格式有误)，请检查！";
            case "E00000108" :
                logger.error("ErrorCode: E00000108, desp: 您上传的文件内容为空，请检查！");
                return "您上传的文件内容为空，请检查！";
            case "E00000109" :
                logger.error("ErrorCode: E00000109, desp: 您上传的文件笔数超过最大笔数，请检查！");
                return "您上传的文件笔数超过最大笔数，请检查！";
            case "E00000110" :
                logger.error("ErrorCode: E00000110, desp: 您上传的文件商户号不匹配，请检查！");
                return "您上传的文件商户号不匹配，请检查！";
            case "E00000111" :
                logger.error("ErrorCode: E00000111, desp: 您上传的文件币种不匹配，请检查！");
                return "您上传的文件币种不匹配，请检查！";
            case "E00000112" :
                logger.error("ErrorCode: E00000112, desp: 验票失败！");
                return "验票失败！";
            case "E00000113" :
                logger.error("ErrorCode: E00000113, desp: 文件上传失败！");
                return "文件上传失败！";
            case "E00000114" :
                logger.error("ErrorCode: E00000114, desp: 文件日期不能晚于当前日期！");
                return "文件日期不能晚于当前日期！";
            case "E00000115" :
                logger.error("ErrorCode: E00000115, desp: 文件日期不能早于当前日期30天！");
                return "文件日期不能早于当前日期30天！";
            case "E00000116" :
                logger.error("ErrorCode: E00000116, desp: 商户类型不支持该文件类型！");
                return "商户类型不支持该文件类型！";
            case "E00000117" :
                logger.error("ErrorCode: E00000117, desp: 签约失败！您在商户网站的手机号与您在我行柜台留存的手机号不符，请检查！");
                return "签约失败！您在商户网站的手机号与您在我行柜台留存的手机号不符，请检查！";
            case "E00000118" :
                logger.error("ErrorCode: E00000118, desp: 交易标识不符，请检查！");
                return "交易标识不符，请检查！";
            case "E00000119" :
                logger.error("ErrorCode: E00000119, desp: 同笔订单正在处理中，请稍后重试！");
                return "同笔订单正在处理中，请稍后重试！";
            case "E00000120" :
                logger.error("ErrorCode: E00000120, desp: 您提交的数据中分期信息有误，请检查！");
                return "您提交的数据中分期信息有误，请检查！";
            case "E00000121" :
                logger.error("ErrorCode: E00000121, desp: 您提交的数据含有分期信息，请使用分期支付接口进行交易！");
                return "您提交的数据含有分期信息，请使用分期支付接口进行交易！";
            case "E00000122" :
                logger.error("ErrorCode: E00000122, desp: 您使用的商户状态有误，请核实后再进行交易");
                return "您使用的商户状态有误，请核实后再进行交易";
            case "E00000123" :
                logger.error("ErrorCode: E00000123, desp: 您使用的商户不支持分行特色业务，请检查！");
                return "您使用的商户不支持分行特色业务，请检查！";
            case "E00000124" :
                logger.error("ErrorCode: E00000124, desp: 签约失败！您在商户网站的卡号与您在我行柜台留存的卡号不符，请检查！");
                return "签约失败！您在商户网站的卡号与您在我行柜台留存的卡号不符，请检查！";
            case "E00000125" :
                logger.error("ErrorCode: E00000125, desp: 签约失败！您在商户网站的证件类型与您在我行柜台留存的证件类型不符，请检查！");
                return "签约失败！您在商户网站的证件类型与您在我行柜台留存的证件类型不符，请检查！";
            case "E00000126" :
                logger.error("ErrorCode: E00000126, desp: 签约失败！您在商户网站的证件号码与您在我行柜台留存的证件号码不符，请检查！");
                return "签约失败！您在商户网站的证件号码与您在我行柜台留存的证件号码不符，请检查！";
            case "E00000127" :
                logger.error("ErrorCode: E00000127, desp: 签约失败！您在商户网站的姓名与您在我行柜台留存的姓名不符，请检查！");
                return "签约失败！您在商户网站的姓名与您在我行柜台留存的姓名不符，请检查！";
            case "E00000999" :
                logger.error("ErrorCode: E00000999, desp: 系统故障");
                return "系统故障";
            case "E00001001" :
                logger.error("ErrorCode: E00001001, desp: merchantNo域不能为空");
                return "merchantNo域不能为空";
            case "E00001002" :
                logger.error("ErrorCode: E00001002, desp: merchantNo域超长");
                return "merchantNo域超长";
            case "E00001003" :
                logger.error("ErrorCode: E00001003, desp: merchantNo域不符合系统要求格式");
                return "merchantNo域不符合系统要求格式";
            case "E00001004" :
                logger.error("ErrorCode: E00001004, desp: 您没有选择证件类型，请修改！");
                return "您没有选择证件类型，请修改！";
            case "E00001005" :
                logger.error("ErrorCode: E00001005, desp: identityType域超长");
                return "identityType域超长";
            case "E00001006" :
                logger.error("ErrorCode: E00001006, desp: 我行暂不支持该证件类型，请修改！");
                return "我行暂不支持该证件类型，请修改！";
            case "E00001007" :
                logger.error("ErrorCode: E00001007, desp: 您没有输入证件号码，请检查！");
                return "您没有输入证件号码，请检查！";
            case "E00001008" :
                logger.error("ErrorCode: E00001008, desp: 您输入的证件号码长度超出限制，请检查！");
                return "您输入的证件号码长度超出限制，请检查！";
            case "E00001009" :
                logger.error("ErrorCode: E00001009, desp: 您输入的证件号码长度或格式不正确，请检查！");
                return "您输入的证件号码长度或格式不正确，请检查！";
            case "E00001010" :
                logger.error("ErrorCode: E00001010, desp: 您没有输入银行卡号，请检查！");
                return "您没有输入银行卡号，请检查！";
            case "E00001011" :
                logger.error("ErrorCode: E00001011, desp: 您所输入的银行卡号大于19位，请检查！");
                return "您所输入的银行卡号大于19位，请检查！";
            case "E00001012" :
                logger.error("ErrorCode: E00001012, desp: 您所输入的银行卡号长度或格式不正确，请检查！");
                return "您所输入的银行卡号长度或格式不正确，请检查！";
            case "E00001013" :
                logger.error("ErrorCode: E00001013, desp: lcpMerchantUrl域不能为空");
                return "lcpMerchantUrl域不能为空";
            case "E00001014" :
                logger.error("ErrorCode: E00001014, desp: lcpMerchantUrl域超长");
                return "lcpMerchantUrl域超长";
            case "E00001015" :
                logger.error("ErrorCode: E00001015, desp: lcpMerchantUrl域不符合系统要求格式");
                return "lcpMerchantUrl域不符合系统要求格式";
            case "E00001016" :
                logger.error("ErrorCode: E00001016, desp: verifyTime域不能为空");
                return "verifyTime域不能为空";
            case "E00001017" :
                logger.error("ErrorCode: E00001017, desp: verifyTime域超长");
                return "verifyTime域超长";
            case "E00001018" :
                logger.error("ErrorCode: E00001018, desp: verifyTime域不符合系统要求格式");
                return "verifyTime域不符合系统要求格式";
            case "E00001019" :
                logger.error("ErrorCode: E00001019, desp: signData域不能为空");
                return "signData域不能为空";
            case "E00001020" :
                logger.error("ErrorCode: E00001020, desp: signData域超长");
                return "signData域超长";
            case "E00001021" :
                logger.error("ErrorCode: E00001021, desp: signData域不符合系统要求格式");
                return "signData域不符合系统要求格式";
            case "E00001022" :
                logger.error("ErrorCode: E00001022, desp: referSeq域不能为空");
                return "referSeq域不能为空";
            case "E00001023" :
                logger.error("ErrorCode: E00001023, desp: referSeq域超长");
                return "referSeq域超长";
            case "E00001024" :
                logger.error("ErrorCode: E00001024, desp: referSeq域不符合系统要求格式");
                return "referSeq域不符合系统要求格式";
            case "E00001025" :
                logger.error("ErrorCode: E00001025, desp: orderNo域不能为空");
                return "orderNo域不能为空";
            case "E00001026" :
                logger.error("ErrorCode: E00001026, desp: orderNo域超长");
                return "orderNo域超长";
            case "E00001027" :
                logger.error("ErrorCode: E00001027, desp: orderNo域不符合系统要求格式");
                return "orderNo域不符合系统要求格式";
            case "E00001028" :
                logger.error("ErrorCode: E00001028, desp: curCode域不能为空");
                return "curCode域不能为空";
            case "E00001029" :
                logger.error("ErrorCode: E00001029, desp: curCode域超长");
                return "curCode域超长";
            case "E00001030" :
                logger.error("ErrorCode: E00001030, desp: curCode域不符合系统要求格式");
                return "curCode域不符合系统要求格式";
            case "E00001031" :
                logger.error("ErrorCode: E00001031, desp: orderAmount域不能为空");
                return "orderAmount域不能为空";
            case "E00001032" :
                logger.error("ErrorCode: E00001032, desp: orderAmount域超长");
                return "orderAmount域超长";
            case "E00001033" :
                logger.error("ErrorCode: E00001033, desp: orderAmount域不符合系统要求格式");
                return "orderAmount域不符合系统要求格式";
            case "E00001034" :
                logger.error("ErrorCode: E00001034, desp: orderTime域不能为空");
                return "orderTime域不能为空";
            case "E00001035" :
                logger.error("ErrorCode: E00001035, desp: orderTime域超长");
                return "orderTime域超长";
            case "E00001036" :
                logger.error("ErrorCode: E00001036, desp: orderTime域不符合系统要求格式");
                return "orderTime域不符合系统要求格式";
            case "E00001037" :
                logger.error("ErrorCode: E00001037, desp: orderNote域不能为空");
                return "orderNote域不能为空";
            case "E00001038" :
                logger.error("ErrorCode: E00001038, desp: orderNote域超长");
                return "orderNote域超长";
            case "E00001039" :
                logger.error("ErrorCode: E00001039, desp: orderNote域不符合系统要求格式");
                return "orderNote域不符合系统要求格式";
            case "E00001040" :
                logger.error("ErrorCode: E00001040, desp: orderUrl域不能为空");
                return "orderUrl域不能为空";
            case "E00001041" :
                logger.error("ErrorCode: E00001041, desp: orderUrl域超长");
                return "orderUrl域超长";
            case "E00001042" :
                logger.error("ErrorCode: E00001042, desp: orderUrl域不符合系统要求格式");
                return "orderUrl域不符合系统要求格式";
            case "E00001043" :
                logger.error("ErrorCode: E00001043, desp: orderNos域不能为空");
                return "orderNos域不能为空";
            case "E00001044" :
                logger.error("ErrorCode: E00001044, desp: orderNos域超长");
                return "orderNos域超长";
            case "E00001045" :
                logger.error("ErrorCode: E00001045, desp: orderNos域不符合系统要求格式");
                return "orderNos域不符合系统要求格式";
            case "E00001046" :
                logger.error("ErrorCode: E00001046, desp: holderMerId域不能为空");
                return "holderMerId域不能为空";
            case "E00001047" :
                logger.error("ErrorCode: E00001047, desp: holderMerId域超长");
                return "holderMerId域超长";
            case "E00001048" :
                logger.error("ErrorCode: E00001048, desp: 您的商户端账户中包含中文字符，我行暂不支持此类签约，请检查！");
                return "您的商户端账户中包含中文字符，我行暂不支持此类签约，请检查！";
            case "E00001049" :
                logger.error("ErrorCode: E00001049, desp: holderName域不能为空");
                return "holderName域不能为空";
            case "E00001050" :
                logger.error("ErrorCode: E00001050, desp: holderName域超长");
                return "holderName域超长";
            case "E00001051" :
                logger.error("ErrorCode: E00001051, desp: holderName域不符合系统要求格式");
                return "holderName域不符合系统要求格式";
            case "E00001052" :
                logger.error("ErrorCode: E00001052, desp: merchantUrl域不能为空");
                return "merchantUrl域不能为空";
            case "E00001053" :
                logger.error("ErrorCode: E00001053, desp: merchantUrl域超长");
                return "merchantUrl域超长";
            case "E00001054" :
                logger.error("ErrorCode: E00001054, desp: merchantUrl域不符合系统要求格式");
                return "merchantUrl域不符合系统要求格式";
            case "E00001055" :
                logger.error("ErrorCode: E00001055, desp: agrmtNo域不能为空");
                return "agrmtNo域不能为空";
            case "E00001056" :
                logger.error("ErrorCode: E00001056, desp: agrmtNo域超长");
                return "agrmtNo域超长";
            case "E00001057" :
                logger.error("ErrorCode: E00001057, desp: agrmtNo域不符合系统要求格式");
                return "agrmtNo域不符合系统要求格式";
            case "E00001058" :
                logger.error("ErrorCode: E00001058, desp: mRefundSeq域不能为空");
                return "mRefundSeq域不能为空";
            case "E00001059" :
                logger.error("ErrorCode: E00001059, desp: mRefundSeq域超长");
                return "mRefundSeq域超长";
            case "E00001060" :
                logger.error("ErrorCode: E00001060, desp: mRefundSeq域不符合系统要求格式");
                return "mRefundSeq域不符合系统要求格式";
            case "E00001061" :
                logger.error("ErrorCode: E00001061, desp: refundAmount域不能为空");
                return "refundAmount域不能为空";
            case "E00001062" :
                logger.error("ErrorCode: E00001062, desp: refundAmount域超长");
                return "refundAmount域超长";
            case "E00001063" :
                logger.error("ErrorCode: E00001063, desp: refundAmount域不符合系统要求格式");
                return "refundAmount域不符合系统要求格式";
            case "E00001067" :
                logger.error("ErrorCode: E00001067, desp: handleType域不能为空");
                return "handleType域不能为空";
            case "E00001068" :
                logger.error("ErrorCode: E00001068, desp: handleType域超长");
                return "handleType域超长";
            case "E00001069" :
                logger.error("ErrorCode: E00001069, desp: handleType域不符合系统要求格式");
                return "handleType域不符合系统要求格式";
            case "E00001070" :
                logger.error("ErrorCode: E00001070, desp: fileType域不能为空");
                return "fileType域不能为空";
            case "E00001071" :
                logger.error("ErrorCode: E00001071, desp: fileType域超长");
                return "fileType域超长";
            case "E00001072" :
                logger.error("ErrorCode: E00001072, desp: fileType域不符合系统要求格式");
                return "fileType域不符合系统要求格式";
            case "E00001073" :
                logger.error("ErrorCode: E00001073, desp: fileDate域不能为空");
                return "fileDate域不能为空";
            case "E00001074" :
                logger.error("ErrorCode: E00001074, desp: fileDate域超长");
                return "fileDate域超长";
            case "E00001075" :
                logger.error("ErrorCode: E00001075, desp: fileDate域不符合系统要求格式");
                return "fileDate域不符合系统要求格式";
            case "E00001076" :
                logger.error("ErrorCode: E00001076, desp: submitTime域不能为空");
                return "submitTime域不能为空";
            case "E00001077" :
                logger.error("ErrorCode: E00001077, desp: submitTime域超长");
                return "submitTime域超长";
            case "E00001078" :
                logger.error("ErrorCode: E00001078, desp: submitTime域不符合系统要求格式");
                return "submitTime域不符合系统要求格式";
            case "E00001079" :
                logger.error("ErrorCode: E00001079, desp: extend域不能为空");
                return "extend域不能为空";
            case "E00001080" :
                logger.error("ErrorCode: E00001080, desp: extend域超长");
                return "extend域超长";
            case "E00001081" :
                logger.error("ErrorCode: E00001081, desp: extend域不符合系统要求格式");
                return "extend域不符合系统要求格式";
            case "E00001082" :
                logger.error("ErrorCode: E00001082, desp: uri域不能为空");
                return "uri域不能为空";
            case "E00001083" :
                logger.error("ErrorCode: E00001083, desp: uri域超长");
                return "uri域超长";
            case "E00001084" :
                logger.error("ErrorCode: E00001084, desp: uri域不符合系统要求格式");
                return "uri域不符合系统要求格式";
            case "E00001085" :
                logger.error("ErrorCode: E00001085, desp: ticketId域不能为空");
                return "ticketId域不能为空";
            case "E00001086" :
                logger.error("ErrorCode: E00001086, desp: ticketId域超长");
                return "ticketId域超长";
            case "E00001087" :
                logger.error("ErrorCode: E00001087, desp: ticketId域不符合系统要求格式");
                return "ticketId域不符合系统要求格式";
            case "E00001088" :
                logger.error("ErrorCode: E00001088, desp: 上传文件内容 银行订单流水号不能为空");
                return "上传文件内容 银行订单流水号不能为空";
            case "E00001089" :
                logger.error("ErrorCode: E00001089, desp: 上传文件内容 银行订单流水号超长");
                return "上传文件内容 银行订单流水号超长";
            case "E00001090" :
                logger.error("ErrorCode: E00001090, desp: 上传文件内容 银行订单流水号不符合系统要求格式");
                return "上传文件内容 银行订单流水号不符合系统要求格式";
            case "E00001091" :
                logger.error("ErrorCode: E00001091, desp: 上传文件内容 商户号不能为空");
                return "上传文件内容 商户号不能为空";
            case "E00001092" :
                logger.error("ErrorCode: E00001092, desp: 上传文件内容 商户号超长");
                return "上传文件内容 商户号超长";
            case "E00001093" :
                logger.error("ErrorCode: E00001093, desp: 上传文件内容 商户号不符合系统要求格式");
                return "上传文件内容 商户号不符合系统要求格式";
            case "E00001094" :
                logger.error("ErrorCode: E00001094, desp: 上传文件内容 订单号不能为空");
                return "上传文件内容 订单号不能为空";
            case "E00001095" :
                logger.error("ErrorCode: E00001095, desp: 上传文件内容 订单号超长");
                return "上传文件内容 订单号超长";
            case "E00001096" :
                logger.error("ErrorCode: E00001096, desp: 上传文件内容 订单号不符合系统要求格式");
                return "上传文件内容 订单号不符合系统要求格式";
            case "E00001097" :
                logger.error("ErrorCode: E00001097, desp: 上传文件内容 订单日期不能为空");
                return "上传文件内容 订单日期不能为空";
            case "E00001098" :
                logger.error("ErrorCode: E00001098, desp: 上传文件内容 订单日期超长");
                return "上传文件内容 订单日期超长";
            case "E00001099" :
                logger.error("ErrorCode: E00001099, desp: 上传文件内容 订单日期不符合系统要求格式");
                return "上传文件内容 订单日期不符合系统要求格式";
            case "E00001100" :
                logger.error("ErrorCode: E00001100, desp: 上传文件内容 支付日期不能为空");
                return "上传文件内容 支付日期不能为空";
            case "E00001101" :
                logger.error("ErrorCode: E00001101, desp: 上传文件内容 支付日期超长");
                return "上传文件内容 支付日期超长";
            case "E00001102" :
                logger.error("ErrorCode: E00001102, desp: 上传文件内容 支付日期不符合系统要求格式");
                return "上传文件内容 支付日期不符合系统要求格式";
            case "E00001103" :
                logger.error("ErrorCode: E00001103, desp: 上传文件内容 币种不能为空");
                return "上传文件内容 币种不能为空";
            case "E00001104" :
                logger.error("ErrorCode: E00001104, desp: 上传文件内容 币种超长");
                return "上传文件内容 币种超长";
            case "E00001105" :
                logger.error("ErrorCode: E00001105, desp: 上传文件内容 币种不符合系统要求格式");
                return "上传文件内容 币种不符合系统要求格式";
            case "E00001106" :
                logger.error("ErrorCode: E00001106, desp: 上传文件内容 订单金额不能为空");
                return "上传文件内容 订单金额不能为空";
            case "E00001107" :
                logger.error("ErrorCode: E00001107, desp: 上传文件内容 订单金额超长");
                return "上传文件内容 订单金额超长";
            case "E00001108" :
                logger.error("ErrorCode: E00001108, desp: 上传文件内容 订单金额不符合系统要求格式");
                return "上传文件内容 订单金额不符合系统要求格式";
            case "E00001109" :
                logger.error("ErrorCode: E00001109, desp: 上传文件内容 退货金额不能为空");
                return "上传文件内容 退货金额不能为空";
            case "E00001110" :
                logger.error("ErrorCode: E00001110, desp: 上传文件内容 退货金额超长");
                return "上传文件内容 退货金额超长";
            case "E00001111" :
                logger.error("ErrorCode: E00001111, desp: 上传文件内容 退货金额不符合系统要求格式");
                return "上传文件内容 退货金额不符合系统要求格式";
            case "E00001112" :
                logger.error("ErrorCode: E00001112, desp: mobileNumber域不能为空");
                return "mobileNumber域不能为空";
            case "E00001113" :
                logger.error("ErrorCode: E00001113, desp: mobileNumber域超长");
                return "mobileNumber域超长";
            case "E00001114" :
                logger.error("ErrorCode: E00001114, desp: mobileNumber域不符合系统要求格式");
                return "mobileNumber域不符合系统要求格式";
            case "E00001115" :
                logger.error("ErrorCode: E00001115, desp: transId域不能为空");
                return "transId域不能为空";
            case "E00001116" :
                logger.error("ErrorCode: E00001116, desp: transId域超长");
                return "transId域超长";
            case "E00001117" :
                logger.error("ErrorCode: E00001117, desp: transId域不符合系统要求格式");
                return "transId域不符合系统要求格式";
            case "E00001118" :
                logger.error("ErrorCode: E00001118, desp: transType域不能为空");
                return "transType域不能为空";
            case "E00001119" :
                logger.error("ErrorCode: E00001119, desp: transType域超长");
                return "transType域超长";
            case "E00001120" :
                logger.error("ErrorCode: E00001120, desp: transType域不符合系统要求格式");
                return "transType域不符合系统要求格式";
            case "E00001121" :
                logger.error("ErrorCode: E00001121, desp: planCode域不能为空");
                return "planCode域不能为空";
            case "E00001122" :
                logger.error("ErrorCode: E00001122, desp: planCode域超长");
                return "planCode域超长";
            case "E00001123" :
                logger.error("ErrorCode: E00001123, desp: planCode域不符合系统要求格式");
                return "planCode域不符合系统要求格式";
            case "E00001124" :
                logger.error("ErrorCode: E00001124, desp: planNumber域不能为空");
                return "planNumber域不能为空";
            case "E00001125" :
                logger.error("ErrorCode: E00001125, desp: planNumber域超长");
                return "planNumber域超长";
            case "E00001126" :
                logger.error("ErrorCode: E00001126, desp: planNumber域不符合系统要求格式");
                return "planNumber域不符合系统要求格式";
            case "E00001127" :
                logger.error("ErrorCode: E00001127, desp: payType域不能为空");
                return "payType域不能为空";
            case "E00001128" :
                logger.error("ErrorCode: E00001128, desp: payType域超长");
                return "payType域超长";
            case "E00001129" :
                logger.error("ErrorCode: E00001129, desp: payType域不符合系统要求格式");
                return "payType域不符合系统要求格式";
            case "E00001130" :
                logger.error("ErrorCode: E00001130, desp: mseq域不能为空");
                return "mseq域不能为空";
            case "E00001131" :
                logger.error("ErrorCode: E00001131, desp: mseq域超长");
                return "mseq域超长";
            case "E00001132" :
                logger.error("ErrorCode: E00001132, desp: mseq域不符合系统要求格式");
                return "mseq域不符合系统要求格式";
            case "E00001133" :
                logger.error("ErrorCode: E00001133, desp: cspdata域不能为空");
                return "cspdata域不能为空";
            case "E00001134" :
                logger.error("ErrorCode: E00001134, desp: cspdata域超长");
                return "cspdata域超长";
            case "E00001135" :
                logger.error("ErrorCode: E00001135, desp: cspdata域不符合系统要求格式");
                return "cspdata域不符合系统要求格式";
            case "E00001136" :
                logger.error("ErrorCode: E00001136, desp: signCspdata域不能为空");
                return "signCspdata域不能为空";
            case "E00001137" :
                logger.error("ErrorCode: E00001137, desp: signCspdata域超长");
                return "signCspdata域超长";
            case "E00001138" :
                logger.error("ErrorCode: E00001138, desp: signCspdata域不符合系统要求格式");
                return "signCspdata域不符合系统要求格式";
            default:
                logger.error("There is no this errorCode: " + errorCode);
                return null;

        }
    }
}
