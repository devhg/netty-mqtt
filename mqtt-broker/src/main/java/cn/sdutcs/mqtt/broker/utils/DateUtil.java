package cn.sdutcs.mqtt.broker.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String now(String format) {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }
}
