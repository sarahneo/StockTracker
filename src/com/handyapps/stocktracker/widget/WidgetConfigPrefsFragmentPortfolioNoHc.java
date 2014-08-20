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
import com.handyapps.stocktracker.model.PortfolioObject;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

public class WidgetConfigPrefsFragmentPortfolioNoHc extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private boolean status_old = false;
	private CharSequence entriesPort[] = null;
	private CharSequence entryValuesPort[] = null;

	private Resources res;
	private ListPreference pConfigurePortfolio;
	private SharedPreferences sp;

	private Bundle bundle = null;

	private String portName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_widget_config_portfolio);

		res = getActivity().getResources();
		sp = getPreferenceScreen().getSharedPreferences();
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			mAppWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			status_old = bundle.getBoolean(WidgetProviderPortfolioNoHc.APP_WIDGET_STATUS_OLD_PORTFOLIO_NO_HC, false);
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
				int watchId = mPortList.get(i).getId();
				entryValuesPort[i] = String.valueOf(watchId);
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
				String mPortName = bundle.getString(Constants.KEY_PORTFOLIO_NAME);
				if (mPortName != null && mPortName.length() > 0) {
					defSummary = mPortName;

					int mIndex = ls.indexOf(mPortName);
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
		pConfigurePortfolio.setValue(defValue);
		pConfigurePortfolio.setSummary(defSummary);
		
		String theme = sp.getString(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME, "Light");
		Preference connectionPref = findPreference(Constants.SP_KEY_CONFIGURE_PORTFOLIO_WIDGET_THEME);
		connectionPref.setSummary(theme);
	}

	public void onBack() {

		if (pConfigurePortfolio.isEnabled()) {
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
		WidgetProviderPortfolioNoHc.updateAppWidget(getActivity(), appWidgetManager, mAppWidgetId, false, false, false);
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

	private void startMyIntent() {
		int portTabPosition = MainFragmentActivity.TAB_POSITION_PORTFOLIO;
		Intent i = new Intent(getActivity(), MainFragmentActivity.class);
		i.putExtra(Constants.KEY_TAB_POSITION, portTabPosition);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(i);
	}
}