package com.handyapps.stocktracker.model;

import java.io.File;

import com.handyapps.stocktracker.Constants;

import android.os.Environment;

public class ChartManager {
	
	public static final String[] chartDateRanges = { "1d", "5d", "1m", "3m", "6m", "1y", "2y", "5y", "max"};

	public static File getIntenalChart(String chartName) {
		File file = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_CHART_DIRECTORY + "/" + chartName + ".png");
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public static void purgeChart(File files[], int daysOlder) {

		long now = System.currentTimeMillis();
		long olderThan = now - (daysOlder * 8640000);

		for (File f : files) {

			long modTime = f.lastModified();
			if (modTime < olderThan) {
				f.delete();
			}
		}
	}

	public static boolean isFileOlderThanMins(File file, int mins) {

		long now = System.currentTimeMillis();
		long olderThan = now - (mins * 60000);

		long modTime = file.lastModified();
		if (modTime < olderThan) {
			return true;
		}
		return false;
	}

}
