package com.handyapps.stocktracker.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog;
import org.holoeverywhere.widget.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handyapps.stocktracker.Constants;
import com.handyapps.stocktracker.FindStocksDialogActivityPort;
import com.handyapps.stocktracker.R;
import com.handyapps.stocktracker.database.DbAdapter;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment;
import com.handyapps.stocktracker.dialogs.CalculatorDialogFragment.CalculatorCallbacks;
import com.handyapps.stocktracker.dialogs.WrongCurrencyDialog;
import com.handyapps.stocktracker.model.PortfolioObject;
import com.handyapps.stocktracker.model.PortfolioStockObject;
import com.handyapps.stocktracker.model.QuoteObject;
import com.handyapps.stocktracker.model.StockObject;
import com.handyapps.stocktracker.model.TransactionObject;
import com.handyapps.stocktracker.service.MyAlarmManager;
import com.handyapps.stocktracker.task.UpdateQuoteTaskSingleSymbol;
import com.handyapps.stocktracker.utils.DecimalsConverter;
import com.handyapps.stocktracker.utils.MyActivityUtils;
import com.handyapps.stocktracker.utils.MyDateFormat;
import com.handyapps.stocktracker.utils.OrientationUtils;
import com.handyapps.stocktracker.utils.ThemeUtils;
import com.handyapps.stocktracker.widget.WidgetUtils;

