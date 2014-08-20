package com.handyapps.stocktracker.model;

import android.database.Cursor;

import com.handyapps.stocktracker.database.DbAdapter;

public class QuoteObject {

	public static final String TABLE_NAME = "quotes";
	public static final String ID = "id";
	public static final String SYMBOL = "symbol";
	public static final String PREV_CLOSE_PRICE = "prev_close_price";
	public static final String OPEN_PRICE = "open_price";
	public static final String BIT_PRICE = "bid_price";
	public static final String BID_SIZE = "bid_size";
	public static final String ASK_PRICE = "ask_price";
	public static final String ASK_SIZE = "ask_size";
	public static final String YEAR_TARGET = "year_target";
	public static final String BETA = "beta";
	public static final String EARNINGS_DATE = "earnings_date";
	public static final String DAY_LOW = "day_low";
	public static final String DAY_HIGH = "day_high";
	public static final String DAY_RANGE = "day_range";
	public static final String YEAR_LOW = "year_low";
	public static final String YEAR_HIGH = "year_high";
	public static final String YEAR_RANGE = "year_range";
	public static final String VOLUME = "volume";
	public static final String AVG_VOLUME = "avg_volume";
	public static final String MARKET_CAP = "market_cap";
	public static final String PE_RATIO_TTM = "pe_ratio_ttm";
	public static final String EPS_TTM = "eps_ttm";
	public static final String DIVIDEND = "dividend";
	public static final String DIVIDEND_YIELD = "dividend_yield";
	public static final String LAST_TRADE_PRICE = "last_trade_price";
	public static final String CHANGE = "change";
	public static final String CHANGE_IN_PERCENT = "change_in_percent";
	public static final String LAST_TRADE_DATE = "last_trade_date";
	public static final String LAST_TRADE_TIME = "last_trade_time";
	public static final String CURRENCY = "currency";
	public static final String MOD_TIME = "mod_time";

	public int id;
	public String symbol;
	public String prevClosePrice;
	public String openPrice;
	public String bidPrice;
	public String bidSize;
	public String askPrice;
	public String askSize;
	public String yearTargetEst;
	public String beta;
	public String nextEarningDate;
	public String dayLow;
	public String dayHigh;
	public String dayRange;
	public String yearLow;
	public String yearHigh;
	public String yearRange;
	public String volume;
	public String avgVolume;
	public String marketCap;
	public String peRatioTTM;
	public String epsTTM;
	public String dividend;
	public String dividendYield;
	public String lastTradePrice;
	public String change;
	public String changeInPercent;
	public String lastTradeDate;
	public String lastTradeTime;
	public String currency;
	public long modTime;

	public QuoteObject() {
	}

	public QuoteObject(int id, String symbol, String prevClosePrice, String openPrice, String bidPrice, String bidSize, String askPrice,
			String askSize, String yearTargetEst, String beta, String nextEarningDate, String dayLow, String dayHigh, String dayRange,
			String yearLow, String yearHigh, String yearRange, String volume, String avgVolume, String marketCap, String peRatioTTM, String epsTTM,
			String dividend, String dividendYield, String lastTradePrice, String change, String changeInPercent, String lastTradeDate,
			String lastTradeTime, String currency, long modTime) {

		this.symbol = symbol;
		this.prevClosePrice = prevClosePrice;
		this.openPrice = openPrice;
		this.bidPrice = bidPrice;
		this.bidSize = bidSize;
		this.askPrice = askPrice;
		this.askSize = askSize;
		this.yearTargetEst = yearTargetEst;
		this.beta = beta;
		this.nextEarningDate = nextEarningDate;
		this.dayLow = dayLow;
		this.dayHigh = dayHigh;
		this.dayRange = dayRange;
		this.yearLow = yearLow;
		this.yearHigh = yearHigh;
		this.yearRange = yearRange;
		this.volume = volume;
		this.avgVolume = avgVolume;
		this.marketCap = marketCap;
		this.peRatioTTM = peRatioTTM;
		this.epsTTM = epsTTM;
		this.dividend = dividend;
		this.dividendYield = dividendYield;
		this.lastTradePrice = lastTradePrice;
		this.change = change;
		this.changeInPercent = changeInPercent;
		this.lastTradeDate = lastTradeDate;
		this.lastTradeTime = lastTradeTime;
		this.currency = currency;
		this.modTime = modTime;
	}

