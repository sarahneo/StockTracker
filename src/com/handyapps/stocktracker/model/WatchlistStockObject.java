package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class WatchlistStockObject {
	
	public static final String TABLE_NAME = "watch_stock";
	public static final String WATCH_ID = "watch_id";
	public static final String STOCK_ID = "stock_id";

	private int watchId;
	private int stockId;
	
	public WatchlistStockObject() {
	}
	
	public WatchlistStockObject(int watchId, int stockId) {
		this.watchId = watchId;
		this.stockId = stockId;
	}
	
	public boolean insert() {

		long rowId = DbAdapter.getSingleInstance().insertWatchStock(watchId, stockId);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean update() {
		long rowId = DbAdapter.getSingleInstance().updateWatchStock(watchId, stockId);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean delete()
	{
		long rowId = DbAdapter.getSingleInstance().deleteWatchStockByStockId(stockId);
		if (rowId > -1) {
			return true;
		}
		return false;
	}
	
	public void load(Cursor cursor) {

		try {
			watchId = cursor.getInt(cursor.getColumnIndex(WATCH_ID));
			stockId = cursor.getInt(cursor.getColumnIndex(STOCK_ID));

		} catch (Exception e) {
		}
	}

	public int getWatchId() {
		return watchId;
	}

	public void setWatchId(int watchId) {
		this.watchId = watchId;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}
}
