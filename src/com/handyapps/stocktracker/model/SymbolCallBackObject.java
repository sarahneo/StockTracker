package com.handyapps.stocktracker.model;

public class SymbolCallBackObject {

	private String symbol; //GOOG
	private String name; //Google Inc.
	private String exch; //NMS
	private String type; //s
	private String typeDisp; //NASDAQ
	private String exchDisp; ///Equity
	public String getExch() {
		return exch;
	}

	public void setExch(String exch) {
		this.exch = exch;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeDisp() {
		return typeDisp;
	}

	public void setTypeDisp(String typeDisp) {
		this.typeDisp = typeDisp;
	}

	public String getExchDisp() {
		return exchDisp;
	}

	public void setExchDisp(String exchDisp) {
		this.exchDisp = exchDisp;
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

	public String getName() {
		return name;
	}

	public String getDescription(){
		
		if (type.equals("E") || type.equals("C")) {
			
			return typeDisp + " - " + exch;

		} else if (type.equals("S") || type.equals("I") || type.equals("M") || type.equals("F")) {

			return typeDisp + " - " + exchDisp;
		}
		return "";
	}
}
