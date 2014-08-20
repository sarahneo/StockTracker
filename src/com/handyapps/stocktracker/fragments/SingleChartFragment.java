package com.handyapps.stocktracker.fragments;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.handyapps.stocktracker.ChartTitlesInitialPage;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.model.ChartInfoObject;
import com.handyapps.stocktracker.model.ChartManager;

public class SingleChartFragment extends Fragment {

	private String KEY_BUNDLE_DATE_PERIOD = "KEY_BUNDLE_DATE_PERIOD";
	private Bundle bundle;
	private MyBroadcastReceiver receiver;
	private ImageViewTouch iv;

	public static SingleChartFragment newInstance(ChartInfoObject object) {
		SingleChartFragment fragment = new SingleChartFragment();
		Bundle mBundle = new Bundle();
		mBundle.putString(ChartTitlesInitialPage.KEY_BUNDLE_SYMBOL, object.getSymbol());
		mBundle.putString(ChartTitlesInitialPage.KEY_BUNDLE_DATE_PERIOD, object.getDatePeriod());
		mBundle.putString(ChartTitlesInitialPage.KEY_BUNDLE_DATE_PERIOD_INDICATOR_TEXT, object.getDatePeriodUiShow());
		fragment.KEY_BUNDLE_DATE_PERIOD = object.getDatePeriod();
		fragment.bundle = mBundle;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			bundle = savedInstanceState.getBundle(KEY_BUNDLE_DATE_PERIOD);
		}

		if (bundle != null) {
			registerBroadcast();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_BUNDLE_DATE_PERIOD, bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.single_chart_layout, container, false);
		iv = (ImageViewTouch) view.findViewById(R.id.iv_chart);
		DisplayType mDisplayType = DisplayType.FIT_TO_SCREEN;
		iv.setDisplayType(mDisplayType);
		setupImage();
		return view;
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		String action = Constants.ACTION_SINGLE_CHART_FRAGMENT + bundle.getString(ChartTitlesInitialPage.KEY_BUNDLE_DATE_PERIOD);
		filter.addAction(action);
		receiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);
	}

	private void setupImage() {
		File mFile = getImageFile();
		if (mFile != null) {
			Bitmap mBitmap = getBitmap(mFile);
			if (mBitmap != null) {
				iv.setImageBitmap(mBitmap);
			}
		}else{
			iv.setImageResource(R.drawable.yhoo_chart);
		}
	}

	private File getImageFile() {
		String dayPeriod = bundle.getString(ChartTitlesInitialPage.KEY_BUNDLE_DATE_PERIOD);
		String symbol = bundle.getString(ChartTitlesInitialPage.KEY_BUNDLE_SYMBOL);
		String chartName = symbol + dayPeriod;
		File mFile = ChartManager.getIntenalChart(chartName);
		if (mFile != null) {
			return mFile;
		}
		return null;
	}

	private Bitmap getBitmap(File file) {
		File mFile = file;
		String path = mFile.getAbsolutePath();
		Options opt = new BitmapFactory.Options();
		opt.inDither = false; // important
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap mBitmap = BitmapFactory.decodeFile(path, opt);
		if (mBitmap != null) {
			return mBitmap;
		}
		return null;
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {

			if (bundle != null && getActivity() != null) {
				String mAction = Constants.ACTION_SINGLE_CHART_FRAGMENT + bundle.getString(ChartTitlesInitialPage.KEY_BUNDLE_DATE_PERIOD);
				if (intent.getAction().equals(mAction)) {
					setupImage();
				}
			}
		}
	}
}
