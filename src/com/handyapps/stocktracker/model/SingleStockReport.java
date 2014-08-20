package com.handyapps.stocktracker.model;

public class SingleStockReport {
	
	double netProfitOrLoss;
	double dailyProfitOrLoss;
	double totalValue;
	double totalCost;
	double avgPrice;
	long remainingQty;
	long shortQty;
	String change;
	String changeInPercent;
	String lastPrice;
	
	double lastTradePrice;
	private double realizedGain;
	private double unrealizedGain;

	public double getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}
	public double getLastTradePrice() {
		return lastTradePrice;
	}
	public void setLastTradePrice(double lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
	}
	public long getRemainingQty() {
		return remainingQty;
	}
	public void setRemainingQty(long remainingQty) {
		this.remainingQty = remainingQty;
	}
	public long getShortQty() {
		return shortQty;
	}
	public void setShortQty(long shortQty) {
		this.shortQty = shortQty;
	}
	public double getNetProfitOrLoss() {
		return netProfitOrLoss;
	}
	public void setNetProfitOrLoss(double netProfitOrLoss) {
		this.netProfitOrLoss = netProfitOrLoss;
	}
	public double getDailyProfitOrLoss() {
		return dailyProfitOrLoss;
	}
	public void setDailyProfitOrLoss(double dailyProfitOrLoss) {
		this.dailyProfitOrLoss = dailyProfitOrLoss;
	}
	public double getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
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
	public String getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(String lastPrice) {
		this.lastPrice = lastPrice;
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
