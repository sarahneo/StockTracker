package com.handyapps.stocktracker;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.handyapps.houseads.AdsApplication;
import com.handyapps.library.store.StoreBuild;
import com.handyapps.stocktracker.database.DatabaseHelper;
import com.handyapps.stocktracker.utils.FolderManager;

public class MyApplication extends AdsApplication {

	private static DatabaseHelper DBHelper;

	@Override
	public void onCreate() {
		super.onCreate();

		DBHelper = new DatabaseHelper(this);

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			FolderManager.toCreateDefaultFolder();
		}

		// **AppRater:
		new StoreBuild.Builder().setStore(Constants.APP_STORE).setPackageName(Constants.sp_pkg).
		// setPackageNamePro(Constants.sp_pkg_pro).
		// setSamsungSellerId(Constants.SAMSUNG_SELLER_ID).
				setIsPro(Constants.isPro()).build().buildStore(this);
	}

	public static SQLiteDatabase getWritableDatabase() {
		return DBHelper.getWritableDatabase();
	}

	public static SQLiteDatabase getReadableDatabase() {
		return DBHelper.getReadableDatabase();
	}

	public static SQLiteDatabase close() {
		DBHelper.close();
		return null;
	}

}
