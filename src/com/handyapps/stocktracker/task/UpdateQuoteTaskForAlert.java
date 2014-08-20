package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.adapter.FindStocksAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.service.MyPriceNotification;

public class UpdateQuoteTaskForAlert extends AsyncTask<String, Void, Boolean> {

	private String symbol;
	private QuoteObject object = null;
	private StockObject stockObj = null;
	private Context context;
	private HttpURLConnection connection = null;

	private SharedPreferences sp;

	public UpdateQuoteTaskForAlert(Context context) {
		this.context = context;
		this.sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(String... arrSymbol) {

		this.symbol = arrSymbol[0];

		try {
			if (!isCancelled()) {

				String myUrlQuoteDataSetJSonFormat = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(\'"
						+ symbol + "\')%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";

				URL mUrl = new URL(myUrlQuoteDataSetJSonFormat);
				connection = (HttpURLConnection) mUrl.openConnection();
				connection.setRequestMethod("GET");
				// httpConnection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("Connection", "close");
				connection.setInstanceFollowRedirects(true);
				connection.setUseCaches(false);
				connection.connect();

				InputStream in = new BufferedInputStream(connection.getInputStream());
				String jsonp = IOUtils.toString(in, "UTF-8");
				object = proceedJSonToObject(jsonp, symbol);

				if (object != null) {
					return true;
				}
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}
		return false;
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
			String name = jsob.getString("Name").toString();
			String lastTradePrice = jsob.get("LastTradePriceOnly").toString();
			String change = jsob.get("Change").toString();
			String changeInPercent = jsob.get("PercentChange").toString();
			String lastTradeDate = jsob.get("LastTradeDate").toString();
			String lastTradeTime = jsob.get("LastTradeTime").toString();
			String currency = jsob.get("Currency").toString();
			
			// new
			stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(_symbol);
			if (stockObj == null) {
				stockObj.setSymbol(_symbol);
				stockObj.setName(name);
				stockObj.setCurrency(currency);
				stockObj.insert();
				
				ArrayList<SymbolCallBackObject> ls = new ArrayList<SymbolCallBackObject>();
				org.holoeverywhere.widget.ProgressBar pb = new org.holoeverywhere.widget.ProgressBar(context);
				ListView lv = new ListView(context);
				FindStocksAdapter customAdapter = new FindStocksAdapter((FragmentActivity) context, android.R.layout.simple_dropdown_item_1line, 
						android.R.id.text1, ls);
				FindStocksCallbackTask callBackTask = new FindStocksCallbackTask((FragmentActivity) context, pb, ls, customAdapter, lv, 
						Constants.FROM_ADD_ALERT_DIALOG, 0, null);
				callBackTask.execute(_symbol);
			}

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

				QuoteObject mQuote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(this.symbol);
				int id;
				if (mQuote != null) {
					id = mQuote.getId();
					object.setId(id);
					object.update();
				} else {
					object.insert();
				}

				// update add alert
				Intent iAddAlertDialog = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
				iAddAlertDialog.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
				iAddAlertDialog.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, false);
				iAddAlertDialog.putExtra(Constants.KEY_IS_QUOTE_OK, true);
				context.sendBroadcast(iAddAlertDialog);

				// Check for Alert:
				String mLastTradePrice = object.getLastTradePrice();
				String mSymbol = object.getSymbol();
				MyPriceNotification.alertNotification(mLastTradePrice, mSymbol, context);

				// update widget:
				Intent iUpdateWidget = new Intent(Constants.ACTION_UPDATE_WIDGET_INTENT_SERVICE);
				context.startService(iUpdateWidget);
			}
		} else {
			// update add alert
			Intent iAddAlertDialog = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
			iAddAlertDialog.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, false);
			iAddAlertDialog.putExtra(Constants.KEY_IS_QUOTE_OK, false);
			context.sendBroadcast(iAddAlertDialog);
		}
	}
}