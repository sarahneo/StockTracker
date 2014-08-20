package com.handyapps.stocktracker.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.MyBannerAd;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog;
import com.handyapps.stocktracker.fragments.MyTradesFragment;
import com.handyapps.stocktracker.fragments.NewsStocksFragment;
import com.handyapps.stocktracker.fragments.SummaryFragment;
import com.handyapps.stocktracker.model.AlertObject;
import com.handyapps.stocktracker.model.CategoryStock;
import com.handyapps.stocktracker.model.NewsAlertObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.NetworkConnectivity;
import com.handyapps.stocktracker.utils.TextColorPicker;
import com.handyapps.stocktracker.utils.ThemeUtils;
import com.viewpagerindicator.TabPageIndicator;

public class TransactionDetailsFragmentActivity extends Activity {

	private int fromId;
	private int mCurrentIndex;
	private int mStockId;
	private int mPortId;
	private int countSelectionStock = 0;
	private String[] arrIndicator;
	private static final String KEY_BUNDLE_TRANSACTION = "KEY_BUNDLE_TRANSACTION";
	private static final String KEY_FROM_ID_TRANSACTION = "KEY_FROM_ID_TRANSACTION";
	private static final String KEY_SYMBOL = "KEY_SYMBOL";
	
	public static final int TAB_POSITION_SUMMARY = 0;
	public static final int TAB_POSITION_NEWS = 1;
	public static final int TAB_POSITION_TRADES= 2;

	private TextView tvSymbol;
	private TextView tvCompanyName;
	private TextView tvLastTradePrice;
	private TextView tvChanges;
	private MenuItemImpl refreshMenuItemImpl;
	private MenuItem refreshMenuItem;
	private ActionBar actionBar;
	private View abprogress;
	private LayoutInflater inflater;
	private ArrayAdapter<String> mSpinnerAdapter;

	private Resources res;
	private List<Fragment> fragments;
	private List<String> mListString;
	private List<CategoryStock> mList;
	private FragmentManager fm;
	private Bundle bundle;
	private CategoryStock cStockObj;
	private MyBroadcastReceiver receiver;
	private SharedPreferences sp;
	private UpdateQuoteTaskSingleSymbol quoteTask = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		res = getResources();

		ThemeUtils.onActivityCreateSetTheme(this, true);
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | 
				ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
		
		setContentView(R.layout.transaction_details_fragment_activity);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		abprogress = inflater.inflate(R.layout.progress_dialog_holo, null);
		
		MyBannerAd ad = new MyBannerAd(findViewById(android.R.id.content), getApplicationContext());
		ad.loadAd();
		
		cStockObj = new CategoryStock();
		mStockId = sp.getInt(Constants.SP_KEY_STOCK_ID, -1);
		mList = new ArrayList<CategoryStock>();
		mListString = new ArrayList<String>();

		mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, mListString);
		mSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				if (countSelectionStock >= 1) {
					mCurrentIndex = position;
					mList.get(position).getStockObj().getId();
					cStockObj = mList.get(position);
					mStockId = cStockObj.getStockObj().getId();
					refreshValues();
				}
				
				countSelectionStock++;
				
				return true;
			}
		};
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);	
		
		// 2. bundle data:
		bundle = getIntent().getExtras();
		if (bundle != null) {
			fromId = bundle.getInt(Constants.KEY_FROM);
		}

		// 3. components setup:
		initialComponentsSetup();

		// 4. Update Current quote:
		boolean hasConn = NetworkConnectivity.hasNetworkConnection(this);
		if (!hasConn) {
			Toast.makeText(this, res.getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
				refreshMenuItem.setActionView(null);
			else
				refreshMenuItemImpl.setActionView(null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerBroadcast();

		switch (fromId) {

		case Constants.FROM_FIND_STOCKS:
			findStockSetup(bundle);
			break;
		case Constants.FROM_PORTFOLIO_LIST:
			mPortId = bundle.getInt(Constants.KEY_PORTFOLIO_ID);
			watchlistOrPortfolioSetup(bundle);
			break;
		case Constants.FROM_WATCH_LIST:
			watchlistOrPortfolioSetup(bundle);
			break;
		}
		
		tvSymbol.setText(cStockObj.getStockObj().getSymbol());
		tvCompanyName.setText(cStockObj.getStockObj().getName());

		refreshQuotes(cStockObj.getStockObj().getSymbol());
		startAlarm();
		refreshAdapter();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
    		unregisterReceiver(receiver);
    	} catch (IllegalArgumentException e) {
    	}
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopAlarm();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (quoteTask != null) {
			quoteTask.cancel(true);
		}
		
	    sp.edit().putInt(Constants.SP_KEY_STOCK_ID, mStockId).commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_BUNDLE_TRANSACTION, bundle);
		outState.putInt(KEY_FROM_ID_TRANSACTION, fromId);
		outState.putString(KEY_SYMBOL, cStockObj.getStockObj().getSymbol());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		bundle = savedInstanceState.getBundle(KEY_BUNDLE_TRANSACTION);
		fromId = savedInstanceState.getInt(KEY_FROM_ID_TRANSACTION);
		String symbol = savedInstanceState.getString(KEY_SYMBOL);
		cStockObj.setStockObj(DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol));
	}

	private void startAlarm() {
		String mSymbol = "";
		if (cStockObj.getStockObj() != null) {
			mSymbol = cStockObj.getStockObj().getSymbol();
			StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
			if (mSo == null) {
				MyAlarmManager am = new MyAlarmManager(this);
				am.startRepeatAlarmUpdateSingleQuote(mSymbol);
				sp.edit().putBoolean(Constants.SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED, true).commit();
			}
		}
	}

	private void stopAlarm() {
		boolean isAlarmStarted = sp.getBoolean(Constants.SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED, false);
		if (isAlarmStarted) {
			String mSymbol = "";
			if (cStockObj.getStockObj() != null) {
				mSymbol = cStockObj.getStockObj().getSymbol();
			}
			MyAlarmManager am = new MyAlarmManager(this);
			am.stopRepeatAlarmUpdateSingleQuote(mSymbol);
			sp.edit().putBoolean(Constants.SP_KEY_IS_SINGLE_QUOTE_ALARM_STARTED, false).commit();
		}
	}


	private void updateQuoteAndChartFromYahoo() {

		// 1. update quote task:
		if (quoteTask == null) {
			quoteTask = new UpdateQuoteTaskSingleSymbol(this, null, 0, 0);
			quoteTask.execute(cStockObj.getStockObj().getSymbol());
		} else {
			quoteTask.cancel(true);
			quoteTask = null;
			quoteTask = new UpdateQuoteTaskSingleSymbol(this, null, 0, 0);
			quoteTask.execute(cStockObj.getStockObj().getSymbol());
		}

		// 2. update chart task: profit: when SUMMARY TAB IS SELETED, IT WILL DO
		// UPDATE, OTHERWISE WAMN'T
		Intent iUpdateOneDayChart = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
		iUpdateOneDayChart.putExtra(Constants.KEY_IS_UPDATE_CHART, true);
		sendBroadcast(iUpdateOneDayChart);

		// 3. update news task: profit: when NEWS TAB IS SELETED, IT WILL DO
		// UPDATE, OTHERWISE WANN'T
		Intent iUpdateRelatedNews = new Intent(Constants.ACTION_NEWS_LIST_FRAGMENT);
		if (cStockObj.getStockObj() != null) {
			iUpdateRelatedNews.putExtra(Constants.KEY_SYMBOL, cStockObj.getStockObj().getSymbol());
			iUpdateRelatedNews.putExtra(Constants.KEY_COMPANY_NAME, cStockObj.getStockObj().getName());
		}
		sendBroadcast(iUpdateRelatedNews);
	}

	private void initialComponentsSetup() {

		// 1. Base Components Setup:
		tvSymbol = (TextView) findViewById(R.id.tv_symbol);
		tvCompanyName = (TextView) findViewById(R.id.tv_company_name);
		tvLastTradePrice = (TextView) findViewById(R.id.tv_last_trade_price);
		tvChanges = (TextView) findViewById(R.id.tv_change_in_percent);

		// 2. Base Components Setup:
		fm = getSupportFragmentManager();
		fragments = new ArrayList<Fragment>();
		fragments.add(SummaryFragment.newInstance());
		//fragments.add(DetailsFragment.newInstance());
		fragments.add(NewsStocksFragment.newInstance());
		fragments.add(MyTradesFragment.newInstance());
		tabPageIndicatorSetup();
	}

	private void findStockSetup(Bundle bundle) {

		Bundle mBundle = bundle;
		String mSymbol = mBundle.getString(Constants.KEY_SYMBOL);
		String mCompanyName = mBundle.getString(Constants.KEY_COMPANY_NAME);
		String mExch = mBundle.getString(Constants.KEY_EXCH);
		String mType = mBundle.getString(Constants.KEY_TYPE);
		String mTypeDisp = mBundle.getString(Constants.KEY_TYPE_DISP);
		String mExchDisp = mBundle.getString(Constants.KEY_EXCH_DISP);
		StockObject so = new StockObject();
		so.setSymbol(mSymbol);
		so.setName(mCompanyName);
		so.setExch(mExch);
		so.setExchDisp(mExchDisp);
		so.setType(mType);
		so.setTypeDisp(mTypeDisp);
		cStockObj.setStockObj(so);
		mStockId = -1;

	}

	private void watchlistOrPortfolioSetup(Bundle bundle) {
		int mStockId = bundle.getInt(Constants.KEY_STOCK_ID);
		cStockObj.setStockObj(DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId));
		mStockId = cStockObj.getStockObj().getId();
	}
	
	private void refreshValues() {
		tvSymbol.setText(cStockObj.getStockObj().getSymbol());
		tvCompanyName.setText(cStockObj.getStockObj().getName());

		refreshQuotes(cStockObj.getStockObj().getSymbol());
		startAlarm();
		
		sp.edit().putInt(Constants.SP_KEY_STOCK_ID, mStockId).commit();
		Intent iUpdateSummaryPage = new Intent(Constants.ACTION_SUMMARY_FRAGMENT);
		iUpdateSummaryPage.putExtra(Constants.KEY_SYMBOL, cStockObj.getStockObj().getSymbol());
		iUpdateSummaryPage.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
		iUpdateSummaryPage.putExtra(Constants.KEY_IS_UPDATE_CHART, false);
		sendBroadcast(iUpdateSummaryPage);
		
		Intent iUpdateNewsPage = new Intent(Constants.ACTION_NEWS_LIST_FRAGMENT);
		iUpdateNewsPage.putExtra(Constants.KEY_SYMBOL, cStockObj.getStockObj().getSymbol());
		iUpdateNewsPage.putExtra(Constants.KEY_COMPANY_NAME, cStockObj.getStockObj().getName());
		sendBroadcast(iUpdateNewsPage);
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY);
		filter.addAction(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);
	}

	private void refreshQuotes(String mSymbol) {

		String _symbol = mSymbol;
		String lastTradePrice = "";
		String change = "";
		String changeInPercent = "";
		String changes = "";

		Spanned spannedChanges;

		QuoteObject quote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(_symbol);
		if (quote != null) {
			lastTradePrice = quote.getLastTradePrice();
			change = quote.getChange();
			changeInPercent = quote.getChangeInPercent();
			changes = change + "(" + changeInPercent + ")";

			if (change.contains("-")) 
				spannedChanges = TextColorPicker.getRedText("", changes);
			else 
				spannedChanges = TextColorPicker.getGreenText("", changes);

			tvLastTradePrice.setText(lastTradePrice);
			tvChanges.setText(spannedChanges);
		}
	}

	private void tabPageIndicatorSetup() {

		arrIndicator = res.getStringArray(R.array.indicator_portfolio_page);

		FragmentStatePagerAdapter adapter = new MyPagerAdapter(fm, fragments, arrIndicator);
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		
		if (bundle != null) {
			int tabPos = bundle.getInt(Constants.KEY_TAB_POSITION);
			if (tabPos > 0 && tabPos < fragments.size())
				indicator.setCurrentItem(tabPos);
		}
	}

	private class MyPagerAdapter extends FragmentStatePagerAdapter {

		private List<Fragment> mFragments;
		private String[] indicators;

		public MyPagerAdapter(FragmentManager fm, List<Fragment> mFms, String[] mArrIndicator) {
			super(fm);
			mFragments = mFms;
			indicators = mArrIndicator;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return indicators[position];
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.summary_menu, menu);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			refreshMenuItem = (MenuItem) menu.findItem(R.id.menu_refresh_stock);
		else
			refreshMenuItemImpl = (MenuItemImpl) menu.findItem(R.id.menu_refresh_stock);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int mItemId = item.getItemId();

		switch (mItemId) {
		case android.R.id.home:
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			return true;

		case R.id.menu_stocks_add_item:
			addItemDialog();
			return true;

		case R.id.menu_refresh_stock:
			boolean hasConn = NetworkConnectivity.hasNetworkConnection(this);
			if (hasConn) {
				getIntent().putExtra(Constants.KEY_IS_SINGLE_QUOTE_DONE, false);// reset
				getIntent().putExtra(Constants.KEY_IS_SINGLE_CHART_DONE, false);//
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
					refreshMenuItem.setActionView(abprogress);
				else
					refreshMenuItemImpl.setActionView(abprogress);
				updateQuoteAndChartFromYahoo();
			} else {
				Toast.makeText(this, res.getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	protected void addItemDialog() {
		String title = res.getString(R.string.add_new_item);
		String[] itemsArr = new String[4];		
		String[] arrFromRes = res.getStringArray(R.array.add_new_item_stocks_activity);
		String addTo = res.getString(R.string.add_new_item_7);
		
		for (int i=0; i<3; i++) {
			itemsArr[i] = arrFromRes[i];
		}
		
		if (cStockObj.getStockCategory() != CategoryStock.WATCHLIST) {
			itemsArr[3] = String.format(addTo, cStockObj.getStockObj().getSymbol());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(itemsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	
            	switch (which) {
            	case 0:
            		if (DbAdapter.getSingleInstance().fetchPortfolioList().isEmpty())
            			noPortOrWatchlistAlert(true);
            		else
            			startAddStockTxn();
            		break;
            	case 1:
            		setPriceAlert();    
            		break;
            	case 2:
            		setNewsAlert();
            		break;
            	case 3:
            		List<WatchlistObject> watchlist = DbAdapter.getSingleInstance().fetchWatchlists();
					if (watchlist.isEmpty())
						noPortOrWatchlistAlert(false);
					else if (watchlist.size() == 1) {
						int onlyOneWatchId = watchlist.get(0).getId();
						addDirectlyToWatch(cStockObj.getStockObj(), onlyOneWatchId);
					} else
						addToWatchList(watchlist, cStockObj.getStockObj());
					break;
            	}

            }
		});
		builder.setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
			TypedValue typedValue = new TypedValue();
			getTheme().resolveAttribute(R.attr.dialog_title_color, typedValue, true);
			
			int titleId = res.getIdentifier("alertTitle", "id", getPackageName());
			TextView dialogTitle = (TextView) aDialog.findViewById(titleId);
			dialogTitle.setTextColor(typedValue.data); 
			
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void noPortOrWatchlistAlert(final boolean isPort) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		dialog.setIcon(typedValue.resourceId);
		if (isPort) {
			dialog.setTitle(getString(R.string.no_portfolio_added));
			dialog.setMessage(getString(R.string.no_portfoli_dialog_msg));
		} else {
			dialog.setTitle(getString(R.string.no_watchlist_added));
			dialog.setMessage(getString(R.string.no_watchlist_dialog_msg));
		}

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				if (isPort) {
					AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog();     
	        		portDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
				} else {
					AddNewWatchlistDialog watchDialog = new AddNewWatchlistDialog();
					watchDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_WATCHLIST);
				}
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		AlertDialog aDialog = dialog.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected void addDirectlyToWatch(final StockObject stockObject, int watchId) {

		int stockId = 0;
		
		String symbol = stockObject.getSymbol();
		String name = stockObject.getName();
		String exch = stockObject.getExch();
		String type = stockObject.getType();
		String typeDisp = stockObject.getTypeDisp();
		String exchDisp = stockObject.getExchDisp();

		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);

		if (so != null) {
			stockId = so.getId();
		} else {
			so = new StockObject();
			so.setSymbol(symbol);
			so.setName(name);
			so.setExch(exch);
			so.setType(type);
			so.setTypeDisp(typeDisp);
			so.setExchDisp(exchDisp);
			so.insert();
			so = new StockObject();
			so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
			stockId = so.getId();
		}

		WatchlistStockObject ws = DbAdapter.getSingleInstance().fetchWatchlistStockObjectByStodckIdAndPortId(watchId, stockId);
		if (ws == null) {

			ws = new WatchlistStockObject();
			ws.setWatchId(watchId);
			ws.setStockId(stockId);

			ws.insert();
			
			// 4. add news alert
			MyAlarmManager alarmManager = new MyAlarmManager(this);
			alarmManager.addNewsAlert(symbol);

			// sendBroadcast when only WatchlistStockObject did
			// insert.
			Toast.makeText(this, res.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
			myNotifyDatasetChange(true, symbol);
			
			alarmManager.addNewsAlert(symbol);
		}
	}
	
	
	protected void addToWatchList(List<WatchlistObject> list, final StockObject stockObj) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(res.getString(R.string.title_add_to));		

		final List<String> wList = new ArrayList<String>();

		for (WatchlistObject po : list) {
			wList.add(po.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wList);
		builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				int watchId;
				int stockId;

				String watchlistName = wList.get(which);

				// 1. get portId;
				WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByName(watchlistName);
				watchId = wo.getId();

				String symbol = stockObj.getSymbol();
				String name = stockObj.getName();
				String exch = stockObj.getExch();
				String type = stockObj.getType();
				String typeDisp = stockObj.getTypeDisp();
				String exchDisp = stockObj.getExchDisp();

				// 2. to get id by symbol;
				// 2.1 if symbol no exist insert one, else get id by symbol;
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);

				if (so != null) {
					stockId = so.getId();
				} else {
					so = new StockObject();
					so.setSymbol(symbol);
					so.setName(name);
					so.setExch(exch);
					so.setType(type);
					so.setTypeDisp(typeDisp);
					so.setExchDisp(exchDisp);
					so.insert();
					so = new StockObject();
					so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
					stockId = so.getId();
				}

				// 3. fetch WatchlistStock Object - if exist to toast added
				// msg,
				// else insert WatchlistStock and then toast added msg
				WatchlistStockObject wl = DbAdapter.getSingleInstance().fetchWatchlistStockObjectByStodckIdAndPortId(watchId, stockId);
				if (wl == null) {

					wl = new WatchlistStockObject();
					wl.setWatchId(watchId);
					wl.setStockId(stockId);
					wl.insert();
					myNotifyDatasetChange(false, symbol);
					
					Toast.makeText(TransactionDetailsFragmentActivity.this, 
							res.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
				}
				
				// 4. add news alert
				MyAlarmManager alarmManager = new MyAlarmManager(TransactionDetailsFragmentActivity.this);
				alarmManager.addNewsAlert(symbol);			
			}
		});
		builder.setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// user create a new watchlist name
				//createWatchlistDialog(strCreateANewPortfolio, symbolCallBackObject);	
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	protected void myNotifyDatasetChange(boolean isUpdatePorfolioList, String symbol) {		

		Intent i = null;

		if (!isUpdatePorfolioList) {
			i = new Intent(Constants.ACTION_WATCHLIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		} else {
			i = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		}
		sendBroadcast(i);
	}
	
	
	private void startAddStockTxn() {
		Intent i = new Intent(TransactionDetailsFragmentActivity.this, AddNewTrade.class);		
		i.putExtra(Constants.KEY_FROM, fromId);
		
		switch (fromId) {

		case Constants.FROM_FIND_STOCKS:
			i.putExtras(bundle);
			break;
		case Constants.FROM_PORTFOLIO_LIST:
			i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
			i.putExtra(Constants.KEY_STOCK_ID, cStockObj.getStockObj().getId());
			break;
		case Constants.FROM_WATCH_LIST:
			i.putExtra(Constants.KEY_STOCK_ID, cStockObj.getStockObj().getId());
			break;
		}
			
		startActivity(i);
	}
	
	private void setPriceAlert() {

		String mSymbol = cStockObj.getStockObj().getSymbol();
		StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);

		if (mSo == null) {
			cStockObj.getStockObj().insert();
		}
		Intent i = new Intent(TransactionDetailsFragmentActivity.this, AddNewAlert.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		AlertObject mAo = DbAdapter.getSingleInstance().fetchAlertObjectBySymbol(mSymbol);
		if (mAo != null) {
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, true);
		} else {
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		}
		startActivity(i);
	}


	private void setNewsAlert() {

		String mSymbol = cStockObj.getStockObj().getSymbol();
		StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);

		if (mSo == null) {
			cStockObj.getStockObj().insert();
		}
		Intent i = new Intent(TransactionDetailsFragmentActivity.this, AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_NEWS_ALERT, true);
		i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		NewsAlertObject mAo = DbAdapter.getSingleInstance().fetchNewsAlertObjectBySymbol(mSymbol);
		if (mAo != null) {
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, true);
		} else {
			i.putExtra(Constants.KEY_SYMBOL, mSymbol);
			i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		}
		startActivity(i);
	}

	
	public class MyComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			return lhs.compareToIgnoreCase(rhs);
		}
	}
	
	public class MyStockComparator implements Comparator<CategoryStock> {
		@Override
		public int compare(CategoryStock lhs, CategoryStock rhs) {
			return lhs.getStockObj().getName().compareToIgnoreCase(rhs.getStockObj().getName());
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			String action = intent.getAction();
			Bundle iBundle = intent.getExtras();

			if (action.equals(Constants.ACTION_TRANSACTION_FRAGMENT_ACTIVITY) && iBundle != null) {

				String iSymbol = iBundle.getString(Constants.KEY_SYMBOL);
				//boolean isSingleChartDone = iBundle.getBoolean(Constants.KEY_IS_SINGLE_CHART_DONE, false);
				boolean isSingleQuoteDone = iBundle.getBoolean(Constants.KEY_IS_SINGLE_QUOTE_DONE, false);
				boolean isQuoteOk = iBundle.getBoolean(Constants.KEY_IS_QUOTE_OK, true);

				if (iSymbol != null && iSymbol.length() > 0) {
					String mSymbol = "";
					if (cStockObj.getStockObj() != null) {
						mSymbol = cStockObj.getStockObj().getSymbol();

						if (iSymbol.equals(mSymbol) && isSingleQuoteDone == true) {
							refreshQuotes(mSymbol);
							getIntent().putExtra(Constants.KEY_IS_SINGLE_QUOTE_DONE, true);
							getIntent().putExtra(Constants.KEY_IS_SINGLE_CHART_DONE, true);
						}
						
						// Server cann't connect:
						if (iSymbol.equals(mSymbol) && !isQuoteOk) {

							// 1. update progress bar.
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
								refreshMenuItem.setActionView(null);
							else
								refreshMenuItemImpl.setActionView(null);
							getIntent().putExtra(Constants.KEY_IS_SINGLE_QUOTE_DONE, false);
							getIntent().putExtra(Constants.KEY_IS_SINGLE_CHART_DONE, false);

							// 2. Prompt server may be updating:
							boolean isServerDown = sp.getBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false);
							if (isServerDown) {
								alertDialog();
							}
						}
					}

					// final to update ProgressBar:
					if (iSymbol.equals(mSymbol)) {
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
							refreshMenuItem.setActionView(null);
						else
							refreshMenuItemImpl.setActionView(null);
					}
				}
			} else if (action.equals(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT) && iBundle != null) {
				refreshAdapter();
			}
		}
	}
	
	private void refreshAdapter() {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mList.clear();
		mListString.clear();
		
		// Get PortfolioStockObject list
		List<PortfolioStockObject> mListPSO = DbAdapter.getSingleInstance().fetchPortStockList();
		for (PortfolioStockObject pso : mListPSO) {
			StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectByStockId(pso.getStockId());
			CategoryStock cStockObj = new CategoryStock();
			cStockObj.setStockObj(stockObj);
			cStockObj.setStockCategory(CategoryStock.PORTFOLIO);
			mList.add(cStockObj); 
		}
		
		// Get WatchlistStockObject list
		List<WatchlistStockObject> mListWSO = DbAdapter.getSingleInstance().fetchWatchStockList();
		for (WatchlistStockObject wso : mListWSO) {
			StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectByStockId(wso.getStockId());
			CategoryStock cStockObj = new CategoryStock();
			cStockObj.setStockObj(stockObj);
			cStockObj.setStockCategory(CategoryStock.WATCHLIST);
			mList.add(cStockObj); 
		}
		
		for (int i=0; i<mList.size(); i++) {
			mListString.add(mList.get(i).getStockObj().getName());
		}
		
		// Remove duplicates
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(mListString);
		mListString.clear();
		mListString.addAll(hs);
		
		Set<CategoryStock> s = new TreeSet<CategoryStock>(new Comparator<CategoryStock>()
        {
            public int compare(CategoryStock o1, CategoryStock o2)
            {
            	return o1.getStockObj().getName().compareToIgnoreCase(o2.getStockObj().getName());
            } 
        });
        s.addAll(mList);
        mList.clear();
        mList.addAll(s);
		
		Collections.sort(mListString, new MyComparator());
		Collections.sort(mList, new MyStockComparator());
		
		if (fromId == Constants.FROM_FIND_STOCKS) {
			Bundle mBundle = bundle;
			String mSymbol = mBundle.getString(Constants.KEY_SYMBOL);
			String mCompanyName = mBundle.getString(Constants.KEY_COMPANY_NAME);
			String mExch = mBundle.getString(Constants.KEY_EXCH);
			String mType = mBundle.getString(Constants.KEY_TYPE);
			String mTypeDisp = mBundle.getString(Constants.KEY_TYPE_DISP);
			String mExchDisp = mBundle.getString(Constants.KEY_EXCH_DISP);
			StockObject tempSO = new StockObject();
			tempSO.setSymbol(mSymbol);
			tempSO.setName(mCompanyName);
			tempSO.setExch(mExch);
			tempSO.setExchDisp(mExchDisp);
			tempSO.setType(mType);
			tempSO.setTypeDisp(mTypeDisp);
			CategoryStock cStockObj = new CategoryStock();
			cStockObj.setStockCategory(CategoryStock.FIND_STOCKS);
			cStockObj.setStockObj(tempSO);
			
			mList.add(0, cStockObj);
			mListString.add(0, tempSO.getName());
		} else {
			mCurrentIndex = mListString.indexOf(cStockObj.getStockObj().getName());
			actionBar.setSelectedNavigationItem(mCurrentIndex);
		}
		
		mSpinnerAdapter.notifyDataSetChanged();
	}

	private void alertDialog() {

		// reset
		sp.edit().putBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false).commit();

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(R.drawable.ic_alerts_warning);
		dialog.setTitle(res.getString(R.string.warning));
		dialog.setMessage(res.getString(R.string.domain_server_updating));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		AlertDialog aDialog = dialog.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
