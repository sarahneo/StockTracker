package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class NewsObject {
	
	public static final String TABLE_NAME = "news";
	public static final String ID = "id";
	public static final String SYMBOL = "symbol";
	public static final String TITLE = "title";
	public static final String LINK = "link";
	public static final String PUB_DATE = "pub_date";
	public static final String DESCRIPTION = "description";
	public static final String MOD_TIME = "mod_time";
	
	private int id;
	private String title;
	private String link;
	private String pubDate;
	private String description;
	private String symbol;
	private long modTime;
	
	public NewsObject() {
	}
	
	public NewsObject(int id, String symbol, String title, String link, String pubDate, String description, long modTime) {
		this.id = id;
		this.symbol = symbol;
		this.title = title;
		this.link = link;
		this.pubDate = pubDate;
		this.description = description;
		this.modTime = modTime;
	}

	public boolean insert() {
		
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertNews(symbol, title, link, pubDate, description, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateNews(id, symbol, title, link, pubDate, description, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			symbol = cursor.getString(cursor.getColumnIndex(SYMBOL));
			title = cursor.getString(cursor.getColumnIndex(TITLE));
			link = cursor.getString(cursor.getColumnIndex(LINK));
			pubDate = cursor.getString(cursor.getColumnIndex(PUB_DATE));
			description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));

		} catch (Exception e) {
		}
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deleteNews(id);
		if (rowId > -1) {
			return true;
		}
		return false;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getDescription() {
		return description;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getModTime() {
		return modTime;
	}
	public void setModTime(long modTime) {
		this.modTime = modTime;
	}
}
