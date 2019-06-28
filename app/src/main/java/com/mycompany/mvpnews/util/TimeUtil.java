package com.mycompany.mvpnews.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;

public class TimeUtil {

    public static String getToday(){
        Calendar t = Calendar.getInstance(); // 默认得到的是当前的日期
        int tyear = t.get(Calendar.YEAR);
        int tmonth = t.get(Calendar.MONTH) + 1;
        int tday = t.get(Calendar.DAY_OF_MONTH);
        String today = format(Locale.CHINA, "%s%02d%02d", tyear, tmonth, tday);
        return today;
    }
    public static String getNextDay(int date){
        String time = format(Locale.CHINA, "%s-%02d-%02d-00-00-00", date / 10000, date / 100 % 100, date % 100);
        int times = valueOf(dataOne(time)) - 60 * 60 * 24;
        String sleepDatess = timedate(String.valueOf(times));
        String date1 = sleepDatess.replace("-", "");
        String date2 = date1.replace(" 00:00:00", "");
        return date2;
    }
    /**
     * 调此方法输入所要转换的时间输入例如（"2014-06-14-16-09-00"）返回时间戳
     *
     */
    public static String dataOne(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
                Locale.CHINA);
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            String stf = String.valueOf(l);
            times = stf.substring(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }

    /**
     * 调用此方法输入所要转换的时间戳输入例如（1402733340）输出（"2014-06-14  16:09:00"）
     *
     */
    public static String timedate(String time) {

        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(time);
        int i = Integer.parseInt(time);
        return sdr.format(new Date(i * 1000L));
    }
}
