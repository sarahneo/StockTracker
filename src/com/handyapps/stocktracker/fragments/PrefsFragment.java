package com.handyapps.stocktracker.fragments;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.preference.CheckBoxPreference;
import org.holoeverywhere.preference.ListPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.preference.SharedPreferences.OnSharedPreferenceChangeListener;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.ImportCSV;
import com.handyapps.stocktracker.dialogs.SetAlertsWindowDialog;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.utils.ThemeUtils;

public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, 
		OnPreferenceClickListener {

	private String[] arrRefreshFrequncyValues;
	private String[] arrStartingPageValues;
	private String[] arrThemeValues;
	
	private String defaultAlertsWindow = "";

	private SharedPreferences sp;
	private Preference pAlertsWindow;
	private Preference pImportCSV;
	private Preference pOtherApplications;
	private Preference pTellFriend;
	private ListPreference ptartingPage;
	private ListPreference pRefreshFrequency;
	private ListPreference pAppTheme;
	private CheckBoxPreference pAutoRefresh;
	private CheckBoxPreference pAutoAddNewsAlert;
	private CheckBoxPreference pIsSetAlertsWindow;
	private Resources res;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeUtils.onActivityCreateSetTheme(getSupportActivity(), false);
		addPreferencesFromResource(R.xml.preferences);
		res = getActivity().getResources();
		sp = getPreferenceScreen().getSharedPreferences();

		// setting on create done - this is to void 'Auto Update Service' run
		// updates during onCreate the time
		sp.edit().putBoolean(Constants.SP_KEY_IS_SETTING_ON_CREATE, true).commit();

		ptartingPage = (ListPreference) findPreference(Constants.SP_KEY_DEFAULT_STARTING_PAGE);
		pRefreshFrequency = (ListPreference) findPreference(Constants.SP_KEY_REFRESH_FREQUENCY);
		pAppTheme = (ListPreference) findPreference(Constants.SP_KEY_APP_THEME);
		pAutoRefresh = (CheckBoxPreference) findPreference(Constants.SP_KEY_AUTO_REFRESH);
		pAutoAddNewsAlert = (CheckBoxPreference) findPreference(Constants.SP_KEY_AUTO_ADD_NEWS_ALERT);
		pIsSetAlertsWindow = (CheckBoxPreference) findPreference(Constants.SP_KEY_IS_SET_ALERTS_WINDOW);
		pAlertsWindow = (Preference) findPreference("sp_key_alerts_window");
		pImportCSV = (Preference) findPreference("sp_key_import_csv");
		pOtherApplications = (Preference) findPreference(Constants.SP_KEY_OTHER_APPLICATIONS);
		pTellFriend = (Preference) findPreference(Constants.SP_KEY_TELL_A_FRIEND);

		setDefaultValue();

		// setting on create done - this is to void 'Auto Update Service' run
		// updates during onCreate the time
		sp.edit().putBoolean(Constants.SP_KEY_IS_SETTING_ON_CREATE, false).commit();
		sp.registerOnSharedPreferenceChangeListener(this);
		pAlertsWindow.setOnPreferenceClickListener(PrefsFragment.this);
		pImportCSV.setOnPreferenceClickListener(PrefsFragment.this);
		pOtherApplications.setOnPreferenceClickListener(PrefsFragment.this);
		pTellFriend.setOnPreferenceClickListener(PrefsFragment.this);
	}
	

	private void setDefaultValue() {
		// set default value
		arrStartingPageValues = res.getStringArray(R.array.defult_starting_page_entryvalues);
		String sStartingPageValue = sp.getString(Constants.SP_KEY_DEFAULT_STARTING_PAGE, arrStartingPageValues[2]);
		setStartingPageSummary(sStartingPageValue);
		ptartingPage.setValue(sStartingPageValue);

		// set default value
		arrRefreshFrequncyValues = res.getStringArray(R.array.sp_refresh_frequency_values);
		String sRefreshFrequencyValue = sp.getString(Constants.SP_KEY_REFRESH_FREQUENCY, arrRefreshFrequncyValues[1]);
		String sRefreshFrequencySummary = getFrequencySummary(sRefreshFrequencyValue);
		pRefreshFrequency.setSummary(sRefreshFrequencySummary);
		pRefreshFrequency.setValue(sRefreshFrequencyValue);
		
		// set default value
		arrThemeValues = res.getStringArray(R.array.sp_app_theme_entries);
		String sAppThemeSummary = sp.getString(Constants.SP_KEY_APP_THEME, arrThemeValues[0]);
		pAppTheme.setSummary(sAppThemeSummary);

		// set default value
		boolean isAutoRefreshValue = sp.getBoolean(Constants.SP_KEY_AUTO_REFRESH, true);
		pAutoRefresh.setChecked(isAutoRefreshValue);
		
		// set default value
		boolean isAutoAddValue = sp.getBoolean(Constants.SP_KEY_AUTO_ADD_NEWS_ALERT, true);
		pAutoAddNewsAlert.setChecked(isAutoAddValue);
		
		// set default value
		boolean isSetWindow = sp.getBoolean(Constants.SP_KEY_IS_SET_ALERTS_WINDOW, false);
		pIsSetAlertsWindow.setChecked(isSetWindow);
		
		// set default value
		defaultAlertsWindow = res.getString(R.string.default_alerts_window);
		String alertsWindow = sp.getString(Constants.SP_KEY_ALERTS_WINDOW, defaultAlertsWindow);
		pAlertsWindow.setSummary(alertsWindow);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sp.unregisterOnSharedPreferenceChangeListener(PrefsFragment.this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		String key = preference.getKey();

		if (key.equalsIgnoreCase(Constants.SP_KEY_OTHER_APPLICATIONS)) {
			viewOurOtherApp();
		} else if (key.equalsIgnoreCase(Constants.SP_KEY_TELL_A_FRIEND)) {
			tellFriend();
		} else if (key.equalsIgnoreCase(Constants.SP_KEY_ALERTS_WINDOW)) {
			SetAlertsWindowDialog dialog = new SetAlertsWindowDialog();
			dialog.show(getActivity().getSupportFragmentManager(), Constants.SP_KEY_ALERTS_WINDOW);
		} else if (key.equalsIgnoreCase("sp_key_import_csv")) {
			Intent importIntent = new Intent(getActivity(), ImportCSV.class);
			startActivity(importIntent);
		}
		return false;
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

		if (key.equals(Constants.SP_KEY_DEFAULT_STARTING_PAGE)) {
			String sValue = prefs.getString(Constants.SP_KEY_DEFAULT_STARTING_PAGE, arrStartingPageValues[0]);
			setStartingPageSummary(sValue);
		} else if (key.equals(Constants.SP_KEY_REFRESH_FREQUENCY)) {
			String sValue = prefs.getString(Constants.SP_KEY_REFRESH_FREQUENCY, arrRefreshFrequncyValues[1]);// default
																												// 5mins
			String summary = getFrequencySummary(sValue);
			pRefreshFrequency.setSummary(summary);
			editAlarm();
		} else if (key.equals(Constants.SP_KEY_AUTO_REFRESH)) {
			editAlarm();
		} else if (key.equals(Constants.SP_KEY_ALERTS_WINDOW)) {
			String summary = prefs.getString(Constants.SP_KEY_ALERTS_WINDOW, defaultAlertsWindow);
			pAlertsWindow.setSummary(summary);
		} else if (key.equals(Constants.SP_KEY_APP_THEME)) {
			String summary = prefs.getString(key, arrThemeValues[0]);
			pAppTheme.setSummary(summary);
			if (summary.equals(arrThemeValues[0]))
				ThemeUtils.changeToTheme(getSupportActivity(), ThemeUtils.LIGHT);
			else
				ThemeUtils.changeToTheme(getSupportActivity(), ThemeUtils.DARK);
		}
	}

	private void editAlarm() {
		MyAlarmManager mAlarm = new MyAlarmManager(getActivity());
		mAlarm.setAlarm();
	}

	private String getFrequencySummary(String sValue) {

		String mValue = sValue;

		String[] arrRefreshFrequncyValues = res.getStringArray(R.array.sp_refresh_frequency_values);
		String[] arrRefreshFrequncyEnties = res.getStringArray(R.array.sp_refresh_frequency_entries);

		List<String> list = new ArrayList<String>();
		for (String s : arrRefreshFrequncyValues) {
			list.add(s);
		}

		int index = list.indexOf(mValue);
		String summary = arrRefreshFrequncyEnties[index];
		return summary;
	}

	private void setStartingPageSummary(String sValue) {

		String mValue = sValue;
		String[] arrValues = res.getStringArray(R.array.defult_starting_page_entryvalues);
		String[] arrEntries = res.getStringArray(R.array.defult_starting_page_entries);

		List<String> list = new ArrayList<String>();
		for (String s : arrValues) {
			list.add(s);
		}

		int index = list.indexOf(mValue);
		String summary = arrEntries[index];
		ptartingPage.setSummary(summary);
	}

	private void viewOurOtherApp() {
		String url = Constants.GOOGLE_PLAY_MORE_APPS;
		Intent viewOurOtherApp = new Intent(Intent.ACTION_VIEW);
		viewOurOtherApp.setData(Uri.parse(url));
		startActivity(viewOurOtherApp);
	}

	private void tellFriend() {
		try {
			Resources rs = getResources();
			String appName = rs.getString(R.string.app_name);
			String recommandData = "\n" + rs.getString(R.string.recommand_this_app) + "\n\n";
			recommandData = recommandData + Constants.STOCK_TRACKERS_URL + "\n\n";
			String chooserTile = rs.getString(R.string.share_by);

			Intent targetInt = new Intent(Intent.ACTION_SEND);
			targetInt.setType("text/plain");
			targetInt.putExtra(Intent.EXTRA_SUBJECT, appName);
			targetInt.putExtra(Intent.EXTRA_TEXT, recommandData);
			startActivity(Intent.createChooser(targetInt, chooserTile));

		} catch (Exception e) {
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}