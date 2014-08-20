package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.task.UpdateNewsTaskAllSymbols;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class WidgetReceiverNewsUpdate extends BroadcastReceiver {

	private String[] mTickerList;
	private UpdateNewsTaskAllSymbols allNewsTask = null;
	private boolean isRefreshPressed = false;
	private boolean isWidgetNews = false;
	private boolean isWidgetNewsNoHc = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.ACTION_WIDGET_RECEIVER_NEWS_UPDATE)) {

			Bundle bundle = intent.getExtras();
			boolean hasConn = NetworkConnectivity.hasNetworkConnection(context);

			if (!hasConn) {
				return;
			}

			if (bundle == null) {
				return;
			}

			if (bundle != null && hasConn == true) {
				isRefreshPressed = bundle.getBoolean(Constants.KEY_IS_WIDGET_REFRESH_PRESSED);
				isWidgetNews = bundle.getBoolean(Constants.KEY_IS_WIDGET_NEWS, false);
				isWidgetNewsNoHc = bundle.getBoolean(Constants.KEY_IS_WIDGET_NEWS_NO_HC, false);

				int widgetId = bundle.getInt(Constants.KEY_WIDGET_ID);

				if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

					String msg = context.getResources().getString(R.string.updating);
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

					if (isWidgetNews)
						updateNewsWidget(context, widgetId, isRefreshPressed);
					else if (isWidgetNewsNoHc)
						WidgetProviderNewsNoHc.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId, 
								isRefreshPressed);
					
					if (isRefreshPressed)
						quoteAllPreExcute(context, widgetId);
				}
			}
		} 
	}

	private void quoteAllPreExcute(Context context, int widgetId) {
		Context mContext = context;
		mTickerList = updateStockList();

		if (mTickerList != null && mTickerList.length > 0) {
			excuteMyTask(mContext);
		} else {
			if (isWidgetNews)
				updateNewsWidget(mContext, widgetId, false);	
			else if (isWidgetNewsNoHc)
				WidgetProviderNewsNoHc.updateAppWidget(mContext, AppWidgetManager.getInstance(mContext), widgetId, false);
		}
	}

	private void updateNewsWidget(Context _context, int _widgetId, boolean _isGoingUpdate) {
		WidgetProviderNews.updateAppWidget(_context, AppWidgetManager.getInstance(_context), _widgetId, 
				_isGoingUpdate);
		Intent updateWidgetIntent = new Intent(_context, WidgetProviderPortfolio.class);
		updateWidgetIntent.setAction(WidgetProviderPortfolio.APP_WIDGET_UPDATE_PORTFOLIO);
		_context.sendBroadcast(updateWidgetIntent);
	}

	
	private void excuteMyTask(Context context) {
		Context mContext = context;
		if (allNewsTask == null) {
			allNewsTask = new UpdateNewsTaskAllSymbols(mContext);
			allNewsTask.execute(mTickerList);
		} else {
			allNewsTask.cancel(true);
			allNewsTask = null;
			allNewsTask = new UpdateNewsTaskAllSymbols(mContext);
			allNewsTask.execute(mTickerList);
		}
	}

	private String[] updateStockList() {

		List<StockObject> mStockList = new ArrayList<StockObject>();
		
		List<PortfolioStockObject> mPortStockList = DbAdapter.getSingleInstance().fetchPortStockList();

		if (mPortStockList.size() > 0) {
			for (PortfolioStockObject po : mPortStockList) {
				int mStockId = po.getStockId();
				StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
				if (mSo != null) {
					mStockList.add(mSo);
				}
			}
		}
		

		if (mStockList != null && mStockList.size() > 0) {
			String[] mTickerList = new String[mStockList.size()];
			for (int i = 0; i < mStockList.size(); i++) {
				String mSymbol = mStockList.get(i).getSymbol();
				mTickerList[i] = mSymbol;
			}
			if (mTickerList != null && mTickerList.length > 0) {
				// Remove duplicates
				List<String> al = new ArrayList<String>();
				for (int i=0; i<mTickerList.length; i++)
					al.add(mTickerList[i]);
				HashSet<String> hs = new HashSet<String>();
				hs.addAll(al);
				al.clear();
				al.addAll(hs);
				
				// Sort by alphabetical order
				Collections.sort(al, new StringComparator());
				
				mTickerList = new String[al.size()];
				mTickerList = al.toArray(mTickerList);
				
				return mTickerList;
			}
		}
		return null;
	}
	
	public class StringComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			return lhs.compareToIgnoreCase(rhs);
		}
	}
}
