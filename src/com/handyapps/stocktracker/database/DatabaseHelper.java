package com.handyapps.stocktracker.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionObject;

public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String DATABASE_CREATE_TABLE_PORTFOLIO_STOCK = "create table portfolio_stock (port_id integer not null, "
			+ "stock_id integer not null) ";			

	protected static final String DATABASE_CREATE_TABLE_PORTFOLIO = "create table portfolio (id integer primary key autoincrement, "
			+ "name text, "
			+ "initial_cash text default 0, "
			+ "currency_type text default \'USD\', "
			+ "mod_time integer) ";

	protected static final String DATABASE_CREATE_TABLE_STOCK = "create table stock (id integer primary key autoincrement, "
			+ "symbol text not null, "
			+ "name text, "
			+ "exch text, "
			+ "type text, "
			+ "type_disp text, "
			+ "exch_disp text, "
			+ "mod_time integer, "
			+ "color_code integer) ";

	protected static final String DATABASE_CREATE_TABLE_MY_TRANSACTION = "create table my_transaction (id text primary key, "
			+ "stock_id integer not null, "
			+ "port_id integer not null, "
			+ "type text, "
			+ "price text, "
			+ "num_shares integer, "
			+ "fee text, "
			+ "trade_date text, "
			+ "total float, "
			+ "notes text, "
			+ "mod_time integer) ";
	

	protected static final String DATABASE_CREATE_TABLE_QUOTE = "create table quotes (id integer primary key autoincrement, "
			+ "symbol text, "
			+ "prev_close_price text, "
			+ "open_price text, "
			+ "bid_price text, "
			+ "bid_size text, "
			+ "ask_price text, "
			+ "ask_size text, "
			+ "year_target text, "
			+ "beta text, "
			+ "earnings_date text, "
			+ "day_low text, "
			+ "day_high text, "
			+ "day_range text, "
			+ "year_low text, "
			+ "year_high text, "
			+ "year_range text, "
			+ "volume text, "
			+ "avg_volume text, "
			+ "market_cap text, "
			+ "pe_ratio_ttm text, "
			+ "eps_ttm text, "
			+ "dividend text, "
			+ "dividend_yield text, "
			+ "last_trade_price text, "
			+ "change text, "
			+ "change_in_percent text, "
			+ "last_trade_date text, "
			+ "last_trade_time text, "
			+ "currency text default \'USD\', "
			+ "mod_time integer) ";

	protected static final String DATABASE_CREATE_TABLE_ALERT = "create table alert (id integer primary key, "
			+ "symbol not null, "
			+ "price text, "
			+ "upper_target text, "
			+ "lower_target text, "
			+ "is_notify_on integer, "
			+ "is_upper_target_on integer, "
			+ "is_lower_target_on integer, "
			+ "mod_time integer) ";
	
	protected static final String DATABASE_CREATE_TABLE_NEWS_ALERT = "create table news_alert (id integer primary key, "
			+ "symbol not null, "
			+ "alert_frequency text, "
			+ "is_notify_on integer, "
			+ "start_time integer, "
			+ "mod_time integer) ";
	
	protected static final String DATABASE_CREATE_TABLE_NEWS = "create table news (id integer primary key autoincrement, "
			+ "symbol text, "
			+ "title text, "
			+ "link text, "
			+ "pub_date text,"
			+ "description text, "
			+ "mod_time integer) ";

	protected static final String DATABASE_CREATE_TABLE_WATCH_STOCK = "create table watch_stock (watch_id integer not null, "
			+ "stock_id integer not null) ";

	protected static final String DATABASE_CREATE_TABLE_WATCHLIST = "create table watchlist (id integer primary key autoincrement, "
			+ "name text not null, "
			+ "mod_time integer) ";
	
	protected static final String DATABASE_CREATE_TABLE_USER_CASH = "create table if not exists user_cash (id text primary key, "
			+ "portfolio_id integer not null, "
			+ "txn_type text, "
			+ "txn_date text, "
			+ "amount text, "
			+ "last_accessed integer, "
			+ "last_update integer) ";
	
	protected static final String DATABASE_ALTER_TABLE_PORTFOLIO_ADD_INITIAL_CASH = "alter table portfolio add column "
			+ "initial_cash text default 0";
	
	protected static final String DATABASE_ALTER_TABLE_PORTFOLIO_ADD_CURRENCY_TYPE = "alter table portfolio add column "
			+ "currency_type text default \'USD\'";

	protected static final String DATABASE_ALTER_TABLE_STOCK_ADD_COLOR_CODE = "alter table stock add column "
			+ "color_code integer";
	
	protected static final String DATABASE_ALTER_TABLE_QUOTE_ADD_CURRENCY = "alter table quotes add column "
			+ "currency text default \'USD\'";
	
	protected static final String DATABASE_DROP_TABLE_MY_TRANSACTION = "drop table my_transaction";

	protected static final String DATABASE_NAME = "stock_tracker";
	protected static final int DATABASE_VERSION = 2;

	protected Context mCtx;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(DATABASE_CREATE_TABLE_PORTFOLIO_STOCK);
		db.execSQL(DATABASE_CREATE_TABLE_PORTFOLIO);
		db.execSQL(DATABASE_CREATE_TABLE_STOCK);
		db.execSQL(DATABASE_CREATE_TABLE_MY_TRANSACTION);
		db.execSQL(DATABASE_CREATE_TABLE_QUOTE);
		db.execSQL(DATABASE_CREATE_TABLE_ALERT);
		db.execSQL(DATABASE_CREATE_TABLE_WATCH_STOCK);
		db.execSQL(DATABASE_CREATE_TABLE_WATCHLIST);
		db.execSQL(DATABASE_CREATE_TABLE_USER_CASH); // new for DB v2
		db.execSQL(DATABASE_CREATE_TABLE_NEWS_ALERT); // new for DB v2
		db.execSQL(DATABASE_CREATE_TABLE_NEWS); // new for DB v2

		onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
			// Add new tables
			db.execSQL(DATABASE_CREATE_TABLE_USER_CASH); // new for DB v2
			db.execSQL(DATABASE_CREATE_TABLE_NEWS_ALERT); // new for DB v2
			db.execSQL(DATABASE_CREATE_TABLE_NEWS); // new for DB v2
			
			// Edit existing tables
			db.execSQL(DATABASE_ALTER_TABLE_PORTFOLIO_ADD_CURRENCY_TYPE);
			db.execSQL(DATABASE_ALTER_TABLE_PORTFOLIO_ADD_INITIAL_CASH);
			db.execSQL(DATABASE_ALTER_TABLE_QUOTE_ADD_CURRENCY);
			db.execSQL(DATABASE_ALTER_TABLE_STOCK_ADD_COLOR_CODE);
			
			// Add random colors for existing stocks
			List<StockObject> soList = fetchStockObjectList(db);
			if (soList != null) {
				for (StockObject so : soList) {
					so.setColorCode(StockObject.getRandomColor());
					updateStock(so, db);
				}
			}
			
			// Drop and re-create my_transaction table
			List<TransactionObject> toList = fetchTransactionObjectAll(db);
			if (toList != null) {
				for (TransactionObject to : toList) {
					to.setId(String.valueOf(UUID.randomUUID()));
					updateTransaction(to, db);
				}
			}
			
			db.execSQL(DATABASE_DROP_TABLE_MY_TRANSACTION);
			db.execSQL(DATABASE_CREATE_TABLE_MY_TRANSACTION);
			
			if (toList != null)
				for (TransactionObject to : toList) {
					insertTransaction(to, db);
				}
			
			break;
		}
		
	}
	
	private List<StockObject> fetchStockObjectList(SQLiteDatabase db) {

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
	
	private List<TransactionObject> fetchTransactionObjectAll(SQLiteDatabase db) {

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
	
	private boolean updateStock(StockObject so, SQLiteDatabase db) {
		long modTime = System.currentTimeMillis();
		long rowId = updateStock(db, so.getId(), so.getSymbol(), so.getName(), so.getExch(), so.getType(), so.getTypeDisp(), 
				so.getExchDisp(), modTime, so.getColorCode());

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	private long updateStock(SQLiteDatabase db, int id, String symbol, String name, String exch, String type, String typeDisp, 
			String exchDisp, long modTime, int colorCode) {
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
	
	private boolean updateTransaction(TransactionObject transObj, SQLiteDatabase db) {
		long modTime = System.currentTimeMillis();
		long rowId = updateTransactionObject(db, transObj.getId(), transObj.getStockId(), transObj.getPortId(), transObj.getType(), 
				transObj.getPrice(), transObj.getNumOfShares(), transObj.getFee(), transObj.getTradeDate(), transObj.getTotal(),
				transObj.getNotes(), modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	private long updateTransactionObject(SQLiteDatabase db, String id, int stockId, int portId, String type, String price, 
			int numOfShares, String fee, int tradeDate, double total, String notes, long modTime) {

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
	
	public boolean insertTransaction(TransactionObject to, SQLiteDatabase db) {

		String id = String.valueOf(UUID.randomUUID());
		long modTime = System.currentTimeMillis();
		long rowId = insertTransactionObject(db, id, to.getStockId(), to.getPortId(), to.getType(), to.getPrice(), 
				to.getNumOfShares(), to.getFee(), to.getTradeDate(), to.getTotal(), to.getNotes(), modTime);

		if (rowId == -1) {
			return false;
		}
		return true;
	}
	
	private long insertTransactionObject(SQLiteDatabase db, String id, int stockId, int portId, String type, 
			String price, int numOfShares, String fee, int tradeDate, double total, String notes, long modTime) {

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
	
}
