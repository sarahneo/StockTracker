package com.handyapps.stocktracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.task.PurgeOlderChart;
import com.handyapps.stocktracker.utils.MyDateFormat;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

public class ReceiverQuoteAll extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		boolean hasConnection = NetworkConnectivity.hasNetworkConnection(ctx);
		if (hasConnection) {
			updateQuoteAllTickers(ctx);
			purgeOlderChart();
		}
	}

	private void updateQuoteAllTickers(Context ctx) {
		if (MyDateFormat.isDoUpdate(ctx)) {
			Log.d("AlarmReceiver", "Called context.startService from AlarmReceiver.onReceive,ReceiverQuoteAll");
			String mAction = Constants.ACTION_INTENT_SERVICE_UPDATE_QUOTE;
			Intent i = new Intent(ctx, IntentServiceUpdateQuote.class);
			i.setAction(mAction);
			i.putExtra(Constants.KEY_IS_UPDATE_QUOTE_ALL_TICKERS, true);
			ctx.startService(i);
		}
	}
	
	private void purgeOlderChart() {
		int olderDay = Constants.PURGE_CHART_OLDER_DAYS;
		PurgeOlderChart purge = new PurgeOlderChart(olderDay);
		purge.execute();
	}
	
}
