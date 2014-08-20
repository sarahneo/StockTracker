package com.handyapps.stocktracker.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;

public class MyDateFormat {

	/**
	 * this string is database date format string(YYYYMMDD) eg: 20121125
	 * 
	 * MyDateFormat mydate = new MyDateFormat(Contex ctx);
	 * 
	 * Calendar cal = mydate.convertYYYYMMDDToCalendar(String textYYYYMMDD);
	 * 
	 */
	public static Calendar convertYYYYMMDDToCalendar(String textYYYYMMDD) {

		Calendar cal = Calendar.getInstance();

		if (textYYYYMMDD != null) {

			int year = Integer.valueOf(textYYYYMMDD.substring(0, 4));

			int month = Integer.valueOf(textYYYYMMDD.substring(4, 6)) - 1;

			int day = Integer.valueOf(textYYYYMMDD.substring(6, 8));

			cal.set(year, month, day, 0, 0, 0);

		} else {

			return null;
		}
		return cal;
	}

	/**
	 * Return date format string. eg: 13-Dec-2012
	 * 
	 * Formater is : dd-MMM-yyyy
	 * 
	 * MyDateFormat mydate = new MyDateFormat(Contex ctx);
	 * 
	 * String dateFormatString = mydate.calendarToStringFormater(Calendar cal);
	 * 
	 */
	public static String convertCalendarToYYYYMMDD(Calendar c) {

		if (c != null) {

			int year = 0;
			int month = 0;
			int day = 0;

			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);

			// 1. get 'Year' string
			String myYear = String.valueOf(year);

			// 2. get 'Month' string
			String myMonth;
			month = month + 1;
			if (month < 10) {
				myMonth = "0" + String.valueOf(month);
			} else {
				myMonth = String.valueOf(month);
			}

			// 3. get 'Day' string
			String myDate;
			if (day < 10) {
				myDate = "0" + String.valueOf(day);
			} else {
				myDate = String.valueOf(day);
			}

			// 4. get 'YYYYMMDD' string, this data will be store to database.
			String strDateYYYYMMDD = (myYear + myMonth + myDate).toString();

			return strDateYYYYMMDD;

		} else {

			return "";
		}
	}

	public static String calendarToDateStringFormater(Calendar cal) {

		if (cal != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
			String result = formatter.format(cal.getTimeInMillis());
			return result;
		} else {

			return null;
		}
	}
	
	public static boolean isDoUpdate(Context ctx) {
		// Check if within notification window
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		String defValue = ctx.getResources().getString(R.string.default_alerts_window);
		boolean isWindowOn = sp.getBoolean(Constants.SP_KEY_IS_SET_ALERTS_WINDOW, false);
		String window = sp.getString(Constants.SP_KEY_ALERTS_WINDOW, defValue);
		String startTime = window.substring(0, window.indexOf("-")-1);
		String endTime = window.substring(window.indexOf("-")+2, window.length());
		DateFormat formatter = new SimpleDateFormat("h:mm a");
		Date startDate = new Date();
		Date endDate = new Date();
		Date currDate = new Date();
		String currDateString = formatter.format(currDate);
		
		if (!isWindowOn)
			return true;
		
		try {
			currDate = formatter.parse(currDateString);
			startDate = formatter.parse(startTime);
			endDate = formatter.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (currDate.after(startDate) && endDate.after(currDate))
			return true;
		else {
			Log.i("alarmreceiver", "outside of update window");
			return false;			
		}
	
	}

}
