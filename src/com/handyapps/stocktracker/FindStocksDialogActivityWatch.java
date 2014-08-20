package com.handyapps.stocktracker;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.handyapps.stocktracker.fragments.FindStocksMainFragmentWithResultCode;

public class FindStocksDialogActivityWatch extends Activity {

	private static final String TAG_DIALOG_FRAGMENT_WATCH = "TAG_DIALOG_FRAGMENT_WATCH";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.container_frame_layout);
		FragmentManager fm = getSupportFragmentManager();
		Fragment mFragment;
		if (bundle == null) {
			mFragment = new FindStocksMainFragmentWithResultCode();
			fm.beginTransaction().add(R.id.container, mFragment, TAG_DIALOG_FRAGMENT_WATCH).commit();
		} else {
			mFragment = fm.findFragmentByTag(TAG_DIALOG_FRAGMENT_WATCH);
		}
	}
}
