package com.handyapps.stocktracker.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class VersionHelper {
	
	public static boolean isHoneyComb(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			return true; 
		}
		
		return false; 
	}
	
	public static String getVersionName(Context ctx){
		String versionName = null;
		try{
		   versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;		
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}
	
	
	public static Integer getVersionCode(Context ctx){
		Integer versionCode = null;
		try{
		   versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;		
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

}
