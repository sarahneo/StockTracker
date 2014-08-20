package com.handyapps.stocktracker.model;

import java.util.Calendar;
import java.util.Random;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class AlertObject {

	public static final String TABLE_NAME = "alert";
	public static final String ID = "id";
	public static final String SYMBOL = "symbol";
	public static final String PRICE = "price";
	public static final String UPPER_TARGET = "upper_target";
	public static final String LOWER_TARGET = "lower_target";
	public static final String IS_NOTIFY_ON = "is_notify_on";
	public static final String IS_UPPER_TARGET_ON = "is_upper_target_on";
	public static final String IS_LOWER_TARGET_ON = "is_lower_target_on";
	public static final String MOD_TIME = "mod_time";

	public static final int DO_NOTIFY = 1;
	public static final int DO_NOT_NOTIFY = 0;

	private int id;
	private String symbol;
	private String lastTradePrice;
	private String upperPrice;
	private String lowerPrice;
	private int isNotifyOn;
	private int isUpperTargetOn;
	private int isLowerTargetOn;
	private long modTime;

	public AlertObject() {
	}

	public AlertObject(int id, String symbol, String lastTradePrice, String upperPrice, String lowerPrice, int isNotifyOn, int isUpperTargetOn,
			int isLowerTargetOn, int modTime) {
		this.id = id;
		this.symbol = symbol;
		this.lastTradePrice = lastTradePrice;
		this.upperPrice = upperPrice;
		this.lastTradePrice = lastTradePrice;
		this.isNotifyOn = isNotifyOn;
		this.isUpperTargetOn = isUpperTargetOn;
		this.isLowerTargetOn = isLowerTargetOn;
		this.modTime = modTime;
	}

	public boolean inset() {

		id = generateRandomId();
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertAlert(id, symbol, lastTradePrice, upperPrice, lowerPrice, isNotifyOn, isUpperTargetOn,
				isLowerTargetOn, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {

		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateAlert(id, symbol, lastTradePrice, upperPrice, lowerPrice, isNotifyOn, isUpperTargetOn,
				isLowerTargetOn, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			symbol = cursor.getString(cursor.getColumnIndex(SYMBOL));
			lastTradePrice = cursor.getString(cursor.getColumnIndex(PRICE));
			upperPrice = cursor.getString(cursor.getColumnIndex(UPPER_TARGET));
			lowerPrice = cursor.getString(cursor.getColumnIndex(LOWER_TARGET));
			isNotifyOn = cursor.getInt(cursor.getColumnIndex(IS_NOTIFY_ON));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));
		} catch (Exception e) {
		}
	}

	public boolean delete() {

		long rowId = DbAdapter.getSingleInstance().deleteAlert(id);
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

	public String getLastTradePrice() {
		return lastTradePrice;
	}

	public void setLastTradePrice(String lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
	}

	public int getIsUpperTargetOn() {
		return isUpperTargetOn;
	}

	public void setIsUpperTargetOn(int isUpperTargetOn) {
		this.isUpperTargetOn = isUpperTargetOn;
	}

	public int getIsLowerTargetOn() {
		return isLowerTargetOn;
	}

	public void setIsLowerTargetOn(int isLowerTargetOn) {
		this.isLowerTargetOn = isLowerTargetOn;
	}

	public String getUpperPrice() {
		return upperPrice;
	}

	public void setUpperPrice(String upperPrice) {
		this.upperPrice = upperPrice;
	}

	public String getLowerPrice() {
		return lowerPrice;
	}

	public void setLowerPrice(String lowerPrice) {
		this.lowerPrice = lowerPrice;
	}

	public int getIsNotifyOn() {
		return isNotifyOn;
	}

	public void setIsNotifyOn(int isNotifyOn) {
		this.isNotifyOn = isNotifyOn;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}

	public static final String[] PROJECTION = { AlertObject.ID, AlertObject.SYMBOL, AlertObject.PRICE, AlertObject.UPPER_TARGET,
			AlertObject.LOWER_TARGET, AlertObject.IS_NOTIFY_ON, AlertObject.IS_UPPER_TARGET_ON, AlertObject.IS_LOWER_TARGET_ON, AlertObject.MOD_TIME };
	
	private int generateRandomId() {
		Calendar cal = Calendar.getInstance();
		int nId = cal.get(Calendar.MILLISECOND);
		Random rand = new Random(nId);
		int rId = rand.nextInt(10000);
		return nId*10000 + rId;
	}
}
