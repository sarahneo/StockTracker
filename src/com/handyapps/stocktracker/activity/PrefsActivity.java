package com.handyapps.stocktracker.activity;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.fragments.PrefsFragment;
import com.handyapps.stocktracker.utils.ThemeUtils;

public class PrefsActivity extends Activity {

	protected void onCreate(Bundle bundles) {
		super.onCreate(bundles);
		
		ThemeUtils.onActivityCreateSetTheme(this, false);
		
		//Spanned spanned = TextColorPicker.getAppLabelSettings(getApplicationContext());		
		ActionBar actionBar = getSupportActionBar();

		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
		setContentView(R.layout.container_pref_layout);

		FragmentManager fm = getSupportFragmentManager();

		if (bundles == null) {
			Fragment mFragment = new PrefsFragment();
	     	fm.beginTransaction().add(R.id.container, mFragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
}
