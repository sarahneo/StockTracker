package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.utils.TextColorPicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetServiceWatchlist extends RemoteViewsService {
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new StackRemoteViewsFactoryWatchlist(this.getApplicationContext(), intent);
	}

	class StackRemoteViewsFactoryWatchlist implements RemoteViewsService.RemoteViewsFactory {
		private Context mContext = null;
		private int mAppWidgetId = -1;
		private List<WatchlistStockObject> list;
		private int watchlistId = -1;
		private String theme = "Light";

		public StackRemoteViewsFactoryWatchlist(Context context, Intent intent) {
			this.mContext = context;
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				this.watchlistId = intent.getExtras().getInt(Constants.KEY_WATCHLIST_ID);				
			}
		}

		@Override
		public void onCreate() {

			if (watchlistId <= 0)
				this.watchlistId = WidgetUtils.loadIntPrefWatchlist(this.mContext, this.mAppWidgetId);
			
			theme = WidgetUtils.loadWatchlistTheme(this.mContext, this.mAppWidgetId);

			Log.d("watchId", "watchlistIdAfter="+watchlistId);
			// 1. get WatchStock Object list.
			this.list = new ArrayList<WatchlistStockObject>();
			if (this.watchlistId != -1) {
				List<WatchlistStockObject> mList = DbAdapter.getSingleInstance().fetchWatchStockListByWatchId(this.watchlistId);
				if (mList != null && mList.size() > 0) {
					this.list = mList;
				}
			}
		}

		@Override
		public int getCount() {
			return (this.list.size());
		}

		@Override
		public long getItemId(int position) {
			return (position);
		}

		@Override
		public RemoteViews getLoadingView() {
			return (null);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public RemoteViews getViewAt(int position) {

			String mSymbol = "";
			String mCompanyName = "";
			String mLastTradePrice = "";
			String mChanges = "";

			RemoteViews rvRow = null;
			if (theme.equals("Dark")) {
				rvRow = new RemoteViews(this.mContext.getPackageName(), R.layout.single_watchlist_row_dark);
				Log.i("theme", "dark");
			} else {
				rvRow = new RemoteViews(this.mContext.getPackageName(), R.layout.single_watchlist_row);
				Log.i("theme", "light");
			}
			WatchlistStockObject mWsObject = this.list.get(position);

			if (mWsObject != null) {

				int mStockId = mWsObject.getStockId();
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);

				if (so != null) {

					mSymbol = so.getSymbol();
					mCompanyName = so.getName();
					
					// 0. set color:
					rvRow.setInt(R.id.tv_color, "setBackgroundColor", so.getColorCode());
					
					// 1.set company:
					rvRow.setTextViewText(R.id.tv_company_name, mCompanyName);					

					// 2.set symbol with exchange:
					rvRow.setTextViewText(R.id.tv_symbol, mSymbol+ ":" + so.getExchDisp());					

					QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(mSymbol);
					if (quote != null) {
						mLastTradePrice = quote.getLastTradePrice();
						String mChange = quote.getChange();
						String mChangeInPercent = quote.getChangeInPercent();
						mChanges = mChange + "(" + mChangeInPercent + ")";
						
						Spanned spannedChanges;
						if (mChanges.contains("-")) {							
							spannedChanges = TextColorPicker.getRedText("", mChanges);
						} else {							
							spannedChanges = TextColorPicker.getGreenText("", mChanges);
						}
						// 3.set last trade price:						
						rvRow.setTextViewText(R.id.tv_last_trade_price, mLastTradePrice);

						// 4.set changes:
						rvRow.setTextViewText(R.id.tv_changes, spannedChanges);

						int stockId = so.getId();
						Intent i = new Intent();
						i.putExtra(Constants.KEY_STOCK_ID, stockId);
						i.putExtra(Constants.KEY_FROM, Constants.FROM_WATCH_LIST);
						rvRow.setOnClickFillInIntent(R.id.row_body_single_watchlist, i);
					}
				}
			}
			return (rvRow);
		}

		@Override
		public void onDataSetChanged() {
			int mWatchlistId = 0;
			if (watchlistId <= 0)
				mWatchlistId = WidgetUtils.loadIntPrefWatchlist(this.mContext, this.mAppWidgetId);
			else 
				mWatchlistId = watchlistId;
			
			theme = WidgetUtils.loadWatchlistTheme(this.mContext, this.mAppWidgetId);
			// 1. get WatchStock Object list.
			if (mWatchlistId != -1) {
				List<WatchlistStockObject> mList = DbAdapter.getSingleInstance().fetchWatchStockListByWatchId(mWatchlistId);
				if (mList != null && mList.size() > 0) {
					this.list = mList;
					//Log.d("WidgetServiceWatchlist", "watchlist:watchId:" + String.valueOf(mWatchlistId));
				}else{
					this.list = new ArrayList<WatchlistStockObject>();
				}
			}else{
				this.list = new ArrayList<WatchlistStockObject>();
			}
		}

		@Override
		public int getViewTypeCount() {
			return (1);
		}

		@Override
		public boolean hasStableIds() {
			return (true);
		}

		@Override
		public void onDestroy() {
		}
	}
}