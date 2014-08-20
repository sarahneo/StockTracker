package com.handyapps.stocktracker.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.task.UpdateNewsTaskSingleSymbol;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class IntentServiceUpdateNews extends IntentService {

	public IntentServiceUpdateNews() {
		super("Intent Service Update News");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		boolean hasNetConnection = hasNetConnection();
		Bundle bundle = intent.getExtras();
		String symbol = "";

		if (bundle != null && hasNetConnection == true) {

			boolean isUpdateNews = bundle.getBoolean(Constants.KEY_IS_UPDATE_NEWS);
			symbol = bundle.getString(Constants.KEY_SYMBOL);
			if (isUpdateNews) {
				updateNews(symbol);
				Log.d("AlarmReceiver", "About to execute MyTask,symbol=["+symbol+"]");
			} 
		}
	}

	private void updateNews(String symbol) {
		//5. final execute task:
		UpdateNewsTaskSingleSymbol task = new UpdateNewsTaskSingleSymbol(this.getApplicationContext());
		task.execute(symbol);
	}

	private boolean hasNetConnection() {
		boolean hasNetConn = NetworkConnectivity.hasNetworkConnection(this.getApplicationContext());
		return hasNetConn;
	}
}