package com.handyapps.stocktracker.task;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.holoeverywhere.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.adapter.FindStocksAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.model.TransactionObject;

/**
 * Symbol Lookup Call back task
 */
public class FindStocksCallbackTask extends AsyncTask<String, Void, Boolean> {

	private ArrayList<SymbolCallBackObject> mList;
	private HttpURLConnection connection = null;
	private FragmentActivity activity;
	private ProgressBar pb;
	private ArrayList<SymbolCallBackObject> ls;
	private ListView lv;
	private int fromId;
	private int portId;
	private TransactionObject to;
	private FindStocksAdapter mCustomAdapter;

	public FindStocksCallbackTask(FragmentActivity activity, ProgressBar pb, 
			ArrayList<SymbolCallBackObject> ls, FindStocksAdapter customAdapter, ListView lv, 
			int fromId, int portId, TransactionObject to) {
		this.activity = activity;
		this.pb = pb;
		this.ls = ls;
		this.mCustomAdapter = customAdapter;
		this.lv = lv;
		this.fromId = fromId;
		this.portId = portId;
		this.to = to;
		
		mCustomAdapter = new FindStocksAdapter(activity, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, this.ls);
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
		
		if (fromId == Constants.FROM_ADD_TRADE && mList.size() > 0) {
			int stockId = 0;
			
			// assume only one item in array list
			SymbolCallBackObject scbo = mList.get(0);
			
			// 1. Create Stock Object
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(scbo.getSymbol());
			if (so == null) {
				so = new StockObject();
				so.setExch(scbo.getExch());
				so.setExchDisp(scbo.getExchDisp());
				so.setName(scbo.getName());
				so.setSymbol(scbo.getSymbol());
				so.setType(scbo.getType());
				so.setTypeDisp(scbo.getTypeDisp());
				so.setCurrency("USD");
				so.insert();
				stockId = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(scbo.getSymbol()).getId();
			} else {
				stockId = so.getId();
			}
			
			// 2. Create PortfolioStockObject
			PortfolioStockObject pso = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portId, stockId);
			if (pso == null) {
				pso = new PortfolioStockObject();
				pso.setPortfolioId(portId);
				pso.setStockId(stockId);
				pso.insert();
			}
			
			// 3. Create TransactionObject
			to.setStockId(stockId);
			to.insert();
			
			// 4. Create QuoteObject
			UpdateQuoteTaskSingleSymbol task = new UpdateQuoteTaskSingleSymbol(activity, null, 0, 0);
			task.execute(scbo.getSymbol());
			
		} else if (fromId == Constants.FROM_ADD_ALERT_DIALOG && mList.size() > 0) {
			
			// assume only one item in array list
			SymbolCallBackObject scbo = mList.get(0);
			
			// 1. Create Stock Object
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(scbo.getSymbol());
			if (so == null) {
				Log.i("stockObj", "stockObj is null");
			} else {
				so.setExch(scbo.getExch());
				so.setExchDisp(scbo.getExchDisp());				
				so.setType(scbo.getType());
				so.setTypeDisp(scbo.getTypeDisp());				
				so.update();
			}
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
