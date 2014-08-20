package com.handyapps.stocktracker.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.adapter.TxnPortAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddCashPosDialog;
import com.handyapps.stocktracker.dialogs.EditCashPosDialog;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionObject;
import com.handyapps.stocktracker.model.TxnPortObject;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.ThemeUtils;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class TransactionsActivity extends Activity implements OnClickListener {
	
	private Spinner mSpinnerSymbols;
	private Spinner mSpinnerSortBy;
	private TextView mBtnAddTxn;
	private RadioButton mRBCash;
	private RadioButton mRBTrades;
	private RadioButton mRBAll;
	
	private int currSpinnerCashTradesIndex;
	private int currSpinnerSortByIndex = 0;
	private int mPortId = -1;
	private int countSelectionPort = 0;
	private int countSelectionSym = 0;
	private int countSelectionSort = 0;
	private String currSym = "";
	private String na = "N.A.";
	private String strViewTxns;
	private String strSetAlert;	
	private String strRemove;
	private String strAllPorts;
	private String strAllSymbols;
	private List<String> portList;
	private List<String> symList;
	
	private List<PortfolioObject> pList;
	private List<PortfolioStockObject> psoList;
	private Bundle bundle;
	private Resources res;
	private SharedPreferences sp;
	private MyBroadcastReceiver receiver;
	
	private GridQuickAction mQuickAction;
	private GridQuickAction mQuickActionCash;
	private ListView lv;
	private ArrayList<TxnPortObject> mTpoList;
	private ArrayList<TxnPortObject> originalList;
	private TxnPortAdapter txnPortAdapter;
	private TxnPortObject tpo;	
	private ArrayAdapter<String> mSpinnerAdapter;
	private ArrayAdapter<String> mSpinnerAdapterSymbols;
	private ActionBar actionBar;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ThemeUtils.onActivityCreateSetTheme(this, false);
		res = getResources();
		actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO 
				| ActionBar.DISPLAY_SHOW_HOME);
		
		setContentView(R.layout.transactions_activity);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);	
		strViewTxns = res.getString(R.string.view_edit_details);
		strSetAlert = res.getString(R.string.q_set_alert);
		strRemove = res.getString(R.string.remove_stock);
		strAllPorts = res.getString(R.string.all_portfolios);
		strAllSymbols = res.getString(R.string.all_stocks);

		mRBAll = (RadioButton) findViewById(R.id.radio_all);
		mRBCash = (RadioButton) findViewById(R.id.radio_cash);
		mRBTrades = (RadioButton) findViewById(R.id.radio_trades);
		mRBAll.setOnClickListener(this);
		mRBCash.setOnClickListener(this);
		mRBTrades.setOnClickListener(this);
		
		portList = new ArrayList<String>();
		pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		for (PortfolioObject po : pList) {
			portList.add(po.getName());
		}
		portList.add(strAllPorts);
		mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_ab_spinner_item, portList);
		mSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				if (countSelectionPort >= 1) {
					try {
						mPortId = pList.get(position).getId(); 
					} catch (IndexOutOfBoundsException e) {
						mPortId = -1;
					}
					updateCards(mPortId);
					mSpinnerSymbols.setSelection(symList.size()-1);
					currSym = "";
				}
				
				countSelectionPort++;
				
				return true;
			}
		};
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);	

		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		//currSpinnerCashTradesIndex = Constants.KEY_ALL;
		lv = (ListView) findViewById(R.id.lv_txn_port);
		LinearLayout emptyView = (LinearLayout) findViewById(R.id.empty_txns_view);
		lv.setEmptyView(emptyView);
		mTpoList = new ArrayList<TxnPortObject>();
		originalList = new ArrayList<TxnPortObject>();

		txnPortAdapter = new TxnPortAdapter(this, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, 
				mTpoList, originalList);
		lv.setAdapter(txnPortAdapter);
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setOnItemClickListener(myChildItemClickListener);
		
		mBtnAddTxn = (TextView) findViewById(R.id.btn_add_new_txn);
		mBtnAddTxn.setOnClickListener(this);
				
		symList = new ArrayList<String>();
		
		mSpinnerSymbols = (Spinner) findViewById(R.id.spinner_symbol);
		mSpinnerAdapterSymbols = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, symList);
		mSpinnerAdapterSymbols.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerSymbols.setAdapter(mSpinnerAdapterSymbols);
		mSpinnerSymbols.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
				if (countSelectionSym >= 1) {
					if (position != symList.size()-1) {
						txnPortAdapter.getFilter().filter(symList.get(position));	
						currSym = symList.get(position);
					} else {
						txnPortAdapter.getFilter().filter("");
						currSym = "";
					}
				}
				
				countSelectionSym++;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		mSpinnerSortBy = (Spinner) findViewById(R.id.spinner_sort_txns_by);
		ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(this,
		        R.array.spinner_sort_txns_by, android.R.layout.simple_spinner_item);
		mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, portList);
		adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerSortBy.setAdapter(adapterSort);
		mSpinnerSortBy.setSelection(currSpinnerSortByIndex);
		mSpinnerSortBy.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				if (countSelectionSort >= 1) {
					currSpinnerSortByIndex = arg2;
					updateCards(mPortId);
					txnPortAdapter.getFilter().filter(currSym);
				} 
				
				countSelectionSort++;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		bundle = getIntent().getExtras();
		if (bundle != null) {
			currSpinnerCashTradesIndex = bundle.getInt(Constants.KEY_CASH_TRADES_ALL);
			String symbol = bundle.getString(Constants.KEY_FILTER_BY_SYMBOL);
			int portId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
			
			switch (currSpinnerCashTradesIndex) {
				case Constants.KEY_CASH:
					mRBCash.setChecked(true);
					mSpinnerSymbols.setEnabled(false);
					mSpinnerSymbols.setClickable(false);
					break;
				case Constants.KEY_TRADES:
					mRBTrades.setChecked(true);
					break;
				case Constants.KEY_ALL:
					mRBAll.setChecked(true);
					break;
			}
			
			if (portId == 0)
				portId = mPortId;
			
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			String portName = "";
			if (po != null) {
				portName = po.getName();
				actionBar.setSelectedNavigationItem(portList.indexOf(portName));
			} else
				actionBar.setSelectedNavigationItem(portList.size()-1);
			
			updateCards(portId);
			if (symbol != null) {
				txnPortAdapter.getFilter().filter(symbol);	
				mSpinnerSymbols.setSelection(symList.indexOf(symbol));
				currSym = symbol;
			}

		} else
			updateCards(mPortId);
				
		buildQuickActionItem();
		buildQuickActionItemCash();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TRANSACTIONS_ACTIVITY);
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			String action = intent.getAction();

			if (action.equals(Constants.ACTION_TRANSACTIONS_ACTIVITY)) {
				updateCards(mPortId);
			}
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int mID = item.getItemId();

		switch (mID) {
		case android.R.id.home:
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			break;
		}
		return true;
	}

	
	private OnItemClickListener myChildItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(android.widget.AdapterView<?> parent,
				View view, int position, long id) {
			tpo = mTpoList.get(position);
			if (tpo.getType().toUpperCase().contains("C"))
				mQuickActionCash.show(view);
			else
				mQuickAction.show(view);
		}
	};
	
	private void updateSymbols(int portId) {
		
		if (symList != null)
			symList.clear();
		
		if (psoList != null)
			psoList.clear();
		
		if (portId == -1) 
			psoList = DbAdapter.getSingleInstance().fetchPortStockList();
		else 
			psoList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
		
		for (PortfolioStockObject pso : psoList) {
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(pso.getStockId());
			symList.add(so.getSymbol());
		}
		
		symList.add(strAllSymbols);
		mSpinnerAdapterSymbols.notifyDataSetChanged();
	}
	
	
	private void updateCards(int portId) {

		if (pList != null)
			pList.clear();
		pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		List<CashPosObject> cpoList = new ArrayList<CashPosObject>();
		List<TransactionObject> toList = new ArrayList<TransactionObject>();
		ArrayList<TxnPortObject> tpoList = new ArrayList<TxnPortObject>();	
		updateSymbols(portId);
		
		if (portId == -1) {
			cpoList = DbAdapter.getSingleInstance().fetchCashPosList();
			toList = DbAdapter.getSingleInstance().fetchTransactionObjectAll();
			if (currSpinnerCashTradesIndex != Constants.KEY_CASH) {
				for (TransactionObject to : toList) {
					TxnPortObject tpo = new TxnPortObject();	
					tpo.setId(to.getId());
					tpo.setFee(to.getFee());
					tpo.setNotes(to.getNotes());
					tpo.setNumOfShares(to.getNumOfShares());
					tpo.setPortId(to.getPortId());
					tpo.setPrice(to.getPrice());
					tpo.setStockId(to.getStockId());
					tpo.setSymbol(DbAdapter.getSingleInstance().fetchStockObjectByStockId(to.getStockId()).getSymbol());
					tpo.setTotal(to.getTotal());
					tpo.setTradeDate(to.getTradeDate());
					tpo.setType(to.getType());
					tpoList.add(tpo);
				}
				if (currSpinnerCashTradesIndex == Constants.KEY_ALL) {
					for (CashPosObject cpo : cpoList) {
						TxnPortObject tpo = new TxnPortObject();	
						tpo.setId(cpo.getId());
						tpo.setPortId(cpo.getPortfolioId());
						tpo.setPrice(cpo.getAmount());
						tpo.setTradeDate(cpo.getTxnDate());
						tpo.setSymbol(na);
						String sTot = cpo.getAmount();
						double dTot = DecimalsConverter.convertToDoubleValue(Double.parseDouble(sTot), Constants.NUMBER_OF_DECIMALS);
						tpo.setTotal(dTot);
						tpo.setType(cpo.getTxnType());
						tpo.setNotes("");
						tpoList.add(tpo);
					}
				}
			} else if (currSpinnerCashTradesIndex == Constants.KEY_CASH) {
				for (CashPosObject cpo : cpoList) {
					TxnPortObject tpo = new TxnPortObject();	
					tpo.setId(cpo.getId());
					tpo.setPortId(cpo.getPortfolioId());
					tpo.setPrice(cpo.getAmount());
					String sTot = cpo.getAmount();
					double dTot = DecimalsConverter.convertToDoubleValue(Double.parseDouble(sTot), Constants.NUMBER_OF_DECIMALS);
					tpo.setTotal(dTot);
					tpo.setTradeDate(cpo.getTxnDate());
					tpo.setSymbol(na);
					tpo.setType(cpo.getTxnType());
					tpo.setNotes("");
					tpoList.add(tpo);
				}
			}
			if (!tpoList.isEmpty())
				Collections.sort(tpoList, new MyComparator());
		} else {
			for (PortfolioObject po : pList) {
				if (portId == po.getId()){
					cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(po.getId());
					List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(po.getId());
					for (PortfolioStockObject pso : psoList) {
						List<TransactionObject> toListRetrieve = new ArrayList<TransactionObject>();
						toListRetrieve = DbAdapter.getSingleInstance().fetchTransactionObjectByStockIdAndPortId(pso.getStockId(), po.getId());
						for (TransactionObject to : toListRetrieve) 
							toList.add(to);
					}
					if (currSpinnerCashTradesIndex != Constants.KEY_CASH) {
						for (TransactionObject to : toList) {
							TxnPortObject tpo = new TxnPortObject();	
							tpo.setFee(to.getFee());
							tpo.setNotes(to.getNotes());
							tpo.setNumOfShares(to.getNumOfShares());
							tpo.setPortId(to.getPortId());
							tpo.setPrice(to.getPrice());
							tpo.setStockId(to.getStockId());
							tpo.setSymbol(DbAdapter.getSingleInstance().fetchStockObjectByStockId(to.getStockId()).getSymbol());
							tpo.setTotal(to.getTotal());
							tpo.setTradeDate(to.getTradeDate());
							tpo.setType(to.getType());
							tpoList.add(tpo);
						}
						if (currSpinnerCashTradesIndex == Constants.KEY_ALL) {
							for (CashPosObject cpo : cpoList) {
								TxnPortObject tpo = new TxnPortObject();	
								tpo.setId(cpo.getId());
								tpo.setPortId(cpo.getPortfolioId());
								tpo.setPrice(cpo.getAmount());
								String sTot = cpo.getAmount();
								double dTot = DecimalsConverter.convertToDoubleValue(Double.parseDouble(sTot), Constants.NUMBER_OF_DECIMALS);
								tpo.setTotal(dTot);
								tpo.setTradeDate(cpo.getTxnDate());
								tpo.setSymbol(na);
								tpo.setType(cpo.getTxnType());
								tpo.setNotes("");
								tpoList.add(tpo);
							}
						}
					} else if (currSpinnerCashTradesIndex == Constants.KEY_CASH) {
						for (CashPosObject cpo : cpoList) {
							TxnPortObject tpo = new TxnPortObject();	
							tpo.setId(cpo.getId());
							tpo.setPortId(cpo.getPortfolioId());
							tpo.setPrice(cpo.getAmount());
							String sTot = cpo.getAmount();
							double dTot = DecimalsConverter.convertToDoubleValue(Double.parseDouble(sTot), Constants.NUMBER_OF_DECIMALS);
							tpo.setTotal(dTot);
							tpo.setTradeDate(cpo.getTxnDate());
							tpo.setSymbol(na);
							tpo.setType(cpo.getTxnType());
							tpo.setNotes("");
							tpoList.add(tpo);
						}
					}
					if (!tpoList.isEmpty())
						Collections.sort(tpoList, new MyComparator());
				}
			}
		}
		
		if (mTpoList != null)
			mTpoList.clear();
		else
			mTpoList = new ArrayList<TxnPortObject>();
		for (TxnPortObject tpo : tpoList)
			mTpoList.add(tpo);
		
		originalList.clear();
		originalList.addAll(mTpoList);
		txnPortAdapter.notifyDataSetChanged();
	}

	
	@Override
	public void onResume() {
		super.onResume();
		registerBroadcast();
	}

	
	public class MyComparator implements Comparator<TxnPortObject> {
		@Override
		public int compare(TxnPortObject lhs, TxnPortObject rhs) {
			if (currSpinnerSortByIndex == 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        	try {
					Date date1 = sdf.parse(Integer.toString(lhs.getTradeDate()));
					Date date2 = sdf.parse(Integer.toString(rhs.getTradeDate()));
					return date1.compareTo(date2);
				} catch (ParseException e) {
					e.printStackTrace();
					return 0;
				}
			} else if (currSpinnerSortByIndex == 1)
				return lhs.getSymbol().compareToIgnoreCase(rhs.getSymbol());
			else if (currSpinnerSortByIndex == 2)
				return Double.compare(lhs.getTotal(), rhs.getTotal());
			return 0;
		}
	}
	
	private void buildQuickActionItem() {

		ActionItem addItemView = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS, strViewTxns, res.getDrawable(
				R.drawable.ic_action_search));
		ActionItem addItemAlert = new ActionItem(Constants.QUICK_ACTION_ID_SET_ALERT, strSetAlert, res.getDrawable(
				R.drawable.ic_alert_dark));
		ActionItem addItemRemove = new ActionItem(Constants.QUICK_ACTION_ID_DELETE, strRemove, res.getDrawable(
				R.drawable.ic_delete_dark));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();

		items.add(addItemView);
		items.add(addItemAlert);
		items.add(addItemRemove);
		boolean isNumColumnThree = true;
		mQuickAction = new GridQuickAction(this, isNumColumnThree, items.size());

		mQuickAction.setupAdapter(items);

		// setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new GridQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(GridQuickAction quickAction, int pos, int actionId) {

				int mActionId = actionId;
				switch (mActionId) {

				case Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS:
					editSingleTransactionIntent();
					break;
					
				case Constants.QUICK_ACTION_ID_SET_ALERT:
					showAddAlert();
					break;
					
				case Constants.QUICK_ACTION_ID_DELETE:
					showDeleteDialog();
					break;
				}
			}
		});
	}
	
	
	private void buildQuickActionItemCash() {

		ActionItem addItemView = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS, strViewTxns, res.getDrawable(
				R.drawable.ic_action_search));
		ActionItem addItemRemove = new ActionItem(Constants.QUICK_ACTION_ID_DELETE, strRemove, res.getDrawable(
				R.drawable.ic_delete_dark));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();

		items.add(addItemView);
		items.add(addItemRemove);
		boolean isNumColumnThree = true;
		mQuickActionCash = new GridQuickAction(this, isNumColumnThree, items.size());

		mQuickActionCash.setupAdapter(items);

		// setup the action item click listener
		mQuickActionCash.setOnActionItemClickListener(new GridQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(GridQuickAction quickAction, int pos, int actionId) {

				int mActionId = actionId;
				switch (mActionId) {

				case Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS:
					editSingleTransactionIntent();
					break;
				case Constants.QUICK_ACTION_ID_DELETE:
					showDeleteDialog();
					break;
				}
			}
		});
	}
	
	
	private void editSingleTransactionIntent() {
		String txnObjType = tpo.getType();
		if (txnObjType.equals(TxnPortObject.CASH_DEPOSIT_TYPE) || 
				txnObjType.equals(TxnPortObject.CASH_WITHDRAWAL_TYPE)) {
			CashPosObject cpo = DbAdapter.getSingleInstance().fetchCashPosObjectByIdAndPortId(tpo.getId(), tpo.getPortId());
			EditCashPosDialog editCashDialog = new EditCashPosDialog(cpo);                 		
			editCashDialog.show(getSupportFragmentManager(), Constants.DIALOG_EDIT_CASH_TXN_S);
		} else {
			Intent i = new Intent(TransactionsActivity.this, AddNewTrade.class);
			i.putExtra(Constants.KEY_FROM, Constants.FROM_EDIT_TRANSACTION);
			i.putExtra(Constants.KEY_TRANSACITON_TXN_ID, tpo.getId());
			i.putExtra(Constants.KEY_PORTFOLIO_ID, tpo.getPortId());
			i.putExtra(Constants.KEY_STOCK_ID, tpo.getStockId());
			startActivity(i);
		}
	}
	
	
	private void showAddAlert() {
		String symbol = tpo.getSymbol();

		Intent i = new Intent(this, AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		startActivity(i);
	}
	

	private void showDeleteDialog() {

		String msg = res.getString(R.string.delete_txn_msg);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_action_warning);
		builder.setTitle(res.getString(R.string.delete_txn));
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				int mPortId = tpo.getPortId();
				boolean isDeleted = false; 
				
				if (!tpo.getType().contains("c")) {
					TransactionObject mObject = DbAdapter.getSingleInstance().fetchTransactionObjectById(tpo.getId());
					isDeleted = mObject.delete();
				} else {
					CashPosObject mObject = DbAdapter.getSingleInstance().fetchCashPosObjectByIdAndPortId(tpo.getId(), mPortId);
					isDeleted = mObject.delete();
				}
				
				if (isDeleted) {
					String msg = res.getString(R.string.txn_deleted);
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					WidgetUtils.updateWidget(getApplicationContext());
					sendBroadastUpdatePagerIndicator();
					/*tpa.clear();
					for (TxnPortObject detail : tpoList) {
						tpa.add(detail);
					}
					tpa.notifyDataSetChanged();*/
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void sendBroadastUpdatePagerIndicator() {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		sendBroadcast(i);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.btn_add_new_txn:
			addNewTxnDialog();
			break;	
		case R.id.radio_cash:
			mSpinnerSymbols.setEnabled(false);
			mSpinnerSymbols.setClickable(false);			
			currSpinnerCashTradesIndex = 0;
			updateCards(mPortId);
			currSym = "";
			break;
		case R.id.radio_trades:
			mSpinnerSymbols.setEnabled(true);
			mSpinnerSymbols.setClickable(true);
			currSpinnerCashTradesIndex = 1;
			updateCards(mPortId);
			break;
		case R.id.radio_all:
			mSpinnerSymbols.setEnabled(true);
			mSpinnerSymbols.setClickable(true);
			currSpinnerCashTradesIndex = 2;
			updateCards(mPortId);
			break;
		}
	}
	
	protected void addNewTxnDialog() {
		String title = res.getString(R.string.add_new_txn);
		final String[] itemsArr = res.getStringArray(R.array.add_new_transaction);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(itemsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (which == 0) {
            		Intent i = new Intent(getApplicationContext(), AddNewTrade.class);
            		i.putExtra(Constants.KEY_FROM, Constants.FROM_TRANSACTIONS_ACTIVITY);
            		if (mPortId != -1)
            			i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
            		startActivity(i);
            	} else if (which == 1) {
            		AddCashPosDialog addCashDialog = new AddCashPosDialog();                 		
            		addCashDialog.show(getSupportFragmentManager(), Constants.DIALOG_ADD_CASH_TXN_S);
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
