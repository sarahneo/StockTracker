package com.handyapps.stocktracker.cards;

import it.gmariotti.cardslib.library.internal.Card;

import java.util.ArrayList;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.GridQuickAction;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.activity.AddNewAlert;
import com.handyapps.stocktracker.activity.AddNewTrade;
import com.handyapps.stocktracker.activity.TransactionDetailsFragmentActivity;
import com.handyapps.stocktracker.adapter.CustomAdapter;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.model.PortfolioManager;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.SymbolCallBackObject;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class PortfolioCard extends Card implements TextWatcher {
	
	private GridQuickAction mQuickAction;
	private Resources res;
	private SharedPreferences sp;
	private FragmentActivity activity;
	private ListAdapter la;
	private TableLayout layout;
	private TextView tv;
	private EditText et;
	private EditText etCash;
	private AlertDialog aDialog;
	
	private String strView;
	private String strAlert;
	private String strNews;
	private String strAddTrade;
	private String strRemove;

	private StockObject so;
	private PortfolioObject po;
	private List<PortfolioStockObject> pso;

	public PortfolioCard(PortfolioObject po, ListAdapter la, FragmentActivity activity){
		super(activity, R.layout.card_portfolio);
		
		this.po = po;
		this.pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(po.getId());
		this.activity = activity;
		this.la = la;
		res = activity.getResources();
		sp = PreferenceManager.getDefaultSharedPreferences(activity);
		
		strView = res.getString(R.string.view_stock_details);
		strAlert = res.getString(R.string.q_set_alert);
		strNews = res.getString(R.string.latest_news);
		strAddTrade = res.getString(R.string.add_trade);
		strRemove = res.getString(R.string.remove_stock);
		buildQuickActionItem();
	}
	
	@Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        tv = (TextView) parent.findViewById(R.id.card_title);

        if (po != null)
            tv.setText(po.getName() + " ("+po.getCurrencyType().toUpperCase()+")");
        
        editOrDeletePort();
		layout = (TableLayout) parent.findViewById(R.id.card_layout);
		buildCard(la);
    }

	
	private void buildQuickActionItem() {

		ActionItem addItemView = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS, strView, res.getDrawable(
				R.drawable.ic_action_search));
		ActionItem addItemAlert = new ActionItem(Constants.QUICK_ACTION_ID_SET_ALERT, strAlert, res.getDrawable(
				R.drawable.ic_alert_dark));
		ActionItem addItemNews = new ActionItem(Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS, strNews, res.getDrawable(
				R.drawable.quick_action_view_news));
		ActionItem addItemAddTrade = new ActionItem(Constants.QUICK_ACTION_ID_ADD_TRADE, strAddTrade, res.getDrawable(
				R.drawable.ic_add_dark));
		ActionItem addItemRemove = new ActionItem(Constants.QUICK_ACTION_ID_DELETE, strRemove, res.getDrawable(
				R.drawable.ic_delete_dark));

		ArrayList<ActionItem> items = new ArrayList<ActionItem>();

		items.add(addItemView);
		items.add(addItemAlert);
		items.add(addItemNews);
		items.add(addItemAddTrade);
		items.add(addItemRemove);
		boolean isNumColumnThree = true;
		mQuickAction = new GridQuickAction(activity, isNumColumnThree, items.size());

		mQuickAction.setupAdapter(items);

		// setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new GridQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(GridQuickAction quickAction, int pos, int actionId) {

				int mActionId = actionId;
				switch (mActionId) {

				case Constants.QUICK_ACTION_ID_VIEW_STOCK_DETAILS:
					startViewStock();
					break;
					
				case Constants.QUICK_ACTION_ID_SET_ALERT:
					showAddAlert();
					break;

				case Constants.QUICK_ACTION_ID_VIEW_LATEST_NEWS:
					startViewNews();
					break;

				case Constants.QUICK_ACTION_ID_ADD_TRADE:
					showAddTrade();
					break;
				case Constants.QUICK_ACTION_ID_DELETE:
					showDeleteDialog();
					break;
				}
			}
		});
	}
	
	private void startViewStock() {

		int stockId = so.getId();
		String symbol = so.getSymbol();
		String companyName = so.getName();
		String exch = so.getExch();
		String type = so.getType();
		String typeDisp = so.getTypeDisp();
		String exchDisp = so.getExchDisp();

		sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
		Intent i = new Intent(activity, TransactionDetailsFragmentActivity.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, po.getId());
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		activity.startActivity(i);
	}
	
	private void showAddAlert() {
		String symbol = so.getSymbol();

		Intent i = new Intent(activity, AddNewAlert.class);
		i.putExtra(Constants.KEY_IS_EDIT_ALERT, false);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		activity.startActivity(i);
	}

	private void showDeleteDialog() {

		String msg = res.getString(R.string.delete_stock_msg);
		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setIcon(typedValue.resourceId);
		builder.setTitle(res.getString(R.string.delete_stock));
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				int mPortId = po.getId();
				int mStockId = so.getId();
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", activity.getPackageName());
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
			refreshCard();
			String msg = res.getString(R.string.stock_deleted);
			Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
			WidgetUtils.updateWidget(activity);
			sendBroadastUpdatePagerIndicator(false);
		}
	}
	
	private void startViewNews() {

		int stockId = so.getId();
		String symbol = so.getSymbol();
		String companyName = so.getName();
		String exch = so.getExch();
		String type = so.getType();
		String typeDisp = so.getTypeDisp();
		String exchDisp = so.getExchDisp();

		sp.edit().putInt(Constants.SP_KEY_STOCK_ID, stockId).commit();
		Intent i = new Intent(activity, TransactionDetailsFragmentActivity.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, po.getId());
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		i.putExtra(Constants.KEY_COMPANY_NAME, companyName);
		i.putExtra(Constants.KEY_EXCH, exch);
		i.putExtra(Constants.KEY_TYPE, type);
		i.putExtra(Constants.KEY_TYPE_DISP, typeDisp);
		i.putExtra(Constants.KEY_EXCH_DISP, exchDisp);
		i.putExtra(Constants.KEY_TAB_POSITION, Constants.TO_NEWS_FRAGMENT);
		activity.startActivity(i);
	}
	
	private void showAddTrade() {
		int stockId = so.getId();
		int portId = po.getId();
		String symbol = so.getSymbol();

		Intent i = new Intent(activity, AddNewTrade.class);
		i.putExtra(Constants.KEY_FROM, Constants.FROM_PORTFOLIO_LIST);
		i.putExtra(Constants.KEY_PORTFOLIO_ID, portId);
		i.putExtra(Constants.KEY_STOCK_ID, stockId);
		i.putExtra(Constants.KEY_SYMBOL, symbol);
		activity.startActivity(i);
	}

	
	private void refreshCard() {

		pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(po.getId());
		ArrayList<SymbolCallBackObject> sList = new ArrayList<SymbolCallBackObject>();
		SymbolCallBackObject scbo;
		
		if (pso.size() > 0) {
			for (PortfolioStockObject pl : pso) {
				scbo = new SymbolCallBackObject();
				
				int stockId = pl.getStockId();
				StockObject stockObject = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);

				if (pl != null) {

					scbo.setExch(stockObject.getExch());
					scbo.setExchDisp(stockObject.getExchDisp());
					scbo.setName(stockObject.getName());
					scbo.setSymbol(stockObject.getSymbol());
					scbo.setType(stockObject.getType());
					scbo.setTypeDisp(stockObject.getTypeDisp());
					sList.add(scbo);
				}
			}
		} else {
			scbo = new SymbolCallBackObject();
			scbo.setName(res.getString(R.string.no_stock_added));
			scbo.setExch("");
			scbo.setExchDisp("");
			scbo.setType("");
			scbo.setSymbol("");
			scbo.setTypeDisp("");
			
			sList.add(scbo);
		}
		
		la = new CustomAdapter(activity, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, sList);
		
		buildCard(la);	
	}
	
	private void buildCard(ListAdapter listAdap) {
		if (layout.getChildCount() != 0) {
			layout.removeAllViews();
			layout.addView(tv);
		}
		
		for (int i = 0; i < listAdap.getCount(); i++) {
			SymbolCallBackObject scbo = (SymbolCallBackObject) listAdap.getItem(i);
			View item = listAdap.getView(i, null, null);
			layout.addView(item);

			if (scbo.getSymbol().equals(""))
				;
			else {
				item.setTag(i);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//int currInt = layout.indexOfChild(v)/2;
						int currInt = (Integer) v.getTag();
						int stockId = pso.get(currInt).getStockId();
						so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(stockId);
						mQuickAction.show(v);
					}
				});
			}
		}
	}
	
	private void editOrDeletePort() {

		final Drawable x = activity.getResources().getDrawable(R.drawable.ic_action_edit);

		x.setBounds(0, 0, x.getIntrinsicWidth() + 7, x.getIntrinsicHeight() + 7);
		tv.setCompoundDrawables(null, null, x, null);
		tv.setCompoundDrawablePadding(-x.getIntrinsicWidth());
		tv.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				
				if (tv.getCompoundDrawables()[2] == null) {
					return false;
				}
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (event.getX() > tv.getWidth() - tv.getPaddingRight() - x.getIntrinsicWidth()) 
						showPortDialog();
				} 
				return true;
			}
		});
	}
	
	protected void showPortDialog() {
		String title = res.getString(R.string.portfolio_options);
		String[] itemsArr = res.getStringArray(R.array.portfolio_dialog_items);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", activity.getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showDeletePortDialog() {

		String msg = res.getString(R.string.delete_portfolio_msg);
		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setIcon(typedValue.resourceId);
		builder.setTitle(res.getString(R.string.delete_portfolio_title));
		builder.setMessage(msg);
		builder.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int portId = po.getId();

				boolean isDeleted = PortfolioManager.deletePorfolioByPortId(portId);
				if (isDeleted) {
					Toast.makeText(activity, res.getString(R.string.portfolio_deleted), Toast.LENGTH_SHORT).show();
					sendBroadastUpdatePagerIndicator(true);
					WidgetUtils.updateWidget(activity);
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", activity.getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showEditPortDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		LayoutInflater inflater = activity.getLayoutInflater();
		View view;
		String dialogTitle = "";

		view = inflater.inflate(R.layout.create_new_portfolio, null);
		et = (EditText) view.findViewById(R.id.et_enter_portfolio_name);
		etCash = (EditText) view.findViewById(R.id.et_enter_initial_cash);
		etCash.setVisibility(View.GONE);

		dialogTitle = res.getString(R.string.edit_portfolio);
		et.setText(po.getName());
		et.setSelection(et.length());

		et.addTextChangedListener(PortfolioCard.this);

		builder.setTitle(dialogTitle);
		builder.setPositiveButton(res.getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				String enteredName = et.getText().toString();								

				boolean isPortEdited = PortfolioManager.updatePortfolio(po.getId(), enteredName);
				if (isPortEdited) {
					Toast.makeText(activity, res.getString(R.string.portfolio_updated), Toast.LENGTH_SHORT).show();
					WidgetUtils.updateWidget(activity);
					sendBroadastUpdatePagerIndicator(false);
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
	        int titleDividerId = res.getIdentifier("titleDivider", "id", activity.getPackageName());
	        View titleDivider = aDialog.findViewById(titleDividerId);
	        titleDivider.setBackgroundColor(res.getColor(R.color.red_tab)); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		aDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	private void sendBroadastUpdatePagerIndicator(boolean isDeletePort) {
		Intent i = new Intent(Constants.ACTION_MAIN_ACTIVITY);
		if (isDeletePort) {
			i.putExtra(Constants.KEY_FILTER_PORTFOLIOS_PAGER, true);
			i.putExtra(Constants.KEY_FILTER_PORTFOLIO_ID, -1);
		} else
			i.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		activity.sendBroadcast(i);
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

}
