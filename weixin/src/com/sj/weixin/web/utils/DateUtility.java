package com.sj.weixin.web.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtility {
	public static final DateFormat DATEFORMAT_YYYYMMDD =
			new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat DATEFORMAT_YYYYMMDD_CH =
			new SimpleDateFormat("yyyy年MM月dd日");
	public static final DateFormat DATEFORMAT_YYYYMM =
			new SimpleDateFormat("yyyyMM");
	public static final DateFormat DATEFORMAT_YYYYMM_CH =
			new SimpleDateFormat("yyyy年MM月");
	public static final DateFormat DATEFORMAT_YYYY =
			new SimpleDateFormat("yyyy");
	public static final DateFormat DATEFORMAT_YYYY_MM_DD_HH_MM_SS =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat DATEFORMAT_YYYY_MM_DD =
			new SimpleDateFormat("yyyy-MM-dd");
	
	public static String formatDate(DateFormat df, Date date) {
		String result = "";
		if(date != null) {
			result = df.format(date);
		}
		return result;
	}
	
	public static String formatDate(DateFormat df, Calendar calendar) {
		String result = "";
		if(calendar != null) {
			result = df.format(calendar.getTime());
		}
		return result;
	}
	
	public static Date parseDate(DateFormat df, String str) {
		try {
			return df.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static Calendar parseCalendar(DateFormat df, String str) {
		Date date = parseDate(df, str);
		return date2Calendar(date);
	}
	
	public static Calendar date2Calendar(Date date) {
		if(date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
	
	public static Calendar getFirstDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar;
	}
	
	public static Calendar getLastDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar;
	}
	
	public static Date getPervMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		return calendar.getTime();
	}
	
	public static int compareDay(Date date1, Date date2) {
		String date1Str = DATEFORMAT_YYYYMMDD.format(date1);
		String date2Str = DATEFORMAT_YYYYMMDD.format(date2);
		return date1Str.compareTo(date2Str);
	}
	
	public static boolean isPrevMonth(String yd) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		String month = formatDate(DATEFORMAT_YYYYMM, calendar);
		return month.equals(yd);
	}
	
	public static boolean isCurrMonth(String yd) {
		Calendar calendar = Calendar.getInstance();
		String month = formatDate(DATEFORMAT_YYYYMM, calendar);
		return month.equals(yd);
	}
}
