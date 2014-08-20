package com.handyapps.stocktracker.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.utils.VersionHelper;

public class WidgetProviderPortfolio extends AppWidgetProvider {

	public static final String APP_WIDGET_UPDATE_PORTFOLIO = "AppWidgetUpdatePortfolio";
	public static final String APP_WIDGET_ID_PORTFOLIO = "WidgetIDPortfolio";
	public static final String APP_WIDGET_STATUS_OLD_PORTFOLIO = "WidgetStatusPortfolio";
	
	private static SharedPreferences sp;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onReceive(Context context, Intent intent) {

		if (!VersionHelper.isHoneyComb()) {
			return;
		}
		final String intentAction = intent.getAction();

		if (intentAction.equals(APP_WIDGET_UPDATE_PORTFOLIO)) {

			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context, WidgetProviderPortfolio.class);

			mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.lv_widget_port_list);
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
			WidgetUtils.deleteTitlePrefPortfolio(context, appWidgetIds[i]);
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
		
		int portId = 0;
		String theme = "Light";
		if (isLeftPressed) {
			portId = sp.getInt(Constants.KEY_PORTFOLIO_LEFT_ID, 1);
			Log.d("portId", "leftIsPressedCurrentId="+portId);
		} else if (isRightPressed) {
			portId = sp.getInt(Constants.KEY_PORTFOLIO_RIGHT_ID, 1);
			Log.d("portId", "rightIsPressedCurrentId="+portId);
		} else if (isRefreshButtonPressed) {
			portId = sp.getInt(Constants.KEY_PORTFOLIO_REFRESH_ID, 0);
			Log.d("portId", "bundledCurrentId="+portId);
		} else {
			portId = WidgetUtils.loadIntPrefPortfolio(mContext, mAppWidgetId);
			Log.d("portId", "widgetUtilsCurrentId="+portId);
		}
		
		theme = WidgetUtils.loadPortfolioTheme(mContext, mAppWidgetId);

