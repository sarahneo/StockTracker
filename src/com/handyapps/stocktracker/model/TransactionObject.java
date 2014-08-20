package com.handyapps.stocktracker.model;

import java.util.UUID;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class TransactionObject {

	public static final String TABLE_NAME = "my_transaction";
	public static final String ID = "id";
	public static final String STOCK_ID = "stock_id";
	public static final String PORT_ID = "port_id";
	public static final String TYPE = "type";
	public static final String PRICE = "price";
	public static final String NUM_SHARES = "num_shares";
	public static final String FEE = "fee";
	public static final String TRADE_DATE = "trade_date";
	public static final String TOTAL = "total";
	public static final String NOTES = "notes";
	public static final String MOD_TIME = "mod_time";
	public static final String BUY_TYPE = "b";
	public static final String SELL_TYPE = "s";
	public static final String DIVIDEND_TYPE = "d";
	public static final String DIVIDEND_TYPE_SHARES = "ds";

	public static final String[] TRANSACTION_TYPE = { BUY_TYPE, SELL_TYPE };

	private String id;
	private int stockId;
	private int portId;
	private String type;
	private String price;
	private int numOfShares;
	private String fee;
	private int tradeDate;
	private double total;
	private String notes;
	private long modTime;

	public TransactionObject() {
	}

	public TransactionObject(String id, int stockId, int portId, String type, String price, int numOfShares, String fee, int tradeDate, double total, String notes,
			long modTime) {

		this.id = id;
		this.stockId = stockId;
		this.portId = portId;
		this.type = type;
		this.price = price;
		this.numOfShares = numOfShares;
		this.fee = fee;
		this.tradeDate = tradeDate;
		this.total = total;
		this.notes = notes;
		this.modTime = modTime;
	}

	public boolean insert() {

		id = String.valueOf(UUID.randomUUID());
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertTransactionObject(id, stockId, portId, type, price, numOfShares, fee, tradeDate, total, notes, modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {
		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateTransactionObject(id, stockId, portId, type, price, numOfShares, fee, tradeDate, total,notes,
				modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public void load(Cursor cursor) {

		try {
			id = cursor.getString(cursor.getColumnIndex(ID));
			stockId = cursor.getInt(cursor.getColumnIndex(STOCK_ID));
			portId = cursor.getInt(cursor.getColumnIndex(PORT_ID));
			type = cursor.getString(cursor.getColumnIndex(TYPE));
			price = cursor.getString(cursor.getColumnIndex(PRICE));
			numOfShares = cursor.getInt(cursor.getColumnIndex(NUM_SHARES));
			fee = cursor.getString(cursor.getColumnIndex(FEE));
			tradeDate = cursor.getInt(cursor.getColumnIndex(TRADE_DATE));
			total = cursor.getDouble(cursor.getColumnIndex(TOTAL));
			notes = cursor.getString(cursor.getColumnIndex(NOTES));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));

		} catch (Exception e) {
		}
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deleteTransactionObject(id);
		if (rowId > -1) {
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getNumOfShares() {
		return numOfShares;
	}

	public void setNumOfShares(int numOfShares) {
		this.numOfShares = numOfShares;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getNotes() {
		return notes;
	}
	
	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}

	public int getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(int tradeDate) {
		this.tradeDate = tradeDate;
	}
}
