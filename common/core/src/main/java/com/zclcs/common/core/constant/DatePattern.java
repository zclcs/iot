package com.zclcs.common.core.constant;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author zclcs
 */
public class DatePattern {

    public static String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static String NORM_DATE_PATTERN = "yyyy-MM-dd";
    public static String NORM_TIME_PATTERN = "HH:mm:ss";
    public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN, Locale.getDefault())
            .withZone(ZoneId.systemDefault());
    public static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATE_PATTERN, Locale.getDefault())
            .withZone(ZoneId.systemDefault());
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_TIME_PATTERN, Locale.getDefault())
            .withZone(ZoneId.systemDefault());

}
