package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class PortfolioStockObject {
	
	public static final String TABLE_NAME = "portfolio_stock";
	public static final String PORT_ID = "port_id";
	public static final String STOCK_ID = "stock_id";	

	private int portfolioId;
	private int stockId;	
	
	public PortfolioStockObject() {
	}
	
	public PortfolioStockObject(int portfolioId, int stockId, int colorCode) {
		this.portfolioId = portfolioId;
		this.stockId = stockId;		
	}
	
	public boolean insert() {
		
		long rowId = DbAdapter.getSingleInstance().insertPortfolioStock(portfolioId, stockId);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean update() {
		long rowId = DbAdapter.getSingleInstance().updatePortfolioStock(portfolioId, stockId);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	public boolean delete()
	{
		long rowId = DbAdapter.getSingleInstance().deletePortfolioStockByStockId(stockId);
		if (rowId > -1) {
			return true;
		}
		return false;
	}
	
	public void load(Cursor cursor) {

		try {
			portfolioId = cursor.getInt(cursor.getColumnIndex(PORT_ID));
			stockId = cursor.getInt(cursor.getColumnIndex(STOCK_ID));			

		} catch (Exception e) {
		}
	}
	

	public int getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	
}
