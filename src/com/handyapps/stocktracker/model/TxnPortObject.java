package com.handyapps.stocktracker.model;

public class TxnPortObject {

	public static final String BUY_TYPE = "b";
	public static final String SELL_TYPE = "s";
	public static final String DIVIDEND_TYPE = "d";
	public static final String DIVIDEND_TYPE_SHARES = "ds";
	public static final String CASH_DEPOSIT_TYPE = "cd";
	public static final String CASH_WITHDRAWAL_TYPE = "cw";

	private String id;
	private int stockId;
	private int portId;
	private String type;
	private String symbol;
	private String price;
	private int numOfShares;
	private String fee;
	private int tradeDate;
	private double total;
	private String notes;

	public TxnPortObject() {
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
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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


	public int getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(int tradeDate) {
		this.tradeDate = tradeDate;
	}
}
