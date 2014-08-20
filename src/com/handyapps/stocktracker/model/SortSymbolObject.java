package com.handyapps.stocktracker.model;

import android.annotation.SuppressLint;

public class SortSymbolObject implements Comparable<SortSymbolObject>{
	
	private String symbol;
	private double value;
	
	
	public SortSymbolObject(String symbol, double value) {
		super();
		this.symbol = symbol;
		this.value = value;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@SuppressLint("UseValueOf")
	@Override
	public int compareTo(SortSymbolObject compareSortSymbolObject) {
		double compareValue = ((SortSymbolObject) compareSortSymbolObject).getValue(); 
		 
		//ascending order
		//return new Double(this.value).compareTo(compareValue);
		//return this.value - compareValue;
 
		//descending order
		return new Double(compareValue).compareTo(this.value);
		//return compareQuantity - this.quantity;
	}

}
