package com.handyapps.stocktracker.model;

import java.util.Calendar;
import java.util.Locale;
import android.content.Context;
import android.content.res.Configuration;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.StateDatePeriodObject;
import com.handyapps.stocktracker.utils.MyDateFormat;

public class StateManager {
	
	public static final int ONE_DAY = 1;
	public static final int FIVE_DAYS = 5;
	public static final int ONE_MONTH = 30;
	public static final int THREE_MONTHS = 90;
	public static final int SIX_MONTHS = 180;
	public static final int ONE_YEAR = 365;
	public static final int TWO_YEARS = 730;

	public static final int[] DATE_PERIODS = { ONE_DAY, FIVE_DAYS, ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, TWO_YEARS };

	
	public static StateDatePeriodObject converTimePeriod(Context context, int idDatePeriod){
		
		int mIdDatePeriod = idDatePeriod;
		Context mContext = context;
		
		StateDatePeriodObject datePeriod = new StateDatePeriodObject();
		
		Configuration config = mContext.getResources().getConfiguration();
		Locale locale = config.locale;
		Calendar toDateCalendar = Calendar.getInstance(locale);
		
		//1.1 covert today to YYYYDDMM(integer):- it's using for query
		String sToDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(toDateCalendar);
		int numToDateTodayYYYYMMDD = Integer.parseInt(sToDateYYYYMMDD);
		
		//1.2 convert today to String format: - used for show "dd-MMM-yyyy";
		String strShowDateTo = MyDateFormat.calendarToDateStringFormater(toDateCalendar);
		
		//1.3 set to DatePeriod Object.
		datePeriod.setTo(numToDateTodayYYYYMMDD);
		datePeriod.setToTxt(strShowDateTo);
		
		switch(mIdDatePeriod){
		
		case ONE_DAY:
		break;
		case FIVE_DAYS:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - FIVE_DAYS);
			break;
		case ONE_MONTH:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - ONE_MONTH);
			break;
		case THREE_MONTHS:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - THREE_MONTHS);
			break;
		case SIX_MONTHS:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - SIX_MONTHS);
			break;
		case ONE_YEAR:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - ONE_YEAR);
			break;
		case TWO_YEARS:
			toDateCalendar.add(Calendar.DAY_OF_YEAR, - TWO_YEARS);
			break;
		}
		
		//2.1 covert From Date to YYYYDDMM(integer):- it's using for query
		String sFromDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(toDateCalendar);
		int numFromDateYYYYMMDD = Integer.parseInt(sFromDateYYYYMMDD);
		
		//2.2 convert From Date to String format: - used for show "dd-MMM-yyyy";
		String strShowDateFrom = MyDateFormat.calendarToDateStringFormater(toDateCalendar);
		
		//3.3 all Full Text Show: 
		String fromDateTitle = mContext.getResources().getString(R.string.from_date_title_datepicker);
		String toDateTitle = mContext.getResources().getString(R.string.to_date_title_datepicker);
		String fullDatePeriodTxt = fromDateTitle + ": " + strShowDateFrom + "\n" + toDateTitle + ": " + strShowDateTo;
		
		//4. set fromDate(Integer - FOR_DB_QUERY), toDate(Integer - FOR_DB_QUERY), fullDatePeriod(String - FOR_SHOW)
		datePeriod.setFrom(numFromDateYYYYMMDD);
		datePeriod.setFromTxt(strShowDateFrom);
		datePeriod.setFullDatePeriodTxt(fullDatePeriodTxt);
		
		return datePeriod;
	}
}
