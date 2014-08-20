package com.handyapps.stocktracker.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.task.UpdateQuoteTaskAllSymbol;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class IntentServiceUpdateQuote extends IntentService {

	public IntentServiceUpdateQuote() {
		super("Intent Service Update Quote");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean hasNetConnection = hasNetConnection();
		Bundle bundle = intent.getExtras();

		if (bundle != null && hasNetConnection == true) {

			boolean isUpdateQuoteAll = bundle.getBoolean(Constants.KEY_IS_UPDATE_QUOTE_ALL_TICKERS, false);
			if (isUpdateQuoteAll) {
				updateQuoteAllTickers();
			} else {
				String mSymbol = bundle.getString(Constants.KEY_SYMBOL);
				updateQuoteSingleTicker(mSymbol);
			}
		}
	}

	private void updateQuoteAllTickers() {

		List<String> dupStocks = new ArrayList<String>();

		// 1. Alert stocks:
		List<AlertObject> alertList = DbAdapter.getSingleInstance().fetchAlertObjectAll();
		if (alertList.size() > 0) {
			for (AlertObject ao : alertList) {
				if (ao != null) {
					dupStocks.add(ao.getSymbol());
				}
			}
		}

		// 2. Watchlist stocks:
		List<WatchlistStockObject> woList = DbAdapter.getSingleInstance().fetchWatchStockList();
		List<Integer> dupStockIdWatch = new ArrayList<Integer>();
		if (woList.size() > 0) {
			for (WatchlistStockObject wo : woList) {
				if (wo != null) {
					dupStockIdWatch.add(wo.getStockId());
				}
			}
		}
		if (dupStockIdWatch != null && dupStockIdWatch.size() > 0) {
			
			ArrayList<Integer> nonDupListWatch = new ArrayList<Integer>();
			Iterator<Integer> dupIterWatch = dupStockIdWatch.listIterator();

			while (dupIterWatch.hasNext()) {
				int dupWord = dupIterWatch.next();
				if (nonDupListWatch.contains(dupWord)) {
					dupIterWatch.remove();
				} else {
					nonDupListWatch.add(dupWord);
				}
			}
			if (nonDupListWatch != null && nonDupListWatch.size() > 0) {
				for (int stockId : nonDupListWatch) {
					StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
					if (so != null) {
						dupStocks.add(so.getSymbol());
					}
				}
			}
		}

		// 3. Portfolio stocks:
		List<PortfolioStockObject> poList = DbAdapter.getSingleInstance().fetchPortStockList();
		List<Integer> dupStockIdPort = new ArrayList<Integer>();
		if (poList.size() > 0) {
			for (PortfolioStockObject po : poList) {
				if (po != null) {
					dupStockIdPort.add(po.getStockId());
				}
			}
		}
		if (dupStockIdPort != null && dupStockIdPort.size() > 0) {
			ArrayList<Integer> nonDupListPort = new ArrayList<Integer>();
			Iterator<Integer> dupIterPort = dupStockIdPort.listIterator();

			while (dupIterPort.hasNext()) {
				int dupWord = dupIterPort.next();
				if (nonDupListPort.contains(dupWord)) {
					dupIterPort.remove();
				} else {
					nonDupListPort.add(dupWord);
				}
			}
			if (nonDupListPort != null && nonDupListPort.size() > 0) {
				for (int stockId : nonDupListPort) {
					StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
					if (so != null) {
						dupStocks.add(so.getSymbol());
					}
				}
			}
		}

		// 4. Remove duplicate stocks:
		if (dupStocks != null && dupStocks.size() > 0) {

			ArrayList<String> nonDupStocks = new ArrayList<String>();
			Iterator<String> dupIterStocks = dupStocks.listIterator();

			while (dupIterStocks.hasNext()) {
				String dupWord = dupIterStocks.next();
				if (nonDupStocks.contains(dupWord)) {
					dupIterStocks.remove();
				} else {
					nonDupStocks.add(dupWord);
				}
			}
			if (nonDupStocks != null && nonDupStocks.size() > 0) {

				String[] arrStocks = new String[nonDupStocks.size()];
				for (int i = 0; i < nonDupStocks.size(); i++) {
					arrStocks[i] = nonDupStocks.get(i);
				}

				//5. final execute task:
				UpdateQuoteTaskAllSymbol allTask = new UpdateQuoteTaskAllSymbol(this.getApplicationContext());
				allTask.execute(arrStocks);
			}
		}
	}

	private void updateQuoteSingleTicker(String symbol) {
		UpdateQuoteTaskSingleSymbol mTask = new UpdateQuoteTaskSingleSymbol(this.getApplicationContext(), null, 0, 0);
		mTask.execute(symbol);
	}

	private boolean hasNetConnection() {
		boolean hasNetConn = NetworkConnectivity.hasNetworkConnection(this.getApplicationContext());
		return hasNetConn;
	}
}