package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.utils.TextColorPicker;

public class WidgetProviderWatchlistNoHc extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_WATCHLIST_NO_HC = "APP_WIDGET_UPDATE_WATCHLIST_NO_HC";
	public static final String APP_WIDGET_ID_WATCHLIST = WidgetProviderWatchlist.APP_WIDGET_ID_WATCHLIST;
	public static final String APP_WIDGET_STATUS_OLD_WATCHLIST_NO_HC = WidgetProviderWatchlist.APP_WIDGET_STATUS_OLD_WATCHLIST;

	public static final int dividerID[] = { R.id.pager_indicator_watch_1, R.id.pager_indicator_watch_2, R.id.pager_indicator_watch_3,
		R.id.pager_indicator_watch_4, R.id.pager_indicator_watch_5};
	public static final int rowID[] = { R.id.row_body_single_watchlist_1, R.id.row_body_single_watchlist_2, R.id.row_body_single_watchlist_3,
		R.id.row_body_single_watchlist_4, R.id.row_body_single_watchlist_5 };
	public static final int nameID[] = { R.id.tv_name_1, R.id.tv_name_2, R.id.tv_name_3, R.id.tv_name_4, R.id.tv_name_5 };
	public static final int symWithExchID[] = { R.id.tv_symbol_with_exch_1, R.id.tv_symbol_with_exch_2, R.id.tv_symbol_with_exch_3, R.id.tv_symbol_with_exch_4,
			R.id.tv_symbol_with_exch_5 };
	public static final int lastTradePriceID[] = { R.id.tv_last_trade_price_1, R.id.tv_last_trade_price_2, R.id.tv_last_trade_price_3,
			R.id.tv_last_trade_price_4, R.id.tv_last_trade_price_5 };
	public static final int changesID[] = { R.id.tv_changes_1, R.id.tv_changes_2, R.id.tv_changes_3, R.id.tv_changes_4, R.id.tv_changes_5 };
	public static final int colorsID[] = { R.id.tv_color_1, R.id.tv_color_2, R.id.tv_color_3, R.id.tv_color_4, R.id.tv_color_5 };
	
	private static SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i], false, false, false);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			WidgetUtils.deleteTitlePrefWatchlist(context, appWidgetIds[i]);
		}
	}

	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
			boolean isRefreshButtonPressed, boolean isLeftPressed, boolean isRightPressed) {

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
		} else if (isRefreshButtonPressed) {
			watchlistId = sp.getInt(Constants.KEY_WATCHLIST_ID, 0);
			Log.d("watchId", "refreshIsPressedCurrentId="+watchlistId);
		} else
			watchlistId = WidgetUtils.loadIntPrefWatchlist(mContext, mAppWidgetId);
		
		theme = WidgetUtils.loadWatchlistTheme(mContext, mAppWidgetId);
		RemoteViews rv = null;
		if (theme.equals("Dark"))
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_watchlist_no_hc_dark);
		else
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_watchlist_no_hc);
		
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
				final Intent iChangeWatchlist = new Intent(mContext, WidgetConfigPrefsActivityWatchlistNoHc.class);
				iChangeWatchlist.putExtra(APP_WIDGET_STATUS_OLD_WATCHLIST_NO_HC, true);
				iChangeWatchlist.putExtra(Constants.KEY_WATCHLIST_NAME, watchName);
				iChangeWatchlist.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				iChangeWatchlist.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				final PendingIntent piChangeWatchlist = PendingIntent.getActivity(mContext, mAppWidgetId, iChangeWatchlist,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.appwidget_title_watch, piChangeWatchlist);

				// 2. ListView setup:
				final List<WatchlistStockObject> wsList = DbAdapter.getSingleInstance().fetchWatchStockListByWatchId(watchlistId);
				if (wsList.size() > 0) {

					// **Reset Ui
					rv.setViewVisibility(R.id.layout_listview_watch_items, View.VISIBLE);
					rv.setViewVisibility(R.id.empty_widget_watchlist, View.GONE);
					// **Reset Ui

					// **New Ui:
					final int mCount = wsList.size();
					for (int i = 0; i < 5; i++) {
						if (i < mCount) {

							final int mStockId = wsList.get(i).getStockId();
							final StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);

							if (so != null) {

								final String mSymbol = so.getSymbol();
								final String mCompanyName = so.getName();
								
								// 0.set color:
								rv.setInt(colorsID[i], "setBackgroundColor", so.getColorCode());

								// 1.set company:
								rv.setTextViewText(nameID[i], mCompanyName);								

								// 2.set symbol with exchange:
								rv.setTextViewText(symWithExchID[i], mSymbol + ":" + so.getExchDisp());								

								// 3.set last trade price and change:
								final QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(mSymbol);
								if (quote != null) {
									final String mLastTradePrice = quote.getLastTradePrice();
									final String mChange = quote.getChange();
									final String mChangeInPercent = quote.getChangeInPercent();
									final String mChanges = mChange + "(" + mChangeInPercent + ")";

									if (mChanges.contains("-")) {
										final Spanned spannedChanges = TextColorPicker.getRedText("", mChanges);
										rv.setTextViewText(changesID[i], spannedChanges);
									} else {
										final Spanned spannedChanges = TextColorPicker.getGreenText("", mChanges);
										rv.setTextViewText(changesID[i], spannedChanges);
									}
									
									rv.setTextViewText(lastTradePriceID[i], mLastTradePrice);
								}
							}
						} else {
							rv.setViewVisibility(dividerID[i], View. INVISIBLE);
							rv.setViewVisibility(rowID[i], View. INVISIBLE);
						}
					}
				} else {
					rv.setViewVisibility(R.id.layout_listview_watch_items, View.GONE);
					rv.setViewVisibility(R.id.empty_widget_watchlist, View.VISIBLE);
				}

				// 3.refresh:(onClicked:refresh_icon)
				final Intent iQuoteUpdates = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iQuoteUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iQuoteUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST_NO_HC, true);
				iQuoteUpdates.putExtra(Constants.KEY_WATCHLIST_ID, watchlistId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);
				sp.edit().putInt(Constants.KEY_WATCHLIST_ID, watchlistId).commit();
				final PendingIntent piQuoteUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId + 3, iQuoteUpdates,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_update_widget_watch, piQuoteUpdates);

				final Intent iViewWatchlist = new Intent(mContext, MainFragmentActivity.class);				
				iViewWatchlist.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piViewWatchlist = PendingIntent.getActivity(mContext, mAppWidgetId, iViewWatchlist,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.layout_listview_watch, piViewWatchlist);
			} else {
				final String watchNameNa = mContext.getResources().getString(R.string.watchlist_na);
				final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
				rv.setTextViewText(R.id.appwidget_title_port, watchNameNa);
				rv.setTextViewText(R.id.appwidget_last_update_watch, lastUpdateNa);
			}
		}

		// 4.search:(onClicked:search_icon)
		final Intent iSearch = new Intent(mContext, MainFragmentActivity.class);
		iSearch.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
		iSearch.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
		iSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent piSearch = PendingIntent.getActivity(mContext, mAppWidgetId, iSearch, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_watch, piSearch);
		rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_watchlist, piSearch);
		
		// 5.previous:(onClicked:btn_left_watch)
		int prevWatchId = WidgetUtils.loadPrevWatch(mContext, mAppWidgetId, watchlistId);	
		if (prevWatchId > 0) {
			Log.d("watchId", "prevWatchId="+prevWatchId);
			final Intent iLeftWatch = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
			iLeftWatch.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iLeftWatch.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iLeftWatch.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST_NO_HC, true);
			iLeftWatch.putExtra(Constants.KEY_WATCHLIST_LEFT_ID, prevWatchId);
			iLeftWatch.putExtra(Constants.KEY_IS_WIDGET_LEFT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_WATCHLIST_LEFT_ID, prevWatchId).commit();
			final PendingIntent piLeftWatch = PendingIntent.getBroadcast(mContext, mAppWidgetId + 4, iLeftWatch, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_left_watch, piLeftWatch);
		}

		// 6.next:(onClicked:btn_right_watch)
		int nextWatchId = WidgetUtils.loadNextWatch(mContext, mAppWidgetId, watchlistId);	
		if (nextWatchId > 0) {
			Log.d("watchId", "nextWatchId="+nextWatchId);
			final Intent iRightWatch = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
			iRightWatch.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iRightWatch.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iRightWatch.putExtra(Constants.KEY_IS_WIDGET_WATCHLIST_NO_HC, true);
			iRightWatch.putExtra(Constants.KEY_WATCHLIST_RIGHT_ID, nextWatchId);
			iRightWatch.putExtra(Constants.KEY_IS_WIDGET_RIGHT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_WATCHLIST_RIGHT_ID, nextWatchId).commit();
			final PendingIntent piRightWatch = PendingIntent.getBroadcast(mContext, mAppWidgetId + 5, iRightWatch, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_right_watch, piRightWatch);
		}

		// 7.update:progressbar:
		if (mIsRefreshBtnPressed) {
			rv.setViewVisibility(R.id.layout_pb, View.VISIBLE);
			rv.setViewVisibility(R.id.btn_update_widget_watch, View.GONE);
		} else {
			rv.setViewVisibility(R.id.layout_pb, View.GONE);
			rv.setViewVisibility(R.id.btn_update_widget_watch, View.VISIBLE);
		}
		//Log.d("No_HC_widget_watchlist", "widget_id:" + String.valueOf(mAppWidgetId));

		// 6.call widget manager do update widget.
		mAppWidgetManager.updateAppWidget(mAppWidgetId, rv);
	}
}
