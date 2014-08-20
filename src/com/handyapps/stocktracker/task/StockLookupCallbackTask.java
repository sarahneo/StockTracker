package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.handyapps.stocktracker.adapter.CustomAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;

/**
 * Symbol Lookup Call back task
 */
public class StockLookupCallbackTask extends AsyncTask<String, Void, Boolean> {

	private ArrayList<SymbolCallBackObject> mList;
	private HttpURLConnection connection = null;
	private Activity activity;
	private ProgressBar pb;
	private ArrayList<SymbolCallBackObject> ls;
	private ListView lv;
	private CustomAdapter mCustomAdapter;

	public StockLookupCallbackTask(Activity activity, ProgressBar pb, 
			ArrayList<SymbolCallBackObject> ls, CustomAdapter customAdapter, ListView lv) {
		this.activity = activity;
		this.pb = pb;
		this.ls = ls;
		this.mCustomAdapter = customAdapter;
		this.lv = lv;
		
		mCustomAdapter = new CustomAdapter(activity, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, this.ls);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pb.setVisibility(View.VISIBLE);
		activity.setProgressBarIndeterminateVisibility(true);
	}

	@Override
	protected Boolean doInBackground(String... url) {
		String sUrl = url[0];

		try {

			if (!isCancelled()) {

				String sFullUrl = String.format("http://autoc.finance.yahoo.com/autoc?query=%s&callback=YAHOO.Finance.SymbolSuggest.ssCallback",
						sUrl);
				URL mUrl = new URL(sFullUrl);
				connection = (HttpURLConnection) mUrl.openConnection();
				connection.setRequestMethod("GET");
				// httpConnection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("Connection", "close");
				connection.setInstanceFollowRedirects(true);
				connection.setUseCaches(false);
				connection.connect();

				InputStream in = connection.getInputStream();
				String jsonp = IOUtils.toString(in, "UTF-8");
				mList = proceedJSONArray(jsonp);

				if (mList.size() > 0) {
					return true;
				}

			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		return false;
	}

	private ArrayList<SymbolCallBackObject> proceedJSONArray(String jsonp) {

		jsonp = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));

		ArrayList<SymbolCallBackObject> list = new ArrayList<SymbolCallBackObject>();
		JSONArray s = null;
		try {
			s = new JSONObject(jsonp).getJSONObject("ResultSet").getJSONArray("Result");
		} catch (JSONException e) {
			list.clear();
			return list;
		}

		if (s != null && s.length() > 0) {

			int size = s.length();
			SymbolCallBackObject details;
			JSONObject jsObject;

			for (int i = 0; i < size; i++) {

				try {
					jsObject = null;
					jsObject = (JSONObject) s.get(i);
					details = new SymbolCallBackObject();

					String symbol = "";
					String name = "";
					String exch = "";
					String type = "";
					String typeDisp = "";
					String exchDisp = "";

					symbol = jsObject.get("symbol").toString();
					name = jsObject.get("name").toString();
					exch = jsObject.get("exch").toString();
					type = jsObject.get("type").toString();
					typeDisp = jsObject.get("typeDisp").toString();

					if (type.equals("E") || type.equals("C")) {
						exchDisp = "";

					} else if (type.equals("S") || type.equals("I") || type.equals("M") || type.equals("F")) {
						try {
							exchDisp = jsObject.get("exchDisp").toString();
						} catch (JSONException e) {
							exchDisp = "";
						}
					}
					
					details.setSymbol(symbol);
					details.setName(name);
					details.setExch(exch);
					details.setType(type);
					details.setTypeDisp(typeDisp);
					details.setExchDisp(exchDisp);
					if (!symbol.equals("^DJI"))
						list.add(details);
					
				} catch (JSONException e) {
					list.clear();
					return list;
				}
			}

			if (list != null && list.size() > 0) {
				String symArr[] = new String[list.size()];
				for (int i=0; i<list.size(); i++) 
					symArr[i] = list.get(i).getSymbol();
				
				List<QuoteObject> mQo = quoteWholeDataSetJSonFormat(symArr);
				
				if (mQo != null) {
					for (int j=0; j<symArr.length; j++) {
						QuoteObject object = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symArr[j]);

						if (object != null) {
							int mId = object.getId();
							mQo.get(j).setId(mId);
							boolean isUpdated = mQo.get(j).update();
							if(isUpdated){
							}

						} else {
							mQo.get(j).insert();
						}
					}
				}

				return list;
			} else 
				return list;
			
		}
		return list;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (activity != null) {
			activity.setProgressBarIndeterminateVisibility(false);
			pb.setVisibility(View.INVISIBLE);

			if (result) {
				setResult();
			}
		}
	}
	
	private List<QuoteObject> quoteWholeDataSetJSonFormat(String[] mSymbols) {

		String _symbol = "";
		List<QuoteObject> list = new ArrayList<QuoteObject>();
		
		for (int i=0; i<mSymbols.length; i++) {
			if (i==0)
				_symbol = mSymbols[i];
			else
				_symbol += "'%2C'" + mSymbols[i];
		}	

		String myUrlQuoteDataSetJSonFormat = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(\'"
				+ _symbol + "\')%0A%09%09&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json";
		
		Log.i("string", myUrlQuoteDataSetJSonFormat );

		try {
			URL url = new URL(myUrlQuoteDataSetJSonFormat);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			// httpConnection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Connection", "close");
			connection.setInstanceFollowRedirects(true);
			connection.setUseCaches(false);
			connection.connect();

			InputStream in = new BufferedInputStream(connection.getInputStream());
			String jsonp = IOUtils.toString(in, "UTF-8");

			list = proceedJSonToObject(jsonp, mSymbols);

		} catch (IOException e) {
			return null;
		}
		return list;
	}
	
	private List<QuoteObject> proceedJSonToObject(String json, String[] symbols) {
		
		try {
			String mJSon = json;
			List<QuoteObject> list = new ArrayList<QuoteObject>();
			
			JSONObject jso = new JSONObject(mJSon).getJSONObject("query");
			JSONObject jsoa = jso.getJSONObject("results");
			JSONArray jsob = jsoa.getJSONArray("quote");
			
			for (int i=0; i<symbols.length; i++) {
				JSONObject jsoc = jsob.getJSONObject(i);
				QuoteObject obj = new QuoteObject();

				// String name = jsob.get("Name").toString();
				String prevClose = jsoc.get("PreviousClose").toString();
				String open = jsoc.get("Open").toString();
				String bid = jsoc.get("Bid").toString();
				String ask = jsoc.get("Ask").toString();
				String yearTargetEst = jsoc.get("OneyrTargetPrice").toString();
				String dayLow = jsoc.get("DaysLow").toString();
				String dayHigh = jsoc.get("DaysHigh").toString();
				String dayRange = jsoc.get("DaysRange").toString();
				String yearLow = jsoc.get("YearLow").toString();
				String yearHigh = jsoc.get("YearHigh").toString();
				String yearRange = jsoc.get("YearRange").toString();
				String volume = jsoc.get("Volume").toString();
				String avgVol = jsoc.get("AverageDailyVolume").toString();
				String marketCap = jsoc.get("MarketCapitalization").toString();
				String priceEarningRatio = jsoc.get("PERatio").toString();
				String earningPerShare = jsoc.get("EarningsShare").toString(); // epsTTM
	
				String dividendShare = jsoc.get("DividendShare").toString();
				String divAndYield = jsoc.getString("DividendYield").toString();
				String lastTradePrice = jsoc.get("LastTradePriceOnly").toString();
				String change = jsoc.get("Change").toString();
				String changeInPercent = jsoc.get("PercentChange").toString();
				String lastTradeDate = jsoc.get("LastTradeDate").toString();
				String lastTradeTime = jsoc.get("LastTradeTime").toString();
				String currency = jsoc.get("Currency").toString();
	
				obj.setSymbol(symbols[i]);
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
				
				
				list.add(obj);
			}
			return list;
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void setResult() {
		ls = mList;
		mCustomAdapter.clear();
		for (SymbolCallBackObject detail : ls) {
			mCustomAdapter.add(detail);
		}
		mCustomAdapter.notifyDataSetChanged();
		lv.setAdapter(mCustomAdapter);
	}
}
