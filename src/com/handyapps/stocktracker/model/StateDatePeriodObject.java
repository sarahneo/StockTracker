package com.handyapps.stocktracker.model;

public class StateDatePeriodObject {

	private int from;
	private int to;
	private String fullDatePeriodTxt;
	private String fromTxt;
	private String toTxt;

	public String getFromTxt() {
		return fromTxt;
	}

	public void setFromTxt(String fromTxt) {
		this.fromTxt = fromTxt;
	}

	public String getToTxt() {
		return toTxt;
	}

	public void setToTxt(String toTxt) {
		this.toTxt = toTxt;
	}

	public String getFullDatePeriodTxt() {
		return fullDatePeriodTxt;
	}

	public void setFullDatePeriodTxt(String fullDatePeriodTxt) {
		this.fullDatePeriodTxt = fullDatePeriodTxt;
	}

	public StateDatePeriodObject() {
	}

	public StateDatePeriodObject(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}
}
