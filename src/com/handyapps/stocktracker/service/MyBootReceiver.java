package com.handyapps.stocktracker.service;

import com.handyapps.stocktracker.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String mAction = intent.getAction();
		if (mAction.equals(Constants.ACTION_BOOT_COMPLETED) || mAction.equals(Constants.ACTION_MY_BOOT_RECEIVER)) {
			startMyAlarmSettings(context);
		}
	}

	private void startMyAlarmSettings(Context context) {
		Context mCtx = context;
		MyAlarmManager mAlarm = new MyAlarmManager(mCtx);
		mAlarm.setAlarm();
	}
}