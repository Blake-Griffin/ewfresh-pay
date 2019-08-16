package com.ewfresh.pay.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * description: 日期工具类, 如果使用过程中发现bug, 请e-mail：jiudongdong@sunkfa.com, 谢谢！
 * @author: JiuDongDong
 * date: 2017/12/13.
 */
public class DateUtil {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    /**
     * Description: 根据到岗日期计算冰鲜类产品的付尾款截止日期
     *              计算规则：到货前一天15点
     * @author: JiuDongDong
     * @param arrivalDate 到货日期
     * @return: java.util.Date 冰鲜类（活鲜、冰鲜）产品的付尾款截止日期
     * date: 2018/1/25 18:03:38
     */
    public static synchronized Date getFreshPayEndTime(Date arrivalDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(arrivalDate);
        // 往前推1天
        calendar.add(Calendar.DAY_OF_MONTH, - 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 15, 0, 0);
        Date date = calendar.getTime();
        return date;
    }

    /**
     * Description: 根据到岗日期计算冻品类产品的付尾款截止日期
     *              计算规则：到港时间提前18天的15点
     * @author: JiuDongDong
     * @param arrivalDate 到港日期
     * @return: java.util.Date 冻品类（冻品、精品）产品的付尾款截止日期
     * date: 2018/1/25 18:03:38
     */
    public static synchronized Date getFrozenPayEndTime(Date arrivalDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(arrivalDate);
        // 往前推17天
        calendar.add(Calendar.DAY_OF_MONTH, - 18);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 15, 0, 0);
        Date date = calendar.getTime();
        return date;
    }

    /**
     * Description: 根据到岗日期计算定金支付订单（冰鲜类或冻品类产品）的短信提醒时间
     *              计算规则：到货前m天hourOfDay点
     * @author: JiuDongDong
     * @param arrivalDate 到货日期
     * @param hourOfDay 时间，0~23，假如时间为：2018-05-10 14:23:23，则hourOfDay=14
     * @param daysBeforeArrivalDate 到货日期的前m天发送短信提醒
     * @return: java.util.Date 冰鲜类产品（活鲜、冰鲜）、冻品类产品（冻品、精品）的短信提醒时间
     * date: 2018/5/10 14:03:38
     */
    public static synchronized Date getFreshOrFrozenSendMessageTime(Date arrivalDate, Integer daysBeforeArrivalDate, Integer hourOfDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(arrivalDate);
        // 往前推m天
        calendar.add(Calendar.DAY_OF_MONTH, - daysBeforeArrivalDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hourOfDay, 0, 0);
        Date date = calendar.getTime();
        return date;
    }

    /**
     * Description: 根据给定日期获取往后推n小时的时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nHours 往后推n小时的时间
     * @return: java.util.Date 给定日期的开始时间
     * date: 2018/1/29 18:27
     */
    public static synchronized Date getFutureMountHoursStart(Date date, Integer nHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, nHours);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取给定日期往后推n分钟的时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nMinutes 往后推n分钟的时间
     * @return: java.util.Date 给定日期的开始时间
     * date: 2018/5/30 16:27
     */
    public static synchronized Date getFutureMountMinutesStart(Date date, Integer nMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, nMinutes);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取给定日期往后推n秒钟的时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nSeconds 往后推n秒的时间
     * @return: java.util.Date 给定日期的开始时间
     * date: 2018/5/30 16:39
     */
    public static synchronized Date getFutureMountSecondsStart(Date date, Integer nSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, nSeconds);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取给定日期往后推n天的时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nDays 往后推n天的时间
     * @return: java.util.Date 给定日期往后推n天的时间
     * date: 2018/1/31 11:27
     */
    public static synchronized Date getFutureMountDays(Date date, Integer nDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, nDays);
        Date time = calendar.getTime();
        return time;
    }


    /**
     * Description: 根据日历获取小时时间
     * @author: JiuDongDong
     * @param calendar 自定义日历实例
     * @return: java.lang.Integer 小时时间
     * date: 2017/12/13 16:36
     */
    public static synchronized Integer getHourOfDay(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Description: 根据日历获取这是这个月的第几天
     * @author: JiuDongDong
     * @param calendar 自定义日历实例
     * @return: java.lang.Integer 小时时间
     * date: 2017/12/13 16:36
     */
    public static synchronized Integer getDayOfMonth(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Description: 根据日历获取当月的天数最大值
     * @author: JiuDongDong
     * @param calendar 自定义日历实例
     * @return: java.lang.Integer 当月天数最大值
     * date: 2017/12/13 16:40
     */
    public static synchronized Integer getMaxDayOfMonth(Calendar calendar) {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Description: 根据给定日期获取下个月月初开始时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @return: java.util.Date 下个月月初时间
     * date: 2017/12/14 11:27
     */
    public static synchronized Date getNextMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(year, month, 1, 0, 0, 0);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取前或后指定n个月月初开始时间:
     *              1.如果获取给定时间往后的时间，n取大于0的整数
     *              2.如果获取给定时间往前的时间，n取小于0的整数
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nMonths 往前或往后推n个月
     * @return: java.util.Date 往前或往后推num_Month个月的月初时间
     * date: 2018/1/19 10:27
     */
    public static synchronized Date getAmountMonthStartDate(Date date, Integer nMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, nMonths);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(year, month, 1, 0, 0, 0);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取前或后指定n个月月初开始时间:
     *              1.如果获取给定时间往后的时间，n取大于0的整数
     *              2.如果获取给定时间往前的时间，n取小于0的整数
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nMonths 往前或往后推n个月
     * @return: java.util.Date 往前或往后推n个月的开始时间
     * date: 2018/1/19 10:27
     */
    public static synchronized Date getAmountMonthDate(Date date, Integer nMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, nMonths);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 获取给定日期的当年的开始时间
     * @author: JiuDongDong
     * @param date 给定日期
     * @return: java.util.Date 给定日期的当年的开始时间
     * date: 2018/1/19 10:27
     */
    public static synchronized Date getYearStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(year, 0, 1, 0, 0, 0);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取给定日期往后推n天的时间
     *              返回时、分、秒设置为0的日期
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nDays 往后推n天
     * @return: java.util.Date 给定日期的开始时间
     * date: 2017/12/14 11:27
     */
    public static synchronized Date getFutureMountDaysStartWithOutHMS(Date date, Integer nDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day + nDays, 0, 0, 0);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * Description: 根据给定日期获取给定日期往后推n小时的时间
     *              返回分、秒设置为0的日期
     * @author: JiuDongDong
     * @param date 给定日期
     * @param nHours 往后推n小时
     * @return: java.util.Date 给定日期的开始时间
     * date: 2018/1/19 10:27
     */
    public static synchronized Date getFutureMountHoursStartWithOutMS(Date date, Integer nHours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(year, month, day, hour + nHours, 0, 0);
        Date time = calendar.getTime();
        return time;
    }


    /**
     * Description: 用于根据输入的日期格式获取SimpleDateFormat对象
     * @author: JiuDongDong
     * @param pattern 输入的日期格式
     * @return: java.text.SimpleDateFormat
     * date: 2017/12/18 20:22:31
     */
    public static synchronized SimpleDateFormat getSimpleDateFormat(String pattern) {
        SimpleDateFormat simpleDateFormat;
        try {
            simpleDateFormat = new SimpleDateFormat(pattern);
        } catch (Exception e) {
            return null;
        }
        return simpleDateFormat;
    }

    public static String toString(Date date, String sFmt){

        if (date == null || StringUtils.isBlank(sFmt)){
            return null;
        }

        return toString(date, new SimpleDateFormat(sFmt));

    }

    private static String toString(Date date, SimpleDateFormat sFmt){

        String timeStr = "";
        try {
            timeStr = sFmt.format(date);
        } catch (Exception e) {
            logger.error("format date err",e);
        }
        return timeStr;

    }
}