		final Intent intent = new Intent(mContext, WidgetServicePortfolio.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		intent.putExtra(Constants.KEY_PORTFOLIO_ID, portId);		
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		RemoteViews rv = null;
		if (theme.equals("Dark")) {
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_portfolio_dark);
			Log.i("theme", "dark");
		} else {
			rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_portfolio);
			Log.i("theme", "light");
		}
		
		rv.setRemoteAdapter(mAppWidgetId, R.id.lv_widget_port_list, intent);
		rv.setEmptyView(R.id.lv_widget_port_list, R.id.empty_widget_portfoliolist);		

		if (portId != -1) {
			// 1. get port name;
			final PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (po != null) {
				final String portName = po.getName();
				Log.d("portId", "portName="+portName);
				rv.setTextViewText(R.id.appwidget_title_port, portName);
				
				// Set last updated time
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM h:mm a");
				Date now = new Date();
				String strDate = sdf.format(now);
				rv.setTextViewText(R.id.appwidget_last_update_port, "Last updated: " + strDate);

				// 1. view single portfolio:(onClicked:portfolio_title_name)
				final Intent iChangePortfolio = new Intent(mContext, WidgetConfigPrefsActivityPortfolio.class);
				iChangePortfolio.putExtra(APP_WIDGET_STATUS_OLD_PORTFOLIO, true);
				iChangePortfolio.putExtra(Constants.KEY_PORTFOLIO_NAME, portName);
				iChangePortfolio.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				iChangePortfolio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piChangePortfolio = PendingIntent.getActivity(mContext, mAppWidgetId, iChangePortfolio,
						PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.appwidget_title_port, piChangePortfolio);

				// 2. setup listview_with_intent template:(onClick row_body with
				// intent, which intent is setup by RemoteViewsFactory)
				final Intent iViewSummary = new Intent(mContext, TransactionDetailsFragmentActivity.class);
				iViewSummary.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				final PendingIntent piRowBodyClicked = PendingIntent.getActivity(mContext, mAppWidgetId, iViewSummary, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setPendingIntentTemplate(R.id.lv_widget_port_list, piRowBodyClicked);

				// 3.refresh:(onClicked:refresh_icon)
				// Intent iRefresh = new Intent(mContext,
				// IntentServiceUpdateQuote.class);
				final Intent iQuoteUpdates = new Intent(mContext, WidgetReceiverQuoteUpdate.class);
				iQuoteUpdates.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
				iQuoteUpdates.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO, true);
				iQuoteUpdates.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
				iQuoteUpdates.putExtra(Constants.KEY_IS_WIDGET_REFRESH_PRESSED, true);
				sp.edit().putInt(Constants.KEY_PORTFOLIO_REFRESH_ID, portId).commit();
				final PendingIntent piQuoteUpdates = PendingIntent.getBroadcast(mContext, mAppWidgetId + 9, iQuoteUpdates, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.btn_update_widget_port, piQuoteUpdates);
			}else{
				final String portNameNa = mContext.getResources().getString(R.string.portfolio_na);
				final String lastUpdateNa = mContext.getResources().getString(R.string.last_udpdated_na);
				rv.setTextViewText(R.id.appwidget_title_port, portNameNa);
				rv.setTextViewText(R.id.appwidget_last_update_port, lastUpdateNa);
			}
		}

		// 4.search:(onClicked:search_icon)
		final Intent iFindSymbol = new Intent(mContext, MainFragmentActivity.class);
		iFindSymbol.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
		iFindSymbol.putExtra(Constants.KEY_FROM, Constants.FROM_WIDGET);
		iFindSymbol.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
		iFindSymbol.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, true);
		iFindSymbol.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent piFindSymbol = PendingIntent.getActivity(mContext, mAppWidgetId, iFindSymbol, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.btn_widget_find_symbol_port, piFindSymbol);
		rv.setOnClickPendingIntent(R.id.iv_stocktracer_widget_portfolio, piFindSymbol);
		
		// 5.previous:(onClicked:btn_left_port)
		int prevPortId = WidgetUtils.loadPrevPort(mContext, mAppWidgetId, portId);
		if (prevPortId > 0) {
			Log.d("portId", "prevPortId=" + prevPortId);
			final Intent iLeftPort = new Intent(mContext,
					WidgetReceiverQuoteUpdate.class);
			iLeftPort.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iLeftPort.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iLeftPort.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO, true);
			iLeftPort.putExtra(Constants.KEY_PORTFOLIO_LEFT_ID, prevPortId);
			iLeftPort.putExtra(Constants.KEY_IS_WIDGET_LEFT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_PORTFOLIO_LEFT_ID, prevPortId).commit();
			final PendingIntent piLeftPort = PendingIntent.getBroadcast(
					mContext, mAppWidgetId + 10, iLeftPort,
					PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_left_port, piLeftPort);
		}

		// 5.next:(onClicked:btn_right_port)
		int nextPortId = WidgetUtils.loadNextPort(mContext, mAppWidgetId, portId);
		if (nextPortId > 0) {
			Log.d("portId", "nextPortId=" + nextPortId);
			final Intent iRightPort = new Intent(mContext,WidgetReceiverQuoteUpdate.class);
			iRightPort.setAction(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE);
			iRightPort.putExtra(Constants.KEY_WIDGET_ID, mAppWidgetId);
			iRightPort.putExtra(Constants.KEY_IS_WIDGET_PORTFOLIO, true);
			iRightPort.putExtra(Constants.KEY_PORTFOLIO_RIGHT_ID, prevPortId);
			iRightPort.putExtra(Constants.KEY_IS_WIDGET_RIGHT_PRESSED, true);
			sp.edit().putInt(Constants.KEY_PORTFOLIO_RIGHT_ID, nextPortId).commit();
			final PendingIntent piRightPort = PendingIntent.getBroadcast(mContext, mAppWidgetId + 11, iRightPort, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_right_port, piRightPort);
		}

		if (mIsRefreshBtnPressed) {
			rv.setViewVisibility(R.id.layout_pb, View.VISIBLE);
			rv.setViewVisibility(R.id.btn_update_widget_port, View.GONE);
		} else {
			rv.setViewVisibility(R.id.layout_pb, View.GONE);
			rv.setViewVisibility(R.id.btn_update_widget_port, View.VISIBLE);
		}

		// 7.call widget manager do update widget.
		mAppWidgetManager.updateAppWidget(mAppWidgetId, rv);
	}
}
