package com.handyapps.stocktracker.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.model.ChartManager;
import com.handyapps.stocktracker.model.UrlManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class UpdateChartTaskFull extends AsyncTask<String, Void, Boolean> {

	private Context ctx;
	private String symbol;
	private HttpURLConnection httpConnection = null;
	private boolean isOneAndFiveDayChart = false;
	private int countOneAndFiveDay = 2;

	public UpdateChartTaskFull(Context context, boolean isOneAndFiveDayChart) {
		this.ctx = context;
		this.isOneAndFiveDayChart = isOneAndFiveDayChart;
	}

	@Override
	protected Boolean doInBackground(String... arrSymbol) {

		symbol = arrSymbol[0];

		try {
			if (!isCancelled()) {

				List<String> chartRangeList = UrlManager.chartRangeList;
				String[] chartRange = ChartManager.chartDateRanges;
				
				int countChartRange = chartRangeList.size();
				
				if(isOneAndFiveDayChart){
					countChartRange = countOneAndFiveDay;
				}

				for (int i = 0; i < countChartRange; i++) {

					String chartName = symbol + chartRange[i];
					String mUrl = chartRangeList.get(i);
					String sUrl = String.format(mUrl, symbol);

					Bitmap bitmap = (Bitmap) getChart(sUrl);

					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

						if (bitmap != null) {
							setupChart(bitmap, chartName);
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result) {
			
			Log.d("FullChart", "Updated");
			Intent iSingleChartFragment;
			String action = Constants.ACTION_SINGLE_CHART_FRAGMENT;
			String[] mChartDateRanges = ChartManager.chartDateRanges;
			for (String s : mChartDateRanges) {
				String mAction = action + s;
				iSingleChartFragment = new Intent(mAction);
				ctx.sendBroadcast(iSingleChartFragment);
			}
		}
	}

	private Boolean setupChart(Bitmap mBitmap, String chartName) {
		String _chartName = chartName;
		Bitmap _bitmap = mBitmap;

		OutputStream outStream = null;

		File fileDir = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_CHART_DIRECTORY);
		File file = new File(fileDir, "/" + _chartName + ".png");

		try {

			outStream = new FileOutputStream(file);
			_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();

		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return isChartExisting(_chartName);
	}

	private Boolean isChartExisting(String chartName) {

		String _chartName = chartName;

		File file = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_CHART_DIRECTORY + "/" + _chartName + ".png");
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	private Bitmap getChart(String sUrl) {
		String mUrl = sUrl;

		Bitmap mChart;

		try {
			URL url = new URL(mUrl);

			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Connection", "close");
			httpConnection.setInstanceFollowRedirects(true);
			httpConnection.setUseCaches(false);
			httpConnection.connect();

			InputStream in = new BufferedInputStream(httpConnection.getInputStream());

			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inDither = false;
			// bmOptions.inSampleSize = 1;
			mChart = BitmapFactory.decodeStream(in, null, bmOptions);

		} catch (IOException e) {
			return null;
		} finally {
		}
		return mChart;
	}
}
