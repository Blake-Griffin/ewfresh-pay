package com.ewfresh.pay.util.unionpayh5pay;

import org.apache.commons.lang3.StringUtils;

import static com.ewfresh.pay.util.Constants.*;
import static com.ewfresh.pay.util.Constants.STR_ZERO;

/**
 * description: 是否分润
 * @author: JiuDongDong
 * date: 2019/6/27.
 */
public class IfShareBenefit {

    /**
     * Description: 是否要进行分润（支付时）
     * @author: JiuDongDong
     * @param shopIdRedis 店铺id
     * @param isRecharge 支付类型：实物订单支付时isRecharge=0，自营充值时isRecharge=4，订单补款isRecharge=8，白条还款isRecharge=15，店铺保证金16
     * @return boolean 是否要进行分润（支付时）
     * date: 2019/6/26 11:01
     */
    public synchronized static boolean ifShareBenefit(String shopIdRedis, String isRecharge) {
        if (StringUtils.isBlank(isRecharge) || StringUtils.isBlank(shopIdRedis)) {
            throw new RuntimeException("isRecharge and shopIdRedis must not be null" +
                    ", shopIdRedis = " + shopIdRedis + ", isRecharge = " + isRecharge);
        }
//        // 实物订单支付时isRecharge=0、订单补款isRecharge=8
//        if ((TRADE_TYPE_0.toString().equals(isRecharge) || TRADE_TYPE_8.toString().equals(isRecharge))
//                && !STR_ZERO.equals(shopIdRedis)) {
//            return true;
//        }
//        // 自营充值时isRecharge=4
//        if (TRADE_TYPE_4.toString().equals(isRecharge)) {
//            return false;
//        }
//        // 白条还款isRecharge=15
//        if (TRADE_TYPE_15.toString().equals(isRecharge)) {
//            return false;
//        }
        // 店铺保证金isRecharge=16
        if (TRADE_TYPE_16.toString().equals(isRecharge)) {
            return false;
        }
//        return false;

        if (STR_ZERO.equals(shopIdRedis)) {
            return false;
        } else {
            return true;
        }

    }
}
