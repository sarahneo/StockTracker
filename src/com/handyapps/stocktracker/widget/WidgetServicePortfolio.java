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
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.utils.TextColorPicker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetServicePortfolio extends RemoteViewsService {
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new StackRemoteViewsFactoryPortfolio(this.getApplicationContext(), intent);
	}

	class StackRemoteViewsFactoryPortfolio implements RemoteViewsService.RemoteViewsFactory {
		private Context mContext = null;
		private int mAppWidgetId = -1;
		private List<PortfolioStockObject> list;
		private int portId = -1;
		private String theme = "Light";

		public StackRemoteViewsFactoryPortfolio(Context context, Intent intent) {
			this.mContext = context;
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				this.portId = intent.getExtras().getInt(Constants.KEY_PORTFOLIO_ID);				
			}
		}

		@Override
		public void onCreate() {

			if (portId <= 0)
				this.portId = WidgetUtils.loadIntPrefPortfolio(this.mContext, this.mAppWidgetId);
			
			theme = WidgetUtils.loadPortfolioTheme(this.mContext, this.mAppWidgetId);

			Log.d("portId", "portIdAfter="+portId);
			// 1. get PortStock Object list.
			this.list = new ArrayList<PortfolioStockObject>();
			if (this.portId != -1) {
				List<PortfolioStockObject> mList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(this.portId);
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
				rvRow = new RemoteViews(this.mContext.getPackageName(), R.layout.single_portfolio_list_row_dark);
				Log.i("theme", "dark");
			} else {
				rvRow = new RemoteViews(this.mContext.getPackageName(), R.layout.single_portfolio_list_row);
				Log.i("theme", "light");
			}
			PortfolioStockObject mWsObject = this.list.get(position);

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
					rvRow.setTextViewText(R.id.tv_symbol_with_exch, mSymbol+ ":" + so.getExchDisp());					

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
						rvRow.setTextViewText(R.id.tv_last_price, mLastTradePrice);

						// 4.set changes:
						rvRow.setTextViewText(R.id.tv_change, spannedChanges);

						int stockId = so.getId();
						Intent i = new Intent();
						i.putExtra(Constants.KEY_STOCK_ID, stockId);
						i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
						rvRow.setOnClickFillInIntent(R.id.row_body_single_portfolio, i);
					}
				}
			}
			return (rvRow);
		}

		@Override
		public void onDataSetChanged() {
			int mPortfolioId = 0;
			if (portId <= 0)
				mPortfolioId = WidgetUtils.loadIntPrefPortfolio(this.mContext, this.mAppWidgetId);
			else 
				mPortfolioId = portId;
			
			theme = WidgetUtils.loadPortfolioTheme(this.mContext, this.mAppWidgetId);
			// 1. get PortStock Object list.
			if (mPortfolioId != -1) {
				List<PortfolioStockObject> mList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(mPortfolioId);
				if (mList != null && mList.size() > 0) {
					this.list = mList;
					//Log.d("WidgetServicePortfolio", "portfoliolist:portId:" + String.valueOf(mPortfolioId));
				}else{
					this.list = new ArrayList<PortfolioStockObject>();
				}
			}else{
				this.list = new ArrayList<PortfolioStockObject>();
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