public class AddNewTrade extends Activity implements OnClickListener, 
						OnDateSetListener, CalculatorCallbacks {

	private int rowId = -1;
	private int fromId;
	private int portId;
	private int originalPortId;
	private int etId;

	private String portName;
	private String tradeType = TransactionObject.BUY_TYPE;
	private String KEY_FROM_ID = "KEY_FROM_ID";
	private String KEY_PORT_ID = "KEY_PORT_ID";
	private String KEY_TRADE_TYPE = "KEY_TRADE_TYPE";
	private String KEY_ROW_ID = "KEY_ROW_ID";
	private String KEY_EDIT_TEXT = "KEY_EDIT_TEXT";
	private static final String KEY_STOCK_OBJECT = "KEY_STOCK_OBJECT";
	private static final String KEY_DATE_PAIR_OBJECT = "KEY_DATE_PAIR_OBJECT";
	private static final int TRADE_TYPE_POSITION_BUY = 0;
	private static final int TRADE_TYPE_POSITION_SELL = 1;
	private static final int TRADE_TYPE_POSITION_DIVIDEND = 2;
	private static final int TRADE_TYPE_POSITION_DIVIDEND_SHARES = 3;

	private List<String> portType;
	private List<PortfolioObject> pList;

	private EditText etDate;
	private EditText etSymbol;
	private EditText etVolume;
	private EditText etPrice;
	private EditText etFees;
	private EditText etTotal;
	private EditText etNotes;
	private TextView tvVolume;
	private TextView tvPrice;
	private TextView tvFees;	
	private ImageButton ibSearch;
	private LinearLayout pd;
	
	private MyAlarmManager alarmManager;

	private Spinner spnTradeType;
	private Spinner spnPortType;

	private StockObject so;
	private TransactionObject transObject;

	private ArrayAdapter<CharSequence> adapterTradeType;
	private ArrayAdapter<String> adapterPortType;
	private DatePair datePair;

	private Resources res;
	private SharedPreferences sp;
	private AddTradeBroadcastReceiver receiver;
	
	private VolumeTextWatcher volumeTextWatcher;
	private PriceTextWatcher priceTextWatcher;
	private FeesTextWatcher feesTextWatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		volumeTextWatcher = new VolumeTextWatcher();
		priceTextWatcher = new PriceTextWatcher();
		feesTextWatcher = new FeesTextWatcher();
		
		alarmManager = new MyAlarmManager(this);

		ThemeUtils.onActivityCreateSetTheme(this, false);
	
		ActionBar actionBar = getSupportActionBar();	
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

		setContentView(R.layout.add_new_trade_layout);
		res = getResources();
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		getSupportActionBar().setLogo(res.getDrawable(R.drawable.fake_icon));

		baseComponantSetup();

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			initializeValue(bundle);
		}

		registerBroadcast();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		so = savedInstanceState.getParcelable(KEY_STOCK_OBJECT);
		datePair = savedInstanceState.getParcelable(KEY_DATE_PAIR_OBJECT);
		fromId = savedInstanceState.getInt(KEY_FROM_ID);
		portId = savedInstanceState.getInt(KEY_PORT_ID);
		tradeType = savedInstanceState.getString(KEY_TRADE_TYPE);
		rowId = savedInstanceState.getInt(KEY_ROW_ID);
		etId = savedInstanceState.getInt(KEY_EDIT_TEXT);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_FROM_ID, fromId);
		outState.putInt(KEY_PORT_ID, portId);
		outState.putString(KEY_TRADE_TYPE, tradeType);
		outState.putInt(KEY_ROW_ID, rowId);
		outState.putInt(KEY_EDIT_TEXT, etId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.new_trade_menu, menu);

		MenuItem itemDelete = menu.findItem(R.id.menu_item_delete);
		MenuItem itemCancel = menu.findItem(R.id.menu_item_cancel);

		if (rowId > -1) {

			itemDelete.setVisible(true);
			itemCancel.setVisible(true);
		} else {

			itemDelete.setVisible(false);
			itemCancel.setVisible(true);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int mID = item.getItemId();

		switch (mID) {
		case R.id.menu_item_cancel:
			showUnSaveChangeTransactionDialog();
			break;

		case R.id.menu_item_delete:
			showDeleteTransactionDialog();
			break;

		case android.R.id.home:
			MyActivityUtils.backToHome(getApplicationContext());
			finish();
			break;
		}
		return true;
	}

	private void baseComponantSetup() {

		// 2. Component
		pd = (LinearLayout) findViewById(R.id.lin_progress_bar);
		ibSearch = (ImageButton) findViewById(R.id.ib_search);
		spnTradeType = (Spinner) findViewById(R.id.spinner_trade_type);
		spnPortType = (Spinner) findViewById(R.id.spinner_portfolio_type);
		etDate = (EditText) findViewById(R.id.et_dt);
		etSymbol = (EditText) findViewById(R.id.et_symbol);

		etVolume = (EditText) findViewById(R.id.et_volume);
		etPrice = (EditText) findViewById(R.id.et_price);
		etFees = (EditText) findViewById(R.id.et_frees_and_commission);
		etTotal = (EditText) findViewById(R.id.et_total);
		etNotes = (EditText) findViewById(R.id.et_notes);
		tvVolume = (TextView) findViewById(R.id.tv_volume_title);
		tvPrice = (TextView) findViewById(R.id.tv_price_title);
		tvFees = (TextView) findViewById(R.id.tv_fees_and_commission_title);

		//etVolume.requestFocus();
		etVolume.setOnClickListener(this);
		etPrice.setOnClickListener(this);
		etFees.setOnClickListener(this);
		etTotal.setOnClickListener(this);
		etSymbol.setOnClickListener(this);
		
		etVolume.addTextChangedListener(volumeTextWatcher);
		etPrice.addTextChangedListener(priceTextWatcher);
		etFees.addTextChangedListener(feesTextWatcher);
		ibSearch.setOnClickListener(this);		
	}

	private void registerBroadcast() {

		String mAction = Constants.ACTION_ADD_NEW_TRADE;
		IntentFilter filter = new IntentFilter();
		filter.addAction(mAction);
		receiver = new AddTradeBroadcastReceiver();
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private void initializeValue(Bundle bundle) {

		Bundle mBundle = bundle;

		transObject = new TransactionObject();
		fromId = mBundle.getInt(Constants.KEY_FROM);
		portName = mBundle.getString(Constants.KEY_PORTFOLIO_NAME);

		adapterTradeType = ArrayAdapter.createFromResource(this, R.array.trade_type, R.layout.spinner_item_light);
		adapterTradeType.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		spnTradeType.setAdapter(adapterTradeType);
		spnTradeType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
				
				if (position == TRADE_TYPE_POSITION_BUY) {
					tradeType = TransactionObject.BUY_TYPE;
					etVolume.setVisibility(View.VISIBLE);
					etPrice.setVisibility(View.VISIBLE);
					etFees.setVisibility(View.VISIBLE);
					tvVolume.setVisibility(View.VISIBLE);
					tvPrice.setVisibility(View.VISIBLE);
					tvFees.setVisibility(View.VISIBLE);
					
				} else if (position == TRADE_TYPE_POSITION_SELL) {
					tradeType = TransactionObject.SELL_TYPE;
					etVolume.setVisibility(View.VISIBLE);
					etPrice.setVisibility(View.VISIBLE);
					etFees.setVisibility(View.VISIBLE);
					tvVolume.setVisibility(View.VISIBLE);
					tvPrice.setVisibility(View.VISIBLE);
					tvFees.setVisibility(View.VISIBLE);
					
				} else if (position == TRADE_TYPE_POSITION_DIVIDEND) {
					tradeType = TransactionObject.DIVIDEND_TYPE;
					/*etVolume.setVisibility(View.INVISIBLE);
					etPrice.setVisibility(View.INVISIBLE);
					etFees.setVisibility(View.INVISIBLE);
					tvVolume.setVisibility(View.INVISIBLE);
					tvPrice.setVisibility(View.INVISIBLE);
					tvFees.setVisibility(View.INVISIBLE);*/
					
					//
					etVolume.setVisibility(View.GONE);
					etPrice.setVisibility(View.GONE);
					etFees.setVisibility(View.GONE);
					tvVolume.setVisibility(View.GONE);
					tvPrice.setVisibility(View.GONE);
					tvFees.setVisibility(View.GONE);
					//
					etTotal.requestFocus();
					etTotal.setSelection(etTotal.length());
					
				} else if (position == TRADE_TYPE_POSITION_DIVIDEND_SHARES) {
					tradeType = TransactionObject.DIVIDEND_TYPE_SHARES;
					etVolume.setVisibility(View.VISIBLE);					
					etFees.setVisibility(View.VISIBLE);
					tvVolume.setVisibility(View.VISIBLE);					
					tvFees.setVisibility(View.VISIBLE);
					tvPrice.setVisibility(View.GONE);
					etPrice.setVisibility(View.GONE);
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		pList = DbAdapter.getSingleInstance().fetchPortfolioList();
		portType = new ArrayList<String>();

		for (int i = 0; i < pList.size(); i++) {
			portType.add(pList.get(i).getName());
		}

		adapterPortType = new ArrayAdapter<String>(this, R.layout.spinner_item_light, portType);
		adapterPortType.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		spnPortType.setAdapter(adapterPortType);
		spnPortType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {

				String mPortName = parent.getItemAtPosition(position).toString();
				PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByName(mPortName);
				if (po != null) {
					portId = po.getId();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		int mStockId;
		String mlastTradePrice;
		switch (fromId) {
		case Constants.FROM_PORTFOLIO_LIST:
			portId = mBundle.getInt(Constants.KEY_PORTFOLIO_ID);
			originalPortId = portId;
			mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
			so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
			mlastTradePrice = DbAdapter.getSingleInstance().fetchLastTradePriceBySymbol(so.getSymbol());
			if (mlastTradePrice.length() > 0) {
				etPrice.setText(mlastTradePrice);
			}
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (po != null) {
				portName = po.getName();
				int positionOfPofolioType = portType.indexOf(portName);
				spnPortType.setSelection(positionOfPofolioType);
			}
			editTextSetup();
			break;

		case Constants.FROM_WATCH_LIST:
			mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
			so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
			mlastTradePrice = DbAdapter.getSingleInstance().fetchLastTradePriceBySymbol(so.getSymbol());
			if (mlastTradePrice.length() > 0) {
				etPrice.setText(mlastTradePrice);
			}
			editTextSetup();
			break;

		case Constants.FROM_FIND_STOCKS:
			so = convertToStockObject(mBundle);
			mlastTradePrice = DbAdapter.getSingleInstance().fetchLastTradePriceBySymbol(so.getSymbol());
			if (mlastTradePrice.length() > 0) {
				etPrice.setText(mlastTradePrice);
			}
			editTextSetup();
			break;
			
		case Constants.FROM_POSITIONS_TAB:
		case Constants.FROM_TRANSACTIONS_ACTIVITY:
			portId = mBundle.getInt(Constants.KEY_PORTFOLIO_ID);
			PortfolioObject poPos = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (poPos != null) {
				portName = poPos.getName();
				int positionOfPofolioType = portType.indexOf(portName);
				spnPortType.setSelection(positionOfPofolioType);
			}
			so = new StockObject();
			editTextSetup();
			break;

		case Constants.FROM_EDIT_TRANSACTION:
			rowId = mBundle.getInt(Constants.KEY_TRANSACITON_ROW_ID, -1);
			String txnId = mBundle.getString(Constants.KEY_TRANSACITON_TXN_ID);
			if (txnId == null)
				txnId = "";
			if (rowId == -1)
				transObject = DbAdapter.getSingleInstance().fetchTransactionObjectById(txnId);
			else
				transObject = DbAdapter.getSingleInstance().fetchTransactionByRowId(rowId);
			portId = mBundle.getInt(Constants.KEY_PORTFOLIO_ID);
			mStockId = mBundle.getInt(Constants.KEY_STOCK_ID);
			so = DbAdapter.getSingleInstance().fetchStockObjectByStockId(mStockId);
			if (transObject != null) {
				showTransaction(transObject, so, portId);
			}
			break;
			
		}
		if (so != null)
			etSymbol.setText(so.getSymbol());
	}

	private void editTextSetup() {
		datePair = new DatePair();
		Calendar cToday = Calendar.getInstance();
		String strDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(cToday);
		String strDate = MyDateFormat.calendarToDateStringFormater(cToday);
		datePair.setStrDate(strDate);
		datePair.setStrDateYYYYMMDD(strDateYYYYMMDD);
		etDate.setText(datePair.getStrDate());
		etDate.setOnClickListener(this);
		
		etVolume.setText("0");
		etFees.setText(DecimalsConverter.convertToStringValueNoCurrencyBasedOnLocale(0, Constants.NUMBER_OF_DECIMALS, this));
	}

	private void showTransaction(TransactionObject transObject2, StockObject so2, int mPortId) {

		int _mPortId = mPortId;
		PortfolioObject mPo = DbAdapter.getSingleInstance().fetchPortfolioByPortId(_mPortId);

		TransactionObject mTransObject = transObject2;
		StockObject mSo = so2;

		// 1. trade type setup
		String mType = mTransObject.getType();
		if (mType.equalsIgnoreCase(TransactionObject.BUY_TYPE)) {
			spnTradeType.setSelection(TRADE_TYPE_POSITION_BUY);
		} else if (mType.equalsIgnoreCase(TransactionObject.SELL_TYPE)) {
			spnTradeType.setSelection(TRADE_TYPE_POSITION_SELL);
		} else if (mType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
			spnTradeType.setSelection(TRADE_TYPE_POSITION_DIVIDEND);
		} else if (mType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
			spnTradeType.setSelection(TRADE_TYPE_POSITION_DIVIDEND_SHARES);
		}

		// 2. symbol setup
		String mSymbol = mSo.getSymbol();
		etSymbol.setText(mSymbol);

		// 3. volume setup
		int mVolume = mTransObject.getNumOfShares();
		String sVolume = String.valueOf(mVolume);
		etVolume.setText(sVolume);
		etVolume.setSelection(etVolume.length());

		// 4. date setup
		// 4.0 convert date string format to calendar and show date format
		// string
		int numDateYYYYMMDD = mTransObject.getTradeDate();
		String mDateYYYYMMDD = String.valueOf(numDateYYYYMMDD);
		Calendar mCalendar = MyDateFormat.convertYYYYMMDDToCalendar(mDateYYYYMMDD);
		String mShowDate = MyDateFormat.calendarToDateStringFormater(mCalendar);

		// 4.1 set to object
		datePair = new DatePair();
		datePair.setStrDateYYYYMMDD(mDateYYYYMMDD);
		datePair.setStrDate(mShowDate);

		// 4.2 set to EditText
		etDate.setText(mShowDate);
		etDate.setOnClickListener(this);

		// 5. price setup
		String mPrice = mTransObject.getPrice();
		etPrice.setText(mPrice);

		// 6. fees setup
		String mFee = mTransObject.getFee();
		etFees.setText(mFee);

		// 7. total value setup/ TextWatcher auto calulate for it.
		if (mType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)){
			double dTotal = mTransObject.getTotal();
			String sTotal = String.valueOf(dTotal);
			etTotal.setText(sTotal);
		}

		// 8. portfoli setup
		String mPortName = mPo.getName();
		int positionOfPofolioType = portType.indexOf(mPortName);
		spnPortType.setSelection(positionOfPofolioType);

		// 9. notes setup
		String mNotes = mTransObject.getNotes();
		etNotes.setText(mNotes);
	}

	private StockObject convertToStockObject(Bundle bundle2) {
		Bundle mBundle = bundle2;
		String mSymbol = mBundle.getString(Constants.KEY_SYMBOL);
		String mCompanyName = mBundle.getString(Constants.KEY_COMPANY_NAME);
		String mExch = mBundle.getString(Constants.KEY_EXCH);
		String mType = mBundle.getString(Constants.KEY_TYPE);
		String mTypeDisp = mBundle.getString(Constants.KEY_TYPE_DISP);
		String mExchDisp = mBundle.getString(Constants.KEY_EXCH_DISP);
		so = new StockObject();
		so.setSymbol(mSymbol);
		so.setName(mCompanyName);
		so.setExch(mExch);
		so.setExchDisp(mExchDisp);
		so.setType(mType);
		so.setTypeDisp(mTypeDisp);
		return so;
	}

	private boolean preCheckEditTextAll() {
		boolean isValidVolume = false;
		boolean isValidPrice = false;
		boolean isValidFee = false;
		boolean isValidTotal = false;
		boolean isValidSymbol = false;
		boolean isValidCurrency = true;
		int stockId = 0;

		if (tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
			isValidTotal = preCheckEditText(R.id.et_total);
			if (isValidTotal) 
				isValidSymbol = preCheckEditText(R.id.et_symbol);
			else
				return false;
			
			if (isValidSymbol)
				return true;
			else 
				return false;
			
		} else if (tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE_SHARES)) {
			isValidVolume = preCheckEditText(R.id.et_volume);
			if (isValidVolume) 
				isValidSymbol = preCheckEditText(R.id.et_symbol);
			else
				return false;
			
			if (isValidSymbol)
				isValidTotal = preCheckEditText(R.id.et_total);
			else
				return false;
			
			if (isValidTotal)
				return true;
			else 
				return false;
		} else {

			isValidVolume = preCheckEditText(R.id.et_volume);

			if (isValidVolume) {
				isValidPrice = preCheckEditText(R.id.et_price);
			} else
				return false; 
			
			if (isValidPrice) {
				isValidFee = preCheckEditText(R.id.et_frees_and_commission);
			} else 
				return false;
			
			if (isValidFee) {
				isValidSymbol = preCheckEditText(R.id.et_symbol);
			} else
				return false;
			
			if (isValidSymbol) {
				if (fromId == Constants.FROM_POSITIONS_TAB || fromId == Constants.FROM_FIND_STOCKS 
						|| fromId == Constants.FROM_WATCH_LIST || portId != originalPortId) {
					StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(so.getSymbol());
					if (mSo == null) {
						boolean isInserted = so.insert();
						if (isInserted) {
							so = new StockObject();
							so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(so.getSymbol());
							stockId = so.getId();
						} else
							;
					} else {
						stockId = mSo.getId();
					}
					isValidCurrency = addMoreInfo(portId, stockId, so.getSymbol());
				}
			} else 
				return false;

			if (isValidVolume == true && isValidPrice == true && isValidFee == true && 
					isValidSymbol && isValidCurrency) {
				return true;
			}
		}
		return false;
	}

	private void showSaveTransactionDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(getString(R.string.dialog_title_save_changes));
		dialog.setMessage(getString(R.string.dialog_msg_save_changes));

		dialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				saveToDatabase();
				dialog.dismiss();
				Intent i = new Intent(AddNewTrade.this, MainFragmentActivity.class);
				i.putExtra(Constants.KEY_TAB_POSITION, MainFragmentActivity.TAB_POSITION_POSITIONS-1);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				finish();
				startActivity(i);
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

	private void showUnSaveChangeTransactionDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(getString(R.string.dialog_title_unchanged));
		dialog.setMessage(getString(R.string.dialog_msg_unchanged));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
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

	private void showDeleteTransactionDialog() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);

		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(getString(R.string.dialog_title_delete_transaction));
		dialog.setMessage(getString(R.string.dialog_msg_delete_transaction));

		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				TransactionObject mObject = DbAdapter.getSingleInstance().fetchTransactionByRowId(rowId);
				boolean hasDeleted = mObject.delete();
				if (hasDeleted) {
					WidgetUtils.updateWidget(AddNewTrade.this);
					Toast.makeText(AddNewTrade.this, getResources().getString(R.string.trade_deleted), Toast.LENGTH_SHORT).show();
					finish();
				}
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

	protected void showAlertDialogForDateNotComplete(String msg) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);
		
		dialog.setIcon(typedValue.resourceId);
		dialog.setTitle(getString(R.string.error));
		dialog.setMessage(msg);
		dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
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

	private boolean preCheckEditText(int editTextId) {

		String sValue = "";
		String message = "";

		switch (editTextId) {

		case R.id.et_volume:
			sValue = etVolume.getText().toString();
			message = res.getString(R.string.volume_is_required);
			break;

		case R.id.et_price:
			sValue = etPrice.getText().toString();
			message = res.getString(R.string.price_is_required);
			break;

		case R.id.et_frees_and_commission:
			sValue = etFees.getText().toString();
			// message = "Fee not be empty!";
			break;
		case R.id.et_total:
			sValue = etTotal.getText().toString();
			message = res.getString(R.string.total_amount_is_required);
			break;
		case R.id.et_symbol:
			sValue = etSymbol.getText().toString();
			message = res.getString(R.string.symbol_is_required);
			break;
		}

		int count = sValue.length();
		double dValue = DecimalsConverter.convertToDoubleValueBaseOnLocale(sValue, this);
		
		if (count < 1 && editTextId == R.id.et_symbol) {			
			showAlertDialogForDateNotComplete(message);
			return false;
		} else if (count > 0 && editTextId == R.id.et_symbol) 
			return true;
		
		if (dValue == 0) {
			switch (editTextId) {

			case R.id.et_volume:

				if (tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
					return true;
				} else {
					etVolume.requestFocus();
					showAlertDialogForDateNotComplete(message);
				}
				break;

			case R.id.et_price:
				if (tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)) {
					return true;
				} else {
					etPrice.requestFocus();
					showAlertDialogForDateNotComplete(message);
				}
				break;

			case R.id.et_frees_and_commission:
				// etFees.requestFocus();
				break;
			case R.id.et_total:
				etTotal.requestFocus();
				showAlertDialogForDateNotComplete(message);
				break;
			}

			if (editTextId == R.id.et_frees_and_commission) {
				return true;
			}
			return false;
		} else if (count == 1) {

			if (sValue.contains(".") || sValue.contains(",")) {

				switch (editTextId) {

				case R.id.et_volume:
					message = res.getString(R.string.volume_is_invalid);
					showAlertDialogForDateNotComplete(message);
					etVolume.requestFocus();
					etVolume.selectAll();
					break;

				case R.id.et_price:
					message = res.getString(R.string.price_is_invalid);
					showAlertDialogForDateNotComplete(message);
					etPrice.requestFocus();
					etPrice.selectAll();
					break;

				case R.id.et_frees_and_commission:
					message = res.getString(R.string.fees_and_commission_is_invalid);
					showAlertDialogForDateNotComplete(message);
					etFees.requestFocus();
					etFees.selectAll();
					break;

				case R.id.et_total:
					message = res.getString(R.string.total_amount_is_invalid);
					showAlertDialogForDateNotComplete(message);
					etTotal.requestFocus();
					etTotal.selectAll();
					break;
				}
				return false;
			} else if (editTextId == R.id.et_volume) {

				String sVolume = etVolume.getText().toString();
				int mVoume = Integer.valueOf(sVolume);
				if (mVoume == 0) {
					etVolume.requestFocus();
					etVolume.selectAll();
					showAlertDialogForDateNotComplete(message);
					return false;
				}
			}
		} else if (count > 1) {
			String subString = sValue.substring(sValue.length() - 1, sValue.length());
			if (subString.contains(".") || subString.contains(",")) {
				String subPrice = sValue.substring(0, sValue.length() - 1);

				switch (editTextId) {

				case R.id.et_volume:
					etVolume.setText(subPrice);
					etVolume.setSelection(etVolume.length());
					break;

				case R.id.et_price:
					etPrice.setText(subPrice);
					etPrice.setSelection(etPrice.length());
					break;

				case R.id.et_frees_and_commission:
					etFees.setText(subPrice);
					etFees.setSelection(etFees.length());
					break;

				case R.id.et_total:
					etTotal.setText(subPrice);
					etTotal.setSelection(etTotal.length());
					break;
				}
				return true;
			}
		}
		return true;
	}

	protected void saveToDatabase() {

		int mStockId = 0;
		String mSymbol= so.getSymbol();

		switch (fromId) {

		case Constants.FROM_POSITIONS_TAB:
		case Constants.FROM_FIND_STOCKS:
			mSymbol = so.getSymbol();
			StockObject mSo = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
			if (mSo == null) {
				so.insert();
				so = new StockObject();
				so = DbAdapter.getSingleInstance().fetchStockObjectBySymbol(mSymbol);
				mStockId = so.getId();
			} else {
				mStockId = mSo.getId();
			}
			
			alarmManager.addNewsAlert(mSymbol);

			break;

		case Constants.FROM_PORTFOLIO_LIST:
			mStockId = so.getId();
			break;

		case Constants.FROM_WATCH_LIST:
			mStockId = so.getId();
			break;

		case Constants.FROM_EDIT_TRANSACTION:
			mStockId = transObject.getStockId();
			break;
		}
		

		String price = "0.00";
		int volume = 0;
		String fees = "0.00";
		String tradeDate = "";
		String notes = "";

		// 1. get volume:
		String sVolume = etVolume.getText().toString();
		if (sVolume.length() > 0) {
			volume = Integer.valueOf(sVolume);
		}

		// 2. get price:
		String sPrice = etPrice.getText().toString();
		if (sPrice.length() > 0) {
			String subPrice = sPrice.substring(sPrice.length() - 1, sPrice.length());
			if (subPrice.contains(".") || subPrice.contains(",")) {
				sPrice = sPrice.substring(0, sPrice.length() - 1);
			}
			if (!(sPrice.contains("."))) {

				int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;
				double mdPrice = Double.parseDouble(sPrice);
				String msPrice = DecimalsConverter.convertToStringValue(Locale.US, mdPrice, numberOfDecimal);
				sPrice = msPrice;
			}
			price = sPrice;
		}

		// 3. get fees:
		String sFees = etFees.getText().toString();
		if (sFees.length() > 0) {
			String subFees = sPrice.substring(sFees.length() - 1, sFees.length());
			if (subFees.contains(".") || subFees.contains(",")) {
				sFees = sFees.substring(0, sFees.length() - 1);
			}
			if (!(sFees.contains("."))) {

				int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;
				double mdFees = Double.parseDouble(sFees);
				String msFees = DecimalsConverter.convertToStringValue(Locale.US, mdFees, numberOfDecimal);
				sFees = msFees;
			}
			fees = sFees;
		}

		tradeDate = datePair.getStrDateYYYYMMDD();
		int numberTradeDate = Integer.parseInt(tradeDate);

		notes = etNotes.getText().toString();

		String sTotal = etTotal.getText().toString();
		double dTotal = Double.parseDouble(sTotal);

		if(tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE)){
			transObject.setPortId(portId);
			transObject.setStockId(mStockId);
			transObject.setType(tradeType);
			transObject.setPrice("");
			transObject.setNumOfShares(0);
			transObject.setFee("");
			transObject.setTradeDate(numberTradeDate);
			transObject.setTotal(dTotal);
			transObject.setNotes(notes);
		}else{
			transObject.setPortId(portId);
			transObject.setStockId(mStockId);
			transObject.setType(tradeType);
			transObject.setPrice(price);
			transObject.setNumOfShares(volume);
			transObject.setFee(fees);
			transObject.setTradeDate(numberTradeDate);
			transObject.setTotal(dTotal);
			transObject.setNotes(notes);
		}
		

		boolean isAdded = false;		

		if (rowId > -1) {
			isAdded = transObject.update();
		} else {
			isAdded = transObject.insert();
		}
		

		if (isAdded) {
			if (rowId > -1) {
				Toast.makeText(this, getResources().getString(R.string.trade_updated), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getResources().getString(R.string.trade_added), Toast.LENGTH_SHORT).show();
			}
			sendBroadcastNotifyDatasetChange(portId, mStockId, fromId);
		}
	}

	private void sendBroadcastNotifyDatasetChange(int mPortId, int stockId2, int mFromId) {

		// 1. If the intent from 'Trades Tab', update trade list fragment
		Intent iTradeTabList = new Intent(Constants.ACTION_TRADES_FRAGMENT);
		iTradeTabList.putExtra(Constants.KEY_STOCK_ID, stockId2);
		sendBroadcast(iTradeTabList);

		// 2. Update single portfolio page indicator.
		// In case portfolio name switch to another, to update single portfolio
		// listview.
		PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(mPortId);
		String mPortName = po.getName();
		Intent iSinglePortPageIndicator = new Intent(Constants.ACTION_PORTFOLIO_INITIAL_PAGER_ACTIVITY);
		iSinglePortPageIndicator.putExtra(Constants.KEY_PORTFOLIO_NAME, mPortName);
		sendBroadcast(iSinglePortPageIndicator);

		// 3. Update single portfolio listview include receiver
		// in case orientation change receiver not updated.
		Intent iSinglePortList = new Intent(portName);
		iSinglePortList.putExtra(Constants.KEY_PORTFOLIO_NAME, mPortName);
		sendBroadcast(iSinglePortList);

		// 4. Update Widget:
		WidgetUtils.updateWidget(AddNewTrade.this);
		
		// 5. Update Positions Fragment
		Intent iPosFrag = new Intent(Constants.ACTION_PORTFOLIO_LIST_FRAGMENT);
		iPosFrag.putExtra(Constants.KEY_UPDATE_PORTFOLIOS_PAGER, true);
		sendBroadcast(iPosFrag);

	}

	private boolean addMoreInfo(int mPortId, int mStockId, String symbol) {
		
		// 2. Add QuoteObject/Check Currency
		String currency = "";	
		boolean isAdded = false;
		QuoteObject qo = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(symbol);
		if (qo == null) {
			UpdateQuoteTaskSingleSymbol updateQuote = new UpdateQuoteTaskSingleSymbol(this, null, portId, mStockId);
			updateQuote.execute(symbol);
		} else {
			currency = qo.getCurrency();
		
			// 3. Add PortfolioStockObject
			PortfolioStockObject ps = DbAdapter.getSingleInstance().fetchPortStockObjectByPortIdAndStockId(portId, mStockId);
			PortfolioObject po = DbAdapter.getSingleInstance().fetchPortfolioByPortId(portId);
			if (ps == null) {
	
				ps = new PortfolioStockObject();
				ps.setPortfolioId(portId);
				ps.setStockId(mStockId);	
				
				List<PortfolioStockObject> pso = DbAdapter.getSingleInstance().fetchPortStockListByPortId(portId);
				if (!pso.isEmpty()) {
					if (currency.equals(po.getCurrencyType())) {
						ps.insert();
						isAdded = true;
					} else {
						// return error
						Log.i("wrong currency", po.getCurrencyType()+":"+currency);
						
						WrongCurrencyDialog currDialog = new WrongCurrencyDialog();     
						currDialog.show(getSupportFragmentManager(), Constants.DIALOG_WRONG_CURRENCY);
					}
				} else {
					ps.insert();
					po.setCurrencyType(currency);
					po.update();	
					isAdded = true;
				}
			} else
				isAdded = true;
		}
		
		return isAdded;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.et_dt:
			myDatePicker();
			break;
		case R.id.et_symbol:
		case R.id.ib_search:
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.KEY_FROM, Constants.FROM_ADD_TRADE);
			bundle.putBoolean(Constants.KEY_ADD_STOCK_IN_SINGLE_PORTFOLIO, true);
			bundle.putBoolean(Constants.KEY_ADD_STOCK_IN_SINGLE_WATCHLIST, false);
			bundle.putString(Constants.KEY_PORTFOLIO_NAME, portName);
			bundle.putInt(Constants.KEY_PORTFOLIO_ID, portId);
			Intent i = new Intent(AddNewTrade.this, FindStocksDialogActivityPort.class);
			i.putExtras(bundle);
			i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivityForResult(i, Constants.FROM_ADD_TRADE);
			break;
		case R.id.et_volume:
			etId = R.id.et_volume;
			showCalcDialog();
			break;
		case R.id.et_price:
			etId = R.id.et_price;
			showCalcDialog();
			break;
		case R.id.et_frees_and_commission:
			etId = R.id.et_frees_and_commission;
			showCalcDialog();
			break;
		case R.id.et_total:
			etId = R.id.et_total;
			showCalcDialog();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == Constants.FROM_ADD_TRADE) {
	        if (resultCode == RESULT_OK) {
	        	Bundle intentBundle = data.getExtras();
	            so = new StockObject();
	            String symbol = intentBundle.getString(Constants.KEY_SYMBOL);
	    		String companyName = intentBundle.getString(Constants.KEY_COMPANY_NAME);
	    		String exch = intentBundle.getString(Constants.KEY_EXCH);
	    		String type = intentBundle.getString(Constants.KEY_TYPE);
	    		String typeDisp = intentBundle.getString(Constants.KEY_TYPE_DISP);
	    		String exchDisp = intentBundle.getString(Constants.KEY_EXCH_DISP);

	    		so.setSymbol(symbol);
	    		so.setName(companyName);
	    		so.setExch(exch);
	    		so.setExchDisp(exchDisp);
	    		so.setType(type);
	    		so.setTypeDisp(typeDisp);
	        }
	    }
	}
	
	private void showCalcDialog() {
		CalculatorDialogFragment dialog = new CalculatorDialogFragment();
		dialog.show(getSupportFragmentManager());
	}

	private void myDatePicker() {

		OrientationUtils.lockOrientation(AddNewTrade.this);
				
		/*FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag(TAG_MY_DATE_PICKER_FRAGMENT);

		if (fragment != null) {
			fm.beginTransaction().remove(fragment).commit();
		}

		MyDatePickerFragment myDatePickerFragment = new MyDatePickerFragment();

		String mDate = datePair.getStrDateYYYYMMDD();
		Calendar mCal = MyDateFormat.convertYYYYMMDDToCalendar(mDate);
		myDatePickerFragment.setCalendar(mCal);
		myDatePickerFragment.show(getSupportFragmentManager(), TAG_MY_DATE_PICKER_FRAGMENT);*/
		
		String mDate = datePair.getStrDateYYYYMMDD();
		Calendar mCalendar = MyDateFormat.convertYYYYMMDDToCalendar(mDate);
		
		int year = 0;
		int month = 0;
		int day = 0;

		year = mCalendar.get(Calendar.YEAR);
		month = mCalendar.get(Calendar.MONTH);
		day = mCalendar.get(Calendar.DAY_OF_MONTH);
		
		DatePickerDialog dialog = new DatePickerDialog(); 
		dialog.setDate(year, month, day);
		dialog.setOnDateSetListener(this);
		dialog.show(getSupportFragmentManager());
	}
	
	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		final Calendar c2 = Calendar.getInstance();
		c2.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

		String strDateYYYYMMDD = MyDateFormat.convertCalendarToYYYYMMDD(c2);
		String strDateFormat = MyDateFormat.calendarToDateStringFormater(c2);

		datePair.setStrDateYYYYMMDD(strDateYYYYMMDD);// using to save
														// database;
		datePair.setStrDate(strDateFormat);// using to display on EditText;

		etDate.setText(datePair.getStrDate());

		OrientationUtils.unlockOrientation(AddNewTrade.this);
	}

	
	public void btnOnClick(View view) {
		int mID = view.getId();

		switch (mID) {
		case R.id.btn_save:
			boolean isOkToSave = preCheckEditTextAll();
			if (isOkToSave) {
				showSaveTransactionDialog();
			}
			break;
		case R.id.btn_cancel:
			showUnSaveChangeTransactionDialog();
			break;
		}
	}

	public class DatePair {

		String strDateYYYYMMDD = "";
		String strDate = "";

		public String getStrDateYYYYMMDD() {
			return strDateYYYYMMDD;
		}

		public void setStrDateYYYYMMDD(String strDateYYYYMMDD) {
			this.strDateYYYYMMDD = strDateYYYYMMDD;
		}

		public String getStrDate() {
			return strDate;
		}

		public void setStrDate(String strDate) {
			this.strDate = strDate;
		}
	}

	public class PriceTextWatcher implements TextWatcher {

		int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;

		@Override
		public void afterTextChanged(Editable s) {

			String strPrice = s.toString();
			String strVolume = etVolume.getText().toString().trim();
			String strFees = etFees.getText().toString().trim();
			if(!(tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE))){
				calculateResult(strVolume, strPrice, strFees);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}

	public class VolumeTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {

			String strVolume = s.toString().trim();
			String strPrice = etPrice.getText().toString().trim();
			String strFees = etFees.getText().toString().trim();
			if(!(tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE))){
				calculateResult(strVolume, strPrice, strFees);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}

	public class FeesTextWatcher implements TextWatcher {

		int numberOfDecimal = 2;

		@Override
		public void afterTextChanged(Editable s) {

			String strFees = s.toString().trim();
			String strVolume = etVolume.getText().toString().trim();
			String strPrice = etPrice.getText().toString().trim();
			
			if(!(tradeType.equalsIgnoreCase(TransactionObject.DIVIDEND_TYPE))){
				calculateResult(strVolume, strPrice, strFees);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}

	public void calculateResult(String strVolume, String strPrice, String strFees) {

		int countV = strVolume.length();
		int countP = strPrice.length();
		int countFee = strFees.length();

		if (countP == 1 && strPrice.contains(".")) {
			strPrice = "0.";
			etPrice.setText(strPrice);
			etPrice.setSelection(etPrice.length());
		}

		if (countFee == 1 && strFees.contains(".")) {
			strFees = "0.";
			etFees.setText(strFees);
			etFees.setSelection(etFees.length());
		}

		long volume = 0;
		double dFees = 0;
		double dPrice = 0;
		double altogether;

		// 1. get volume(integer)
		if (countV == 0) {
			volume = 0;
		} else {
			if (strVolume.contains("."))
				strVolume = strVolume.substring(0, strVolume.indexOf("."));
			volume = Long.valueOf(strVolume);
		}

		// 2. get price(double)
		if (countP == 0) {
			dPrice = 0;
		} else {
			String subPrice = strPrice.substring(strPrice.length() - 1, strPrice.length());
			String mSubPrice = subPrice.replaceAll(" ", "");
			if (mSubPrice.contains(".") || mSubPrice.contains(",")) {
				strPrice = strPrice.substring(0, strPrice.length() - 1);
			}
			dPrice = Double.parseDouble(strPrice);
		}

		// 3. get fees
		if (countFee > 0) {
			String subFees = strFees.substring(strFees.length() - 1, strFees.length());
			String mSubFees = subFees.replaceAll(" ", "");
			if (mSubFees.contains(".") || mSubFees.contains(",")) {
				strFees = strFees.substring(0, strFees.length() - 1);
			}
			dFees = Double.parseDouble(strFees);
		} else {
			dFees = 0;
		}

		altogether = volume * dPrice + dFees;
		int numberOfDecimal = Constants.NUMBER_OF_DECIMALS;
		String sTotal = DecimalsConverter.convertToStringValue(Locale.US, altogether, numberOfDecimal);
		etTotal.setText(sTotal);
	}
	
	@Override
	public void OnResult(int type, String amount) {
		
		double dAmt = 0;
		String sAmt = "0";
		try {
			dAmt = Double.parseDouble(amount);
			sAmt = DecimalsConverter.convertToStringValueNoCurrencyBasedOnLocale(dAmt, Constants.NUMBER_OF_DECIMALS, this);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		switch (etId) {
		case R.id.et_price:
			
			etPrice.setText(sAmt);
			break;
		case R.id.et_volume:
			if (amount.contains("."))
				amount = amount.substring(0, amount.indexOf("."));
			etVolume.setText(amount);
			break;
		case R.id.et_total:
			etTotal.setText(sAmt);
			break;
		case R.id.et_frees_and_commission:
			etFees.setText(sAmt);
		}
		
	}

	@Override
	public void OnResult(String amount) {
			
	}

	public class DataSet {
		String lastTradePrice = "";
		String changes = "";
		String changeInPercent = "";

		public String getLastTradePrice() {
			return lastTradePrice;
		}

		public void setLastTradePrice(String lastTradePrice) {
			this.lastTradePrice = lastTradePrice;
		}

		public String getChanges() {
			return changes;
		}

		public void setChanges(String changes) {
			this.changes = changes;
		}

		public String getChangeInPercent() {
			return changeInPercent;
		}

		public void setChangeInPercent(String changeInPercent) {
			this.changeInPercent = changeInPercent;
		}
	}

	private class AddTradeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.ACTION_ADD_NEW_TRADE)) {

				Bundle mBundle = intent.getExtras();
				boolean isStartQuoteService = false;
				boolean isQuoteOk = false;
				
				// hide keyboard
				InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

				// This is return by find stocks:
				if (mBundle != null) {

					// 1. set symbol to ui:
					String symbol = mBundle.getString(Constants.KEY_SYMBOL);
					String currency = mBundle.getString(Constants.KEY_CURRENCY);
					if (symbol != null && symbol.length() > 0) {
						etSymbol.setText(symbol);
						so.setSymbol(symbol);
					}
					if (currency != null && currency.length() > 0) {
						so.setCurrency(currency);
					}
					

					// 2. start update
					// 2.1 required for update current price by find stock
					// result.
					// 2.2 if quote current price success, set price to edittext
					// box
					// 2.2 if quote current price fail, show alert dialog
					isStartQuoteService = mBundle.getBoolean(Constants.KEY_IS_START_QUOTE_SERVICE);
					isQuoteOk = mBundle.getBoolean(Constants.KEY_IS_QUOTE_OK);
					if (isStartQuoteService) {
						startQuote();						
						pd.setVisibility(View.VISIBLE);
					} else if (isStartQuoteService == false && isQuoteOk == true) {
						if (pd != null) {
							pd.setVisibility(View.GONE);
						}
						QuoteObject mQuote = DbAdapter.getSingleInstance().fetchQuoteObjectBySymbol(etSymbol.getText().toString());
						if (mQuote != null) {
							String mLastTradePrice = mQuote.getLastTradePrice();
							etPrice.setText(mLastTradePrice);
						}
					} else if (isStartQuoteService == false && isQuoteOk == false) {
						if (pd != null) {
							pd.setVisibility(View.GONE);
						}
						boolean isServerDown = sp.getBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false);
						if (isServerDown) {
							alertDialog();
						}
					}
				}
			}
		}
	}
	
	private void startQuote() {
		UpdateQuoteTaskSingleSymbol task = new UpdateQuoteTaskSingleSymbol(AddNewTrade.this, null, 0, 0);
		task.execute(etSymbol.getText().toString().trim());
	}
	
	private void alertDialog() {

		// reset
		sp.edit().putBoolean(Constants.SP_KEY_IS_YAHOO_SERVICE_DOWN, false).commit();

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.warning_icon, typedValue, true);
		
		dialog.setIcon(typedValue.resourceId);
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