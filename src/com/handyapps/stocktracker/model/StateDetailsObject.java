package com.handyapps.stocktracker.model;

public class StateDetailsObject {
	
	private double netSell;
	private long volumeSold;
	private double netPurchase;
	private long volumePachased;
	private double currentHolding;
	private long currentHoldingVolume;
	private long currentShortVolume;
	private double netProfitLoss;
	private double feesAndCommission;
	private double returnOnInvesment;
	
	public StateDetailsObject() {
	}
	
	public double getNetSell() {
		return netSell;
	}
	public void setNetSell(double netSell) {
		this.netSell = netSell;
	}
	public double getNetPurchase() {
		return netPurchase;
	}
	public void setNetPurchase(double netPurchase) {
		this.netPurchase = netPurchase;
	}
	public double getCurrentHolding() {
		return currentHolding;
	}
	public void setCurrentHolding(double currentHolding) {
		this.currentHolding = currentHolding;
	}
	public double getNetProfitLoss() {
		return netProfitLoss;
	}
	public void setNetProfitLoss(double netProfitLoss) {
		this.netProfitLoss = netProfitLoss;
	}
	public double getReturnOnInvesment() {
		return returnOnInvesment;
	}
	public void setReturnOnInvesment(double returnOnInvesment) {
		this.returnOnInvesment = returnOnInvesment;
	}
	public long getVolumeSold() {
		return volumeSold;
	}
	public void setVolumeSold(long volumeSold) {
		this.volumeSold = volumeSold;
	}
	public long getVolumePachased() {
		return volumePachased;
	}
	public void setVolumePachased(long volumePachased) {
		this.volumePachased = volumePachased;
	}
	public long getCurrentHoldingVolume() {
		return currentHoldingVolume;
	}
	public void setCurrentHoldingVolume(long currentHoldingVolume) {
		this.currentHoldingVolume = currentHoldingVolume;
	}
	public long getCurrentShortVolume() {
		return currentShortVolume;
	}
	public void setCurrentShortVolume(long currentShortVolume) {
		this.currentShortVolume = currentShortVolume;
	}
	public double getFeesAndCommission() {
		return feesAndCommission;
	}
	public void setFeesAndCommission(double feesAndCommission) {
		this.feesAndCommission = feesAndCommission;
	}
}
