package com.scorpion.sleep.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by stephen on 2015-11-29.
 */
public class ConversionUtils {

    public static String dateToString(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date dateObject = format.parse(date);
            return dateObject.getDate() + "/" + (dateObject.getMonth() + 1) + "/" + (dateObject.getYear() + 1900);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static long dateToMillis(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            c.setTime(format.parse(date));
            return c.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
