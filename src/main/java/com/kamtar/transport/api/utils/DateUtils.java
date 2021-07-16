package com.kamtar.transport.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public static Date addTimezone(Date date, Integer timezone) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, timezone * -1);  
		return c.getTime(); 
	}

	public static String reverseDateFrancaiseToUS(String oldDateString)  {

		final String OLD_FORMAT = "dd/MM/yyyy";
		final String NEW_FORMAT = "yyyy/MM/dd";


		SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
		Date d = null;
		try {
			d = sdf.parse(oldDateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sdf.applyPattern(NEW_FORMAT);
		return sdf.format(d);
	}

}
