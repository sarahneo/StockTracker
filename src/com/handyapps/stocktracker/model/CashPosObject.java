package com.handyapps.stocktracker.model;

import java.util.UUID;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class CashPosObject {
	
	public static final String CASH_WITHDRAWAL_TYPE = "cw";
	public static final String CASH_DEPOSIT_TYPE = "cd";
	public static final String TABLE_NAME = "user_cash";
	public static final String ID = "id";
	public static final String PORTFOLIO_ID = "portfolio_id";
	public static final String TXN_TYPE = "txn_type"; 
	public static final String AMOUNT = "amount";
	public static final String TXN_DATE = "txn_date";
	public static final String LAST_ACCESSED = "last_accessed";
	public static final String LAST_UPDATE = "last_update";

	private String id;
	private int portfolioId;
	public String txnType;
	public String amount;
	public int txnDate;
	public long lastAccessed;
	public long lastUpdate;
	
	public CashPosObject() {
	}
	
	public CashPosObject(String id, int portfolioId, String txnType, String amount, 
			int txnDate, long lastAccessed, long lastUpdate) {
		this.id = id;
		this.portfolioId = portfolioId;
		this.txnType = txnType;
		this.amount = amount;
		this.txnDate = txnDate;
		this.lastAccessed = lastAccessed;
		this.lastUpdate = lastUpdate;
	}
	
	
	public boolean insert() {

		id = String.valueOf(UUID.randomUUID());
		lastAccessed = System.currentTimeMillis();
		lastUpdate = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertCashPos(id, portfolioId, txnType, amount,
				txnDate, lastAccessed, lastUpdate);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean update() {
		lastAccessed = System.currentTimeMillis();
		lastUpdate = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().updateCashPos(id, portfolioId, txnType, amount,
				txnDate, lastAccessed, lastUpdate);
		
		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean delete()
	{
		long rowId = DbAdapter.getSingleInstance().deleteCashPosById(id);
		if (rowId > -1) {
			return true;
		}
		return false;
	}
	
	public void load(Cursor cursor) {

		try {
			id = cursor.getString(cursor.getColumnIndex(ID));
			portfolioId = cursor.getInt(cursor.getColumnIndex(PORTFOLIO_ID));
			txnType = cursor.getString(cursor.getColumnIndex(TXN_TYPE));
			amount = cursor.getString(cursor.getColumnIndex(AMOUNT));
			txnDate = cursor.getInt(cursor.getColumnIndex(TXN_DATE));
			lastAccessed = cursor.getLong(cursor.getColumnIndex(LAST_ACCESSED));
			lastUpdate = cursor.getLong(cursor.getColumnIndex(LAST_UPDATE));

		} catch (Exception e) {
		}
	}
	

	public int getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getTxnType() {
		return txnType;
	}
	
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public int getTxnDate() {
		return txnDate;
	}
	
	public void setTxnDate(int txnDate) {
		this.txnDate = txnDate;
	}
		
	
}
