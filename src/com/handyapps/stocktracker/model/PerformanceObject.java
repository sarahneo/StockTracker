package com.handyapps.stocktracker.model;

public class PerformanceObject {

	public static final String STOCK_TYPE = "s";
	public static final String PORTFOLIO_TYPE = "p";

	private int stockId;
	private int portId;
	private int colorCode;
	private String name;
	private String symbol;
	private String value;
	private String percentROI;
	private String lastPrice;
	private String netCash;
	private String priceChange;
	private String changeInPercent;
	private String type;
	private String currency;
	private double dValue;
	private double dRealizedGainLoss;
	private double dUnrealizedGainLoss;
	private double dOverallGainLoss;

	public PerformanceObject() {
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
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
	
	public String getPercentROI() {
		return percentROI;
	}
	
	public void setPercentROI(String percentROI) {
		this.percentROI = percentROI;
	}
	
	public double getDRealizedGainLoss() {
		return dRealizedGainLoss;
	}
	
	public void setDRealizedGainLoss(double dRealizedGainLoss) {
		this.dRealizedGainLoss = dRealizedGainLoss;
	}
	
	public double getDUnrealizedGainLoss() {
		return dUnrealizedGainLoss;
	}
	
	public void setDUnrealizedGainLoss(double dUnrealizedGainLoss) {
		this.dUnrealizedGainLoss = dUnrealizedGainLoss;
	}
	
	public double getDValue() {
		return dValue;
	}
	
	public void setDValue(double dValue) {
		this.dValue = dValue;
	}
	
	public int getColorCode() {
		return colorCode;
	}
	
	public void setColorCode(int colorCode) {
		this.colorCode = colorCode;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getChange() {
		return priceChange;
	}
	public void setChange(String priceChange) {
		this.priceChange = priceChange;
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
	public String getNetCash() {
		return netCash;
	}
	public void setNetCash(String netCash) {
		this.netCash = netCash;
	}
	public double getOverallGainLoss() {
		return dOverallGainLoss;
	}
	public void setOverallGainLoss(double dOverallGainLoss) {
		this.dOverallGainLoss = dOverallGainLoss;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
}
