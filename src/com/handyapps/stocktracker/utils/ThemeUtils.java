package com.handyapps.stocktracker.utils;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;

public class ThemeUtils {

	private static int cTheme;
	public final static int LIGHT = 0;
	public final static int DARK = 1;
	public final static int STOCKS_ACTIVITY_LIGHT = 2;
	public final static int STOCKS_ACTIVITY_DARK = 3;
	
	private static SharedPreferences sp;

	public static void changeToTheme(Activity activity, int theme) {
		cTheme = theme;
		Intent intent = new Intent(activity, activity.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.finish();
		activity.startActivity(intent);
	}

	public static void onActivityCreateSetTheme(Activity activity, boolean isStocks) {
		
		sp = PreferenceManager.getDefaultSharedPreferences(activity);
		
		String[] arrThemeValues = activity.getResources().getStringArray(R.array.sp_app_theme_entries);
		String sAppTheme = sp.getString(Constants.SP_KEY_APP_THEME, arrThemeValues[0]);
		
		int lightBg = activity.getResources().getColor(R.color.app_background_light);
		int darkBg = activity.getResources().getColor(R.color.app_background);
		
		boolean isLight = true;
		
		if (sAppTheme.equals(arrThemeValues[0]) && !isStocks) 
			cTheme = ThemeUtils.LIGHT;
		else if (sAppTheme.equals(arrThemeValues[1]) && !isStocks) 
			cTheme = ThemeUtils.DARK;
		else if (sAppTheme.equals(arrThemeValues[0]) && isStocks) 
			cTheme = ThemeUtils.STOCKS_ACTIVITY_LIGHT;
		else if (sAppTheme.equals(arrThemeValues[1]) && isStocks)
			cTheme = ThemeUtils.STOCKS_ACTIVITY_DARK;
		

		switch (cTheme) {
		default:
			break;
		case LIGHT:
			activity.setTheme(R.style.Theme_MainActivityLight);
			isLight = true;
			break;
		case DARK:
			activity.setTheme(R.style.Theme_MainActivity);
			isLight = false;
			break;
		case STOCKS_ACTIVITY_LIGHT:
			activity.setTheme(R.style.Theme_StocksActivityLight);
			isLight = true;
			break;
		case STOCKS_ACTIVITY_DARK:
			activity.setTheme(R.style.Theme_StocksActivity);
			isLight = false;
			break;
		}
		
		activity.getWindow().setBackgroundDrawable(new ColorDrawable(isLight ? lightBg : darkBg));

	}

}