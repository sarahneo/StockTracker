package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class PortfolioObject {

	public static final String TABLE_NAME = "portfolio";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String INITIAL_CASH = "initial_cash";
	public static final String CURRENCY_TYPE = "currency_type";
	public static final String MOD_TIME = "mod_time";
	public static final String SGD = "SGD";
	public static final String USD = "USD";
	public static final String EURO = "EURO";
	public static final String JPY = "JPY";
	public static final String GBP = "GBP";
	public static final String CAD = "CAD";
	

	private int id;
	private String name;
	private String initialCash = "0";
	private String currencyType;
	private long modTime;

	public PortfolioObject() {
	}

	public PortfolioObject(int id, String name, String initialCash, String currencyType, long modTime) {
		this.id = id;
		this.name = name;
		this.initialCash = initialCash;
		this.currencyType = currencyType;
		this.modTime = modTime;
	}

	public boolean insert() {
		
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertPortfolio(name, initialCash, currencyType, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updatePortfolio(id, name, initialCash, currencyType, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			name = cursor.getString(cursor.getColumnIndex(NAME));
			initialCash = cursor.getString(cursor.getColumnIndex(INITIAL_CASH));
			currencyType = cursor.getString(cursor.getColumnIndex(CURRENCY_TYPE));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));

		} catch (Exception e) {
		}
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deletePortfolio(id);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getInitialCash() {
		return initialCash;
	}

	public void setInitialCash(String initialCash) {
		this.initialCash = initialCash;
	}
	
	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}
}
