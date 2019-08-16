package com.ewfresh.pay.util.bill99share;

import com.ewfresh.pay.model.bill99share.Bill99ShareDetail;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * description: 将ShareDetail拼接成String
 * @author: JiuDongDong
 * date: 2019/8/8.
 */
public class CreateShareDetailUtil {

    /**
     * Description: 创建分账信息
     * @author: JiuDongDong
     * @param bill99ShareDetailList
     * @return java.lang.String 分账信息
     * date: 2019/8/8 17:02
     */
    public static String createShareDetail(List<Bill99ShareDetail> bill99ShareDetailList) {
        if (CollectionUtils.isEmpty(bill99ShareDetailList)) {
            return null;
        }
        StringBuilder result = new StringBuilder("");
        Iterator<Bill99ShareDetail> iterator = bill99ShareDetailList.iterator();
        while (iterator.hasNext()) {
            Bill99ShareDetail bill99ShareDetail = iterator.next();
            String sharingContactType = bill99ShareDetail.getSharingContactType();
            result = result.append(sharingContactType).append("^");
            String sharingContact = bill99ShareDetail.getSharingContact();
            result = result.append(sharingContact).append("^");
            String sharingApplyAmount = bill99ShareDetail.getSharingApplyAmount();
            result = result.append(sharingApplyAmount).append("^");
            String sharingFeeRate = bill99ShareDetail.getSharingFeeRate();
            result = result.append(sharingFeeRate).append("^");
            String sharingDesc = bill99ShareDetail.getSharingDesc();
            String sharingSyncFlag = bill99ShareDetail.getSharingSyncFlag();
            if (StringUtils.isBlank(sharingSyncFlag)) {
                result = result.append(sharingDesc).append("|");
            } else {
                result = result.append(sharingDesc).append("^");
                result = result.append(sharingSyncFlag).append("|");
            }
        }
        int length = result.length();
        result = result.deleteCharAt(length - 1);
        return result.toString();
    }

}
