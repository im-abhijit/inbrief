package io.github.abhijit.inbrief.utils;

import io.netty.util.internal.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateParser {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(DateParser.class);
    public static Date parseDate(String date)  {
        try{
            return sdf.parse(date);
        }
        catch (Exception e){
            LOGGER.warn("Failed to parse date string '{}', defaulting to current time. Error: {}", date, e.toString());
        }
        return new Date();
    }

    public static String parseDate(java.util.Date date)  {
        try{
            return sdf.format(date);
        }
        catch (Exception e){
            LOGGER.warn("Failed to format date '{}', returning null. Error: {}", String.valueOf(date), e.toString());
        }
        return null;
    }
}
