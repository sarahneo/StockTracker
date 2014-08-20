package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.task.UpdateQuoteTaskAllSymbol;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class WidgetReceiverQuoteUpdate extends BroadcastReceiver {

	private String[] mTickerList;
	private UpdateQuoteTaskAllSymbol allQuoteTask = null;
	private int id = 1;
	private boolean isWidgetPortfolio = false;
	private boolean isWidgetWatchlist = false;
	private boolean isWidgetWitchlistNoHc = false;
	private boolean isWidgetPortfolioNoHc = false;
	private boolean isLeftPressed = false;
	private boolean isRightPressed = false;
	private boolean isRefreshPressed = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.ACTION_WIDGET_RECEIVER_QUOTE_UPDATE)) {

			Bundle bundle = intent.getExtras();
			boolean hasConn = NetworkConnectivity.hasNetworkConnection(context);

			if (!hasConn) {
				return;
			}

			if (bundle == null) {
				return;
			}

			if (bundle != null && hasConn == true) {
				isWidgetPortfolio = bundle.getBoolean(Constants.KEY_IS_WIDGET_PORTFOLIO, false);
				isWidgetWatchlist = bundle.getBoolean(Constants.KEY_IS_WIDGET_WATCHLIST, false);
				isWidgetPortfolioNoHc = bundle.getBoolean(Constants.KEY_IS_WIDGET_PORTFOLIO_NO_HC, false);
				isWidgetWitchlistNoHc = bundle.getBoolean(Constants.KEY_IS_WIDGET_WATCHLIST_NO_HC, false);
				isLeftPressed = bundle.getBoolean(Constants.KEY_IS_WIDGET_LEFT_PRESSED);
				isRightPressed = bundle.getBoolean(Constants.KEY_IS_WIDGET_RIGHT_PRESSED);
				isRefreshPressed = bundle.getBoolean(Constants.KEY_IS_WIDGET_REFRESH_PRESSED);

				int widgetId = bundle.getInt(Constants.KEY_WIDGET_ID);

				if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

					String msg = context.getResources().getString(R.string.updating);
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();


					if (isWidgetPortfolio) {
						isWidgetWatchlist = false;
						isWidgetPortfolioNoHc = false;
						isWidgetWitchlistNoHc = false;
						id = bundle.getInt(Constants.KEY_PORTFOLIO_ID);		
						if (isLeftPressed) {
							id = bundle.getInt(Constants.KEY_PORTFOLIO_LEFT_ID);							
						} else if (isRightPressed) {
							id = bundle.getInt(Constants.KEY_PORTFOLIO_RIGHT_ID);							
						}
						Log.d("portId", "WidgetReceiverQuoteUpdate="+id);
						updatePortfolioWidget(context, widgetId, isRefreshPressed, isLeftPressed, isRightPressed);
					} else if (isWidgetWatchlist) {
						isWidgetPortfolio = false;
						isWidgetPortfolioNoHc = false;
						isWidgetWitchlistNoHc = false;
						id = bundle.getInt(Constants.KEY_WATCHLIST_ID);
						if (isLeftPressed) {
							id = bundle.getInt(Constants.KEY_WATCHLIST_LEFT_ID);							
						} else if (isRightPressed) {
							id = bundle.getInt(Constants.KEY_WATCHLIST_RIGHT_ID);							
						}
						updateWatchlistWidget(context, widgetId, isRefreshPressed, isLeftPressed, isRightPressed);
					} else if (isWidgetPortfolioNoHc) {
						isWidgetPortfolio = false;
						isWidgetWatchlist = false;
						isWidgetWitchlistNoHc = false;
						id = bundle.getInt(Constants.KEY_PORTFOLIO_ID);
						if (isLeftPressed) {
							id = bundle.getInt(Constants.KEY_PORTFOLIO_LEFT_ID);							
						} else if (isRightPressed) {
							id = bundle.getInt(Constants.KEY_PORTFOLIO_RIGHT_ID);							
						}
						Log.d("portId", "WidgetReceiverQuoteUpdate="+id);
						WidgetProviderPortfolioNoHc.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId, 
								isRefreshPressed, isLeftPressed, isRightPressed);
					} else if (isWidgetWitchlistNoHc) {
						isWidgetPortfolio = false;
						isWidgetWatchlist = false;
						isWidgetPortfolioNoHc = false;
						id = bundle.getInt(Constants.KEY_WATCHLIST_ID);
						if (isLeftPressed) {
							id = bundle.getInt(Constants.KEY_WATCHLIST_LEFT_ID);							
						} else if (isRightPressed) {
							id = bundle.getInt(Constants.KEY_WATCHLIST_RIGHT_ID);							
						}
						Log.d("watchId", "WidgetReceiverQuoteUpdate="+id);
						WidgetProviderWatchlistNoHc.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId, 
								isRefreshPressed, isLeftPressed, isRightPressed);
					}

					if (!isLeftPressed && !isRightPressed && isRefreshPressed)
						quoteAllPreExcute(context, widgetId);
				}
			}
		} 
	}

	private void quoteAllPreExcute(Context context, int widgetId) {
		Context mContext = context;
		mTickerList = updateStockList(id);

		if (mTickerList != null && mTickerList.length > 0) {
			excuteMyTask(mContext);
		} else {

			if (isWidgetPortfolio) {
				updatePortfolioWidget(mContext, widgetId, false, false, false);
			} else if (isWidgetWatchlist) {
				updateWatchlistWidget(mContext, widgetId, false, false, false);
			} else if (isWidgetPortfolioNoHc) {
				WidgetProviderPortfolioNoHc.updateAppWidget(mContext, AppWidgetManager.getInstance(mContext), widgetId, false, false, false);
			} else if (isWidgetWitchlistNoHc) {
				WidgetProviderWatchlistNoHc.updateAppWidget(mContext, AppWidgetManager.getInstance(mContext), widgetId, false, false, false);
			}
		}
	}

	private void updatePortfolioWidget(Context _context, int _widgetId, 
			boolean _isGoingUpdate, boolean isLeftPressed, boolean isRightPressed) {
		WidgetProviderPortfolio.updateAppWidget(_context, AppWidgetManager.getInstance(_context), _widgetId, 
				_isGoingUpdate, isLeftPressed, isRightPressed);
		Intent updateWidgetIntent = new Intent(_context, WidgetProviderPortfolio.class);
		updateWidgetIntent.setAction(WidgetProviderPortfolio.APP_WIDGET_UPDATE_PORTFOLIO);
		_context.sendBroadcast(updateWidgetIntent);
	}

	private void updateWatchlistWidget(Context _context, int _widgetId, 
			boolean _isGoingUpdate, boolean isLeftPressed, boolean isRightPressed) {
		WidgetProviderWatchlist.updateAppWidget(_context, AppWidgetManager.getInstance(_context), _widgetId, 
				_isGoingUpdate, isLeftPressed, isRightPressed);
		Intent updateWidgetIntent = new Intent(_context, WidgetProviderWatchlist.class);
		updateWidgetIntent.setAction(WidgetProviderWatchlist.APP_WIDGET_UPDATE_WATCHLIST);
		_context.sendBroadcast(updateWidgetIntent);
	}

	private void excuteMyTask(Context context) {
		Context mContext = context;
		if (allQuoteTask == null) {
			allQuoteTask = new UpdateQuoteTaskAllSymbol(mContext);
			allQuoteTask.execute(mTickerList);
		} else {
			allQuoteTask.cancel(true);
			allQuoteTask = null;
			allQuoteTask = new UpdateQuoteTaskAllSymbol(mContext);
			allQuoteTask.execute(mTickerList);
		}
	}

	private String[] updateStockList(int _id) {

		int mID = _id;
		List<StockObject> mStockList = new ArrayList<StockObject>();
		if (isWidgetPortfolio == true || isWidgetPortfolioNoHc == true) {
			List<PortfolioStockObject> mPortStockList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(mID);

			if (mPortStockList.size() > 0) {
				for (PortfolioStockObject po : mPortStockList) {
					int mStockId = po.getStockId();
					StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
					if (mSo != null) {
						mStockList.add(mSo);
					}
				}
			}
		} else if (isWidgetWatchlist == true || isWidgetWitchlistNoHc == true) {
			List<WatchlistStockObject> mWatchStockList = DbAdapter.getSingleInstance().fetchWatchStockListByWatchId(mID);

			if (mWatchStockList.size() > 0) {
				for (WatchlistStockObject wo : mWatchStockList) {
					int mStockId = wo.getStockId();
					StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
					if (mSo != null) {
						mStockList.add(mSo);
					}
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
				return mTickerList;
			}
		}
		return null;
	}
}
