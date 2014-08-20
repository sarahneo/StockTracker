package com.handyapps.stocktracker.utils;

import java.io.File;
import java.io.IOException;
import com.handyapps.stocktracker.Constants;

import android.os.Environment;

public class FolderManager {

	public static void toCreateDefaultFolder() {

		if (isSDCardMounted()) {

			File passwordWalletFolder = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_DIRECTORY);

			if (!passwordWalletFolder.exists()) {
				passwordWalletFolder.mkdir();
			}
			if (!passwordWalletFolder.isDirectory()) {
				passwordWalletFolder.mkdirs();
			}

			File chartFolder = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_CHART_DIRECTORY);

			if (!chartFolder.exists()) {
				chartFolder.mkdir();
			}
			if (!chartFolder.isDirectory()) {
				chartFolder.mkdirs();
			}

			File nomediaFile = new File(Environment.getExternalStorageDirectory() + Constants.STOCKTACKER_NOMEDIA_FILE);

			if (!nomediaFile.isFile()) {
				try {
					nomediaFile.createNewFile();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static boolean isSDCardMounted(){
		boolean isMySDCardMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		return isMySDCardMounted;
	}
}
