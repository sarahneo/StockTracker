package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.utils.CsvReader;

public class UpdateQuoteWithSizeTask extends AsyncTask<Void, Void, Boolean> {

	private String symbol;
	private QuoteObject object = null;
	private Context context;

	public UpdateQuoteWithSizeTask(Context context, String symbol) {
		this.symbol = symbol;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {

		object = quoteWholeDataSetJSonFormat(this.symbol);
		if (object != null) {
			quoteWholeDataSetCsvFormat(this.symbol);
			return true;
		}
		return false;
	}

	private QuoteObject quoteWholeDataSetJSonFormat(String mSymbol) {

		String _symbol = mSymbol;

		HttpURLConnection httpConnection;

		String myUrlQuoteDataSetJSonFormat = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(\'"
				+ _symbol + "\')%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";

		try {
			URL url = new URL(myUrlQuoteDataSetJSonFormat);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			//httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Connection", "close");
			httpConnection.setInstanceFollowRedirects(true);
			httpConnection.setUseCaches(false);
			httpConnection.connect();

			InputStream in = new BufferedInputStream(httpConnection.getInputStream());
			String jsonp = IOUtils.toString(in, "UTF-8");

			QuoteObject quote = proceedJSonToObject(jsonp, _symbol);

			httpConnection.disconnect();

			return quote;

		} catch (IOException e) {
			return null;
		}
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

			return obj;

		} catch (JSONException e) {
			return null;
		}
	}

	private void quoteWholeDataSetCsvFormat(String mSymbol) {

		HttpURLConnection httpConnection;

		String mmSymbol = mSymbol;

		String csvAskSize = String.format("http://quote.yahoo.com/d/quotes.csv?s=%s&f=b6", mmSymbol);
		String csvBitSize = String.format("http://quote.yahoo.com/d/quotes.csv?s=%s&f=a5", mmSymbol);

		// http://quote.yahoo.com/d/quotes.csv?s=goog&f=npob3b6b2a5t8mwva2j1rel1c1p2d1t1
		// http://finance.yahoo.com/d/quotes.csv?s=%s&f=npob3b6b2a5t8mwva2j1rel1c1p2d1t1

		try {
			URL urlAskSize = new URL(csvAskSize);
			URL urlBitSize = new URL(csvBitSize);

			httpConnection = (HttpURLConnection) urlAskSize.openConnection();
			httpConnection.setRequestMethod("GET");
			//httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Connection", "close");
			httpConnection.setInstanceFollowRedirects(true);
			httpConnection.setUseCaches(false);
			httpConnection.connect();

			InputStream in = new BufferedInputStream(httpConnection.getInputStream());

			httpConnection = (HttpURLConnection) urlBitSize.openConnection();
			httpConnection.setRequestMethod("GET");
			//httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Connection", "close");
			httpConnection.setInstanceFollowRedirects(true);
			httpConnection.setUseCaches(false);
			httpConnection.connect();

			InputStream in2 = new BufferedInputStream(httpConnection.getInputStream());
			
			httpConnection.disconnect();

			proceedToObject(in, true);

			proceedToObject(in2, false);

		} catch (IOException e) {
		}
	}

	private void proceedToObject(InputStream strResult, boolean isAskSize) {

		String[] fields = null;

		try {
			CsvReader reader = new CsvReader(new InputStreamReader(strResult), ',');

			reader.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);

			while (reader.readRecord()) {
				fields = reader.getValues();

				String size = "";

				if (fields.length > 1) {

					size = fields[0] + fields[1];

				} else {

					size = fields[0];
				}

				if (isAskSize) {
					object.setAskSize(size);
				} else {
					object.setBidSize(size);
				}
			}
		} catch (IOException e) {
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
			}
		}
		Intent i = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
		i.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
		context.sendBroadcast(i);
		
		Intent i2 = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
		i2.putExtra(Constants.KEY_SYMBOL, object.getSymbol());
		context.sendBroadcast(i2);
	}
}