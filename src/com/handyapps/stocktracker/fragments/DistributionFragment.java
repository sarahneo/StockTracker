package com.handyapps.stocktracker.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;
import net.londatiga.android.GridQuickAction.OnActionItemClickListener;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.CheckBox;

import afzkl.development.colorpickerview.dialog.ColorDialogPickerFragment.ColorDialogCallbacks;
import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;
import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewTrade;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionsActivity;
import com.handyapps.stocktracker.adapter.DistributionAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.DistributionObject;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.SingleTickerDataset;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionManager;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.MyComparatorUtils;

public class DistributionFragment extends Fragment implements OnClickListener, 
				ColorDialogCallbacks, OnActionItemClickListener  {
		
	private SharedPreferences sp;
	private Resources res;
	private MainWatchBroadcastReceiver receiver;
	private LinearLayout touchForTV;
	private TableLayout tableLayout;
	private TableRow firstRow;
	private TextView tvTotValue;
	private TextView tvValueDisplay;
	private TextView tvDateToday;
	private DistributionAdapter da;
	private CheckBox cb;
	private PieGraph pg;
	private GridQuickAction mQuickAction;
	private GridQuickAction mQuickActionCash;
	
	private String strViewTxns;
	private String strViewStock;
	private String strNews;
	private String strAddTrade;
	private String strChangeColor;
	
	private int defaultCashColor;
	private int mCashColor;
	private int mSelectedColor = Color.WHITE;
	private int mPortId;
	private boolean isIncludeCash;
	private double dTotValue;
	private String sTotValue;
	private ArrayList<DistributionObject> doList;
	private DistributionObject currDO;
	private View currView;
	private int currInt;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getActivity().getResources();
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	public static DistributionFragment newInstance() {
		DistributionFragment fragment = new DistributionFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.distribution_fragment, container, false);
		
		defaultCashColor = res.getColor(R.color.orange);
		mCashColor = sp.getInt(Constants.SP_KEY_COLOR_CASH, defaultCashColor);
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		isIncludeCash = sp.getBoolean(Constants.SP_KEY_INCLUDE_CASH, true);
		
		strViewStock = res.getString(R.string.view_stock_details);
		strViewTxns = res.getString(R.string.view_transactions);
		strNews = res.getString(R.string.latest_news);
		strAddTrade = res.getString(R.string.add_trade);
		strChangeColor = res.getString(R.string.change_color);	
		buildQuickActionItem();
		
		tvTotValue = (TextView) view.findViewById(R.id.tv_label);
		tvValueDisplay = (TextView) view.findViewById(R.id.tv_value);
		tvDateToday = (TextView) view.findViewById(R.id.tv_date_today);
		touchForTV = (LinearLayout) view.findViewById(R.id.touch_for_total_value);
		tableLayout = (TableLayout) view.findViewById(R.id.table_distribution);
		firstRow = (TableRow) view.findViewById(R.id.tr_distribution);
		pg = (PieGraph)view.findViewById(R.id.piegraph);
		pg.setThickness(res.getDimensionPixelSize(R.dimen.pie_thickness));
		
		int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");
		cb = (CheckBox) view.findViewById(R.id.check_incl_cash);
		cb.setButtonDrawable(id);
		cb.setOnClickListener(this);
		
		touchForTV.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					tvTotValue.setText(res.getString(R.string.title_total_value));
					tvValueDisplay.setText(sTotValue);
					
					cb.setVisibility(View.VISIBLE);
				} 
				return true;
			}
        });
		
		DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
		Calendar cal = Calendar.getInstance();
		tvDateToday.setText(dateFormat.format(cal.getTime()));
		
		doList = new ArrayList<DistributionObject>();
		
		if (mPortId != -1)
			doList = getDOList();
		else
			doList = getAllDOList();

		return view;
	}
		
	
	private void buildQuickActionItem() {
		
		ColorDrawable cd = new ColorDrawable();
		cd.setColorFilter(mSelectedColor, Mode.MULTIPLY);

		ActionItem addItemView = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS, strViewStock, res.getDrawable(
				R.drawable.ic_action_search));
		ActionItem addItemViewTxns = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS, strViewTxns, res.getDrawable(
				R.drawable.icon_transactions));
		ActionItem addItemNews = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS, strNews, res.getDrawable(
				R.drawable.quick_action_view_news));
		ActionItem addItemAddTrade = new ActionItem(Constants.QUICK_ACTION_ID_ADD_TRADE, strAddTrade, res.getDrawable(
				R.drawable.ic_add_dark));
		ActionItem addItemChangeColor = new ActionItem(Constants.QUICK_ACTION_ID_CHANGE_COLOR, strChangeColor, res.getDrawable(
				R.drawable.ic_hue));
		

		ArrayList<ActionItem> itemsNonCash = new ArrayList<ActionItem>();
		ArrayList<ActionItem> itemsCash = new ArrayList<ActionItem>();

		itemsNonCash.add(addItemView);
		itemsNonCash.add(addItemViewTxns);
		itemsNonCash.add(addItemNews);
		itemsNonCash.add(addItemAddTrade);
		itemsNonCash.add(addItemChangeColor);
		boolean isNumColumnThree = true;
		mQuickAction = new GridQuickAction(getActivity(), isNumColumnThree, itemsNonCash.size());
		mQuickAction.setupAdapter(itemsNonCash);
		
		itemsCash.addAll(itemsNonCash);
		itemsCash.remove(addItemNews);
		mQuickActionCash = new GridQuickAction(getActivity(), isNumColumnThree, itemsCash.size());
		mQuickActionCash.setupAdapter(itemsCash);

		// setup the action item click listener
		mQuickAction.setOnActionItemClickListener(this);
		mQuickActionCash.setOnActionItemClickListener(this);
	}
	
	
	@Override
	public void onItemClick(GridQuickAction source, int pos, int actionId) {
		int mActionId = actionId;
		switch (mActionId) {

		case Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS:
			startViewStock();
			break;
			
		case Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS:
			showViewTxns();
			break;

		case Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS:
			startViewNews();
			break;

		case Constants.QUICK_ACTION_ID_ADD_TRADE:
			showAddTrade();
			break;
		case Constants.QUICK_ACTION_ID_CHANGE_COLOR:
			onClickColorPickerDialog(currDO, currView, currInt);
			break;
		}
	}
	
	
	private void startViewStock() {

		int stockId = currDO.getStockId();
		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
		String symbol = so.getSymbol();
		String companyName = so.getName();
		String exch = so.getExch();
		String type = so.getType();
		String typeDisp = so.getTypeDisp();
		String exchDisp = so.getExchDisp();

		sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
		Intent i = new Intent(getActivity(), TransactionDetailsFragmentActivity.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		getActivity().startActivity(i);
	}
	
	
	private void showViewTxns() {
		
		Intent i = new Intent(getActivity(), TransactionsActivity.class);
		
		if (currDO.getType().equals(DistributionObject.STOCK_TYPE)) {
			String symbol = currDO.getSymbol();
			i.putExtra(Constants.KEY_FILTER_BY_SYMBOL, symbol);
			i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_TRADES);
		} else {
			i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_CASH);
		}
		getActivity().startActivity(i);
	}
	
	
	private void startViewNews() {

		int stockId = currDO.getStockId();
		StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
		String symbol = so.getSymbol();
		String companyName = so.getName();
		String exch = so.getExch();
		String type = so.getType();
		String typeDisp = so.getTypeDisp();
		String exchDisp = so.getExchDisp();

		sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
		Intent i = new Intent(getActivity(), TransactionDetailsFragmentActivity.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_WATCH_LIST);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		i.putExtra(Constants.KEY_TAB_POSITION, Constants.TO_NEWS_FRAGMENT);
		getActivity().startActivity(i);
	}
	
	
	private void showAddTrade() {
		int stockId = currDO.getStockId();
		int portId = currDO.getPortId();

		Intent i = new Intent(getActivity(), AddNewTrade.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, currDO.getSymbol());
		getActivity().startActivity(i);
	}
	
	private ArrayList<DistributionObject> getDOList() {
		doList = new ArrayList<DistributionObject>();
		double dTotPortValue = 0;
		
		List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(mPortId);
		
		for (PortfolioStockObject pso : pList) {
			DistributionObject distObj = new DistributionObject();
			int stockId = pso.getStockId();
			StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
			SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(mPortId, stockId, getActivity());
			dTotPortValue += std.getDoubleTotalValue();
			
			if (std.getDoubleTotalValue() == 0.0)
				;
			else {
				distObj.setName(so.getName());
				distObj.setSymbol(so.getSymbol());
				distObj.setPortId(mPortId);
				distObj.setStockId(stockId);
				distObj.setType(DistributionObject.STOCK_TYPE);
				distObj.setValue(std.getTotalValue());
				distObj.setDValue(std.getDoubleTotalValue());
				distObj.setColor(so.getColorCode());
				doList.add(distObj);
			}
		}
		
		if (isIncludeCash) {
			List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(mPortId);
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(mPortId);
			double totCashValue = 0;
			try {
				totCashValue = Double.parseDouble(po.getInitialCash());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			for (CashPosObject cpo : cpoList) {
				double dAmt = Double.parseDouble(cpo.getAmount());
				String type = cpo.getTxnType();
				if (type.equals(CashPosObject.CASH_DEPOSIT_TYPE))
					totCashValue += dAmt;
				else
					totCashValue -= dAmt;
			}
			
			dTotPortValue += totCashValue;
			String sCashValue = DecimalsConverter.convertToStringValueBaseOnLocale(totCashValue, Constants.NUMBER_OF_DECIMALS, getActivity());
			DistributionObject distObj = new DistributionObject();		
			distObj.setName("Cash");
			distObj.setPortId(mPortId);
			distObj.setType(DistributionObject.CASH_TYPE);
			distObj.setValue(sCashValue);
			distObj.setDValue(totCashValue);
			distObj.setColor(mCashColor);
			doList.add(distObj);
		}
		
		
		dTotValue = dTotPortValue;
		sTotValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotPortValue, Constants.NUMBER_OF_DECIMALS, getActivity());
		tvValueDisplay.setText(sTotValue);
		
		//calculate percentages
		for (DistributionObject dObj : doList) {			
			dObj.setPercent(getPercentage(dTotPortValue, dObj.getDValue()));
		}
		
		return doList;
	}
	
	
	private ArrayList<DistributionObject> getAllDOList() {
		doList = new ArrayList<DistributionObject>();
		double dTotPortValue = 0;
		double totCashValue = 0;
		
		List<PortfolioObject> poList = DbAdapter.getSingleInstance().fetchPortfolioList();
		
		for (PortfolioObject portObj : poList) {
			List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portObj.getId());
			
			for (PortfolioStockObject pso : pList) {
				DistributionObject distObj = new DistributionObject();
				int stockId = pso.getStockId();
				StockObject so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
				SingleTickerDataset std = TransactionManager.getSingleStockProfitReport(portObj.getId(), stockId, getActivity());
				dTotPortValue += std.getDoubleTotalValue();
				
				if (std.getDoubleTotalValue() == 0.0)
					;
				else {
					distObj.setName(so.getName());
					distObj.setSymbol(so.getSymbol());
					distObj.setPortId(portObj.getId());
					distObj.setStockId(stockId);
					distObj.setType(DistributionObject.STOCK_TYPE);
					distObj.setValue(std.getTotalValue());
					distObj.setDValue(std.getDoubleTotalValue());
					distObj.setColor(so.getColorCode());
					//distObj.setPercent(percent);
					
					doList.add(distObj);
				}
			}
			
			try {
				totCashValue += Double.parseDouble(portObj.getInitialCash());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
		}
		
		if (isIncludeCash) {
			List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosList();
			
			for (CashPosObject cpo : cpoList) {
				double dAmt = Double.parseDouble(cpo.getAmount());
				String type = cpo.getTxnType();
				if (type.equals(CashPosObject.CASH_DEPOSIT_TYPE))
					totCashValue += dAmt;
				else
					totCashValue -= dAmt;
			}
			
			dTotPortValue += totCashValue;
			//dCashValue = totCashValue;
			String sCashValue = DecimalsConverter.convertToStringValueBaseOnLocale(totCashValue, Constants.NUMBER_OF_DECIMALS, getActivity());
			
			DistributionObject distObj = new DistributionObject();		
			distObj.setName("Cash");
			//distObj.setPortId(mPortId);
			distObj.setType(DistributionObject.CASH_TYPE);
			distObj.setValue(sCashValue);
			distObj.setDValue(totCashValue);
			//distObj.setPercent(getPercentage(dTotPortValue, totCashValue));
			distObj.setColor(mCashColor);
			doList.add(distObj);
		}
				
		
		dTotValue = dTotPortValue;
		sTotValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotPortValue, Constants.NUMBER_OF_DECIMALS, getActivity());
		tvValueDisplay.setText(sTotValue);
		
		//calculate percentages
		for (DistributionObject dObj : doList) {
			dObj.setPercent(getPercentage(dTotPortValue, dObj.getDValue()));
		}
		
		return doList;
	}
		
	
	private double getPercentage(double dTotalValue, double dValue) {
		int numberOfDecimals = Constants.NUMBER_OF_DECIMALS;

		double percentage = (dValue/dTotalValue)*100;
		if (!Double.isNaN(percentage))
			percentage = DecimalsConverter.convertToDoubleValue(percentage, numberOfDecimals);
		else
			percentage = 0;
		
		return percentage;
	}
	
	
	private void buildTable() {
		tvTotValue.setText(res.getString(R.string.title_total_value));
		tvValueDisplay.setText(sTotValue);
		Collections.sort(doList, new MyComparatorUtils.DistributionObjectValueComparator());
		da = new DistributionAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1, doList);
		if (tableLayout.getChildCount() != 0) {
			tableLayout.removeAllViews();
			tableLayout.addView(firstRow);
		}
		
		for (int i = 0; i < da.getCount(); i++) {
			DistributionObject distObj = (DistributionObject) da.getItem(i);
			
			View item = da.getView(i, null, null);
			tableLayout.addView(item);

			if (distObj.getName().equals(res.getString(R.string.no_stock_added)))
				;
			else {
				item.setTag(i);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						currInt = (Integer) v.getTag();
						DistributionObject distObj = doList.get(currInt);
						currDO = distObj;
						String name = distObj.getName();
						tvTotValue.setText(name);	
						tvValueDisplay.setText(doList.get(currInt).getValue());
						if (!currDO.getName().equals("Cash"))
							mQuickAction.show(v);
						else
							mQuickActionCash.show(v);
						if (name.equals(res.getString(R.string.title_total_value)))
							cb.setVisibility(View.VISIBLE);
						else
							cb.setVisibility(View.GONE);
						currView = v;
					}
				});
			}
		}
	}
	
	
	public void onClickColorPickerDialog(DistributionObject distObj, final View v, final int currInt) {
		
		final int stockId = distObj.getStockId();		

		final ColorPickerDialog colorDialog = new ColorPickerDialog(getActivity(), distObj.getColor());

		colorDialog.setAlphaSliderVisible(true);
		colorDialog.setTitle("Pick a Color!");

		colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSelectedColor = colorDialog.getColor();
				if (stockId > 0) {
					StockObject stockObj = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
					stockObj.setColorCode(mSelectedColor);
					stockObj.update();		
				}
				
				// Set color of imageView in table row
				ImageView ivColor = (ImageView) ((LinearLayout) v).getChildAt(0);
				GradientDrawable bgShape = (GradientDrawable)ivColor.getBackground();
				bgShape.setColor(mSelectedColor);	
				
				// Set color of slice
				PieSlice ps = pg.getSlice(currInt);
				ps.setColor(mSelectedColor); 
				pg.invalidate();
				
				// Check if 'cash' was selected
				TextView tvCash = (TextView) ((LinearLayout) v).getChildAt(1);
				if (tvCash.getText().equals("Cash")) {
					mCashColor = colorDialog.getColor();
					sp.edit().putInt(Constants.SP_KEY_COLOR_CASH, mCashColor).commit();
				}
			}
		});

		colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Nothing to do here.
			}
		});

		colorDialog.show();
	}

	
	private void buildChart(ArrayList<DistributionObject> distObjList) {
		if (pg != null)
			pg.removeSlices();
		for (DistributionObject distObj : distObjList) {
			PieSlice slice = new PieSlice();
			slice.setColor(distObj.getColor());	
			slice.setValue( Math.round(distObj.getDValue()) );
			pg.addSlice(slice);
		}
		
		pg.setOnSliceClickedListener(new OnSliceClickedListener(){

			@Override
			public void onClick(int index) {
				String name = doList.get(index).getName();
				tvTotValue.setText(name);
				tvValueDisplay.setText(doList.get(index).getValue());
				if (tvTotValue.getText().equals(res.getString(R.string.title_total_value)))
					cb.setVisibility(View.VISIBLE);
				else
					cb.setVisibility(View.GONE);
			}
			
		});
	}
	
	
	public void onClick(View view) {
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.check_incl_cash:
	        	String sTotValue = "";
	            if (checked) {
	            	boolean hasCash = false;
	            	isIncludeCash = true;
	            	for (DistributionObject distObj : doList) {
	            		if (distObj.getName().equals("Cash")) {
	            			hasCash = true;
	            			break;
	            		}
	            	}
	            	if (!hasCash) {
	            		if (mPortId == -1)
	            			doList = getAllDOList();
	            		else
	            			doList = getDOList();
	            		buildTable();
	            		buildChart(doList);
	            	}
	            } else {
	            	isIncludeCash = false;
	            	for (DistributionObject distObj : doList) {
	            		if (distObj.getName().equals("Cash")) {
	            			if (mPortId == -1)
		            			doList = getAllDOList();
		            		else
		            			doList = getDOList();
		            		buildTable();
		            		buildChart(doList);
	            			break;
	            		}
	            	}
	            }
	            sTotValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotValue, Constants.NUMBER_OF_DECIMALS, getActivity());	
	            tvValueDisplay.setText(sTotValue);
	            break;
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
					
					if (isFilterPortList || isUpdatePort) {
						mPortId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
							
						if (mPortId == -1)
							doList = getAllDOList();
						else
							doList = getDOList();

						buildTable();
						buildChart(doList);
					} 
				}
			}
		}
	}
	
	
	@Override
	public void OnColorSelected(int color) {
		mSelectedColor = color;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		registerBroadcast();
		isIncludeCash = sp.getBoolean(Constants.SP_KEY_INCLUDE_CASH, true);
		mCashColor = sp.getInt(Constants.SP_KEY_COLOR_CASH, defaultCashColor);
		cb.setChecked(isIncludeCash);
		buildTable();
		buildChart(doList);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		sp.edit().putBoolean(Constants.SP_KEY_INCLUDE_CASH, isIncludeCash).commit();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	} 
	

}
