package com.ewfresh.pay.util.unionpayh5pay;

import java.security.MessageDigest;

public class UnionpayH5B2BMD5Util {
    /**
     * <p>
     * 功能描述:[Md5加密]
     * </p>
     *
     * @param str
     * @return
     * @author:mayuanfei
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }
}
