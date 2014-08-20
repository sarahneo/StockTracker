package com.handyapps.stocktracker.model;

import java.util.Calendar;
import java.util.Random;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class NewsAlertObject {

	public static final String TABLE_NAME = "news_alert";
	public static final String ID = "id";
	public static final String SYMBOL = "symbol";
	public static final String ALERT_FREQUENCY = "alert_frequency";
	public static final String IS_NOTIFY_ON = "is_notify_on";
	public static final String START_TIME = "start_time";
	public static final String MOD_TIME = "mod_time";

	public static final int DO_NOTIFY = 1;
	public static final int DO_NOT_NOTIFY = 0;

	private int id;
	private String symbol;
	private int alertFrequency;
	private int isNotifyOn;
	private long startTime;
	private long modTime;

	public NewsAlertObject() {
	}

	public NewsAlertObject(int id, String symbol, int alertFrequency, int isNotifyOn, int startTime, int modTime) {
		this.id = id;
		this.symbol = symbol;
		this.alertFrequency = alertFrequency;
		this.isNotifyOn = isNotifyOn;
		this.startTime = startTime;
		this.modTime = modTime;
	}

	public boolean insert() {

		id = generateRandomId();
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertNewsAlert(id, symbol, alertFrequency, isNotifyOn, 
				startTime, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {

		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateNewsAlert(id, symbol, alertFrequency, isNotifyOn, 
				startTime, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			symbol = cursor.getString(cursor.getColumnIndex(SYMBOL));
			alertFrequency = cursor.getInt(cursor.getColumnIndex(ALERT_FREQUENCY));
			isNotifyOn = cursor.getInt(cursor.getColumnIndex(IS_NOTIFY_ON));
			startTime = cursor.getLong(cursor.getColumnIndex(START_TIME));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));
		} catch (Exception e) {
		}
	}

	public boolean delete() {

		long rowId = DbAdapter.getSingleInstance().deleteNewsAlert(id);
		if (rowId > -1) {
			return true;
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public int getAlertFrequency() {
		return alertFrequency;
	}

	public void setAlertFrequency(int alertFrequency) {
		this.alertFrequency = alertFrequency;
	}

	public int getIsNotifyOn() {
		return isNotifyOn;
	}

	public void setIsNotifyOn(int isNotifyOn) {
		this.isNotifyOn = isNotifyOn;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}

	public static final String[] PROJECTION = { NewsAlertObject.ID, NewsAlertObject.SYMBOL, NewsAlertObject.ALERT_FREQUENCY, 
			NewsAlertObject.IS_NOTIFY_ON, NewsAlertObject.START_TIME, NewsAlertObject.MOD_TIME };
	
	private int generateRandomId() {
		Calendar cal = Calendar.getInstance();
		int nId = cal.get(Calendar.MILLISECOND);
		Random rand = new Random(nId);
		int rId = rand.nextInt(1000);
		return nId*1000 + rId;
	}
}
