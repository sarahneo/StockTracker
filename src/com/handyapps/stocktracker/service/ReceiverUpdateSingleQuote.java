package com.handyapps.stocktracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.NetworkConnectivity;

//**This class only used to auto updates with TrasactionActivity on running state.
public class ReceiverUpdateSingleQuote extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		boolean hasConnection = NetworkConnectivity.hasNetworkConnection(ctx);
		Bundle bundle = intent.getExtras();
		String mAction = intent.getAction();
		if (mAction.equals(Constants.ACTION_RECEIVER_UPDATE_SINGLE_QUOTE)) {
			if (bundle != null && hasConnection == true) {
				String mSymbol = bundle.getString(Constants.KEY_SYMBOL);
				updateSingleQuote(ctx, mSymbol);
			}
		}
	}

	private void updateSingleQuote(Context ctx, String symbol) {
		Context mCtx = ctx;
		String mSymbol = symbol;
		UpdateQuoteTaskSingleSymbol task = new UpdateQuoteTaskSingleSymbol(mCtx, null, 0, 0);
		task.execute(mSymbol);
	}
}
