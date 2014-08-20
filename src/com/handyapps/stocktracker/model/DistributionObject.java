package com.handyapps.stocktracker.model;

public class DistributionObject {

	public static final String STOCK_TYPE = "s";
	public static final String CASH_TYPE = "c";

	private int stockId;
	private int portId;
	private String name;
	private String symbol;
	private String value;
	private String type;
	private int color;
	private double dValue;
	private double percent;

	public DistributionObject() {
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
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
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
	
	public double getPercent() {
		return percent;
	}
	
	public void setPercent(double percent) {
		this.percent = percent;
	}
	
	public double getDValue() {
		return dValue;
	}
	
	public void setDValue(double dValue) {
		this.dValue = dValue;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
}
