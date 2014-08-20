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

public class UpdateChartTaskOneDay extends AsyncTask<String, Void, Boolean> {

	private Context ctx;
	private String symbol;
	private HttpURLConnection httpConnection = null;

	public UpdateChartTaskOneDay(Context context) {
		this.ctx = context;
	}

	@Override
	protected Boolean doInBackground(String... arrSymbol) {

		symbol = arrSymbol[0];

		try {
			if (!isCancelled()) {

				List<String> chartRangeList = UrlManager.chartRangeList;
				String[] chartRange = ChartManager.chartDateRanges;

				String oneDayRange = chartRange[0];
				String mUrlOneday = chartRangeList.get(0);
				String chartName = symbol + oneDayRange;

				String sUrl = String.format(mUrlOneday, symbol);
				Bitmap bitmap = (Bitmap) getChart(sUrl);

				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					if (bitmap != null) {
						return setupChart(bitmap, chartName);
					}
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result) {
			//Log.d("OneDayChart", "updated");
			Intent iTransactionFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
			iTransactionFragmentActivity.putExtra(Constants.KEY_SYMBOL, this.symbol);
			iTransactionFragmentActivity.putExtra(Constants.KEY_IS_SINGLE_CHART_DONE, true);//purpose to stop progressing bar
			ctx.sendBroadcast(iTransactionFragmentActivity);

			Intent iSummaryFragment = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
			iSummaryFragment.putExtra(Constants.KEY_SYMBOL, this.symbol);//purposely to update chart. 
			ctx.sendBroadcast(iSummaryFragment);
			
		}else{
			//In case update fail and progress bar can be stops.
			Intent iTransactionFragmentActivity = new Intent(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
			iTransactionFragmentActivity.putExtra(Constants.KEY_SYMBOL, this.symbol);
			iTransactionFragmentActivity.putExtra(Constants.KEY_IS_SINGLE_CHART_DONE, true);//purpose to stop progressing bar
			ctx.sendBroadcast(iTransactionFragmentActivity);
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
			httpConnection.disconnect();
			return null;
		} finally {
			httpConnection.disconnect();
		}
		return mChart;
	}
}
