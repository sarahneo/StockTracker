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

public class WidgetConfigPrefsActivityPortfolio extends Activity implements OnClickListener {

	private Fragment mFragment;
	private Button btnSave;
	private static final String TAG_PORTFOLIO_FRAGMENT = "TAG_PORTFOLIO_FRAGMENT";

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
			mFragment = new WidgetConfigPrefsFragmentPortfolio();
			fm.beginTransaction().add(R.id.container, mFragment, TAG_PORTFOLIO_FRAGMENT).commit();
		}else{
			mFragment = fm.findFragmentByTag(TAG_PORTFOLIO_FRAGMENT);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			((WidgetConfigPrefsFragmentPortfolio) mFragment).onBack();
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		((WidgetConfigPrefsFragmentPortfolio) mFragment).onBack();
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		int mId = v.getId();
		if (mId == R.id.btn_widget_config_done) {
			((WidgetConfigPrefsFragmentPortfolio) mFragment).onBack();
			finish();
		}
	}
}
