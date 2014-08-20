package com.handyapps.stocktracker.model;

public class ChartInfoObject {

	private String symbol;
	private String datePeriod;
	private String datePeriodUiShow;

	public ChartInfoObject() {
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getDatePeriod() {
		return datePeriod;
	}

	public void setDatePeriod(String datePeriod) {
		this.datePeriod = datePeriod;
	}

	public String getDatePeriodUiShow() {
		return datePeriodUiShow;
	}

	public void setDatePeriodUiShow(String datePeriodUiShow) {
		this.datePeriodUiShow = datePeriodUiShow;
	}
}
