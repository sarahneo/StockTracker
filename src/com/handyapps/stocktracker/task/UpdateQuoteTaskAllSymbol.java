package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.service.BroadcaseUtils;
import com.handyapps.stocktracker.service.MyPriceNotification;
import com.handyapps.stocktracker.widget.WidgetUpdateIntentService;

public class UpdateQuoteTaskAllSymbol extends AsyncTask<String, Void, Boolean> {

	private List<QuoteObject> qList = null;
	private Context mCtx;
	private HttpURLConnection httpConnection = null;

	public UpdateQuoteTaskAllSymbol(Context context) {
		this.mCtx = context;
		this.qList = new ArrayList<QuoteObject>();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... arr) {

		try {
			if (!isCancelled()) {
				if (arr.length > 0) {

					for (String s : arr) {
						QuoteObject mQo = quoteWholeDataSetJSonFormat(s);
						if (mQo != null) {

							QuoteObject object = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(s);
							if (object != null) {
								int mId = object.getId();
								mQo.setId(mId);
								boolean isUpdated = mQo.update();
								if(isUpdated){
								}

							} else {
								mQo.insert();
							}
							qList.add(mQo);
						}
					}

					if (qList.size() > 0) {
						return true;
					}
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result) {
			//Log.d("AutoQuoteUpdate", "Done.");
			if (qList.size() > 0) {
				for (QuoteObject mQo : qList) {
					sendMyBroadcastBySymbol(mQo);
				}
				sendMyBroadcastByOnceRefresh();
			}
		} else {
			// update widget: to tell widget to stop progress bar too if failure
			// updates.
			Intent iUpdateWidget = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
			this.mCtx.startService(iUpdateWidget);
		}
	}

	private void sendMyBroadcastByOnceRefresh() {

		// Main portfolio list:
		Intent iMainPortList = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		iMainPortList.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		this.mCtx.sendBroadcast(iMainPortList);

		// Update portfolio:
		List<PortfolioObject> pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		if (pList.size() > 0) {
			for (PortfolioObject po : pList) {
				String portName = po.getName();
				BroadcaseUtils.portRefreshSinglePage(portName, this.mCtx);
			}
		}

		// Update watchlist:
		List<WatchlistObject> wList = DbAdapter.getSingleInstance().fetchWatchlists();
		List<String> wNameList = new ArrayList<String>();
		if (wList.size() > 0) {
			for (WatchlistObject wo : wList) {
				wNameList.add(wo.getName());
			}
		}
		Intent iWatchlist;
		if (wNameList != null && wNameList.size() > 0) {
			for (String mWatchName : wNameList) {
				String mAction = Constants.ACTION_SINGLE_WATCH_FRAGMENT_BROADCASE + mWatchName;
				iWatchlist = new Intent(mAction);
				this.mCtx.sendBroadcast(iWatchlist);
			}
		}

		// update watchlist initial page activity:
		Intent iWatchlistInitialPage = new Intent(Constants.ACTION_WATCHLIST_INITIAL_PAGER_ACTIVITY);
		iWatchlistInitialPage.putExtra(Constants.KEY_IS_QUOTE_OK, true);
		this.mCtx.sendBroadcast(iWatchlistInitialPage);

		// update widget:
		Intent iUpdateWidget = new Intent(mCtx, WidgetUpdateIntentService.class);
		iUpdateWidget.setAction(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
		this.mCtx.startService(iUpdateWidget);
	}

	private void sendMyBroadcastBySymbol(QuoteObject quote) {

		Intent iSummaryFragment = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
		iSummaryFragment.putExtra(Constants.KEY_SYMBOL, quote.getSymbol());
		iSummaryFragment.putExtra(Constants.KEY_IS_UPDATE_CHART, true);
		this.mCtx.sendBroadcast(iSummaryFragment);

		Intent iTransactionFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
		iTransactionFragmentActivity.putExtra(Constants.KEY_SYMBOL, quote.getSymbol());
		iTransactionFragmentActivity.putExtra(Constants.KEY_IS_SINGLE_QUOTE_DONE, true);
		this.mCtx.sendBroadcast(iTransactionFragmentActivity);

		Intent iAddNewTrade = new Intent(Constants.ACTION_ADD_NEW_TRADE);
		iAddNewTrade.putExtra(Constants.KEY_SYMBOL, quote.getSymbol());
		this.mCtx.sendBroadcast(iAddNewTrade);

		// Check for Alert:
		String mLastTradePrice = quote.getLastTradePrice();
		String mSymbol = quote.getSymbol();
		MyPriceNotification.alertNotification(mLastTradePrice, mSymbol, this.mCtx);
	}

	private QuoteObject quoteWholeDataSetJSonFormat(String mSymbol) {

		String _symbol = mSymbol;
		QuoteObject quote = null;

		String myUrlQuoteDataSetJSonFormat = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(\'"
				+ _symbol + "\')%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";

		try {
			URL url = new URL(myUrlQuoteDataSetJSonFormat);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			// httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Connection", "close");
			httpConnection.setInstanceFollowRedirects(true);
			httpConnection.setUseCaches(false);
			httpConnection.connect();

			InputStream in = new BufferedInputStream(httpConnection.getInputStream());
			String jsonp = IOUtils.toString(in, "UTF-8");

			quote = proceedJSonToObject(jsonp, _symbol);

		} catch (IOException e) {
			return null;
		}
		return quote;
	}

	private QuoteObject proceedJSonToObject(String json, String symbol) {

		String _symbol = symbol;
		String mJSon = json;

		try {
			JSONObject jso = new JSONObject(mJSon).getJSONObject("query");
			JSONObject jsoa = jso.getJSONObject("results");
			JSONObject jsob = jsoa.getJSONObject("quote");

			// String name = jsob.get("Name").toString();
			String prevClose = jsob.get("PreviousClose").toString();
			String open = jsob.get("Open").toString();
			String bid = jsob.get("Bid").toString();
			String ask = jsob.get("Ask").toString();
			String yearTargetEst = jsob.get("OneyrTargetPrice").toString();
			String dayLow = jsob.get("DaysLow").toString();
			String dayHigh = jsob.get("DaysHigh").toString();
			String dayRange = jsob.get("DaysRange").toString();
			String yearLow = jsob.get("YearLow").toString();
			String yearHigh = jsob.get("YearHigh").toString();
			String yearRange = jsob.get("YearRange").toString();
			String volume = jsob.get("Volume").toString();
			String avgVol = jsob.get("AverageDailyVolume").toString();
			String marketCap = jsob.get("MarketCapitalization").toString();
			String priceEarningRatio = jsob.get("PERatio").toString();
			String earningPerShare = jsob.get("EarningsShare").toString(); // epsTTM

			String dividendShare = jsob.get("DividendShare").toString();
			String divAndYield = jsob.getString("DividendYield").toString();
			String lastTradePrice = jsob.get("LastTradePriceOnly").toString();
			String change = jsob.get("Change").toString();
			String changeInPercent = jsob.get("PercentChange").toString();
			String lastTradeDate = jsob.get("LastTradeDate").toString();
			String lastTradeTime = jsob.get("LastTradeTime").toString();
			String currency = jsob.get("Currency").toString();

			QuoteObject obj = new QuoteObject();

			obj.setSymbol(_symbol);
			obj.setPrevClosePrice(prevClose);
			obj.setOpenPrice(open);
			obj.setBidPrice(bid);
			obj.setAskPrice(ask);
			obj.setYearTargetEst(yearTargetEst);
			obj.setDayLow(dayLow);
			obj.setDayHigh(dayHigh);
			obj.setDayRange(dayRange);
			obj.setYearLow(yearLow);
			obj.setYearHigh(yearHigh);
			obj.setYearRange(yearRange);
			obj.setVolume(volume);
			obj.setAvgVolume(avgVol);
			obj.setMarketCap(marketCap);
			obj.setPeRatioTTM(priceEarningRatio);
			obj.setEpsTTM(earningPerShare);
			obj.setDividend(dividendShare);
			obj.setDividendYield(divAndYield);
			obj.setLastTradePrice(lastTradePrice);
			obj.setChange(change);
			obj.setChangeInPercent(changeInPercent);
			obj.setLastTradeDate(lastTradeDate);
			obj.setLastTradeTime(lastTradeTime);
			obj.setCurrency(currency);
			return obj;
		} catch (JSONException e) {
			return null;
		}
	}
}
