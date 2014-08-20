package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.utils.VersionHelper;

public class WidgetProviderNews extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_NEWS = "AppWidgetUpdateNews";
	public static final String APP_WIDGET_ID_NEWS = "WidgetIDNews";
	public static final String APP_WIDGET_STATUS_OLD_NEWS = "WidgetStatusNews";	

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!VersionHelper.isHoneyComb()) {
			return;
		}

		final String intentAction = intent.getAction();
		if (intentAction.equals(APP_WIDGET_UPDATE_NEWS)) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context, WidgetProviderNews.class);

			mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.lv_widget_news_list);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i], false);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@SuppressWarnings("deprecation")
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, 
			boolean isRefreshButtonPressed) {

		if (!VersionHelper.isHoneyComb()) {
			return;
		}

		final Context mContext = context;
		final int mAppWidgetId = appWidgetId;
		final boolean mIsRefreshBtnPressed = isRefreshButtonPressed;
		final AppWidgetManager mAppWidgetManager = appWidgetManager;

		final Intent intent = new Intent(mContext, WidgetServiceNews.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);		
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_news);
		rv.setRemoteAdapter(mAppWidgetId, R.id.lv_widget_news_list, intent);
		rv.setEmptyView(R.id.lv_widget_news_list, R.id.empty_widget_newslist);	

		long count = DbAdapter.getSingleInstance().countStockList();
			
		if (count > 0) {
			
			// 1.Set last updated time
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a");
			Date now = new Date();
			String strDate = sdf.format(now);
			rv.setTextViewText(R.id.appwidget_last_update_news, "Last updated: " + strDate);

			// 2. setup listview_with_intent template:(onClick row_body with
			// intent, which intent is setup by RemoteViewsFactory)
			final Intent iView = new Intent(mContext, TransactionDetailsFragmentActivity.class);
			iView.putExtra(Constants.KEY_TAB_POSITION, TransactionDetailsFragmentActivity.TAB_POSITION_NEWS);			
			iView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			final PendingIntent piRowBodyClicked = PendingIntent.getActivity(mContext, mAppWidgetId, iView, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setPendingIntentTemplate(R.id.lv_widget_news_list, piRowBodyClicked);

			// 3.refresh:(onClicked:refresh_icon)
			final Intent iNewsUpdates = new Intent(mContext, WidgetReceiverNewsUpdate.class);
			iNewsUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_NEWS_UPDATE);
			iNewsUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);				
			iNewsUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);		
			iNewsUpdates.putExtra(Constants.KEY_IS_WIDGET_NEWS, true);
			final PendingIntent piNewsUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId + 6, iNewsUpdates,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_update_widget_news, piNewsUpdates);
		} else {				
			final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
			rv.setTextViewText(R.id.appwidget_last_update_news, lastUpdateNa);
		}		

		// 4.search:(onClicked:search_icon)
		final Intent iMainActivity = new Intent(mContext, MainFragmentActivity.class);
		iMainActivity.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
		iMainActivity.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
		iMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent pi = PendingIntent.getActivity(mContext, mAppWidgetId, iMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_news, pi);
		rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_news, pi);

		if (mIsRefreshBtnPressed) {
			rv.setViewVisibility(R.id.layout_pb, View.VISIBLE);
			rv.setViewVisibility(R.id.btn_update_widget_news, View.GONE);
		} else {
			rv.setViewVisibility(R.id.layout_pb, View.GONE);
			rv.setViewVisibility(R.id.btn_update_widget_news, View.VISIBLE);
		}
	
		// 5.call widget manager do update widget.
		mAppWidgetManager.updateAppWidget(mAppWidgetId, rv);
	}
}
