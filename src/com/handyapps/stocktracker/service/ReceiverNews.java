package com.handyapps.stocktracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.utils.MyDateFormat;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class ReceiverNews extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		boolean hasConnection = NetworkConnectivity.hasNetworkConnection(ctx);
		String symbol = "";
		if (arg1 != null && arg1.getExtras() != null)  {
			Bundle bun = arg1.getExtras();
			symbol = bun.getString(Constants.KEY_SYMBOL);
		}
		if (hasConnection) {
			updateNews(ctx, symbol);
		}		
	}

	private void updateNews(Context ctx, String symbol) {
		
		if (MyDateFormat.isDoUpdate(ctx)) {
			Log.d("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive,symbol=["+symbol+"]");
			Intent i = new Intent(ctx, IntentServiceUpdateNews.class);
			i.putExtra(Constants.KEY_IS_UPDATE_NEWS, true);
			i.putExtra(Constants.KEY_SYMBOL, symbol);
			ctx.startService(i);
		}
	}

}
