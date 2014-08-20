package com.handyapps.stocktracker.service;

import com.handyapps.stocktracker.Constants;

import android.content.Context;
import android.content.Intent;

public class BroadcaseUtils {
	
	public static void portChangeSinglePageName(String portName, Context context){
		String mPortName = portName;
		Context mCtx = context;
		String mAction = Constants.ACTION_PORTFOLIO_INITIAL_PAGER_ACTIVITY;
		Intent i = new Intent(mAction);
		i.putExtra(Constants.KEY_IS_CHANGE_PORT_NAME, true);
		i.putExtra(Constants.KEY_PORTFOLIO_NAME, mPortName);
		mCtx.sendBroadcast(i);
	}
	
	public static void portRefreshSinglePage(String portName, Context context){
		String mPortName = portName;
		Context mCtx = context;
		String pAction = Constants.ACTION_SINGLE_PORT_FRAGMENT_BROADCASE + mPortName;
		Intent iSinglePortFragment = new Intent(pAction);
		iSinglePortFragment.putExtra(Constants.KEY_PORTFOLIO_NAME, portName);
		mCtx.sendBroadcast(iSinglePortFragment);
	}

}
