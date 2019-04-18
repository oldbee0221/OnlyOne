package mms5.onepagebook.com.onlyonesms.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.RealmObject;

public class LogData extends RealmObject {
    private String log;
    private Long time;

    public static String convertToLocal(long time, String format) {
        SimpleDateFormat formatOutgoing = new SimpleDateFormat(format, Locale.getDefault());
        TimeZone tz = TimeZone.getDefault();
        formatOutgoing.setTimeZone(tz);

        Calendar calendar = Calendar.getInstance(tz, Locale.getDefault());
        calendar.setTimeInMillis(time);
        return formatOutgoing.format(calendar.getTime());
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return convertToLocal(time, "MM/dd hh:mm:ss:SSS");
    }
}