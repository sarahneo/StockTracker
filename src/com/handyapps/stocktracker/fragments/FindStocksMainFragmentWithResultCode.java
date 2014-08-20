package com.handyapps.stocktracker.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.Toast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewAlert;
import com.handyapps.stocktracker.activity.AddNewTrade;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.adapter.CustomAdapter;
import com.handyapps.stocktracker.adapter.FindStocksAdapter;
import com.handyapps.stocktracker.cards.PortfolioCard;
import com.handyapps.stocktracker.cards.WatchlistCard;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog;
import com.handyapps.stocktracker.dialogs.WrongCurrencyDialog;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.model.WatchlistObject;
import com.handyapps.stocktracker.model.WatchlistStockObject;
import com.handyapps.stocktracker.service.IntentServiceUpdateQuote;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.FindStocksCallbackTask;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.MyComparatorUtils;
import com.handyapps.stocktracker.utils.NetworkConnectivity;
import com.handyapps.stocktracker.widget.WidgetUpdateIntentService;

public final class FindStocksMainFragmentWithResultCode extends Fragment implements android.widget.AdapterView.OnItemClickListener,
		OnFocusChangeListener, OnClickListener {

	private boolean hasAsyncTaskExcuted = false;
	private boolean isHideCards = false;
	
	private String strViewStockDetails;
	private String strAlert;
	private String strViewLatestNews;
	private String strAddToWatchlist;
	private String strAddToPortfolio;
	
	private int mPortId;
	
	private RelativeLayout layoutResults;
	private TextView tvNetworkConnection;
	private EditText etSearchTerm;
	private ProgressBar pb;
	private CardListView mCardView;
	private List<Card> mCards;
	private CardArrayAdapter mCardArrayAdapter;
	private MyAlarmManager alarmManager;
	
	private ListView lv;
	private ArrayList<SymbolCallBackObject> ls;
	private FindStocksAdapter customAdapter;
	private FindStocksCallbackTask callBackTask;
	private Resources resources;
	private GridQuickAction mQuickAction;
	private SymbolCallBackObject scbo;
	private MainWatchBroadcastReceiver receiver;
	private SharedPreferences sp;
	private UpdateQuoteTaskSingleSymbol quoteTask = null; 
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resources = getActivity().getResources();
		//registerBroadcast();
		
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		strViewStockDetails = resources.getString(R.string.view_stock_details);
		strAlert = resources.getString(R.string.q_set_alert);
		strViewLatestNews = resources.getString(R.string.latest_news);
		strAddToWatchlist = resources.getString(R.string.add_to_watchlist);
		strAddToPortfolio = resources.getString(R.string.add_to_portfolio);
		
		Bundle bundle = this.getArguments();
		if (bundle != null)
			isHideCards = bundle.getBoolean(Constants.KEY_IS_HIDE_CARDS);
		else
			isHideCards = false;
		
	}

	public static FindStocksMainFragmentWithResultCode newInstance() {
		FindStocksMainFragmentWithResultCode fragment = new FindStocksMainFragmentWithResultCode();
		return fragment;
	}

	@Override
	public void onPause() {
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.find_stocks_fragment_layout, container, false);
		
		mCardView = (CardListView) view.findViewById(R.id.cardsview);
		//mCardView.setSwipeable(false);
		if (isHideCards)
			mCardView.setVisibility(View.GONE);
		
		mCards = new ArrayList<Card>();
		mCardArrayAdapter = new CardArrayAdapter(getActivity(), mCards);
		mCardView.setAdapter(mCardArrayAdapter);
		
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		updateCards(mPortId);
		
		layoutResults = (RelativeLayout) view.findViewById(R.id.list_layout);
		layoutResults.setVisibility(View.GONE);
		
		alarmManager = new MyAlarmManager(getActivity());

		etSearchTerm = (EditText) view.findViewById(R.id.et_search_term);	
		etSearchTerm.setOnFocusChangeListener(this);
		etSearchTerm.setOnClickListener(this);

		pb = (ProgressBar) view.findViewById(R.id.pb_loading);
		pb.setVisibility(View.GONE);

		tvNetworkConnection = (TextView) view.findViewById(R.id.tv_no_network_connection);
		tvNetworkConnection.setVisibility(View.GONE);

		addDeleteAllTextButton(getActivity());

		lv = (ListView) view.findViewById(R.id.lv_symbols_call_back);
		ls = new ArrayList<SymbolCallBackObject>();

		customAdapter = new FindStocksAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, ls);
		lv.setAdapter(customAdapter);
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setOnItemClickListener(myChildItemClickListener);
		
		buildQuickActionItem();

		return view;
	}

	private OnItemClickListener myChildItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			scbo = ls.get(position);
			Bundle bundle = getActivity().getIntent().getExtras();
			if (bundle != null) {
				int keyFrom = bundle.getInt(Constants.KEY_FROM);
				if (keyFrom == Constants.FROM_ADD_TRADE || keyFrom == Constants.FROM_ADD_ALERT_DIALOG || 
						keyFrom == Constants.FROM_ADD_NEWS_ALERT) {
					int fromId = bundle.getInt(Constants.KEY_FROM);
					boolean isFromPortfolio = bundle.getBoolean(Constants.KEY_ADD_STOCK_IN_SINGLE_PORTFOLIO);
					boolean isFromWatchlist = bundle.getBoolean(Constants.KEY_ADD_STOCK_IN_SINGLE_WATCHLIST);
					int portId = bundle.getInt(Constants.KEY_PORTFOLIO_ID);
					int watchId = bundle.getInt(Constants.KEY_WATCHLIST_ID);
					String portName = bundle.getString(Constants.KEY_PORTFOLIO_NAME);
					String watchName = bundle.getString(Constants.KEY_WATCHLIST_NAME);

					setActivityResult(scbo, fromId, isFromPortfolio, isFromWatchlist, portId, watchId, portName, watchName);
				} else if (keyFrom == Constants.FROM_WIDGET) 
					mQuickAction.show(view);
			} else
				mQuickAction.show(view);
		}
	};

	protected void setActivityResult(SymbolCallBackObject symbolCallBackObject, int fromId, boolean isFromPortfolio, boolean isFromWatchlist,
			int portId, int watchId, String portName, String watchName) {

		int stockId;
		String symbol = symbolCallBackObject.getSymbol();
		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
		if (so == null) {
			stockId = insertStockObjectAndGetStockId(symbolCallBackObject);
		} else {
			stockId = so.getId();
		}

		switch (fromId) {

		case Constants.FROM_PORTFOLIO_LIST:
			addStockToThePortfolio(stockId, portId, symbol, fromId);
			break;
		case Constants.FROM_WATCH_LIST:
			addStockToWatchlist(stockId, watchId, symbol, fromId);
			break;
		case Constants.FROM_ADD_ALERT_DIALOG:
			sendBackSymbolToAddAlertForResult(symbol);
			break;
		case Constants.FROM_ADD_NEWS_ALERT:
			sendBackSymbolToAddAlertWithoutQuoteForResult(symbol);
			break;
		case Constants.FROM_WIDGET:
			viewDetailsIntent(symbolCallBackObject);
			break;
		case Constants.FROM_FIND_STOCKS:
			viewDetailsIntent(symbolCallBackObject);
			break;
		case Constants.FROM_ADD_TRADE:
			sendBackSymbolToAddTradeForResult(symbolCallBackObject, symbol);
			break;
		}
	}

	private int insertStockObjectAndGetStockId(SymbolCallBackObject symbolCallBackObject) {

		String symbol = symbolCallBackObject.getSymbol();
		String companyName = symbolCallBackObject.getName();
		String exch = symbolCallBackObject.getExch();
		String type = symbolCallBackObject.getType();
		String typeDisp = symbolCallBackObject.getTypeDisp();
		String exchDisp = symbolCallBackObject.getExchDisp();

		StockObject so = new StockObject();
		so.setSymbol(symbol);
		so.setName(companyName);
		so.setExch(exch);
		so.setExchDisp(exchDisp);
		so.setType(type);
		so.setTypeDisp(typeDisp);
		so.insert();

		so = new StockObject();
		so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
		return so.getId();
	}

	private void sendBackSymbolToAddAlertForResult(String symbol) {

		Intent i = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, true);
		getActivity().sendBroadcast(i);
		getActivity().finish();
	}
	
	private void sendBackSymbolToAddAlertWithoutQuoteForResult(String symbol) {

		Intent i = new Intent(Constants.ACTION_ADD_ALERT_DIALOG_ACTIVITY);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, false);
		i.putExtra(Constants.KEY_IS_QUOTE_OK, false);
		getActivity().sendBroadcast(i);
		getActivity().finish();
	}
	
	private void sendBackSymbolToAddTradeForResult(SymbolCallBackObject symbolCallBackObject, String symbol) {
		
		Intent i = new Intent(Constants.ACTION_ADD_NEW_TRADE);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, symbolCallBackObject.getName());
		i.putExtra(Constants.KEY_EXCH, symbolCallBackObject.getExch());
		i.putExtra(Constants.KEY_EXCH_DISP, symbolCallBackObject.getExchDisp());
		i.putExtra(Constants.KEY_TYPE, symbolCallBackObject.getType());
		i.putExtra(Constants.KEY_TYPE_DISP, symbolCallBackObject.getTypeDisp());
		i.putExtra(Constants.KEY_IS_START_QUOTE_SERVICE, true);
		getActivity().setResult(Activity.RESULT_OK, i);
		getActivity().sendBroadcast(i);
		getActivity().finish();
	}

	private void addStockToWatchlist(int stockId, int watchId, String symbol2, int fromId) {

		String mSymbol = symbol2;

		// 1. insert stock to the watchlist
		WatchlistStockObject wl = DbAdapter.getSingleInstance().fetchWatchlistStockObjectByStodckIdAndPortId(watchId, stockId);

		if (wl == null) {

			wl = new WatchlistStockObject();
			wl.setWatchId(watchId);
			wl.setStockId(stockId);
			wl.insert();

			WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByWatchId(watchId);
			if (wo != null) {
				String watchName = wo.getName();
				Intent intent = new Intent(watchName);
				getActivity().sendBroadcast(intent);
			}
		}

		// 2. to show msg
		Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
		// 3. startQuote
		startQuoteIntentService(mSymbol);

		// 4. to update widget
		startWidgetUpdateIntentService();

		// 5. call activity finish()
		getActivity().finish();
	}

	private void addStockToThePortfolio(int stockId, int portId, String symbol2, int fromId) {
		String mSymbol = symbol2;
		int mFromId = fromId;

		// 1. insert stock to the portfolio
		PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portId, stockId);
		if (ps == null) {
			ps = new PortfolioStockObject();
			ps.setPortfolioId(portId);
			ps.setStockId(stockId);
			
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			String currency = exchToCurrency(scbo.getExch());
			List<PortfolioStockObject> pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
			if (pso != null) {
				
				if (currency.equals(po.getCurrencyType())) {
					ps.insert();
				} else {
					// return error
					Log.i("wrong currency", po.getCurrencyType()+":"+currency);
					WrongCurrencyDialog currDialog = new WrongCurrencyDialog();     
					currDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_WRONG_CURRENCY);
				}
			} else {
				ps.insert();
				po.setCurrencyType(currency);
				po.update();
			}
		}

		// 2. to show msg
		Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();

		// 3. startQuote
		startQuoteIntentService(mSymbol);

		// 4. add a new Trade
		addNewTradeIntent(portId, stockId, mSymbol, mFromId);

		// 5. to update widget
		startWidgetUpdateIntentService();

		// 6. call activity finish();
		getActivity().finish();
	}

	private void startWidgetUpdateIntentService() {
		Intent iUpdateWidget = new Intent(getActivity(), WidgetUpdateIntentService.class);
		getActivity().startService(iUpdateWidget);
	}

	private void startQuoteIntentService(String symbol) {
		String mSymbol = symbol;
		Intent i = new Intent(getActivity(), IntentServiceUpdateQuote.class);
		i.putExtra(Constants.KEY_SYMBOL, mSymbol);
		getActivity().startService(i);
	}

	private void addNewTradeIntent(int portId, int stockId, String symbol, int fromId) {

		Intent i = new Intent(getActivity(), AddNewTrade.class);
		i.putExtra(Constants.KEY_FROM, fromId);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		getActivity().startActivity(i);

	}

	private void viewDetailsIntent(SymbolCallBackObject symbolCallBackObject) {
		
		String symbol = symbolCallBackObject.getSymbol();
		String companyName = symbolCallBackObject.getName();
		String exch = symbolCallBackObject.getExch();
		String type = symbolCallBackObject.getType();
		String typeDisp = symbolCallBackObject.getTypeDisp();
		String exchDisp = symbolCallBackObject.getExchDisp();
		Intent i = new Intent(getActivity(), TransactionDetailsFragmentActivity.class);
		
		// Check if stock exists in any portfolio or watchlist
		List<PortfolioStockObject> poList = DbAdapter.getSingleInstance().fetchPortStockList();
		List<WatchlistStockObject> woList = DbAdapter.getSingleInstance().fetchWatchStockList();
		
		StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbolCallBackObject.getSymbol());
		boolean isPortWatchStock = false;
		
		if (stockObj != null) {
			for (PortfolioStockObject po : poList) {
				if (po.getStockId() == stockObj.getId())
					isPortWatchStock = true;
			}
			for (WatchlistStockObject wo : woList) {
				if (wo.getStockId() == stockObj.getId())
					isPortWatchStock = true;
			}
		}
		if (isPortWatchStock) {
			int stockId = stockObj.getId();
			sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
			i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
			i.putExtra(Constants.KEY_STOCK_ID, stockId);
		} else {	
			sp.edit().putInt(Constants.SP_KEY_STOCK_ID, -1).commit();		
			i.putExtra(Constants.KEY_FROM, Constants.FROM_FIND_STOCKS);
			getActivity().startActivity(i);
		}
		
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		i.putExtra(Constants.KEY_TAB_POSITION, Constants.TO_SUMMARY_FRAGMENT);
		getActivity().startActivity(i);
	}
	
	private void showAddAlert(SymbolCallBackObject symbolCallBackObject) {
		String symbol = symbolCallBackObject.getSymbol();

		Intent i = new Intent(getActivity().getApplicationContext(), AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		getActivity().startActivity(i);
	}
	
	private void viewNewsIntent(SymbolCallBackObject symbolCallBackObject) {
		
		String symbol = symbolCallBackObject.getSymbol();
		String companyName = symbolCallBackObject.getName();
		String exch = symbolCallBackObject.getExch();
		String type = symbolCallBackObject.getType();
		String typeDisp = symbolCallBackObject.getTypeDisp();
		String exchDisp = symbolCallBackObject.getExchDisp();
		Intent i = new Intent(getActivity(), TransactionDetailsFragmentActivity.class);
		
		// Check if stock exists in any portfolio or watchlist
		List<PortfolioStockObject> poList = DbAdapter.getSingleInstance().fetchPortStockList();
		List<WatchlistStockObject> woList = DbAdapter.getSingleInstance().fetchWatchStockList();
		
		StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbolCallBackObject.getSymbol());
		boolean isPortWatchStock = false;
		
		if (stockObj != null) {
			for (PortfolioStockObject po : poList) {
				if (po.getStockId() == stockObj.getId())
					isPortWatchStock = true;
			}
			for (WatchlistStockObject wo : woList) {
				if (wo.getStockId() == stockObj.getId())
					isPortWatchStock = true;
			}
		}
		if (isPortWatchStock) {
			int stockId = stockObj.getId();
			sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
			i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
			i.putExtra(Constants.KEY_STOCK_ID, stockId);
		} else {	
			sp.edit().putInt(Constants.SP_KEY_STOCK_ID, -1).commit();		
			i.putExtra(Constants.KEY_FROM, Constants.FROM_FIND_STOCKS);
			getActivity().startActivity(i);
		}
		
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		i.putExtra(Constants.KEY_TAB_POSITION, Constants.TO_NEWS_FRAGMENT);
		getActivity().startActivity(i);
	}
	

	public void addDeleteAllTextButton(Context ctx) {
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(R.attr.cancel_icon, typedValue, true);
		
		final Drawable x = ctx.getResources().getDrawable(typedValue.resourceId);

		x.setBounds(0, 0, x.getIntrinsicWidth() + 7, x.getIntrinsicHeight() + 7);
		etSearchTerm.setCompoundDrawables(null, null, etSearchTerm.getText().toString().equals("") ? null : x, null);
		etSearchTerm.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				
				if (etSearchTerm.getCompoundDrawables()[2] == null) {
					return false;
				}
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}
				if (event.getX() > etSearchTerm.getWidth() - etSearchTerm.getPaddingRight() - x.getIntrinsicWidth()) {
					if (!isHideCards)
						mCardView.setVisibility(View.VISIBLE);
					layoutResults.setVisibility(View.GONE);
					
					ls.clear();
					customAdapter.clear();
					customAdapter.notifyDataSetChanged();
					
					etSearchTerm.setText("");
					etSearchTerm.setCompoundDrawables(null, null, null, null);
				}
				return false;
			}
		});
		etSearchTerm.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				etSearchTerm.setCompoundDrawables(null, null, etSearchTerm.getText().toString().equals("") ? null : x, null);
				
				if (!etSearchTerm.getText().toString().equals("")) {
					mCardView.setVisibility(View.GONE);
					layoutResults.setVisibility(View.VISIBLE);
				} else if (etSearchTerm.getText().toString().equals("")) {
					if (!isHideCards)
						mCardView.setVisibility(View.VISIBLE);
					layoutResults.setVisibility(View.GONE);					
					ls.clear();
					customAdapter.clear();
					customAdapter.notifyDataSetChanged();
				}

				String mSymbol = s.toString().trim();

				boolean hasConnection = hasNetworkConnection();
				if (hasConnection) {
					if (mSymbol.length() > 0) {
						getYahooSymbols(mSymbol);
						getActivity().setProgressBarIndeterminateVisibility(true);
						pb.setVisibility(View.VISIBLE);
					}else{
						getYahooSymbols(mSymbol);
						getActivity().setProgressBarIndeterminateVisibility(false);
						pb.setVisibility(View.GONE);
					}
				}
			}

			public void afterTextChanged(Editable arg0) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}
	
	private void getYahooSymbols(String mQuote) {

		if (!hasAsyncTaskExcuted) {
			callBackTask = new FindStocksCallbackTask(getActivity(), pb, ls, customAdapter, lv, 0, 0, null);
			callBackTask.execute(mQuote);
			hasAsyncTaskExcuted = true;

		}

		else if (hasAsyncTaskExcuted == true && mQuote.length() == 0) {
			
			if(callBackTask != null){
				callBackTask.cancel(true);
				callBackTask = null;
			}
			
		} else if(hasAsyncTaskExcuted == true && mQuote.length() > 0){

			if(callBackTask != null){
				callBackTask.cancel(true);
				callBackTask = null;
			}

			callBackTask = new FindStocksCallbackTask(getActivity(), pb, ls, customAdapter, lv, 0, 0, null);
			callBackTask.execute(mQuote);
		}
	}
	

	@Override
	public void onResume() {
		// MyInputMethodManager.showKeyboard(StockMainActivity.this);
		registerBroadcast();
		super.onResume();
	}

	protected void addToWatchList(List<WatchlistObject> list, final SymbolCallBackObject symbolCallBackObject) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(resources.getString(R.string.title_add_to));
		//final String strCreateANewPortfolio = resources.getString(R.string.add_new_watchlist);

		final List<String> wList = new ArrayList<String>();

		for (WatchlistObject po : list) {
			wList.add(po.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, wList);
		builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				int watchId;
				int stockId;

				String watchlistName = wList.get(which);

				// 1. get portId;
				WatchlistObject wo = DbAdapter.getSingleInstance().fetchWatchlistByName(watchlistName);
				watchId = wo.getId();

				String symbol = symbolCallBackObject.getSymbol();
				String name = symbolCallBackObject.getName();
				String exch = symbolCallBackObject.getExch();
				String type = symbolCallBackObject.getType();
				String typeDisp = symbolCallBackObject.getTypeDisp();
				String exchDisp = symbolCallBackObject.getExchDisp();

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
					
					Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
					if (!isHideCards)
						mCardView.setVisibility(View.VISIBLE);
					layoutResults.setVisibility(View.GONE);					
					ls.clear();
					customAdapter.clear();
					customAdapter.notifyDataSetChanged();
				}
				
				// 4. add news alert
				alarmManager.addNewsAlert(symbol);
				
				etSearchTerm.clearFocus();				
			}
		});
		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	protected void addDirectlyToPort(final SymbolCallBackObject symbolCallBackObject, int portId) {

		int stockId = 0;
		
		String symbol = symbolCallBackObject.getSymbol();
		String name = symbolCallBackObject.getName();
		String exch = symbolCallBackObject.getExch();
		String type = symbolCallBackObject.getType();
		String typeDisp = symbolCallBackObject.getTypeDisp();
		String exchDisp = symbolCallBackObject.getExchDisp();

		// 1. Add StockObject
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

		// 2. Add QuoteObject/Check Currency
		String currency = "";
		boolean isInsert = false;
		QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
		if (qo == null) {
			UpdateQuoteTaskSingleSymbol updateQuote = new UpdateQuoteTaskSingleSymbol(getActivity(), pb, portId, stockId);
			updateQuote.execute(symbol);
		} else {
			currency = qo.getCurrency();
		
			// 3. Add PortfolioStockObject
			PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portId, stockId);
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (ps == null) {
	
				ps = new PortfolioStockObject();
				ps.setPortfolioId(portId);
				ps.setStockId(stockId);	
				
				List<PortfolioStockObject> pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
				if (!pso.isEmpty()) {
					if (currency.equals(po.getCurrencyType())) {
						ps.insert();
						isInsert = true;
					} else {
						// return error
						Log.i("wrong currency", po.getCurrencyType()+":"+currency);
						
						WrongCurrencyDialog currDialog = new WrongCurrencyDialog();     
						currDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_WRONG_CURRENCY);
					}
				} else {
					ps.insert();
					po.setCurrencyType(currency);
					po.update();
					isInsert = true;
				}
			}
		}
		
		if (!isHideCards)
			mCardView.setVisibility(View.VISIBLE);
		layoutResults.setVisibility(View.GONE);					
		ls.clear();
		customAdapter.clear();
		customAdapter.notifyDataSetChanged();
		
		if (isInsert) {
			// 3. Add News Alert
			alarmManager.addNewsAlert(symbol);
			
			// sendBroadcast when only PortfolioStockObject did
			// insert.
			Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
			myNotifyDatasetChange(true, symbol);
			
			etSearchTerm.clearFocus();
			startAddTrade(portId, stockId);
		}
	}
	
	protected void addDirectlyToWatch(final SymbolCallBackObject symbolCallBackObject, int watchId) {

		int stockId = 0;
		
		String symbol = symbolCallBackObject.getSymbol();
		String name = symbolCallBackObject.getName();
		String exch = symbolCallBackObject.getExch();
		String type = symbolCallBackObject.getType();
		String typeDisp = symbolCallBackObject.getTypeDisp();
		String exchDisp = symbolCallBackObject.getExchDisp();

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
			alarmManager.addNewsAlert(symbol);

			// sendBroadcast when only WatchlistStockObject did
			// insert.
			Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
			myNotifyDatasetChange(true, symbol);
			
			if (!isHideCards)
				mCardView.setVisibility(View.VISIBLE);
			layoutResults.setVisibility(View.GONE);					
			ls.clear();
			customAdapter.clear();
			customAdapter.notifyDataSetChanged();
			
			alarmManager.addNewsAlert(symbol);
			
			etSearchTerm.clearFocus();
		}
	}

	protected void addToPortfolio(List<PortfolioObject> list, final SymbolCallBackObject symbolCallBackObject) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(resources.getString(R.string.title_add_to));

		final List<String> pList = new ArrayList<String>();
		//final String strCreateANewPortfolio = resources.getString(R.string.add_new_portfolio);
		
		for (PortfolioObject po : list) {
			pList.add(po.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, pList);
		builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				int stockId;
				int portfId;
				String portfName = pList.get(which);

				// 1. get portId;
				PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(portfName);
				portfId = po.getId();

				String symbol = symbolCallBackObject.getSymbol();
				String name = symbolCallBackObject.getName();
				String exch = symbolCallBackObject.getExch();
				String type = symbolCallBackObject.getType();
				String typeDisp = symbolCallBackObject.getTypeDisp();
				String exchDisp = symbolCallBackObject.getExchDisp();

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
				
				// 3. Add QuoteObject/Check Currency
				String currency = "";
				boolean isInsert = false;
				QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
				if (qo == null) {
					UpdateQuoteTaskSingleSymbol updateQuote = new UpdateQuoteTaskSingleSymbol(getActivity(), pb, portfId, stockId);
					updateQuote.execute(symbol);
				} else {
					currency = qo.getCurrency();
				
					// 3. Add PortfolioStockObject
					PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portfId, stockId);
					if (ps == null) {
			
						ps = new PortfolioStockObject();
						ps.setPortfolioId(portfId);
						ps.setStockId(stockId);	
						
						List<PortfolioStockObject> pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portfId);
						if (!pso.isEmpty()) {
							if (currency.equals(po.getCurrencyType())) {
								ps.insert();
								isInsert = true;
							} else {
								// return error
								Log.i("wrong currency", po.getCurrencyType()+":"+currency);
								
								WrongCurrencyDialog currDialog = new WrongCurrencyDialog();     
								currDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_WRONG_CURRENCY);
							}
						} else {
							ps.insert();
							po.setCurrencyType(currency);
							po.update();
							isInsert = true;
						}
					}
				}
				
				if (!isHideCards)
					mCardView.setVisibility(View.VISIBLE);
				layoutResults.setVisibility(View.GONE);					
				ls.clear();
				customAdapter.clear();
				customAdapter.notifyDataSetChanged();
				
				if (isInsert) {
					// 3. Add News Alert
					alarmManager.addNewsAlert(symbol);
					
					// sendBroadcast when only PortfolioStockObject did
					// insert.
					Toast.makeText(getActivity(), resources.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
					myNotifyDatasetChange(true, symbol);
					
					etSearchTerm.clearFocus();
					startAddTrade(portfId, stockId);
				}
			}	
			
		});
		
		builder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				/*// user create a new portfolio name
				AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog(scbo, null);     
        		portDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
        		updateQuoteAndChartFromYahoo(scbo.getSymbol());*/
				dialog.dismiss();
			}
		});
		AlertDialog aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startAddTrade(int portId, int stockId) {
		Intent i = new Intent(getActivity(), AddNewTrade.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(i);
	}
	

	protected void myNotifyDatasetChange(boolean isUpdatePorfolioList, String symbol) {		
		
		updateQuoteAndChartFromYahoo(symbol);

		Intent i = null;

		if (!isUpdatePorfolioList) {
			i = new Intent(Constants.ACTION_WATCHLIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		} else {
			i = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		}
		getActivity().sendBroadcast(i);

	}

	
	private void hideSoftKeyboard(){
		etSearchTerm.setText("");
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchTerm.getWindowToken(), 0);   
	}
	
	private void showSoftKeyboard(){
	    if(getActivity().getCurrentFocus()!=null && getActivity().getCurrentFocus() instanceof EditText){
	    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.showSoftInput(etSearchTerm, InputMethodManager.SHOW_IMPLICIT);
	    }
	}
	
	private void noPortOrWatchlistAlert(final boolean isPort) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

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
	        		portDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
				} else {
					AddNewWatchlistDialog watchDialog = new AddNewWatchlistDialog();
					watchDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_ADD_WATCHLIST);
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
	        int titleDividerId = resources.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(resources.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_MAIN_ACTIVITY);
		filter.addAction(Constants.ACTION_WATCHLIST_FRAGMENT);
		filter.addAction(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		filter.addAction(Constants.ACTION_FILTER_PORTFOLIO);
		receiver = new MainWatchBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}
	
	private class MainWatchBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {

			if ( ( intent.getAction().equals(Constants.ACTION_MAIN_ACTIVITY) ||
					intent.getAction().equals(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT) ||
						intent.getAction().equals(Constants.ACTION_WATCHLIST_FRAGMENT) ||
							intent.getAction().equals(Constants.ACTION_FILTER_PORTFOLIO))
				&& getActivity() != null) {

				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					
					boolean isFilterPortList = bundle.getBoolean(Constants.KEY_FILTER_PORTFOLIOS_PAGER);
					boolean isUpdatePortList = bundle.getBoolean(Constants.KEY_UPDATE_PORTFOLIOS_PAGER);
					boolean isUpdateWatchList = bundle.getBoolean(Constants.KEY_UPDATE_WATCHLISTS_PAGER);
					
					if (isFilterPortList) {
						mPortId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
						Log.i("portId", Integer.toString(mPortId));
						updateCards(mPortId);
					} else if (isUpdatePortList || isUpdateWatchList) {
						updateCards(mPortId);
					} else
						updateCards(-1);
				}
			}
		}
	}
	
	private void updateCards(int portId) {

		List<WatchlistObject> wList = DbAdapter.getSingleInstance().fetchWatchlists();
		List<PortfolioObject> pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		
		mCards.clear();

		if (wList.size() > 0) {
			Collections.sort(wList, new MyComparatorUtils.WatchlistNameComparator());
			for (WatchlistObject wo : wList) {
				ListAdapter listAdapter = refreshWatchList(wo.getId());
				mCards.add(new WatchlistCard(wo, listAdapter, getActivity()));
			}
		} 
		
		if (pList.size() > 0) {
			Collections.sort(pList, new MyComparatorUtils.PortfolioNameComparator());
			for (PortfolioObject po : pList) {
				if (portId == -1) {
					ListAdapter listAdapter = refreshPortList(po.getId());
					mCards.add(new PortfolioCard(po, listAdapter, getActivity()));
				} else if (portId == po.getId()){
					ListAdapter listAdapter = refreshPortList(po.getId());
					mCards.add(new PortfolioCard(po, listAdapter, getActivity()));
				}
			}
		} 
		mCardArrayAdapter.notifyDataSetChanged();
	}

	
	public ListAdapter refreshWatchList(int id) {

		// 1. based on watchlist id to get all stock ids:
		int mWatchId = id;
		List<WatchlistStockObject> wList = DbAdapter.getSingleInstance().fetchWatchStockListByWatchId(mWatchId);
		ListAdapter newAdapter = null;

		// 2. base on stock ids to get all stocksObject
		ArrayList<SymbolCallBackObject> sList = new ArrayList<SymbolCallBackObject>();

		StockObject so;
		SymbolCallBackObject s;
		if (wList.size() > 0) {
			for (WatchlistStockObject wl : wList) {
				so = new StockObject();
				s = new SymbolCallBackObject();

				int stockId = wl.getStockId();

				so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);

				if (so != null) {

					s.setExch(so.getExch());
					s.setExchDisp(so.getExchDisp());
					s.setName(so.getName());
					s.setSymbol(so.getSymbol());
					s.setType(so.getType());
					s.setTypeDisp(so.getTypeDisp());

					sList.add(s);
				}
			}
		} else {
			s = new SymbolCallBackObject();
			s.setName(getActivity().getResources().getString(R.string.no_stock_added));
			s.setExch("");
			s.setExchDisp("");
			s.setType("");
			s.setSymbol("");
			s.setTypeDisp("");
			
			sList.add(s);
		}
		
		Collections.sort(sList, new MyComparatorUtils.SymbolCallbackObjectNameComparator());

		if (sList.size() > 0) {
			newAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, sList);
		} 
		
		return newAdapter;
	}
	
	public ListAdapter refreshPortList(int id) {

		// 1. based on portfolio id to get all stock ids:
		List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(id);
		ListAdapter newAdapter = null;

		// 2. base on stock ids to get all stocksObject
		ArrayList<SymbolCallBackObject> sList = new ArrayList<SymbolCallBackObject>();

		StockObject so;
		SymbolCallBackObject s;
		if (pList.size() > 0) {
			for (PortfolioStockObject pl : pList) {
				so = new StockObject();
				s = new SymbolCallBackObject();

				int stockId = pl.getStockId();

				so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);

				if (so != null) {

					s.setExch(so.getExch());
					s.setExchDisp(so.getExchDisp());
					s.setName(so.getName());
					s.setSymbol(so.getSymbol());
					s.setType(so.getType());
					s.setTypeDisp(so.getTypeDisp());

					sList.add(s);
				}
			}
		} else {
			s = new SymbolCallBackObject();
			s.setName(getActivity().getResources().getString(R.string.no_stock_added));
			s.setExch("");
			s.setExchDisp("");
			s.setType("");
			s.setSymbol("");
			s.setTypeDisp("");
			
			sList.add(s);
		}
		
		Collections.sort(sList, new MyComparatorUtils.SymbolCallbackObjectNameComparator());

		if (sList.size() > 0) {
			newAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, sList);
		} 
		
		return newAdapter;
	}
	
	private void updateQuoteAndChartFromYahoo(String symbol) {

		if (quoteTask == null) {
			quoteTask = new UpdateQuoteTaskSingleSymbol(getActivity(), pb, 0, 0);
			quoteTask.execute(symbol);
		} else {
			quoteTask.cancel(true);
			quoteTask = null;
			quoteTask = new UpdateQuoteTaskSingleSymbol(getActivity(), pb, 0, 0);
			quoteTask.execute(symbol);
		}
		
	}

	private boolean hasNetworkConnection() {
		boolean hasNetworkConnection = NetworkConnectivity.hasNetworkConnection(getActivity());

		if (!hasNetworkConnection) {
			tvNetworkConnection.setVisibility(View.VISIBLE);
			mCardView.setVisibility(View.GONE);
			layoutResults.setVisibility(View.GONE);

			ls.clear();
			customAdapter.clear();
			customAdapter.notifyDataSetChanged();
			return false;
		} else {
			tvNetworkConnection.setVisibility(View.GONE);
			return true;
		}
	}
	
	private String exchToCurrency(String exch) {
		String currency = "";
		if (exch.equals("NAS") || exch.equals("NYSE"))
			currency = "USD";
		else if (exch.equals("Stuttgart") || exch.equals("Berlin") || exch.equals("Munich"))
			currency = "EURO";
		else if (exch.equals("London"))
			currency = "GBP";
		else if (exch.equals("Tokyo"))
			currency = "JPY";
		
		return currency;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}
	
	private void buildQuickActionItem() {

		ActionItem addItemViewStockDetails = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS, strViewStockDetails, 
				resources.getDrawable(R.drawable.ic_action_search));
		ActionItem addItemAlert = new ActionItem(Constants.QUICK_ACTION_ID_SET_ALERT, strAlert, resources.getDrawable(
				R.drawable.ic_alert_dark));
		ActionItem addItemViewLatestNews = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS, strViewLatestNews, 
				resources.getDrawable(R.drawable.quick_action_view_news));
		ActionItem addItemAddToWatchlist = new ActionItem(Constants.QUICK_ACTION_ID_ADD_TO_WATCHLIST, strAddToWatchlist, 
				resources.getDrawable(R.drawable.ic_add_dark));
		ActionItem addItemAddToPortfolio = new ActionItem(Constants.QUICK_ACTION_ID_ADD_TO_PORTFOLIO, strAddToPortfolio, 
				resources.getDrawable(R.drawable.ic_add_dark));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();
		items.add(addItemViewStockDetails);
		items.add(addItemAlert);
		items.add(addItemViewLatestNews);
		items.add(addItemAddToWatchlist);
		items.add(addItemAddToPortfolio);	

		boolean isNumColumnThree = true;
		mQuickAction = new GridQuickAction(getActivity(), isNumColumnThree, items.size());
		mQuickAction.setupAdapter(items);
		mQuickAction.setOnActionItemClickListener(new GridQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(GridQuickAction quickAction, int pos, int actionId) {

				int mActionId = actionId;

				switch (mActionId) {

				case Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS:
					viewDetailsIntent(scbo);
					break;
				case Constants.QUICK_ACTION_ID_SET_ALERT:
					showAddAlert(scbo);
					break;
				case Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS:
					viewNewsIntent(scbo);
					break;
				case Constants.QUICK_ACTION_ID_ADD_TO_WATCHLIST:
					List<WatchlistObject> watchlist = DbAdapter.getSingleInstance().fetchWatchlists();
					if (watchlist.isEmpty())
						noPortOrWatchlistAlert(false);
					else if (watchlist.size() == 1) {
						int onlyOneWatchId = watchlist.get(0).getId();
						addDirectlyToWatch(scbo, onlyOneWatchId);
					} else
						addToWatchList(watchlist, scbo);
					break;
				case Constants.QUICK_ACTION_ID_ADD_TO_PORTFOLIO:
					int currPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, 0);
					if (currPortId > 0)
						addDirectlyToPort(scbo, currPortId);
					else {
						List<PortfolioObject> portList = DbAdapter.getSingleInstance().fetchPortfolioList();
						if (portList.isEmpty())
							noPortOrWatchlistAlert(true);
						else if (portList.size() == 1) {
							int onlyOnePortId = portList.get(0).getId();
							addDirectlyToPort(scbo, onlyOnePortId);
						} else
							addToPortfolio(portList, scbo);
					}
					break;
				}
			}

		});
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (quoteTask != null) {
			quoteTask.cancel(true);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.et_search_term) {
			if (hasFocus)
				showSoftKeyboard();
			else
				hideSoftKeyboard();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		 case R.id.et_search_term:
			 etSearchTerm.requestFocus();
			 break;
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (etSearchTerm != null && !isVisibleToUser)
			etSearchTerm.clearFocus();
	}

	
}
