package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.ListPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.WatchlistObject;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

public class WidgetConfigPrefsFragmentWatchlistNoHc extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private boolean status_old = false;
	private CharSequence entriesWatch[] = null;
	private CharSequence entryValuesWatch[] = null;

	private Resources res;
	private ListPreference pConfigureWatchlist;
	private SharedPreferences sp;

	private Bundle bundle = null;

	private String watchName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_widget_config_watchlist);

		res = getActivity().getResources();
		sp = getPreferenceScreen().getSharedPreferences();
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			mAppWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			status_old = bundle.getBoolean(WidgetProviderWatchlistNoHc.APP_WIDGET_STATUS_OLD_WATCHLIST_NO_HC, false);
			watchName = bundle.getString(Constants.KEY_WATCHLIST_NAME);
		} else {
			myResultCanceled();
			getActivity().finish();
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			myResultCanceled();
			getActivity().finish();
		}

		setupList();
		sp.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sp.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

		if (key.equals(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET + mAppWidgetId)) {
			String defValue = "";
			String summary = "";
			if (entryValuesWatch != null) {
				defValue = String.valueOf(entryValuesWatch[0]);
				String sValue = prefs.getString(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET + mAppWidgetId, defValue);

				List<String> ls = new ArrayList<String>();
				for (CharSequence cs : entryValuesWatch) {
					ls.add(String.valueOf(cs));
				}
				int mIndex = ls.indexOf(sValue);
				CharSequence csSummay = entriesWatch[mIndex];
				summary = String.valueOf(csSummay);
			}
			pConfigureWatchlist.setSummary(summary);

			Bundle mBundle = getActivity().getIntent().getExtras();
			if (mBundle != null && summary.length() > 0) {
				getActivity().getIntent().putExtra(Constants.KEY_WATCHLIST_NAME, summary);
			}
		} else if (key.equals(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET_THEME)) {
			String value = sp.getString(key, "Light");
			Preference connectionPref = findPreference(key);
			connectionPref.setSummary(value);
		}
	}

	private void setupList() {

		pConfigureWatchlist = (ListPreference) findPreference(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET);
		pConfigureWatchlist.setKey(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET + mAppWidgetId);

		List<WatchlistObject> mWatchList = DbAdapter.getSingleInstance().fetchWatchlists();

		if (mWatchList != null && mWatchList.size() > 0) {
			entriesWatch = new CharSequence[mWatchList.size()];
			entryValuesWatch = new CharSequence[mWatchList.size()];

			for (int i = 0; i < mWatchList.size(); i++) {
				String mWatchName = mWatchList.get(i).getName();
				entriesWatch[i] = mWatchName;
				int watchId = mWatchList.get(i).getId();
				entryValuesWatch[i] = String.valueOf(watchId);
			}

			// 1. Setup Entries and EntryValues:
			pConfigureWatchlist.setEntries(entriesWatch);
			pConfigureWatchlist.setEntryValues(entryValuesWatch);

			// setup defaultValue:
			setupDefaultValues();

		} else {
			pConfigureWatchlist.setEnabled(false);
			myResultCanceled();
			alertDialog();
		}
	}

	private void setupDefaultValues() {

		String defValue = "";
		String defSummary = "";

		List<String> ls = new ArrayList<String>();
		for (CharSequence cs : entriesWatch) {
			ls.add(String.valueOf(cs));
		}

		if (!status_old) {
			if (bundle != null) {
				String mWatchName = bundle.getString(Constants.KEY_WATCHLIST_NAME);
				if (mWatchName != null && mWatchName.length() > 0) {
					defSummary = mWatchName;

					int mIndex = ls.indexOf(mWatchName);
					CharSequence csEntryValue = null;
					try {
						csEntryValue = entryValuesWatch[mIndex];
						defValue = String.valueOf(csEntryValue);
						defSummary = watchName;
					} catch (Exception e) {
						defValue = String.valueOf(entryValuesWatch[0]);
						defSummary = String.valueOf(entriesWatch[0]);
					}

				} else {
					defValue = String.valueOf(entryValuesWatch[0]);
					defSummary = String.valueOf(entriesWatch[0]);
				}
			}

		} else {

			int mIndex = ls.indexOf(watchName);
			CharSequence csEntryValue = null;
			try {
				csEntryValue = entryValuesWatch[mIndex];
				defValue = String.valueOf(csEntryValue);
				defSummary = watchName;
			} catch (Exception e) {
				defValue = String.valueOf(entryValuesWatch[0]);
				defSummary = String.valueOf(entriesWatch[0]);
			}
		}
		pConfigureWatchlist.setValue(defValue);
		pConfigureWatchlist.setSummary(defSummary);
		
		String theme = sp.getString(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET_THEME, "Light");
		Preference connectionPref = findPreference(Constants.SP_KEY_CONFIGURE_WATCHLIST_WIDGET_THEME);
		connectionPref.setSummary(theme);
	}

	public void onBack() {

		if (pConfigureWatchlist.isEnabled()) {
			updateWidgetByGivenAppWidgetId();
			myResultOk();
		} else {
			myResultCanceled();
		}
	}

	private void myResultOk() {
		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		getActivity().setResult(Activity.RESULT_OK, resultValue);
	}

	private void myResultCanceled() {
		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		getActivity().setResult(Activity.RESULT_CANCELED, resultValue);
	}

	private void updateWidgetByGivenAppWidgetId() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
		WidgetProviderWatchlistNoHc.updateAppWidget(getActivity(), appWidgetManager, mAppWidgetId, false, false, false);
	}

	private void alertDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setIcon(R.drawable.ic_alerts_warning);
		dialog.setTitle(res.getString(R.string.warning));
		dialog.setMessage(res.getString(R.string.error_to_create_watchlist_widget_msg));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				startMyIntent();
				getActivity().finish();
			}
		});
		dialog.create().show();
	}

	private void startMyIntent() {
		int watchlistTabPosition = MainFragmentActivity.TAB_POSITION_WATCHLIST;
		Intent i = new Intent(getActivity(), MainFragmentActivity.class);
		i.putExtra(Constants.KEY_TAB_POSITION, watchlistTabPosition);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(i);
	}
}