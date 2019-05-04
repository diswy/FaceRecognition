package com.yibaiqi.face.recognition.tools;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 格式化时间 JAVA写可以在xml中引用使用
 * Created by @author xiaofu on 2019/3/15.
 */
public class TimeFormat {
    public static String MDHM(String time) {
        if (time == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat("MM月dd日  HH:mm", Locale.CHINA);
        return format.format(new Date(formatTime(time)));
    }

    public static String YMD(String time){
        if(time == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return format.format(new Date(formatTime(time)));
    }

    public static String MDHM(Long time) {
        if (time == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat("MM月dd日  HH:mm", Locale.CHINA);
        return format.format(new Date(time));
    }

    public static String MD(String time) {
        if (time == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat("MM月dd日", Locale.CHINA);
        return format.format(new Date(formatTime(time)));
    }

    public static String YMDHMS(String time) {
        if (time == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return format.format(new Date(formatTime(time)));
    }

    public static String FULL(long timeMillis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return format.format(calendar.getTime());
    }

    private static Long formatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
        try {
            return format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

}