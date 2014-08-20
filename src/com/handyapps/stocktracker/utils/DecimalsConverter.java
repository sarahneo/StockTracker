package com.handyapps.stocktracker.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;

public class DecimalsConverter {

	public static String convertToStringValueBaseOnLocale(double doubleValue, int numberOfDecimal, Context context) {

		double mDoubleValue = doubleValue;
		int mNumberOfDecimal = numberOfDecimal;
		Context mContext = context;

		String sAfter;

		Configuration config = mContext.getResources().getConfiguration();

		// Locale localUk = Locale.UK;
		Locale local = config.locale;

		/*
		 * DecimalFormat format = (DecimalFormat)
		 * NumberFormat.getNumberInstance(Locale.FRANCE);
		 */

		DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(local);

		switch (mNumberOfDecimal) {

		case 0:
			format.applyPattern("$###,###,###,###,###,##0");
			break;

		case 1:
			format.applyPattern("$###,###,###,###,###,##0.0");
			break;

		case 2:
			format.applyPattern("$###,###,###,###,###,##0.00");
			break;

		case 3:
			format.applyPattern("$###,###,###,###,###,##0.000");
			break;
		}

		sAfter = format.format(mDoubleValue);
		return sAfter;
	}
	
	public static String convertToStringValueNoCurrencyBasedOnLocale(double doubleValue, int numberOfDecimal, Context context) {

		double mDoubleValue = doubleValue;
		int mNumberOfDecimal = numberOfDecimal;
		Context mContext = context;

		String sAfter;

		Configuration config = mContext.getResources().getConfiguration();

		// Locale localUk = Locale.UK;
		Locale local = config.locale;

		/*
		 * DecimalFormat format = (DecimalFormat)
		 * NumberFormat.getNumberInstance(Locale.FRANCE);
		 */

		DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(local);

		switch (mNumberOfDecimal) {

		case 0:
			format.applyPattern("###,###,###,###,###,##0");
			break;

		case 1:
			format.applyPattern("###,###,###,###,###,##0.0");
			break;

		case 2:
			format.applyPattern("###,###,###,###,###,##0.00");
			break;

		case 3:
			format.applyPattern("###,###,###,###,###,##0.000");
			break;
		}

		sAfter = format.format(mDoubleValue);
		return sAfter;
	}
	
	public static double convertToDoubleValueBaseOnLocale(String stringValue, Context context) {

		double mDoubleValue = 0d;
		Context mContext = context;

		String sBefore = stringValue;

		Configuration config = mContext.getResources().getConfiguration();

		// Locale localUk = Locale.UK;
		Locale local = config.locale;
		
		NumberFormat nf = NumberFormat.getInstance(local);
		try {
			mDoubleValue = nf.parse(sBefore).doubleValue();
		} catch (ParseException e) {
			mDoubleValue = 0d;
		}
		
		return mDoubleValue;
	}

	public static double convertToDoubleValue(double doubleValue, int numberOfDecimal) {

		double mDoubleValue = doubleValue;
		int mNumberOfDecimal = numberOfDecimal;

		double dAfter = mDoubleValue;

		switch (mNumberOfDecimal) {

		case 0:

			// input : 12345.12344
			dAfter = Math.round(doubleValue);
			// Output: 12345
			break;

		case 1:

			// input : 12345.12344
			dAfter = Math.round(doubleValue * 10.0) / 10.0;
			// Output: 12345.1
			break;

		case 2:

			// input : 12345.12344
			dAfter = Math.round(doubleValue * 100.0) / 100.0;
			// Output: 12345.12
			break;

		case 3:
			// input : 12345.12344
			dAfter = Math.round(doubleValue * 1000.0) / 1000.0;
			// Output: 12345.123
			break;

		case 4:
			// input : 12345.12344
			dAfter = Math.round(doubleValue * 10000.0) / 10000.0;
			// Output: 12345.1234
			break;
		}

		return dAfter;

	}
	
	@SuppressLint("DefaultLocale")
	public static String convertToStringValue(Locale locale, double doubleValue, int numberOfDecimal) {
		
		String s = null;
		
		Locale mLocal = locale;

		switch (numberOfDecimal) {

		case 0:
			s = String.format(mLocal, "%.0f", doubleValue);
			break;
		case 1:
			s = String.format(mLocal, "%.1f", doubleValue);
			break;
		case 2:
			s = String.format(mLocal, "%.2f", doubleValue);
			break;
		case 3:
			s = String.format(mLocal, "%.3f", doubleValue);
			break;
		case 4:
			s = String.format(mLocal, "%.4f", doubleValue);
			break;
		case 5:
			s = String.format(mLocal, "%.5f", doubleValue);
			break;
		default: s = String.format(mLocal, "%.5f", doubleValue);
		}
		return s;
		}
}
