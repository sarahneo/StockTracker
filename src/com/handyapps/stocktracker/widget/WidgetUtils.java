package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.WatchlistObject;

public class WidgetUtils {
	
	public static int loadPrevPort(Context context, int appWidgetId, int currId) {
		int prevPortId = 0;
				
		List<PortfolioObject> poList = DbAdapter.getSingleInstance().fetchPortfolioList();
		List<Integer> intList = new ArrayList<Integer>();
		for (PortfolioObject po : poList) {
			intList.add(po.getId());			
		}
		int index = intList.indexOf(currId);
		if (index > 0) {
			prevPortId = poList.get(index-1).getId();			
			if (prevPortId > 0)
				return prevPortId;
		}
		
		return -1;
	}
	
	public static int loadPrevWatch(Context context, int appWidgetId, int currId) {
		int prevWatchId = 0;
				
		List<WatchlistObject> woList = DbAdapter.getSingleInstance().fetchWatchlists();
		List<Integer> intList = new ArrayList<Integer>();
		for (WatchlistObject wo : woList) {
			intList.add(wo.getId());			
		}
		int index = intList.indexOf(currId);
		if (index > 0) {
			prevWatchId = woList.get(index-1).getId();			
			if (prevWatchId > 0)
				return prevWatchId;
		}
		
		return -1;
	}
	
	public static int loadNextPort(Context context, int appWidgetId, int currId) {
		int nextPortId = 0;
		Log.d("portId", "currId="+currId);
		
		List<PortfolioObject> poList = DbAdapter.getSingleInstance().fetchPortfolioList();
		List<Integer> intList = new ArrayList<Integer>();
		for (PortfolioObject po : poList) {
			intList.add(po.getId());			
		}
		int index = intList.indexOf(currId);
		if (index >= 0 && index < intList.size()-1) {
			nextPortId = poList.get(index+1).getId();
			if (nextPortId > 0)
				return nextPortId;
		}
		
		return -1;
	}
	
	public static int loadNextWatch(Context context, int appWidgetId, int currId) {
		int nextWatchId = 0;
		
		List<WatchlistObject> woList = DbAdapter.getSingleInstance().fetchWatchlists();
		List<Integer> intList = new ArrayList<Integer>();
		for (WatchlistObject wo : woList) {
			intList.add(wo.getId());			
		}
		int index = intList.indexOf(currId);
		if (index >= 0 && index < intList.size()-1) {
			nextWatchId = woList.get(index+1).getId();
			if (nextWatchId > 0)
				return nextWatchId;
		}
		
		return -1;
	}

	public static int loadIntPrefPortfolio(Context context, int appWidgetId) {
		Context mCtx = context;
		int mAppWidgetId = appWidgetId;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);

		String prefix = prefs.getString((Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET + mAppWidgetId), null);
		int mPortId = 0;
		if (prefix != null) {
			try {
				mPortId = Integer.parseInt(prefix);
			} catch (NumberFormatException e) {
				return -1;
			}
			return mPortId;
		}
		return -1;
	}

	public static int loadIntPrefWatchlist(Context context, int appWidgetId) {
		Context mCtx = context;
		int mAppWidgetId = appWidgetId;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		String prefix = prefs.getString((Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET + mAppWidgetId), null);
		int mWatchId = 0;
		if (prefix != null) {
			try {
				mWatchId = Integer.parseInt(prefix);
			} catch (NumberFormatException e) {
				return -1;
			}
			return mWatchId;
		}
		return -1;

	}
	
	public static String loadWatchlistTheme(Context context, int appWidgetId) {
		Context mCtx = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		String theme = prefs.getString((Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET_THEME), null);
		if (theme == null || theme.length() <= 0) {
			theme = "Light";
		}
		return theme;

	}
	
	public static String loadPortfolioTheme(Context context, int appWidgetId) {
		Context mCtx = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		String theme = prefs.getString((Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME), null);
		if (theme == null || theme.length() <= 0) {
			theme = "Light";
		}
		return theme;

	}

	public static void deleteTitlePrefPortfolio(Context context, int appWidgetId) {
		Context mCtx = context;
		int mAppWidgetId = appWidgetId;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		prefs.edit().remove((Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET + mAppWidgetId)).commit();
	}

	public static void deleteTitlePrefWatchlist(Context context, int appWidgetId) {
		Context mCtx = context;
		int mAppWidgetId = appWidgetId;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		prefs.edit().remove((Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET + mAppWidgetId)).commit();
	}

	public static void updateWidget(Context context) {
		Context mCtx = context;
		Intent i = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
		mCtx.startService(i);
	}
}