	public boolean insert() {

		modTime = System.currentTimeMillis();
		long rowId = DbAdapter.getSingleInstance().insertQuoteObject(symbol, prevClosePrice, openPrice, bidPrice, bidSize, askPrice, askSize,
				yearTargetEst, beta, nextEarningDate, dayLow, dayHigh, dayRange, yearLow, yearHigh, yearRange, volume, avgVolume, marketCap,
				peRatioTTM, epsTTM, dividend, dividendYield, lastTradePrice, change, changeInPercent, lastTradeDate, lastTradeTime, currency,
				modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean update() {

		modTime = System.currentTimeMillis();

		long rowId = DbAdapter.getSingleInstance().updateQuote(id, symbol, prevClosePrice, openPrice, bidPrice, bidSize, askPrice, askSize,
				yearTargetEst, beta, nextEarningDate, dayLow, dayHigh, dayRange, yearLow, yearHigh, yearRange, volume, avgVolume, marketCap,
				peRatioTTM, epsTTM, dividend, dividendYield, lastTradePrice, change, changeInPercent, lastTradeDate, lastTradeTime, currency,
				modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}

	public boolean delete() {
		long rowId = DbAdapter.getSingleInstance().deleteQuote(id);
		if (rowId > -1) {
			return true;
		}
		return false;
	}

	public void load(Cursor cursor) {

		try {

			id = cursor.getInt(cursor.getColumnIndex(ID));
			symbol = cursor.getString(cursor.getColumnIndex(SYMBOL));
			prevClosePrice = cursor.getString(cursor.getColumnIndex(PREV_CLOSE_PRICE));
			openPrice = cursor.getString(cursor.getColumnIndex(OPEN_PRICE));
			bidPrice = cursor.getString(cursor.getColumnIndex(BIT_PRICE));
			bidSize = cursor.getString(cursor.getColumnIndex(BID_SIZE));
			askPrice = cursor.getString(cursor.getColumnIndex(ASK_PRICE));
			askSize = cursor.getString(cursor.getColumnIndex(ASK_SIZE));
			yearTargetEst = cursor.getString(cursor.getColumnIndex(YEAR_TARGET));
			beta = cursor.getString(cursor.getColumnIndex(BETA));
			nextEarningDate = cursor.getString(cursor.getColumnIndex(EARNINGS_DATE));
			dayLow = cursor.getString(cursor.getColumnIndex(DAY_LOW));
			dayHigh = cursor.getString(cursor.getColumnIndex(DAY_HIGH));
			dayRange = cursor.getString(cursor.getColumnIndex(DAY_RANGE));
			yearLow = cursor.getString(cursor.getColumnIndex(YEAR_LOW));
			yearHigh = cursor.getString(cursor.getColumnIndex(YEAR_HIGH));
			yearRange = cursor.getString(cursor.getColumnIndex(YEAR_RANGE));
			volume = cursor.getString(cursor.getColumnIndex(VOLUME));
			avgVolume = cursor.getString(cursor.getColumnIndex(AVG_VOLUME));
			marketCap = cursor.getString(cursor.getColumnIndex(MARKET_CAP));
			peRatioTTM = cursor.getString(cursor.getColumnIndex(PE_RATIO_TTM));
			epsTTM = cursor.getString(cursor.getColumnIndex(EPS_TTM));
			dividend = cursor.getString(cursor.getColumnIndex(DIVIDEND));
			dividendYield = cursor.getString(cursor.getColumnIndex(DIVIDEND_YIELD));
			lastTradePrice = cursor.getString(cursor.getColumnIndex(LAST_TRADE_PRICE));
			change = cursor.getString(cursor.getColumnIndex(CHANGE));
			changeInPercent = cursor.getString(cursor.getColumnIndex(CHANGE_IN_PERCENT));
			lastTradeDate = cursor.getString(cursor.getColumnIndex(LAST_TRADE_DATE));
			lastTradeTime = cursor.getString(cursor.getColumnIndex(LAST_TRADE_TIME));
			currency = cursor.getString(cursor.getColumnIndex(CURRENCY));
			modTime = cursor.getLong(cursor.getColumnIndex(MOD_TIME));

		} catch (Exception e) {
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getPrevClosePrice() {
		return prevClosePrice;
	}

	public void setPrevClosePrice(String prevClosePrice) {
		this.prevClosePrice = prevClosePrice;
	}

	public String getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(String openPrice) {
		this.openPrice = openPrice;
	}

	public String getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(String bidPrice) {
		this.bidPrice = bidPrice;
	}

	public String getBidSize() {
		return bidSize;
	}

	public void setBidSize(String bidSize) {
		this.bidSize = bidSize;
	}

	public String getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(String askPrice) {
		this.askPrice = askPrice;
	}

	public String getAskSize() {
		return askSize;
	}

	public void setAskSize(String askSize) {
		this.askSize = askSize;
	}

	public String getYearTargetEst() {
		return yearTargetEst;
	}

	public void setYearTargetEst(String yearTargetEst) {
		this.yearTargetEst = yearTargetEst;
	}

	public String getBeta() {
		return beta;
	}

	public void setBeta(String beta) {
		this.beta = beta;
	}

	public String getNextEarningDate() {
		return nextEarningDate;
	}

	public void setNextEarningDate(String nextEarningDate) {
		this.nextEarningDate = nextEarningDate;
	}

	public String getDayLow() {
		return dayLow;
	}

	public void setDayLow(String dayLow) {
		this.dayLow = dayLow;
	}

	public String getDayHigh() {
		return dayHigh;
	}

	public void setDayHigh(String dayHigh) {
		this.dayHigh = dayHigh;
	}

	public String getDayRange() {
		return dayRange;
	}

	public void setDayRange(String dayRange) {
		this.dayRange = dayRange;
	}

	public String getYearLow() {
		return yearLow;
	}

	public void setYearLow(String yearLow) {
		this.yearLow = yearLow;
	}

	public String getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(String yearHigh) {
		this.yearHigh = yearHigh;
	}

	public String getYearRange() {
		return yearRange;
	}

	public void setYearRange(String yearRange) {
		this.yearRange = yearRange;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getAvgVolume() {
		return avgVolume;
	}

	public void setAvgVolume(String avgVolume) {
		this.avgVolume = avgVolume;
	}

	public String getMarketCap() {
		return marketCap;
	}

	public void setMarketCap(String marketCap) {
		this.marketCap = marketCap;
	}

	public String getPeRatioTTM() {
		return peRatioTTM;
	}

	public void setPeRatioTTM(String peRatioTTM) {
		this.peRatioTTM = peRatioTTM;
	}

	public String getEpsTTM() {
		return epsTTM;
	}

	public void setEpsTTM(String epsTTM) {
		this.epsTTM = epsTTM;
	}

	public String getDividend() {
		return dividend;
	}

	public void setDividend(String dividend) {
		this.dividend = dividend;
	}

	public String getLastTradePrice() {
		return lastTradePrice;
	}

	public void setLastTradePrice(String lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
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

	public String getLastTradeDate() {
		return lastTradeDate;
	}

	public void setLastTradeDate(String lastTradeDate) {
		this.lastTradeDate = lastTradeDate;
	}

	public String getLastTradeTime() {
		return lastTradeTime;
	}

	public void setLastTradeTime(String lastTradeTime) {
		this.lastTradeTime = lastTradeTime;
	}

	public long getModTime() {
		return modTime;
	}

	public void setModTime(long modTime) {
		this.modTime = modTime;
	}

	public String getDividendYield() {
		return dividendYield;
	}

	public void setDividendYield(String dividendYield) {
		this.dividendYield = dividendYield;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public static final String[] PROJECTION = { QuoteObject.ID, QuoteObject.SYMBOL, QuoteObject.PREV_CLOSE_PRICE, QuoteObject.OPEN_PRICE,
			QuoteObject.BIT_PRICE, QuoteObject.BID_SIZE, QuoteObject.ASK_PRICE, QuoteObject.ASK_SIZE, QuoteObject.YEAR_TARGET, QuoteObject.BETA,
			QuoteObject.EARNINGS_DATE, QuoteObject.DAY_LOW, QuoteObject.DAY_HIGH, QuoteObject.DAY_RANGE, QuoteObject.YEAR_LOW, QuoteObject.YEAR_HIGH,
			QuoteObject.YEAR_RANGE, QuoteObject.VOLUME, QuoteObject.AVG_VOLUME, QuoteObject.MARKET_CAP, QuoteObject.PE_RATIO_TTM,
			QuoteObject.EPS_TTM, QuoteObject.DIVIDEND, QuoteObject.DIVIDEND_YIELD, QuoteObject.LAST_TRADE_PRICE, QuoteObject.CHANGE,
			QuoteObject.CHANGE_IN_PERCENT, QuoteObject.LAST_TRADE_DATE, QuoteObject.LAST_TRADE_TIME, QuoteObject.CURRENCY, QuoteObject.MOD_TIME };
}
