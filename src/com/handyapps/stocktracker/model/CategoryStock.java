package com.handyapps.stocktracker.model;


public class CategoryStock {
	
	public static final String PORTFOLIO = "portfolio";
	public static final String WATCHLIST = "watchlist";
	public static final String FIND_STOCKS = "find_stocks";
	
	StockObject mStockObject;
	String stockCategory;
	
	public StockObject getStockObj() {
		return mStockObject;
	}
	
	public void setStockObj(StockObject mStockObject) {
		this.mStockObject = mStockObject;
	}
	
	public String getStockCategory() {
		return stockCategory;
	}
	
	public void setStockCategory(String stockCategory) {
		this.stockCategory = stockCategory;
	}

}
