package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.WrongCurrencyDialog;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.service.BroadcaseUtils;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.service.MyPriceNotification;

public class UpdateQuoteTaskSingleSymbol extends AsyncTask<String, Void, Boolean> {

	private String symbol;
	private QuoteObject object = null;
	private Context context;
	private HttpURLConnection httpConnection = null;
	private SharedPreferences sp;
	private ProgressBar pb;
	private int portId = 0;
	private int stockId = 0;

	public UpdateQuoteTaskSingleSymbol(Context context, ProgressBar pb, int portId, int stockId) {
		this.context = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(context);
		this.pb = pb;
		this.portId = portId;
		this.stockId = stockId;
	}

	@Override
	protected void onPreExecute() {
		if (pb != null)
			pb.setVisibility(View.VISIBLE);
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... arrSymbols) {
		this.symbol = arrSymbols[0];

		try {
			if (!isCancelled()) {

				object = quoteWholeDataSetJSonFormat(this.symbol);
				if (object != null) {
					return true;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return false;

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
			String currency = jsob.getString("Currency").toString();

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
			sp.edit().putBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false).commit();
			return obj;

		} catch (JSONException e) {

			String exception = String.valueOf(e);
			if (exception.contains("results") && exception.contains("null")) {
				sp.edit().putBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, true).commit();
			}
			
			return null;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
			if (object != null) {
				//Log.d("SingleQuote", "updated");
				QuoteObject mQuote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(this.symbol);
				int id;
				if (mQuote != null) {
					id = mQuote.getId();
					object.setId(id);
					object.update();
				} else {
					object.insert();
				}				
				
				// from FIND_STOCKS
				if (portId != 0 && stockId != 0) {
					String currency = object.getCurrency();
					boolean isInsert = false;
					
					// 3. Add PortfolioStockObject
					PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portId, stockId);
					PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
					if (ps == null) {
			
						ps = new PortfolioStockObject();
						ps.setPortfolioId(portId);
						ps.setStockId(stockId);	
						
						List<PortfolioStockObject> pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
						if (!pso.isEmpty()) {
							if (currency.equals(po.getCurrencyType())) {
								ps.insert();
								isInsert = true;
							} else {
								// return error
								Log.i("wrong currency", po.getCurrencyType()+":"+currency);
								
								WrongCurrencyDialog currDialog = new WrongCurrencyDialog();     
								currDialog.show(((FragmentActivity) context).getSupportFragmentManager(), Constants.DIALOG_WRONG_CURRENCY);
							}
						} else {
							ps.insert();
							po.setCurrencyType(currency);
							po.update();
							isInsert = true;
						}
					}
					
					if (isInsert) {
						// Add News Alert
						MyAlarmManager alarmManager = new MyAlarmManager(context);
						alarmManager.addNewsAlert(symbol);
						
						// sendBroadcast when only PortfolioStockObject did
						// insert.
						Toast.makeText(context, context.getResources().getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
					}
				}

				//1. transaction activity:
				Intent iTransactionFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
				iTransactionFragmentActivity.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
				iTransactionFragmentActivity.putExtra(Constants.KEY_IS_SINGLE_QUOTE_DONE, true);
				context.sendBroadcast(iTransactionFragmentActivity);
				
				//2. summary fragment:
				Intent iSummaryFragment = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
				iSummaryFragment.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
				iSummaryFragment.putExtra(Constants.KEY_IS_UPDATE_CHART, true);	
				context.sendBroadcast(iSummaryFragment);
				
				//3. trade fragment:
				Intent iTradeFragment = new Intent(Constants.ACTION_TRADES_FRAGMENT);
				iTradeFragment.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
				context.sendBroadcast(iTradeFragment);

				//4. add new trade activity:
				Intent iAddNewTrade = new Intent(Constants.ACTION_ADD_NEW_TRADE);
				iAddNewTrade.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
				iAddNewTrade.putExtra(Constants.KEY_CURRENCY, object.getCurrency());
				iAddNewTrade.putExtra(Constants.KEY_IS_QUOTE_OK, true);
				context.sendBroadcast(iAddNewTrade);

				// Update portfolio:
				List<PortfolioObject> pList = DbAdapter.getSingleInstance().fetchPortfolioList();
				if (pList.size() > 0) {
					for (PortfolioObject po : pList) {
						String portName = po.getName();
						BroadcaseUtils.portRefreshSinglePage(portName, context);
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
					for (String watchName : wNameList) {
						String mAction = Constants.ACTION_SINGLE_WATCH_FRAGMENT_BROADCASE + watchName;
						iWatchlist = new Intent(mAction);
						context.sendBroadcast(iWatchlist);
					}
				}

				// Check for Alert:
				String mLastTradePrice = object.getLastTradePrice();
				String mSymbol = object.getSymbol();
				MyPriceNotification.alertNotification(mLastTradePrice, mSymbol, context);

				// update widget:
				Intent iUpdateWidget = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
				context.startService(iUpdateWidget);
				
				// Update pager
				Intent iUpdatePager; 
				iUpdatePager = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
				iUpdatePager.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
				context.sendBroadcast(iUpdatePager);
			}
		} else {

			Intent iTransationFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
			iTransationFragmentActivity.putExtra(Constants.KEY_IS_QUOTE_OK, false);
			context.sendBroadcast(iTransationFragmentActivity);

			Intent iAddAlertDialog = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
			iAddAlertDialog.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, false);
			iAddAlertDialog.putExtra(Constants.KEY_IS_QUOTE_OK, false);
			context.sendBroadcast(iAddAlertDialog);
		}
		if (pb != null)
			pb.setVisibility(View.VISIBLE);
	}
}