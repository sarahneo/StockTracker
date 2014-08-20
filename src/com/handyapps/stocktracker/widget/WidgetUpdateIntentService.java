package com.handyapps.stocktracker.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

public class WidgetUpdateIntentService extends IntentService {
	

	public WidgetUpdateIntentService() {
		super("Widget Update Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		// **Honeycomb and above:portfolio
		ComponentName thisAppWidget = new ComponentName(this.getPackageName(), WidgetProviderPortfolio.class.getName());
		int[] widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int i : widgetIds) {
			WidgetProviderPortfolio.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), i,
					false, false, false);
			Intent updateWidgetIntent = new Intent(this.getApplicationContext(), WidgetProviderPortfolio.class);
			updateWidgetIntent.setAction(WidgetProviderPortfolio.APP_WIDGET_UPDATE_PORTFOLIO);
			sendBroadcast(updateWidgetIntent);
		}

		// **Honeycomb and above:watchlist
		thisAppWidget = new ComponentName(this.getApplicationContext().getPackageName(), WidgetProviderWatchlist.class.getName());

		widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int j : widgetIds) {
			WidgetProviderWatchlist.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), j,
					false, false, false);
			Intent updateWidgetIntent = new Intent(this.getApplicationContext(), WidgetProviderWatchlist.class);
			updateWidgetIntent.setAction(WidgetProviderWatchlist.APP_WIDGET_UPDATE_WATCHLIST);
			sendBroadcast(updateWidgetIntent);
		}
		
		// **Honeycomb and above:news
		thisAppWidget = new ComponentName(this.getApplicationContext().getPackageName(), WidgetProviderNews.class.getName());

		widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int k : widgetIds) {
			WidgetProviderNews.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), k,
					false);
			Intent updateWidgetIntent = new Intent(this.getApplicationContext(), WidgetProviderNews.class);
			updateWidgetIntent.setAction(WidgetProviderNews.APP_WIDGET_UPDATE_NEWS);
			sendBroadcast(updateWidgetIntent);
		}

		// **Below HoneyComb:portfolio
		thisAppWidget = new ComponentName(this.getApplicationContext().getPackageName(), WidgetProviderPortfolioNoHc.class.getName());
		widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int l : widgetIds) {
			WidgetProviderPortfolioNoHc.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), l,
					false, false, false);
		}

		// **Below HoneyComb:watchlist
		thisAppWidget = new ComponentName(this.getApplicationContext().getPackageName(), WidgetProviderWatchlistNoHc.class.getName());
		widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int m : widgetIds) {
			WidgetProviderWatchlistNoHc.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), m,
					false, false, false);
		}
		
		// **Below HoneyComb:news
		thisAppWidget = new ComponentName(this.getApplicationContext().getPackageName(), WidgetProviderNewsNoHc.class.getName());
		widgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		for (int n : widgetIds) {
			WidgetProviderNewsNoHc.updateAppWidget(this.getApplicationContext(), AppWidgetManager.getInstance(this.getApplicationContext()), n,
					false);
		}
	}
}
