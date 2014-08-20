package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.StockObject;

public class WidgetProviderNewsNoHc extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_NEWS_NO_HC = "APP_WIDGET_UPDATE_NEWS_NO_HC";
	public static final String APP_WIDGET_ID_NEWS = WidgetProviderNews.APP_WIDGET_ID_NEWS;
	public static final String APP_WIDGET_STATUS_OLD_NEWS_NO_HC = WidgetProviderNews.APP_WIDGET_STATUS_OLD_NEWS;

	public static final int nameID[] = { R.id.tv_company_news_1, R.id.tv_company_news_2, R.id.tv_company_news_3, R.id.tv_company_news_4, R.id.tv_company_news_5,
		R.id.tv_company_news_6};
	public static final int headlineID[] = { R.id.tv_news_headline_1, R.id.tv_news_headline_2, R.id.tv_news_headline_3, R.id.tv_news_headline_4,
			R.id.tv_news_headline_5, R.id.tv_news_headline_6 };

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i], false);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
			boolean isRefreshButtonPressed) {

		final Context mContext = context;
		final int mAppWidgetId = appWidgetId;
		final boolean mIsRefreshBtnPressed = isRefreshButtonPressed;
		final AppWidgetManager mAppWidgetManager = appWidgetManager;

		final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_news_no_hc);
		long count = DbAdapter.getSingleInstance().countStockList();

		if (count > 1) {
		
			// Set last updated time
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a");
			Date now = new Date();
			String strDate = sdf.format(now);
			rv.setTextViewText(R.id.appwidget_last_update_watch, "Last updated: " + strDate);

			// 2. ListView setup:
			final List<NewsObject> nList = DbAdapter.getSingleInstance().fetchNewsList();
			if (nList.size() > 0) {

				// **Reset Ui
				rv.setViewVisibility(R.id.layout_listview_news_items, View.VISIBLE);
				rv.setViewVisibility(R.id.empty_widget_news, View.GONE);
				// **Reset Ui

				// **New Ui:
				final int mCount = nList.size();
				for (int i = 0; i < 6; i++) {
					if (i < mCount) {

						final String mSymbol = nList.get(i).getSymbol();
						String mCompanyName = "";
						final StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);

						if (so != null) {
							
							if (i % 2 == 0) { // even
								mCompanyName = so.getName();					
								// 0. set color:
								rv.setInt(nameID[i], "setBackgroundColor", so.getColorCode());
								
								// 1.set company:
								rv.setTextViewText(nameID[i], mCompanyName + " News");
								rv.setViewVisibility(nameID[i], View.VISIBLE);
							} else {
								rv.setViewVisibility(nameID[i], View.GONE);
							}

							// 2.set headline:
							rv.setTextViewText(headlineID[i], nList.get(i).getTitle());
						}
					} else {
						rv.setInt(nameID[i], "setBackgroundColor", android.R.color.white);
						rv.setTextViewText(nameID[i], "");
						rv.setTextViewText(headlineID[i], "");
					}
				}
			} else {
				rv.setViewVisibility(R.id.layout_listview_news_items, View.GONE);
				rv.setViewVisibility(R.id.empty_widget_news, View.VISIBLE);
			}

			// 3.refresh:(onClicked:refresh_icon)
			final Intent iNewsUpdates = new Intent(mContext, WidgetReceiverNewsUpdate.class);
			iNewsUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_NEWS_UPDATE);
			iNewsUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);				
			iNewsUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);		
			iNewsUpdates.putExtra(Constants.KEY_IS_WIDGET_NEWS_NO_HC, true);
			final PendingIntent piNewsUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId + 15, iNewsUpdates,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_update_widget_news, piNewsUpdates);

			final Intent iViewNewslist = new Intent(mContext, MainFragmentActivity.class);		
			iViewNewslist.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_NEWS-1);
			iViewNewslist.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			final PendingIntent piViewNewslist = PendingIntent.getActivity(mContext, mAppWidgetId, iViewNewslist,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.layout_listview_news, piViewNewslist);
		} else {
			final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
			rv.setTextViewText(R.id.appwidget_last_update_watch, lastUpdateNa);
			rv.setViewVisibility(R.id.layout_listview_news_items, View.GONE);
			rv.setViewVisibility(R.id.empty_widget_news, View.VISIBLE);
		}

		// 4.search:(onClicked:search_icon)
		final Intent iSearch = new Intent(mContext, MainFragmentActivity.class);
		iSearch.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_NEWS-1);
		iSearch.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
		iSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent piSearch = PendingIntent.getActivity(mContext, mAppWidgetId, iSearch, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_news, piSearch);
		rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_news , piSearch);

		// 5.update:progressbar:
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
