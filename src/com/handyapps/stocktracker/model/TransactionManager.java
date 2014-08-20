package com.handyapps.stocktracker.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.utils.DecimalsConverter;

public class TransactionManager {

	public static SingleStockReport getSingleStockReport(List<TransactionObject> transactionList) {

		SingleStockReport report = new SingleStockReport();
		List<TransactionObject> list = transactionList;

		List<TransactionObject> listBuy = new ArrayList<TransactionObject>();
		List<TransactionObject> listSold = new ArrayList<TransactionObject>();
		List<TransactionObject> listDividend = new ArrayList<TransactionObject>();

		String sLastTradePrice = "0";
		String sPrevClosePrice = "0";
		String sPercentageChange = "+0.00%";
		String sChange = "0";

		if (list != null && list.size() > 0) {

			// Get Last trade price
			int stockId = list.get(0).getStockId();
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
			String symbol = so.getSymbol();
			QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
			if (quote != null) {
				sLastTradePrice = quote.getLastTradePrice();
				sPrevClosePrice = quote.getPrevClosePrice();
				sPercentageChange = quote.getChangeInPercent();
				sChange = quote.getChange();
			}

			for (TransactionObject mTo : list) {
				if (mTo.getType().equalsIgnoreCase(TransactionObject.BUY_TYPE)) {
					listBuy.add(mTo);
				} else if (mTo.getType().equalsIgnoreCase(TransactionObject.SELL_TYPE)) {
					listSold.add(mTo);
				} else if (mTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)
						|| mTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
					listDividend.add(mTo);
				}
			}
		}

		double boughtWorth = 0;
		double soldWorth = 0;
		double remainingValue = 0;
		double profit = 0;
		double dailyProfit = 0;
		//double avgPrice = 0;
		double avgBoughtPrice = 0;
		double realizedGainLoss = 0;
		double unrealizedGainLoss = 0;

		long boughtShares = 0;
		long soldShares = 0;
		double dLastTradePrice = 0;
		double dPrevClosePrice = 0;

		try {
			dLastTradePrice = Double.valueOf(sLastTradePrice);
			dPrevClosePrice = Double.valueOf(sPrevClosePrice);
		} catch (Exception e1) {
		}

		// Get: bought amount, number of shares
		if (listBuy != null && listBuy.size() > 0) {
			for (TransactionObject bTo : listBuy) {
				boughtWorth = boughtWorth + getTotalWorthOfSingleTransaction(bTo);
				boughtShares = boughtShares + bTo.getNumOfShares();
			}
		}

		// Get: sold amount, number of shares
		if (listSold != null && listSold.size() > 0) {
			for (TransactionObject sTo : listSold) {
				soldWorth = soldWorth + getTotalWorthOfSingleTransaction(sTo);
				soldShares = soldShares + sTo.getNumOfShares();
			}
		}

		// Get: dividend amount.
		int dividentShares = 0;
		if (listDividend != null && listDividend.size() > 0) {
			for (TransactionObject dTo : listDividend) {
				if (dTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
					dividentShares = dividentShares + dTo.getNumOfShares();
				} else if (dTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
					double mTotalDividend = dTo.getTotal();
					soldWorth = soldWorth + mTotalDividend;
				}
			}
		}

		//avgPrice = (boughtWorth + soldWorth)/(boughtShares + soldShares);
		avgBoughtPrice = boughtWorth/boughtShares;
		long remainingShares = boughtShares - soldShares + dividentShares;
		long shortShares = 0;
		if (remainingShares > 0) {
			remainingValue = dLastTradePrice * remainingShares;
		} else {
			shortShares = Math.abs(remainingShares);
			remainingShares = 0;
		}

		//remainingValue  = remainingValue + dLastTradePrice * dividentShares;
		profit = (remainingValue + soldWorth) - boughtWorth;
		//profit = (dLastTradePrice - avgBoughtPrice) * remainingShares;
		
		dailyProfit = (dLastTradePrice - dPrevClosePrice) * remainingShares;
		
