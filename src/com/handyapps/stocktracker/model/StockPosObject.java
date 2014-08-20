package com.handyapps.stocktracker.model;

public class StockPosObject {
	
	public static final String LONG_POS_TYPE = "long";
	public static final String SHORT_POS_TYPE = "short";
	public static final String CLOSED_POS_TYPE = "closed";

	private int stockId;
	private int portId;
	/*private double dQuantity;
	private double dAvgPrice;
	private double dLastPrice;
	private double dChange;*/
	private double dMktValue;
	private double dGainLoss;
	private String symbol;
	private String name;
	private String quantity;
	private String avgPrice;
	private String lastPrice;
	private String change;
	private String changeInPercent;
	private String mktValue;
	private String gainLoss;
	private String stockType; // long, short or closed
	
	public int getPortId() {
		return portId;
	}
	
	public void setPortId(int portId) {
		this.portId = portId;
	}
	
	/*public double getDQuantity() {
		return dQuantity;
	}
	
	public void setDQuantity(double dQuantity) {
		this.dQuantity = dQuantity;
	}
	
	public double getDAvgPrice() {
		return dAvgPrice;
	}
	
	public void setDAvgPrice(double dAvgPrice) {
		this.dAvgPrice = dAvgPrice;
	}
	
	public double getDLastPrice() {
		return dLastPrice;
	}
	
	public void setDLastPrice(double dLastPrice) {
		this.dLastPrice = dLastPrice;
	}
	
	public double getDChange() {
		return dChange;
	}
	
	public void setDChange(double dChange) {
		this.dChange = dChange;
	}*/
	
	public double getDMktValue() {
		return dMktValue;
	}
	
	public void setDMktValue(double dMktValue) {
		this.dMktValue = dMktValue;
	}
	
	public double getDGainLoss() {
		return dGainLoss;
	}
	
	public void setDGainLoss(double dGainLoss) {
		this.dGainLoss = dGainLoss;
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

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	
	public String getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}

	public String getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(String lastPrice) {
		this.lastPrice = lastPrice;
	}
	
	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}
	
	public String getChangeInPercent() {
		return changeInPercent;
	}

	public void setChangeInPercent(String changeInPercent) {
		this.changeInPercent = changeInPercent;
	}

	public String getMktValue() {
		return mktValue;
	}

	public void setMktValue(String mktValue) {
		this.mktValue = mktValue;
	}
	
	public String getGainLoss() {
		return gainLoss;
	}

	public void setGainLoss(String gainLoss) {
		this.gainLoss = gainLoss;
	}
	
	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}
	
	public String getStockType() {
		return stockType;
	}

	public void setStockType(String stockType) {
		this.stockType = stockType;
	}

}
