package com.handyapps.stocktracker.service;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.handyapps.stocktracker.Constants;

public class ServiceUtils {

	public static void singleQuoteUpdate(String symbol, Context context) {
		String mSymbol = symbol;
		Context mCtx = context;
		Intent i = new Intent(Constants.ACTION_INTENT_SERVICE_UPDATE_QUOTE);// IntentServiceUpdateQuote.class
		i.putExtra(Constants.KEY_SYMBOL, mSymbol);
		i.putExtra(Constants.KEY_IS_UPDATE_QUOTE_ALL_TICKERS, false);
		mCtx.startService(i);
	}

	public static boolean isServiceRunning(Context context, String serviceClassName) {

		Context mCtx = context;
		String mServiceClassName = serviceClassName;

		ActivityManager activityManager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
		List<android.app.ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

		for (android.app.ActivityManager.RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(mServiceClassName)) {
				return true;
			}
		}
		return false;
	}
}
