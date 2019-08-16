package com.ewfresh.pay.util.bob;

import com.alibaba.fastjson.TypeReference;
import com.ewfresh.commons.util.ItvJsonUtil;
import com.ewfresh.pay.util.Constants;
import com.ewfresh.pay.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * description:
 *      获取银行卡类别
 * @author: JiuDongDong
 * date: 2018/5/22.
 */
public class GetCardInfo {

    private static final String CART_TYPE_DC = "DC";//DC: "储蓄卡"
    private static final String CART_TYPE_CC = "CC";//CC: "信用卡"
    private static final String CART_TYPE_SCC = "SCC";//SCC: "准贷记卡"
    private static final String CART_TYPE_PC = "PC";//PC: "预付费卡"

    /**
     * Description: 根据银行卡号获取银行卡信息
     * @author: JiuDongDong
     * @param cardNo  银行卡号
     * @return java.lang.String 类别
     * date: 2018/5/22 21:40
     */
    public static synchronized Map<String, String> getCartInfoByCartNo(String cardNo) {
        Map<String, String> stringObjectMap = null;
        // 创建HttpClient实例
        try {
            String url = "https://ccdcapi.alipay.com/validateAndCacheCardInfo.json?_input_charset=utf-8&cardNo=";
            url += cardNo;
            url += "&cardBinCheck=true";
            StringBuilder sb = new StringBuilder();
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            stringObjectMap = ItvJsonUtil.jsonToObj(sb.toString(), new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return stringObjectMap;
        }
    }

    /**
     * Description: 根据银行卡号获取费率
     * @author: JiuDongDong
     * @param cardNo 银行卡号
     * @return java.lang.String 费率
     * date: 2018/5/22 22:05
     */
    public static synchronized String calBankFate(String cardNo) {
        Map<String, String> cartInfo = getCartInfoByCartNo(cardNo);
        String bank = cartInfo.get("bank");
        String cardType = cartInfo.get("cardType");
        switch (bank) {
            case "BOB" :
                if (CART_TYPE_CC.equals(cardType)) {
                    return Constants.FATE_BOB_CREDIT;
                }
                if (CART_TYPE_DC.equals(cardType)) {
                    return Constants.FATE_BOB_BORROW;
                }
                if (CART_TYPE_SCC.equals(cardType)) {
                    // TODO 同晓佳确认
                    return Constants.FATE_BOB_CREDIT;
                }
                if (CART_TYPE_PC.equals(cardType)) {
                    // TODO 同晓佳确认
                    return Constants.FATE_BOB_BORROW;
                }
                break;
        }
//        private static final String CART_TYPE_DC = "DC";//DC: "储蓄卡"
//        private static final String CART_TYPE_CC = "CC";//CC: "信用卡"
//        private static final String CART_TYPE_SCC = "SCC";//SCC: "准贷记卡"
//        private static final String CART_TYPE_PC = "PC";//PC: "预付费卡"


        return "";
    }

}
