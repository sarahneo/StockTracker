package com.handyapps.stocktracker;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Toast;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.handyapps.stocktracker.adapter.ChartFragmentAdapter;
import com.handyapps.stocktracker.model.ChartInfoObject;
import com.handyapps.stocktracker.model.ChartManager;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateChartTaskFull;
import com.handyapps.stocktracker.utils.NetworkConnectivity;
import com.viewpagerindicator.TabPageIndicator;

public class ChartTitlesInitialPage extends Activity {

	private String symbol;
	public static String KEY_BUNDLE_SYMBOL = "KEY_BUNDLE_SYMBOL";
	public static String KEY_BUNDLE_DATE_PERIOD = "KEY_BUNDLE_DATE_PERIOD";
	public static String KEY_BUNDLE_DATE_PERIOD_INDICATOR_TEXT = "KEY_BUNDLE_DATE_PERIOD_INDICATOR_TEXT";
	private static final String KEY_IS_ALARM_STARTED = "KEY_IS_ALARM_STARTED";
	private static final String KEY_MY_BUNDLE = "KEY_MY_BUNDLE";

	private Bundle bundle;
	private Resources res;
	private UpdateChartTaskFull fullChartsTask = null;
	private List<ChartInfoObject> chartList;
	private SharedPreferences sp;
	private boolean isAlarmStarted = false;

	private ChartFragmentAdapter mAdapter;
	private ViewPager mPager;
	private TabPageIndicator mIndicator;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.single_chart_pager_layout);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		res = getResources();
		chartList = new ArrayList<ChartInfoObject>();
		
		MyBannerAd ad = new MyBannerAd(findViewById(android.R.id.content), getApplicationContext());
		ad.loadAd();

		bundle = getIntent().getExtras();
		if (bundle != null) {
			String symbol = bundle.getString(Constants.KEY_SYMBOL);
			chartList = getChartList(symbol);
			startAlarm(symbol);
		}

		mAdapter = new ChartFragmentAdapter(getSupportFragmentManager(), chartList);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setCurrentItem(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean hasConn = NetworkConnectivity.hasNetworkConnection(ChartTitlesInitialPage.this);
		if (!hasConn) {
			Toast.makeText(ChartTitlesInitialPage.this, res.getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
		}

		if (bundle != null && hasConn == true) {
			symbol = bundle.getString(Constants.KEY_SYMBOL);
			chartList = getChartList(symbol);
			updateFullCharts();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		int orientation = newConfig.orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
		    Log.d("tag", "Portrait");
		    finish();
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) 
		    Log.d("tag", "Landscape");
		else
		    Log.w("tag", "other: " + orientation);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_ALARM_STARTED, isAlarmStarted);
		outState.putBundle(KEY_MY_BUNDLE, bundle);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isAlarmStarted = savedInstanceState.getBoolean(KEY_IS_ALARM_STARTED, false);
		bundle = savedInstanceState.getBundle(KEY_MY_BUNDLE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopAlarm(symbol);
		if (fullChartsTask != null) {
			fullChartsTask.cancel(true);
			fullChartsTask = null;
		}
	}

	private void updateFullCharts() {

		boolean isOneAndFiveDayChart = false; //**if it's false, will update nine-chart:
		if (fullChartsTask == null) {
			fullChartsTask = new UpdateChartTaskFull(this, isOneAndFiveDayChart);
			fullChartsTask.execute(symbol);
		} else {
			fullChartsTask.cancel(true);
			fullChartsTask = new UpdateChartTaskFull(this, isOneAndFiveDayChart);
			fullChartsTask.execute(symbol);
		}
	}

	private List<ChartInfoObject> getChartList(String symbol) {

		List<ChartInfoObject> mList = new ArrayList<ChartInfoObject>();
		String[] dateRangesUiShow = res.getStringArray(R.array.date_ranges_chart);
		String[] chartDateRanges = ChartManager.chartDateRanges;
		ChartInfoObject object;
		for (int i = 0; i < dateRangesUiShow.length; i++) {
			object = new ChartInfoObject();
			object.setDatePeriodUiShow(dateRangesUiShow[i]);
			object.setDatePeriod(chartDateRanges[i]);
			object.setSymbol(symbol);
			mList.add(object);
		}
		return mList;
	}

	private void startAlarm(String symbol) {
		boolean isAutoRefreshOn = sp.getBoolean(Constants.SP_KEY_AUTO_REFRESH, Constants.SP_AUTO_REFRESH_DEFAULT_ON_OFF);
		if (isAutoRefreshOn) {
			isAlarmStarted = true;
			MyAlarmManager am = new MyAlarmManager(this);
			am.startRepeatAlarmUpdateChart(symbol);
		}
	}

	private void stopAlarm(String symbol) {
		if (isAlarmStarted) {
			MyAlarmManager am = new MyAlarmManager(this);
			am.stopRepeatAlarmUpdateChart(symbol);
		}
	}
}
