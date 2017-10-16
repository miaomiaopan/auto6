package com.yh.qa.util;

import java.util.Calendar;

/**
 * @author matt Gong
 */
public class DateUtil {
    /**
     *获取某天的时间戳
     * @param time  表示要增加/减少的天数，
     * @return
     */
    public static String getTime(int time) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE,time);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return String.valueOf(now.getTimeInMillis());
    }

    //得到当天的时间戳
    public static String getTodyTimeInMillis(){
        return getTime(0);
    }
    //得到明天的时间戳
    public static String getTomorrowTimeInMillis(){
        return getTime(1);
    }
}