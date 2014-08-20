package com.handyapps.stocktracker.widget;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.ListPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;
import org.holoeverywhere.widget.Toast;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.utils.VersionHelper;

public class WidgetConfigPrefsFragmentPortfolio extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private String portName = "";
	private boolean status_old = false;
	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	private CharSequence[] entriesPort = null;
	private CharSequence[] entryValuesPort = null;

	private Resources res;
	private ListPreference pConfigurePortfolio;
	private SharedPreferences sp;
	private Bundle bundle = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_widget_config_portfolio);
		res = getActivity().getResources();
		sp = getPreferenceScreen().getSharedPreferences();

		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			mAppWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			status_old = bundle.getBoolean(WidgetProviderPortfolio.APP_WIDGET_STATUS_OLD_PORTFOLIO, false);
			portName = bundle.getString(Constants.KEY_PORTFOLIO_NAME);
		} else {
			myResultCanceled();
			getActivity().finish();
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			myResultCanceled();
			getActivity().finish();
		}
		
		if (!VersionHelper.isHoneyComb()) {
			String msg = res.getString(R.string.msg_widget_conflict);
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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
		if (key.equals(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET + mAppWidgetId)) {
			String defValue = "";
			String summary = "";
			if (entryValuesPort != null) {

				defValue = String.valueOf(entryValuesPort[0]);
				String sValue = prefs.getString(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET + mAppWidgetId, defValue);

				List<String> ls = new ArrayList<String>();
				for (CharSequence cs : entryValuesPort) {
					ls.add(String.valueOf(cs));
				}
				int mIndex = ls.indexOf(sValue);
				CharSequence csSummay = entriesPort[mIndex];
				summary = String.valueOf(csSummay);
			}
			pConfigurePortfolio.setSummary(summary);

			Bundle mBundle = getActivity().getIntent().getExtras();
			if (mBundle != null && summary.length() > 0) {
				getActivity().getIntent().putExtra(Constants.KEY_PORTFOLIO_NAME, summary);				
			}
		} else if (key.equals(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME)) {
			String value = sp.getString(key, "Light");
			Preference connectionPref = findPreference(key);
			connectionPref.setSummary(value);
		}
	}

	private void setupList() {

		pConfigurePortfolio = (ListPreference) findPreference(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET);
		pConfigurePortfolio.setKey(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET + mAppWidgetId);

		List<PortfolioObject> mPortList = DbAdapter.getSingleInstance().fetchPortfolioList();

		if (mPortList != null && mPortList.size() > 0) {
			entriesPort = new CharSequence[mPortList.size()];
			entryValuesPort = new CharSequence[mPortList.size()];

			for (int i = 0; i < mPortList.size(); i++) {
				String mPortName = mPortList.get(i).getName();
				entriesPort[i] = mPortName;
				int portId = mPortList.get(i).getId();
				entryValuesPort[i] = String.valueOf(portId);
			}

			// 1. Setup Entries and EntryValues:
			pConfigurePortfolio.setEntries(entriesPort);
			pConfigurePortfolio.setEntryValues(entryValuesPort);

			// setup defaultValue:
			setupDefaultValues();

		} else {
			pConfigurePortfolio.setEnabled(false);
			myResultCanceled();
			alertDialog();
		}
	}

	private void setupDefaultValues() {

		String defValue = "";
		String defSummary = "";

		List<String> ls = new ArrayList<String>();
		for (CharSequence cs : entriesPort) {
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
						csEntryValue = entryValuesPort[mIndex];
						defValue = String.valueOf(csEntryValue);
						defSummary = portName;
					} catch (Exception e) {
						defValue = String.valueOf(entryValuesPort[0]);
						defSummary = String.valueOf(entriesPort[0]);
					}

				} else {
					defValue = String.valueOf(entryValuesPort[0]);
					defSummary = String.valueOf(entriesPort[0]);
				}
			}
		} else {

			int mIndex = ls.indexOf(portName);
			CharSequence csEntryValue = null;
			try {
				csEntryValue = entryValuesPort[mIndex];
				defValue = String.valueOf(csEntryValue);
				defSummary = portName;

			} catch (Exception e) {
				defValue = String.valueOf(entryValuesPort[0]);
				defSummary = String.valueOf(entriesPort[0]);
			}
		}

		// 2. Set default value and summary:
		pConfigurePortfolio.setValue(defValue);
		pConfigurePortfolio.setSummary(defSummary);
		
		String theme = sp.getString(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME, "Light");
		Preference connectionPref = findPreference(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME);
		connectionPref.setSummary(theme);
	}

	public void onBack() {
		if (pConfigurePortfolio.isEnabled()) {
			updateWidgetByGivenAppWidgetId();
			sendMyBroadcasetUpdateWidget();
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

	private void sendMyBroadcasetUpdateWidget() {
		Intent updateWidgetIntent = new Intent(getActivity(), WidgetProviderPortfolio.class);
		updateWidgetIntent.setAction(WidgetProviderPortfolio.APP_WIDGET_UPDATE_PORTFOLIO);
		getActivity().sendBroadcast(updateWidgetIntent);
	}

	private void updateWidgetByGivenAppWidgetId() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
		WidgetProviderPortfolio.updateAppWidget(getActivity(), appWidgetManager, mAppWidgetId, false, false, false);
	}

	private void alertDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setIcon(R.drawable.ic_alerts_warning);
		dialog.setTitle(res.getString(R.string.warning));
		dialog.setMessage(res.getString(R.string.error_to_create_portfolio_widget_msg));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startMyIntent();
				getActivity().finish();
			}
		});
		dialog.create().show();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void startMyIntent() {
		int portfolioTabPosition = MainFragmentActivity.TAB_POSITION_PORTFOLIO;
		Intent i = new Intent(getActivity(), MainFragmentActivity.class);
		i.putExtra(Constants.KEY_TAB_POSITION, portfolioTabPosition);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(i);
	}
}