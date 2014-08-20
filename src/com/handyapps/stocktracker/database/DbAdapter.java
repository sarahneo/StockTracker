package com.handyapps.stocktracker.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.handyapps.stocktracker.MyApplication;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.NewsAlertObject;
import com.handyapps.stocktracker.model.NewsObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {

	private static DbAdapter dbAdapter;
	private SQLiteDatabase db;

	public static DbAdapter getSingleInstance() {
		if (dbAdapter == null)
			dbAdapter = new DbAdapter();
		return dbAdapter;
	}

	public DbAdapter() {
		this.db = MyApplication.getWritableDatabase();
	}
	
	public SQLiteDatabase getSqlDb(){
		return db;
	}

	/******************************************************************************************************************
	 * Portfolio Object
	 */
	public long insertPortfolio(String name, String initialCash, String currencyType, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(PortfolioObject.NAME, name);
		cv.put(PortfolioObject.INITIAL_CASH, initialCash);
		cv.put(PortfolioObject.CURRENCY_TYPE, currencyType);
		cv.put(PortfolioObject.MOD_TIME, modTime);

		return db.insert(PortfolioObject.TABLE_NAME, null, cv);
	}

	public long updatePortfolio(int id, String name, String initialCash, String currencyType, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(PortfolioObject.NAME, name);
		cv.put(PortfolioObject.INITIAL_CASH, initialCash);
		cv.put(PortfolioObject.CURRENCY_TYPE, currencyType);
		cv.put(PortfolioObject.MOD_TIME, modTime);

		return db.update(PortfolioObject.TABLE_NAME, cv, PortfolioObject.ID + "=" + id, null);
	}

	public long deletePortfolio(int id) throws SQLException {
		return db.delete(PortfolioObject.TABLE_NAME, PortfolioObject.ID + "=" + id, null);
	}

	public PortfolioObject fetchPortfolioByName(String portfolioName) {

		PortfolioObject po = new PortfolioObject();
		String where = PortfolioObject.NAME + " = '" + portfolioName + "'";

		Cursor c = db.query(PortfolioObject.TABLE_NAME, new String[] { PortfolioObject.ID, PortfolioObject.NAME, PortfolioObject.INITIAL_CASH,
				PortfolioObject.CURRENCY_TYPE, PortfolioObject.MOD_TIME }, where, null, null, null, PortfolioObject.NAME);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			po.load(c);
			return po;
		}
		c.close();
		return null;
	}

	public PortfolioObject fetchPortfolioByPortId(int portfolioId) {

		PortfolioObject po = new PortfolioObject();
		String where = PortfolioObject.ID + " = " + portfolioId;

		Cursor c = db.query(PortfolioObject.TABLE_NAME, new String[] { PortfolioObject.ID, PortfolioObject.NAME, PortfolioObject.INITIAL_CASH,
				PortfolioObject.CURRENCY_TYPE, PortfolioObject.MOD_TIME }, where, null, null, null, PortfolioObject.NAME);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			po.load(c);
			return po;
		}
		c.close();
		return null;
	}

	public List<PortfolioObject> fetchPortfolioList() {

		List<PortfolioObject> ls = new ArrayList<PortfolioObject>();
		Cursor c = getPortfolioCursor();

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					PortfolioObject po = new PortfolioObject();

					po.load(c);
					ls.add(po);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public long countPortfolioList() {

		String mSql = "select count( " + PortfolioObject.ID + " ) from " + PortfolioObject.TABLE_NAME;
		Cursor c = db.rawQuery(mSql, null);
		c.moveToFirst();
		long count = c.getLong(0);
		return count;

	}

	public Cursor getPortfolioCursor() {

		Cursor cs = db.query(PortfolioObject.TABLE_NAME, new String[] { PortfolioObject.ID, PortfolioObject.NAME, PortfolioObject.INITIAL_CASH, 
				PortfolioObject.CURRENCY_TYPE, PortfolioObject.MOD_TIME }, null, null, null, null, PortfolioObject.NAME);
		return cs;
	}
	
	/******************************************************************************************************************
	 * News Object
	 */
	public long insertNews(String symbol, String title, String link, String pubDate, String description, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(NewsObject.SYMBOL, symbol);
		cv.put(NewsObject.TITLE, title);
		cv.put(NewsObject.LINK, link);
		cv.put(NewsObject.PUB_DATE, pubDate);
		cv.put(NewsObject.DESCRIPTION, description);
		cv.put(NewsObject.MOD_TIME, modTime);

		return db.insert(NewsObject.TABLE_NAME, null, cv);
	}

	public long updateNews(int id, String symbol, String title, String link, String pubDate, String description, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(NewsObject.SYMBOL, symbol);
		cv.put(NewsObject.TITLE, title);
		cv.put(NewsObject.LINK, link);
		cv.put(NewsObject.PUB_DATE, pubDate);
		cv.put(NewsObject.DESCRIPTION, description);
		cv.put(NewsObject.MOD_TIME, modTime);

		return db.update(NewsObject.TABLE_NAME, cv, NewsObject.ID + "=" + id, null);
	}

	public long deleteNews(int id) throws SQLException {
		return db.delete(NewsObject.TABLE_NAME, NewsObject.ID + "=" + id, null);
	}

	public List<NewsObject> fetchNewsBySymbol(String symbol) {

		List<NewsObject> ls = new ArrayList<NewsObject>();

		String where = NewsObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(NewsObject.TABLE_NAME, new String[] { NewsObject.ID, NewsObject.SYMBOL, NewsObject.TITLE,
				NewsObject.LINK, NewsObject.PUB_DATE, NewsObject.DESCRIPTION, NewsObject.MOD_TIME}, 
				where, null, null, null, NewsObject.SYMBOL);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					NewsObject n = new NewsObject();

					n.load(c);
					ls.add(n);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public List<NewsObject> fetchNewsList() {

		List<NewsObject> ls = new ArrayList<NewsObject>();
		Cursor c = getNewsCursor();

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					NewsObject po = new NewsObject();

					po.load(c);
					ls.add(po);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public Cursor getNewsCursor() {

		Cursor cs = db.query(NewsObject.TABLE_NAME, new String[] { NewsObject.ID, NewsObject.SYMBOL, NewsObject.TITLE, 
				NewsObject.LINK, NewsObject.PUB_DATE, NewsObject.DESCRIPTION, NewsObject.MOD_TIME }, null, null, null, null, NewsObject.ID);
		return cs;
	}

	/******************************************************************************************************************
	 * Stock Object
	 */
	public long insertStock(String symbol, String name, String exch, String type, String typeDisp, String exchDisp, 
			long modTime, int colorCode) {

		ContentValues cv = new ContentValues();
		cv.put(StockObject.SYMBOL, symbol);
		cv.put(StockObject.NAME, name);
		cv.put(StockObject.EXCH, exch);
		cv.put(StockObject.TYPE, type);
		cv.put(StockObject.TYPE_DISP, typeDisp);
		cv.put(StockObject.EXCH_DISP, exchDisp);
		cv.put(StockObject.MOD_TIME, modTime);
		cv.put(StockObject.COLOR_CODE, colorCode);

		return db.insert(StockObject.TABLE_NAME, null, cv);
	}

	public long updateStock(int id, String symbol, String name, String exch, String type, String typeDisp, String exchDisp, 
			long modTime, int colorCode) {
		ContentValues cv = new ContentValues();
		cv.put(StockObject.SYMBOL, symbol);
		cv.put(StockObject.NAME, name);
		cv.put(StockObject.EXCH, exch);
		cv.put(StockObject.TYPE, type);
		cv.put(StockObject.TYPE_DISP, typeDisp);
		cv.put(StockObject.EXCH_DISP, exchDisp);
		cv.put(StockObject.MOD_TIME, modTime);
		cv.put(StockObject.COLOR_CODE, colorCode);
		return db.update(StockObject.TABLE_NAME, cv, StockObject.ID + "=" + id, null);
	}

	public StockObject fetchStockObjectBySymbol(String symbol) {

		StockObject so = new StockObject();
		String where = StockObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(StockObject.TABLE_NAME, new String[] { StockObject.ID, StockObject.SYMBOL, StockObject.NAME, StockObject.EXCH,
				StockObject.TYPE, StockObject.TYPE_DISP, StockObject.EXCH_DISP, StockObject.MOD_TIME, StockObject.COLOR_CODE }, 
				where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			so.load(c);
			return so;
		}
		c.close();
		return null;
	}

	public StockObject fetchStockObjectByStockId(int stockId) {

		StockObject so = new StockObject();
		String where = StockObject.ID + " = " + stockId;

		Cursor c = db.query(StockObject.TABLE_NAME, new String[] { StockObject.ID, StockObject.SYMBOL, StockObject.NAME, StockObject.EXCH,
				StockObject.TYPE, StockObject.TYPE_DISP, StockObject.EXCH_DISP, StockObject.MOD_TIME, StockObject.COLOR_CODE }, 
				where, null, null, null, StockObject.ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			so.load(c);
			return so;
		}
		c.close();
		return null;
	}

	public long deleteStock(int id) throws SQLException {
		return db.delete(StockObject.TABLE_NAME, StockObject.ID + "=" + id, null);
	}

	public List<StockObject> fetchStockObjectList() {

		List<StockObject> ls = new ArrayList<StockObject>();

		Cursor c = db.query(StockObject.TABLE_NAME, new String[] { StockObject.ID, StockObject.SYMBOL, StockObject.NAME, StockObject.EXCH,
				StockObject.TYPE, StockObject.TYPE_DISP, StockObject.EXCH_DISP, StockObject.MOD_TIME, StockObject.COLOR_CODE }, 
				null, null, null, null, StockObject.SYMBOL);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					StockObject so = new StockObject();

					so.load(c);
					ls.add(so);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
	
	public long countStockList() {

		String mSql = "select count( " + StockObject.ID + " ) from " + StockObject.TABLE_NAME;
		Cursor c = db.rawQuery(mSql, null);
		c.moveToFirst();
		long count = c.getLong(0);
		return count;

	}

	/******************************************************************************************************************
	 * Portfolio_Stock Object
	 */
	public long insertPortfolioStock(int portfolioId, int stockId) {

		ContentValues cv = new ContentValues();
		cv.put(PortfolioStockObject.PORT_ID, portfolioId);
		cv.put(PortfolioStockObject.STOCK_ID, stockId);		

		return db.insert(PortfolioStockObject.TABLE_NAME, null, cv);
	}

	public long updatePortfolioStock(int portfolioId, int stockId) {
		ContentValues cv = new ContentValues();
		cv.put(PortfolioStockObject.PORT_ID, portfolioId);
		cv.put(PortfolioStockObject.STOCK_ID, stockId);		
		return db.update(PortfolioStockObject.TABLE_NAME, cv, PortfolioStockObject.STOCK_ID + "=" + stockId, null);
	}

	public long deletePortfolioStockByStockId(int stockId) throws SQLException {
		return db.delete(PortfolioStockObject.TABLE_NAME, PortfolioStockObject.STOCK_ID + "=" + stockId, null);
	}

	public long deletePortfolioStockByPortIdAndStockId(int portId, int stockId) throws SQLException {
		String where = PortfolioStockObject.PORT_ID + "=" + portId + " AND " + PortfolioStockObject.STOCK_ID + "=" + stockId;
		return db.delete(PortfolioStockObject.TABLE_NAME, where, null);
	}

	public long deletePortfolioStockByPortId(int portId) throws SQLException {
		String where = PortfolioStockObject.PORT_ID + "=" + portId;
		return db.delete(PortfolioStockObject.TABLE_NAME, where, null);
	}

	public PortfolioStockObject fetchPortStockObjectByPortIdAndStockId(int portId, int stockId) {

		PortfolioStockObject ps = new PortfolioStockObject();
		String where = PortfolioStockObject.PORT_ID + " = " + portId + " AND " + PortfolioStockObject.STOCK_ID + " = " + stockId;

		Cursor c = db.query(PortfolioStockObject.TABLE_NAME, new String[] { PortfolioStockObject.PORT_ID, PortfolioStockObject.STOCK_ID}, 
				where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			ps.load(c);
			return ps;
		}
		c.close();
		return null;
	}

	public List<PortfolioStockObject> fetchPortStockListByPortId(int portfolioId) {

		List<PortfolioStockObject> ls = new ArrayList<PortfolioStockObject>();

		String where = PortfolioStockObject.PORT_ID + " = " + portfolioId;

		Cursor c = db.query(PortfolioStockObject.TABLE_NAME, new String[] { PortfolioStockObject.PORT_ID, PortfolioStockObject.STOCK_ID}, 
				where, null, null, null, PortfolioStockObject.PORT_ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					PortfolioStockObject ps = new PortfolioStockObject();

					ps.load(c);
					ls.add(ps);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<PortfolioStockObject> fetchPortStockList() {

		List<PortfolioStockObject> ls = new ArrayList<PortfolioStockObject>();
		Cursor c = db.query(PortfolioStockObject.TABLE_NAME, new String[] { PortfolioStockObject.PORT_ID, PortfolioStockObject.STOCK_ID}, 
				null, null, null, null, PortfolioStockObject.PORT_ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					PortfolioStockObject ps = new PortfolioStockObject();

					ps.load(c);
					ls.add(ps);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	/******************************************************************************************************************
	 * WatchList Object
	 */
	public long insertWatchList(String name, long modTime) {
		ContentValues cv = new ContentValues();
		cv.put(WatchlistObject.NAME, name);
		cv.put(WatchlistObject.MOD_TIME, modTime);

		return db.insert(WatchlistObject.TABLE_NAME, null, cv);
	}

	public long updateWatchList(int id, String name, long modTime) {
		ContentValues cv = new ContentValues();
		cv.put(WatchlistObject.NAME, name);
		cv.put(WatchlistObject.MOD_TIME, modTime);

		return db.update(WatchlistObject.TABLE_NAME, cv, WatchlistObject.ID + "=" + id, null);
	}

	public long deleteWatchList(int id) throws SQLException {
		return db.delete(WatchlistObject.TABLE_NAME, WatchlistObject.ID + "=" + id, null);
	}

	public WatchlistObject fetchWatchlistByName(String watchlistName) {

		WatchlistObject wl = new WatchlistObject();
		String where = WatchlistObject.NAME + " = '" + watchlistName + "'";

		Cursor c = db.query(WatchlistObject.TABLE_NAME, new String[] { WatchlistObject.ID, WatchlistObject.NAME, WatchlistObject.MOD_TIME }, where,
				null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			wl.load(c);
			return wl;
		}
		c.close();
		return null;
	}

	public WatchlistObject fetchWatchlistByWatchId(int watchlistId) {

		WatchlistObject wl = new WatchlistObject();
		String where = WatchlistObject.ID + " = " + watchlistId;

		Cursor c = db.query(WatchlistObject.TABLE_NAME, new String[] { WatchlistObject.ID, WatchlistObject.NAME, WatchlistObject.MOD_TIME }, where,
				null, null, null, WatchlistObject.NAME);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			wl.load(c);
			return wl;
		}
		c.close();
		return null;
	}

	public List<WatchlistObject> fetchWatchlists() {

		List<WatchlistObject> ls = new ArrayList<WatchlistObject>();
		Cursor c = getWatchlistCursor();

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					WatchlistObject wo = new WatchlistObject();

					wo.load(c);
					ls.add(wo);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public long countWatchlist() {

		String mSql = "select count( " + WatchlistObject.ID + " ) from " + WatchlistObject.TABLE_NAME;

		Cursor c = db.rawQuery(mSql, null);
		c.moveToFirst();
		long count = c.getLong(0);
		return count;

	}

	public Cursor getWatchlistCursor() {

		Cursor cs = db.query(WatchlistObject.TABLE_NAME, new String[] { WatchlistObject.ID, WatchlistObject.NAME, WatchlistObject.MOD_TIME }, null,
				null, null, null, WatchlistObject.NAME);
		return cs;
	}

	/******************************************************************************************************************
	 * Watch_Stock Object
	 */
	public long insertWatchStock(int watchId, int stockId) {

		ContentValues cv = new ContentValues();
		cv.put(WatchlistStockObject.WATCH_ID, watchId);
		cv.put(WatchlistStockObject.STOCK_ID, stockId);

		return db.insert(WatchlistStockObject.TABLE_NAME, null, cv);
	}

	public long updateWatchStock(int watchId, int stockId) {
		ContentValues cv = new ContentValues();
		cv.put(WatchlistStockObject.WATCH_ID, watchId);
		cv.put(WatchlistStockObject.STOCK_ID, stockId);
		return db.update(WatchlistStockObject.TABLE_NAME, cv, WatchlistStockObject.STOCK_ID + "=" + stockId, null);
	}

	public long deleteWatchStockByWatchIdAndStockId(int watchId, int stockId) throws SQLException {
		String where = WatchlistStockObject.WATCH_ID + "=" + watchId + " AND " + WatchlistStockObject.STOCK_ID + "=" + stockId;
		return db.delete(WatchlistStockObject.TABLE_NAME, where, null);
	}
	
	public long deleteWatchStockByStockId(int stockId) throws SQLException {
		return db.delete(WatchlistStockObject.TABLE_NAME, WatchlistStockObject.STOCK_ID + "=" + stockId, null);
	}

	public long deleteWatchStockByWatchlistId(int watchlistId) throws SQLException {
		return db.delete(WatchlistStockObject.TABLE_NAME, WatchlistStockObject.WATCH_ID + "=" + watchlistId, null);
	}

	public WatchlistStockObject fetchWatchlistStockObjectByStodckIdAndPortId(int watchId, int stockId) {

		WatchlistStockObject ws = new WatchlistStockObject();
		String where = WatchlistStockObject.WATCH_ID + " = " + watchId + " AND " + WatchlistStockObject.STOCK_ID + " = " + stockId;

		Cursor c = db.query(WatchlistStockObject.TABLE_NAME, new String[] { WatchlistStockObject.WATCH_ID, WatchlistStockObject.STOCK_ID }, where,
				null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			ws.load(c);
			return ws;
		}
		c.close();
		return null;
	}

	public List<WatchlistStockObject> fetchWatchStockListByWatchId(int watchId) {

		List<WatchlistStockObject> ls = new ArrayList<WatchlistStockObject>();

		String where = WatchlistStockObject.WATCH_ID + " = " + watchId;

		Cursor c = db.query(WatchlistStockObject.TABLE_NAME, new String[] { WatchlistStockObject.WATCH_ID, WatchlistStockObject.STOCK_ID }, where,
				null, null, null, WatchlistStockObject.WATCH_ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					WatchlistStockObject wl = new WatchlistStockObject();

					wl.load(c);
					ls.add(wl);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<WatchlistStockObject> fetchWatchStockList() {

		List<WatchlistStockObject> ls = new ArrayList<WatchlistStockObject>();
		Cursor c = db.query(WatchlistStockObject.TABLE_NAME, new String[] { WatchlistStockObject.WATCH_ID, WatchlistStockObject.STOCK_ID }, null,
				null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					WatchlistStockObject wl = new WatchlistStockObject();

					wl.load(c);
					ls.add(wl);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	/******************************************************************************************************************
	 * Transaction Object
	 */
	public long insertTransactionObject(String id, int stockId, int portId, String type, String price, int numOfShares, String fee, int tradeDate, double total, 
			String notes, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(TransactionObject.ID, id);
		cv.put(TransactionObject.STOCK_ID, stockId);
		cv.put(TransactionObject.PORT_ID, portId);
		cv.put(TransactionObject.TYPE, type);
		cv.put(TransactionObject.PRICE, price);
		cv.put(TransactionObject.NUM_SHARES, numOfShares);
		cv.put(TransactionObject.FEE, fee);
		cv.put(TransactionObject.TRADE_DATE, tradeDate);
		cv.put(TransactionObject.TOTAL, total);
		cv.put(TransactionObject.NOTES, notes);
		cv.put(TransactionObject.MOD_TIME, modTime);

		return db.insert(TransactionObject.TABLE_NAME, null, cv);
	}

	public long updateTransactionObject(String id, int stockId, int portId, String type, String price, int numOfShares, String fee, int tradeDate, double total,
			String notes, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(TransactionObject.ID, id);
		cv.put(TransactionObject.STOCK_ID, stockId);
		cv.put(TransactionObject.PORT_ID, portId);
		cv.put(TransactionObject.TYPE, type);
		cv.put(TransactionObject.PRICE, price);
		cv.put(TransactionObject.NUM_SHARES, numOfShares);
		cv.put(TransactionObject.FEE, fee);
		cv.put(TransactionObject.TRADE_DATE, tradeDate);
		cv.put(TransactionObject.TOTAL, total);
		cv.put(TransactionObject.NOTES, notes);
		cv.put(TransactionObject.MOD_TIME, modTime);

		return db.update(TransactionObject.TABLE_NAME, cv, TransactionObject.ID + "='" + id + "'", null);
	}

	public long deleteTransactionObject(String id) throws SQLException {
		return db.delete(TransactionObject.TABLE_NAME, TransactionObject.ID + "='" + id + "'", null);
	}

	public long deleteTransactionByPortfolioId(int portId) throws SQLException {
		return db.delete(TransactionObject.TABLE_NAME, TransactionObject.PORT_ID + "=" + portId, null);
	}

	public long deleteTransactionByPortIdAndStockId(int portId, int stockId) throws SQLException {
		String where = TransactionObject.PORT_ID + " = " + portId + " AND " + TransactionObject.STOCK_ID + " = " + stockId;
		return db.delete(TransactionObject.TABLE_NAME, where, null);
	}

	public List<TransactionObject> fetchTransactionObjectByStockIdAndPortId(int stockId, int portId) {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		String where = TransactionObject.STOCK_ID + " = " + stockId + " AND " + TransactionObject.PORT_ID + " = " + portId;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public List<TransactionObject> fetchTransactionObjectByStockIdAndPortIdWithTradeTimePeroid(int stockId, int portId, int fromTime, int toTime) {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		String where = TransactionObject.STOCK_ID + " = " + stockId + " AND " + TransactionObject.PORT_ID + " = " + portId + " AND "
				+ TransactionObject.TRADE_DATE + " >= " + fromTime + " AND " + TransactionObject.TRADE_DATE + " <= " + toTime;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public List<TransactionObject> fetchTransactionObjectByStockId(int stockId) {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		String where = TransactionObject.STOCK_ID + " = " + stockId;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
	
	public TransactionObject fetchTransactionObjectById(String id) {

		TransactionObject transactionObject = new TransactionObject();
		String where = TransactionObject.ID + " = '" + id + "'";

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			transactionObject.load(c);
			return transactionObject;
		}
		c.close();
		return null;
	}

	public List<TransactionObject> fetchTransactionObjectByStockIdWithTimePeroid(int stockId, int fromTime, int toTime) {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		String where = TransactionObject.STOCK_ID + " = " + stockId + " AND " + TransactionObject.TRADE_DATE + " > = " + fromTime
				+ TransactionObject.TRADE_DATE + " < = " + toTime;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public List<TransactionObject> fetchTransactionObjectByStockIdWithTimePeriod(int stockId, String fromTime, String toTime) {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		String where = TransactionObject.STOCK_ID + " = " + stockId;

		// String where = TransactionObject.STOCK_ID + " = " + stockId + " AND "
		// + TransactionObject;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public List<TransactionObject> fetchTransactionObjectAll() {

		List<TransactionObject> ls = new ArrayList<TransactionObject>();

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, null, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					TransactionObject to = new TransactionObject();

					to.load(c);
					ls.add(to);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public TransactionObject fetchTransactionByRowId(int rowId) {

		TransactionObject transactionObject = new TransactionObject();
		String where = TransactionObject.ID + " = " + rowId;

		Cursor c = db.query(TransactionObject.TABLE_NAME, new String[] { TransactionObject.ID, TransactionObject.STOCK_ID, TransactionObject.PORT_ID,
				TransactionObject.TYPE, TransactionObject.PRICE, TransactionObject.NUM_SHARES, TransactionObject.FEE, TransactionObject.TRADE_DATE,
				TransactionObject.TOTAL, TransactionObject.NOTES, TransactionObject.MOD_TIME }, where, null, null, null, TransactionObject.TRADE_DATE);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			transactionObject.load(c);
			return transactionObject;
		}
		c.close();
		return null;
	}

	/******************************************************************************************************************
	 * Quote Object
	 */
	public long insertQuoteObject(String symbol, String prevClosePrice, String openPrice, String bidPrice, String bidSize, String askPrice,
			String askSize, String yearTargetEst, String beta, String nextEarningDate, String dayLow, String dayHigh, String dayRange,
			String yearLow, String yearHigh, String yearRange, String volume, String avgVolume, String marketCap, String peRatioTTM, String epsTTM,
			String dividend, String dividendYield, String lastTradePrice, String change, String changeInPercent, String lastTradeDate,
			String lastTradeTime, String currency, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(QuoteObject.SYMBOL, symbol);
		cv.put(QuoteObject.PREV_CLOSE_PRICE, prevClosePrice);
		cv.put(QuoteObject.OPEN_PRICE, openPrice);
		cv.put(QuoteObject.BIT_PRICE, bidPrice);
		cv.put(QuoteObject.BID_SIZE, bidSize);
		cv.put(QuoteObject.ASK_PRICE, askPrice);
		cv.put(QuoteObject.ASK_SIZE, askSize);
		cv.put(QuoteObject.YEAR_TARGET, yearTargetEst);
		cv.put(QuoteObject.BETA, beta);
		cv.put(QuoteObject.EARNINGS_DATE, nextEarningDate);
		cv.put(QuoteObject.DAY_LOW, dayLow);
		cv.put(QuoteObject.DAY_HIGH, dayHigh);
		cv.put(QuoteObject.DAY_RANGE, dayRange);
		cv.put(QuoteObject.YEAR_LOW, yearLow);
		cv.put(QuoteObject.YEAR_HIGH, yearHigh);
		cv.put(QuoteObject.YEAR_RANGE, yearRange);
		cv.put(QuoteObject.VOLUME, volume);
		cv.put(QuoteObject.AVG_VOLUME, avgVolume);
		cv.put(QuoteObject.MARKET_CAP, marketCap);
		cv.put(QuoteObject.PE_RATIO_TTM, peRatioTTM);
		cv.put(QuoteObject.EPS_TTM, epsTTM);
		cv.put(QuoteObject.DIVIDEND, dividend);
		cv.put(QuoteObject.DIVIDEND_YIELD, dividendYield);
		cv.put(QuoteObject.LAST_TRADE_PRICE, lastTradePrice);
		cv.put(QuoteObject.CHANGE, change);
		cv.put(QuoteObject.CHANGE_IN_PERCENT, changeInPercent);
		cv.put(QuoteObject.LAST_TRADE_DATE, lastTradeDate);
		cv.put(QuoteObject.LAST_TRADE_TIME, lastTradeTime);
		cv.put(QuoteObject.CURRENCY, currency);
		cv.put(QuoteObject.MOD_TIME, modTime);

		return db.insert(QuoteObject.TABLE_NAME, null, cv);
	}

	public long updateQuote(int id, String symbol, String prevClosePrice, String openPrice, String bidPrice, String bidSize, String askPrice,
			String askSize, String yearTargetEst, String beta, String nextEarningDate, String dayLow, String dayHigh, String dayRange,
			String yearLow, String yearHigh, String yearRange, String volume, String avgVolume, String marketCap, String peRatioTTM, String epsTTM,
			String dividend, String dividendYield, String lastTradePrice, String change, String changeInPercent, String lastTradeDate,
			String lastTradeTime, String currency, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(QuoteObject.ID, id);
		cv.put(QuoteObject.SYMBOL, symbol);
		cv.put(QuoteObject.PREV_CLOSE_PRICE, prevClosePrice);
		cv.put(QuoteObject.OPEN_PRICE, openPrice);
		cv.put(QuoteObject.BIT_PRICE, bidPrice);
		cv.put(QuoteObject.BID_SIZE, bidSize);
		cv.put(QuoteObject.ASK_PRICE, askPrice);
		cv.put(QuoteObject.ASK_SIZE, askSize);
		cv.put(QuoteObject.YEAR_TARGET, yearTargetEst);
		cv.put(QuoteObject.BETA, beta);
		cv.put(QuoteObject.EARNINGS_DATE, nextEarningDate);
		cv.put(QuoteObject.DAY_LOW, dayLow);
		cv.put(QuoteObject.DAY_HIGH, dayHigh);
		cv.put(QuoteObject.DAY_RANGE, dayRange);
		cv.put(QuoteObject.YEAR_LOW, yearLow);
		cv.put(QuoteObject.YEAR_HIGH, yearHigh);
		cv.put(QuoteObject.YEAR_RANGE, yearRange);
		cv.put(QuoteObject.VOLUME, volume);
		cv.put(QuoteObject.AVG_VOLUME, avgVolume);
		cv.put(QuoteObject.MARKET_CAP, marketCap);
		cv.put(QuoteObject.PE_RATIO_TTM, peRatioTTM);
		cv.put(QuoteObject.DIVIDEND, dividend);
		cv.put(QuoteObject.DIVIDEND_YIELD, dividendYield);
		cv.put(QuoteObject.LAST_TRADE_PRICE, lastTradePrice);
		cv.put(QuoteObject.CHANGE, change);
		cv.put(QuoteObject.CHANGE_IN_PERCENT, changeInPercent);
		cv.put(QuoteObject.LAST_TRADE_DATE, lastTradeDate);
		cv.put(QuoteObject.LAST_TRADE_TIME, lastTradeTime);
		cv.put(QuoteObject.CURRENCY, currency);
		cv.put(QuoteObject.MOD_TIME, modTime);

		return db.update(QuoteObject.TABLE_NAME, cv, QuoteObject.ID + "=" + id, null);
	}

	public long deleteQuote(int id) throws SQLException {
		return db.delete(QuoteObject.TABLE_NAME, QuoteObject.ID + "=" + id, null);
	}

	public QuoteObject fetchQuoteObjectBySymbol(String symbol) {

		String where = QuoteObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(QuoteObject.TABLE_NAME, QuoteObject.PROJECTION, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			QuoteObject qo = new QuoteObject();
			qo.load(c);
			c.close();
			return qo;
		}
		return null;
	}

	public String fetchLastTradePriceBySymbol(String symbol) {

		String where = QuoteObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(QuoteObject.TABLE_NAME, new String[] { QuoteObject.LAST_TRADE_PRICE }, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			String lastTradePrice = c.getString(c.getColumnIndex(QuoteObject.LAST_TRADE_PRICE));
			c.close();
			return lastTradePrice;
		}
		c.close();
		return "";
	}

	/**
	 * Alert Object
	 * 
	 * @param modTime2
	 * @param isLowerTargetOn
	 */
	public long insertAlert(int id, String symbol, String lastTradePrice, String upperPrice, String lowerPrice, int isNotifyOn, long isUpperTargetOn,
			int isLowerTargetOn, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(AlertObject.ID, id);
		cv.put(AlertObject.SYMBOL, symbol);
		cv.put(AlertObject.PRICE, lastTradePrice);
		cv.put(AlertObject.UPPER_TARGET, upperPrice);
		cv.put(AlertObject.LOWER_TARGET, lowerPrice);
		cv.put(AlertObject.IS_NOTIFY_ON, isNotifyOn);
		cv.put(AlertObject.IS_UPPER_TARGET_ON, isUpperTargetOn);
		cv.put(AlertObject.IS_LOWER_TARGET_ON, isLowerTargetOn);
		cv.put(AlertObject.MOD_TIME, modTime);

		return db.insert(AlertObject.TABLE_NAME, null, cv);
	}

	public long updateAlert(int id, String symbol, String lastTradePrice, String upperPrice, String lowerPrice, int isNotify, long isUpperTargetOn,
			int isLowerTargetOn, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(AlertObject.ID, id);
		cv.put(AlertObject.SYMBOL, symbol);
		cv.put(AlertObject.PRICE, lastTradePrice);
		cv.put(AlertObject.UPPER_TARGET, upperPrice);
		cv.put(AlertObject.LOWER_TARGET, lowerPrice);
		cv.put(AlertObject.IS_NOTIFY_ON, isNotify);
		cv.put(AlertObject.IS_UPPER_TARGET_ON, isUpperTargetOn);
		cv.put(AlertObject.IS_LOWER_TARGET_ON, isLowerTargetOn);
		cv.put(AlertObject.MOD_TIME, modTime);

		return db.update(AlertObject.TABLE_NAME, cv, AlertObject.ID + "=" + id, null);
	}

	public long deleteAlert(int id) throws SQLException {
		return db.delete(AlertObject.TABLE_NAME, AlertObject.ID + "=" + id, null);
	}

	public AlertObject fetchAlertObjectBySymbol(String symbol) {

		String where = AlertObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(AlertObject.TABLE_NAME, AlertObject.PROJECTION, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			AlertObject ao = new AlertObject();
			ao.load(c);
			c.close();
			return ao;
		}
		return null;
	}
	
	public AlertObject fetchAlertObjectById(int id) {

		String where = AlertObject.ID + " = '" + id + "'";

		Cursor c = db.query(AlertObject.TABLE_NAME, AlertObject.PROJECTION, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			AlertObject ao = new AlertObject();
			ao.load(c);
			c.close();
			return ao;
		}
		return null;
	}


	public List<AlertObject> fetchAlertObjectAll() {

		List<AlertObject> ls = new ArrayList<AlertObject>();

		Cursor c = db.query(AlertObject.TABLE_NAME, AlertObject.PROJECTION, null, null, null, null, AlertObject.SYMBOL);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					AlertObject ao = new AlertObject();

					ao.load(c);
					ls.add(ao);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public long countAlertList() {

		String mSql = "select count( " + AlertObject.ID + " ) from " + AlertObject.TABLE_NAME;
		Cursor c = db.rawQuery(mSql, null);
		c.moveToFirst();
		long count = c.getLong(0);
		return count;
	}
	
	/**
	 * News Alert Object
	 * 
	 */
	public long insertNewsAlert(int id, String symbol, int alertFrequency, int isNotifyOn, 
			long startTime, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(NewsAlertObject.ID, id);
		cv.put(NewsAlertObject.SYMBOL, symbol);
		cv.put(NewsAlertObject.ALERT_FREQUENCY, alertFrequency);
		cv.put(NewsAlertObject.IS_NOTIFY_ON, isNotifyOn);
		cv.put(NewsAlertObject.START_TIME, startTime);
		cv.put(NewsAlertObject.MOD_TIME, modTime);

		return db.insert(NewsAlertObject.TABLE_NAME, null, cv);
	}

	public long updateNewsAlert(int id, String symbol, int alertFrequency, int isNotifyOn, 
			long startTime, long modTime) {

		ContentValues cv = new ContentValues();
		cv.put(NewsAlertObject.ID, id);
		cv.put(NewsAlertObject.SYMBOL, symbol);
		cv.put(NewsAlertObject.ALERT_FREQUENCY, alertFrequency);
		cv.put(NewsAlertObject.IS_NOTIFY_ON, isNotifyOn);
		cv.put(NewsAlertObject.START_TIME, startTime);
		cv.put(NewsAlertObject.MOD_TIME, modTime);

		return db.update(NewsAlertObject.TABLE_NAME, cv, NewsAlertObject.ID + "=" + id, null);
	}

	public long deleteNewsAlert(int id) throws SQLException {
		return db.delete(NewsAlertObject.TABLE_NAME, NewsAlertObject.ID + "=" + id, null);
	}

	public NewsAlertObject fetchNewsAlertObjectBySymbol(String symbol) {

		String where = NewsAlertObject.SYMBOL + " = '" + symbol + "'";

		Cursor c = db.query(NewsAlertObject.TABLE_NAME, NewsAlertObject.PROJECTION, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			NewsAlertObject ao = new NewsAlertObject();
			ao.load(c);
			c.close();
			return ao;
		}
		return null;
	}
	
	public NewsAlertObject fetchNewsAlertObjectById(int id) {

		String where = NewsAlertObject.ID + " = '" + id + "'";

		Cursor c = db.query(NewsAlertObject.TABLE_NAME, NewsAlertObject.PROJECTION, where, null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			NewsAlertObject ao = new NewsAlertObject();
			ao.load(c);
			c.close();
			return ao;
		}
		return null;
	}

	public List<NewsAlertObject> fetchNewsAlertObjectAll() {

		List<NewsAlertObject> ls = new ArrayList<NewsAlertObject>();

		Cursor c = db.query(NewsAlertObject.TABLE_NAME, NewsAlertObject.PROJECTION, null, null, null, null, NewsAlertObject.SYMBOL);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					NewsAlertObject ao = new NewsAlertObject();

					ao.load(c);
					ls.add(ao);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}

	public long countNewsAlertList() {

		String mSql = "select count( " + NewsAlertObject.ID + " ) from " + NewsAlertObject.TABLE_NAME;
		Cursor c = db.rawQuery(mSql, null);
		c.moveToFirst();
		long count = c.getLong(0);
		return count;
	}
	
	/******************************************************************************************************************
	 * Cash_Pos Object
	 */
	public long insertCashPos(String id, int portfolioId, String txnType, String amount,
			int txnDate, long lastAccessed, long lastUpdate) {

		ContentValues cv = new ContentValues();
		cv.put(CashPosObject.ID, id);
		cv.put(CashPosObject.PORTFOLIO_ID, portfolioId);
		cv.put(CashPosObject.TXN_TYPE, txnType);
		cv.put(CashPosObject.AMOUNT, amount);
		cv.put(CashPosObject.TXN_DATE, txnDate);
		cv.put(CashPosObject.LAST_ACCESSED, lastAccessed);
		cv.put(CashPosObject.LAST_UPDATE, lastUpdate);

		return db.insert(CashPosObject.TABLE_NAME, null, cv);
	}

	public long updateCashPos(String id, int portfolioId, String txnType, String amount,
			int txnDate, long lastAccessed, long lastUpdate) {
		ContentValues cv = new ContentValues();
		cv.put(CashPosObject.ID, id);
		cv.put(CashPosObject.PORTFOLIO_ID, portfolioId);
		cv.put(CashPosObject.TXN_TYPE, txnType);
		cv.put(CashPosObject.AMOUNT, amount);
		cv.put(CashPosObject.TXN_DATE, txnDate);
		cv.put(CashPosObject.LAST_ACCESSED, lastAccessed);
		cv.put(CashPosObject.LAST_UPDATE, lastUpdate);
		
		return db.update(CashPosObject.TABLE_NAME, cv, CashPosObject.ID + "='" + id + "'", null);
	}

	public long deleteCashPosById(String id) throws SQLException {
		return db.delete(CashPosObject.TABLE_NAME, CashPosObject.ID + "='" + id + "'", null);
	}

	public long deleteCashPosByIdAndPortId(int id, int portId) throws SQLException {
		String where = CashPosObject.ID + "=" + id + " AND " + CashPosObject.PORTFOLIO_ID + "=" + portId;
		return db.delete(CashPosObject.TABLE_NAME, where, null);
	}

	public long deleteCashPosByPortId(int portId) throws SQLException {
		String where = CashPosObject.PORTFOLIO_ID + "=" + portId;
		return db.delete(CashPosObject.TABLE_NAME, where, null);
	}

	public CashPosObject fetchCashPosObjectByIdAndPortId(String id, int portId) {

		CashPosObject ps = new CashPosObject();
		String where = CashPosObject.ID + " = '" + id + "' AND " + CashPosObject.PORTFOLIO_ID + " = " + portId;

		Cursor c = db.query(CashPosObject.TABLE_NAME, new String[] { CashPosObject.ID, CashPosObject.PORTFOLIO_ID, CashPosObject.TXN_TYPE,
				CashPosObject.AMOUNT, CashPosObject.TXN_DATE, CashPosObject.LAST_ACCESSED, CashPosObject.LAST_UPDATE}, where,
				null, null, null, null);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();
			ps.load(c);
			return ps;
		}
		c.close();
		return null;
	}

	public List<CashPosObject> fetchCashPosByPortId(int portfolioId) {

		List<CashPosObject> ls = new ArrayList<CashPosObject>();

		String where = CashPosObject.PORTFOLIO_ID + " = " + portfolioId;

		Cursor c = db.query(CashPosObject.TABLE_NAME, new String[] { CashPosObject.ID, CashPosObject.PORTFOLIO_ID, CashPosObject.TXN_TYPE,
				CashPosObject.AMOUNT, CashPosObject.TXN_DATE, CashPosObject.LAST_ACCESSED, CashPosObject.LAST_UPDATE}, where,
				null, null, null, CashPosObject.PORTFOLIO_ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					CashPosObject ps = new CashPosObject();

					ps.load(c);
					ls.add(ps);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<CashPosObject> fetchCashPosList() {

		List<CashPosObject> ls = new ArrayList<CashPosObject>();
		Cursor c = db.query(CashPosObject.TABLE_NAME, new String[] { CashPosObject.ID, CashPosObject.PORTFOLIO_ID, CashPosObject.TXN_TYPE,
				CashPosObject.AMOUNT, CashPosObject.TXN_DATE, CashPosObject.LAST_ACCESSED, CashPosObject.LAST_UPDATE}, null,
				null, null, null, CashPosObject.ID);

		if (c != null && c.getCount() > 0) {

			c.moveToFirst();

			try {
				while (!c.isAfterLast()) {

					CashPosObject ps = new CashPosObject();

					ps.load(c);
					ls.add(ps);
					c.moveToNext();
				}
			} finally {

				c.close();
			}
		}
		if (ls.size() > 0) {
			return ls;
		} else {
			return Collections.emptyList();
		}
	}
}