package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class WatchlistObject {

	public static final String TABLE_NAME = "watchlist";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String MOD_TIME = "mod_time";

	private int id;
	private String name;
	private long modTime;

	public WatchlistObject() {
	}

	public WatchlistObject(int id, String name, long modTime) {
		this.id = id;
		this.name = name;
		this.modTime = modTime;
	}

	public boolean insert() {

		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertWatchList(name, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateWatchList(id, name, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			name = cursor.getString(cursor.getColumnIndex(NAME));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));

		} catch (Exception e) {
		}
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deleteWatchList(id);
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

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}
}