package com.handyapps.stocktracker.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewAlert;
import com.handyapps.stocktracker.activity.AddNewTrade;
import com.handyapps.stocktracker.adapter.TxnPortAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog;
import com.handyapps.stocktracker.dialogs.AddNewWatchlistDialog;
import com.handyapps.stocktracker.dialogs.EditCashPosDialog;
import com.handyapps.stocktracker.dialogs.WrongCurrencyDialog;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.SingleTickerDataset;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionManager;
import com.handyapps.stocktracker.model.TransactionObject;
import com.handyapps.stocktracker.model.TxnPortObject;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.widget.WidgetUtils;

public final class MyTradesFragment extends Fragment implements OnClickListener  {
	
	private static final String KEY_SYMBOL = "KEY_SYMBOL";
	private static final String KEY_STOCK_ID = "KEY_STOCK_ID";

	private int fromId; 
	private int mStockId;
	private int mPortId = -1;
	private Bundle bundle;
	private MyBroadcastReceiver receiver;
	private StockObject so;
	private PortfolioObject po;
	private TxnPortObject tpo;	
	private String allPortfolios;
	private String strViewTxns;
	private String strSetAlert;	
	private String strRemove;
		
	private TextView tvOPTitle;
	private TextView tvNPTitle;
	private TextView tvNPValue;
	private TextView tvNVTitle;
	private TextView tvNVValue;
	private TextView tvAPTitle;
	private TextView tvAPValue;
	private TextView tvNoInfoFound;	
	private Button btnAddTxn;
	private Spinner spFilterPort;
	private TextView btnAddStock;	
	private LinearLayout emptyView;
	private ArrayAdapter<String> portAdapter;
	private ArrayList<String> portList;
	private ArrayList<PortfolioObject> portObjList;
	private ListView lv;
	private ArrayList<TxnPortObject> mTpoList;
	private TxnPortAdapter txnPortAdapter;
	private GridQuickAction mQuickAction;
	
	private Resources res;
	private SharedPreferences sp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		// Handle Bundle:
		bundle = getActivity().getIntent().getExtras();
		if (bundle != null) {
			fromId = bundle.getInt(Constants.KEY_FROM);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (so != null)
			outState.putString(KEY_SYMBOL, so.getSymbol());
		outState.putInt(KEY_STOCK_ID, mStockId);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			String symbol = savedInstanceState.getString(KEY_SYMBOL);
			so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
			mStockId = savedInstanceState.getInt(KEY_STOCK_ID);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.my_trades_fragment, container, false);
		View header = getLayoutInflater().inflate(R.layout.my_trades_header, null);
		View footer = getLayoutInflater().inflate(R.layout.my_trades_footer, null);
		
		Typeface helveticaSReg = Typeface.createFromAsset(getActivity().getAssets(), "helvetica-s-regular.ttf");
		Typeface helveticaSBold = Typeface.createFromAsset(getActivity().getAssets(), "helvetica-s-bold.ttf");
		
		res = getActivity().getResources();
		
		allPortfolios = res.getString(R.string.all_portfolios);
		strViewTxns = res.getString(R.string.view_edit_details);
		strSetAlert = res.getString(R.string.q_set_alert);
		strRemove = res.getString(R.string.remove_stock);
		
		tvOPTitle = (TextView) header.findViewById(R.id.tv_overall_pos_title);
		tvNPTitle = (TextView) header.findViewById(R.id.tv_net_pos_title);
		tvNPValue = (TextView) header.findViewById(R.id.tv_net_pos_value);
		tvNVTitle = (TextView) header.findViewById(R.id.tv_net_value_title);
		tvNVValue = (TextView) header.findViewById(R.id.tv_net_value_value);
		tvAPTitle = (TextView) header.findViewById(R.id.tv_avg_price_title);
		tvAPValue = (TextView) header.findViewById(R.id.tv_avg_price_value);
		tvNoInfoFound = (TextView) view.findViewById(R.id.tv_no_info_found);		
		
		tvOPTitle.setTypeface(helveticaSReg);
		tvNPTitle.setTypeface(helveticaSReg);
		tvNPValue.setTypeface(helveticaSBold, Typeface.BOLD);
		tvNVTitle.setTypeface(helveticaSReg);
		tvNVValue.setTypeface(helveticaSBold, Typeface.BOLD);
		tvAPTitle.setTypeface(helveticaSReg);
		tvAPValue.setTypeface(helveticaSBold, Typeface.BOLD);
		
