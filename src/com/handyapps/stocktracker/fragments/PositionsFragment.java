package com.handyapps.stocktracker.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ExpandableListView;
import org.holoeverywhere.widget.ExpandableListView.OnChildClickListener;
import org.holoeverywhere.widget.ExpandableListView.OnGroupClickListener;
import org.holoeverywhere.widget.RadioButton;
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
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewAlert;
import com.handyapps.stocktracker.activity.AddNewTrade;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.activity.TransactionsActivity;
import com.handyapps.stocktracker.adapter.PosExpandableListAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.AddCashPosDialog;
import com.handyapps.stocktracker.dialogs.AddNewPortfolioDialog;
import com.handyapps.stocktracker.model.CashPosObject;
import com.handyapps.stocktracker.model.PortfolioManager;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.SingleTickerDataset;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.StockPosObject;
import com.handyapps.stocktracker.model.TransactionManager;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.MyComparatorUtils;
import com.handyapps.stocktracker.utils.TextColorPicker;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class PositionsFragment extends Fragment implements OnClickListener, OnScrollListener {
	
	private ExpandableListView mCardView;
	private PosExpandableListAdapter _listAdapter;
	HashMap<String, List<StockPosObject>> listDataChild;
	private ArrayList<String> nameList;
	
	private ImageButton mBtnAddPos;
	
	private TextView mTVPortValue;
	private TextView mTVOverallGainLoss;
	private TextView mTVGainLossTitle;
	
	private RadioButton mRBOverall;
	private RadioButton mRBDaily;
	
	private SharedPreferences sp;
	private MainWatchBroadcastReceiver receiver;
	private ArrayList<StockPosObject> spoList;
	private List<PortfolioObject> pList;
	private List<CashPosObject> cpoList;
	private List<String> _initialCash;
	private StockPosObject spo;
	
	private GridQuickAction mQuickAction;
	private String strViewTxns;
	private String strViewStock;
	private String strAlert;
	private String strNews;
	private String strRemove;
	
	private String strLongPosOverall;
	private String strLongPosDaily;
	private String strShortPosOverall;
	private String strShortPosDaily;
	private String strClosedPosOverall;
	private String strClosedPosDaily;
	
	private int mPortId;
	private int numDecimals = Constants.NUMBER_OF_DECIMALS;
	private boolean isOverallChecked;
	private Resources res;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	public static PositionsFragment newInstance() {
		PositionsFragment fragment = new PositionsFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.positions_list_fragment, container, false);
		res = getActivity().getResources();
		
		strViewTxns = res.getString(R.string.view_transactions);
		strViewStock = res.getString(R.string.view_stock_details);
		strNews = res.getString(R.string.latest_news);
		strAlert = res.getString(R.string.q_set_alert);
		strRemove = res.getString(R.string.remove_stock);
		buildQuickActionItem();
		
		strLongPosOverall = res.getString(R.string.long_positions_overall);
		strLongPosDaily = res.getString(R.string.long_positions_daily);
		strShortPosOverall = res.getString(R.string.short_positions_overall);
		strShortPosDaily = res.getString(R.string.short_positions_daily);
		strClosedPosOverall = res.getString(R.string.closed_positions_overall);
		strClosedPosDaily = res.getString(R.string.closed_positions_daily);
		
		mTVPortValue = (TextView) view.findViewById(R.id.portfolio_value);
		mTVOverallGainLoss = (TextView) view.findViewById(R.id.overall_gain_loss);
		mTVGainLossTitle = (TextView) view.findViewById(R.id.tv_overall_gain_loss);
		
		Typeface helveticaSReg = Typeface.createFromAsset(getActivity().getAssets(), "helvetica-s-regular.ttf");
		Typeface helveticaSBold = Typeface.createFromAsset(getActivity().getAssets(), "helvetica-s-bold.ttf");
		
		mTVPortValue.setTypeface(helveticaSBold, Typeface.BOLD);
		mTVOverallGainLoss.setTypeface(helveticaSReg, Typeface.BOLD);
		
		isOverallChecked = sp.getBoolean(Constants.SP_KEY_IS_OVERALL_CHECKED, false);
		mRBOverall = (RadioButton) view.findViewById(R.id.radio_overall);
		mRBDaily = (RadioButton) view.findViewById(R.id.radio_daily);
		mRBOverall.setOnClickListener(this);
		mRBDaily.setOnClickListener(this);
		
		mBtnAddPos = (ImageButton) view.findViewById(R.id.btn_add_pos);
		mBtnAddPos.setOnClickListener(this);
		
		mPortId = sp.getInt(Constants.SP_KEY_PORTFOLIO_ID, -1);
		if (mPortId == 0) 
			mPortId = -1;
		mCardView = (ExpandableListView) view.findViewById(R.id.cardsview_positions);	
		View footerView = ((LayoutInflater) getActivity().getApplicationContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shadow_container, null, false);		
		mCardView.addFooterView(footerView);
		mCardView.setOnGroupClickListener(new OnGroupClickListener() {
			  @Override
			  public boolean onGroupClick(ExpandableListView parent, View v,
			                              int groupPosition, long id) { 
			    return true; // This way the expander cannot be collapsed
			  }
			});
		
		_initialCash = new ArrayList<String>(1);
		listDataChild = new HashMap<String, List<StockPosObject>>();
		nameList = new ArrayList<String>();
		cpoList = new ArrayList<CashPosObject>();
		_listAdapter = new PosExpandableListAdapter(getActivity(), nameList, listDataChild, cpoList, _initialCash);
		mCardView.setAdapter(_listAdapter);
		mCardView.setOnChildClickListener(new OnChildClickListener() {
			 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
            	String headerTitle = (String) nameList.get(groupPosition);
            	if (headerTitle.equals(res.getString(R.string.cash_positions))) {
            		Intent i = new Intent(getActivity(), TransactionsActivity.class);
					i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, mPortId);
					i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_CASH);
					 
					getActivity().startActivity(i);
            	} else {
	            	spo = listDataChild.get(nameList.get(groupPosition))
	            			.get(childPosition);
	            	mQuickAction.show(v);
            	}

                return false;
            }
        });
		mCardView.setOnScrollListener(this);

		return view;
	}
	
	private void registerBroadcast() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_MAIN_ACTIVITY);
		filter.addAction(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		filter.addAction(Constants.ACTION_FILTER_PORTFOLIO);
		filter.addAction(Constants.ACTION_TRADES_FRAGMENT);
		receiver = new MainWatchBroadcastReceiver();
		getActivity().registerReceiver(receiver, filter);

	}
	
	private class MainWatchBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {

			if ( (intent.getAction().equals(Constants.ACTION_MAIN_ACTIVITY) ||
					intent.getAction().equals(Constants.ACTION_FILTER_PORTFOLIO) ||
						intent.getAction().equals(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT))
						&& getActivity() != null) {

				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					
					boolean isFilterPortList = bundle.getBoolean(Constants.KEY_FILTER_PORTFOLIOS_PAGER);
					boolean isUpdatePortList = bundle.getBoolean(Constants.KEY_UPDATE_PORTFOLIOS_PAGER);
					
					if (isFilterPortList) {
						mPortId = bundle.getInt(Constants.KEY_FILTER_PORTFOLIO_ID);
						updateCards(mPortId);
					} else if (isUpdatePortList) 
						updateCards(mPortId);
				}
			} else if (intent.getAction().equals(Constants.ACTION_TRADES_FRAGMENT) && getActivity() != null) {
				updateCards(mPortId);
			}
		}
	}
	
	private void updateCards(int portId) {

		if (pList != null)
			pList.clear();
		pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		if (cpoList != null)
			cpoList.clear();
		if (listDataChild != null)
			listDataChild.clear();
		if (nameList != null)
			nameList.clear();
		if (_initialCash != null)
			_initialCash.clear();
		List<List<StockPosObject>> listAdapter = null;
		double dTotInitialCash = 0;
		
		
		if (portId == -1) {
			List<CashPosObject> tempList = DbAdapter.getSingleInstance().fetchCashPosList();
			cpoList.addAll(tempList);
			listAdapter = getAllStockList(pList);
			
			for (PortfolioObject po : pList) {
				double dInitialCash = Double.parseDouble(po.getInitialCash());
				dTotInitialCash += dInitialCash;
			}
			_initialCash.add(String.valueOf(dTotInitialCash));
		} else {
			for (PortfolioObject po : pList) {
				if (portId == po.getId()){
					List<CashPosObject> tempList = DbAdapter.getSingleInstance().fetchCashPosByPortId(po.getId());
					cpoList.addAll(tempList);
					listAdapter = refreshStockList(po.getId());
					_initialCash.add(po.getInitialCash());
				}
			}
		}
		
		if (listAdapter == null) {
			
			StockPosObject emptySpo = new StockPosObject();
			emptySpo.setName(getActivity().getResources().getString(R.string.no_stock_added));
			emptySpo.setSymbol("");
			emptySpo.setAvgPrice("");
			emptySpo.setChange("");
			emptySpo.setGainLoss("");
			emptySpo.setLastPrice("");
			emptySpo.setMktValue("");
			emptySpo.setQuantity("");
			
			ArrayList<StockPosObject> sListLong = new ArrayList<StockPosObject>();
			
			sListLong.add(emptySpo);

			listAdapter = new ArrayList<List<StockPosObject>>();
			listAdapter.add(sListLong);
		} 
		
		if (isOverallChecked) {			
			nameList.add(strLongPosOverall);
			listDataChild.put(strLongPosOverall, (List<StockPosObject>) listAdapter.get(0));
			if (!listAdapter.get(1).isEmpty()) {
				nameList.add(strShortPosOverall);
				listDataChild.put(strShortPosOverall, (List<StockPosObject>) listAdapter.get(1));				
			}
			if (!listAdapter.get(2).isEmpty()) {
				nameList.add(strClosedPosOverall);
				listDataChild.put(strClosedPosOverall, (List<StockPosObject>) listAdapter.get(2));					
			}
		} else {
			nameList.add(strLongPosDaily);
			listDataChild.put(strLongPosDaily, (List<StockPosObject>) listAdapter.get(0));
			if (!listAdapter.get(1).isEmpty()) {
				nameList.add(strShortPosDaily);
				listDataChild.put(strShortPosDaily, (List<StockPosObject>) listAdapter.get(1));				
			}
			if (!listAdapter.get(2).isEmpty()) {
				nameList.add(strClosedPosDaily);
				listDataChild.put(strClosedPosDaily, (List<StockPosObject>) listAdapter.get(2));					
			}
		}
		
		nameList.add(res.getString(R.string.cash_positions).toUpperCase());		
		_listAdapter.notifyDataSetChanged();
				
		refreshTotalWorth();
	}
	
	public List<List<StockPosObject>> refreshStockList(int id) {
		
		List<List<StockPosObject>> laList = new ArrayList<List<StockPosObject>>();

		// 1. based on portfolio id to get all stock ids:
		List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(id);

		// 2. base on stock ids to get all stocksObject
		ArrayList<StockPosObject> sListLong = new ArrayList<StockPosObject>();
		ArrayList<StockPosObject> sListShort = new ArrayList<StockPosObject>();
		ArrayList<StockPosObject> sListClosed = new ArrayList<StockPosObject>();

		StockObject so;
		StockPosObject s;
		StockPosObject emptySpo;
		QuoteObject qo;
		spoList = new ArrayList<StockPosObject>();
		
		emptySpo = new StockPosObject();
		emptySpo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptySpo.setSymbol("");
		emptySpo.setAvgPrice("");
		emptySpo.setChange("");
		emptySpo.setGainLoss("");
		emptySpo.setLastPrice("");
		emptySpo.setMktValue("");
		emptySpo.setQuantity("");
		
		if (pList.size() > 0) {
			for (PortfolioStockObject pl : pList) {
				so = new StockObject();
				s = new StockPosObject();
				qo = new QuoteObject();

				int stockId = pl.getStockId();

				so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
				qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(so.getSymbol());
				SingleTickerDataset singleTickerDataSet = TransactionManager.getSingleStockProfitReport(id, stockId, getActivity());

				if (so != null && qo != null) {
					
					s.setStockId(so.getId());
					s.setPortId(id);
					s.setAvgPrice(singleTickerDataSet.getAvgPrice());
					Double dQuantity = Double.valueOf(singleTickerDataSet.getQuantity());
					Double dShortQty = Double.valueOf(singleTickerDataSet.getShortQty());
					if (dQuantity > 0)
						s.setQuantity(Integer.toString(dQuantity.intValue()));
					else if (dQuantity == 0 && dShortQty > 0)
						s.setQuantity(Integer.toString(dShortQty.intValue()));
					else 
						s.setQuantity("0");
					s.setName(so.getName());
					s.setSymbol(so.getSymbol());
					s.setChange(qo.getChange());
					s.setChangeInPercent(qo.getChangeInPercent());
					s.setLastPrice(qo.getLastTradePrice());
					Double dLastTradePrice = Double.parseDouble(qo.getLastTradePrice());
					Double dMktValue = dLastTradePrice * dQuantity;
					String sMktValue = DecimalsConverter.convertToStringValueBaseOnLocale(dMktValue, numDecimals, getActivity());
					if (isOverallChecked) {
						s.setGainLoss(singleTickerDataSet.getNetProfit());
						s.setDGainLoss(singleTickerDataSet.getDoubleNetProfit());
						s.setMktValue(sMktValue);
						s.setDMktValue(singleTickerDataSet.getDoubleTotalValue());
					} else {
						s.setGainLoss(singleTickerDataSet.getDailyProfit());
						s.setDGainLoss(singleTickerDataSet.getDoubleDailyProfit());
						s.setMktValue("");
						s.setDMktValue(singleTickerDataSet.getDoubleTotalValue());
					}
					if (dQuantity > 0) {
						s.setStockType(StockPosObject.LONG_POS_TYPE);
						sListLong.add(s);
					} else if (dQuantity == 0 && dShortQty > 0) {
						dMktValue = dLastTradePrice * dShortQty;
						double gainLoss = 0;
						if (isOverallChecked)
							gainLoss = singleTickerDataSet.getDoubleNetProfit() - dMktValue;
						else
							gainLoss = singleTickerDataSet.getDoubleDailyProfit() - dMktValue;
						s.setDGainLoss(gainLoss);
						s.setGainLoss(DecimalsConverter.convertToStringValueBaseOnLocale(gainLoss, numDecimals, getActivity()));
						s.setStockType(StockPosObject.SHORT_POS_TYPE);
						sListShort.add(s);
					} else if (singleTickerDataSet.getHasTxn()) {
						s.setStockType(StockPosObject.CLOSED_POS_TYPE);
						sListClosed.add(s);
					}
					
					this.spoList.add(s);
				}
			}
		} else {			
			sListLong.add(emptySpo);
		}
		
		if (sListLong.isEmpty())
			sListLong.add(emptySpo);
		
		if (isOverallChecked) {
			Collections.sort(sListLong, new MyComparatorUtils.StockPosObjectMktValueComparator());
			Collections.sort(sListShort, new MyComparatorUtils.StockPosObjectMktValueComparator());
			Collections.sort(sListClosed, new MyComparatorUtils.StockPosObjectMktValueComparator());
		} else {
			Collections.sort(sListLong, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
			Collections.sort(sListShort, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
			Collections.sort(sListClosed, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
		}

		laList.add(sListLong);
		laList.add(sListShort);
		laList.add(sListClosed);
		
		return laList;
	}
	
	public List<List<StockPosObject>> getAllStockList(List<PortfolioObject> po) {

		List<List<StockPosObject>> laList = new ArrayList<List<StockPosObject>>();
		spoList = new ArrayList<StockPosObject>();
		ArrayList<StockPosObject> sListLong = new ArrayList<StockPosObject>();
		ArrayList<StockPosObject> sListShort = new ArrayList<StockPosObject>();
		ArrayList<StockPosObject> sListClosed = new ArrayList<StockPosObject>();
		
		StockPosObject emptySpo = new StockPosObject();
		emptySpo.setName(getActivity().getResources().getString(R.string.no_stock_added));
		emptySpo.setSymbol("");
		emptySpo.setAvgPrice("");
		emptySpo.setChange("");
		emptySpo.setGainLoss("");
		emptySpo.setLastPrice("");
		emptySpo.setMktValue("");
		emptySpo.setQuantity("");
		
		for (PortfolioObject portObj : po) {

			List<PortfolioStockObject> pList = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portObj.getId());
	
			if (pList.size() > 0) {
				for (PortfolioStockObject pl : pList) {
					StockObject so = new StockObject();
					StockPosObject s = new StockPosObject();
					QuoteObject qo = new QuoteObject();
	
					int stockId = pl.getStockId();
	
					so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
					qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(so.getSymbol());
					SingleTickerDataset singleTickerDataSet = TransactionManager.getSingleStockProfitReport(portObj.getId(), stockId, getActivity());
	
					if (so != null && qo != null) {
						
						s.setPortId(portObj.getId());
						s.setStockId(stockId);
						s.setAvgPrice(singleTickerDataSet.getAvgPrice());
						Double dQuantity = Double.valueOf(singleTickerDataSet.getQuantity());
						Double dShortQty = Double.valueOf(singleTickerDataSet.getShortQty());
						if (dQuantity > 0)
							s.setQuantity(Integer.toString(dQuantity.intValue()));
						else if (dQuantity == 0 && dShortQty > 0)
							s.setQuantity(Integer.toString(dShortQty.intValue()));
						else 
							s.setQuantity("0");
						
						s.setName(so.getName());
						s.setSymbol(so.getSymbol());
						s.setChange(qo.getChange());
						s.setChangeInPercent(qo.getChangeInPercent());
						s.setLastPrice(qo.getLastTradePrice());
						Double dLastTradePrice = Double.parseDouble(qo.getLastTradePrice());
						Double dMktValue = dLastTradePrice * dQuantity;
						String sMktValue = DecimalsConverter.convertToStringValueBaseOnLocale(dMktValue, numDecimals, getActivity());
						if (isOverallChecked) {
							s.setGainLoss(singleTickerDataSet.getNetProfit());
							s.setDGainLoss(singleTickerDataSet.getDoubleNetProfit());
							s.setMktValue(sMktValue);
							s.setDMktValue(singleTickerDataSet.getDoubleTotalValue());
						} else {
							s.setGainLoss(singleTickerDataSet.getDailyProfit());
							s.setDGainLoss(singleTickerDataSet.getDoubleDailyProfit());
							s.setMktValue("");
							s.setDMktValue(singleTickerDataSet.getDoubleTotalValue());
						}
						if (dQuantity > 0) {
							s.setStockType(StockPosObject.LONG_POS_TYPE);
							sListLong.add(s);
						} else if (dQuantity == 0 && dShortQty > 0) {
							dMktValue = dLastTradePrice * dShortQty;
							double gainLoss = 0;
							if (isOverallChecked)
								gainLoss = singleTickerDataSet.getDoubleNetProfit() - dMktValue;
							else
								gainLoss = singleTickerDataSet.getDoubleDailyProfit() - dMktValue;
							s.setDGainLoss(gainLoss);
							s.setGainLoss(DecimalsConverter.convertToStringValueBaseOnLocale(gainLoss, numDecimals, getActivity()));
							s.setStockType(StockPosObject.SHORT_POS_TYPE);
							sListShort.add(s);
						}  else if (singleTickerDataSet.getHasTxn()) {
							s.setStockType(StockPosObject.CLOSED_POS_TYPE);
							sListClosed.add(s);
						}
						
						this.spoList.add(s);
					}
				}
			} 
		}

		if (sListLong.size() < 1) {			
			sListLong.add(emptySpo);
		}
		
		if (isOverallChecked) {
			Collections.sort(sListLong, new MyComparatorUtils.StockPosObjectMktValueComparator());
			Collections.sort(sListShort, new MyComparatorUtils.StockPosObjectMktValueComparator());
			Collections.sort(sListClosed, new MyComparatorUtils.StockPosObjectMktValueComparator());
		} else {
			Collections.sort(sListLong, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
			Collections.sort(sListShort, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
			Collections.sort(sListClosed, new MyComparatorUtils.StockPosObjectAbsGainLossComparator());
		}
		
		laList.add(sListLong);
		laList.add(sListShort);
		laList.add(sListClosed);
		
		return laList;
	}

	@Override
	public void onClick(View v) {
	    
	    // Check which radio button was clicked
	    switch(v.getId()) {
	        case R.id.radio_overall:
	        	boolean isChecked = ((RadioButton) v).isChecked();
	            if (isChecked) {
	            	isOverallChecked = true;
	                mTVGainLossTitle.setText(res.getString(R.string.overall_gain_loss));
	                updateCards(mPortId);
	            }
	            break;
	        case R.id.radio_daily:
	        	boolean isDailyChecked = ((RadioButton) v).isChecked();
	            if (isDailyChecked) {
	            	isOverallChecked = false;
	            	mTVGainLossTitle.setText(res.getString(R.string.daily_gain_loss));
	            	updateCards(mPortId);
	            }
	            break;
	        case R.id.btn_add_pos:
	        	if (DbAdapter.getSingleInstance().fetchPortfolioList().isEmpty())
        			noPortOrWatchlistAlert(true);
        		else
        			addNewTxnDialog();
				break;
	    }
	}
	
	
	private void noPortOrWatchlistAlert(boolean isPort) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

		dialog.setIcon(R.drawable.ic_action_warning);
		dialog.setTitle(getString(R.string.no_portfolio_added));
		dialog.setMessage(getString(R.string.no_portfoli_dialog_msg));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				AddNewPortfolioDialog portDialog = new AddNewPortfolioDialog();     
        		portDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_ADD_PORTFOLIO);
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", getActivity().getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/*public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK)
			if (requestCode == Constants.DIALOG_ADD_CASH_TXN_I) {
				updateCards(mPortId);
				sendBroadastUpdatePagerIndicator();
			}
	}*/
	
	protected void addNewTxnDialog() {
		String title = res.getString(R.string.add_new_txn);
		final String[] itemsArr = res.getStringArray(R.array.add_new_transaction);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setItems(itemsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (which == 0) {
            		Intent i = new Intent(getActivity(), AddNewTrade.class);
    	    		i.putExtra(Constants.KEY_FROM, Constants.FROM_POSITIONS_TAB);
    	    		if (mPortId != -1)
    	    			i.putExtra(Constants.KEY_PORTFOLIO_ID, mPortId);
    	    		getActivity().startActivity(i);
            	} else if (which == 1) {
            		AddCashPosDialog addCashDialog = new AddCashPosDialog();     
            		//addCashDialog.setTargetFragment(PositionsFragment.this, Constants.DIALOG_ADD_CASH_TXN_I);
            		addCashDialog.show(getActivity().getSupportFragmentManager(), Constants.DIALOG_ADD_CASH_TXN_S);
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
	
	private void refreshTotalWorth() {

		double dTotalValue = 0;
		double dNetProfit = 0;
		double dFinalBalance = 0;

		if (spoList != null && spoList.size() > 0) {
			for (StockPosObject c : spoList) {

				double singleStockValue = c.getDMktValue();
				double singleStockNetProfit = c.getDGainLoss();
				dTotalValue = dTotalValue + singleStockValue;
				dNetProfit = dNetProfit + singleStockNetProfit;
			}
		}
		
		if (pList.size() > 0) {
			for (PortfolioObject po : pList) {
				double dInitialBalance = 0;
				double dTotDeposits = 0, dTotWithdrawals = 0;
				if (mPortId == -1) {
					dInitialBalance = Double.parseDouble(po.getInitialCash());
					List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(po.getId());
					for (CashPosObject cpo : cpoList) {
						if (cpo.getTxnType().equals("cd"))
							dTotDeposits += Double.parseDouble(cpo.getAmount());
						else if (cpo.getTxnType().equals("cw"))
							dTotWithdrawals += Double.parseDouble(cpo.getAmount());
					}
					dFinalBalance += dInitialBalance + dTotDeposits - dTotWithdrawals;
				} else if (mPortId == po.getId()) {
					dInitialBalance = Double.parseDouble(po.getInitialCash());
					List<CashPosObject> cpoList = DbAdapter.getSingleInstance().fetchCashPosByPortId(po.getId());
					for (CashPosObject cpo : cpoList) {
						if (cpo.getTxnType().equals("cd"))
							dTotDeposits += Double.parseDouble(cpo.getAmount());
						else if (cpo.getTxnType().equals("cw"))
							dTotWithdrawals += Double.parseDouble(cpo.getAmount());
					}
					dFinalBalance = dInitialBalance + dTotDeposits - dTotWithdrawals;
				}
			}
		}
		
		dTotalValue += dFinalBalance;
		String sTotalValue = DecimalsConverter.convertToStringValueBaseOnLocale(dTotalValue, numDecimals, getActivity());
		String sNetGainOrLoss = DecimalsConverter.convertToStringValueBaseOnLocale(dNetProfit, numDecimals, getActivity());

		//Spanned spannedToalValue = TextColorPicker.getOrangeText("", sTotalValue);

		Spanned spannedGainLoss;
		if (sNetGainOrLoss.contains("-")) {
			spannedGainLoss = TextColorPicker.getRedText("", sNetGainOrLoss + getPercentageGainLoss(dTotalValue, dNetProfit));
		} else {
			spannedGainLoss = TextColorPicker.getGreenText("", sNetGainOrLoss + getPercentageGainLoss(dTotalValue, dNetProfit));
		}

		mTVPortValue.setText(sTotalValue);
		mTVOverallGainLoss.setText(spannedGainLoss);
	}
	
	private String getPercentageGainLoss(double dTotalValue, double dNetProfit) {
		String sPercentageGainLoss = "0.00";
		double percentageGainLoss = (dNetProfit/dTotalValue)*100;
		if (dTotalValue != 0.0)
			sPercentageGainLoss = DecimalsConverter.convertToStringValueBaseOnLocale(percentageGainLoss, numDecimals, getActivity());
		if (dNetProfit < 0 && dTotalValue != 0.0)
			sPercentageGainLoss = " (-" + sPercentageGainLoss.substring(2) + "%)";
		else if (dNetProfit >= 0.0)
			sPercentageGainLoss = " (+" + sPercentageGainLoss.substring(1) + "%)";
		else
			sPercentageGainLoss = " (-" + sPercentageGainLoss + "%)";
		
		return sPercentageGainLoss;
	}
	
	private void sendBroadastUpdatePagerIndicator() {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		getActivity().sendBroadcast(i);
	}
	
	private void buildQuickActionItem() {

		ActionItem addItemViewStock = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS, strViewStock, res.getDrawable(
				R.drawable.ic_action_search));
		ActionItem addItemView = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS, strViewTxns, res.getDrawable(
				R.drawable.icon_transactions));
		ActionItem addItemNews = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS, strNews, res.getDrawable(
				R.drawable.quick_action_view_news));
		ActionItem addItemAlert = new ActionItem(Constants.QUICK_ACTION_ID_SET_ALERT, strAlert, res.getDrawable(
				R.drawable.ic_alert_dark));
		ActionItem addItemRemove = new ActionItem(Constants.QUICK_ACTION_ID_DELETE, strRemove, res.getDrawable(
				R.drawable.ic_delete_dark));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();

		items.add(addItemViewStock);
		items.add(addItemView);
		items.add(addItemNews);
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
				
				case Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS:
					StockObject soPrice = DbAdapter.getSingleInstance().fetchStockObjectByStockId(spo.getStockId());
					startViewStock(soPrice);
					break;

				case Constants.QUICK_ACTION_ID_VIEW_TRANSACTIONS:
					startViewTxns();
					break;
					
				case Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS:
					StockObject soNews = DbAdapter.getSingleInstance().fetchStockObjectByStockId(spo.getStockId());
					startViewNews(soNews);
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
	
	private void startViewStock(StockObject so) {

		int stockId = so.getId();
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
	
	private void showAddAlert() {
		String symbol = spo.getSymbol();

		Intent i = new Intent(getActivity(), AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		getActivity().startActivity(i);
	}
	
	private void startViewTxns() {
		String symbol = spo.getSymbol();
		
		Intent i = new Intent(getActivity(), TransactionsActivity.class);
		i.putExtra(Constants.KEY_FILTER_BY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_CASH_TRADES_ALL, Constants.KEY_TRADES);
		 
		getActivity().startActivity(i);
	}

	private void showDeleteDialog() {

		String msg = res.getString(R.string.delete_stock_msg);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_alerts_warning);
		builder.setTitle(res.getString(R.string.delete_stock));
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				int mPortId = spo.getPortId();
				int mStockId = spo.getStockId();
				deleteAllTradeBySingleStock(mPortId, mStockId);
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
	
	protected void deleteAllTradeBySingleStock(int portId, int stockId) {

		int mPortId = portId;
		int mStockId = stockId;

		boolean isDeleted = PortfolioManager.deleteStockTransactionOfSinglePortfolio(mPortId, mStockId);

		if (isDeleted) {
			String msg = res.getString(R.string.stock_deleted);
			Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
			WidgetUtils.updateWidget(getActivity());
			sendBroadastUpdatePagerIndicator();
		}
	}
	
	private void startViewNews(StockObject so) {

		int stockId = so.getId();
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
		i.putExtra(Constants.KEY_TAB_POSITION, Constants.TO_NEWS_FRAGMENT);
		getActivity().startActivity(i);
	}
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if ( scrollState == OnScrollListener.SCROLL_STATE_IDLE )
			mCardView.requestFocusFromTouch();
	
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		isOverallChecked = sp.getBoolean(Constants.SP_KEY_IS_OVERALL_CHECKED, true);
		if (isOverallChecked) {
			mRBOverall.setChecked(true);
			mTVGainLossTitle.setText(res.getString(R.string.overall_gain_loss));
		} else {
			mRBDaily.setChecked(true);
			mTVGainLossTitle.setText(res.getString(R.string.daily_gain_loss));
		}
		updateCards(mPortId);
		registerBroadcast();
	}

	@Override
	public void onPause() {
		super.onPause();
		sp.edit().putBoolean(Constants.SP_KEY_IS_OVERALL_CHECKED, isOverallChecked).commit();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			
		}
	}

}
