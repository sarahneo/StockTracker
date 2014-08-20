package com.handyapps.stocktracker.utils;

import java.io.File;
import java.io.FilenameFilter;

import com.handyapps.stocktracker.Constants;

import android.os.Environment;

public class OlderChart {

	public static File[] getChartFiles() {

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			String path = Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_CHART_DIRECTORY;
			final String CHART_EXTENSION = ".png";

			File backupDir = new File(path);

			return backupDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					if (filename.contains(CHART_EXTENSION)) {
						return true;
					}
					return false;
				}
			});
		} else {
			return null;
		}

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
}