		if (boughtShares == soldShares) {
			realizedGainLoss = soldWorth - boughtWorth;
			unrealizedGainLoss = 0;
		} else if (soldShares > boughtShares) { //short sell
			realizedGainLoss = (soldWorth/soldShares)*boughtShares - boughtWorth;
			unrealizedGainLoss = 0;
		} else {
			realizedGainLoss = soldWorth - (boughtWorth/boughtShares)*soldShares;
			unrealizedGainLoss = remainingValue - (remainingShares*avgBoughtPrice);
		}
		report.setAvgPrice(avgBoughtPrice);
		report.setRemainingQty(remainingShares);
		report.setShortQty(shortShares);
		report.setTotalValue(remainingValue);
		report.setNetProfitOrLoss(profit);
		report.setDailyProfitOrLoss(dailyProfit);
		report.setLastTradePrice(dLastTradePrice);
		report.setChange(sChange);
		report.setChangeInPercent(sPercentageChange);
		report.setRealizedGain(realizedGainLoss);
		report.setUnrealizedGain(unrealizedGainLoss);
		report.setTotalCost(boughtWorth);
		return report;
	}

	public static double getSingleStockProfitByTrasactionList(List<TransactionObject> transactionList) {

		List<TransactionObject> list = transactionList;

		List<TransactionObject> listBuy = new ArrayList<TransactionObject>();
		List<TransactionObject> listSold = new ArrayList<TransactionObject>();
		List<TransactionObject> listDividend = new ArrayList<TransactionObject>();

		String sLastTradePrice = "0";

		if (list != null && list.size() > 0) {

			// Get Last trade price
			int stockId = list.get(0).getStockId();
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
			String symbol = so.getSymbol();
			QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
			if (quote != null) {
				sLastTradePrice = quote.getLastTradePrice();
			}

			for (TransactionObject mTo : list) {
				if (mTo.getType().equalsIgnoreCase(TransactionObject.BUY_TYPE)) {
					listBuy.add(mTo);
				} else if (mTo.getType().equalsIgnoreCase(TransactionObject.SELL_TYPE)) {
					listSold.add(mTo);
				} else if (mTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)
						|| mTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
					listDividend.add(mTo);
				}
			}
		}

		double boughtWorth = 0;
		double soldWorth = 0;
		double remainingWorth = 0;
		double profit = 0;

		long boughtShares = 0;
		long soldShares = 0;
		double dLastTradePrice = 0;

		try {
			dLastTradePrice = Double.valueOf(sLastTradePrice);
		} catch (Exception e1) {
		}

		// Get: bought amount, number of shares
		if (listBuy != null && listBuy.size() > 0) {
			for (TransactionObject bTo : listBuy) {
				boughtWorth = boughtWorth + getTotalWorthOfSingleTransaction(bTo);
				boughtShares = boughtShares + bTo.getNumOfShares();
			}
		}

		// Get: sold amount, number of shares
		if (listSold != null && listSold.size() > 0) {
			for (TransactionObject sTo : listSold) {
				soldWorth = soldWorth + getTotalWorthOfSingleTransaction(sTo);
				soldShares = soldShares + sTo.getNumOfShares();
			}
		}

		// Get: dividend amount.
		int dividentShares = 0;
		if (listDividend != null && listDividend.size() > 0) {
			for (TransactionObject dTo : listDividend) {
				if (dTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
					dividentShares = dividentShares + dTo.getNumOfShares();
				} else if (dTo.getType().equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
					double mTotalDividend = dTo.getTotal();
					soldWorth = soldWorth + mTotalDividend;
				}
			}
		}

		long remainingShares = boughtShares - soldShares + dividentShares;
		if (remainingShares > 0) {
			remainingWorth = dLastTradePrice * remainingShares;
		}else {
			remainingShares = 0;
		}
		//note: dividentShare always is extra one, so final step needs to add-in:
		//remainingWorth  = remainingWorth + dLastTradePrice * dividentShares;
		profit = (remainingWorth + soldWorth) - boughtWorth;
		return profit;
	}

	public static double getTotalWorthOfSingleTransaction(TransactionObject transactionObject) {

		double result = 0.0;
		double price = 0;
		double volume = 0;
		double fee = 0;

		TransactionObject mTransactionObject = transactionObject;

		if (mTransactionObject != null) {

			String mTradeType = mTransactionObject.getType();
			if (mTradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
				result = mTransactionObject.getTotal();
				return result;
			} else {

				if (mTransactionObject.getPrice() != null) {
					String sPrice = mTransactionObject.getPrice();
					try {
						price = Double.valueOf(sPrice);
					} catch (NumberFormatException e) {
					}
				}

				volume = mTransactionObject.getNumOfShares();
				if (mTransactionObject.getFee() != null) {
					String sFees = mTransactionObject.getFee();
					try {
						fee = Double.valueOf(sFees);
					} catch (NumberFormatException e) {
					}
				}
				result = price * volume + fee;
				return result;
			}
		}
		return result;
	}

	public static SingleTickerDataset getSingleStockProfitReport(int portId, int stockId, Context ctx) {

		int mPortId = portId;
		int mStockId = stockId;
		String mSymbol = "", mPriceChange = "", mChangeInPercentage = "";
		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
		if (so != null) {
			mSymbol = so.getSymbol();
		}
		SingleTickerDataset dataset = new SingleTickerDataset();
		dataset.setHasTxn(true);

		List<TransactionObject> mTransList = DbAdapter.getSingleInstance().fetchTransactionObjectByStockIdAndPortId(mStockId, mPortId);
		if (mTransList.isEmpty())
			dataset.setHasTxn(false);
		SingleStockReport report = TransactionManager.getSingleStockReport(mTransList);
		double avgPrice = report.getAvgPrice();
		double mSingleProfit = report.getNetProfitOrLoss();
		double mSingleDailyProfit = report.getDailyProfitOrLoss();
		double mSingleStockRemindingValue = report.getTotalValue();
		long remainingQty = report.getRemainingQty();
		long shortQty = report.getShortQty();
		double mLastTradePrice = report.getLastTradePrice();
		mPriceChange = report.getChange();
		mChangeInPercentage = report.getChangeInPercent();

		if (mLastTradePrice == 0 || mPriceChange.equals("") || mChangeInPercentage.equals("")) {
			QuoteObject mQo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(mSymbol);
			if (mQo != null) {
				String msLastTradePrice = mQo.getLastTradePrice();
				mPriceChange = mQo.getChange();
				mChangeInPercentage = mQo.getChangeInPercent();
				try {
					mLastTradePrice = Double.valueOf(msLastTradePrice);
				} catch (NumberFormatException e) {
				}
			}
		}

		int numberOfDecimals = Constants.NUMBER_OF_DECIMALS;
		String sAvgPrice = DecimalsConverter.convertToStringValueBaseOnLocale(avgPrice, numberOfDecimals, ctx);
		String sSingleStockRemainingValue = DecimalsConverter.convertToStringValueBaseOnLocale(mSingleStockRemindingValue, numberOfDecimals, ctx);
		String sSingleStockProfit = DecimalsConverter.convertToStringValueBaseOnLocale(mSingleProfit, numberOfDecimals, ctx);
		String sSingleStockDailyProfit = DecimalsConverter.convertToStringValueBaseOnLocale(mSingleDailyProfit, numberOfDecimals, ctx);
		String sLastTradePrice = DecimalsConverter.convertToStringValueBaseOnLocale(mLastTradePrice, numberOfDecimals, ctx);
		String strQty = String.valueOf(remainingQty);

		String symbolWithQty = mSymbol + " " + "(" + strQty + " x " + sLastTradePrice + ")";

		dataset.setAvgPrice(sAvgPrice);
		dataset.setDoubleNetProfit(mSingleProfit);
		dataset.setDoubleDailyProfit(mSingleDailyProfit);
		dataset.setNetProfit(sSingleStockProfit);
		dataset.setDailyProfit(sSingleStockDailyProfit);
		dataset.setDoubleTotalValue(mSingleStockRemindingValue);
		dataset.setTotalValue(sSingleStockRemainingValue);
		dataset.setQuantity(remainingQty);
		dataset.setShortQty(shortQty);
		dataset.setSymbolWithQty(symbolWithQty);
		dataset.setLastPrice(sLastTradePrice);
		dataset.setPriceChange(mPriceChange);
		dataset.setChangeInPercent(mChangeInPercentage);
		dataset.setRealizedGain(report.getRealizedGain());
		dataset.setUnrealizedGain(report.getUnrealizedGain());
		dataset.setDoubleTotalCost(report.getTotalCost());

		return dataset;
	}

	// **Need to be deleted:
	public static StateDetailsObject getStatisticBySingleStock(List<TransactionObject> mTransList) {

		String sLastTradePrice = "0";
		StateDetailsObject state = new StateDetailsObject();

		List<TransactionObject> list = mTransList;

		List<TransactionObject> listBuy = new ArrayList<TransactionObject>();
		List<TransactionObject> listSold = new ArrayList<TransactionObject>();

		if (list != null && list.size() > 0) {

			// Get Last trade price
			int stockId = list.get(0).getStockId();
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
			String symbol = so.getSymbol();
			QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
			if (quote != null) {
				sLastTradePrice = quote.getLastTradePrice();
			}

			for (TransactionObject mTo : list) {

				if (mTo.getType().equalsIgnoreCase("b")) {
					listBuy.add(mTo);
				} else {
					listSold.add(mTo);
				}
			}
		}

		double boughtWorth = 0;
		double soldWorth = 0;
		double remindingWorth = 0;
		double profit = 0;
		double returnOnInvestment = 0;
		double feesAndCommission = 0;
		long boughtShares = 0;
		long soldShares = 0;

		// Get: bought amount, number of shares
		if (listBuy != null && listBuy.size() > 0) {
			for (TransactionObject bTo : listBuy) {
				boughtWorth = boughtWorth + getTotalWorthOfSingleTransaction(bTo);
				boughtShares = boughtShares + bTo.getNumOfShares();

				try {
					feesAndCommission = feesAndCommission + Double.parseDouble(bTo.getFee());
				} catch (NumberFormatException e) {
				}
			}
		}

		// Get: sold amount, number of shares
		if (listSold != null && listSold.size() > 0) {
			for (TransactionObject sTo : listSold) {
				soldWorth = soldWorth + getTotalWorthOfSingleTransaction(sTo);
				soldShares = soldShares + sTo.getNumOfShares();
				try {
					feesAndCommission = feesAndCommission + Double.parseDouble(sTo.getFee());
				} catch (NumberFormatException e) {
				}
			}
		}

		double dLastTradePrice = 0;
		long remainingShares = 0;
		long shortShares = 0;

		try {
			dLastTradePrice = Double.valueOf(sLastTradePrice);
		} catch (Exception e1) {
		}

		remainingShares = boughtShares - soldShares;
		if (remainingShares > 0) {
			remindingWorth = dLastTradePrice * remainingShares;
		} else {
			shortShares = Math.abs(remainingShares); 
			remainingShares = 0;
		}

		if (list != null && list.size() > 0) {
			profit = (remindingWorth + soldWorth) - boughtWorth;
			returnOnInvestment = profit / boughtWorth * 100;
		}

		state.setNetSell(soldWorth);
		state.setVolumeSold(soldShares);
		state.setNetPurchase(boughtWorth);
		state.setVolumePachased(boughtShares);
		state.setCurrentHolding(remindingWorth);
		state.setCurrentHoldingVolume(remainingShares);
		state.setCurrentShortVolume(shortShares);
		state.setNetProfitLoss(profit);
		state.setFeesAndCommission(feesAndCommission);
		state.setReturnOnInvesment(returnOnInvestment);
		return state;
	}
}
