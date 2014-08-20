package com.handyapps.stocktracker.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class OrientationUtils{
	public static void unlockOrientation(Activity activity){
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public static void lockOrientation(Activity activity){
		int currentOrientation = activity.getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
    
	public static int getCurrentOrientation(Activity mContext){
		return mContext.getResources().getConfiguration().orientation;
	}
    
	public static boolean isLandScaple(Activity mContext){
		return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
    
	public static boolean isPotrait(Activity mContext){
		return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
    
	
}
