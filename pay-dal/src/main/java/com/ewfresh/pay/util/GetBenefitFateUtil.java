package com.ewfresh.pay.util;

import com.ewfresh.pay.redisService.GetShopInfoRedisService;
import com.ewfresh.pay.util.bob.FenYuanConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ewfresh.pay.util.Constants.*;

/**
 * description: 根据店铺id（非0）获取mid、店铺分润后金额、易网聚鲜分润所得金额
 * @author: JiuDongDong
 * date: 2019/6/26.
 */
@Component
public class GetBenefitFateUtil {
    @Autowired
    private GetShopInfoRedisService getShopInfoRedisService;

    /**
     * Description: 获取mid
     * @author: JiuDongDong
     * @param shopIdRedis 三方店铺的id
     * @param tradeType  1H5, 2QrCode
     * @return java.lang.String mid
     * date: 2019/6/27 15:34
     */
    public synchronized String getMid(String shopIdRedis, String tradeType) throws Exception {
        Map<String, Map<String, Object>> shopInfoMap = getShopInfoRedisService.getShopInfo(shopIdRedis);
        Map<String, Object> shopInfo = shopInfoMap.get(SHOP);//店铺信息
        String shopMid;
        String cardBusiNum = shopInfo.get("cardBusiNum").toString();//银联卡商户号
        String ctobBusiNum = shopInfo.get("ctobBusiNum").toString();//公共支付c扫b商户号
        if (STR_ONE.equals(tradeType)) {
            shopMid = cardBusiNum;
        } else {
            shopMid = ctobBusiNum;
        }
        return shopMid;
    }

    /**
     * Description: 获取email
     * @author: JiuDongDong
     * @param shopIdRedis 三方店铺的id
     * @return java.lang.String 三方店铺的email
     * date: 2019/8/12 9:45
     */
    public synchronized String getEmail(String shopIdRedis) {
        Map<String, Map<String, Object>> shopInfoMap = getShopInfoRedisService.getShopInfo(shopIdRedis);
        //店铺信息
        Map<String, Object> shopInfo = shopInfoMap.get(SHOP);
        String email = shopInfo.get("email").toString();
        return email;
    }

    /**
     * Description: 获取分润金额及mid
     * @author: JiuDongDong
     * @param shopIdRedis 三方店铺的id
     * @param shareBenefitAmt  分润金额，单位为元
     * @param tradeType  1H5, 2QrCode
     * @return java.lang.String 分润比例
     * date: 2019/6/26 15:59
     */
    public synchronized Map<String, String> getBenefitAmtAndMid(String shopIdRedis, BigDecimal shareBenefitAmt, String tradeType) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        Map<String, Map<String, Object>> shopInfoMap = getShopInfoRedisService.getShopInfo(shopIdRedis);
        Map<String, Object> shopInfo = shopInfoMap.get(SHOP);//店铺信息
        String shopMid;
        String cardBusiNum = shopInfo.get("cardBusiNum").toString();//银联卡商户号
        String ctobBusiNum = shopInfo.get("ctobBusiNum").toString();//公共支付c扫b商户号
        if (STR_ONE.equals(tradeType)) {
            shopMid = cardBusiNum;
        } else {
            shopMid = ctobBusiNum;
        }

        Map<String, Object> shareBenefitMap = shopInfoMap.get(SHAREBENEFIT);//分润信息
        Integer benefit = new Integer(shareBenefitMap.get("benefit").toString());//所得利润比例
        Integer discountBenefit = new Integer(shareBenefitMap.get("discountBenefit").toString());//优惠利润比例
        String endDiscountTime = shareBenefitMap.get("endDiscountTime").toString();//优惠截止时间
        // 获取分润比例
        Integer benefitFate = getBenefitFate(benefit, discountBenefit, endDiscountTime);
        // 计算店铺实得金额，单位为元
        BigDecimal shopAmt =
                FormatBigDecimal.formatBigDecimal(shareBenefitAmt.multiply(new BigDecimal(benefitFate)).divide(new BigDecimal("100")));
        // 计算易网聚鲜分润金额，单位为元
        BigDecimal ewfreshAmt = shareBenefitAmt.subtract(shopAmt);
        // 返回，元转换为分
        resultMap.put("shopMid", shopMid);
        resultMap.put("shopAmt", FenYuanConvert.yuan2Fen(shopAmt.toString()).toString());
        resultMap.put("ewfreshAmt", FenYuanConvert.yuan2Fen(ewfreshAmt.toString()).toString());
        return resultMap;
    }


    /**
     * Description: 获取分润比例
     * @author: JiuDongDong
     * @param benefit 原始分润比例
     * @param discountBenefit 减免比例
     * @param endDiscountTime  优惠截止日期
     * @return java.lang.String 分润比例
     * date: 2019/6/26 15:59
     */
    private Integer getBenefitFate(Integer benefit, Integer discountBenefit, String endDiscountTime) throws Exception {
        Integer benefitFate;
        int i = new Date().compareTo(DateUtil.getFutureMountDaysStartWithOutHMS(new SimpleDateFormat("yyyy-MM-dd").parse(endDiscountTime), 1));
        if (i <= 0) {
            benefitFate = discountBenefit;
        } else {
            benefitFate = benefit;
        }
        return benefitFate;
    }
}
