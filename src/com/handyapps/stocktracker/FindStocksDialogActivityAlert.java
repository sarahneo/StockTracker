package com.handyapps.stocktracker;

import org.holoeverywhere.app.Activity;

import com.handyapps.stocktracker.fragments.FindStocksMainFragmentWithResultCode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FindStocksDialogActivityAlert extends Activity {

	private static final String TAG_DIALOG_FRAGMENT_ALERT = "TAG_DIALOG_FRAGMENT_ALERT";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.container_frame_layout);
		FragmentManager fm = getSupportFragmentManager();
		Fragment mFragment;
		if (bundle == null) {
			mFragment = new FindStocksMainFragmentWithResultCode();
			Bundle newBundle = new Bundle();
			newBundle.putBoolean(Constants.KEY_IS_HIDE_CARDS, true);
			mFragment.setArguments(newBundle);
			fm.beginTransaction().add(R.id.container, mFragment, TAG_DIALOG_FRAGMENT_ALERT).commit();
		} else {
			mFragment = fm.findFragmentByTag(TAG_DIALOG_FRAGMENT_ALERT);
		}
	}
}
