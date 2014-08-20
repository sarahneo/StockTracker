package com.handyapps.stocktracker.model;

import android.database.Cursor;
import android.graphics.Color;

import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.utils.RandomColorProvider;

public class StockObject {

	public static final String TABLE_NAME = "stock";
	public static final String ID = "id";
	public static final String SYMBOL = "symbol";
	public static final String NAME = "name";
	public static final String EXCH = "exch";
	public static final String TYPE = "type";
	public static final String TYPE_DISP = "type_disp";
	public static final String EXCH_DISP = "exch_disp";
	public static final String MOD_TIME = "mod_time";
	public static final String COLOR_CODE = "color_code";

	private int id;
	private String symbol;
	private String name;
	private String exch;
	private String type;
	private String typeDisp;
	private String exchDisp;
	private String currency;
	private long modTime;
	private int colorCode;

	public StockObject() {
	}

	public StockObject(int id, String symbol, String name, String exch, String type, String typeDisp, String exchDiap, 
			long modTime, int colorCode) {
		this.id = id;
		this.symbol = symbol;
		this.name = name;
		this.exch = exch;
		this.type = type;
		this.typeDisp = typeDisp;
		this.exchDisp = exchDiap;
		this.modTime = modTime;
		this.colorCode = colorCode;
	}

	public boolean insert() {

		colorCode = getRandomColor();
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertStock(symbol, name, exch, type, typeDisp, exchDisp, 
				modTime, colorCode);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateStock(id, symbol, name, exch, type, typeDisp, exchDisp, 
				modTime, colorCode);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getInt(cursor.getColumnIndex(ID));
			symbol = cursor.getString(cursor.getColumnIndex(SYMBOL));
			name = cursor.getString(cursor.getColumnIndex(NAME));
			exch = cursor.getString(cursor.getColumnIndex(EXCH));
			type = cursor.getString(cursor.getColumnIndex(TYPE));
			typeDisp = cursor.getString(cursor.getColumnIndex(TYPE_DISP));
			exchDisp = cursor.getString(cursor.getColumnIndex(EXCH_DISP));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));
			colorCode = cursor.getInt(cursor.getColumnIndex(COLOR_CODE));

		} catch (Exception e) {
		}
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deleteStock(id);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExch() {
		return exch;
	}

	public void setExch(String exch) {
		this.exch = exch;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeDisp() {
		return typeDisp;
	}

	public void setTypeDisp(String typeDisp) {
		this.typeDisp = typeDisp;
	}

	public String getExchDisp() {
		return exchDisp;
	}

	public void setExchDisp(String exchDisp) {
		this.exchDisp = exchDisp;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}
	
	public int getColorCode() {
		return colorCode;
	}
	
	public void setColorCode(int colorCode) {
		this.colorCode = colorCode;
	}
	
	public static int getRandomColor() {
		int r = 0, g = 0, b = 0;
		while ((r + g + b < 170) || (r + g + b > 500)) {
			r = (int) Math.floor(Math.random() * 255);
			g = (int) Math.floor(Math.random() * 255);
			b = (int) Math.floor(Math.random() * 255);
		}
		return RandomColorProvider.generateRandomColor(Color.rgb(128, 128, 128));
	}
}
