package com.handyapps.stocktracker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Context;
import android.view.View;

public class MyBannerAd {
	private View mView;
	private Context mContext;
	
	public MyBannerAd(View v, Context c) {
		this.mView = v;
		this.mContext = c;
	}
	
	public void loadAd() {
		AdView adView = (AdView) mView.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
			.addTestDevice(mContext.getString(R.string.admob_test_device_id))
			.build();
		adView.loadAd(adRequest);
	}
}
