package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.utils.VersionHelper;
import com.handyapps.stocktracker.widget.WidgetServiceWatchlist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProviderWatchlist extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_WATCHLIST = "AppWidgetUpdateWatchlist";
	public static final String APP_WIDGET_ID_WATCHLIST = "WidgetIDWatchlist";
	public static final String APP_WIDGET_STATUS_OLD_WATCHLIST = "WidgetStatusWatchlist";
	
	private static SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!VersionHelper.isHoneyComb()) {
			return;
		}

		final String intentAction = intent.getAction();
		if (intentAction.equals(APP_WIDGET_UPDATE_WATCHLIST)) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context, WidgetProviderWatchlist.class);

			mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.lv_widget_watchlist);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i], false, false, false);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			WidgetUtils.deleteTitlePrefWatchlist(context, appWidgetIds[i]);
		}
	}

	@SuppressWarnings("deprecation")
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
			boolean isRefreshButtonPressed, boolean isLeftPressed, boolean isRightPressed) {

		if (!VersionHelper.isHoneyComb()) {
			return;
		}

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		final Context mContext = context;
		final int mAppWidgetId = appWidgetId;
		final boolean mIsRefreshBtnPressed = isRefreshButtonPressed;
		final AppWidgetManager mAppWidgetManager = appWidgetManager;
		
		int watchlistId = 0;
		String theme = "Light";
		if (isLeftPressed) {
			watchlistId = sp.getInt(Constants.KEY_WATCHLIST_LEFT_ID, 1);
			Log.d("watchId", "leftIsPressedCurrentId="+watchlistId);
		} else if (isRightPressed) {
			watchlistId = sp.getInt(Constants.KEY_WATCHLIST_RIGHT_ID, 1);
			Log.d("watchId", "rightIsPressedCurrentId="+watchlistId);
		} else if (isRefreshButtonPressed)
			watchlistId = sp.getInt(Constants.KEY_WATCHLIST_ID, 0);
		else
			watchlistId = WidgetUtils.loadIntPrefWatchlist(mContext, mAppWidgetId);
		
		theme = WidgetUtils.loadWatchlistTheme(mContext, mAppWidgetId);

		final Intent intent = new Intent(mContext, WidgetServiceWatchlist.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		intent.putExtra(Constants.KEY_WATCHLIST_ID, watchlistId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		RemoteViews rv = null;
		if (theme.equals("Dark"))
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_watchlist_dark);
		else
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_watchlist);
		
		rv.setRemoteAdapter(mAppWidgetId, R.id.lv_widget_watchlist, intent);
		rv.setEmptyView(R.id.lv_widget_watchlist, R.id.empty_widget_watchlist);	

		if (!(watchlistId == -1)) {
			// 1. get watch name;
			final WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByWatchId(watchlistId);
			if (wo != null) {
				final String watchName = wo.getName();
				rv.setTextViewText(R.id.appwidget_title_watch, watchName);
				
				// Set last updated time
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a");
				Date now = new Date();
				String strDate = sdf.format(now);
				rv.setTextViewText(R.id.appwidget_last_update_watch, "Last updated: " + strDate);

				// 1. view single watchlist:(onClicked:watchlist_title_name)
				final Intent iChangeWatchlist = new Intent(mContext, WidgetConfigPrefsActivityWatchlist.class);
				iChangeWatchlist.putExtra(APP_WIDGET_STATUS_OLD_WATCHLIST, true);
				iChangeWatchlist.putExtra(Constants.KEY_WATCHLIST_NAME, watchName);
				iChangeWatchlist.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				iChangeWatchlist.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piChangeWatchlist = PendingIntent.getActivity(mContext, mAppWidgetId, iChangeWatchlist,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.appwidget_title_watch, piChangeWatchlist);

				// 2. setup listview_with_intent template:(onClick row_body with
				// intent, which intent is setup by RemoteViewsFactory)
				final Intent iView = new Intent(mContext, TransactionDetailsFragmentActivity.class);
				iView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piRowBodyClicked = PendingIntent.getActivity(mContext, mAppWidgetId, iView, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setPendingIntentTemplate(R.id.lv_widget_watchlist, piRowBodyClicked);

				// 3.refresh:(onClicked:refresh_icon)
				final Intent iQuoteUpdates = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iQuoteUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iQuoteUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST, true);
				iQuoteUpdates.putExtra(Constants.KEY_WATCHLIST_ID, watchlistId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);
				sp.edit().putInt(Constants.KEY_WATCHLIST_ID, watchlistId).commit();
				final PendingIntent piQuoteUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId + 6, iQuoteUpdates,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_update_widget_watch, piQuoteUpdates);
			} else {
				final String watchNameNa = mContext.getResources().getString(R.string.watchlist_na);
				final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
				rv.setTextViewText(R.id.appwidget_title_watch, watchNameNa);
				rv.setTextViewText(R.id.appwidget_last_update_watch, lastUpdateNa);
			}
		}

		// 4.search:(onClicked:search_icon)
		final Intent iMainActivity = new Intent(mContext, MainFragmentActivity.class);
		iMainActivity.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
		iMainActivity.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
		iMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent pi = PendingIntent.getActivity(mContext, mAppWidgetId, iMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_watch, pi);
		rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_watchlist, pi);

		if (mIsRefreshBtnPressed) {
			rv.setViewVisibility(R.id.layout_pb, View.VISIBLE);
			rv.setViewVisibility(R.id.btn_update_widget_watch, View.GONE);
		} else {
			rv.setViewVisibility(R.id.layout_pb, View.GONE);
			rv.setViewVisibility(R.id.btn_update_widget_watch, View.VISIBLE);
		}
		
		// 5.previous:(onClicked:btn_left_watch)
		int prevWatchId = WidgetUtils.loadPrevWatch(mContext, mAppWidgetId, watchlistId);	
		if (prevWatchId > 0) {
			Log.d("watchId", "prevWatchId="+prevWatchId);
			final Intent iLeftWatch = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
			iLeftWatch.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iLeftWatch.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iLeftWatch.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST, true);
			iLeftWatch.putExtra(Constants.KEY_WATCHLIST_LEFT_ID, prevWatchId);
			iLeftWatch.putExtra(Constants.KEY_IS_WIDGET_LEFT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_WATCHLIST_LEFT_ID, prevWatchId).commit();
			final PendingIntent piLeftWatch = PendingIntent.getBroadcast(mContext, mAppWidgetId + 7, iLeftWatch, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_left_watch, piLeftWatch);
		}

		// 6.next:(onClicked:btn_right_watch)
		int nextWatchId = WidgetUtils.loadNextWatch(mContext, mAppWidgetId, watchlistId);	
		if (nextWatchId > 0) {
			Log.d("watchId", "nextWatchId="+nextWatchId);
			final Intent iRightWatch = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
			iRightWatch.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iRightWatch.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iRightWatch.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST, true);
			iRightWatch.putExtra(Constants.KEY_WATCHLIST_RIGHT_ID, nextWatchId);
			iRightWatch.putExtra(Constants.KEY_IS_WIDGET_RIGHT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_WATCHLIST_RIGHT_ID, nextWatchId).commit();
			final PendingIntent piRightWatch = PendingIntent.getBroadcast(mContext, mAppWidgetId + 8, iRightWatch, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_right_watch, piRightWatch);
		}

		// 7.call widget manager do update widget.
		mAppWidgetManager.updateAppWidget(mAppWidgetId, rv);
	}
}
