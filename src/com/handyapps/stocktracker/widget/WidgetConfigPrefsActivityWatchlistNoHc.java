package com.handyapps.stocktracker.widget;

import org.holoeverywhere.app.Activity;

import com.handyapps.stocktracker.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.handyapps.stocktracker.utils.TextColorPicker;

public class WidgetConfigPrefsActivityWatchlistNoHc extends Activity implements OnClickListener {

	private Fragment mFragment;
	private Button btnSave;
	private static final String TAG_WATCHLIST_FRAGMENT_NO_HC = "TAG_WATCHLIST_FRAGMENT_NO_HC";

	protected void onCreate(Bundle bundles) {
		super.onCreate(bundles);

		Spanned spanned = TextColorPicker.getAppLabelWidgetConfigure(getApplicationContext());
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(spanned);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.container_frame_layout_widget);
		btnSave = (Button) findViewById(R.id.btn_widget_config_done);
		btnSave.setOnClickListener(this);

		FragmentManager fm = getSupportFragmentManager();

		if (bundles == null) {
			mFragment = new WidgetConfigPrefsFragmentWatchlistNoHc();
			fm.beginTransaction().add(R.id.container, mFragment, TAG_WATCHLIST_FRAGMENT_NO_HC).commit();
		} else {
			mFragment = fm.findFragmentByTag(TAG_WATCHLIST_FRAGMENT_NO_HC);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			((WidgetConfigPrefsFragmentWatchlistNoHc) mFragment).onBack();
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		((WidgetConfigPrefsFragmentWatchlistNoHc) mFragment).onBack();
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		int mId = v.getId();
		if (mId == R.id.btn_widget_config_done) {
			((WidgetConfigPrefsFragmentWatchlistNoHc) mFragment).onBack();
			finish();
		}
	}
}
