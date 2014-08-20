package com.handyapps.stocktracker.utils;

public class MyTimeFormat {

	public static String convertIntToStringTime(int hourOfDay, int minute) {
		String sHourOfDay = Integer.toString(hourOfDay);
		String amPm = "AM";
		if (hourOfDay >= 12)
			amPm = "PM";
		switch (hourOfDay) {
		case 0:
			sHourOfDay = "12";
			break;
		case 13: 
			sHourOfDay = "1";
			break;
		case 14:
			sHourOfDay = "2";
			break;
		case 15: 
			sHourOfDay = "3";
			break;
		case 16:
			sHourOfDay = "4";
			break;
		case 17: 
			sHourOfDay = "5";
			break;
		case 18:
			sHourOfDay = "6";
			break;
		case 19: 
			sHourOfDay = "7";
			break;
		case 20:
			sHourOfDay = "8";
			break;
		case 21: 
			sHourOfDay = "9";
			break;
		case 22:
			sHourOfDay = "10";
			break;
		case 23: 
			sHourOfDay = "11";
			break;
		}

		String sMin = Integer.toString(minute);
		switch (minute) {
		case 0:
			sMin = "00";
			break;
		case 1:
			sMin = "01";
			break;
		case 2:
			sMin = "02";
			break;
		case 3:
			sMin = "03";
			break;
		case 4:
			sMin = "04";
			break;
		case 5:
			sMin = "05";
			break;
		case 6:
			sMin = "06";
			break;
		case 7:
			sMin = "07";
			break;
		case 8:
			sMin = "08";
			break;
		case 9:
			sMin = "09";
			break;
		}
		
		//sMin = String.format("%02d", minute);
		//sHourOfDay = String.format("%02d", hourOfDay);
		return sHourOfDay + ":" + sMin + " " + amPm;
	}

}
