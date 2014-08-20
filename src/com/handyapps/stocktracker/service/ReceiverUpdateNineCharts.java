package com.handyapps.stocktracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.task.UpdateChartTaskFull;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

//**This class only used to auto updates with 9charts tab on running state.
public class ReceiverUpdateNineCharts extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		boolean hasConnection = NetworkConnectivity.hasNetworkConnection(ctx);
		Bundle bundle = intent.getExtras();
		String mAction = intent.getAction();
		if (mAction.equals(Constants.ACTION_RECEIVER_UPDATE_NINE_CHARTS)) {
			if (bundle != null && hasConnection == true) {
				String mSymbol = bundle.getString(Constants.KEY_SYMBOL);
				updateNineCharts(ctx, mSymbol);
			}
		}
	}

	private void updateNineCharts(Context ctx, String symbol) {
		Context mCtx = ctx;
		String mSymbol = symbol;
		boolean isOneAndFiveDayChart = true; //**if it's true, will only update 1d, and 5d chart.
		UpdateChartTaskFull taskChart = new UpdateChartTaskFull(mCtx, isOneAndFiveDayChart);
		taskChart.execute(mSymbol);
	}
}
