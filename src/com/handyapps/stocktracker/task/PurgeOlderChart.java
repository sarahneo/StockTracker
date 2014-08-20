package com.handyapps.stocktracker.task;

import java.io.File;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.utils.OlderChart;

import android.os.AsyncTask;
import android.os.Environment;

public class PurgeOlderChart extends AsyncTask<Void, Void, Boolean> {
	
	private int olderDay = Constants.PURGE_CHART_OLDER_DAYS;

	public PurgeOlderChart(int day) {
		this.olderDay = day;
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			
			File[] files = OlderChart.getChartFiles();
			
			if (files.length != 0) {
				OlderChart.purgeChart(files, olderDay);
				return true;
			}
		}
		return null;
	}
}
