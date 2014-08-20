package com.handyapps.stocktracker.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.RadioButton;
import org.holoeverywhere.widget.Toast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.MainFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionsActivity;
import com.handyapps.stocktracker.adapter.PerformanceAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment;
import com.handyapps.stocktracker.dialogs.SortByDialog;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PerformanceObject;
import com.handyapps.stocktracker.model.PortfolioManager;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.SingleTickerDataset;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionManager;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class PerformanceFragment extends Fragment implements OnClickListener, OnFocusChangeListener,
				TextWatcher {
	
	private static final String EXTRA_INT = "extra_int";
	private static final int CALCULATOR_DIALOG_ID = 15;
	
	private SharedPreferences sp;
	private Resources res;
	private MainWatchBroadcastReceiver receiver;
	private ListView lv;
	private GridQuickAction mQuickAction;
	
	private String strEditPortfolio;
	private String strViewDistribution;
	private String strViewTxns;
	private String strViewStocks;
	
	private int mPortId;
	private int mCurrentSpinnerIndex = 0;
	private boolean isPortfoliosChecked;
	private ArrayList<PerformanceObject> poList;
	private ArrayList<PerformanceObject> originalList;
	private PerformanceAdapter pa;
	private PerformanceObject po;
	
	//viewgroups
	private RadioGroup radioGrp;
	private LinearLayout layoutSearch;
	
	//widgets
	private RadioButton mRBPort;
	private RadioButton mRBStocks;
	private ImageButton mBtnSortBy;
	private ImageButton mBtnSearch;
	private ImageButton mBtnClose;
	private EditText mSearch;
	private EditText etCash;
	private AlertDialog aDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getActivity().getResources();
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		strEditPortfolio = res.getString(R.string.edit_portfolio);
		strViewDistribution = res.getString(R.string.view_distribution);
		strViewTxns = res.getString(R.string.view_transactions);
		strViewStocks = res.getString(R.string.view_stock_details);
	}

	public static PerformanceFragment newInstance() {
		PerformanceFragment fragment = new PerformanceFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.performance_fragment, container, false);
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		isPortfoliosChecked = sp.getBoolean(Constants.SP_KEY_IS_PORTFOLIOS_CHECKED, false);
				
		mRBPort = (RadioButton) view.findViewById(R.id.radio_portfolio);
		mRBStocks = (RadioButton) view.findViewById(R.id.radio_stocks);
		mRBPort.setOnClickListener(this);
		mRBStocks.setOnClickListener(this);
		
		mBtnSortBy = (ImageButton) view.findViewById(R.id.btn_sort_by);
		mBtnSortBy.setOnClickListener(this);
		mBtnSearch = (ImageButton) view.findViewById(R.id.btn_filter);
		mBtnSearch.setOnClickListener(this);
		mBtnClose = (ImageButton) view.findViewById(R.id.ib_close_keyboard);
		mBtnClose.setOnClickListener(this);
		
		poList = new ArrayList<PerformanceObject>();
		originalList = new ArrayList<PerformanceObject>();
		if (!isPortfoliosChecked) {
			if (mPortId != -1)
				poList = getPOList(mPortId);
			else
				poList = getAllPOList();
		} else {
			if (mPortId != -1) {
				poList = getSinglePerformanceSummary();
			} else {
				poList = getAllPerformanceSummary();
			}
		}
		
		lv = (ListView) view.findViewById(R.id.lv_performance);
		originalList.addAll(poList);
		pa = new PerformanceAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, 
				android.R.id.text1, poList, originalList);
		lv.setAdapter(pa);
		lv.setOnItemClickListener(new OnItemClickListener() {
			 
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				po = (PerformanceObject) lv.getItemAtPosition(position);
				if (po.getType() == null) {
					;
				} else if (po.getType().equals(PerformanceObject.STOCK_TYPE)) {
					/*String symbol = currPO.getSymbol();
					i.putExtra(Constants.KEY_FILTER_BY_SYMBOL, symbol);
					i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_TRADES);*/

					int stockId = po.getStockId();

					Intent i = new Intent(getActivity(), TransactionDetailsFragmentActivity.class);
					i.putExtra(Constants.KEY_TAB_POSITION, TransactionDetailsFragmentActivity.TAB_POSITION_TRADES);
					i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
					i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
					i.putExtra(Constants.KEY_STOCK_ID, stockId);
					sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
					getActivity().startActivity(i);
					
				} else {
					/*Intent i = new Intent(getActivity(), TransactionsActivity.class);
					int portId = currPO.getPortId();
					i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, portId);
					i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_ALL);
					getActivity().startActivity(i);*/
					mQuickAction.show(view);
				}
			}
        });
		
		buildQuickActionItem();
		
		layoutSearch = (LinearLayout) view.findViewById(R.id.layout_et_filter);
		mSearch = (EditText) view.findViewById(R.id.et_filter);
		mSearch.setOnFocusChangeListener(this);
		radioGrp = (RadioGroup) view.findViewById(R.id.radioGrp_performance);
		layoutSearch.setVisibility(View.GONE);

		return view;
	}
	
	
	private void buildQuickActionItem() {

		ActionItem addItemEditPortfolio = new ActionItem(Constants.QUICK_ACTION_ID_EDIT, strEditPortfolio, 
				res.getDrawable(R.drawable.ic_action_edit));
		ActionItem addItemViewDist = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_DISTRIBUTION, strViewDistribution, res.getDrawable(
				R.drawable.quick_action_view_distribution));
		ActionItem addItemViewTxns = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS, strViewTxns, 
				res.getDrawable(R.drawable.icon_transactions));
		ActionItem addItemViewStocks = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS, strViewStocks, 
				res.getDrawable(R.drawable.ic_action_search));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();
		items.add(addItemEditPortfolio);
		items.add(addItemViewDist);
		items.add(addItemViewTxns);
		items.add(addItemViewStocks);

		boolean isNumColumnThree = true;
		mQuickAction = new GridQuickAction(getActivity(), isNumColumnThree, items.size());
		mQuickAction.setupAdapter(items);
		mQuickAction.setOnActionItemClickListener(new GridQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(GridQuickAction quickAction, int pos, int actionId) {

				int mActionId = actionId;

				switch (mActionId) {

				case Constants.QUICK_ACTION_ID_EDIT:
					showPortDialog();
					break;
				case Constants.QUICK_ACTION_ID_VIEW_DISTRIBUTION:
					Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
					i.putExtra(Constants.KEY_IS_CHANGE_TAB, true);
					i.putExtra(Constants.KEY_CHANGE_TAB_TO, MainFragmentActivity.TAB_POSITION_DIST);
					i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, po.getPortId());
					getActivity().sendBroadcast(i);
					
					Intent i2 = new Intent(Constants.ACTION_FILTER_PORTFOLIO);
					i2.putExtra(Constants.KEY_FILTER_PORTFOLIOS_PAGER, true);
					i2.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, po.getPortId());
					getActivity().sendBroadcast(i2);
					break;
				case Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS:
					Intent txnsIntent = new Intent(getActivity(), TransactionsActivity.class);
					txnsIntent.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, po.getPortId());
					txnsIntent.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_ALL);
					getActivity().startActivity(txnsIntent);
					break;
				case Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS:
					Intent stocksIntent = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
					stocksIntent.putExtra(Constants.KEY_FILTER_PORTFOLIOS_PAGER, true);
					stocksIntent.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, po.getPortId());
					getActivity().sendBroadcast(stocksIntent);
					
					mRBStocks.setChecked(true);
					mRBStocks.callOnClick();
					/*isPortfoliosChecked = false;
					mSearch.removeTextChangedListener(mTextWatcher);
					mSearch.setText("");
					refreshAdapter();
					mSearch.addTextChangedListener(mTextWatcher);*/
					 
					
					break;
				}
			}

		});
	}
	
	protected void showPortDialog() {
		String title = res.getString(R.string.portfolio_options);
		String[] itemsArr = { "Edit", "Delete" };

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setItems(itemsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (which == 0)
            		showEditPortDialog();
            	else if (which == 1) 
            		showDeletePortDialog();
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showDeletePortDialog() {

		String msg = res.getString(R.string.delete_portfolio_msg);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_action_warning);
		builder.setTitle(res.getString(R.string.delete_portfolio_title));
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int portId = po.getPortId();

				boolean isDeleted = PortfolioManager.deletePorfolioByPortId(portId);
				if (isDeleted) {
					Toast.makeText(getActivity(), res.getString(R.string.portfolio_deleted), Toast.LENGTH_SHORT).show();
					sendBroadastUpdatePagerIndicator();
					WidgetUtils.updateWidget(getActivity());
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showEditPortDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View view;
		String dialogTitle = "";

		view = getActivity().getLayoutInflater().inflate(R.layout.create_new_portfolio, null);
		final EditText et = (EditText) view.findViewById(R.id.et_enter_portfolio_name);
		etCash = (EditText) view.findViewById(R.id.et_enter_initial_cash);
		etCash.setVisibility(View.GONE);

		dialogTitle = res.getString(R.string.edit_portfolio);
		et.setText(po.getName());
		et.setSelection(et.length());

		et.addTextChangedListener(PerformanceFragment.this);

		builder.setTitle(dialogTitle);
		builder.setPositiveButton(res.getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String enteredName = et.getText().toString();				

				boolean isPortEdited = PortfolioManager.updatePortfolio(po.getPortId(), enteredName);
				if (isPortEdited) {
					Toast.makeText(getActivity(), res.getString(R.string.portfolio_updated), Toast.LENGTH_SHORT).show();
					WidgetUtils.updateWidget(getActivity());
					sendBroadastUpdatePagerIndicator();
				}			
			}
		});

		builder.setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setView(view);
		aDialog = builder.create();
		aDialog.show();
		// change divider color
		try {
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {

			String name = s.toString();

			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(name);
			if (po == null) {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			} else {
				aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}

		} else {
			aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}
	}
	
	private void sendBroadastUpdatePagerIndicator() {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		getActivity().sendBroadcast(i);
	}
	
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.et_filter) {
			if (hasFocus)
				showSoftKeyboard();
			else
				hideSoftKeyboard();
		}
	}
	
	@Override
	public void onClick(View v) {
		 switch(v.getId()) {
			 case R.id.btn_sort_by:
				 Bundle args = new Bundle();
				 args.putInt(Constants.KEY_CASH_TRADES_ALL, mCurrentSpinnerIndex);
				 SortByDialog dialog = new SortByDialog();  
				 dialog.setArguments(args);
				 dialog.setTargetFragment(PerformanceFragment.this, Constants.DIALOG_SORT_BY_I);
				 dialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_SORT_BY_S);
				 break;
			 case R.id.btn_filter:
				 radioGrp.setVisibility(View.GONE);
				 layoutSearch.setVisibility(View.VISIBLE);
				 mSearch.requestFocus();
				 break;
			 case R.id.ib_close_keyboard:
				 radioGrp.setVisibility(View.VISIBLE);
				 layoutSearch.setVisibility(View.GONE);
				 mSearch.setText("");
				 mSearch.clearFocus();
				 break;
			 case R.id.radio_portfolio:
				 boolean isChecked = ((RadioButton) v).isChecked();
				 if (isChecked) {
					 isPortfoliosChecked = true;
					 mSearch.removeTextChangedListener(mTextWatcher);
					 mSearch.setText("");
					 refreshAdapter();
					 mSearch.addTextChangedListener(mTextWatcher);
				 }
				 break;
			 case R.id.radio_stocks:
				 boolean isStocksChecked = ((RadioButton) v).isChecked();
				 if (isStocksChecked) {
					 isPortfoliosChecked = false;
					 mSearch.removeTextChangedListener(mTextWatcher);
					 mSearch.setText("");
					 refreshAdapter();
					 mSearch.addTextChangedListener(mTextWatcher);
				 }
				 break;
			 case R.id.et_enter_initial_cash:
				 CalculatorDialogFragment fragment = CalculatorDialogFragment.newInstance(CALCULATOR_DIALOG_ID, "0");
				 fragment.setTargetFragment(this, CALCULATOR_DIALOG_ID);
				 fragment.show(getActivity().getSupportFragmentManager());
				 break;
		 }
		
	}
	
	private ArrayList<PerformanceObject> getPOList(int portId) {
		ArrayList<PerformanceObject> tempPOList = new ArrayList<PerformanceObject>();
		
		List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
		
		PerformanceObject emptyPo = new PerformanceObject();
		emptyPo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptyPo.setValue("");
		
		for (PortfolioStockObject pso : pList) {
			PerformanceObject po = new PerformanceObject();
			int stockId = pso.getStockId();
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
			SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(portId, stockId, getActivity());
			
			if (std.getHasTxn()) {
				po.setType(PerformanceObject.STOCK_TYPE);
				po.setName(so.getName());
				po.setSymbol(so.getSymbol());
				po.setPortId(portId);
				po.setStockId(stockId);
				po.setChange(std.getPriceChange());
				po.setChangeInPercent(std.getChangeInPercent());
				po.setLastPrice(std.getLastPrice());
				po.setOverallGainLoss(DecimalsConverter.convertToDoubleValue(std.getDoubleNetProfit(), Constants.NUMBER_OF_DECIMALS));
				po.setDRealizedGainLoss(DecimalsConverter.convertToDoubleValue(std.getRealizedGain(), Constants.NUMBER_OF_DECIMALS));
				po.setDUnrealizedGainLoss(DecimalsConverter.convertToDoubleValue(std.getUnrealizedGain(), Constants.NUMBER_OF_DECIMALS));
				po.setDValue(std.getDoubleTotalValue());
				po.setPercentROI(getPercentageGainLoss(std.getDoubleTotalCost(), std.getDoubleNetProfit()));
				po.setValue(std.getTotalValue());
				po.setColorCode(so.getColorCode());
				
				tempPOList.add(po);
			}
		}
		
		if (tempPOList.isEmpty())
			tempPOList.add(emptyPo);
		
		return tempPOList;
	}
	
	
	private ArrayList<PerformanceObject> getAllPOList() {
		ArrayList<PerformanceObject> tempPOList = new ArrayList<PerformanceObject>();
		List<PortfolioObject> portObjList = DbAdapter.getSingleInstance().fetchPortfolioList();
		
		PerformanceObject emptyPo = new PerformanceObject();
		emptyPo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptyPo.setValue("");
		emptyPo.setColorCode(R.color.black);
		emptyPo.setDRealizedGainLoss(0);
		emptyPo.setDRealizedGainLoss(0);
		emptyPo.setDValue(0);
		emptyPo.setPercentROI("");
		
		for (PortfolioObject portObj : portObjList) {
			List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portObj.getId());

			for (PortfolioStockObject pso : pList) {
				PerformanceObject po = new PerformanceObject();
				int stockId = pso.getStockId();
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
				SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(portObj.getId(), stockId, getActivity());
				
				po.setType(PerformanceObject.STOCK_TYPE);
				po.setName(so.getName());
				po.setSymbol(so.getSymbol());
				po.setPortId(portObj.getId());
				po.setStockId(stockId);
				po.setChange(std.getPriceChange());
				po.setChangeInPercent(std.getChangeInPercent());
				po.setLastPrice(std.getLastPrice());
				po.setOverallGainLoss(DecimalsConverter.convertToDoubleValue(std.getDoubleNetProfit(), Constants.NUMBER_OF_DECIMALS));
				po.setDRealizedGainLoss(DecimalsConverter.convertToDoubleValue(std.getRealizedGain(), Constants.NUMBER_OF_DECIMALS));
				po.setDUnrealizedGainLoss(DecimalsConverter.convertToDoubleValue(std.getUnrealizedGain(), Constants.NUMBER_OF_DECIMALS));
				po.setDValue(std.getDoubleTotalValue());
				po.setPercentROI(getPercentageGainLoss(std.getDoubleTotalCost(), std.getDoubleNetProfit()));
				po.setValue(std.getTotalValue());
				po.setColorCode(so.getColorCode());
				tempPOList.add(po);
			}
		}
		
		if (tempPOList.isEmpty())
			tempPOList.add(emptyPo);
		
		return tempPOList;
	}
	
	
	private ArrayList<PerformanceObject> getSinglePerformanceSummary() {
		ArrayList<PerformanceObject> perfObjSummary = new ArrayList<PerformanceObject>();
		PortfolioObject portObj = DbAdapter.getSingleInstance().fetchPortfolioByPortId(mPortId);
		
		PerformanceObject emptyPo = new PerformanceObject();
		emptyPo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptyPo.setValue("");
		emptyPo.setColorCode(R.color.black);
		emptyPo.setDRealizedGainLoss(0);
		emptyPo.setDUnrealizedGainLoss(0);
		emptyPo.setDValue(0);
		emptyPo.setPercentROI("");

		double dTotCost = 0, dGainLoss = 0, dRealizedGainLoss = 0, dUnrealizedGainLoss = 0, dTotalValue = 0;

		List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(mPortId);
		PerformanceObject perfObj = new PerformanceObject();

		for (PortfolioStockObject pso : psoList) {
			
			int stockId = pso.getStockId();
			SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(mPortId, stockId, getActivity());

			dRealizedGainLoss += std.getRealizedGain();
			dUnrealizedGainLoss += std.getUnrealizedGain();
			dGainLoss += std.getDoubleNetProfit();
			dTotCost += std.getDoubleTotalCost(); 
			dTotalValue += std.getDoubleTotalValue();
		}
		
		double dInitialBalance = 0;
		try {
			dInitialBalance = Double.parseDouble(portObj.getInitialCash());
		} catch (NullPointerException e) {
			dInitialBalance = 0;
		}
		double dTotDeposits = 0, dTotWithdrawals = 0, dFinalBalance = 0;
		List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(portObj.getId());
		for (CashPosObject cpo : cpoList) {
			if (cpo.getTxnType().equals("cd"))
				dTotDeposits += Double.parseDouble(cpo.getAmount());
			else if (cpo.getTxnType().equals("cw"))
				dTotWithdrawals += Double.parseDouble(cpo.getAmount());
		}
		dFinalBalance = dInitialBalance + dTotDeposits - dTotWithdrawals;
		
		dTotalValue += dFinalBalance;
		String sTotalValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotalValue, Constants.NUMBER_OF_DECIMALS, getActivity());
		String sNetCash = DecimalsConverter.convertToStringValueBaseOnLocale(dFinalBalance, Constants.NUMBER_OF_DECIMALS, getActivity());
		
		if (psoList.isEmpty())
			perfObjSummary.add(emptyPo);
		else {
			perfObj.setType(PerformanceObject.PORTFOLIO_TYPE);
			perfObj.setName(portObj.getName());
			perfObj.setPortId(mPortId);
			perfObj.setCurrency(portObj.getCurrencyType());
			perfObj.setNetCash(sNetCash);
			//perfObj.setStockId(stockId);
			perfObj.setOverallGainLoss(DecimalsConverter.convertToDoubleValue(dGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setDUnrealizedGainLoss(DecimalsConverter.convertToDoubleValue(dUnrealizedGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setDRealizedGainLoss(DecimalsConverter.convertToDoubleValue(dRealizedGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setPercentROI(getPercentageGainLoss(dTotCost, perfObj.getOverallGainLoss()));
			perfObj.setLastPrice(sTotalValue);
			perfObj.setColorCode(res.getColor(R.color.green));
			perfObjSummary.add(perfObj);
		}
			
		
		return perfObjSummary;
	}
	
	
	private ArrayList<PerformanceObject> getAllPerformanceSummary() {
		ArrayList<PerformanceObject> perfObjSummary = new ArrayList<PerformanceObject>();
		List<PortfolioObject> pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		
		PerformanceObject emptyPo = new PerformanceObject();
		emptyPo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptyPo.setValue("");
		emptyPo.setColorCode(R.color.black);
		emptyPo.setDRealizedGainLoss(0);
		emptyPo.setDUnrealizedGainLoss(0);
		emptyPo.setDValue(0);
		emptyPo.setDValue(0);
		emptyPo.setPercentROI("");

		for (PortfolioObject po : pList) {
			double dTotCost = 0, dGainLoss = 0, dRealizedGainLoss = 0, dUnrealizedGainLoss = 0, dTotalValue = 0;

			List<PortfolioStockObject> psoList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(po.getId());
			PerformanceObject perfObj = new PerformanceObject();

			for (PortfolioStockObject pso : psoList) {
				
				int stockId = pso.getStockId();
				SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(po.getId(), stockId, getActivity());

				//dVolume += std.getQuantity();
				//numTrades += toList.size();
				dGainLoss += std.getDoubleNetProfit();
				dRealizedGainLoss += std.getRealizedGain();
				dUnrealizedGainLoss += std.getUnrealizedGain();
				dTotCost += std.getDoubleTotalCost();
				dTotalValue += std.getDoubleTotalValue();
			}
			
			double dInitialBalance = 0;
			try {
				dInitialBalance = Double.parseDouble(po.getInitialCash());
			} catch (NullPointerException e) {
				dInitialBalance = 0;
			}
			double dTotDeposits = 0, dTotWithdrawals = 0, dFinalBalance = 0;
			List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(po.getId());
			for (CashPosObject cpo : cpoList) {
				if (cpo.getTxnType().equals("cd"))
					dTotDeposits += Double.parseDouble(cpo.getAmount());
				else if (cpo.getTxnType().equals("cw"))
					dTotWithdrawals += Double.parseDouble(cpo.getAmount());
			}
			dFinalBalance = dInitialBalance + dTotDeposits - dTotWithdrawals;
			
			dTotalValue += dFinalBalance;
			String sTotalValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotalValue, Constants.NUMBER_OF_DECIMALS, getActivity());
			String sNetCash = DecimalsConverter.convertToStringValueBaseOnLocale(dFinalBalance, Constants.NUMBER_OF_DECIMALS, getActivity());
				
			perfObj.setType(PerformanceObject.PORTFOLIO_TYPE);
			perfObj.setName(po.getName());
			perfObj.setPortId(po.getId());
			//perfObj.setStockId(stockId);
			perfObj.setCurrency(po.getCurrencyType());
			perfObj.setLastPrice(sTotalValue);
			perfObj.setNetCash(sNetCash);
			perfObj.setOverallGainLoss(DecimalsConverter.convertToDoubleValue(dGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setDUnrealizedGainLoss(DecimalsConverter.convertToDoubleValue(dUnrealizedGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setDRealizedGainLoss(DecimalsConverter.convertToDoubleValue(dRealizedGainLoss, Constants.NUMBER_OF_DECIMALS));
			perfObj.setPercentROI(getPercentageGainLoss(dTotCost, perfObj.getOverallGainLoss()));
			perfObj.setColorCode(res.getColor(R.color.green));
			perfObjSummary.add(perfObj);
		}		
		
		return perfObjSummary;
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
	
	
	public class MyComparator implements Comparator<PerformanceObject> {
		@Override
		public int compare(PerformanceObject lhs, PerformanceObject rhs) {
			if (mCurrentSpinnerIndex == 0) {
				double lhsValue = 0;
				double rhsValue = 0;
				try {
					lhsValue = Double.parseDouble(lhs.getPercentROI().substring(0, lhs.getPercentROI().length()-1));
					rhsValue = Double.parseDouble(rhs.getPercentROI().substring(0, rhs.getPercentROI().length()-1));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				return Double.compare(rhsValue, lhsValue);
			} else if (mCurrentSpinnerIndex == 1)
				return Double.compare(rhs.getDRealizedGainLoss(), lhs.getDRealizedGainLoss());
			else if (mCurrentSpinnerIndex == 2)
				return Double.compare(rhs.getDUnrealizedGainLoss(), lhs.getDUnrealizedGainLoss());
			else if (mCurrentSpinnerIndex == 3)
				return Double.compare(rhs.getOverallGainLoss(), lhs.getOverallGainLoss());
			else if (mCurrentSpinnerIndex == 4)
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			return 0;
		}
	}
	
	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_FILTER_PORTFOLIO);
		filter.addAction(Constants.ACTION_MAIN_ACTIVITY);
		receiver = new MainWatchBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}
	
	private class MainWatchBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			Bundle bundle = intent.getExtras();

			if ( (intent.getAction().equals(Constants.ACTION_FILTER_PORTFOLIO) || 
					intent.getAction().equals(Constants.ACTION_MAIN_ACTIVITY))
					&& getActivity() != null) {

				if (bundle != null) {
					
					boolean isFilterPortList = bundle.getBoolean(Constants.KEY_FILTER_PORTFOLIOS_PAGER);
					boolean isUpdatePort = bundle.getBoolean(Constants.KEY_UPDATE_PORTFOLIOS_PAGER);
					
					if (isFilterPortList) {
						mPortId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
						refreshAdapter();
					} else if (isUpdatePort) {						
						refreshAdapter();
					}
				}
			}
		}
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK)
			if (requestCode == Constants.DIALOG_SORT_BY_I) {
				if (data.getExtras() != null) {
					mCurrentSpinnerIndex = data.getExtras().getInt(EXTRA_INT);
					refreshAdapter();
				}
			}
	}
	
	
	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void afterTextChanged(Editable s) {
			String search = s.toString();
			pa.getFilter().filter(search);				
		}
	};
	
	
	private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
	    
	}
	
	
	private void showSoftKeyboard(){
	    if(getActivity().getCurrentFocus()!=null && getActivity().getCurrentFocus() instanceof EditText){
	    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.showSoftInput(mSearch, InputMethodManager.SHOW_IMPLICIT);
	    }
	}
	
	
	private void refreshAdapter() {
		poList.clear();
		ArrayList<PerformanceObject> temp;
			
		if (!isPortfoliosChecked) {
			if (mPortId != -1)
				temp = getPOList(mPortId);
			else
				temp = getAllPOList();
		} else {
			if (mPortId != -1) {
				temp = getSinglePerformanceSummary();
			} else {
				temp = getAllPerformanceSummary();
			}
		}
		Collections.sort(temp, new MyComparator());
		for (PerformanceObject po : temp)
			poList.add(po);
		
		originalList.clear();
		originalList.addAll(poList);
		pa.notifyDataSetChanged();
	}

	
	@Override
	public void onResume() {
		super.onResume();
		mSearch.addTextChangedListener(mTextWatcher);
		isPortfoliosChecked = sp.getBoolean(Constants.SP_KEY_IS_PORTFOLIOS_CHECKED, true);
		if (isPortfoliosChecked) {
			mRBPort.setChecked(true);
		} else {
			mRBStocks.setChecked(true);
		}
		refreshAdapter();
		registerBroadcast();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mSearch.removeTextChangedListener(mTextWatcher);
		mSearch.setText("");
		sp.edit().putBoolean(Constants.SP_KEY_IS_PORTFOLIOS_CHECKED, isPortfoliosChecked).commit();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			
		}
	}
	

}
