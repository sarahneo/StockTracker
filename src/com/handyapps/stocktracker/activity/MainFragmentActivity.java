package com.handyapps.stocktracker.activity;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.DrawerLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.MyBannerAd;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.adapter.MyNavDrawerAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddCashPosDialog;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog.AddNewPortfolioDialogListener;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog.AddNewWatchlistDialogListener;
import com.handyapps.stocktracker.fragments.AlertsMainFragment;
import com.handyapps.stocktracker.fragments.DistributionFragment;
import com.handyapps.stocktracker.fragments.FindStocksMainFragmentWithResultCode;
import com.handyapps.stocktracker.fragments.NewsPortFragment;
import com.handyapps.stocktracker.fragments.PerformanceFragment;
import com.handyapps.stocktracker.fragments.PositionsFragment;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.rater.AppRater;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateQuoteTaskAllSymbol;
import com.handyapps.stocktracker.utils.MyInputMethodManager;
import com.handyapps.stocktracker.utils.NetworkConnectivity;
import com.handyapps.stocktracker.utils.ThemeUtils;
import com.viewpagerindicator.TabPageIndicator;

public class MainFragmentActivity extends Activity implements OnItemClickListener, OnQueryTextListener, OnPageChangeListener,
		OnSharedPreferenceChangeListener, AddNewPortfolioDialogListener, AddNewWatchlistDialogListener {

	private FragmentManager fm = null;
	private TabPageIndicator indicator;
	private MyPagerAdapter adapter;
	private DrawerLayout mDrawerLayout;
	private LinearLayout frame;
	private ActionBarDrawerToggle mDrawerToggle;
	private ArrayAdapter<String> mSpinnerAdapter;
	private ListView mDrawerList;
	private String allPortfolios;
	private int mPortId;
	private int mCurrentIndex;
	private int countSelectionPort = 0;
	private float lastTranslate = 0.0f;
	private List<PortfolioObject> mList;
	private List<String> mListString;
	private List<String> lsIndicators;
	private List<Fragment> fragments;
	private String[] mDrawerItems;
	private ViewPager pager;
	private Resources resources;
	private SharedPreferences sp;
	private MyMainActivityBroadcastReceiver receiver;
	private ActionBar actionBar;
	private MenuItemImpl refreshMenuItemImpl;
	private MenuItem refreshMenuItem;
	private View abprogress;
	private LayoutInflater inflater;
	private UpdateQuoteTaskAllSymbol quoteTask = null; 

	//public static final int TAB_POSITION_FAKE_FIRST = 0;
	public static final int TAB_POSITION_PORTFOLIO = 0;
	public static final int TAB_POSITION_ALERT = 1;
	public static final int TAB_POSITION_NEWS = 2;
	public static final int TAB_POSITION_FIND_STOCK = 3;
	public static final int TAB_POSITION_POSITIONS = 4;
	public static final int TAB_POSITION_PERF = 5;
	public static final int TAB_POSITION_DIST = 6;
	public static final int TAB_POSITION_WATCHLIST = 7;
	//public static final int TAB_POSITION_FAKE_LAST = 5;

	private InterstitialAd interstitialAd;
	private String interstitial_AD_ID = "a15212d233a9831";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resources = getResources();
		
		ThemeUtils.onActivityCreateSetTheme(this, false);
		
		actionBar = getSupportActionBar();		
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);

		setContentView(R.layout.main_fragment_activity);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		abprogress = inflater.inflate(R.layout.progress_dialog_holo, null);
		
		MyBannerAd ad = new MyBannerAd(findViewById(android.R.id.content), getApplicationContext());
		ad.loadAd();
		
		allPortfolios = resources.getString(R.string.all_portfolios);
		
		frame = (LinearLayout) findViewById(R.id.ll_container);
		
		mDrawerItems = getResources().getStringArray(R.array.nav_drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setAdapter(new MyNavDrawerAdapter(this,
                R.layout.nav_drawer_list_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		mDrawerToggle = new ActionBarDrawerToggle(
                this,                   //host Activity 
                mDrawerLayout,          //DrawerLayout object 
                R.drawable.ic_drawer,   //nav drawer image to replace 'Up' caret 
                R.string.drawer_open,   //"open drawer" description for accessibility 
                R.string.drawer_close   //"close drawer" description for accessibility 
                ) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
            	supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float moveFactor = (mDrawerList.getWidth() * slideOffset);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    frame.setTranslationX(moveFactor);
                } else {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    frame.startAnimation(anim);

                    lastTranslate = moveFactor;
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);     
                
        mList = DbAdapter.getSingleInstance().fetchPortfolioList();
        isFirstRun();
		mListString = new ArrayList<String>();
		mCurrentIndex = sp.getInt(Constants.SP_KEY_PORTFOLIO_ARRAY_INDEX, 0);
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		
		mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, mListString);
		mSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				if (countSelectionPort >= 1) {
					mCurrentIndex = position;
					if (mListString.get(position).equals(getResources().getString(R.string.no_portfolio_added)))
						;// do nothing
					else if (mListString.get(position).equals(allPortfolios)) {
						mPortId = -1;
						sendBroadcastFilterPort();
					} else {
						mPortId = mList.get(position).getId();
						sendBroadcastFilterPort();
					}
				}
				
				countSelectionPort++;
				return true;
			}
		};
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);		
		actionBar.setSelectedNavigationItem(mCurrentIndex);

		fm = getSupportFragmentManager();
		fragments = new ArrayList<Fragment>();
		//fragments.add(FakeFirstFragment.newInstance()); // fake fragment
		fragments.add(AlertsMainFragment.newInstance());
		fragments.add(NewsPortFragment.newInstance());
		fragments.add(FindStocksMainFragmentWithResultCode.newInstance());
		fragments.add(PositionsFragment.newInstance());
		fragments.add(PerformanceFragment.newInstance());
		fragments.add(DistributionFragment.newInstance());
		//fragments.add(FakeLastFragment.newInstance()); // fake fragment

		lsIndicators = getIndicators();

		pager = (ViewPager) findViewById(R.id.pager);
		//adapter = new CircularPagerAdapter(lsIndicators, fm, pager, fragments);
		
		adapter = new MyPagerAdapter(fm, fragments, lsIndicators);
		pager.setAdapter(adapter);
		

		indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(this);

		setTabPosition();

		isAppFirstAlarmDone();

		// Create an ad.
		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdUnitId(interstitial_AD_ID);

		
		AdRequest adRequest = new AdRequest.Builder()
		.addTestDevice(getString(R.string.admob_test_device_id))
		.build();
		interstitialAd.loadAd(adRequest);

		// Set the AdListener.
		interstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				finish();
			}
		});

		AppRater.app_launched(this, Constants.isPro() ? AppRater.VERSION.VERSION_PRO : AppRater.VERSION.VERSION_LITE);
	}

	private void isAppFirstAlarmDone() {
		boolean isMyAppFirstAlarmDone = sp.getBoolean(Constants.SP_KEY_IS_APP_FIRST_ALARM_DONE, false);
		if (!isMyAppFirstAlarmDone) {
			MyAlarmManager mAlarm = new MyAlarmManager(MainFragmentActivity.this);
			mAlarm.setAlarm();
		}
	}
	
	private void isFirstRun() {
		boolean isFirstRun = sp.getBoolean(Constants.SP_KEY_IS_FIRST_RUN, true);
		if (isFirstRun) {
			// Create default portfolio and watchlist
			if (mList.isEmpty()) {
				PortfolioObject po = new PortfolioObject();
				po.setName(resources.getString(R.string.default_port_title));
				po.setCurrencyType(PortfolioObject.USD);
				po.insert();
				int portId = DbAdapter.getSingleInstance().fetchPortfolioByName(resources.getString(R.string.default_port_title)).getId();
				sp.edit().putInt(Constants.SP_KEY_PORTFOLIO_ID, portId).commit();
			}
			if (DbAdapter.getSingleInstance().fetchWatchlists().isEmpty()) {
				WatchlistObject wo = new WatchlistObject();
				wo.setName(resources.getString(R.string.default_watchlist_title));
				wo.insert();
			}	
		}
		sp.edit().putBoolean(Constants.SP_KEY_IS_FIRST_RUN, false).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerBroadcast();
		refreshIndicator();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
    		unregisterReceiver(receiver);
    	} catch (IllegalArgumentException e) {
    	}
	}
	
	private void refreshAdapter() {
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mList.clear();
		mList = DbAdapter.getSingleInstance().fetchPortfolioList();
		mListString.clear();
		for (int i=0; i<mList.size(); i++) {
			mListString.add(mList.get(i).getName());
		}
		mListString.add(allPortfolios);
		
		mSpinnerAdapter.notifyDataSetChanged();
		boolean isFound = false;
		for (int j=0; j<mList.size(); j++) {
			if (mPortId == mList.get(j).getId()) {
				mCurrentIndex = j;
				isFound = true;
			}
		}
		if (!isFound) {
			mCurrentIndex = mListString.size() - 1;
			mPortId = -1;
		}
		actionBar.setSelectedNavigationItem(mCurrentIndex);
		sendBroadcastFilterPort();
	}

	private void setTabPosition() {

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			int tabPos = bundle.getInt(Constants.KEY_TAB_POSITION, 2);
			indicator.setCurrentItem(tabPos);
		} else {
			String sTabPos = sp.getString(Constants.SP_KEY_DEFAULT_STARTING_PAGE, "2");
			int tabPos = Integer.parseInt(sTabPos);
			indicator.setCurrentItem(tabPos);
		}
	}

	private void refreshIndicator() {
		lsIndicators = getIndicators();
		adapter.setIndicators(lsIndicators);
		indicator.notifyDataSetChanged();
		refreshAdapter();
	}

	private List<String> getIndicators() {

		String[] arrIndicator = resources.getStringArray(R.array.indicator_main_page);
		List<String> ls = new ArrayList<String>();
		
		//ls.add(arrIndicator[0]);

		for (int i = 1; i < arrIndicator.length+1; i++) {
			
			/*if (i == TAB_POSITION_FAKE_FIRST) {
				ls.add(arrIndicator[0]);
			}

			if (i == TAB_POSITION_PORTFOLIO) {
				long count = PortfolioManager.getPortfolioCount();
				String sCount = String.valueOf(count);
				ls.add(arrIndicator[0] + "(" + sCount + ")");
			}*/
			
			if (i == TAB_POSITION_ALERT) {
				ls.add(arrIndicator[0]);
			}
			
			if (i == TAB_POSITION_NEWS) {
				ls.add(arrIndicator[1]);
			}
			
			if (i == TAB_POSITION_FIND_STOCK) {
				ls.add(arrIndicator[2]);
			}
			
			if (i == TAB_POSITION_POSITIONS) {
				ls.add(arrIndicator[3]);
			}
			
			if (i == TAB_POSITION_PERF) {
				ls.add(arrIndicator[4]);
			}
			
			if (i == TAB_POSITION_DIST) {
				ls.add(arrIndicator[5]);
			}
						
			/*if (i == TAB_POSITION_WATCHLIST) {
				long count = WatchlistManager.getWatchlistCount();
				String sCount = String.valueOf(count);
				ls.add(arrIndicator[8] + "(" + sCount + ")");
			}
			
			if (i == TAB_POSITION_FAKE_LAST) {
				ls.add(arrIndicator[5]);
			}*/
		}
		
		//ls.add(arrIndicator[arrIndicator.length-1]);

		return ls;
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {

		private List<String> indicators;
		private List<Fragment> mFragments;
		public MyPagerAdapter(FragmentManager fm, List<Fragment> mFms, List<String> mArrIndicator) {
			super(fm);
			mFragments = mFms;
			indicators = mArrIndicator;
		}

		public void clear() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return indicators.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		public void setIndicators(List<String> indicators) {
			this.indicators = indicators;
			notifyDataSetChanged();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			refreshMenuItem = (MenuItem) menu.findItem(R.id.menu_refresh);
		else
			refreshMenuItemImpl = (MenuItemImpl) menu.findItem(R.id.menu_refresh);
		
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (item.getItemId() == android.R.id.home) {

	        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
	        	mDrawerLayout.closeDrawer(mDrawerList);
	        } else {
	        	mDrawerLayout.openDrawer(mDrawerList);
	        }
	    }
		
		switch (item.getItemId()) {
		case R.id.menu_add_item:
			addNewItemDialog();
			break;
		case R.id.menu_refresh:
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
				Toast.makeText(this, resources.getString(R.string.no_network_connection), Toast.LENGTH_LONG).show();
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	private void updateQuoteAndChartFromYahoo() {
		
		List<StockObject> listStock = DbAdapter.getSingleInstance().fetchStockObjectList();
		String[] arrSymbols = new String[listStock.size()];
		for (int s=0; s<listStock.size(); s++) {
			arrSymbols[s] = listStock.get(s).getSymbol();
		}

		if (arrSymbols.length > 0) {
			if (quoteTask == null) {
				quoteTask = new UpdateQuoteTaskAllSymbol(this);
				quoteTask.execute(arrSymbols);
			} else {
				quoteTask.cancel(true);
				quoteTask = null;
				quoteTask = new UpdateQuoteTaskAllSymbol(this);
				quoteTask.execute(arrSymbols);
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
				refreshMenuItem.setActionView(null);
			else
				refreshMenuItemImpl.setActionView(null);
		}
	}
	
	protected void addNewItemDialog() {
		String title = resources.getString(R.string.add_new_item);
		String[] itemsArr = resources.getStringArray(R.array.add_new_item);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(itemsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	
            	switch (which) {
            	case 0:
            		AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog();     
            		portDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
            		break;
            	case 1:
            		AddNewWatchlistDialog watchDialog = new AddNewWatchlistDialog();     
            		watchDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_WATCHLIST);
            		break;
            	case 2:
            		if (DbAdapter.getSingleInstance().fetchPortfolioList().isEmpty())
            			noPortAlert();
            		else
            			startAddStockTxn();
            		break;
            	case 3:
            		if (DbAdapter.getSingleInstance().fetchPortfolioList().isEmpty())
            			noPortAlert();
            		else {
	            		AddCashPosDialog addCashDialog = new AddCashPosDialog();                 		
	            		addCashDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_CASH_TXN_S);
            		}
            		break;
            	}

            }
		});
		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startAddStockTxn() {
		Intent i = new Intent(MainFragmentActivity.this, AddNewTrade.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_POSITIONS_TAB);
		if (mPortId != -1)
			i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
		startActivity(i);
	}
	
	private void noPortAlert() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setIcon(R.drawable.ic_action_warning);
		dialog.setTitle(getString(R.string.no_portfolio_added));
		dialog.setMessage(getString(R.string.no_portfoli_dialog_msg));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog();     
        		portDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
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
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendBroadcastFilterPort() {
		sp.edit().putInt(Constants.SP_KEY_PORTFOLIO_ID, mPortId).commit();
		sp.edit().putInt(Constants.SP_KEY_PORTFOLIO_ARRAY_INDEX, mCurrentIndex).commit();
		Intent i = new Intent(Constants.ACTION_FILTER_PORTFOLIO);
		i.putExtra(Constants.KEY_FILTER_PORTFOLIOS_PAGER, true);
		i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, mPortId);
		sendBroadcast(i);
	}
	
	private void sendBroadastUpdatePagerIndicator(boolean isUpdateWL) {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		if (isUpdateWL)
			i.putExtra(Constants.KEY_UPDATE_WATCHLISTS_PAGER, true);
		i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, mPortId);
		sendBroadcast(i);
	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_MAIN_ACTIVITY);
		filter.addAction(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		receiver = new MyMainActivityBroadcastReceiver();
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Save to shared preferences
    	sp.edit().putInt(Constants.SP_KEY_PORTFOLIO_ARRAY_INDEX, mCurrentIndex).commit();
    	sp.edit().putInt(Constants.SP_KEY_PORTFOLIO_ID, mPortId).commit();
    	
		if (quoteTask != null) {
			quoteTask.cancel(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		
		/*if (position == fragments.size()-1){
			pager.setCurrentItem(1,false);
		}
		if(position==0){
			pager.setCurrentItem(fragments.size()-2,false);
		}*/
		
		//if (position > 0) {
			MyInputMethodManager.hideKeyboard(MainFragmentActivity.this, indicator);
		//} else {
		//	MyInputMethodManager.showKeyboard(MainFragmentActivity.this);
		//}

	}

	private class MyMainActivityBroadcastReceiver extends BroadcastReceiver {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.ACTION_MAIN_ACTIVITY) ||
					intent.getAction().equals(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT)) {
				
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					boolean isChangeTab = bundle.getBoolean(Constants.KEY_IS_CHANGE_TAB);
					mPortId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID, mPortId);
					
					int index = -1;
					for (int i=0; i<mList.size(); i++) {
						if (mList.get(i).getId() == mPortId)
							index = i;
					}
					if (index != -1)
						mCurrentIndex = index;
					else
						mCurrentIndex = sp.getInt(Constants.SP_KEY_PORTFOLIO_ARRAY_INDEX, 0);
					
					Log.i("index", "mCurrentIndex="+mCurrentIndex);
					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					actionBar.setSelectedNavigationItem(mCurrentIndex);
					
					if (isChangeTab) {
						int currentTab = bundle.getInt(Constants.KEY_CHANGE_TAB_TO, 0);
						indicator.setCurrentItem(currentTab);
						return;
					}
						
				}
				refreshIndicator();
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					if (refreshMenuItem != null)
						refreshMenuItem.setActionView(null);
				} else {
					if (refreshMenuItemImpl != null)
						refreshMenuItemImpl.setActionView(null);
				}
				
			}
		}
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		boolean isSettingOnCreating = sp.getBoolean(Constants.SP_KEY_IS_SETTING_ON_CREATE, false);

		if (!isSettingOnCreating) {
			if (key.equals(Constants.SP_KEY_DEFAULT_STARTING_PAGE)) {

				String sTabPos = sp.getString(Constants.SP_KEY_DEFAULT_STARTING_PAGE, "1");
				int tabPos = Integer.parseInt(sTabPos);
				indicator.setCurrentItem(tabPos);

				Bundle bundle = getIntent().getExtras();// ensure that
														// orientation
														// changed before the
														// bundle to be updated.
				if (bundle != null) {
					getIntent().putExtra(Constants.KEY_TAB_POSITION, tabPos);
				}
			} else if (key.equals(Constants.SP_KEY_APP_THEME)) {
				String appTheme = sp.getString(Constants.SP_KEY_APP_THEME, resources.getString(R.string.light_theme));
				if (appTheme.equals(resources.getString(R.string.light_theme))) {
					ThemeUtils.changeToTheme(this, ThemeUtils.LIGHT);			
				} else
					ThemeUtils.changeToTheme(this, ThemeUtils.DARK);
								
			}
		}
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.menu_add_item).setVisible(!drawerOpen);
        if (drawerOpen)
        	getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        else
        	getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        return super.onPrepareOptionsMenu(menu);
    }

	
	/* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(android.widget.AdapterView<?> parent, View view,
				int position, long id) {
			selectItem(position);
		}
    }

    private void selectItem(int position) {
  	
    	switch (position) {
    	case 0:
    		indicator.setCurrentItem(TAB_POSITION_FIND_STOCK-1);
    		break;
    	case 1:
    		indicator.setCurrentItem(TAB_POSITION_NEWS-1);	
    		break;
    	case 2:
    		indicator.setCurrentItem(TAB_POSITION_POSITIONS-1);	
    		break;
    	case 3:
    		indicator.setCurrentItem(TAB_POSITION_ALERT-1);
    		break;
    	case 4:
    		indicator.setCurrentItem(TAB_POSITION_PERF-1);
    		break;
    	case 5:
    		indicator.setCurrentItem(TAB_POSITION_DIST-1);	
    		break;
    	case 6:
    		Intent settingsIntent = new Intent(MainFragmentActivity.this, PrefsActivity.class);
    		startActivity(settingsIntent);
    		break;
    	}
      
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mFragmentTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    @Override
	public void onDialogPositiveClick(DialogFragment dialog) {
    	refreshAdapter();
    	if (dialog.getTag().equals(Constants.DIALOG_ADD_PORTFOLIO))
    		sendBroadastUpdatePagerIndicator(false);
    	else
    		sendBroadastUpdatePagerIndicator(true);
    	
    	indicator.setCurrentItem(MainFragmentActivity.TAB_POSITION_FIND_STOCK-1);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	@Override
	public void onBackPressed() {
		// AdsUtilIn.startAds(this);
		// super.onBackPressed();
		// TODO Auto-generated method stub
		if (interstitialAd.isLoaded()) {
			interstitialAd.show();
		}
		finish();
	}

	
}
