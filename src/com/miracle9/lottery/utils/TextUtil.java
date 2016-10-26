package com.miracle9.lottery.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class TextUtil {
	private static String str = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
	private static Random random = new Random();
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 随机获取指定长度的字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String getStr(int length) {
		char[] carr = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(carr[random.nextInt(carr.length)]);
		}

		return sb.toString();
	}

	public static Date dateformat(String date) {
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			LogManager.error(e);
			return null;
		}
	}

	/**
	 * 判断两个日期是否同一天
	 * 
	 * @param oneDate
	 * @param twoDate
	 * @return
	 */
	public static boolean isSameDay(Date oneDate, Date twoDate) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(oneDate);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(twoDate);

		return isSameDay(c1, c2);
	}

	/**
	 * 判断两个日期是否同一天
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public static boolean isSameDay(Calendar one, Calendar two) {
		return ((one.get(Calendar.YEAR) == two.get(Calendar.YEAR))
				&& (one.get(Calendar.DAY_OF_YEAR) == two.get(Calendar.DAY_OF_YEAR)));
	}

	public static int getCurrentHour() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * 当前是否在指定时间范围内
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isBetween(Date begin, Date end) {
		return isBetween(Calendar.getInstance().getTime(), begin, end);
	}

	/**
	 * 指定时间是否在两个时间点之间
	 * 
	 * @param checkPoint
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isBetween(Date checkPoint, Date begin, Date end) {
		return checkPoint.getTime() > begin.getTime() && checkPoint.getTime() < end.getTime();
	}
}
