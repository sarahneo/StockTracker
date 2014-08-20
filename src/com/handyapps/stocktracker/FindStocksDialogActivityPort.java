package com.handyapps.stocktracker;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog.AddNewPortfolioDialogListener;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog.AddNewWatchlistDialogListener;
import com.handyapps.stocktracker.fragments.FindStocksMainFragmentWithResultCode;

public class FindStocksDialogActivityPort extends Activity implements 
					AddNewPortfolioDialogListener, AddNewWatchlistDialogListener {
	
	private static final String TAG_DIALOG_FRAGMENT_PORT = "TAG_DIALOG_FRAGMENT_PORT";
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		//ThemeUtils.onActivityCreateSetTheme(this);
		
		setContentView(R.layout.container_frame_layout);
		FragmentManager fm = getSupportFragmentManager();
		Fragment mFragment;
		if (bundle == null) {
			mFragment = new FindStocksMainFragmentWithResultCode();
			Bundle newBundle = new Bundle();
			newBundle.putBoolean(Constants.KEY_IS_HIDE_CARDS, true);
			mFragment.setArguments(newBundle);
			fm.beginTransaction().add(R.id.container, mFragment, TAG_DIALOG_FRAGMENT_PORT).commit();
		}else{
			mFragment = fm.findFragmentByTag(TAG_DIALOG_FRAGMENT_PORT);
		}
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
    	if (dialog.getTag().equals(Constants.DIALOG_ADD_PORTFOLIO))
    		sendBroadastUpdatePagerIndicator(false);
    	else
    		sendBroadastUpdatePagerIndicator(true);
		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		
	}
	
	private void sendBroadastUpdatePagerIndicator(boolean isUpdateWL) {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		if (isUpdateWL)
			i.putExtra(Constants.KEY_UPDATE_WATCHLISTS_PAGER, true);
		sendBroadcast(i);
	}
}
