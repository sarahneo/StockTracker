package com.handyapps.stocktracker.model;

public class SingleTickerDataset {
	private String totalValue;
	private String netProfit;
	private String dailyProfit;
	private String avgPrice;
	private String lastPrice;
	private String priceChange;
	private String changeInPercent;

	private double doubleTotalValue;
	private double doubleTotalCost;
	private double doubleNetProfit;
	private double doubleDailyProfit;
	
	private double realizedGain;
	private double unrealizedGain;

	private double quantity;
	private double shortQty;
	private String symbolWithQty;
	
	private boolean hasTxn;

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getSymbolWithQty() {
		return symbolWithQty;
	}

	public void setSymbolWithQty(String symbolWithQty) {
		this.symbolWithQty = symbolWithQty;
	}
	
	public void setAvgPrice(String avgPrice) {
		this.avgPrice = avgPrice;
	}
	
	public String getAvgPrice() {
		return avgPrice;
	}
	
	public String getPrice() {
		String price = "$0.00";
		if (!symbolWithQty.equals("") && symbolWithQty.contains("$"))
			price = symbolWithQty.substring(symbolWithQty.indexOf("$"), symbolWithQty.lastIndexOf(")"));
		return price;
	}

	public double getDoubleTotalValue() {
		return doubleTotalValue;
	}

	public void setDoubleTotalValue(double doubleTotalValue) {
		this.doubleTotalValue = doubleTotalValue;
	}
	
	public double getDoubleTotalCost() {
		return doubleTotalCost;
	}
	
	public void setDoubleTotalCost(double doubleTotalCost) {
		this.doubleTotalCost = doubleTotalCost;
	}

	public double getDoubleNetProfit() {
		return doubleNetProfit;
	}

	public void setDoubleNetProfit(double doubleNetProfit) {
		this.doubleNetProfit = doubleNetProfit;
	}
	
	public double getDoubleDailyProfit() {
		return doubleDailyProfit;
	}

	public void setDoubleDailyProfit(double doubleDailyProfit) {
		this.doubleDailyProfit = doubleDailyProfit;
	}

	public String getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(String totalValue) {
		this.totalValue = totalValue;
	}

	public String getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(String netProfit) {
		this.netProfit = netProfit;
	}
	
	public String getDailyProfit() {
		return dailyProfit;
	}

	public void setDailyProfit(String dailyProfit) {
		this.dailyProfit = dailyProfit;
	}
	
	public double getShortQty() {
		return shortQty;
	}
	
	public void setShortQty(double shortQty) {
		this.shortQty = shortQty;
	}
	
	public void setHasTxn(boolean hasTxn) {
		this.hasTxn = hasTxn;
	}
	
	public boolean getHasTxn() {
		return hasTxn;
	}
	
	public void setLastPrice(String lastPrice) {
		this.lastPrice = lastPrice;
	}
	
	public String getLastPrice() {
		return lastPrice;
	}
	
	public void setPriceChange(String priceChange) {
		this.priceChange = priceChange;
	}
	
	public String getPriceChange() {
		return priceChange;
	}
	
	public String getChangeInPercent() {
		return changeInPercent;
	}
	
	public void setChangeInPercent(String changeInPercent) {
		this.changeInPercent = changeInPercent;
	}
	
	public void setRealizedGain(double realizedGain) {
		this.realizedGain = realizedGain;
	}
	
	public double getRealizedGain() {
		return realizedGain;
	}
	
	public void setUnrealizedGain(double unrealizedGain) {
		this.unrealizedGain = unrealizedGain;
	}
	
	public double getUnrealizedGain() {
		return unrealizedGain;
	}
}
