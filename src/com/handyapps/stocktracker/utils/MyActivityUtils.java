package com.handyapps.stocktracker.utils;

import com.handyapps.stocktracker.activity.MainFragmentActivity;

import android.content.Context;
import android.content.Intent;

public class MyActivityUtils {
	public static void backToHome(Context context){
		Context mCtx = context;
		Intent iHome = new Intent(mCtx, MainFragmentActivity.class);
		iHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(iHome);
	}
}