		btnAddTxn = (Button) footer.findViewById(R.id.btn_add_transaction);
		btnAddTxn.setOnClickListener(this);
		
		lv = (ListView) view.findViewById(R.id.lv_txn_stock);
		emptyView = (LinearLayout) view.findViewById(R.id.layout_when_empty);
		lv.setEmptyView(emptyView);
		lv.addHeaderView(header);
		lv.addFooterView(footer);
		mTpoList = new ArrayList<TxnPortObject>();
		ArrayList<TxnPortObject> originalList = new ArrayList<TxnPortObject>();

		txnPortAdapter = new TxnPortAdapter((Activity) getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, 
				mTpoList, originalList);
		lv.setAdapter(txnPortAdapter);
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setOnItemClickListener(myChildItemClickListener);
		
		portList = new ArrayList<String>();
		
		spFilterPort = (Spinner) view.findViewById(R.id.spinner_filter_port);
		portAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_item, portList);
		portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spFilterPort.setAdapter(portAdapter);
		spFilterPort.setSelection(portList.size()-1);
		spFilterPort.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
				String currPort = portList.get(arg2);	
				if (!currPort.equals(allPortfolios)) {
					po = DbAdapter.getSingleInstance().fetchPortfolioByName(currPort);
					mPortId = po.getId();
					tvOPTitle.setText("Current position in " + po.getName().toUpperCase());		
					refreshTxns(mPortId);
					updateSingleValue();
				} else {
					tvOPTitle.setText("Current position in all portfolios");			
					refreshTxns(-1);
					updateValues();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
					
		
		btnAddStock = (TextView) view.findViewById(R.id.btn_add_stock_to_port);
		btnAddStock.setOnClickListener(this);
		
		buildQuickActionItem();

		return view;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_add_stock_to_port) {
			String text = btnAddStock.getText().toString();
			List<PortfolioObject> portList = DbAdapter.getSingleInstance().fetchPortfolioList();
			if (text.equals(res.getString(R.string.add_stock_to_port))) {
				if (portList.isEmpty())
					noPortOrWatchlistAlert(true);
				else
					addToPortfolio(portList, so);
			} else if (text.equals(res.getString(R.string.add_new_txn))) {
				if (portList.isEmpty())
					noPortOrWatchlistAlert(true);
				else {
					Intent i = new Intent(getActivity(), AddNewTrade.class);
		    		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		    		i.putExtra(Constants.KEY_STOCK_ID, so.getId());
		    		i.putExtra(Constants.KEY_SYMBOL, so.getSymbol());
		    		i.putExtra(Constants.KEY_COMPANY_NAME, so.getName());
		    		i.putExtra(Constants.KEY_EXCH, so.getExch());
		    		i.putExtra(Constants.KEY_TYPE, so.getType());
		    		i.putExtra(Constants.KEY_TYPE_DISP, so.getTypeDisp());
		    		i.putExtra(Constants.KEY_EXCH_DISP, so.getExchDisp());
		    		i.putExtra(Constants.KEY_PORTFOLIO_ID, po.getId());
		    		startActivity(i);
				}
			}
		} else if (v.getId() == R.id.btn_add_transaction) {
			Intent i = new Intent(getActivity(), AddNewTrade.class);		
			i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
			i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
			i.putExtra(Constants.KEY_STOCK_ID, so.getId());
			getActivity().startActivity(i);
		}
	}
	
	private void refreshTxns(int portId) {

		List<PortfolioObject> pList = DbAdapter.getSingleInstance().fetchPortfolioList();		
		List<TransactionObject> toList = new ArrayList<TransactionObject>();
		ArrayList<TxnPortObject> tpoList = new ArrayList<TxnPortObject>();			
		
		if (portId == -1) {			
			toList = DbAdapter.getSingleInstance().fetchTransactionObjectByStockId(so.getId());

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
			
			if (!tpoList.isEmpty())
				Collections.sort(tpoList, new MyComparator());
		} else {
			for (PortfolioObject po : pList) {
				if (portId == po.getId()){
					List<TransactionObject> toListRetrieve = new ArrayList<TransactionObject>();
					toListRetrieve = DbAdapter.getSingleInstance().fetchTransactionObjectByStockIdAndPortId(so.getId(), po.getId());
					for (TransactionObject to : toListRetrieve) 
						toList.add(to);				
					
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
		
		txnPortAdapter.notifyDataSetChanged();
	}
	
	public class MyComparator implements Comparator<TxnPortObject> {
		@Override
		public int compare(TxnPortObject lhs, TxnPortObject rhs) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        	try {
				Date date1 = sdf.parse(Integer.toString(lhs.getTradeDate()));
				Date date2 = sdf.parse(Integer.toString(rhs.getTradeDate()));
				return date1.compareTo(date2);
			} catch (ParseException e) {
				e.printStackTrace();
				return 0;
			}
		}
	}
	
	private OnItemClickListener myChildItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(android.widget.AdapterView<?> parent,
				View view, int position, long id) {
			tpo = mTpoList.get(position-1); // minus 1 because of header view
			mQuickAction.show(view);
		}
	};
	
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
			getActivity().getTheme().resolveAttribute(R.attr.dialog_title_color, typedValue, true);
			
			int titleId = res.getIdentifier("alertTitle", "id", getActivity().getPackageName());
			TextView dialogTitle = (TextView) aDialog.findViewById(titleId);
			dialogTitle.setTextColor(typedValue.data); 
			
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
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
		mQuickAction = new GridQuickAction(getActivity(), isNumColumnThree, items.size());

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
	
	private void editSingleTransactionIntent() {
		String txnObjType = tpo.getType();
		if (txnObjType.equals(TxnPortObject.CASH_DEPOSIT_TYPE) || 
				txnObjType.equals(TxnPortObject.CASH_WITHDRAWAL_TYPE)) {
			CashPosObject cpo = DbAdapter.getSingleInstance().fetchCashPosObjectByIdAndPortId(tpo.getId(), tpo.getPortId());
			EditCashPosDialog editCashDialog = new EditCashPosDialog(cpo);                 		
			editCashDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_EDIT_CASH_TXN_S);
		} else {
			Intent i = new Intent(getActivity(), AddNewTrade.class);
			i.putExtra(Constants.KEY_FROM, Constants.FROM_EDIT_TRANSACTION);
			i.putExtra(Constants.KEY_TRANSACITON_TXN_ID, tpo.getId());
			i.putExtra(Constants.KEY_PORTFOLIO_ID, tpo.getPortId());
			i.putExtra(Constants.KEY_STOCK_ID, tpo.getStockId());
			startActivity(i);
		}
	}
	
	private void showAddAlert() {
		String symbol = tpo.getSymbol();

		Intent i = new Intent(getActivity(), AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		startActivity(i);
	}
	

	private void showDeleteDialog() {

		String msg = res.getString(R.string.delete_txn_msg);
		TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(typedValue.resourceId);
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
					Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
					WidgetUtils.updateWidget(getActivity().getApplicationContext());
					refreshTxns(mPortId);
					sendBroadastUpdatePagerIndicator();
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
			getActivity().getTheme().resolveAttribute(R.attr.dialog_title_color, typedValue, true);
			
			int titleId = res.getIdentifier("alertTitle", "id", getActivity().getPackageName());
			TextView dialogTitle = (TextView) aDialog.findViewById(titleId);
			dialogTitle.setTextColor(typedValue.data); 
			
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendBroadastUpdatePagerIndicator() {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		getActivity().sendBroadcast(i);
	}
	
	protected void addToPortfolio(List<PortfolioObject> list, final StockObject stockObj) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(res.getString(R.string.title_add_to));

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
				PortfolioObject mPo = DbAdapter.getSingleInstance().fetchPortfolioByName(portfName);
				portfId = mPo.getId();

				String symbol = stockObj.getSymbol();
				String name = stockObj.getName();
				String exch = stockObj.getExch();
				String type = stockObj.getType();
				String typeDisp = stockObj.getTypeDisp();
				String exchDisp = stockObj.getExchDisp();

				// 2. to get id by symbol;
				// 2.1 if symbol no exist insert one, else get id by symbol;
				StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);

				if (mSo != null) {
					stockId = mSo.getId();
				} else {

					mSo = new StockObject();
					mSo.setSymbol(symbol);
					mSo.setName(name);
					mSo.setExch(exch);
					mSo.setType(type);
					mSo.setTypeDisp(typeDisp);
					mSo.setExchDisp(exchDisp);
					mSo.insert();
					mSo = new StockObject();
					mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(symbol);
					stockId = mSo.getId();
				}
				
				mStockId = stockId;
				so = mSo;
				po = mPo;
				
				// 3. Add QuoteObject/Check Currency
				String currency = "";
				boolean isInsert = false;
				QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
				if (qo == null) {
					UpdateQuoteTaskSingleSymbol updateQuote = new UpdateQuoteTaskSingleSymbol(getActivity(), null, portfId, stockId);
					updateQuote.execute(symbol);
				} else {
					currency = qo.getCurrency();
				
					// 3. Add PortfolioStockObject
					PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portfId, stockId);
					PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portfId);
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

				if (isInsert) {
					// sendBroadcast when only PortfolioStockObject did
					// insert.
					myNotifyDatasetChange(true, symbol);
					Toast.makeText(getActivity(), res.getString(R.string.stock_added), Toast.LENGTH_SHORT).show();
					startAddTrade(portfId, stockId);
				}
				
				// 4. add news alert
				MyAlarmManager alarmManager = new MyAlarmManager(getActivity());
				alarmManager.addNewsAlert(symbol);
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
			getActivity().getTheme().resolveAttribute(R.attr.dialog_title_color, typedValue, true);
			
			int titleId = res.getIdentifier("alertTitle", "id", getActivity().getPackageName());
			TextView dialogTitle = (TextView) aDialog.findViewById(titleId);
			dialogTitle.setTextColor(typedValue.data); 
			
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
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
		Intent i = null;

		if (!isUpdatePorfolioList) {
			i = new Intent(Constants.ACTION_WATCHLIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		} else {
			i = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		}
		getActivity().sendBroadcast(i);
		
		refreshAdapter();
		refreshTxns(-1);
		updateValues();
	}
	
	private String getPercentageGainLoss(double dTotalValue, double dNetProfit) {
		int numberOfDecimals = Constants.NUMBER_OF_DECIMALS;
		String sPercentageGainLoss = "0.00";
		double percentageGainLoss = (dNetProfit/dTotalValue)*100;
		if (!Double.isNaN(percentageGainLoss) && dTotalValue != 0.0)
			sPercentageGainLoss = DecimalsConverter.convertToStringValueBaseOnLocale(percentageGainLoss, numberOfDecimals, getActivity());
		if (dNetProfit < 0)
			sPercentageGainLoss = "-" + sPercentageGainLoss.substring(2) + "%";
		else if (dNetProfit > 0)
			sPercentageGainLoss = "+" + sPercentageGainLoss.substring(1) + "%";
		else 
			sPercentageGainLoss = sPercentageGainLoss.substring(1) + "%";
		
		return sPercentageGainLoss;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerBroadcast();
		
		mStockId = sp.getInt(Constants.SP_KEY_STOCK_ID, -1);
		if (mStockId != -1)
			so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
		else {
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
		}
		
		refreshAdapter();
		refreshTxns(-1);
		updateValues();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}
	
	private void watchlistOrPortfolioSetup(Bundle mBundle) {
		int mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
		so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
	}

	private void findStockSetup(Bundle fBundle) {
		String mSymbol = fBundle.getString(Constants.KEY_SYMBOL);
		String mCompanyName = fBundle.getString(Constants.KEY_COMPANY_NAME);
		String mExch = fBundle.getString(Constants.KEY_EXCH);
		String mType = fBundle.getString(Constants.KEY_TYPE);
		String mTypeDisp = fBundle.getString(Constants.KEY_TYPE_DISP);
		String mExchDisp = fBundle.getString(Constants.KEY_EXCH_DISP);
		so = new StockObject();
		so.setSymbol(mSymbol);
		so.setName(mCompanyName);
		so.setExch(mExch);
		so.setExchDisp(mExchDisp);
		so.setType(mType);
		so.setTypeDisp(mTypeDisp);
	}
	
	
	private void refreshAdapter() {
		portObjList = new ArrayList<PortfolioObject>();
		List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockList();
		for (PortfolioStockObject pso : psoList) {
			if (pso.getStockId() == so.getId()) {
				PortfolioObject po = new PortfolioObject();
				po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(pso.getPortfolioId());
				portObjList.add(po);
			}
		}
		
		if (portObjList != null) {
			if (portObjList.size() > 1) {
				spFilterPort.setVisibility(View.VISIBLE);
				portList.clear();
				for (PortfolioObject portObj : portObjList)
					portList.add(portObj.getName());
				
				portList.add(allPortfolios);
				spFilterPort.setSelection(portList.indexOf(allPortfolios));
			} else if (portObjList.size() == 1) {
				po = portObjList.get(0);
				spFilterPort.setVisibility(View.GONE);
			} else
				spFilterPort.setVisibility(View.GONE);
		}
		
		if (portAdapter != null)
			portAdapter.notifyDataSetChanged();
		
		if (po != null) {
			tvOPTitle.setText("Current position in " + po.getName().toUpperCase());			
		} else { 
			tvOPTitle.setText("Current position in all portfolios");			
		}	
		
	}
	
	private void updateValues() {
		
		double dTotCost = 0, dGainLoss = 0, dTotalValue = 0, dTotLongQty = 0,
				dTotShortQty = 0, dAvgPrice = 0;
		String sAvgPrice = "", sTotalValue = "", sROI = "", sTotQty = "";
		boolean bHasTxn = false;
		int count = 0;

		for (PortfolioObject po : portObjList) {
			
			SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(po.getId(), so.getId(), getActivity());
			
			dGainLoss += std.getDoubleNetProfit();
			dTotCost += std.getDoubleTotalCost();
			dTotalValue += std.getDoubleTotalValue();
			dTotLongQty += std.getQuantity();	
			dTotShortQty += std.getShortQty();
			sAvgPrice = std.getAvgPrice(); 
			if (sAvgPrice.contains("$"))
				sAvgPrice = sAvgPrice.replace("$", "");
			dAvgPrice += DecimalsConverter.convertToDoubleValueBaseOnLocale(sAvgPrice, getActivity());
			if (Double.isNaN(dAvgPrice))
				dAvgPrice = 0d;
			
			if (dAvgPrice != 0d)
				count++;
			
			if (std.getHasTxn())
				bHasTxn = true;
		}

		if (portObjList == null || portObjList.size() == 0) {
			tvNoInfoFound.setText(res.getString(R.string.stock_not_found_in_port_text));
			btnAddStock.setText(res.getString(R.string.add_stock_to_port));
		} else if (!bHasTxn) {
			tvNoInfoFound.setText(res.getString(R.string.no_txns_for_stock_text));
			btnAddStock.setText(res.getString(R.string.add_new_txn));
		} else {
			
			if (count != 0) {
				dAvgPrice = dAvgPrice/count;
				sAvgPrice = DecimalsConverter.convertToStringValueBaseOnLocale(dAvgPrice, Constants.NUMBER_OF_DECIMALS, getActivity());
			} else {
				sAvgPrice = "-";
			}
			
			sTotalValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotalValue, Constants.NUMBER_OF_DECIMALS, getActivity());
			if (dTotLongQty != 0) {
				sTotQty = DecimalsConverter.convertToStringValueBaseOnLocale(dTotLongQty, 0, getActivity());
				sTotQty = "LONG " + sTotQty;
			} else if (dTotShortQty != 0) {
				sTotQty = DecimalsConverter.convertToStringValueBaseOnLocale(dTotShortQty, 0, getActivity());
				sTotQty = "SHORT " + sTotQty;
			} else
				sTotQty = "CLOSED";
			
			if (sTotQty.contains("$"))
				sTotQty = sTotQty.replace("$", "");
			
			sROI = getPercentageGainLoss(dTotCost, DecimalsConverter.convertToDoubleValue(dGainLoss, Constants.NUMBER_OF_DECIMALS));
		
			if (sROI.contains("-"))
				sROI = sROI.replace("-", "");
			if (sROI.contains("$"))
				sROI = sROI.replace("$", "");
			sROI += "\nRETURN ON INVESTMENT";
			SpannableString spannableROI = new SpannableString(sROI);
			spannableROI.setSpan(new RelativeSizeSpan(0.7f), sROI.indexOf("%"), sROI.indexOf("%")+1, 0);
			spannableROI.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sROI.indexOf("%")+1, 0);
			spannableROI.setSpan(new RelativeSizeSpan(0.55f), sROI.indexOf("%")+1, sROI.length(), 0);
			
			tvNVValue.setText(sTotalValue);
			tvAPValue.setText(sAvgPrice);	
			tvNPValue.setText(sTotQty);
		}
	}
	
	private void updateSingleValue() {
		
		double dTotCost = 0, dGainLoss = 0, dTotalValue = 0, dTotLongQty = 0,
				dTotShortQty = 0;
		String sAvgPrice = "", sTotalValue = "", sROI = "", sTotQty = "";
		boolean bHasTxn = false;
			
		SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(po.getId(), so.getId(), getActivity());
		
		dGainLoss = std.getDoubleNetProfit();
		dTotCost = std.getDoubleTotalCost();
		dTotalValue = std.getDoubleTotalValue();
		dTotLongQty = std.getQuantity();	
		dTotShortQty += std.getShortQty();
		sAvgPrice = std.getAvgPrice(); 
		
		if (std.getHasTxn())
			bHasTxn = true;
		
		
		if (!bHasTxn) {
			tvNoInfoFound.setText(res.getString(R.string.no_txns_for_stock_text));
			btnAddStock.setText(res.getString(R.string.add_new_txn));
		} else {
			
			sTotalValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotalValue, Constants.NUMBER_OF_DECIMALS, getActivity());
			if (dTotLongQty != 0) {
				sTotQty = DecimalsConverter.convertToStringValueBaseOnLocale(dTotLongQty, 0, getActivity());
				sTotQty = "LONG " + sTotQty;
			} else if (dTotShortQty != 0) {
				sTotQty = DecimalsConverter.convertToStringValueBaseOnLocale(dTotShortQty, 0, getActivity());
				sTotQty = "SHORT " + sTotQty;
			} else
				sTotQty = "CLOSED";
			
			if (sTotQty.contains("$"))
				sTotQty = sTotQty.replace("$", "");
			
			sROI = getPercentageGainLoss(dTotCost, DecimalsConverter.convertToDoubleValue(dGainLoss, Constants.NUMBER_OF_DECIMALS));
		
			if (sROI.contains("-"))
				sROI = sROI.replace("-", "");
			if (sROI.contains("$"))
				sROI = sROI.replace("$", "");
			sROI += "\nRETURN ON INVESTMENT";
			SpannableString spannableROI = new SpannableString(sROI);
			spannableROI.setSpan(new RelativeSizeSpan(0.7f), sROI.indexOf("%"), sROI.indexOf("%")+1, 0);
			spannableROI.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sROI.indexOf("%")+1, 0);
			spannableROI.setSpan(new RelativeSizeSpan(0.55f), sROI.indexOf("%")+1, sROI.length(), 0);
			
			tvNVValue.setText(sTotalValue);
			tvAPValue.setText(sAvgPrice);	
			tvNPValue.setText(sTotQty);
		}
	}
	
	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_SUMMARY_FRAGMENT);
		receiver = new MyBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			String action = intent.getAction();
			Bundle bundleIntent = intent.getExtras();

			if (action.equals(Constants.ACTION_SUMMARY_FRAGMENT) && getActivity() != null) {

				if (bundleIntent != null) {
					int portId = bundleIntent.getInt(Constants.KEY_PORTFOLIO_ID);
					if (portId != 0)
						mPortId = portId;
					String mSymbol = bundleIntent.getString(Constants.KEY_SYMBOL);
					String _symbol = "";
					if (so != null) {
						_symbol = so.getSymbol();
						if (mSymbol != null && mSymbol.length() > 0 && !mSymbol.equals(_symbol)) {
							so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
							if (so != null) {
								refreshAdapter();
								refreshTxns(-1);
								updateValues();
							}
						}
					}

				}
			}
		}
		
	}
	
	public static MyTradesFragment newInstance() {
		MyTradesFragment myTradesFragment = new MyTradesFragment();
		return myTradesFragment;
	}

}
